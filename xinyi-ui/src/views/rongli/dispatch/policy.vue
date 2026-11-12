<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { companyTree, type CompanyNode } from '@/api/rongli/driver';
import { intentKey } from '@/api/rongli/aftersale';
import { vehicleTypes, type LicenseCompatibility } from '@/api/rongli/dispatch';
import {
  getPolicy,
  savePolicy,
  type DispatchCompanyPolicy,
  type SaveDispatchPolicy
} from '@/api/rongli/dispatch';
const companies = ref<CompanyNode[]>([]),
  companyId = ref(''),
  directoryError = ref('');
const config = ref<DispatchCompanyPolicy | null>(null),
  error = ref(''),
  reason = ref('');
const loading = ref(false),
  busy = ref(false),
  stale = ref(false);
type Form = {
  inviteResponseSeconds: number | null;
  radiusMeter: number | null;
  expandedRadiusMeter: number | null;
  overallDeadlineSeconds: number | null;
  licenseCompatibility: LicenseCompatibility[];
};
const empty = (): Form => ({
  inviteResponseSeconds: null,
  radiusMeter: null,
  expandedRadiusMeter: null,
  overallDeadlineSeconds: null,
  licenseCompatibility: []
});
const types = ref<{ code: string; label: string; enabled: boolean }[]>([]),
  dictionaryError = ref('');
