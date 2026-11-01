import { request } from '../../services/http';

// C02 检测点（MINI）。公开营业列表仅启用站点、电话脱敏、不含内部状态；
// 员工本站点信息须切换到 STATION 工作身份（API_095/096），员工端不接受迁部门。
export interface GeoPoint {
  lng: number;
  lat: number;
  coordinateSystem: string;
}

export interface PublicStation {
  stationId: string;
  name: string;
  address: string;
  location: GeoPoint;
  phoneMask?: string | null;
  supportedVehicleTypeCodes: string[];
  businessHours?: string | null;
  serviceRadiusMeter?: number;
}

export interface Station extends PublicStation {
  enabled: boolean;
  version: number;
}

export interface StationWrite {
  name: string;
  address: string;
  location: GeoPoint;
  contactPhone?: string;
  enabled?: boolean;
  supportedVehicleTypeCodes: string[];
  businessHours?: string;
  serviceRadiusMeter?: number;
  qualificationFileIds?: string[];
  expectedVersion: number;
}

// 公开营业站点（API_011，MINI 登录态可读）；rows/total 信封
export async function listPublicStations(pageNum = 1, pageSize = 20): Promise<PublicStation[]> {
  const r = await request<{ code: number; msg: string; rows: PublicStation[]; total: number }>(
    'GET', '/api/common/stations?pageNum=' + pageNum + '&pageSize=' + pageSize);
  return r.rows;
}

// 员工本站点信息（API_095，须 STATION 工作身份）
export async function getOwnStation(): Promise<Station> {
  const r = await request<{ code: number; msg: string; data: Station }>('GET', '/api/app/station/info');
  return r.data;
}

// 员工更新本站点（API_096，CAS；不含 deptId 迁移）
export async function updateOwnStation(input: StationWrite, idem: string): Promise<Station> {
  const r = await request<{ code: number; msg: string; data: Station }>('PUT', '/api/app/station/info', input, idem);
  return r.data;
}