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
        <text class="tag" :class="statusClass">{{ statusLabel }}</text>
      </view>
      <view class="row-between">
        <text class="label">应付金额</text>
        <text class="price">¥{{ fen2yuan(amountFen) }}</text>
      </view>
      <view v-if="payment.expiresAt" class="row-between">
        <text class="label">支付截止</text>
        <text class="value">{{ dateTime(payment.expiresAt) }}</text>
      </view>
      <view v-if="optionsNote" class="muted" style="margin-top: 12rpx">{{ optionsNote }}</view>
    </view>

    <view class="card">
      <view class="card-title">支付方式</view>

      <view v-if="!paid && options.length">
        <view v-for="opt in options" :key="opt.method" class="method"
              :class="{ active: selectedMethod === opt.method, disabled: !opt.available && selectedMethod !== opt.method }"
              @tap="selectMethod(opt)">
          <view class="method-main">
            <view class="value" style="text-align: left">{{ opt.label }}</view>
            <view class="muted">{{ methodHint(opt) }}</view>
          </view>
          <view class="tag" :class="methodTagClass(opt)">
            {{ methodTagText(opt) }}
          </view>
        </view>
      </view>

      <view v-else-if="paid" class="row-between">
        <view>
          <view class="value" style="text-align: left">{{ lockedMethodLabel }}</view>
          <view class="muted">{{ paidHint }}</view>
        </view>
        <view class="tag ok">已支付</view>
      </view>

      <view v-else class="muted">当前未开放任何支付方式，请联系门店确认后再支付。</view>
    </view>

    <!-- 线下付款说明：只有选中或已锁定线下时才展示，避免误导为“可以不付” -->
    <view v-if="showOfflineInfo" class="card">
      <view class="card-title">线下付款说明</view>
      <view v-if="offline.instructions" class="offline-text">{{ offline.instructions }}</view>
      <view v-if="offline.receiverName" class="row-between">
        <text class="label">收款单位</text>
        <text class="value">{{ offline.receiverName }}</text>
      </view>
      <view v-if="offline.bankName" class="row-between">
        <text class="label">开户行</text>
        <text class="value">{{ offline.bankName }}</text>
      </view>
      <view v-if="offline.bankAccountName" class="row-between">
        <text class="label">户名</text>
        <text class="value">{{ offline.bankAccountName }}</text>
      </view>
      <view v-if="offline.receiverAccountDisplay" class="row-between">
        <text class="label">收款账号</text>
        <text class="value">{{ offline.receiverAccountDisplay }}</text>
      </view>
      <view v-if="offlineContact" class="row-between">
        <text class="label">联系电话</text>
        <text class="value">{{ offlineContact }}</text>
      </view>
      <view class="muted" style="margin-top: 16rpx">
        选择线下付款后订单仍为待支付：需要门店登记真实收款，并由另一位有权人员核实全额到账后，才会进入派单。
      </view>
    </view>

    <view v-if="error" class="card">
      <view class="muted">{{ error }}</view>
    </view>

    <view class="action-bar">
      <view class="btn gray" style="flex: 1" @tap="goDetail">查看订单</view>
      <view v-if="paid" class="btn" style="flex: 2" @tap="goDetail">支付完成，查看订单</view>
      <view v-else-if="selectedMethod === 'OFFLINE'" class="btn" :class="{ disabled: busy }" style="flex: 2"
            @tap="confirmOffline">
        {{ busy ? '提交中…' : (lockedMethod === 'OFFLINE' ? '已选择线下付款' : '确认选择线下付款') }}
      </view>
      <view v-else class="btn" :class="{ disabled: busy || !wechatOption || !wechatOption.available }" style="flex: 2"
            @tap="pay">
        {{ busy ? '支付中…' : '立即支付' }}
      </view>
    </view>

    <!-- 订阅提醒入口：手动触发，失败不影响任何业务 -->
    <view v-if="orderId && paid" class="card">
      <view class="card-title">检测进度提醒</view>
      <view class="muted">订阅后可在微信收到取车、检测完成等提醒；不订阅也能正常查看订单，站内消息照常送达。</view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="subscribe">
        {{ subscribed ? '已提交订阅' : '开启微信进度提醒' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { commonApi, ownerApi } from '@/api'
import { dateTime, fen2yuan } from '@/utils/format'
import { requestSubscribe } from '@/utils/subscribe'

const orderNo = ref('')
const orderId = ref('')
const order = ref<any>(null)
const payment = ref<any>({})
const prepayInfo = ref<any>({})
const amountFen = ref(0)
const busy = ref(false)
const error = ref('')
const subscribed = ref(false)
let timer: any = null

/* 支付方式：完全由服务端 payment-options 决定，前端不自行判断开关 */
const options = ref<any[]>([])
const optionsNote = ref('')
const lockedMethod = ref('UNSELECTED')
const paymentVersion = ref(0)
const policyRevisionId = ref('')
const selectedMethod = ref('')

const PAID_STATUS = ['PAID', 'PARTIAL_REFUNDED', 'REFUNDED']

const paid = computed(() => payment.value && PAID_STATUS.indexOf(payment.value.status) >= 0)
const statusClass = computed(() => (paid.value ? 'ok' : 'warn'))
const statusLabel = computed(() => (payment.value && payment.value.statusLabel) || '待支付')

const wechatOption = computed(() => options.value.find((o) => o.method === 'WECHAT'))
const offlineOption = computed(() => options.value.find((o) => o.method === 'OFFLINE'))
const offline = computed(() => offlineOption.value || {})
const offlineContact = computed(() => {
  const o = offline.value
  const name = o.contactName || ''
  const phone = o.contactPhone || ''
  if (!name && !phone) return ''
  return name && phone ? `${name} ${phone}` : name || phone
})
const showOfflineInfo = computed(() => !paid.value && selectedMethod.value === 'OFFLINE')
const lockedMethodLabel = computed(() => {
  if (lockedMethod.value === 'OFFLINE') return '线下付款'
  if (lockedMethod.value === 'WECHAT') return '微信支付'
  return '已支付'
})
const paidHint = computed(() => {
  if (lockedMethod.value === 'OFFLINE') return '线下收款已核实到账'
  if (lockedMethod.value === 'WECHAT') return '微信支付成功'
  return '以服务器记录为准'
})

const REASON_TEXT: Record<string, string> = {
  POLICY_DISABLED: '当前未开启',
  METHOD_LOCKED: '已选择其它方式',
  ORDER_EXPIRED: '支付已超时',
  ALREADY_PAID: '已完成支付',
  MERCHANT_NOT_CONFIGURED: '商户尚未配置完成'
}

function methodHint(opt: any) {
  if (!opt.available) return REASON_TEXT[opt.reasonCode] || '当前不可用'
  if (opt.method === 'OFFLINE') return '到店现金或银行转账，需门店登记核实'
  return '微信安全支付'
}

function methodTagClass(opt: any) {
  if (!opt.available && selectedMethod.value !== opt.method) return ''
  return opt.method === 'OFFLINE' ? 'warn' : ''
}

function methodTagText(opt: any) {
  if (selectedMethod.value === opt.method) return '已选择'
  return opt.available ? '可选' : '不可用'
}

onLoad(async (opts) => {
  orderNo.value = (opts && opts.orderNo) || ''
  await load()
  timer = setInterval(checkPaid, 4000)
})

onUnload(() => {
  if (timer) clearInterval(timer)
})

async function load() {
  try {
    order.value = await ownerApi.order(orderNo.value)
    orderId.value = order.value.orderId || ''
    amountFen.value = Number(order.value.totalFen || 0)
    payment.value = await ownerApi.payment(orderNo.value)
    if (!paid.value) {
      await loadOptions()
    }
  } catch (e: any) {
    error.value = e && e.message ? e.message : '获取支付信息失败'
  }
}

async function loadOptions() {
  try {
    const res: any = await ownerApi.paymentOptions(orderNo.value)
    options.value = res.options || []
    lockedMethod.value = res.lockedMethod || 'UNSELECTED'
    paymentVersion.value = Number(res.paymentVersion || 0)
    policyRevisionId.value = res.policyRevisionId || ''
    optionsNote.value = res.note || ''
    if (lockedMethod.value === 'OFFLINE' || lockedMethod.value === 'WECHAT') {
      selectedMethod.value = lockedMethod.value
    } else {
      // 唯一可用时直接选中，减少一次点击；两个都不可用则清空
      const usable = options.value.filter((o) => o.available)
      selectedMethod.value = usable.length === 1 ? usable[0].method : ''
    }
  } catch (e: any) {
    options.value = []
    optionsNote.value = (e && e.message) || '获取支付方式失败'
  }
}

function selectMethod(opt: any) {
  if (lockedMethod.value === 'OFFLINE' || lockedMethod.value === 'WECHAT') return
  if (!opt.available) {
    uni.showToast({ title: REASON_TEXT[opt.reasonCode] || '当前不可用', icon: 'none' })
    return
  }
  selectedMethod.value = opt.method
}

async function confirmOffline() {
  if (busy.value) return
  if (lockedMethod.value === 'OFFLINE') {
    goDetail()
    return
  }
  busy.value = true
  error.value = ''
  try {
    const res: any = await ownerApi.chooseOfflinePayment(orderNo.value, {
      expectedVersion: paymentVersion.value,
      expectedPolicyRevisionId: policyRevisionId.value || undefined
    })
    lockedMethod.value = 'OFFLINE'
    paymentVersion.value = Number(res.paymentVersion || paymentVersion.value + 1)
    uni.showModal({
      title: '已记录线下付款意向',
      content: res.note || '订单仍为待支付，门店登记并核实到账后才会进入派单。',
      showCancel: false
    })
  } catch (e: any) {
    error.value = e && e.message ? e.message : '提交失败'
    await loadOptions()
  } finally {
    busy.value = false
  }
}

async function pay() {
  if (busy.value || paid.value) return
  if (selectedMethod.value === 'OFFLINE') {
    await confirmOffline()
    return
  }
  busy.value = true
  error.value = ''
  try {
    const info: any = prepayInfo.value && prepayInfo.value.channel
      ? prepayInfo.value
      : await ownerApi.prepay(orderNo.value)
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
        success: () => {
          refreshPayment()
          subscribe()
        },
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
    // 预支付可能因“已锁定线下”或配置变更被拒绝，重新拉一次权威状态
    await loadOptions()
  } finally {
    busy.value = false
  }
}

async function subscribe() {
  if (!orderId.value || subscribed.value) return
  const res = await requestSubscribe(commonApi, {
    businessObjectType: 'ORDER',
    businessObjectId: orderId.value,
    scenes: ['PICKUP_ARRIVED', 'INSPECTION_FINISHED', 'RETURN_COMPLETED']
  })
  if (res.outcome === 'reported') {
    subscribed.value = true
    uni.showToast({ title: '已开启进度提醒', icon: 'success' })
  } else if (res.outcome === 'skipped') {
    uni.showToast({ title: res.note || '暂无可订阅的提醒', icon: 'none' })
  } else {
    uni.showToast({ title: res.note || '授权未完成', icon: 'none' })
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
.method {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  border: 2rpx solid transparent;
  border-radius: 12rpx;
}
.method.active {
  border-color: #2f6bff;
  background: rgba(47, 107, 255, 0.06);
}
.method.disabled {
  opacity: 0.45;
}
.method-main {
  flex: 1;
}
.offline-text {
  font-size: 26rpx;
  line-height: 1.7;
  margin-bottom: 16rpx;
}
.btn.ghost {
  background: #fff;
  color: #2f6bff;
  border: 2rpx solid #2f6bff;
  border-radius: 12rpx;
  text-align: center;
  padding: 20rpx 0;
}
</style>
