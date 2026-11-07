<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { checkPermi } from '@/utils/permission';
import {
  listReconciliation,
  queryReconciliation,
  resolveReconciliation,
  financeLabel,
  financeReason,
  financeTime,
  type ReconciliationCase,
  type VersionReason
} from '@/api/rongli/refund';
import RefundDrawer from '../refund/RefundDrawer.vue';
import VersionReasonAction from '../refund/VersionReasonAction.vue';
const query = reactive({ orderNo: '', type: '', status: 'OPEN' });
const rows = ref<ReconciliationCase[]>([]),
  page = ref(1),
  total = ref(0),
  loading = ref(false),
  error = ref('');
const detail = ref<ReconciliationCase | null>(null),
  detailOpen = ref(false),
  actionOpen = ref(false),
  action = ref<'query' | 'resolve'>('query'),
  refundOpen = ref(false),
  refundNo = ref('');
let sequence = 0;
const allowed = computed(
  () =>
    Boolean(
      detail.value?.allowedActions.includes(
        action.value === 'query' ? 'QUERY_RECONCILIATION' : 'RESOLVE_RECONCILIATION'
      )
    ) &&
    checkPermi([
      action.value === 'query'
        ? 'biz:paymentreconciliation:query'
        : 'biz:paymentreconciliation:resolve'
    ])
);
const description = computed(() =>
  action.value === 'query'
    ? '重新核对原支付或原退款号的可信渠道结果。未知状态继续保留，不凭手工输入认定已付款或已退款。'
    : '仅在服务端已确认资金事实收敛后办结此异常。本操作不手工改写已付、已退金额或渠道状态，不解除资金预留；条件不满足时会拒绝办结。'
);
const typeLabel = (value: string) =>
  value === 'PAYMENT' ? '付款异常' : value === 'REFUND' ? '退款异常' : value;
