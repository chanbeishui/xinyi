<template>
  <view class="page">
    <view class="card">
      <view class="card-title">支付订单</view>
      <view class="row-between">
        <text class="label">订单号</text>
        <text class="value">{{ orderNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">支付状态</text>
        <text class="tag" :class="statusClass">{{ payment.statusLabel || '待支付' }}</text>
      </view>
      <view class="row-between">
        <text class="label">应付金额</text>
        <text class="price">¥{{ fen2yuan(amountFen) }}</text>
      </view>
      <view v-if="prepayInfo.expiresAt" class="row-between">
        <text class="label">支付截止</text>
        <text class="value">{{ dateTime(prepayInfo.expiresAt) }}</text>
      </view>
      <view v-if="prepayInfo.hint" class="muted" style="margin-top: 12rpx">{{ prepayInfo.hint }}</view>
    </view>

    <view class="card">
      <view class="card-title">支付方式</view>
      <view class="row-between">
        <view>
          <view class="value" style="text-align: left">微信支付</view>
          <view class="muted">{{ prepayInfo.channel === 'MOCK' ? '联调通道（模拟支付）' : '微信支付' }}</view>
        </view>
        <view class="tag" :class="paid ? 'ok' : ''">{{ paid ? '已支付' : '待支付' }}</view>
      </view>
    </view>

    <view v-if="error" class="card">
      <view class="muted">{{ error }}</view>
    </view>

    <view class="action-bar">
      <view class="btn gray" style="flex: 1" @tap="goDetail">查看订单</view>
      <view v-if="!paid" class="btn" :class="{ disabled: paying }" style="flex: 2" @tap="pay">
        {{ paying ? '支付中…' : '立即支付' }}
      </view>
      <view v-else class="btn" style="flex: 2" @tap="goDetail">支付完成，查看订单</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import { dateTime, fen2yuan } from '@/utils/format'

const orderNo = ref('')
const order = ref<any>(null)
const payment = ref<any>({})
const prepayInfo = ref<any>({})
const amountFen = ref(0)
const paying = ref(false)
const error = ref('')
let timer: any = null

const paid = computed(() => payment.value && ['PAID', 'PARTIAL_REFUNDED', 'REFUNDED'].indexOf(payment.value.status) >= 0)
const statusClass = computed(() => (paid.value ? 'ok' : 'warn'))

onLoad(async (options) => {
  orderNo.value = (options && options.orderNo) || ''
  await load()
  timer = setInterval(checkPaid, 4000)
})

onUnload(() => {
  if (timer) clearInterval(timer)
})

async function load() {
  try {
    order.value = await ownerApi.order(orderNo.value)
    amountFen.value = Number(order.value.totalFen || 0)
    payment.value = await ownerApi.payment(orderNo.value)
    if (!paid.value) {
      prepayInfo.value = await ownerApi.prepay(orderNo.value)
    }
  } catch (e: any) {
    error.value = e && e.message ? e.message : '获取支付信息失败'
  }
}

async function pay() {
  if (paying.value || paid.value) return
  paying.value = true
  error.value = ''
  try {
    const info: any = prepayInfo.value && prepayInfo.value.channel ? prepayInfo.value : await ownerApi.prepay(orderNo.value)
    prepayInfo.value = info
    if (info.mock) {
      await ownerApi.mockPay(orderNo.value)
      await refreshPayment()
      uni.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(goDetail, 800)
    } else {
      // #ifdef MP-WEIXIN
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: info.timeStamp,
        nonceStr: info.nonceStr,
        package: info.package,
        signType: info.signType,
        paySign: info.paySign,
        success: () => refreshPayment(),
        fail: () => {
          error.value = '支付未完成，可稍后在订单详情继续支付'
        }
      })
      // #endif
      // #ifndef MP-WEIXIN
      error.value = '当前环境不支持微信支付，请在微信小程序内完成支付'
      // #endif
    }
  } catch (e: any) {
    error.value = e && e.message ? e.message : '支付失败'
  } finally {
    paying.value = false
  }
}

async function refreshPayment() {
  try {
    payment.value = await ownerApi.payment(orderNo.value)
  } catch (e) {
    // 忽略
  }
}

function checkPaid() {
  if (!orderNo.value || paid.value) return
  refreshPayment()
}

function goDetail() {
  uni.redirectTo({ url: `/pages/owner/order/detail?orderNo=${orderNo.value}` })
}
</script>

<style scoped>
.action-bar .btn {
  flex: 1;
}
</style>
