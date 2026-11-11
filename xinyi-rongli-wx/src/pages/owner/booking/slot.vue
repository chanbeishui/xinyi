<template>
  <view class="page">
    <view class="page-intro">
      <view class="eyebrow">上门接车</view>
      <view class="page-intro__title">选择时段</view>
      <view class="page-intro__hint">可预约未来 8 天，开始前 30 分钟截止预约。下拉可刷新余量。</view>
    </view>

    <view v-for="group in groups" :key="group.date" class="day">
      <view class="day__head">
        <text class="day__label num">{{ group.dateLabel }}</text>
        <text class="day__meta">{{ group.items.length }} 个时段</text>
      </view>

      <view class="slots">
        <view
          v-for="s in group.items"
          :key="s.slotId"
          class="slot"
          :class="{ 'slot--on': picked === s.slotId, 'slot--off': !s.selectable }"
          hover-class="slot-hover"
          @tap="pick(s)"
        >
          <view class="slot__time num">{{ timeOf(s.startsAt) }} - {{ timeOf(s.endsAt) }}</view>
          <view class="slot__left">{{ s.selectable ? `剩 ${s.remaining} 个` : '不可约' }}</view>
        </view>
      </view>
    </view>

    <view v-if="!groups.length" class="empty">
      <view class="empty__title">暂无可预约时段</view>
      <view class="empty__hint">检测站可能已满约或尚未放出班次，稍后刷新再看</view>
    </view>

    <view class="footer-space"></view>

    <view class="action-bar">
      <view class="btn gray" hover-class="btn-hover" @tap="refresh">刷新</view>
      <view class="btn btn-wide" :class="{ disabled: !picked }" hover-class="btn-hover" @tap="next">
        {{ picked ? '下一步 · 确认信息' : '请先选择时段' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import { shortTime, toDate } from '@/utils/format'

const slots = ref<any[]>([])
const picked = ref('')
const vehicleId = ref('')
const addressId = ref('')

const groups = computed(() => {
  const map = new Map<string, any[]>()
  for (const s of slots.value) {
    const d = toDate(s.startsAt)
    if (!d) continue
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(s)
  }
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return Array.from(map.entries()).map(([date, items]) => {
    const d = toDate(items[0].startsAt)
    const label = d ? `${date} ${week[d.getDay()]}` : date
    return { date, dateLabel: label, items }
  })
})

onLoad((options) => {
  vehicleId.value = (options && options.vehicleId) || ''
  addressId.value = (options && options.addressId) || ''
  load()
})

onPullDownRefresh(() => {
  load().finally(() => uni.stopPullDownRefresh())
})

async function load() {
  try {
    slots.value = ((await ownerApi.slots()) as any) || []
  } catch (e) {
    slots.value = []
  }
}

function refresh() {
  picked.value = ''
  load()
}

function timeOf(iso: string) {
  return shortTime(iso).substring(6)
}

function pick(s: any) {
  if (!s.selectable) return
  picked.value = s.slotId
}

function next() {
  if (!picked.value) return
  uni.navigateTo({
    url: `/pages/owner/booking/confirm?vehicleId=${vehicleId.value}&addressId=${addressId.value}&slotId=${picked.value}`
  })
}
</script>

<style scoped>
.page-intro {
  padding: 4rpx 6rpx 20rpx;
}

.page-intro__title {
  margin-top: 8rpx;
  font-size: 40rpx;
  font-weight: 600;
  letter-spacing: 0.6rpx;
}

.page-intro__hint {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: var(--ink-3);
}

.day {
  margin-bottom: 24rpx;
}

.day__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 0 6rpx 12rpx;
}

.day__label {
  font-size: 26rpx;
  font-weight: 600;
  letter-spacing: 0.8rpx;
}

.day__meta {
  font-size: 21rpx;
  color: var(--ink-3);
}

/* 三列时段：用 margin 而不是 gap，兼容不支持 flex gap 的旧 WebView */
.slots {
  display: flex;
  flex-wrap: wrap;
}

.slot {
  width: calc((100% - 24rpx) / 3);
  margin-right: 12rpx;
  margin-bottom: 12rpx;
  padding: 16rpx 8rpx;
  border: 1rpx solid var(--line);
  border-radius: 12rpx;
  background: #fff;
  text-align: center;
}

.slot:nth-child(3n) {
  margin-right: 0;
}

.slot-hover {
  background: #fafbfc;
}

.slot--on {
  border-color: var(--brand);
  background: var(--brand-soft);
}

.slot--off {
  opacity: 0.4;
  border-style: dashed;
}

.slot__time {
  font-size: 25rpx;
  font-weight: 500;
  letter-spacing: 0.4rpx;
}

.slot__left {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: var(--ink-3);
}

.slot--on .slot__left {
  color: var(--brand);
}

.btn-wide {
  flex: 2;
}
</style>
