<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onHide, onShow, onUnload } from '@dcloudio/uni-app';
import { getPublicServiceConfig, type PublicServiceConfig } from '../../features/config/api';
import { getSession } from '../../features/identity/api';
import {
  getQualification,
  pendingQualificationIntent,
  qualificationStatus,
  runQualificationIntent,
  type EvidenceField,
  type MyQualification,
  type QualificationIntent,
  type SaveQualification
} from '../../features/qualification/api';
import {
  chooseAndUploadQualificationEvidence,
  intentKey,
  previewQualificationEvidence
} from '../../features/file/api';
import { currentSession } from '../../services/http';
import type { SessionView } from '../../api/types';

type Form = {
  realName: string;
  idCardNo: string;
  idCardFrontFileId: string;
  idCardBackFileId: string;
  licenseMainFileId: string;
  licenseSubFileId: string;
  licenseClass: string;
  firstIssueDate: string;
  validFrom: string;
  validTo: string;
  longTermValid: boolean;
};
const emptyForm = (): Form => ({
  realName: '',
  idCardNo: '',
  idCardFrontFileId: '',
  idCardBackFileId: '',
  licenseMainFileId: '',
  licenseSubFileId: '',
  licenseClass: '',
  firstIssueDate: '',
  validFrom: '',
  validTo: '',
  longTermValid: false
});
const form = reactive<Form>(emptyForm());
const baseline = ref<Form>(emptyForm());
const application = ref<MyQualification | null>(null);
const session = ref<SessionView | null>(null);
const config = ref<PublicServiceConfig | null>(null);
const loading = ref(false);
const busy = ref(false);
const uploading = ref<EvidenceField | null>(null);
const error = ref('');
const notice = ref('');
const accepted = ref(false);
const acceptedVersion = ref('');
const acceptedTextHash = ref('');
const reason = ref('');
const unknown = ref(false);
const mustRefresh = ref(false);
let sequence = 0;
let visible = false;

const evidence: Array<{
  field: EvidenceField;
  label: string;
  purpose: 'ID_CARD' | 'DRIVER_LICENSE';
}> = [
  { field: 'idCardFrontFileId', label: '身份证人像面', purpose: 'ID_CARD' },
  { field: 'idCardBackFileId', label: '身份证国徽面', purpose: 'ID_CARD' },
  { field: 'licenseMainFileId', label: '驾驶证主页', purpose: 'DRIVER_LICENSE' },
  { field: 'licenseSubFileId', label: '驾驶证副页', purpose: 'DRIVER_LICENSE' }
];
const fieldLabels: Record<string, string> = {
  realName: '姓名',
  idCardNo: '身份证号码',
  idCardFrontFileId: '身份证人像面',
  idCardBackFileId: '身份证国徽面',
  licenseMainFileId: '驾驶证主页',
  licenseSubFileId: '驾驶证副页',
  licenseClass: '准驾车型',
  firstIssueDate: '初领日期',
  validFrom: '有效期起始日',
  validTo: '有效期截止日',
  longTermValid: '长期有效',
  consentVersion: '申请同意说明',
  ownerDeptId: '所属公司'
};
const lock = computed(
  () => loading.value || busy.value || unknown.value || Boolean(uploading.value)
);
const consentReady = computed(() =>
  Boolean(
    config.value?.qualificationConsentVersion &&
      config.value?.qualificationConsentText &&
      /^[0-9a-f]{64}$/.test(config.value?.qualificationConsentHash || '')
  )
);
const acceptedCurrentConsent = computed(
  () =>
    accepted.value &&
    acceptedVersion.value === config.value?.qualificationConsentVersion &&
    acceptedTextHash.value === config.value?.qualificationConsentHash
);
const consentChanged = computed(
  () =>
    application.value?.draft.consentVersion !== config.value?.qualificationConsentVersion ||
    application.value?.draft.consentTextHash !== config.value?.qualificationConsentHash
);
const longTermBlocked = computed(
  () => !form.longTermValid && Boolean(form.validTo) && !editable('validTo')
);
const canCreate = computed(
  () =>
    session.value?.phoneBound === true &&
    consentReady.value &&
    (!application.value || hasAction('CREATE_QUALIFICATION_REVISION'))
);
const changedFields = computed(() =>
  (Object.keys(form) as Array<keyof Form>).filter((key) => form[key] !== baseline.value[key])
);
const dirty = computed(
  () => changedFields.value.length > 0 || (consentChanged.value && acceptedCurrentConsent.value)
);
const effectiveStatus = computed(() =>
  qualificationStatus(
    application.value?.effectiveQualificationStatus || session.value?.driverQualification
  )
);
const canSwitchDriver = computed(() => session.value?.availableRoles.includes('DRIVER'));
const reviewReasons = computed(
  () =>
    application.value?.rejectReasons.filter(
      (item) => item !== application.value?.publicReviewOpinion
    ) || []
);

