import { request } from '../../services/http';
export interface Capacity { slotId: string; stationId: string; quotaType: string; startAt: string; endAt: string; total: number; held: number; confirmed: number; consumed: number; available: number; enabled: boolean; version: number; canApply?: boolean | null }
export const listCapacity = (slotId?: string, proposedTotal?: number, pageNum = 1) => {
  const query = slotId ? `?slotId=${encodeURIComponent(slotId)}&proposedTotal=${proposedTotal}` : `?pageNum=${pageNum}`;
  return request<{ rows: Capacity[]; total: number }>('GET', '/api/app/station/capacity' + query);
};
export const changeCapacity = (id: string, input: { total: number; expectedVersion: number; reason: string; enabled: boolean }, key: string) =>
  request<{ data: Capacity }>('PUT', `/api/app/station/capacity/${id}`, input, key);
