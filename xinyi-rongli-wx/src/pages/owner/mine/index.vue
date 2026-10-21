<template>
  <view class="page">
    <view class="profile-card">
      <view class="avatar">{{ initial }}</view>
      <view class="info">
        <view class="name">{{ profile.nickname || '车主' }}</view>
        <view class="sub">{{ profile.phoneBound ? '手机号已绑定' : '未绑定手机号' }}</view>
      </view>
      <view class="tag" style="background: rgba(255,255,255,0.2); color: #fff">车主视角</view>
    </view>

    <view class="card">
      <view class="row-between" @tap="go('/pages/owner/orders/index')">
        <text class="label">我的预约</text>
        <text class="muted">全部记录 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/owner/after-sales/list')">
        <text class="label">售后与退款</text>
        <text class="muted">进度查询 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/owner/vehicle/list')">
        <text class="label">常用车辆</text>
        <text class="muted">管理 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/owner/vehicle/list?tab=address')">
        <text class="label">接车地址</text>
        <text class="muted">管理 ›</text>
      </view>
    </view>

    <view class="card">
      <view class="row-between" @tap="go('/pages/role/index')">
        <text class="label">切换视角</text>
        <text class="muted">已开通 {{ roleCount }} 个 ›</text>
      </view>
      <view class="row-between" @tap="go('/pages/settings/index')">
        <text class="label">设置与隐私</text>
        <text class="muted">账号安全 ›</text>
      </view>
      <view class="row-between" @tap="callService">
        <text class="label">联系客服</text>
        <text class="muted">{{ servicePhone }} ›</text>
      </view>
    </view>

    <view class="btn gray" @tap="logout">退出登录</view>

    <RoleTabBar role="OWNER" active="mine" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi } from '@/api'
import RoleTabBar from '@/components/RoleTabBar.vue'
import { applySession, resetSession, session } from '@/store/session'

const servicePhone = '400-000-0000'

const profile = computed(() => session.profile || {})
const initial = computed(() => {
  const n = (session.profile && session.profile.nickname) || '车'
  return n.substring(0, 1)
})
const roleCount = computed(() => {
  const roles = (session.profile && session.profile.roles) || []
  return roles.length
})

onShow(() => {
  commonApi
    .session()
    .then((view: any) => applySession(view))
    .catch(() => {})
})

function go(url: string) {
  uni.navigateTo({ url })
}

function callService() {
  uni.makePhoneCall({ phoneNumber: servicePhone.replace(/-/g, ''), fail: () => {} })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前账号？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await commonApi.logout(false)
      } catch (e) {
        // 忽略
      }
      resetSession()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}
</script>

<style scoped>
.profile-card {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 36rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  font-weight: 600;
}

.info {
  flex: 1;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.sub {
  margin-top: 6rpx;
  font-size: 24rpx;
  opacity: 0.85;
}
</style>
