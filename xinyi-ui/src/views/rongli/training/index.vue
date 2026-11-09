<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import { companyTree, type CompanyNode } from '@/api/rongli/driver';
import { readTrainingMedia, uploadTrainingMedia } from '@/api/rongli/privateFile';
import {
  createTraining,
  getTraining,
  listTraining,
  publishTraining,
  saveTraining,
  type CreateTraining,
  type PublishTraining,
  type SaveTraining,
  type TrainingDetail,
  type TrainingQuestion,
  type QuestionOption,
  type TrainingSummary
} from '@/api/rongli/training';

type Intent =
  | { action: 'CREATE' | 'FORK'; key: string; body: CreateTraining }
  | { action: 'SAVE'; key: string; body: SaveTraining }
  | { action: 'PUBLISH'; key: string; body: PublishTraining };
type EditorForm = Omit<SaveTraining, 'expectedVersion' | 'reason'>;
const emptyForm = (): EditorForm => ({
  title: '',
  description: null,
  mediaFileId: null,
  requiredDurationSeconds: null,
  passScore: null,
  attemptTimeLimitSeconds: null,
  questions: []
});
const query = reactive({ keyword: '', status: '', ownerDeptId: '', sort: 'idDesc' });
const rows = ref<TrainingSummary[]>([]),
  total = ref(0),
  page = ref(1),
  loading = ref(false),
  listError = ref('');
const companies = ref<CompanyNode[]>([]),
  companyError = ref('');
const detail = ref<TrainingDetail | null>(null),
  editorOpen = ref(false),
  editorLoading = ref(false);
const editorError = ref(''),
  editorBusy = ref(false),
  editorStale = ref(false),
  editorUnknown = ref(false);
const form = reactive<EditorForm>(emptyForm()),
  baseline = ref(''),
  saveReason = ref('');
const uploading = ref(false),
  previewBusy = ref(false),
  videoUrl = ref(''),
  uploadName = ref('');
const fileInput = ref<HTMLInputElement | null>(null);
const actionOpen = ref(false),
  actionKind = ref<'CREATE' | 'FORK' | 'PUBLISH'>('CREATE');
const actionRow = ref<TrainingDetail | null>(null),
  actionBusy = ref(false),
  actionLoading = ref(false);
const actionError = ref(''),
  actionUnknown = ref(false),
  actionStale = ref(false);
const actionForm = reactive({ title: '', ownerDeptId: '', reason: '' });
// 同一课程和动作的未知写入保留原请求；重开弹窗也不能用新键重复创建或发布。
const pending = new Map<string, Intent>();
let listSequence = 0,
  editorSequence = 0,
  actionSequence = 0;
const login = getToken();
const wideScreen = ref(window.innerWidth >= 800);
const resize = () => {
  wideScreen.value = window.innerWidth >= 800;
};
const copy = <T,>(value: T): T => JSON.parse(JSON.stringify(value));
const dirty = computed(() => JSON.stringify(form) !== baseline.value);
const editorLocked = computed(
  () => editorBusy.value || editorLoading.value || editorUnknown.value || uploading.value
);
const canEdit = computed(() =>
  Boolean(detail.value && can(detail.value, 'EDIT_TRAINING_COURSE', 'edit'))
);
const hasRowActions = computed(() => {
  const records: TrainingSummary[] = rows.value;
  return records.some(
    (row) =>
      can(row, 'VIEW_TRAINING_COURSE', 'query') ||
      can(row, 'PUBLISH_TRAINING_COURSE', 'publish') ||
      can(row, 'CREATE_TRAINING_REVISION', 'create')
  );
});
const actionTitle = computed(() => {
  const kind: unknown = actionKind.value;
  return kind === 'FORK'
    ? '派生新的课程草稿'
    : kind === 'PUBLISH'
      ? '发布不可变课程版本'
      : '新建公司培训课程';
});
const statusLabel = (value: string) =>
  (({ DRAFT: '草稿', PUBLISHED: '已发布', DISABLED: '已停用' }) as Record<string, string>)[value] ||
  value;
