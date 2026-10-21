<template>
  <view class="page">
    <view v-if="detail" class="card">
      <view class="row-between">
        <view class="card-title" style="margin: 0">{{ detail.kindLabel }}</view>
        <view class="tag" :class="detail.caseStatus === 'CLOSED' ? 'ok' : 'warn'">{{ detail.caseStatusLabel }}</view>
      </view>
      <view class="row-between">
        <text class="label">售后单号</text>
        <text class="value">{{ detail.caseNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">关联订单</text>
        <text class="value">{{ detail.orderNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">订单金额</text>
        <text class="value">¥{{ fen2yuan(detail.orderTotalFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">已支付</text>
        <text class="value">¥{{ fen2yuan(detail.paidFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">申请退款</text>
        <text class="value">¥{{ fen2yuan(detail.requestedFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">批准退款</text>
        <text class="value">{{ detail.approvedFen ? `¥${fen2yuan(detail.approvedFen)}` : '待审核' }}</text>
      </view>
      <view class="row-between">
        <text class="label">是否需要还车</text>
        <text class="value">{{ returnLabel(detail.returnState) }}</text>
      </view>
      <view class="row-between">
        <text class="label">申请原因</text>
        <text class="value" style="max-width: 60%">{{ detail.reason }}</text>
      </view>
    </view>

    <view v-if="detail && detail.refund" class="card">
      <view class="card-title">退款进度</view>
      <view class="row-between">
        <text class="label">退款单号</text>
        <text class="value">{{ detail.refund.refundNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">退款金额</text>
        <text class="value">¥{{ fen2yuan(detail.refund.amountFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">状态</text>
        <text class="tag">{{ detail.refund.statusLabel }}</text>
      </view>
      <view v-if="detail.refund.successAt" class="row-between">
        <text class="label">到账时间</text>
        <text class="value">{{ dateTime(detail.refund.successAt) }}</text>
      </view>
    </view>

    <view v-if="detail && detail.reviews && detail.reviews.length" class="card">
      <view class="card-title">审核记录</view>
      <view v-for="r in detail.reviews" :key="r.revision" class="review">
        <view class="row-between">
          <text class="value" style="text-align: left">第 {{ r.revision }} 次审核</text>
          <text class="tag" :class="r.decision === 'APPROVED' ? 'ok' : 'gray'">
            {{ r.decision === 'APPROVED' ? '同意' : '驳回' }}
          </text>
        </view>
        <view class="muted">批准退款 ¥{{ fen2yuan(r.approvedFen) }} · {{ r.returnRequired ? '需还车' : '无需还车' }}</view>
        <view v-if="r.opinion" class="muted">意见：{{ r.opinion }}</view>
        <view class="muted">{{ dateTime(r.reviewedAt) }}</view>
      </view>
    </view>

    <view v-if="detail" class="card">
      <view class="card-title">补充说明</view>
      <textarea v-model="note" class="textarea" placeholder="可补充说明或回应平台问询" />
      <view class="btn ghost" style="margin-top: 20rpx" @tap="submitNote">提交说明</view>
    </view>

    <view v-if="!detail" class="empty">加载中…</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import { dateTime, fen2yuan } from '@/utils/format'

const caseId = ref('')
const detail = ref<any>(null)
const note = ref('')

onLoad((options) => {
  caseId.value = (options && options.caseId) || ''
  load()
})

async function load() {
  try {
    detail.value = await ownerApi.afterSale(caseId.value)
  } catch (e) {
    detail.value = null
  }
}

function returnLabel(s: string) {
  const map: Record<string, string> = {
    UNDETERMINED: '待审核判定',
    NOT_REQUIRED: '无需还车',
    WAIT_RETURN: '需实还车辆',
    RETURNED: '已还车'
  }
  return map[s] || s || '-'
}

async function submitNote() {
  if (!note.value) {
    uni.showToast({ title: '请填写说明', icon: 'none' })
    return
  }
  try {
    await ownerApi.afterSaleNote(caseId.value, note.value)
    note.value = ''
    uni.showToast({ title: '已提交', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.review {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.review:last-child {
  border-bottom: none;
}
</style>
