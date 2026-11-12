<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { companyTree, type CompanyNode } from '@/api/rongli/driver';
import { intentKey } from '@/api/rongli/aftersale';
import {
  getListeningConfig,
  saveListeningConfig,
  type ListeningConfig,
  type SaveListeningConfig
} from '@/api/rongli/listeningPolicy';
const companies = ref<CompanyNode[]>([]),
  companyId = ref(''),
  directoryError = ref('');
const config = ref<ListeningConfig | null>(null),
  error = ref(''),
  reason = ref('');
const loading = ref(false),
  busy = ref(false),
  stale = ref(false);
type Form = {
  closingTime: string;
  locationMaxAgeSeconds: number | null;
  reachabilityMaxAgeSeconds: number | null;
  maxAccuracyMeter: number | null;
  locationReportIntervalSeconds: number | null;
};
const empty = (): Form => ({
  closingTime: '',
  locationMaxAgeSeconds: null,
  reachabilityMaxAgeSeconds: null,
  maxAccuracyMeter: null,
  locationReportIntervalSeconds: null
});
const form = reactive<Form>(empty());
type Intent = { company: string; body: SaveListeningConfig; key: string };
// 写结果未知时按真实公司保存原体与键，切换选择不会将旧请求发送给另一家公司。
const pending = reactive(new Map<string, Intent>());
function pendingValues(): Intent[] {
  return Array.from(pending.values());
}
const unknown = computed(() => pending.has(companyId.value));
const locked = computed(() => loading.value || busy.value || unknown.value || stale.value);
const permitted = (action: string) => checkPermi(['biz:dispatchlistening:' + action]);
const canEdit = computed(
  () =>
    permitted('query') &&
    permitted('edit') &&
    config.value?.allowedActions.includes('EDIT_LISTENING_CONFIG')
);
const login = getToken();
let sequence = 0,
  disposed = false;
const status = (failure: unknown) => Number((failure as { httpStatus?: number })?.httpStatus || 0);
const message = (failure: unknown) =>
  (failure as { msg?: string; message?: string })?.msg ||
  (failure as { message?: string })?.message ||
  '操作未完成，请重新核对';