const form = reactive<Form>(empty());
type Intent = { company: string; body: SaveDispatchPolicy; key: string };
// 写结果未知时按真实公司保存原体与键，切换选择不会将旧请求发送给另一家公司。
const pending = reactive(new Map<string, Intent>());
function pendingValues(): Intent[] {
  return Array.from(pending.values());
}
const unknown = computed(() => pending.has(companyId.value));
const locked = computed(() => loading.value || busy.value || unknown.value || stale.value);
const permitted = (action: string) => checkPermi(['biz:dispatchpolicy:' + action]);
const canEdit = computed(
  () =>
    permitted('query') &&
    permitted('edit') &&
    config.value?.allowedActions.includes('EDIT_DISPATCH_POLICY')
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
function fill(value: DispatchCompanyPolicy) {
  Object.assign(form, {
    inviteResponseSeconds: value.inviteResponseSeconds,
    radiusMeter: value.radiusMeter,
    expandedRadiusMeter: value.expandedRadiusMeter,
    overallDeadlineSeconds: value.overallDeadlineSeconds,
    licenseCompatibility: structuredClone(value.licenseCompatibility || [])
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
    const value = await getPolicy(company);
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
function numberInput(field: Exclude<keyof Form, 'licenseCompatibility'>, value: unknown) {
  form[field] = typeof value === 'number' && Number.isFinite(value) ? value : null;
}
function body(): SaveDispatchPolicy {
  if (!config.value || !canEdit.value) throw new Error('当前没有公司派单策略写入权限');
  const fields: Array<[Exclude<keyof Form, 'licenseCompatibility'>, string, number, number]> = [
    ['inviteResponseSeconds', '邀约响应秒数', 1, 3600],
    ['radiusMeter', '初始半径', 1, 1000000],
    ['expandedRadiusMeter', '扩大后半径', 1, 1000000],
    ['overallDeadlineSeconds', '批次总时限', 1, 86400]
  ];
  for (const [field, label, min, max] of fields) {
    const value = form[field];
    if (value === null || !Number.isInteger(value) || value < min || value > max)
      throw new Error(`请明确填写${label}，须为 ${min} 至 ${max} 的整数`);
  }
  if (form.expandedRadiusMeter! < form.radiusMeter!) throw new Error('扩大后半径不能小于初始半径');
  if (dictionaryError.value || !types.value.length)
    throw new Error('启用车型目录未取得，不能维护兼容关系');
  const values: LicenseCompatibility[] = form.licenseCompatibility;
  if (
    !values.length ||
    values.length > 200 ||
    new Set(values.map((row) => row.vehicleTypeCode)).size !== values.length
  )
    throw new Error('请维护1至200条不重复的车型兼容关系');
  for (const row of values) {
    if (
      !types.value.some((type: { code: string }) => type.code === row.vehicleTypeCode) ||
      !row.licenseClasses.length ||
      row.licenseClasses.length > 32 ||
      new Set(row.licenseClasses).size !== row.licenseClasses.length ||
      row.licenseClasses.some(
        (value) => !value.trim() || value.trim() !== value || value.length > 32
      )
    )
      throw new Error('请选择启用车型并明确填写不重复准驾类别，每条1至32项、每项最多32字');
  }
  if (!reason.value.trim()) throw new Error('请填写1至500字维护原因');
  return {
    expectedVersion: config.value.version,
    inviteResponseSeconds: form.inviteResponseSeconds!,
    radiusMeter: form.radiusMeter!,
    expandedRadiusMeter: form.expandedRadiusMeter!,
    overallDeadlineSeconds: form.overallDeadlineSeconds!,
    roundPolicy: 'COMPANY_CURSOR',
    distanceMode: 'GCJ02_GREAT_CIRCLE',
    licenseCompatibility: values.map((row) => ({
      vehicleTypeCode: row.vehicleTypeCode,
      licenseClasses: [...row.licenseClasses]
    })),
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
    const value = await savePolicy(intent.company, intent.body, intent.key);
    if (!current(intent.company, run)) return;
    pending.delete(intent.company);
    config.value = value;
    stale.value = false;
    fill(value);
    ElMessage.success('公司派单策略已保存；新订单冻结新版本，旧订单与已启动批次不覆盖');
  } catch (failure) {
    if (!current(intent.company, run)) return;
    const code = status(failure);
    if (code > 0 && code < 500) pending.delete(intent.company);
    error.value = message(failure);
    config.value = null;
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
    if (permitted('edit')) {
      try {
        types.value = await vehicleTypes();
      } catch (failure) {
        dictionaryError.value = message(failure);
      }
    }
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
  <div class="app-container dispatch-policy-page">
    <h2>公司派单策略</h2>
    <p class="intro">
      同公司代驾共用派单池，订单在创建时冻结服务公司和策略版本。参数必须由公司明确维护。
    </p>
    <el-alert
      type="info"
      :closable="false"
      title="公司游标轮转，距离为GCJ02直线半径；初圈无候选只扩一圈。保存仅供新订单冻结，旧订单与已启动批次不覆盖。"
    />
    <p v-if="!permitted('query')" class="warning">当前没有公司派单策略查询权限。</p>
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
          轮转固定为公司游标；距离固定为 GCJ02 直线距离。以下值均须明确填写，不使用运营默认值。
        </p>
        <el-form class="config-form" label-position="top" :disabled="locked || !canEdit">
          <el-form-item
            v-for="field in [
              ['inviteResponseSeconds', '邀约响应秒数（1–3600）', 3600],
              ['radiusMeter', '初始半径（米，1–1000000）', 1000000],
              ['expandedRadiusMeter', '扩大后半径（米，不能小于初始半径）', 1000000],
              ['overallDeadlineSeconds', '批次总时限（秒，1–86400）', 86400]
            ] as const"
            :key="field[0]"
            :label="field[1]"
          >
            <el-input-number
              :key="field[0] + String(locked || !canEdit)"
              :model-value="form[field[0]]"
              :min="1"
              :max="field[2]"
              :precision="0"
              :value-on-clear="null"
              placeholder="请明确配置"
              @update:model-value="numberInput(field[0], $event)"
            />
          </el-form-item>
          <div class="wide compatibility">
            <strong>车型与允许准驾类别（显式业务关系）</strong>
            <p class="intro">
              按公司业务明确填写，不由客户端推导法定准驾关系；每种启用车型只配置一次。
            </p>
            <p v-if="dictionaryError" class="error">{{ dictionaryError }}</p>
            <div
              v-for="(row, index) in form.licenseCompatibility"
              :key="index"
              class="compatibility-row"
            >
              <el-select v-model="row.vehicleTypeCode" placeholder="选择启用车型"
                ><el-option
                  v-for="type in types"
                  :key="type.code"
                  :value="type.code"
                  :label="type.label" /></el-select
              ><el-select
                v-model="row.licenseClasses"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="逐项输入允许准驾类别并回车"
                :reserve-keyword="false"
              /><el-button
                :disabled="locked || !canEdit"
                @click="form.licenseCompatibility.splice(index, 1)"
                >删除关系</el-button
              >
            </div>
            <el-button
              :disabled="locked || !canEdit || !!dictionaryError || !types.length"
              @click="
                form.licenseCompatibility.push({
                  vehicleTypeCode: '',
                  licenseClasses: []
                })
              "
              >添加车型兼容关系</el-button
            >
          </div>
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
          批次总截止优先于单次响应期限，扩大半径不得小于初始半径。人工邀约仍需同公司、当前合格与本人响应，不跳过半径或占用检查。
        </p>
        <div class="actions">
          <el-button v-if="canEdit" type="primary" :disabled="locked" @click="save"
            >保存公司派单策略</el-button
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
    <div v-if="companyId && !config && !loading" class="actions">
      <el-button :disabled="busy" @click="load(stale)">重读配置并保留填写</el-button
      ><el-button v-if="unknown" :disabled="busy" type="warning" @click="retry(companyId)"
        >重试原公司保存操作</el-button
      >
    </div>
    <p v-if="stale" class="warning">配置版本已变化。填写内容已保留，请重新读取并核对后再保存。</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
  </div>
</template>
<style scoped>
.dispatch-policy-page {
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
.compatibility-row {
  display: grid;
  grid-template-columns: 1fr 2fr auto;
  gap: 12px;
  margin: 14px 0;
}
.compatibility-row :deep(.el-select) {
  width: 100%;
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
  .compatibility-row {
    grid-template-columns: minmax(0, 1fr);
  }
  .config-form {
    grid-template-columns: minmax(0, 1fr);
  }
  .company-selector {
    max-width: none;
  }
}
</style>
