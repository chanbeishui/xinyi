<template>
  <view class="page">
    <view v-if="detail" class="slip">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">售后单 {{ detail.caseNo }}</text>
          <view class="slip__plate slip-plate-sm">{{ detail.kindLabel }}</view>
        </view>
        <view class="status" :class="detail.caseStatus === 'CLOSED' ? 'status--ok' : 'status--warn'">
          {{ detail.caseStatusLabel }}
        </view>
      </view>

      <view class="slip__row">
        <text class="slip__key">关联订单</text>
        <text class="slip__val num">{{ detail.orderNo }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">订单金额</text>
        <text class="slip__val num">¥{{ fen2yuan(detail.orderTotalFen) }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">已支付</text>
        <text class="slip__val num">¥{{ fen2yuan(detail.paidFen) }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">申请退款</text>
        <text class="slip__val num">¥{{ fen2yuan(detail.requestedFen) }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">批准退款</text>
        <text class="slip__val num">{{ detail.approvedFen ? `¥${fen2yuan(detail.approvedFen)}` : '待审核' }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">是否需要还车</text>
        <text class="slip__val">{{ returnLabel(detail.returnState) }}</text>
      </view>
      <view class="slip__row slip__row--stack">
        <text class="slip__key">申请原因</text>
        <text class="slip__val slip__val--wrap">{{ detail.reason }}</text>
      </view>
    </view>

    <view v-if="detail && detail.refund" class="slip">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">退款进度</text>
          <view class="slip__plate slip-plate-sm">{{ detail.refund.statusLabel }}</view>
        </view>
      </view>
      <view class="slip__row">
        <text class="slip__key">退款单号</text>
        <text class="slip__val num">{{ detail.refund.refundNo }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">退款金额</text>
        <text class="slip__val num">¥{{ fen2yuan(detail.refund.amountFen) }}</text>
      </view>
      <view v-if="detail.refund.successAt" class="slip__row">
        <text class="slip__key">到账时间</text>
        <text class="slip__val num">{{ dateTime(detail.refund.successAt) }}</text>
      </view>
    </view>

    <view v-if="detail && detail.reviews && detail.reviews.length" class="card">
      <view class="card-title">审核记录</view>
      <view v-for="r in detail.reviews" :key="r.revision" class="review">
        <view class="review__top">
          <text class="review__title">第 {{ r.revision }} 次审核</text>
          <text class="status" :class="r.decision === 'APPROVED' ? 'status--ok' : 'status--gray'">
            {{ r.decision === 'APPROVED' ? '同意' : '驳回' }}
          </text>
        </view>
        <view class="muted num">
          批准退款 ¥{{ fen2yuan(r.approvedFen) }} · {{ r.returnRequired ? '需还车' : '无需还车' }}
        </view>
        <view v-if="r.opinion" class="muted">意见：{{ r.opinion }}</view>
        <view class="muted num">{{ dateTime(r.reviewedAt) }}</view>
      </view>
    </view>

    <view v-if="detail" class="card">
      <view class="card-title">补充说明</view>
      <textarea v-model="note" class="textarea" placeholder="可补充说明或回应平台问询" />
      <view class="btn ghost block btn-gap" hover-class="btn-hover" @tap="submitNote">提交说明</view>
    </view>

    <view v-if="!detail" class="slip skeleton-card">
      <view class="skeleton" style="width: 46%; height: 30rpx"></view>
      <view class="skeleton" style="width: 72%; height: 24rpx; margin-top: 18rpx"></view>
      <view class="skeleton" style="width: 100%; height: 1rpx; margin-top: 22rpx"></view>
      <view class="skeleton" style="width: 60%; height: 24rpx; margin-top: 22rpx"></view>
      <view class="skeleton" style="width: 60%; height: 24rpx; margin-top: 16rpx"></view>
    </view>
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
.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slip__head .slip-plate-sm {
  font-size: 34rpx;
  letter-spacing: 0.5rpx;
}

/* 长文本行：标签在上、内容在下，避免右对齐把地址挤成竖排 */
.slip__row--stack {
  display: block;
}

.slip__val--wrap {
  display: block;
  margin-top: 8rpx;
  text-align: left;
  line-height: 1.65;
  color: var(--ink-2);
}

.review {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--line-2);
}

.review:last-child {
  border-bottom: none;
}

.review__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.review__title {
  font-size: 26rpx;
  font-weight: 600;
}

.btn-gap {
  margin-top: 18rpx;
}

.skeleton-card {
  padding: 26rpx 24rpx;
}
</style>
