<template>
  <div class="app-container">
    <el-tabs v-model="active" @tab-change="onTabChange">
      <el-tab-pane label="服务争议" name="dispute">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="disputeQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadDisputes">
              <el-option label="待处理" value="OPEN" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadDisputes">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="disputeRows" border>
          <el-table-column label="编号" prop="disputeId" width="90" />
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="类型" prop="type" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'RESOLVED' || row.status === 'CLOSED' ? 'success' : 'warning'">
                {{ row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="争议内容" prop="description" min-width="220" show-overflow-tooltip />
          <el-table-column label="处理结论" prop="conclusion" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="createdAt" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 'RESOLVED' || row.status === 'CLOSED'" @click="openDispute(row)">
                处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="disputeQuery.pageNum"
          v-model:page-size="disputeQuery.pageSize"
          :total="disputeTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadDisputes"
          @size-change="loadDisputes"
        />
      </el-tab-pane>

      <el-tab-pane label="服务异常" name="exception">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="exceptionQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadExceptions">
              <el-option label="待处理" value="OPEN" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadExceptions">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="exceptionRows" border>
          <el-table-column label="编号" prop="exceptionId" width="90" />
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="类型" prop="type" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" prop="description" min-width="240" show-overflow-tooltip />
          <el-table-column label="处理结果" prop="resolution" min-width="200" show-overflow-tooltip />
          <el-table-column label="上报时间" prop="createdAt" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 'RESOLVED'" @click="openException(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="exceptionQuery.pageNum"
          v-model:page-size="exceptionQuery.pageSize"
          :total="exceptionTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadExceptions"
          @size-change="loadExceptions"
        />
      </el-tab-pane>

      <el-tab-pane label="结果更正" name="correction">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="correctionQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadCorrections">
              <el-option label="待审核" value="APPLIED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadCorrections">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="correctionRows" border>
          <el-table-column label="编号" prop="correctionId" width="90" />
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'info' : 'warning'">
                {{ row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更正原因" prop="reason" min-width="220" show-overflow-tooltip />
          <el-table-column label="更正内容" min-width="240">
            <template #default="{ row }">
              <span class="mono">{{ row.replacement ? JSON.stringify(row.replacement) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审核意见" prop="opinion" min-width="160" show-overflow-tooltip />
          <el-table-column label="申请时间" prop="createdAt" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'APPLIED'" @click="openCorrection(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="correctionQuery.pageNum"
          v-model:page-size="correctionQuery.pageSize"
          :total="correctionTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadCorrections"
          @size-change="loadCorrections"
        />
      </el-tab-pane>

      <el-tab-pane label="支付对账异常" name="reconcile">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="reconcileQuery.status" clearable placeholder="全部" style="width: 150px" @change="loadReconciliation">
              <el-option label="待处理" value="OPEN" />
              <el-option label="核查中" value="CHECKING" />
              <el-option label="已解决" value="RESOLVED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadReconciliation">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="对账异常只做核查与结论记录，不发起退款；退款统一走「取消与退款」一次性审核。"
        />

        <el-table v-loading="loading" :data="reconcileRows" border>
          <el-table-column label="案件号" prop="caseId" width="90" />
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" prop="reason" min-width="240" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="createdAt" width="180" />
          <el-table-column label="处理时限" prop="expiresAt" width="180" />
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="queryChannel(row)">通道查询</el-button>
              <el-button link type="primary" :disabled="row.status === 'RESOLVED'" @click="openReconcile(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="reconcileQuery.pageNum"
          v-model:page-size="reconcileQuery.pageSize"
          :total="reconcileTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadReconciliation"
          @size-change="loadReconciliation"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 争议处理 -->
    <el-dialog v-model="disputeVisible" title="处理服务争议" width="480px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ disputeForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="处理结论">
          <el-input v-model="disputeForm.conclusion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="双方沟通结果与处理方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDispute">提交</el-button>
      </template>
    </el-dialog>

    <!-- 异常处理 -->
    <el-dialog v-model="exceptionVisible" title="处理服务异常" width="480px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ exceptionForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="exceptionForm.resolution" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exceptionVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitException">提交</el-button>
      </template>
    </el-dialog>

    <!-- 更正审核 -->
    <el-dialog v-model="correctionVisible" title="结果更正审核" width="480px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ correctionForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="correctionForm.approve">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="correctionForm.opinion" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="correctionVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCorrection">提交</el-button>
      </template>
    </el-dialog>

    <!-- 对账异常处理 -->
    <el-dialog v-model="reconcileVisible" title="支付对账异常处理" width="480px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ reconcileForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="处理结论">
          <el-input v-model="reconcileForm.conclusion" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reconcileVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReconcile">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliException" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditCorrection,
  listCorrections,
  listDisputes,
  listExceptions,
  listReconciliation,
  queryReconciliation,
  resolveDispute,
  resolveException,
  resolveReconciliation
} from '@/api/rongli'

const active = ref('dispute')
const loading = ref(false)
const saving = ref(false)

const disputeRows = ref<any[]>([])
const disputeTotal = ref(0)
const disputeQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const disputeVisible = ref(false)
const disputeForm = reactive<any>({ disputeId: '', orderNo: '', conclusion: '' })

const exceptionRows = ref<any[]>([])
const exceptionTotal = ref(0)
const exceptionQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const exceptionVisible = ref(false)
const exceptionForm = reactive<any>({ exceptionId: '', orderNo: '', resolution: '' })

const correctionRows = ref<any[]>([])
const correctionTotal = ref(0)
const correctionQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const correctionVisible = ref(false)
const correctionForm = reactive<any>({ correctionId: '', orderNo: '', approve: true, opinion: '' })

const reconcileRows = ref<any[]>([])
const reconcileTotal = ref(0)
const reconcileQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const reconcileVisible = ref(false)
const reconcileForm = reactive<any>({ caseId: '', orderNo: '', conclusion: '' })

onMounted(() => {
  loadDisputes()
})

function onTabChange(name: string) {
  if (name === 'dispute') loadDisputes()
  if (name === 'exception') loadExceptions()
  if (name === 'correction') loadCorrections()
  if (name === 'reconcile') loadReconciliation()
}

async function loadDisputes() {
  loading.value = true
  try {
    const res: any = await listDisputes(disputeQuery)
    disputeRows.value = (res && res.rows) || []
    disputeTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openDispute(row: any) {
  disputeForm.disputeId = row.disputeId
  disputeForm.orderNo = row.orderNo
  disputeForm.conclusion = row.conclusion || ''
  disputeVisible.value = true
}

async function submitDispute() {
  if (!disputeForm.conclusion) {
    ElMessage.warning('请填写处理结论')
    return
  }
  saving.value = true
  try {
    await resolveDispute(disputeForm.disputeId, { conclusion: disputeForm.conclusion })
    ElMessage.success('已处理')
    disputeVisible.value = false
    loadDisputes()
  } finally {
    saving.value = false
  }
}

async function loadExceptions() {
  loading.value = true
  try {
    const res: any = await listExceptions(exceptionQuery)
    exceptionRows.value = (res && res.rows) || []
    exceptionTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openException(row: any) {
  exceptionForm.exceptionId = row.exceptionId
  exceptionForm.orderNo = row.orderNo
  exceptionForm.resolution = row.resolution || ''
  exceptionVisible.value = true
}

async function submitException() {
  if (!exceptionForm.resolution) {
    ElMessage.warning('请填写处理结果')
    return
  }
  saving.value = true
  try {
    await resolveException(exceptionForm.exceptionId, { resolution: exceptionForm.resolution })
    ElMessage.success('已处理')
    exceptionVisible.value = false
    loadExceptions()
  } finally {
    saving.value = false
  }
}

async function loadCorrections() {
  loading.value = true
  try {
    const res: any = await listCorrections(correctionQuery)
    correctionRows.value = (res && res.rows) || []
    correctionTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openCorrection(row: any) {
  correctionForm.correctionId = row.correctionId
  correctionForm.orderNo = row.orderNo
  correctionForm.approve = true
  correctionForm.opinion = ''
  correctionVisible.value = true
}

async function submitCorrection() {
  saving.value = true
  try {
    await auditCorrection(correctionForm.correctionId, {
      approve: correctionForm.approve,
      opinion: correctionForm.opinion
    })
    ElMessage.success(correctionForm.approve ? '已通过' : '已驳回')
    correctionVisible.value = false
    loadCorrections()
  } finally {
    saving.value = false
  }
}

async function loadReconciliation() {
  loading.value = true
  try {
    const res: any = await listReconciliation(reconcileQuery)
    reconcileRows.value = (res && res.rows) || []
    reconcileTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

async function queryChannel(row: any) {
  saving.value = true
  try {
    const res: any = await queryReconciliation(row.caseId)
    ElMessageBox.alert(
      `<pre style="white-space:pre-wrap;font-size:12px;margin:0">${JSON.stringify(res, null, 2)}</pre>`,
      '支付通道查询结果',
      { dangerouslyUseHTMLString: true }
    )
  } finally {
    saving.value = false
  }
}

function openReconcile(row: any) {
  reconcileForm.caseId = row.caseId
  reconcileForm.orderNo = row.orderNo
  reconcileForm.conclusion = ''
  reconcileVisible.value = true
}

async function submitReconcile() {
  if (!reconcileForm.conclusion) {
    ElMessage.warning('请填写处理结论')
    return
  }
  saving.value = true
  try {
    await resolveReconciliation(reconcileForm.caseId, { conclusion: reconcileForm.conclusion })
    ElMessage.success('已处理')
    reconcileVisible.value = false
    loadReconciliation()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.mono { font-family: Consolas, Monaco, monospace; font-size: 12px; word-break: break-all; }
</style>
