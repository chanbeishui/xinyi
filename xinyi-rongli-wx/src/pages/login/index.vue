<template>
  <view class="login">
    <view class="brand">
      <view class="brand__rule"></view>
      <view class="brand__top">
        <view class="logo">荣利</view>
        <view class="brand__meta">
          <text class="brand__meta-line">RONGLI · 机动车检测</text>
          <text class="brand__meta-line">广东 茂名</text>
        </view>
      </view>
      <view class="brand__title">上门接车，送检到站</view>
      <view class="brand__desc">代驾接车 · 到站检测 · 结果回传，一张工单走完全程</view>
    </view>

    <view class="panel">
      <view class="panel__head">
        <text class="panel__title">微信登录</text>
        <text class="panel__flag">身份自动识别</text>
      </view>
      <view class="panel__desc">
        授权手机号即可预约；手机号与公司在册账号一致时自动识别员工身份。新账号默认为车主，代驾能力需另行申请。
      </view>

      <view v-if="showDev" class="field">
        <view class="field-label">联调手机号（后端 mock 模式，选填）</view>
        <input v-model="devPhone" class="input" type="number" maxlength="11" placeholder="例如 15888888888" />
      </view>

      <!-- 真实环境：手机号由微信校验，服务端用 code 换取，避免前端伪造号码 -->
      <!-- #ifdef MP-WEIXIN -->
      <button
        v-if="!isMock"
        class="btn wx-btn"
        :class="{ disabled: loading }"
        hover-class="btn-hover"
        open-type="getPhoneNumber"
        @getphonenumber="onGetPhoneNumber"
      >
        {{ loading ? '登录中…' : '微信手机号一键登录' }}
      </button>
      <!-- #endif -->

      <!-- 联调环境：getPhoneNumber 不可用，用输入框模拟同一条链路 -->
      <view v-if="isMock" class="btn" :class="{ disabled: loading }" hover-class="btn-hover" @tap="mockLogin">
        {{ loading ? '登录中…' : '微信手机号一键登录（联调）' }}
      </view>

      <view class="btn ghost" :class="{ disabled: loading }" hover-class="btn-hover" @tap="loginWithoutPhone">
        暂不授权手机号，以车主身份进入
      </view>

      <view class="agreement">
        登录即表示同意
        <text class="link" @tap="showTerms = true">《服务协议》</text>
        与
        <text class="link" @tap="showPrivacy = true">《隐私政策》</text>
      </view>
    </view>

    <view class="section-head">
      <text class="section-head__title">进入后的身份</text>
      <text class="section-head__meta">三种视角</text>
    </view>

    <view class="list roles">
      <view class="role">
        <view class="role__name">车主</view>
        <view class="role__desc">微信登录即可预约，车辆与接车地址随时维护</view>
      </view>
      <view class="role">
        <view class="role__name">代驾</view>
        <view class="role__desc">提交实名与驾驶证资料，审核通过后听单接车</view>
      </view>
      <view class="role">
        <view class="role__name">检测员工</view>
        <view class="role__desc">公司账号登记的手机号与微信一致时自动识别，检测点权限由后台配置</view>
      </view>
    </view>

    <view v-if="showTerms || showPrivacy" class="modal" @tap="closeDoc">
      <view class="doc" @tap.stop>
        <view class="sheet__head">
          <text class="sheet__title">{{ showTerms ? '服务协议（摘要）' : '隐私政策（摘要）' }}</text>
          <text class="sheet__close" @tap="closeDoc">关闭</text>
        </view>
        <scroll-view scroll-y class="doc-body">
          <view v-for="(p, i) in docParagraphs" :key="i" class="doc-p">{{ p }}</view>
        </scroll-view>
        <view class="btn ghost small doc-btn" hover-class="btn-hover" @tap="closeDoc">我知道了</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { commonApi } from '@/api'
