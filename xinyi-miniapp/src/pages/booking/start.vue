<script setup lang="ts">
import { ref, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { currentSession } from '../../services/http';
import { listSlots, getQuote, availableStations } from '../../features/booking/api';
import { listAddresses, type Address } from '../../features/address/api';
import { listVehicles, type Vehicle } from '../../features/vehicle/api';
import { getDict, type DictItem } from '../../features/config/api';
import type { Slot, Quote, QuoteRequest, StationSelectionRequest, AvailableStation } from '../../api/types';
const step = ref(0), busy = ref(false), message = ref('');
const date = ref(new Date(Date.now() + 8 * 3600000).toISOString().slice(0, 10));
const slots = ref<Slot[]>([]), slot = ref<Slot | null>(null), total = ref(0), page = ref(1);
const addresses = ref<Address[]>([]), vehicles = ref<Vehicle[]>([]), types = ref<DictItem[]>([]), inspections = ref<DictItem[]>([]);
const addressId = ref(''), address = ref(''), lng = ref(''), lat = ref('');
const vehicleId = ref(''), plate = ref(''), vehicleType = ref(''), inspection = ref('');
const quote = ref<Quote | null>(null);
const stationOptions = ref<AvailableStation[]>([]), selectedStation = ref('');
let sessionId = '', sequence = 0, attempt: { body: string; key: string } | null = null;
const displayTime = (utc: string) => new Date(Date.parse(utc) + 8 * 3600000).toISOString().slice(0, 19).replace('T', ' ') + '（北京时间）';
const error = (e: unknown) => { message.value = e instanceof Error ? e.message : '请求未完成，请重试'; };
function invalidate() { sequence++; quote.value = null; attempt = null; stationOptions.value = []; selectedStation.value = ''; if (step.value >= 3) step.value = 2; }
watch([date, slot, addressId, address, lng, lat, vehicleId, plate, vehicleType, inspection], invalidate, { flush: 'sync' });
onShow(async () => {
  const id = currentSession()?.sessionId || '';
  if (!id || currentSession()?.activeRole !== 'OWNER') { uni.redirectTo({ url: '/pages/identity/login' }); return; }
  if (sessionId && sessionId !== id) {
    slot.value = null; addressId.value = ''; address.value = ''; lng.value = ''; lat.value = '';
    vehicleId.value = ''; plate.value = ''; vehicleType.value = ''; inspection.value = ''; step.value = 0; invalidate();
  }
  sessionId = id;
  await loadSlots();
  const result = await Promise.allSettled([listAddresses(), listVehicles(), getDict('vi_vehicle_type'), getDict('vi_inspection_type')]);
  if (sessionId !== currentSession()?.sessionId) return;
  if (result[0].status === 'fulfilled') addresses.value = result[0].value;
  if (result[1].status === 'fulfilled') vehicles.value = result[1].value;
  if (result[2].status === 'fulfilled') types.value = result[2].value.filter(x => x.enabled);
  if (result[3].status === 'fulfilled') inspections.value = result[3].value.filter(x => x.enabled);
  if (result.some(x => x.status === 'rejected')) message.value = '部分资料加载失败，请返回后重试；请勿使用未核实的信息';
});
let loadSequence = 0;
async function loadSlots() {
  const ticket = ++loadSequence; busy.value = true; message.value = '';
  try { const r = await listSlots(date.value, page.value); if (ticket !== loadSequence) return; slots.value = r.rows; total.value = r.total; }
  catch (e) { if (ticket === loadSequence) { slots.value = []; error(e); } }
  finally { if (ticket === loadSequence) busy.value = false; }
}
function changeDate(e: any) { date.value = e.detail.value; slot.value = null; page.value = 1; loadSlots(); }
function chooseSlot(s: Slot) { slot.value = s; step.value = 1; message.value = ''; }
function locate() {
  uni.chooseLocation({ success: r => { addressId.value = ''; address.value = r.address || r.name; lng.value = String(r.longitude); lat.value = String(r.latitude); }, fail: () => { message.value = '定位未成功，可选已有地址或手动填写完整地址和经纬度'; } });
}
function nextAddress() {
  if (!addressId.value && (!address.value.trim() || !lng.value.trim() || !lat.value.trim() || !Number.isFinite(Number(lng.value)) || !Number.isFinite(Number(lat.value)) || Math.abs(Number(lng.value)) > 180 || Math.abs(Number(lat.value)) > 90)) { message.value = '请选择已有地址，或填写完整地址及有效坐标'; return; }
  message.value = ''; step.value = 2;
}
function uuid() { return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => { const n = Math.floor(Math.random() * 16); return (c === 'x' ? n : (n & 3) | 8).toString(16); }); }
async function calculate() {
  if (!slot.value || !inspection.value || (!vehicleId.value && (!plate.value.trim() || !vehicleType.value))) { message.value = '请补齐车辆、车型和检测类型'; return; }
  busy.value = true; message.value = ''; quote.value = null; const ticket = ++sequence;
  const input: StationSelectionRequest = { slotId: slot.value.slotId, inspectionTypeCode: inspection.value,
    ...(vehicleId.value ? { vehicleId: vehicleId.value } : { vehicleDraft: { plateNo: plate.value.trim(), vehicleTypeCode: vehicleType.value } }),
    ...(addressId.value ? { addressId: addressId.value } : { addressDraft: { address: address.value.trim(), location: { lng: Number(lng.value), lat: Number(lat.value), coordinateSystem: 'GCJ02' as const } } }) };
  try {
    const result = await availableStations(input);
    if (ticket !== sequence) return;
    stationOptions.value = result; selectedStation.value = ''; step.value = 3;
    if (!result.length) message.value = '当前地址、车型和时段暂无可用站点，请修改信息后重试';
    pendingInput = input;
  } catch (e) { if (ticket === sequence) error(e); }
  finally { busy.value = false; }
}
let pendingInput: StationSelectionRequest | null = null;
function selectStation(id: string) { selectedStation.value = id; quote.value = null; attempt = null; sequence++; }
async function confirmStation() {
  if (!pendingInput || !selectedStation.value) { message.value = '请选择可用站点'; return; }
  const input: QuoteRequest = { ...pendingInput, stationId: selectedStation.value };
  const body = JSON.stringify(input), ticket = ++sequence;
  if (!attempt || attempt.body !== body) attempt = { body, key: uuid() };
  busy.value = true; quote.value = null; message.value = '';
  try { const result = await getQuote(input, attempt.key); if (ticket !== sequence) return; quote.value = result; step.value = 4; }
  catch (e: any) {
    if (ticket === sequence) {
      error(e);
      if (['VI_STATE_CONFLICT', 'VI_CONFIG_CHANGED', 'VI_VERSION_CONFLICT', 'VI_SERVICE_UNAVAILABLE'].includes(e?.errorCode)) { attempt = null; stationOptions.value = []; selectedStation.value = ''; step.value = 2; }
    }
  } finally { busy.value = false; }

}
</script>
<template>
  <view class="screen">
    <text class="title">预约上门检测</text>
    <text class="muted">{{ ['1 选择上门时段', '2 填写接车地址', '3 确认车辆', '4 选择服务站点', '5 核对报价'][step] }}</text>
    <view v-if="message" class="notice">{{ message }}</view>
    <template v-if="step === 0">
      <picker mode="date" :value="date" @change="changeDate"><view class="picker-value">{{ date }} · 更换日期</view></picker>
      <text class="muted">先选候选时间，再核实地址和可服务站点；此处不占名额。</text>
      <text v-if="!busy && !slots.length">当天暂无可选时段，请更换日期。</text>
      <button v-for="s in slots" :key="s.slotId" @click="chooseSlot(s)">{{ s.displayText }}</button>
      <view class="row"><button :disabled="page <= 1 || busy" @click="page--; loadSlots()">上一页</button><button :disabled="page * 20 >= total || busy" @click="page++; loadSlots()">下一页</button></view>
      <button :loading="busy" @click="loadSlots">刷新时段</button>
    </template>
    <template v-if="step === 1">
      <text>{{ slot?.displayText }}</text>
      <button v-for="a in addresses" :key="a.addressId" @click="addressId = a.addressId">{{ addressId === a.addressId ? '已选：' : '' }}{{ a.address }}</button>
      <button @click="addressId = ''">使用本次临时地址</button>
      <template v-if="!addressId"><button @click="locate">地图选择接车位置</button><input v-model="address" placeholder="完整接车地址" /><input v-model="lng" type="text" placeholder="经度（地图坐标）" /><input v-model="lat" type="text" placeholder="纬度（地图坐标）" /></template>
      <button class="primary" @click="nextAddress">下一步，确认车辆</button>
    </template>
    <template v-if="step === 2">
      <button v-for="v in vehicles" :key="v.vehicleId" @click="vehicleId = v.vehicleId">{{ vehicleId === v.vehicleId ? '已选：' : '' }}{{ v.plateNo }} · {{ v.vehicleTypeLabel || v.vehicleTypeCode }}</button>
      <button @click="vehicleId = ''">使用本次临时车辆</button>
      <template v-if="!vehicleId"><input v-model="plate" placeholder="车牌号" /><text class="muted">选择车型</text><button v-for="t in types" :key="t.code" @click="vehicleType = t.code">{{ vehicleType === t.code ? '已选：' : '' }}{{ t.label }}</button></template>
      <text class="muted">选择检测类型</text><button v-for="t in inspections" :key="t.code" @click="inspection = t.code">{{ inspection === t.code ? '已选：' : '' }}{{ t.label }}</button>
      <text class="muted">地址与车辆修改后须重新核实可用站点、名额和价格。</text>
      <button class="primary" :loading="busy" :disabled="busy" @click="calculate">查询可用站点</button>
    </template>
    <template v-if="step === 3">
      <text class="muted">请选择本次服务站点；选择后再次校验费用和名额。</text>
      <button v-for="s in stationOptions" :key="s.stationId" :disabled="busy" @click="selectStation(s.stationId)">{{ selectedStation === s.stationId ? '已选：' : '' }}{{ s.name }} · {{ (s.routeDistanceMeter / 1000).toFixed(1) }} 公里
{{ s.address }}</button>
      <button class="primary" :disabled="busy || !selectedStation" :loading="busy" @click="confirmStation">确认站点并获取报价</button>
    </template>
    <template v-if="step === 4 && quote">
      <text>{{ quote.stationName }}</text><text>服务总价：{{ (quote.totalAmount / 100).toFixed(2) }} 元</text><text>报价有效至 {{ displayTime(quote.expiresAt) }}</text><text v-for="item in quote.items" :key="item.feeCode">{{ item.name }}：{{ (item.amount / 100).toFixed(2) }} 元 · {{ item.calculation }}</text>
      <text class="muted">报价未占用名额，提交预约时仍须校验。</text><button @click="step = 2; invalidate()">修改信息并重新报价</button>
    </template>
    <button v-if="step > 0" :disabled="busy" @click="step--; invalidate()">上一步</button>
  </view>
</template>
<style scoped>
.screen { padding: 24px 20px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 25px; font-weight: 600; }.muted { color: #667085; font-size: 14px; line-height: 1.7; }
.notice { background: #fff4e5; padding: 14px; border-radius: 10px; }input { background: white; padding: 14px; border: 1px solid #d9dfe8; border-radius: 8px; }
button { width: 100%; margin: 0; font-size: 15px; }.picker-value { padding: 14px; border: 1px solid #d9dfe8; border-radius: 8px; background: white; text-align: center; }.primary { background: #005fcb; color: white; }.row { display: flex; gap: 12px; }
</style>
