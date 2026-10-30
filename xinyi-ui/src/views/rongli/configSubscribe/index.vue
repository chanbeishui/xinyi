<template>
  <RongliPage title="订阅消息配置" subtitle="订阅模板与开关；未订阅不等于可无限重试。">
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
          <ul v-if="problems.length" class="missing">
            <li v-for="item in problems" :key="item">{{ item }}</li>
          </ul>
        </div>
      </template>
    </el-alert>

    <el-alert
      v-if="data.mockWechat"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
      title="服务端当前为联调模式（rongli.wechat.mock=true）：保存后不会真实下发订阅消息，把该开关设为 false 并重启后生效。"
    />

    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>微信订阅消息</span>
          <el-tag :type="data.enabled ? 'success' : 'info'" size="small">
            {{ data.enabled ? '已启用' : '已关闭' }}
          </el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="180px" style="max-width: 900px">
        <el-form-item label="启用订阅消息">
          <el-switch v-model="form.enabled" />
          <div class="tip">关闭后所有通知只发站内信；开启时至少要配置一个场景的模板 ID。</div>
        </el-form-item>

        <el-divider content-position="left">模板 ID</el-divider>

        <el-form-item
          v-for="item in templateFields"
          :key="item.key"
          :label="item.label"
          :prop="item.key"
        >
          <el-input v-model="form[item.key]" :placeholder="item.hint" clearable style="width: 460px" />
          <el-tag v-if="form[item.key]" type="success" size="small" class="ml8">已配置</el-tag>
          <el-tag v-else type="info" size="small" class="ml8">只发站内信</el-tag>
        </el-form-item>

        <el-divider content-position="left">跳转与字段</el-divider>

        <el-form-item label="点击跳转页面" prop="page">
          <el-input v-model="form.page" placeholder="pages/index/index" style="width: 460px" />
          <div class="tip">不要以 / 开头，且页面必须已在小程序内注册。</div>
        </el-form-item>

        <el-form-item label="运行环境" prop="miniprogramState">
          <el-radio-group v-model="form.miniprogramState">
            <el-radio-button label="developer">开发版</el-radio-button>
            <el-radio-button label="trial">体验版</el-radio-button>
            <el-radio-button label="formal">正式版</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="字段映射" prop="fields">
          <el-input
            v-model="form.fields"
            type="textarea"
            :rows="4"
            placeholder='{"thing1":"content","time2":"now"}'
          />
          <div class="tip">
            JSON：模板字段名 → 取值来源。来源只能是 content（通知摘要）、now（当前时间）、scene（场景码）。
          </div>
          <div v-if="fieldsError" class="tip err">{{ fieldsError }}</div>
        </el-form-item>

        <el-divider content-position="left">小程序凭据（登录与订阅消息共用）</el-divider>

        <el-form-item label="小程序 AppID" prop="miniAppId">
          <el-input v-model="form.miniAppId" placeholder="wx1234567890abcdef" clearable style="width: 460px" />
          <div class="tip">留空时沿用服务端 application.yml 的 rongli.wechat.appId。</div>
        </el-form-item>

        <el-form-item label="小程序 AppSecret" prop="miniAppSecret">
          <el-input
            v-model="form.miniAppSecret"
            :placeholder="secretPlaceholder"
            show-password
            clearable
            style="width: 460px"
          />
          <el-button v-if="data.miniAppSecretConfigured" link type="danger" @click="clearSecret">清除</el-button>
          <div class="tip">与手机号授权登录共用同一套凭据，保存后加密存储。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
          <el-button @click="load">重新读取</el-button>
          <el-button :loading="checking" @click="selfCheck">连通性自检</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="checkResult" shadow="never" class="mt16">
      <template #header>自检结果</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="结论">
          <el-tag :type="checkResult.passed ? 'success' : 'danger'">{{ checkResult.passed ? '通过' : '未通过' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="AppID">{{ checkResult.appId || '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="AppSecret">
          <el-tag :type="checkResult.secretConfigured ? 'success' : 'danger'">
            {{ checkResult.secretConfigured ? '已配置' : '未配置' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已配置场景">
          {{ (checkResult.configuredScenes || []).join('、') || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="说明" :span="2">{{ checkResult.hint }}</el-descriptions-item>
        <el-descriptions-item v-if="checkResult.error" label="错误" :span="2">{{ checkResult.error }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="mt16">
      <template #header>场景配置状态</template>
      <el-table :data="data.scenes || []" border size="small">
        <el-table-column label="场景" prop="label" width="200" />
        <el-table-column label="场景码" prop="scene" width="200" />
        <el-table-column label="模板 ID" prop="templateId" show-overflow-tooltip />
        <el-table-column label="状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configured ? 'success' : 'info'" size="small">
              {{ row.configured ? '会下发订阅消息' : '仅站内信' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </RongliPage>
</template>

<script setup name="RongliConfigSubscribe" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSubscribeConfig, saveSubscribeConfig, selfCheckSubscribeConfig } from '@/api/rongli'

const data = ref<any>({})
const formRef = ref()
const saving = ref(false)
const checking = ref(false)
const checkResult = ref<any>(null)
const secretCleared = ref(false)

const templateFields = [
  { key: 'dispatchInvite', label: '派单邀约模板 ID', hint: '代驾收到新订单邀约时推送' },
  { key: 'assignmentAccepted', label: '接单成功模板 ID', hint: '接单后通知车主' },
  { key: 'resultReady', label: '结果可查看模板 ID', hint: '检测结果登记后通知车主' },
  { key: 'refundDone', label: '退款完成模板 ID', hint: '退款到账后通知车主' }
]

const form = reactive<any>({
  enabled: true,
  dispatchInvite: '',
  assignmentAccepted: '',
  resultReady: '',
  refundDone: '',
  page: 'pages/index/index',
  miniprogramState: 'formal',
  fields: '{"thing1":"content","time2":"now"}',
  miniAppId: '',
  miniAppSecret: ''
})

const problems = computed<string[]>(() => data.value.problems || [])
const statusAlertType = computed(() => {
  if (!data.value.enabled) return 'info'
  return (data.value.problems || []).length ? 'warning' : 'success'
})

const templatePattern = /^[A-Za-z0-9_-]{10,64}$/

const rules = {
  dispatchInvite: [{ pattern: templatePattern, message: '模板 ID 为 10~64 位字母数字下划线连字符', trigger: 'blur' }],
  assignmentAccepted: [{ pattern: templatePattern, message: '模板 ID 为 10~64 位字母数字下划线连字符', trigger: 'blur' }],
  resultReady: [{ pattern: templatePattern, message: '模板 ID 为 10~64 位字母数字下划线连字符', trigger: 'blur' }],
  refundDone: [{ pattern: templatePattern, message: '模板 ID 为 10~64 位字母数字下划线连字符', trigger: 'blur' }],
  page: [{ pattern: /^[^\s/][^\s]*$/, message: '不要以 / 开头或含空格', trigger: 'blur' }],
  miniAppId: [{ pattern: /^wx[0-9a-fA-F]{16}$/, message: 'AppID 必须是 wx 开头 18 位', trigger: 'blur' }]
}

const fieldsError = computed(() => {
  const raw = String(form.fields || '').trim()
  if (!raw) return '不能为空，至少配置 {thing1: content}'
  try {
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return '必须是 JSON 对象'
    const keys = Object.keys(parsed)
    if (!keys.length) return '至少配置一个字段'
    for (const key of keys) {
      if (!/^[a-z_]+\d{1,2}$/.test(key)) return `字段名 ${key} 不合法，应形如 thing1 / time2`
      if (!['content', 'now', 'scene'].includes(parsed[key])) {
        return `字段 ${key} 的取值来源必须是 content / now / scene`
      }
    }
    return ''
  } catch (e) {
    return '不是合法 JSON'
  }
})

const secretPlaceholder = computed(() => {
  if (secretCleared.value) return '将清除该配置'
  if (data.value.miniAppSecretConfigured) {
    return `已配置 ${data.value.miniAppSecretMasked || '****'}，留空保持原值`
  }
  return '粘贴后不可再查看'
})

onMounted(load)

async function load() {
  data.value = await getSubscribeConfig()
  const d = data.value
  form.enabled = d.enabled !== false
  form.dispatchInvite = d.dispatchInvite || ''
  form.assignmentAccepted = d.assignmentAccepted || ''
  form.resultReady = d.resultReady || ''
  form.refundDone = d.refundDone || ''
  form.page = d.page || 'pages/index/index'
  form.miniprogramState = d.miniprogramState || 'formal'
  form.fields = d.fields || '{"thing1":"content","time2":"now"}'
  form.miniAppId = d.miniAppId || ''
  form.miniAppSecret = ''
  secretCleared.value = false
  checkResult.value = null
}

function clearSecret() {
  secretCleared.value = true
  form.miniAppSecret = ''
  ElMessage.info('保存后将清除 AppSecret，登录与订阅消息都会不可用')
}

async function save() {
  if (fieldsError.value) {
    ElMessage.error(`字段映射有误：${fieldsError.value}`)
    return
  }
  await formRef.value.validate()
  const secret = String(form.miniAppSecret || '').trim()
  const payload = {
    enabled: form.enabled,
    dispatchInvite: form.dispatchInvite,
    assignmentAccepted: form.assignmentAccepted,
    resultReady: form.resultReady,
    refundDone: form.refundDone,
    page: form.page,
    miniprogramState: form.miniprogramState,
    fields: form.fields,
    miniAppId: form.miniAppId,
    miniAppSecret: secret ? secret : secretCleared.value ? '' : '__UNCHANGED__'
  }
  saving.value = true
  try {
    data.value = await saveSubscribeConfig(payload)
    ElMessage.success('已保存并即时生效')
    await load()
  } finally {
    saving.value = false
  }
}

async function selfCheck() {
  checking.value = true
  try {
    checkResult.value = await selfCheckSubscribeConfig()
    if (checkResult.value.passed) {
      ElMessage.success('自检通过')
    } else {
      ElMessage.warning('自检未通过')
    }
  } finally {
    checking.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; }
.ml8 { margin-left: 8px; }
.card-head { display: flex; align-items: center; gap: 12px; }
.tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 4px; }
.tip.err { color: #f56c6c; }
.alert-body { line-height: 1.7; }
.missing { margin: 6px 0 0 18px; padding: 0; color: #e6a23c; }
.missing li { list-style: disc; }
</style>
