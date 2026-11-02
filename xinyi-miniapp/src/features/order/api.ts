import { request } from '../../services/http';
import type { OrderCreate, OwnerOrderView } from '../../api/types';
export async function createOrder(input: OrderCreate, key: string): Promise<OwnerOrderView> {
  const response = await request<{ data: OwnerOrderView }>('POST', '/api/app/owner/orders', input, key);
  return response.data;
}
