<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { intentKey } from '@/api/rongli/aftersale';
import * as api from '@/api/rongli/dispatch';
const router = useRouter();
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  stationId: '',
  serviceCompanyId: '',
  status: ''
});
const rows = ref<api.DispatchQueueItem[]>([]),
  total = ref(0),
  loading = ref(false),
  error = ref('');
const selected = ref<api.DispatchQueueItem | null>(null),
  open = ref(false),
  candidateLoading = ref(false),
  candidateError = ref('');
const candidates = ref<api.DispatchCandidate[]>([]),
  candidateTotal = ref(0),
  pageNum = ref(1),
  sort = ref('cursor');
const driverId = ref(''),
  reason = ref(''),
  busy = ref(false),
  stale = ref(false),
  receipt = ref<api.DispatchInvite | null>(null);
type Intent = { orderNo: string; body: api.ManualInvite; key: string };
const pending = reactive(new Map<string, Intent>());
const unknown = computed(() => (selected.value ? pending.has(selected.value.orderNo) : false));
const locked = computed(() => busy.value || candidateLoading.value || stale.value || unknown.value);
const permitted = (value: string) => checkPermi(['biz:dispatch:' + value]);
const canCreate = computed(
  () =>
    permitted('create') &&
    permitted('list') &&
    selected.value?.allowedActions.includes('CREATE_MANUAL_INVITE')
);
const login = getToken();
let disposed = false,
  sequence = 0,
  candidateSequence = 0;