import { MOCK_LOGIN_CODE, STORAGE, USE_MOCK_LOGIN } from '@/utils/config'
import { saveTokens } from '@/utils/request'
import { applySession } from '@/store/session'

const loading = ref(false)
const devPhone = ref('')
const showTerms = ref(false)
const showPrivacy = ref(false)
const showDev = USE_MOCK_LOGIN
const isMock = USE_MOCK_LOGIN

const docParagraphs = computed(() => {
  if (showTerms.value) {
    return [
      '一、服务内容：平台提供机动车检测预约、代驾接送车、检测结果回传与售后处理服务。',
      '二、预约与支付：预约需在支付时限内完成支付，超时订单将自动关闭并释放时段名额。',
      '三、取消与退款：取消需经审核，退款金额以实际服务进度与审核结论为准。',
      '四、交接与留证：车辆交接通过 4 位交接码核验，双方需完成车况照片与里程留证。',
      '五、争议处理：对服务结果有异议可在订单完成后发起争议，平台按留证材料处理。'
    ]
  }
  return [
    '一、我们收集的信息：微信标识、手机号、车辆信息、上门地址、服务过程留证照片与里程。',
    '二、使用目的：用于预约履约、代驾派单、检测送检、结果回传与售后争议处理。',
    '三、最小必要：联系方式在履约节点按需开放，非履约方仅可见脱敏信息。',
    '四、保存期限：按业务与合规要求保存，超期后删除或匿名化。',
    '五、您的权利：可申请查阅、更正、删除个人信息，可在设置中发起隐私请求。'
  ]
})

function closeDoc() {
  showTerms.value = false
  showPrivacy.value = false
}

function fetchWechatCode(): Promise<string> {
  return new Promise((resolve) => {
    // 演示/联调：后端把 code 当 openId，必须用固定标识，否则每次登录都会新建账号
    if (USE_MOCK_LOGIN) {
      resolve(MOCK_LOGIN_CODE)
      return
    }
    // #ifdef MP-WEIXIN
    uni.login({
      provider: 'weixin',
      success: (res) => resolve(res.code || ''),
      fail: () => resolve('')
    })
    // #endif
    // #ifndef MP-WEIXIN
    resolve('')
    // #endif
  })
}

/**
 * 统一登录入口。
 *
 * @param phoneCode 微信 getPhoneNumber 回调的 code；不传则只做身份登录，仍为车主身份
 */
async function doLogin(phoneCode?: string) {
  if (loading.value) return
  loading.value = true
  try {
    const code = await fetchWechatCode()
    if (!code) {
      uni.showToast({ title: '微信登录失败，请重试', icon: 'none' })
      return
    }
    const payload: any = { code, deviceLabel: 'mini-program' }
    if (phoneCode) payload.phoneCode = phoneCode
    const view: any = await commonApi.login(payload)
    saveTokens(view.accessToken, view.refreshToken)
    applySession(view)
    uni.setStorageSync(STORAGE.lastRole, view.activeRole)
    uni.reLaunch({ url: '/pages/role/index' })
  } catch (e: any) {
    // 原来这里是空 catch，只靠请求层的一条 toast；toast 一闪而过，现象就是"点了没反应"。
    // 登录是入口，失败必须让人看得见原因，否则只能靠猜域名、证书还是网络。
    const message = (e && (e.message || e.errMsg)) || '登录失败，请稍后重试'
    console.error('[login] 失败', message)
    uni.showModal({
      title: '登录失败',
      content: message,
      showCancel: false,
      confirmText: '知道了'
    })
  } finally {
    loading.value = false
  }
}

/** 真实环境：微信手机号授权回调 */
function onGetPhoneNumber(e: any) {
  const detail = e && e.detail ? e.detail : {}
  if (!detail.code) {
    // 用户拒绝或基础库不支持：退回无手机号登录，不阻断进入
    uni.showToast({ title: '未授权手机号，将以车主身份进入', icon: 'none' })
    doLogin()
    return
  }
  doLogin(detail.code)
}

