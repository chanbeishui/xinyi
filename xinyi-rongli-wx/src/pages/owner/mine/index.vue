<template>
  <view class="page">
    <view class="ink-panel">
      <view class="ink-panel__rule"></view>
      <view class="profile">
        <view class="avatar">{{ initial }}</view>
        <view class="profile__info">
          <view class="profile__name">{{ profile.nickname || '车主' }}</view>
          <view class="profile__sub">{{ profile.phoneBound ? '手机号已绑定' : '未绑定手机号' }}</view>
        </view>
        <view class="profile__role">车主视角</view>
      </view>
    </view>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/orders/index')">
        <text class="list-row__key">我的预约</text>
        <view class="row">
          <text class="list-row__hint">全部记录</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/after-sales/list')">
        <text class="list-row__key">售后与退款</text>
        <view class="row">
          <text class="list-row__hint">进度查询</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/vehicle/list')">
        <text class="list-row__key">常用车辆</text>
        <view class="row">
          <text class="list-row__hint">管理</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/owner/vehicle/list?tab=address')">
        <text class="list-row__key">接车地址</text>
        <view class="row">
          <text class="list-row__hint">管理</text>
          <view class="chev"></view>
        </view>
      </view>
    </view>

    <view class="list">
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/role/index')">
        <text class="list-row__key">切换视角</text>
        <view class="row">
          <text class="list-row__hint">已开通 {{ roleCount }} 个</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="go('/pages/settings/index')">
        <text class="list-row__key">设置与隐私</text>
        <view class="row">
          <text class="list-row__hint">账号安全</text>
          <view class="chev"></view>
        </view>
      </view>
      <view class="list-row" hover-class="card-hover" @tap="callService">
        <text class="list-row__key">联系客服</text>
        <view class="row">
          <text class="list-row__hint num">{{ servicePhone }}</text>
          <view class="chev"></view>
        </view>
      </view>
    </view>

    <view class="btn gray block" hover-class="btn-hover" @tap="logout">退出登录</view>

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
.profile {
  display: flex;
  align-items: center;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.profile__info {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.profile__name {
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 0.6rpx;
}

.profile__sub {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.6);
}

.profile__role {
  flex: 0 0 auto;
  padding: 4rpx 14rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.28);
  border-radius: 4rpx;
  font-size: 20rpx;
  letter-spacing: 1rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>
