<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import { getToken } from '@/utils/auth';
import { checkPermi } from '@/utils/permission';
import { intentKey } from '@/api/rongli/aftersale';
import * as api from '@/api/rongli/dispatch';
const route = useRoute();
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: typeof route.query.orderNo === 'string' ? route.query.orderNo : '',
  driverId: '',
  status: ''
});
const receipt = ref<api.DispatchInvite | null>(null);
const rows = ref<api.DispatchInvite[]>([]),
  total = ref(0),
  loading = ref(false),
  error = ref('');
const selected = ref<api.DispatchInvite | null>(null),
  open = ref(false),
  reason = ref(''),
  busy = ref(false),
  stale = ref(false),
  detailError = ref('');
type Intent = {
  id: string;
  orderNo: string;
  key: string;
  body: api.RevokeInvite;
};
const pending = reactive(new Map<string, Intent>());
const unknown = computed(() => Boolean(selected.value && pending.has(selected.value.inviteId)));
const permitted = (action: string) => checkPermi(['biz:dispatch:' + action]);
const locked = computed(() => busy.value || loading.value || stale.value || unknown.value);
const canRevoke = computed(
  () =>
    permitted('list') &&
    permitted('revoke') &&
    selected.value?.status === 'WAIT_RESPONSE' &&
    selected.value.allowedActions.includes('REVOKE_INVITE')
);
const login = getToken();
let disposed = false,
  sequence = 0,
  targetId = '';
