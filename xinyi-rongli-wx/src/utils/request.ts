import { API_BASE, STORAGE } from './config'

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

export interface RequestOptions {
  url: string
  method?: HttpMethod
  data?: Record<string, any> | any
  /** 静默模式：失败不弹 toast，由调用方自行处理 */
  silent?: boolean
  /** 内部使用：标记这是刷新令牌后的重放，避免死循环 */
  retried?: boolean
  /**
   * 内部使用：本次请求实际带出的令牌快照。
   *
   * 401 回来时若存储中的令牌已与快照不同，说明令牌在请求飞行期间已被其他请求
   * 成功续期，此时应直接用新令牌重放，而不是再发一次刷新去轮换令牌。
   */
  token?: string
}

let refreshing: Promise<boolean> | null = null

/** 并发 401 时只跳转一次登录页，避免多次 reLaunch 叠加导致页面栈异常 */
let redirecting = false

function readAccess(): string {
  return (uni.getStorageSync(STORAGE.access) as string) || ''
}

function readRefresh(): string {
  return (uni.getStorageSync(STORAGE.refresh) as string) || ''
}

export function saveTokens(access: string, refresh: string) {
  if (access) uni.setStorageSync(STORAGE.access, access)
  if (refresh) uni.setStorageSync(STORAGE.refresh, refresh)
}

export function clearSession() {
  uni.removeStorageSync(STORAGE.access)
  uni.removeStorageSync(STORAGE.refresh)
  uni.removeStorageSync(STORAGE.role)
  uni.removeStorageSync(STORAGE.profile)
}

/** 刷新令牌：并发请求共用同一次刷新，避免令牌代际错乱 */
export function refreshToken(): Promise<boolean> {
  if (refreshing) return refreshing
  const token = readRefresh()
  if (!token) return Promise.resolve(false)
  const task = new Promise<boolean>((resolve) => {
    uni.request({
      url: `${API_BASE}/api/common/token/refresh`,
      method: 'POST',
      data: { refreshToken: token },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        const body: any = res.data
        if (res.statusCode === 200 && body && body.accessToken) {
          saveTokens(body.accessToken, body.refreshToken)
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail: () => resolve(false)
    })
  })
  // 刷新标志在本次结果落地后才释放：过早清空会让等待中的 401 再发起一次刷新，
  // 而刷新令牌是单次消费 + 整链轮换的，第二次必然失败并把用户踢回登录页。
  refreshing = task.then(
    (ok) => {
      refreshing = null
      return ok
    },
    () => {
      refreshing = null
      return false
    }
  )
  return refreshing
}

function toLogin() {
  if (redirecting) return
  redirecting = true
  clearSession()
  const pages = getCurrentPages()
  const current = pages.length ? pages[pages.length - 1].route : ''
  if (current && current.indexOf('pages/login') === 0) {
    redirecting = false
    return
  }
  uni.reLaunch({
    url: '/pages/login/index',
    complete: () => {
      redirecting = false
    }
  })
}

function toast(message: string) {
  uni.showToast({ title: message, icon: 'none', duration: 2200 })
}

/**
 * 后端部分异常（方法不支持、全局兜底异常等）会以 HTTP 200 + {code,message} 返回，
 * 仅按 HTTP 状态判断会把失败当成功。
 * 注意：交接码、推广码等正常响应体的 code 是字符串，故只认「数字且 >= 400」的业务码。
 */
function bizError(body: any): string | null {
  if (!body || typeof body !== 'object') return null
  const code = body.code
  if (typeof code !== 'number' || code < 400) return null
  const message = body.message || body.msg
  return typeof message === 'string' && message ? message : `请求失败（${code}）`
}

/**
 * 统一请求封装：自动附带小程序令牌、401 自动续期重放、业务错误统一抛出。
 */
export function request<T = any>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, silent = false } = options
  const header: Record<string, string> = { 'Content-Type': 'application/json' }
  const access = options.token !== undefined ? options.token : readAccess()
  if (access) header['X-Mini-Token'] = access

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: url.startsWith('http') ? url : `${API_BASE}${url}`,
      method,
      data,
      header,
      success: async (res) => {
        const status = res.statusCode
        const body: any = res.data
        if (status >= 200 && status < 300) {
          const biz = bizError(body)
          if (biz) {
            if (!silent) toast(biz)
            reject(new Error(biz))
            return
          }
          resolve(body as T)
          return
        }
        if (status === 401 && !options.retried) {
          // 请求飞行期间令牌已被其他请求续期：直接换新令牌重放，不再轮换
          const latest = readAccess()
          if (latest && latest !== access) {
            try {
              resolve(await request<T>({ ...options, token: latest, retried: true }))
            } catch (e) {
              reject(e)
            }
            return
          }
          const ok = await refreshToken()
          if (ok) {
            try {
              resolve(await request<T>({ ...options, token: readAccess(), retried: true }))
            } catch (e) {
              reject(e)
            }
            return
          }
          toLogin()
          reject(new Error('登录已过期，请重新进入'))
          return
        }
        const message = bizError(body) || (body && (body.message || body.msg)) || `请求失败（${status}）`
        if (!silent) toast(message)
        reject(new Error(message))
      },
      fail: (err) => {
        const message = '网络异常，请检查网络后重试'
        if (!silent) toast(message)
        reject(new Error(err.errMsg || message))
      }
    })
  })
}