function reset() {
  sequence++;
  config.value = null;
  loading.value = false;
  stale.value = false;
  error.value = '';
  reason.value = '';
  Object.assign(form, empty());
}
function current(company: string, run: number) {
  if (disposed) return false;
  if (getToken() !== login) {
    reset();
    pending.clear();
    companyId.value = '';
    companies.value = [];
    error.value = '登录已变化，请重新进入并读取公司配置';
    return false;
  }
  return companyId.value === company && sequence === run;
}
function fill(value: ListeningConfig) {
  Object.assign(form, {
    closingTime: value.closingTime || '',
    locationMaxAgeSeconds: value.locationMaxAgeSeconds,
    reachabilityMaxAgeSeconds: value.reachabilityMaxAgeSeconds,
    maxAccuracyMeter: value.maxAccuracyMeter,
    locationReportIntervalSeconds: value.locationReportIntervalSeconds
  });
  reason.value = '';
}
function restore(intent: Intent) {
  const { expectedVersion: ignored, reason: originalReason, ...values } = intent.body;
  Object.assign(form, values);
  reason.value = originalReason;
}
async function load(preserve = false) {
  if (!companyId.value || !permitted('query') || busy.value) return;
  const company = companyId.value,
    saved = { ...form },
    originalReason = reason.value;
  const run = ++sequence;
  loading.value = true;
  error.value = '';
  config.value = null;
  try {
    const value = await getListeningConfig(company);
    if (!current(company, run)) return;
    config.value = value;
    fill(value);
    const intent = pending.get(company);
    if (intent) restore(intent);
    else if (preserve) {
      Object.assign(form, saved);
      reason.value = originalReason;
    }
    stale.value = false;
  } catch (failure) {
    if (!current(company, run)) return;
    error.value = message(failure);
    if ([401, 403, 404].includes(status(failure))) {
      pending.delete(company);
      Object.assign(form, empty());
      reason.value = '';
    }
  } finally {
    if (current(company, run)) loading.value = false;
  }
}
async function chooseCompany() {
  reset();
  if (companyId.value) await load();
}
function numberInput(field: Exclude<keyof Form, 'closingTime'>, value: unknown) {
  form[field] = typeof value === 'number' && Number.isFinite(value) ? value : null;
}
function body(): SaveListeningConfig {
  if (!config.value || !canEdit.value) throw new Error('当前没有公司听单配置写入权限');
  if (!/^([01][0-9]|2[0-3]):[0-5][0-9]$/.test(form.closingTime))
    throw new Error('请明确填写公司每日下班时间，格式为 HH:mm');
  const fields: Array<[Exclude<keyof Form, 'closingTime'>, string, number, number]> = [
    ['locationMaxAgeSeconds', '位置有效期', 31, 86400],
    ['reachabilityMaxAgeSeconds', '近期可达有效期', 31, 86400],
    ['maxAccuracyMeter', '最大位置误差', 1, 100000],
    ['locationReportIntervalSeconds', '前台上报间隔', 30, 3600]
  ];
  for (const [field, label, minimum, maximum] of fields) {
    const value = form[field];
    if (value === null || !Number.isInteger(value) || value < minimum || value > maximum)
      throw new Error(`请明确填写${label}，须为 ${minimum} 至 ${maximum} 的整数`);
  }
  if (
    form.locationReportIntervalSeconds! >= form.locationMaxAgeSeconds! ||
    form.locationReportIntervalSeconds! >= form.reachabilityMaxAgeSeconds!
  )
    throw new Error('前台上报间隔必须小于位置和近期可达的有效期');
  if (!reason.value.trim() || reason.value.trim().length > 500)
    throw new Error('请填写 1 至 500 字维护原因');
  return {
    expectedVersion: config.value.version,
    closingTime: form.closingTime,
    locationMaxAgeSeconds: form.locationMaxAgeSeconds!,
    reachabilityMaxAgeSeconds: form.reachabilityMaxAgeSeconds!,
    maxAccuracyMeter: form.maxAccuracyMeter!,
    locationReportIntervalSeconds: form.locationReportIntervalSeconds!,
    reason: reason.value.trim()
  };
}
async function execute(intent: Intent) {
  if (busy.value || disposed || getToken() !== login) return;
  const run = sequence;
  busy.value = true;
  error.value = '';
  pending.set(intent.company, intent);
  try {
    const value = await saveListeningConfig(intent.company, intent.body, intent.key);
    if (!current(intent.company, run)) return;
    pending.delete(intent.company);
    config.value = value;
    stale.value = false;
    fill(value);
    ElMessage.success('公司听单配置已保存并立即生效');
  } catch (failure) {
    if (!current(intent.company, run)) return;
    const code = status(failure);
    if (code > 0 && code < 500) pending.delete(intent.company);
    error.value = message(failure);
    if (code === 409) stale.value = true;
    if ([401, 403, 404].includes(code)) {
      config.value = null;
      Object.assign(form, empty());
      reason.value = '';
    }
  } finally {
    if (!disposed) busy.value = false;
  }
}
function save() {
  if (locked.value) return;
  try {
    void execute({ company: companyId.value, body: body(), key: intentKey() });
  } catch (failure) {
    error.value = message(failure);
  }
}
function retry(company: string) {
  const intent = pending.get(company);
  if (intent) void execute(intent);
}
onMounted(async () => {
  if (!permitted('query') || !checkPermi(['system:dept:list'])) return;
  try {
    const result = await companyTree();
    if (!disposed && getToken() === login) companies.value = result.data;
  } catch (failure) {
    if (!disposed) directoryError.value = message(failure);
  }
});
onBeforeUnmount(() => {
  disposed = true;
  reset();
  pending.clear();
  companies.value = [];
});
</script>
<template>
  <div class="app-container listening-config-page">
    <h2>公司听单设置</h2>
    <p class="intro">
      同公司代驾共用每日下班时间。切到后台保持听单意愿；每日到下班时刻关闭，次日由本人手动开启。
    </p>
    <el-alert
      type="info"
      :closable="false"
      title="保存后立即生效。提前下班会收紧已开启窗口，延后下班不自动延长已有窗口；没有上班时间或次日自动恢复。"
    />
    <p v-if="!permitted('query')" class="warning">当前没有公司听单配置查询权限。</p>
    <p v-else-if="!checkPermi(['system:dept:list'])" class="warning">
      选择真实公司需要部门列表权限，请联系管理员配置；不会使用账号主部门替代。
    </p>
    <p v-if="directoryError" class="error" role="alert">公司目录读取失败：{{ directoryError }}</p>
    <div class="company-selector">
      <label>维护公司（明确选择）</label>
      <el-tree-select
        v-model="companyId"
        :data="companies"
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        check-strictly
        clearable
        :disabled="busy || !permitted('query') || !companies.length"
        placeholder="请选择实际归属公司"
        @change="chooseCompany"
      />
    </div>
    <el-alert
      v-for="intent in pendingValues()"
      :key="intent.company"
      type="warning"
      :closable="false"
      :title="`公司 ${intent.company} 的原配置保存结果尚未确认；原目标、版本、正文及请求键保持不变。`"
    >
      <el-button
        :disabled="busy || loading"
        @click="
          companyId = intent.company;
          chooseCompany();
        "
        >返回原公司核对</el-button
      >
    </el-alert>
    <p v-if="loading" class="intro">正在读取实际公司配置…</p>
    <template v-if="config">
      <el-card
        ><template #header
          ><div class="card-heading">
            <strong>{{ config.companyName }}</strong
            ><el-tag
              >{{ config.configured ? '当前生效配置' : '尚未配置' }} · 版本
              {{ config.version }}</el-tag
            >
          </div></template
        >
        <p class="intro">
          时区固定为北京时间（Asia/Shanghai）。以下运营值均须明确填写，不使用平台默认值。
        </p>
        <el-form class="config-form" label-position="top" :disabled="locked || !canEdit">
          <el-form-item label="每日下班时间（北京时间，必填）"
            ><el-input v-model="form.closingTime" maxlength="5" placeholder="HH:mm"
          /></el-form-item>
          <el-form-item label="位置有效期（秒，31–86400）"
            ><el-input-number
              :key="'location-' + String(locked || !canEdit)"
              :model-value="form.locationMaxAgeSeconds"
              :min="31"
              :max="86400"
              :precision="0"
              :value-on-clear="null"
              placeholder="请明确配置"
              @update:model-value="numberInput('locationMaxAgeSeconds', $event)"
          /></el-form-item>
          <el-form-item label="近期可达有效期（秒，31–86400）"
            ><el-input-number
              :key="'contact-' + String(locked || !canEdit)"
              :model-value="form.reachabilityMaxAgeSeconds"
              :min="31"
              :max="86400"
              :precision="0"
              :value-on-clear="null"
              placeholder="请明确配置"
              @update:model-value="numberInput('reachabilityMaxAgeSeconds', $event)"
          /></el-form-item>
          <el-form-item label="最大位置误差（米，1–100000）"
            ><el-input-number
              :key="'accuracy-' + String(locked || !canEdit)"
              :model-value="form.maxAccuracyMeter"
              :min="1"
              :max="100000"
              :precision="0"
              :value-on-clear="null"
              placeholder="请明确配置"
              @update:model-value="numberInput('maxAccuracyMeter', $event)"
          /></el-form-item>
          <el-form-item label="前台上报间隔（秒，30–3600）"
            ><el-input-number
              :key="'interval-' + String(locked || !canEdit)"
              :model-value="form.locationReportIntervalSeconds"
              :min="30"
              :max="3600"
              :precision="0"
              :value-on-clear="null"
              placeholder="请明确配置"
              @update:model-value="numberInput('locationReportIntervalSeconds', $event)"
          /></el-form-item>
          <el-form-item class="wide" label="维护原因（必填，仅本次输入）"
            ><el-input
              v-model="reason"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="填写本次配置维护原因，最多500字"
          /></el-form-item>
        </el-form>
        <p class="warning">
          前台上报间隔必须小于两个有效期。近期交互有效不保证设备始终在线，位置新鲜与精度分别判断；关闭听单不解除在手任务或车辆责任。
        </p>
        <div class="actions">
          <el-button v-if="canEdit" type="primary" :disabled="locked" @click="save"
            >保存并立即生效</el-button
          >
          <el-button :disabled="busy || loading" @click="load(stale)">{{
            stale ? '重读配置并保留填写' : '重新读取配置'
          }}</el-button>
          <el-button
            v-if="unknown"
            type="warning"
            :disabled="busy || loading"
            @click="retry(companyId)"
            >重试原公司保存操作</el-button
          >
        </div>
      </el-card>
    </template>
    <p v-else-if="!loading && !companyId" class="intro">
      请先选择有权维护的公司；尚未选择时不读取任何公司的业务配置。
    </p>
    <p v-if="stale" class="warning">配置版本已变化。填写内容已保留，请重新读取并核对后再保存。</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
  </div>
</template>
<style scoped>
.listening-config-page {
  max-width: 1280px;
}
h2 {
  margin: 0 0 12px;
}
.intro {
  color: #606266;
  line-height: 1.8;
}
.company-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 440px;
  margin: 22px 0;
}
.card-heading {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: space-between;
  align-items: center;
}
.config-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 28px;
  margin-top: 20px;
}
.config-form :deep(.el-input-number) {
  width: 100%;
}
.wide {
  grid-column: 1 / -1;
}
.warning {
  padding: 12px;
  background: #fff6e8;
  border-radius: 8px;
  color: #87520c;
  line-height: 1.8;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}
.actions :deep(.el-button) {
  margin-left: 0;
}
@media (max-width: 700px) {
  .config-form {
    grid-template-columns: minmax(0, 1fr);
  }
  .company-selector {
    max-width: none;
  }
}
</style>
