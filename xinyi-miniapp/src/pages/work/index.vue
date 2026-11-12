<script setup lang="ts">
import { computed, ref } from 'vue';
import { onHide, onShow, onUnload } from '@dcloudio/uni-app';
import { getProfile, getSession, logout } from '../../features/identity/api';
import { getAuthorizedStations, type AuthorizedStation } from '../../features/scope/api';
import { currentSession, onSessionChanged, saveSession } from '../../services/http';
import { intentKey } from '../../features/file/api';
import type { Profile, SessionView } from '../../api/types';
import DriverListening from '../../components/dispatch/DriverListening.vue';
const profile = ref<Profile | null>(null),
  session = ref<SessionView | null>(null),
  stations = ref<AuthorizedStation[]>([]),
  loading = ref(false),
  error = ref('');
const logoutBusy = ref(false),
  logoutMessage = ref(''),
  logoutKey = ref('');
let logoutOwner = '';
const identityScope = () => {
  const value = currentSession();
  return value
    ? [value.accountId, value.sessionId, value.contextVersion, value.activeRole].join(':')
    : '';
};
let identityOwner = identityScope();
const unsubscribeSession = onSessionChanged(() => {
  const next = identityScope();
  if (identityOwner === next) return;
  identityOwner = next;
  clear();
  logoutKey.value = '';
  logoutOwner = '';
});
let sequence = 0,
  workForeground = false;
