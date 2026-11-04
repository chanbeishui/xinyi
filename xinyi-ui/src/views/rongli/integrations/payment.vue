<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, onDeactivated } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listDept } from '@/api/system/dept';
import * as api from '@/api/rongli/integrations';
const kind = ref<api.Kind>('PAYMENT_POLICY'),
  rows = ref<api.Profile[]>([]),
  total = ref(0),
  page = ref(1),
  busy = ref(false),
  loading = ref(false),
  error = ref(''),
  selected = ref<api.Profile | null>(null),
  config = ref<Record<string, any>>({}),
  check = ref<api.Check | null>(null),
  issues = ref<api.Issue[]>([]),
  reason = ref(''),
  dirty = ref(false);
const departments = ref<{ id: string; name: string }[]>([]),
  references = ref<api.Profile[]>([]),
  history = ref<any[]>([]),
  historyTotal = ref(0),
  historyPage = ref(1),
  historyOpen = ref(false),
  createOpen = ref(false);
const newProfile = reactive({ name: '', ownerDeptId: '', environment: 'TEST' as 'TEST' | 'LIVE' });
const secrets = reactive<Record<string, { action: string; value: string }>>({});
let seq = 0,
  writeAttempt: { body: string; key: string; action: string } | null = null;
const labels = { PAYMENT_POLICY: '支付设置', WECHAT_PAY: '微信商户', WECHAT_APP: '小程序凭据' };
const secretSlots = computed(() =>
  kind.value === 'PAYMENT_POLICY'
    ? [{ key: 'offlineBankAccount', label: '企业银行账号' }]
    : kind.value === 'WECHAT_PAY'
      ? [
          { key: 'merchantPrivateKey', label: '商户 API 私钥 PEM' },
          { key: 'apiV3Key', label: 'API v3 密钥（32字节）' }
        ]
      : [{ key: 'appSecret', label: '小程序 AppSecret' }]
);
const referenceKind = computed(() =>
  kind.value === 'PAYMENT_POLICY' ? 'WECHAT_PAY' : 'WECHAT_APP'
);
const secretState = (key: string) =>
  (selected.value?.draft || selected.value?.active)?.settings.credentialStates?.[key]?.configured
    ? '已配置，内容不回显'
    : '尚未配置';
const company = (id: string) =>
  departments.value.find((x) => x.id === id)?.name || `公司部门 ${id}`;
