<template>
  <view class="page">
    <template v-if="!detail">
      <view v-for="s in rows" :key="s.statementId" class="card" @tap="open(s)">
        <view class="row-between">
          <view>
            <view class="value" style="text-align: left">{{ s.statementNo }}</view>
            <view class="muted" style="margin-top: 4rpx">第 {{ s.revision }} 版 · {{ s.statusLabel }}</view>
          </view>
          <view class="price" style="font-size: 32rpx">¥{{ s.amountYuan || fen2yuan(s.amountFen) }}</view>
        </view>
        <view class="row-between" style="margin-top: 8rpx">
          <text class="muted">发出 {{ s.sentAt ? shortTime(s.sentAt) : '-' }}</text>
          <text class="tag" :class="s.status === 'SENT' ? 'warn' : s.status === 'CONFIRMED' ? 'ok' : 'gray'">
            {{ s.status === 'SENT' ? '待核对' : s.status === 'CONFIRMED' ? '已确认' : '查看' }}
          </text>
        </view>
      </view>
      <view v-if="!rows.length" class="empty">暂无对账单</view>

      <view class="card">
        <view class="card-title">对账说明</view>
        <view class="muted">
          对账单按业务日汇总接送与代驾费用。确认后即锁定该版本快照，如发现差异请先提交差异再与运营核实。
        </view>
      </view>

      <RoleTabBar role="DRIVER" active="statement" />
    </template>

    <template v-else>
      <view class="card">
        <view class="row-between">
          <view class="card-title" style="margin: 0">{{ detail.statementNo }}</view>
          <text class="tag">{{ detail.statusLabel }}</text>
        </view>
        <view class="row-between">
          <text class="label">账单金额</text>
          <text class="price">¥{{ detail.amountYuan || fen2yuan(detail.amountFen) }}</text>
        </view>
        <view class="row-between">
          <text class="label">版本</text>
          <text class="value">第 {{ detail.revision }} 版</text>
        </view>
        <view class="row-between">
          <text class="label">快照哈希</text>
          <text class="value" style="max-width: 60%; word-break: break-all; font-size: 22rpx">{{ detail.snapshotHash || '-' }}</text>
        </view>
        <view class="row-between">
          <text class="label">发出时间</text>
          <text class="value">{{ detail.sentAt ? dateTime(detail.sentAt) : '-' }}</text>
        </view>
        <view v-if="detail.confirmedAt" class="row-between">
          <text class="label">确认时间</text>
          <text class="value">{{ dateTime(detail.confirmedAt) }}</text>
        </view>
      </view>

      <view class="card">
        <view class="card-title">明细（{{ (detail.lines || []).length }} 条）</view>
        <view v-for="(line, i) in detail.lines" :key="i" class="line">
          <view class="row-between">
            <text class="value" style="text-align: left">{{ lineLabel(line) }}</text>
            <text class="value">¥{{ fen2yuan(lineAmount(line)) }}</text>
          </view>
          <view class="muted">{{ lineDesc(line) }}</view>
        </view>
        <view v-if="!(detail.lines || []).length" class="muted">该版本无可展示明细</view>
      </view>

      <view class="card">
        <view class="card-title">提交差异</view>
        <view class="field">
          <view class="field-label">差异说明</view>
          <textarea v-model="difference" class="textarea" placeholder="说明差异订单与期望金额" />
        </view>
        <view class="btn ghost" style="margin-top: 16rpx" @tap="submitDifference">提交差异</view>
      </view>

      <view class="action-bar">
        <view class="btn gray" style="flex: 1" @tap="detail = null">返回列表</view>
        <view v-if="detail.status === 'SENT'" class="btn" style="flex: 2" @tap="confirm">确认对账单</view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { driverApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { dateTime, fen2yuan, shortTime } from '@/utils/format'

const rows = ref<any[]>([])
const detail = ref<any>(null)
const difference = ref('')

onShow(() => {
  if (!detail.value) load()
})

async function load() {
  try {
    const view: any = await driverApi.statements()
    rows.value = (view && view.rows) || []
  } catch (e) {
    rows.value = []
  }
}

async function open(s: any) {
  try {
    detail.value = await driverApi.statement(s.statementId)
    difference.value = ''
  } catch (e) {
    // 已提示
  }
}

function lineLabel(line: any) {
  return line.orderNo || line.title || line.businessDate || line.label || '明细'
}

function lineDesc(line: any) {
  const parts: string[] = []
  if (line.businessDate) parts.push(String(line.businessDate))
  if (line.note) parts.push(String(line.note))
  if (line.mileageKm) parts.push(`${line.mileageKm} km`)
  return parts.join(' · ')
}

function lineAmount(line: any) {
  const candidates = ['amountFen', 'feeFen', 'totalFen', 'amount']
  for (const k of candidates) {
    if (line[k] != null) return Number(line[k])
  }
  return 0
}

function confirm() {
  uni.showModal({
    title: '确认对账单',
    content: '确认后该版本快照将锁定，后续如有异议需通过差异流程处理。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await driverApi.confirmStatement(detail.value.statementId, {
          revision: detail.value.revision,
          snapshotHash: detail.value.snapshotHash
        })
        uni.showToast({ title: '已确认', icon: 'success' })
        detail.value = null
        load()
      } catch (e) {
        // 已提示
      }
    }
  })
}

async function submitDifference() {
  if (!difference.value) {
    uni.showToast({ title: '请填写差异说明', icon: 'none' })
    return
  }
  try {
    await driverApi.statementDifference(detail.value.statementId, { description: difference.value })
    difference.value = ''
    uni.showToast({ title: '已提交差异', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.line {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.line:last-child {
  border-bottom: none;
}
</style>
