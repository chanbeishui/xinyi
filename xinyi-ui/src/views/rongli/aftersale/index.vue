<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getOrder, type AdminOrderView } from '@/api/rongli/order';
import {
  appendNote,
  amountFen,
  createCase,
  getCase,
  intentKey,
  listCases,
  previewReview,
  reviewCase,
  type AdminCase,
  type ReviewInput,
  type ReviewPreview
} from '@/api/rongli/aftersale';
import type { AfterSaleApply, AfterSaleNote } from '../../../../../xinyi-miniapp/src/api/types';
import type { Voucher } from '@/api/rongli/privateFile';
import VoucherFiles from './VoucherFiles.vue';
const route = useRoute();
const router = useRouter();
const rows = ref<AdminCase[]>([]),
  total = ref(0),
  page = ref(1),
  loading = ref(false),
  error = ref('');
const scope = ref<'all' | 'order' | 'owner'>('all'),
  resource = ref('');
const query = reactive({ status: '', kind: '', returnState: '', refundState: '' });
const detail = ref<AdminCase | null>(null),
  detailOpen = ref(false),
  detailLoading = ref(false),
  detailError = ref('');
const preview = ref<ReviewPreview | null>(null),
  previewBusy = ref(false),
  reviewBusy = ref(false),
  reviewUnknown = ref(false),
  needsRefresh = ref(false);
const reviewForm = reactive({
  decision: 'APPROVE' as 'APPROVE' | 'REJECT',
  amount: '',
  opinion: '',
  publicOpinion: ''
});
const note = ref(''),
  noteFiles = ref<Voucher[]>([]),
  noteBusy = ref(false),
  noteUploading = ref(false),
  noteUnknown = ref(false);
const createOpen = ref(false),
  createBusy = ref(false),
  createUnknown = ref(false),
  createUploading = ref(false),
  checkingOrder = ref(false),
  checkedOrder = ref<AdminOrderView | null>(null),
  createError = ref('');
const apply = reactive({
  orderNo: '',
  kind: 'CANCEL_REFUND' as 'CANCEL_REFUND' | 'REFUND_ONLY',
  amount: '',
  reason: ''
});
const applyFiles = ref<Voucher[]>([]);
let listSequence = 0,
  detailSequence = 0,
  orderSequence = 0,
  previewSignature = '';
let reviewAttempt: { body: ReviewInput; key: string } | null = null;
let noteAttempt: { body: AfterSaleNote; key: string } | null = null;
let createAttempt: { body: AfterSaleApply; key: string; orderNo: string } | null = null;
const can = (...permissions: string[]) =>
  permissions.every((permission) => checkPermi([permission]));
const mayPreview = computed(() => can('biz:aftersale:reviewpreview', 'biz:refund:query'));
const mayPreparePreview = computed(
  () =>
    mayPreview.value &&
    detail.value?.caseStatus === 'PENDING_REVIEW' &&
    !detail.value.selfReviewDenied
);

const mayReview = computed(
  () =>
    can('biz:aftersale:review', 'biz:refund:approve') &&
    detail.value?.allowedActions.includes('REVIEW_AFTERSALE') &&
    !detail.value.selfReviewDenied
);
const canUpload = computed(() => can('biz:uploads:create', 'biz:uploads:complete'));
const reviewLocked = computed(() => reviewBusy.value || reviewUnknown.value);
const decisionText = computed(() => {
  if (reviewForm.decision === 'REJECT')
    return '驳回申请，不新增资金预留，不改变当前履约与保管事实。';
  if (detail.value?.kind === 'REFUND_ONLY')
    return '仅执行退款决定，保留当前履约、保管方及司机承接。';
  if (preview.value?.returnRequired)
    return '取消通过后等待实际归还车辆，预留批准金额；当前持车司机继续承担原任务，归还后再执行退款。';
  return '取消服务并处理未使用额度、对应邀约或承接；按本次批准金额预留退款，0 元不创建退款单。';
});
const labels: Record<string, string> = {
  CANCEL_REFUND: '取消并退款',
  REFUND_ONLY: '仅退款',
  PENDING_REVIEW: '待审核',
  EXECUTING: '执行中',
  CLOSED: '已办结',
  REJECTED: '已驳回',
  PENDING: '待审核',
  APPROVED: '已批准',
  UNDETERMINED: '尚未确定',
  NOT_REQUIRED: '无需处理',
  WAIT_RETURN: '等待还车',
  RETURNED: '已还车',
  NONE: '未发起',
  READY: '待执行',
  PROCESSING: '处理中',
  SUCCESS: '已成功',
  UNKNOWN: '结果未知',
  ABNORMAL: '异常待核查',
  OWNER: '车主',
  DRIVER: '代驾',
  STATION: '检测站',
  ACCOUNT: '小程序账号',
  SYS_USER: '后台人员'
};
const label = (value?: string | null) => (value ? labels[value] || value : '—');
const fulfillmentLabels: Record<string, string> = {
  PENDING_PAY: '待付款',
  SCHEDULED: '已预约，待派单',
  PENDING_DISPATCH: '待派单',
  DISPATCHING: '派单中',
  MANUAL_DISPATCH: '待人工派单',
  ACCEPTED: '代驾已接单',
  GOING_PICKUP: '前往接车',
  ARRIVED_PICKUP: '代驾已到达',
  PICKED_UP: '车辆已接走',
  ARRIVED_STATION: '车辆已到站',
  INSPECTING: '检测中',
  INSPECTED: '检测完成',
  CAR_TAKEN: '已取车返还',
  WAIT_CONFIRM: '待车主确认',
  COMPLETED: '服务已完成',
  CANCELLED: '服务已取消',
  CLOSED: '订单已关闭'
};
const fulfillment = (value: string) => fulfillmentLabels[value] || value;

