<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onHide } from '@dcloudio/uni-app';
import { listCases, caseLabel, type PublicCase } from '../../features/aftersale/api';
import { orderTime, yuan } from '../../features/order/api';
const rows = ref<PublicCase[]>([]),
  total = ref(0),
  page = ref(1),
  loading = ref(false),
  error = ref('');
let orderNo = '',
  seq = 0;
onLoad((query) => {
  orderNo = String(query?.orderNo || '');
});
onShow(() => {
  page.value = 1;
  load();
});
onHide(() => {
  seq++;
  rows.value = [];
  loading.value = false;
});
async function load() {
  const run = ++seq;
  rows.value = [];
  error.value = '';
  loading.value = true;
  try {
    const value = await listCases(page.value, orderNo);
    if (run === seq) {
      rows.value = value.rows;
      total.value = value.total;
    }
  } catch (failure) {
    if (run === seq) error.value = failure instanceof Error ? failure.message : '加载失败';
  } finally {
    if (run === seq) loading.value = false;
  }
}
function open(id: string) {
  uni.navigateTo({ url: '/pages/aftersale/detail?caseId=' + encodeURIComponent(id) });
}
function turn(delta: number) {
  page.value += delta;
  load();
}
</script>
<template>
  <view class="screen">
    <text class="title">{{ orderNo ? '订单售后记录' : '我的售后' }}</text>
    <text class="muted">查看本人申请的审核、还车和退款进度</text>
    <text v-if="loading" class="muted">正在加载…</text>
    <view v-else-if="error" class="error"
      ><text>{{ error }}</text
      ><button @click="load">重新加载</button></view
    >
    <text v-else-if="!rows.length" class="empty">暂无售后申请</text>
    <view v-for="row in rows" :key="row.caseId" class="card">
      <view class="heading"
        ><text>{{ row.kind === 'CANCEL_REFUND' ? '取消服务并退款' : '仅申请退款' }}</text
        ><text class="status">{{ caseLabel(row.caseStatus) }}</text></view
      >
      <text class="number muted">{{ row.orderNo }}</text>
      <text>申请 ¥{{ yuan(row.requestedAmount ?? 0) }}</text>
      <text class="muted">{{ orderTime(row.createdAt) }}</text>
      <text>{{ caseLabel(row.refundState) }}</text>
      <button @click="open(row.caseId)">查看售后进度</button>
    </view>
    <view v-if="!loading && !error" class="pages">
      <button :disabled="page <= 1" @click="turn(-1)">上一页</button>
      <text>{{ page }} / {{ Math.max(1, Math.ceil(total / 10)) }}</text>
      <button :disabled="page * 10 >= total" @click="turn(1)">下一页</button>
    </view>
  </view>
</template>
<style scoped>
.screen {
  box-sizing: border-box;
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  font-size: 24px;
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}
.number {
  overflow-wrap: anywhere;
}
.card {
  padding: 18px;
  border-radius: 12px;
  background: white;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 15px;
}
.heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}
.status {
  color: #005fcb;
}
.empty {
  padding: 48px 0;
  color: #64748b;
  text-align: center;
}
.error {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #b42318;
}
button {
  width: 100%;
  margin: 0;
  font-size: 14px;
}
.pages {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pages button {
  flex: 1;
}
</style>
