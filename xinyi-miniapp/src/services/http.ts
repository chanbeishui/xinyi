import type { Session, AccessSession } from '../api/types';
const base = String(import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');
const mode = String(import.meta.env.VITE_APP_REALM || 'local');
if (!base || (mode === 'production' && !base.startsWith('https://'))) throw new Error('API地址未配置或生产不是HTTPS');
const key = 'vi.session.' + mode + '.' + base;
let session: Session | null = uni.getStorageSync(key) || null;
let refreshing: Promise<void> | null = null;
export function saveSession(value: Session | null) {
  session = value; if (value) uni.setStorageSync(key, value); else uni.removeStorageSync(key);
}
export function currentSession(): Session | null { return session; }
// I04 切换后合并新访问令牌与上下文（保留会话级 refreshToken，不随切换轮换）。
export function applyAccessSession(a: AccessSession) {
  if (!session) return;
  saveSession({ ...session, ...a, refreshToken: session.refreshToken, refreshExpiresIn: session.refreshExpiresIn } as Session);
}
function raw(method: 'GET'|'POST'|'PUT'|'PATCH'|'DELETE', path: string, data?: object, idem?: string): Promise<{ status: number; body: any }> {
  if (!path.startsWith('/api/')) return Promise.reject(new Error('拒绝非业务API路径'));
  // 微信 wx.request 不支持 PATCH：以 POST + X-HTTP-Method-Override 表达；后端须启用方法覆盖过滤器（见 E04 报告，E05/E06 落实）
  const wxMethod = method === 'PATCH' ? 'POST' : method;
  return new Promise((resolve, reject) => uni.request({ url: base + path, method: wxMethod, data,
    header: { 'Content-Type': 'application/json', 'X-Client-Type': (session?.activeRole || 'OWNER').toLowerCase(),
      ...(method === 'PATCH' ? { 'X-HTTP-Method-Override': 'PATCH' } : {}),
      ...(session ? { Authorization: 'Bearer ' + session.token } : {}), ...(idem ? { 'Idempotency-Key': idem } : {}) },
    success: result => resolve({ status: result.statusCode, body: result.data }), fail: () => reject(new Error('网络失败；保留草稿，写入结果未知先查询，不换键重试')) }));
}
async function renew() {
  if (!session?.refreshToken) throw new Error('请重新登录');
  const oldSessionId = session.sessionId;
  const r = await raw('POST', '/api/common/token/refresh', { refreshToken: session.refreshToken });
  if (session?.sessionId !== oldSessionId) throw new Error('会话已切换，请重试');
  if (r.status !== 200 || !r.body?.data) { saveSession(null); throw new Error('会话已失效，请重新登录'); }
  saveSession(r.body.data as Session);
}
export async function request<T>(method:'GET'|'POST'|'PUT'|'PATCH'|'DELETE',path:string,data?:object,idempotencyKey?:string):Promise<T> {
  const context=session?.contextVersion, callerSessionId=session?.sessionId;
  let r=await raw(method,path,data,idempotencyKey);
  if (callerSessionId!==undefined && (session?.sessionId!==callerSessionId || session?.contextVersion!==context)) throw new Error('旧会话响应已丢弃');
  if (r.status===401 && r.body?.errorCode==='VI_AUTH_REQUIRED' && session?.refreshToken && !path.includes('/token/refresh')) {
    refreshing ||= renew().finally(()=>{ refreshing=null; }); await refreshing;
    r=await raw(method,path,data,idempotencyKey); // 同一个业务意图沿用原幂等键，最多重试一次。
  }
  if (callerSessionId!==undefined && (session?.sessionId!==callerSessionId || session?.contextVersion!==context)) throw new Error('工作身份已切换，旧响应已丢弃');
  if (r.status<200 || r.status>=300) throw new Error(r.body?.message || r.body?.msg || '请求未完成，请刷新状态');
  return r.body as T;
}
