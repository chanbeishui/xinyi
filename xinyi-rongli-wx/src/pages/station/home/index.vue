<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">{{ stationName }}</view>
      <view class="hero-sub">{{ stationAddress || '检测点信息未完善' }}</view>
      <view class="hero-actions">
        <view class="hero-chip" @tap="switchStation">切换检测点</view>
        <view class="hero-chip" @tap="loadCapacity">时段容量</view>
      </view>
    </view>

    <scroll-view scroll-x class="filters">
      <view class="filter-row">
        <view
          v-for="f in filters"
          :key="f.value"
          class="chip"
          :class="{ on: status === f.value }"
          @tap="switchStatus(f.value)"
        >
          {{ f.label }}
        </view>
      </view>
    </scroll-view>

    <view v-for="o in orders" :key="o.orderNo" class="card" @tap="open(o)">
      <view class="row-between">
        <view>
          <view class="plate">{{ o.plateNo }}</view>
          <view class="muted" style="margin-top: 4rpx">{{ o.vehicleTypeName }} · 车主 {{ o.ownerName || '-' }}</view>
        </view>
        <view class="tag">{{ o.fulfillmentStatusLabel }}</view>
      </view>
      <view class="divider"></view>
      <view class="row-between">
        <text class="muted">上门 {{ windowLabel(o.pickupStartsAt) }}</text>
        <text class="muted">{{ o.orderNo }}</text>
      </view>
    </view>

    <view v-if="!orders.length" class="empty">当前筛选下暂无订单</view>

    <view v-if="capacitySlots.length" class="card">
      <view class="card-title">时段容量</view>
      <view v-for="s in capacitySlots" :key="s.slotId" class="row-between">
        <text class="label">{{ s.label }}</text>
        <text class="value">总 {{ s.totalQuota }} · 已占 {{ s.usedQuota }}</text>
      </view>
    </view>

    <RoleTabBar role="STATION" active="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { commonApi, stationApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { applySession, session } from '@/store/session'
import { windowLabel } from '@/utils/format'

const filters = [
  { label: '全部', value: '' },
  { label: '已到站', value: 'ARRIVED_STATION' },
  { label: '检测中', value: 'INSPECTING' },
  { label: '待取车', value: 'INSPECTED' },
  { label: '返还中', value: 'CAR_TAKEN' }
]

const orders = ref<any[]>([])
const status = ref('')
const info = ref<any>({})
const stations = ref<any[]>([])
const capacitySlots = ref<any[]>([])

const stationName = computed(() => (info.value && info.value.name) || '检测工作台')
const stationAddress = computed(() => (info.value && info.value.address) || '')

onShow(() => {
  load()
})

onPullDownRefresh(() => {
  load().finally(() => uni.stopPullDownRefresh())
})

async function load() {
  await loadInfo()
  await loadOrders()
}

async function loadInfo() {
  try {
    info.value = await stationApi.info()
  } catch (e) {
    info.value = {}
  }
  try {
    stations.value = ((await stationApi.authorizedStations()) as any) || []
  } catch (e) {
    stations.value = []
  }
}

async function loadOrders() {
  try {
    const page: any = await stationApi.orders(status.value)
    orders.value = (page && page.rows) || []
  } catch (e) {
    orders.value = []
  }
}

async function loadCapacity() {
  try {
    const view: any = await stationApi.capacity()
    capacitySlots.value = (view && view.slots) || []
  } catch (e) {
    capacitySlots.value = []
  }
}

function switchStatus(v: string) {
  status.value = v
  loadOrders()
}

async function switchStation() {
  if (stations.value.length <= 1) {
    uni.showToast({ title: '当前仅授权一个检测点', icon: 'none' })
    return
  }
  uni.showActionSheet({
    itemList: stations.value.map((s: any) => s.name),
    success: async (res) => {
      const target = stations.value[res.tapIndex]
      try {
        const view: any = await commonApi.switchStation(target.stationId)
        applySession(view)
        load()
        uni.showToast({ title: '已切换', icon: 'success' })
      } catch (e) {
        // 已提示
      }
    }
  })
}

function open(o: any) {
  uni.navigateTo({ url: `/pages/station/order/index?orderNo=${o.orderNo}` })
}

void session
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 32rpx;
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
  opacity: 0.88;
}

.hero-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.hero-chip {
  padding: 8rpx 22rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.2);
  font-size: 23rpx;
}

.filters {
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.filter-row {
  display: flex;
  gap: 14rpx;
}

.chip {
  flex-shrink: 0;
  padding: 12rpx 26rpx;
  border-radius: 30rpx;
  background: #fff;
  color: var(--sub);
  font-size: 25rpx;
  border: 1rpx solid var(--line);
}

.chip.on {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
}

.plate {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}
</style>
