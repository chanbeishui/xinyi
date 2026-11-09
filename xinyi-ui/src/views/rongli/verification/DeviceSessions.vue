<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { intentKey } from '@/api/rongli/aftersale';
import * as api from '@/api/rongli/verification';
type Intent = { id: string; body: api.CloseDeviceSession; key: string };
const filter = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 10 });
const rows = ref<api.DeviceSessionRecord[]>([]),
  total = ref(0),
  selected = ref<api.DeviceSessionRecord | null>(null);
const loading = ref(false),
  busy = ref(false),
  open = ref(false),
  stale = ref(false),
  reason = ref(''),
  error = ref('');
// 封存未知结果冻结原目标和版本；切换详情不能将原封存请求写入另一会话。
const pending = ref<Intent | null>(null);
const permission = (action: string) => checkPermi(['biz:verification:' + action]);
const locked = computed(() => loading.value || busy.value || Boolean(pending.value));
const canClose = computed(
  () => permission('close') && selected.value?.allowedActions.includes('CLOSE_DEVICE_AUTH')
);
const token = getToken();
let disposed = false,
  sequence = 0;
const status = (e: unknown) => Number((e as { httpStatus?: number })?.httpStatus || 0);
const message = (e: unknown) =>
  (e as { msg?: string; message?: string })?.msg ||
  (e as { message?: string })?.message ||
  '会话操作未完成';