const money = (value?: number | null) =>
  value === null || value === undefined ? '未授权查看' : '¥' + (value / 100).toFixed(2);
const time = (value?: string | null) =>
  value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—';
const message = (failure: any) => failure.msg || failure.message || '操作失败，请核对当前状态';
onMounted(() => {
  if (route.query.orderNo) {
    scope.value = 'order';
    resource.value = String(route.query.orderNo);
  } else if (route.query.ownerId) {
    scope.value = 'owner';
    resource.value = String(route.query.ownerId);
  }
  load();
});
onBeforeUnmount(() => {
  listSequence++;
  detailSequence++;
  orderSequence++;
});
watch([() => reviewForm.amount, () => reviewForm.decision], () => {
  preview.value = null;
  previewSignature = '';
});
watch(
  () => apply.orderNo,
  () => {
    orderSequence++;
    checkingOrder.value = false;
    checkedOrder.value = null;
    applyFiles.value = [];
  }
);

async function load(reset = false) {
  const run = ++listSequence;
  if (reset) page.value = 1;
  loading.value = true;
  rows.value = [];
  total.value = 0;
  error.value = '';
  try {
    if (scope.value !== 'all' && !resource.value.trim())
      throw new Error('请填写订单号或车主账号编号');
    const params: Record<string, string | number> = { pageNum: page.value, pageSize: 20 };
    Object.entries(query).forEach(([key, value]) => {
      if (typeof value === 'string' && value) params[key] = value;
    });
    const result = await listCases(params, scope.value, resource.value.trim());
    if (run === listSequence) {
      rows.value = result.rows;
      total.value = result.total;
    }
  } catch (failure) {
    if (run === listSequence) error.value = message(failure);
  } finally {
    if (run === listSequence) loading.value = false;
  }
}
function resetFilters() {
  Object.assign(query, { status: '', kind: '', returnState: '', refundState: '' });
  scope.value = 'all';
  resource.value = '';
  load(true);
}
async function show(id: string, preserve = false) {
  if (!preserve) {
    detail.value = null;
    Object.assign(reviewForm, { decision: 'APPROVE', amount: '', opinion: '', publicOpinion: '' });
    note.value = '';
    noteFiles.value = [];
    reviewAttempt = null;
    noteAttempt = null;
    reviewUnknown.value = false;
    noteUnknown.value = false;
  }
  const run = ++detailSequence;
  previewBusy.value = false;
  detailOpen.value = true;
  detailLoading.value = true;
  detailError.value = '';
  preview.value = null;
  try {
    const result = await getCase(id);
    if (run !== detailSequence) return;
    detail.value = result.data;
    needsRefresh.value = false;
    if (!preserve && result.data.requestedAmount !== null)
      reviewForm.amount = (result.data.requestedAmount / 100).toFixed(2);
    // 审核结果未知时，读取到已生效决定才解除本地重试锁；不能用另一个幂等键再做一笔审核。
    if (reviewUnknown.value && result.data.reviewStatus !== 'PENDING') {
      reviewUnknown.value = false;
      reviewAttempt = null;
    }
  } catch (failure) {
    if (run === detailSequence) detailError.value = message(failure);
  } finally {
    if (run === detailSequence) detailLoading.value = false;
  }
}
function beforeClose(done: () => void) {
  if (
    reviewUnknown.value ||
    noteUnknown.value ||
    reviewBusy.value ||
    noteBusy.value ||
    noteUploading.value
  ) {
    ElMessage.warning('操作结果尚未核对，请先刷新原申请或沿用原请求重试');
    return;
  }
  detailSequence++;
  detail.value = null;
  preview.value = null;
  done();
}
function approvedFen() {
  if (reviewForm.decision === 'REJECT') return 0;
  const value = amountFen(reviewForm.amount.trim());
  if (detail.value?.kind === 'REFUND_ONLY' && value <= 0)
    throw new Error('仅退款的批准金额必须大于 0');
  return value;
}
async function makePreview() {
  if (
    !detail.value ||
    detailLoading.value ||
    !mayPreview.value ||
    previewBusy.value ||
    needsRefresh.value ||
    reviewLocked.value
  )
    return;
  detailError.value = '';
  const id = detail.value.caseId,
    version = detail.value.version;
  const signature = reviewForm.decision + ':' + reviewForm.amount;
  const run = detailSequence;
  previewBusy.value = true;
  try {
    const result = await previewReview(id, version, approvedFen(), reviewForm.decision);
    if (run === detailSequence && signature === reviewForm.decision + ':' + reviewForm.amount) {
      if (result.data.decision !== reviewForm.decision) {
        throw new Error('预览决定与当前选择不一致，请重新核对');
      }
      preview.value = result.data;
      previewSignature = signature;
    }
  } catch (failure: any) {
    if (run === detailSequence) {
      detailError.value = message(failure);
      if (failure.httpStatus === 409) {
        needsRefresh.value = true;
        preview.value = null;
      }
    }
  } finally {
    if (run === detailSequence) previewBusy.value = false;
  }
}
async function submitReview() {
  if (!detail.value || detailLoading.value || !mayReview.value || reviewBusy.value) return;
  detailError.value = '';
  const run = detailSequence;
  reviewBusy.value = true;
  try {
    if (!reviewAttempt) {
      if (
        !preview.value ||
        needsRefresh.value ||
        previewSignature !== reviewForm.decision + ':' + reviewForm.amount
      )
        throw new Error('请先刷新并核对本次处理预览');
      if (!reviewForm.opinion.trim() || !reviewForm.publicOpinion.trim())
        throw new Error('内部审核意见和车主可见处理说明均为必填');
      if (reviewForm.opinion.trim().length > 1000 || reviewForm.publicOpinion.trim().length > 1000)
        throw new Error('审核意见与公开说明分别不超过 1000 字');
      const confirmed = await ElMessageBox.confirm(
        decisionText.value,
        reviewForm.decision === 'APPROVE' ? '确认批准' : '确认驳回',
        { type: 'warning', confirmButtonText: '确认提交', cancelButtonText: '返回核对' }
      ).catch(() => false);
      if (!confirmed || run !== detailSequence) return;
      reviewAttempt = {
        body: {
          expectedVersion: preview.value.caseVersion,
          observedOrderVersion: preview.value.orderVersion,
          decision: reviewForm.decision,
          approvedAmount: approvedFen(),
          opinion: reviewForm.opinion.trim(),
          publicOpinion: reviewForm.publicOpinion.trim()
        },
        key: intentKey()
      };
    }
    const result = await reviewCase(detail.value.caseId, reviewAttempt.body, reviewAttempt.key);
    if (run !== detailSequence) return;
    detail.value = result.data;
    reviewAttempt = null;
    reviewUnknown.value = false;
    preview.value = null;
    ElMessage.success('统一审核决定已保存；退款执行状态以服务端事实为准');
    load();
  } catch (failure: any) {
    if (run !== detailSequence) return;
    detailError.value = message(failure);
    if (failure.httpStatus && failure.httpStatus < 500) {
      reviewAttempt = null;
      reviewUnknown.value = false;
      preview.value = null;
      if (failure.httpStatus === 409) needsRefresh.value = true;
    } else if (reviewAttempt) reviewUnknown.value = true;
  } finally {
    if (run === detailSequence) reviewBusy.value = false;
  }
}
async function submitNote() {
  if (!detail.value || detailLoading.value || noteBusy.value || noteUploading.value) return;
  const run = detailSequence;
  detailError.value = '';
  try {
    if (!noteAttempt) {
      if (!note.value.trim() || note.value.trim().length > 1000)
        throw new Error('公开补充说明需填写 1 至 1000 字');
      noteAttempt = {
        body: {
          expectedVersion: detail.value.version,
          note: note.value.trim(),
          voucherFileIds: noteFiles.value.map((file: Voucher) => file.fileId)
        },
        key: intentKey()
      };
    }
    noteBusy.value = true;
    const result = await appendNote(detail.value.caseId, noteAttempt.body, noteAttempt.key);
    if (run !== detailSequence) return;
    detail.value = result.data;
    note.value = '';
    noteFiles.value = [];
    noteAttempt = null;
    noteUnknown.value = false;
    preview.value = null;
    ElMessage.success('车主可见补充说明已追加');
  } catch (failure: any) {
    if (run !== detailSequence) return;
    detailError.value = message(failure);
    if (failure.httpStatus && failure.httpStatus < 500) {
      noteAttempt = null;
      noteUnknown.value = false;
    } else if (noteAttempt) noteUnknown.value = true;
  } finally {
    if (run === detailSequence) noteBusy.value = false;
  }
}
function openOriginalRefund() {
  if (detail.value?.refundNo) {
    router.push({ path: '/payment-finance/refunds', query: { refundNo: detail.value.refundNo } });
  }
}
function newCase() {
  Object.assign(apply, {
    orderNo: scope.value === 'order' ? resource.value : '',
    kind: 'CANCEL_REFUND',
    amount: '',
    reason: ''
  });
  checkedOrder.value = null;
  applyFiles.value = [];
  createError.value = '';
  createAttempt = null;
  createUnknown.value = false;
  createOpen.value = true;
}
async function checkOrder() {
  if (checkingOrder.value || !apply.orderNo.trim()) return;
  const run = ++orderSequence;
  checkingOrder.value = true;
  createError.value = '';
  checkedOrder.value = null;
  try {
    const result = await getOrder(apply.orderNo.trim());
    if (run === orderSequence) checkedOrder.value = result.data;
  } catch (failure) {
    if (run === orderSequence) createError.value = message(failure);
  } finally {
    if (run === orderSequence) checkingOrder.value = false;
  }
}
async function submitCreate() {
  if (!checkedOrder.value || createBusy.value || createUploading.value) return;
  createError.value = '';
  try {
    if (!createAttempt) {
      if (!apply.reason.trim() || apply.reason.trim().length > 1000)
        throw new Error('申请原因需填写 1 至 1000 字');
      createAttempt = {
        body: {
          kind: apply.kind,
          reason: apply.reason.trim(),
          requestedAmount: amountFen(apply.amount.trim()),
          expectedOrderVersion: checkedOrder.value.version,
          voucherFileIds: applyFiles.value.map((file: Voucher) => file.fileId)
        },
        key: intentKey(),
        orderNo: checkedOrder.value.orderNo
      };
    }
    createBusy.value = true;
    const result = await createCase(createAttempt.orderNo, createAttempt.body, createAttempt.key);
    createAttempt = null;
    createUnknown.value = false;
    createOpen.value = false;
    ElMessage.success('后台代办申请已提交，仍需独立人员审核');
    load();
    show(result.data.caseId);
  } catch (failure: any) {
    createError.value = message(failure);
    if (failure.httpStatus && failure.httpStatus < 500) {
      createAttempt = null;
      createUnknown.value = false;
      checkedOrder.value = null;
    } else if (createAttempt) createUnknown.value = true;
  } finally {
    createBusy.value = false;
  }
}
function closeCreate(done: () => void) {
  if (createBusy.value || createUnknown.value || createUploading.value) {
    ElMessage.warning('请先核对原申请提交结果');
    return;
  }
  orderSequence++;
  done();
}
</script>
<template>
  <div class="app-container aftersale-page">
    <div class="page-heading">
      <div>
        <h2>取消与退款</h2>
        <p>同一份申请完成取消、返还要求和退款金额审核；资金执行状态单独跟踪。</p>
      </div>
      <el-button v-hasPermi="['biz:aftersale:create']" type="primary" @click="newCase"
        >后台代办申请</el-button
      >
    </div>
    <el-form class="filters" inline label-position="top" @submit.prevent="load(true)">
      <el-form-item label="查看范围"
        ><el-select v-model="scope" style="width: 130px"
          ><el-option label="全部可见申请" value="all" /><el-option
            label="指定订单"
            value="order" /><el-option label="指定车主" value="owner" /></el-select
      ></el-form-item>
      <el-form-item v-if="scope !== 'all'" :label="scope === 'order' ? '订单号' : '车主账号编号'"
        ><el-input
          v-model="resource"
          :placeholder="scope === 'order' ? '输入完整订单号' : '输入车主账号编号'"
          clearable
          style="width: 270px"
      /></el-form-item>
      <el-form-item label="申请状态"
        ><el-select v-model="query.status" clearable placeholder="全部状态" style="width: 130px"
          ><el-option
            v-for="item in ['PENDING_REVIEW', 'EXECUTING', 'CLOSED', 'REJECTED']"
            :key="item"
            :label="label(item)"
            :value="item" /></el-select
      ></el-form-item>
      <el-form-item label="申请类型"
        ><el-select v-model="query.kind" clearable placeholder="全部类型" style="width: 130px"
          ><el-option label="取消并退款" value="CANCEL_REFUND" /><el-option
            label="仅退款"
            value="REFUND_ONLY" /></el-select
      ></el-form-item>
      <el-form-item label="还车状态"
        ><el-select
          v-model="query.returnState"
          clearable
          placeholder="全部还车状态"
          style="width: 150px"
          ><el-option
            v-for="item in ['UNDETERMINED', 'NOT_REQUIRED', 'WAIT_RETURN', 'RETURNED']"
            :key="item"
            :label="label(item)"
            :value="item" /></el-select
      ></el-form-item>
      <el-form-item label="退款状态"
        ><el-select
          v-model="query.refundState"
          clearable
          placeholder="全部退款状态"
          style="width: 150px"
          ><el-option
            v-for="item in [
              'NONE',
              'NOT_REQUIRED',
              'WAIT_RETURN',
              'READY',
              'PROCESSING',
              'SUCCESS',
              'UNKNOWN',
              'ABNORMAL',
              'CLOSED'
            ]"
            :key="item"
            :label="label(item)"
            :value="item" /></el-select
      ></el-form-item>
      <el-form-item label="操作"
        ><el-button type="primary" native-type="submit" :loading="loading">查询</el-button
        ><el-button @click="resetFilters">重置</el-button></el-form-item
      >
    </el-form>
    <el-alert v-if="error" type="error" :title="error" :closable="false" show-icon />
    <el-table v-loading="loading" :data="rows" row-key="caseId" empty-text="暂无符合条件的售后申请">
      <el-table-column prop="caseId" label="申请编号" min-width="110" />
      <el-table-column prop="orderNo" label="订单号" min-width="285" />
      <el-table-column label="车辆 / 客户" min-width="150"
        ><template #default="{ row }"
          ><div>{{ row.orderPlateNo || '—' }}</div>
          <small>{{ row.ownerLabel || '车主' }}</small></template
        ></el-table-column
      >
      <el-table-column label="申请类型" min-width="120"
        ><template #default="{ row }">{{ label(row.kind) }}</template></el-table-column
      >
      <el-table-column label="申请状态" width="110"
        ><template #default="{ row }"
          ><el-tag
            :type="
              row.caseStatus === 'REJECTED'
                ? 'danger'
                : row.caseStatus === 'CLOSED'
                  ? 'info'
                  : 'warning'
            "
            >{{ label(row.caseStatus) }}</el-tag
          ></template
        ></el-table-column
      >
      <el-table-column label="申请金额" width="130"
        ><template #default="{ row }">{{ money(row.requestedAmount) }}</template></el-table-column
      >
      <el-table-column label="当前保管" width="100"
        ><template #default="{ row }">{{ label(row.custody) }}</template></el-table-column
      >
      <el-table-column label="还车 / 退款" min-width="160"
        ><template #default="{ row }"
          ><div>{{ label(row.returnState) }}</div>
          <small>{{ label(row.refundState) }}</small></template
        ></el-table-column
      >
      <el-table-column label="申请时间" min-width="175"
        ><template #default="{ row }">{{ time(row.createdAt) }}</template></el-table-column
      >
      <el-table-column label="操作" width="95" fixed="right"
        ><template #default="{ row }"
          ><el-button
            v-if="row.allowedActions.includes('VIEW_DETAIL')"
            v-hasPermi="['biz:aftersale:query']"
            link
            type="primary"
            @click="show(row.caseId)"
            >查看处理</el-button
          ></template
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
      title="售后申请与统一审核"
      size="min(1040px, 100vw)"
      :before-close="beforeClose"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="case-detail">
        <el-alert
          v-if="detailError"
          :title="detailError"
          type="error"
          :closable="false"
          show-icon
        />
        <template v-if="detail">
          <section class="detail-heading">
            <div>
              <h2>{{ label(detail.caseStatus) }} · {{ label(detail.kind) }}</h2>
              <p class="number">申请 {{ detail.caseId }} · {{ detail.orderNo }}</p>
            </div>
            <el-button
              :loading="detailLoading"
              :disabled="reviewBusy || noteBusy"
              @click="show(detail.caseId, true)"
              >刷新当前事实</el-button
            >
          </section>
          <el-descriptions :column="2" border class="case-facts">
            <el-descriptions-item label="车辆">{{
              detail.orderPlateNo || '—'
            }}</el-descriptions-item>
            <el-descriptions-item label="客户"
              >{{ detail.ownerLabel || '车主' }}（账号
              {{ detail.ownerAccountId }}）</el-descriptions-item
            >
            <el-descriptions-item label="实际申请主体"
              >{{ label(detail.applicantType) }} #{{ detail.applicantId }}</el-descriptions-item
            >
            <el-descriptions-item label="申请时间">{{
              time(detail.createdAt)
            }}</el-descriptions-item>
            <el-descriptions-item label="当前保管方"
              ><strong>{{ label(detail.custody) }}</strong></el-descriptions-item
            >
            <el-descriptions-item label="当前承接">{{
              detail.assignmentId || '无当前承接'
            }}</el-descriptions-item>
            <el-descriptions-item label="履约状态">{{
              fulfillment(detail.fulfillmentStatus)
            }}</el-descriptions-item>
            <el-descriptions-item label="还车 / 退款"
              >{{ label(detail.returnState) }} /
              {{ label(detail.refundState) }}</el-descriptions-item
            >
            <el-descriptions-item label="申请金额">{{
              money(detail.requestedAmount)
            }}</el-descriptions-item>
            <el-descriptions-item label="批准金额">{{
              detail.approvedAmount === null ? '尚未确定或未授权查看' : money(detail.approvedAmount)
            }}</el-descriptions-item>
          </el-descriptions>
          <section class="funds-grid" aria-label="当前资金事实">
            <div>
              <small>实付</small><strong>{{ money(detail.paidAmount) }}</strong>
            </div>
            <div>
              <small>已成功退款</small><strong>{{ money(detail.refundedAmount) }}</strong>
            </div>
            <div>
              <small>有效退款预留</small><strong>{{ money(detail.reservedAmount) }}</strong>
            </div>
            <div>
              <small>当前可退</small><strong>{{ money(detail.availableAmount) }}</strong>
            </div>
          </section>
          <el-alert
            v-if="detail.availableAmount === null"
            title="资金信息需退款查询权限及收款主体数据权限；未授权金额保持隐藏。"
            type="info"
            :closable="false"
          />
          <section>
            <h3>原申请与凭证</h3>
            <p class="preserve">{{ detail.reason }}</p>
            <VoucherFiles :order-no="detail.orderNo" :file-ids="detail.voucherFileIds" />
          </section>
          <section>
            <h3>公开补充记录</h3>
            <p class="muted">此处内容车主可见，不能用于记录内部审核意见。</p>
            <p v-if="!detail.notes.length" class="muted">暂无补充记录</p>
            <article v-for="item in detail.notes" :key="item.noteId" class="history">
              <small>{{ time(item.createdAt) }}</small>
              <p class="preserve">{{ item.note }}</p>
              <VoucherFiles :order-no="detail.orderNo" :file-ids="item.voucherFileIds" />
            </article>
          </section>
          <section v-if="can('biz:aftersale:note') && detail.allowedActions.includes('ADD_NOTE')">
            <h3>追加说明（车主可见）</h3>
            <el-input
              v-model="note"
              type="textarea"
              :rows="3"
              maxlength="1000"
              show-word-limit
              placeholder="请填写公开补充说明，不要填写内部意见"
              :disabled="noteBusy || noteUnknown || reviewBusy || reviewUnknown"
            />
            <VoucherFiles
              v-if="canUpload"
              v-model="noteFiles"
              :order-no="detail.orderNo"
              editable
              :disabled="noteBusy || noteUnknown || reviewBusy || reviewUnknown"
              @busy="noteUploading = $event"
            />
            <el-alert
              v-if="noteUnknown"
              title="补充结果尚未确认，请刷新原申请或沿用原请求重试，不能更改内容另起一次提交。"
              type="warning"
              :closable="false"
            />
            <el-button
              type="primary"
              :loading="noteBusy"
              :disabled="noteUploading || reviewBusy || reviewUnknown"
              @click="submitNote"
              >{{ noteUnknown ? '重试原补充请求' : '提交公开补充' }}</el-button
            >
          </section>
          <section v-if="detail.caseStatus === 'PENDING_REVIEW'" class="review-section">
            <h3>一次审核</h3>
            <el-alert
              v-if="detail.selfReviewDenied"
              title="当前账号参与了申请或补充资料，或与参与人存在可信员工关联，不能自行审核，请由其他有权人员处理。"
              type="warning"
              :closable="false"
              show-icon
            />
            <el-alert
              v-else-if="!mayReview"
              title="审核需统一售后审核和退款批准权限，并覆盖订单与收款主体；按钮状态以服务端允许动作为准。"
              type="info"
              :closable="false"
            />
            <el-form label-position="top" class="review-form" @submit.prevent>
              <el-form-item label="审核决定"
                ><el-radio-group
                  v-model="reviewForm.decision"
                  :disabled="!mayPreparePreview || reviewLocked || noteBusy || noteUnknown"
                  ><el-radio value="APPROVE">批准</el-radio
                  ><el-radio value="REJECT">驳回</el-radio></el-radio-group
                ></el-form-item
              >
              <el-form-item label="批准退款金额（元）"
                ><el-input
                  v-model="reviewForm.amount"
                  inputmode="decimal"
                  placeholder="最多两位小数"
                  :disabled="!mayPreparePreview || reviewLocked || reviewForm.decision === 'REJECT'"
                  class="amount-input"
                /><span class="hint">{{
                  detail.kind === 'REFUND_ONLY'
                    ? '仅退款的批准金额须大于 0'
                    : '取消并退款可批准 0 元；仍须按实际保管事实处理还车'
                }}</span></el-form-item
              >
              <el-form-item label="内部审核意见（必填，仅后台可见）"
                ><el-input
                  v-model="reviewForm.opinion"
                  type="textarea"
                  :rows="3"
                  maxlength="1000"
                  show-word-limit
                  placeholder="填写内部判断依据，不会作为公开处理说明展示"
                  :disabled="!mayReview || reviewLocked"
              /></el-form-item>
              <el-form-item label="公开处理说明（必填，车主可见）"
                ><el-input
                  v-model="reviewForm.publicOpinion"
                  type="textarea"
                  :rows="3"
                  maxlength="1000"
                  show-word-limit
                  placeholder="请单独填写适合向车主展示的说明"
                  :disabled="!mayReview || reviewLocked"
              /></el-form-item>
            </el-form>
            <el-alert
              v-if="needsRefresh"
              title="订单保管、资金或申请版本已变化。填写的意见已保留，请先刷新当前事实，再重新生成预览。"
              type="warning"
              :closable="false"
              show-icon
            />
            <el-alert
              v-if="reviewUnknown"
              title="审核结果尚未确认。请先刷新原申请核对，或沿用同一请求重试；本页已锁定审核内容。"
              type="warning"
              :closable="false"
              show-icon
            />
            <el-button
              :loading="previewBusy"
              :disabled="
                !mayPreparePreview || needsRefresh || reviewLocked || noteBusy || noteUnknown
              "
              @click="makePreview"
              >生成处理预览</el-button
            >
            <p v-if="!mayPreview" class="muted">预览需售后审核预览、退款查询及相应数据范围权限。</p>
            <div v-if="preview" class="preview-card">
              <h4>本次处理预览</h4>
              <p>
                <strong>{{ decisionText }}</strong>
              </p>
              <p>
                实时保管方：{{ label(preview.custody) }} · 承接 {{ preview.assignmentId || '无' }}
              </p>
              <p>
                实付 {{ money(preview.paidAmount) }} · 已退 {{ money(preview.refundedAmount) }} ·
                其他预留 {{ money(preview.reservedAmount) }} · 可退
                {{ money(preview.availableAmount) }}
              </p>
              <p>
                本次{{
                  reviewForm.decision === 'REJECT' ? '不预留金额' : '批准 ' + money(approvedFen())
                }}
              </p>
              <p v-for="warning in preview.warnings || []" :key="warning" class="warning">
                {{ warning }}
              </p>
            </div>
            <el-button
              type="primary"
              :loading="reviewBusy"
              :disabled="
                !mayReview ||
                noteBusy ||
                noteUnknown ||
                (!reviewUnknown && (!preview || needsRefresh))
              "
              @click="submitReview"
              >{{
                reviewUnknown
                  ? '重试原审核请求'
                  : reviewForm.decision === 'APPROVE'
                    ? '确认批准并保存预留'
                    : '确认驳回申请'
              }}</el-button
            >
          </section>
          <section v-if="detail.reviews.length">
            <h3>审核历史</h3>
            <article v-for="(item, index) in detail.reviews" :key="index" class="history">
              <p>
                <strong>{{ item.decision === 'APPROVE' ? '批准' : '驳回' }}</strong> ·
                {{ time(item.reviewedAt) }} · 审核人 #{{ item.reviewerUserId }}
              </p>
              <p>批准金额：{{ money(item.approvedAmount) }}</p>
              <h4>内部审核意见（仅后台可见）</h4>
              <p class="preserve">{{ item.opinion }}</p>
              <h4>公开处理说明（车主可见）</h4>
              <p class="preserve">{{ item.publicOpinion }}</p>
            </article>
          </section>
          <el-alert
            v-if="detail.refundState === 'UNKNOWN' || detail.refundState === 'ABNORMAL'"
            title="退款结果未知或异常时继续核查原退款号，不能在此更换退款号重新退款。"
            type="warning"
            :closable="false"
          />
          <el-alert
            v-if="detail.returnState === 'WAIT_RETURN'"
            title="车辆尚待实际归还。当前承接与司机占用继续保留，不能用退款成功替代还车。"
            type="warning"
            :closable="false"
          />
          <p v-if="detail.refundNo" class="number muted">原退款编号：{{ detail.refundNo }}</p>
          <el-button
            v-if="detail.refundNo"
            v-hasPermi="['biz:refund:query']"
            @click="openOriginalRefund"
            >查看原退款执行</el-button
          >
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="createOpen"
      title="后台代办售后申请"
      width="min(760px, 96vw)"
      :before-close="closeCreate"
      destroy-on-close
    >
      <el-alert
        title="此申请以当前后台人员为实际操作主体，提交后仍需其他有权人员审核。公开原因、补充与凭证可由原车主查看。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form label-position="top" class="create-form" @submit.prevent>
        <el-form-item label="原订单号"
          ><div class="order-check">
            <el-input
              v-model="apply.orderNo"
              placeholder="输入完整原订单号"
              :disabled="createBusy || createUnknown"
            /><el-button
              :loading="checkingOrder"
              :disabled="!can('biz:order:query') || createBusy || createUnknown"
              @click="checkOrder"
              >核对订单</el-button
            >
          </div></el-form-item
        >
        <p v-if="!can('biz:order:query')" class="warning">
          代办申请需订单查询权限，先核对当前订单及版本；不能手工填写版本号。
        </p>
        <template v-if="checkedOrder">
          <el-descriptions :column="1" border
            ><el-descriptions-item label="车辆"
              >{{ checkedOrder.vehicle.plateNo }} ·
              {{ checkedOrder.station.name }}</el-descriptions-item
            ><el-descriptions-item label="状态"
              >{{ checkedOrder.displayStatus }} · 当前保管
              {{ label(checkedOrder.custody) }}</el-descriptions-item
            ><el-descriptions-item label="客户"
              >{{ checkedOrder.ownerContact.displayName }}
              {{ checkedOrder.ownerContact.phoneMask }}</el-descriptions-item
            ></el-descriptions
          >
          <el-form-item label="申请类型"
            ><el-radio-group v-model="apply.kind" :disabled="createBusy || createUnknown"
              ><el-radio value="CANCEL_REFUND">取消并退款</el-radio
              ><el-radio value="REFUND_ONLY">仅退款</el-radio></el-radio-group
            ></el-form-item
          >
          <el-form-item label="申请退款金额（元）"
            ><el-input
              v-model="apply.amount"
              inputmode="decimal"
              placeholder="最多两位小数"
              :disabled="createBusy || createUnknown"
          /></el-form-item>
          <el-form-item label="公开申请原因（车主可见）"
            ><el-input
              v-model="apply.reason"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              :disabled="createBusy || createUnknown"
              placeholder="如实记录客户申请原因，不填写内部私密意见"
          /></el-form-item>
          <VoucherFiles
            v-if="canUpload"
            v-model="applyFiles"
            :order-no="checkedOrder.orderNo"
            editable
            :disabled="createBusy || createUnknown"
            @busy="createUploading = $event"
          />
        </template>
      </el-form>
      <el-alert
        v-if="createUnknown"
        title="提交结果尚未确认，已保留原请求。请沿用同一请求重试，系统会核对原申请结果。"
        type="warning"
        :closable="false"
      />
      <el-alert v-if="createError" :title="createError" type="error" :closable="false" />
      <template #footer
        ><el-button
          :disabled="createBusy || createUnknown || createUploading"
          @click="createOpen = false"
          >返回</el-button
        ><el-button
          type="primary"
          :loading="createBusy"
          :disabled="!checkedOrder || createUploading"
          @click="submitCreate"
          >{{ createUnknown ? '重试原申请请求' : '提交代办申请' }}</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>
