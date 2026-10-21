<template>
  <div class="app-container">
    <el-tabs v-model="active" @tab-change="onTabChange">
      <!-- 隐私与数据请求 -->
      <el-tab-pane label="隐私与数据请求" name="privacy">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="privacyQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadPrivacy">
              <el-option label="已提交" value="APPLIED" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已处理" value="COMPLETED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadPrivacy">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="privacyRows" border>
          <el-table-column label="案件号" prop="caseId" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'REJECTED' ? 'info' : 'warning'">
                {{ row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="关联订单" prop="orderNo" width="180" />
          <el-table-column label="请求内容" prop="reason" min-width="240" show-overflow-tooltip />
          <el-table-column label="请求账号" prop="requesterAccountId" width="120" />
          <el-table-column label="提交时间" prop="createdAt" width="180" />
          <el-table-column label="处理时限" prop="expiresAt" width="180" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 'COMPLETED' || row.status === 'REJECTED'" @click="openPrivacy(row)">
                处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="privacyQuery.pageNum"
          v-model:page-size="privacyQuery.pageSize"
          :total="privacyTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadPrivacy"
          @size-change="loadPrivacy"
        />
      </el-tab-pane>

      <!-- 敏感数据导出审批 -->
      <el-tab-pane label="敏感数据导出" name="export">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="exportQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadExports">
              <el-option label="已申请" value="APPLIED" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Plus" @click="openExportApply">申请导出</el-button>
            <el-button icon="Refresh" @click="loadExports">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          class="mb8"
          type="warning"
          :closable="false"
          show-icon
          title="导出数据一律脱敏：车牌保留前后各 2 位，联系人只保留姓氏；批准后 24 小时内可查看，超时自动失效。"
        />

        <el-table v-loading="loading" :data="exportRows" border>
          <el-table-column label="任务号" prop="caseId" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'info' : 'warning'">
                {{ row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请原因" prop="reason" min-width="220" show-overflow-tooltip />
          <el-table-column label="申请人" prop="requesterUserId" width="100" />
          <el-table-column label="审批人" prop="reviewerId" width="100" />
          <el-table-column label="申请时间" prop="createdAt" width="180" />
          <el-table-column label="有效期至" prop="expiresAt" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'APPLIED'" @click="openExportAudit(row)">审批</el-button>
              <el-button link type="success" :disabled="row.status !== 'APPROVED'" @click="viewExport(row)">查看数据</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="exportQuery.pageNum"
          v-model:page-size="exportQuery.pageSize"
          :total="exportTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadExports"
          @size-change="loadExports"
        />
      </el-tab-pane>

      <!-- 员工账号绑定 -->
      <el-tab-pane label="员工账号绑定" name="binding">
        <el-form :inline="true" class="mb8">
          <el-form-item>
            <el-button icon="Refresh" @click="loadBindings">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="员工在小程序提交绑定申请后，由管理员签发一次性凭证（8 位数字，24 小时有效）；凭证只显示一次，请线下告知员工本人。"
        />

        <el-table v-loading="loading" :data="bindingRows" border>
          <el-table-column label="申请号" prop="id" width="100" />
          <el-table-column label="申请码" prop="request_code" width="180" show-overflow-tooltip />
          <el-table-column label="申请账号" prop="nickname" width="160" />
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CREDENTIAL_ISSUED' ? 'success' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请说明" prop="request_reason" min-width="200" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="created_at" width="180" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'REQUESTED'" @click="openIssue(row)">签发凭证</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 隐私处理 -->
    <el-dialog v-model="privacyVisible" title="处理隐私请求" width="480px">
      <el-form label-width="90px">
        <el-form-item label="案件号">
          <span>{{ privacyForm.caseId }}</span>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-select v-model="privacyForm.status" style="width: 100%">
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已处理" value="COMPLETED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="privacyForm.conclusion" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="privacyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPrivacy">提交</el-button>
      </template>
    </el-dialog>

    <!-- 导出审批 -->
    <el-dialog v-model="exportAuditVisible" title="导出审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="任务号">
          <span>{{ exportForm.caseId }}</span>
        </el-form-item>
        <el-form-item label="审批结果">
          <el-radio-group v-model="exportForm.approve">
            <el-radio :value="true">批准</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="exportForm.opinion" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportAuditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExportAudit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 申请导出 -->
    <el-dialog v-model="exportApplyVisible" title="申请导出订单数据" width="520px">
      <el-form label-width="90px">
        <el-form-item label="起始日期">
          <el-date-picker v-model="exportApply.from" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="exportApply.to" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-input v-model="exportApply.status" placeholder="留空表示全部，例如 COMPLETED" />
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="exportApply.reason" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportApplyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExportApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 导出数据预览 -->
    <el-dialog v-model="exportViewVisible" title="导出数据（已脱敏）" width="880px">
      <el-alert v-if="exportView.maskingRule" :title="exportView.maskingRule" type="info" :closable="false" show-icon class="mb8" />
      <el-table :data="exportView.rows || []" border size="small" max-height="420">
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="车牌" prop="plateNo" width="120" />
        <el-table-column label="车主" prop="ownerName" width="100" />
        <el-table-column label="状态" prop="fulfillmentStatus" width="140" />
        <el-table-column label="上门时间" prop="pickupStartsAt" width="180" />
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">¥{{ (Number(row.totalFen || 0) / 100).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
      <div class="muted mt8">共 {{ exportView.rowCount || 0 }} 条，数据有效期至 {{ exportView.expiresAt || '-' }}</div>
    </el-dialog>

    <!-- 签发绑定凭证 -->
    <el-dialog v-model="issueVisible" title="签发员工绑定凭证" width="480px">
      <el-alert
        class="mb8"
        type="warning"
        :closable="false"
        show-icon
        title="签发后凭证只显示一次，请立即线下告知员工本人；绑定后需再次签发才能换绑。"
      />
      <el-form label-width="90px">
        <el-form-item label="申请号">
          <span>{{ issueForm.requestId }}</span>
        </el-form-item>
        <el-form-item label="员工账号">
          <el-select v-model="issueForm.employeeUserId" filterable placeholder="选择系统员工" style="width: 100%">
            <el-option
              v-for="u in employees"
              :key="u.userId"
              :label="`${u.nickName || u.userName}（${u.userName}）`"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitIssue">签发</el-button>
      </template>
    </el-dialog>

    <!-- 凭证结果 -->
    <el-dialog v-model="credentialVisible" title="绑定凭证（仅显示一次）" width="440px">
      <div class="code-box">{{ credential.code }}</div>
      <div class="muted mt8">
        员工：{{ credential.employeeName }} · 有效期至 {{ credential.expiresAt }}
      </div>
      <div class="muted mt8">{{ credential.hint }}</div>
      <template #footer>
        <el-button type="danger" plain :loading="saving" @click="revokeCredential">撤销该凭证</el-button>
        <el-button type="primary" @click="credentialVisible = false">我已记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliGovernance" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditExport,
  getExportTask,
  issueBindingCredential,
  listBindingRequests,
  listExports,
  listPrivacyCases,
  requestExport,
  resolvePrivacyCase,
  revokeBindingCredential
} from '@/api/rongli'
import { listUser } from '@/api/system/user'

const active = ref('privacy')
const loading = ref(false)
const saving = ref(false)

const privacyRows = ref<any[]>([])
const privacyTotal = ref(0)
const privacyQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const privacyVisible = ref(false)
const privacyForm = reactive<any>({ caseId: '', status: 'COMPLETED', conclusion: '' })

const exportRows = ref<any[]>([])
const exportTotal = ref(0)
const exportQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const exportAuditVisible = ref(false)
const exportForm = reactive<any>({ caseId: '', approve: true, opinion: '' })
const exportApplyVisible = ref(false)
const exportApply = reactive<any>({ from: '', to: '', status: '', reason: '' })
const exportViewVisible = ref(false)
const exportView = ref<any>({})

const bindingRows = ref<any[]>([])
const issueVisible = ref(false)
const issueForm = reactive<any>({ requestId: '', employeeUserId: null })
const employees = ref<any[]>([])
const credentialVisible = ref(false)
const credential = ref<any>({})

onMounted(() => {
  loadPrivacy()
})

function onTabChange(name: string) {
  if (name === 'privacy') loadPrivacy()
  if (name === 'export') loadExports()
  if (name === 'binding') loadBindings()
}

async function loadPrivacy() {
  loading.value = true
  try {
    const res: any = await listPrivacyCases(privacyQuery)
    privacyRows.value = (res && res.rows) || []
    privacyTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openPrivacy(row: any) {
  privacyForm.caseId = row.caseId
  privacyForm.status = row.status === 'APPLIED' ? 'PROCESSING' : 'COMPLETED'
  privacyForm.conclusion = ''
  privacyVisible.value = true
}

async function submitPrivacy() {
  saving.value = true
  try {
    await resolvePrivacyCase(privacyForm.caseId, {
      status: privacyForm.status,
      conclusion: privacyForm.conclusion
    })
    ElMessage.success('已提交处理结果')
    privacyVisible.value = false
    loadPrivacy()
  } finally {
    saving.value = false
  }
}

async function loadExports() {
  loading.value = true
  try {
    const res: any = await listExports(exportQuery)
    exportRows.value = (res && res.rows) || []
    exportTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openExportAudit(row: any) {
  exportForm.caseId = row.caseId
  exportForm.approve = true
  exportForm.opinion = ''
  exportAuditVisible.value = true
}

async function submitExportAudit() {
  saving.value = true
  try {
    await auditExport(exportForm.caseId, { approve: exportForm.approve, opinion: exportForm.opinion })
    ElMessage.success(exportForm.approve ? '已批准' : '已驳回')
    exportAuditVisible.value = false
    loadExports()
  } finally {
    saving.value = false
  }
}

function openExportApply() {
  exportApply.from = ''
  exportApply.to = ''
  exportApply.status = ''
  exportApply.reason = ''
  exportApplyVisible.value = true
}

async function submitExportApply() {
  if (!exportApply.reason) {
    ElMessage.warning('请填写申请原因')
    return
  }
  saving.value = true
  try {
    await requestExport({ ...exportApply })
    ElMessage.success('已提交，等待另一位管理员审批')
    exportApplyVisible.value = false
    loadExports()
  } finally {
    saving.value = false
  }
}

async function viewExport(row: any) {
  const res: any = await getExportTask(row.caseId)
  exportView.value = res || {}
  exportViewVisible.value = true
}

async function loadBindings() {
  loading.value = true
  try {
    const res: any = await listBindingRequests()
    bindingRows.value = (res && res.rows) || []
    if (!employees.value.length) {
      const page: any = await listUser({ pageNum: 1, pageSize: 200, status: '0' })
      employees.value = (page && page.rows) || []
    }
  } finally {
    loading.value = false
  }
}

function openIssue(row: any) {
  issueForm.requestId = row.id
  issueForm.employeeUserId = null
  issueVisible.value = true
}

async function submitIssue() {
  if (!issueForm.employeeUserId) {
    ElMessage.warning('请选择员工账号')
    return
  }
  saving.value = true
  try {
    const res: any = await issueBindingCredential({
      requestId: issueForm.requestId,
      employeeUserId: issueForm.employeeUserId
    })
    credential.value = res || {}
    issueVisible.value = false
    credentialVisible.value = true
    loadBindings()
  } finally {
    saving.value = false
  }
}

async function revokeCredential() {
  await ElMessageBox.confirm('撤销后该凭证立即失效，员工需重新申请。确认撤销？', '撤销凭证', { type: 'warning' })
  saving.value = true
  try {
    await revokeBindingCredential(credential.value.credentialId)
    ElMessage.success('已撤销')
    credentialVisible.value = false
    loadBindings()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt8 { margin-top: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.muted { color: #909399; font-size: 12px; }
.code-box {
  font-family: Consolas, Monaco, monospace;
  font-size: 34px;
  letter-spacing: 8px;
  text-align: center;
  padding: 18px;
  background: #f5f9ff;
  border: 1px dashed #1f6feb;
  border-radius: 8px;
  color: #1f6feb;
}
</style>
