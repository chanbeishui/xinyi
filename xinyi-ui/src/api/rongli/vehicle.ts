// 机动车检测预约 · 后台车辆管理（A01，B09，ADMIN 域）
// 一行=客户-车辆关系；VIN/手机号脱敏；维护CAS不追改订单快照；功能权限服务端校验。
import { viGet, viPatch } from './request'

export interface VehicleRecord {
  vehicleId: string
  plateNo: string
  vehicleTypeCode: string
  vehicleTypeLabel?: string | null
  brandModel?: string | null
  vinMask?: string | null
  registeredAt?: string | null
  useType?: string | null
  ownerRelation?: { ownerId: string; phoneMask: string | null }
  verificationStatus?: string
  lastResult?: string | null
  lastInspectAt?: string | null
  version: number
  allowedActions?: string[]
}

export function listVehicles(pageNum = 1, pageSize = 10) {
  return viGet<{ code: number; msg: string; data: VehicleRecord[] }>('/api/admin/vehicles', { pageNum, pageSize })
}

export function getVehicle(vehicleId: string) {
  return viGet<{ code: number; msg: string; data: VehicleRecord }>(`/api/admin/vehicles/${vehicleId}`)
}

export function maintainVehicle(vehicleId: string, input: { expectedVersion: number; reason: string; brandModel?: string; vehicleTypeCode?: string; useType?: string; registeredAt?: string }) {
  return viPatch<{ code: number; msg: string; data: VehicleRecord }>(`/api/admin/vehicles/${vehicleId}`, input)
}

export function vehicleOrders(vehicleId: string) {
  return viGet<{ code: number; msg: string; data: Array<{ orderNo: string; fulfillmentStatus: string; createdAt: string }> }>(`/api/admin/vehicles/${vehicleId}/orders`)
}

export function vehicleResults(vehicleId: string) {
  return viGet<{ code: number; msg: string; data: Array<{ orderNo: string; conclusion: string; inspectedAt: string }> }>(`/api/admin/vehicles/${vehicleId}/inspection-results`)
}