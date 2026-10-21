<template>
  <view class="page">
    <view class="card">
      <view class="card-title">当前账号</view>
      <view class="row-between">
        <text class="label">昵称</text>
        <text class="value">{{ profile.nickname || '车主' }}</text>
      </view>
      <view class="row-between">
        <text class="label">手机号</text>
        <text class="value">{{ profile.phoneBound ? '已绑定' : '未绑定' }}</text>
      </view>
      <view class="row-between">
        <text class="label">公司绑定</text>
        <text class="value">{{ profile.employeeBound ? (profile.employeeName || '已绑定') : '未绑定' }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">选择视角</view>
      <view class="muted" style="margin-bottom: 16rpx">切换视角只改变使用界面，不改变账号归属与已成立的承接任务。</view>
      <view v-for="r in roleList" :key="r.code" class="role" :class="{ disabled: !r.enabled }" @tap="pick(r)">
        <view class="role-main">
          <view class="role-name">{{ r.label }}</view>
          <view class="role-desc">{{ r.desc }}</view>
        </view>
        <view class="tag" :class="r.enabled ? '' : 'gray'">{{ r.enabled ? '进入' : '未开通' }}</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">还没有员工视角？</view>
      <view class="muted" style="margin-bottom: 20rpx">
        代驾与检测员工视角必须由公司受控绑定开通：检测员工需公司下发绑定凭证，代驾需提交实名与驾驶证资料审核。
      </view>
      <view class="btn ghost small" @tap="goBinding">填写绑定申请</view>
    </view>

    <view class="btn gray" style="margin-top: 12rpx" @tap="logout">退出登录</view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi } from '@/api'
import { applySession, resetSession, session } from '@/store/session'

const profile = computed(() => session.profile || {})
const roles = computed<string[]>(() => (session.profile && session.profile.roles) || ['OWNER'])
const bindingReason = ref('')

const roleList = computed(() => [
  {
    code: 'OWNER',
    label: '车主',
    desc: '预约检测、上门时段、支付与售后',
    enabled: roles.value.indexOf('OWNER') >= 0
  },
  {
    code: 'DRIVER',
    label: '代驾',
    desc: '听单、接单邀约、接送车履约与对账',
    enabled: roles.value.indexOf('DRIVER') >= 0
  },
  {
    code: 'STATION',
    label: '检测员工',
    desc: '检测点订单、证件核对、结果登记',
    enabled: roles.value.indexOf('STATION') >= 0
  }
])

onShow(() => {
  commonApi
    .session()
    .then((view: any) => applySession(view))
    .catch(() => {})
})

async function pick(role: any) {
  if (!role.enabled) {
    uni.showModal({
      title: '视角未开通',
      content:
        role.code === 'DRIVER'
          ? '请先提交代驾实名与驾驶证资料，审核通过后开通接单视角'
          : '请先使用公司下发的绑定凭证完成员工账号绑定',
      confirmText: role.code === 'DRIVER' ? '去认证' : '去绑定',
      success: (res) => {
        if (!res.confirm) return
        uni.navigateTo({
          url: role.code === 'DRIVER' ? '/pages/driver/qualification/index' : '/pages/settings/index'
        })
      }
    })
    return
  }
  try {
    const view: any = await commonApi.switchRole(role.code)
    applySession(view)
    const target =
      role.code === 'DRIVER'
        ? '/pages/driver/home/index'
        : role.code === 'STATION'
        ? '/pages/station/home/index'
        : '/pages/owner/home/index'
    uni.reLaunch({ url: target })
  } catch (e) {
    // 请求层已提示
  }
}

function goBinding() {
  uni.navigateTo({ url: '/pages/settings/index?tab=binding' })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '退出后需重新微信登录，是否继续？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await commonApi.logout(false)
      } catch (e) {
        // 忽略：本地仍清理
      }
      resetSession()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}

void bindingReason
</script>

<style scoped>
.role {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.role:last-child {
  border-bottom: none;
}

.role.disabled {
  opacity: 0.55;
}

.role-name {
  font-size: 30rpx;
  font-weight: 500;
}

.role-desc {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: var(--muted);
}

.btn.gray {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 16rpx;
  background: #f0f1f4;
  color: var(--sub);
  font-size: 30rpx;
}
</style>
