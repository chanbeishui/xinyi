<template>
  <view class="page">
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
          <view class="muted" style="margin-top: 4rpx">{{ o.vehicleTypeName }} · {{ o.stationName }}</view>
        </view>
        <view class="tag" :class="statusClass(o.fulfillmentStatus)">{{ o.fulfillmentStatusLabel }}</view>
      </view>
      <view class="divider"></view>
      <view class="row-between">
        <text class="muted">上门 {{ windowLabel(o.pickupStartsAt) }}</text>
        <text class="price" style="font-size: 30rpx">¥{{ o.totalYuan }}</text>
      </view>
      <view class="row-between">
        <text class="muted">订单号 {{ o.orderNo }}</text>
        <text class="muted">{{ shortTime(o.createdAt) }}</text>
      </view>
    </view>

    <view v-if="!orders.length && !loading" class="empty">暂无预约记录</view>
    <view v-if="loading" class="empty">加载中…</view>

    <RoleTabBar role="OWNER" active="orders" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { shortTime, windowLabel } from '@/utils/format'

const filters = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'PENDING_PAY' },
  { label: '待派单', value: 'PENDING_DISPATCH' },
  { label: '派单中', value: 'DISPATCHING' },
  { label: '服务中', value: 'PICKED_UP' },
  { label: '待确认', value: 'WAIT_CONFIRM' },
  { label: '已完成', value: 'COMPLETED' }
]

const orders = ref<any[]>([])
const status = ref('')
const loading = ref(false)

onShow(() => {
  load()
})

onPullDownRefresh(() => {
  load().finally(() => uni.stopPullDownRefresh())
})

async function load() {
  loading.value = true
  try {
    const page: any = await ownerApi.orders(status.value)
    orders.value = (page && page.rows) || []
  } catch (e) {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function switchStatus(value: string) {
  status.value = value
  load()
}

function statusClass(s: string) {
  if (s === 'COMPLETED') return 'ok'
  if (s === 'CANCELLED' || s === 'CLOSED') return 'gray'
  if (s === 'PENDING_PAY') return 'warn'
  return ''
}

function open(o: any) {
  uni.navigateTo({ url: `/pages/owner/order/detail?orderNo=${o.orderNo}` })
}
</script>

<style scoped>
.filters {
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.filter-row {
  display: flex;
  gap: 14rpx;
  padding: 4rpx 2rpx 8rpx;
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
