<template>
  <view class="tabbar">
    <view
      v-for="item in items"
      :key="item.key"
      class="tab"
      :class="{ on: item.key === active }"
      hover-class="tab--press"
      :hover-stay-time="80"
      @tap="go(item)"
    >
      <view class="tab__mark"></view>
      <text class="tab__label">{{ item.label }}</text>
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
/* 底部导航：不画图标，用「顶部短横 + 字重」区分当前页。
   小程序里图标字体要额外打包，纯文字反而更稳、也与工单体的排版一致。 */
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 80;
  display: flex;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.98);
  border-top: 1rpx solid var(--line);
}

.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  padding-top: 6rpx;
}

.tab--press {
  background: #f7f9fc;
}

/* 当前项的短横：位置固定在导航条上沿，视觉上像工单的分栏标 */
.tab__mark {
  width: 36rpx;
  height: 5rpx;
  border-radius: 3rpx;
  background: transparent;
}

.tab.on .tab__mark {
  background: var(--brand);
}

.tab__label {
  margin-top: 10rpx;
  font-size: 22rpx;
  letter-spacing: 1rpx;
  color: var(--ink-3);
}

.tab.on .tab__label {
  color: var(--ink);
  font-weight: 600;
}

/* 安全区直接撑在容器上，避免 iPhone 上标签被 home indicator 压住 */
.tabbar {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
