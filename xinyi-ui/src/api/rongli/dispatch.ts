import { viGet, viPost, viPut } from './request';
import { validId } from './aftersale';
export type InviteStatus = 'WAIT_RESPONSE' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'REVOKED';
export interface DispatchInvite {
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
}
export interface DispatchQueueItem {
  orderNo: string;
  orderVersion: number;
  serviceCompanyId: string | null;
  serviceCompanyName: string | null;
  stationId: string;
  stationName: string;
  fulfillmentStatus: string;
  cancelStatus: string;
  pickupSlotStartAt: string;
  pickupSlotEndAt: string;
  pickupArea: string;
  batchId: string | null;
  batchStatus: string | null;
  batchVersion: number | null;
  policyVersion: number | null;
  overallDeadlineAt: string | null;
  radiusStage: number | null;
  activeInviteId: string | null;
  reasonCode: string | null;
  allowedActions: string[];
}
export interface DispatchCandidate {
  driverId: string;
  driverName: string;
  phoneMask: string | null;
  driverVersion: number;
  serviceCompanyId: string;
  licenseClass: string;
  distanceMeter: number;
  distanceMode: 'GCJ02_GREAT_CIRCLE';
  eligibilityExpiresAt: string;
  locationCapturedAt: string;
  locationExpiresAt: string;
  reachabilityExpiresAt: string;
  listeningUntil: string;
  reviewDueAt: string;
  allowedActions: string[];
}
export interface LicenseCompatibility {
  vehicleTypeCode: string;
  licenseClasses: string[];
}
export interface DispatchCompanyPolicy {
  ownerDeptId: string;
  companyName: string;
  configured: boolean;
  version: number;
  inviteResponseSeconds: number | null;
  radiusMeter: number | null;
  expandedRadiusMeter: number | null;
  overallDeadlineSeconds: number | null;
  roundPolicy: 'COMPANY_CURSOR' | null;
  distanceMode: 'GCJ02_GREAT_CIRCLE' | null;
  licenseCompatibility: LicenseCompatibility[] | null;
  allowedActions: string[];
}
export interface SaveDispatchPolicy {
  expectedVersion: number;
  inviteResponseSeconds: number;
  radiusMeter: number;
  expandedRadiusMeter: number;
  overallDeadlineSeconds: number;
  roundPolicy: 'COMPANY_CURSOR';
  distanceMode: 'GCJ02_GREAT_CIRCLE';
  licenseCompatibility: LicenseCompatibility[];
  reason: string;
}
export interface ManualInvite {
  orderNo: string;
  driverId: string;
  expectedOrderVersion: number;
  expectedDriverVersion: number;
  reason: string;
}
export interface RevokeInvite {
  expectedVersion: number;
  expectedOrderVersion: number;
  reason: string;
}
const base = '/api/admin/dispatch';
function result<T>(response: { code: number; data: T }) {
  if (response?.code !== 200 || !response.data) throw new Error('响应尚未确认，请重新读取');
  return response.data;
}
async function list<T>(path: string, query: object) {
  const response = await viGet<{ code: number; rows: T[]; total: number }>(base + path, query);
  if (
    response.code !== 200 ||
    !Array.isArray(response.rows) ||
    !Number.isSafeInteger(response.total)
  )
    throw new Error('派单列表响应尚未确认');
  return response;
}
export const queue = (query: object) => list<DispatchQueueItem>('/queue', query);
export const candidates = (query: object) => list<DispatchCandidate>('/candidates', query);
export const invites = (query: object) => list<DispatchInvite>('/invites', query);
export async function manualInvite(body: ManualInvite, key: string) {
  const value = result(
    await viPost<{ code: number; data: DispatchInvite }>(base + '/manual-invites', body, {
      idempotencyKey: key
    })
  );
  if (value.orderNo !== body.orderNo || value.driverId !== body.driverId)
    throw new Error('原人工邀约目标尚未确认');
  return value;
}
export async function revokeInvite(id: string, body: RevokeInvite, key: string) {
  const value = result(
    await viPost<{ code: number; data: DispatchInvite }>(
      base + '/invites/' + validId(id) + '/revoke',
      body,
      { idempotencyKey: key }
    )
  );
  if (value.inviteId !== id) throw new Error('原撤回目标尚未确认');
  return value;
}
const policyPath = (company: string) =>
  base + '/companies/' + validId(company) + '/dispatch-policy';
