/**
 * V6.4 手机号授权接续的工程适配接口。
 * inject真实请求/会话持久化；不在这里写微信成功替身。工程落地须在全局单飞刷新锁内实现refresh。
 */
import type { PhoneRequest, PhoneBindResult, Session } from "../api/types";
export interface PhoneFlowPort {
  currentSession(): Session;
  bindPhone(body: PhoneRequest): Promise<PhoneBindResult>;
  refreshSingleFlight(): Promise<Session>; // 使用现有refreshToken；与拦截器共用锁
  persistSession(session: Session): void;
  nextRequestId(): string; // 真实UUID，不用时间戳拼接
}
export async function authorizeVerifiedPhone(phoneCode: string, port: PhoneFlowPort): Promise<PhoneBindResult> {
  if (!phoneCode) throw new Error("手机号授权未完成");
  const current = port.currentSession();
  const result = await port.bindPhone({
    phoneCode, expectedAccountVersion: current.accountVersion,
    verificationRequestId: port.nextRequestId()
  });
  if (result.requiresSessionRefresh) {
    const updated = await port.refreshSingleFlight();
    if (updated.accountId !== current.accountId) throw new Error("会话已切换，请重新操作");
    port.persistSession(updated);
  }
  // PROCESSING/UNKNOWN由页面查本人回执；不得伪装成授权成功。
  return result;
}
