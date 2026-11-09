<script setup lang="ts">
import { computed, ref } from 'vue';
import { onHide, onShow, onUnload } from '@dcloudio/uni-app';
import { getQualification, type MyQualification } from '../../features/qualification/api';
import { getPublicServiceConfig, type PublicServiceConfig } from '../../features/config/api';
import { returnToTraining } from '../../features/training/api';
import { intentKey } from '../../features/file/api';
import {
  deviceAuthPlatform,
  enrolledDeviceModes,
  authenticateDevice
} from '../../features/verification/device';
import {
  getDeviceSession,
  runDeviceIntent,
  pendingDeviceIntent,
  verificationScope,
  type DeviceSession,
  type DeviceIntent
} from '../../features/verification/api';
import {
  verificationStatus,
  verificationType,
  verificationTime
} from '../../features/verification/labels';
const application = ref<MyQualification | null>(null);
const config = ref<PublicServiceConfig | null>(null);
const device = ref<DeviceSession | null>(null);
const loading = ref(false),
  busy = ref(false),
  error = ref(''),
  notice = ref('');
const accepted = ref(false),
  acceptedVersion = ref(''),
  acceptedHash = ref('');
const unknown = ref(false),
  stale = ref(false);
const platformReady = deviceAuthPlatform();
const consentReady = computed(() =>
  Boolean(
    config.value?.verificationConsentVersion &&
      config.value?.verificationConsentText &&
      /^[a-f0-9]{64}$/.test(config.value?.verificationConsentHash || '')
  )
);
const consentAccepted = computed(
  () =>
    accepted.value &&
    acceptedVersion.value === config.value?.verificationConsentVersion &&
    acceptedHash.value === config.value?.verificationConsentHash
);
const locked = computed(() => loading.value || busy.value || unknown.value || stale.value);
const canCreate = computed(() =>
  Boolean(
    application.value &&
      ['DRAFT', 'REJECTED'].includes(application.value.status) &&
      consentReady.value &&
      consentAccepted.value &&
      platformReady &&
      (!device.value || ['FAILED', 'EXPIRED', 'CLOSED'].includes(device.value.status))
  )
);
let visible = false,
  sequence = 0;
