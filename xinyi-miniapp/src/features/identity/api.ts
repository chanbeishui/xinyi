import { request } from '../../services/http';
import type { Session, Profile, PhoneBindResult } from '../../api/types';

// I01/I02 身份接口客户端（MINI 认证域）。响应为 vi 信封 {code,msg,data}；http.ts 已处理真实状态码/401 刷新。

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