const guard = api.expiryGuard(() => {
  candidates.value = [];
  driverId.value = '';
  stale.value = true;
  candidateError.value = '候选有效期已到，请重新核对；不沿用旧位置或接受资格。';
});
function sessionCurrent() {
  if (disposed) return false;
  if (getToken() === login) return true;
  pending.clear();
  rows.value = [];
  selected.value = null;
  candidates.value = [];
  guard.clear();
  sequence++;
  candidateSequence++;
  error.value = '登录已变化，请重新进入派单队列';
  return false;
}
function active(order: string, run: number) {
  return sessionCurrent() && selected.value?.orderNo === order && run === candidateSequence;
}
async function load() {
  if (!permitted('list') || busy.value) return;
  const run = ++sequence;
  rows.value = [];
  total.value = 0;
  error.value = '';
  loading.value = true;
  try {
    const value = await api.queue(
      Object.fromEntries(Object.entries(query).filter(([, value]) => value !== ''))
    );
    if (sessionCurrent() && run === sequence) {
      rows.value = value.rows;
      total.value = value.total;
    }
  } catch (failure) {
    if (sessionCurrent() && run === sequence) error.value = api.errorMessage(failure);
  } finally {
    if (sessionCurrent() && run === sequence) loading.value = false;
  }
}
function close() {
  candidateSequence++;
  guard.clear();
  selected.value = null;
  candidates.value = [];
  driverId.value = '';
  reason.value = '';
  receipt.value = null;
  stale.value = false;
  candidateError.value = '';
  candidateLoading.value = false;
}
async function choose(row: api.DispatchQueueItem) {
  close();
  selected.value = row;
  open.value = true;
  pageNum.value = 1;
  const intent = pending.get(row.orderNo);
  if (intent) {
    reason.value = intent.body.reason;
    driverId.value = intent.body.driverId;
  }
  await loadCandidates();
}
async function loadCandidates() {
  const order = selected.value?.orderNo;
  if (!order || !permitted('list') || busy.value) return;
  const run = ++candidateSequence;
  guard.clear();
  candidates.value = [];
  candidateTotal.value = 0;
  candidateLoading.value = true;
  candidateError.value = '';
  try {
    // 订单版本必须重新取真实队列，不能仅刷新司机版本后沿用旧订单CAS。
    const orderResult = await api.queue({
      orderNo: order,
      pageNum: 1,
      pageSize: 10
    });
    if (!active(order, run)) return;
    const item = orderResult.rows.find((row) => row.orderNo === order);
    if (!item || !item.allowedActions.includes('VIEW_DISPATCH_CANDIDATES'))
      throw new Error('该订单当前不可查看候选，请关闭后重新查询');
    selected.value = item;
    const value = await api.candidates({
      orderNo: order,
      pageNum: pageNum.value,
      pageSize: 10,
      sort: sort.value
    });
    if (!active(order, run)) return;
    if (
      value.rows.some(
        (row) => row.serviceCompanyId !== item.serviceCompanyId || !row.eligibilityExpiresAt
      )
    )
      throw new Error('候选归属或截止未确认');
    candidates.value = value.rows;
    candidateTotal.value = value.total;
    stale.value = false;
    if (!pending.has(order)) driverId.value = '';
    guard.arm(value.rows.map((row) => row.eligibilityExpiresAt));
  } catch (failure) {
    if (active(order, run)) {
      candidates.value = [];
      driverId.value = '';
      candidateError.value = api.errorMessage(failure);
      stale.value = true;
      if ([401, 403, 404].includes(api.errorStatus(failure))) {
        pending.delete(order);
        selected.value = null;
        reason.value = '';
      }
    }
  } finally {
    if (active(order, run)) candidateLoading.value = false;
  }
}
async function execute(intent: Intent) {
  if (busy.value || !sessionCurrent()) return;
  const run = candidateSequence;
  busy.value = true;
  candidateError.value = '';
  pending.set(intent.orderNo, intent);
  guard.clear();
  try {
    const value = await api.manualInvite(intent.body, intent.key);
    pending.delete(intent.orderNo);
    if (!active(intent.orderNo, run)) return;
    candidates.value = [];
    driverId.value = '';
    reason.value = '';
    receipt.value = value;
    stale.value = true;
  } catch (failure) {
    if (!active(intent.orderNo, run)) return;
    const status = api.errorStatus(failure);
    if (status > 0 && status < 500) pending.delete(intent.orderNo);
    candidates.value = [];
    driverId.value = '';
    stale.value = true;
    candidateError.value = api.errorMessage(failure);
    if ([401, 403, 404].includes(status)) {
      selected.value = null;
      reason.value = '';
    }
  } finally {
    if (!disposed) busy.value = false;
  }
}
function send() {
  if (locked.value || !canCreate.value || !selected.value) return;
  const driver = candidates.value.find(
    (row: api.DispatchCandidate) => row.driverId === driverId.value
  );
  if (!driver || !driver.allowedActions.includes('CREATE_MANUAL_INVITE')) {
    candidateError.value = '请选择当前有效候选代驾';
    return;
  }
  if (Date.parse(driver.eligibilityExpiresAt) <= Date.now()) {
    guard.arm([driver.eligibilityExpiresAt]);
    return;
  }
  if (!reason.value.trim()) {
    candidateError.value = '请填写人工邀约原因（1至500字）';
    return;
  }
  void execute({
    orderNo: selected.value.orderNo,
    key: intentKey(),
    body: {
      orderNo: selected.value.orderNo,
      driverId: driver.driverId,
      expectedOrderVersion: selected.value.orderVersion,
      expectedDriverVersion: driver.driverVersion,
      reason: reason.value.trim()
    }
  });
}
function retry() {
  const intent = selected.value && pending.get(selected.value.orderNo);
  if (intent) void execute(intent);
}
function history(orderNo: string) {
  void router.push({ path: '/dispatch/invites', query: { orderNo } });
}
function pendingValues(): Intent[] {
  return Array.from(pending.values());
}
onMounted(() => void load());
onBeforeUnmount(() => {
  disposed = true;
  sequence++;
  close();
  pending.clear();
});
</script>
<template>
  <div class="app-container dispatch-queue-page">
    <h2>派单队列</h2>
    <p class="intro">
      按订单冻结的服务公司选择代驾池。人工邀约仍需本人接受，不直接建立承接；站点迁移不改变旧订单服务公司。
    </p>
    <el-alert
      title="候选距离为 GCJ02 直线距离，仅用于半径过滤，不是导航路线或预计到达时间。自动轮转按公司游标，不按距离排名。"
      type="info"
      :closable="false"
    />
    <p v-if="!permitted('list')" class="error">没有派单查询权限。</p>
    <el-form v-else inline class="filters" @submit.prevent
      ><el-form-item label="订单编号"
        ><el-input v-model="query.orderNo" placeholder="精确订单编号" clearable /></el-form-item
      ><el-form-item label="站点编号"
        ><el-input
          v-model="query.stationId"
          placeholder="站点编号（可选）"
          clearable /></el-form-item
      ><el-form-item label="服务公司编号"
        ><el-input
          v-model="query.serviceCompanyId"
          placeholder="冻结服务公司编号（可选）"
          clearable /></el-form-item
      ><el-form-item label="派单状态"
        ><el-select v-model="query.status" clearable placeholder="全部派单状态"
          ><el-option
            v-for="value in ['SCHEDULED', 'PENDING_DISPATCH', 'DISPATCHING', 'MANUAL_DISPATCH']"
            :key="value"
            :value="value"
            :label="api.dispatchLabel(value)" /></el-select></el-form-item
      ><el-form-item
        ><el-button
          type="primary"
          :disabled="loading || busy"
          @click="
            query.pageNum = 1;
            load();
          "
          >查询队列</el-button
        ></el-form-item
      ></el-form
    >
    <el-alert
      v-for="intent in pendingValues()"
      :key="intent.orderNo"
      type="warning"
      :closable="false"
      :title="`订单 ${intent.orderNo} 的原人工邀约结果未知，保持原目标、正文及请求键。`"
      ><el-button
        :disabled="busy"
        @click="choose({ orderNo: intent.orderNo } as api.DispatchQueueItem)"
        >返回原订单核对</el-button
      ></el-alert
    >
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <el-table v-loading="loading" :data="rows" row-key="orderNo"
      ><el-table-column prop="orderNo" label="订单编号" min-width="190" /><el-table-column
        prop="serviceCompanyName"
        label="冻结服务公司"
        min-width="140"
      /><el-table-column prop="stationName" label="当前站点" min-width="130" /><el-table-column
        label="派单状态"
        min-width="145"
        ><template #default="{ row }"
          >{{ api.dispatchLabel(row.fulfillmentStatus) }}
          <div class="muted">
            {{ api.dispatchLabel(row.batchStatus) }}
          </div></template
        ></el-table-column
      ><el-table-column label="上门时段" min-width="220"
        ><template #default="{ row }"
          >{{ api.dispatchTime(row.pickupSlotStartAt) }}
          <div>{{ api.dispatchTime(row.pickupSlotEndAt) }}</div></template
        ></el-table-column
      ><el-table-column label="批次截止 / 原因" min-width="190"
        ><template #default="{ row }"
          >{{ api.dispatchTime(row.overallDeadlineAt) }}
          <div class="muted">{{ row.reasonCode || '—' }}</div></template
        ></el-table-column
      ><el-table-column label="操作" min-width="200"
        ><template #default="{ row }"
          ><el-button
            v-if="permitted('list') && row.allowedActions.includes('VIEW_DISPATCH_CANDIDATES')"
            link
            type="primary"
            @click="choose(row)"
            >候选与人工邀约</el-button
          ><el-button
            v-if="row.allowedActions.includes('VIEW_DISPATCH_INVITES')"
            link
            @click="history(row.orderNo)"
            >邀约记录</el-button
          ></template
        ></el-table-column
      ></el-table
    >
    <el-pagination
      v-if="total"
      v-model:current-page="query.pageNum"
      :page-size="10"
      :total="total"
      layout="prev,pager,next,total"
      @current-change="load"
    />
    <el-drawer
      v-model="open"
      title="订单候选与人工邀约"
      size="min(950px, 100vw)"
      :before-close="
        (done: () => void) => {
          if (!busy) {
            close();
            done();
          }
        }
      "
    >
      <p v-if="selected">
        订单 {{ selected.orderNo }} · 冻结公司
        {{ selected.serviceCompanyName || selected.serviceCompanyId || '未确认' }}
        · 版本 {{ selected.orderVersion }}
      </p>
      <p class="intro">
        仅当前订单同公司且满足资格、听单、近期位置、可达和占用条件的代驾。信息为上次核对，最早有效期到达后需重读。
      </p>
      <div class="actions">
        <el-select
          v-model="sort"
          :disabled="locked"
          @change="
            pageNum = 1;
            loadCandidates();
          "
          ><el-option value="cursor" label="公司轮转顺序" /><el-option
            value="distanceAsc"
            label="直线距离升序（仅展示）" /></el-select
        ><el-button :disabled="busy || candidateLoading" @click="loadCandidates"
          >重新核对订单与候选</el-button
        ><el-button v-if="selected" @click="history(selected.orderNo)"
          >查看原订单邀约记录</el-button
        >
      </div>
      <p v-if="candidateError" class="error" role="alert">
        {{ candidateError }}
      </p>
      <el-alert
        v-if="unknown"
        title="原人工邀约结果尚未确认。重复核对仍使用同一订单、代驾、两个版本、正文与UUID，不生成第二份邀约。"
        type="warning"
        :closable="false"
        ><el-button :disabled="busy || candidateLoading" @click="retry"
          >核对并重试原人工邀约</el-button
        ></el-alert
      >
      <el-alert
        v-if="receipt"
        :title="`已发出邀约 ${receipt.inviteId}：${api.dispatchLabel(receipt.status)}。司机明确接受后才建立承接。`"
        type="success"
        :closable="false"
      />
      <el-table v-loading="candidateLoading" :data="candidates" row-key="driverId"
        ><el-table-column label="选择" width="65"
          ><template #default="{ row }"
            ><el-radio
              v-model="driverId"
              :value="row.driverId"
              :disabled="
                locked || !canCreate || !row.allowedActions.includes('CREATE_MANUAL_INVITE')
              "
              ><span class="sr-only">选择代驾 {{ row.driverId }}</span></el-radio
            ></template
          ></el-table-column
        ><el-table-column label="代驾昵称" min-width="140"
          ><template #default="{ row }"
            >{{ row.driverName || '昵称未提供' }}
            <div class="muted">
              {{ row.phoneMask || '号码摘要未提供' }}
            </div></template
          ></el-table-column
        ><el-table-column prop="licenseClass" label="准驾类别" width="95" /><el-table-column
          label="直线距离"
          min-width="110"
          ><template #default="{ row }">{{
            api.distance(row.distanceMeter)
          }}</template></el-table-column
        ><el-table-column label="候选最早有效期" min-width="200"
          ><template #default="{ row }">{{
            api.dispatchTime(row.eligibilityExpiresAt)
          }}</template></el-table-column
        ></el-table
      >
      <el-pagination
        v-if="candidateTotal"
        v-model:current-page="pageNum"
        :page-size="10"
        :total="candidateTotal"
        layout="prev,pager,next,total"
        @current-change="loadCandidates"
      />
      <el-input
        v-model="reason"
        :disabled="locked || !canCreate"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="人工邀约原因（必填，最多500字）"
      />
      <el-button v-if="canCreate" type="primary" :disabled="locked || !driverId" @click="send"
        >向所选代驾发出邀约</el-button
      >
      <p class="warning">
        发出人工邀约不会推进自动公平游标，也不会绕过半径、资格或占用校验。服务端再次校验两份版本和实际权限。
      </p>
    </el-drawer>
  </div>
</template>
<style scoped>
h2 {
  margin: 0 0 12px;
}
.intro,
.muted {
  color: #606266;
  line-height: 1.8;
}
.muted {
  font-size: 12px;
}
.filters {
  margin-top: 20px;
}
.filters :deep(.el-select) {
  width: 190px;
}
.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 18px 0;
}
.actions :deep(.el-select) {
  width: 240px;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
.warning {
  background: #fff6e8;
  color: #87520c;
  padding: 12px;
  line-height: 1.8;
}
.sr-only {
  position: absolute;
  clip: rect(0, 0, 0, 0);
  width: 1px;
  height: 1px;
  overflow: hidden;
}
:deep(.el-pagination) {
  margin: 18px 0;
  max-width: 100%;
  overflow: auto;
}
:deep(.el-textarea) {
  margin: 18px 0;
}
:deep(.el-alert) {
  margin: 12px 0;
}
@media (max-width: 700px) {
  .filters :deep(.el-form-item) {
    display: block;
    margin-right: 0;
  }
  .filters :deep(.el-form-item__content),
  .filters :deep(.el-select) {
    width: 100%;
  }
}
</style>
