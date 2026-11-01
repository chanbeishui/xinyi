import { request } from '../../services/http';

// A02 地址簿（MINI）。地图失败可手填地址+坐标后解析；临时地址不强制先保存（预约可携带草稿）。
export interface GeoPoint { lng: number; lat: number; coordinateSystem: 'GCJ02' }
export interface Address {
  addressId: string;
  label: string | null;
  address: string;
  location: GeoPoint;
  isDefault: boolean;
  version: number;
}

export async function listAddresses(): Promise<Address[]> {
  const r = await request<{ code: number; msg: string; data: Address[] }>('GET', '/api/app/owner/addresses');
  return r.data;
}

export async function createAddress(input: { label?: string; address: string; location: GeoPoint; isDefault?: boolean }, idem: string): Promise<Address> {
  const r = await request<{ code: number; msg: string; data: Address }>('POST', '/api/app/owner/addresses', input, idem);
  return r.data;
}

export async function updateAddress(id: string, input: { label?: string; address: string; location: GeoPoint; isDefault?: boolean; expectedVersion: number }, idem: string): Promise<Address> {
  const r = await request<{ code: number; msg: string; data: Address }>('PUT', '/api/app/owner/addresses/' + id, input, idem);
  return r.data;
}

export async function removeAddress(id: string, expectedVersion: number, reason: string, idem: string): Promise<void> {
  await request('DELETE', '/api/app/owner/addresses/' + id, { expectedVersion, reason }, idem);
}

export async function setDefaultAddress(id: string, expectedVersion: number, reason: string, idem: string): Promise<Address> {
  const r = await request<{ code: number; msg: string; data: Address }>('POST', '/api/app/owner/addresses/' + id + '/default', { expectedVersion, reason }, idem);
  return r.data;
}