<script setup lang="ts">
import { ref } from 'vue';
import { onHide, onShow, onUnload } from '@dcloudio/uni-app';
import { onSessionChanged } from '../../services/http';
import { createExpiryGuard } from '../../features/dispatch/expiry';
import {
  ensureInviteSession,
  inviteScope,
  listInvites,
  pendingInvites,
  inviteLabel,
  inviteTime,
  straightDistance,
  type DriverInvite,
  type InviteIntent
} from '../../features/dispatch/invites';
const rows = ref<DriverInvite[]>([]),
  total = ref(0),
  pageNum = ref(1),
  status = ref(''),
  orderNo = ref('');
const pending = ref<InviteIntent[]>([]),
  loading = ref(false),
  error = ref('');
const statuses = ['', 'WAIT_RESPONSE', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'REVOKED'];
let foreground = false,
  sequence = 0,
  owner = '';
const guard = createExpiryGuard(() => {
  rows.value = [];
  total.value = 0;
  error.value = '邀约显示截止已到，请重新读取本人邀约；不会延长原邀约或自动接受。';
});
function clear() {
  sequence++;
  guard.clear();
  rows.value = [];
  total.value = 0;
  loading.value = false;
}
function current(run: number) {
  try {
    return foreground && run === sequence && inviteScope() === owner;
  } catch {
    return false;
  }
}
const unsubscribe = onSessionChanged(() => {
  let next = '';
  try {
    next = inviteScope();
  } catch {}
  if (next !== owner) {
    clear();
    pending.value = [];
  }
});
onShow(() => {
  foreground = true;
  void load(true);
});
onHide(() => {
  foreground = false;
  clear();
  pending.value = [];
});
onUnload(() => {
  foreground = false;
  clear();
  pending.value = [];
  unsubscribe();
});
async function load(reset = false) {
  if (loading.value) return;
  clear();
  if (reset) pageNum.value = 1;
  const run = sequence;
  loading.value = true;
  error.value = '';
  try {
    owner = inviteScope();
    await ensureInviteSession();
    if (!current(run)) return;
    pending.value = pendingInvites();
    const result = await listInvites(pageNum.value, status.value, orderNo.value);
    if (!current(run)) return;
    rows.value = result.rows;
    total.value = result.total;
    // 有效记录到绝对截止即撤回列表投影；本地不改写服务端EXPIRED或续期。
    guard.arm(
      rows.value.filter((row) => row.status === 'WAIT_RESPONSE').map((row) => row.expiresAt)
    );
  } catch (failure) {
    if (!foreground || run !== sequence) return;
    rows.value = [];
    total.value = 0;
    error.value = failure instanceof Error ? failure.message : '本人邀约暂不可读取';
    pending.value = pendingInvites();
  } finally {
    if (foreground && run === sequence) loading.value = false;
  }
}
function choose(event: { detail: { value: string } }) {
  status.value = statuses[Number(event.detail.value)];
  void load(true);
}
function page(delta: number) {
  pageNum.value += delta;
  void load();
}
function detail(id: string) {
  uni.navigateTo({
    url: '/pages/dispatch/invite?inviteId=' + encodeURIComponent(id)
  });
}
function work() {
  uni.reLaunch({ url: '/pages/work/index' });
}
</script>
<template>
  <view class="screen">
    <text class="title">我的派单邀约</text>
    <text class="muted"
      >仅本人邀约。人工发出邀请仍需你明确响应，不会替你接单；收费不作为本单收入展示。</text
    >
    <view class="card filters">
      <picker
        :range="statuses.map((value) => (value ? inviteLabel(value) : '全部状态'))"
        @change="choose"
      >
        <view class="picker">{{ status ? inviteLabel(status) : '全部状态' }}</view>
      </picker>
      <input v-model="orderNo" class="input" placeholder="订单编号（可选）" />
      <button :disabled="loading" @click="load(true)">查询本人邀约</button>
    </view>
    <view v-for="intent in pending" :key="intent.inviteId" class="notice">
      <text
        >邀约 {{ intent.inviteId }} 的原{{ inviteLabel(intent.body.decision) }}结果尚未确认。</text
      >
      <button @click="detail(intent.inviteId)">进入原邀约核对结果</button>
    </view>
    <text v-if="loading" class="muted">正在核对当前代驾身份及本人记录…</text>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <view v-for="row in rows" :key="row.inviteId" class="card invite-card">
      <view class="between"
        ><text class="section">{{ row.stationName }}</text
        ><text class="badge">{{ inviteLabel(row.status) }}</text></view
      >
      <text>{{ row.vehicleTypeName }} · {{ row.vehiclePlateMasked || '车牌摘要未提供' }}</text>
      <text>接车范围：{{ row.pickupArea }}</text>
      <text
        >上门时段：{{ inviteTime(row.pickupSlotStartAt) }} —
        {{ inviteTime(row.pickupSlotEndAt) }}</text
      >
      <text>接车点直线距离：{{ straightDistance(row.distanceMeter) }}</text>
      <text>响应截止：{{ inviteTime(row.expiresAt) }}</text>
      <text class="muted">{{ inviteLabel(row.source) }} · 订单 {{ row.orderNo }}</text>
      <button class="primary" @click="detail(row.inviteId)">查看本次邀约</button>
    </view>
    <text v-if="!loading && !error && !rows.length" class="muted"
      >当前查询没有本人邀约；不会以空列表推断在手承接已结束。</text
    >
    <view class="between">
      <button :disabled="loading || pageNum <= 1" @click="page(-1)">上一页</button>
      <text class="muted">第{{ pageNum }}页 · 共{{ total }}条</text>
      <button :disabled="loading || pageNum * 10 >= total" @click="page(1)">下一页</button>
    </view>
    <button @click="work">返回我的工作</button>
  </view>
</template>
<style scoped>
.screen {
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
.card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: 1.8;
}
.section {
  font-size: 17px;
  font-weight: 600;
}
.between {
  display: flex;
  gap: 12px;
  justify-content: space-between;
  align-items: center;
}
.between > button {
  flex: 1;
}
.badge {
  color: #005fcb;
  font-size: 13px;
}
.muted {
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}
.input,
.picker {
  padding: 10px 12px;
  border: 1px solid #d6dce5;
  border-radius: 8px;
  font-size: 14px;
}
.input {
  height: 42px;
}
.notice {
  padding: 14px;
  background: #fff4e5;
  border-radius: 10px;
  color: #854d0e;
  line-height: 1.8;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
button {
  margin: 0;
  width: 100%;
  font-size: 15px;
}
.primary {
  background: #005fcb;
  color: white;
}
</style>
