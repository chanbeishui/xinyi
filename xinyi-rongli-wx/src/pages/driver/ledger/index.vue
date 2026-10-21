<template>
  <view class="page">
    <view class="card">
      <view class="card-title">收支流水</view>
      <view class="row-between">
        <text class="label">明细合计</text>
        <text class="price">¥{{ totalYuan }}</text>
      </view>
      <view class="row-between">
        <text class="label">已收款合计</text>
        <text class="value">¥{{ paidYuan }}</text>
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 16rpx">
        <picker mode="date" :value="from" @change="(e:any) => onDate('from', e.detail.value)">
          <view class="input picker">{{ from || '开始日期' }}</view>
        </picker>
        <picker mode="date" :value="to" @change="(e:any) => onDate('to', e.detail.value)">
          <view class="input picker">{{ to || '结束日期' }}</view>
        </picker>
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 12rpx">
        <input v-model="keyword" class="input" placeholder="订单号 / 车牌号" style="flex: 1" />
        <view class="btn small" @tap="loadEntries">查询</view>
        <view class="btn small gray" @tap="reset">重置</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">接送明细（{{ entries.length }}）</view>
      <view v-if="!entries.length" class="muted">该时间段暂无可结算明细</view>
      <view v-for="e in entries" :key="e.entryId" class="field">
        <view class="row-between">
          <text class="value" style="text-align: left">{{ e.orderNo }}</text>
          <text class="price">¥{{ e.amountYuan != null ? e.amountYuan : '待定价' }}</text>
        </view>
        <view class="muted" style="margin-top: 6rpx">
          {{ e.plateNo || '-' }} · {{ e.entryKindLabel }} · {{ dateLabel(e.serviceEndedAt) }}
        </view>
        <view class="muted" style="margin-top: 4rpx">
          {{ e.statementNo ? '已入对账单 ' + e.statementNo : '未入对账单' }}
          <text v-if="e.statementStatus"> · {{ e.statementStatus }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">已登记收款（{{ payments.length }}）</view>
      <view v-if="!payments.length" class="muted">暂无外部付款记录</view>
      <view v-for="p in payments" :key="p.id" class="field">
        <view class="row-between">
          <text class="value" style="text-align: left">{{ p.statement_no || '-' }}</text>
          <text class="price">¥{{ fenToYuan(p.amount_fen) }}</text>
        </view>
        <view class="muted" style="margin-top: 6rpx">
          付款时间 {{ dateLabel(p.paid_at) }} · {{ paymentStatus(p.status) }}
        </view>
        <view v-if="p.reason" class="muted" style="margin-top: 4rpx">说明：{{ p.reason }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { driverApi } from '@/api'

const entries = ref<any[]>([])
const payments = ref<any[]>([])
const from = ref('')
const to = ref('')
const keyword = ref('')

const totalYuan = computed(() => {
  const sum = entries.value.reduce((acc, e) => acc + Number(e.amountFen || 0), 0)
  return (sum / 100).toFixed(2)
})

const paidYuan = computed(() => {
  const sum = payments.value
    .filter((p) => p.status === 'VERIFIED')
    .reduce((acc, p) => acc + Number(p.amount_fen || 0), 0)
  return (sum / 100).toFixed(2)
})

onShow(() => {
  loadEntries()
  loadPayments()
})

function onDate(field: 'from' | 'to', value: string) {
  if (field === 'from') from.value = value
  else to.value = value
  loadEntries()
}

async function loadEntries() {
  try {
    const view: any = await driverApi.reconciliationEntries({
      from: from.value || undefined,
      to: to.value || undefined,
      keyword: keyword.value || undefined
    })
    entries.value = (view && view.rows) || []
  } catch (e) {
    entries.value = []
  }
}

async function loadPayments() {
  try {
    const view: any = await driverApi.paymentRecords()
    payments.value = (view && view.rows) || []
  } catch (e) {
    payments.value = []
  }
}

function reset() {
  from.value = ''
  to.value = ''
  keyword.value = ''
  loadEntries()
}

function fenToYuan(fen: number) {
  return ((Number(fen) || 0) / 100).toFixed(2)
}

function paymentStatus(status: string) {
  const map: Record<string, string> = {
    PENDING: '待核实',
    VERIFIED: '已核实',
    REJECTED: '已驳回',
    REVERSED: '已冲正'
  }
  return map[status || ''] || status || '-'
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
.picker {
  display: flex;
  align-items: center;
  color: var(--sub);
}
</style>
