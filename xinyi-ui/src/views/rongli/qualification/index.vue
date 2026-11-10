<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { companyTree, type CompanyNode } from '@/api/rongli/driver';
import { intentKey } from '@/api/rongli/aftersale';
import * as api from '@/api/rongli/qualification';
import QualificationEvidence from './QualificationEvidence.vue';

type Form = {
  decision: api.AuditDecision | '';
  opinion: string;
  publicOpinion: string;
  rejectFields: api.MaterialField[];
  reviewDueAt: string;
};
type Intent = { id: string; key: string; body: api.QualificationAudit };
const blank = (): Form => ({
  decision: '',
  opinion: '',
  publicOpinion: '',
  rejectFields: [],
  reviewDueAt: ''
});
const form = reactive<Form>(blank());
const query = reactive({
  keyword: '',
  ownerDeptId: '',
  status: '',
  sort: 'submittedAtDesc',
  pageNum: 1,
  pageSize: 10
});
const rows = ref<api.QualificationSummary[]>([]),
  total = ref(0),
  loading = ref(false),
  error = ref('');
const detail = ref<api.QualificationDetail | null>(null),
  detailOpen = ref(false),
  detailLoading = ref(false),
  detailError = ref('');
const detailId = ref(''),
  busy = ref(false),
  stale = ref(false),
  companies = ref<CompanyNode[]>([]),
  directoryError = ref('');
// 内部意见不入存储。未知审核按申请冻结原四个版本、正文和UUID，关闭抽屉或换申请均不重指向。
const pending = reactive(new Map<string, Intent>());
const permission = (action: string) => checkPermi(['biz:driverqualification:' + action]);
const unknown = computed(() => pending.has(detailId.value));
const locked = computed(() => detailLoading.value || busy.value || unknown.value || stale.value);
const canAudit = computed(() =>
  Boolean(
    permission('audit') &&
      detail.value &&
      !detail.value.selfReviewDenied &&
      detail.value.allowedActions.includes('AUDIT_QUALIFICATION')
  )
);
const canRepair = computed(() =>
  Boolean(
    permission('audit') &&
      detail.value &&
      !detail.value.selfReviewDenied &&
      detail.value.allowedActions.includes('REPAIR_QUALIFICATION_REJECTION')
  )
);
const auditAvailable = computed(() => canAudit.value || canRepair.value);
const effect = computed(() => detail.value?.effectiveQualification);
// 审核动作也包含驳回；有审核权限不代表材料齐备，批准仍受服务端具体阻断约束。
const decisionAllowed = computed(() =>
  form.decision === 'REPAIR_REJECTION'
    ? canRepair.value
    : form.decision === 'APPROVE'
      ? canAudit.value && detail.value?.reviewBlockers.length === 0
      : form.decision === 'REJECT' && canAudit.value
);
const token = getToken();
let disposed = false,
  listSequence = 0,
  detailSequence = 0;
const status = (e: unknown) => Number((e as { httpStatus?: number })?.httpStatus || 0);
const message = (e: unknown) =>
  (e as { msg?: string; message?: string })?.msg ||
  (e as { message?: string })?.message ||
  '操作尚未完成';
