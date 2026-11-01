import { viGet, viPost, viPut } from './request'
export interface PriceContent {
  name: string; vehicleTypeCode: string; inspectionTypeCode: string
  inspectionFee: number; driverBaseFee: number; includedDistanceMeter: number
  distanceUnitMeter: number; distanceUnitFee: number
  distanceRounding: 'CEIL_UNIT' | 'EXACT_PRORATA'; routeBasis: 'ONE_WAY' | 'ROUND_TRIP'
  waitingFeeEnabled: boolean; waitingFreeSeconds: number; waitingUnitSeconds: number; waitingUnitFee: number
  effectiveFrom?: string | null; effectiveTo?: string | null
}
export interface PriceRule {
  ruleId: string; ownerDeptId: string; companyName: string; environment: 'TEST' | 'LIVE'
  ruleKey: string; revision: number; status: 'DRAFT' | 'IN_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'DISABLED'
  config: PriceContent | null; version: number; sourceRuleId?: string | null
  createdBy: string; submittedBy?: string | null; approvedBy?: string | null
  contentHash: string; approvedHash?: string | null; publishedAt?: string | null
}
export interface PriceWrite extends Omit<PriceContent, 'effectiveFrom' | 'effectiveTo'> {
  ownerDeptId: string; environment: 'TEST' | 'LIVE'; sourceRuleId?: string; expectedVersion?: number
  effectiveFrom?: string; effectiveTo?: string
}
export interface PriceTrial {
  ruleId: string; revision: number; version: number; ownerDeptId: string; environment: string
  matchedPublished: boolean; routeBasis: string; distanceRounding: string
  oneWayDistanceMeter: number; billableDistanceMeter: number; includedDistanceMeter: number
  excessDistanceMeter: number; chargedUnits: string; inspectionFee: number; driverBaseFee: number
  distanceFee: number; totalFen: number; contentHash: string
}
export const listPrices = (params: Record<string, unknown> = {}) =>
  viGet<{ rows: PriceRule[]; total: number; environment: 'TEST' | 'LIVE' }>('/api/admin/prices', params)
export const createPrice = (body: PriceWrite, key: string) => viPost<{ data: PriceRule }>('/api/admin/prices', body, { idempotencyKey: key })
export const updatePrice = (id: string, body: PriceWrite, key: string) => viPut<{ data: PriceRule }>(`/api/admin/prices/${id}`, body, { idempotencyKey: key })
export const priceAction = (id: string, action: string, body: object, key: string) =>
  viPost<{ data: PriceRule }>(`/api/admin/prices/${id}/${action}`, body, { idempotencyKey: key })
export const trialPrice = (id: string, distanceMeter: number) => viPost<{ data: PriceTrial }>(`/api/admin/prices/${id}/trial`, { distanceMeter })
export const matchPrice = (body: { stationId: string; vehicleTypeCode: string; inspectionTypeCode: string; distanceMeter: number; serviceAt?: string }) =>
  viPost<{ data: PriceTrial }>('/api/admin/prices/trial', body)
