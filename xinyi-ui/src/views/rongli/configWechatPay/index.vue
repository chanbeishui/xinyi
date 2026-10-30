<template>
  <RongliPage title="微信支付配置" subtitle="商户参数加密保存；通道切换在配置文件完成。">
    <el-alert
      :type="statusAlertType"
      :closable="false"
      show-icon
      class="mb8"
      :title="data.statusText || '正在读取配置'"
    >
      <template #default>
        <div class="alert-body">
          <span>{{ data.hint }}</span>
          <ul v-if="missing.length" class="missing">
            <li v-for="item in missing" :key="item">{{ item }}</li>
          </ul>
        </div>
      </template>
    </el-alert>

    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>微信支付商户参数</span>
          <el-tag :type="channelTagType" size="small">{{ data.channel === 'WECHAT' ? '真实通道' : '联调通道' }}</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="180px" style="max-width: 880px">
        <el-form-item label="支付通道" prop="channel">
          <el-radio-group v-model="form.channel">
            <el-radio-button label="MOCK">联调通道</el-radio-button>
            <el-radio-button label="WECHAT">真实微信支付</el-radio-button>
          </el-radio-group>
          <div class="tip">
            联调通道不产生真实收款，仅用于内部验证；切换为真实通道后必须补全下方所有必填项才能保存。
          </div>
        </el-form-item>

        <el-form-item label="配置模式" prop="mode">
          <el-radio-group v-model="form.mode">
            <el-radio-button label="NORMAL">正式</el-radio-button>
            <el-radio-button label="SANDBOX">沙箱（仅验证签名）</el-radio-button>
          </el-radio-group>
          <div class="tip">沙箱模式只用于验证签名链路，不会真实下单。</div>
        </el-form-item>

        <el-divider content-position="left">小程序与商户身份</el-divider>

        <el-form-item label="小程序 AppID" prop="appId">
          <el-input v-model="form.appId" placeholder="wx1234567890abcdef" clearable style="width: 360px" />
          <div class="tip">微信公众平台 → 开发管理 → 开发设置。必须与商户号主体一致。</div>
        </el-form-item>

        <el-form-item label="商户号" prop="mchId">
          <el-input v-model="form.mchId" placeholder="1900000109" clearable style="width: 360px" />
          <div class="tip">微信支付商户平台 → 账户中心 → 商户信息。</div>
        </el-form-item>

        <el-divider content-position="left">密钥与证书</el-divider>

        <el-form-item label="APIv3 密钥" prop="apiV3Key">
          <el-input
            v-model="form.apiV3Key"
            :placeholder="secretPlaceholder('apiV3Key', '32 位字母数字')"
            show-password
            clearable
            style="width: 360px"
          />
          <el-button v-if="data.apiV3KeyConfigured" link type="danger" @click="clearSecret('apiV3Key')">
            清除
          </el-button>
          <div class="tip">商户平台 → 账户中心 → API安全 → 设置 APIv3 密钥。用于回调报文解密，保存后加密存储。</div>
        </el-form-item>

        <el-form-item label="商户证书序列号" prop="serialNo">
          <el-input v-model="form.serialNo" placeholder="十六进制序列号" clearable style="width: 360px" />
          <div class="tip">商户平台 → API安全 → 申请 API 证书，下载后可查看序列号。</div>
        </el-form-item>

        <el-form-item label="商户私钥" prop="privateKey">
          <el-input
            v-model="form.privateKey"
            type="textarea"
            :rows="5"
            :placeholder="secretPlaceholder('privateKey', '粘贴 apiclient_key.pem 的全部内容，含 BEGIN/END 行')"
          />
          <el-button v-if="data.privateKeyConfigured" link type="danger" @click="clearSecret('privateKey')">
            清除
          </el-button>
          <div class="tip">只接受 PKCS#8 格式（apiclient_key.pem），保存后加密存储，页面不再回显明文。</div>
        </el-form-item>

        <el-form-item label="平台证书序列号" prop="platformSerialNo">
          <el-input v-model="form.platformSerialNo" placeholder="选填" clearable style="width: 360px" />
        </el-form-item>

        <el-form-item label="平台证书" prop="platformCert">
          <el-input
            v-model="form.platformCert"
            type="textarea"
            :rows="4"
            :placeholder="secretPlaceholder('platformCert', '选填，粘贴微信支付平台证书 PEM')"
          />
          <el-button v-if="data.platformCertConfigured" link type="danger" @click="clearSecret('platformCert')">
            清除
          </el-button>
          <div class="tip">开启回调验签时必填。证书与商户号不匹配会导致所有回调被拒绝。</div>
        </el-form-item>

        <el-form-item label="回调验签">
          <el-switch v-model="form.verifyNotify" active-text="强制验签" inactive-text="不验签" />
          <div class="tip warn">关闭验签只允许在联调环境使用；生产环境关闭等于允许伪造支付成功回调。</div>
        </el-form-item>

        <el-divider content-position="left">回调与展示</el-divider>

        <el-form-item label="支付回调地址" prop="notifyUrl">
          <el-input v-model="form.notifyUrl" placeholder="https://your-domain/api/notify/wechat/pay" style="width: 480px" />
          <div class="tip">必须公网可达的 https；微信服务器会 POST 到此地址。不能填 localhost。</div>
        </el-form-item>

        <el-form-item label="退款回调地址" prop="refundNotifyUrl">
          <el-input
            v-model="form.refundNotifyUrl"
            :placeholder="data.effectiveRefundNotifyUrl || '留空自动派生'"
            style="width: 480px"
          />
          <div class="tip">留空时自动取支付回调地址并把末段替换为 /refund。</div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" maxlength="127" show-word-limit style="width: 480px" />
          <div class="tip">用户在支付弹窗中看到的说明，1~127 字符。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
          <el-button @click="load">重新读取</el-button>
          <el-button :loading="checking" @click="selfCheck">本地自检</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="checkResult" shadow="never" class="mt16">
      <template #header>自检结果</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="私钥可解析">
          <el-tag :type="checkResult.privateKeyParsed ? 'success' : 'danger'">
            {{ checkResult.privateKeyParsed ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签名可产出">
          <el-tag :type="checkResult.signatureProduced ? 'success' : 'danger'">
            {{ checkResult.signatureProduced ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="平台证书可解析">
          <el-tag :type="checkResult.platformCertParsed ? 'success' : 'info'">
            {{ checkResult.platformCertParsed ? '是' : '未配置或不可解析' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="APIv3 密钥长度">{{ checkResult.apiV3KeyLength }}</el-descriptions-item>
        <el-descriptions-item label="可真实收款">
          <el-tag :type="checkResult.canCharge ? 'success' : 'warning'">{{ checkResult.canCharge ? '可以' : '不可以' }}</el-tag>
          <span v-if="checkResult.reason" class="tip"> {{ checkResult.reason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="结论">{{ checkResult.hint }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </RongliPage>
</template>

<script setup name="RongliConfigWechatPay" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPayConfig, savePayConfig, selfCheckPayConfig } from '@/api/rongli'

const data = ref<any>({})
const formRef = ref()
const saving = ref(false)
const checking = ref(false)
const checkResult = ref<any>(null)
const cleared = reactive<Record<string, boolean>>({})

const form = reactive<any>({
  channel: 'MOCK',
  mode: 'NORMAL',
  appId: '',
  mchId: '',
  apiV3Key: '',
  serialNo: '',
  privateKey: '',
  platformSerialNo: '',
  platformCert: '',
  notifyUrl: '',
  refundNotifyUrl: '',
  description: '机动车检测代驾预约',
  verifyNotify: true
})

const missing = computed<string[]>(() => data.value.missing || [])
const statusAlertType = computed(() => {
  switch (data.value.status) {
    case 'READY':
      return 'success'
    case 'INVALID':
      return 'error'
    case 'INCOMPLETE':
      return 'warning'
    default:
      return 'info'
  }
})
const channelTagType = computed(() => (data.value.channel === 'WECHAT' ? 'success' : 'warning'))

// 与后端 PaySettings 的规则保持一致，避免前端放行、后端拒绝
const rules = {
  appId: [{ pattern: /^wx[0-9a-fA-F]{16}$/, message: 'AppID 必须是 wx 开头 18 位', trigger: 'blur' }],
  mchId: [{ pattern: /^\d{8,12}$/, message: '商户号必须是 8~12 位数字', trigger: 'blur' }],
  apiV3Key: [{ pattern: /^[A-Za-z0-9]{32}$/, message: 'APIv3 密钥必须是 32 位字母数字', trigger: 'blur' }],
  serialNo: [{ pattern: /^[0-9A-Fa-f]{16,64}$/, message: '证书序列号必须是十六进制', trigger: 'blur' }],
  platformSerialNo: [{ pattern: /^[0-9A-Fa-f]{16,64}$/, message: '序列号必须是十六进制', trigger: 'blur' }],
  notifyUrl: [{ pattern: /^https:\/\/[^\s]+$/, message: '必须是 https 开头的完整地址', trigger: 'blur' }],
  refundNotifyUrl: [{ pattern: /^https:\/\/[^\s]+$/, message: '必须是 https 开头的完整地址', trigger: 'blur' }],
  description: [{ max: 127, message: '最多 127 个字符', trigger: 'blur' }]
}

onMounted(load)

async function load() {
  data.value = await getPayConfig()
  const d = data.value
  form.channel = d.channel || 'MOCK'
  form.mode = d.mode || 'NORMAL'
  form.appId = d.appId || ''
  form.mchId = d.mchId || ''
  form.serialNo = d.serialNo || ''
  form.platformSerialNo = d.platformSerialNo || ''
  form.notifyUrl = d.notifyUrl || ''
  form.refundNotifyUrl = d.refundNotifyUrl || ''
  form.description = d.description || '机动车检测代驾预约'
  form.verifyNotify = d.verifyNotify !== false
  // 敏感项永不回填明文：留空提交时由后端保持原值
  form.apiV3Key = ''
  form.privateKey = ''
  form.platformCert = ''
  for (const key of Object.keys(cleared)) delete cleared[key]
  checkResult.value = null
}

function secretPlaceholder(key: string, fallback: string) {
  if (cleared[key]) return '将清除该配置'
  const configured = data.value[`${key}Configured`]
  if (configured) {
    const masked = data.value[`${key}Masked`] || '****'
    return `已配置 ${masked}，留空保持原值`
  }
  return fallback
}

function clearSecret(key: string) {
  cleared[key] = true
  form[key] = ''
  ElMessage.info('保存后将清除该项，可重新填写覆盖')
}

async function save() {
  if (form.channel === 'WECHAT' && !form.verifyNotify) {
    ElMessage.warning('已关闭回调验签：仅联调环境允许，请确认风险')
  }
  await formRef.value.validate()
  const payload: Record<string, any> = {
    channel: form.channel,
    mode: form.mode,
    appId: form.appId,
    mchId: form.mchId,
    serialNo: form.serialNo,
    platformSerialNo: form.platformSerialNo,
    notifyUrl: form.notifyUrl,
    refundNotifyUrl: form.refundNotifyUrl,
    description: form.description,
    verifyNotify: form.verifyNotify
  }
  // 敏感项：未改动传 __UNCHANGED__ 保持原值，点了清除传空串
  payload.apiV3Key = secretValue('apiV3Key')
  payload.privateKey = secretValue('privateKey')
  payload.platformCert = secretValue('platformCert')

  saving.value = true
  try {
    data.value = await savePayConfig(payload)
    ElMessage.success('已保存并即时生效')
    await load()
  } finally {
    saving.value = false
  }
}

function secretValue(key: string) {
  const value = String(form[key] ?? '').trim()
  if (value) return value
  if (cleared[key]) return ''
  return '__UNCHANGED__'
}

async function selfCheck() {
  checking.value = true
  try {
    checkResult.value = await selfCheckPayConfig()
    if (checkResult.value.passed) {
      ElMessage.success('自检通过')
    } else {
      ElMessage.warning('自检未通过，请看下方结论')
    }
  } finally {
    checking.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; }
.card-head { display: flex; align-items: center; gap: 12px; }
.tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 4px; }
.tip.warn { color: #e6a23c; }
.alert-body { line-height: 1.7; }
.missing { margin: 6px 0 0 18px; padding: 0; color: #e6a23c; }
.missing li { list-style: disc; }
</style>
