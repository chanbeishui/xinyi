<template>
  <view class="page">
    <view class="card">
      <view class="row-between">
        <view>
          <view class="card-title" style="margin: 0">消息中心</view>
          <view class="muted">未读 {{ unread }} 条 · 当前视角 {{ roleLabel }}</view>
        </view>
        <view class="btn gray small" @tap="load">刷新</view>
      </view>
    </view>

    <view v-for="m in rows" :key="m.messageId" class="card" :class="{ unread: !m.read }" @tap="open(m)">
      <view class="row-between">
        <text class="scene">{{ sceneLabel(m.scene) }}</text>
        <text class="muted">{{ shortTime(m.createdAt) }}</text>
      </view>
      <view class="content">{{ m.content }}</view>
      <view v-if="!m.read" class="tag" style="margin-top: 12rpx">未读</view>
    </view>

    <view v-if="!rows.length" class="empty">暂无消息</view>

    <view class="footer-space"></view>

    <RoleTabBar :role="session.activeRole" active="messages" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { ROLE_LABEL, session } from '@/store/session'
import { shortTime } from '@/utils/format'

const rows = ref<any[]>([])
const unread = ref(0)

const roleLabel = computed(() => ROLE_LABEL[session.activeRole] || session.activeRole)

onShow(() => {
  load()
})

async function load() {
  try {
    const page: any = await commonApi.messages()
    rows.value = (page && page.rows) || []
    unread.value = Number((page && page.unread) || 0)
  } catch (e) {
    rows.value = []
  }
}

const SCENE: Record<string, string> = {
  ASSIGNMENT_ACCEPTED: '派单结果',
  ORDER_AUTO_COMPLETED: '订单完成',
  AFTERSALE_APPLIED: '售后申请',
  HANDOVER_CONFIRMED: '交接完成',
  ORDER_CREATED: '预约创建',
  PAYMENT_SUCCEEDED: '支付成功'
}

function sceneLabel(scene: string) {
  return SCENE[scene] || scene || '通知'
}

async function open(m: any) {
  if (!m.read) {
    try {
      await commonApi.readMessage(m.messageId)
      m.read = true
      unread.value = Math.max(0, unread.value - 1)
    } catch (e) {
      // 忽略
    }
  }
}
</script>

<style scoped>
.card.unread {
  border-left: 6rpx solid var(--brand);
}

.scene {
  font-size: 27rpx;
  font-weight: 600;
}

.content {
  margin-top: 10rpx;
  font-size: 26rpx;
  color: var(--sub);
  line-height: 1.6;
}
</style>
