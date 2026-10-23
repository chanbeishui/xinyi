<template>
  <div class="config-shell">
    <el-card shadow="never" class="header-card">
      <div class="head">
        <div>
          <div class="title">{{ title }}</div>
          <div class="sub">{{ subtitle }}</div>
        </div>
        <div class="head-right">
          <el-tag v-if="detail" :type="detail.lifecycle === 'ACTIVE' ? 'success' : 'info'" size="small">
            生效版本 {{ detail.activeRevisionId || '未发布' }}
          </el-tag>
          <el-tag v-if="detail && detail.draftRevisionId && detail.draftRevisionId !== detail.activeRevisionId"
                  type="warning" size="small" style="margin-left: 8px">
            草稿 {{ detail.draftRevisionId }} 未生效
          </el-tag>
          <el-button size="small" style="margin-left: 12px" @click="load">刷新</el-button>
        </div>
      </div>
      <el-alert v-if="!detail" type="info" :closable="false" show-icon style="margin-top: 12px"
                :title="`当前主体还没有「${title}」配置，填写名称后点击“创建配置”开始`" />
      <el-alert v-else-if="issues.length" type="warning" :closable="false" show-icon style="margin-top: 12px"
                :title="`有 ${issues.length} 项需要处理，未处理完成不能发布`">
        <div v-for="(item, idx) in issues" :key="idx" class="issue">
          <el-tag :type="item.severity === 'ERROR' ? 'danger' : 'warning'" size="small">{{ item.severity }}</el-tag>
          <span class="issue-field">{{ item.field }}</span>
          <span>{{ item.message }}</span>
        </div>
      </el-alert>
    </el-card>

    <el-card v-if="!detail" shadow="never" style="margin-top: 12px">
      <el-form label-width="140px" style="max-width: 620px">
        <el-form-item label="归属主体部门">
          <el-input v-model="createForm.ownerDeptId" placeholder="部门ID，留空取当前账号部门" />
        </el-form-item>
        <el-form-item label="环境">
          <el-radio-group v-model="createForm.environment">
            <el-radio-button value="TEST">TEST 联调</el-radio-button>
            <el-radio-button value="LIVE">LIVE 生产</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input v-model="createForm.name" :placeholder="`${title}（${new Date().toLocaleDateString()}）`" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="creating" @click="create">创建配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else shadow="never" style="margin-top: 12px">
      <el-form label-width="160px" class="config-form">
        <el-form-item v-for="field in fields" :key="field.key" :label="field.label">
          <el-switch v-if="field.type === 'switch'" v-model="form[field.key]" />
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.key]" type="textarea"
                    :rows="field.rows || 3" :maxlength="field.max || 1000" show-word-limit
                    :placeholder="field.hint" />
          <el-input v-else-if="field.type === 'password'" v-model="form[field.key]" type="password"
                    show-password :placeholder="field.hint" />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.key]" style="width: 100%"
                     :placeholder="field.hint">
            <el-option v-for="opt in field.options || []" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-checkbox-group v-else-if="field.type === 'checks'" v-model="form[field.key]">
            <el-checkbox v-for="opt in field.options || []" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-checkbox>
          </el-checkbox-group>
          <el-select v-else-if="field.type === 'ref'" v-model="form[field.key]" clearable style="width: 100%"
                     :placeholder="field.hint" @change="(v: any) => onRefChange(field, v)">
            <el-option v-for="opt in refOptions[field.key] || []" :key="opt.value" :value="opt.value"
                       :label="opt.label" />
          </el-select>
          <div v-else>
            <el-input v-model="form[field.key]" :placeholder="field.hint" :maxlength="field.max" />
            <div v-if="field.hint" class="hint">{{ field.hint }}</div>
          </div>
        </el-form-item>

        <el-divider content-position="left">敏感凭据（只写不读）</el-divider>
        <el-form-item v-for="slot in secretSlots" :key="slot.key" :label="slot.label">
          <div class="secret-row">
            <el-tag :type="stateOf(slot.key).configured ? 'success' : 'info'" size="small">
              {{ stateOf(slot.key).configured ? `已配置 · ${stateOf(slot.key).fingerprint || ''}` : '未配置' }}
            </el-tag>
            <span v-if="stateOf(slot.key).writtenAt" class="hint">写入于 {{ stateOf(slot.key).writtenAt }}</span>
            <el-input v-model="secrets[slot.key]" type="password" show-password style="width: 320px"
                      :placeholder="slot.hint || '留空表示保持原值；输入即替换'" />
            <el-checkbox v-model="clearFlags[slot.key]">清除</el-checkbox>
          </div>
        </el-form-item>

        <el-form-item label="变更原因" required>
          <el-input v-model="reason" maxlength="200" placeholder="至少 2 字，会写入发布审计" />
        </el-form-item>

        <el-form-item>
          <el-button :loading="saving" @click="saveDraft">保存草稿</el-button>
          <el-button :loading="checking" :disabled="!draftRev" @click="runCheck">校验</el-button>
          <el-button type="primary" :loading="publishing" :disabled="!checkResult" @click="publish">发布生效</el-button>
          <el-button v-if="detail && detail.activeRevisionId" text @click="loadVersions">版本与发布记录</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="checkResult" type="success" :closable="false" show-icon style="margin-top: 8px"
                :title="`校验 ${checkResult.status}（${checkResult.level}）· 校验号 ${checkResult.checkId}，内容摘要 ${shortHash}`" />
      <el-alert v-if="lastMessage" type="success" :closable="true" show-icon style="margin-top: 8px"
                :title="lastMessage" @close="lastMessage = ''" />
    </el-card>

    <el-drawer v-model="drawer" title="版本与发布记录" size="640px">
      <el-tabs v-model="tab">
        <el-tab-pane label="历史版本" name="revisions">
          <el-table :data="revisions" size="small">
            <el-table-column prop="revision" label="版本" width="70" />
            <el-table-column prop="revisionId" label="ID" width="90" />
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column prop="reason" label="原因" />
            <el-table-column label="" width="90">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="rollback(row)">回滚</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="发布审计" name="publications">
          <el-table :data="publications" size="small">
            <el-table-column prop="action" label="动作" width="90" />
            <el-table-column prop="revisionId" label="版本ID" width="90" />
            <el-table-column prop="publishedAt" label="时间" width="170" />
            <el-table-column prop="reason" label="原因" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  checkProfile, createProfile, getProfile, listProfiles, listPublications, listRevisions,
  publishProfile, rollbackProfile, saveDraft
} from '@/api/rongli/integration'

