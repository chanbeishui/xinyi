import { http, uploadFile } from '@/utils/request'

/** 公共域：登录、会话、档案、字典、站点、消息、隐私 */
export const commonApi = {
  login: (data: { code: string; nickname?: string; deviceLabel?: string }) =>
    http.post('/api/common/login/wechat', data),
  session: () => http.get('/api/common/session'),
  logout: (all = false) => http.post(`/api/common/session/logout?all=${all}`),
  switchRole: (role: string) => http.post('/api/common/session/role', { role }),
  switchStation: (stationId: number | string) => http.post('/api/common/session/station', { stationId }),
  profile: () => http.get('/api/common/profile'),
  patchProfile: (data: { nickname?: string; serviceName?: string }) => http.post('/api/common/profile', data),
  bindPhone: (phone: string, smsCode: string) => http.post('/api/common/phone/bind', { phone, smsCode }),
  sendSms: (phone: string) => http.post('/api/common/phone/sms/send', { phone }),
  balanceSheet: (dictType: string) => http.get(`/api/common/dict/${dictType}`),
  stations: () => http.get('/api/common/stations'),
  messages: (pageNum = 1, pageSize = 20) => http.get('/api/common/messages', { pageNum, pageSize }),
  readMessage: (id: number | string) => http.post(`/api/common/messages/${id}/read`),
  createPrivacy: (reason: string, orderNo?: string) => http.post('/api/common/privacy-cases', { reason, orderNo }),
  privacyCases: () => http.get('/api/common/privacy-cases'),
  bindingRequest: (reason?: string) => http.post('/api/common/employee-binding-requests', { reason }),
  bindingRequests: () => http.get('/api/common/employee-binding-requests'),
  bindEmployee: (credentialToken: string) => http.post('/api/common/employee-binding', { credentialToken }),
  contactAccess: (orderNo: string, purpose: string) =>
    http.post(`/api/common/orders/${orderNo}/contact-access`, { purpose }),
  fileAccess: (fileId: number | string) => http.post(`/api/common/files/${fileId}/access`),
  upload: (filePath: string, purpose: string, orderNo?: string) => uploadFile(filePath, purpose, orderNo)
}