function hasAction(action: string) {
  return application.value?.allowedActions.includes(action) === true;
}
function editable(field: string) {
  return (
    hasAction('EDIT_QUALIFICATION') && application.value?.editableFields.includes(field) === true
  );
}
function changeDate(
  field: 'firstIssueDate' | 'validFrom' | 'validTo',
  event: { detail: { value: string } }
) {
  if (!lock.value && editable(field)) form[field] = event.detail.value;
}
function consentCheck(event: { detail: { value: string[] } }) {
  accepted.value = event.detail.value.includes('accepted');
  acceptedVersion.value = accepted.value ? config.value?.qualificationConsentVersion || '' : '';
  acceptedTextHash.value = accepted.value ? config.value?.qualificationConsentHash || '' : '';
}
function longTermCheck(event: unknown) {
  const value = (event as { detail?: { value?: unknown } }).detail?.value;
  if (lock.value || !editable('longTermValid') || typeof value !== 'boolean') return;
  if (value && form.validTo && !editable('validTo')) {
    error.value = '截止日不在本次补正范围，请联系管理员补齐补正范围后再改为长期有效';
    return;
  }
  // 长期与到期日互斥；仅在服务端允许编辑截止日时显式清除，不顺带改未被驳回的材料。
  if (value && editable('validTo')) form.validTo = '';
  form.longTermValid = value;
}
function clearDate(field: 'firstIssueDate' | 'validFrom' | 'validTo') {
  if (!lock.value && editable(field)) form[field] = '';
}
function fill(value: MyQualification | null) {
  const draft = value?.draft;
  Object.assign(form, emptyForm(), {
    realName: draft?.realName || '',
    idCardFrontFileId: draft?.idCardFrontFileId || '',
    idCardBackFileId: draft?.idCardBackFileId || '',
    licenseMainFileId: draft?.licenseMainFileId || '',
    licenseSubFileId: draft?.licenseSubFileId || '',
    licenseClass: draft?.licenseClass || '',
    firstIssueDate: draft?.firstIssueDate || '',
    validFrom: draft?.validFrom || '',
    validTo: draft?.validTo || '',
    longTermValid: draft?.longTermValid === true
  });
  // 掩码仅用于说明已有原件；空身份证输入表示保留服务端密文，禁止把掩码当原号码回写。
  baseline.value = { ...form };
}

onShow(() => {
  visible = true;
  unknown.value = Boolean(pendingQualificationIntent());
  load(false);
});
function clear() {
  visible = false;
  sequence++;
  application.value = null;
  session.value = null;
  config.value = null;
  Object.assign(form, emptyForm());
  baseline.value = emptyForm();
  accepted.value = false;
  acceptedVersion.value = '';
  acceptedTextHash.value = '';
  reason.value = '';
  error.value = '';
  notice.value = '';
  loading.value = false;
  busy.value = false;
  uploading.value = null;
  // 页面离开即清输入和展示；未知意图仅由 API 模块按当前账号/会话/视角在内存保留。
}
onHide(clear);
onUnload(clear);

