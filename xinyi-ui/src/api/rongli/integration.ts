import request from '@/utils/request'

/**
 * V6.4 第三方服务接入配置接口。
 *
 * 统一规则：读取是脱敏视图（密钥只有“已配置/指纹/写入时间”），写入走
 * 草稿 → 校验 → 发布三步；任何一步失败都保留原生效版本不变。
 */

const BASE = '/api/admin/integrations'

export function listProfiles(params: any): Promise<any> {
  return request({ url: `${BASE}/profiles`, method: 'get', params })
}
export function createProfile(data: any): Promise<any> {
  return request({ url: `${BASE}/profiles`, method: 'post', data })
}
export function getProfile(profileId: string | number): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}`, method: 'get' })
}
export function saveDraft(profileId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/draft`, method: 'put', data })
}
export function checkProfile(profileId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/checks`, method: 'post', data })
}
export function publishProfile(profileId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/publish`, method: 'post', data })
}
export function rollbackProfile(profileId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/rollback`, method: 'post', data })
}
export function listRevisions(profileId: string | number, params: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/revisions`, method: 'get', params })
}
export function listPublications(profileId: string | number, params: any): Promise<any> {
  return request({ url: `${BASE}/profiles/${profileId}/publications`, method: 'get', params })
}

// ---------------------------------------------------------------- 模板
export function listTemplates(params: any): Promise<any> {
  return request({ url: `${BASE}/templates`, method: 'get', params })
}
export function createTemplate(data: any): Promise<any> {
  return request({ url: `${BASE}/templates`, method: 'post', data })
}
export function getTemplate(templateId: string | number): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}`, method: 'get' })
}
export function saveTemplateDraft(templateId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/draft`, method: 'put', data })
}
export function checkTemplate(templateId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/checks`, method: 'post', data })
}
export function previewTemplate(templateId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/preview`, method: 'post', data })
}
export function publishTemplate(templateId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/publish`, method: 'post', data })
}
export function listTemplatePublications(templateId: string | number, params: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/publications`, method: 'get', params })
}
export function testTemplate(templateId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/templates/${templateId}/test`, method: 'post', data })
}

// ---------------------------------------------------------------- 路由与诊断
export function listRoutes(params: any): Promise<any> {
  return request({ url: `${BASE}/routes`, method: 'get', params })
}
export function createRoute(data: any): Promise<any> {
  return request({ url: `${BASE}/routes`, method: 'post', data })
}
export function updateRoute(routeId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/routes/${routeId}`, method: 'put', data })
}
export function listDeliveries(params: any): Promise<any> {
  return request({ url: `${BASE}/deliveries`, method: 'get', params })
}
export function queryReceipt(deliveryId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/deliveries/${deliveryId}/query`, method: 'post', data })
}
export function retryDelivery(deliveryId: string | number, data: any): Promise<any> {
  return request({ url: `${BASE}/deliveries/${deliveryId}/retry`, method: 'post', data })
}

// ---------------------------------------------------------------- 测试收件人
export function listTestRecipients(params: any): Promise<any> {
  return request({ url: `${BASE}/test-recipients`, method: 'get', params })
}
export function addTestRecipient(data: any): Promise<any> {
  return request({ url: `${BASE}/test-recipients`, method: 'post', data })
}
export function revokeTestRecipient(recipientId: string | number): Promise<any> {
  return request({ url: `${BASE}/test-recipients/${recipientId}/revoke`, method: 'post' })
}

// ---------------------------------------------------------------- 客户线下资金
export function listOfflinePayments(params: any): Promise<any> {
  return request({ url: '/api/admin/offline-payments', method: 'get', params })
}
export function getOfflinePayment(proofId: string | number): Promise<any> {
  return request({ url: `/api/admin/offline-payments/${proofId}`, method: 'get' })
}
export function registerOfflinePayment(data: any): Promise<any> {
  return request({ url: '/api/admin/offline-payments', method: 'post', data })
}
export function verifyOfflinePayment(proofId: string | number, data: any): Promise<any> {
  return request({ url: `/api/admin/offline-payments/${proofId}/verify`, method: 'post', data })
}
