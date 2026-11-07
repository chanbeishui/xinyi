<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { checkPermi } from '@/utils/permission';
import {
  getRefund,
  queryRefund,
  retryRefund,
  financeLabel,
  financeReason,
  financeMoney,
  financeTime,
  type RefundDetail,
  type VersionReason
} from '@/api/rongli/refund';
import VersionReasonAction from './VersionReasonAction.vue';
const props = defineProps<{ modelValue: boolean; refundNo: string }>();
const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'changed'): void;
}>();
const row = ref<RefundDetail | null>(null),
  loading = ref(false),
  error = ref(''),
  actionOpen = ref(false),
  action = ref<'query' | 'retry'>('query');
let sequence = 0;
const permission = computed(() =>
  action.value === 'query' ? 'biz:refund:query' : 'biz:refund:retry'
);
const allowed = computed(
  () =>
    Boolean(
      row.value?.allowedActions.includes(action.value === 'query' ? 'QUERY_REFUND' : 'RETRY_REFUND')
    ) && checkPermi([permission.value])
);
const description = computed(() =>
  action.value === 'query'
    ? '向原支付渠道查询原退款号的可信结果。查询不会创建新退款号，未知结果仍保留原金额预留。'
    : '只对服务端已确认允许重试的原退款单执行。沿用原退款号、原支付方法和已批准金额，不新增退款决定。'
);
watch(
  () => [props.modelValue, props.refundNo],
  () => {
    if (props.modelValue) load().catch(() => undefined);
    else {
      sequence++;
      row.value = null;
      error.value = '';
    }
  },
  { immediate: true }
);
onBeforeUnmount(() => {
  sequence++;
});
async function load() {
  const run = ++sequence,
    no = props.refundNo;
  loading.value = true;
  error.value = '';
  row.value = null;
  try {
    const result = await getRefund(no);
    if (run === sequence && no === props.refundNo) row.value = result.data;
  } catch (failure: any) {
    if (run === sequence) error.value = failure.msg || failure.message || '退款详情读取失败';
    throw failure;
  } finally {
    if (run === sequence) loading.value = false;
  }
}
async function reload() {
  await load();
}
function showAction(kind: 'query' | 'retry') {
  action.value = kind;
  actionOpen.value = true;
}
async function submit(body: VersionReason, key: string) {
  const no = props.refundNo,
    run = sequence;
  const result = await (action.value === 'query'
    ? queryRefund(no, body, key)
    : retryRefund(no, body, key));
  if (run === sequence && no === props.refundNo) row.value = result.data;
}
function close(done: () => void) {
  if (actionOpen.value) return;
  sequence++;
  row.value = null;
  done();
  emit('update:modelValue', false);
}
function changed() {
  emit('changed');
}
</script>
<template>
  <el-drawer
    :model-value="modelValue"
    title="原退款单与渠道核查"
    size="min(850px, 100vw)"
    :before-close="close"
    append-to-body
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="refund-detail">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <div class="heading">
        <p class="number">原退款号：{{ refundNo }}</p>
        <el-button :loading="loading" :disabled="actionOpen" @click="load().catch(() => undefined)"
          >刷新本地事实</el-button
        >
      </div>
      <template v-if="row">
        <h2>{{ financeLabel(row.status) }}</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ row.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="原支付方法">{{
            financeLabel(row.paymentMethod)
          }}</el-descriptions-item>
          <el-descriptions-item label="执行方式">{{
            financeLabel(row.executionMode)
          }}</el-descriptions-item>
          <el-descriptions-item label="批准退款金额">{{
            financeMoney(row.amount)
          }}</el-descriptions-item>
          <el-descriptions-item label="原售后审核"
            >申请 {{ row.caseId }} · 审核版本 {{ row.reviewRevision }}</el-descriptions-item
          >
          <el-descriptions-item label="实际返还">{{
            financeLabel(row.returnState)
          }}</el-descriptions-item>
          <el-descriptions-item label="渠道退款号">{{
            row.channelRefundId || '尚未取得可信渠道号'
          }}</el-descriptions-item>
          <el-descriptions-item label="开始执行">{{
            financeTime(row.executionStartedAt)
          }}</el-descriptions-item>
          <el-descriptions-item label="渠道确认成功">{{
            financeTime(row.successAt)
          }}</el-descriptions-item>
          <el-descriptions-item label="记录版本">{{ row.version }}</el-descriptions-item>
        </el-descriptions>
        <section class="funds">
          <div>
            <small>原支付实付</small><strong>{{ financeMoney(row.paidAmount) }}</strong>
          </div>
          <div>
            <small>已成功退款</small><strong>{{ financeMoney(row.refundedAmount) }}</strong>
          </div>
          <div>
            <small>仍有效的资金预留</small><strong>{{ financeMoney(row.reservedAmount) }}</strong>
          </div>
        </section>
        <el-alert
          v-if="row.returnState === 'WAIT_RETURN'"
          title="等待车辆实际归还。退款金额继续预留，当前持车司机与承接不因本页查询而释放。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="row.status === 'UNKNOWN'"
          title="渠道结果未知，请先查询原退款号。未获得可信的未受理结论前，不能盲目重提或更换退款号，原金额预留继续保留。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="row.status === 'ABNORMAL'"
          title="退款异常待核查。按原退款号查询可信渠道事实，是否允许重试由服务端当前结论决定。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="row.status === 'CLOSED'"
          title="原渠道退款单已关闭，这是关闭终态，不表示退款成功。本页不能原号重提，也不会自动生成新退款号；后续按明确修订流程处理。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="row.executionMode === 'MANUAL_OFFLINE'"
          title="原支付为企业线下付款。需进入 X06 线下实际退款凭证及独立核实流程，本页不代替凭证确认，也不调用微信退款。"
          type="info"
          :closable="false"
          show-icon
        />
        <p v-if="row.executionMode === 'MANUAL_OFFLINE'" class="muted">
          线下凭证状态：{{
            row.manualProofState ? financeLabel(row.manualProofState) : '待线下凭证流程接通'
          }}
        </p>
        <p v-if="row.lastErrorCode" class="muted number">
          最近诊断：{{ financeReason(row.lastErrorCode) }}
        </p>
        <div class="actions">
          <el-button
            v-if="row.allowedActions.includes('QUERY_REFUND')"
            v-hasPermi="['biz:refund:query']"
            type="primary"
            @click="showAction('query')"
            >查询原退款号</el-button
          >
          <el-button
            v-if="row.allowedActions.includes('RETRY_REFUND')"
            v-hasPermi="['biz:refund:retry']"
            type="warning"
            @click="showAction('retry')"
            >受控重试原退款</el-button
          >
          <span v-if="!row.allowedActions.length" class="muted"
            >当前服务端未开放查询或重试操作。</span
          >
        </div>
      </template>
    </div>
    <VersionReasonAction
      v-model="actionOpen"
      :title="action === 'query' ? '查询原退款号' : '受控重试原退款'"
      :description="description"
      :target-key="refundNo + ':' + action"
      :version="row?.version ?? 0"
      :can-submit="allowed"
      :submit="submit"
      :reload="reload"
      @done="changed"
    />
  </el-drawer>
</template>
<style scoped>
.refund-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 180px;
}
.heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.heading p,
h2 {
  margin: 0;
}
.number {
  overflow-wrap: anywhere;
}
.muted {
  color: #64748b;
  line-height: 1.7;
}
.funds {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.funds div {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}
.funds small {
  color: #64748b;
}
.funds strong {
  font-size: 19px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.actions .el-button {
  margin-left: 0;
}
@media (max-width: 600px) {
  .heading {
    flex-direction: column;
    align-items: flex-start;
  }
  .funds {
    grid-template-columns: 1fr;
  }
}
</style>
