import { request } from '../../services/http';
import type { Session, Profile, PhoneBindResult, SessionView, LogoutResult } from '../../api/types';

// I01/I02/I03 身份接口客户端（MINI 认证域）。响应为 vi 信封 {code,msg,data}；http.ts 已处理真实状态码/401 刷新。

export async function loginWechat(code: string): Promise<Session> {
  const resp = await request<{ code: number; msg: string; data: Session }>(
    'POST', '/api/common/login/wechat', { code });
  return resp.data;
}

export async function getProfile(): Promise<Profile> {
  const resp = await request<{ code: number; msg: string; data: Profile }>(
    'GET', '/api/common/profile');
  return resp.data;
}

// I02 手机号授权绑定：phoneCode 与 wx.login code 不同；verificationRequestId 为本流程恢复编号（UUID）。
export async function bindPhone(phoneCode: string, expectedAccountVersion: number, verificationRequestId: string): Promise<PhoneBindResult> {
  const resp = await request<{ code: number; msg: string; data: PhoneBindResult }>(
    'POST', '/api/common/phone/bind', { phoneCode, expectedAccountVersion, verificationRequestId });
  return resp.data;
}

// 响应丢失/结果不明时查询本人回执（不重复消费 phoneCode）。
export async function queryPhoneVerification(verificationRequestId: string): Promise<PhoneBindResult> {
  const resp = await request<{ code: number; msg: string; data: PhoneBindResult }>(
    'GET', '/api/common/phone/verifications/' + verificationRequestId);
  return resp.data;
}

// I03 显式刷新轮换（PUBLIC）。http.ts 已在 401 时自动调用；此函数供主动恢复会话使用。
// 单次轮换：旧 refreshToken 消费后不可重用，重用会撤销整个会话族。
export async function refreshSession(refreshToken: string): Promise<Session> {
  const resp = await request<{ code: number; msg: string; data: Session }>(
    'POST', '/api/common/token/refresh', { refreshToken });
  return resp.data;
}

// 当前会话只读视图（不含令牌）；可用角色/资格/员工关联由服务端按权威状态实时重算。
export async function getSession(): Promise<SessionView> {
  const resp = await request<{ code: number; msg: string; data: SessionView }>(
    'GET', '/api/common/session');
  return resp.data;
}

// 退出当前会话或全部设备（allDevices=true 递增 session_epoch）。只清会话，不取消订单/不解除持车任务。
// 退出后调用方应 saveSession(null)；幂等键缺省自动生成（业务动作幂等，不缓存凭据）。
export async function logout(allDevices = false, idempotencyKey?: string): Promise<LogoutResult> {
  const resp = await request<{ code: number; msg: string; data: LogoutResult }>(
    'POST', '/api/common/session/logout', { allDevices }, idempotencyKey || newIdempotencyKey());
  return resp.data;
}

function newIdempotencyKey(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}