const props = defineProps<{
  kind: string
  title: string
  subtitle: string
  fields: any[]
  secretSlots: any[]
  defaults: any
}>()

const detail = ref<any>(null)
const form = reactive<any>({})
const secrets = reactive<any>({})
const clearFlags = reactive<any>({})
const reason = ref('')
const issues = ref<any[]>([])
const draftRev = ref<any>(null)
const checkResult = ref<any>(null)
const lastMessage = ref('')
const saving = ref(false)
const checking = ref(false)
const publishing = ref(false)
const creating = ref(false)
const createForm = reactive<any>({ ownerDeptId: '', environment: 'TEST', name: '' })
const refOptions = reactive<any>({})
const drawer = ref(false)
const tab = ref('revisions')
const revisions = ref<any[]>([])
const publications = ref<any[]>([])

const shortHash = computed(() => (checkResult.value?.contentHash || '').slice(0, 12))

function stateOf(slot: string) {
  return (detail.value?.draft?.credentialStates || detail.value?.active?.credentialStates || {})[slot] || {}
}

async function load() {
  const res = await listProfiles({ kind: props.kind, pageSize: 20 })
  const rows = res.rows || []
  if (!rows.length) {
    detail.value = null
    return
  }
  detail.value = await getProfile(rows[0].profileId)
  fillForm(detail.value.draft?.config || detail.value.active?.config || props.defaults)
  issues.value = detail.value.validationIssues || []
  draftRev.value = null
  checkResult.value = null
  await loadRefs()
}

function fillForm(config: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, JSON.parse(JSON.stringify(props.defaults || {})))
  Object.assign(form, config || {})
  props.secretSlots.forEach((slot: any) => {
    secrets[slot.key] = ''
    clearFlags[slot.key] = false
  })
}

async function loadRefs() {
  for (const field of props.fields) {
    if (field.type !== 'ref') continue
    const res = await listProfiles({ kind: field.refKind, pageSize: 50 })
    const rows = (res.rows || []).filter((r: any) => r.activeRevisionId)
    refOptions[field.key] = rows.map((r: any) => ({
      value: JSON.stringify({ profileId: r.profileId, revisionId: r.activeRevisionId }),
      label: `${r.name}（生效 ${r.activeRevisionId}）`
    }))
    if (form[field.key] && typeof form[field.key] === 'object') {
      form[field.key] = JSON.stringify(form[field.key])
    }
  }
}

function onRefChange(field: any, value: string) {
  form[field.key] = value
}

function buildConfig() {
  const config: any = {}
  for (const field of props.fields) {
    let value = form[field.key]
    if (field.type === 'ref') {
      if (typeof value === 'string' && value) {
        value = JSON.parse(value)
      } else if (!value) {
        value = undefined
      }
    }
    if (value !== undefined) config[field.key] = value
  }
  return config
}

