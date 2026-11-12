import { currentSession, onSessionChanged, request } from '../../services/http';
import { intentKey } from '../file/api';
import type { ForegroundPoint } from './location';

export type Availability = {
  driverId: string;
  ownerDeptId: string | null;
  companyName: string | null;
  companyRevision: number;
  businessDate: string;
  sessionId: string;
  contextVersion: number;
  newAssignmentEnabled: boolean;
  listeningIntentEnabled: boolean;
  listeningEnabled: boolean;
  version: number;
  listeningVersion: number;
  profileVersion: number;
  completedAssignmentCount: number;
  listeningUntil: string | null;
  listeningCompanyRevision: number | null;
  listeningConfigVersion: number | null;
  configVersion: number | null;
  configurationReady: boolean;
  closingTime: string | null;
  locationReportIntervalSeconds: number | null;
  qualificationValid: boolean;
  qualificationReason: string | null;
  occupationType: 'NONE' | 'INVITE' | 'ASSIGNMENT';
  holderId: string | null;
  occupied: boolean;
  activePenaltyIds: string[];
  locationVersion: number;
  locationStatus: string;
  locationFresh: boolean;
  locationAccuracyValid: boolean;
  locationCapturedAt: string | null;
  locationReceivedAt: string | null;
  locationExpiresAt: string | null;
  accuracyMeter: number | null;
  presenceVersion: number;
  clientState: 'FOREGROUND' | 'BACKGROUND' | null;
  reachabilityStatus: string;
  clientReachable: boolean;
  lastContactAt: string | null;
  reachabilityExpiresAt: string | null;
  availableForNewOrder: boolean;
  eligibilityScope: 'BASE_CANDIDATE_ONLY';
  reason: string;
  allowedActions: string[];
};
export type Observations = {
  expectedCompanyRevision: number;
  expectedSessionId: string;
  expectedContextVersion: number;
};
export type ListeningInput = Observations & {
  enabled: boolean;
  expectedVersion: number;
  expectedConfigVersion: number | null;
  expectedBusinessDate: string;
};
export type LocationInput = Observations & ForegroundPoint & { expectedVersion: number };
export type PresenceInput = Observations & {
  clientState: 'FOREGROUND' | 'BACKGROUND';
  observedAt: string;
  expectedVersion: number;
};
export type DispatchIntent =
  | { action: 'LISTENING'; body: ListeningInput; key: string }
  | { action: 'LOCATION'; body: LocationInput; key: string }
  | { action: 'PRESENCE'; body: PresenceInput; key: string };
type Ack = { accepted: boolean; requestId: string; version: number };
const base = '/api/app/driver';

