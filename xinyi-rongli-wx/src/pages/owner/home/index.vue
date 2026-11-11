<template>
  <view class="page">
    <!-- 墨色抬头：先说清服务，再给出当前进度 -->
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="ink-panel__eyebrow">机动车检测 · 上门代驾</view>
      <view class="ink-panel__title">预约检车，代驾上门接车</view>
      <view class="ink-panel__desc">选好车辆和接车地址，代驾按时上门，到站检测、缴费、结果回传都由我们代办。</view>

      <view class="steps">
        <view class="step" :class="{ 'step--done': currentStep > 0, 'step--now': currentStep === 0 }">
          <text class="step__no">01</text>
          <text class="step__label">选车</text>
        </view>
        <view class="step__link"></view>
        <view class="step" :class="{ 'step--done': currentStep > 1, 'step--now': currentStep === 1 }">
          <text class="step__no">02</text>
          <text class="step__label">时段</text>
        </view>
        <view class="step__link"></view>
        <view class="step" :class="{ 'step--done': currentStep > 2, 'step--now': currentStep === 2 }">
          <text class="step__no">03</text>
          <text class="step__label">支付</text>
        </view>
        <view class="step__link"></view>
        <view class="step">
          <text class="step__no">04</text>
          <text class="step__label">接车</text>
        </view>
      </view>
    </view>

    <!-- 工单首段：车辆与接车地址，缺一项就无法查时段 -->
    <view class="slip">
      <view class="slip__head" hover-class="card-hover" @tap="pickVehicle">
        <view class="slip__main">
          <text class="eyebrow">车辆</text>
          <view class="slip__plate">{{ vehicle ? vehicle.plateNo : '未选择' }}</view>
          <view class="slip__sub">
            {{ vehicle ? vehicle.vehicleTypeName || vehicle.vehicleTypeCode : '先添加一辆要年检的车' }}
          </view>
        </view>
        <view class="status" :class="vehicle ? 'status--brand' : 'status--warn'">
          {{ vehicle ? '更换' : '添加' }}
        </view>
      </view>

      <view class="slip__row slip__row--tap" hover-class="card-hover" @tap="pickAddress">
        <text class="slip__key">接车地址</text>
        <view class="slip__val-wrap">
          <text class="slip__val ellipsis">{{ address ? address.address : '未填写' }}</text>
          <text class="slip__sub-inline">{{ address ? address.label || '默认地址' : '点此选择' }}</text>
        </view>
        <view class="chev"></view>
      </view>
    </view>

    <view class="btn block" :class="{ disabled: !canBook }" hover-class="btn-hover" @tap="startBooking">
      查询可约时段
    </view>
    <view v-if="!canBook" class="form-hint">先选择车辆与接车地址，才能查询时段</view>

    <view class="section-head">
      <text class="section-head__title">我的预约</text>
      <text class="section-head__meta">记录 · 售后 · 车辆</text>
    </view>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/orders/index')">
        <text class="list-row__key">全部预约记录</text>
        <view class="row">
          <text class="list-row__hint">查看</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/after-sales/list')">
        <text class="list-row__key">售后与退款</text>
        <view class="row">
          <text class="list-row__hint">查看</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/vehicle/list')">
        <text class="list-row__key">常用车辆与地址</text>
        <view class="row">
          <text class="list-row__hint">管理</text>
          <view class="chev"></view>
        </view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">可服务检测点</text>
      <text class="section-head__meta">{{ stations.length }} 个</text>
    </view>

    <view class="list">
      <view v-for="s in stations" :key="s.stationId" class="station">
        <view class="station__name">{{ s.name }}</view>
        <view class="station__addr ellipsis">{{ s.address || '地址待完善' }}</view>
      </view>
      <view v-if="!stations.length" class="empty">
        <view class="empty__title">暂无可用检测点</view>
        <view class="empty__hint">检测点由平台按区域授权，稍后刷新或联系客服确认</view>
      </view>
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

/** 步骤条当前位：0 选车、1 选时段…（本页只走到「选时段」之前） */
const currentStep = computed(() => (vehicle.value && address.value ? 1 : 0))

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
.slip__main {
  flex: 1;
  min-width: 0;
}

.slip__val-wrap {
  flex: 1;
  min-width: 0;
  padding-left: 16rpx;
  text-align: right;
}

.slip__sub-inline {
  display: block;
  margin-top: 4rpx;
  font-size: 21rpx;
  color: var(--ink-3);
}

.station {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.station:last-child {
  border-bottom: none;
}

.station__name {
  font-size: 28rpx;
  font-weight: 500;
  letter-spacing: 0.3rpx;
}

.station__addr {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: var(--ink-3);
}
</style>
