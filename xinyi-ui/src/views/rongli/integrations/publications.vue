<template>
  <div>
    <el-card shadow="never" class="intro">
      <div class="title">发布历史与发送诊断（B51）</div>
      <div class="hint">
        这里只做只读追踪与受控动作，不提供「一键重发全部失败」。<br />
        发布记录说明每次生效切换的版本、操作人与原因；发送记录里 <b>ACCEPTED</b> 只代表供应商已受理，不等于已送达，更不代表用户已读；
        <b>UNKNOWN</b> 只允许查回执、不允许重发（换通道重发会造成双发）；只有 <b>FAILED</b> 才是确定失败，可以重试。
      </div>
    </el-card>

    <el-tabs v-model="tab" type="border-card">
      <!-- ================================================== 发布审计 -->
      <el-tab-pane label="发布审计" name="publish">
        <div class="bar">
          <el-radio-group v-model="targetType" size="small" @change="onTargetTypeChange">
            <el-radio-button value="PROFILE">配置档案</el-radio-button>
            <el-radio-button value="TEMPLATE">消息模板</el-radio-button>
          </el-radio-group>
          <el-select v-model="targetId" filterable clearable placeholder="选择对象" style="width: 320px" @change="loadPublications">
            <el-option v-for="opt in targetOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button size="small" :disabled="!targetId" @click="loadPublications">刷新</el-button>
        </div>

        <el-table v-loading="pubLoading" :data="publications" size="small" style="margin-top: 12px">
          <el-table-column prop="publishedAt" label="发布时刻" width="180" />
          <el-table-column prop="action" label="动作" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="row.action === 'ROLLBACK' ? 'warning' : 'success'">{{ actionLabel(row.action) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="revisionId" label="生效版本" width="120" />
          <el-table-column prop="previousRevisionId" label="原版本" width="120">
            <template #default="{ row }">{{ row.previousRevisionId || '—' }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
          <el-table-column prop="publishedBy" label="操作人" width="100" />
          <el-table-column label="历史交易" min-width="200">
            <template #default="{ row }">{{ row.historicalHandling }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!pubLoading && !publications.length" description="暂无发布记录（草稿保存与校验不产生发布记录）" />
      </el-tab-pane>

      <!-- ================================================== 发送记录 -->
      <el-tab-pane label="发送记录与回执" name="delivery">
        <div class="bar">
          <el-select v-model="filter.provider" clearable placeholder="通道" style="width: 160px">
            <el-option label="腾讯云短信" value="TENCENT_SMS" />
            <el-option label="阿里云短信" value="ALIYUN_SMS" />
            <el-option label="微信订阅消息" value="WECHAT_SUBSCRIBE" />
          </el-select>
          <el-select v-model="filter.status" clearable placeholder="状态" style="width: 160px">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
          <el-input v-model="filter.scene" clearable placeholder="场景编码" style="width: 200px" />
          <el-button type="primary" size="small" @click="loadDeliveries">查询</el-button>
          <span class="hint">回执与重试都按“期望版本”做并发保护，页面数据过期会被拒绝并提示刷新。</span>
        </div>

        <el-table v-loading="deliveryLoading" :data="deliveries" size="small" style="margin-top: 12px">
          <el-table-column prop="deliveryId" label="发送ID" width="100" />
          <el-table-column prop="scene" label="场景" width="170" />
          <el-table-column prop="provider" label="通道" width="140" />
          <el-table-column prop="recipientMask" label="接收方" width="130" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="attemptCount" label="尝试" width="70" />
          <el-table-column prop="safeErrorCode" label="错误码" width="150" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时刻" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.allowedActions.includes('QUERY_RECEIPT')" link type="primary" size="small"
                         @click="doQueryReceipt(row)">查回执</el-button>
              <el-button v-if="row.allowedActions.includes('RETRY_DEFINITE_FAILURE')" link type="warning" size="small"
                         @click="openRetry(row)">重试</el-button>
              <span v-if="!row.allowedActions.length" class="hint">无可用动作</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="deliveryPage.pageNum" :page-size="deliveryPage.pageSize"
                       :total="deliveryPage.total" layout="total, prev, pager, next" style="margin-top: 12px"
                       @current-change="loadDeliveries" />

        <el-alert v-if="receiptResult" type="info" :closable="true" style="margin-top: 12px"
                  :title="`查回执结果：${receiptResult.queryResult}`" :description="receiptResult.note" />

        <el-dialog v-model="retryDialog" title="重试发送" width="460px">
          <el-alert type="warning" :closable="false" show-icon
                    title="仅对确定失败（FAILED）的发送生效；重试会复用同一模板版本与同一通道，不换通道。"
                    style="margin-bottom: 12px" />
          <el-form label-width="90px">
            <el-form-item label="发送ID">
              <span>{{ retryTarget?.deliveryId }}</span>
            </el-form-item>
            <el-form-item label="重试原因" required>
              <el-input v-model="retryReason" type="textarea" :rows="3" placeholder="请说明重试原因，会写入审计" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="retryDialog = false">取消</el-button>
            <el-button type="primary" :loading="retrying" @click="doRetry">确认重试</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ================================================== 联调白名单 -->
      <el-tab-pane label="联调白名单" name="recipient">
        <div class="bar">
          <el-button type="primary" size="small" @click="openAddRecipient">新增白名单</el-button>
          <el-button size="small" @click="loadRecipients">刷新</el-button>
          <span class="hint">真实测试发送只允许命中白名单，未登记号码一律拒绝，避免把测试短信发到真实客户手机上。</span>
        </div>

        <el-table v-loading="recipientLoading" :data="recipients" size="small" style="margin-top: 12px">
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column label="类型" width="130">
            <template #default="{ row }">{{ row.kind === 'PHONE' ? '手机号' : '小程序账号' }}</template>
          </el-table-column>
          <el-table-column prop="phoneMask" label="号码" width="150">
            <template #default="{ row }">{{ row.phoneMask || '—' }}</template>
          </el-table-column>
          <el-table-column prop="accountId" label="账号ID" width="110">
            <template #default="{ row }">{{ row.accountId || '—' }}</template>
          </el-table-column>
          <el-table-column prop="environment" label="环境" width="110" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '有效' : '已撤销' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="登记时刻" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.enabled" link type="danger" size="small" @click="doRevoke(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!recipientLoading && !recipients.length" description="还没有登记联调白名单" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="recipientDialog" title="新增联调白名单" width="520px">
      <el-form label-width="110px">
        <el-form-item label="类型" required>
          <el-radio-group v-model="recipientForm.kind">
            <el-radio value="PHONE">手机号</el-radio>
            <el-radio value="MINI_ACCOUNT">小程序账号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="recipientForm.name" placeholder="例如：张三（内部联调）" />
        </el-form-item>
        <el-form-item v-if="recipientForm.kind === 'PHONE'" label="手机号" required>
          <el-input v-model="recipientForm.phone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <template v-else>
          <el-form-item label="账号ID" required>
            <el-input v-model="recipientForm.accountId" placeholder="小程序账号 ID" />
          </el-form-item>
          <el-form-item label="小程序凭据" required>
            <el-select v-model="recipientForm.appProfileId" filterable placeholder="选择 WECHAT_APP 配置档案" style="width: 100%">
              <el-option v-for="opt in appProfiles" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="环境" required>
          <el-select v-model="recipientForm.environment" style="width: 100%">
            <el-option label="测试 TEST" value="TEST" />
            <el-option label="生产 LIVE" value="LIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="登记原因" required>
          <el-input v-model="recipientForm.reason" type="textarea" :rows="2" placeholder="说明为什么需要这个白名单" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recipientDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingRecipient" @click="doAddRecipient">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addTestRecipient, listDeliveries, listProfiles, listPublications, listTemplatePublications,
  listTemplates, listTestRecipients, queryReceipt, retryDelivery, revokeTestRecipient
} from '@/api/rongli/integration'

const tab = ref('publish')

/* ---------------- 发布审计 ---------------- */
const targetType = ref('PROFILE')
const targetId = ref('')
const targetOptions = ref<any[]>([])
const publications = ref<any[]>([])
const pubLoading = ref(false)

function actionLabel(action: string) {
  if (action === 'ROLLBACK') return '回滚'
  if (action === 'PUBLISH') return '发布'
  return action || '—'
}

async function loadTargetOptions() {
  targetOptions.value = []
  targetId.value = ''
  publications.value = []
  if (targetType.value === 'PROFILE') {
    const res = await listProfiles({ pageSize: 200 })
    targetOptions.value = (res.rows || []).map((r: any) => ({
      value: String(r.profileId),
      label: `${r.name}（${r.kind} · ${r.scope?.environment || ''}）`
    }))
  } else {
    const res = await listTemplates({ pageSize: 200 })
    targetOptions.value = (res.rows || []).map((r: any) => ({
      value: String(r.templateId),
      label: `${r.name}（${r.provider} · ${r.scene}）`
    }))
  }
}

function onTargetTypeChange() {
  loadTargetOptions()
}

async function loadPublications() {
  if (!targetId.value) return
  pubLoading.value = true
  try {
    const res = targetType.value === 'PROFILE'
      ? await listPublications(targetId.value, { pageSize: 100 })
      : await listTemplatePublications(targetId.value, { pageSize: 100 })
    publications.value = res.rows || []
  } finally {
    pubLoading.value = false
  }
}

/* ---------------- 发送记录 ---------------- */
const statuses = ['PENDING', 'SENDING', 'ACCEPTED', 'DELIVERED', 'FAILED', 'UNKNOWN', 'SKIPPED']
const filter = reactive({ provider: '', status: '', scene: '' })
const deliveries = ref<any[]>([])
const deliveryLoading = ref(false)
const deliveryPage = reactive({ pageNum: 1, pageSize: 20, total: 0 })
const receiptResult = ref<any>(null)

function statusTag(status: string) {
  if (status === 'DELIVERED') return 'success'
  if (status === 'ACCEPTED') return 'primary'
  if (status === 'UNKNOWN') return 'warning'
  if (status === 'FAILED') return 'danger'
  if (status === 'SKIPPED') return 'info'
  return 'info'
}

async function loadDeliveries() {
  deliveryLoading.value = true
  try {
    const res = await listDeliveries({
      provider: filter.provider || undefined,
      status: filter.status || undefined,
      scene: filter.scene || undefined,
      pageNum: deliveryPage.pageNum,
      pageSize: deliveryPage.pageSize
    })
    deliveries.value = res.rows || []
    deliveryPage.total = res.total || 0
  } finally {
    deliveryLoading.value = false
  }
}

async function doQueryReceipt(row: any) {
  try {
    receiptResult.value = await queryReceipt(row.deliveryId, { expectedVersion: row.version })
    await loadDeliveries()
  } catch (e: any) {
    ElMessage.error(e?.message || '查询回执失败')
  }
}

const retryDialog = ref(false)
const retryTarget = ref<any>(null)
const retryReason = ref('')
const retrying = ref(false)

function openRetry(row: any) {
  retryTarget.value = row
  retryReason.value = ''
  retryDialog.value = true
}

async function doRetry() {
  if (retryReason.value.trim().length < 2) {
    ElMessage.warning('请填写重试原因')
    return
  }
  retrying.value = true
  try {
    await retryDelivery(retryTarget.value.deliveryId, {
      expectedVersion: retryTarget.value.version,
      reason: retryReason.value.trim()
    })
    ElMessage.success('已提交重试')
    retryDialog.value = false
    await loadDeliveries()
  } catch (e: any) {
    ElMessage.error(e?.message || '重试失败')
  } finally {
    retrying.value = false
  }
}

/* ---------------- 白名单 ---------------- */
const recipients = ref<any[]>([])
const recipientLoading = ref(false)
const recipientDialog = ref(false)
const savingRecipient = ref(false)
const appProfiles = ref<any[]>([])
const recipientForm = reactive({
  kind: 'PHONE', name: '', phone: '', accountId: '', appProfileId: '', environment: 'TEST', reason: ''
})

async function loadRecipients() {
  recipientLoading.value = true
  try {
    const res = await listTestRecipients({})
    recipients.value = res.rows || []
  } finally {
    recipientLoading.value = false
  }
}

async function loadAppProfiles() {
  const res = await listProfiles({ kind: 'WECHAT_APP', pageSize: 200 })
  appProfiles.value = (res.rows || []).map((r: any) => ({
    value: String(r.profileId),
    label: `${r.name}（${r.scope?.environment || ''}）`
  }))
}

function openAddRecipient() {
  recipientForm.kind = 'PHONE'
  recipientForm.name = ''
  recipientForm.phone = ''
  recipientForm.accountId = ''
  recipientForm.appProfileId = ''
  recipientForm.environment = 'TEST'
  recipientForm.reason = ''
  recipientDialog.value = true
}

async function doAddRecipient() {
  savingRecipient.value = true
  try {
    await addTestRecipient({ ...recipientForm })
    ElMessage.success('已登记白名单')
    recipientDialog.value = false
    await loadRecipients()
  } catch (e: any) {
    ElMessage.error(e?.message || '登记失败')
  } finally {
    savingRecipient.value = false
  }
}

async function doRevoke(row: any) {
  await ElMessageBox.confirm(`确认撤销白名单「${row.name}」？撤销后该号码不再接收真实测试发送。`, '撤销白名单', {
    type: 'warning'
  })
  await revokeTestRecipient(row.recipientId)
  ElMessage.success('已撤销')
  await loadRecipients()
}

onMounted(async () => {
  await loadTargetOptions()
  await loadDeliveries()
  await loadRecipients()
  await loadAppProfiles()
})
</script>

<style scoped>
.intro { margin-bottom: 12px; }
.title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.hint { font-size: 12px; line-height: 1.7; color: var(--el-text-color-secondary); }
.bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>
