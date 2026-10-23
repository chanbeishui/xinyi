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
      <view class="muted" style="margin-top: 12rpx">
        建议使用微信授权手机号：若该号码与公司在册员工账号一致，系统会自动识别员工身份，无需另行申请。
      </view>
      <!-- #ifdef MP-WEIXIN -->
      <button
        v-if="!isMock"
        class="btn ghost"
        style="margin-top: 20rpx"
        open-type="getPhoneNumber"
        @getphonenumber="onGetPhoneNumber"
      >
        微信授权手机号
      </button>
      <!-- #endif -->
      <template v-if="isMock">
        <view class="field" style="margin-top: 16rpx">
          <view class="field-label">联调手机号（后端 mock 模式）</view>
          <input v-model="wechatPhone" class="input" type="number" maxlength="11" placeholder="11 位手机号" />
        </view>
        <view class="btn ghost" style="margin-top: 20rpx" @tap="mockBindWechatPhone">模拟微信授权</view>
      </template>

      <view class="divider"></view>
      <view class="muted">无法获得微信授权时，可用短信验证码兜底绑定。</view>
      <view class="field" style="margin-top: 12rpx">
        <view class="field-label">手机号</view>
        <input v-model="phone" class="input" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 16rpx">
        <view class="btn gray small" @tap="sendSms">获取验证码</view>
        <input v-model="smsCode" class="input" type="number" maxlength="6" placeholder="验证码" style="flex: 1" />
      </view>
      <view class="btn ghost" style="margin-top: 20rpx" @tap="bindPhone">短信验证绑定</view>
    </view>

    <view class="card">
      <view class="card-title">员工身份</view>
      <view class="row-between">
        <text class="label">绑定状态</text>
        <text class="value">{{ profile.employeeBound ? (profile.employeeName || '已绑定') : '未绑定' }}</text>
      </view>
      <view v-if="profile.employeeBound" class="row-between">
        <text class="label">所属部门</text>
        <text class="value">{{ profile.employeeDeptName || '—' }}</text>
      </view>
      <view v-if="profile.employeeBound" class="row-between">
        <text class="label">已授权检测点</text>
        <text class="value">{{ profile.employeeStationCount || 0 }} 个</text>
      </view>
      <view class="muted" style="margin-top: 12rpx">
        {{ employeeHint }}
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
import { USE_MOCK_LOGIN } from '@/utils/config'

const profile = computed(() => session.profile || {})
const form = reactive({ nickname: '', serviceName: '' })
const phone = ref('')
const smsCode = ref('')
const wechatPhone = ref('')
const privacyReason = ref('')
const privacyCases = ref<any[]>([])
const tab = ref('')
const isMock = USE_MOCK_LOGIN

/** 员工身份说明：绑定与检测点授权都只在后台维护，小程序端不发起申请 */
const employeeHint = computed(() => {
  const p: any = profile.value
  if (!p.employeeBound) {
    return '未识别到员工身份。若你是在职员工，请确认公司人事登记的手机号与本微信授权手机号一致；如仍不识别，请联系公司管理员核对账号。'
  }
  if (!p.employeeStationCount) {
    return '已识别员工身份，但尚未获得检测点授权，暂时没有检测点工作台入口。请联系公司管理员在后台为你的账号配置检测点。'
  }
  return '员工身份由公司账号按手机号自动识别，检测点权限由公司管理员在后台配置。如需调整请联系管理员。'
})

onLoad((options) => {
  if (options && options.tab) tab.value = options.tab
})

onShow(async () => {
  await loadProfile()
  await loadPrivacy()
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
    const res: any = await commonApi.bindPhone(phone.value, smsCode.value)
    // 绑定关系变化时后端会换发令牌并返回完整会话视图
    if (res && res.accessToken) applySession(res)
    uni.showToast({ title: '绑定成功', icon: 'success' })
    loadProfile()
  } catch (e) {
    // 已提示
  }
}

/** 微信手机号授权：真实环境拿 code，联调环境直接用输入框里的手机号 */
async function applyWechatPhone(phoneCode: string) {
  try {
    const view: any = await commonApi.bindWechatPhone(phoneCode)
    applySession(view)
    uni.showToast({ title: '手机号已授权', icon: 'success' })
    loadProfile()
  } catch (e) {
    // 已提示
  }
}

function onGetPhoneNumber(e: any) {
  const detail = e && e.detail ? e.detail : {}
  if (!detail.code) {
    uni.showToast({ title: '已取消授权', icon: 'none' })
    return
  }
  applyWechatPhone(detail.code)
}

function mockBindWechatPhone() {
  if (!/^1\d{10}$/.test(wechatPhone.value)) {
    uni.showToast({ title: '请输入 11 位手机号', icon: 'none' })
    return
  }
  applyWechatPhone(wechatPhone.value)
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
