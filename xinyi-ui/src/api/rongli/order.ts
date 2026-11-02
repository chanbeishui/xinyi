import { viGet } from './request'
import type { AdminOrderSummary, AdminOrderView } from '../../../../xinyi-miniapp/src/api/types'
export type { AdminOrderSummary, AdminOrderView }
export const listOrders = (pageNum: number, pageSize: number) => viGet<{ rows: AdminOrderSummary[]; total: number }>('/api/admin/orders', { pageNum, pageSize })
export const getOrder = (orderNo: string) => viGet<{ data: AdminOrderView }>('/api/admin/orders/' + encodeURIComponent(orderNo))