async function load(preserve = true) {
  if (busy.value || uploading.value) return;
  const run = ++sequence;
  const previous = currentSession();
  const keep = preserve ? changedFields.value.slice() : [];
  const previousForm = { ...form };
  const previousApplicationId = application.value?.applicationId;
  loading.value = true;
  error.value = '';
  try {
    const identity = await getSession();
    if (!visible || run !== sequence) return;
    if (
      !previous ||
      identity.accountId !== previous.accountId ||
      identity.sessionId !== previous.sessionId ||
      identity.contextVersion !== previous.contextVersion ||
      identity.activeRole !== previous.activeRole ||
      currentSession()?.sessionId !== previous.sessionId ||
      currentSession()?.contextVersion !== previous.contextVersion
    )
      throw new Error('身份信息已变化，请重新进入认证中心');
    session.value = identity;
    const published = await getPublicServiceConfig();
    if (!visible || run !== sequence) return;
    if (
      acceptedVersion.value !== published.qualificationConsentVersion ||
      acceptedTextHash.value !== published.qualificationConsentHash
    ) {
      accepted.value = false;
      acceptedVersion.value = '';
      acceptedTextHash.value = '';
    }
    config.value = published;
    if (!identity.phoneBound) {
      application.value = null;
      fill(null);
      return;
    }
    const value = await getQualification();
    if (!visible || run !== sequence) return;
    application.value = value;
    fill(value);
    // 冲突刷新只合并仍允许修改的输入，不能用旧申请文件覆盖新复审或已锁定材料。
    if (value?.applicationId === previousApplicationId)
      for (const key of keep) {
        if (editable(key)) Object.assign(form, { [key]: previousForm[key] });
      }
    mustRefresh.value = false;
    unknown.value = Boolean(pendingQualificationIntent());
  } catch (failure) {
    if (visible && run === sequence) {
      application.value = null;
      session.value = null;
      config.value = null;
      fill(null);
      error.value = failure instanceof Error ? failure.message : '认证资料读取失败';
    }
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}

async function upload(field: EvidenceField) {
  if (lock.value || !application.value || !editable(field)) return;
  const run = sequence;
  const applicationId = application.value.applicationId;
  const item = evidence.find((value) => value.field === field)!;
  uploading.value = field;
  error.value = '';
  try {
    const ready = await chooseAndUploadQualificationEvidence(applicationId, item.purpose);
    if (visible && run === sequence && application.value?.applicationId === applicationId) {
      form[field] = ready.fileId;
      notice.value = item.label + '已通过文件核验，请保存草稿。';
    }
  } catch (failure) {
    if (visible && run === sequence)
      error.value = failure instanceof Error ? failure.message : '证件上传未完成';
  } finally {
    if (visible && run === sequence) uploading.value = null;
  }
}
async function preview(field: EvidenceField) {
  if (!form[field] || busy.value || uploading.value) return;
  const run = sequence;
  try {
    await previewQualificationEvidence(form[field]);
  } catch (failure) {
    if (visible && run === sequence)
      error.value = failure instanceof Error ? failure.message : '证件查看失败';
  }
}
function buildSave(): SaveQualification {
  if (!application.value) throw new Error('请先建立本人申请草稿');
  if (form.longTermValid && form.validTo)
    throw new Error('长期有效不能保留截止日；请在允许的补正范围内清除截止日后再保存');
  const input: SaveQualification = {
    applicationId: application.value.applicationId,
    expectedVersion: application.value.version
  };
  for (const field of changedFields.value) {
    if (!editable(field)) continue;
    if (field === 'idCardNo') {
      const number = form.idCardNo.trim().toUpperCase();
      if (!number) continue;
      if (!/^[1-9]\d{16}[\dX]$/.test(number)) throw new Error('请填写有效的 18 位身份证号码');
      input.idCardNo = number;
    } else if (field === 'longTermValid') input.longTermValid = form.longTermValid;
    else Object.assign(input, { [field]: form[field].trim() || null });
  }
  if (consentChanged.value) {
    if (!consentReady.value || !acceptedCurrentConsent.value)
      throw new Error('请阅读并同意当前申请说明');
    input.consentVersion = config.value!.qualificationConsentVersion!;
    input.consentTextHash = config.value!.qualificationConsentHash!;
    input.acceptedConsent = true;
  }
  if (Object.keys(input).length < 3) throw new Error('请先修改需要保存的资料');
  return input;
}
async function perform(action: 'CREATE' | 'SAVE' | 'SUBMIT') {
  if (lock.value || mustRefresh.value) return;
  error.value = '';
  notice.value = '';
  try {
    let intent: QualificationIntent;
    if (action === 'CREATE') {
      if (!canCreate.value || !acceptedCurrentConsent.value)
        throw new Error('请先阅读并同意当前申请说明');
      intent = {
        action,
        key: intentKey(),
        input: {
          consentVersion: config.value!.qualificationConsentVersion!,
          consentTextHash: config.value!.qualificationConsentHash!,
          acceptedConsent: true
        }
      };
    } else if (action === 'SAVE') {
      if (!hasAction('EDIT_QUALIFICATION')) return;
      intent = { action, key: intentKey(), input: buildSave() };
    } else {
      if (!application.value || !hasAction('SUBMIT_QUALIFICATION')) return;
      if (dirty.value) throw new Error('请先保存本次资料修改，再提交审核');
      if (!reason.value.trim() || reason.value.trim().length > 500)
        throw new Error('请填写 1 至 500 字提交说明');
      intent = {
        action,
        key: intentKey(),
        input: {
          applicationId: application.value.applicationId,
          expectedVersion: application.value.version,
          expectedProfileVersion: application.value.profileVersion,
          reason: reason.value.trim()
        }
      };
    }
    pendingQualificationIntent(intent);
    await execute(intent);
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : '操作未完成';
  }
}
async function retryUnknown() {
  if (busy.value || loading.value) return;
  const intent = pendingQualificationIntent();
  if (intent) await execute(intent);
}
async function execute(intent: QualificationIntent) {
  const run = sequence;
  busy.value = true;
  unknown.value = true;
  error.value = '';
  try {
    const value = await runQualificationIntent(intent);
    if (!visible || run !== sequence) return;
    pendingQualificationIntent(null);
    unknown.value = false;
    application.value = value;
    fill(value);
    accepted.value = false;
    reason.value = '';
    notice.value =
      intent.action === 'CREATE'
        ? '本人草稿已建立，请上传本次申请的证件并保存资料。'
        : intent.action === 'SAVE'
          ? '草稿已保存；保存资料不会批准代驾资格。'
          : '申请已提交，结果以实际审核进度为准。';
  } catch (failure) {
    if (!visible || run !== sequence) return;
    const response = failure as { status?: number; message?: string };
    if (response.status && response.status < 500) {
      pendingQualificationIntent(null);
      unknown.value = false;
      mustRefresh.value = response.status === 409;
    }
    error.value = response.message || '写入结果未确认，请查询后用原意图重试';
  } finally {
    if (visible && run === sequence) busy.value = false;
  }
}
function checkLabel(type: string) {
  return (
    (
      {
        REALNAME: '实名核验',
        FACE: '活体核验',
        BACKGROUND: '背景核验',
        LICENSE: '驾驶证核验',
        TRAINING: '培训考试'
      } as Record<string, string>
    )[type] || type
  );
}
function checkStatus(status: string) {
  return (
    (
      {
        CREATED: '待核验',
        PROCESSING: '核验中',
        PASSED: '已通过',
        FAILED: '未通过',
        EXPIRED: '已失效',
        UNAVAILABLE: '尚未接通或暂无有效结果'
      } as Record<string, string>
    )[status] || status
  );
}
function dateTime(value: string | null | undefined) {
  if (!value) return '暂无';
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false });
}
function open(path: string) {
  uni.navigateTo({ url: path });
}
</script>