const time = (value: string | null) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    : '尚未发布';
function can(row: TrainingSummary, action: string, permission: string) {
  // 按钮只收窄交互；公司数据范围和最终写入权限仍由服务端重新核对。
  return row.allowedActions.includes(action) && checkPermi(['biz:trainingcourse:' + permission]);
}
function current() {
  if (!login || getToken() !== login) throw new Error('登录身份已变化，请重新进入课程管理');
}
function clearVideo() {
  if (videoUrl.value) URL.revokeObjectURL(videoUrl.value);
  videoUrl.value = '';
}
function applyDetail(value: TrainingDetail, preserve = false) {
  const previous = preserve ? copy(form) : null;
  detail.value = value;
  Object.assign(form, {
    title: value.title,
    description: value.description,
    mediaFileId: value.mediaFileId,
    requiredDurationSeconds: value.requiredDurationSeconds,
    passScore: value.passScore,
    attemptTimeLimitSeconds: value.attemptTimeLimitSeconds,
    questions: copy(value.questions)
  });
  baseline.value = JSON.stringify(form);
  if (previous) Object.assign(form, previous);
}
onMounted(() => {
  window.addEventListener('resize', resize);
  load();
  loadCompanies();
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize);
  listSequence++;
  editorSequence++;
  actionSequence++;
  pending.clear();
  clearVideo();
  detail.value = null;
  Object.assign(form, emptyForm());
});
async function load(reset = false) {
  if (reset) page.value = 1;
  const run = ++listSequence;
  loading.value = true;
  listError.value = '';
  try {
    current();
    const params: Record<string, string | number> = {
      pageNum: page.value,
      pageSize: 10,
      sort: query.sort
    };
    if (query.keyword.trim()) params.keyword = query.keyword.trim();
    if (query.status) params.status = query.status;
    if (query.ownerDeptId) params.ownerDeptId = query.ownerDeptId;
    const result = await listTraining(params);
    current();
    if (run === listSequence) {
      rows.value = result.rows;
      total.value = result.total;
    }
  } catch (failure: any) {
    if (run === listSequence) {
      rows.value = [];
      total.value = 0;
      listError.value = failure.msg || failure.message || '课程列表读取失败';
    }
  } finally {
    if (run === listSequence) loading.value = false;
  }
}
async function loadCompanies() {
  if (!checkPermi(['system:dept:list'])) {
    companyError.value = '选择公司需要部门列表权限，请联系管理员配置。';
    return;
  }
  try {
    const result = await companyTree();
    current();
    companies.value = result.data;
  } catch (failure: any) {
    companies.value = [];
    companyError.value = failure.msg || failure.message || '公司目录读取失败';
  }
}
function resetQuery() {
  Object.assign(query, { keyword: '', status: '', ownerDeptId: '', sort: 'idDesc' });
  load(true);
}
async function openEditor(id: string) {
  const run = ++editorSequence;
  detail.value = null;
  Object.assign(form, emptyForm());
  baseline.value = JSON.stringify(form);
  editorError.value = '';
  saveReason.value = '';
  uploadName.value = '';
  editorStale.value = false;
  editorUnknown.value = false;
  clearVideo();
  editorOpen.value = true;
  editorLoading.value = true;
  try {
    const result = await getTraining(id);
    current();
    if (run !== editorSequence) return;
    applyDetail(result.data);
    const saved = pending.get('SAVE:' + id);
    if (saved?.action === 'SAVE') {
      const { expectedVersion: _version, reason, ...values } = saved.body;
      Object.assign(form, copy(values));
      saveReason.value = reason;
      editorUnknown.value = true;
    }
  } catch (failure: any) {
    if (run === editorSequence)
      editorError.value = failure.msg || failure.message || '课程详情读取失败';
  } finally {
    if (run === editorSequence) editorLoading.value = false;
  }
}
async function refreshEditor() {
  if (!detail.value || editorBusy.value || uploading.value || editorLoading.value) return;
  const run = editorSequence,
    id = detail.value.trainingId;
  editorLoading.value = true;
  editorError.value = '';
  try {
    const result = await getTraining(id);
    current();
    if (run !== editorSequence) return;
    applyDetail(result.data, true);
    editorStale.value = false;
  } catch (failure: any) {
    if (run === editorSequence) {
      detail.value = null;
      Object.assign(form, emptyForm());
      baseline.value = JSON.stringify(form);
      saveReason.value = '';
      uploadName.value = '';
      clearVideo();
      editorError.value = failure.msg || failure.message || '详情不可读取，已清除原课程与标准答案';
      if (failure.httpStatus === 403 || failure.httpStatus === 404) pending.delete('SAVE:' + id);
      // 撤权后的在途预览也必须丢弃，不能晚到后再次显示原课程媒体。
      editorSequence++;
      editorLoading.value = false;
      previewBusy.value = false;
    }
  } finally {
    if (run === editorSequence) editorLoading.value = false;
  }
}
function addQuestion() {
  if (!canEdit.value || editorLocked.value || form.questions.length >= 200) return;
  form.questions.push({
    questionCode: 'Q_' + crypto.randomUUID().slice(0, 8),
    type: 'SINGLE',
    prompt: '',
    options: [
      { code: 'A', text: '' },
      { code: 'B', text: '' }
    ],
    correctOptionCodes: []
  });
}
function addOption(question: TrainingQuestion) {
  if (editorLocked.value || !canEdit.value || question.options.length >= 10) return;
  const codes = new Set(question.options.map((item) => item.code));
  const code =
    'ABCDEFGHIJ'.split('').find((item) => !codes.has(item)) ||
    'O_' + crypto.randomUUID().slice(0, 6);
  question.options.push({ code, text: '' });
}
function removeOption(question: TrainingQuestion, index: number) {
  if (editorLocked.value || !canEdit.value || question.options.length <= 2) return;
  const [removed] = question.options.splice(index, 1);
  question.correctOptionCodes = question.correctOptionCodes.filter((code) => code !== removed.code);
}
function singleAnswer(question: TrainingQuestion, value: unknown) {
  if (!editorLocked.value && canEdit.value && typeof value === 'string')
    question.correctOptionCodes = [value];
}
function changeQuestionType(question: TrainingQuestion, value: unknown) {
  if (
    editorLocked.value ||
    !canEdit.value ||
    !['SINGLE', 'MULTIPLE', 'TRUE_FALSE'].includes(String(value))
  )
    return;
  question.type = value as TrainingQuestion['type'];
  if (question.type !== 'MULTIPLE')
    question.correctOptionCodes = question.correctOptionCodes.slice(0, 1);
}
function changeOptionCode(question: TrainingQuestion, option: QuestionOption, value: unknown) {
  if (editorLocked.value || !canEdit.value || typeof value !== 'string') return;
  const old = option.code;
  option.code = value;
  // 选项编码可编辑时同步维护选中的标准答案，避免留下界面已无法取消的旧编码。
  question.correctOptionCodes = [
    ...new Set(question.correctOptionCodes.map((code) => (code === old ? value : code)))
  ];
}
function validateQuestions() {
  if (form.questions.length > 200) throw new Error('每门课程最多 200 道题');
  const questionCodes = new Set<string>();
  const questions: TrainingQuestion[] = form.questions;
  for (const [index, question] of questions.entries()) {
    const label = '第 ' + (index + 1) + ' 题：';
    if (
      !/^[A-Za-z0-9_-]{1,64}$/.test(question.questionCode) ||
      questionCodes.has(question.questionCode)
    )
      throw new Error(label + '题目编码须为唯一的 1 至 64 位字母、数字、下划线或短横线');
    questionCodes.add(question.questionCode);
    if (!question.prompt.trim() || question.prompt.trim().length > 2000)
      throw new Error(label + '请填写 1 至 2000 字题干');
    if (question.options.length < 2 || question.options.length > 10)
      throw new Error(label + '选项须为 2 至 10 项');
    const codes = new Set<string>();
    for (const option of question.options) {
      if (!/^[A-Za-z0-9_-]{1,32}$/.test(option.code) || codes.has(option.code))
        throw new Error(label + '选项编码须为唯一的 1 至 32 位字母、数字、下划线或短横线');
      if (!option.text.trim() || option.text.trim().length > 500)
        throw new Error(label + '请填写 1 至 500 字选项内容');
      codes.add(option.code);
    }
    const answers = question.correctOptionCodes;
    if (
      !answers.length ||
      new Set(answers).size !== answers.length ||
      answers.some((code) => !codes.has(code))
    )
      throw new Error(label + '请选择题目选项中的有效标准答案');
    if (question.type !== 'MULTIPLE' && answers.length !== 1)
      throw new Error(label + '单选和判断题须恰有一个正确答案');
    if (question.type === 'TRUE_FALSE' && question.options.length !== 2)
      throw new Error(label + '判断题须恰有两个选项');
  }
}
function buildSave(): SaveTraining {
  if (!detail.value) throw new Error('请先读取课程');
  if (!form.title.trim() || form.title.trim().length > 200)
    throw new Error('请填写 1 至 200 字课程标题');
  if ((form.description || '').length > 5000) throw new Error('课程说明最多 5000 字');
  if (!saveReason.value.trim() || saveReason.value.trim().length > 500)
    throw new Error('请填写 1 至 500 字保存原因');
  for (const [value, min, max, label] of [
    [form.requiredDurationSeconds, 1, 86400, '必学时长'],
    [form.passScore, 0, 100, '通过分数'],
    [form.attemptTimeLimitSeconds, 60, 86400, '考试限时']
  ] as Array<[number | null, number, number, string]>) {
    if (
      value !== null &&
      value !== undefined &&
      (!Number.isInteger(value) || value < min || value > max)
    )
      throw new Error(label + '须为 ' + min + ' 至 ' + max + ' 的整数');
  }
  validateQuestions();
  // 编辑为完整替换，尚未配置的门槛和教材明确传 null，不冒充已有正式培训标准。
  return {
    ...copy(form),
    title: form.title.trim(),
    description: form.description?.trim() || null,
    requiredDurationSeconds: form.requiredDurationSeconds ?? null,
    passScore: form.passScore ?? null,
    attemptTimeLimitSeconds: form.attemptTimeLimitSeconds ?? null,
    expectedVersion: detail.value.version,
    reason: saveReason.value.trim()
  };
}
async function save() {
  if (
    !detail.value ||
    !canEdit.value ||
    editorBusy.value ||
    editorLoading.value ||
    uploading.value ||
    editorStale.value
  )
    return;
  const id = detail.value.trainingId,
    key = 'SAVE:' + id,
    run = editorSequence;
  editorError.value = '';
  let intent = pending.get(key);
  try {
    if (!intent) intent = { action: 'SAVE', key: crypto.randomUUID(), body: buildSave() };
    if (intent.action !== 'SAVE') return;
    pending.set(key, intent);
    editorBusy.value = true;
    const result = await saveTraining(id, intent.body, intent.key);
    current();
    if (run !== editorSequence) return;
    pending.delete(key);
    editorUnknown.value = false;
    saveReason.value = '';
    uploadName.value = '';
    applyDetail(result.data);
    ElMessage.success('课程草稿已保存，尚未发布');
    await load();
  } catch (failure: any) {
    if (run !== editorSequence) return;
    editorError.value = failure.msg || failure.message || '保存结果尚未确认';
    if (failure.httpStatus && failure.httpStatus < 500) {
      pending.delete(key);
      editorUnknown.value = false;
      editorStale.value = failure.httpStatus === 409;
    } else if (intent) {
      pending.set(key, intent);
      editorUnknown.value = true;
    }
  } finally {
    if (run === editorSequence) editorBusy.value = false;
  }
}
async function upload(event: Event) {
  const selected = (event.target as HTMLInputElement).files?.[0];
  if (fileInput.value) fileInput.value.value = '';
  if (!selected || !detail.value || !canEdit.value || editorLocked.value) return;
  const run = editorSequence,
    id = detail.value.trainingId;
  uploading.value = true;
  editorError.value = '';
  try {
    const ready = await uploadTrainingMedia(id, selected);
    current();
    if (run !== editorSequence) return;
    clearVideo();
    form.mediaFileId = ready.fileId;
    uploadName.value = ready.name;
    ElMessage.success('视频已通过文件核验，请保存草稿以绑定本次教材');
  } catch (failure: any) {
    if (run === editorSequence)
      editorError.value = failure.msg || failure.message || '教材上传未完成';
  } finally {
    if (run === editorSequence) uploading.value = false;
  }
}
async function preview() {
  if (!form.mediaFileId || previewBusy.value) return;
  const run = editorSequence;
  previewBusy.value = true;
  editorError.value = '';
  clearVideo();
  try {
    const url = await readTrainingMedia(form.mediaFileId);
    if (run !== editorSequence || getToken() !== login) {
      URL.revokeObjectURL(url);
      current();
      return;
    }
    videoUrl.value = url;
  } catch (failure: any) {
    if (run === editorSequence)
      editorError.value = failure.msg || failure.message || '教材读取失败';
  } finally {
    if (run === editorSequence) previewBusy.value = false;
  }
}
async function closeEditor(done: () => void) {
  if (editorBusy.value || editorLoading.value || uploading.value) {
    ElMessage.warning('操作处理中，请等待回执');
    return;
  }
  if (dirty.value && !editorUnknown.value) {
    try {
      await ElMessageBox.confirm('关闭将丢弃未保存的课程和题目修改，是否继续？', '未保存的草稿');
    } catch {
      return;
    }
  }
  editorSequence++;
  detail.value = null;
  Object.assign(form, emptyForm());
  baseline.value = JSON.stringify(form);
  saveReason.value = '';
  uploadName.value = '';
  clearVideo();
  done();
}
async function openAction(kind: 'CREATE' | 'FORK' | 'PUBLISH', row?: TrainingSummary) {
  if (kind === 'PUBLISH' && editorOpen.value && dirty.value) {
    ElMessage.warning('请先保存课程修改，再核对发布');
    return;
  }
  const run = ++actionSequence;
  actionKind.value = kind;
  actionRow.value = null;
  actionOpen.value = true;
  actionError.value = '';
  actionStale.value = false;
  actionUnknown.value = false;
  Object.assign(actionForm, { title: '', ownerDeptId: '', reason: '' });
  const intent = pending.get(kind + ':' + (row?.trainingId || 'new'));
  if (intent) {
    actionUnknown.value = true;
    if (intent.action === 'CREATE' && 'title' in intent.body)
      Object.assign(actionForm, intent.body);
    if (intent.action === 'PUBLISH') actionForm.reason = intent.body.reason;
  }
  if (!row) return;
  // 发布和派生都重读真实源版本；禁止把列表旧版本或前端输入当作最新事实。
  actionLoading.value = true;
  try {
    const result = await getTraining(row.trainingId);
    current();
    if (run === actionSequence) actionRow.value = result.data;
  } catch (failure: any) {
    if (run === actionSequence)
      actionError.value = failure.msg || failure.message || '课程不可读取';
  } finally {
    if (run === actionSequence) actionLoading.value = false;
  }
}
async function refreshAction() {
  if (!actionRow.value || actionBusy.value) return;
  const run = actionSequence;
  actionLoading.value = true;
  try {
    const result = await getTraining(actionRow.value.trainingId);
    current();
    if (run === actionSequence) {
      actionRow.value = result.data;
      actionStale.value = false;
    }
  } catch (failure: any) {
    if (run === actionSequence) {
      actionRow.value = null;
      actionError.value = failure.msg || failure.message || '课程不可读取';
    }
  } finally {
    if (run === actionSequence) actionLoading.value = false;
  }
}
const actionAllowed = computed(() =>
  actionKind.value === 'CREATE'
    ? checkPermi(['biz:trainingcourse:create']) && companies.value.length > 0
    : Boolean(
        actionRow.value &&
          can(
            actionRow.value,
            actionKind.value === 'PUBLISH' ? 'PUBLISH_TRAINING_COURSE' : 'CREATE_TRAINING_REVISION',
            actionKind.value === 'PUBLISH' ? 'publish' : 'create'
          )
      )
);
async function submitAction() {
  if (!actionAllowed.value || actionBusy.value || actionLoading.value || actionStale.value) return;
  const kind = actionKind.value,
    id = actionRow.value?.trainingId || 'new',
    mapKey = kind + ':' + id,
    run = actionSequence;
  let intent = pending.get(mapKey);
  actionError.value = '';
  try {
    if (!intent) {
      if (kind === 'CREATE') {
        if (!/^[1-9][0-9]{0,18}$/.test(actionForm.ownerDeptId))
          throw new Error('请明确选择实际所属公司部门');
        if (!actionForm.title.trim() || actionForm.title.trim().length > 200)
          throw new Error('请填写 1 至 200 字课程标题');
        intent = {
          action: kind,
          key: crypto.randomUUID(),
          body: { ownerDeptId: actionForm.ownerDeptId, title: actionForm.title.trim() }
        };
      } else if (kind === 'FORK') {
        if (!actionRow.value) return;
        intent = {
          action: kind,
          key: crypto.randomUUID(),
          body: { sourceTrainingId: id, expectedSourceVersion: actionRow.value.version }
        };
      } else {
        if (!actionRow.value) return;
        if (!actionForm.reason.trim() || actionForm.reason.trim().length > 500)
          throw new Error('请填写 1 至 500 字发布原因');
        intent = {
          action: kind,
          key: crypto.randomUUID(),
          body: { expectedVersion: actionRow.value.version, reason: actionForm.reason.trim() }
        };
      }
    }
    pending.set(mapKey, intent);
    actionBusy.value = true;
    const result =
      intent.action === 'PUBLISH'
        ? await publishTraining(id, intent.body, intent.key)
        : intent.action === 'SAVE'
          ? null
          : await createTraining(intent.body, intent.key);
    current();
    if (run !== actionSequence || !result) return;
    pending.delete(mapKey);
    actionUnknown.value = false;
    actionOpen.value = false;
    ElMessage.success(kind === 'PUBLISH' ? '课程版本已发布，内容保持不可变' : '课程草稿已建立');
    await load();
    if (checkPermi(['biz:trainingcourse:query'])) await openEditor(result.data.trainingId);
  } catch (failure: any) {
    if (run !== actionSequence) return;
    actionError.value = failure.msg || failure.message || '操作结果尚未确认';
    if (failure.httpStatus && failure.httpStatus < 500) {
      pending.delete(mapKey);
      actionUnknown.value = false;
      actionStale.value = failure.httpStatus === 409;
    } else if (intent) {
      pending.set(mapKey, intent);
      actionUnknown.value = true;
    }
  } finally {
    if (run === actionSequence) actionBusy.value = false;
  }
}
function closeAction(done: () => void) {
  if (actionBusy.value || actionLoading.value) return;
  actionSequence++;
  actionRow.value = null;
  done();
}
</script>

