// 机动车检测预约 · 员工微信关联后台管理（I05，B36，ADMIN 域）
// 复用 vi 客户端（框架 JWT）；功能权限+部门数据范围由服务端校验，前端仅提示。
import { viGet, viPost } from './request'
import type { EmployeeLinkAdmin, EmployeeLinkRecheckResult } from './identity'

export function listEmployeeLinks() {
  return viGet<{ code: number; msg: string; data: EmployeeLinkAdmin[] }>('/api/admin/employee-links')
}

export function getEmployeeLink(bindingId: string) {
  return viGet<{ code: number; msg: string; data: EmployeeLinkAdmin }>(`/api/admin/employee-links/${bindingId}`)
}

export function suspendEmployeeLink(bindingId: string, expectedVersion: number, reason: string) {
  return viPost<{ code: number; msg: string; data: EmployeeLinkAdmin }>(
    `/api/admin/employee-links/${bindingId}/suspend`, { expectedVersion, reason })
}

export function revokeEmployeeLink(bindingId: string, expectedVersion: number, reason: string) {
  return viPost<{ code: number; msg: string; data: EmployeeLinkAdmin }>(
    `/api/admin/employee-links/${bindingId}/revoke`, { expectedVersion, reason })
}

export function allowEmployeeRematch(bindingId: string, expectedVersion: number, reason: string) {
  return viPost<{ code: number; msg: string; data: EmployeeLinkRecheckResult }>(
    `/api/admin/employee-links/${bindingId}/allow-rematch`, { expectedVersion, reason })
}