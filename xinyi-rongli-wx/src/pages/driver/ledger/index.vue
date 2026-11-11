<template>
  <view class="page">
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="ink-panel__eyebrow">收支流水</view>
      <view class="ink-panel__title num">
        <text class="amount-unit">¥</text>{{ totalYuan }}
      </view>
      <view class="ink-panel__foot">
        <text class="panel-meta num">已收款 ¥{{ paidYuan }}</text>
        <text class="panel-meta">{{ entries.length }} 条明细</text>
      </view>
    </view>

    <view class="card">
      <view class="filter">
        <view class="filter__item">
          <picker mode="date" :value="from" @change="(e:any) => onDate('from', e.detail.value)">
            <view class="input filter__date num">{{ from || '开始日期' }}</view>
          </picker>
        </view>
        <view class="filter__item">
          <picker mode="date" :value="to" @change="(e:any) => onDate('to', e.detail.value)">
            <view class="input filter__date num">{{ to || '结束日期' }}</view>
          </picker>
        </view>
      </view>
      <view class="filter filter--gap">
        <input v-model="keyword" class="input filter__kw" placeholder="订单号 / 车牌号" />
        <view class="btn small" hover-class="btn-hover" @tap="loadEntries">查询</view>
        <view class="btn small gray filter__btn" hover-class="btn-hover" @tap="reset">重置</view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">接送明细</text>
      <text class="section-head__meta">{{ entries.length }} 条</text>
    </view>

    <view class="list">
      <view v-for="e in entries" :key="e.entryId" class="row-item">
        <view class="row-item__top">
          <text class="row-item__title num">{{ e.orderNo }}</text>
          <text class="row-item__amount num">¥{{ e.amountYuan != null ? e.amountYuan : '待定价' }}</text>
        </view>
        <view class="row-item__meta">{{ e.plateNo || '-' }} · {{ e.entryKindLabel }} · {{ dateLabel(e.serviceEndedAt) }}</view>
        <view class="row-item__meta">
          {{ e.statementNo ? '已入对账单 ' + e.statementNo : '未入对账单' }}
          <text v-if="e.statementStatus"> · {{ e.statementStatus }}</text>
        </view>
      </view>

      <view v-if="!entries.length" class="empty">
        <view class="empty__title">该时间段暂无可结算明细</view>
        <view class="empty__hint">换个日期范围或清空关键词再查</view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">已登记收款</text>
      <text class="section-head__meta">{{ payments.length }} 条</text>
    </view>

    <view class="list">
      <view v-for="p in payments" :key="p.id" class="row-item">
        <view class="row-item__top">
          <text class="row-item__title">{{ p.statement_no || '-' }}</text>
          <text class="row-item__amount num">¥{{ fenToYuan(p.amount_fen) }}</text>
        </view>
        <view class="row-item__meta num">付款时间 {{ dateLabel(p.paid_at) }} · {{ paymentStatus(p.status) }}</view>
        <view v-if="p.reason" class="row-item__meta">说明：{{ p.reason }}</view>
      </view>

      <view v-if="!payments.length" class="empty">
        <view class="empty__title">暂无外部付款记录</view>
        <view class="empty__hint">对账确认后由平台登记收款，核实到账会显示在这里</view>
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
.ink-panel__title .amount-unit {
  margin-right: 6rpx;
  font-size: 34rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
}

.panel-meta {
  font-size: 21rpx;
  letter-spacing: 0.6rpx;
  color: rgba(255, 255, 255, 0.55);
}

.filter {
  display: flex;
  align-items: center;
}

.filter--gap {
  margin-top: 14rpx;
}

.filter__item {
  flex: 1;
  min-width: 0;
}

.filter__item + .filter__item {
  margin-left: 14rpx;
}

.filter__date {
  width: 100%;
  text-align: center;
  font-size: 25rpx;
  line-height: 78rpx;
  color: var(--ink-2);
}

.filter__kw {
  flex: 1;
}

.filter__btn {
  margin-left: 12rpx;
}

.row-item {
  padding: 18rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.row-item:last-child {
  border-bottom: none;
}

.row-item__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.row-item__title {
  font-size: 27rpx;
  font-weight: 500;
  letter-spacing: 0.4rpx;
}

.row-item__amount {
  font-size: 27rpx;
  color: var(--ink);
}

.row-item__meta {
  margin-top: 6rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: var(--ink-3);
}
</style>
