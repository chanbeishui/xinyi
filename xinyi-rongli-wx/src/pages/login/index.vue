<template>
  <view class="login">
    <view class="brand">
      <view class="logo">荣利</view>
      <view class="title">机动车检测预约</view>
      <view class="subtitle">上门接车 · 代驾送检 · 结果回传</view>
    </view>

    <view class="panel">
      <view class="panel-title">微信登录</view>
      <view class="panel-desc">登录后可使用车主预约、代驾接单与检测点工作台。员工能力须由公司账号受控绑定后开通。</view>

      <view v-if="showDev" class="field">
        <view class="field-label">联调登录标识（后端开启 mock 时使用）</view>
        <input v-model="devCode" class="input" placeholder="例如 dev_owner_001" />
      </view>

      <view class="btn" :class="{ disabled: loading }" @tap="doLogin">
        {{ loading ? '登录中…' : '微信一键登录' }}
      </view>

      <view class="agreement">
        登录即表示同意
        <text class="link" @tap="showTerms = true">《服务协议》</text>
        与
        <text class="link" @tap="showPrivacy = true">《隐私政策》</text>
      </view>
    </view>

    <view class="tips">
      <view class="tips-title">账号说明</view>
      <view class="tips-item">· 车主：微信登录后即可预约，车辆与地址可随时维护</view>
      <view class="tips-item">· 代驾：需提交实名与驾驶证资料并审核通过后接单</view>
      <view class="tips-item">· 检测员工：需通过公司发放的绑定凭证开通检测点视角</view>
    </view>

    <view v-if="showTerms || showPrivacy" class="mask" @tap="closeDoc">
      <view class="doc" @tap.stop>
        <view class="doc-title">{{ showTerms ? '服务协议（摘要）' : '隐私政策（摘要）' }}</view>
        <scroll-view scroll-y class="doc-body">
          <view v-for="(p, i) in docParagraphs" :key="i" class="doc-p">{{ p }}</view>
        </scroll-view>
        <view class="btn ghost" @tap="closeDoc">我知道了</view>
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
const devCode = ref(MOCK_LOGIN_CODE)
const showTerms = ref(false)
const showPrivacy = ref(false)
const showDev = ref(USE_MOCK_LOGIN)

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
      resolve(devCode.value || MOCK_LOGIN_CODE)
      return
    }
    // #ifdef MP-WEIXIN
    uni.login({
      provider: 'weixin',
      success: (res) => resolve(res.code || devCode.value),
      fail: () => resolve(devCode.value)
    })
    // #endif
    // #ifndef MP-WEIXIN
    resolve(devCode.value)
    // #endif
  })
}

async function doLogin() {
  if (loading.value) return
  loading.value = true
  try {
    const code = await fetchWechatCode()
    const view: any = await commonApi.login({ code, deviceLabel: 'mini-program' })
    saveTokens(view.accessToken, view.refreshToken)
    applySession(view)
    uni.setStorageSync(STORAGE.lastRole, view.activeRole)
    uni.reLaunch({ url: '/pages/role/index' })
  } catch (e) {
    // 请求层已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding: calc(120rpx + env(safe-area-inset-top)) 40rpx 60rpx;
  background: linear-gradient(180deg, #eaf1ff 0%, #f5f6f8 42%);
}

.brand {
  padding: 20rpx 0 48rpx;
}

.logo {
  width: 104rpx;
  height: 104rpx;
  border-radius: 28rpx;
  background: var(--brand);
  color: #fff;
  font-size: 34rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 2rpx;
}

.title {
  margin-top: 28rpx;
  font-size: 44rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.subtitle {
  margin-top: 10rpx;
  color: var(--sub);
  font-size: 26rpx;
}

.panel {
  background: #fff;
  border-radius: 28rpx;
  padding: 36rpx 32rpx;
  box-shadow: 0 8rpx 40rpx rgba(31, 111, 235, 0.08);
}

.panel-title {
  font-size: 32rpx;
  font-weight: 600;
}

.panel-desc {
  margin-top: 12rpx;
  color: var(--sub);
  font-size: 25rpx;
  line-height: 1.6;
}

.panel .field {
  margin-top: 24rpx;
  border-bottom: none;
}

.panel .btn {
  margin-top: 32rpx;
}

.agreement {
  margin-top: 24rpx;
  font-size: 23rpx;
  color: var(--muted);
  text-align: center;
}

.link {
  color: var(--brand);
}

.tips {
  margin-top: 40rpx;
  padding: 0 8rpx;
}

.tips-title {
  font-size: 26rpx;
  color: var(--sub);
  margin-bottom: 12rpx;
}

.tips-item {
  font-size: 24rpx;
  color: var(--muted);
  line-height: 1.9;
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
}

.doc {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
}

.doc-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.doc-body {
  max-height: 620rpx;
  margin-bottom: 24rpx;
}

.doc-p {
  font-size: 25rpx;
  color: var(--sub);
  line-height: 1.8;
  margin-bottom: 14rpx;
}
</style>