function clearDetail() {
  detailSequence++;
  detail.value = null;
  detailId.value = '';
  detailLoading.value = false;
  stale.value = false;
  detailError.value = '';
  Object.assign(form, blank());
}
function sessionCurrent() {
  if (disposed) return false;
  if (getToken() === token) return true;
  listSequence++;
  clearDetail();
  detailOpen.value = false;
  pending.clear();
  rows.value = [];
  companies.value = [];
  error.value = '登录已变化，请重新进入资格审核';
  return false;
}
function current(id: string, run: number) {
  return sessionCurrent() && detailId.value === id && detailSequence === run;
}
function restore(intent: Intent) {
  Object.assign(form, {
    decision: intent.body.decision,
    opinion: intent.body.opinion,
    publicOpinion: intent.body.publicOpinion || '',
    rejectFields: [...intent.body.rejectFields],
    reviewDueAt: api.reviewDueInput(intent.body.reviewDueAt)
  });
}
async function list(reset = false) {
  if (!permission('list') || !sessionCurrent()) return;
  if (reset) query.pageNum = 1;
  const run = ++listSequence;
  loading.value = true;
  error.value = '';
  rows.value = [];
  const params = Object.fromEntries(Object.entries(query).filter(([, value]) => value !== ''));
  if (params.keyword) params.keyword = String(params.keyword).trim();
  if (params.ownerDeptId) params.ownerDeptId = String(params.ownerDeptId);
  try {
    const result = await api.listQualifications(params);
    if (!sessionCurrent() || run !== listSequence) return;
    rows.value = result.rows;
    total.value = result.total;
  } catch (e) {
    if (sessionCurrent() && run === listSequence) {
      error.value = message(e);
      if ([401, 403, 404].includes(status(e))) {
        rows.value = [];
        total.value = 0;
      }
    }
  } finally {
    if (sessionCurrent() && run === listSequence) loading.value = false;
  }
}
async function show(id: string, preserve = false) {
  if (!permission('query') || !sessionCurrent() || busy.value) return;
  const saved: Form = { ...form, rejectFields: [...form.rejectFields] };
  clearDetail();
  detailId.value = id;
  detailOpen.value = true;
  detailLoading.value = true;
  const run = detailSequence;
  try {
    const value = await api.getQualification(id);
    if (!current(id, run)) return;
    detail.value = value;
    const original = pending.get(id);
    if (original) restore(original);
    else if (preserve) Object.assign(form, saved);
    else if (!canAudit.value && canRepair.value) form.decision = 'REPAIR_REJECTION';
  } catch (e) {
    if (current(id, run)) {
      detailError.value = message(e);
      if ([401, 403, 404].includes(status(e))) {
        pending.delete(id);
        detail.value = null;
        Object.assign(form, blank());
      }
    }
  } finally {
    if (current(id, run)) detailLoading.value = false;
  }
}
function filePermissionLost(value: string) {
  detail.value = null;
  Object.assign(form, blank());
  stale.value = true;
  detailError.value = value;
}
function makeBody(): api.QualificationAudit {
  const target = detail.value;
  if (
    !target ||
    target.profileVersion === null ||
    target.companyRevision === null ||
    !decisionAllowed.value
  )
    throw new Error('当前没有可执行的审核动作，请重读申请');
  const opinion = form.opinion.trim(),
    publicOpinion = form.publicOpinion.trim();
  if (!opinion) throw new Error('请填写本次内部审核意见');
  if (opinion.length > 1000 || publicOpinion.length > 1000) throw new Error('审核意见最多 1000 字');
  const approval = form.decision === 'APPROVE';
  if (!approval && !publicOpinion) throw new Error('驳回或历史修复须填写申请人可见的具体补正说明');
  if (!approval && !form.rejectFields.length) throw new Error('请明确选择允许补正的具体材料字段');
  const known = new Set(api.materialFields.map((item) => item.field));
  if (form.rejectFields.some((field: api.MaterialField) => !known.has(field)))
    throw new Error('补正字段不在允许范围');
  return {
    expectedVersion: target.version,
    expectedProfileVersion: target.profileVersion,
    expectedSubmissionRevision: target.submissionRevision,
    expectedCompanyRevision: target.companyRevision,
    decision: form.decision as api.AuditDecision,
    opinion,
    publicOpinion: publicOpinion || null,
    rejectFields: approval ? [] : [...new Set<api.MaterialField>(form.rejectFields)],
    reviewDueAt: approval ? api.reviewDueInstant(form.reviewDueAt) : null
  };
}
async function execute(intent: Intent) {
  if (!sessionCurrent() || busy.value) return;
  busy.value = true;
  error.value = '';
  detailError.value = '';
  pending.set(intent.id, intent);
  try {
    const value = await api.auditQualification(intent.id, intent.body, intent.key);
    if (!sessionCurrent()) return;
    pending.delete(intent.id);
    if (detailId.value === intent.id) {
      detail.value = value;
      stale.value = false;
      Object.assign(form, blank());
    }
    ElMessage.success(`申请 ${intent.id} 的${api.qualificationLabel(intent.body.decision)}已确认`);
    await list();
  } catch (e) {
    if (!sessionCurrent()) return;
    const code = status(e);
    if (code > 0 && code < 500) pending.delete(intent.id);
    if (detailId.value === intent.id) {
      detailError.value = message(e);
      if (code === 409) stale.value = true;
      if ([401, 403, 404].includes(code)) {
        detail.value = null;
        Object.assign(form, blank());
      }
    } else error.value = message(e);
  } finally {
    if (sessionCurrent()) busy.value = false;
  }
}
function submit() {
  if (locked.value || !auditAvailable.value || !sessionCurrent()) return;
  try {
    const body = makeBody();
    execute({ id: detail.value!.applicationId, body, key: intentKey() });
  } catch (e) {
    detailError.value = message(e);
  }
}
function retry(id: string) {
  const original = pending.get(id);
  if (original && !busy.value) execute(original);
}
onMounted(async () => {
  if (permission('list') && checkPermi(['system:dept:list'])) {
    try {
      const result = await companyTree();
      if (sessionCurrent()) companies.value = result.data;
    } catch (e) {
      if (sessionCurrent()) directoryError.value = message(e);
    }
  }
  await list();
});
onBeforeUnmount(() => {
  clearDetail();
  pending.clear();
  rows.value = [];
  disposed = true;
});
</script>
<template>
  <div class="app-container qualification-page">
    <h2>代驾资格审核</h2>
    <p class="description">
      本次提交与当前生效资格分别核对。本机认证只确认设备使用者；背景、驾驶证人工经办或复核人员可参与最终资格审核，申请人不能审核自己。
    </p>
    <el-alert
      type="info"
      :closable="false"
      title="批准不自动开启听单或新单准入；证件与复审到期后限制新单，历史服务和车辆保管责任继续保留。"
    />
    <div class="toolbar filters">
      <el-input
        v-model="query.keyword"
        maxlength="100"
        clearable
        placeholder="账号昵称或申请编号"
      />
      <el-tree-select
        v-if="checkPermi(['system:dept:list'])"
        v-model="query.ownerDeptId"
        :data="companies"
        :props="{ label: 'label', children: 'children' }"
        node-key="id"
        check-strictly
        clearable
        filterable
        :disabled="!companies.length"
        placeholder="原归属公司（可选）"
      />
      <el-select v-model="query.status" clearable placeholder="全部申请状态"
        ><el-option
          v-for="value in ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'EXPIRED', 'SUSPENDED']"
          :key="value"
          :value="value"
          :label="api.qualificationLabel(value)"
      /></el-select>
      <el-select v-model="query.sort" aria-label="申请排序"
        ><el-option value="submittedAtDesc" label="最近提交优先" /><el-option
          value="idDesc"
          label="最新申请优先"
      /></el-select>
      <el-button
        type="primary"
        :disabled="loading || busy || !permission('list')"
        @click="list(true)"
        >查询资格申请</el-button
      >
    </div>
    <p v-if="directoryError" class="muted">
      公司筛选暂不可用：{{ directoryError }}。仍按服务端真实权限读取申请，不默认账号主部门。
    </p>
    <p v-if="!permission('list')" class="muted">当前没有资格审核列表权限。</p>
    <el-table v-else :data="rows" v-loading="loading">
      <el-table-column prop="applicationId" label="申请编号" width="110" />
      <el-table-column label="账号昵称" min-width="130"
        ><template #default="s">{{
          s.row.applicantName || '昵称未设置'
        }}</template></el-table-column
      >
      <el-table-column label="原提交公司" min-width="150"
        ><template #default="s">{{ s.row.companyName || '尚未归属' }}</template></el-table-column
      >
      <el-table-column label="本次申请" min-width="130"
        ><template #default="s">{{
          api.qualificationLabel(s.row.status)
        }}</template></el-table-column
      >
      <el-table-column label="生效档案状态" min-width="140"
        ><template #default="s">{{
          s.row.profileVersion === null
            ? '当前公司范围未授权'
            : api.qualificationLabel(s.row.effectiveQualificationStatus)
        }}</template></el-table-column
      >
      <el-table-column label="提交时间" min-width="175"
        ><template #default="s">{{
          api.qualificationTime(s.row.submittedAt)
        }}</template></el-table-column
      >
      <el-table-column label="操作" width="130" fixed="right"
        ><template #default="s"
          ><el-button
            v-if="permission('query') && s.row.allowedActions.includes('VIEW_QUALIFICATION')"
            link
            type="primary"
            :disabled="busy"
            @click="show(s.row.applicationId)"
            >查看与审核</el-button
          ></template
        ></el-table-column
      >
    </el-table>
    <el-pagination
      v-if="permission('list')"
      v-model:current-page="query.pageNum"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="list(false)"
    />
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <el-alert
      v-for="intent in [...pending.values()]"
      :key="intent.id"
      type="warning"
      :closable="false"
      :title="`申请 ${intent.id} 的原审核结果尚未确认；原目标、决定、四个版本和请求键已冻结。`"
      ><el-button :disabled="busy" @click="retry(intent.id)"
        >恢复申请 {{ intent.id }} 的原审核操作</el-button
      ></el-alert
    >
    <el-drawer
      v-model="detailOpen"
      title="代驾资格申请详情与审核"
      size="min(1100px, 100vw)"
      destroy-on-close
      @close="clearDetail"
    >
      <p v-if="detailLoading" class="muted">正在读取授权范围内的冻结提交和生效资格…</p>
      <p v-if="detailError" class="error" role="alert">{{ detailError }}</p>
      <template v-if="detail">
        <div class="identity-line">
          <h3>申请 {{ detail.applicationId }} · {{ detail.applicantName || '昵称未设置' }}</h3>
          <span>{{ api.qualificationLabel(detail.status) }}</span>
        </div>
        <p class="muted">账号昵称仅用于辨认申请人，不作为证件实名核验结论。</p>
        <div class="snapshot-grid">
          <el-card class="submitted-card"
            ><template #header>本次冻结提交</template
            ><el-descriptions :column="1" border>
              <el-descriptions-item label="原提交公司">{{
                detail.companyName || '历史归属未确定'
              }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{
                api.qualificationTime(detail.submittedAt)
              }}</el-descriptions-item>
              <el-descriptions-item label="提交 / 材料修订"
                >{{ detail.submissionRevision }} /
                {{ detail.materialRevision }}</el-descriptions-item
              >
              <el-descriptions-item label="申请 / 档案版本"
                >{{ detail.version }} /
                {{
                  detail.profileVersion === null ? '未授权' : detail.profileVersion
                }}</el-descriptions-item
              >
              <el-descriptions-item label="提交可信性">{{
                detail.hasTrustedSubmission ? '已认证冻结快照' : '无可信提交，不可据此批准'
              }}</el-descriptions-item>
            </el-descriptions>
            <p v-if="!detail.hasTrustedSubmission" class="muted">
              草稿及缺认证结构的历史记录不反显未认证材料，也不推测核验通过。
            </p></el-card
          >
          <el-card class="effective-card"
            ><template #header>当前生效资格</template
            ><template v-if="effect"
              ><el-descriptions :column="1" border>
                <el-descriptions-item label="资格修订">{{
                  effect.qualificationRevision
                }}</el-descriptions-item>
                <el-descriptions-item label="当前有效状态">{{
                  api.qualificationLabel(effect.effectiveStatus)
                }}</el-descriptions-item>
                <el-descriptions-item label="准驾车型">{{
                  effect.licenseClass
                }}</el-descriptions-item>
                <el-descriptions-item label="证件有效期">{{
                  effect.licenseLongTerm ? '长期有效' : effect.licenseValidTo || '未明确'
                }}</el-descriptions-item>
                <el-descriptions-item label="复审截止（北京时间）">{{
                  api.qualificationTime(effect.reviewDueAt)
                }}</el-descriptions-item>
                <el-descriptions-item label="资格到期时刻">{{
                  api.qualificationTime(effect.expiresAt)
                }}</el-descriptions-item>
                <el-descriptions-item label="批准时间">{{
                  api.qualificationTime(effect.approvedAt)
                }}</el-descriptions-item>
              </el-descriptions></template
            >
            <p v-else-if="detail.profileVersion === null" class="muted">
              当前公司资格不在本次查询授权范围，未展示其生效快照。
            </p>
            <p v-else class="muted">
              当前授权范围内没有可认证的生效快照。不能把历史状态或本次草稿当作有效资格。
            </p>
            <p class="muted">
              本次复审的草稿、驳回或待审不覆盖原生效快照；新单准入另行实时判断。
            </p></el-card
          >
        </div>
        <el-card v-if="detail.draft" class="materials-card"
          ><template #header>冻结提交材料摘要</template
          ><el-descriptions :column="1" border>
            <el-descriptions-item label="填写姓名">{{
              detail.draft.realName || '暂无'
            }}</el-descriptions-item>
            <el-descriptions-item label="身份证摘要">{{
              detail.draft.idCardNoMask ||
              (detail.draft.idCardNoProvided ? '已提供，号码不回显' : '未提供')
            }}</el-descriptions-item>
            <el-descriptions-item label="准驾车型">{{
              detail.draft.licenseClass || '暂无'
            }}</el-descriptions-item>
            <el-descriptions-item label="初领日期">{{
              detail.draft.firstIssueDate || '暂无'
            }}</el-descriptions-item>
            <el-descriptions-item label="驾驶证有效期"
              >{{ detail.draft.validFrom || '暂无' }} 至
              {{
                detail.draft.longTermValid ? '长期有效' : detail.draft.validTo || '暂无'
              }}</el-descriptions-item
            >
            <el-descriptions-item label="申请同意"
              >{{ detail.draft.consentVersion || '暂无' }} ·
              {{ api.qualificationTime(detail.draft.consentAcceptedAt) }}</el-descriptions-item
            >
          </el-descriptions></el-card
        >
        <el-card class="proof-card"
          ><template #header>本次提交冻结的四项证明</template>
          <div v-for="check in detail.checks" :key="check.type" class="proof-row">
            <strong>{{ api.qualificationLabel(check.type) }}</strong
            ><span>{{ api.qualificationLabel(check.status) }}</span>
          </div>
          <p class="muted">
            设备认证只认证提交时原登录会话对应事实，不要求申请人此刻仍在线。后台不会重新生成本机挑战。
          </p>
          <p v-for="(blocker, index) in detail.reviewBlockers" :key="index" class="error">
            {{ blocker.message }}
          </p></el-card
        >
        <el-card class="evidence-card"
          ><QualificationEvidence
            :key="detail.applicationId + ':' + detail.submissionId"
            :detail="detail"
            @permission-lost="filePermissionLost"
        /></el-card>
        <el-card class="review-history"
          ><template #header>审核历史（公开意见）</template>
          <p v-if="!detail.reviews.length" class="muted">暂无可信审核记录。</p>
          <div v-for="review in detail.reviews" :key="review.reviewId" class="history-row">
            <strong
              >{{ api.qualificationLabel(review.decision) }} · 提交修订
              {{ review.submissionRevision }}</strong
            >
            <p class="muted">
              {{ api.qualificationTime(review.reviewedAt) }} · 审核人员编号
              {{ review.reviewerUserId }}
            </p>
            <p>{{ review.publicOpinion || '未填写公开意见' }}</p>
            <p v-if="review.rejectFields.length" class="muted">
              允许补正：{{ review.rejectFields.map(api.fieldLabel).join('、') }}
            </p>
          </div>
          <p class="muted">内部审核意见不通过查询回显；下面输入仅用于本次提交。</p></el-card
        >
        <el-alert
          v-if="detail.selfReviewDenied"
          type="warning"
          :closable="false"
          title="该申请与当前审核人存在本人身份关联，不能审核或修复自己的申请。"
        />
        <el-alert
          v-if="stale"
          type="warning"
          :closable="false"
          title="版本或提交上下文已变化，已保留填写。请重读后重新核对，不能直接提交旧版本。"
        />
        <el-button
          v-if="stale"
          :disabled="busy || unknown"
          @click="show(detail!.applicationId, true)"
          >重读申请并保留填写</el-button
        >
        <el-card v-if="auditAvailable || unknown" class="audit-card"
          ><template #header>本次资格审核决定</template>
          <el-form label-position="top" :disabled="locked">
            <el-form-item label="审核决定（明确选择）"
              ><el-radio-group v-model="form.decision"
                ><el-radio
                  v-if="canAudit"
                  value="APPROVE"
                  :disabled="Boolean(detail.reviewBlockers.length)"
                  >批准资格</el-radio
                ><el-radio v-if="canAudit" value="REJECT">驳回并指定补正</el-radio
                ><el-radio v-if="canRepair" value="REPAIR_REJECTION"
                  >仅修复历史补正范围</el-radio
                ></el-radio-group
              ></el-form-item
            >
            <el-form-item
              v-if="form.decision === 'APPROVE'"
              label="复审截止（北京时间，必填且须未来时刻）"
              ><el-date-picker
                v-model="form.reviewDueAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择北京时间复审截止，必填"
                style="width: 100%"
              />
              <p class="muted">
                不预填一年或永久有效。服务器还会核对冻结证件与公司、材料和提交版本。
              </p></el-form-item
            >
            <template v-if="form.decision === 'REJECT' || form.decision === 'REPAIR_REJECTION'"
              ><el-alert
                v-if="form.decision === 'REPAIR_REJECTION'"
                :closable="false"
                type="warning"
                title="只补齐历史驳回的补正范围和公开说明，不批准资格或伪造历史证明。"
              /><el-form-item label="允许补正的精确材料字段（至少一项）"
                ><el-checkbox-group v-model="form.rejectFields" class="correction-fields"
                  ><el-checkbox
                    v-for="field in api.materialFields"
                    :key="field.field"
                    :value="field.field"
                    >{{ field.label }}</el-checkbox
                  ></el-checkbox-group
                ></el-form-item
              ></template
            >
            <el-form-item label="内部审核意见（必填，不向申请人公开）"
              ><el-input
                v-model="form.opinion"
                maxlength="1000"
                show-word-limit
                type="textarea"
                :rows="3"
                placeholder="填写本次内部审核依据，最多 1000 字"
            /></el-form-item>
            <el-form-item
              :label="
                form.decision === 'APPROVE'
                  ? '申请人可见意见（可留空）'
                  : '申请人可见补正说明（必填）'
              "
              ><el-input
                v-model="form.publicOpinion"
                maxlength="1000"
                show-word-limit
                type="textarea"
                :rows="3"
                placeholder="只填写适合申请人查看的说明，最多 1000 字"
            /></el-form-item>
          </el-form>
          <p class="decision-consequence">
            作用对象：申请 {{ detail.applicationId }}，提交修订 {{ detail.submissionRevision }}。{{
              form.decision === 'APPROVE'
                ? '批准后生成新的生效资格快照；不自动开启听单。'
                : form.decision === 'REPAIR_REJECTION'
                  ? '保持驳回状态，仅允许指定字段补正。'
                  : form.decision === 'REJECT'
                    ? '保留审核历史，仅开放指定字段补正。'
                    : '请先明确选择本次决定。'
            }}
          </p>
          <el-button
            v-if="!unknown"
            type="primary"
            :disabled="locked || !decisionAllowed"
            @click="submit"
            >提交本次审核决定</el-button
          ><el-button v-else type="warning" :disabled="busy" @click="retry(detail.applicationId)"
            >重试本申请原审核操作</el-button
          >
        </el-card>
        <p v-else class="muted">
          当前没有可执行的审核动作，以服务端状态、本人隔离及原公司权限为准。
        </p>
      </template>
    </el-drawer>
  </div>
