import request from '@/utils/request'

/**
 * 荣利机动车检测预约管理端接口。
 *
 * 全部接口返回业务视图对象（非框架 AjaxResult），错误体为 {code,message}，
 * 由 request 拦截器统一弹出提示。
 */

// ---------------------------------------------------------------- 订单
export function listOrders(params: any): Promise<any> {
  return request({ url: '/api/admin/orders', method: 'get', params })
}
export function getOrder(orderNo: string): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}`, method: 'get' })
}
export function getOrderLicense(orderNo: string): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/license`, method: 'get' })
}
export function saveOrderLicense(orderNo: string, data: any): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/license`, method: 'put', data })
}
export function getOrderResults(orderNo: string): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/results`, method: 'get' })
}
export function submitOrderResult(orderNo: string, data: any): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/results`, method: 'post', data })
}
export function applyResultCorrection(orderNo: string, data: any): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/result-corrections`, method: 'post', data })
}
export function notifyPickup(orderNo: string): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/pickup-notification`, method: 'post' })
}
export function reportOrderException(orderNo: string, data: any): Promise<any> {
  return request({ url: `/api/admin/orders/${orderNo}/exceptions`, method: 'post', data })
}
export function listCapacity(stationId: number | string): Promise<any> {
  return request({ url: '/api/admin/capacity', method: 'get', params: { stationId } })
}
export function updateCapacity(slotId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/capacity/${slotId}`, method: 'put', data })
}

// ---------------------------------------------------------------- 档案
export function listOwners(params: any): Promise<any> {
  return request({ url: '/api/admin/owners', method: 'get', params })
}
export function getOwner(ownerId: number | string): Promise<any> {
  return request({ url: `/api/admin/owners/${ownerId}`, method: 'get' })
}
export function updateOwner(ownerId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/owners/${ownerId}`, method: 'patch', data })
}
export function listOwnerVehicles(ownerId: number | string): Promise<any> {
  return request({ url: `/api/admin/owners/${ownerId}/vehicles`, method: 'get' })
}
export function listOwnerOrders(ownerId: number | string): Promise<any> {
  return request({ url: `/api/admin/owners/${ownerId}/orders`, method: 'get' })
}

export function listDrivers(params: any): Promise<any> {
  return request({ url: '/api/admin/drivers', method: 'get', params })
}
export function getDriver(driverId: number | string): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}`, method: 'get' })
}
export function updateDriver(driverId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}`, method: 'patch', data })
}
export function disableDriverNewAssignments(driverId: number | string, disabled: boolean): Promise<any> {
  return request({
    url: `/api/admin/drivers/${driverId}/disable-new-assignments`,
    method: 'post',
    data: { disabled }
  })
}
export function listDriverAssignments(driverId: number | string, params?: any): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}/assignments`, method: 'get', params })
}
export function getDriverFeeLedger(driverId: number | string, params?: any): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}/fee-ledger`, method: 'get', params })
}

export function listVehicles(params: any): Promise<any> {
  return request({ url: '/api/admin/vehicles', method: 'get', params })
}
export function getVehicle(vehicleId: number | string): Promise<any> {
  return request({ url: `/api/admin/vehicles/${vehicleId}`, method: 'get' })
}
export function updateVehicle(vehicleId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/vehicles/${vehicleId}`, method: 'patch', data })
}
export function listVehicleOrders(vehicleId: number | string): Promise<any> {
  return request({ url: `/api/admin/vehicles/${vehicleId}/orders`, method: 'get' })
}
export function listVehicleResults(vehicleId: number | string): Promise<any> {
  return request({ url: `/api/admin/vehicles/${vehicleId}/inspection-results`, method: 'get' })
}

export function listStations(params?: any): Promise<any> {
  return request({ url: '/api/admin/stations', method: 'get', params })
}
export function createStation(data: any): Promise<any> {
  return request({ url: '/api/admin/stations', method: 'post', data })
}
export function updateStation(stationId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/stations/${stationId}`, method: 'put', data })
}
export function listStationGrants(params?: any): Promise<any> {
  return request({ url: '/api/admin/station-grants', method: 'get', params })
}
export function updateStationGrant(employeeUserId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/station-grants/${employeeUserId}`, method: 'put', data })
}