<template>
  <view class="screen">
    <text class="title">代驾认证中心</text>
    <text class="muted">本人申请与当前有效资格分别核对。员工也须完成同一套真实认证和审核。</text>
    <text v-if="loading" class="muted">正在读取本人资格与公开申请说明…</text>

    <view v-if="session" class="card">
      <text class="section">当前有效资格</text>
      <text class="status">{{ effectiveStatus }}</text>
      <text class="muted">{{
        application?.companyName
          ? '所属公司：' + application.companyName
          : '所属公司尚未配置，请联系平台管理员认领。'
      }}</text>
      <text class="note"
        >申请、保存或刷新都不会授予代驾身份，也不会开启听单。已有在手任务仍须继续保管并实际归还车辆。</text
      >
      <button v-if="canSwitchDriver" @click="open('/pages/identity/switch')">
        切换已有代驾身份
      </button>
      <button v-if="!session.phoneBound" @click="open('/pages/identity/phone')">
        先核验本人手机号
      </button>
    </view>

    <view v-if="application" class="card">
      <view class="heading"
        ><text class="section">本次申请</text
        ><text class="badge">{{ qualificationStatus(application.status) }}</text></view
      >
      <text class="muted"
        >申请编号 {{ application.applicationId }} · 资料版本 {{ application.version }}</text
      >
      <text v-if="application.reviewDueAt" class="muted"
        >复审截止：{{ dateTime(application.reviewDueAt) }}</text
      >
      <text v-if="application.publicReviewOpinion" class="review"
        >审核意见：{{ application.publicReviewOpinion }}</text
      >
      <text v-for="(item, index) in reviewReasons" :key="'reason-' + index" class="error">{{
        item
      }}</text>
      <text
        v-if="application.status === 'REJECTED' && application.rejectFields === null"
        class="note"
        >这份历史申请未记录可补正字段，请联系管理员核实；不能直接覆盖已审核资料。</text
      >
      <text v-else-if="application.status === 'REJECTED'" class="note"
        >仅可补正审核指出的相关项，其余资料保持原记录。</text
      >
      <text v-if="application.status === 'SUBMITTED'" class="muted"
        >材料已提交，等待实际审核。刷新不会改变审核结论。</text
      >
    </view>

    <view v-if="application" class="card">
      <text class="section">实名资料</text>
      <view class="field"
        ><text>姓名</text
        ><input
          v-model="form.realName"
          aria-label="姓名"
          placeholder="填写证件上的本人姓名"
          maxlength="64"
          :disabled="lock || !editable('realName')"
      /></view>
      <view class="field"
        ><text>身份证号码</text
        ><input
          v-model="form.idCardNo"
          aria-label="身份证号码"
          placeholder="填写完整号码；已存号码留空保留"
          maxlength="18"
          :disabled="lock || !editable('idCardNo')"
      /></view>
      <text v-if="application.draft.idCardNoProvided" class="muted"
        >已保存：{{
          application.draft.idCardNoMask || '身份证号码已加密保存'
        }}。留空会保留原号码，不会清除。</text
      >
      <text class="muted">完整身份证号码不会回显，也不会保存到本地缓存。</text>
      <view
        v-for="item in evidence.filter((value) => value.purpose === 'ID_CARD')"
        :key="item.field"
        class="evidence"
      >
        <text>{{ item.label }}</text
        ><text class="muted">{{
          form[item.field]
            ? form[item.field] === baseline[item.field]
              ? '已保存'
              : '已核验，待保存'
            : '尚未上传'
        }}</text>
        <view class="buttons"
          ><button :disabled="lock || !editable(item.field)" @click="upload(item.field)">
            {{
              uploading === item.field
                ? '正在上传核验…'
                : form[item.field]
                  ? '重新上传'
                  : '上传图片'
            }}</button
          ><button
            :disabled="!form[item.field] || busy || !!uploading"
            @click="preview(item.field)"
          >
            查看原图
          </button></view
        >
      </view>
    </view>

    <view v-if="application" class="card">
      <text class="section">驾驶证资料</text>
      <view class="field"
        ><text>准驾车型</text
        ><input
          v-model="form.licenseClass"
          aria-label="准驾车型"
          placeholder="按驾驶证填写，例如 C1"
          maxlength="32"
          :disabled="lock || !editable('licenseClass')"
      /></view>
      <text class="muted"
        >准驾车型按真实兼容规则核验，不按字母或编号简单比较。识别能力尚未接入，请人工核对资料。</text
      >
      <view class="field"
        ><text>初领日期</text
        ><picker
          mode="date"
          :value="form.firstIssueDate"
          :disabled="lock || !editable('firstIssueDate')"
          @change="changeDate('firstIssueDate', $event)"
          ><view class="date">{{ form.firstIssueDate || '选择初领日期' }}</view></picker
        ><button
          v-if="form.firstIssueDate && editable('firstIssueDate')"
          :disabled="lock"
          @click="clearDate('firstIssueDate')"
        >
          清除初领日期
        </button></view
      >
      <view class="field"
        ><text>有效期起始日</text
        ><picker
          mode="date"
          :value="form.validFrom"
          :disabled="lock || !editable('validFrom')"
          @change="changeDate('validFrom', $event)"
          ><view class="date">{{ form.validFrom || '选择起始日期' }}</view></picker
        ><button
          v-if="form.validFrom && editable('validFrom')"
          :disabled="lock"
          @click="clearDate('validFrom')"
        >
          清除起始日
        </button></view
      >
      <view class="heading"
        ><text>长期有效</text
        ><switch
          :checked="form.longTermValid"
          :disabled="lock || !editable('longTermValid') || longTermBlocked"
          @change="longTermCheck"
      /></view>
      <text v-if="longTermBlocked && editable('longTermValid')" class="note"
        >截止日不在本次补正范围，请联系管理员补齐补正范围后再改为长期有效。</text
      >
      <view v-if="!form.longTermValid" class="field"
        ><text>有效期截止日</text
        ><picker
          mode="date"
          :value="form.validTo"
          :disabled="lock || !editable('validTo')"
          @change="changeDate('validTo', $event)"
          ><view class="date">{{ form.validTo || '选择截止日期' }}</view></picker
        ><button
          v-if="form.validTo && editable('validTo')"
          :disabled="lock"
          @click="clearDate('validTo')"
        >
          清除截止日
        </button></view
      >
      <view
        v-for="item in evidence.filter((value) => value.purpose === 'DRIVER_LICENSE')"
        :key="item.field"
        class="evidence"
      >
        <text>{{ item.label }}</text
        ><text class="muted">{{
          form[item.field]
            ? form[item.field] === baseline[item.field]
              ? '已保存'
              : '已核验，待保存'
            : '尚未上传'
        }}</text>
        <view class="buttons"
          ><button :disabled="lock || !editable(item.field)" @click="upload(item.field)">
            {{
              uploading === item.field
                ? '正在上传核验…'
                : form[item.field]
                  ? '重新上传'
                  : '上传图片'
            }}</button
          ><button
            :disabled="!form[item.field] || busy || !!uploading"
            @click="preview(item.field)"
          >
            查看原图
          </button></view
        >
      </view>
      <text class="muted"
        >每张限 JPEG / PNG，最大 10MB。须通过服务端文件核验后才能保存到本次申请。</text
      >
    </view>

    <view v-if="application" class="card">
      <text class="section">真实核验与提交条件</text>
      <text class="muted"
        >资料完整、所属公司已配置、同意当前说明，且五项真实核验与培训均通过后，才可提交审核。</text
      >
      <view v-for="check in application.checks" :key="check.type" class="check"
        ><text>{{ checkLabel(check.type) }}</text
        ><text class="muted">{{ checkStatus(check.status) }}</text></view
      >
      <text class="muted"
        >活体、背景核验和培训考试入口尚未接通。未取得有效结果时不会展示通过；后台审核另行进行。</text
      >
      <view v-if="application.submitBlockers.length" class="blockers"
        ><text v-for="(blocker, index) in application.submitBlockers" :key="index"
          >{{ fieldLabels[blocker.field] || '提交条件' }}：{{ blocker.message }}</text
        ></view
      >
    </view>

    <view
      v-if="
        session &&
        session.phoneBound &&
        (!application || canCreate || (hasAction('EDIT_QUALIFICATION') && consentChanged))
      "
      class="card"
    >
      <text class="section">申请同意说明</text>
      <template v-if="consentReady">
        <text class="muted">当前发布版本：{{ config?.qualificationConsentVersion }}</text>
        <text class="consent">{{ config?.qualificationConsentText }}</text>
        <checkbox-group @change="consentCheck"
          ><label class="consent-row"
            ><checkbox value="accepted" :checked="accepted" :disabled="lock" /><text
              >我已阅读并同意以上说明</text
            ></label
          ></checkbox-group
        >
      </template>
      <text v-else class="note"
        >申请说明尚未发布，暂不能建立申请或确认新版本，请联系管理员配置。</text
      >
      <text v-if="canCreate && application" class="muted"
        >提交复审将建立新的材料草稿，须重新上传本次证件；不会复制旧申请文件或直接覆盖当前有效资格。</text
      >
      <button
        v-if="canCreate"
        class="primary"
        :disabled="lock || !acceptedCurrentConsent || mustRefresh"
        @click="perform('CREATE')"
      >
        {{ application ? '建立复审草稿' : '建立本人申请草稿' }}
      </button>
    </view>

    <view v-if="application && hasAction('EDIT_QUALIFICATION')" class="card">
      <button class="primary" :disabled="lock || !dirty || mustRefresh" @click="perform('SAVE')">
        保存草稿
      </button>
      <text class="muted"
        >仅保存到本人的当前申请。修改真实材料后，既有核验结果是否仍有效由服务端重新核对。</text
      >
    </view>
    <view v-if="application && hasAction('SUBMIT_QUALIFICATION')" class="card">
      <text class="section">提交审核</text>
      <textarea
        v-model="reason"
        aria-label="提交说明"
        placeholder="填写本次提交或补正说明，最多 500 字"
        maxlength="500"
        :disabled="lock || mustRefresh"
      />
      <text v-if="dirty" class="note">存在尚未保存的资料，请先保存草稿。</text>
      <button
        class="primary"
        :disabled="lock || dirty || mustRefresh || !reason.trim()"
        @click="perform('SUBMIT')"
      >
        提交本次申请
      </button>
    </view>

    <view v-if="unknown" class="card"
      ><text class="note"
        >上次写入结果尚未确认，资料已暂时锁定。可先刷新查询，再按原请求重试；不会换键或重复建立申请。</text
      ><button :disabled="busy || loading" @click="retryUnknown">按原意图核对重试</button></view
    >
    <text v-if="mustRefresh" class="note"
      >资料或公司归属版本已变化。填写内容仍保留，请先刷新核对最新版本，再继续保存或提交。</text
    >
    <text v-if="notice" class="success" role="status">{{ notice }}</text>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="busy || loading || !!uploading" @click="load(true)">刷新资格与进度</button>
    <button @click="open('/pages/work/index')">返回我的工作</button>
  </view>
