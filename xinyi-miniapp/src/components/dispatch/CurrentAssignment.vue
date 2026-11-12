<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { onSessionChanged } from '../../services/http';
import {
  ensureInviteSession,
  getCurrentAssignment,
  inviteScope,
  inviteTime
} from '../../features/dispatch/invites';
import type { DriverOrderView } from '../../api/types';
const emit = defineEmits<{ accessLost: [message: string] }>();
const value = ref<DriverOrderView | null>(null),
  loading = ref(false),
  confirmed = ref(false),
  error = ref('');
let sequence = 0,
  disposed = false,
  owner = '';
function clear() {
  sequence++;
  value.value = null;
  confirmed.value = false;
  loading.value = false;
}
const unsubscribe = onSessionChanged(() => {
  let next = '';
  try {
    next = inviteScope();
  } catch {
    /* 清理退出后的承接摘要，不删除服务端责任。 */
  }
  if (next !== owner) clear();
});
async function load() {
  clear();
  const run = sequence;
  loading.value = true;
  error.value = '';
  try {
    owner = inviteScope();
    await ensureInviteSession();
    const result = await getCurrentAssignment();
    if (disposed || run !== sequence || inviteScope() !== owner) return;
    value.value = result;
    confirmed.value = true;
  } catch (failure) {
    if (disposed || run !== sequence) return;
    value.value = null;
    confirmed.value = false;
    error.value = failure instanceof Error ? failure.message : '当前承接未取得';
    if ([401, 403].includes((failure as { status?: number }).status || 0))
      emit('accessLost', error.value);
  } finally {
    if (!disposed && run === sequence) loading.value = false;
  }
}
function detail() {
  if (value.value?.allowedActions.includes('VIEW_DETAIL'))
    uni.navigateTo({
      url: '/pages/order/detail?orderNo=' + encodeURIComponent(value.value.orderNo)
    });
}
onMounted(() => void load());
onBeforeUnmount(() => {
  disposed = true;
  clear();
  unsubscribe();
});
</script>
<template>
  <view class="assignment">
    <text class="section">当前承接</text>
    <text v-if="loading" class="muted">正在核对本人当前承接…</text>
    <template v-if="value">
      <text>上次服务端核对：{{ value.displayStatus }}</text>
      <text>订单 {{ value.orderNo }}</text>
      <text>承接 {{ value.assignmentId }}</text>
      <text>接车范围：{{ value.pickupArea }}</text>
      <text>预约时段：{{ inviteTime(value.slotStartAt) }} — {{ inviteTime(value.slotEndAt) }}</text>
      <text class="muted"
        >此摘要以本次查询为准。暂停听单、切身份及退出均不解除在手服务或车辆保管归还责任。</text
      >
      <button v-if="value.allowedActions.includes('VIEW_DETAIL')" class="primary" @click="detail">
        查看当前承接订单
      </button>
    </template>
    <text v-else-if="confirmed" class="muted">本次查询暂无当前承接；历史服务记录仍可查看。</text>
    <text v-else-if="!loading" class="muted">当前承接尚未核对，不据旧摘要推断服务已结束。</text>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="loading" @click="load">刷新当前承接</button>
  </view>
</template>
<style scoped>
.assignment {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #e5e7eb;
  line-height: 1.8;
}
.section {
  font-size: 18px;
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 13px;
}
.error {
  color: #b42318;
}
button {
  width: 100%;
  margin: 0;
  font-size: 15px;
}
.primary {
  color: white;
  background: #005fcb;
}
</style>
