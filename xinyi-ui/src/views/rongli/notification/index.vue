<template>
  <RongliPage
    title="消息与内容模板"
    subtitle="公开介绍、客服和业务通知统一维护；不把渠道接受标为用户已读。"
  >
    <template #actions>
      <el-button icon="Refresh" @click="loadAll">刷新</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>业务消息模板</h3>
        <span class="rl-muted">模板变量用 { } 包裹，保存后仅影响之后产生的消息</span>
      </div>

      <el-table v-loading="loading" :data="templates" :border="false">
        <el-table-column label="业务事件" width="170">
          <template #default="{ row }">
            <div class="rl-strong">{{ sceneLabel(row.scene) }}</div>
            <div class="rl-mono">{{ row.scene }}</div>
          </template>
        </el-table-column>
        <el-table-column label="接收方" width="170">
          <template #default="{ row }">{{ sceneMeta(row.scene).receiver }}</template>
        </el-table-column>
        <el-table-column label="渠道" width="170">
          <template #default="{ row }">{{ sceneMeta(row.scene).channel }}</template>
        </el-table-column>
        <el-table-column label="配置状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.customized ? 'rl-tag--green' : 'rl-tag--gray'">
              {{ row.customized ? '已自定义' : '默认模板' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="内容摘要" min-width="260">
          <template #default="{ row }">
            <div class="rl-cell-sub">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTemplate(row)">编辑</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">暂无通知模板</div>
            <div class="rl-empty__hint">业务事件产生后按模板下发，未配置时使用默认模板。</div>
          </div>
        </template>
      </el-table>
    </section>

    <div class="rl-grid rl-grid--2 mt16">
      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>公开内容</h3>
        </div>
        <div class="rl-panel__body">
          <el-form label-position="top">
            <el-form-item label="服务介绍">
              <el-input v-model="intro" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="客服说明">
              <el-input v-model="support" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
          <el-button :loading="saving" @click="saveContent">保存内容草稿</el-button>
        </div>
      </section>

      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>发送与失败记录</h3>
          <el-button link type="primary" @click="loadJobs">刷新记录</el-button>
        </div>
        <div class="rl-panel__body">
          <p class="rl-hint">未订阅不等于可无限重试，过期邀约不能通过补发重新生效。</p>
          <el-table :data="jobRows" :border="false" max-height="300" class="mt12">
            <el-table-column label="任务 / 事件" min-width="200">
              <template #default="{ row }">
                <div class="rl-mono">#{{ row.taskId }}</div>
                <div class="rl-sub">{{ row.eventType || row.eventKey }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <span class="rl-tag" :class="jobClass(row.status)">{{ jobLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="重试" prop="attempts" width="70" align="center" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="isJobDone(row.status)" @click="retry(row)">重试</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="rl-empty">
                <div class="rl-empty__title">没有投递记录</div>
                <div class="rl-empty__hint">业务事件投递成功或失败都会留痕。</div>
              </div>
            </template>
          </el-table>
        </div>
      </section>
    </div>

    <!-- 编辑业务通知模板 -->
    <el-drawer v-model="templateVisible" title="编辑业务通知模板" size="520px">
      <el-form label-position="top">
        <el-form-item label="业务事件">
          <el-select v-model="form.scene" style="width: 100%" disabled>
            <el-option :label="sceneLabel(form.scene)" :value="form.scene" />
          </el-select>
        </el-form-item>
        <el-form-item label="微信模板 ID">
          <el-input v-model="form.vendorTemplateId" placeholder="由正式小程序已获准模板配置" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <div class="rl-kv">
        <div style="grid-column: span 2">
          <span>可用变量</span>
          <b>
            <span v-for="v in form.variables || []" :key="v" class="rl-tag rl-tag--plain rl-tag--blue rl-var">
              {{ v }}
            </span>
            <span v-if="!form.variables || !form.variables.length" class="rl-muted">无</span>
          </b>
        </div>
      </div>
      <p class="rl-hint mt12">
        模板不包含明文证件、支付密钥和交接码。测试只显示本地预览，不发送生产通知。
      </p>
      <template #footer>
        <el-button @click="templateVisible = false">取消</el-button>
        <el-button :loading="saving" @click="saveTemplate">保存模板草稿</el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliNotification" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getBusinessConfig,
  listJobs,
  listNotificationTemplates,
  retryJob,
  saveBusinessConfig,
  updateNotificationTemplate
} from '@/api/rongli'

const loading = ref(false)
const saving = ref(false)
const templates = ref<any[]>([])
const jobRows = ref<any[]>([])

const intro = ref('')
const support = ref('')

const templateVisible = ref(false)
const form = reactive<any>({ scene: '', content: '', vendorTemplateId: '', variables: [] })

/** 场景的接收方与渠道属于产品定义，后端只存内容，这里按场景补齐展示 */
const SCENE_META: Record<string, { label: string; receiver: string; channel: string }> = {
  PAYMENT_SUCCEEDED: { label: '预约支付成功', receiver: '车主', channel: '站内 / 微信订阅' },
  ASSIGNED: { label: '代驾已接单', receiver: '车主', channel: '站内 / 微信订阅' },
  VEHICLE_RETURNED: { label: '实际归还完成', receiver: '车主', channel: '站内 / 微信订阅' },
  RESULT_REGISTERED: { label: '检测结果登记', receiver: '车主 / 当前代驾', channel: '站内 / 微信订阅' },
  AFTERSALE_REJECTED: { label: '售后驳回', receiver: '车主', channel: '站内待办' },
  REFUND_SUCCEEDED: { label: '退款完成', receiver: '车主', channel: '站内 / 微信订阅' },
  QUALIFICATION_APPROVED: { label: '资格审核通过', receiver: '代驾', channel: '站内 / 微信订阅' }
}

onMounted(loadAll)

async function loadAll() {
  await Promise.all([loadTemplates(), loadContent(), loadJobs()])
}

function sceneLabel(code: string) {
  return SCENE_META[code]?.label || code
}

function sceneMeta(code: string) {
  return SCENE_META[code] || { label: code, receiver: '当事人', channel: '站内待办' }
}

async function loadTemplates() {
  loading.value = true
  try {
    const res: any = await listNotificationTemplates()
    templates.value = (res && res.rows) || []
  } finally {
    loading.value = false
  }
}

async function loadContent() {
  const res: any = await getBusinessConfig().catch(() => null)
  const e = (res && res.effective) || {}
  intro.value = e['rongli.content.intro'] || ''
  support.value = e['rongli.content.support'] || ''
}

async function loadJobs() {
  const res: any = await listJobs({ pageNum: 1, pageSize: 20 }).catch(() => null)
  jobRows.value = (res && res.rows) || []
}

function openTemplate(row: any) {
  Object.assign(form, {
    scene: row.scene,
    content: row.content,
    vendorTemplateId: '',
    variables: row.variables || []
  })
  templateVisible.value = true
}

async function saveTemplate() {
  if (!form.content || !String(form.content).trim()) {
    ElMessage.warning('模板内容不能为空')
    return
  }
  saving.value = true
  try {
    await updateNotificationTemplate(form.scene, { content: form.content })
    ElMessage.success('模板草稿已保存')
    templateVisible.value = false
    loadTemplates()
  } finally {
    saving.value = false
  }
}

async function saveContent() {
  saving.value = true
  try {
    await saveBusinessConfig({ contentIntro: intro.value, contentSupport: support.value })
    ElMessage.success('公开内容已保存')
  } finally {
    saving.value = false
  }
}

const JOB_LABEL: Record<string, string> = {
  DONE: '已投递',
  SENT: '已投递',
  FAILED: '投递失败',
  PENDING: '待投递',
  RETRY: '待重试'
}

function jobLabel(code: string) {
  return JOB_LABEL[code] || code
}

function jobClass(code: string) {
  if (isJobDone(code)) return 'rl-tag--green'
  if (code === 'FAILED') return 'rl-tag--red'
  return 'rl-tag--amber'
}

function isJobDone(code: string) {
  return code === 'DONE' || code === 'SENT'
}

async function retry(row: any) {
  saving.value = true
  try {
    await retryJob(row.taskId)
    ElMessage.success('已重新入队，下一次补偿轮询会尝试投递')
    loadJobs()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.rl-var {
  margin-right: 6px;
}
</style>
