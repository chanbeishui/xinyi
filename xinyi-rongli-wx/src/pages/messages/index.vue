<template>
  <view class="page">
    <view class="msg-head">
      <view class="msg-head__main">
        <view class="msg-head__count">
          <text class="num">{{ unread }}</text>
          <text class="msg-head__unit">条未读</text>
        </view>
        <view class="msg-head__meta">当前视角 · {{ roleLabel }}</view>
      </view>
      <view class="btn ghost small" hover-class="btn-hover" @tap="load">刷新</view>
    </view>

    <view class="list">
      <view
        v-for="m in rows"
        :key="m.messageId"
        class="msg"
        :class="{ 'msg--unread': !m.read }"
        hover-class="card-hover"
        @tap="open(m)"
      >
        <view class="msg__top">
          <view class="msg__scene">
            <view v-if="!m.read" class="msg__dot"></view>
            <text class="msg__scene-text">{{ sceneLabel(m.scene) }}</text>
          </view>
          <text class="msg__time num">{{ shortTime(m.createdAt) }}</text>
        </view>
        <view class="msg__content">{{ m.content }}</view>
      </view>

      <view v-if="!rows.length" class="empty">
        <view class="empty__title">暂无消息</view>
        <view class="empty__hint">派单结果、支付与售后进展会推送到这里</view>
      </view>
    </view>

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
.msg-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 8rpx 6rpx 24rpx;
}

.msg-head__count {
  display: flex;
  align-items: flex-end;
}

.msg-head__count .num {
  font-size: 56rpx;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: 1rpx;
}

.msg-head__unit {
  margin-left: 8rpx;
  padding-bottom: 8rpx;
  font-size: 23rpx;
  color: var(--ink-3);
}

.msg-head__meta {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}

/* 未读用左侧竖条 + 圆点双标，扫视时不必读文字 */
.msg {
  position: relative;
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.msg:last-child {
  border-bottom: none;
}

.msg--unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22rpx;
  bottom: 22rpx;
  width: 5rpx;
  border-radius: 0 3rpx 3rpx 0;
  background: var(--brand);
}

.msg__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.msg__scene {
  display: flex;
  align-items: center;
}

.msg__dot {
  width: 10rpx;
  height: 10rpx;
  margin-right: 10rpx;
  border-radius: 50%;
  background: var(--brand);
}

.msg__scene-text {
  font-size: 26rpx;
  font-weight: 600;
  letter-spacing: 0.4rpx;
}

.msg__time {
  font-size: 22rpx;
  color: var(--ink-3);
}

.msg__content {
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 1.65;
  color: var(--ink-2);
}

.msg--unread .msg__content {
  color: var(--ink);
}
</style>
