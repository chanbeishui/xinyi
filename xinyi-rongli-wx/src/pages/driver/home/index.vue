<template>
  <view class="page">
    <view class="status-card">
      <view class="row-between">
        <view>
          <view class="status-title">{{ canReceive ? '正在听单' : '未在听单' }}</view>
          <view class="status-sub">{{ statusHint }}</view>
        </view>
        <switch :checked="listening" color="#ffffff" @change="toggleListening" />
      </view>
      <view v-if="blockers.length" class="blockers">
        <view v-for="b in blockers" :key="b" class="blocker">· {{ b }}</view>
      </view>
    </view>

    <view v-if="current" class="card" @tap="openTask(current.orderNo)">
      <view class="row-between">
        <view class="card-title" style="margin: 0">当前任务</view>
        <view class="tag">{{ current.fulfillmentStatusLabel }}</view>
      </view>
      <view class="row-between">
        <text class="label">车辆</text>
        <text class="value">{{ current.plateNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">上门时段</text>
        <text class="value">{{ current.pickupWindowLabel }}</text>
      </view>
      <view class="row-between">
        <text class="label">检测点</text>
        <text class="value">{{ current.stationName }}</text>
      </view>
      <view class="btn" style="margin-top: 20rpx">进入接送任务</view>
    </view>

    <view class="card">
      <view class="row-between">
        <view class="card-title" style="margin: 0">待响应邀约</view>
        <text class="muted">{{ invites.length }} 条</text>
      </view>
      <view v-if="!invites.length" class="muted" style="margin-top: 12rpx">暂无邀约，请保持听单状态</view>
      <view v-for="i in invites" :key="i.inviteId" class="invite" @tap="openInvite(i)">
        <view class="row-between">
          <text class="value" style="text-align: left">{{ i.orderNo }}</text>
          <text class="tag warn">{{ countdownText(i.expiresAt) }}</text>
        </view>
        <view class="muted">{{ i.stationName }} · {{ i.pickupWindow }}</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">快捷入口</view>
      <view class="row-between" @tap="go('/pages/driver/qualification/index')">
        <text class="label">认证与复审</text>
        <text class="muted">{{ qualificationLabel }} ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/driver/statement/index')">
        <text class="label">我的对账</text>
        <text class="muted">核对与确认 ›</text>
      </view>
      <view class="row-between" @tap="refreshLocation">
        <text class="label">更新定位</text>
        <text class="muted">{{ locationText }}</text>
      </view>
    </view>

    <RoleTabBar role="DRIVER" active="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { driverApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { countdownText } from '@/utils/format'

const availability = ref<any>({})
const current = ref<any>(null)
const invites = ref<any[]>([])
const agreed = ref(false)

const listening = computed(() => !!availability.value.listeningEnabled)
const canReceive = computed(() => !!availability.value.canReceiveInvite)
const blockers = computed<string[]>(() => availability.value.blockers || [])
const statusHint = computed(() => {
  const a = availability.value
  if (!a.qualificationStatus) return '请先完成代驾认证'
  if (a.qualificationStatus !== 'APPROVED') return '认证审核中，审核通过后可接单'
  if (a.onTask) return '当前有在手任务，完成归还后自动恢复听单'
  return a.canReceiveInvite ? '系统将按轮转顺序推送派单邀约' : '开启听单并保持定位有效才能接单'
})
const qualificationLabel = computed(() => {
  const s = availability.value.qualificationStatus
  const map: Record<string, string> = {
    DRAFT: '资料待提交',
    SUBMITTED: '审核中',
    APPROVED: '已通过',
    REJECTED: '未通过',
    EXPIRED: '已到期'
  }
  return map[s] || '去认证'
})
const locationText = computed(() =>
  availability.value.locationFresh ? '定位有效' : '定位已过期'
)

onShow(() => {
  load()
})

onPullDownRefresh(() => {
  load().finally(() => uni.stopPullDownRefresh())
})

async function load() {
  try {
    availability.value = await driverApi.availability()
  } catch (e) {
    availability.value = {}
  }
  try {
    current.value = await driverApi.currentAssignment()
  } catch (e) {
    current.value = null
  }
  try {
    const view: any = await driverApi.invites()
    invites.value = (view && view.rows) || []
  } catch (e) {
    invites.value = []
  }
}

async function toggleListening(e: any) {
  const enabled = !!e.detail.value
  try {
    await driverApi.setListening(enabled)
    if (enabled) await reportLocation()
    load()
  } catch (err) {
    load()
  }
}

function refreshLocation() {
  reportLocation().then(() => load())
}

function reportLocation(): Promise<void> {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: async (res) => {
        try {
          await driverApi.reportLocation(res.longitude, res.latitude)
        } catch (e) {
          // 忽略
        }
        resolve()
      },
      fail: () => {
        uni.showToast({ title: '未获取到定位，请检查授权', icon: 'none' })
        resolve()
      }
    })
  })
}

function openTask(orderNo: string) {
  uni.navigateTo({ url: `/pages/driver/task/index?orderNo=${orderNo}` })
}

function openInvite(i: any) {
  uni.navigateTo({ url: `/pages/driver/invite/index?inviteId=${i.inviteId}` })
}

function go(url: string) {
  uni.navigateTo({ url })
}

void agreed
</script>

<style scoped>
.status-card {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.status-title {
  font-size: 38rpx;
  font-weight: 600;
}

.status-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.9;
}

.blockers {
  margin-top: 20rpx;
  background: rgba(255, 255, 255, 0.14);
  border-radius: 14rpx;
  padding: 16rpx 20rpx;
}

.blocker {
  font-size: 23rpx;
  line-height: 1.8;
  opacity: 0.94;
}

.invite {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.invite:last-child {
  border-bottom: none;
}
</style>
