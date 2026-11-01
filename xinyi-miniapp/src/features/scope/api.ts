import { request } from '../../services/http';

// I06 员工获授权站点（S18，API_094，MINI）。仅本人有效绑定+启用站点。
export interface AuthorizedStation {
  stationId: string;
  name: string;
  address: string;
  location?: { lng: number; lat: number; coordinateSystem: string };
  enabled: boolean;
  supportedVehicleTypeCodes?: string[];
  businessHours?: string;
  serviceRadiusMeter?: number;
  version: number;
}

export async function getAuthorizedStations(): Promise<AuthorizedStation[]> {
  const resp = await request<{ code: number; msg: string; data: AuthorizedStation[] }>(
    'GET', '/api/app/station/authorized-stations');
  return resp.data;
}