/** 联调环境：输入框里的手机号直接当作授权 code 传给后端（mock 模式下后端按手机号解析） */
function mockLogin() {
  const phone = devPhone.value.trim()
  if (phone && !/^1\d{10}$/.test(phone)) {
    uni.showToast({ title: '请输入 11 位手机号', icon: 'none' })
    return
  }
  doLogin(phone || undefined)
}

function loginWithoutPhone() {
  doLogin()
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding-bottom: calc(60rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  background: var(--bg);
}

/* 墨色招牌：半幅抬头，下沿压在白卡上 */
.brand {
  position: relative;
  overflow: hidden;
  padding: 120rpx 40rpx 96rpx;
  padding-top: calc(120rpx + constant(safe-area-inset-top));
  padding-top: calc(120rpx + env(safe-area-inset-top));
  background: #0e1726;
  color: #fff;
}

/* 右侧细竖纹，像工单纸边的印刷线 */
.brand__rule {
  position: absolute;
  top: 0;
  right: 40rpx;
  width: 1rpx;
  height: 100%;
  background: rgba(255, 255, 255, 0.08);
}

.brand__top {
  display: flex;
  align-items: center;
}

.logo {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.24);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand__meta {
  margin-left: 20rpx;
}

.brand__meta-line {
  display: block;
  font-size: 21rpx;
  letter-spacing: 1.6rpx;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.7;
}

.brand__title {
  margin-top: 44rpx;
  font-size: 48rpx;
  line-height: 1.3;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.brand__desc {
  margin-top: 14rpx;
  font-size: 25rpx;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.62);
}

/* 白卡上提，压住招牌下沿：纸压墨，是这套视觉的接头 */
.panel {
  position: relative;
  margin: -64rpx 24rpx 0;
  padding: 30rpx 26rpx 26rpx;
  background: #fff;
  border: 1rpx solid var(--line);
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(14, 23, 38, 0.06);
}

/* 小节抬头与卡片左边距对齐（全局默认是 4rpx） */
.login .section-head {
  margin: 32rpx 28rpx 16rpx;
}

.panel__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.panel__title {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 0.4rpx;
}

.panel__flag {
  font-size: 21rpx;
  letter-spacing: 1rpx;
  color: var(--ink-3);
}

.panel__desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--ink-3);
}

.panel .field {
  margin-top: 22rpx;
  border-bottom: none;
  padding: 0;
}

.panel .btn {
  margin-top: 22rpx;
}

/* 原生 button 用于 getPhoneNumber 时需抹掉默认样式，保持与 .btn 一致 */
.wx-btn {
  width: 100%;
  margin: 22rpx 0 0;
  padding: 0;
  line-height: 1;
  border: none;
  overflow: visible;
}

.wx-btn::after {
  border: none;
}

.agreement {
  margin-top: 22rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: var(--ink-3);
  text-align: center;
}

.link {
  color: var(--brand);
}

.roles {
  margin: 0 24rpx;
}

.role {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.role:last-child {
  border-bottom: none;
}

.role__name {
  font-size: 26rpx;
  font-weight: 600;
  letter-spacing: 0.4rpx;
}

.role__desc {
  margin-top: 6rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: var(--ink-3);
}

/* 文档弹窗居中：不用全局 .mask（它是底部对齐的） */
.modal {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx 40rpx;
  background: rgba(14, 23, 38, 0.45);
}

.doc {
  width: 100%;
  max-height: 76vh;
  padding: 28rpx 26rpx 24rpx;
  background: #fff;
  border-radius: 20rpx;
}

.doc-body {
  max-height: 52vh;
  margin: 18rpx 0;
}

.doc-p {
  font-size: 24rpx;
  line-height: 1.8;
  color: var(--ink-2);
  margin-bottom: 14rpx;
}

.doc-btn {
  width: 100%;
}
</style>