<style scoped>
.page-heading,
.detail-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.page-heading p,
.muted,
.history small {
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
.case-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 180px;
}
.case-detail section {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 20px;
}
.case-detail h2,
.case-detail h3 {
  margin: 0 0 12px;
}
.number,
.preserve {
  overflow-wrap: anywhere;
}
.preserve {
  white-space: pre-wrap;
  line-height: 1.8;
}
.funds-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.funds-grid div {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.funds-grid small {
  color: #64748b;
}
.funds-grid strong {
  font-size: 19px;
}
.history {
  margin-top: 14px;
  padding: 14px 18px;
  border-left: 3px solid #dbeafe;
  background: #f8fafc;
}
.review-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}
.review-form,
.review-form .el-form-item,
.create-form {
  width: 100%;
}
.amount-input {
  max-width: 220px;
}
.hint {
  margin-left: 12px;
  font-size: 13px;
  color: #64748b;
}
.preview-card {
  width: 100%;
  box-sizing: border-box;
  padding: 18px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  line-height: 1.8;
}
.preview-card h4 {
  margin-top: 0;
}
.warning {
  color: #9a6700;
  line-height: 1.7;
}
.order-check {
  display: flex;
  gap: 12px;
  width: 100%;
}
.create-form {
  margin-top: 20px;
}
.case-detail section > .el-button {
  margin-top: 12px;
}
@media (max-width: 700px) {
  .page-heading,
  .detail-heading {
    flex-direction: column;
    align-items: flex-start;
  }
  .funds-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .hint {
    display: block;
    margin-left: 0;
  }
  .order-check {
    flex-direction: column;
  }
}
</style>