onShow(() => {
  visible = true;
  try {
    unknown.value = Boolean(pendingDeviceIntent());
  } catch {
    unknown.value = false;
  }
  load();
});
function clear() {
  visible = false;
  sequence++;
  application.value = null;
  config.value = null;
  device.value = null;
  loading.value = false;
  busy.value = false;
  error.value = '';
  notice.value = '';
  accepted.value = false;
  acceptedVersion.value = '';
  acceptedHash.value = '';
}
onHide(clear);
onUnload(clear);
function has(action: string) {
  return device.value?.allowedActions.includes(action) === true;
}
function invalidateContext() {
  sequence++;
  application.value = null;
  config.value = null;
  device.value = null;
  accepted.value = false;
  acceptedVersion.value = '';
  acceptedHash.value = '';
  pendingDeviceIntent(null);
  unknown.value = false;
  stale.value = true;
  loading.value = false;
  busy.value = false;
  notice.value = '';
  error.value = '身份已变化，请重新进入本机认证';
}
function requestCurrent(scope: string, run: number) {
  if (!visible || run !== sequence) return false;
  let current = '';
  try {
    current = verificationScope();
  } catch {}
  if (current !== scope) {
    // 丢弃迟到响应时同时清除旧投影；仅忽略返回值仍会留下上一身份的申请/挑战。
    invalidateContext();
    return false;
  }
  return true;
}
function readScope() {
  try {
    return verificationScope();
  } catch {
    invalidateContext();
    return null;
  }
}
function assertCurrent(value: DeviceSession, app: MyQualification) {
  if (
    value.applicationId !== app.applicationId ||
    value.companyRevision !== app.companyRevision ||
    value.materialRevision !== app.materialRevision
  )
    throw new Error('认证会话与当前申请或资料不一致，请重新核对');
}
function consentChange(event: { detail: { value: string[] } }) {
  accepted.value = event.detail.value.includes('accepted');
  acceptedVersion.value = accepted.value ? config.value?.verificationConsentVersion || '' : '';
  acceptedHash.value = accepted.value ? config.value?.verificationConsentHash || '' : '';
}
async function load() {
  if (busy.value || loading.value) return;
  const run = ++sequence;
  loading.value = true;
  error.value = '';
  device.value = null;
  application.value = null;
  let scope = '';
  try {
    scope = verificationScope();
    const app = await getQualification();
    if (!requestCurrent(scope, run)) return;
    application.value = app;
    const published = await getPublicServiceConfig();
    if (!requestCurrent(scope, run)) return;
    if (
      acceptedVersion.value !== published.verificationConsentVersion ||
      acceptedHash.value !== published.verificationConsentHash
    ) {
      accepted.value = false;
      acceptedVersion.value = '';
      acceptedHash.value = '';
    }
    config.value = published;
    if (app?.verificationSessionId) {
      const value = await getDeviceSession(app.verificationSessionId);
      if (!requestCurrent(scope, run)) return;
      assertCurrent(value, app);
      device.value = value;
      if (['PASSED', 'FAILED', 'EXPIRED', 'CLOSED'].includes(value.status)) {
        pendingDeviceIntent(null);
        unknown.value = false;
      } else unknown.value = Boolean(pendingDeviceIntent());
    }
    stale.value = false;
  } catch (e) {
    if (!visible || run !== sequence || (scope && !requestCurrent(scope, run))) return;
    const status = (e as { status?: number }).status;
    if (status === 409) stale.value = true;
    error.value = e instanceof Error ? e.message : '本人认证进度读取失败';
    if ([401, 403, 404].includes(status || 0)) {
      application.value = null;
      config.value = null;
      device.value = null;
      try {
        pendingDeviceIntent(null);
      } catch {}
      unknown.value = false;
    }
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}
async function execute(intent: DeviceIntent) {
  if (!application.value || busy.value) return;
  const scope = readScope();
  if (!scope) return;
  const app = application.value,
    run = ++sequence;
  busy.value = true;
  error.value = '';
  notice.value = '';
  pendingDeviceIntent(intent);
  unknown.value = true;
  try {
    const value = await runDeviceIntent(intent);
    if (!requestCurrent(scope, run)) return;
    assertCurrent(value, app);
    device.value = value;
    pendingDeviceIntent(null);
    unknown.value = false;
    notice.value =
      value.status === 'PASSED'
        ? '服务端已确认本次设备认证，其他条件仍分别判断。'
        : '设备认证状态已更新，以服务端结果为准。';
    const current = await getQualification();
    if (requestCurrent(scope, run)) {
      if (current?.applicationId === app.applicationId) application.value = current;
      else {
        device.value = null;
        stale.value = true;
      }
    }
  } catch (e) {
    if (!requestCurrent(scope, run)) return;
    const status = (e as { status?: number }).status;
    unknown.value = !status || status >= 500;
    if (!unknown.value) pendingDeviceIntent(null);
    stale.value = status === 409;
    if ([401, 403, 404].includes(status || 0)) {
      application.value = null;
      device.value = null;
      config.value = null;
    }
    error.value = e instanceof Error ? e.message : '设备签名校验尚未确认，请读取原会话或恢复原请求';
  } finally {
    if (visible && run === sequence) busy.value = false;
  }
}
async function create() {
  if (locked.value || !canCreate.value || !application.value || !config.value) return;
  const scope = readScope();
  if (!scope) return;
  const app = application.value,
    run = ++sequence;
  busy.value = true;
  error.value = '';
  try {
    await enrolledDeviceModes();
    if (!requestCurrent(scope, run)) return;
    busy.value = false;
    await execute({
      action: 'CREATE',
      key: intentKey(),
      body: {
        applicationId: app.applicationId,
        expectedApplicationVersion: app.version,
        expectedCompanyRevision: app.companyRevision,
        expectedMaterialRevision: app.materialRevision,
        consentVersion: config.value.verificationConsentVersion!,
        consentTextHash: config.value.verificationConsentHash!,
        acceptedConsent: true
      }
    });
  } catch (e) {
    if (requestCurrent(scope, run)) error.value = e instanceof Error ? e.message : '设备尚未就绪';
  } finally {
    if (visible && run === sequence) busy.value = false;
  }
}
async function authenticate() {
  const target = device.value;
  if (
    locked.value ||
    !platformReady ||
    !target?.challenge ||
    !has('START_DEVICE_AUTH') ||
    !has('CONFIRM_DEVICE_AUTH')
  )
    return;
  const scope = readScope();
  if (!scope) return;
  const run = ++sequence;
  busy.value = true;
  error.value = '';
  try {
    const modes = await enrolledDeviceModes();
    if (!requestCurrent(scope, run)) return;
    const signed = await authenticateDevice(target.challenge, modes, target.authContent);
    if (!requestCurrent(scope, run)) return;
    busy.value = false;
    // 原签名原样提交，只有微信服务端验签和业务上下文校验才能形成 DEVICE_AUTH 证明。
    await execute({
      action: 'CONFIRM',
      id: target.verificationSessionId,
      key: intentKey(),
      body: {
        expectedVersion: target.version,
        resultJSON: signed.resultJSON,
        resultJSONSignature: signed.resultJSONSignature
      }
    });
  } catch (e) {
    if (requestCurrent(scope, run)) error.value = e instanceof Error ? e.message : '设备认证未完成';
  } finally {
    if (visible && run === sequence) busy.value = false;
  }
}
function recover() {
  if (!readScope()) return;
  const target = device.value;
  if (!target || busy.value || loading.value || !has('RECOVER_DEVICE_AUTH')) return;
  const original = pendingDeviceIntent();
  if (original) {
    // 本页面仍持有未知请求时先恢复原意图；不能用新的恢复键绕过未确认的签名提交。
    retry();
    return;
  }
  execute({
    action: 'RECOVER',
    id: target.verificationSessionId,
    key: intentKey(),
    body: { expectedVersion: target.version, recover: true }
  });
}
function retry() {
  if (!readScope()) return;
  const intent = pendingDeviceIntent();
  if (intent && !busy.value && !loading.value && !stale.value) execute(intent);
}
</script>
<template>
  <view class="verification-screen">
    <text class="title">本机生物认证</text>
    <text class="note">仅确认当前设备使用者，不等同于身份证实名、人证比对或活体核验。</text>
    <text v-if="loading" class="muted">正在核对本人申请与各项进度…</text>
    <view v-if="application" class="card">
      <text class="section">{{ application.companyName || '所属公司尚未配置' }}</text>
      <text class="muted"
        >本次申请：{{ application.applicationId }} · 资料修订
        {{ application.materialRevision }}</text
      >
      <view v-for="check in application.checks" :key="check.type" class="check-row"
        ><text>{{ verificationType(check.type) }}</text
        ><text :class="check.status === 'PASSED' ? 'success' : 'muted'">{{
          verificationStatus(check.status)
        }}</text></view
      >
      <text class="muted"
        >本机认证、背景与驾驶证人工核验、培训分别判断，之后仍需提交最终资格审核。</text
      >
    </view>
    <view v-if="application" class="card">
      <text class="section">微信 SOTER 设备确认</text>
      <text v-if="!platformReady" class="note"
        >本机生物认证需使用微信真机，网页端只可查看服务端进度。</text
      >
      <text class="muted"
        >使用本机实际支持且已录入的指纹或人脸。取消、离开或设备回调成功都不会直接批准资格。</text
      >
      <template v-if="device">
        <text>认证状态：{{ verificationStatus(device.status) }}</text
        ><text class="muted">业务会话：{{ device.verificationSessionId }}</text
        ><text class="muted">挑战有效期：{{ verificationTime(device.expiresAt) }}</text>
        <text v-if="device.failureCode" class="muted">状态说明：{{ device.failureCode }}</text>
        <text v-if="device.status === 'UNKNOWN'" class="note"
          >原签名校验结果尚未确认。请查询或恢复原会话，不重新生成挑战或更换签名。</text
        >
        <text v-if="device.status === 'CLOSED'" class="note"
          >会话已封存，封存不会生成认证通过证明。</text
        >
        <button
          v-if="has('START_DEVICE_AUTH')"
          class="primary"
          :disabled="locked || !platformReady"
          @click="authenticate"
        >
          使用本机指纹或人脸确认
        </button>
        <button v-if="has('RECOVER_DEVICE_AUTH')" :disabled="loading || busy" @click="recover">
          恢复原签名校验
        </button>
      </template>
      <template v-if="!device || ['FAILED', 'EXPIRED', 'CLOSED'].includes(device.status)">
        <view v-if="consentReady" class="consent"
          ><text class="section">本机认证同意说明</text
          ><text class="muted">发布版本：{{ config?.verificationConsentVersion }}</text
          ><text class="muted">{{ config?.verificationConsentText }}</text
          ><checkbox-group @change="consentChange"
            ><label
              ><checkbox
                value="accepted"
                :checked="consentAccepted"
                :disabled="busy || unknown"
              />我已阅读并同意本次本机认证说明</label
            ></checkbox-group
          ></view
        >
        <text v-else class="note">尚未发布完整的本机认证同意说明，暂不可发起。</text>
        <button class="primary" :disabled="locked || !canCreate" @click="create">
          建立本次设备认证
        </button>
      </template>
    </view>
    <view v-if="!application && !loading && !error" class="card"
      ><text>请先建立本人的代驾资格申请。</text></view
    >
    <text v-if="unknown" class="note"
      >原请求结果尚未确认，原目标、正文与请求键已冻结；签名不写入本地存储。</text
    >
    <button v-if="unknown" :disabled="busy || loading || stale || !application" @click="retry">
      重试原认证请求
    </button>
    <text v-if="error" class="error" role="alert">{{ error }}</text
    ><text v-if="notice" class="success">{{ notice }}</text>
    <button :disabled="loading || busy" @click="load">刷新本人认证进度</button>
    <button @click="returnToTraining('/pages/qualification/index')">返回认证中心</button>
  </view>
</template>
<style scoped>
.verification-screen {
  box-sizing: border-box;
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 18px 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  font-size: 24px;
  font-weight: 600;
}
.card {
  min-width: 0;
  padding: 18px;
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: 1.7;
}
.section {
  font-size: 18px;
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
  overflow-wrap: anywhere;
}
.note {
  color: #854d0e;
  background: #fff4e5;
  padding: 12px;
  border-radius: 8px;
  line-height: 1.8;
}
.check-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.consent {
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: 1.8;
}
.success {
  color: #087443;
}
.error {
  color: #b42318;
}
button {
  width: 100%;
  margin: 0;
  font-size: 15px;
}
.primary {
  background: #005fcb;
  color: #fff;
}
</style>
