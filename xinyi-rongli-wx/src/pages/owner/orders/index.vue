<template>
  <view class="page">
    <scroll-view scroll-x class="filters" :show-scrollbar="false">
      <view class="chip-row">
        <view
          v-for="f in filters"
          :key="f.value"
          class="chip chip-row__item"
          :class="{ on: status === f.value }"
          hover-class="btn-hover"
          @tap="switchStatus(f.value)"
        >
          {{ f.label }}
        </view>
      </view>
    </scroll-view>

    <template v-if="loading">
      <view v-for="n in 3" :key="n" class="slip skeleton-card">
        <view class="skeleton" style="width: 44%; height: 34rpx"></view>
        <view class="skeleton" style="width: 66%; height: 24rpx; margin-top: 16rpx"></view>
        <view class="skeleton" style="width: 100%; height: 1rpx; margin-top: 24rpx"></view>
        <view class="skeleton" style="width: 52%; height: 24rpx; margin-top: 24rpx"></view>
      </view>
    </template>

    <template v-else>
      <view
        v-for="o in orders"
        :key="o.orderNo"
        class="slip"
        hover-class="card-hover"
        @tap="open(o)"
      >
        <view class="slip__head">
          <view class="slip__main">
            <view class="slip__plate">{{ o.plateNo }}</view>
            <view class="slip__sub ellipsis">{{ o.vehicleTypeName }} · {{ o.stationName }}</view>
          </view>
          <view class="status" :class="statusStyle(o.fulfillmentStatus)">
            {{ o.fulfillmentStatusLabel }}
          </view>
        </view>

        <view class="slip__row">
          <text class="slip__key">上门时段</text>
          <text class="slip__val">{{ windowLabel(o.pickupStartsAt) }}</text>
        </view>

        <view class="slip__foot">
          <text class="slip__no">{{ o.orderNo }} · {{ shortTime(o.createdAt) }}</text>
          <text class="price"><text class="price__unit">¥</text>{{ o.totalYuan }}</text>
        </view>
      </view>
    </template>

    <view v-if="!loading && !orders.length" class="empty">
      <view class="empty__title">暂无预约记录</view>
      <view class="empty__hint">
        {{ status ? '这个状态下还没有订单，换个筛选看看' : '下单后这里会显示进度、交接码与检测结果' }}
      </view>
      <view v-if="!status" class="empty__action" hover-class="btn-hover" @tap="goBooking">去预约</view>
    </view>

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

function statusStyle(s: string) {
  if (s === 'COMPLETED') return 'status--ok'
  if (s === 'CANCELLED' || s === 'CLOSED') return 'status--gray'
  if (s === 'PENDING_PAY') return 'status--warn'
  return 'status--brand'
}

function open(o: any) {
  uni.navigateTo({ url: `/pages/owner/order/detail?orderNo=${o.orderNo}` })
}

function goBooking() {
  uni.navigateTo({ url: '/pages/owner/home/index' })
}
</script>

<style scoped>
.filters {
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.skeleton-card {
  padding: 24rpx;
}
</style>
