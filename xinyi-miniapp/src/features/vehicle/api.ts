import { request } from '../../services/http';

// A01 本人车辆（MINI）。CRUD/常用/软删；同车主车牌冲突409；生成列不入DTO。
export interface Vehicle {
  vehicleId: string;
  plateNo: string;
  vehicleTypeCode: string;
  vehicleTypeLabel?: string | null;
  brandModel?: string | null;
  version: number;
  isDefault: boolean;
}

export async function listVehicles(): Promise<Vehicle[]> {
  const r = await request<{ code: number; msg: string; data: Vehicle[] }>('GET', '/api/app/owner/vehicles');
  return r.data;
}

export async function createVehicle(input: { plateNo: string; vehicleTypeCode: string; brandModel?: string; useType?: string; vinLast4?: string; isDefault?: boolean }, idem: string): Promise<Vehicle> {
  const r = await request<{ code: number; msg: string; data: Vehicle }>('POST', '/api/app/owner/vehicles', input, idem);
  return r.data;
}

export async function updateVehicle(id: string, input: { plateNo: string; vehicleTypeCode: string; brandModel?: string; useType?: string; vinLast4?: string; isDefault?: boolean; expectedVersion: number }, idem: string): Promise<Vehicle> {
  const r = await request<{ code: number; msg: string; data: Vehicle }>('PUT', '/api/app/owner/vehicles/' + id, input, idem);
  return r.data;
}

export async function removeVehicle(id: string, expectedVersion: number, reason: string, idem: string): Promise<void> {
  await request('DELETE', '/api/app/owner/vehicles/' + id, { expectedVersion, reason }, idem);
}

export async function setDefaultVehicle(id: string, expectedVersion: number, reason: string, idem: string): Promise<Vehicle> {
  const r = await request<{ code: number; msg: string; data: Vehicle }>('POST', '/api/app/owner/vehicles/' + id + '/default', { expectedVersion, reason }, idem);
  return r.data;
}