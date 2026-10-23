<template>
  <div class="app-container">
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
          <span>短信通道</span>
          <el-tag :type="data.enabled ? 'success' : 'warning'" size="small">
            {{ data.enabled ? '真实下发' : '联调模式' }}
          </el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="180px" style="max-width: 880px">
        <el-form-item label="启用真实下发">
          <el-switch v-model="form.enabled" />
          <div class="tip">
            关闭时小程序获取验证码会直接回显，不发送真实短信；短信仅作为微信手机号授权的兜底。
          </div>
        </el-form-item>

        <el-form-item label="短信供应商" prop="provider">
          <el-radio-group v-model="form.provider">
            <el-radio-button label="ALIYUN">阿里云</el-radio-button>
            <el-radio-button label="TENCENT">腾讯云</el-radio-button>
          </el-radio-group>
          <div class="tip">切换供应商后，下方凭据字段会随之变化，需重新填写。</div>
        </el-form-item>

        <el-divider content-position="left">签名与模板</el-divider>

        <el-form-item label="短信签名" prop="signName">
          <el-input v-model="form.signName" placeholder="例如：新意出行" clearable style="width: 360px" />
          <div class="tip">必须是供应商后台已审核通过的签名，2~20 字符。</div>
        </el-form-item>

        <el-form-item label="模板编号" prop="templateCode">
          <el-input
            v-model="form.templateCode"
            :placeholder="form.provider === 'TENCENT' ? '例如 1234567' : '例如 SMS_123456789'"
            clearable
            style="width: 360px"
          />
          <div class="tip">验证码模板，模板变量只需一个且值为验证码。</div>
        </el-form-item>

        <el-divider content-position="left">供应商凭据</el-divider>

        <template v-if="form.provider === 'ALIYUN'">
          <el-form-item label="AccessKeyId" prop="accessKeyId">
            <el-input v-model="form.accessKeyId" placeholder="LTAI..." clearable style="width: 360px" />
            <div class="tip">建议使用只授予短信发送权限的 RAM 子账号。</div>
          </el-form-item>
          <el-form-item label="AccessKeySecret" prop="accessKeySecret">
            <el-input
              v-model="form.accessKeySecret"
              :placeholder="secretPlaceholder('accessKeySecret', '粘贴后不可再查看')"
              show-password
              clearable
              style="width: 360px"
            />
            <el-button v-if="data.accessKeySecretConfigured" link type="danger" @click="clearSecret('accessKeySecret')">
              清除
            </el-button>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="SecretId" prop="secretId">
            <el-input v-model="form.secretId" placeholder="AKID..." clearable style="width: 360px" />
          </el-form-item>
          <el-form-item label="SecretKey" prop="secretKey">
            <el-input
              v-model="form.secretKey"
              :placeholder="secretPlaceholder('secretKey', '粘贴后不可再查看')"
              show-password
              clearable
              style="width: 360px"
            />
            <el-button v-if="data.secretKeyConfigured" link type="danger" @click="clearSecret('secretKey')">
              清除
            </el-button>
          </el-form-item>
          <el-form-item label="SdkAppId" prop="sdkAppId">
            <el-input v-model="form.sdkAppId" placeholder="例如 1400000000" clearable style="width: 360px" />
            <div class="tip">腾讯云控制台 → 短信 → 应用管理。</div>
          </el-form-item>
          <el-form-item label="地域" prop="region">
            <el-input v-model="form.region" placeholder="ap-guangzhou" style="width: 360px" />
          </el-form-item>
        </template>

        <el-divider content-position="left">频控</el-divider>

        <el-form-item label="验证码有效期（秒）" prop="codeExpireSeconds">
          <el-input-number v-model="form.codeExpireSeconds" :min="60" :max="1800" :step="30" />
          <div class="tip">60~1800 秒。</div>
        </el-form-item>

        <el-form-item label="重发间隔（秒）" prop="resendSeconds">
          <el-input-number v-model="form.resendSeconds" :min="10" :max="600" :step="10" />
          <div class="tip">同一手机号两次发送的最小间隔。</div>
        </el-form-item>

        <el-form-item label="单号日上限（条）" prop="dailyLimit">
          <el-input-number v-model="form.dailyLimit" :min="1" :max="50" />
          <div class="tip">同一手机号 24 小时内最多发送条数；同一账号另有 2 倍上限。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
          <el-button @click="load">重新读取</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt16">
      <template #header>连通性测试</template>
      <el-form :inline="true">
        <el-form-item label="测试手机号">
          <el-input v-model="testPhone" placeholder="11 位手机号" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button :loading="testing" :disabled="!data.enabled" @click="runTest">发送测试短信</el-button>
        </el-form-item>
      </el-form>
      <div v-if="!data.enabled" class="tip">未启用真实下发，测试按钮不可用。先打开上方开关并保存。</div>
      <el-alert v-if="testResult" class="mt8" type="success" :closable="false" show-icon>
        <template #title>{{ testResult.hint }}</template>
        <div>发送至 {{ testResult.phoneMasked }}，通道 {{ testResult.provider }}</div>
      </el-alert>
    </el-card>

    <el-card shadow="never" class="mt16">
      <template #header>最近发送记录</template>
      <el-table :data="data.recentLogs || []" border size="small">
        <el-table-column label="时间" prop="createdAt" width="200" />
        <el-table-column label="手机号" prop="phoneMasked" width="140" />
        <el-table-column label="场景" prop="scene" width="100" />
        <el-table-column label="通道" prop="provider" width="120" />
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OK' ? 'success' : row.status === 'BLOCKED' ? 'warning' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="错误码" prop="errorCode" width="180" />
        <el-table-column label="说明" prop="errorMessage" show-overflow-tooltip />
      </el-table>
      <div v-if="!(data.recentLogs || []).length" class="tip">暂无发送记录。</div>
    </el-card>
  </div>