</template>
<style scoped>
.qualification-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
h2,
h3 {
  margin: 0;
}
.description,
.muted {
  color: #606266;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.description {
  margin: 0;
}
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.filters :deep(.el-input),
.filters :deep(.el-select),
.filters :deep(.el-tree-select) {
  width: 210px;
}
.snapshot-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin: 16px 0;
}
.identity-line,
.proof-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.proof-row {
  margin: 12px 0;
}
.error {
  color: #b42318;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.materials-card,
.proof-card,
.evidence-card,
.review-history,
.audit-card {
  margin: 16px 0;
}
.history-row {
  border-bottom: 1px solid #ebeef5;
  padding: 12px 0;
  line-height: 1.7;
}
.history-row p {
  margin: 8px 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
.correction-fields {
  display: flex;
  gap: 4px 14px;
  flex-wrap: wrap;
}
.decision-consequence {
  padding: 12px;
  background: #f0f7ff;
  line-height: 1.8;
  border-radius: 8px;
}
:deep(.el-descriptions__content) {
  overflow-wrap: anywhere;
}
:deep(.el-drawer__body) {
  min-width: 0;
}
@media (max-width: 720px) {
  .snapshot-grid {
    grid-template-columns: 1fr;
  }
  .filters :deep(.el-input),
  .filters :deep(.el-select),
  .filters :deep(.el-tree-select) {
    width: 100%;
  }
}
</style>