function current() {
  if (disposed) return false;
  if (getToken() === token) return true;
  sequence++;
  rows.value = [];
  selected.value = null;
  pending.value = null;
  reason.value = '';
  open.value = false;
  error.value = '登录已变化，请重新进入本机认证管理';
  return false;
}
async function list() {
  if (!permission('list') || !current()) return;
  const run = ++sequence;
  loading.value = true;
  error.value = '';
  try {
    const result = await api.listDeviceSessions(
      Object.fromEntries(Object.entries(filter).filter(([, v]) => v !== ''))
    );
    if (!current() || run !== sequence) return;
    if (result.code !== 200) throw new Error('会话列表响应尚未确认');
    rows.value = result.rows;
    total.value = result.total;
  } catch (e) {
    if (current() && run === sequence) {
      error.value = message(e);
      if ([401, 403, 404].includes(status(e))) {
        rows.value = [];
        selected.value = null;
        open.value = false;
      }
    }
  } finally {
    if (current() && run === sequence) loading.value = false;
  }
}
async function detail(id: string, preserve = false) {
  if (!permission('query') || !current() || busy.value) return;
  const run = ++sequence,
    previousReason = reason.value;
  selected.value = null;
  open.value = true;
  loading.value = true;
  error.value = '';
  if (!preserve) reason.value = '';
  try {
    const value = await api.getDeviceSession(id);
    if (!current() || run !== sequence) return;
    if (value.verificationSessionId !== id) throw new Error('会话目标尚未确认');
    selected.value = value;
    stale.value = false;
    if (preserve) reason.value = previousReason;
  } catch (e) {
    if (current() && run === sequence) {
      error.value = message(e);
      if ([401, 403, 404].includes(status(e))) {
        selected.value = null;
        reason.value = '';
        open.value = false;
      }
    }
  } finally {
    if (current() && run === sequence) loading.value = false;
  }
}
async function execute(intent: Intent) {
  if (!current() || busy.value) return;
  busy.value = true;
  pending.value = intent;
  error.value = '';
  try {
    const result = await api.closeDeviceSession(intent.id, intent.body, intent.key);
    if (!current()) return;
    pending.value = null;
    selected.value = result;
    open.value = true;
    stale.value = false;
    reason.value = '';
    ElMessage.success('原设备认证会话已封存，未生成认证通过证明');
    await list();
  } catch (e) {
    if (!current()) return;
    error.value = message(e);
    if (status(e) > 0 && status(e) < 500) pending.value = null;
    stale.value = status(e) === 409;
    if ([401, 403, 404].includes(status(e))) {
      selected.value = null;
      reason.value = '';
      open.value = false;
    }
  } finally {
    if (current()) busy.value = false;
  }
}
function closeSession() {
  const value = selected.value;
  if (!value || locked.value || stale.value || !canClose.value) return;
  if (!reason.value.trim()) {
    error.value = '请填写封存原因';
    return;
  }
  execute({
    id: value.verificationSessionId,
    body: { expectedVersion: value.version, reason: reason.value.trim() },
    key: intentKey()
  });
}
onMounted(list);
onBeforeUnmount(() => {
  disposed = true;
  sequence++;
  selected.value = null;
  pending.value = null;
  reason.value = '';
});
</script>
<template>
  <section class="device-sessions">
    <el-alert
      type="info"
      :closable="false"
      title="查询仅读取服务端会话事实。封存用于结束本地未决挑战，不等于认证通过，也不修改人工核验或最终资格。"
    />
    <div class="toolbar">
      <el-input
        v-model="filter.keyword"
        maxlength="100"
        clearable
        placeholder="业务请求号或完整申请编号"
        style="max-width: 300px"
      /><el-select v-model="filter.status" clearable placeholder="认证状态" style="width: 170px"
        ><el-option
          v-for="value in [
            'CREATED',
            'PROCESSING',
            'UNKNOWN',
            'PASSED',
            'FAILED',
            'EXPIRED',
            'CLOSED'
          ]"
          :key="value"
          :value="value"
          :label="api.verificationLabel(value)" /></el-select
      ><el-button
        :disabled="locked || !permission('list')"
        @click="
          filter.pageNum = 1;
          list();
        "
        >查询认证会话</el-button
      >
    </div>
    <p v-if="!permission('list')" class="muted">当前没有设备认证会话列表权限。</p>
    <el-table v-else :data="rows" v-loading="loading"
      ><el-table-column prop="verificationSessionId" label="会话编号" width="110" /><el-table-column
        prop="companyName"
        label="原归属公司"
        min-width="140"
      /><el-table-column prop="applicationId" label="申请编号" width="110" /><el-table-column
        label="认证状态"
        min-width="140"
        ><template #default="s">{{
          api.verificationLabel(s.row.status)
        }}</template></el-table-column
      ><el-table-column label="建立时间" min-width="180"
        ><template #default="s">{{
          api.verificationTime(s.row.createdAt)
        }}</template></el-table-column
      ><el-table-column label="操作" width="110"
        ><template #default="s"
          ><el-button
            v-if="permission('query') && s.row.allowedActions.includes('VIEW_DEVICE_AUTH')"
            link
            type="primary"
            :disabled="busy"
            @click="detail(s.row.verificationSessionId)"
            >查看会话</el-button
          ></template
        ></el-table-column
      ></el-table
    >
    <el-pagination
      v-if="permission('list')"
      v-model:current-page="filter.pageNum"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="list"
    />
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <el-alert
      v-if="pending"
      :closable="false"
      type="warning"
      :title="`原会话 ${pending.id} 封存结果尚未确认，原正文和请求键已冻结。`"
      ><el-button :disabled="busy" @click="execute(pending!)">重试原封存操作</el-button></el-alert
    >
    <el-dialog v-model="open" title="本机认证会话详情" width="720px">
      <template v-if="selected"
        ><el-descriptions :column="1" border
          ><el-descriptions-item label="会话 / 申请"
            >{{ selected.verificationSessionId }} /
            {{ selected.applicationId }}</el-descriptions-item
          ><el-descriptions-item label="业务请求号">{{ selected.requestNo }}</el-descriptions-item
          ><el-descriptions-item label="原归属公司">{{
            selected.companyName || '历史归属未确定'
          }}</el-descriptions-item
          ><el-descriptions-item label="认证范围"
            >只确认本机使用者，不证明身份证实名</el-descriptions-item
          ><el-descriptions-item label="状态">{{
            api.verificationLabel(selected.status)
          }}</el-descriptions-item
          ><el-descriptions-item label="材料与版本"
            >公司修订 {{ selected.companyRevision }} · 资料修订 {{ selected.materialRevision }} ·
            记录版本 {{ selected.version }}</el-descriptions-item
          ><el-descriptions-item label="建立 / 校验时间"
            >{{ api.verificationTime(selected.createdAt) }} /
            {{ api.verificationTime(selected.verifiedAt) }}</el-descriptions-item
          ><el-descriptions-item label="挑战有效期">{{
            api.verificationTime(selected.expiresAt)
          }}</el-descriptions-item
          ><el-descriptions-item label="状态说明">{{
            selected.failureCode || '暂无'
          }}</el-descriptions-item></el-descriptions
        >
        <template v-if="canClose"
          ><p class="muted">
            仅可封存服务端明确允许的未决会话。此操作不构成核验通过，申请人后续仍须本人重新完成设备认证。
          </p>
          <el-input
            v-model="reason"
            :disabled="locked || stale"
            type="textarea"
            maxlength="500"
            placeholder="填写封存原因，最多 500 字"
        /></template>
        <p v-else class="muted">当前没有可执行的封存动作，以服务端状态与原归属公司权限为准。</p>
        <el-alert
          v-if="stale"
          type="warning"
          :closable="false"
          title="会话已变化，已保留原因，请重读后重新核对。"
        /><el-button
          v-if="stale"
          :disabled="locked"
          @click="detail(selected!.verificationSessionId, true)"
          >重读会话并保留原因</el-button
        ></template
      >
      <template #footer
        ><el-button @click="open = false">关闭</el-button
        ><el-button v-if="canClose" type="warning" :disabled="locked || stale" @click="closeSession"
          >确认封存原会话</el-button
        ></template
      >
    </el-dialog>
  </section>
</template>
<style scoped>
.device-sessions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.muted {
  color: #606266;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
:deep(.el-dialog) {
  max-width: calc(100vw - 24px);
}
:deep(.el-descriptions__content) {
  overflow-wrap: anywhere;
}
</style>
