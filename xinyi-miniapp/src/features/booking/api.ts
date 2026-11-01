import { request } from '../../services/http';
import type { Slot, Quote, QuoteRequest, StationSelectionRequest, AvailableStation } from '../../api/types';
export async function listSlots(date: string, pageNum = 1) {
  return request<{ rows: Slot[]; total: number }>('GET', '/api/app/owner/schedule/slots', { date, pageNum, pageSize: 20 });
}
export async function getQuote(input: QuoteRequest, key: string): Promise<Quote> {
  const r = await request<{ data: Quote }>('POST', '/api/app/owner/orders/quote', input, key);
  return r.data;
}

export async function availableStations(input: StationSelectionRequest): Promise<AvailableStation[]> {
  const r = await request<{ data: AvailableStation[] }>('POST', '/api/app/owner/orders/stations', input);
  return r.data;
}
