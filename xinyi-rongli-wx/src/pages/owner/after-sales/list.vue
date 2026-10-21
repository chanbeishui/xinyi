<template>
  <view class="page">
    <view v-for="c in rows" :key="c.caseId" class="card" @tap="open(c)">
      <view class="row-between">
        <view>
          <view class="value" style="text-align: left">{{ c.kindLabel }}</view>
          <view class="muted" style="margin-top: 4rpx">订单 {{ c.orderNo }}</view>
        </view>
        <view class="tag" :class="statusClass(c.caseStatus)">{{ c.caseStatusLabel }}</view>
      </view>
      <view class="divider"></view>
      <view class="row-between">
        <text class="label">申请金额</text>
        <text class="value">¥{{ fen2yuan(c.requestedFen) }}</text>
      </view>
      <view class="row-between">
        <text class="label">批准金额</text>
        <text class="value">{{ c.approvedFen ? `¥${fen2yuan(c.approvedFen)}` : '待审核' }}</text>
      </view>
      <view class="row-between">
        <text class="muted">{{ c.reason }}</text>
        <text class="muted">{{ shortTime(c.createdAt) }}</text>
      </view>
    </view>

    <view v-if="!rows.length" class="empty">暂无售后申请</view>

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
  if (s === 'CLOSED') return 'ok'
  if (s === 'REJECTED') return 'gray'
  if (s === 'EXECUTING') return 'warn'
  return ''
}

function open(c: any) {
  uni.navigateTo({ url: `/pages/owner/after-sales/detail?caseId=${c.caseId}` })
}
</script>
