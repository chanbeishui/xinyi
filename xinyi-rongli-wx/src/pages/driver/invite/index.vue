<template>
  <view class="page">
    <view v-if="invite" class="hero">
      <view class="hero-title">新的派单邀约</view>
      <view class="hero-sub">{{ invite.orderNo }}</view>
      <view class="hero-timer">{{ countdownText(invite.expiresAt) }}</view>
    </view>

    <view v-if="invite" class="card">
      <view class="card-title">订单信息</view>
      <view class="row-between">
        <text class="label">车牌</text>
        <text class="value">{{ vehicle.plateNo || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">车型</text>
        <text class="value">{{ vehicle.vehicleTypeName || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">接车地址</text>
        <text class="value" style="max-width: 62%">{{ vehicle.address || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">检测点</text>
        <text class="value">{{ invite.stationName }}</text>
      </view>
      <view class="row-between">
        <text class="label">上门时段</text>
        <text class="value">{{ windowLabel(invite.pickupStartsAt) }}</text>
      </view>
      <view v-if="invite.hint" class="muted" style="margin-top: 12rpx">{{ invite.hint }}</view>
    </view>

    <view class="card">
      <view class="card-title">接单说明</view>
      <view class="muted">
        接单后请按约定时间上门，交接时需与车主核对 4 位交接码并完成车况留证；
        接车后如需取消请联系运营，已接车不可自助解除。
      </view>
    </view>

    <view class="action-bar">
      <view class="btn gray" style="flex: 1" @tap="respond(false)">放弃</view>
      <view class="btn" style="flex: 2" @tap="respond(true)">接受邀约</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { driverApi } from '@/api'
import { countdownText, windowLabel } from '@/utils/format'

const invite = ref<any>(null)
const vehicle = computed(() => (invite.value && invite.value.vehicle) || {})

onLoad((options) => {
  const id = (options && options.inviteId) || ''
  if (id) load(id)
})

async function load(id: string) {
  try {
    invite.value = await driverApi.invite(id)
  } catch (e) {
    invite.value = null
  }
}

async function respond(accept: boolean) {
  if (!invite.value) return
  try {
    const res: any = await driverApi.respondInvite(invite.value.inviteId, accept, accept ? undefined : '本轮不便承接')
    if (accept && res && res.accepted) {
      uni.showToast({ title: '已接单', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: `/pages/driver/task/index?orderNo=${invite.value.orderNo}` })
      }, 600)
    } else {
      uni.navigateBack()
    }
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 36rpx 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.hero-title {
  font-size: 36rpx;
  font-weight: 600;
}

.hero-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.9;
}

.hero-timer {
  margin-top: 18rpx;
  display: inline-flex;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.2);
  font-size: 24rpx;
}
</style>
