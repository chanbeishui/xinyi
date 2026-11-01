// 机动车检测预约 · 后台车主档案管理（A03，B27，ADMIN 域）
// 一行=车主运营档案（vi_owner_profile 扩展 vi_account）；手机号脱敏；数据范围按订单权威部门，跨公司历史不暴露；
// 维护仅运营字段（服务称呼/标记/备注），不改微信标识/已验证手机，不因标记封禁下单；CAS 不静默覆盖。
import { viGet, viPatch } from './request'

export interface OwnerRecord {
  ownerId: string
  nickname?: string | null
  serviceName?: string | null
  phoneMask?: string | null
  phoneVerified?: boolean
  vehicleCount?: number
  orderCount?: number
  lastServiceAt?: string | null
  serviceFlag?: string
  internalRemark?: string | null
  version: number
  allowedActions?: string[]
}

export interface OwnerVehicleRef {
  vehicleId: string
  plateNo: string
  vehicleTypeCode: string
  brandModel?: string | null
  vinMask?: string | null
  verificationStatus?: string | null
  version: number
}

export interface OwnerOrderRef {
  view: string
  orderNo: string
  fulfillmentStatus: string
  displayStatus: string
  cancelStatus: string
  custody: string
  station?: { stationId: string; name: string; publicAddress: string }
  slotStartAt?: string | null
  version: number
  paymentStatus?: string
}

export function listOwners(pageNum = 1, pageSize = 20, keyword?: string, status?: string) {
  return viGet<{ code: number; msg: string; rows: OwnerRecord[]; total: number }>('/api/admin/owners', { pageNum, pageSize, keyword, status })
}

export function getOwner(ownerId: string) {
  return viGet<{ code: number; msg: string; data: OwnerRecord }>(`/api/admin/owners/${ownerId}`)
}

export function maintainOwner(ownerId: string, input: { expectedVersion: number; reason: string; serviceName?: string; serviceFlag?: string; internalRemark?: string }, idempotencyKey: string) {
  return viPatch<{ code: number; msg: string; data: OwnerRecord }>(`/api/admin/owners/${ownerId}`, input, { idempotencyKey })
}

export function ownerVehicles(ownerId: string, pageNum = 1, pageSize = 20) {
  return viGet<{ code: number; msg: string; rows: OwnerVehicleRef[]; total: number }>(`/api/admin/owners/${ownerId}/vehicles`, { pageNum, pageSize })
}

export function ownerOrders(ownerId: string, pageNum = 1, pageSize = 20) {
  return viGet<{ code: number; msg: string; rows: OwnerOrderRef[]; total: number }>(`/api/admin/owners/${ownerId}/orders`, { pageNum, pageSize })
}