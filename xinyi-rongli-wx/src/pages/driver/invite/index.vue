<template>
  <view class="page">
    <view v-if="invite" class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="ink-panel__eyebrow">新的派单邀约</view>
      <view class="ink-panel__title">是否承接这一单</view>
      <view class="ink-panel__desc num">单号 {{ invite.orderNo }}</view>
      <view class="timer">
        <text class="timer__label">剩余</text>
        <text class="timer__value num">{{ countdownText(invite.expiresAt) }}</text>
      </view>
    </view>

    <view v-if="invite" class="slip">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">车辆</text>
          <view class="slip__plate">{{ vehicle.plateNo || '-' }}</view>
          <view class="slip__sub">{{ vehicle.vehicleTypeName || '-' }}</view>
        </view>
      </view>

      <view class="slip__row slip__row--stack">
        <text class="slip__key">接车地址</text>
        <text class="slip__val slip__val--wrap">{{ vehicle.address || '-' }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">检测点</text>
        <text class="slip__val">{{ invite.stationName }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">上门时段</text>
        <text class="slip__val num">{{ windowLabel(invite.pickupStartsAt) }}</text>
      </view>
      <view v-if="invite.hint" class="slip__foot">
        <text class="slip__no">{{ invite.hint }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">接单说明</view>
      <view class="muted">
        接单后请按约定时间上门，交接时需与车主核对 4 位交接码并完成车况留证；
        接车后如需取消请联系运营，已接车不可自助解除。
      </view>
    </view>

    <view class="footer-space"></view>

    <view class="action-bar">
      <view class="btn gray" hover-class="btn-hover" @tap="respond(false)">放弃</view>
      <view class="btn btn-wide" hover-class="btn-hover" @tap="respond(true)">接受邀约</view>
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
.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slip__row--stack {
  display: block;
}

.slip__val--wrap {
  display: block;
  margin-top: 8rpx;
  text-align: left;
  line-height: 1.65;
  color: var(--ink-2);
}

/* 倒计时是这一页最紧的信息：琥珀色，和「新邀约」的紧迫感一致 */
.timer {
  display: flex;
  align-items: baseline;
  margin-top: 22rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.12);
}

.timer__label {
  font-size: 21rpx;
  letter-spacing: 1rpx;
  color: rgba(255, 255, 255, 0.5);
}

.timer__value {
  margin-left: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #f0b56a;
}

.btn-wide {
  flex: 2;
}
</style>
