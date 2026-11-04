<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onHide } from '@dcloudio/uni-app';
import { getOrder, getTimeline, orderTime, yuan, type OrderDetail } from '../../features/order/api';
import type { OrderTimelinePublic } from '../../api/types';
const order = ref<OrderDetail | null>(null), events = ref<OrderTimelinePublic[]>([]), loading = ref(false), error = ref(''), timelineError = ref(''), timelineBusy = ref(false), total = ref(0);
let orderNo = '', seq = 0, timelinePage = 1;
onLoad(q => { orderNo = String(q?.orderNo || ''); });
onShow(load);
onHide(() => { seq++; order.value = null; events.value = []; loading.value = false; timelineBusy.value = false; });
async function load() {
  const run = ++seq; order.value = null; events.value = []; error.value = ''; timelineError.value = ''; total.value = 0; timelinePage = 1; loading.value = true;
  try { const data = await getOrder(orderNo); if (run !== seq) return; order.value = data; if (data.view === 'OWNER') await timeline(run); }
  catch (e) { if (run === seq) error.value = e instanceof Error ? e.message : '加载失败'; }
  finally { if (run === seq) loading.value = false; }
}
async function timeline(run = seq) {
  if (timelineBusy.value) return; timelineBusy.value = true; timelineError.value = '';
  try { const data = await getTimeline(orderNo, timelinePage); if (run !== seq) return; events.value.push(...data.rows); total.value = data.total; timelinePage++; }
  catch (e) { if (run === seq) timelineError.value = e instanceof Error ? e.message : '进度加载失败'; }
  finally { if (run === seq) timelineBusy.value = false; }
}
const statusLabel = (value: string) => ({'PENDING': '待处理','PAID': '已付款','CLOSED': '已关闭','NONE': '无','NOT_REQUIRED': '无需退款','WAIT_RETURN': '待还车','READY': '待执行','PROCESSING': '处理中','SUCCESS': '已完成','UNKNOWN': '结果待确认','ABNORMAL': '待核查','PENDING_REVIEW': '待审核','EXECUTING': '执行中','REJECTED': '未通过','MISSING': '未提交','VERIFIED': '已核验','PASS': '合格','FAIL': '不合格'} as Record<string, string>)[value] || value;
function payment(no: string) { uni.navigateTo({ url: "/pages/payment/index?orderNo=" + encodeURIComponent(no) }); }
</script>
<template>
  <view class="screen">
    <text class="title">订单详情</text>
    <text v-if="loading">正在加载…</text>
    <view v-if="error" class="error"><text>{{ error }}</text><button @click="load">重新加载</button></view>
    <template v-if="order">
      <view class="card"><text class="status">{{ order.displayStatus }}</text><text>{{ order.vehicle.plateNo }} · {{ order.vehicle.vehicleTypeLabel }}</text><text class="muted number">{{ order.orderNo }}</text><text>接车：{{ orderTime(order.slotStartAt) }} — {{ orderTime(order.slotEndAt) }}</text><text v-if="order.returnedAt">还车：{{ orderTime(order.returnedAt) }}</text></view>
      <view class="card"><text class="section">服务站点</text><text>{{ order.station.name }}</text><text class="muted">{{ order.station.publicAddress }}</text></view>
      <view v-if="order.view !== 'DRIVER' && order.driver" class="card"><text class="section">当前代驾</text><text>{{ order.driver.displayName }} · {{ order.driver.driverCode }}</text><text v-if="order.driver.rating !== null">服务评分 {{ order.driver.rating }}</text><text v-if="order.driver.contact.phoneMask">{{ order.driver.contact.phoneMask }}</text></view>
      <template v-if="order.view !== 'STATION'">
        <view v-if="order.view === 'DRIVER' && order.accessPhase === 'HISTORY'" class="notice">历史服务，地址与联系人已隐藏</view>
        <view v-if="order.pickupAddress" class="card"><text class="section">接车地址</text><text>{{ order.pickupAddress.address }}</text><text>{{ order.pickupAddress.contact.displayName }} {{ order.pickupAddress.contact.phoneMask }}</text></view>
        <view v-if="order.returnAddress" class="card"><text class="section">还车地址</text><text>{{ order.returnAddress.address }}</text></view>
        <view class="card"><text class="section">交接里程</text><text>接车 {{ order.mileage.pickupKm ?? '未记录' }} · 还车 {{ order.mileage.returnKm ?? '未记录' }}</text><text v-if="order.mileage.distanceKm">行驶 {{ order.mileage.distanceKm }} 公里</text><text v-if="order.mileage.publicExplanation">{{ order.mileage.publicExplanation }}</text></view>
      </template>
      <template v-if="order.view === 'OWNER'">
        <view class="card"><text class="section">客户费用</text><view v-for="item in order.money.items" :key="item.feeCode" class="line"><text>{{ item.name }}</text><text>¥{{ yuan(item.amount) }}</text></view><text>应付 ¥{{ yuan(order.money.totalAmount) }} · 已付 ¥{{ yuan(order.money.paidAmount) }}</text><text>已退 ¥{{ yuan(order.money.refundedAmount) }}</text><text class="muted">支付 {{ statusLabel(order.paymentStatus) }} · 退款 {{ statusLabel(order.refundStatus) }}</text><text v-if="order.paymentStatus === 'PENDING'">付款截止：{{ orderTime(order.payExpireAt) }}</text></view>
        <button v-if="['PENDING', 'UNKNOWN'].includes(order.paymentStatus)" @click="payment(order.orderNo)">查看付款</button>
        <view v-if="order.afterSale" class="card"><text class="section">售后进度</text><text>{{ statusLabel(order.afterSale.caseStatus) }} · {{ statusLabel(order.afterSale.refundState) }}</text><text v-if="order.afterSale.publicOpinion">{{ order.afterSale.publicOpinion }}</text></view>
        <view class="card"><text class="section">服务进度</text><view v-for="event in events" :key="event.eventId" class="event"><text>{{ event.displayText }}</text><text class="muted">{{ event.actorLabel }} · {{ orderTime(event.at) }}</text></view><text v-if="!events.length && !timelineError">暂无公开进度</text><text v-if="timelineError" class="error">{{ timelineError }}</text><button v-if="timelineError || events.length < total" :disabled="timelineBusy" @click="timeline()">{{ timelineError ? '重试加载进度' : '更多进度' }}</button></view>
      </template>
      <view v-else-if="order.cancellation.returnRequired" class="notice">{{ order.cancellation.publicInstruction }}</view>
      <view v-if="order.view === 'STATION'" class="card"><text class="section">检测信息</text><text>行驶证：{{ statusLabel(order.licenseReviewStatus) }}</text><text>检测结果：{{ order.result ? statusLabel(order.result) : '尚未登记' }}</text></view>
      <button @click="load">刷新订单状态</button>
    </template>
  </view>
</template>
<style scoped>
.screen { padding: 24px 18px; display: flex; flex-direction: column; gap: 16px; }.title { font-size: 24px; font-weight: 600; }.card { padding: 18px; background: white; border-radius: 12px; display: flex; flex-direction: column; gap: 12px; font-size: 15px; line-height: 1.6; }.section { font-size: 17px; font-weight: 600; }.status { font-size: 22px; color: #005fcb; font-weight: 600; }.muted { font-size: 13px; color: #64748b; }.number { overflow-wrap: anywhere; }.notice { padding: 16px; border-radius: 12px; background: #fff4e5; color: #854d0e; }.error { color: #b42318; }.line { display: flex; justify-content: space-between; gap: 12px; }.event { border-left: 2px solid #cbd5e1; padding-left: 14px; display: flex; flex-direction: column; }.screen > button { width: 100%; font-size: 15px; }
</style>
