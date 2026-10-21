<template>
  <view class="page">
    <view class="card">
      <view class="card-title">处罚与申诉</view>
      <view class="muted">
        当日停单或跳过本轮会自动记录一次限制，限制记录在有效期内不可自助解除；如认为判定有误，可在记录失效前提交一次申诉。
      </view>
    </view>

    <view v-if="!penalties.length" class="empty">暂无处罚记录</view>

    <view v-for="p in penalties" :key="p.penaltyId" class="card">
      <view class="row-between">
        <text class="value" style="text-align: left">{{ p.penaltyTypeLabel }}</text>
        <text class="tag" :class="p.status === 'ACTIVE' ? 'danger' : 'gray'">{{ p.statusLabel }}</text>
      </view>
      <view class="row-between">
        <text class="label">判定原因</text>
        <text class="value">{{ p.reason || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">业务日</text>
        <text class="value">{{ p.businessDate || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">生效至</text>
        <text class="value">{{ dateLabel(p.effectiveUntil) }}</text>
      </view>
      <view
        v-if="p.status === 'ACTIVE'"
        class="btn ghost small"
        style="margin-top: 16rpx"
        @tap="openAppeal(p)"
      >
        提交申诉
      </view>
    </view>

    <view class="card">
      <view class="card-title">我的申诉</view>
      <view v-if="!appeals.length" class="muted">暂无申诉记录</view>
      <view v-for="a in appeals" :key="a.appealId" class="field">
        <view class="row-between">
          <text class="value" style="text-align: left">{{ a.penaltyType || '限制记录' }}</text>
          <text class="tag" :class="a.status === 'APPROVED' ? 'ok' : a.status === 'REJECTED' ? 'gray' : 'warn'">
            {{ a.statusLabel }}
          </text>
        </view>
        <view class="muted" style="margin-top: 8rpx">{{ a.reason }}</view>
        <view class="muted" style="margin-top: 4rpx">
          提交 {{ dateLabel(a.submittedAt) }}
          <text v-if="a.reviewedAt"> · 处理 {{ dateLabel(a.reviewedAt) }}</text>
        </view>
        <view v-if="a.publicOpinion" class="muted" style="margin-top: 4rpx">处理意见：{{ a.publicOpinion }}</view>
      </view>
    </view>

    <view v-if="showAppeal" class="mask" @tap="closeAppeal">
      <view class="sheet" @tap.stop>
        <view class="card-title">提交申诉</view>
        <view class="muted">{{ current ? current.penaltyTypeLabel + ' · ' + (current.reason || '') : '' }}</view>
        <textarea
          v-model="appealReason"
          class="textarea"
          maxlength="1000"
          placeholder="请说明申诉理由，如当时网络异常、订单信息有误等"
          style="margin-top: 20rpx"
        />
        <view class="muted" style="margin-top: 8rpx">{{ appealReason.length }}/1000</view>
        <view class="btn" style="margin-top: 24rpx" @tap="submitAppeal">提交</view>
        <view class="btn gray" style="margin-top: 12rpx" @tap="closeAppeal">取消</view>
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
.mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
}
</style>
