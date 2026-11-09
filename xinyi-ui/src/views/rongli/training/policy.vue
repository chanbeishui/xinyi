<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { companyTree, type CompanyNode } from '@/api/rongli/driver';
import { intentKey } from '@/api/rongli/aftersale';
import { listTraining, type TrainingSummary } from '@/api/rongli/training';
import {
  getTrainingPolicy,
  publishTrainingPolicy,
  saveTrainingPolicy,
  type TrainingPolicy,
  type PolicyRevision,
  type SaveTrainingPolicy,
  type PublishTrainingPolicy
} from '@/api/rongli/trainingPolicy';

type Intent =
  | { action: 'SAVE'; company: string; key: string; body: SaveTrainingPolicy }
  | { action: 'PUBLISH'; company: string; key: string; body: PublishTrainingPolicy };
const companies = ref<CompanyNode[]>([]);
const companyError = ref('');
const companyId = ref('');
const policy = ref<TrainingPolicy | null>(null);
const courses = ref<TrainingSummary[]>([]);
const coursePage = ref(0);
const courseTotal = ref(0);
const courseError = ref('');
const loading = ref(false);
const courseLoading = ref(false);
const busy = ref(false);
const stale = ref(false);
const error = ref('');
const reason = ref('');
const baseline = ref('');
const publishOpen = ref(false);
const publishReason = ref('');
const publishTarget = ref<{ version: number; revision: PolicyRevision } | null>(null);
const form = reactive({
  requiredTrainingIds: [] as string[],
  maxAttempts: null as number | null,
  retryIntervalSeconds: null as number | null
});
// 未确认的写入按真实公司保存原目标、正文和键；切换公司不能把旧请求投递给新目标。
const pending = reactive(new Map<string, Intent>());
const unknown = computed(() => pending.has(companyId.value));
const locked = computed(() => busy.value || loading.value || unknown.value);
const dirty = computed(() => baseline.value !== JSON.stringify(form));
const selectedRevision = computed(
  () => policy.value?.draftRevision || policy.value?.activeRevision
);
const can = (action: string, permission: string) =>
  Boolean(
    policy.value?.allowedActions.includes(action) &&
      checkPermi(['biz:trainingpolicy:' + permission])
  );
const canEdit = computed(
  () => can('EDIT_TRAINING_POLICY', 'edit') && checkPermi(['biz:trainingcourse:query'])
);
const canPublish = computed(() => can('PUBLISH_TRAINING_POLICY', 'publish'));
const publishReady = computed(
  () =>
    canPublish.value &&
    policy.value?.draftRevision?.status === 'DRAFT' &&
    !policy.value.draftRevision.publishBlockers.length &&
    !dirty.value &&
    !stale.value &&
    !locked.value
);
const courseLookupAllowed = computed(() => checkPermi(['biz:trainingcourse:list']));
const login = getToken();
let sequence = 0;
let disposed = false;
const copy = <T,>(value: T): T => JSON.parse(JSON.stringify(value));
const status = (e: unknown): number => Number((e as { httpStatus?: number })?.httpStatus || 0);
const message = (e: unknown): string =>
  (e as { msg?: string; message?: string })?.msg ||
  (e as { message?: string })?.message ||
  '操作未完成，请重新核对';
function sessionCurrent() {
  if (disposed) return false;
  if (getToken() === login) return true;
  clear();
  pending.clear();
  companyId.value = '';
  error.value = '登录已变化，请重新进入并读取政策';
  return false;
}
const current = (company: string, request: number) =>
  sessionCurrent() && companyId.value === company && request === sequence;
const versionText = (revision: PolicyRevision | null | undefined) =>
  revision ? `第 ${revision.revisionNo} 版` : '尚未配置';
