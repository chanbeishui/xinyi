<template>
  <view class="page">
    <view v-if="quote" class="slip">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">预约信息</text>
          <view class="slip__plate">{{ vehiclePlate || '—' }}</view>
          <view class="slip__sub ellipsis">{{ quote.vehicleTypeName }}</view>
        </view>
        <view class="status status--gray">待提交</view>
      </view>

      <view class="slip__row">
        <text class="slip__key">检测点</text>
        <text class="slip__val ellipsis">{{ quote.stationName }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">上门时段</text>
        <text class="slip__val num">{{ slotLabel }}</text>
      </view>
      <view class="slip__row slip__row--stack">
        <text class="slip__key">接车地址</text>
        <text class="slip__val slip__val--wrap">{{ addressText }}</text>
      </view>
    </view>

    <view v-if="quote" class="slip">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">费用明细</text>
        </view>
      </view>
      <view class="slip__row">
        <text class="slip__key">检测服务费</text>
        <text class="slip__val num">¥{{ fen2yuan(quote.items.inspectionFeeFen) }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">起步费</text>
        <text class="slip__val num">
          ¥{{ fen2yuan(quote.items.baseFeeFen) }}（含 {{ Math.round((quote.items.includedMeters || 0) / 1000) }} km）
        </text>
      </view>
      <view class="slip__row">
        <text class="slip__key">超出里程</text>
        <text class="slip__val num">{{ ((quote.items.extraMeters || 0) / 1000).toFixed(1) }} km</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">接车距离</text>
        <text class="slip__val num">{{ (quote.distanceMeters / 1000).toFixed(1) }} km</text>
      </view>
      <view class="slip__foot">
        <text class="slip__no">应付金额</text>
        <text class="price"><text class="price__unit">¥</text>{{ fen2yuan(quote.amountFen) }}</text>
      </view>
    </view>

    <view v-if="quote" class="form-hint form-hint--left">报价 15 分钟内有效，超时需重新核价。</view>

    <view v-if="quote" class="card">
      <view class="card-title">联系人</view>
      <view class="field">
        <view class="field-label">联系人姓名</view>
        <input v-model="contactName" class="input" placeholder="接车时联系的姓名" />
      </view>
      <view class="field">
        <view class="field-label">联系电话</view>
        <input v-model="contactPhone" class="input" type="number" placeholder="接车时联系的手机号" maxlength="11" />
      </view>
      <view class="form-hint form-hint--left">联系方式仅向本次服务的代驾在履约期间开放。</view>
    </view>

    <view v-if="error" class="card">
      <view class="card-title">暂时无法预约</view>
      <view class="muted">{{ error }}</view>
      <view class="btn ghost block btn-gap" hover-class="btn-hover" @tap="backToSlots">重新选择时段</view>
    </view>

    <view class="footer-space"></view>

    <view class="action-bar">
      <view class="btn gray" hover-class="btn-hover" @tap="backToSlots">改时段</view>
      <view class="btn btn-wide" :class="{ disabled: !quote || submitting }" hover-class="btn-hover" @tap="submit">
        {{ submitting ? '提交中…' : '提交订单' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import { fen2yuan, windowLabel } from '@/utils/format'
import { session } from '@/store/session'

const quote = ref<any>(null)
const error = ref('')
const submitting = ref(false)
const vehicleId = ref('')
const addressId = ref('')
const stationId = ref('')
const slotId = ref('')
const slotLabel = ref('')
const addressText = ref('')
const vehiclePlate = ref('')
const contactName = ref('')
const contactPhone = ref('')

const canSubmit = computed(() => !!quote.value)

onLoad(async (options) => {
  vehicleId.value = (options && options.vehicleId) || ''
  addressId.value = (options && options.addressId) || ''
  stationId.value = (options && options.stationId) || ''
  slotId.value = (options && options.slotId) || ''
  contactName.value = (session.profile && session.profile.nickname) || ''
  await loadContext()
  await doQuote()
})

async function loadContext() {
  try {
    const [vehicles, addresses, slots] = await Promise.all([
      ownerApi.vehicles(),
      ownerApi.addresses(),
      ownerApi.slots()
    ])
    const v = (vehicles as any[]).find((x) => String(x.vehicleId) === vehicleId.value)
    vehiclePlate.value = v ? v.plateNo : ''
    const a = (addresses as any[]).find((x) => String(x.addressId) === addressId.value)
    addressText.value = a ? a.address : ''
    const s = (slots as any[]).find((x) => String(x.slotId) === slotId.value)
    slotLabel.value = s ? windowLabel(s.startsAt, s.endsAt) : ''
  } catch (e) {
    // 已提示
  }
}

async function doQuote() {
  error.value = ''
  try {
    const view: any = await ownerApi.quote({
      vehicleId: vehicleId.value,
      addressId: addressId.value,
      // 车主手选的检测点：后端据此覆盖按地址自动匹配的结果，并校验车型与服务半径
      stationId: stationId.value || undefined,
      pickupSlotId: slotId.value,
      inspectionTypeCode: 'ANNUAL'
    })
    quote.value = view
    slotLabel.value = slotLabel.value || windowLabel(view.pickupSlotId)
  } catch (e: any) {
    quote.value = null
    error.value = e && e.message ? e.message : '核价失败，请稍后再试'
  }
}

function backToSlots() {
  // 首页现在就能直接选时间和检测点，返回上一页比绕去独立的时段页更顺
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: '/pages/owner/home/index' })
  })
}

async function submit() {
  if (!canSubmit.value || submitting.value) return
  submitting.value = true
  try {
    const order: any = await ownerApi.createOrder({
      quoteId: quote.value.quoteId,
      vehicleId: vehicleId.value,
      addressId: addressId.value,
      contactName: contactName.value,
      contactPhone: contactPhone.value
    })
    uni.redirectTo({ url: `/pages/owner/pay/index?orderNo=${order.orderNo}` })
  } catch (e) {
    // 已提示
  } finally {
    submitting.value = false
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

.form-hint--left {
  text-align: left;
  margin: 4rpx 6rpx 18rpx;
}

.btn-gap {
  margin-top: 18rpx;
}

.btn-wide {
  flex: 2;
}
</style>
