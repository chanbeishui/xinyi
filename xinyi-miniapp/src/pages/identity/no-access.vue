<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getSession, switchRole } from '../../features/identity/api';
import { applyAccessSession, currentSession } from '../../services/http';
import type { SessionView } from '../../api/types';

// S22 无工作权限：允许 0 授权；不自动回到已无权的所属站点。仍可切回车主/已授权视角。
const view = ref<SessionView | null>(null);
const message = ref('');

onShow(load);

async function load() {
  try {
    view.value = await getSession();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载会话失败';
  }
}

async function toOwner() {
  const s = currentSession();
  if (!s) return;
  try {
    const access = await switchRole('OWNER', s.contextVersion);
    applyAccessSession(access);
    uni.reLaunch({ url: '/pages/index/index' });
  } catch (err) {
    message.value = err instanceof Error ? err.message : '切换失败，请刷新';
  }
}

function toStation() {
  uni.redirectTo({ url: '/pages/identity/station' });
}

function contactAdmin() {
  uni.showToast({ title: '请联系管理员配置站点授权', icon: 'none' });
}
</script>

<template>
  <view class="screen">
    <text class="title">无工作权限</text>
    <view class="note">
      <text v-if="view && view.employeeLink.status === 'LINKED'">已关联员工，但当前无可操作站点或授权已被撤销。</text>
      <text v-else>尚未获得检测工作授权。手机号核验与员工关联不影响车主身份。</text>
    </view>

    <view class="card">
      <text class="section">仍可进入的视角</text>
      <view class="row switchable" @click="toOwner">
        <text class="role">车主</text><text class="chev">›</text>
      </view>
      <view v-if="view && view.stationIds && view.stationIds.length > 0" class="row switchable" @click="toStation">
        <text class="role">切到授权站点</text><text class="chev">›</text>
      </view>
    </view>

    <button class="plain" @click="contactAdmin">联系管理员</button>
    <text v-if="message" class="warn">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 14px; }
.section { font-size: 17px; font-weight: 600; }
.row { display: flex; justify-content: space-between; align-items: center; }
.role { font-size: 17px; font-weight: 600; }
.chev { color: #999; font-size: 20px; }
.switchable { padding: 6px 0; }
.note { background: #eef2f7; border-radius: 12px; padding: 14px; font-size: 14px; color: #555; line-height: 1.6; }
.warn { font-size: 15px; color: #c0392b; }
.plain { background: #eef2f7; color: #333; }
</style>