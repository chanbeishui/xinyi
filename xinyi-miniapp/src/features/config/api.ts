import { request } from '../../services/http';

// C01 字典与公开服务配置（MINI/PUBLIC）。
// 公开配置仅白名单发布值：缺已发布协议不开放下单、第三方未配置不宣称可支付、不含商户密钥。
// 业务参数不作为客户端权限依据；最终支付方式以本人订单 payment-options 为准。
export interface PublicServiceConfig {
  version: number;
  companyName: string;
  supportPhone: string;
  termsVersion: string;
  termsText: string;
  privacyVersion: string;
  privacyText: string;
  serviceCities: string[];
  bookingEnabled: boolean;
  availablePaymentMethods: string[];
  paymentNotice: string;
}

export interface DictItem {
  code: string;
  label: string;
  sort: number;
  enabled: boolean;
  version: number;
}

// 公开服务说明与已发布协议（登录前可读，PUBLIC 白名单）
export async function getPublicServiceConfig(): Promise<PublicServiceConfig> {
  const r = await request<{ code: number; msg: string; data: PublicServiceConfig }>('GET', '/api/public/service-config');
  return r.data;
}

// 业务字典（车型 vi_vehicle_type / 检测类型 vi_inspection_type）；契约 rows/total 分页信封
export async function getDict(dictType: string, pageNum = 1, pageSize = 100): Promise<DictItem[]> {
  const r = await request<{ code: number; msg: string; rows: DictItem[]; total: number }>(
    'GET', '/api/common/dict/' + encodeURIComponent(dictType) + '?pageNum=' + pageNum + '&pageSize=' + pageSize);
  return r.rows;
}