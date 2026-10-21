// 机动车检测预约 vi API 错误契约映射（纯模块，无框架依赖，可独立单测）
// 对应 04_接口/错误码表.json 的 ApiError：{code,errorCode,msg,requestId,retryable,retryAfterSeconds,data}
// vi 端点返回真实 HTTP 状态码（400/401/403/404/409/422/429/500/503），与 sys「恒200+body.code」不同。
// 客户端按 errorCode 分支，禁止用中文 msg 分支（契约红线）。

export interface ViFieldError { field?: string; code?: string; msg?: string }
export interface ViConflict { code?: string; entryId?: string; reason?: string; [k: string]: unknown }
export interface ViApiErrorBody {
  code?: number
  errorCode?: string
  msg?: string
  message?: string
  requestId?: string
  retryable?: boolean
  retryAfterSeconds?: number
  data?: { fieldErrors?: ViFieldError[]; conflicts?: ViConflict[] } | null
}
export interface ViApiError {
  name: 'ViApiError'
  httpStatus: number
  errorCode: string
  msg: string
  requestId?: string
  retryable: boolean
  retryAfterSeconds?: number
  fieldErrors: ViFieldError[]
  conflicts: ViConflict[]
}

// errorCode → 用户可读提示（UI 展示用；业务分支请用 errorCode 而非本文案）
export const VI_ERROR_HINTS: Record<string, string> = {
  VI_AUTH_REQUIRED: '登录已失效，请重新登录',
  VI_FORBIDDEN: '无权执行此操作',
  VI_NOT_FOUND: '目标不存在或不可见',
  VI_VERSION_CONFLICT: '数据已被他人更新，请刷新后重试',
  VI_STATE_CONFLICT: '当前状态不允许该操作，请刷新',
  VI_IDEMPOTENCY_CONFLICT: '重复请求但内容不同，请刷新后重试',
  VI_OCCUPANCY_CONFLICT: '资源已被占用，请刷新后重试',
  VI_SELECTION_CONFLICT: '所选条目已变化，整批未保存，请重新选择',
  VI_DATA_INVALID: '输入不合法，请检查后重试',
  VI_EVIDENCE_INCOMPLETE: '证据或前置条件不完整',
  VI_HANDOVER_CODE_INVALID: '交接码无效',
  VI_HANDOVER_LOCKED: '交接码错误次数过多，本阶段已锁定',
  VI_BINDING_CREDENTIAL_INVALID: '绑定凭证无效或已过期',
  VI_CONTEXT_CHANGED: '工作身份已切换，请刷新',
  VI_CAPABILITY_NOT_CONFIGURED: '能力未配置或不可用',
  VI_AUTHORITY_UNAVAILABLE: '权限权威源暂不可用，请稍后重试（不放行）',
  VI_RATE_LIMITED: '请求过于频繁，请稍后重试',
  VI_INTERNAL_ERROR: '系统繁忙，请稍后重试'
}

export const VI_VERSION_CONFLICT_CODES = ['VI_VERSION_CONFLICT', 'VI_STATE_CONFLICT', 'VI_IDEMPOTENCY_CONFLICT', 'VI_OCCUPANCY_CONFLICT', 'VI_SELECTION_CONFLICT', 'VI_CONTEXT_CHANGED']

// 把 axios 错误响应映射为规范 ViApiError；httpStatus 缺失（网络层）时归 0
export function mapViError(httpStatus: number | undefined, body: ViApiErrorBody | undefined | null, retryAfterHeader?: string | null): ViApiError {
  const status = typeof httpStatus === 'number' && httpStatus > 0 ? httpStatus : 0
  const errorCode = (body && (body.errorCode || (body.code ? String(body.code) : ''))) || defaultCodeForStatus(status)
  const msg = (body && (body.msg || body.message)) || VI_ERROR_HINTS[errorCode] || '请求未完成，请刷新状态'
  const ra = retryAfterHeader != null && retryAfterHeader !== '' ? Number(retryAfterHeader) : (body ? body.retryAfterSeconds : undefined)
  return {
    name: 'ViApiError',
    httpStatus: status,
    errorCode,
    msg,
    requestId: body ? body.requestId : undefined,
    retryable: body && typeof body.retryable === 'boolean' ? body.retryable : status === 429 || status === 503,
    retryAfterSeconds: typeof ra === 'number' && !isNaN(ra) ? ra : undefined,
    fieldErrors: (body && body.data && body.data.fieldErrors) || [],
    conflicts: (body && body.data && body.data.conflicts) || []
  }
}

function defaultCodeForStatus(status: number): string {
  switch (status) {
    case 400: return 'VI_DATA_INVALID'
    case 401: return 'VI_AUTH_REQUIRED'
    case 403: return 'VI_FORBIDDEN'
    case 404: return 'VI_NOT_FOUND'
    case 409: return 'VI_STATE_CONFLICT'
    case 422: return 'VI_EVIDENCE_INCOMPLETE'
    case 429: return 'VI_RATE_LIMITED'
    case 503: return 'VI_AUTHORITY_UNAVAILABLE'
    case 0: return 'VI_NETWORK'
    default: return 'VI_INTERNAL_ERROR'
  }
}

export function isAuthRequired(e: ViApiError): boolean { return e.httpStatus === 401 || e.errorCode === 'VI_AUTH_REQUIRED' }
export function isVersionConflict(e: ViApiError): boolean { return e.httpStatus === 409 && VI_VERSION_CONFLICT_CODES.indexOf(e.errorCode) >= 0 }
export function isForbidden(e: ViApiError): boolean { return e.httpStatus === 403 || e.errorCode === 'VI_FORBIDDEN' }
export function isNetwork(e: ViApiError): boolean { return e.httpStatus === 0 || e.errorCode === 'VI_NETWORK' }
