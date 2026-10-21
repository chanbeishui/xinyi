<template>
  <view class="page">
    <view v-if="order" class="hero">
      <view class="hero-status">{{ order.fulfillmentStatusLabel }}</view>
      <view class="hero-sub">{{ order.plateNo }} · {{ order.vehicleTypeName }}</view>
      <view class="hero-hint">{{ hint }}</view>
    </view>

    <view v-if="order" class="card">
      <view class="card-title">预约信息</view>
      <view class="row-between">
        <text class="label">订单号</text>
        <text class="value">{{ order.orderNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">检测点</text>
        <text class="value">{{ order.stationName }}</text>
      </view>
      <view class="row-between">
        <text class="label">上门时段</text>
        <text class="value">{{ windowLabel(order.pickupStartsAt, order.pickupEndsAt) }}</text>
      </view>
      <view class="row-between">
        <text class="label">接车地址</text>
        <text class="value" style="max-width: 62%">{{ order.address || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">金额</text>
        <text class="price">¥{{ order.totalYuan }}</text>
      </view>
      <view v-if="order.payExpiresAt && order.fulfillmentStatus === 'PENDING_PAY'" class="row-between">
        <text class="label">支付截止</text>
        <text class="value">{{ dateTime(order.payExpiresAt) }}</text>
      </view>
    </view>

    <!-- 交接 -->
    <view v-if="handover" class="card">
      <view class="card-title">{{ handover.handoverTypeLabel }}</view>
      <view class="row-between">
        <text class="label">状态</text>
        <text class="tag" :class="handover.status === 'CONFIRMED' ? 'ok' : 'warn'">{{ handover.statusLabel }}</text>
      </view>
      <view v-if="handover.expiresAt && handover.status === 'PENDING'" class="row-between">
        <text class="label">有效至</text>
        <text class="value">{{ dateTime(handover.expiresAt) }}</text>
      </view>
      <view v-if="handover.actionHint" class="muted" style="margin-top: 12rpx">{{ handover.actionHint }}</view>

      <view v-if="code" class="code-box">
        <view class="code">{{ code.code }}</view>
        <view class="muted">{{ code.displayHint }}</view>
      </view>

      <view class="row" style="gap: 16rpx; margin-top: 20rpx">
        <view v-if="handover.canViewCode" class="btn ghost small" @tap="loadCode">查看交接码</view>
        <view v-if="handover.canConfirm" class="btn small" @tap="openConfirm">核验交接码</view>
      </view>
    </view>

    <!-- 检测结果 -->
    <view v-if="result && result.available" class="card">
      <view class="card-title">检测结果</view>
      <view class="row-between">
        <text class="label">结论</text>
        <text class="tag" :class="result.conclusion === 'PASS' ? 'ok' : 'danger'">{{ result.conclusionLabel || result.conclusion }}</text>
      </view>
      <view v-for="(item, idx) in resultItems" :key="idx" class="row-between">
        <text class="label">{{ item.label }}</text>
        <text class="value">{{ item.value }}</text>
      </view>
      <view v-if="result.inspectedAt" class="row-between">
        <text class="label">检测时间</text>
        <text class="value">{{ dateTime(result.inspectedAt) }}</text>
      </view>
    </view>

    <!-- 时间线 -->
    <view v-if="timeline.length" class="card">
      <view class="card-title">服务进度</view>
      <view v-for="(t, i) in timeline" :key="i" class="timeline-item">
        <view class="dot" :class="{ first: i === 0 }"></view>
        <view class="tl-body">
          <view class="tl-action">{{ t.label || t.action }}</view>
          <view class="muted">{{ dateTime(t.at) }}<text v-if="t.reason"> · {{ t.reason }}</text></view>
        </view>
      </view>
    </view>

    <!-- 留证 -->
    <view v-if="evidenceGroups.length" class="card">
      <view class="card-title">车况留证</view>
      <view v-for="g in evidenceGroups" :key="g.groupId" class="ev-group">
        <view class="row-between">
          <text class="value" style="text-align: left">{{ g.nodeLabel || g.nodeName || '留证' }}</text>
          <text class="muted">{{ (g.photos || []).length }} 张</text>
        </view>
        <view v-if="g.mileageKm" class="muted">里程 {{ g.mileageKm }} km</view>
      </view>
    </view>

    <!-- 操作 -->
    <view class="action-bar">
      <view v-if="has('APPLY_AFTER_SALE')" class="btn ghost" style="flex: 1" @tap="openAfterSale">取消与退款</view>
      <view v-if="has('OPEN_DISPUTE')" class="btn ghost" style="flex: 1" @tap="openDispute">发起争议</view>
      <view v-if="has('CONFIRM_SERVICE')" class="btn" style="flex: 1" @tap="confirmService">确认服务</view>
      <view v-if="has('PAY')" class="btn" style="flex: 1" @tap="goPay">去支付</view>
      <view v-if="has('REVIEW')" class="btn" style="flex: 1" @tap="openReview">评价</view>
    </view>

    <!-- 交接码核验 -->
    <view v-if="confirmForm.show" class="mask" @tap="confirmForm.show = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">核验交接码</view>
        <view class="muted">请输入对方出示的 4 位交接码，连续 3 次错误将锁定本阶段交接。</view>
        <input v-model="confirmForm.code" class="input" type="number" maxlength="4" placeholder="4 位数字" style="margin-top: 20rpx" />
        <view class="btn" style="margin-top: 24rpx" @tap="submitConfirm">确认交接</view>
      </view>
    </view>

    <!-- 取消与退款 -->
    <view v-if="afterSaleForm.show" class="mask" @tap="afterSaleForm.show = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">申请取消与退款</view>
        <view class="muted">提交后由平台审核，审核结论将决定是否取消、是否需要实还车辆与批准退款金额。</view>
        <view class="field" style="margin-top: 16rpx">
          <view class="field-label">申请类型</view>
          <picker mode="selector" :range="kindLabels" :value="afterSaleForm.kindIndex" @change="onKindChange">
            <view class="input picker">{{ kindLabels[afterSaleForm.kindIndex] }}</view>
          </picker>
        </view>
        <view class="field">
          <view class="field-label">申请金额（元）</view>
          <input v-model="afterSaleForm.amount" class="input" type="digit" placeholder="默认按已支付金额" />
        </view>
        <view class="field">
          <view class="field-label">申请原因</view>
          <textarea v-model="afterSaleForm.reason" class="textarea" placeholder="请说明取消或退款原因" />
        </view>
        <view class="btn" style="margin-top: 24rpx" @tap="submitAfterSale">提交申请</view>
      </view>
    </view>

    <!-- 争议 -->
    <view v-if="disputeForm.show" class="mask" @tap="disputeForm.show = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">发起服务争议</view>
        <view class="field" style="margin-top: 12rpx">
          <view class="field-label">争议类型</view>
          <picker mode="selector" :range="disputeTypes" :value="disputeForm.typeIndex" @change="onDisputeType">
            <view class="input picker">{{ disputeTypes[disputeForm.typeIndex] }}</view>
          </picker>
        </view>
        <view class="field">
          <view class="field-label">争议说明</view>
          <textarea v-model="disputeForm.description" class="textarea" placeholder="请描述问题与诉求" />
        </view>
        <view class="btn" style="margin-top: 24rpx" @tap="submitDispute">提交争议</view>
      </view>
    </view>

    <!-- 评价 -->
    <view v-if="reviewForm.show" class="mask" @tap="reviewForm.show = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">服务评价</view>
        <view class="row" style="gap: 12rpx; margin-top: 16rpx">
          <view
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ on: n <= reviewForm.score }"
            @tap="reviewForm.score = n"
          >
            {{ n }}
          </view>
        </view>
        <view class="field" style="margin-top: 12rpx">
          <view class="field-label">评价内容</view>
          <textarea v-model="reviewForm.content" class="textarea" placeholder="服务体验如何？" />
        </view>
        <view class="btn" style="margin-top: 24rpx" @tap="submitReview">提交评价</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { handoverApi, ownerApi } from '@/api'
import { dateTime, fen2yuan, windowLabel } from '@/utils/format'

const orderNo = ref('')
const order = ref<any>(null)
const timeline = ref<any[]>([])
const handover = ref<any>(null)
const code = ref<any>(null)
const result = ref<any>(null)
const evidenceGroups = ref<any[]>([])

const confirmForm = reactive<any>({ show: false, code: '' })
const afterSaleForm = reactive<any>({ show: false, kindIndex: 0, amount: '', reason: '' })
const disputeForm = reactive<any>({ show: false, typeIndex: 0, description: '' })
const reviewForm = reactive<any>({ show: false, score: 5, content: '' })

const kindLabels = ['取消并退款', '仅退款']
const kinds = ['CANCEL_REFUND', 'REFUND_ONLY']
const disputeTypes = ['车况争议', '结果异议', '费用争议', '服务态度']
const disputeTypeCodes = ['VEHICLE', 'RESULT', 'FEE', 'SERVICE']

const hint = computed(() => {
  const s = order.value && order.value.fulfillmentStatus
  const map: Record<string, string> = {
    PENDING_PAY: '请尽快完成支付，超时订单将自动关闭',
    SCHEDULED: '预约已确认，系统将在上门前安排代驾',
    PENDING_DISPATCH: '正在安排代驾，请保持电话畅通',
    DISPATCHING: '正在邀约代驾接单',
    MANUAL_DISPATCH: '已转人工安排，稍后会有客服与您联系',
    ACCEPTED: '代驾已接单，将按约定时间上门',
    GOING_PICKUP: '代驾正在前往接车地点',
    ARRIVED_PICKUP: '代驾已到达，请出示交接码完成交车',
    PICKED_UP: '车辆已交接，代驾正在送往检测点',
    ARRIVED_STATION: '车辆已到达检测点',
    INSPECTING: '检测进行中',
    INSPECTED: '检测完成，等待取车与返还',
    CAR_TAKEN: '车辆已取回，正在返还给您',
    WAIT_CONFIRM: '车辆已归还，请确认服务完成',
    COMPLETED: '服务已完成，感谢使用',
    CANCELLED: '订单已取消',
    CLOSED: '订单未支付已关闭'
  }
  return (s && map[s]) || ''
})

const resultItems = computed(() => {
  const items = (result.value && result.value.items) || null
  if (!items) return []
  if (Array.isArray(items)) {
    return items.map((it: any) => ({
      label: it.name || it.itemName || it.item || '检测项',
      value: it.result || it.value || it.conclusion || '-'
    }))
  }
  return Object.keys(items).map((k) => ({ label: k, value: String(items[k]) }))
})

onLoad((options) => {
  orderNo.value = (options && options.orderNo) || ''
  load()
})

onPullDownRefresh(() => {
  load().finally(() => uni.stopPullDownRefresh())
})

async function load() {
  try {
    order.value = await ownerApi.order(orderNo.value)
    const tl: any = await ownerApi.timeline(orderNo.value)
    timeline.value = (tl && tl.nodes) || []
    await loadHandover()
    await loadResult()
    await loadEvidence()
  } catch (e) {
    // 已提示
  }
}

function has(action: string) {
  const list = (order.value && order.value.allowedActions) || []
  return list.indexOf(action) >= 0
}

async function loadHandover() {
  const type = nextHandoverType(order.value && order.value.fulfillmentStatus)
  if (!type) {
    handover.value = null
    return
  }
  try {
    const view: any = await handoverApi.create(orderNo.value, type)
    handover.value = view
  } catch (e) {
    handover.value = null
  }
}

function nextHandoverType(status: string): string | null {
  switch (status) {
    case 'ARRIVED_PICKUP':
      return 'OWNER_TO_DRIVER'
    case 'CAR_TAKEN':
      return 'DRIVER_TO_OWNER'
    default:
      return null
  }
}

async function loadResult() {
  try {
    result.value = await ownerApi.result(orderNo.value)
  } catch (e) {
    result.value = null
  }
}

async function loadEvidence() {
  try {
    const view: any = await ownerApi.evidence(orderNo.value)
    evidenceGroups.value = (view && view.groups) || []
  } catch (e) {
    evidenceGroups.value = []
  }
}

async function loadCode() {
  if (!handover.value) return
  try {
    code.value = await handoverApi.secret(orderNo.value, handover.value.handoverId)
  } catch (e) {
    // 已提示
  }
}

function openConfirm() {
  confirmForm.code = ''
  confirmForm.show = true
}

async function submitConfirm() {
  if (!/\d{4}/.test(confirmForm.code)) {
    uni.showToast({ title: '请输入 4 位交接码', icon: 'none' })
    return
  }
  try {
    await handoverApi.confirm(orderNo.value, handover.value.handoverId, {
      code: confirmForm.code,
      version: order.value.version,
      evidence: null
    })
    confirmForm.show = false
    uni.showToast({ title: '交接完成', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}

function goPay() {
  uni.navigateTo({ url: `/pages/owner/pay/index?orderNo=${orderNo.value}` })
}

function confirmService() {
  uni.showModal({
    title: '确认服务完成',
    content: '确认车辆已归还且服务已完成？确认后进入评价与售后阶段。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await ownerApi.confirmService(orderNo.value, order.value.version)
        uni.showToast({ title: '已确认', icon: 'success' })
        load()
      } catch (e) {
        // 已提示
      }
    }
  })
}

function openAfterSale() {
  afterSaleForm.amount = order.value ? order.value.totalYuan : ''
  afterSaleForm.reason = ''
  afterSaleForm.kindIndex = 0
  afterSaleForm.show = true
}

function onKindChange(e: any) {
  afterSaleForm.kindIndex = Number(e.detail.value)
}

async function submitAfterSale() {
  try {
    await ownerApi.createAfterSale(orderNo.value, {
      kind: kinds[afterSaleForm.kindIndex],
      requestedFen: afterSaleForm.amount ? Math.round(Number(afterSaleForm.amount) * 100) : null,
      reason: afterSaleForm.reason
    })
    afterSaleForm.show = false
    uni.showToast({ title: '已提交，等待审核', icon: 'success' })
    uni.navigateTo({ url: '/pages/owner/after-sales/list' })
  } catch (e) {
    // 已提示
  }
}

function openDispute() {
  disputeForm.description = ''
  disputeForm.typeIndex = 0
  disputeForm.show = true
}

function onDisputeType(e: any) {
  disputeForm.typeIndex = Number(e.detail.value)
}

async function submitDispute() {
  if (!disputeForm.description) {
    uni.showToast({ title: '请填写争议说明', icon: 'none' })
    return
  }
  try {
    await ownerApi.createDispute(orderNo.value, {
      type: disputeTypeCodes[disputeForm.typeIndex],
      description: disputeForm.description
    })
    disputeForm.show = false
    uni.showToast({ title: '已提交争议', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}

function openReview() {
  reviewForm.content = ''
  reviewForm.score = 5
  reviewForm.show = true
}

async function submitReview() {
  try {
    await ownerApi.review(orderNo.value, { score: reviewForm.score, content: reviewForm.content, tags: '' })
    reviewForm.show = false
    uni.showToast({ title: '感谢评价', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}

void fen2yuan
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.hero-status {
  font-size: 38rpx;
  font-weight: 600;
}

.hero-sub {
  margin-top: 10rpx;
  font-size: 26rpx;
  opacity: 0.92;
}

.hero-hint {
  margin-top: 14rpx;
  font-size: 24rpx;
  opacity: 0.85;
}

.code-box {
  margin-top: 20rpx;
  background: var(--brand-soft);
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
}

.code {
  font-size: 64rpx;
  font-weight: 700;
  letter-spacing: 14rpx;
  color: var(--brand);
}

.timeline-item {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
}

.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: var(--line);
  margin-top: 12rpx;
}

.dot.first {
  background: var(--brand);
}

.tl-action {
  font-size: 27rpx;
}

.ev-group {
  padding: 14rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.ev-group:last-child {
  border-bottom: none;
}

.picker {
  display: flex;
  align-items: center;
}

.star {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  border: 1rpx solid var(--line);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 26rpx;
}

.star.on {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
  max-height: 88vh;
  overflow-y: auto;
}
</style>
