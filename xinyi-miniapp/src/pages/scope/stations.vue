<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getAuthorizedStations, type AuthorizedStation } from '../../features/scope/api';

// S18 员工获授权站点：仅列出获授权的可操作站点；不暴露无权站点单量/司机/订单。
const stations = ref<AuthorizedStation[]>([]);
const message = ref('');

onShow(load);

async function load() {
  try {
    stations.value = await getAuthorizedStations();
    message.value = stations.value.length === 0 ? '暂无获授权站点，请联系管理员配置。' : '';
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载站点失败';
  }
}
</script>

<template>
  <view class="screen">
    <text class="title">可操作站点</text>
    <view v-for="s in stations" :key="s.stationId" class="card">
      <text class="name">{{ s.name }}</text>
      <text class="addr">{{ s.address }}</text>
    </view>
    <text v-if="message" class="desc">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 6px; }
.name { font-size: 17px; font-weight: 600; }
.addr, .desc { font-size: 15px; color: #666; line-height: 1.6; }
</style>