<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import { onSessionChanged } from '../../services/http';
import { createExpiryGuard } from '../../features/dispatch/expiry';
import {
  ensureInviteSession,
  inviteId,
  inviteScope,
  getInvite,
  createInviteIntent,
  runInviteIntent,
  pendingInvite,
  inviteLabel,
  inviteTime,
  straightDistance,
  type DriverInvite,
  type InviteDecision,
  type InviteIntent
} from '../../features/dispatch/invites';
const view = ref<DriverInvite | null>(null),
  pending = ref<InviteIntent | null>(null),
  reason = ref('');
const loading = ref(false),
  busy = ref(false),
  stale = ref(false),
  error = ref('');
let id = '',
  owner = '',
  foreground = false,
  sequence = 0;
const guard = createExpiryGuard(() => {
  view.value = null;
  stale.value = true;
  error.value = '原邀约显示截止已到，请刷新核对；不重新计算有效期或显示接受按钮。';
});
const locked = computed(() => loading.value || busy.value || stale.value || Boolean(pending.value));
const can = (action: string) =>
  Boolean(view.value?.status === 'WAIT_RESPONSE' && view.value.allowedActions.includes(action));
function current(run = sequence) {
  try {
    return foreground && run === sequence && inviteScope() === owner;
  } catch {
    return false;
  }
}
function clear() {
  sequence++;
  guard.clear();
  view.value = null;
  reason.value = '';
  loading.value = false;
  busy.value = false;
  stale.value = false;
}
function fill(value: DriverInvite) {
  view.value = value;
  guard.arm(value.status === 'WAIT_RESPONSE' ? [value.expiresAt] : []);
}
const unsubscribe = onSessionChanged(() => {
  let next = '';
  try {
    next = inviteScope();
  } catch {}
  if (next !== owner) {
    clear();
    pending.value = null;
  }
});
onLoad((options) => {
  try {
    id = inviteId(String(options?.inviteId || ''));
  } catch (failure) {
    error.value = String((failure as Error).message);
  }
});
onShow(() => {
  foreground = true;
  void load();
});
onHide(() => {
  foreground = false;
  clear();
  pending.value = null;
});
onUnload(() => {
  foreground = false;
  clear();
  pending.value = null;
  unsubscribe();
});
async function load() {
  if (!id || loading.value || busy.value) return;
  clear();
  const run = sequence;
  loading.value = true;
  error.value = '';
  try {
    owner = inviteScope();
    await ensureInviteSession();
    if (!current(run)) return;
    pending.value = pendingInvite(id);
    if (pending.value) reason.value = pending.value.body.reason || '';
    const value = await getInvite(id);
    if (!current(run)) return;
    fill(value);
  } catch (failure) {
    if (!foreground || run !== sequence) return;
    view.value = null;
    error.value = failure instanceof Error ? failure.message : '邀约详情暂不可读取';
    pending.value = pendingInvite(id);
  } finally {
    if (foreground && run === sequence) loading.value = false;
  }
}
async function execute(intent: InviteIntent) {
  if (!current() || busy.value) return;
  const run = sequence;
  busy.value = true;
  error.value = '';
  guard.clear();
  pending.value = intent;
  try {
    const result = await runInviteIntent(intent);
    if (!current(run)) return;
    pending.value = null;
    reason.value = '';
    stale.value = false;
    fill(result);
  } catch (failure) {
    if (!current(run)) return;
    // 失败撤回旧有效投影；恢复原意图的入口在投影外，绝对截止也不会擦掉未知回执。
    view.value = null;
    guard.clear();
    error.value = failure instanceof Error ? failure.message : '响应结果尚未确认';
    stale.value = (failure as { status?: number }).status === 409;
    pending.value = pendingInvite(intent.inviteId);
  } finally {
    if (current(run)) busy.value = false;
  }
}
function respond(decision: InviteDecision) {
  if (
    locked.value ||
    !view.value ||
    !can(decision === 'ACCEPT' ? 'ACCEPT_INVITE' : 'REJECT_INVITE')
  )
    return;
  try {
    void execute(createInviteIntent(view.value, decision, reason.value));
  } catch (failure) {
    error.value = (failure as Error).message;
  }
}
function work() {
  uni.reLaunch({ url: '/pages/work/index' });
}
function list() {
  uni.redirectTo({ url: '/pages/dispatch/invites' });
}
</script>
<template>
  <view class="screen">
    <text class="title">本次派单邀约</text>
    <text v-if="loading" class="muted">正在核对本人、原邀约及服务端截止…</text>
    <view v-if="view" class="card">
      <view class="between"
        ><text class="section">{{ inviteLabel(view.status) }}</text
        ><text class="badge">{{ inviteLabel(view.source) }}</text></view
      >
      <text>{{ view.stationName }} · {{ view.serviceCompanyName || '服务公司未配置' }}</text>
      <text>{{ view.vehicleTypeName }} · {{ view.vehiclePlateMasked || '车牌摘要未提供' }}</text>
      <text>接车范围：{{ view.pickupArea }}</text>
      <text
        >上门时段：{{ inviteTime(view.pickupSlotStartAt) }} —
        {{ inviteTime(view.pickupSlotEndAt) }}</text
      >
      <text>接车点直线距离：{{ straightDistance(view.distanceMeter) }}</text>
      <text class="muted"
        >直线距离用于派单半径判断，不是导航路线或行车时长。服务收费不作为本单收入展示。</text
      >
      <text class="deadline">响应截止：{{ inviteTime(view.expiresAt) }}</text>
      <text class="muted">订单 {{ view.orderNo }} · 邀约 {{ view.inviteId }}</text>
      <template v-if="view.status === 'WAIT_RESPONSE'">
        <text v-if="view.acceptBlockReason" class="notice"
          >当前接受限制：{{ inviteLabel(view.acceptBlockReason) }}</text
        >
        <textarea
          v-model="reason"
          class="input"
          :disabled="locked"
          maxlength="500"
          placeholder="响应说明（可选，最多500字）"
        />
        <button
          v-if="can('ACCEPT_INVITE')"
          class="primary"
          :disabled="locked"
          @click="respond('ACCEPT')"
        >
          接受本次邀约
        </button>
        <button v-if="can('REJECT_INVITE')" :disabled="locked" @click="respond('REJECT')">
          拒绝本次邀约
        </button>
        <text class="notice"
          >接受后建立实际承接与占用；人工邀约也需要本人响应。最终资格、订单与截止由服务端再次校验。</text
        >
      </template>
      <template v-else-if="view.status === 'ACCEPTED'">
        <text class="notice"
          >本次邀约已接受。切身份、关闭听单或刷新不会结束承接或解除车辆保管归还责任。</text
        >
        <text v-if="view.assignmentId">承接记录：{{ view.assignmentId }}</text>
        <button
          v-if="view.allowedActions.includes('VIEW_ASSIGNMENT')"
          class="primary"
          @click="work"
        >
          查看当前承接
        </button>
      </template>
      <text v-else class="notice"
        >本次邀约已失效，不能凭旧消息接受；请查看本人其它邀约或返回工作台。</text
      >
    </view>
    <view v-if="pending" class="notice unknown">
      <text
        >原{{
          inviteLabel(pending.body.decision)
        }}结果尚未确认。原邀约、版本、正文与请求键保持不变；超过显示截止也只核对原回执。</text
      >
      <button :disabled="busy || loading" @click="execute(pending)">核对并重试原响应</button>
    </view>
    <text v-if="!loading && !view" class="muted"
      >当前有效邀约信息尚未取得，不显示旧接受资格或推断已建立承接。</text
    >
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="loading || busy" @click="load">
      {{ stale ? '重读原邀约' : '刷新原邀约' }}
    </button>
    <button @click="list">返回本人邀约列表</button>
    <button @click="work">返回我的工作</button>
  </view>
</template>
<style scoped>
.screen {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  font-size: 24px;
  font-weight: 600;
}
.card {
  padding: 18px;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 13px;
  line-height: 1.8;
}
.between {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}
.section {
  font-size: 19px;
  font-weight: 600;
}
.badge {
  color: #005fcb;
  font-size: 13px;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.8;
}
.deadline {
  color: #854d0e;
  font-weight: 600;
}
.notice {
  padding: 13px;
  background: #fff4e5;
  color: #854d0e;
  border-radius: 8px;
  line-height: 1.8;
  font-size: 14px;
}
.unknown {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.input {
  box-sizing: border-box;
  width: 100%;
  min-height: 100px;
  border: 1px solid #d6dce5;
  padding: 12px;
  border-radius: 8px;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
button {
  margin: 0;
  width: 100%;
  font-size: 15px;
}
.primary {
  color: white;
  background: #005fcb;
}
</style>
