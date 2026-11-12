import { viGet, viPut } from './request';
import { validId } from './aftersale';

export interface ListeningConfig {
  ownerDeptId: string;
  companyName: string;
  timeZone: 'Asia/Shanghai';
  configured: boolean;
  version: number;
  closingTime: string | null;
  locationMaxAgeSeconds: number | null;
  reachabilityMaxAgeSeconds: number | null;
  maxAccuracyMeter: number | null;
  locationReportIntervalSeconds: number | null;
  allowedActions: string[];
}
export interface SaveListeningConfig {
  expectedVersion: number;
  closingTime: string;
  locationMaxAgeSeconds: number;
  reachabilityMaxAgeSeconds: number;
  maxAccuracyMeter: number;
  locationReportIntervalSeconds: number;
  reason: string;
}
const path = (company: string) =>
  '/api/admin/dispatch/companies/' + validId(company) + '/listening-config';
function result(response: { code: number; data: ListeningConfig }, company: string) {
  const value = response?.data;
  if (
    response.code !== 200 ||
    !value ||
    value.ownerDeptId !== company ||
    value.timeZone !== 'Asia/Shanghai' ||
    !Number.isSafeInteger(value.version) ||
    !Array.isArray(value.allowedActions)
  )
    throw new Error('公司听单配置响应尚未确认，请重新读取');
  return value;
}
export async function getListeningConfig(company: string) {
  return result(await viGet<{ code: number; data: ListeningConfig }>(path(company)), company);
}
export async function saveListeningConfig(company: string, body: SaveListeningConfig, key: string) {
  return result(
    await viPut<{ code: number; data: ListeningConfig }>(path(company), body, {
      idempotencyKey: key
    }),
    company
  );
}