</template>

<style scoped>
.screen {
  box-sizing: border-box;
  max-width: 720px;
  padding: 24px 18px 40px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  font-size: 24px;
  font-weight: 600;
}
.card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border-radius: 12px;
  background: white;
  font-size: 15px;
  line-height: 1.7;
  min-width: 0;
}
.section {
  font-size: 18px;
  font-weight: 600;
}
.status {
  font-size: 20px;
  color: #005fcb;
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
  overflow-wrap: anywhere;
}
.heading,
.check {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.badge {
  padding: 2px 10px;
  color: #005fcb;
  background: #eef5ff;
  border-radius: 6px;
  font-size: 13px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
input,
.date,
textarea {
  box-sizing: border-box;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 15px;
  background: #fafcff;
}
input {
  min-height: 44px;
  height: 44px;
}
textarea {
  width: 100%;
  min-height: 100px;
}
.evidence {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}
.buttons {
  display: flex;
  gap: 10px;
}
.buttons button {
  flex: 1;
  font-size: 14px;
  min-width: 0;
}
.note,
.review,
.blockers {
  padding: 12px;
  border-radius: 8px;
  background: #fff4e5;
  color: #854d0e;
  font-size: 14px;
  line-height: 1.7;
}
.blockers {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.review {
  background: #eff6ff;
  color: #1e40af;
}
.error {
  color: #b42318;
  line-height: 1.7;
}
.success {
  color: #087443;
  line-height: 1.7;
}
.consent {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  max-height: 300px;
  overflow-y: auto;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 14px;
}
.consent-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
button {
  width: 100%;
  margin: 0;
  font-size: 15px;
}
.primary {
  color: white;
  background: #005fcb;
}
</style>