function resetForm() {
  form.requiredTrainingIds = [];
  form.maxAttempts = null;
  form.retryIntervalSeconds = null;
  reason.value = '';
  baseline.value = JSON.stringify(form);
}
function clear() {
  sequence++;
  loading.value = false;
  courseLoading.value = false;
  policy.value = null;
  courses.value = [];
  courseTotal.value = 0;
  coursePage.value = 0;
  courseError.value = '';
  error.value = '';
  stale.value = false;
  publishOpen.value = false;
  publishTarget.value = null;
  publishReason.value = '';
  resetForm();
}
function fill(policyData: TrainingPolicy) {
  const revision = policyData.draftRevision || policyData.activeRevision;
  form.requiredTrainingIds = [...(revision?.requiredTrainingIds || [])];
  form.maxAttempts = revision?.maxAttempts ?? null;
  form.retryIntervalSeconds = revision?.retryIntervalSeconds ?? null;
  reason.value = '';
  baseline.value = JSON.stringify(form);
}
function title(id: string) {
  const course = courses.value.find((row: TrainingSummary) => row.trainingId === id);
  return course ? `${course.title} · V${course.courseVersion}` : `课程编号 ${id}`;
}
function sameSeriesSelected(course: TrainingSummary) {
  return courses.value.some(
    (row: TrainingSummary) =>
      row.courseKey === course.courseKey &&
      row.trainingId !== course.trainingId &&
      form.requiredTrainingIds.includes(row.trainingId)
  );
}
async function chooseCompany() {
  clear();
  if (companyId.value) await load(false);
}
async function load(preserve: boolean) {
  if (!sessionCurrent() || !companyId.value || busy.value) return;
  const company = companyId.value;
  const request = ++sequence;
  const retained = preserve ? copy(form) : null;
  const retainedReason = reason.value;
  loading.value = true;
  error.value = '';
  try {
    const data = await getTrainingPolicy(company);
    if (!current(company, request)) return;
    policy.value = data;
    fill(data);
    const unresolved = pending.get(company);
    if (unresolved?.action === 'SAVE') {
      form.requiredTrainingIds = [...unresolved.body.requiredTrainingIds];
      form.maxAttempts = unresolved.body.maxAttempts;
      form.retryIntervalSeconds = unresolved.body.retryIntervalSeconds;
      reason.value = unresolved.body.reason;
    } else if (retained) {
      Object.assign(form, retained);
      reason.value = retainedReason;
    }
    stale.value = false;
    if (courseLookupAllowed.value) await loadCourses(true, request);
  } catch (e) {
    if (!current(company, request)) return;
    error.value = message(e);
    if ([401, 403, 404].includes(status(e))) {
      pending.delete(company);
      policy.value = null;
      courses.value = [];
      courseTotal.value = 0;
      resetForm();
    }
  } finally {
    if (current(company, request)) loading.value = false;
  }
}
async function loadCourses(reset = false, request = sequence) {
  if (!sessionCurrent() || !companyId.value || !courseLookupAllowed.value || courseLoading.value)
    return;
  const company = companyId.value;
  const page = reset ? 1 : coursePage.value + 1;
  courseLoading.value = true;
  courseError.value = '';
  try {
    const response = await listTraining({
      ownerDeptId: company,
      status: 'PUBLISHED',
      pageNum: page,
      pageSize: 100,
      sort: 'idDesc'
    });
    if (!current(company, request)) return;
    const rows = response.rows.filter(
      (row: TrainingSummary) => row.ownerDeptId === company && row.status === 'PUBLISHED'
    );
    const combined: TrainingSummary[] = reset ? rows : [...courses.value, ...rows];
    courses.value = Array.from(
      new Map(combined.map((row: TrainingSummary) => [row.trainingId, row])).values()
    );
    courseTotal.value = response.total;
    coursePage.value = page;
  } catch (e) {
    if (current(company, request)) courseError.value = message(e);
  } finally {
    if (current(company, request)) courseLoading.value = false;
  }
}
function normalizeNumber(field: 'maxAttempts' | 'retryIntervalSeconds', value: number | undefined) {
  form[field] = typeof value === 'number' && Number.isFinite(value) ? value : null;
}
function validForm() {
  if (form.requiredTrainingIds.length > 100) return '每份政策最多选择 100 个课程版本';
  for (const value of [form.maxAttempts, form.retryIntervalSeconds]) {
    if (value !== null && !Number.isInteger(value)) return '考试次数和间隔必须填写整数';
  }
  if (form.maxAttempts !== null && (form.maxAttempts < 1 || form.maxAttempts > 100))
    return '最多考试次数须为 1 至 100 次';
  if (
    form.retryIntervalSeconds !== null &&
    (form.retryIntervalSeconds < 0 || form.retryIntervalSeconds > 2592000)
  )
    return '重考间隔须为 0 至 2592000 秒';
  if (!reason.value.trim()) return '请填写维护原因';
  return '';
}
async function save() {
  if (!sessionCurrent() || !policy.value || !canEdit.value || locked.value || stale.value) return;
  const problem = validForm();
  if (problem) return void ElMessage.warning(problem);
  await execute({
    action: 'SAVE',
    company: companyId.value,
    key: intentKey(),
    body: {
      expectedVersion: policy.value.version,
      requiredTrainingIds: [...form.requiredTrainingIds],
      maxAttempts: form.maxAttempts ?? null,
      retryIntervalSeconds: form.retryIntervalSeconds ?? null,
      reason: reason.value.trim()
    }
  });
}
function preparePublish() {
  if (!sessionCurrent() || !policy.value || !publishReady.value) return;
  const revision = policy.value.draftRevision;
  if (!revision || revision.status !== 'DRAFT' || revision.publishBlockers.length) return;
  publishTarget.value = { version: policy.value.version, revision: copy(revision) };
  publishOpen.value = true;
}
async function publish() {
  if (!sessionCurrent() || !publishTarget.value || locked.value || stale.value) return;
  if (!publishReason.value.trim()) return void ElMessage.warning('请填写发布原因');
  await execute({
    action: 'PUBLISH',
    company: companyId.value,
    key: intentKey(),
    body: {
      expectedVersion: publishTarget.value.version,
      draftRevisionId: publishTarget.value.revision.revisionId,
      reason: publishReason.value.trim()
    }
  });
}
async function execute(intent: Intent) {
  if (!sessionCurrent() || busy.value || intent.company !== companyId.value) return;
  const request = sequence;
  busy.value = true;
  error.value = '';
  try {
    const data =
      intent.action === 'SAVE'
        ? await saveTrainingPolicy(intent.company, intent.body, intent.key)
        : await publishTrainingPolicy(intent.company, intent.body, intent.key);
    pending.delete(intent.company);
    if (!current(intent.company, request)) return;
    policy.value = data;
    fill(data);
    stale.value = false;
    publishOpen.value = false;
    publishTarget.value = null;
    publishReason.value = '';
    ElMessage.success(
      intent.action === 'SAVE' ? '政策草稿已保存，尚未生效' : '政策已发布，已固定课表继续保留原版本'
    );
  } catch (e) {
    if (!current(intent.company, request)) return;
    error.value = message(e);
    if (!status(e) || status(e) >= 500) {
      pending.set(intent.company, copy(intent));
      error.value = '操作结果尚未确认，填写已锁定。请沿用原操作重试。';
    } else {
      pending.delete(intent.company);
      if (status(e) === 409) {
        stale.value = true;
        error.value = '政策已被更新，填写和原因已保留。请重读最新政策后再保存或重新核对发布。';
      } else if ([401, 403, 404].includes(status(e))) {
        policy.value = null;
        courses.value = [];
        courseTotal.value = 0;
        publishOpen.value = false;
        publishTarget.value = null;
        publishReason.value = '';
        resetForm();
      }
    }
  } finally {
    busy.value = false;
  }
}
async function retry() {
  const intent = pending.get(companyId.value);
  if (intent) await execute(intent);
}
async function reread() {
  publishOpen.value = false;
  publishTarget.value = null;
  await load(true);
}
async function useServerDraft() {
  if (locked.value || !policy.value) return;
  try {
    await ElMessageBox.confirm('载入当前服务器草稿将替换未保存的填写，是否继续？', '重新载入', {
      type: 'warning'
    });
    await load(false);
  } catch {
    // 取消仅保留当前填写，不触发保存或发布。
  }
}
onMounted(async () => {
  if (!checkPermi(['system:dept:list'])) {
    companyError.value = '没有部门列表权限，请联系管理员配置公司目录权限';
    return;
  }
  try {
    const result = await companyTree();
    if (!disposed && getToken() === login) companies.value = result.data;
  } catch (e) {
    if (!disposed) companyError.value = message(e);
  }
});
onBeforeUnmount(() => {
  disposed = true;
  clear();
  pending.clear();
});
</script>

