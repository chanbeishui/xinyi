<template>
  <view class="page">
    <view class="card">
      <view class="card-title">账号资料</view>
      <view class="field">
        <view class="field-label">昵称</view>
        <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
      </view>
      <view class="field">
        <view class="field-label">称呼（用于服务沟通）</view>
        <input v-model="form.serviceName" class="input" placeholder="例如 王先生" />
      </view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="saveProfile">保存资料</view>
    </view>

    <view class="card">
      <view class="card-title">手机号</view>
      <view class="row-between">
        <text class="label">绑定状态</text>
        <text class="value">{{ profile.phoneBound ? '已绑定' : '未绑定' }}</text>
      </view>
      <view class="field" style="margin-top: 12rpx">
        <view class="field-label">手机号</view>
        <input v-model="phone" class="input" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 16rpx">
        <view class="btn gray small" @tap="sendSms">获取验证码</view>
        <input v-model="smsCode" class="input" type="number" maxlength="6" placeholder="验证码" style="flex: 1" />
      </view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="bindPhone">绑定手机号</view>
    </view>

    <view class="card">
      <view class="card-title">员工账号绑定</view>
      <view class="muted">
        检测员工需使用公司下发的绑定凭证开通工作视角；代驾请到代驾认证入口提交资料。绑定只开通能力，不改变账号归属。
      </view>
      <view class="field" style="margin-top: 16rpx">
        <view class="field-label">绑定凭证</view>
        <input v-model="credential" class="input" placeholder="粘贴公司下发的凭证" />
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 20rpx">
        <view class="btn small" style="flex: 1" @tap="bindEmployee">立即绑定</view>
        <view class="btn ghost small" style="flex: 1" @tap="requestBinding">申请凭证</view>
      </view>
      <view v-if="bindingRequests.length" class="divider"></view>
      <view v-for="r in bindingRequests" :key="r.requestId" class="row-between">
        <text class="muted">{{ r.reason || '绑定申请' }}</text>
        <text class="tag gray">{{ r.status }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">隐私与数据</view>
      <view class="muted">可申请查阅、更正或删除个人信息，平台按合规要求处理并留痕。</view>
      <view class="field" style="margin-top: 16rpx">
        <view class="field-label">请求说明</view>
        <textarea v-model="privacyReason" class="textarea" placeholder="说明希望处理的个人信息范围" />
      </view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="submitPrivacy">提交隐私请求</view>
      <view v-if="privacyCases.length" class="divider"></view>
      <view v-for="c in privacyCases" :key="c.id" class="row-between">
        <text class="muted">{{ c.reason || '隐私请求' }}</text>
        <text class="tag gray">{{ c.status }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">其他</view>
      <view class="row-between" @tap="goRole">
        <text class="label">切换视角</text>
        <text class="muted">前往 ›</text>
      </view>
      <view class="row-between" @tap="goMessages">
        <text class="label">消息中心</text>
        <text class="muted">查看 ›</text>
      </view>
    </view>

    <view class="btn gray" @tap="logout">退出登录</view>
    <view class="footer-space"></view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { commonApi } from '@/api'
import { applySession, resetSession, session } from '@/store/session'

const profile = computed(() => session.profile || {})
const form = reactive({ nickname: '', serviceName: '' })
const phone = ref('')
const smsCode = ref('')
const credential = ref('')
const privacyReason = ref('')
const privacyCases = ref<any[]>([])
const bindingRequests = ref<any[]>([])
const tab = ref('')

onLoad((options) => {
  if (options && options.tab) tab.value = options.tab
})

onShow(async () => {
  await loadProfile()
  await loadPrivacy()
  await loadBindingRequests()
})

async function loadProfile() {
  try {
    const view: any = await commonApi.session()
    applySession(view)
    form.nickname = (view.profile && view.profile.nickname) || ''
    form.serviceName = (view.profile && view.profile.serviceName) || ''
  } catch (e) {
    // 已提示
  }
}

async function loadPrivacy() {
  try {
    privacyCases.value = ((await commonApi.privacyCases()) as any) || []
  } catch (e) {
    privacyCases.value = []
  }
}

async function loadBindingRequests() {
  try {
    bindingRequests.value = ((await commonApi.bindingRequests()) as any) || []
  } catch (e) {
    bindingRequests.value = []
  }
}

async function saveProfile() {
  try {
    await commonApi.patchProfile({ nickname: form.nickname, serviceName: form.serviceName })
    uni.showToast({ title: '已保存', icon: 'success' })
    loadProfile()
  } catch (e) {
    // 已提示
  }
}

async function sendSms() {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  try {
    const res: any = await commonApi.sendSms(phone.value)
    uni.showToast({
      title: res && res.devCode ? `联调验证码：${res.devCode}` : '验证码已发送',
      icon: 'none',
      duration: 3000
    })
  } catch (e) {
    // 已提示
  }
}

async function bindPhone() {
  if (!phone.value || !smsCode.value) {
    uni.showToast({ title: '请填写手机号与验证码', icon: 'none' })
    return
  }
  try {
    await commonApi.bindPhone(phone.value, smsCode.value)
    uni.showToast({ title: '绑定成功', icon: 'success' })
    loadProfile()
  } catch (e) {
    // 已提示
  }
}

async function bindEmployee() {
  if (!credential.value) {
    uni.showToast({ title: '请填写绑定凭证', icon: 'none' })
    return
  }
  try {
    await commonApi.bindEmployee(credential.value)
    credential.value = ''
    uni.showToast({ title: '绑定成功', icon: 'success' })
    loadProfile()
    loadBindingRequests()
  } catch (e) {
    // 已提示
  }
}

async function requestBinding() {
  try {
    await commonApi.bindingRequest('员工账号绑定申请')
    uni.showToast({ title: '申请已提交', icon: 'success' })
    loadBindingRequests()
  } catch (e) {
    // 已提示
  }
}

async function submitPrivacy() {
  if (!privacyReason.value) {
    uni.showToast({ title: '请填写请求说明', icon: 'none' })
    return
  }
  try {
    await commonApi.createPrivacy(privacyReason.value)
    privacyReason.value = ''
    uni.showToast({ title: '已提交', icon: 'success' })
    loadPrivacy()
  } catch (e) {
    // 已提示
  }
}

function goRole() {
  uni.navigateTo({ url: '/pages/role/index' })
}

function goMessages() {
  uni.navigateTo({ url: '/pages/messages/index' })
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

void tab
</script>
