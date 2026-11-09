<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { intentKey } from '@/api/rongli/aftersale';
import * as api from '@/api/rongli/verification';

type Intent =
  | { action: 'CREATE'; id: string; body: api.CreateManual; key: string }
  | { action: 'REVIEW'; id: string; body: api.ReviewManual; key: string };
const rows = ref<api.ManualRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const busy = ref(false);
const error = ref('');
const filter = reactive({ applicationId: '', type: '', status: '', pageNum: 1, pageSize: 10 });
const open = ref(false);
const createOpen = ref(false);
const selected = ref<api.ManualRecord | null>(null);
const candidates = ref<api.ManualApplication[]>([]);
const candidatePage = ref(1);
const candidateTotal = ref(0);
const candidateLoading = ref(false);
const chosen = ref('');
const form = reactive({
  type: 'BACKGROUND' as api.CreateManual['type'],
  conclusion: 'PASSED' as api.CreateManual['conclusion'],
  note: ''
});
const review = reactive({ decision: 'CONFIRM' as api.ReviewManual['decision'], note: '' });
const stale = ref(false);
// 未知写只在当前登录内保留原目标、正文和 UUID；关闭弹窗或查看另一记录不会重指向新对象。
const pending = ref<Intent | null>(null);
const permission = (action: string) => checkPermi(['biz:manualverification:' + action]);
const canCreate = computed(() => permission('create') && permission('query'));
const locked = computed(() => loading.value || busy.value || Boolean(pending.value));
const canReview = computed(
  () =>
    permission('review') && selected.value?.allowedActions.includes('REVIEW_MANUAL_VERIFICATION')
);
const token = getToken();
let sequence = 0;
let candidateSequence = 0;
let disposed = false;
const status = (e: unknown) => Number((e as { httpStatus?: number })?.httpStatus || 0);
const message = (e: unknown) =>
  (e as { msg?: string; message?: string })?.msg ||
  (e as { message?: string })?.message ||
  '操作未完成';
