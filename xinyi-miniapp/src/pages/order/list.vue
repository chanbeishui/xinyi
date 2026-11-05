<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onHide } from '@dcloudio/uni-app';
import { currentSession } from '../../services/http';
import { listOrders, orderTime, yuan, type OrderSummary } from '../../features/order/api';
const rows = ref<OrderSummary[]>([]),
  total = ref(0),
  page = ref(1),
  loading = ref(false),
  error = ref(''),
  role = ref(''),
  status = ref('');
const filters = [
  { label: '全部订单', value: '' },
  { label: '待付款', value: 'PENDING_PAY' },
  { label: '服务中', value: 'ACCEPTED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
];
let seq = 0;
onShow(() => {
  role.value = currentSession()?.activeRole || '';
  status.value = '';
  page.value = 1;
  load();
});
onHide(() => {
  seq++;
  rows.value = [];
  error.value = '';
  loading.value = false;
});
// 当前工作身份决定订单视角；分页或卸载后丢弃旧响应，避免跨身份数据残留。
async function load() {
  const run = ++seq;
  rows.value = [];
  error.value = '';
  loading.value = true;
  try {
    const data = await listOrders(page.value, status.value);
    if (run !== seq) return;
    rows.value = data.rows;
    total.value = data.total;
  } catch (e) {
    if (run === seq) error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    if (run === seq) loading.value = false;
  }
}
function filter(event: { detail: { value: string | number } }) {
  status.value = filters[Number(event.detail.value)].value;
  page.value = 1;
  load();
}
function turn(delta: number) {
  page.value += delta;
  load();
}
function aftersales() {
  uni.navigateTo({ url: '/pages/aftersale/list' });
}
function open(row: OrderSummary) {
  uni.navigateTo({ url: '/pages/order/detail?orderNo=' + encodeURIComponent(row.orderNo) });
}
</script>
<template>
  <view class="screen">
    <text class="title">{{
      role === 'OWNER' ? '我的订单' : role === 'DRIVER' ? '我的服务记录' : '本站订单'
    }}</text>
    <text class="muted">{{
      role === 'DRIVER'
        ? '仅显示本人承接的服务记录'
        : role === 'STATION'
          ? '按当前站点及工作权限展示'
          : '查看预约状态、费用和服务进度'
    }}</text>
    <picker v-if="role === 'OWNER'" :range="filters" range-key="label" @change="filter"
      ><view class="filter">{{ filters.find((f) => f.value === status)?.label }} ▾</view></picker
    >
    <button v-if="role === 'OWNER'" @click="aftersales">我的售后</button>
    <text v-if="loading" class="muted">正在加载订单…</text>
    <view v-else-if="error" class="error"
      ><text>{{ error }}</text
      ><button @click="load">重新加载</button></view
    >
    <text v-else-if="!rows.length" class="empty">暂无符合条件的订单</text>
    <view v-for="row in rows" :key="row.orderNo" class="card">
      <view class="heading"
        ><text>{{ row.vehicle.plateNo }}</text
        ><text class="badge">{{ row.displayStatus }}</text></view
      >
      <text>{{ row.station.name }}</text
      ><text class="muted">接车时间：{{ orderTime(row.slotStartAt) }}</text
      ><text class="number">{{ row.orderNo }}</text>
      <text v-if="row.view === 'OWNER'">应付 ¥{{ yuan(row.money.totalAmount) }}</text>
      <text v-if="row.view === 'DRIVER' && row.accessPhase === 'HISTORY'" class="muted"
        >历史服务 · 地址与联系人已隐藏</text
      >
      <button v-if="row.allowedActions.includes('VIEW_DETAIL')" class="primary" @click="open(row)">
        查看详情
      </button>
    </view>
    <view v-if="!loading && !error" class="pages"
      ><button :disabled="page <= 1" @click="turn(-1)">上一页</button
      ><text>{{ page }} / {{ Math.max(1, Math.ceil(total / 10)) }}</text
      ><button :disabled="page * 10 >= total" @click="turn(1)">下一页</button></view
    >
  </view>
</template>
<style scoped>
.screen {
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  font-size: 24px;
  font-weight: 600;
}
.muted,
.number {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}
.number {
  overflow-wrap: anywhere;
}
.filter,
.card {
  padding: 16px;
  border-radius: 12px;
  background: white;
}
.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}
.badge {
  color: #005fcb;
}
.empty {
  text-align: center;
  padding: 48px 0;
  color: #64748b;
}
.error {
  color: #b42318;
  display: grid;
  gap: 16px;
}
.pages {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pages button {
  flex: 1;
  font-size: 14px;
}
.primary {
  color: #005fcb;
  background: #eef5ff;
  width: 100%;
  font-size: 15px;
}
</style>