function buildCredentials() {
  const credentials: any = {}
  props.secretSlots.forEach((slot: any) => {
    if (clearFlags[slot.key]) {
      credentials[slot.key] = { action: 'CLEAR' }
    } else if (secrets[slot.key]) {
      credentials[slot.key] = { action: 'REPLACE', value: secrets[slot.key] }
    }
  })
  return credentials
}

async function create() {
  creating.value = true
  try {
    await createProfile({
      kind: props.kind,
      name: createForm.name || `${props.title}`,
      scope: { ownerDeptId: createForm.ownerDeptId || undefined, environment: createForm.environment }
    })
    ElMessage.success('已创建草稿配置档案')
    await load()
  } finally {
    creating.value = false
  }
}

async function saveDraft() {
  if (!reason.value || reason.value.length < 2) {
    ElMessage.warning('请填写变更原因')
    return
  }
  saving.value = true
  try {
    const res = await saveDraft(detail.value.profileId, {
      kind: props.kind,
      expectedVersion: detail.value.version,
      config: buildConfig(),
      credentials: buildCredentials(),
      reason: reason.value
    })
    draftRev.value = res
    checkResult.value = null
    issues.value = res.validationIssues || []
    ElMessage.success('草稿已保存（未生效）')
    await load()
  } finally {
    saving.value = false
  }
}

async function runCheck() {
  if (!draftRev.value) return
  checking.value = true
  try {
    const res = await checkProfile(detail.value.profileId, {
      revisionId: draftRev.value.revisionId,
      expectedVersion: detail.value.version,
      level: 'LOCAL'
    })
    checkResult.value = res
    issues.value = res.issues || []
    ElMessage[res.status === 'FAIL' ? 'error' : 'success'](`校验结果：${res.status}`)
  } finally {
    checking.value = false
  }
}

async function publish() {
  const bothOff = props.kind === 'PAYMENT_POLICY' && !form.offlineEnabled && !form.wechatEnabled
  let acknowledge = false
  if (bothOff) {
    await ElMessageBox.confirm('两种支付方式都关闭会暂停新付费预约（历史订单与退款仍可查）。确认发布？', '双重关闭确认',
      { type: 'warning' })
    acknowledge = true
  }
  publishing.value = true
  try {
    const res = await publishProfile(detail.value.profileId, {
      revisionId: checkResult.value.revisionId,
      expectedVersion: detail.value.version,
      contentHash: checkResult.value.contentHash,
      checkId: checkResult.value.checkId,
      reason: reason.value,
      acknowledgeNoPaymentMethods: acknowledge
    })
    lastMessage.value = `已发布版本 ${res.activeRevisionId}，时间 ${res.effectiveAt}；新交易立即使用新配置，无需重启`
    ElMessage.success('已发布生效')
    await load()
  } finally {
    publishing.value = false
  }
}

async function loadVersions() {
  drawer.value = true
  const [rev, pub] = await Promise.all([
    listRevisions(detail.value.profileId, { pageSize: 20 }),
    listPublications(detail.value.profileId, { pageSize: 20 })
  ])
  revisions.value = rev.rows || []
  publications.value = pub.rows || []
}

async function rollback(row: any) {
  const { value } = await ElMessageBox.prompt(
    `将重新发布版本 ${row.revision}，需要重新通过当前依赖校验。请填写回滚原因：`, '回滚配置',
    { inputValidator: (v: string) => (v && v.length >= 2) || '至少 2 字' }
  )
  const check = await checkProfile(detail.value.profileId, {
    revisionId: row.revisionId,
    expectedVersion: detail.value.version,
    level: 'LOCAL'
  })
  await rollbackProfile(detail.value.profileId, {
    targetRevisionId: row.revisionId,
    expectedVersion: detail.value.version,
    reason: value,
    checkId: check.checkId
  })
  ElMessage.success('已按新发布回滚')
  drawer.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.config-shell { padding: 8px 4px; }
.header-card .head { display: flex; justify-content: space-between; align-items: flex-start; }
.title { font-size: 16px; font-weight: 600; }
.sub { color: var(--el-text-color-secondary); font-size: 12px; margin-top: 4px; max-width: 780px; }
.head-right { display: flex; align-items: center; }
.config-form { max-width: 980px; }
.hint { color: var(--el-text-color-secondary); font-size: 12px; }
.issue { display: flex; gap: 8px; align-items: baseline; font-size: 12px; margin-top: 4px; }
.issue-field { font-family: monospace; color: var(--el-color-primary); }
.secret-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
</style>
