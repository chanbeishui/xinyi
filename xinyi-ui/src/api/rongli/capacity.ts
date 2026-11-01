import { viGet, viPost, viPut } from './request'

export interface Capacity {
  slotId: string
  stationId: string
  quotaType: 'PICKUP' | 'STATION_RECEIVE'
  startAt: string
  endAt: string
  total: number
  held: number
  confirmed: number
  consumed: number
  available: number
  enabled: boolean
  version: number
  proposedTotal?: number | null
  canApply?: boolean | null
}
export interface CapacityChange { total: number; expectedVersion: number; reason: string; enabled?: boolean }
export interface CapacityCreate { stationId: string; quotaType: Capacity['quotaType']; startAt: string; endAt: string; total: number; reason: string }
export const listCapacity = (params: Record<string, unknown> = {}) =>
  viGet<{ rows: Capacity[]; total: number }>('/api/admin/capacity', params)
export const createCapacity = (input: CapacityCreate, key: string) =>
  viPost<{ data: Capacity }>('/api/admin/capacity', input, { idempotencyKey: key })
export const changeCapacity = (id: string, input: CapacityChange, key: string) =>
  viPut<{ data: Capacity }>(`/api/admin/capacity/${id}`, input, { idempotencyKey: key })
