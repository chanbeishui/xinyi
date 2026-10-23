<template>
  <div>
    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filterScene" clearable placeholder="场景" style="width: 200px" @change="load">
          <el-option v-for="s in scenes" :key="s" :label="sceneLabel(s)" :value="s" />
        </el-select>
        <el-button type="primary" @click="openCreate">新建模板</el-button>
        <span class="hint">模板必须来自供应商审核通过的版本；本地保存只记录映射，不替代供应商审批。</span>
      </div>
      <el-table :data="rows" size="small" style="margin-top: 12px">
        <el-table-column prop="name" label="模板名称" min-width="160" />
        <el-table-column label="场景" width="170">
          <template #default="{ row }">{{ sceneLabel(row.scene) }}</template>
        </el-table-column>
        <el-table-column label="接收身份" width="100">
          <template #default="{ row }">{{ audienceLabel(row.audience) }}</template>
        </el-table-column>
        <el-table-column label="生效版本" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.activeRevisionId" type="success" size="small">{{ row.activeRevisionId }}</el-tag>
            <span v-else class="hint">未发布</span>
          </template>
        </el-table-column>
        <el-table-column label="草稿" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.draftRevisionId && row.draftRevisionId !== row.activeRevisionId" type="warning"
                    size="small">{{ row.draftRevisionId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="open(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="!rows.length" shadow="never" style="margin-top: 12px">
      <el-empty :description="`还没有 ${providerLabel} 模板，点击“新建模板”开始`" />
    </el-card>

    <el-drawer v-model="drawer" :title="`${providerLabel} · ${current?.name || ''}`" size="760px">
      <el-form v-if="current" label-width="150px">
        <el-form-item label="通道配置" required>
          <el-select v-model="draft.profileValue" style="width: 100%" placeholder="选择已发布的通道配置">
            <el-option v-for="opt in profileOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item :label="isSubscribe ? '订阅模板 ID' : '供应商模板 ID'">
          <el-input v-model="draft.vendorTemplateId" placeholder="真实模板 ID / TemplateCode" />
        </el-form-item>
        <el-form-item label="审核通过的正文">
          <el-input v-model="draft.approvedContent" type="textarea" :rows="2"
                    placeholder="仅本地映射用；实际内容以供应商审核结果为准" />
        </el-form-item>
        <el-form-item v-if="!isSubscribe" label="签名内容">
          <el-input v-model="draft.signName" placeholder="签名内容，不是签名 ID" />
        </el-form-item>
        <el-form-item label="启用发送">
          <el-switch v-model="draft.enabled" />
        </el-form-item>

        <el-divider content-position="left">参数映射</el-divider>
        <div v-for="(p, idx) in draft.parameters" :key="idx" class="param-row">
          <el-input v-model="p['parameterKey']" style="width: 150px"
                    :placeholder="isSubscribe ? '字段键 thing1' : (provider === 'TENCENT_SMS' ? '顺序键 1' : '变量名 name')" />
          <el-select v-model="p['variable']" placeholder="来源变量" style="width: 200px">
            <el-option v-for="v in variables" :key="v" :label="v" :value="v" />
          </el-select>
          <el-input-number v-model="p['maxLength']" :min="1" :max="500" controls-position="right"
                           style="width: 130px" />
          <el-button link type="danger" @click="draft.parameters.splice(idx, 1)">删除</el-button>
        </div>
        <el-button size="small" @click="addParam">增加参数</el-button>

        <template v-if="isSubscribe">
          <el-divider content-position="left">订阅专属</el-divider>
          <el-form-item label="跳转页面">
            <el-input v-model="draft.pageRoute" placeholder="如 /pages/owner/order/detail" />
          </el-form-item>
          <el-form-item label="页面参数">
            <el-checkbox-group v-model="draft.pageParameterKeys">
              <el-checkbox value="orderNo">orderNo</el-checkbox>
              <el-checkbox value="statementId">statementId</el-checkbox>
              <el-checkbox value="taskId">taskId</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="运行环境">
            <el-select v-model="draft.miniProgramState" style="width: 200px">
              <el-option label="formal（生产）" value="formal" />
              <el-option label="trial（体验版）" value="trial" />
              <el-option label="developer（开发版）" value="developer" />
            </el-select>
            <el-select v-model="draft.subscriptionType" style="width: 200px; margin-left: 8px">
              <el-option label="一次性订阅" value="ONCE" />
              <el-option label="长期订阅（需资格证据）" value="LONG" />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item label="变更原因" required>
          <el-input v-model="reason" placeholder="至少 2 字" />
        </el-form-item>

        <el-form-item>
          <el-button :loading="busy" @click="saveDraft">保存草稿</el-button>
          <el-button :disabled="!revision" @click="runCheck">校验</el-button>
          <el-button :disabled="!revision" @click="runPreview">预览</el-button>
          <el-button type="primary" :disabled="!checkResult" @click="publish">发布</el-button>
          <el-button :disabled="!current?.activeRevisionId" @click="openTest">真实测试</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="issues.length" type="warning" :closable="false" show-icon
                :title="`${issues.length} 项待处理`">
        <div v-for="(item, idx) in issues" :key="idx" class="issue">
          <el-tag :type="item.severity === 'ERROR' ? 'danger' : 'warning'" size="small">{{ item.severity }}</el-tag>
          <span class="mono">{{ item.field }}</span><span>{{ item.message }}</span>
        </div>
      </el-alert>

      <el-card v-if="preview" shadow="never" style="margin-top: 12px">
        <div class="hint">仅本地渲染，不会发送、不消耗订阅次数</div>
        <pre class="preview">{{ preview.renderedText }}</pre>
        <div v-for="(f, idx) in preview.mappedFields" :key="idx" class="hint">
          {{ f.key }} ← {{ f.variable || '未映射' }} = {{ f.sampleValue || '（空）' }}
        </div>
      </el-card>
    </el-drawer>

    <el-dialog v-model="createDialog" title="新建模板" width="520px">
      <el-form label-width="110px">
        <el-form-item label="模板名称" required>
          <el-input v-model="createForm.name" maxlength="100" placeholder="如：预约确认-车主" />
        </el-form-item>
        <el-form-item label="业务场景" required>
          <el-select v-model="createForm.scene" style="width: 100%">
            <el-option v-for="s in scenes" :key="s" :label="sceneLabel(s)" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收身份" required>
          <el-select v-model="createForm.audience" style="width: 100%">
            <el-option label="车主" value="OWNER" />
            <el-option label="代驾" value="DRIVER" />
            <el-option label="检测站" value="STATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="环境" required>
          <el-radio-group v-model="createForm.environment">
            <el-radio-button value="TEST">TEST</el-radio-button>
            <el-radio-button value="LIVE">LIVE</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!createForm.name" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialog" title="真实测试发送" width="520px">
      <el-alert type="warning" :closable="false" show-icon
                title="真实发送可能产生费用或消耗订阅次数，且只允许发往白名单收件人" />
      <el-form label-width="110px" style="margin-top: 12px">
        <el-form-item label="收件人">
          <el-select v-model="testRecipient" placeholder="选择测试白名单" style="width: 100%">
            <el-option v-for="r in recipients" :key="r.recipientId" :value="r.recipientId"
                       :label="`${r.name}（${r.phoneMask || r.accountId}）`" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="hint">没有合适收件人？到 B51 页面登记测试白名单后再回来。</div>
      <template #footer>
        <el-button @click="testDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!testRecipient" @click="sendTest">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  checkTemplate, createTemplate, getTemplate, listProfiles, listTemplates, listTestRecipients,
  previewTemplate, publishTemplate, saveTemplateDraft, testTemplate
} from '@/api/rongli/integration'

const props = defineProps<{ provider: string }>()

const scenes = ['LOGIN_OTP', 'PHONE_VERIFY', 'BOOKING_CONFIRMED', 'DRIVER_ASSIGNED', 'PICKUP_ARRIVED',
  'INSPECTION_FINISHED', 'RETURN_COMPLETED', 'AFTERSALE_DECIDED', 'REFUND_CHANGED', 'DRIVER_INVITE',
  'STATEMENT_READY']
const variables = ['code', 'ttlMinutes', 'orderNo', 'plateNo', 'pickupWindow', 'stationName',
  'driverDisplayName', 'statusText', 'resultText', 'amountYuan', 'statementNo', 'serviceDate', 'supportPhone']

const rows = ref<any[]>([])
const filterScene = ref('')
const drawer = ref(false)
const current = ref<any>(null)
const revision = ref<any>(null)
const checkResult = ref<any>(null)
const issues = ref<any[]>([])
const preview = ref<any>(null)
const reason = ref('')
const busy = ref(false)
const profileOptions = ref<any[]>([])
const testDialog = ref(false)
const createDialog = ref(false)
const recipients = ref<any[]>([])
const testRecipient = ref('')

const draft = reactive<any>({
  profileValue: '', vendorTemplateId: '', approvedContent: '', signName: '', enabled: false,
  parameters: [], pageRoute: '', pageParameterKeys: [], miniProgramState: 'formal',
  lang: 'zh_CN', subscriptionType: 'ONCE'
})
const createForm = reactive<any>({ name: '', scene: 'BOOKING_CONFIRMED', audience: 'OWNER', environment: 'TEST' })

const isSubscribe = computed(() => props.provider === 'WECHAT_SUBSCRIBE')
const providerLabel = computed(() => (isSubscribe.value ? '订阅消息模板' : '短信场景模板'))

function sceneLabel(s: string) {
  const map: Record<string, string> = {
    LOGIN_OTP: '登录验证码', PHONE_VERIFY: '手机号校验', BOOKING_CONFIRMED: '预约确认',
    DRIVER_ASSIGNED: '已派单', PICKUP_ARRIVED: '到达取车点', INSPECTION_FINISHED: '检测完成',
    RETURN_COMPLETED: '归还完成', AFTERSALE_DECIDED: '售后结论', REFUND_CHANGED: '退款状态',
    DRIVER_INVITE: '派单邀约', STATEMENT_READY: '对账单可查看'
  }
  return map[s] || s
}
function audienceLabel(a: string) {
  return ({ OWNER: '车主', DRIVER: '代驾', STATION: '检测站' } as any)[a] || a
}

async function load() {
  const res = await listTemplates({ provider: props.provider, scene: filterScene.value || undefined, pageSize: 100 })
  rows.value = res.rows || []
}

function openCreate() {
  createForm.name = ''
  createDialog.value = true
}

async function doCreate() {
  await createTemplate({ name: createForm.name, provider: props.provider, scene: createForm.scene,
    audience: createForm.audience, scope: { environment: createForm.environment } })
  createDialog.value = false
  ElMessage.success('模板档案已创建，请补充模板ID与参数映射')
  await load()
}

async function open(row: any) {
  current.value = await getTemplate(row.templateId)
  revision.value = null
  checkResult.value = null
  preview.value = null
  reason.value = ''
  const source = current.value.draft || current.value.active
  const profileKind = isSubscribe.value ? 'WECHAT_APP' : (props.provider === 'TENCENT_SMS' ? 'TENCENT_SMS' : 'ALIYUN_SMS')
  const profiles = await listProfiles({ kind: profileKind, pageSize: 50 })
  profileOptions.value = (profiles.rows || []).filter((p: any) => p.activeRevisionId).map((p: any) => ({
    value: JSON.stringify({ profileId: p.profileId, revisionId: p.activeRevisionId }),
    label: `${p.name}（生效 ${p.activeRevisionId}）`
  }))
  draft.profileValue = source?.profile
    ? JSON.stringify({ profileId: source.profile.profileId, revisionId: source.profile.revisionId })
    : ''
  draft.vendorTemplateId = source?.vendorTemplateId || ''
  draft.approvedContent = source?.approvedContent || ''
  draft.signName = source?.signName || ''
  draft.enabled = !!source?.enabled
  draft.parameters = (source?.smsParameters?.length ? source.smsParameters : source?.subscribeParameters || [])
    .map((p: any) => ({ ...p }))
  draft.pageRoute = source?.pageRoute || ''
  draft.pageParameterKeys = source?.pageParameterKeys || []
  draft.miniProgramState = source?.miniProgramState || 'formal'
  draft.subscriptionType = source?.subscriptionType || 'ONCE'
  issues.value = []
  drawer.value = true
}

function addParam() {
  draft.parameters.push({ parameterKey: '', variable: 'orderNo', maxLength: 32 })
}

function payload() {
  const ref = draft.profileValue ? JSON.parse(draft.profileValue) : {}
  const body: any = {
    expectedVersion: current.value.version,
    profile: { profileId: ref.profileId, revisionId: ref.revisionId },
    vendorTemplateId: draft.vendorTemplateId,
    approvedContent: draft.approvedContent,
    enabled: draft.enabled,
    provider: props.provider,
    reason: reason.value,
    parameters: draft.parameters
  }
  if (!isSubscribe.value) {
    body.signName = draft.signName
  } else {
    body.pageRoute = draft.pageRoute
    body.pageParameterKeys = draft.pageParameterKeys
    body.miniProgramState = draft.miniProgramState
    body.lang = draft.lang
    body.subscriptionType = draft.subscriptionType
  }
  return body
}

async function saveDraft() {
  busy.value = true
  try {
    const res = await saveTemplateDraft(current.value.templateId, payload())
    revision.value = res
    checkResult.value = null
    issues.value = res.validationIssues || []
    ElMessage.success('草稿已保存（未生效）')
    current.value = await getTemplate(current.value.templateId)
  } finally {
    busy.value = false
  }
}

async function runCheck() {
  const res = await checkTemplate(current.value.templateId, {
    revisionId: revision.value.revisionId,
    expectedVersion: current.value.version,
    level: 'LOCAL'
  })
  checkResult.value = res
  issues.value = res.issues || []
  ElMessage[res.status === 'FAIL' ? 'error' : 'success'](`校验：${res.status}`)
}

async function runPreview() {
  preview.value = await previewTemplate(current.value.templateId, {
    revisionId: revision.value.revisionId,
    sampleValues: {
      code: '123456', ttlMinutes: '5', orderNo: 'RL202610230001', plateNo: '粤K12345',
      pickupWindow: '2026-10-24 09:00 - 11:00', stationName: '荣利检测站', driverDisplayName: '张师傅',
      statusText: '待取车', resultText: '合格', amountYuan: '330.00', statementNo: 'ST20261023',
      serviceDate: '2026-10-24', supportPhone: '0668-0000000'
    }
  })
}

async function publish() {
  if (!reason.value || reason.value.length < 2) {
    ElMessage.warning('请填写变更原因')
    return
  }
  await publishTemplate(current.value.templateId, {
    revisionId: checkResult.value.revisionId,
    expectedVersion: current.value.version,
    contentHash: checkResult.value.contentHash,
    checkId: checkResult.value.checkId,
    reason: reason.value
  })
  ElMessage.success('模板已发布')
  drawer.value = false
  await load()
}

async function openTest() {
  const res = await listTestRecipients({ environment: current.value.scope?.environment })
  recipients.value = res.rows || []
  testDialog.value = true
}

async function sendTest() {
  const res = await testTemplate(current.value.templateId, { recipientId: testRecipient.value, confirmed: true })
  ElMessage.info(res.note || res.status)
  testDialog.value = false
}

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 12px; align-items: center; }
.hint { color: var(--el-text-color-secondary); font-size: 12px; }
.param-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.issue { display: flex; gap: 8px; font-size: 12px; margin-top: 4px; }
.mono { font-family: monospace; color: var(--el-color-primary); }
.preview { white-space: pre-wrap; font-size: 13px; background: var(--el-fill-color-light); padding: 10px; border-radius: 6px; }
</style>