// ---------------------------------------------------------------- 计价与费用规则
export function listPrices(params?: any): Promise<any> {
  return request({ url: '/api/admin/prices', method: 'get', params })
}
export function createPrice(data: any): Promise<any> {
  return request({ url: '/api/admin/prices', method: 'post', data })
}
export function updatePrice(ruleId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/prices/${ruleId}`, method: 'put', data })
}
export function submitPrice(ruleId: number | string): Promise<any> {
  return request({ url: `/api/admin/prices/${ruleId}/submit`, method: 'post' })
}
export function auditPrice(ruleId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/prices/${ruleId}/audit`, method: 'post', data })
}
export function publishPrice(ruleId: number | string): Promise<any> {
  return request({ url: `/api/admin/prices/${ruleId}/publish`, method: 'post' })
}

export function listFeeRules(params?: any): Promise<any> {
  return request({ url: '/api/admin/driver-fee-rules', method: 'get', params })
}
export function createFeeRule(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-fee-rules', method: 'post', data })
}
export function getFeeRule(ruleId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-fee-rules/${ruleId}`, method: 'get' })
}
export function updateFeeRule(ruleId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-fee-rules/${ruleId}`, method: 'put', data })
}
export function publishFeeRule(ruleId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-fee-rules/${ruleId}/publish`, method: 'post' })
}
export function trialFeeRule(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-fee-rules/trial', method: 'post', data })
}

// ---------------------------------------------------------------- 派单
export function getDashboard(): Promise<any> {
  return request({ url: '/api/admin/dashboard', method: 'get' })
}
export function listDispatchQueue(params?: any): Promise<any> {
  return request({ url: '/api/admin/dispatch/queue', method: 'get', params })
}
export function listCandidates(keyword?: string): Promise<any> {
  return request({ url: '/api/admin/dispatch/candidates', method: 'get', params: { keyword } })
}
export function manualInvite(data: any): Promise<any> {
  return request({ url: '/api/admin/dispatch/manual-invites', method: 'post', data })
}
/** 立即派单：跳过「接车前提前量」等待，按自动规则当场发起一轮邀约 */
export function dispatchOrderNow(orderNo: string): Promise<any> {
  return request({ url: `/api/admin/dispatch/orders/${orderNo}/dispatch-now`, method: 'post' })
}
export function listInvites(params?: any): Promise<any> {
  return request({ url: '/api/admin/dispatch/invites', method: 'get', params })
}
export function getDispatchConfig(): Promise<any> {
  return request({ url: '/api/admin/dispatch/config', method: 'get' })
}
export function saveDispatchConfig(data: any): Promise<any> {
  return request({ url: '/api/admin/dispatch/config', method: 'put', data })
}
export function previewDispatchPlan(data?: any): Promise<any> {
  return request({ url: '/api/admin/dispatch/plans/preview', method: 'post', data })
}
export function applyDispatchPlan(planId: string | number, leadMinutes?: number): Promise<any> {
  return request({
    url: `/api/admin/dispatch/plans/${planId}/apply`,
    method: 'post',
    data: { leadMinutes }
  })
}

// ---------------------------------------------------------------- 售后
export function listAfterSales(params: any): Promise<any> {
  return request({ url: '/api/admin/after-sales', method: 'get', params })
}
export function getAfterSale(caseId: number | string): Promise<any> {
  return request({ url: `/api/admin/after-sales/${caseId}`, method: 'get' })
}
export function addAfterSaleNote(caseId: number | string, content: string): Promise<any> {
  return request({ url: `/api/admin/after-sales/${caseId}/notes`, method: 'post', data: { content } })
}
export function reviewPreview(caseId: number | string, data?: any): Promise<any> {
  return request({ url: `/api/admin/after-sales/${caseId}/review-preview`, method: 'post', data })
}
export function reviewAfterSale(caseId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/after-sales/${caseId}/review`, method: 'post', data })
}
export function getRefund(refundNo: string): Promise<any> {
  return request({ url: `/api/admin/refunds/${refundNo}`, method: 'get' })
}
export function queryRefund(refundNo: string): Promise<any> {
  return request({ url: `/api/admin/refunds/${refundNo}/query`, method: 'post' })
}
export function retryRefund(refundNo: string): Promise<any> {
  return request({ url: `/api/admin/refunds/${refundNo}/retry`, method: 'post' })
}

