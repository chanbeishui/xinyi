<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import { getOrder, yuan } from '../../features/order/api';
import {
  applyCase,
  amountInFen,
  listCases,
  draftScope,
  type PublicCase
} from '../../features/aftersale/api';
import { intentKey, type Voucher } from '../../features/file/api';
import type { AfterSaleApply, OwnerOrderView } from '../../api/types';
import VoucherPicker from '../../components/aftersale/VoucherPicker.vue';
const order = ref<OwnerOrderView | null>(null),
  active = ref<PublicCase | null>(null),
  kind = ref<'CANCEL_REFUND' | 'REFUND_ONLY'>('CANCEL_REFUND'),
  reason = ref(''),
  amount = ref(''),
  vouchers = ref<Voucher[]>([]),
  loading = ref(false),
  busy = ref(false),
  uploading = ref(false),
  error = ref(''),
  unknown = ref(false);
const choices = ['取消服务并退款', '仅申请退款'];
let orderNo = '',
  seq = 0,
  visible = false,
  draftKey = '';
let attempt: { input: AfterSaleApply; key: string } | null = null;
const locked = computed(() => busy.value || unknown.value);
const permitted = computed(() =>
  order.value?.allowedActions.some((value) =>
    ['APPLY_AFTERSALE', 'APPLY_CANCEL_REFUND', 'APPLY_REFUND_ONLY'].includes(String(value))
  )
);
const paidRemainder = computed(() =>
  order.value ? Math.max(0, order.value.money.paidAmount - order.value.money.refundedAmount) : 0
);
onLoad((query) => {
  orderNo = String(query?.orderNo || '');
});
onShow(() => {
  visible = true;
  try {
    draftKey = draftScope('order.' + orderNo);
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : '请登录';
    return;
  }
  const saved = uni.getStorageSync(draftKey);
  if (saved && saved.orderNo === orderNo) {
    kind.value = saved.kind === 'REFUND_ONLY' ? 'REFUND_ONLY' : 'CANCEL_REFUND';
    reason.value = typeof saved.reason === 'string' ? saved.reason : '';
    amount.value = typeof saved.amount === 'string' ? saved.amount : '';
    vouchers.value = Array.isArray(saved.vouchers) ? saved.vouchers : [];
    attempt = saved.attempt || null;
    unknown.value = Boolean(attempt);
  }
  load();
});
function clearView() {
  saveDraft();
  visible = false;
  seq++;
  order.value = null;
  active.value = null;
  reason.value = '';
  amount.value = '';
  vouchers.value = [];
  attempt = null;
  unknown.value = false;
}
onHide(clearView);
onUnload(clearView);
watch(
  [kind, reason, amount, vouchers],
  () => {
    if (!locked.value) error.value = '';
    saveDraft();
  },
  { deep: true }
);
// 草稿按真实账号与订单隔离；只保存输入和已核验的文件ID，不存令牌、临时地址或文件票据。
function saveDraft() {
  if (!visible || !draftKey || active.value) return;
  uni.setStorageSync(draftKey, {
    orderNo,
    kind: kind.value,
    reason: reason.value,
    amount: amount.value,
    vouchers: vouchers.value,
    attempt
  });
}
async function load() {
  const run = ++seq;
  loading.value = true;
  error.value = '';
  try {
    const [value, cases] = await Promise.all([getOrder(orderNo), listCases(1, orderNo)]);
    if (run !== seq || !visible) return;
    if (value.view !== 'OWNER') throw new Error('请切换车主身份后申请售后');
    order.value = value;
    active.value =
      cases.rows.find((row) => ['PENDING_REVIEW', 'EXECUTING'].includes(row.caseStatus)) || null;
    if (active.value) {
      attempt = null;
      unknown.value = false;
      uni.removeStorageSync(draftKey);
    }
  } catch (failure) {
    if (run === seq && visible)
      error.value = failure instanceof Error ? failure.message : '加载失败';
  } finally {
    if (run === seq && visible) loading.value = false;
  }
}
function changeKind(event: { detail: { value: string | number } }) {
  if (!locked.value)
    kind.value = Number(event.detail.value) === 1 ? 'REFUND_ONLY' : 'CANCEL_REFUND';
}
function openCase(id: string) {
  uni.redirectTo({ url: '/pages/aftersale/detail?caseId=' + encodeURIComponent(id) });
}
async function submit() {
  if (busy.value || uploading.value || !order.value || active.value || !permitted.value) return;
  error.value = '';
  try {
    if (!attempt) {
      if (!reason.value.trim() || reason.value.trim().length > 1000)
        throw new Error('请填写 1 至 1000 字申请原因');
      const fen = amountInFen(amount.value.trim());
      if (fen > paidRemainder.value) throw new Error('申请金额不能超过已付未退金额');
      attempt = {
        input: {
          kind: kind.value,
          reason: reason.value.trim(),
          requestedAmount: fen,
          voucherFileIds: vouchers.value.map((file) => file.fileId),
          expectedOrderVersion: order.value.version
        },
        key: intentKey()
      };
    }
    // 请求结果未知时保留整份请求与原键，禁止编辑后用同一键提交另一份业务意图。
    unknown.value = true;
    saveDraft();
    busy.value = true;
    const row = await applyCase(orderNo, attempt.input, attempt.key);
    if (!visible) return;
    attempt = null;
    unknown.value = false;
    uni.removeStorageSync(draftKey);
    visible = false;
    openCase(row.caseId);
  } catch (failure) {
    if (!visible) return;
    const value = failure as { status?: number; message?: string };
    if (value.status && value.status < 500) {
      attempt = null;
      unknown.value = false;
      saveDraft();
      await load();
    }
    error.value = value.message || '提交结果尚未确认，请核对原申请后重试';
  } finally {
    if (visible) busy.value = false;
  }
}
</script>
<template>
  <view class="screen">
    <text class="title">申请售后</text>
    <text v-if="loading" class="muted">正在核对订单及已有申请…</text>
    <view v-if="active" class="card notice">
      <text class="section">已有进行中的申请</text>
      <text>同一订单只处理一笔进行中的售后，请在原申请中查看进度或补充说明。</text>
      <button @click="openCase(active.caseId)">查看原申请</button>
    </view>
    <template v-else-if="order">
      <view class="card">
        <text class="section">{{ order.vehicle.plateNo }} · {{ order.station.name }}</text>
        <text class="muted">{{ orderNo }}</text>
        <text
          >已付 ¥{{ yuan(order.money.paidAmount) }} · 已退 ¥{{
            yuan(order.money.refundedAmount)
          }}</text
        >
        <text class="muted"
          >当前已付未退 ¥{{ yuan(paidRemainder) }}，实际可退金额由服务端核验。</text
        >
      </view>
      <view v-if="!permitted" class="notice"
        >当前订单暂不可申请售后，请返回订单核对支付和服务状态。</view
      >
      <view v-else class="card">
        <text class="section">申请内容</text>
        <picker
          :range="choices"
          :value="kind === 'REFUND_ONLY' ? 1 : 0"
          :disabled="locked"
          @change="changeKind"
        >
          <view class="field">{{ kind === 'REFUND_ONLY' ? choices[1] : choices[0] }} ▾</view>
        </picker>
        <text class="muted">{{
          kind === 'CANCEL_REFUND'
            ? '申请取消服务并人工审核退款；已接车的订单需按审核安排完成还车。'
            : '仅申请退款，不取消服务，也不自动解除在手任务。'
        }}</text>
        <text class="label">申请金额（元）</text>
        <input
          v-model="amount"
          class="field"
          type="digit"
          :disabled="locked"
          maxlength="14"
          placeholder="例如 100.50"
          aria-label="申请金额（元）"
        />
        <text class="label">申请原因</text>
        <textarea
          v-model="reason"
          class="reason"
          :disabled="locked"
          maxlength="1000"
          placeholder="请说明申请原因，最多 1000 字"
          aria-label="申请原因"
        />
        <text class="muted">{{ reason.length }} / 1000 · 输入会在本机保存为草稿</text>
        <VoucherPicker
          v-model="vouchers"
          :order-no="orderNo"
          :disabled="locked"
          @busy="uploading = $event"
        />
      </view>
      <view class="notice"
        >付款后即使尚未接车，也需人工审核。提交申请不代表退款已批准或已发起。</view
      >
      <view v-if="unknown" class="notice"
        >上次提交结果待确认，已保留原请求。请先刷新核对；仍未查到申请时，可用原请求重试。</view
      >
      <button
        v-if="permitted"
        class="primary"
        :disabled="busy || uploading || loading"
        @click="submit"
      >
        {{ busy ? '正在提交…' : unknown ? '使用原请求核对并重试' : '提交售后申请' }}
      </button>
    </template>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="loading || busy" @click="load">刷新核对状态</button>
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
.card {
  padding: 18px;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 15px;
  line-height: 1.6;
}
.section,
.label {
  font-weight: 600;
}
.section {
  font-size: 17px;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  overflow-wrap: anywhere;
}
.field {
  padding: 12px;
  border-radius: 8px;
  background: #f4f7fb;
  font-size: 16px;
}
input.field {
  min-height: 24px;
}
.reason {
  box-sizing: border-box;
  width: 100%;
  min-height: 140px;
  height: 140px;
  padding: 12px;
  border-radius: 8px;
  background: #f4f7fb;
  font-size: 15px;
}
.notice {
  padding: 16px;
  border-radius: 12px;
  background: #fff4e5;
  color: #854d0e;
  font-size: 14px;
  line-height: 1.7;
}
.error {
  color: #b42318;
  line-height: 1.7;
}
button {
  width: 100%;
  margin: 0;
  font-size: 15px;
}
.primary {
  background: #005fcb;
  color: white;
}
</style>
