import { currentSession, onSessionChanged, request } from '../../services/http';
import type { DriverOrderView } from '../../api/types';
import { intentKey } from '../file/api';
import { getSession } from '../identity/api';

export type InviteStatus = 'WAIT_RESPONSE' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'REVOKED';
export type DriverInvite = {
  inviteId: string;
  orderNo: string;
  driverId: string;
  driverName: string;
  assignmentId: string | null;
  batchId: string | null;
  source: 'AUTO' | 'MANUAL';
  status: InviteStatus;
  storedStatus: InviteStatus;
  version: number;
  orderVersion: number;
  serviceCompanyId: string | null;
  serviceCompanyName: string | null;
  stationId: string;
  stationName: string;
  pickupArea: string;
  pickupSlotStartAt: string;
  pickupSlotEndAt: string;
  vehicleTypeCode: string;
  vehicleTypeName: string;
  vehiclePlateMasked: string | null;
  distanceMeter: number | null;
  distanceMode: 'GCJ02_GREAT_CIRCLE';
  issuedAt: string;
  expiresAt: string;
  overallDeadlineAt: string | null;
  respondedAt: string | null;
  sessionId: string | null;
  contextVersion: number | null;
  acceptBlockReason: string | null;
  allowedActions: string[];
};
export type InviteDecision = 'ACCEPT' | 'REJECT';
export type RespondInvite = {
  decision: InviteDecision;
  expectedVersion: number;
  expectedOrderVersion: number;
  expectedSessionId: string;
  expectedContextVersion: number;
  reason: string | null;
};
export type InviteIntent = {
  inviteId: string;
  body: RespondInvite;
  key: string;
};
async function securedRequest<T>(
  method: 'GET' | 'POST',
  path: string,
  data?: object,
  key?: string
): Promise<T> {
  try {
    return await request<T>(method, path, data, key);
  } catch (failure) {
    if ([401, 403, 404].includes((failure as { status?: number }).status || 0)) intents.clear();
    throw failure;
  }
}
const base = '/api/app/driver';
export function inviteId(value: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(value) || (value.length === 19 && value > '9223372036854775807'))
    throw new Error('邀约记录编号无效');
  return value;
}
export function inviteScope() {
  const session = currentSession();
  if (!session || session.activeRole !== 'DRIVER') throw new Error('请先切换到已开放的代驾身份');
  return [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(
    ':'
  );
}
let owner = '';
const intents = new Map<string, InviteIntent>();
function refreshOwner() {
  let next = '';
  try {
    next = inviteScope();
  } catch {
    /* 退出或切角色时立即清理旧身份意图。 */
  }
  if (owner !== next) {
    intents.clear();
    owner = next;
  }
}
onSessionChanged(refreshOwner);
/** 原响应意图仅内存保留。截止、换页和GET失败不重建正文，账号/上下文变化则立即清理。 */
export function pendingInvite(id: string, value?: InviteIntent | null) {
  refreshOwner();
  if (!owner) return null;
  if (value === null) intents.delete(id);
  else if (value) intents.set(id, value);
  return intents.get(id) || null;
}
export function pendingInvites() {
  refreshOwner();
  return Array.from(intents.values());
}
export async function ensureInviteSession() {
  const before = inviteScope();
  try {
    const session = await getSession();
    const cached = currentSession();
    if (
      inviteScope() !== before ||
      !cached ||
      session.activeRole !== 'DRIVER' ||
      session.accountId !== cached.accountId ||
      session.sessionId !== cached.sessionId ||
      session.contextVersion !== cached.contextVersion
    )
      throw Object.assign(new Error('工作身份已变化，请重新核对代驾身份'), {
        status: 403
      });
    return session;
  } catch (failure) {
    if ([401, 403, 404].includes((failure as { status?: number }).status || 0)) intents.clear();
    throw failure;
  }
}
function invalid(message: string): never {
  throw Object.assign(new Error(message), { invalidProjection: true });
}
function validate(value: DriverInvite): DriverInvite {
  const session = currentSession();
  if (
    !value ||
    !session ||
    session.activeRole !== 'DRIVER' ||
    value.sessionId !== session.sessionId ||
    value.contextVersion !== session.contextVersion ||
    !Array.isArray(value.allowedActions) ||
    !Number.isSafeInteger(value.version) ||
    !Number.isSafeInteger(value.orderVersion) ||
    value.distanceMode !== 'GCJ02_GREAT_CIRCLE' ||
    !Number.isFinite(Date.parse(value.expiresAt))
  )
    invalid('邀约响应与当前身份或截止信息不符，请重新核对');
  inviteId(value.inviteId);
  return value;
}
export async function listInvites(pageNum = 1, status = '', orderNo = '') {
  inviteScope();
  const value = await securedRequest<{
    code: number;
    rows: DriverInvite[];
    total: number;
  }>('GET', base + '/invites', {
    pageNum,
    pageSize: 10,
    ...(status ? { status } : {}),
    ...(orderNo.trim() ? { orderNo: orderNo.trim() } : {})
  });
  if (value.code !== 200 || !Array.isArray(value.rows) || !Number.isSafeInteger(value.total))
    invalid('本人邀约列表尚未确认，请重新读取');
  value.rows.forEach(validate);
  return value;
}
export async function getInvite(id: string) {
  inviteScope();
  const value = await securedRequest<{ code: number; data: DriverInvite }>(
    'GET',
    base + '/invites/' + inviteId(id)
  );
  if (value.code !== 200 || value.data?.inviteId !== id) invalid('邀约目标响应尚未确认');
  return validate(value.data);
}
export function createInviteIntent(
  value: DriverInvite,
  decision: InviteDecision,
  reason: string
): InviteIntent {
  validate(value);
  const text = reason.trim();
  if (text.length > 500) throw new Error('响应说明最多500字');
  return {
    inviteId: value.inviteId,
    key: intentKey(),
    body: {
      decision,
      expectedVersion: value.version,
      expectedOrderVersion: value.orderVersion,
      expectedSessionId: value.sessionId!,
      expectedContextVersion: value.contextVersion!,
      reason: text || null
    }
  };
}
export async function runInviteIntent(intent: InviteIntent) {
  const scope = inviteScope();
  pendingInvite(intent.inviteId, intent);
  try {
    const value = await securedRequest<{ code: number; data: DriverInvite }>(
      'POST',
      base + '/invites/' + inviteId(intent.inviteId) + '/response',
      intent.body,
      intent.key
    );
    if (inviteScope() !== scope) invalid('身份已变化，原响应已丢弃');
    if (value.code !== 200 || value.data?.inviteId !== intent.inviteId)
      invalid('原邀约响应尚未确认');
    const result = validate(value.data);
    pendingInvite(intent.inviteId, null);
    return result;
  } catch (failure) {
    const status = (failure as { status?: number }).status;
    if (status !== undefined && status < 500) pendingInvite(intent.inviteId, null);
    throw failure;
  }
}
export async function getCurrentAssignment() {
  inviteScope();
  const value = await securedRequest<{
    code: number;
    data: DriverOrderView | null;
  }>('GET', base + '/assignments/current');
  if (value.code !== 200) invalid('当前承接尚未确认，请重新读取');
  if (
    value.data &&
    (value.data.view !== 'DRIVER' ||
      value.data.accessPhase !== 'ACTIVE_ASSIGNMENT' ||
      !value.data.assignmentId ||
      !Array.isArray(value.data.allowedActions))
  )
    invalid('当前承接投影不完整，请重新核对');
  return value.data;
}
export function inviteLabel(value: string | null | undefined) {
  if (!value) return '暂无';
  const labels: Record<string, string> = {
    WAIT_RESPONSE: '等待本人响应',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    EXPIRED: '已过期',
    REVOKED: '已撤回',
    AUTO: '自动轮转邀约',
    MANUAL: '人工邀约',
    ACCEPT: '接受',
    REJECT: '拒绝',
    OWNER: '车主保管',
    DRIVER: '代驾保管',
    STATION: '站点保管',
    ACCEPTED_ORDER: '已建立承接',
    QUALIFICATION_NOT_APPROVED: '当前资格未批准',
    QUALIFICATION_COMPANY_CHANGED: '资格公司已变化',
    NEW_ASSIGNMENTS_DISABLED: '公司已暂停新邀约',
    LISTENING_DISABLED: '当前未开启听单',
    LISTENING_EXPIRED_OR_CONTEXT_CHANGED: '听单已到期或上下文变化',
    LICENSE_EXPIRED_OR_UNKNOWN: '驾驶证到期或期限未确认',
    REVIEW_EXPIRED_OR_UNKNOWN: '复审到期或截止未确认',
    ACTIVE_PENALTY: '存在生效限制',
    OCCUPIED: '已有其他占用',
    CLIENT_STALE: '近期联系已过期',
    LOCATION_STALE: '位置已过期',
    ORDER_NOT_DISPATCHABLE: '订单当前不可派单'
  };
  return labels[value] || value;
}
export const inviteTime = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
      })
    : '暂无';
export const straightDistance = (value: number | null) =>
  value === null ? '尚未确认' : value >= 1000 ? (value / 1000).toFixed(2) + '公里' : value + '米';
