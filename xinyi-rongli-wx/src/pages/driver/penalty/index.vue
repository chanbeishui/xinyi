<template>
  <view class="page">
    <view class="card">
      <view class="card-title">处罚与申诉</view>
      <view class="muted">
        当日停单或跳过本轮会自动记录一次限制，限制记录在有效期内不可自助解除；如认为判定有误，可在记录失效前提交一次申诉。
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">限制记录</text>
      <text class="section-head__meta">{{ penalties.length }} 条</text>
    </view>

    <view class="list">
      <view v-for="p in penalties" :key="p.penaltyId" class="penalty">
        <view class="penalty__top">
          <text class="penalty__type">{{ p.penaltyTypeLabel }}</text>
          <text class="status" :class="p.status === 'ACTIVE' ? 'status--danger' : 'status--gray'">
            {{ p.statusLabel }}
          </text>
        </view>
        <view class="penalty__row">
          <text class="penalty__key">判定原因</text>
          <text class="penalty__val">{{ p.reason || '-' }}</text>
        </view>
        <view class="penalty__row">
          <text class="penalty__key">业务日</text>
          <text class="penalty__val num">{{ p.businessDate || '-' }}</text>
        </view>
        <view class="penalty__row">
          <text class="penalty__key">生效至</text>
          <text class="penalty__val num">{{ dateLabel(p.effectiveUntil) }}</text>
        </view>
        <view v-if="p.status === 'ACTIVE'" class="penalty__actions">
          <text class="op" @tap="openAppeal(p)">提交申诉</text>
        </view>
      </view>

      <view v-if="!penalties.length" class="empty">
        <view class="empty__title">暂无处罚记录</view>
        <view class="empty__hint">按时履约、按约定交接就不会产生限制</view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">我的申诉</text>
    </view>

    <view class="list">
      <view v-for="a in appeals" :key="a.appealId" class="appeal">
        <view class="appeal__top">
          <text class="appeal__type">{{ a.penaltyType || '限制记录' }}</text>
          <text
            class="status"
            :class="a.status === 'APPROVED' ? 'status--ok' : a.status === 'REJECTED' ? 'status--gray' : 'status--warn'"
          >
            {{ a.statusLabel }}
          </text>
        </view>
        <view class="appeal__reason">{{ a.reason }}</view>
        <view class="appeal__meta num">
          提交 {{ dateLabel(a.submittedAt) }}
          <text v-if="a.reviewedAt"> · 处理 {{ dateLabel(a.reviewedAt) }}</text>
        </view>
        <view v-if="a.publicOpinion" class="appeal__meta">处理意见：{{ a.publicOpinion }}</view>
      </view>

      <view v-if="!appeals.length" class="empty">
        <view class="empty__title">暂无申诉记录</view>
        <view class="empty__hint">对限制有异议时，可在记录失效前提交一次申诉</view>
      </view>
    </view>

    <view v-if="showAppeal" class="mask" @tap="closeAppeal">
      <view class="sheet" @tap.stop>
        <view class="sheet__head">
          <text class="sheet__title">提交申诉</text>
          <text class="sheet__close" @tap="closeAppeal">关闭</text>
        </view>
        <view class="muted sheet__desc">
          {{ current ? current.penaltyTypeLabel + ' · ' + (current.reason || '') : '' }}
        </view>
        <textarea
          v-model="appealReason"
          class="textarea"
          maxlength="1000"
          placeholder="请说明申诉理由，如当时网络异常、订单信息有误等"
        />
        <view class="counter num">{{ appealReason.length }}/1000</view>
        <view class="btn block btn-gap" hover-class="btn-hover" @tap="submitAppeal">提交</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { driverApi } from '@/api'

const penalties = ref<any[]>([])
const appeals = ref<any[]>([])
const showAppeal = ref(false)
const current = ref<any>(null)
const appealReason = ref('')

onShow(() => {
  load()
})

async function load() {
  await Promise.all([loadPenalties(), loadAppeals()])
}

async function loadPenalties() {
  try {
    const page: any = await driverApi.penalties()
    penalties.value = (page && page.rows) || []
  } catch (e) {
    penalties.value = []
  }
}

async function loadAppeals() {
  try {
    const page: any = await driverApi.appeals()
    appeals.value = (page && page.rows) || []
  } catch (e) {
    appeals.value = []
  }
}

function openAppeal(p: any) {
  current.value = p
  appealReason.value = ''
  showAppeal.value = true
}

function closeAppeal() {
  showAppeal.value = false
  current.value = null
}

async function submitAppeal() {
  const reason = appealReason.value.trim()
  if (!reason) {
    uni.showToast({ title: '请填写申诉理由', icon: 'none' })
    return
  }
  try {
    await driverApi.appeal(current.value.penaltyId, { reason })
    uni.showToast({ title: '已提交', icon: 'success' })
    closeAppeal()
    load()
  } catch (e) {
    // 已提示
  }
}

function dateLabel(value: string) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.penalty {
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.penalty:last-child {
  border-bottom: none;
}

.penalty__top,
.appeal__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.penalty__type,
.appeal__type {
  font-size: 28rpx;
  font-weight: 600;
  letter-spacing: 0.4rpx;
}

.penalty__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 6rpx 0;
}

.penalty__key {
  flex: 0 0 auto;
  font-size: 24rpx;
  color: var(--ink-3);
}

.penalty__val {
  flex: 1;
  min-width: 0;
  padding-left: 20rpx;
  text-align: right;
  font-size: 25rpx;
  color: var(--ink);
}

.penalty__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10rpx;
}

.op {
  padding: 6rpx 0 6rpx 24rpx;
  font-size: 25rpx;
  color: var(--brand);
}

.appeal {
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.appeal:last-child {
  border-bottom: none;
}

.appeal__reason {
  font-size: 26rpx;
  line-height: 1.6;
  color: var(--ink-2);
}

.appeal__meta {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}

.sheet__desc {
  margin: 14rpx 0 4rpx;
}

.counter {
  margin-top: 8rpx;
  text-align: right;
  font-size: 21rpx;
  color: var(--ink-3);
}

.btn-gap {
  margin-top: 22rpx;
}
</style>
