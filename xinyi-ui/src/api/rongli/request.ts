// 机动车检测预约 vi API 客户端（PC 后台 = ADMIN 认证域，复用框架 sys_user JWT）
// 独立 axios 实例，不导入、不修改 @/utils/request.ts（保留原 Jwt 响应与错误处理）。
// baseURL 复用 VITE_APP_BASE_API（经 vite 代理 rewrite），vi 路径自带 /api，全链路 /api 只出现一次。
// 类型用 any 配置（与现有 utils/request.ts 同风格），避免 axios 具名类型在本项目版本的解析差异。
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'
import { mapViError, isAuthRequired, type ViApiError, type ViApiErrorBody } from './viError'

export interface ViRequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  params?: object
  data?: object
  headers?: Record<string, string>
  // 写操作幂等键（契约 Idempotency-Key）；同一业务意图重试须复用同键
  idempotencyKey?: string
  // 401 时不自动弹重新登录（由调用方自行处理）
  skipAuthRedirect?: boolean
}

const viService = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
})

let reloginShowing = false

viService.interceptors.request.use((config: any) => {
  config.headers = config.headers || {}
  if (getToken()) config.headers['Authorization'] = 'Bearer ' + getToken()
  // X-Client-Type 仅审计来源，不增权（契约红线）；PC 后台固定 admin
  config.headers['X-Client-Type'] = 'admin'
  if (config.idempotencyKey) config.headers['Idempotency-Key'] = config.idempotencyKey
  return config
})

viService.interceptors.response.use(
  (res: any) => {
    // vi 契约：{code,msg,data} 或 {code,msg,rows,total}，HTTP 2xx；与 sys 一致的信封，直接返回
    return res.data
  },
  (error: any) => {
    const resp = error.response
    const httpStatus: number | undefined = resp ? resp.status : undefined
    const body: ViApiErrorBody | undefined = resp ? (resp.data as ViApiErrorBody) : undefined
    const retryAfter = resp && resp.headers ? (resp.headers['retry-after'] as string | undefined) : undefined
    const viErr: ViApiError = mapViError(httpStatus, body, retryAfter)
    if (isAuthRequired(viErr) && !reloginShowing) {
      reloginShowing = true
      ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
        confirmButtonText: '重新登录', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        reloginShowing = false
        useUserStore().logOut().then(() => { location.href = '/index' })
      }).catch(() => { reloginShowing = false })
    } else if (viErr.httpStatus !== 401) {
      // 统一提示；调用方据 viErr.errorCode 做精细分支（不要用中文 msg 分支）
      ElMessage({ message: viErr.msg, type: viErr.httpStatus === 403 ? 'warning' : 'error', duration: 5000 })
    }
    return Promise.reject(viErr)
  }
)

// 统一入口：返回契约信封；调用方取 data/rows。写操作传 idempotencyKey。
export function viRequest<T = any>(config: ViRequestConfig): Promise<T> {
  const { idempotencyKey, skipAuthRedirect, ...rest } = config
  return viService.request({ ...(rest as any), idempotencyKey, skipAuthRedirect } as any) as Promise<T>
}
export const viGet = <T = any>(url: string, params?: object, config?: Partial<ViRequestConfig>) =>
  viRequest<T>({ url, method: 'GET', params, ...config })
export const viPost = <T = any>(url: string, data?: object, config?: Partial<ViRequestConfig>) =>
  viRequest<T>({ url, method: 'POST', data, ...config })
export const viPut = <T = any>(url: string, data?: object, config?: Partial<ViRequestConfig>) =>
  viRequest<T>({ url, method: 'PUT', data, ...config })
export const viPatch = <T = any>(url: string, data?: object, config?: Partial<ViRequestConfig>) =>
  viRequest<T>({ url, method: 'PATCH', data, ...config })
export const viDelete = <T = any>(url: string, data?: object, config?: Partial<ViRequestConfig>) =>
  viRequest<T>({ url, method: 'DELETE', data, ...config })

export default viService
export { viService }
