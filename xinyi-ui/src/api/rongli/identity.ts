// 机动车检测预约 · 身份域 V6.4 契约类型（PC 后台 ADMIN 域共享）
// 来源：docs/vehicle/V6.4/04_接口/dto-types.ts（Session/Profile/EmployeeLinkPublic 等只读投影）。
// I01 的登录/统一档案是 MINI 端能力（见 xinyi-miniapp/src/features/identity）；PC 后台对小程序账号
// 的查看/暂停/解除/允许重核（B36）等写接口由对应任务落地，此处先提供权威类型，避免各处重复定义漂移。

export type MiniRole = 'OWNER' | 'DRIVER' | 'STATION'
export type QualificationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'EXPIRED' | 'SUSPENDED'
export type EmployeeLinkStatus = 'UNLINKED' | 'LINKED' | 'NEEDS_ADMIN' | 'SUSPENDED' | 'REVOKED'
export type EmployeeLinkMessageKey =
  | 'NO_EMPLOYEE_MATCH' | 'EMPLOYEE_LINKED' | 'WAIT_ADMIN_CONFIGURATION'
  | 'CONTACT_ADMIN' | 'PHONE_REVERIFY_REQUIRED' | 'LINK_REVOKED'

// 员工关联公开投影：粗粒度，不含 sysUserId、员工名单、其他手机号、完整 OpenID 或后台权限
export interface EmployeeLinkPublic {
  status: EmployeeLinkStatus
  workAccessEnabled: boolean
  authorizedStationCount: number
  messageKey: EmployeeLinkMessageKey
  canReverifyPhone: boolean
}

// 会话视图（不含令牌）：后台/客户端展示当前身份视角与可用角色
export interface SessionView {
  sessionId: string
  accountId: string
  activeRole: MiniRole
  availableRoles: MiniRole[]
  contextVersion: number
  phoneBound: boolean
  capabilities: string[]
  permissions?: string[]
  stationIds?: string[]
  stationId?: string | null
  workAccessDisabled?: boolean
  driverQualification: QualificationStatus | null
  employeeLink: EmployeeLinkPublic
  accountVersion: number
}

// 统一档案只读投影
export interface ProfileView {
  accountId: string
  nickname: string | null
  phoneMask: string | null
  driverQualification: QualificationStatus | null
  availableRoles: MiniRole[]
  permissions: string[]
  version: number
  realName: string | null
  avatarFileId: string | null
  defaultAddressId: string | null
  employeeLink: EmployeeLinkPublic
}
