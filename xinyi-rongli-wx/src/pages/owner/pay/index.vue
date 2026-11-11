<template>
  <view class="page">
    <!-- 金额抬头：这一页唯一的主角是「还要付多少」 -->
    <view class="ink-panel pay-head">
      <view class="ink-panel__rule"></view>
      <view class="pay-head__top">
        <text class="ink-panel__eyebrow">应付金额</text>
        <text class="status pay-head__status" :class="paid ? 'status--ok' : 'status--warn'">
          {{ statusLabel }}
        </text>
      </view>
      <view class="pay-head__amount num">
        <text class="pay-head__unit">¥</text>{{ fen2yuan(amountFen) }}
      </view>
      <view class="pay-head__meta num">订单号 {{ orderNo }}</view>
      <view v-if="payment.expiresAt" class="pay-head__expire num">
        支付截止 {{ dateTime(payment.expiresAt) }}
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">支付方式</text>
      <text v-if="optionsNote" class="section-head__meta ellipsis">{{ optionsNote }}</text>
    </view>

    <view v-if="!paid && options.length" class="list">
      <view
        v-for="opt in options"
        :key="opt.method"
        class="method"
        :class="{
          'method--on': selectedMethod === opt.method,
          'method--off': !opt.available && selectedMethod !== opt.method
        }"
        hover-class="card-hover"
        @tap="selectMethod(opt)"
      >
        <view class="method__radio" :class="{ 'method__radio--on': selectedMethod === opt.method }">
          <view class="method__dot"></view>
        </view>
        <view class="method__main">
          <view class="method__label">{{ opt.label }}</view>
          <view class="method__hint">{{ methodHint(opt) }}</view>
        </view>
        <text class="status" :class="methodTagClass(opt)">{{ methodTagText(opt) }}</text>
      </view>
    </view>

    <view v-else-if="paid" class="list">
      <view class="method">
        <view class="method__main">
          <view class="method__label">{{ lockedMethodLabel }}</view>
          <view class="method__hint">{{ paidHint }}</view>
        </view>
        <text class="status status--ok">已支付</text>
      </view>
    </view>

    <view v-else class="card">
      <view class="muted">当前未开放任何支付方式，请联系门店确认后再支付。</view>
    </view>

    <!-- 线下付款说明：只有选中或已锁定线下时才展示，避免误导为“可以不付” -->
    <view v-if="showOfflineInfo" class="card">
      <view class="card-title">线下付款说明</view>
      <view v-if="offline.instructions" class="offline-text">{{ offline.instructions }}</view>
      <view v-if="offline.receiverName" class="slip__row slip__row--loose">
        <text class="slip__key">收款单位</text>
        <text class="slip__val">{{ offline.receiverName }}</text>
      </view>
      <view v-if="offline.bankName" class="slip__row slip__row--loose">
        <text class="slip__key">开户行</text>
        <text class="slip__val">{{ offline.bankName }}</text>
      </view>
      <view v-if="offline.bankAccountName" class="slip__row slip__row--loose">
        <text class="slip__key">户名</text>
        <text class="slip__val">{{ offline.bankAccountName }}</text>
      </view>
      <view v-if="offline.receiverAccountDisplay" class="slip__row slip__row--loose">
        <text class="slip__key">收款账号</text>
        <text class="slip__val num">{{ offline.receiverAccountDisplay }}</text>
      </view>
      <view v-if="offlineContact" class="slip__row slip__row--loose">
        <text class="slip__key">联系电话</text>
        <text class="slip__val num">{{ offlineContact }}</text>
      </view>
      <view class="muted offline-note">
        选择线下付款后订单仍为待支付：需要门店登记真实收款，并由另一位有权人员核实全额到账后，才会进入派单。
      </view>
    </view>

    <view v-if="error" class="card card--error">
      <view class="muted">{{ error }}</view>
    </view>

    <!-- 订阅提醒入口：手动触发，失败不影响任何业务 -->
    <view v-if="orderId && paid" class="card">
      <view class="card-title">检测进度提醒</view>
      <view class="muted">订阅后可在微信收到取车、检测完成等提醒；不订阅也能正常查看订单，站内消息照常送达。</view>
      <view class="btn ghost block btn-gap" hover-class="btn-hover" @tap="subscribe">
        {{ subscribed ? '已提交订阅' : '开启微信进度提醒' }}
      </view>
    </view>

    <view class="footer-space"></view>

    <view class="action-bar">
      <view class="btn gray" hover-class="btn-hover" @tap="goDetail">查看订单</view>
      <view v-if="paid" class="btn btn-wide" hover-class="btn-hover" @tap="goDetail">支付完成，查看订单</view>
      <view
        v-else-if="selectedMethod === 'OFFLINE'"
        class="btn btn-wide"
        :class="{ disabled: busy }"
        hover-class="btn-hover"
        @tap="confirmOffline"
      >
        {{ busy ? '提交中…' : lockedMethod === 'OFFLINE' ? '已选择线下付款' : '确认选择线下付款' }}
      </view>
      <view
        v-else
        class="btn btn-wide"
        :class="{ disabled: busy || !wechatOption || !wechatOption.available }"
        hover-class="btn-hover"
        @tap="pay"
      >
        {{ busy ? '支付中…' : '立即支付' }}
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
  if (selectedMethod.value === opt.method) return 'status--brand'
  if (!opt.available) return 'status--gray'
  return opt.method === 'OFFLINE' ? 'status--warn' : 'status--brand'
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
.pay-head__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 墨底上的状态章要反白描边，否则浅色底与墨色融在一起 */
.pay-head__status {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.35);
  color: #fff;
}

.pay-head__amount {
  display: flex;
  align-items: baseline;
  margin-top: 18rpx;
  font-size: 76rpx;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: 1rpx;
}

.pay-head__unit {
  margin-right: 6rpx;
  font-size: 34rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
}

.pay-head__meta {
  margin-top: 14rpx;
  font-size: 22rpx;
  letter-spacing: 0.8rpx;
  color: rgba(255, 255, 255, 0.55);
}

.pay-head__expire {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #f0b56a;
}

/* 支付方式：单选行，选中用墨点 + 淡蓝底，不用整块高亮 */
.method {
  display: flex;
  align-items: center;
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.method:last-child {
  border-bottom: none;
}

.method--on {
  background: var(--brand-soft);
}

.method--off {
  opacity: 0.5;
}

.method__radio {
  flex: 0 0 auto;
  width: 34rpx;
  height: 34rpx;
  margin-right: 18rpx;
  border: 1rpx solid #c3ccd8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.method__radio--on {
  border-color: var(--brand);
}

.method__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: transparent;
}

.method__radio--on .method__dot {
  background: var(--brand);
}

.method__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.method__label {
  font-size: 28rpx;
  font-weight: 500;
}

.method__hint {
  margin-top: 6rpx;
  font-size: 22rpx;
  line-height: 1.5;
  color: var(--ink-3);
}

.offline-text {
  font-size: 26rpx;
  line-height: 1.7;
  color: var(--ink-2);
  margin-bottom: 8rpx;
}

.offline-note {
  margin-top: 14rpx;
}

/* 借用 slip 行的排版，但卡片本身是 .card */
.slip__row--loose {
  padding: 12rpx 0;
  border-top: 1rpx solid var(--line-2);
}

.card--error {
  border-color: #f3c9c4;
  background: var(--danger-soft);
}

.btn-gap {
  margin-top: 18rpx;
}

.btn-wide {
  flex: 2;
}
</style>