const guard = api.expiryGuard(() => {
  rows.value = [];
  total.value = 0;
  selected.value = null;
  stale.value = true;
  detailError.value = '原邀约显示截止已到，请重读。不会凭旧等待状态撤回已变化的邀约。';
});
function current() {
  if (disposed) return false;
  if (getToken() === login) return true;
  sequence++;
  pending.clear();
  receipt.value = null;
  rows.value = [];
  selected.value = null;
  guard.clear();
  error.value = '登录已变化，请重新进入邀约记录';
  return false;
}
function arm() {
  guard.arm(
    rows.value
      .filter((row: api.DispatchInvite) => row.status === 'WAIT_RESPONSE')
      .map((row: api.DispatchInvite) => row.expiresAt)
  );
}
async function load(preserve = false) {
  if (!permitted('list') || busy.value) return;
  const id = targetId,
    originalReason = reason.value;
  const run = ++sequence;
  guard.clear();
  rows.value = [];
  total.value = 0;
  selected.value = null;
  error.value = '';
  loading.value = true;
  try {
    const value = await api.invites(
      Object.fromEntries(Object.entries(query).filter(([, value]) => value !== ''))
    );
    if (!current() || run !== sequence) return;
    rows.value = value.rows;
    total.value = value.total;
    if (id) {
      selected.value = value.rows.find((row) => row.inviteId === id) || null;
      if (!selected.value) {
        detailError.value = '当前查询未返回原邀约，请按原订单和邀约重新核对';
        stale.value = true;
      } else {
        stale.value = false;
        if (preserve) reason.value = originalReason;
      }
    }
    arm();
  } catch (failure) {
    if (!current() || run !== sequence) return;
    error.value = api.errorMessage(failure);
    if ([401, 403, 404].includes(api.errorStatus(failure))) {
      pending.clear();
      reason.value = '';
    }
  } finally {
    if (current() && run === sequence) loading.value = false;
  }
}
function choose(row: api.DispatchInvite) {
  targetId = row.inviteId;
  selected.value = row;
  reason.value = pending.get(row.inviteId)?.body.reason || '';
  stale.value = false;
  detailError.value = '';
  open.value = true;
}
function close() {
  targetId = '';
  selected.value = null;
  reason.value = '';
  detailError.value = '';
  stale.value = false;
}
async function execute(intent: Intent) {
  if (busy.value || !current()) return;
  busy.value = true;
  detailError.value = '';
  pending.set(intent.id, intent);
  guard.clear();
  try {
    const value = await api.revokeInvite(intent.id, intent.body, intent.key);
    if (!current()) return;
    pending.delete(intent.id);
    receipt.value = value;
    rows.value = rows.value.map((row: api.DispatchInvite) =>
      row.inviteId === intent.id ? value : row
    );
    if (targetId === intent.id) {
      selected.value = value;
      reason.value = '';
      stale.value = false;
    }
    arm();
  } catch (failure) {
    if (!current()) return;
    const status = api.errorStatus(failure);
    if (status > 0 && status < 500) pending.delete(intent.id);
    receipt.value = null;
    rows.value = [];
    total.value = 0;
    selected.value = null;
    stale.value = true;
    detailError.value = api.errorMessage(failure);
    if ([401, 403, 404].includes(status)) reason.value = '';
  } finally {
    if (!disposed) busy.value = false;
  }
}
function send() {
  if (locked.value || !canRevoke.value || !selected.value) return;
  if (!reason.value.trim()) {
    detailError.value = '请填写撤回原因（1至500字）';
    return;
  }
  void execute({
    id: selected.value.inviteId,
    orderNo: selected.value.orderNo,
    key: intentKey(),
    body: {
      expectedVersion: selected.value.version,
      expectedOrderVersion: selected.value.orderVersion,
      reason: reason.value.trim()
    }
  });
}
function pendingValues(): Intent[] {
  return Array.from(pending.values());
}
function retry(intent: Intent) {
  void execute(intent);
}
onMounted(() => void load());
onBeforeUnmount(() => {
  disposed = true;
  sequence++;
  guard.clear();
  pending.clear();
});
</script>
<template>
  <div class="app-container dispatch-invites-page">
    <h2>派单邀约记录</h2>
    <p class="intro">
      记录自动与人工邀约的本人响应。撤回只释放本次等待邀约占用并转人工等待，不取消订单、不释放已接受承接。
    </p>
    <p v-if="!permitted('list')" class="error">没有派单查询权限。</p>
    <el-form v-else inline class="filters" @submit.prevent
      ><el-form-item label="订单编号"
        ><el-input v-model="query.orderNo" placeholder="精确订单编号" clearable /></el-form-item
      ><el-form-item label="代驾编号"
        ><el-input v-model="query.driverId" placeholder="代驾档案编号" clearable /></el-form-item
      ><el-form-item label="邀约状态"
        ><el-select v-model="query.status" placeholder="全部邀约状态" clearable
          ><el-option
            v-for="value in ['WAIT_RESPONSE', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'REVOKED']"
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
          >查询邀约</el-button
        ></el-form-item
      ></el-form
    >
    <el-alert
      v-for="intent in pendingValues()"
      :key="intent.id"
      type="warning"
      :closable="false"
      :title="`原邀约 ${intent.id} 的撤回结果未知，仅按原版本、原因与UUID核对。`"
      ><el-button :disabled="busy || loading" @click="retry(intent)"
        >核对并重试原撤回</el-button
      ></el-alert
    >
    <el-alert
      v-if="receipt"
      type="success"
      :closable="false"
      :title="`原邀约 ${receipt.inviteId} ${api.dispatchLabel(receipt.status)}，原订单 ${receipt.orderNo}；请重新查询当前记录。`"
    />
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <el-table v-loading="loading" :data="rows" row-key="inviteId"
      ><el-table-column prop="inviteId" label="邀约编号" min-width="120" /><el-table-column
        prop="orderNo"
        label="订单编号"
        min-width="190"
      /><el-table-column label="代驾昵称" min-width="140"
        ><template #default="{ row }"
          >{{ row.driverName || '昵称未提供' }}
          <div class="muted">档案 {{ row.driverId }}</div></template
        ></el-table-column
      ><el-table-column label="服务公司 / 站点" min-width="170"
        ><template #default="{ row }"
          >{{ row.serviceCompanyName || '未配置' }}
          <div>{{ row.stationName }}</div></template
        ></el-table-column
      ><el-table-column label="状态 / 来源" min-width="145"
        ><template #default="{ row }"
          >{{ api.dispatchLabel(row.status) }}
          <div class="muted">{{ api.dispatchLabel(row.source) }}</div></template
        ></el-table-column
      ><el-table-column label="响应截止（北京时间）" min-width="200"
        ><template #default="{ row }">{{
          api.dispatchTime(row.expiresAt)
        }}</template></el-table-column
      ><el-table-column label="操作" min-width="120"
        ><template #default="{ row }"
          ><el-button link type="primary" @click="choose(row)">查看邀约</el-button></template
        ></el-table-column
      ></el-table
    >
    <el-pagination
      v-if="total"
      v-model:current-page="query.pageNum"
      :total="total"
      :page-size="10"
      layout="prev,pager,next,total"
      @current-change="load"
    />
    <el-drawer
      v-model="open"
      title="邀约详情与撤回"
      size="min(700px,100vw)"
      :before-close="
        (done: () => void) => {
          if (!busy) {
            close();
            done();
          }
        }
      "
    >
      <template v-if="selected"
        ><el-descriptions :column="1" border
          ><el-descriptions-item label="邀约 / 订单"
            >{{ selected.inviteId }} / {{ selected.orderNo }}</el-descriptions-item
          ><el-descriptions-item label="状态"
            >{{ api.dispatchLabel(selected.status) }} ·
            {{ api.dispatchLabel(selected.source) }}</el-descriptions-item
          ><el-descriptions-item label="代驾昵称"
            >{{ selected.driverName || '昵称未提供' }}（档案
            {{ selected.driverId }}）</el-descriptions-item
          ><el-descriptions-item label="冻结服务公司">{{
            selected.serviceCompanyName || '未配置'
          }}</el-descriptions-item
          ><el-descriptions-item label="当前站点">{{ selected.stationName }}</el-descriptions-item
          ><el-descriptions-item label="接车范围">{{ selected.pickupArea }}</el-descriptions-item
          ><el-descriptions-item label="响应截止">{{
            api.dispatchTime(selected.expiresAt)
          }}</el-descriptions-item
          ><el-descriptions-item label="批次总截止">{{
            api.dispatchTime(selected.overallDeadlineAt)
          }}</el-descriptions-item
          ><el-descriptions-item label="直线距离"
            >{{ api.distance(selected.distanceMeter) }}（不代表导航路线）</el-descriptions-item
          ><el-descriptions-item label="承接记录">{{
            selected.assignmentId || '尚未建立'
          }}</el-descriptions-item
          ><el-descriptions-item label="邀约 / 订单版本"
            >{{ selected.version }} / {{ selected.orderVersion }}</el-descriptions-item
          ></el-descriptions
        >
        <template v-if="canRevoke"
          ><el-input
            v-model="reason"
            :disabled="locked"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="撤回原因（必填，最多500字）"
          /><el-button type="danger" :disabled="locked" @click="send"
            >撤回本次等待邀约</el-button
          ></template
        >
        <p v-else class="intro">
          当前状态或权限不允许撤回；已接受承接须按实际服务责任继续处理。
        </p></template
      >
      <p v-if="detailError" class="error" role="alert">{{ detailError }}</p>
      <p v-if="stale" class="warning">
        旧有效状态已撤回，原因仍保留；重新读取后由管理员明确核对新状态。
      </p>
      <el-button :disabled="busy || loading" @click="load(true)">重读记录并保留填写</el-button>
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
.error {
  color: #b42318;
  line-height: 1.8;
}
.warning {
  padding: 12px;
  background: #fff6e8;
  color: #87520c;
  line-height: 1.8;
}
:deep(.el-alert) {
  margin: 12px 0;
}
:deep(.el-pagination) {
  margin-top: 18px;
  max-width: 100%;
  overflow: auto;
}
:deep(.el-textarea) {
  margin: 18px 0;
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