<template>
  <div class="app-container policy-page">
    <header class="page-heading">
      <div>
        <h2>公司培训政策</h2>
        <p>明确必修课程和考试规则；发布后用于首次学习，已经固定的课表保留原版本。</p>
      </div>
    </header>
    <el-card shadow="never" class="company-card">
      <div class="company-controls">
        <label for="training-policy-company">所属公司</label>
        <el-tree-select
          id="training-policy-company"
          v-model="companyId"
          :data="companies"
          check-strictly
          filterable
          clearable
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          placeholder="请明确选择公司"
          :disabled="busy || Boolean(companyError)"
          @change="chooseCompany"
        />
        <el-button :disabled="!companyId || locked" @click="load(Boolean(policy))"
          >重新读取政策</el-button
        >
      </div>
      <el-alert v-if="companyError" :title="companyError" type="error" :closable="false" />
      <p v-if="!companyId" class="hint">请选择公司后读取；不会使用当前账号的主部门代替公司归属。</p>
    </el-card>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="notice" />
    <div v-if="unknown" class="recovery">
      <el-button type="warning" :loading="busy" @click="retry">重试原操作</el-button>
      <span>结果确认前不修改填写，也不创建第二次操作。</span>
    </div>
    <div v-if="stale && !unknown" class="recovery">
      <el-button type="warning" :loading="loading" @click="reread"
        >重读最新政策并保留填写</el-button
      >
      <span>重新读取后请核对新的已保存草稿。</span>
    </div>
    <div v-loading="loading" class="policy-content">
      <template v-if="policy">
        <section class="revision-grid">
          <el-card shadow="never">
            <h3>当前生效政策</h3>
            <el-tag :type="policy.activeRevision ? 'success' : 'info'">{{
              versionText(policy.activeRevision)
            }}</el-tag>
            <template v-if="policy.activeRevision">
              <p>
                {{ policy.activeRevision.requiredTrainingIds.length }} 个必修课程版本 · 最多
                {{ policy.activeRevision.maxAttempts }} 次考试
              </p>
              <p>重考间隔 {{ policy.activeRevision.retryIntervalSeconds }} 秒</p>
              <ul>
                <li v-for="id in policy.activeRevision.requiredTrainingIds" :key="id">
                  {{ title(id) }}
                </li>
              </ul>
            </template>
            <p v-else class="hint">尚未发布政策，不能据此开始考试或认定培训通过。</p>
          </el-card>
          <el-card shadow="never">
            <h3>已保存草稿</h3>
            <el-tag :type="selectedRevision?.status === 'DRAFT' ? 'warning' : 'info'">{{
              versionText(policy.draftRevision)
            }}</el-tag>
            <p>公司：{{ policy.companyName }} · 记录版本 {{ policy.version }}</p>
            <p class="hint">保存草稿不会改变当前生效政策。发布会核对已保存的精确版本。</p>
          </el-card>
        </section>
        <el-card shadow="never" class="editor-card">
          <template #header><h3>维护必修课程与考试规则</h3></template>
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="必修课程与确切版本">
              <el-select
                v-model="form.requiredTrainingIds"
                multiple
                filterable
                clearable
                class="course-select"
                placeholder="选择本公司已发布的课程版本"
                :disabled="!canEdit || locked || stale || !courseLookupAllowed"
              >
                <el-option
                  v-for="course in courses"
                  :key="course.trainingId"
                  :value="course.trainingId"
                  :label="`${course.title} · V${course.courseVersion}`"
                  :disabled="sameSeriesSelected(course)"
                />
              </el-select>
              <p class="hint">同一课程只选一个版本。首次学习后，本次申请的课程与版本固定。</p>
              <p v-if="!courseLookupAllowed" class="hint">
                更换课程需要课程列表权限；已有选择会保留。
              </p>
              <el-alert v-if="courseError" :title="courseError" type="warning" :closable="false" />
              <el-button
                v-if="courses.length < courseTotal"
                :loading="courseLoading"
                :disabled="locked"
                @click="loadCourses(false)"
                >加载更多已发布课程</el-button
              >
            </el-form-item>
            <div class="rule-grid">
              <el-form-item label="最多考试次数（含首次）">
                <!-- 当前组件仅挂载时写 aria-disabled；锁状态切换时重建，使读屏与真实可编辑状态一致。 -->
                <el-input-number
                  :key="`attempts-${!canEdit || locked || stale}`"
                  v-model="form.maxAttempts"
                  @change="normalizeNumber('maxAttempts', $event)"
                  :min="1"
                  :max="100"
                  :precision="0"
                  placeholder="尚未配置"
                  :disabled="!canEdit || locked || stale"
                  controls-position="right"
                />
                <p class="hint">同一申请、同一课程累计；修改材料不重置，失败和超时均计次。</p>
              </el-form-item>
              <el-form-item label="失败或超时后的重考间隔（秒）">
                <el-input-number
                  :key="`interval-${!canEdit || locked || stale}`"
                  v-model="form.retryIntervalSeconds"
                  @change="normalizeNumber('retryIntervalSeconds', $event)"
                  :min="0"
                  :max="2592000"
                  :precision="0"
                  placeholder="尚未配置"
                  :disabled="!canEdit || locked || stale"
                  controls-position="right"
                />
                <p class="hint">需明确填写。0 表示不额外等待，不代表不限考试次数。</p>
              </el-form-item>
            </div>
            <el-alert
              title="评分规则：每题等分，总分 100；多选必须完全匹配，最终四舍五入到整数分后判定通过。"
              type="info"
              :closable="false"
            />
            <el-form-item label="维护原因" class="reason-field">
              <el-input
                v-model="reason"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-word-limit
                placeholder="填写本次维护原因，最多 500 字"
                :disabled="!canEdit || locked || stale"
              />
            </el-form-item>
            <div class="actions">
              <el-button
                v-if="canEdit"
                type="primary"
                :loading="busy"
                :disabled="locked || stale"
                @click="save"
                >保存政策草稿</el-button
              >
              <el-button
                v-if="canPublish"
                type="success"
                :disabled="!publishReady"
                @click="preparePublish"
                >核对已保存草稿并发布</el-button
              >
              <el-button :disabled="locked" @click="useServerDraft">载入服务器草稿</el-button>
            </div>
            <p v-if="dirty" class="hint">当前填写尚未保存。请先保存，再核对发布。</p>
            <ul v-if="selectedRevision?.publishBlockers.length" class="blockers">
              <li
                v-for="blocker in selectedRevision.publishBlockers"
                :key="blocker.field + blocker.reasonCode"
              >
                {{ blocker.message }}
              </li>
            </ul>
          </el-form>
        </el-card>
      </template>
    </div>
    <el-dialog
      v-model="publishOpen"
      title="发布公司培训政策"
      width="min(680px, 94vw)"
      :close-on-click-modal="false"
      :close-on-press-escape="!busy"
      :show-close="!busy"
    >
      <template v-if="publishTarget">
        <p>公司：{{ policy?.companyName }} · {{ versionText(publishTarget.revision) }}</p>
        <p>
          本次将发布以下 {{ publishTarget.revision.requiredTrainingIds.length }} 个确切课程版本：
        </p>
        <ul>
          <li v-for="id in publishTarget.revision.requiredTrainingIds" :key="id">
            {{ title(id) }}
          </li>
        </ul>
        <p>
          最多 {{ publishTarget.revision.maxAttempts }} 次考试，重考间隔
          {{ publishTarget.revision.retryIntervalSeconds }} 秒。
        </p>
        <p class="hint">仅在确认正式要求后发布。已有申请固定的课表继续保留原版本。</p>
        <el-input
          v-model="publishReason"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="填写发布原因，最多 500 字"
          :disabled="locked || stale"
        />
      </template>
      <template #footer>
        <el-button :disabled="busy" @click="publishOpen = false">关闭</el-button>
        <el-button
          v-if="!unknown"
          type="primary"
          :loading="busy"
          :disabled="locked || stale"
          @click="publish"
          >确认发布</el-button
        >
        <el-button v-else type="warning" :loading="busy" @click="retry">重试原操作</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-heading h2 {
  margin: 0 0 10px;
}
.page-heading p,
.hint {
  color: #65758b;
  line-height: 1.7;
}
.company-card,
.editor-card {
  margin-top: 20px;
}
.company-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
  margin-bottom: 12px;
}
.company-controls .el-tree-select {
  width: 300px;
  max-width: 100%;
}
.notice,
.recovery {
  margin: 18px 0;
}
.recovery {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
}
.revision-grid,
.rule-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.revision-grid h3,
.editor-card h3 {
  margin: 0 0 16px;
}
.course-select {
  width: 100%;
}
.el-form-item :deep(.el-form-item__content) {
  display: block;
}
.rule-grid .el-input-number {
  width: 100%;
}
.reason-field {
  margin-top: 22px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.blockers {
  padding: 16px 16px 16px 32px;
  color: #975e18;
  background: #fff7e9;
  border-radius: 8px;
  line-height: 1.8;
}
@media (max-width: 700px) {
  .revision-grid,
  .rule-grid {
    grid-template-columns: 1fr;
  }
  .company-controls .el-tree-select {
    width: 100%;
  }
  .actions .el-button {
    margin-left: 0;
  }
}
</style>
