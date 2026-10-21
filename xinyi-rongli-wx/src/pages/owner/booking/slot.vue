<template>
  <view class="page">
    <view class="card">
      <view class="card-title">上门接车时段</view>
      <view class="muted">可预约未来 8 天，开始前 30 分钟截止预约。</view>
    </view>

    <view v-for="group in groups" :key="group.date" class="card">
      <view class="card-title">{{ group.dateLabel }}</view>
      <view class="slots">
        <view
          v-for="s in group.items"
          :key="s.slotId"
          class="slot"
          :class="{ on: picked === s.slotId, off: !s.selectable }"
          @tap="pick(s)"
        >
          <view class="slot-time">{{ timeOf(s.startsAt) }} - {{ timeOf(s.endsAt) }}</view>
          <view class="slot-left">{{ s.selectable ? `剩 ${s.remaining} 个` : '不可约' }}</view>
        </view>
      </view>
    </view>

    <view v-if="!groups.length" class="empty">暂无可预约时段，请稍后再试</view>

    <view class="action-bar">
      <view class="btn gray" style="flex: 1" @tap="refresh">刷新</view>
      <view class="btn" :class="{ disabled: !picked }" style="flex: 2" @tap="next">下一步</view>
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
.slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.slot {
  width: calc((100% - 32rpx) / 3);
  border: 1rpx solid var(--line);
  border-radius: 14rpx;
  padding: 18rpx 12rpx;
  text-align: center;
}

.slot.on {
  border-color: var(--brand);
  background: var(--brand-soft);
}

.slot.off {
  opacity: 0.4;
}

.slot-time {
  font-size: 26rpx;
  font-weight: 500;
}

.slot-left {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: var(--muted);
}

.slot.on .slot-left {
  color: var(--brand);
}
</style>
