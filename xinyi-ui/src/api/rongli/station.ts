// 机动车检测预约 · 后台检测点管理（C02，B17，ADMIN 域 API_128/129/130）
// 公开资料/资质/营业时间/支持车型/服务范围；联系电话脱敏；版本必填 CAS；启停；迁部门有在手订单受控拒绝。
import { viGet, viPost, viPut } from './request'

export interface GeoPoint {
  lng: number
  lat: number
  coordinateSystem: string
}

export interface StationRecord {
  stationId: string
  deptId: string
  name: string
  address: string
  location: GeoPoint
  phoneMask?: string | null
  enabled: boolean
  supportedVehicleTypeCodes: string[]
  businessHours?: string | null
  serviceRadiusMeter?: number
  version: number
}

export interface StationWrite {
  name: string
  address: string
  location: GeoPoint
  contactPhone?: string
  enabled?: boolean
  supportedVehicleTypeCodes: string[]
  businessHours?: string
  serviceRadiusMeter?: number
  qualificationFileIds?: string[]
  expectedVersion?: number
  deptId?: string
}

export function listStations(pageNum = 1, pageSize = 20) {
  return viGet<{ code: number; msg: string; rows: StationRecord[]; total: number }>('/api/admin/stations', { pageNum, pageSize })
}

export function createStation(input: StationWrite, idempotencyKey: string) {
  return viPost<{ code: number; msg: string; data: StationRecord }>('/api/admin/stations', input, { idempotencyKey })
}

export function updateStation(stationId: string, input: StationWrite, idempotencyKey: string) {
  return viPut<{ code: number; msg: string; data: StationRecord }>(`/api/admin/stations/${stationId}`, input, { idempotencyKey })
}