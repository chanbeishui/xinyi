<template>
  <view class="page">
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="profile">
        <view class="avatar">{{ initial }}</view>
        <view class="profile__info">
          <view class="profile__name">{{ profile.nickname || '代驾' }}</view>
          <view class="profile__sub">{{ qualificationText }}</view>
        </view>
        <view class="profile__role">代驾视角</view>
      </view>
    </view>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/qualification/index')">
        <text class="list-row__key">认证与复审</text>
        <view class="row">
          <text class="list-row__hint">{{ qualificationText }}</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/statement/index')">
        <text class="list-row__key">我的对账</text>
        <view class="row">
          <text class="list-row__hint">查看</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/ledger/index')">
        <text class="list-row__key">收支流水</text>
        <view class="row">
          <text class="list-row__hint">明细与收款</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/training/index')">
        <text class="list-row__key">服务培训</text>
        <view class="row">
          <text class="list-row__hint">{{ trainingText }}</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="loadOrders">
        <text class="list-row__key">接送任务记录</text>
        <view class="row">
          <text class="list-row__hint num">{{ orderCount }} 单</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/driver/penalty/index')">
        <text class="list-row__key">处罚与申诉</text>
        <view class="row">
          <text class="list-row__hint num">{{ penaltyCount }} 条</text>
          <view class="chev"></view>
        </view>
      </view>
    </view>

    <template v-if="orders.length">
      <view class="section-head">
        <text class="section-head__title">最近任务</text>
        <text class="section-head__meta">近 5 单</text>
      </view>

      <view class="list">
        <view
          v-for="o in orders.slice(0, 5)"
          :key="o.orderNo"
          class="list-row"
          hover-class="card-hover"
          @tap="openTask(o)"
        >
          <view class="task">
            <view class="task__no num">{{ o.orderNo }}</view>
            <view class="task__meta">{{ o.plateNo || '' }} {{ o.fulfillmentStatusLabel || '' }}</view>
          </view>
          <view class="chev"></view>
        </view>
      </view>
    </template>

    <template v-if="penalties.length">
      <view class="section-head">
        <text class="section-head__title">处罚记录</text>
        <text class="section-head__meta">{{ penaltyCount }} 条</text>
      </view>

      <view class="list">
        <view v-for="p in penalties.slice(0, 5)" :key="p.penaltyId" class="list-row">
          <view class="task">
            <view class="task__no">{{ p.penaltyTypeLabel }}</view>
            <view class="task__meta">{{ p.reason }}</view>
          </view>
          <text class="status" :class="p.status === 'ACTIVE' ? 'status--danger' : 'status--gray'">
            {{ p.statusLabel }}
          </text>
        </view>
      </view>
    </template>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/role/index')">
        <text class="list-row__key">切换视角</text>
        <view class="row">
          <text class="list-row__hint">前往</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/settings/index')">
        <text class="list-row__key">设置与隐私</text>
        <view class="row">
          <text class="list-row__hint">账号安全</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/messages/index')">
        <text class="list-row__key">消息中心</text>
        <view class="row">
          <text class="list-row__hint">查看</text>
          <view class="chev"></view>
        </view>
      </view>
    </view>

    <view class="btn gray block" hover-class="btn-hover" @tap="logout">退出登录</view>

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
.profile {
  display: flex;
  align-items: center;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.profile__info {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.profile__name {
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 0.6rpx;
}

.profile__sub {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.6);
}

.profile__role {
  flex: 0 0 auto;
  padding: 4rpx 14rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.28);
  border-radius: 4rpx;
  font-size: 20rpx;
  letter-spacing: 1rpx;
  color: rgba(255, 255, 255, 0.8);
}

.task {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.task__no {
  font-size: 26rpx;
  font-weight: 500;
  letter-spacing: 0.4rpx;
}

.task__meta {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}
</style>
