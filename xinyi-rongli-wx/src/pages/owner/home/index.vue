<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">机动车检测预约</view>
      <view class="hero-desc">上门接车 · 代驾送检 · 结果回传</view>
      <view class="hero-steps">
        <view class="step">选车</view>
        <view class="step-line"></view>
        <view class="step">选时段</view>
        <view class="step-line"></view>
        <view class="step">支付</view>
        <view class="step-line"></view>
        <view class="step">上门接车</view>
      </view>
    </view>

    <view class="card" @tap="pickVehicle">
      <view class="row-between">
        <view>
          <view class="card-title" style="margin-bottom: 6rpx">选择车辆</view>
          <view v-if="vehicle" class="muted">{{ vehicle.plateNo }} · {{ vehicle.vehicleTypeName || vehicle.vehicleTypeCode }}</view>
          <view v-else class="muted">尚未添加车辆，请先添加</view>
        </view>
        <view class="tag" :class="vehicle ? '' : 'warn'">{{ vehicle ? '更换' : '添加' }}</view>
      </view>
    </view>

    <view class="card" @tap="pickAddress">
      <view class="row-between">
        <view style="flex: 1">
          <view class="card-title" style="margin-bottom: 6rpx">上门接车地址</view>
          <view v-if="address" class="muted">{{ address.label || '默认地址' }} · {{ address.address }}</view>
          <view v-else class="muted">请选择或新增接车地址</view>
        </view>
        <view class="tag" :class="address ? '' : 'warn'">{{ address ? '更换' : '填写' }}</view>
      </view>
    </view>

    <view class="btn" :class="{ disabled: !canBook }" @tap="startBooking">查询可约时段</view>
    <view v-if="!canBook" class="muted" style="margin-top: 12rpx; text-align: center">
      请先选择车辆与上门地址
    </view>

    <view class="card" style="margin-top: 28rpx">
      <view class="card-title">我的预约</view>
      <view class="row-between" @tap="go('/pages/owner/orders/index')">
        <text class="label">全部预约记录</text>
        <text class="muted">查看 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/owner/after-sales/list')">
        <text class="label">售后与退款</text>
        <text class="muted">查看 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/owner/vehicle/list')">
        <text class="label">常用车辆与地址</text>
        <text class="muted">管理 ›</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">检测点</view>
      <view v-for="s in stations" :key="s.stationId" class="row-between">
        <view style="flex: 1">
          <view class="value" style="text-align: left">{{ s.name }}</view>
          <view class="muted">{{ s.address || '地址待完善' }}</view>
        </view>
      </view>
      <view v-if="!stations.length" class="muted">暂无可服务检测点</view>
    </view>

    <RoleTabBar role="OWNER" active="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi, ownerApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { session } from '@/store/session'

const vehicles = ref<any[]>([])
const addresses = ref<any[]>([])
const stations = ref<any[]>([])
const pickedVehicleId = ref<string>('')
const pickedAddressId = ref<string>('')

const vehicle = computed(() => {
  if (pickedVehicleId.value) {
    return vehicles.value.find((v) => String(v.vehicleId) === pickedVehicleId.value) || null
  }
  return vehicles.value.find((v) => v.isDefault) || vehicles.value[0] || null
})

const address = computed(() => {
  if (pickedAddressId.value) {
    return addresses.value.find((a) => String(a.addressId) === pickedAddressId.value) || null
  }
  return addresses.value.find((a) => a.isDefault) || addresses.value[0] || null
})

const canBook = computed(() => !!vehicle.value && !!address.value)

onShow(() => {
  load()
})

async function load() {
  try {
    const [v, a, s] = await Promise.all([ownerApi.vehicles(), ownerApi.addresses(), commonApi.stations()])
    vehicles.value = (v as any) || []
    addresses.value = (a as any) || []
    stations.value = (s as any) || []
  } catch (e) {
    // 请求层已提示
  }
}

function pickVehicle() {
  uni.$once('vehicle:selected', (id: string) => {
    pickedVehicleId.value = String(id)
  })
  uni.navigateTo({ url: '/pages/owner/vehicle/list?select=1' })
}

function pickAddress() {
  if (!addresses.value.length) {
    uni.showModal({
      title: '还没有接车地址',
      content: '请先新增一个上门接车地址',
      confirmText: '去新增',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/owner/vehicle/list?tab=address' })
      }
    })
    return
  }
  const items = addresses.value.map((a) => `${a.label || '地址'} ${a.address}`)
  uni.showActionSheet({
    itemList: items.slice(0, 6),
    success: (res) => {
      const target = addresses.value[res.tapIndex]
      if (target) pickedAddressId.value = String(target.addressId)
    }
  })
}

function startBooking() {
  if (!canBook.value) return
  uni.navigateTo({
    url: `/pages/owner/booking/slot?vehicleId=${vehicle.value.vehicleId}&addressId=${address.value.addressId}`
  })
}

function go(url: string) {
  uni.navigateTo({ url })
}

void session
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 36rpx 32rpx 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.hero-desc {
  margin-top: 8rpx;
  font-size: 25rpx;
  opacity: 0.9;
}

.hero-steps {
  margin-top: 30rpx;
  display: flex;
  align-items: center;
}

.step {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.18);
}

.step-line {
  flex: 1;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.35);
  margin: 0 10rpx;
}

.btn.disabled {
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.45;
}
</style>
