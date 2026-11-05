import { currentSession, request } from '../../services/http';
import type { AfterSaleApply, AfterSaleNote, AfterSalePublic } from '../../api/types';
export type PublicCase = AfterSalePublic;
export type CasePage = { rows: PublicCase[]; total: number };
export function draftScope(resource: string) {
  const session = currentSession();
  if (!session || session.activeRole !== 'OWNER') throw new Error('请切换车主身份后使用草稿');
  return [
    'vi.aftersale',
    import.meta.env.VITE_APP_REALM || 'local',
    import.meta.env.VITE_API_BASE || '',
    session.accountId,
    resource
  ].join('.');
}
function caseId(id: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(id)) throw new Error('售后记录编号无效');
  return id;
}
function owner() {
  if (currentSession()?.activeRole !== 'OWNER') throw new Error('请切换车主身份后查看售后');
}
export function listCases(pageNum = 1, orderNo = '') {
  owner();
  const path = orderNo
    ? '/api/app/owner/orders/' + encodeURIComponent(orderNo) + '/after-sales'
    : '/api/app/owner/after-sales';
  return request<CasePage>('GET', path, { pageNum, pageSize: 10 });
}
export async function getCase(id: string) {
  owner();
  return (await request<{ data: PublicCase }>('GET', '/api/app/owner/after-sales/' + caseId(id)))
    .data;
}
export async function applyCase(orderNo: string, input: AfterSaleApply, key: string) {
  owner();
  return (
    await request<{ data: PublicCase }>(
      'POST',
      '/api/app/owner/orders/' + encodeURIComponent(orderNo) + '/after-sales',
      input,
      key
    )
  ).data;
}
export async function appendNote(id: string, input: AfterSaleNote, key: string) {
  owner();
  return (
    await request<{ data: PublicCase }>(
      'POST',
      '/api/app/owner/after-sales/' + caseId(id) + '/notes',
      input,
      key
    )
  ).data;
}
export function amountInFen(value: string) {
  if (!/^(0|[1-9][0-9]{0,10})(\.[0-9]{1,2})?$/.test(value))
    throw new Error('申请金额请填写非负金额，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  const result = Number(whole) * 100 + Number(fraction.padEnd(2, '0'));
  if (!Number.isSafeInteger(result)) throw new Error('申请金额超出范围');
  return result;
}
export const caseLabel = (status: string) =>
  (
    ({
      PENDING_REVIEW: '待人工审核',
      EXECUTING: '售后处理中',
      CLOSED: '已结束',
      REJECTED: '审核未通过',
      PENDING: '待审核',
      APPROVED: '已通过',
      UNDETERMINED: '审核后确定',
      NOT_REQUIRED: '无需处理',
      WAIT_RETURN: '等待车辆归还',
      RETURNED: '车辆已归还',
      NONE: '尚未发起退款',
      READY: '等待退款执行',
      PROCESSING: '退款处理中',
      SUCCESS: '退款已完成',
      UNKNOWN: '退款结果待确认',
      ABNORMAL: '退款异常，正在核查'
    }) as Record<string, string>
  )[status] || status;

export function caseInstruction(row: PublicCase) {
  if (row.caseStatus === 'PENDING_REVIEW')
    return '申请已提交，等待人工审核。已支付但尚未接车的订单也需审核；提交申请不代表退款已发起。';
  if (row.caseStatus === 'REJECTED')
    return '本次申请未通过，请查看公开处理说明。如需再次申请，请返回订单核对。';
  if (row.refundState === 'SUCCESS') return '服务端已确认退款完成，实际到账请以支付渠道账单为准。';
  if (row.refundState === 'WAIT_RETURN' || row.returnState === 'WAIT_RETURN')
    return '请先完成实际还车，确认归还后才进入后续退款处理。';
  if (row.refundState === 'ABNORMAL' || row.refundState === 'UNKNOWN')
    return '退款结果正在核查，请勿重复发起申请。可补充说明后等待工作人员处理。';
  if (row.caseStatus === 'CLOSED') return '本次售后处理已结束。';
  return '售后正在处理，退款状态以服务端确认结果为准。';
}
