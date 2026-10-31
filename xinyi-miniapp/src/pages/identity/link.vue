<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getEmployeeLink, reconcileEmployeeLink } from '../../features/identity/api';
import { currentSession } from '../../services/http';
import type { EmployeeLinkPublic } from '../../api/types';

// U02 员工自动关联状态：同一状态源；已关联不同时显示未核验；不提供员工编号/凭证/站点申请表。
const link = ref<EmployeeLinkPublic | null>(null);
const message = ref('');

const TEXT: Record<string, string> = {
  NO_EMPLOYEE_MATCH: '未匹配到员工，不影响车主身份。',
  EMPLOYEE_LINKED: '已识别员工，可切换检测工作（按授权站点）。',
  WAIT_ADMIN_CONFIGURATION: '已识别员工，暂未配置检测工作权限，请联系管理员。',
  CONTACT_ADMIN: '员工关联异常，请联系管理员处理。',
  PHONE_REVERIFY_REQUIRED: '手机号已变化，请重新核验。',
  LINK_REVOKED: '关联已被管理员解除，请联系管理员。'
};

onShow(load);

async function load() {
  try {
    link.value = await getEmployeeLink();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载关联状态失败';
  }
}

async function reconcile() {
  const s = currentSession();
  if (!s) return;
  try {
    link.value = await reconcileEmployeeLink(s.contextVersion);
    message.value = '';
  } catch (err) {
    message.value = err instanceof Error ? err.message : '刷新失败，请重新授权';
  }
}

function reverify() {
  uni.navigateTo({ url: '/pages/identity/phone' });
}

function contactAdmin() {
  uni.showToast({ title: '请联系管理员处理', icon: 'none' });
}
</script>

<template>
  <view class="screen">
    <text class="title">员工关联</text>
    <view v-if="link" class="card">
      <text class="status">{{ link.status }}</text>
      <text class="desc">{{ TEXT[link.messageKey] || '' }}</text>
      <text class="kv">可操作站点：{{ link.authorizedStationCount }}</text>
      <text class="kv">工作权限：{{ link.workAccessEnabled ? '已开通' : '未开通' }}</text>
    </view>

    <button class="plain" @click="reconcile">刷新权限</button>
    <button v-if="link && link.canReverifyPhone" class="primary" @click="reverify">重新授权手机号</button>
    <button class="plain" @click="contactAdmin">联系管理员</button>
    <text v-if="message" class="warn">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 10px; }
.status { font-size: 18px; font-weight: 600; }
.desc, .kv { font-size: 15px; color: #555; line-height: 1.6; }
.warn { font-size: 15px; color: #c0392b; }
.primary { background: #005fcb; color: #fff; }
.plain { background: #eef2f7; color: #005fcb; }
</style>