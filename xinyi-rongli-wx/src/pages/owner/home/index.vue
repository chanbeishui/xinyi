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

      <view class="slip__row slip__row--tap" hover-class="card-hover" @tap="scrollToStations">
        <text class="slip__key">检测点</text>
        <view class="slip__val-wrap">
          <text class="slip__val ellipsis">{{ pickedStation ? pickedStation.name : '未选择' }}</text>
          <text class="slip__sub-inline">
            {{ pickedStation ? (distanceText(pickedStation) || '在下方列表中更换') : '在下方可服务检测点中选择' }}
          </text>
        </view>
        <view class="chev"></view>
      </view>

      <view class="slip__row slip__row--tap" hover-class="card-hover" @tap="openPickupSheet">
        <text class="slip__key">上门接车时间</text>
        <view class="slip__val-wrap">
          <text class="slip__val ellipsis">{{ pickup.label }}</text>
          <text class="slip__sub-inline">
            {{ pickup.mode === 'immediate' ? '默认立即上门，可改为检测点营业时间内的时间' : '检测点营业时间内' }}
          </text>
        </view>
        <view class="chev"></view>
      </view>
    </view>

    <view class="btn block" :class="{ disabled: !canBook }" hover-class="btn-hover" @tap="startBooking">
      预约检测
    </view>
    <view v-if="!canBook" class="form-hint">{{ bookHint }}</view>

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

    <view id="station-list" class="list">
      <view
        v-for="s in stationList"
        :key="s.stationId"
        class="station"
        :class="{ 'station--on': isPicked(s) }"
        hover-class="card-hover"
        @tap="chooseStation(s)"
      >
        <view class="station__top">
          <view class="station__name">{{ s.name }}</view>
          <text v-if="distanceText(s)" class="station__dist">{{ distanceText(s) }}</text>
        </view>
        <view class="station__addr ellipsis">{{ s.address || '地址待完善' }}</view>
        <view v-if="isPicked(s)" class="station__badge">已选择</view>
      </view>
      <view v-if="!stations.length" class="empty">
        <view class="empty__title">暂无可用检测点</view>
        <view class="empty__hint">检测点由平台按区域授权，稍后刷新或联系客服确认</view>
      </view>
    </view>

    <!-- 上门接车时间：只列所选检测点营业时间内仍可约的班次 -->
    <view v-if="sheetVisible" class="sheet-mask" @tap="sheetVisible = false">
      <view class="sheet" @tap.stop>
        <view class="sheet__head">
          <text class="sheet__title">上门接车时间</text>
          <text class="sheet__sub">{{ pickedStation ? pickedStation.name : '' }}</text>
        </view>
        <scroll-view scroll-y class="sheet__body">
          <view class="sheet__item" :class="{ 'sheet__item--on': pickup.mode === 'immediate' }" @tap="pickImmediate">
            <text>立即上门</text>
            <text class="sheet__meta">尽量 30 分钟内到达</text>
          </view>
          <view v-for="g in slotGroups" :key="g.date">
            <view class="sheet__day">{{ g.dateLabel }}</view>
            <view
              v-for="s in g.items"
              :key="s.slotId"
              class="sheet__item"
              :class="{ 'sheet__item--on': pickup.slotId === String(s.slotId) }"
              @tap="pickSlot(s)"
            >
              <text>{{ timeText(s.startsAt) }} - {{ timeText(s.endsAt) }}</text>
              <text class="sheet__meta">剩 {{ s.remaining }} 个</text>
            </view>
          </view>
          <view v-if="!slotGroups.length" class="sheet__empty">
            该检测点营业时间内暂无可约时段，可先选「立即上门」
          </view>
        </scroll-view>
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
import { toDate } from '@/utils/format'

const vehicles = ref<any[]>([])
const addresses = ref<any[]>([])
const stations = ref<any[]>([])
const pickedVehicleId = ref<string>('')
const pickedAddressId = ref<string>('')
const pickedStationId = ref<string>('')

/** 当前定位，用于算到各检测点的直线距离；拿不到就退化为按原顺序展示 */
const myLng = ref(0)
const myLat = ref(0)
const located = ref(false)

/** 上门接车时间：默认立即上门，改选后落到具体班次 */
const pickup = ref<{ mode: 'immediate' | 'slot'; slotId: string; label: string }>({
  mode: 'immediate',
  slotId: '',
  label: '立即上门'
})
const slots = ref<any[]>([])
const sheetVisible = ref(false)

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

