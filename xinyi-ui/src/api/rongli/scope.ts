// 机动车检测预约 · 员工站点预配置（I06，B26，ADMIN 域）
// 按 sys_user 查改（不要求 accountId/openid）；集合 CAS 含空集合；部门数据范围服务端校验。
import { viGet, viPut } from './request'

export interface StationGrantView {
  employeeUserId: string
  ownStationId: string | null
  authorizedStationIds: string[]
  version: number
  wechatLinkStatus: 'NOT_LOGGED_IN_OR_UNLINKED' | 'LINKED' | 'SUSPENDED' | 'REVOKED'
  updatedAt: string | null
}

export function listStationGrants(pageNum = 1, pageSize = 10) {
  return viGet<{ code: number; msg: string; data: StationGrantView[] }>('/api/admin/station-grants', { pageNum, pageSize })
}

export function getStationGrant(employeeUserId: string) {
  return viGet<{ code: number; msg: string; data: StationGrantView }>(`/api/admin/station-grants/${employeeUserId}`)
}

export function putStationGrant(employeeUserId: string, authorizedStationIds: string[], expectedVersion: number, reason: string, ownStationId?: string | null) {
  return viPut<{ code: number; msg: string; data: StationGrantView }>(
    `/api/admin/station-grants/${employeeUserId}`,
    { ownStationId: ownStationId ?? null, authorizedStationIds, expectedVersion, reason },
    { idempotencyKey: crypto.randomUUID() })
}