function policyResult(response: { code: number; data: DispatchCompanyPolicy }, company: string) {
  const value = result(response);
  if (
    value.ownerDeptId !== company ||
    !Number.isSafeInteger(value.version) ||
    !Array.isArray(value.allowedActions) ||
    (value.configured
      ? !Array.isArray(value.licenseCompatibility)
      : value.licenseCompatibility !== null && !Array.isArray(value.licenseCompatibility))
  )
    throw new Error('原公司派单策略尚未确认');
  return value;
}
export const getPolicy = async (company: string) =>
  policyResult(
    await viGet<{ code: number; data: DispatchCompanyPolicy }>(policyPath(company)),
    company
  );
export const savePolicy = async (company: string, body: SaveDispatchPolicy, key: string) =>
  policyResult(
    await viPut<{ code: number; data: DispatchCompanyPolicy }>(policyPath(company), body, {
      idempotencyKey: key
    }),
    company
  );
export async function vehicleTypes() {
  type Item = { code: string; label: string; enabled: boolean };
  const rows: Item[] = [];
  let pageNum = 1;
  // 真实C01目录最多每页100项；必须读完分页，不能把未返回的车型误当停用。
  for (;;) {
    const response = await viGet<{ code: number; rows: Item[]; total: number }>(
      '/api/common/dict/vi_vehicle_type',
      { pageNum, pageSize: 100 }
    );
    if (
      response.code !== 200 ||
      !Array.isArray(response.rows) ||
      !Number.isSafeInteger(response.total) ||
      response.total < 0 ||
      response.total > 200000 ||
      (!response.rows.length && rows.length < response.total)
    )
      throw new Error('启用车型字典尚未完整取得');
    rows.push(...response.rows);
    if (rows.length >= response.total) break;
    pageNum++;
  }
  if (new Set(rows.map((row) => row.code)).size !== rows.length)
    throw new Error('车型目录已变化，请重新进入核对');
  return rows.filter((row) => row.enabled);
}
export const errorStatus = (failure: unknown) =>
  Number((failure as { httpStatus?: number })?.httpStatus || 0);
export const errorMessage = (failure: unknown) =>
  (failure as { msg?: string; message?: string })?.msg ||
  (failure as { message?: string })?.message ||
  '操作尚未确认';
export const dispatchTime = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
      })
    : '暂无';
export const distance = (value: number | null) =>
  value === null ? '未取得' : value >= 1000 ? (value / 1000).toFixed(2) + '公里' : value + '米';
const labels: Record<string, string> = {
  SCHEDULED: '待进入派单窗口',
  PENDING_DISPATCH: '待自动派单',
  DISPATCHING: '邀约中',
  MANUAL_DISPATCH: '待人工派单',
  WAIT_RESPONSE: '等待本人响应',
  ACCEPTED: '已接受',
  REJECTED: '已拒绝',
  EXPIRED: '已过期',
  REVOKED: '已撤回',
  AUTO: '自动轮转',
  MANUAL: '人工邀约',
  RUNNING: '批次运行中',
  PAUSED: '批次已暂停',
  ASSIGNED: '已建立承接',
  CLOSED: '批次已关闭'
};
export const dispatchLabel = (value: string | null | undefined) =>
  value ? labels[value] || value : '暂无';
/** 最早截止只撤回本地操作投影，不代替后端决定过期、占用或批次状态。 */
export function expiryGuard(expire: () => void) {
  let timer: ReturnType<typeof setTimeout> | null = null;
  function clear() {
    if (timer !== null) clearTimeout(timer);
    timer = null;
  }
  return {
    clear,
    arm(values: string[]) {
      clear();
      if (!values.length) return;
      const left = Math.min(...values.map(Date.parse)) - Date.now();
      if (!Number.isFinite(left) || left <= 0) expire();
      else
        timer = setTimeout(
          () => {
            clear();
            expire();
          },
          Math.min(left, 2147483647)
        );
    }
  };
}