function haversine(lng1: number, lat1: number, lng2: number, lat2: number): number {
  const R = 6371000
  const rad = (d: number) => (d * Math.PI) / 180
  const dLat = rad(lat2 - lat1)
  const dLng = rad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

/** 检测点按距离升序；没有定位时保持服务端顺序 */
const stationList = computed(() => {
  const list = stations.value.map((s) => {
    const lng = Number(s.lng)
    const lat = Number(s.lat)
    const distance = located.value && lng && lat ? haversine(myLng.value, myLat.value, lng, lat) : null
    return { ...s, distance }
  })
  if (list.some((s) => s.distance !== null)) {
    list.sort((a, b) => (a.distance ?? Number.MAX_SAFE_INTEGER) - (b.distance ?? Number.MAX_SAFE_INTEGER))
  }
  return list
})

const pickedStation = computed(() => {
  if (pickedStationId.value) {
    return stationList.value.find((s) => String(s.stationId) === pickedStationId.value) || null
  }
  return stationList.value[0] || null
})

function distanceText(s: any): string {
  if (s.distance === null || s.distance === undefined) return ''
  return s.distance < 1000 ? `${Math.round(s.distance)} m` : `${(s.distance / 1000).toFixed(1)} km`
}

/** 模板里不用 String() 比较：小程序模板对全局对象调用的支持不完整 */
function isPicked(s: any): boolean {
  return !!pickedStationId.value && String(pickedStationId.value) === String(s.stationId)
}

/** 该检测点在指定时间的营业时间判断；weekdays 用 1=周一…7=周日 */
function inBusinessHours(station: any, iso: string): boolean {
  const hours = station && station.businessHours ? station.businessHours : null
  if (!hours || hours.startHour === undefined || hours.endHour === undefined) return true
  const d = toDate(iso)
  if (!d) return true
  const weekday = d.getDay() === 0 ? 7 : d.getDay()
  const days: number[] = Array.isArray(hours.weekdays) ? hours.weekdays : []
  if (days.length && !days.includes(weekday)) return false
  const hour = d.getHours()
  return hour >= Number(hours.startHour) && hour < Number(hours.endHour)
}

/** 弹层里可选的上门时间：只保留营业时间内、仍可预约且不早于 30 分钟后的班次 */
const slotGroups = computed(() => {
  const station = pickedStation.value
  if (!station) return []
  const map = new Map<string, any[]>()
  for (const s of slots.value) {
    if (!s.selectable || !inBusinessHours(station, s.startsAt)) continue
    const d = toDate(s.startsAt)
    if (!d) continue
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(s)
  }
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return Array.from(map.entries()).map(([date, items]) => {
    const d = toDate(items[0].startsAt)
    return { date, dateLabel: d ? `${date} ${week[d.getDay()]}` : date, items }
  })
})

/** 立即上门取最近的可用班次；没有可约班次时返回空 */
const nearestSlot = computed(() => {
  // 不用 flatMap：部分小程序基础库没有该方法，用 reduce 拼接最稳
  const flat = slotGroups.value.reduce((acc: any[], g: any) => acc.concat(g.items), [])
  return flat.length ? flat[0] : null
})

const canBook = computed(
  () => !!vehicle.value && !!address.value && !!pickedStation.value && (pickup.value.mode === 'slot' ? !!pickup.value.slotId : !!nearestSlot.value)
)

/** 按钮下方的缺项提示，按顺序只提示第一个没填的 */
const bookHint = computed(() => {
  if (!vehicle.value) return '请先选择车辆'
  if (!address.value) return '请先填写接车地址'
  if (!pickedStation.value) return '请选择检测点'
  if (pickup.value.mode === 'slot' && !pickup.value.slotId) return '请选择上门接车时间'
  if (pickup.value.mode === 'immediate' && !nearestSlot.value) return '该检测点当前没有可预约时段'
  return ''
})

/** 步骤条当前位：0 选车、1 选时段…（本页只走到「选时段」之前） */
const currentStep = computed(() => (canBook.value ? 1 : vehicle.value ? 1 : 0))

onShow(() => {
  load()
})

async function load() {
  try {
    const [v, a, s, sl] = await Promise.all([
      ownerApi.vehicles(),
      ownerApi.addresses(),
      commonApi.stations(),
      ownerApi.slots().catch(() => [])
    ])
    vehicles.value = (v as any) || []
    addresses.value = (a as any) || []
    stations.value = (s as any) || []
    slots.value = (sl as any) || []
  } catch (e) {
    // 请求层已提示
  }
  locateOnce()
}

/** 定位只取一次：失败就按服务端顺序展示，不反复弹权限 */
function locateOnce() {
  if (located.value) return
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      myLng.value = res.longitude
      myLat.value = res.latitude
      located.value = true
      // 默认选中最近的检测点，列表本身已按距离排序
      if (!pickedStationId.value && stationList.value.length) {
        pickedStationId.value = String(stationList.value[0].stationId)
      }
    },
    fail: () => {
      if (!pickedStationId.value && stationList.value.length) {
        pickedStationId.value = String(stationList.value[0].stationId)
      }
    }
  })
}

