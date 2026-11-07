import { viGet, viPost } from './request';

export interface VersionReason {
  expectedVersion: number;
  reason: string;
}
export interface RefundDetail {
  refundNo: string;
  orderNo: string;
  amount: number;
  status: string;
  channelRefundId: string | null;
  successAt: string | null;
  queryRequired: boolean;
  version: number;
  caseId: string;
  reviewRevision: number;
  paymentMethod: 'WECHAT' | 'OFFLINE';
  executionMode: 'WECHAT_API' | 'MANUAL_OFFLINE';
  manualProofState: string | null;
  returnState: string;
  paidAmount: number;
  refundedAmount: number;
  reservedAmount: number;
  executionStartedAt: string | null;
  lastErrorCode: string | null;
  allowedActions: string[];
}
export interface ReconciliationCase {
  caseId: string;
  orderNo: string;
  type: 'PAYMENT' | 'REFUND';
  localStatus: string;
  channelStatus: string | null;
  status: 'OPEN' | 'CHECKING' | 'RESOLVED';
  version: number;
  subjectId: string;
  paymentMethod: 'WECHAT' | 'OFFLINE';
  refundNo: string | null;
  reasonCode: string;
  verifiedAt?: string | null;
  resolvedAt?: string | null;
  allowedActions: string[];
}
function refundPath(no: string) {
  if (!no.trim() || no.length > 64) throw new Error('请填写有效的原退款号');
  return '/api/admin/refunds/' + encodeURIComponent(no);
}
function casePath(id: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(id)) throw new Error('对账记录编号无效');
  return '/api/admin/reconciliation/' + id;
}
export const getRefund = (no: string) => viGet<{ data: RefundDetail }>(refundPath(no));
export const queryRefund = (no: string, body: VersionReason, key: string) =>
  viPost<{ data: RefundDetail }>(refundPath(no) + '/query', body, { idempotencyKey: key });
export const retryRefund = (no: string, body: VersionReason, key: string) =>
  viPost<{ data: RefundDetail }>(refundPath(no) + '/retry', body, { idempotencyKey: key });
export const listReconciliation = (params: Record<string, string | number>) =>
  viGet<{ rows: ReconciliationCase[]; total: number }>('/api/admin/reconciliation', params);
export const queryReconciliation = (id: string, body: VersionReason, key: string) =>
  viPost<{ data: ReconciliationCase }>(casePath(id) + '/query', body, { idempotencyKey: key });
export const resolveReconciliation = (id: string, body: VersionReason, key: string) =>
  viPost<{ data: ReconciliationCase }>(casePath(id) + '/resolve', body, { idempotencyKey: key });
export const financeMoney = (value: number) => '¥' + (value / 100).toFixed(2);
// 财务核对统一按上海业务时区显示，不随操作人员电脑时区漂移。
export const financeTime = (value: string | null) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' })
    : '—';
const STATUS: Record<string, string> = {
  CREATED: '等待执行',
  PROCESSING: '处理中',
  SUCCESS: '已成功',
  CLOSED: '已关闭',
  CHECKING: '核查中',
  ABNORMAL: '异常待核查',
  UNKNOWN: '渠道结果未知',
  OPEN: '待核查',
  RESOLVED: '已办结',
  PENDING: '待处理',
  REVIEW_PENDING: '待核实',
  NONE: '无',
  UNDETERMINED: '尚未确定',
  NOT_REQUIRED: '无需还车',
  WAIT_RETURN: '等待实际还车',
  RETURNED: '已实际归还',
  WECHAT: '微信支付',
  OFFLINE: '企业线下付款',
  WECHAT_API: '原微信交易退款',
  MANUAL_OFFLINE: '线下实际退款凭证核实',
  PAYMENT: '付款异常',
  REFUND: '退款异常'
};
export const financeLabel = (value: string | null) => (value ? STATUS[value] || value : '—');

const REASONS: Record<string, string> = {
  RESULT_UNKNOWN: '渠道结果尚未确认',
  CHANNEL_RESULT_UNKNOWN: '渠道结果尚未确认',
  REFUND_NOT_FOUND: '渠道未找到原退款单，需继续核实是否受理',
  LATE_PAYMENT_CLOSED_ORDER: '订单关闭后确认付款，需核对资金与履约',
  OFFLINE_PROOF_REQUIRED: '企业线下收退款凭证待核实',
  AMOUNT_MISMATCH: '本地金额与渠道金额不一致',
  IDENTITY_MISMATCH: '原支付身份与渠道记录不匹配',
  VI_CONFIG_INVALID: '原支付渠道配置需核对',
  VI_UPSTREAM_UNAVAILABLE: '原支付渠道暂不可读取'
};
export const financeReason = (value: string) => REASONS[value] || value;