// 同一动作与相同请求复用幂等键；内容变更才创建新键，防止未知结果下重复提交。
function keyFor(action: string, body: object) {
  const raw = JSON.stringify(body);
  if (!writeAttempt || writeAttempt.body !== raw || writeAttempt.action !== action)
    writeAttempt = { action, body: raw, key: crypto.randomUUID() };
  return writeAttempt.key;
}
// 保存、切换或离开编辑器时清理秘密输入，不把秘密放入列表、路由或浏览器持久化存储。
function clearSecrets() {
  Object.keys(secrets).forEach((k) => delete secrets[k]);
  writeAttempt = null;
}
function resetEditor() {
  seq++;
  selected.value = null;
  config.value = {};
  check.value = null;
  issues.value = [];
  reason.value = '';
  dirty.value = false;
  references.value = [];
  clearSecrets();
}
// 原因不属于配置内容，修改原因不使校验票据失效；配置字段变化则必须重新校验。
function formChanged(event: Event) {
  if ((event.target as HTMLElement)?.closest?.('[data-reason]')) return;
  changed();
}
function changed() {
  dirty.value = true;
  check.value = null;
}
function loadEditor(p: api.Profile) {
  clearSecrets();
  selected.value = p;
  config.value = JSON.parse(
    JSON.stringify(
      (p.draft || p.active)?.settings.config ||
        (p.kind === 'PAYMENT_POLICY'
          ? {
              offlineEnabled: false,
              wechatEnabled: false,
              offlineCollectionMode: 'PREPAID_MANUAL_VERIFIED',
              offlineMethods: []
            }
          : p.kind === 'WECHAT_PAY'
            ? {
                verificationMode: 'WECHATPAY_PUBLIC_KEY',
                bindingAcknowledged: false,
                platformCertificates: []
              }
            : { enabled: false })
    )
  );
  secretSlots.value.forEach((s) => (secrets[s.key] = { action: 'KEEP', value: '' }));
  issues.value = p.validationIssues || [];
  check.value = null;
  dirty.value = false;
  reason.value = '';
}
async function list() {
  const run = ++seq;
  loading.value = true;
  error.value = '';
  try {
    const r = await api.profiles({ kind: kind.value, pageNum: page.value, pageSize: 10 });
    if (run === seq) {
      rows.value = r.rows;
      total.value = r.total;
    }
  } catch (e: any) {
    if (run === seq) error.value = e.msg || '配置读取失败';
  } finally {
    if (run === seq) loading.value = false;
  }
}
async function select(p: api.Profile) {
  if (busy.value) return;
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('离开后将丢弃未保存内容和输入的凭据，是否继续？', '切换配置');
    } catch {
      return;
    }
  }
  resetEditor();
  const run = seq;
  loading.value = true;
  try {
    const r = await api.profile(p.profileId);
    if (run !== seq) return;
    loadEditor(r.data);
    await loadReferences();
  } catch (e: any) {
    if (run === seq) error.value = e.msg || '配置读取失败';
  } finally {
    if (run === seq) loading.value = false;
  }
}
async function loadReferences() {
  const current = selected.value;
  if (!current || kind.value === 'WECHAT_APP') return;
  const collected: api.Profile[] = [];
  let pageNum = 1;
  for (;;) {
    const r = await api.profiles({
      kind: referenceKind.value,
      ownerDeptId: current.scope.ownerDeptId,
      environment: current.scope.environment,
      pageNum,
      pageSize: 100
    });
    if (selected.value?.profileId !== current.profileId) return;
    collected.push(...r.rows.filter((x) => x.activeRevisionId));
    if (pageNum * 100 >= r.total) break;
    pageNum++;
  }
  references.value = collected;
}
async function changeKind(value: string | number) {
  if (busy.value) return;
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('切换后将丢弃未保存内容，是否继续？', '切换配置类型');
    } catch {
      return;
    }
  }
  kind.value = value as api.Kind;
  resetEditor();
  page.value = 1;
  await list();
}
function setReference(id: string) {
  const r = references.value.find((v) => v.profileId === id);
  const field = kind.value === 'PAYMENT_POLICY' ? 'wechatMerchant' : 'appProfile';
  if (r) config.value[field] = { profileId: r.profileId, revisionId: r.activeRevisionId };
  else delete config.value[field];
  changed();
}
async function copyUrl(value?: string | null) {
  if (!value) return;
  try {
    await navigator.clipboard.writeText(value);
    ElMessage.success('已复制回调地址');
  } catch {
    ElMessage.warning('复制失败，请手动选择地址复制');
  }
}
function openCreate() {
  newProfile.name = '';
  newProfile.ownerDeptId = '';
  newProfile.environment = 'TEST';
  createOpen.value = true;
}
async function create() {
  if (!newProfile.name.trim() || !newProfile.ownerDeptId) {
    ElMessage.warning('请填写名称并选择收款公司部门');
    return;
  }
  busy.value = true;
  try {
    const body = {
      kind: kind.value,
      name: newProfile.name.trim(),
      scope: { ownerDeptId: newProfile.ownerDeptId, environment: newProfile.environment }
    };
    const r = await api.createProfile(body, keyFor('create', body));
    createOpen.value = false;
    loadEditor(r.data);
    await loadReferences();
    await list();
  } finally {
    busy.value = false;
  }
}
async function save() {
  const p = selected.value;
  if (!p || busy.value) return;
  if (reason.value.trim().length < 2) {
    ElMessage.warning('请填写至少2字的变更原因');
    return;
  }
  const credentials: Record<string, object> = {};
  for (const slot of secretSlots.value) {
    const s = secrets[slot.key];
    if (s.action === 'REPLACE' && !s.value) {
      ElMessage.warning(`请填写${slot.label}`);
      return;
    }
    credentials[slot.key] =
      s.action === 'REPLACE' ? { action: s.action, value: s.value } : { action: s.action };
  }
  const clean = Object.fromEntries(
    Object.entries(config.value).filter(([, v]) => v !== '' && v !== undefined)
  );
  const body = {
    kind: p.kind,
    expectedVersion: p.version,
    config: clean,
    credentials,
    reason: reason.value.trim()
  };
  busy.value = true;
  try {
    const r = await api.saveDraft(p.profileId, body, keyFor('draft:' + p.profileId, body));
    loadEditor(r.data);
    ElMessage.success('草稿已保存，尚未生效');
    await list();
  } catch (e: any) {
    check.value = null;
    if (e.errorCode === 'VI_VERSION_CONFLICT')
      error.value = '配置已由其他管理员修改，请重新选择配置加载最新版本';
  } finally {
    busy.value = false;
  }
}
async function validate() {
  const p = selected.value;
  if (!p?.draftRevisionId || dirty.value || busy.value) return;
  busy.value = true;
  try {
    const body = { revisionId: p.draftRevisionId, expectedVersion: p.version, level: 'LOCAL' };
    const r = await api.action<api.Check>(
      p.profileId,
      'checks',
      body,
      keyFor('check:' + p.profileId, body)
    );
    check.value = r.data;
    issues.value = r.data.issues;
    ElMessage[r.data.status === 'PASS' ? 'success' : 'warning'](
      r.data.status === 'PASS' ? '本地校验通过，可显式发布' : '校验未通过，请按字段提示修正'
    );
  } finally {
    busy.value = false;
  }
}
// 保存和本地校验均不生效；显式发布使用当前修订及校验票据，双关须再次确认。
async function publish() {
  const p = selected.value,
    c = check.value;
  if (!p?.draft || !c || c.status !== 'PASS' || dirty.value || busy.value) return;
  if (reason.value.trim().length < 2) {
    ElMessage.warning('请填写发布原因');
    return;
  }
  const bothOff =
    p.kind === 'PAYMENT_POLICY' && !config.value.offlineEnabled && !config.value.wechatEnabled;
  try {
    await ElMessageBox.confirm(
      bothOff
        ? '两种支付均关闭后将暂停新付费预约。已有付款和退款仍需处理。确认发布？'
        : '发布后新操作将使用此版本，历史交易保留原收款身份。确认发布？',
      '发布配置',
      { type: bothOff ? 'warning' : 'info' }
    );
  } catch {
    return;
  }
  busy.value = true;
  try {
    const body = {
      revisionId: p.draftRevisionId,
      expectedVersion: p.version,
      contentHash: p.draft.contentHash,
      checkId: c.checkId,
      reason: reason.value.trim(),
      acknowledgeNoPaymentMethods: bothOff
    };
    await api.action(p.profileId, 'publish', body, keyFor('publish:' + p.profileId, body));
    const r = await api.profile(p.profileId);
    loadEditor(r.data);
    ElMessage.success('发布成功，已生效');
    await list();
  } catch {
    check.value = null;
  } finally {
    busy.value = false;
  }
}
async function showHistory() {
  const p = selected.value;
  if (!p) return;
  historyOpen.value = true;
  const r = await api.publications(p.profileId, historyPage.value);
  if (selected.value?.profileId === p.profileId) {
    history.value = r.rows;
    historyTotal.value = r.total;
  }
}
onMounted(async () => {
  await list();
  try {
    const r = await listDept();
    departments.value = (r.data || [])
      .filter((x) => x.status === '0' && x.deptId != null)
      .map((x) => ({ id: String(x.deptId), name: x.deptName || String(x.deptId) }));
  } catch {
    error.value = '无法读取部门选项，请联系管理员配置部门查询权限';
  }
});
onBeforeUnmount(resetEditor);
onDeactivated(resetEditor);
</script>
<template>
  <div class="app-container payment-settings">
    <h2>支付方式与微信商户</h2>
    <p class="muted">同公司多站点共用收款配置。保存草稿后，需校验并发布才会生效。</p>
    <el-radio-group :model-value="kind" :disabled="busy" @change="changeKind"
      ><el-radio-button value="PAYMENT_POLICY">支付设置</el-radio-button
      ><el-radio-button value="WECHAT_PAY">微信商户</el-radio-button
      ><el-radio-button value="WECHAT_APP">小程序凭据</el-radio-button></el-radio-group
    >
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <div class="toolbar">
      <el-button
        v-hasPermi="['biz:integration:create']"
        type="primary"
        :disabled="busy"
        @click="openCreate"
        >新增{{ labels[kind] }}</el-button
      ><el-button :disabled="busy" @click="list">刷新列表</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" border
      ><el-table-column label="名称" prop="name" min-width="160" /><el-table-column
        label="公司部门"
        min-width="150"
        ><template #default="s">{{ company(s.row.scope.ownerDeptId) }}</template></el-table-column
      ><el-table-column label="环境" width="90"
        ><template #default="s">{{
          s.row.scope.environment === 'LIVE' ? '生产' : '测试'
        }}</template></el-table-column
      ><el-table-column label="生效版本" min-width="110"
        ><template #default="s">{{
          s.row.activeRevisionId || '尚未发布'
        }}</template></el-table-column
      ><el-table-column label="操作" width="100"
        ><template #default="s"
          ><el-button link type="primary" :disabled="busy" @click="select(s.row)"
            >维护</el-button
          ></template
        ></el-table-column
      ></el-table
    >
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="10"
      layout="prev, pager, next"
      @current-change="list"
    />
    <el-card v-if="selected" class="editor" :key="selected.profileId">
      <template #header
        ><div class="editor-head">
          <strong>{{ selected.name }}</strong
          ><el-button
            :disabled="busy"
            @click="
              historyPage = 1;
              showHistory();
            "
            >发布历史</el-button
          >
        </div></template
      >
      <el-descriptions :column="2" border
        ><el-descriptions-item label="收款公司">{{
          company(selected.scope.ownerDeptId)
        }}</el-descriptions-item
        ><el-descriptions-item label="环境">{{
          selected.scope.environment === 'LIVE' ? '生产' : '测试'
        }}</el-descriptions-item
        ><el-descriptions-item label="生效版本">{{
          selected.activeRevisionId || '尚未发布'
        }}</el-descriptions-item
        ><el-descriptions-item label="草稿版本"
          >{{ selected.draftRevisionId || '尚未保存'
          }}{{ dirty ? ' · 存在未保存修改' : '' }}</el-descriptions-item
        ></el-descriptions
      >
      <el-form label-position="top" :disabled="busy" @input="formChanged" @change="formChanged">
        <template v-if="kind === 'PAYMENT_POLICY'">
          <div class="switches">
            <el-form-item label="线下支付"
              ><el-switch v-model="config.offlineEnabled" @change="changed" /></el-form-item
            ><el-form-item label="微信支付"
              ><el-switch v-model="config.wechatEnabled" @change="changed"
            /></el-form-item>
          </div>
          <el-alert
            v-if="!config.offlineEnabled && !config.wechatEnabled"
            title="两种支付均关闭：发布后暂停新付费预约，不视为免费。"
            type="warning"
            :closable="false"
          />
          <el-form-item v-if="config.wechatEnabled" label="已发布微信商户" required
            ><el-select
              :model-value="config.wechatMerchant?.profileId"
              placeholder="选择同公司同环境商户"
              clearable
              @change="setReference"
              ><el-option
                v-for="p in references"
                :key="p.profileId"
                :value="p.profileId"
                :label="p.name + ' · 版本 ' + p.activeRevisionId" /></el-select
            ><span class="muted">未找到时，请先维护微信商户并发布。</span></el-form-item
          >
          <template v-if="config.offlineEnabled"
            ><el-alert
              title="仅支持企业预收款核实。选择线下不算付款，不允许代驾个人代收。"
              type="info"
              :closable="false" /><el-form-item label="企业收款方式" required
              ><el-checkbox-group v-model="config.offlineMethods" @change="changed"
                ><el-checkbox value="CASH">公司柜台现金</el-checkbox
                ><el-checkbox value="BANK_TRANSFER">对公转账</el-checkbox></el-checkbox-group
              ></el-form-item
            ><el-form-item label="收款主体名称" required
              ><el-input v-model="config.offlineReceiverName" maxlength="200" /></el-form-item
            ><el-form-item label="收款说明及凭证要求" required
              ><el-input
                v-model="config.offlineInstructions"
                type="textarea"
                :rows="3"
                maxlength="1000"
            /></el-form-item>
            <div class="columns">
              <el-form-item label="企业联系人" required
                ><el-input v-model="config.offlineContactName" maxlength="80" /></el-form-item
              ><el-form-item label="企业联系电话" required
                ><el-input v-model="config.offlineContactPhone" maxlength="30"
              /></el-form-item>
            </div>
            <div v-if="config.offlineMethods?.includes('BANK_TRANSFER')" class="columns">
              <el-form-item label="开户行" required
                ><el-input v-model="config.offlineBankName" maxlength="100" /></el-form-item
              ><el-form-item label="对公户名" required
                ><el-input v-model="config.offlineAccountName" maxlength="200"
              /></el-form-item></div
          ></template>
        </template>
        <template v-if="kind === 'WECHAT_PAY'">
          <div class="columns">
            <el-form-item label="商户名称" required
              ><el-input v-model="config.merchantName" maxlength="200" /></el-form-item
            ><el-form-item label="商户号 mchid" required
              ><el-input v-model="config.mchid" maxlength="32"
            /></el-form-item>
          </div>
          <el-form-item label="已发布小程序" required
            ><el-select
              :model-value="config.appProfile?.profileId"
              placeholder="选择同公司同环境小程序"
              clearable
              @change="setReference"
              ><el-option
                v-for="p in references"
                :key="p.profileId"
                :value="p.profileId"
                :label="p.name + ' · 版本 ' + p.activeRevisionId" /></el-select></el-form-item
          ><el-checkbox v-model="config.bindingAcknowledged" @change="changed"
            >已核实该小程序 AppID 与商户关联</el-checkbox
          >
          <el-form-item label="商户 API 证书序列号" required
            ><el-input v-model="config.merchantCertificateSerial" maxlength="128" /></el-form-item
          ><el-form-item label="商户 API 证书 PEM（可选，用于核对配对）"
            ><el-input
              v-model="config.merchantCertificatePem"
              type="textarea"
              :rows="3"
              maxlength="32768"
          /></el-form-item>
          <el-form-item label="微信支付验签方式" required
            ><el-radio-group v-model="config.verificationMode" @change="changed"
              ><el-radio value="WECHATPAY_PUBLIC_KEY">微信支付公钥</el-radio
              ><el-radio value="PLATFORM_CERT">平台证书</el-radio></el-radio-group
            ></el-form-item
          >
          <template v-if="config.verificationMode === 'WECHATPAY_PUBLIC_KEY'"
            ><el-form-item label="微信支付公钥 ID" required
              ><el-input v-model="config.wechatPayPublicKeyId" maxlength="128" /></el-form-item
            ><el-form-item label="微信支付公钥 PEM" required
              ><el-input
                v-model="config.wechatPayPublicKeyPem"
                type="textarea"
                :rows="3"
                maxlength="16384" /></el-form-item
          ></template>
          <template v-else
            ><div v-for="(cert, i) in config.platformCertificates" :key="i" class="certificate">
              <el-form-item label="平台证书序列号" required
                ><el-input v-model="cert.serial" /></el-form-item
              ><el-form-item label="平台证书 PEM" required
                ><el-input
                  v-model="cert.pem"
                  type="textarea"
                  :rows="3"
                  maxlength="32768" /></el-form-item
              ><el-button
                @click="
                  config.platformCertificates.splice(i, 1);
                  changed();
                "
                >移除此证书</el-button
              >
            </div>
            <el-button
              @click="
                (config.platformCertificates ||= []).push({ serial: '', pem: '' });
                changed();
              "
              >添加平台证书</el-button
            ></template
          >
          <el-form-item label="支付通知地址"
            ><el-input
              :model-value="selected.generatedPayNotifyUrl || '部署域名尚未配置'"
              readonly
            /><el-button
              :disabled="!selected.generatedPayNotifyUrl"
              @click="copyUrl(selected.generatedPayNotifyUrl)"
              >复制支付通知地址</el-button
            ></el-form-item
          ><el-form-item label="退款通知地址"
            ><el-input
              :model-value="selected.generatedRefundNotifyUrl || '部署域名尚未配置'"
              readonly
            /><el-button
              :disabled="!selected.generatedRefundNotifyUrl"
              @click="copyUrl(selected.generatedRefundNotifyUrl)"
              >复制退款通知地址</el-button
            ></el-form-item
          >
          <el-alert
            title="回调地址由后端可信部署域名生成，不能在此改为任意外部地址。已有交易仍使用原商户身份。"
            type="info"
            :closable="false"
          />
        </template>
        <template v-if="kind === 'WECHAT_APP'"
          ><el-form-item label="启用小程序"
            ><el-switch v-model="config.enabled" @change="changed" /></el-form-item
          ><el-form-item label="小程序名称" required
            ><el-input v-model="config.appName" maxlength="100" /></el-form-item
          ><el-form-item label="小程序 AppID" required
            ><el-input v-model="config.appId" maxlength="64" /></el-form-item
        ></template>
        <div v-for="slot in secretSlots" :key="slot.key" class="secret">
          <el-form-item :label="slot.label"
            ><span class="muted">{{ secretState(slot.key) }}</span
            ><el-select
              v-model="secrets[slot.key].action"
              @change="
                secrets[slot.key].value = '';
                changed();
              "
              ><el-option label="保持原值" value="KEEP" /><el-option
                label="替换为新值"
                value="REPLACE" /><el-option label="清除未发布凭据" value="CLEAR" /></el-select
            ><el-input
              v-if="secrets[slot.key].action === 'REPLACE'"
              v-model="secrets[slot.key].value"
              type="password"
              autocomplete="new-password"
              :placeholder="'粘贴' + slot.label"
              maxlength="32768"
          /></el-form-item>
        </div>
        <el-alert
          title="凭据只写不回显；保存成功、切换配置或离开页面后清除输入。AppSecret 与支付 API v3 密钥用途不同。"
          type="info"
          :closable="false"
        />
        <el-form-item data-reason label="变更 / 发布原因" required
          ><el-input v-model="reason" maxlength="500" placeholder="说明此次修改用途"
        /></el-form-item>
      </el-form>
      <div v-if="issues.length" class="issues">
        <h3>校验提示</h3>
        <div
          v-for="(issue, i) in issues"
          :key="i"
          :class="issue.severity === 'ERROR' ? 'error' : 'muted'"
        >
          <strong>{{ issue.field }}</strong
          >：{{ issue.message }}
        </div>
      </div>
      <div class="toolbar">
        <el-button v-hasPermi="['biz:integration:edit']" :disabled="busy" @click="save"
          >保存草稿</el-button
        ><el-button
          v-hasPermi="['biz:integration:check']"
          :disabled="busy || dirty || !selected.draftRevisionId"
          @click="validate"
          >校验草稿</el-button
        ><el-button
          v-hasPermi="['biz:integration:publish']"
          type="primary"
          :disabled="busy || dirty || check?.status !== 'PASS'"
          @click="publish"
          >发布生效</el-button
        ><span class="muted">本地校验不代表真实支付已验证。</span>
      </div>
    </el-card>
    <el-dialog
      v-model="createOpen"
      :title="'新增' + labels[kind]"
      width="min(520px, 94vw)"
      :close-on-click-modal="false"
      ><el-form label-position="top"
        ><el-form-item label="配置名称" required
          ><el-input v-model="newProfile.name" maxlength="100" /></el-form-item
        ><el-form-item label="收款公司部门" required
          ><el-select v-model="newProfile.ownerDeptId" filterable
            ><el-option
              v-for="d in departments"
              :key="d.id"
              :value="d.id"
              :label="d.name" /></el-select></el-form-item
        ><el-form-item label="运行环境" required
          ><el-radio-group v-model="newProfile.environment"
            ><el-radio value="TEST">测试</el-radio
            ><el-radio value="LIVE">生产</el-radio></el-radio-group
          ></el-form-item
        >
        <p class="muted">公司及环境创建后不能更换；当前部署仅允许对应环境。</p></el-form
      ><template #footer
        ><el-button :disabled="busy" @click="createOpen = false">取消</el-button
        ><el-button type="primary" :loading="busy" @click="create">创建配置</el-button></template
      ></el-dialog
    >
    <el-dialog v-model="historyOpen" title="发布历史" width="min(840px, 94vw)"
      ><el-table :data="history"
        ><el-table-column prop="revisionId" label="版本" /><el-table-column
          prop="action"
          label="操作" /><el-table-column prop="reason" label="原因" /><el-table-column
          prop="publishedAt"
          label="发布时间"
          min-width="180" /></el-table
      ><el-pagination
        v-model:current-page="historyPage"
        :total="historyTotal"
        :page-size="10"
        layout="prev, pager, next"
        @current-change="showHistory"
    /></el-dialog>
  </div>
</template>
<style scoped>
.payment-settings {
  max-width: 1200px;
}
.muted {
  color: #64748b;
  line-height: 1.7;
}
.toolbar,
.editor-head,
.switches {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.toolbar {
  margin: 20px 0;
}
.editor-head {
  justify-content: space-between;
}
.editor {
  margin-top: 24px;
}
.columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.el-form {
  margin-top: 24px;
}
.el-select {
  width: 100%;
}
.secret .el-input,
.secret .el-select {
  margin-top: 8px;
}
.issues {
  padding: 16px;
  background: #f8fafc;
  line-height: 1.8;
}
.error {
  color: #b42318;
}
.certificate {
  padding: 16px;
  border: 1px solid #e2e8f0;
  margin: 12px 0;
}
.el-alert {
  margin: 16px 0;
}
.el-pagination {
  margin-top: 16px;
}
@media (max-width: 640px) {
  .columns {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .payment-settings {
    padding: 14px;
  }
}
</style>
