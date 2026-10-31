<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getSession, switchRole } from '../../features/identity/api';
import { applyAccessSession, currentSession } from '../../services/http';
import type { SessionView, MiniRole } from '../../api/types';

// U01 切换身份：只展示服务端 availableRoles；切换不结束正在承接的代驾任务。
// 状态变体（unqualified/driver/staff/all）由同一页面按权威状态渲染。
const view = ref<SessionView | null>(null);
const busy = ref(false);
const message = ref('');

const ROLE_META: Record<MiniRole, { label: string; desc: string }> = {
  OWNER: { label: '车主', desc: '预约接送、支付及本人服务记录' },
  DRIVER: { label: '代驾', desc: '承接代驾任务与本人账单' },
  STATION: { label: '检测工作', desc: '收车 / 结果登记（按授权站点）' }
};

onShow(load);

async function load() {
  try {
    view.value = await getSession();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载会话失败';
  }
}

async function doSwitch(role: MiniRole) {
  const s = currentSession();
  if (!s || busy.value) return;
  busy.value = true;
  message.value = '';
  try {
    const access = await switchRole(role, s.contextVersion);
    applyAccessSession(access);
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '切换失败，请刷新后重试';
    await load();
  } finally {
    busy.value = false;
  }
}

function applyDriver() {
  // 代驾申请属资格任务；此处仅提示状态，不重复引导已批准账号
  const q = view.value?.driverQualification;
  uni.showToast({ title: q === 'APPROVED' ? '已具备代驾资格' : '审核通过后才开通正式代驾身份', icon: 'none' });
}

function viewLink() {
  uni.navigateTo({ url: '/pages/identity/phone' });
}

function back() {
  uni.reLaunch({ url: '/pages/index/index' });
}
</script>

<template>
  <view class="screen">
    <text class="title">切换身份</text>
    <text class="desc">以下是你当前可使用的身份。切换不会结束正在承接的代驾任务。</text>

    <view v-if="view" class="current card">
      <view class="row">
        <view>
          <text class="role">{{ ROLE_META[view.activeRole].label }}</text>
          <text class="sub">{{ ROLE_META[view.activeRole].desc }}</text>
        </view>
        <text class="badge">当前</text>
      </view>
    </view>

    <view v-if="view && view.availableRoles.length > 1" class="card">
      <text class="section">可切换身份</text>
      <view v-for="r in view.availableRoles" :key="r">
        <view v-if="r !== view.activeRole" class="row switchable" @click="doSwitch(r)">
          <view>
            <text class="role">{{ ROLE_META[r].label }}</text>
            <text class="sub">{{ ROLE_META[r].desc }}</text>
          </view>
          <text class="chev">›</text>
        </view>
      </view>
    </view>

    <view class="card">
      <text class="section">资格和工作信息</text>
      <view class="row switchable" @click="applyDriver">
        <view>
          <text class="role">申请成为代驾</text>
          <text class="sub">{{ view && view.driverQualification === 'APPROVED' ? '已审核通过，可切换代驾身份' : '审核通过后才开通正式代驾身份' }}</text>
        </view>
        <text class="chev">›</text>
      </view>
      <view class="row switchable" @click="viewLink">
        <view>
          <text class="role">员工关联</text>
          <text class="sub">{{ view && view.employeeLink.status === 'LINKED' ? '已关联员工' : '未关联员工' }}</text>
        </view>
        <text class="chev">›</text>
      </view>
    </view>

    <view class="note">
      <text>员工身份不免除代驾审核；未获授权的身份不会显示为可切换。</text>
    </view>
    <text v-if="message" class="warn">{{ message }}</text>

    <button class="plain bottom" @click="back">返回我的</button>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 24px; font-weight: 600; }
.desc, .sub { font-size: 15px; line-height: 1.7; color: #666; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 14px; }
.current { border: 1px solid #005fcb; background: #eef5ff; }
.section { font-size: 17px; font-weight: 600; }
.row { display: flex; justify-content: space-between; align-items: center; }
.role { font-size: 17px; font-weight: 600; display: block; }
.badge { color: #005fcb; font-size: 15px; }
.chev { color: #999; font-size: 20px; }
.switchable { padding: 6px 0; }
.note { background: #eef2f7; border-radius: 12px; padding: 14px; font-size: 14px; color: #555; line-height: 1.6; }
.warn { font-size: 15px; color: #c0392b; }
.plain { background: #eef2f7; color: #333; }
.bottom { margin-top: 12px; }
</style>