<template>
  <view class="page">
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="ink-panel__eyebrow">检测工作台</view>
      <view class="ink-panel__title">{{ stationName }}</view>
      <view class="ink-panel__desc ellipsis">{{ stationAddress || '检测点信息未完善' }}</view>
      <view class="ink-panel__foot">
        <view class="panel-actions">
          <view class="ghost-chip" hover-class="ghost-chip--press" @tap="switchStation">切换检测点</view>
          <view class="ghost-chip" hover-class="ghost-chip--press" @tap="openCapacity">时段容量</view>
        </view>
        <text class="panel-meta">当前 {{ orders.length }} 单</text>
      </view>
    </view>

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

    <view v-for="o in orders" :key="o.orderNo" class="slip" hover-class="card-hover" @tap="open(o)">
      <view class="slip__head">
        <view class="slip__main">
          <view class="slip__plate">{{ o.plateNo }}</view>
          <view class="slip__sub ellipsis">{{ o.vehicleTypeName }} · 车主 {{ o.ownerName || '-' }}</view>
        </view>
        <view class="status status--brand">{{ o.fulfillmentStatusLabel }}</view>
      </view>
      <view class="slip__row">
        <text class="slip__key">上门时段</text>
        <text class="slip__val num">{{ windowLabel(o.pickupStartsAt) }}</text>
      </view>
      <view class="slip__foot">
        <text class="slip__no">{{ o.orderNo }}</text>
        <view class="chev"></view>
      </view>
    </view>

    <view v-if="!orders.length" class="empty">
      <view class="empty__title">当前筛选下暂无订单</view>
      <view class="empty__hint">车辆到站后会出现在这里，下拉可刷新</view>
    </view>

    <RoleTabBar role="STATION" active="home" />

    <!-- 时段容量：点开才拉数据，用底部弹层避免把列表挤到看不见 -->
    <view v-if="capacityVisible" class="mask" @tap="capacityVisible = false">
      <view class="sheet" @tap.stop>
        <view class="sheet__head">
          <text class="sheet__title">时段容量</text>
          <text class="sheet__close" @tap="capacityVisible = false">关闭</text>
        </view>
        <view v-if="capacitySlots.length" class="slots">
          <view v-for="s in capacitySlots" :key="s.slotId" class="slot">
            <view class="slot__top">
              <text class="slot__label">{{ s.label }}</text>
              <text class="slot__num num">{{ s.usedQuota }} / {{ s.totalQuota }}</text>
            </view>
            <view class="slot__bar">
              <view class="slot__fill" :style="{ width: usageWidth(s) }"></view>
            </view>
          </view>
        </view>
        <view v-else class="empty">
          <view class="empty__title">暂无时段数据</view>
          <view class="empty__hint">容量按站点接收时段生成，打开检测点后自动补齐</view>
        </view>
      </view>
    </view>
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
const capacityVisible = ref(false)

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

function openCapacity() {
  capacityVisible.value = true
  loadCapacity()
}

/** 占用比例，用来画一条容量进度条；除零时返回 0 */
function usageWidth(slot: any): string {
  const total = Number(slot.totalQuota || 0)
  const used = Number(slot.usedQuota || 0)
  if (!total) return '0%'
  const percent = Math.min(100, Math.round((used / total) * 100))
  return `${percent}%`
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
.panel-actions {
  display: flex;
}

.ghost-chip {
  height: 56rpx;
  padding: 0 22rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.28);
  border-radius: 28rpx;
  font-size: 22rpx;
  line-height: 54rpx;
  color: rgba(255, 255, 255, 0.9);
}

.ghost-chip + .ghost-chip {
  margin-left: 14rpx;
}

.ghost-chip--press {
  background: rgba(255, 255, 255, 0.12);
}

.panel-meta {
  font-size: 21rpx;
  letter-spacing: 1rpx;
  color: rgba(255, 255, 255, 0.5);
}

.filters {
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slots {
  margin-top: 8rpx;
}

.slot {
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--line-2);
}

.slot:last-child {
  border-bottom: none;
}

.slot__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.slot__label {
  font-size: 26rpx;
  color: var(--ink);
}

.slot__num {
  font-size: 24rpx;
  color: var(--ink-2);
}

.slot__bar {
  margin-top: 12rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: #eef1f5;
  overflow: hidden;
}

.slot__fill {
  height: 100%;
  border-radius: 4rpx;
  background: var(--brand);
}
</style>
