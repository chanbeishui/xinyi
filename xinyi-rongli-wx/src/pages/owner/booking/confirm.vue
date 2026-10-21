<template>
  <view class="page">
    <view v-if="quote" class="card">
      <view class="card-title">预约信息</view>
      <view class="row-between">
        <text class="label">检测点</text>
        <text class="value">{{ quote.stationName }}</text>
      </view>
      <view class="row-between">
        <text class="label">车辆</text>
        <text class="value">{{ vehiclePlate }}</text>
      </view>
      <view class="row-between">
        <text class="label">车型</text>
        <text class="value">{{ quote.vehicleTypeName }}</text>
      </view>
      <view class="row-between">
        <text class="label">上门时段</text>
        <text class="value">{{ slotLabel }}</text>
      </view>
      <view class="row-between">
        <text class="label">接车地址</text>
        <text class="value" style="max-width: 60%">{{ addressText }}</text>
      </view>
    </view>

    <view v-if="quote" class="card">
      <view class="card-title">费用明细</view>
      <view class="row-between">
        <text class="label">检测服务费</text>
        <text class="value">¥{{ fen2yuan(quote.items.inspectionFeeFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">起步费（含 {{ Math.round((quote.items.includedMeters || 0) / 1000) }} km）</text>
        <text class="value">¥{{ fen2yuan(quote.items.baseFeeFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">超出里程 {{ ((quote.items.extraMeters || 0) / 1000).toFixed(1) }} km</text>
        <text class="value">计算在内</text>
      </view>
      <view class="row-between">
        <text class="label">接车距离</text>
        <text class="value">{{ (quote.distanceMeters / 1000).toFixed(1) }} km</text>
      </view>
      <view class="divider"></view>
      <view class="row-between">
        <text class="label">应付金额</text>
        <text class="price">¥{{ fen2yuan(quote.amountFen) }}</text>
      </view>
      <view class="muted" style="margin-top: 10rpx">报价 15 分钟内有效，超时需重新核价。</view>
    </view>

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
      <view class="muted" style="margin-top: 10rpx">联系方式仅向本次服务的代驾在履约期间开放。</view>
    </view>

    <view v-if="error" class="card">
      <view class="card-title">暂时无法预约</view>
      <view class="muted">{{ error }}</view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="backToSlots">重新选择时段</view>
    </view>

    <view class="action-bar">
      <view class="btn gray" style="flex: 1" @tap="backToSlots">改时段</view>
      <view class="btn" :class="{ disabled: !quote || submitting }" style="flex: 2" @tap="submit">
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
  uni.redirectTo({
    url: `/pages/owner/booking/slot?vehicleId=${vehicleId.value}&addressId=${addressId.value}`
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
.action-bar .btn {
  flex: 1;
}
</style>