<template>
  <div class="app-container training-page">
    <div class="heading">
      <div>
        <h2>培训课程与题库管理</h2>
        <p>各公司独立维护，旗下站点与公司代驾共用。发布内容版本不代表学员已完成培训或资格获批。</p>
      </div>
      <div class="toolbar">
        <el-button :loading="loading" @click="load()">刷新列表</el-button
        ><el-button
          v-if="checkPermi(['biz:trainingcourse:create'])"
          type="primary"
          @click="openAction('CREATE')"
          >新建课程草稿</el-button
        >
      </div>
    </div>
    <el-form class="filters" inline label-position="top" @submit.prevent="load(true)">
      <el-form-item label="课程标题"
        ><el-input v-model="query.keyword" maxlength="200" placeholder="按课程标题查询" clearable
      /></el-form-item>
      <el-form-item label="发布状态"
        ><el-select v-model="query.status" clearable placeholder="全部发布状态" style="width: 150px"
          ><el-option
            v-for="status in ['DRAFT', 'PUBLISHED', 'DISABLED']"
            :key="status"
            :value="status"
            :label="statusLabel(status)" /></el-select
      ></el-form-item>
      <el-form-item label="所属公司部门"
        ><el-tree-select
          v-model="query.ownerDeptId"
          :data="companies"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          check-strictly
          clearable
          placeholder="全部有权课程范围"
          style="width: 210px"
          :disabled="!companies.length"
      /></el-form-item>
      <el-form-item label="排序"
        ><el-select v-model="query.sort" style="width: 150px"
          ><el-option label="课程编号倒序" value="idDesc" /><el-option
            label="课程标题升序"
            value="titleAsc" /></el-select
      ></el-form-item>
      <el-form-item label="操作"
        ><el-button type="primary" native-type="submit">查询</el-button
        ><el-button @click="resetQuery">重置</el-button></el-form-item
      >
    </el-form>
    <el-alert v-if="listError" type="error" :title="listError" :closable="false" show-icon />
    <el-table
      v-loading="loading"
      :data="rows"
      row-key="trainingId"
      empty-text="当前授权范围暂无培训课程"
    >
      <el-table-column label="课程" min-width="220"
        ><template #default="{ row }"
          ><strong>{{ row.title }}</strong
          ><small>{{ row.courseKey }}</small></template
        ></el-table-column
      >
      <el-table-column label="所属公司部门" min-width="170"
        ><template #default="{ row }">{{
          row.companyName || '历史归属待核实'
        }}</template></el-table-column
      >
      <el-table-column label="内容版本" width="100"
        ><template #default="{ row }">V{{ row.courseVersion }}</template></el-table-column
      >
      <el-table-column label="状态" width="110"
        ><template #default="{ row }"
          ><el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{
            statusLabel(row.status)
          }}</el-tag></template
        ></el-table-column
      >
      <el-table-column prop="questionCount" label="题数" width="80" />
      <el-table-column label="发布时间" min-width="180"
        ><template #default="{ row }">{{ time(row.publishedAt) }}</template></el-table-column
      >
      <el-table-column
        v-if="hasRowActions"
        label="操作"
        min-width="270"
        :fixed="wideScreen ? 'right' : false"
        ><template #default="{ row }"
          ><el-button
            v-if="can(row, 'VIEW_TRAINING_COURSE', 'query')"
            link
            type="primary"
            @click="openEditor(row.trainingId)"
            >查看课程</el-button
          ><el-button
            v-if="can(row, 'PUBLISH_TRAINING_COURSE', 'publish')"
            link
            type="success"
            @click="openAction('PUBLISH', row)"
            >发布</el-button
          ><el-button
            v-if="can(row, 'CREATE_TRAINING_REVISION', 'create')"
            link
            @click="openAction('FORK', row)"
            >派生新草稿</el-button
          ></template
        ></el-table-column
      >
    </el-table>
    <el-pagination
      class="pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="10"
      v-model:current-page="page"
      @current-change="load()"
    />

    <el-drawer
      v-model="editorOpen"
      title="课程与题库编辑"
      size="min(1100px, 100vw)"
      :before-close="closeEditor"
      destroy-on-close
    >
      <div v-loading="editorLoading" class="editor">
        <el-alert
          v-if="editorError"
          :title="editorError"
          type="error"
          :closable="false"
          show-icon
        />
        <template v-if="detail">
          <div class="heading">
            <div>
              <h3>{{ detail.title }}</h3>
              <p>
                {{ detail.companyName || '历史归属待核实' }} · 内容版本 V{{
                  detail.courseVersion
                }}
                · 记录版本 {{ detail.version }}
              </p>
              <p>课程编号 {{ detail.trainingId }} · {{ statusLabel(detail.status) }}</p>
            </div>
            <el-button :disabled="editorBusy || uploading" @click="refreshEditor"
              >重读当前课程</el-button
            >
          </div>
          <el-alert
            v-if="detail.status !== 'DRAFT'"
            title="该版本内容只读。修订须从已发布版本派生新草稿，旧题库和旧试卷版本继续保留。"
            type="info"
            :closable="false"
          />
          <el-form label-position="top" :disabled="editorLocked || !canEdit">
            <h3>教材与培训标准</h3>
            <el-form-item label="课程标题"
              ><el-input
                v-model="form.title"
                placeholder="课程标题，最多 200 字"
                maxlength="200"
                show-word-limit
            /></el-form-item>
            <el-form-item label="课程说明"
              ><el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="培训说明，最多 5000 字"
                maxlength="5000"
                show-word-limit
            /></el-form-item>
            <div class="standards">
              <el-form-item label="必学时长（秒）"
                ><el-input-number
                  v-model="form.requiredDurationSeconds"
                  :min="1"
                  :max="86400"
                  :precision="0"
                  placeholder="尚未配置"
                  controls-position="right" /></el-form-item
              ><el-form-item label="通过分数（0–100）"
                ><el-input-number
                  v-model="form.passScore"
                  :min="0"
                  :max="100"
                  :precision="0"
                  placeholder="尚未配置"
                  controls-position="right" /></el-form-item
              ><el-form-item label="考试限时（秒）"
                ><el-input-number
                  v-model="form.attemptTimeLimitSeconds"
                  :min="60"
                  :max="86400"
                  :precision="0"
                  placeholder="尚未配置"
                  controls-position="right"
              /></el-form-item>
            </div>
          </el-form>
          <section class="media-card">
            <h4>私有培训教材</h4>
            <p v-if="form.mediaFileId">
              {{ uploadName ? uploadName + ' · 已核验，待保存' : '已绑定私有教材'
              }}<span v-if="detail.media?.inherited"> · 从原已发布版本继承，只读引用</span>
            </p>
            <p v-else>尚未上传教材，允许先保存不完整草稿。</p>
            <p v-if="detail.media && detail.media.fileId === form.mediaFileId" class="muted">
              {{ detail.media.status === 'READY' ? '已核验' : detail.media.status }} ·
              {{
                detail.media.size === null
                  ? '大小尚未核验'
                  : (detail.media.size / 1024 / 1024).toFixed(2) + ' MiB'
              }}<span v-if="detail.media.durationMillis !== null">
                · 实际时长 {{ (detail.media.durationMillis / 1000).toFixed(1) }} 秒</span
              >
            </p>
            <p class="muted">
              MP4，最大 100MiB。须通过服务端核验后绑定；教材不会作为公开链接发布。
            </p>
            <input
              ref="fileInput"
              class="file-input"
              type="file"
              accept="video/mp4,.mp4"
              aria-label="选择培训视频"
              @change="upload"
            />
            <div class="toolbar">
              <el-button
                v-if="canEdit"
                :loading="uploading"
                :disabled="editorLocked || editorStale"
                @click="fileInput?.click()"
                >上传或替换 MP4</el-button
              ><el-button
                :loading="previewBusy"
                :disabled="!form.mediaFileId || uploading || editorBusy"
                @click="preview"
                >核对教材原片</el-button
              ><el-button v-if="videoUrl" @click="clearVideo">关闭教材预览</el-button>
            </div>
            <video v-if="videoUrl" class="video" :src="videoUrl" controls preload="metadata" />
          </section>
          <section>
            <div class="heading">
              <div>
                <h3>题库与标准答案</h3>
                <p class="muted">标准答案仅供获授权的课程管理员维护。空题库可暂存，但不能发布。</p>
              </div>
              <el-button
                v-if="canEdit"
                :disabled="editorLocked || form.questions.length >= 200"
                @click="addQuestion"
                >添加题目</el-button
              >
            </div>
            <p v-if="!form.questions.length" class="muted">当前草稿暂无题目。</p>
            <el-card
              v-for="(question, index) in form.questions"
              :key="index"
              class="question-card"
              shadow="never"
              ><template #header
                ><div class="heading">
                  <strong>第 {{ index + 1 }} 题</strong>
                  <div class="toolbar">
                    <el-button
                      v-if="canEdit"
                      size="small"
                      :disabled="editorLocked || index === 0"
                      @click="
                        [form.questions[index - 1], form.questions[index]] = [
                          form.questions[index],
                          form.questions[index - 1]
                        ]
                      "
                      >上移</el-button
                    ><el-button
                      v-if="canEdit"
                      size="small"
                      type="danger"
                      :disabled="editorLocked"
                      @click="form.questions.splice(index, 1)"
                      >删除题目</el-button
                    >
                  </div>
                </div></template
              ><el-form label-position="top" :disabled="editorLocked || !canEdit"
                ><div class="standards">
                  <el-form-item label="稳定题目编码"
                    ><el-input
                      v-model="question.questionCode"
                      placeholder="题目编码，同课程内唯一"
                      maxlength="64" /></el-form-item
                  ><el-form-item label="题型"
                    ><el-select
                      :model-value="question.type"
                      @update:model-value="changeQuestionType(question, $event)"
                      ><el-option label="单选题" value="SINGLE" /><el-option
                        label="多选题"
                        value="MULTIPLE" /><el-option
                        label="判断题"
                        value="TRUE_FALSE" /></el-select
                  ></el-form-item>
                </div>
                <el-form-item label="题干"
                  ><el-input
                    v-model="question.prompt"
                    type="textarea"
                    :rows="2"
                    maxlength="2000"
                    placeholder="填写题干，最多 2000 字"
                /></el-form-item>
                <div
                  v-for="(option, optionIndex) in question.options"
                  :key="optionIndex"
                  class="option-row"
                >
                  <el-input
                    :model-value="option.code"
                    @update:model-value="changeOptionCode(question, option, $event)"
                    class="option-code"
                    maxlength="32"
                    placeholder="选项编码"
                  /><el-input
                    v-model="option.text"
                    maxlength="500"
                    placeholder="选项内容，最多 500 字"
                  /><el-button
                    v-if="canEdit"
                    type="danger"
                    :disabled="question.options.length <= 2"
                    @click="removeOption(question, optionIndex)"
                    >移除</el-button
                  >
                </div>
                <el-button
                  v-if="canEdit"
                  :disabled="question.options.length >= 10"
                  @click="addOption(question)"
                  >添加选项</el-button
                ><el-form-item label="标准答案"
                  ><el-checkbox-group
                    v-if="question.type === 'MULTIPLE'"
                    v-model="question.correctOptionCodes"
                    ><el-checkbox
                      v-for="(option, optionIndex) in question.options"
                      :key="optionIndex"
                      :value="option.code"
                      >{{ option.code || '未填编码' }}</el-checkbox
                    ></el-checkbox-group
                  ><el-radio-group
                    v-else
                    :model-value="question.correctOptionCodes[0] || ''"
                    @update:model-value="singleAnswer(question, $event)"
                    ><el-radio
                      v-for="(option, optionIndex) in question.options"
                      :key="optionIndex"
                      :value="option.code"
                      >{{ option.code || '未填编码' }}</el-radio
                    ></el-radio-group
                  ></el-form-item
                ></el-form
              >
              <p class="muted">
                {{
                  question.type === 'TRUE_FALSE'
                    ? '判断题恰好两个选项、一个正确答案。'
                    : question.type === 'MULTIPLE'
                      ? '多选题可选一个或多个标准答案，提交答案须为选项编码的子集。'
                      : '单选题恰好一个正确答案。'
                }}
              </p></el-card
            >
          </section>
          <el-alert
            v-if="detail.publishBlockers.length"
            type="warning"
            title="当前保存版本尚不能发布"
            :closable="false"
            ><ul>
              <li v-for="(blocker, index) in detail.publishBlockers" :key="index">
                {{ blocker.message }}
              </li>
            </ul></el-alert
          >
          <el-alert
            v-if="editorStale"
            type="warning"
            title="课程版本已变化，填写内容已保留。请先重读当前课程，再决定是否保存。"
            :closable="false"
          />
          <el-alert
            v-if="editorUnknown"
            type="warning"
            title="上次保存结果未知，已锁定原请求。重读不会更换原请求与幂等键。"
            :closable="false"
          />
          <template v-if="canEdit"
            ><el-input
              v-model="saveReason"
              type="textarea"
              maxlength="500"
              placeholder="请填写本次草稿维护原因，最多 500 字"
              :disabled="editorLocked"
            />
            <div class="toolbar">
              <el-button
                type="primary"
                :loading="editorBusy"
                :disabled="editorLoading || uploading || editorStale || (!dirty && !editorUnknown)"
                @click="save"
                >{{ editorUnknown ? '沿用原保存请求重试' : '保存课程草稿' }}</el-button
              ><el-button
                v-if="can(detail, 'PUBLISH_TRAINING_COURSE', 'publish')"
                :disabled="editorLocked || dirty"
                @click="openAction('PUBLISH', detail)"
                >核对并发布</el-button
              >
            </div></template
          >
          <el-button
            v-if="can(detail, 'CREATE_TRAINING_REVISION', 'create')"
            @click="openAction('FORK', detail)"
            >从此版本派生新草稿</el-button
          >
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="actionOpen"
      :title="actionTitle"
      width="620px"
      :before-close="closeAction"
      destroy-on-close
      ><div v-loading="actionLoading">
        <el-alert v-if="actionError" type="error" :title="actionError" :closable="false" /><template
          v-if="actionKind === 'CREATE'"
          ><el-alert
            v-if="companyError"
            type="warning"
            :title="companyError"
            :closable="false"
          /><el-form label-position="top" :disabled="actionBusy || actionUnknown"
            ><el-form-item label="实际所属公司部门"
              ><el-tree-select
                v-model="actionForm.ownerDeptId"
                :data="companies"
                :props="{ label: 'label', children: 'children' }"
                node-key="id"
                check-strictly
                placeholder="明确选择实际所属公司部门"
                :disabled="!companies.length" /></el-form-item
            ><el-form-item label="课程标题"
              ><el-input
                v-model="actionForm.title"
                placeholder="填写新课程标题"
                maxlength="200" /></el-form-item
          ></el-form>
          <p class="muted">
            创建后再上传教材、填写培训门槛与题库。不会默认使用当前用户主部门。
          </p></template
        ><template v-else-if="actionRow"
          ><p>
            <strong>{{ actionRow.title }}</strong> · {{ actionRow.companyName || '归属待核实' }} ·
            内容版本 V{{ actionRow.courseVersion }}
          </p>
          <el-alert
            v-if="actionKind === 'FORK'"
            title="将继承该已发布版本的内容与题库，建立同课程族的新草稿。旧版本保持不变，教材以受控只读方式继承。"
            type="info"
            :closable="false" /><template v-else
            ><el-alert
              title="发布后本版本内容与标准答案不可原地修改。请确认教材、必学时长、通过分数和题库均为公司的正式培训标准。"
              type="warning"
              :closable="false" />
            <p>
              题数 {{ actionRow.questionCount }} · 必学 {{ actionRow.requiredDurationSeconds }} 秒 ·
              通过 {{ actionRow.passScore }} 分 · 限时 {{ actionRow.attemptTimeLimitSeconds }} 秒
            </p>
            <ul v-if="actionRow.publishBlockers.length">
              <li v-for="(blocker, index) in actionRow.publishBlockers" :key="index">
                {{ blocker.message }}
              </li>
            </ul>
            <el-input
              v-model="actionForm.reason"
              type="textarea"
              maxlength="500"
              placeholder="请填写发布原因，最多 500 字"
              :disabled="actionBusy || actionUnknown" /></template></template
        ><el-alert
          v-if="actionStale"
          type="warning"
          title="课程已变化，原因已保留。请重读最新课程后再确认。"
          :closable="false"
        /><el-alert
          v-if="actionUnknown"
          type="warning"
          title="操作结果未知，保留原请求与原幂等键；核对后沿用原请求重试。"
          :closable="false"
        />
      </div>
      <template #footer
        ><el-button
          v-if="actionKind !== 'CREATE'"
          :disabled="actionBusy || actionLoading"
          @click="refreshAction"
          >重读当前课程</el-button
        ><el-button
          :disabled="actionBusy || actionLoading"
          @click="
            actionOpen = false;
            actionSequence++;
          "
          >关闭</el-button
        ><el-button
          type="primary"
          :loading="actionBusy"
          :disabled="!actionAllowed || actionLoading || actionStale"
          @click="submitAction"
          >{{
            actionUnknown
              ? '沿用原请求重试'
              : actionKind === 'PUBLISH'
                ? '确认发布'
                : '确认建立草稿'
          }}</el-button
        ></template
      ></el-dialog
    >
  </div>
