<template>
  <view class="page">
    <!-- 听单状态：司机最关心的一条信息，给足面积 -->
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="ink-panel__eyebrow">听单状态</view>
      <view class="listen">
        <view class="listen__body">
          <view class="listen__title">{{ statusTitle }}</view>
          <view v-if="!blockers.length" class="listen__desc">{{ statusHint }}</view>
        </view>
        <switch :checked="listening" color="#3B6BFF" @change="toggleListening" />
      </view>
      <view v-if="blockers.length" class="listen__blockers">
        <view v-for="b in blockers" :key="b" class="listen__blocker">· {{ b }}</view>
      </view>
    </view>

    <!-- 当前任务 -->
    <view v-if="current" class="slip" hover-class="card-hover" @tap="openTask(current.orderNo)">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">当前任务</text>
          <view class="slip__plate">{{ current.plateNo }}</view>
        </view>
        <view class="status status--brand">{{ current.fulfillmentStatusLabel }}</view>
      </view>
      <view class="slip__row">
        <text class="slip__key">上门时段</text>
        <text class="slip__val num">{{ current.pickupWindowLabel }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">检测点</text>
        <text class="slip__val">{{ current.stationName }}</text>
      </view>
      <view class="slip__foot">
        <text class="slip__no">进入接送任务</text>
        <view class="chev"></view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">待响应邀约</text>
      <text class="section-head__meta">{{ invites.length }} 条</text>
    </view>

    <view class="list">
      <view
        v-for="i in invites"
        :key="i.inviteId"
        class="invite"
        hover-class="card-hover"
        @tap="openInvite(i)"
      >
        <view class="invite__top">
          <text class="invite__no num">{{ i.orderNo }}</text>
          <text class="status status--warn">{{ countdownText(i.expiresAt) }}</text>
        </view>
        <view class="invite__meta">{{ i.stationName }} · {{ i.pickupWindow }}</view>
      </view>
      <view v-if="!invites.length" class="empty">
        <view class="empty__title">暂无邀约</view>
        <view class="empty__hint">保持听单并确保定位有效，系统会按轮转顺序推送派单</view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">快捷入口</text>
    </view>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/qualification/index')">
        <text class="list-row__key">认证与复审</text>
        <view class="row">
          <text class="list-row__hint">{{ qualificationLabel }}</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/statement/index')">
        <text class="list-row__key">我的对账</text>
        <view class="row">
          <text class="list-row__hint">核对与确认</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="refreshLocation">
        <text class="list-row__key">更新定位</text>
        <view class="row">
          <text class="list-row__hint" :class="{ 'hint--warn': !availability.locationFresh }">
            {{ locationText }}
          </text>
          <view class="chev"></view>
        </view>
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
/** 标题跟着开关走，别出现「开关是开的、标题写未在听单」这种自相矛盾 */
const statusTitle = computed(() => {
  if (!listening.value) return '未在听单'
  return canReceive.value ? '正在听单' : '听单中，暂不可接单'
})
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
.listen {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14rpx;
}

.listen__body {
  flex: 1;
  min-width: 0;
  padding-right: 20rpx;
}

.listen__title {
  font-size: 44rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.listen__desc {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.62);
}

.listen__blockers {
  margin-top: 22rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.12);
}

.listen__blocker {
  font-size: 22rpx;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.72);
}

.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.invite {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.invite:last-child {
  border-bottom: none;
}

.invite__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.invite__no {
  font-size: 27rpx;
  font-weight: 500;
  letter-spacing: 0.6rpx;
}

.invite__meta {
  margin-top: 8rpx;
  font-size: 23rpx;
  color: var(--ink-3);
}

.hint--warn {
  color: var(--warn);
}
</style>