export const http = {
  get: <T = any>(url: string, data?: any, silent = false) => request<T>({ url, method: 'GET', data, silent }),
  post: <T = any>(url: string, data?: any, silent = false) => request<T>({ url, method: 'POST', data, silent }),
  put: <T = any>(url: string, data?: any, silent = false) => request<T>({ url, method: 'PUT', data, silent }),
  del: <T = any>(url: string, data?: any, silent = false) => request<T>({ url, method: 'DELETE', data, silent })
}

/** 文件上传：先申请上传会话，再提交内容并完成校验 */
export function uploadFile(filePath: string, purpose: string, orderNo?: string, retried = false): Promise<any> {
  return new Promise((resolve, reject) => {
    http
      .post('/api/common/uploads', { purpose, mimeType: guessMime(filePath), size: 0, orderNo })
      .then((session: any) => {
        uni.uploadFile({
          url: `${API_BASE}/api/common/uploads/${session.uploadId}/content`,
          filePath,
          name: 'file',
          header: { 'X-Mini-Token': readAccess() },
          success: (res) => {
            if (res.statusCode === 401 && !retried) {
              // 上传不走 uni.request，无法复用请求层的 401 续期，这里单独补一次
              refreshToken().then((ok) => {
                if (!ok) {
                  toLogin()
                  reject(new Error('登录已过期，请重新进入'))
                  return
                }
                uploadFile(filePath, purpose, orderNo, true)
                  .then(resolve)
                  .catch(reject)
              })
              return
            }
            let body: any = {}
            try {
              body = JSON.parse(res.data)
            } catch (e) {
              body = {}
            }
            const biz = bizError(body)
            if (res.statusCode !== 200 || biz) {
              const message = biz || body.message || body.msg || '上传失败，请重试'
              toast(message)
              reject(new Error(message))
              return
            }
            // 后端接收二进制时已顺带核验，READY 直接返回，避免多打一次请求
            if (body.status === 'READY') {
              resolve(body)
              return
            }
            http
              .post(`/api/common/uploads/${session.uploadId}/complete`, {
                size: body.size || 0,
                sha256: body.sha256 || ''
              })
              .then((done: any) => resolve(done))
              .catch(reject)
          },
          fail: () => {
            toast('上传失败')
            reject(new Error('上传失败'))
          }
        })
      })
      .catch(reject)
  })
}

function guessMime(path: string): string {
  const lower = path.toLowerCase()
  if (lower.endsWith('.png')) return 'image/png'
  if (lower.endsWith('.webp')) return 'image/webp'
  if (lower.endsWith('.pdf')) return 'application/pdf'
  return 'image/jpeg'
}
