<template>
  <view class="tabbar">
    <view
      v-for="item in items"
      :key="item.key"
      class="tab"
      :class="{ on: item.key === active }"
      @tap="go(item)"
    >
      <view class="dot" :class="{ filled: item.key === active }"></view>
      <text class="txt">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface TabItem {
  key: string
  label: string
  url: string
}

const props = defineProps<{ role: string; active: string }>()

const MAP: Record<string, TabItem[]> = {
  OWNER: [
    { key: 'home', label: '预约', url: '/pages/owner/home/index' },
    { key: 'orders', label: '订单', url: '/pages/owner/orders/index' },
    { key: 'messages', label: '消息', url: '/pages/messages/index' },
    { key: 'mine', label: '我的', url: '/pages/owner/mine/index' }
  ],
  DRIVER: [
    { key: 'home', label: '工作台', url: '/pages/driver/home/index' },
    { key: 'statement', label: '对账', url: '/pages/driver/statement/index' },
    { key: 'messages', label: '消息', url: '/pages/messages/index' },
    { key: 'mine', label: '我的', url: '/pages/driver/mine/index' }
  ],
  STATION: [
    { key: 'home', label: '检测工作台', url: '/pages/station/home/index' },
    { key: 'messages', label: '消息', url: '/pages/messages/index' },
    { key: 'mine', label: '我的', url: '/pages/settings/index' }
  ]
}

const items = computed(() => MAP[props.role] || MAP.OWNER)

function go(item: TabItem) {
  if (item.key === props.active) return
  uni.redirectTo({ url: item.url })
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc(112rpx + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  border-top: 1rpx solid var(--line);
  display: flex;
  align-items: center;
}

.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: var(--muted);
  font-size: 22rpx;
}

.tab.on {
  color: var(--brand);
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  border: 2rpx solid currentColor;
}

.dot.filled {
  background: currentColor;
}

.txt {
  font-size: 22rpx;
}
</style>
