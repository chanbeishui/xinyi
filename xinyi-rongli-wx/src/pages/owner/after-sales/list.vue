<template>
  <view class="page">
    <view v-for="c in rows" :key="c.caseId" class="slip" hover-class="card-hover" @tap="open(c)">
      <view class="slip__head">
        <view class="slip__main">
          <text class="eyebrow">售后申请</text>
          <view class="slip__plate slip__plate--sm">{{ c.kindLabel }}</view>
        </view>
        <view class="status" :class="statusClass(c.caseStatus)">{{ c.caseStatusLabel }}</view>
      </view>

      <view class="slip__row">
        <text class="slip__key">订单号</text>
        <text class="slip__val num">{{ c.orderNo }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">申请金额</text>
        <text class="slip__val num">¥{{ fen2yuan(c.requestedFen) }}</text>
      </view>
      <view class="slip__row">
        <text class="slip__key">批准金额</text>
        <text class="slip__val num">{{ c.approvedFen ? `¥${fen2yuan(c.approvedFen)}` : '待审核' }}</text>
      </view>

      <view class="slip__foot">
        <text class="slip__no slip__no--reason ellipsis">{{ c.reason }}</text>
        <text class="slip__no">{{ shortTime(c.createdAt) }}</text>
      </view>
    </view>

    <view v-if="!rows.length" class="empty">
      <view class="empty__title">暂无售后申请</view>
      <view class="empty__hint">订单取消、退款与争议可在对应订单详情里发起</view>
    </view>

    <RoleTabBar role="OWNER" active="orders" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { fen2yuan, shortTime } from '@/utils/format'

const rows = ref<any[]>([])

onShow(() => {
  load()
})

async function load() {
  try {
    const page: any = await ownerApi.afterSales()
    rows.value = (page && page.rows) || []
  } catch (e) {
    rows.value = []
  }
}

function statusClass(s: string) {
  if (s === 'CLOSED') return 'status--ok'
  if (s === 'REJECTED') return 'status--gray'
  if (s === 'EXECUTING') return 'status--warn'
  return 'status--brand'
}

function open(c: any) {
  uni.navigateTo({ url: `/pages/owner/after-sales/detail?caseId=${c.caseId}` })
}
</script>

<style scoped>
.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slip__head .slip__plate--sm {
  font-size: 32rpx;
  letter-spacing: 0.5rpx;
}

.slip__no--reason {
  flex: 1;
  min-width: 0;
  padding-right: 20rpx;
  text-align: left;
}
</style>