/** 车主端 */
export const ownerApi = {
  vehicles: () => http.get('/api/app/owner/vehicles'),
  createVehicle: (data: any) => http.post('/api/app/owner/vehicles', data),
  updateVehicle: (id: number | string, data: any) => http.put(`/api/app/owner/vehicles/${id}`, data),
  deleteVehicle: (id: number | string) => http.del(`/api/app/owner/vehicles/${id}`),
  defaultVehicle: (id: number | string) => http.post(`/api/app/owner/vehicles/${id}/default`),
  addresses: () => http.get('/api/app/owner/addresses'),
  createAddress: (data: any) => http.post('/api/app/owner/addresses', data),
  updateAddress: (id: number | string, data: any) => http.put(`/api/app/owner/addresses/${id}`, data),
  deleteAddress: (id: number | string) => http.del(`/api/app/owner/addresses/${id}`),
  defaultAddress: (id: number | string) => http.post(`/api/app/owner/addresses/${id}/default`),
  slots: () => http.get('/api/app/owner/schedule/slots'),
  quote: (data: any) => http.post('/api/app/owner/orders/quote', data),
  createOrder: (data: any) => http.post('/api/app/owner/orders', data),
  orders: (status?: string, pageNum = 1, pageSize = 10) =>
    http.get('/api/app/owner/orders', { status: status || undefined, pageNum, pageSize }),
  order: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}`),
  timeline: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/timeline`),
  prepay: (orderNo: string) => http.post(`/api/app/owner/orders/${orderNo}/prepay`),
  mockPay: (orderNo: string) => http.post(`/api/app/owner/orders/${orderNo}/mock-pay`),
  payment: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/payment`),
  closeUnpaid: (orderNo: string, reason?: string) =>
    http.post(`/api/app/owner/orders/${orderNo}/close-unpaid`, { reason }),
  confirmService: (orderNo: string, version?: number) =>
    http.post(`/api/app/owner/orders/${orderNo}/service-confirmation`, { version }),
  result: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/result`),
  evidence: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/evidence`),
  snapshot: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/service-snapshot`),
  createDispute: (orderNo: string, data: any) => http.post(`/api/app/owner/orders/${orderNo}/disputes`, data),
  disputes: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/disputes`),
  review: (orderNo: string, data: any) => http.post(`/api/app/owner/orders/${orderNo}/reviews`, data),
  createAfterSale: (orderNo: string, data: any) => http.post(`/api/app/owner/orders/${orderNo}/after-sales`, data),
  afterSalesOfOrder: (orderNo: string) => http.get(`/api/app/owner/orders/${orderNo}/after-sales`),
  afterSales: (pageNum = 1, pageSize = 10) => http.get('/api/app/owner/after-sales', { pageNum, pageSize }),
  afterSale: (caseId: number | string) => http.get(`/api/app/owner/after-sales/${caseId}`),
  afterSaleNote: (caseId: number | string, content: string) =>
    http.post(`/api/app/owner/after-sales/${caseId}/notes`, { content })
}

/** 代驾端 */
export const driverApi = {
  qualification: () => http.get('/api/app/driver/qualification'),
  createQualification: (data: any) => http.post('/api/app/driver/qualification', data),
  updateQualification: (data: any) => http.put('/api/app/driver/qualification', data),
  submitQualification: () => http.post('/api/app/driver/qualification/submit'),
  createFaceSession: () => http.post('/api/app/driver/qualification/face-session'),
  mockFacePass: (sessionId: string) =>
    http.post(`/api/app/driver/qualification/face-sessions/${sessionId}/mock-pass`),
  faceSession: (sessionId: string) => http.get(`/api/app/driver/qualification/face-sessions/${sessionId}`),
  training: () => http.get('/api/app/driver/qualification/training'),
  trainingCourse: (trainingId: number | string) => http.get(`/api/app/driver/qualification/training/${trainingId}`),
  startPlayback: (trainingId: number | string) =>
    http.post(`/api/app/driver/qualification/training/${trainingId}/playback`),
  reportProgress: (trainingId: number | string, data: any) =>
    http.post(`/api/app/driver/qualification/training/${trainingId}/progress`, data),
  createAttempt: (trainingId: number | string) =>
    http.post(`/api/app/driver/qualification/training/${trainingId}/attempts`),
  attempt: (attemptId: number | string) => http.get(`/api/app/driver/qualification/training/attempts/${attemptId}`),
  submitAttempt: (data: any) => http.post('/api/app/driver/qualification/training/submit', data),
  availability: () => http.get('/api/app/driver/availability'),
  // 后端同时提供 PATCH/POST /listening；小程序端 uni.request 不支持 PATCH，统一走 POST
  setListening: (enabled: boolean) => http.post('/api/app/driver/listening', { enabled }),
  reportLocation: (lng: number, lat: number) =>
    http.post('/api/app/driver/location', { lng, lat, capturedAt: new Date().toISOString() }),
  invites: () => http.get('/api/app/driver/invites'),
  invite: (inviteId: number | string) => http.get(`/api/app/driver/invites/${inviteId}`),
  respondInvite: (inviteId: number | string, accept: boolean, reason?: string) =>
    http.post(`/api/app/driver/invites/${inviteId}/response`, { accept, reason }),
  currentAssignment: () => http.get('/api/app/driver/assignments/current'),
  orders: (status?: string, pageNum = 1, pageSize = 10) =>
    http.get('/api/app/driver/orders', { status: status || undefined, pageNum, pageSize }),
  order: (orderNo: string) => http.get(`/api/app/driver/orders/${orderNo}`),
  depart: (orderNo: string) => http.post(`/api/app/driver/orders/${orderNo}/depart`),
  arrivePickup: (orderNo: string) => http.post(`/api/app/driver/orders/${orderNo}/arrive-pickup`),
  arriveStation: (orderNo: string) => http.post(`/api/app/driver/orders/${orderNo}/arrive-station`),
  cancelAssignment: (orderNo: string, reason?: string) =>
    http.post(`/api/app/driver/orders/${orderNo}/assignment-cancellation`, { reason }),
  reportException: (orderNo: string, data: any) =>
    http.post(`/api/app/driver/orders/${orderNo}/exceptions`, data),
  createEvidence: (orderNo: string, data: any) => http.post(`/api/app/driver/orders/${orderNo}/evidence`, data),
  evidence: (orderNo: string) => http.get(`/api/app/driver/orders/${orderNo}/evidence`),
  addPhoto: (orderNo: string, groupId: number | string, data: any) =>
    http.post(`/api/app/driver/orders/${orderNo}/evidence/${groupId}/photos`, data),
  setMileage: (orderNo: string, groupId: number | string, data: any) =>
    http.put(`/api/app/driver/orders/${orderNo}/evidence/${groupId}/mileage`, data),
  evidenceReady: (orderNo: string, groupId: number | string) =>
    http.post(`/api/app/driver/orders/${orderNo}/evidence/${groupId}/ready`),
  statements: () => http.get('/api/app/driver/statements'),
  statement: (statementId: number | string) => http.get(`/api/app/driver/statements/${statementId}`),
  confirmStatement: (statementId: number | string, data?: any) =>
    http.post(`/api/app/driver/statements/${statementId}/confirm`, data || {}),
  statementDifference: (statementId: number | string, data: any) =>
    http.post(`/api/app/driver/statements/${statementId}/differences`, data),
  // 后端按日期区间与关键字筛选，返回 { total, rows }
  reconciliationEntries: (params?: { from?: string; to?: string; keyword?: string }) =>
    http.get('/api/app/driver/reconciliation-entries', params || {}),
  paymentRecords: () => http.get('/api/app/driver/payment-records'),
  penalties: () => http.get('/api/app/driver/penalties'),
  penalty: (penaltyId: number | string) => http.get(`/api/app/driver/penalties/${penaltyId}`),
  appeal: (penaltyId: number | string, data: any) =>
    http.post(`/api/app/driver/penalties/${penaltyId}/appeals`, data),
  appeals: () => http.get('/api/app/driver/penalty-appeals'),
  appealDetail: (appealId: number | string) => http.get(`/api/app/driver/penalty-appeals/${appealId}`),
  profile: () => http.get('/api/app/driver/profile')
}

/** 检测员工端 */
export const stationApi = {
  authorizedStations: () => http.get('/api/app/station/authorized-stations'),
  info: () => http.get('/api/app/station/info'),
  updateInfo: (data: any) => http.put('/api/app/station/info', data),
  orders: (status?: string, pageNum = 1, pageSize = 10) =>
    http.get('/api/app/station/orders', { status: status || undefined, pageNum, pageSize }),
  order: (orderNo: string) => http.get(`/api/app/station/orders/${orderNo}`),
  license: (orderNo: string) => http.get(`/api/app/station/orders/${orderNo}/license`),
  saveLicense: (orderNo: string, data: any) => http.put(`/api/app/station/orders/${orderNo}/license`, data),
  results: (orderNo: string) => http.get(`/api/app/station/orders/${orderNo}/results`),
  submitResult: (orderNo: string, data: any) => http.post(`/api/app/station/orders/${orderNo}/results`, data),
  correction: (orderNo: string, data: any) =>
    http.post(`/api/app/station/orders/${orderNo}/result-corrections`, data),
  pickupNotification: (orderNo: string) => http.post(`/api/app/station/orders/${orderNo}/pickup-notification`),
  reportException: (orderNo: string, data: any) => http.post(`/api/app/station/orders/${orderNo}/exceptions`, data),
  capacity: () => http.get('/api/app/station/capacity'),
  updateCapacity: (slotId: number | string, data: any) => http.put(`/api/app/station/capacity/${slotId}`, data)
}

/** 交接（车主与代驾共用） */
export const handoverApi = {
  create: (orderNo: string, handoverType: string) =>
    http.post(`/api/common/orders/${orderNo}/handovers`, { handoverType }),
  detail: (orderNo: string, handoverId: number | string) =>
    http.get(`/api/common/orders/${orderNo}/handovers/${handoverId}`),
  secret: (orderNo: string, handoverId: number | string) =>
    http.get(`/api/common/orders/${orderNo}/handovers/${handoverId}/secret`),
  confirm: (orderNo: string, handoverId: number | string, data: any) =>
    http.post(`/api/common/orders/${orderNo}/handovers/${handoverId}/confirm`, data)
}
