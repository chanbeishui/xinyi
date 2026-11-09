import { viGet, viPost } from './request';
import { validId } from './aftersale';

export interface ManualApplication {
  applicationId: string;
  driverId: string;
  applicantName: string | null;
  ownerDeptId: string;
  companyName: string;
  version: number;
  companyRevision: number;
  materialRevision: number;
}
export interface ManualRecord {
  recordId: string;
  applicationId: string;
  driverId: string;
  applicantName: string | null;
  ownerDeptId: string;
  companyName: string;
  companyRevision: number;
  materialRevision: number;
  type: 'BACKGROUND' | 'LICENSE';
  conclusion: 'PASSED' | 'FAILED';
  status: 'PENDING_REVIEW' | 'CONFIRMED' | 'RETURNED';
  submittedByUserId: string;
  submittedByName: string | null;
  submittedAt: string;
  reviewedByUserId: string | null;
  reviewedByName: string | null;
  reviewedAt: string | null;
  note: string | null;
  reviewNote: string | null;
  version: number;
  allowedActions: string[];
}
export interface CreateManual {
  applicationId: string;
  expectedApplicationVersion: number;
  expectedCompanyRevision: number;
  expectedMaterialRevision: number;
  type: 'BACKGROUND' | 'LICENSE';
  conclusion: 'PASSED' | 'FAILED';
  note: string;
}
export interface ReviewManual {
  expectedVersion: number;
  decision: 'CONFIRM' | 'RETURN';
  note: string;
}
const manual = '/api/admin/manual-verifications';
type Envelope<T> = { code: number; data: T };
function data<T>(response: Envelope<T>): T {
  if (response?.code !== 200 || !response.data) throw new Error('响应尚未确认，请重新读取');
  return response.data;
}
export const listManual = (params: object) =>
  viGet<{ code: number; rows: ManualRecord[]; total: number }>(manual, params);
export const manualApplications = (params: object) =>
  viGet<{ code: number; rows: ManualApplication[]; total: number }>(
    manual + '/applications',
    params
  );
export async function getManual(id: string) {
  return data(await viGet<Envelope<ManualRecord>>(manual + '/' + validId(id)));
}
export async function createManual(body: CreateManual, key: string) {
  return data(await viPost<Envelope<ManualRecord>>(manual, body, { idempotencyKey: key }));
}
export async function reviewManual(id: string, body: ReviewManual, key: string) {
  return data(
    await viPost<Envelope<ManualRecord>>(manual + '/' + validId(id) + '/review', body, {
      idempotencyKey: key
    })
  );
}

export interface DeviceSessionRecord {
  verificationSessionId: string;
  applicationId: string;
  requestNo: string;
  status: string;
  expiresAt: string;
  resultAvailable: boolean;
  failureCode: string | null;
  version: number;
  materialRevision: number;
  companyRevision: number;
  ownerDeptId: string | null;
  companyName: string | null;
  provider: string | null;
  createdAt: string;
  verifiedAt: string | null;
  allowedActions: string[];
}
export interface CloseDeviceSession {
  expectedVersion: number;
  reason: string;
}
const sessions = '/api/admin/verification-sessions';
export const listDeviceSessions = (params: object) =>
  viGet<{ code: number; rows: DeviceSessionRecord[]; total: number }>(sessions, params);
export async function getDeviceSession(id: string) {
  return data(await viGet<Envelope<DeviceSessionRecord>>(sessions + '/' + validId(id)));
}
export async function closeDeviceSession(id: string, body: CloseDeviceSession, key: string) {
  return data(
    await viPost<Envelope<DeviceSessionRecord>>(sessions + '/' + validId(id) + '/close', body, {
      idempotencyKey: key
    })
  );
}

export const verificationLabel = (value: string | null | undefined) =>
  value
    ? (
        {
          BACKGROUND: '背景核验',
          LICENSE: '驾驶证核验',
          PASSED: '通过',
          FAILED: '不通过',
          PENDING_REVIEW: '待独立复核',
          CONFIRMED: '已复核确认',
          RETURNED: '已退回',
          CREATED: '已建立',
          PROCESSING: '核验处理中',
          UNKNOWN: '结果尚未确认',
          EXPIRED: '已失效',
          CLOSED: '已封存'
        } as Record<string, string>
      )[value] || value
    : '暂无';
export const verificationTime = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    : '暂无';
