import { reactive } from 'vue'
import { STORAGE } from '@/utils/config'
import { clearSession as clearStorage, saveTokens } from '@/utils/request'

export interface SessionProfile {
  accountId?: string
  nickname?: string
  phoneBound?: boolean
  roles?: string[]
  serviceName?: string | null
  driverQualificationStatus?: string | null
  driverListeningEnabled?: boolean
  employeeBound?: boolean
  employeeName?: string | null
  employeeDeptName?: string | null
}

export interface StationBrief {
  stationId: string
  name: string
  address?: string
  enabled?: boolean
}

export interface SessionState {
  loggedIn: boolean
  activeRole: string
  stationId: string | null
  profile: SessionProfile
  stations: StationBrief[]
  contextVersion: number
}

export const session = reactive<SessionState>({
  loggedIn: false,
  activeRole: 'OWNER',
  stationId: null,
  profile: {},
  stations: [],
  contextVersion: 1
})

export const ROLE_LABEL: Record<string, string> = {
  OWNER: '车主',
  DRIVER: '代驾',
  STATION: '检测员工'
}

export const ROLE_HOME: Record<string, string> = {
  OWNER: '/pages/owner/home/index',
  DRIVER: '/pages/driver/home/index',
  STATION: '/pages/station/home/index'
}

/** 应用登录返回的会话视图 */
export function applySession(view: any) {
  if (!view) return
  /**
   * 切换角色、切换检测点、员工绑定都会让服务端撤销整条旧令牌链并签发新令牌，
   * 会话视图里带回了新令牌就必须落盘。此前只更新内存状态、不写存储，
   * 后续请求仍携带已被撤销的旧令牌，表现为"登录令牌频繁失效"。
   */
  if (view.accessToken) {
    saveTokens(view.accessToken, view.refreshToken)
  }
  session.loggedIn = true
  session.activeRole = view.activeRole || 'OWNER'
  session.stationId = view.stationId ? String(view.stationId) : null
  session.profile = view.profile || {}
  session.stations = Array.isArray(view.stations) ? view.stations : []
  session.contextVersion = Number(view.contextVersion || 1)
  uni.setStorageSync(STORAGE.role, session.activeRole)
  uni.setStorageSync(STORAGE.profile, session.profile)
}

/** 冷启动时从本地恢复展示态（真实权限仍由服务端每次校验） */
export function restoreSession() {
  const role = (uni.getStorageSync(STORAGE.role) as string) || ''
  const profile = uni.getStorageSync(STORAGE.profile)
  if (role) session.activeRole = role
  if (profile) session.profile = profile
  session.loggedIn = !!uni.getStorageSync(STORAGE.access)
}

export function resetSession() {
  clearStorage()
  session.loggedIn = false
  session.activeRole = 'OWNER'
  session.stationId = null
  session.profile = {}
  session.stations = []
}

export function hasRole(role: string): boolean {
  const roles = session.profile && session.profile.roles
  return Array.isArray(roles) && roles.indexOf(role) >= 0
}

export function homeOfCurrentRole(): string {
  return ROLE_HOME[session.activeRole] || ROLE_HOME.OWNER
}
