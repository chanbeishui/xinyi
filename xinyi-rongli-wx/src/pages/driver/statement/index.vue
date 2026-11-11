<template>
  <view class="page">
    <template v-if="!detail">
      <view v-for="s in rows" :key="s.statementId" class="slip" hover-class="card-hover" @tap="open(s)">
        <view class="slip__head">
          <view class="slip__main">
            <text class="eyebrow">对账单</text>
            <view class="slip__plate slip-plate-sm num">{{ s.statementNo }}</view>
            <view class="slip__sub">第 {{ s.revision }} 版 · {{ s.statusLabel }}</view>
          </view>
          <text
            class="status"
            :class="s.status === 'SENT' ? 'status--warn' : s.status === 'CONFIRMED' ? 'status--ok' : 'status--gray'"
          >
            {{ s.status === 'SENT' ? '待核对' : s.status === 'CONFIRMED' ? '已确认' : '查看' }}
          </text>
        </view>
        <view class="slip__foot">
          <text class="slip__no num">发出 {{ s.sentAt ? shortTime(s.sentAt) : '-' }}</text>
          <text class="price"><text class="price__unit">¥</text>{{ s.amountYuan || fen2yuan(s.amountFen) }}</text>
        </view>
      </view>

      <view v-if="!rows.length" class="empty">
        <view class="empty__title">暂无对账单</view>
        <view class="empty__hint">完成接送任务后，平台按业务日汇总生成对账单</view>
      </view>

      <view class="card">
        <view class="card-title">对账说明</view>
        <view class="muted">
          对账单按业务日汇总接送与代驾费用。确认后即锁定该版本快照，如发现差异请先提交差异再与运营核实。
        </view>
      </view>

      <RoleTabBar role="DRIVER" active="statement" />
    </template>

    <template v-else>
      <view class="slip">
        <view class="slip__head">
          <view class="slip__main">
            <text class="eyebrow">对账单 {{ detail.statementNo }}</text>
            <view class="slip__plate slip-plate-sm">第 {{ detail.revision }} 版</view>
          </view>
          <text class="status status--brand">{{ detail.statusLabel }}</text>
        </view>

        <view class="slip__row">
          <text class="slip__key">发出时间</text>
          <text class="slip__val num">{{ detail.sentAt ? dateTime(detail.sentAt) : '-' }}</text>
        </view>
        <view v-if="detail.confirmedAt" class="slip__row">
          <text class="slip__key">确认时间</text>
          <text class="slip__val num">{{ dateTime(detail.confirmedAt) }}</text>
        </view>
        <view class="slip__row slip__row--stack">
          <text class="slip__key">快照哈希</text>
          <text class="slip__val slip__val--mono">{{ detail.snapshotHash || '-' }}</text>
        </view>
        <view class="slip__foot">
          <text class="slip__no">账单金额</text>
          <text class="price"><text class="price__unit">¥</text>{{ detail.amountYuan || fen2yuan(detail.amountFen) }}</text>
        </view>
      </view>

      <view class="section-head">
        <text class="section-head__title">明细</text>
        <text class="section-head__meta">{{ (detail.lines || []).length }} 条</text>
      </view>

      <view class="list">
        <view v-for="(line, i) in detail.lines" :key="i" class="line">
          <view class="line__top">
            <text class="line__label">{{ lineLabel(line) }}</text>
            <text class="line__amount num">¥{{ fen2yuan(lineAmount(line)) }}</text>
          </view>
          <view class="line__desc">{{ lineDesc(line) }}</view>
        </view>
        <view v-if="!(detail.lines || []).length" class="empty">
          <view class="empty__title">该版本无可展示明细</view>
        </view>
      </view>

      <view class="card">
        <view class="card-title">提交差异</view>
        <view class="field">
          <view class="field-label">差异说明</view>
          <textarea v-model="difference" class="textarea" placeholder="说明差异订单与期望金额" />
        </view>
        <view class="btn ghost block btn-gap" hover-class="btn-hover" @tap="submitDifference">提交差异</view>
      </view>

      <view class="footer-space"></view>

      <view class="action-bar">
        <view class="btn gray" hover-class="btn-hover" @tap="detail = null">返回列表</view>
        <view v-if="detail.status === 'SENT'" class="btn btn-wide" hover-class="btn-hover" @tap="confirm">
          确认对账单
        </view>
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
.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slip__head .slip-plate-sm {
  font-size: 30rpx;
  letter-spacing: 0.4rpx;
}

.slip__row--stack {
  display: block;
}

/* 哈希是一长串十六进制，用等宽字体换行显示，别右对齐挤成一列 */
.slip__val--mono {
  display: block;
  margin-top: 8rpx;
  text-align: left;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 21rpx;
  line-height: 1.6;
  color: var(--ink-2);
  word-break: break-all;
}

.line {
  padding: 18rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.line:last-child {
  border-bottom: none;
}

.line__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.line__label {
  font-size: 26rpx;
  font-weight: 500;
}

.line__amount {
  font-size: 26rpx;
  color: var(--ink);
}

.line__desc {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--ink-3);
}

.btn-gap {
  margin-top: 18rpx;
}

.btn-wide {
  flex: 2;
}
</style>