export function dispatchScope() {
  const session = currentSession();
  if (!session || session.activeRole !== 'DRIVER') throw new Error('请先切换到已开放的代驾身份');
  return [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(
    ':'
  );
}
let scope = '';
const pending = new Map<DispatchIntent['action'], DispatchIntent>();
onSessionChanged(() => {
  let next = '';
  try {
    next = dispatchScope();
  } catch {
    /* 退出或切回车主时清理原位置及请求体。 */
  }
  if (scope !== next) {
    pending.clear();
    scope = next;
  }
});
/** 未知写入只在本人当前身份内保留原体和UUID；坐标不进入本地存储、路由或日志。 */
export function pendingDispatch(action: DispatchIntent['action'], intent?: DispatchIntent | null) {
  let next = '';
  try {
    next = dispatchScope();
  } catch {
    pending.clear();
    scope = '';
    return null;
  }
  if (scope !== next) {
    pending.clear();
    scope = next;
  }
  if (intent === null) pending.delete(action);
  else if (intent) pending.set(action, intent);
  return pending.get(action) || null;
}
export function observations(value: Availability): Observations {
  return {
    expectedCompanyRevision: value.companyRevision,
    expectedSessionId: value.sessionId,
    expectedContextVersion: value.contextVersion
  };
}
function validate(value: Availability) {
  const session = currentSession();
  if (
    !value ||
    value.eligibilityScope !== 'BASE_CANDIDATE_ONLY' ||
    !Array.isArray(value.allowedActions) ||
    !session ||
    value.sessionId !== session.sessionId ||
    value.contextVersion !== session.contextVersion ||
    ![
      value.listeningVersion,
      value.locationVersion,
      value.presenceVersion,
      value.completedAssignmentCount
    ].every((version) => Number.isSafeInteger(version) && version >= 0)
  )
    throw Object.assign(new Error('听单状态响应与当前身份不符或服务计数未取得，请重新核对'), {
      invalidProjection: true
    });
  return value;
}
export async function getAvailability() {
  dispatchScope();
  const response = await request<{ code: number; data: Availability }>(
    'GET',
    base + '/availability'
  );
  if (response.code !== 200) throw new Error('听单状态尚未确认，请重新读取');
  return validate(response.data);
}
export function newDispatchIntent<T extends DispatchIntent>(value: Omit<T, 'key'>): T {
  return { ...value, key: intentKey() } as T;
}
export function unknownDispatch(failure: unknown) {
  const status = (failure as { status?: number }).status;
  return status === undefined || status >= 500;
}
/** 三种写入版本互相独立；回放不重新取点、改时间或换键，以免延长旧定位/可达租约。 */
export async function runDispatchIntent(intent: DispatchIntent): Promise<Availability | Ack> {
  const owner = dispatchScope();
  pendingDispatch(intent.action, intent);
  try {
    if (intent.action === 'LISTENING') {
      const response = await request<{ code: number; data: Availability }>(
        'PATCH',
        base + '/listening',
        intent.body,
        intent.key
      );
      if (dispatchScope() !== owner) throw new Error('代驾身份已变化，原响应已丢弃');
      if (response.code !== 200) throw new Error('听单操作结果尚未确认');
      const value = validate(response.data);
      pendingDispatch(intent.action, null);
      return value;
    }
    const response = await request<{ code: number; data: Ack }>(
      'POST',
      base + (intent.action === 'LOCATION' ? '/location' : '/presence'),
      intent.body,
      intent.key
    );
    if (dispatchScope() !== owner) throw new Error('代驾身份已变化，原响应已丢弃');
    if (
      response.code !== 200 ||
      !response.data?.accepted ||
      response.data.requestId !== intent.key ||
      !Number.isSafeInteger(response.data.version)
    )
      throw new Error('上报回执尚未确认，请保留原请求核对');
    pendingDispatch(intent.action, null);
    return response.data;
  } catch (failure) {
    if (!unknownDispatch(failure)) pendingDispatch(intent.action, null);
    throw failure;
  }
}
export function dispatchLabel(value: string | null | undefined) {
  if (!value) return '暂无';
  const labels: Record<string, string> = {
    NONE: '当前无占用',
    INVITE: '待响应邀约占用',
    ASSIGNMENT: '在手承接占用',
    MISSING: '尚无位置',
    UNTRUSTED: '位置来源尚未可信',
    CONTEXT_CHANGED: '身份或公司上下文已变化',
    STALE: '已超过有效期',
    INACCURATE: '精度不足',
    UNKNOWN_ACCURACY: '精度未知',
    FRESH: '位置仍在有效期',
    RECENT_CONTACT: '近期认证交互有效',
    UNKNOWN: '尚无有效联系记录',
    FOREGROUND: '最近报告在前台',
    BACKGROUND: '最近报告在后台',
    BASE_READY: '基础候选条件满足',
    LISTENING_DISABLED: '未开启听单',
    LISTENING_EXPIRED: '已到公司每日下班时间',
    CONFIGURATION_NOT_READY: '公司听单配置未就绪',
    DRIVER_OCCUPIED: '已有邀约或承接占用',
    LOCATION_STALE: '位置已过期',
    LOCATION_MISSING: '尚无有效位置',
    CLIENT_UNREACHABLE: '近期可达凭据不足',
    NEW_ASSIGNMENTS_DISABLED: '公司已暂停新邀约',
    QUALIFICATION_INVALID: '资格当前不符合新单条件'
  };
  Object.assign(labels, {
    DRIVER_COMPANY_UNASSIGNED: '尚未认领公司',
    DRIVER_COMPANY_UNAVAILABLE: '归属公司当前不可用',
    LISTENING_CONFIG_NOT_READY: '公司听单配置未就绪',
    LISTENING_EXPIRED_OR_CONTEXT_CHANGED: '听单已到期或公司上下文已变化',
    OCCUPIED: '已有邀约或在手承接占用',
    ACTIVE_PENALTY: '存在生效中的新单限制',
    CLIENT_STALE: '近期联系已过期',
    CLIENT_UNKNOWN: '尚无有效联系记录',
    CLIENT_CONTEXT_CHANGED: '联系记录来自旧身份上下文',
    LOCATION_UNTRUSTED: '位置来源尚未可信',
    LOCATION_CONTEXT_CHANGED: '位置来自旧身份或公司上下文',
    LOCATION_INACCURATE: '位置精度不足',
    LOCATION_UNKNOWN_ACCURACY: '位置精度未知',
    QUALIFICATION_SNAPSHOT_UNTRUSTED: '资格快照尚未可信',
    ACCOUNT_UNAVAILABLE: '账号当前不可用',
    QUALIFICATION_NOT_APPROVED: '当前没有已批准资格',
    QUALIFICATION_COMPANY_CHANGED: '资格所属公司已变化',
    LICENSE_CLASS_UNKNOWN: '准驾车型尚未明确',
    LICENSE_EXPIRED_OR_UNKNOWN: '驾驶证已到期或期限不明',
    REVIEW_EXPIRED_OR_UNKNOWN: '复审已到期或截止不明',
    CLIENT_CONTEXT_REQUIRED: '需核对当前代驾身份'
  });
  return labels[value] || value;
}
export const dispatchTime = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    : '暂无';