const canReportBackground = () => !workForeground && !logoutBusy.value;
const title = computed(() =>
  session.value?.activeRole === 'DRIVER'
    ? '我的代驾工作'
    : session.value?.activeRole === 'STATION'
      ? '我的检测工作'
      : '我的工作'
);
const roleName = computed(
  () =>
    ({ OWNER: '车主', DRIVER: '代驾', STATION: '检测工作' })[session.value?.activeRole || 'OWNER']
);
const displayName = computed(
  () => profile.value?.realName || profile.value?.nickname || '当前账号'
);
const qualification = computed(() => {
  const value = session.value?.driverQualification;
  return value
    ? (
        {
          DRAFT: '资料草稿',
          SUBMITTED: '审核中',
          APPROVED: '已审核通过',
          REJECTED: '审核未通过',
          EXPIRED: '资格已到期',
          SUSPENDED: '资格已暂停'
        } as Record<string, string>
      )[value] || value
    : '尚未取得代驾资格';
});
const employeeStatus = computed(() => {
  const labels: Record<string, string> = {
    LINKED: '已关联',
    UNLINKED: '未关联',
    NEEDS_ADMIN: '待管理员核实',
    SUSPENDED: '已暂停',
    REVOKED: '已解除'
  };
  return labels[session.value?.employeeLink.status || ''] || '待核实';
});
const currentStation = computed(() =>
  stations.value.find((station) => station.stationId === session.value?.stationId)
);
const driverRecords = computed(
  () =>
    session.value?.activeRole === 'DRIVER' &&
    session.value.capabilities.includes('DRIVER_ORDER_LIST')
);
const stationWork = computed(
  () => session.value?.activeRole === 'STATION' && session.value.workAccessDisabled === false
);
const stationRecords = computed(
  () =>
    stationWork.value &&
    Boolean(session.value?.stationId) &&
    session.value?.permissions?.includes('biz:order:list')
);
onShow(() => {
  workForeground = true;
  void load();
});
onHide(() => {
  workForeground = false;
  clear();
});
onUnload(() => {
  workForeground = false;
  clear();
  unsubscribeSession();
  logoutKey.value = '';
});
function clear() {
  sequence++;
  profile.value = null;
  session.value = null;
  stations.value = [];
  loading.value = false;
  error.value = '';
}
async function load() {
  if (logoutKey.value && logoutOwner === identityScope()) return;
  logoutMessage.value = '';
  const run = ++sequence,
    previous = currentSession();
  profile.value = null;
  session.value = null;
  stations.value = [];
  error.value = '';
  loading.value = true;
  try {
    const [identity, publicProfile] = await Promise.all([getSession(), getProfile()]);
    if (run !== sequence) return;
    if (
      !previous ||
      publicProfile.accountId !== identity.accountId ||
      identity.accountId !== previous.accountId ||
      identity.activeRole !== previous.activeRole ||
      identity.contextVersion !== previous.contextVersion ||
      identity.sessionId !== previous.sessionId ||
      currentSession()?.sessionId !== previous.sessionId ||
      currentSession()?.contextVersion !== previous.contextVersion
    )
      throw new Error('身份信息已变化，请重新加载');
    // 工作角色、资格与工作权限只读取同一份会话投影；服务称呼不能当作实名核验结论。
    session.value = identity;
    profile.value = publicProfile;
    if (identity.activeRole === 'STATION' && identity.workAccessDisabled === false) {
      try {
        const available = await getAuthorizedStations();
        if (run === sequence) stations.value = available;
      } catch (failure) {
        if (run === sequence)
          error.value = failure instanceof Error ? failure.message : '站点权限读取失败';
      }
    }
  } catch (failure) {
    if (run === sequence) {
      profile.value = null;
      session.value = null;
      error.value = failure instanceof Error ? failure.message : '工作信息读取失败';
    }
  } finally {
    if (run === sequence) loading.value = false;
  }
}
function workAccessLost(message: string) {
  clear();
  error.value = message;
}
async function logoutCurrent() {
  if (logoutBusy.value || !currentSession()) return;
  const owner = identityScope();
  if (logoutOwner !== owner) {
    logoutOwner = owner;
    logoutKey.value = '';
  }
  logoutKey.value ||= intentKey();
  const key = logoutKey.value;
  logoutBusy.value = true;
  logoutMessage.value = '';
  // 原退出结果未知时停止本页采点并隐藏旧投影，但不先删登录凭据或伪装服务端退出成功。
  clear();
  try {
    const value = await logout(false, key);
    if (identityScope() !== owner) return;
    if (value?.loggedOut !== true || value.scope !== 'CURRENT') throw new Error('退出回执尚未确认');
    saveSession(null);
    logoutMessage.value = '已退出当前登录，听单与已有服务责任以服务端实际状态为准。';
  } catch (failure) {
    if (identityScope() !== owner) return;
    logoutMessage.value = failure instanceof Error ? failure.message : '退出结果尚未确认';
    const status = (failure as { status?: number }).status;
    if (status !== undefined && status < 500) logoutKey.value = '';
  } finally {
    logoutBusy.value = false;
  }
}
function open(path: string) {
  uni.navigateTo({ url: path });
}
</script>
<template>
  <view class="screen">
    <text class="title">{{ title }}</text>
    <text v-if="loading" class="muted">正在核对当前身份与工作权限…</text>
    <template v-if="session && profile">
      <view class="card">
        <text class="name">{{ displayName }}</text>
        <text class="muted">账号称呼仅用于展示，不代表实名核验结论。</text>
        <text>当前身份：{{ roleName }}</text>
        <text
          >手机号：{{
            session.phoneBound ? profile.phoneMask || '已核验，号码暂不可读取' : '尚未核验'
          }}</text
        >
        <text>员工关联：{{ employeeStatus }}</text>
      </view>
      <view v-if="session.activeRole === 'DRIVER'" class="card">
        <text class="section">代驾资格与服务</text>
        <text>上次服务端核对：{{ qualification }}</text>
        <button v-if="driverRecords" class="primary" @click="open('/pages/order/list')">
          查看本人服务记录
        </button>
        <text v-else class="muted">当前会话暂未开放本人服务入口，请核对权限后重试。</text>
        <text class="note">暂停新邀约不会结束在手服务，也不免除车辆保管和实际归还责任。</text>
        <DriverListening :background-allowed="canReportBackground" @access-lost="workAccessLost" />
        <button @click="open('/pages/qualification/index')">查看认证与复审资料</button>
      </view>
      <view v-else-if="session.activeRole === 'STATION'" class="card">
        <text class="section">检测工作权限</text>
        <text v-if="stationWork"
          >当前站点：{{
            currentStation?.name || (session.stationId ? '已选择获授权站点' : '尚未选择')
          }}</text
        >
        <text v-else class="note"
          >当前工作权限已停用或未配置，请联系管理员。已有服务记录不会因此删除。</text
        >
        <button v-if="stationRecords" class="primary" @click="open('/pages/order/list')">
          查看本站订单
        </button>
        <button v-if="stationWork" @click="open('/pages/identity/station')">切换获授权站点</button>
        <button v-if="stationWork" @click="open('/pages/scope/stations')">查看获授权站点</button>
      </view>
      <view v-else class="card">
        <text class="section">车主服务</text>
        <text class="muted">切换到服务端已开放的工作身份后，可查看相应工作入口。</text>
        <button class="primary" @click="open('/pages/order/list')">查看本人订单</button>
      </view>
      <view v-if="session.activeRole === 'DRIVER'" class="card">
        <text class="section">本人对账</text>
        <text class="muted"
          >本人对账、账单与付款摘要暂不可用。未取得正式账单前，不显示推算收入或虚构余额。</text
        >
        <button disabled>本人对账暂不可用</button>
      </view>
      <button @click="open('/pages/training/list')">查看本人必修培训</button>
      <button @click="open('/pages/identity/switch')">切换已有身份</button>
      <button v-if="session.activeRole !== 'DRIVER'" @click="open('/pages/qualification/index')">
        申请代驾或查看认证进度
      </button>
      <button v-if="!session.phoneBound" @click="open('/pages/identity/phone')">
        核验本人手机号
      </button>
      <button :disabled="logoutBusy" @click="logoutCurrent">退出当前登录</button>
    </template>
    <text v-if="logoutMessage" class="note">{{ logoutMessage }}</text>
    <template v-if="logoutKey">
      <text class="note"
        >原退出回执尚未确认。当前凭据尚未删除，保留原请求键核对，不创建新的退出意图。</text
      >
      <button :disabled="logoutBusy" @click="logoutCurrent">重试原退出操作</button>
    </template>
    <button v-if="logoutMessage && !logoutKey && !session" @click="open('/pages/identity/login')">
      重新登录
    </button>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button v-if="error && !session" @click="open('/pages/identity/login')">
      重新登录核对身份
    </button>
    <button :disabled="loading || logoutBusy || Boolean(logoutKey)" @click="load">
      刷新工作信息
    </button>
  </view>
</template>
<style scoped>
.screen {
  box-sizing: border-box;
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
  border-radius: 12px;
  background: white;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 15px;
  line-height: 1.7;
}
.name {
  font-size: 22px;
  font-weight: 600;
}
.section {
  font-size: 17px;
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.note {
  padding: 12px;
  border-radius: 8px;
  background: #fff4e5;
  color: #854d0e;
  font-size: 14px;
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