</template>

<style scoped>
.heading,
.toolbar,
.filters,
.standards,
.option-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.heading {
  justify-content: space-between;
}
.heading h2,
.heading h3 {
  margin: 0 0 10px;
}
p,
.muted,
small {
  color: #64748b;
  line-height: 1.7;
}
small {
  display: block;
  font-size: 12px;
  overflow-wrap: anywhere;
}
.filters {
  margin-top: 20px;
  align-items: flex-end;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
.editor {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}
.standards {
  align-items: flex-start;
}
.standards > * {
  flex: 1;
  min-width: 180px;
}
.media-card {
  padding: 18px;
  border: 1px solid #dce3ec;
  border-radius: 8px;
}
.media-card h4 {
  margin-top: 0;
}
.file-input {
  display: none;
}
.video {
  display: block;
  margin-top: 15px;
  width: 100%;
  max-height: 420px;
  background: #111;
}
.question-card {
  margin: 14px 0;
}
.option-row {
  flex-wrap: nowrap;
  margin-bottom: 10px;
}
.option-code {
  flex: 0 0 110px;
}
.option-row > .el-input:not(.option-code) {
  flex: 1;
  min-width: 0;
}
:deep(.el-dialog) {
  max-width: calc(100vw - 24px);
}
:deep(.el-select),
:deep(.el-input-number) {
  max-width: 100%;
}
:deep(.el-checkbox-group),
:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
}
@media (max-width: 600px) {
  .training-page {
    padding: 14px;
  }
  .heading {
    align-items: flex-start;
  }
  .option-row {
    flex-wrap: wrap;
  }
  .option-code {
    flex-basis: 90px;
  }
  .standards > * {
    min-width: 100%;
  }
  .pagination {
    overflow: auto;
    justify-content: flex-start;
  }
}
</style>