// ---------------------------------------------------------------- 对账
export function previewStatement(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-statements/preview', method: 'post', data })
}
export function createStatement(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-statements', method: 'post', data })
}
export function listStatements(params: any): Promise<any> {
  return request({ url: '/api/admin/driver-statements', method: 'get', params })
}
export function getStatement(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}`, method: 'get' })
}
export function getStatementRevision(statementId: number | string, revision: number): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/revisions/${revision}`, method: 'get' })
}
export function addStatementLine(statementId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/lines/add`, method: 'post', data })
}
export function removeStatementLine(statementId: number | string, lineId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/lines/${lineId}/remove`, method: 'post' })
}
export function priceStatementLine(statementId: number | string, lineId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/lines/${lineId}/price`, method: 'put', data })
}
export function statementLinePriceHistory(statementId: number | string, lineId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/lines/${lineId}/price-history`, method: 'get' })
}
export function submitStatementReview(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/submit-review`, method: 'post' })
}
export function reviewStatement(statementId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/review`, method: 'post', data })
}
export function listStatementReviews(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/reviews`, method: 'get' })
}
export function withdrawStatement(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/withdraw`, method: 'post' })
}
export function sendStatement(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/send`, method: 'post' })
}
export function voidStatement(statementId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-statements/${statementId}/void`, method: 'post' })
}
export function listStatementDifferences(params: any): Promise<any> {
  return request({ url: '/api/admin/statement-differences', method: 'get', params })
}
export function resolveStatementDifference(differenceId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/statement-differences/${differenceId}/resolve`, method: 'post', data })
}
export function listFeeAdjustments(params: any): Promise<any> {
  return request({ url: '/api/admin/driver-fee-adjustments', method: 'get', params })
}
export function createFeeAdjustment(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-fee-adjustments', method: 'post', data })
}
export function auditFeeAdjustment(adjustmentId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-fee-adjustments/${adjustmentId}/audit`, method: 'post', data })
}
export function listDriverPayments(params: any): Promise<any> {
  return request({ url: '/api/admin/driver-payment-records', method: 'get', params })
}
export function getDriverPayment(recordId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-payment-records/${recordId}`, method: 'get' })
}
export function createDriverPayment(data: any): Promise<any> {
  return request({ url: '/api/admin/driver-payment-records', method: 'post', data })
}
export function verifyDriverPayment(recordId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-payment-records/${recordId}/verify`, method: 'post', data })
}
export function driverReconciliationOverview(driverId: number | string): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}/reconciliation-overview`, method: 'get' })
}
export function driverReconciliationEntries(driverId: number | string, params?: any): Promise<any> {
  return request({ url: `/api/admin/drivers/${driverId}/reconciliation-entries`, method: 'get', params })
}

// ---------------------------------------------------------------- 支持与治理
export function listDisputes(params: any): Promise<any> {
  return request({ url: '/api/admin/disputes', method: 'get', params })
}
export function getDispute(disputeId: number | string): Promise<any> {
  return request({ url: `/api/admin/disputes/${disputeId}`, method: 'get' })
}
export function resolveDispute(disputeId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/disputes/${disputeId}/resolve`, method: 'post', data })
}
export function listExceptions(params: any): Promise<any> {
  return request({ url: '/api/admin/exceptions', method: 'get', params })
}
export function resolveException(exceptionId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/exceptions/${exceptionId}/resolve`, method: 'post', data })
}
export function listCorrections(params: any): Promise<any> {
  return request({ url: '/api/admin/result-corrections', method: 'get', params })
}
export function auditCorrection(correctionId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/result-corrections/${correctionId}/audit`, method: 'post', data })
}
export function listReconciliation(params: any): Promise<any> {
  return request({ url: '/api/admin/reconciliation', method: 'get', params })
}
export function queryReconciliation(caseId: number | string): Promise<any> {
  return request({ url: `/api/admin/reconciliation/${caseId}/query`, method: 'post' })
}
export function resolveReconciliation(caseId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/reconciliation/${caseId}/resolve`, method: 'post', data })
}
export function listExports(params: any): Promise<any> {
  return request({ url: '/api/admin/exports', method: 'get', params })
}
export function requestExport(data: any): Promise<any> {
  return request({ url: '/api/admin/orders/export', method: 'post', data })
}
export function auditExport(taskId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/exports/${taskId}/audit`, method: 'post', data })
}
export function getExportTask(taskId: number | string): Promise<any> {
  return request({ url: `/api/admin/exports/${taskId}`, method: 'get' })
}
export function listAuditEvents(params: any): Promise<any> {
  return request({ url: '/api/admin/audit-events', method: 'get', params })
}
export function getStatistics(params?: any): Promise<any> {
  return request({ url: '/api/admin/statistics', method: 'get', params })
}
export function listNotificationTemplates(): Promise<any> {
  return request({ url: '/api/admin/notification-templates', method: 'get' })
}
export function updateNotificationTemplate(templateId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/notification-templates/${templateId}`, method: 'put', data })
}
export function listJobs(params?: any): Promise<any> {
  return request({ url: '/api/admin/jobs', method: 'get', params })
}
export function retryJob(taskId: number | string): Promise<any> {
  return request({ url: `/api/admin/jobs/${taskId}/retry`, method: 'post' })
}
export function listPrivacyCases(params: any): Promise<any> {
  return request({ url: '/api/admin/privacy-cases', method: 'get', params })
}
export function resolvePrivacyCase(caseId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/privacy-cases/${caseId}/resolve`, method: 'post', data })
}
export function getBusinessConfig(): Promise<any> {
  return request({ url: '/api/admin/config', method: 'get' })
}
export function saveBusinessConfig(data: any): Promise<any> {
  return request({ url: '/api/admin/config', method: 'put', data })
}
export function listBindingRequests(): Promise<any> {
  return request({ url: '/api/admin/employee-binding-requests', method: 'get' })
}
export function issueBindingCredential(data: any): Promise<any> {
  return request({ url: '/api/admin/employee-binding-credentials', method: 'post', data })
}
export function revokeBindingCredential(credentialId: number | string): Promise<any> {
  return request({ url: `/api/admin/employee-binding-credentials/${credentialId}/revoke`, method: 'post' })
}
export function listPenalties(params: any): Promise<any> {
  return request({ url: '/api/admin/penalties', method: 'get', params })
}
export function getPenalty(penaltyId: number | string): Promise<any> {
  return request({ url: `/api/admin/penalties/${penaltyId}`, method: 'get' })
}
export function listAppeals(params: any): Promise<any> {
  return request({ url: '/api/admin/penalty-appeals', method: 'get', params })
}
export function auditAppeal(appealId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/penalty-appeals/${appealId}/audit`, method: 'post', data })
}
export function listTrainingCourses(): Promise<any> {
  return request({ url: '/api/admin/training-courses', method: 'get' })
}
export function createTrainingCourse(data: any): Promise<any> {
  return request({ url: '/api/admin/training-courses', method: 'post', data })
}
export function getTrainingCourse(trainingId: number | string): Promise<any> {
  return request({ url: `/api/admin/training-courses/${trainingId}`, method: 'get' })
}
export function updateTrainingCourse(trainingId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/training-courses/${trainingId}`, method: 'put', data })
}
export function publishTrainingCourse(trainingId: number | string): Promise<any> {
  return request({ url: `/api/admin/training-courses/${trainingId}/publish`, method: 'post' })
}

// ---------------------------------------------------------------- 资格审核
export function listDriverQualifications(params: any): Promise<any> {
  return request({ url: '/api/admin/driver-qualifications', method: 'get', params })
}
export function getDriverQualification(applicationId: number | string): Promise<any> {
  return request({ url: `/api/admin/driver-qualifications/${applicationId}`, method: 'get' })
}
export function auditDriverQualification(applicationId: number | string, data: any): Promise<any> {
  return request({ url: `/api/admin/driver-qualifications/${applicationId}/audit`, method: 'post', data })
}
