import { request } from '../../services/http';
import type { Session, Profile } from '../../api/types';

// I01 身份接口客户端（MINI 认证域）。响应为 vi 信封 {code,msg,data}；http.ts 已处理真实状态码/401 刷新。

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
