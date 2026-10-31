<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getSession, switchStation } from '../../features/identity/api';
import { applyAccessSession, currentSession } from '../../services/http';
import type { SessionView } from '../../api/types';

// S21 切换检测点：仅列出获授权的可操作站点；切换后业务数据随当前站点更新。
// 站点基础名称由公开站点目录任务富化；此处以授权 ID 渲染，不暴露无权站点单量/司机/订单。
const view = ref<SessionView | null>(null);
const busy = ref(false);
const message = ref('');

onShow(load);

async function load() {
  try {
    const v = await getSession();
    if (!v.stationIds || v.stationIds.length === 0) {
      uni.redirectTo({ url: '/pages/identity/no-access' });
      return;
    }
    view.value = v;
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载站点失败';
  }
}

async function doSwitch(stationId: string) {
  const s = currentSession();
  if (!s || busy.value) return;
  busy.value = true;
  message.value = '';
  try {
    const access = await switchStation(stationId, s.contextVersion);
    applyAccessSession(access);
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '切换失败，请刷新后重试';
    await load();
  } finally {
    busy.value = false;
  }
}

function noAccess() {
  uni.navigateTo({ url: '/pages/identity/no-access' });
}

function back() {
  uni.reLaunch({ url: '/pages/index/index' });
}
</script>

<template>
  <view class="screen">
    <text class="title">切换检测点</text>
    <view class="note"><text>仅列出获授权的可操作站点。切换后业务数据和待办随当前站点更新。</text></view>

    <view v-for="id in view?.stationIds || []" :key="id"
          class="card" :class="{ current: id === view?.stationId }"
          @click="id !== view?.stationId && doSwitch(id)">
      <view class="row">
        <view>
          <text class="role">检测站 #{{ id }}</text>
          <text class="sub">收车 / 结果登记 / 放行按功能权限执行</text>
        </view>
        <text v-if="id === view?.stationId" class="badge">当前</text>
        <text v-else class="chev">›</text>
      </view>
    </view>

    <text class="link" @click="noAccess">查看无权限状态</text>
    <text v-if="message" class="warn">{{ message }}</text>

    <button class="plain bottom" @click="back">返回当前工作台</button>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; }
.current { border: 1px solid #005fcb; background: #eef5ff; }
.row { display: flex; justify-content: space-between; align-items: center; }
.role { font-size: 17px; font-weight: 600; display: block; }
.sub { font-size: 14px; color: #666; line-height: 1.6; }
.badge { color: #005fcb; font-size: 15px; }
.chev { color: #999; font-size: 20px; }
.note { background: #eef2f7; border-radius: 12px; padding: 14px; font-size: 14px; color: #555; line-height: 1.6; }
.link { color: #005fcb; font-size: 15px; text-align: center; }
.warn { font-size: 15px; color: #c0392b; }
.plain { background: #eef2f7; color: #333; }
.bottom { margin-top: 12px; }
</style>