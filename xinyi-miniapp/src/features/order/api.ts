import { request } from '../../services/http';
import type { OrderCreate, OwnerOrderView } from '../../api/types';
export async function createOrder(input: OrderCreate, key: string): Promise<OwnerOrderView> {
  const response = await request<{ data: OwnerOrderView }>('POST', '/api/app/owner/orders', input, key);
  return response.data;
}

import { currentSession } from '../../services/http';
import type { OwnerOrderSummary, DriverOrderSummary, StationOrderSummary, DriverOrderView, StationOrderView, OrderTimelinePublic } from '../../api/types';
export type OrderSummary = OwnerOrderSummary | DriverOrderSummary | StationOrderSummary;
export type OrderDetail = OwnerOrderView | DriverOrderView | StationOrderView;
export type Page<T> = { rows: T[]; total: number };
function prefix() {
  const role = currentSession()?.activeRole;
  if (!role || !['OWNER', 'DRIVER', 'STATION'].includes(role)) throw new Error('请先选择有效身份');
  return '/api/app/' + role.toLowerCase() + '/orders';
}
export async function listOrders(pageNum = 1, status = '') {
  const params = { pageNum, pageSize: 10, ...(currentSession()?.activeRole === 'OWNER' && status ? { status } : {}) };
  return request<Page<OrderSummary>>('GET', prefix(), params);
}
export async function getOrder(orderNo: string) {
  return (await request<{ data: OrderDetail }>('GET', prefix() + '/' + encodeURIComponent(orderNo))).data;
}
export async function getTimeline(orderNo: string, pageNum = 1) {
  if (currentSession()?.activeRole !== 'OWNER') throw new Error('当前身份不可读取车主时间轴');
  return request<Page<OrderTimelinePublic>>('GET', prefix() + '/' + encodeURIComponent(orderNo) + '/timeline', { pageNum, pageSize: 20 });
}
export const orderTime = (value?: string | null) => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—';
export const yuan = (fen: number) => (fen / 100).toFixed(2);
