import { request, currentSession } from '../../services/http';
import type { Prepay } from '../../api/types';
export type PaymentOptions = { orderNo: string; paymentVersion: number; policyRevisionId: string; lockedMethod: 'UNSELECTED' | 'WECHAT' | 'OFFLINE'; expiresAt: string; newPaymentAllowed: boolean; options: { method: 'WECHAT' | 'OFFLINE'; available: boolean; label: string; instructions?: string }[] };
export type PaymentIntent = { expectedVersion: number; expectedPolicyRevisionId: string };
function path(no: string) {
  if (currentSession()?.activeRole !== 'OWNER') throw new Error('请切换车主身份后查看付款');
  return '/api/app/owner/orders/' + encodeURIComponent(no);
}
export async function paymentOptions(no: string) { return (await request<{ data: PaymentOptions }>('GET', path(no) + '/payment-options')).data; }
export async function prepay(no: string, input: PaymentIntent, key: string) { return (await request<{ data: Prepay }>('POST', path(no) + '/prepay', input, key)).data; }