function chooseStation(s: any) {
  pickedStationId.value = String(s.stationId)
  // 换了检测点，原来选的时间可能不在它营业时间内，回到默认值重新选
  if (pickup.value.mode === 'slot') {
    pickup.value = { mode: 'immediate', slotId: '', label: '立即上门' }
  }
}

function openPickupSheet() {
  if (!pickedStation.value) {
    uni.showToast({ title: '请先选择检测点', icon: 'none' })
    return
  }
  sheetVisible.value = true
}

function timeText(iso: string): string {
  const d = toDate(iso)
  if (!d) return ''
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function scrollToStations() {
  uni.pageScrollTo({ selector: '#station-list', duration: 200 })
}

function pickImmediate() {
  pickup.value = { mode: 'immediate', slotId: '', label: '立即上门' }
  sheetVisible.value = false
}

function pickSlot(s: any) {
  const d = toDate(s.startsAt)
  const e = toDate(s.endsAt)
  const fmt = (x: Date | null) =>
    x ? `${String(x.getMonth() + 1).padStart(2, '0')}-${String(x.getDate()).padStart(2, '0')} ${String(x.getHours()).padStart(2, '0')}:${String(x.getMinutes()).padStart(2, '0')}` : ''
  pickup.value = {
    mode: 'slot',
    slotId: String(s.slotId),
    label: `${fmt(d)} - ${fmt(e).slice(-5)}`
  }
  sheetVisible.value = false
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
  const slotId = pickup.value.mode === 'slot' ? pickup.value.slotId : nearestSlot.value && nearestSlot.value.slotId
  if (!slotId) {
    uni.showToast({ title: '该检测点暂无可预约时段', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/owner/booking/confirm?vehicleId=${vehicle.value.vehicleId}&addressId=${address.value.addressId}`
      + `&stationId=${pickedStation.value.stationId}&slotId=${slotId}`
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
  position: relative;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.station:last-child {
  border-bottom: none;
}

/* 选中态：底色 + 左侧色条，比只换文字颜色更容易一眼看到 */
.station--on {
  background: var(--brand-soft);
  box-shadow: inset 6rpx 0 0 var(--brand);
}

.station__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.station__dist {
  margin-left: 12rpx;
  font-size: 22rpx;
  color: var(--brand);
  white-space: nowrap;
}

.station__badge {
  position: absolute;
  right: 20rpx;
  bottom: 14rpx;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
  background: var(--brand);
  color: #fff;
  font-size: 19rpx;
}

.station__name {
  font-size: 28rpx;
  font-weight: 500;
  letter-spacing: 0.3rpx;
}

.station__addr {
  margin-top: 6rpx;
  padding-right: 96rpx;
  font-size: 23rpx;
  color: var(--ink-3);
}

/* 时间选择弹层 */
.sheet-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 90;
  background: rgba(12, 16, 24, 0.45);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  max-height: 70vh;
  border-radius: 24rpx 24rpx 0 0;
  background: #fff;
  padding-bottom: env(safe-area-inset-bottom);
}

.sheet__head {
  padding: 28rpx 28rpx 16rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.sheet__title {
  font-size: 30rpx;
  font-weight: 600;
}

.sheet__sub {
  margin-left: 12rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}

.sheet__body {
  max-height: 56vh;
  padding: 8rpx 0 24rpx;
}

.sheet__day {
  padding: 18rpx 28rpx 8rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}

.sheet__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 28rpx;
  font-size: 27rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.sheet__item--on {
  color: var(--brand);
  background: var(--brand-soft);
}

.sheet__meta {
  font-size: 21rpx;
  color: var(--ink-3);
}

.sheet__empty {
  padding: 40rpx 28rpx;
  text-align: center;
  font-size: 23rpx;
  color: var(--ink-3);
}
</style>
