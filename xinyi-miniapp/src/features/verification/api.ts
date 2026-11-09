import { currentSession, request } from '../../services/http';

export type DeviceSession = {
  verificationSessionId: string;
  applicationId: string;
  requestNo: string;
  status: 'CREATED' | 'PROCESSING' | 'UNKNOWN' | 'PASSED' | 'FAILED' | 'EXPIRED' | 'CLOSED';
  launchMode: 'WX_SOTER_DEVICE_AUTH';
  launchUrl: null;
  challenge: string | null;
  authContent: string;
  expiresAt: string;
  resultAvailable: boolean;
  failureCode: string | null;
  version: number;
  materialRevision: number;
  companyRevision: number;
  allowedActions: string[];
};
export type CreateDeviceSession = {
  applicationId: string;
  expectedApplicationVersion: number;
  expectedCompanyRevision: number;
  expectedMaterialRevision: number;
  consentVersion: string;
  consentTextHash: string;
  acceptedConsent: true;
};
export type ConfirmDeviceSession = {
  expectedVersion: number;
  resultJSON: string;
  resultJSONSignature: string;
};
export type RecoverDeviceSession = { expectedVersion: number; recover: true };
export type DeviceIntent =
  | { action: 'CREATE'; key: string; body: CreateDeviceSession }
  | { action: 'CONFIRM'; id: string; key: string; body: ConfirmDeviceSession }
  | { action: 'RECOVER'; id: string; key: string; body: RecoverDeviceSession };
const base = '/api/app/driver/qualification';
function id(value: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(value)) throw new Error('设备认证会话编号无效');
  return value;
}
export function verificationScope() {
  const session = currentSession();
  if (!session) throw new Error('请先登录');
  return [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(
    ':'
  );
}
function validate(value: DeviceSession) {
  if (
    !value ||
    value.launchMode !== 'WX_SOTER_DEVICE_AUTH' ||
    value.launchUrl !== null ||
    !Array.isArray(value.allowedActions) ||
    (value.challenge !== null && !/^[A-Za-z0-9_-]{43}$/.test(value.challenge))
  )
    throw new Error('设备认证响应尚未确认，请重新读取');
  return value;
}
export async function getDeviceSession(value: string) {
  return validate(
    (await request<{ data: DeviceSession }>('GET', base + '/face-sessions/' + id(value))).data
  );
}
export async function runDeviceIntent(intent: DeviceIntent) {
  const path =
    intent.action === 'CREATE'
      ? base + '/face-session'
      : base + '/face-sessions/' + id(intent.id) + '/confirm';
  return validate(
    (await request<{ data: DeviceSession }>('POST', path, intent.body, intent.key)).data
  );
}

let pending: { scope: string; intent: DeviceIntent } | null = null;
/** 原签名只在当前会话的未知请求中保留，绝不写本地存储、路由或日志；切身份即丢弃。 */
export function pendingDeviceIntent(intent?: DeviceIntent | null) {
  if (!currentSession()) {
    pending = null;
    return null;
  }
  const scope = verificationScope();
  if (pending?.scope !== scope) pending = null;
  if (intent !== undefined) pending = intent ? { scope, intent } : null;
  return pending?.intent || null;
}