</template>

<script setup name="RongliConfigSms" lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSmsConfig, saveSmsConfig, testSmsConfig } from '@/api/rongli'

const data = ref<any>({})
const formRef = ref()
const saving = ref(false)
const testing = ref(false)
const testPhone = ref('')
const testResult = ref<any>(null)
const cleared = reactive<Record<string, boolean>>({})

const form = reactive<any>({
  enabled: false,
  provider: 'ALIYUN',
  signName: '',
  templateCode: '',
  accessKeyId: '',
  accessKeySecret: '',
  secretId: '',
  secretKey: '',
  sdkAppId: '',
  region: 'ap-guangzhou',
  codeExpireSeconds: 300,
  resendSeconds: 60,
  dailyLimit: 10
})

const missing = computed<string[]>(() => data.value.missing || [])
const statusAlertType = computed(() => {
  if (!data.value.enabled) return 'warning'
  return (data.value.missing || []).length ? 'warning' : 'success'
})

const rules = {
  signName: [{ min: 2, max: 20, message: '签名需 2~20 字符', trigger: 'blur' }],
  sdkAppId: [{ pattern: /^\d{6,20}$/, message: 'SdkAppId 必须是数字', trigger: 'blur' }]
}

onMounted(load)

async function load() {
  data.value = await getSmsConfig()
  const d = data.value
  form.enabled = !!d.enabled
  form.provider = d.provider || 'ALIYUN'
  form.signName = d.signName || ''
  form.templateCode = d.templateCode || ''
  form.accessKeyId = d.accessKeyId || ''
  form.secretId = d.secretId || ''
  form.sdkAppId = d.sdkAppId || ''
  form.region = d.region || 'ap-guangzhou'
  form.codeExpireSeconds = d.codeExpireSeconds ?? 300
  form.resendSeconds = d.resendSeconds ?? 60
  form.dailyLimit = d.dailyLimit ?? 10
  form.accessKeySecret = ''
  form.secretKey = ''
  for (const key of Object.keys(cleared)) delete cleared[key]
  testResult.value = null
}

function secretPlaceholder(key: string, fallback: string) {
  if (cleared[key]) return '将清除该配置'
  const configured = data.value[`${key}Configured`]
  if (configured) return `已配置 ${data.value[`${key}Masked`] || '****'}，留空保持原值`
  return fallback
}

function clearSecret(key: string) {
  cleared[key] = true
  form[key] = ''
  ElMessage.info('保存后将清除该项')
}

function secretValue(key: string) {
  const value = String(form[key] ?? '').trim()
  if (value) return value
  if (cleared[key]) return ''
  return '__UNCHANGED__'
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    data.value = await saveSmsConfig({
      enabled: form.enabled,
      provider: form.provider,
      signName: form.signName,
      templateCode: form.templateCode,
      accessKeyId: form.accessKeyId,
      secretId: form.secretId,
      sdkAppId: form.sdkAppId,
      region: form.region,
      codeExpireSeconds: form.codeExpireSeconds,
      resendSeconds: form.resendSeconds,
      dailyLimit: form.dailyLimit,
      accessKeySecret: secretValue('accessKeySecret'),
      secretKey: secretValue('secretKey')
    })
    ElMessage.success('已保存并即时生效')
    await load()
  } finally {
    saving.value = false
  }
}

async function runTest() {
  if (!/^\d{11}$/.test(testPhone.value)) {
    ElMessage.warning('请输入 11 位手机号')
    return
  }
  testing.value = true
  try {
    testResult.value = await testSmsConfig(testPhone.value)
    ElMessage.success('已提交供应商')
    await load()
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt8 { margin-top: 8px; }
.mt16 { margin-top: 16px; }
.card-head { display: flex; align-items: center; gap: 12px; }
.tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 4px; }
.alert-body { line-height: 1.7; }
.missing { margin: 6px 0 0 18px; padding: 0; color: #e6a23c; }
.missing li { list-style: disc; }
</style>
