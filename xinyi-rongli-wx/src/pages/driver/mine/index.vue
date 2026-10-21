<template>
  <view class="page">
    <view class="profile-card">
      <view class="avatar">{{ initial }}</view>
      <view class="info">
        <view class="name">{{ profile.nickname || '代驾' }}</view>
        <view class="sub">{{ qualificationText }}</view>
      </view>
      <view class="tag" style="background: rgba(255,255,255,0.2); color: #fff">代驾视角</view>
    </view>

    <view class="card">
      <view class="row-between" @tap="go('/pages/driver/qualification/index')">
        <text class="label">认证与复审</text>
        <text class="muted">{{ qualificationText }} ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/driver/statement/index')">
        <text class="label">我的对账</text>
        <text class="muted">查看 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/driver/ledger/index')">
        <text class="label">收支流水</text>
        <text class="muted">明细与收款 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/driver/training/index')">
        <text class="label">服务培训</text>
        <text class="muted">{{ trainingText }} ›</text>
      </view>
      <view class="row-between" @tap="loadOrders">
        <text class="label">接送任务记录</text>
        <text class="muted">{{ orderCount }} 单 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/driver/penalty/index')">
        <text class="label">处罚与申诉</text>
        <text class="muted">{{ penaltyCount }} 条 ›</text>
      </view>
    </view>

    <view v-if="orders.length" class="card">
      <view class="card-title">最近任务</view>
      <view v-for="o in orders.slice(0, 5)" :key="o.orderNo" class="row-between" @tap="openTask(o)">
        <view>
          <view class="value" style="text-align: left">{{ o.orderNo }}</view>
          <view class="muted">{{ o.plateNo || '' }} {{ o.fulfillmentStatusLabel || '' }}</view>
        </view>
        <text class="muted">查看 ›</text>
      </view>
    </view>

    <view v-if="penalties.length" class="card">
      <view class="card-title">处罚记录</view>
      <view v-for="p in penalties.slice(0, 5)" :key="p.penaltyId" class="row-between">
        <view>
          <view class="value" style="text-align: left">{{ p.penaltyTypeLabel }}</view>
          <view class="muted">{{ p.reason }}</view>
        </view>
        <text class="tag" :class="p.status === 'ACTIVE' ? 'danger' : 'gray'">{{ p.statusLabel }}</text>
      </view>
    </view>

    <view class="card">
      <view class="row-between" @tap="go('/pages/role/index')">
        <text class="label">切换视角</text>
        <text class="muted">前往 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/settings/index')">
        <text class="label">设置与隐私</text>
        <text class="muted">账号安全 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/messages/index')">
        <text class="label">消息中心</text>
        <text class="muted">查看 ›</text>
      </view>
    </view>

    <view class="btn gray" @tap="logout">退出登录</view>

    <RoleTabBar role="DRIVER" active="mine" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi, driverApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { applySession, resetSession, session } from '@/store/session'

const orders = ref<any[]>([])
const penalties = ref<any[]>([])
const orderCount = ref(0)
const penaltyCount = ref(0)
const trainingText = ref('查看')

const profile = computed(() => session.profile || {})
const initial = computed(() => ((session.profile && session.profile.nickname) || '代').substring(0, 1))
const qualificationText = computed(() => {
  const s = session.profile && session.profile.driverQualificationStatus
  const map: Record<string, string> = {
    DRAFT: '资料待完善',
    SUBMITTED: '审核中',
    APPROVED: '认证已通过',
    REJECTED: '审核未通过',
    EXPIRED: '认证已到期'
  }
  return map[s || ''] || '未认证'
})

onShow(() => {
  commonApi
    .session()
    .then((view: any) => applySession(view))
    .catch(() => {})
  loadOrders()
  loadPenalties()
  loadTraining()
})

async function loadTraining() {
  try {
    const view: any = await driverApi.training()
    const rows: any[] = (view && view.rows) || []
    if (!rows.length) {
      trainingText.value = '暂无课程'
      return
    }
    const passed = rows.filter((r) => r.progressStatus === 'PASSED').length
    trainingText.value = passed >= rows.length ? '已全部通过' : `${passed}/${rows.length} 已通过`
  } catch (e) {
    trainingText.value = '查看'
  }
}

async function loadOrders() {
  try {
    const page: any = await driverApi.orders()
    orders.value = (page && page.rows) || []
    orderCount.value = Number((page && page.total) || orders.value.length)
  } catch (e) {
    orders.value = []
  }
}

async function loadPenalties() {
  try {
    const view: any = await driverApi.penalties()
    penalties.value = (view && view.rows) || (Array.isArray(view) ? view : [])
    penaltyCount.value = penalties.value.length
  } catch (e) {
    penalties.value = []
  }
}

function openTask(o: any) {
  uni.navigateTo({ url: `/pages/driver/task/index?orderNo=${o.orderNo}` })
}

function go(url: string) {
  uni.navigateTo({ url })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前账号？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await commonApi.logout(false)
      } catch (e) {
        // 忽略
      }
      resetSession()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}
</script>

<style scoped>
.profile-card {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 36rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  font-weight: 600;
}

.info {
  flex: 1;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.sub {
  margin-top: 6rpx;
  font-size: 24rpx;
  opacity: 0.85;
}
</style>
