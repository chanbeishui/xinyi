import { viGet, viPost } from './request';
import type { AfterSaleApply, AfterSaleNote } from '../../../../xinyi-miniapp/src/api/types';

export interface CaseNote {
  noteId: string;
  note: string;
  voucherFileIds: string[];
  createdAt: string;
}
export interface CaseReview {
  decision: 'APPROVE' | 'REJECT';
  approvedAmount: number | null;
  opinion: string;
  publicOpinion: string;
  reviewedAt: string;
  reviewerUserId: string;
}
export interface AdminCase {
  caseId: string;
  orderNo: string;
  kind: 'CANCEL_REFUND' | 'REFUND_ONLY';
  caseStatus: string;
  reviewStatus: string;
  reason: string;
  reasonCode: string | null;
  voucherFileIds: string[];
  notes: CaseNote[];
  reviews: CaseReview[];
  requestedAmount: number | null;
  approvedAmount: number | null;
  paidAmount: number | null;
  refundedAmount: number | null;
  reservedAmount: number | null;
  availableAmount: number | null;
  custody: string;
  fulfillmentStatus: string;
  returnState: string;
  refundState: string;
  refundNo?: string | null;
  applicantType: string;
  applicantId: string;
  orderVersion: number;
  assignmentId: string | null;
  selfReviewDenied: boolean;
  publicOpinion: string | null;
  allowedActions: string[];
  version: number;
  createdAt: string;
  updatedAt: string;
  orderPlateNo?: string | null;
  ownerLabel: string;
  ownerAccountId: string;
}
export interface ReviewPreview {
  decision: 'APPROVE' | 'REJECT';
  caseId: string;
  caseVersion: number;
  orderVersion: number;
  custody: string;
  assignmentId: string | null;
  paidAmount: number;
  refundedAmount: number;
  reservedAmount: number;
  availableAmount: number;
  returnRequired: boolean;
  allowedActions?: string[];
  warnings?: string[];
}
export interface ReviewInput {
  expectedVersion: number;
  observedOrderVersion: number;
  decision: 'APPROVE' | 'REJECT';
  approvedAmount: number;
  opinion: string;
  publicOpinion: string;
}
export type CaseQuery = Record<string, string | number>;
export function validId(id: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(id)) throw new Error('记录编号无效');
  return id;
}
export const listCases = (query: CaseQuery, scope: 'all' | 'order' | 'owner', resource: string) => {
  const path =
    scope === 'order'
      ? '/api/admin/orders/' + encodeURIComponent(resource) + '/after-sales'
      : scope === 'owner'
        ? '/api/admin/owners/' + validId(resource) + '/after-sales'
        : '/api/admin/after-sales';
  return viGet<{ rows: AdminCase[]; total: number }>(path, query);
};
export const getCase = (id: string) =>
  viGet<{ data: AdminCase }>('/api/admin/after-sales/' + validId(id));
export const createCase = (orderNo: string, body: AfterSaleApply, key: string) =>
  viPost<{ data: AdminCase }>(
    '/api/admin/orders/' + encodeURIComponent(orderNo) + '/after-sales',
    body,
    { idempotencyKey: key }
  );
export const appendNote = (id: string, body: AfterSaleNote, key: string) =>
  viPost<{ data: AdminCase }>('/api/admin/after-sales/' + validId(id) + '/notes', body, {
    idempotencyKey: key
  });
export const previewReview = (
  id: string,
  expectedVersion: number,
  approvedAmount: number,
  decision: 'APPROVE' | 'REJECT'
) =>
  viPost<{ data: ReviewPreview }>(
    '/api/admin/after-sales/' + validId(id) + '/review-preview',
    { expectedVersion, approvedAmount, decision },
    { idempotencyKey: intentKey() }
  );
export const reviewCase = (id: string, body: ReviewInput, key: string) =>
  viPost<{ data: AdminCase }>('/api/admin/after-sales/' + validId(id) + '/review', body, {
    idempotencyKey: key
  });

export function intentKey() {
  return crypto.randomUUID();
}
/** 用十进制字符串转换为分，避免浮点乘法让财务金额出现一分偏差。 */
export function amountFen(text: string) {
  if (!/^(0|[1-9][0-9]{0,10})(\.[0-9]{1,2})?$/.test(text))
    throw new Error('金额须为非负金额，最多两位小数');
  const [whole, fraction = ''] = text.split('.');
  const value = Number(whole) * 100 + Number(fraction.padEnd(2, '0'));
  if (!Number.isSafeInteger(value)) throw new Error('金额超出范围');
  return value;
}
