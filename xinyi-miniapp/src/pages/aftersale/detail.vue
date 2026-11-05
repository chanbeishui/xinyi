<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import {
  appendNote,
  getCase,
  caseInstruction,
  caseLabel,
  draftScope,
  type PublicCase
} from '../../features/aftersale/api';
import { intentKey, previewVoucher, type Voucher } from '../../features/file/api';
import { orderTime, yuan } from '../../features/order/api';
import type { AfterSaleNote } from '../../api/types';
import VoucherPicker from '../../components/aftersale/VoucherPicker.vue';
const row = ref<PublicCase | null>(null),
  loading = ref(false),
  busy = ref(false),
  uploading = ref(false),
  error = ref(''),
  note = ref(''),
  vouchers = ref<Voucher[]>([]),
  unknown = ref(false),
  notice = ref('');
let caseId = '',
  draftKey = '',
  seq = 0,
  visible = false;
let attempt: { input: AfterSaleNote; key: string } | null = null;
const canAppend = computed(() =>
  row.value?.allowedActions?.some((value) => String(value) === 'ADD_NOTE')
);
const locked = computed(() => busy.value || unknown.value);
const noteRevision = ref(0);
onLoad((query) => {
  caseId = String(query?.caseId || '');
});
onShow(() => {
  visible = true;
  try {
    draftKey = draftScope('note.' + caseId);
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : '请登录';
    return;
  }
  const saved = uni.getStorageSync(draftKey);
  if (saved && saved.caseId === caseId) {
    note.value = typeof saved.note === 'string' ? saved.note : '';
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
  row.value = null;
  note.value = '';
  vouchers.value = [];
  attempt = null;
  unknown.value = false;
}
onHide(clearView);
onUnload(clearView);
watch([note, vouchers], saveDraft, { deep: true });
function saveDraft() {
  if (visible && draftKey)
    uni.setStorageSync(draftKey, { caseId, note: note.value, vouchers: vouchers.value, attempt });
}
async function load() {
  const run = ++seq;
  loading.value = true;
  error.value = '';
  try {
    const value = await getCase(caseId);
    if (visible && run === seq) row.value = value;
  } catch (failure) {
    if (visible && run === seq)
      error.value = failure instanceof Error ? failure.message : '加载失败';
  } finally {
    if (visible && run === seq) loading.value = false;
  }
}
async function preview(fileId: string) {
  if (!row.value) return;
  error.value = '';
  try {
    await previewVoucher(fileId, row.value.orderNo);
  } catch (failure) {
    if (visible) error.value = failure instanceof Error ? failure.message : '查看凭证失败';
  }
}
async function submit() {
  if (!row.value || !canAppend.value || busy.value || uploading.value) return;
  error.value = '';
  notice.value = '';
  try {
    if (!attempt) {
      if (!note.value.trim() || note.value.trim().length > 1000)
        throw new Error('请填写 1 至 1000 字补充说明');
      attempt = {
        input: {
          expectedVersion: row.value.version,
          note: note.value.trim(),
          voucherFileIds: vouchers.value.map((file) => file.fileId)
        },
        key: intentKey()
      };
    }
    unknown.value = true;
    saveDraft();
    busy.value = true;
    const value = await appendNote(caseId, attempt.input, attempt.key);
    if (!visible) return;
    row.value = value;
    attempt = null;
    unknown.value = false;
    note.value = '';
    // uni-app 原生文本域在连续输入后可能保留内部值；成功追加后重建输入，避免误以为未提交。
    noteRevision.value++;
    vouchers.value = [];
    uni.removeStorageSync(draftKey);
    notice.value = '补充说明已提交，原申请内容保持不变。';
  } catch (failure) {
    if (!visible) return;
    const value = failure as { status?: number; message?: string };
    if (value.status && value.status < 500) {
      attempt = null;
      unknown.value = false;
      saveDraft();
      await load();
    }
    error.value = value.message || '结果尚未确认，已保留原补充请求';
  } finally {
    if (visible) busy.value = false;
  }
}
function orderDetail() {
  if (row.value)
    uni.navigateTo({ url: '/pages/order/detail?orderNo=' + encodeURIComponent(row.value.orderNo) });
}
</script>
<template>
  <view class="screen">
    <text class="title">售后进度</text>
    <text v-if="loading" class="muted">正在加载…</text>
    <template v-if="row">
      <view class="card">
        <text class="status">{{ caseLabel(row.caseStatus) }}</text>
        <text>{{ row.kind === 'CANCEL_REFUND' ? '取消服务并退款' : '仅申请退款' }}</text>
        <text class="muted number">申请 {{ row.caseId }} · 订单 {{ row.orderNo }}</text>
        <text class="muted">提交时间：{{ orderTime(row.createdAt) }}</text>
      </view>
      <view class="notice">{{ caseInstruction(row) }}</view>
      <view class="card">
        <text class="section">审核与退款</text>
        <text>申请金额 ¥{{ yuan(row.requestedAmount ?? 0) }}</text>
        <text>审核：{{ caseLabel(row.reviewStatus) }}</text>
        <text v-if="row.approvedAmount !== undefined && row.approvedAmount !== null"
          >批准金额 ¥{{ yuan(row.approvedAmount) }}</text
        >
        <text v-else class="muted">批准金额尚未确定</text>
        <text>还车：{{ caseLabel(row.returnState) }}</text>
        <text>退款：{{ caseLabel(row.refundState) }}</text>
        <text v-if="row.refundNo" class="muted number">退款编号：{{ row.refundNo }}</text>
        <text v-if="row.publicOpinion">处理说明：{{ row.publicOpinion }}</text>
      </view>
      <view class="card">
        <text class="section">原申请</text>
        <text class="preserve">{{ row.reason }}</text>
        <text v-if="!row.voucherFileIds?.length" class="muted">未附凭证图片</text>
        <button v-for="(id, index) in row.voucherFileIds" :key="id" @click="preview(id)">
          查看原凭证 {{ index + 1 }}
        </button>
      </view>
      <view class="card">
        <text class="section">补充记录</text>
        <text v-if="!row.notes?.length" class="muted">暂无补充说明</text>
        <view v-for="item in row.notes" :key="item.noteId" class="history">
          <text class="muted">{{ orderTime(item.createdAt) }}</text>
          <text class="preserve">{{ item.note }}</text>
          <button v-for="(id, index) in item.voucherFileIds" :key="id" @click="preview(id)">
            查看补充凭证 {{ index + 1 }}
          </button>
        </view>
      </view>
      <view v-if="canAppend" class="card">
        <text class="section">补充说明</text>
        <text class="muted">补充会作为新的记录保留，不能修改原申请原因和金额。</text>
        <textarea
          :key="noteRevision"
          v-model="note"
          :disabled="locked"
          class="note"
          maxlength="1000"
          placeholder="补充说明，最多 1000 字"
          aria-label="补充说明"
        />
        <VoucherPicker
          v-model="vouchers"
          :order-no="row.orderNo"
          :disabled="locked"
          @busy="uploading = $event"
        />
        <text v-if="unknown" class="notice"
          >上次补充结果待确认，已保留原请求。刷新核对后可沿用原请求重试。</text
        >
        <button :disabled="busy || uploading || loading" class="primary" @click="submit">
          {{ busy ? '正在提交…' : unknown ? '重试原补充请求' : '提交补充说明' }}
        </button>
      </view>
      <button @click="orderDetail">查看订单</button>
    </template>
    <text v-if="notice" class="success" role="status">{{ notice }}</text>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="loading || busy" @click="load">刷新售后进度</button>
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
  line-height: 1.7;
}
.section {
  font-size: 17px;
  font-weight: 600;
}
.status {
  font-size: 22px;
  font-weight: 600;
  color: #005fcb;
}
.muted {
  color: #64748b;
  font-size: 13px;
}
.number,
.preserve {
  overflow-wrap: anywhere;
}
.preserve {
  white-space: pre-wrap;
}
.notice {
  padding: 16px;
  background: #fff4e5;
  color: #854d0e;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
}
.history {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 0;
  border-top: 1px solid #e2e8f0;
}
.note {
  box-sizing: border-box;
  width: 100%;
  height: 140px;
  padding: 12px;
  background: #f4f7fb;
  border-radius: 8px;
  font-size: 15px;
}
button {
  width: 100%;
  margin: 0;
  font-size: 14px;
}
.primary {
  color: white;
  background: #005fcb;
}
.error {
  color: #b42318;
  line-height: 1.7;
}
.success {
  color: #067647;
  line-height: 1.7;
}
</style>