function current() {
  if (disposed) return false;
  if (getToken() === token) return true;
  sequence++;
  candidateSequence++;
  rows.value = [];
  candidates.value = [];
  selected.value = null;
  pending.value = null;
  open.value = false;
  createOpen.value = false;
  form.note = '';
  review.note = '';
  error.value = '登录已变化，请重新进入核验记录';
  return false;
}
async function list() {
  if (!current() || !permission('list')) return;
  const run = ++sequence;
  loading.value = true;
  error.value = '';
  const params = Object.fromEntries(Object.entries(filter).filter(([, v]) => v !== ''));
  try {
    const result = await api.listManual(params);
    if (!current() || run !== sequence) return;
    if (result.code !== 200) throw new Error('核验列表响应尚未确认');
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
  if (!current() || busy.value || !permission('query')) return;
  const run = ++sequence,
    saved = { ...review };
  selected.value = null;
  open.value = true;
  loading.value = true;
  error.value = '';
  if (!preserve) {
    review.note = '';
    review.decision = 'CONFIRM';
  }
  try {
    const value = await api.getManual(id);
    if (!current() || run !== sequence) return;
    if (value.recordId !== id) throw new Error('核验记录目标不一致');
    selected.value = value;
    stale.value = false;
    if (preserve) Object.assign(review, saved);
  } catch (e) {
    if (current() && run === sequence) {
      error.value = message(e);
      if ([401, 403, 404].includes(status(e))) {
        selected.value = null;
        review.note = '';
        open.value = false;
      }
    }
  } finally {
    if (current() && run === sequence) loading.value = false;
  }
}
async function loadCandidates(more = false) {
  if (!current() || !canCreate.value || candidateLoading.value) return;
  const run = ++candidateSequence;
  candidateLoading.value = true;
  if (!more) {
    candidates.value = [];
    candidatePage.value = 1;
    chosen.value = '';
  }
  try {
    const result = await api.manualApplications({ pageNum: candidatePage.value, pageSize: 30 });
    if (!current() || run !== candidateSequence) return;
    if (result.code !== 200) throw new Error('候选申请响应尚未确认');
    candidates.value = more ? [...candidates.value, ...result.rows] : result.rows;
    candidateTotal.value = result.total;
    candidatePage.value++;
    stale.value = false;
  } catch (e) {
    if (current() && run === candidateSequence) {
      error.value = message(e);
      candidates.value = [];
      chosen.value = '';
    }
  } finally {
    if (current() && run === candidateSequence) candidateLoading.value = false;
  }
}
async function newRecord() {
  if (locked.value || !canCreate.value) return;
  createOpen.value = true;
  chosen.value = '';
  form.note = '';
  form.type = 'BACKGROUND';
  form.conclusion = 'PASSED';
  error.value = '';
  stale.value = false;
  await loadCandidates();
}
async function execute(intent: Intent) {
  if (!current() || busy.value) return;
  busy.value = true;
  error.value = '';
  pending.value = intent;
  try {
    const value =
      intent.action === 'CREATE'
        ? await api.createManual(intent.body, intent.key)
        : await api.reviewManual(intent.id, intent.body, intent.key);
    if (!current()) return;
    pending.value = null;
    selected.value = value;
    createOpen.value = false;
    open.value = true;
    stale.value = false;
    review.note = '';
    form.note = '';
    ElMessage.success(
      intent.action === 'CREATE'
        ? '经办结论已登记，等待另一位有权限的人员复核'
        : intent.body.decision === 'CONFIRM'
          ? '已复核确认本项经办结论'
          : '已退回，未生成核验通过证明'
    );
    await list();
  } catch (e) {
    if (!current()) return;
    error.value = message(e);
    if (status(e) > 0 && status(e) < 500) pending.value = null;
    stale.value = status(e) === 409;
    if ([401, 403, 404].includes(status(e))) {
      selected.value = null;
      candidates.value = [];
      chosen.value = '';
      form.note = '';
      review.note = '';
      open.value = false;
      createOpen.value = false;
    }
  } finally {
    if (current()) busy.value = false;
  }
}
function submit() {
  if (locked.value || stale.value || !canCreate.value) return;
  const app = candidates.value.find(
    (item: api.ManualApplication) => item.applicationId === chosen.value
  );
  if (!app) {
    error.value = '请选择已授权的当前申请；申请版本不能手工填写';
    return;
  }
  // 版本与归属取自已授权的当前候选投影；不提供手填版本、公司或证明字段的捷径。
  const body: api.CreateManual = {
    applicationId: app.applicationId,
    expectedApplicationVersion: app.version,
    expectedCompanyRevision: app.companyRevision,
    expectedMaterialRevision: app.materialRevision,
    type: form.type,
    conclusion: form.conclusion,
    note: form.note.trim()
  };
  execute({ action: 'CREATE', id: app.applicationId, body, key: intentKey() });
}
function submitReview() {
  const value = selected.value;
  if (!value || locked.value || stale.value || !canReview.value) return;
  // 确认采纳经办原结论，退回不改写经办事实；是否允许复核由服务端动作和权限再次判断。
  execute({
    action: 'REVIEW',
    id: value.recordId,
    body: { expectedVersion: value.version, decision: review.decision, note: review.note.trim() },
    key: intentKey()
  });
}
onMounted(list);
onBeforeUnmount(() => {
  disposed = true;
  sequence++;
  candidateSequence++;
  pending.value = null;
  selected.value = null;
  candidates.value = [];
  form.note = '';
  review.note = '';
});
</script>
<template>
  <section class="manual-records">
    <el-alert
      :closable="false"
      type="info"
      title="无需上传凭证。经办结论登记后，由同公司另一位有权限的人员独立复核；复核人仍可参与最终资格审核。"
    />
    <div class="toolbar">
      <el-input
        v-model="filter.applicationId"
        placeholder="申请编号"
        clearable
        style="max-width: 180px"
      /><el-select v-model="filter.type" clearable placeholder="核验项目" style="width: 160px"
        ><el-option value="BACKGROUND" label="背景核验" /><el-option
          value="LICENSE"
          label="驾驶证核验" /></el-select
      ><el-select v-model="filter.status" clearable placeholder="复核状态" style="width: 170px"
        ><el-option value="PENDING_REVIEW" label="待独立复核" /><el-option
          value="CONFIRMED"
          label="已复核确认" /><el-option value="RETURNED" label="已退回" /></el-select
      ><el-button
        :disabled="locked || !permission('list')"
        @click="
          filter.pageNum = 1;
          list();
        "
        >查询记录</el-button
      ><el-button v-if="canCreate" :disabled="locked" type="primary" @click="newRecord"
        >登记人工核验</el-button
      >
    </div>
    <p v-if="!permission('list')" class="muted">当前没有核验记录列表权限。</p>
    <el-table v-else :data="rows" v-loading="loading"
      ><el-table-column prop="recordId" label="记录编号" width="110" /><el-table-column
        prop="companyName"
        label="所属公司"
        min-width="130"
      /><el-table-column label="申请 / 昵称" min-width="180"
        ><template #default="s"
          >{{ s.row.applicationId }} · {{ s.row.applicantName || '昵称未设置' }}</template
        ></el-table-column
      ><el-table-column label="项目" min-width="130"
        ><template #default="s">{{ api.verificationLabel(s.row.type) }}</template></el-table-column
      ><el-table-column label="经办结论" min-width="110"
        ><template #default="s">{{
          api.verificationLabel(s.row.conclusion)
        }}</template></el-table-column
      ><el-table-column label="复核状态" min-width="130"
        ><template #default="s">{{
          api.verificationLabel(s.row.status)
        }}</template></el-table-column
      ><el-table-column label="操作" width="110"
        ><template #default="s"
          ><el-button
            v-if="permission('query') && s.row.allowedActions.includes('VIEW_MANUAL_VERIFICATION')"
            link
            type="primary"
            :disabled="busy"
            @click="detail(s.row.recordId)"
            >查看核验</el-button
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
      :title="`原${pending.action === 'CREATE' ? '登记申请' : '复核记录'} ${pending.id} 的结果尚未确认，原目标、正文和请求键已冻结。`"
      ><el-button :disabled="busy" @click="execute(pending!)"
        >重试原人工核验操作</el-button
      ></el-alert
    >
    <el-dialog v-model="createOpen" title="登记人工核验" width="620px">
      <p class="muted">
        请选择服务端返回的本人之外的当前申请。昵称仅用于辨认账号，不作为实名核验结果。
      </p>
      <el-form label-position="top" :disabled="locked || stale"
        ><el-form-item label="当前申请与公司"
          ><el-select
            v-model="chosen"
            filterable
            placeholder="选择已授权的当前申请"
            style="width: 100%"
            ><el-option
              v-for="app in candidates"
              :key="app.applicationId"
              :value="app.applicationId"
              :label="`${app.companyName} · 申请 ${app.applicationId} · ${app.applicantName || '昵称未设置'}`" /></el-select
          ><el-button
            v-if="candidates.length < candidateTotal"
            :disabled="candidateLoading"
            link
            @click="loadCandidates(true)"
            >加载更多候选申请</el-button
          ></el-form-item
        ><el-form-item label="核验项目"
          ><el-radio-group v-model="form.type"
            ><el-radio value="BACKGROUND">背景核验</el-radio
            ><el-radio value="LICENSE">驾驶证核验</el-radio></el-radio-group
          ></el-form-item
        ><el-form-item label="经办结论"
          ><el-radio-group v-model="form.conclusion"
            ><el-radio value="PASSED">通过</el-radio
            ><el-radio value="FAILED">不通过</el-radio></el-radio-group
          ></el-form-item
        ><el-form-item label="经办备注（可留空）"
          ><el-input
            v-model="form.note"
            type="textarea"
            maxlength="500"
            placeholder="普通核验备注，最多 500 字" /></el-form-item
      ></el-form>
      <el-alert
        v-if="stale"
        :closable="false"
        type="warning"
        title="申请上下文已变化，已保留项目、结论和备注，请重读候选申请后重新明确选择。"
      /><el-button v-if="stale" :disabled="locked" @click="loadCandidates(false)"
        >重读候选申请</el-button
      >
      <template #footer
        ><el-button @click="createOpen = false">关闭</el-button
        ><el-button type="primary" :disabled="locked || stale || !chosen" @click="submit"
          >确认登记并交独立复核</el-button
        ></template
      >
    </el-dialog>
    <el-dialog v-model="open" title="人工核验详情与复核" width="720px">
      <template v-if="selected">
        <el-descriptions :column="1" border
          ><el-descriptions-item label="记录 / 申请"
            >{{ selected.recordId }} / {{ selected.applicationId }}</el-descriptions-item
          ><el-descriptions-item label="公司 / 账号昵称"
            >{{ selected.companyName }} /
            {{ selected.applicantName || '昵称未设置' }}</el-descriptions-item
          ><el-descriptions-item label="材料上下文"
            >公司修订 {{ selected.companyRevision }} · 资料修订 {{ selected.materialRevision }} ·
            记录版本 {{ selected.version }}</el-descriptions-item
          ><el-descriptions-item label="核验项目">{{
            api.verificationLabel(selected.type)
          }}</el-descriptions-item
          ><el-descriptions-item label="经办结论">{{
            api.verificationLabel(selected.conclusion)
          }}</el-descriptions-item
          ><el-descriptions-item label="复核状态">{{
            api.verificationLabel(selected.status)
          }}</el-descriptions-item
          ><el-descriptions-item label="经办人员 / 时间"
            >{{ selected.submittedByName || selected.submittedByUserId }} /
            {{ api.verificationTime(selected.submittedAt) }}</el-descriptions-item
          ><el-descriptions-item label="经办备注">{{
            selected.note || '未填写'
          }}</el-descriptions-item
          ><el-descriptions-item label="复核人员 / 时间"
            >{{ selected.reviewedByName || selected.reviewedByUserId || '尚未复核' }} /
            {{ api.verificationTime(selected.reviewedAt) }}</el-descriptions-item
          ><el-descriptions-item label="复核备注">{{
            selected.reviewNote || '未填写'
          }}</el-descriptions-item></el-descriptions
        >
        <template v-if="canReview"
          ><p class="muted">确认将采纳上述经办结论；退回不产生核验证明。请独立核对后操作。</p>
          <el-form label-position="top" :disabled="locked || stale"
            ><el-form-item label="复核决定"
              ><el-radio-group v-model="review.decision"
                ><el-radio value="CONFIRM">确认经办结论</el-radio
                ><el-radio value="RETURN">退回经办</el-radio></el-radio-group
              ></el-form-item
            ><el-form-item label="复核备注（可留空）"
              ><el-input
                v-model="review.note"
                type="textarea"
                maxlength="500"
                placeholder="复核备注，最多 500 字" /></el-form-item></el-form
        ></template>
        <p v-else class="muted">
          当前记录无可执行的复核动作。经办人与复核人须不同，权限、申请与材料上下文由服务端判断。
        </p>
        <el-alert
          v-if="stale"
          type="warning"
          :closable="false"
          title="记录或申请上下文已变化，已保留复核填写，请重读后重新核对。"
        /><el-button v-if="stale" :disabled="locked" @click="detail(selected!.recordId, true)"
          >重读记录并保留复核填写</el-button
        >
      </template>
      <p v-else-if="loading">正在读取已授权核验记录…</p>
      <template #footer
        ><el-button @click="open = false">关闭</el-button
        ><el-button
          v-if="canReview"
          type="primary"
          :disabled="locked || stale"
          @click="submitReview"
          >提交独立复核决定</el-button
        ></template
      >
    </el-dialog>
  </section>
</template>
<style scoped>
.manual-records {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
