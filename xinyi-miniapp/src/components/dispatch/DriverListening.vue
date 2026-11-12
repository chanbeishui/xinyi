<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { currentSession, onSessionChanged } from '../../services/http';
import {
  getAvailability,
  runDispatchIntent,
  pendingDispatch,
  newDispatchIntent,
  observations,
  dispatchScope,
  dispatchLabel,
  dispatchTime,
  type Availability,
  type DispatchIntent
} from '../../features/dispatch/api';
import { collectForegroundPoint, locationPlatform } from '../../features/dispatch/location';
const emit = defineEmits<{ (event: 'access-lost', message: string): void }>();
const props = defineProps<{ backgroundAllowed?: () => boolean }>();
const state = ref<Availability | null>(null),
  loading = ref(false),
  busy = ref(false),
  error = ref(''),
  stale = ref(false),
  collecting = ref(false),
  unresolved = ref<DispatchIntent[]>([]);
const owner = dispatchScope();
let disposed = false,
  sequence = 0,
  timer: ReturnType<typeof setTimeout> | null = null;
let presenceFlight: Promise<boolean> | null = null;
let source: Availability | null = null;
let lastPointRequest = 0;
let projectionTimer: ReturnType<typeof setTimeout> | null = null;
const unsubscribeSession = onSessionChanged(() => {
  if (sameOwner()) return;
  sequence++;
  stopTimer();
  collecting.value = false;
  source = null;
  state.value = null;
  unresolved.value = [];
  emit('access-lost', '登录或工作身份已变化，请重新核对当前工作信息');
});
const locked = computed(
  () => loading.value || busy.value || stale.value || unresolved.value.length > 0
);
const nativeLocation = locationPlatform();
const has = (action: string) => Boolean(state.value?.allowedActions.includes(action));
const heading = computed(() =>
  !state.value
    ? loading.value
      ? '正在核对听单条件'
      : '有效状态尚未取得'
    : state.value.occupied
      ? '有在手任务或待响应邀约'
      : state.value.listeningEnabled
        ? '已开启听单意愿'
        : '当前未开启听单'
);
function current() {
  if (disposed) return false;
  try {
    return dispatchScope() === owner;
  } catch {
    return false;
  }
}
function sameOwner() {
  try {
    return dispatchScope() === owner;
  } catch {
    return false;
  }
}
function syncPending() {
  unresolved.value = ['LISTENING', 'LOCATION', 'PRESENCE'].flatMap((action) => {
    const intent = pendingDispatch(action as DispatchIntent['action']);
    return intent ? [intent] : [];
  });
}
function stopTimer() {
  if (timer !== null) clearTimeout(timer);
  timer = null;
}
function clearProjection() {
  stopTimer();
  if (projectionTimer !== null) clearTimeout(projectionTimer);
  projectionTimer = null;
  source = null;
  state.value = null;
  collecting.value = false;
}
function armProjectionExpiry(value: Availability) {
  if (projectionTimer !== null) clearTimeout(projectionTimer);
  projectionTimer = null;
  const deadlines = [
    ...(value.locationFresh ? [value.locationExpiresAt] : []),
    ...(value.clientReachable ? [value.reachabilityExpiresAt] : []),
    ...(value.listeningEnabled ? [value.listeningUntil] : [])
  ];
  if (!deadlines.length) return;
  const remaining =
    Math.min(...deadlines.map((date) => (date ? Date.parse(date) : NaN))) - Date.now();
  const invalidate = () => {
    if (!current() || source !== value) return;
    // 客户端只能撤回缓存，不能用本地时钟授予可接单资格、续租或改写后台听单意愿。
    clearProjection();
    error.value = '上次核对的有效状态已到期或期限未取得，请重新读取；听单意愿未在客户端改变。';
  };
  if (!Number.isFinite(remaining) || remaining <= 0) invalidate();
  else projectionTimer = setTimeout(invalidate, Math.min(remaining, 2147483647));
}
function setState(value: Availability) {
  source = value;
  if (current()) {
    state.value = value;
    armProjectionExpiry(value);
  }
}
function failureMessage(failure: unknown) {
  return failure instanceof Error ? failure.message : '操作尚未完成，请重新核对';
}
function fail(failure: unknown) {
  if (!current()) return;
  error.value = failureMessage(failure);
  const status = (failure as { status?: number }).status;
  // 写未知、GET失败及采点拒绝都撤回旧有效事实；保留原请求体/UUID供核对，不假装关听单。
  clearProjection();
  if (status === 409) stale.value = true;
  if ([401, 403, 404].includes(status || 0)) {
    emit('access-lost', error.value);
  }
}
async function read() {
  const request = ++sequence;
  const value = await getAvailability();
  if (!current() || request !== sequence) return null;
  setState(value);
  stale.value = false;
  return value;
}
function schedule() {
  stopTimer();
  const interval = source?.locationReportIntervalSeconds;
  if (
    !current() ||
    !source?.listeningEnabled ||
    unresolved.value.length ||
    stale.value ||
    !Number.isInteger(interval) ||
    interval! < 30 ||
    interval! > 3600
  )
    return;
  // 间隔来自公司已生效参数；串行上报，不补报后台离线时段，也不自行续期听单窗口。
  timer = setTimeout(() => {
    void tick();
  }, interval! * 1000);
}
async function perform(intent: DispatchIntent, refresh = true) {
  if (!sameOwner()) return false;
  if (current()) {
    busy.value = true;
    error.value = '';
  }
  try {
    const result = await runDispatchIntent(intent);
    if (!sameOwner()) return false;
    if ('eligibilityScope' in result) setState(result);
    else if (source) {
      if (intent.action === 'LOCATION') source.locationVersion = result.version;
      if (intent.action === 'PRESENCE') source.presenceVersion = result.version;
    }
    if (refresh && current()) await read();
    return true;
  } catch (failure) {
    fail(failure);
    return false;
  } finally {
    if (current()) {
      busy.value = false;
      syncPending();
    }
  }
}
async function presence(
  clientState: 'FOREGROUND' | 'BACKGROUND',
  refresh = true,
  observedAt = new Date().toISOString()
) {
  if (!source || !sameOwner() || pendingDispatch('PRESENCE')) return false;
  if (!source.allowedActions.includes('REPORT_DRIVER_PRESENCE')) return false;
  const intent = newDispatchIntent({
    action: 'PRESENCE',
    body: {
      ...observations(source),
      expectedVersion: source.presenceVersion,
      clientState,
      observedAt
    }
  });
  const flight = perform(intent, refresh);
  presenceFlight = flight;
  try {
    return await flight;
  } finally {
    if (presenceFlight === flight) presenceFlight = null;
  }
}
async function point() {
  if (!current() || !source || !collecting.value || pendingDispatch('LOCATION')) return;
  const request = sequence,
    observed = source;
  // 30秒是本系统的最短采点间隔，不声称等于微信平台统一限额；原生限频/拒绝仍按失败处理。
  if (Date.now() - lastPointRequest < 30000) return;
  lastPointRequest = Date.now();
  const sample = await collectForegroundPoint(
    () => current() && collecting.value && request === sequence
  );
  if (!current() || !collecting.value || request !== sequence || !source) return;
  const intent = newDispatchIntent({
    action: 'LOCATION',
    body: {
      ...sample,
      ...observations(observed),
      expectedVersion: observed.locationVersion
    }
  });
  await perform(intent);
}
async function tick() {
  if (!current() || locked.value) return;
  try {
    await presence('FOREGROUND');
    if (current() && !locked.value && source?.listeningEnabled && collecting.value) await point();
    if (current() && !locked.value) await read();
  } catch (failure) {
    fail(failure);
  } finally {
    if (current()) {
      syncPending();
      schedule();
    }
  }
}
async function refresh() {
  if (!current() || busy.value) return;
  loading.value = true;
  error.value = '';
  try {
    await read();
  } catch (failure) {
    fail(failure);
  } finally {
    if (current()) {
      loading.value = false;
      syncPending();
      schedule();
    }
  }
}
async function toggle(enabled: boolean) {
  if (!current() || !source || locked.value) return;
  const action = enabled ? 'ENABLE_LISTENING' : 'DISABLE_LISTENING';
  if (!source.allowedActions.includes(action)) return;
  stopTimer();
  const intent = newDispatchIntent({
    action: 'LISTENING',
    body: {
      ...observations(source),
      enabled,
      expectedVersion: source.listeningVersion,
      expectedConfigVersion: source.configVersion,
      expectedBusinessDate: source.businessDate
    }
  });
  await perform(intent);
  if (current()) schedule();
}
async function startLocation() {
  if (!nativeLocation || locked.value || !has('REPORT_DRIVER_LOCATION')) return;
  collecting.value = true;
  try {
    await point();
  } catch (failure) {
    collecting.value = false;
    fail(failure);
  } finally {
    if (current()) schedule();
  }
}
function stopLocation() {
  collecting.value = false;
}
async function retry(intent: DispatchIntent) {
  if (!current() || busy.value || loading.value) return;
  stopTimer();
  await perform(intent);
  if (current()) schedule();
}
onMounted(async () => {
  syncPending();
  loading.value = true;
  try {
    await read();
    if (current() && !unresolved.value.length) await presence('FOREGROUND');
  } catch (failure) {
    fail(failure);
  } finally {
    if (current()) {
      loading.value = false;
      syncPending();
      schedule();
    }
  }
});
onBeforeUnmount(() => {
  const hiddenAt = new Date().toISOString();
  disposed = true;
  unsubscribeSession();
  sequence++;
  stopTimer();
  if (projectionTimer !== null) clearTimeout(projectionTimer);
  projectionTimer = null;
  collecting.value = false;
  // 仅尽力报告前后台，不关听单。切身份、无版本或未知原请求时不拼新体；服务器租约负责失联兜底。
  void (async () => {
    if (props.backgroundAllowed?.() === false) return;
    if (presenceFlight) await presenceFlight;
    if (sameOwner() && source && !pendingDispatch('PRESENCE'))
      await presence('BACKGROUND', false, hiddenAt);
  })();
  state.value = null;
  unresolved.value = [];
  if (currentSession()?.activeRole !== 'DRIVER') pendingDispatch('LOCATION');
});
</script>
<template>
  <view class="listening-panel">
    <view class="panel-heading"
      ><text class="section">听单工作台</text><text class="badge">{{ heading }}</text></view
    >
    <text v-if="loading" class="muted">正在按当前代驾身份核对服务端条件…</text>
    <text v-if="!loading && !state" class="warning"
      >当前有效状态尚未取得，请重新核对。页面不会据此推断听单已关闭或车辆责任已解除。</text
    >
    <template v-if="state">
      <text class="muted">所属公司：{{ state.companyName || '尚未配置' }}</text>
      <text>已完成服务单量：{{ state.completedAssignmentCount }} 单（实际归还承接）</text>
      <text class="muted"
        >本人派单邀约与当前承接入口已接通；此处占用摘要不会代替邀约响应或解除已有服务责任。</text
      >
      <view class="intent-card">
        <text class="state-title">{{
          state.listeningEnabled ? '听单意愿已开启' : '听单意愿已关闭'
        }}</text>
        <text>公司每日下班：{{ state.closingTime || '尚未配置' }}（北京时间）</text>
        <text>当前业务日：{{ state.businessDate }}</text>
        <text>本次听单截止：{{ dispatchTime(state.listeningUntil) }}</text>
        <text class="muted"
          >切到后台保留听单意愿；到公司每日下班时间自动关闭，次日须本人手动开启。</text
        >
        <button
          v-if="has('ENABLE_LISTENING')"
          class="primary"
          :disabled="locked"
          @click="toggle(true)"
        >
          开启今日听单
        </button>
        <button v-if="has('DISABLE_LISTENING')" :disabled="locked" @click="toggle(false)">
          关闭听单
        </button>
        <text v-if="!state.configurationReady" class="warning"
          >公司听单配置未就绪，请联系有权限的管理员；不会套用默认下班时间。</text
        >
      </view>
      <view class="conditions">
        <text class="section">上次服务端核对</text>
        <view
          ><text>公司新邀约许可</text
          ><text>{{ state.newAssignmentEnabled ? '已允许' : '已暂停' }}</text></view
        >
        <view
          ><text>资格条件（上次核对）</text
          ><text>{{ state.qualificationValid ? '当前有效' : '当前不满足' }}</text></view
        >
        <text v-if="state.qualificationReason" class="muted">{{
          dispatchLabel(state.qualificationReason)
        }}</text>
        <view
          ><text>占用状态</text><text>{{ dispatchLabel(state.occupationType) }}</text></view
        >
        <text v-if="state.holderId" class="muted">占用记录：{{ state.holderId }}</text>
        <view
          ><text>位置状态</text><text>{{ dispatchLabel(state.locationStatus) }}</text></view
        >
        <view
          ><text>位置仍在有效期</text><text>{{ state.locationFresh ? '是' : '否' }}</text></view
        >
        <view
          ><text>位置精度</text
          ><text
            >{{ state.accuracyMeter === null ? '尚未确认' : state.accuracyMeter + '米' }}
            ·
            {{ state.locationAccuracyValid ? '符合配置' : '不符合或未知' }}</text
          ></view
        >
        <view
          ><text>近期可达记录</text><text>{{ dispatchLabel(state.reachabilityStatus) }}</text></view
        >
        <view
          ><text>最近客户端报告</text><text>{{ dispatchLabel(state.clientState) }}</text></view
        >
      </view>
      <view class="detail-times">
        <text>位置采点：{{ dispatchTime(state.locationCapturedAt) }}</text>
        <text>服务端收到：{{ dispatchTime(state.locationReceivedAt) }}</text>
        <text>位置有效至：{{ dispatchTime(state.locationExpiresAt) }}</text>
        <text>近期联系有效至：{{ dispatchTime(state.reachabilityExpiresAt) }}</text>
      </view>
      <view class="base-status" :class="{ ready: state.availableForNewOrder }">
        <text class="section">{{
          state.availableForNewOrder
            ? '最近核对：基础候选条件满足'
            : '最近核对：暂不满足基础候选条件'
        }}</text>
        <text>{{ dispatchLabel(state.reason) }}</text>
        <text class="muted"
          >基础条件满足仍需具体订单的车型、半径等匹配与接受校验。本页不代表已派单或已接单。</text
        >
      </view>
      <view class="location-actions">
        <text class="section">前台位置更新</text>
        <text class="muted"
          >仅此工作台在前台且主动启用时采点，按公司间隔上报最近位置；离开即停止，不持续后台定位或展示轨迹。</text
        >
        <text v-if="!nativeLocation" class="warning"
          >网页端仅查看服务端状态。采集GCJ02位置需使用微信真机并完成隐私及位置授权。</text
        >
        <button
          v-if="nativeLocation && !collecting"
          :disabled="locked || !has('REPORT_DRIVER_LOCATION')"
          @click="startLocation"
        >
          授权并启用前台位置更新
        </button>
        <button v-if="collecting" @click="stopLocation">停止前台位置更新</button>
        <text v-if="collecting" class="muted"
          >已启用前台更新，间隔{{
            state.locationReportIntervalSeconds
          }}秒；失败或过期不会显示为实时位置。</text
        >
      </view>
      <text class="warning"
        >关闭听单、到期或暂停只影响新邀约；已有邀约、承接、占用与车辆保管归还责任不会自动解除。</text
      >
    </template>
    <view v-for="intent in unresolved" :key="intent.action" class="unknown">
      <text
        >原{{
          intent.action === 'LISTENING'
            ? '听单操作'
            : intent.action === 'LOCATION'
              ? '位置上报'
              : '前后台上报'
        }}结果尚未确认。保留原目标、时间、版本及请求键核对，不生成新请求覆盖。</text
      >
      <button :disabled="busy || loading" @click="retry(intent)">核对并重试原操作</button>
    </view>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <text v-if="stale" class="warning">记录已变化，请重读状态后再决定下一步。</text>
    <button :disabled="busy || loading" @click="refresh">
      {{ stale ? '重读听单状态' : '刷新听单状态' }}
    </button>
  </view>
</template>
<style scoped>
.listening-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-heading {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
}
.section {
  font-size: 17px;
  font-weight: 600;
}
.badge {
  padding: 4px 9px;
  border-radius: 20px;
  color: #31577d;
  background: #eef5ff;
  font-size: 12px;
}
.intent-card,
.location-actions,
.base-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 10px;
  background: #f4f7fb;
}
.state-title {
  font-size: 20px;
  font-weight: 600;
  color: #005fcb;
}
.conditions {
  display: flex;
  flex-direction: column;
  gap: 11px;
}
.conditions > view {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}
.conditions > view > text:last-child {
  text-align: right;
  color: #31577d;
}
.detail-times,
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.8;
}
.detail-times {
  display: flex;
  flex-direction: column;
}
.warning,
.unknown {
  padding: 12px;
  border-radius: 8px;
  background: #fff4e5;
  color: #854d0e;
  font-size: 13px;
  line-height: 1.8;
}
.unknown {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.base-status.ready {
  background: #eef8f1;
}
.error {
  color: #b42318;
  line-height: 1.7;
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