onMounted(() => load());
onBeforeUnmount(() => {
  sequence++;
  detail.value = null;
});
async function load(reset = false) {
  const run = ++sequence;
  if (reset) page.value = 1;
  rows.value = [];
  total.value = 0;
  loading.value = true;
  error.value = '';
  try {
    const params: Record<string, string | number> = { pageNum: page.value, pageSize: 20 };
    Object.entries(query).forEach(([key, value]) => {
      if (typeof value === 'string' && value.trim()) params[key] = value.trim();
    });
    const result = await listReconciliation(params);
    if (run === sequence) {
      rows.value = result.rows;
      total.value = result.total;
    }
  } catch (failure: any) {
    if (run === sequence) error.value = failure.msg || failure.message || '异常列表读取失败';
  } finally {
    if (run === sequence) loading.value = false;
  }
}
function reset() {
  Object.assign(query, { orderNo: '', type: '', status: 'OPEN' });
  load(true);
}
function show(row: ReconciliationCase) {
  detail.value = row;
  detailOpen.value = true;
}
function showAction(kind: 'query' | 'resolve') {
  action.value = kind;
  actionOpen.value = true;
}
function openRefund(no: string) {
  refundNo.value = no;
  refundOpen.value = true;
}
async function reloadDetail() {
  const id = detail.value?.caseId;
  if (!id) throw new Error('请重新选择原异常记录');
  // 精确重读原异常，不能拿列表中另一条记录的版本去办结当前记录。
  try {
    const result = await listReconciliation({ pageNum: 1, pageSize: 1, caseId: id });
    const current = result.rows.find((row: ReconciliationCase) => row.caseId === id);
    if (!current) {
      detail.value = null;
      throw new Error('原异常不存在或当前不可访问，请返回列表核对');
    }
    if (detail.value?.caseId === id) detail.value = current;
  } catch (failure: any) {
    // 撤权或记录变为不可见时清除旧详情，不能继续把旧版本当作可操作事实。
    if (failure.httpStatus === 403 || failure.httpStatus === 404) detail.value = null;
    throw failure;
  }
}
async function submit(body: VersionReason, key: string) {
  const id = detail.value?.caseId;
  if (!id) throw new Error('请重新选择原异常记录');
  const result = await (action.value === 'query'
    ? queryReconciliation(id, body, key)
    : resolveReconciliation(id, body, key));
  if (detail.value?.caseId === id) detail.value = result.data;
}
function close(done: () => void) {
  if (actionOpen.value || refundOpen.value) return;
  detail.value = null;
  done();
}
</script>
<template>
  <div class="app-container reconciliation-page">
    <div class="heading">
      <div>
        <h2>客户支付对账异常</h2>
        <p>按原收款主体授权范围查看实际异常，核对本地业务与可信渠道事实。</p>
      </div>
      <el-button :loading="loading" @click="load()">刷新列表</el-button>
    </div>
    <el-alert
      title="对账办结不等于手工确认付款或退款。资金结果未知时继续查原单，不更换退款号、不盲目重提。"
      type="info"
      :closable="false"
      show-icon
    />
    <el-form class="filters" inline label-position="top" @submit.prevent="load(true)">
      <el-form-item label="订单号"
        ><el-input
          v-model="query.orderNo"
          placeholder="输入原订单号"
          clearable
          style="width: 280px"
      /></el-form-item>
      <el-form-item label="异常类型"
        ><el-select v-model="query.type" clearable placeholder="全部类型" style="width: 150px"
          ><el-option label="付款异常" value="PAYMENT" /><el-option
            label="退款异常"
            value="REFUND" /></el-select
      ></el-form-item>
      <el-form-item label="处理状态"
        ><el-select v-model="query.status" clearable placeholder="全部状态" style="width: 150px"
          ><el-option
            v-for="status in ['OPEN', 'CHECKING', 'RESOLVED']"
            :key="status"
            :label="financeLabel(status)"
            :value="status" /></el-select
      ></el-form-item>
      <el-form-item label="操作"
        ><el-button type="primary" native-type="submit" :loading="loading">查询</el-button
        ><el-button @click="reset">重置</el-button></el-form-item
      >
    </el-form>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-table
      v-loading="loading"
      :data="rows"
      row-key="caseId"
      empty-text="当前范围暂无符合条件的真实异常记录"
    >
      <el-table-column prop="caseId" label="异常编号" width="110" />
      <el-table-column prop="orderNo" label="原订单号" min-width="270" />
      <el-table-column label="异常类型" width="120"
        ><template #default="{ row }">{{ typeLabel(row.type) }}</template></el-table-column
      >
      <el-table-column label="原支付方式" width="140"
        ><template #default="{ row }">{{
          financeLabel(row.paymentMethod)
        }}</template></el-table-column
      >
      <el-table-column label="本地 / 渠道状态" min-width="180"
        ><template #default="{ row }"
          ><div>{{ financeLabel(row.localStatus) }}</div>
          <small
            >渠道：{{ row.channelStatus ? financeLabel(row.channelStatus) : '尚无可信结论' }}</small
          ></template
        ></el-table-column
      >
      <el-table-column label="处理状态" width="115"
        ><template #default="{ row }"
          ><el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'">{{
            financeLabel(row.status)
          }}</el-tag></template
        ></el-table-column
      >
      <el-table-column label="异常原因" min-width="200">
        <template #default="{ row }">{{ financeReason(row.reasonCode) }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="110"
        ><template #default="{ row }"
          ><el-button link type="primary" @click="show(row)">查看核对</el-button></template
        ></el-table-column
      >
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :page-size="20"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load()"
    />
    <el-drawer
      v-model="detailOpen"
      title="对账异常核对"
      size="min(760px, 100vw)"
      :before-close="close"
      destroy-on-close
    >
      <div v-if="detail" class="detail">
        <h2>{{ typeLabel(detail.type) }} · {{ financeLabel(detail.status) }}</h2>
        <el-descriptions :column="1" border
          ><el-descriptions-item label="异常编号">{{ detail.caseId }}</el-descriptions-item
          ><el-descriptions-item label="原订单号">{{ detail.orderNo }}</el-descriptions-item
          ><el-descriptions-item label="原业务主体编号">{{ detail.subjectId }}</el-descriptions-item
          ><el-descriptions-item label="原支付方式">{{
            financeLabel(detail.paymentMethod)
          }}</el-descriptions-item
          ><el-descriptions-item label="本地资金状态">{{
            financeLabel(detail.localStatus)
          }}</el-descriptions-item
          ><el-descriptions-item label="可信渠道结论">{{
            detail.channelStatus ? financeLabel(detail.channelStatus) : '尚未取得可信结论'
          }}</el-descriptions-item
          ><el-descriptions-item label="原退款号">{{
            detail.refundNo || '付款异常无退款号'
          }}</el-descriptions-item
          ><el-descriptions-item label="异常原因">{{
            financeReason(detail.reasonCode)
          }}</el-descriptions-item
          ><el-descriptions-item label="最近可信核验">{{
            financeTime(detail.verifiedAt || null)
          }}</el-descriptions-item
          ><el-descriptions-item label="异常办结时间">{{
            financeTime(detail.resolvedAt || null)
          }}</el-descriptions-item
          ><el-descriptions-item label="记录版本">{{
            detail.version
          }}</el-descriptions-item></el-descriptions
        >
        <el-alert
          v-if="detail.status !== 'RESOLVED'"
          title="请先核对可信资金事实。仍未知、不匹配或线下凭证未核实的记录，不能通过手工办结将其改为已成功。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="detail.paymentMethod === 'OFFLINE'"
          title="此笔为企业线下付款。实际收退款需独立凭证核实，不通过微信查询或本页手工改资金状态。"
          type="info"
          :closable="false"
        />
        <div class="actions">
          <el-button
            v-if="detail.allowedActions.includes('QUERY_RECONCILIATION')"
            v-hasPermi="['biz:paymentreconciliation:query']"
            type="primary"
            @click="showAction('query')"
            >重查可信渠道</el-button
          ><el-button
            v-if="detail.allowedActions.includes('RESOLVE_RECONCILIATION')"
            v-hasPermi="['biz:paymentreconciliation:resolve']"
            type="warning"
            @click="showAction('resolve')"
            >按可信事实办结</el-button
          ><el-button
            v-if="detail.refundNo"
            v-hasPermi="['biz:refund:query']"
            @click="openRefund(detail.refundNo)"
            >查看原退款单</el-button
          >
        </div>
        <p v-if="!detail.allowedActions.length" class="muted">当前服务端未开放重查或办结操作。</p>
      </div>
      <VersionReasonAction
        v-model="actionOpen"
        :title="action === 'query' ? '重查可信渠道' : '按可信事实办结'"
        :description="description"
        :target-key="'reconciliation:' + detail?.caseId + ':' + action"
        :version="detail?.version ?? 0"
        :can-submit="allowed"
        :submit="submit"
        :reload="reloadDetail"
        @done="load()"
      />
    </el-drawer>
    <RefundDrawer v-model="refundOpen" :refund-no="refundNo" @changed="load()" />
  </div>
</template>
<style scoped>
.heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.heading p,
.muted,
small {
  color: #64748b;
  line-height: 1.7;
}
.filters {
  margin-top: 20px;
  padding: 18px 18px 0;
  background: #f8fafc;
  border-radius: 8px;
}
.el-pagination {
  margin-top: 20px;
}
.detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.detail h2 {
  margin: 0;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.actions .el-button {
  margin-left: 0;
}
@media (max-width: 650px) {
  .heading {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
