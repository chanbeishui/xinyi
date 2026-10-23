<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :span="5">
        <el-select v-model="query.status" placeholder="全部状态" clearable class="w100">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-col>
      <el-col :span="19" class="right">
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新建调整</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="本页只记录已确认账单或已有付款记录之后的独立调差；未确认草稿的日常改价请在「所选服务对账单」内完成。原账不覆盖，调整会并入后续单。"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="调整号" prop="id" width="90" />
      <el-table-column label="代驾" prop="driver_name" width="120" />
      <el-table-column label="源任务" min-width="190">
        <template #default="{ row }">
          <div>{{ row.source_order_no || '—' }}</div>
          <div class="muted">{{ entryKindLabel(row.source_entry_kind) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="原费用(元)" width="120" align="right">
        <template #default="{ row }">{{ yuan(row.source_amount_fen) }}</template>
      </el-table-column>
      <el-table-column label="调整金额(元)" width="130" align="right">
        <template #default="{ row }">
          <span :class="Number(row.amount_fen) >= 0 ? 'up' : 'down'">{{ signedYuan(row.amount_fen) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="原因 / 依据" min-width="220">
        <template #default="{ row }">
          <div class="reason">{{ row.reason }}</div>
          <div class="muted">凭证 {{ evidenceText(row.evidence_ids) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核记录" min-width="180">
        <template #default="{ row }">
          <div class="muted">经办 {{ row.created_by || '—' }}</div>
          <div class="muted">审核 {{ row.reviewer_id || '—' }}</div>
          <div class="muted" v-if="row.opinion">{{ row.opinion }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" link type="success" @click="openAudit(row)">审核</el-button>
          <span v-else class="muted">已处理</span>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无调整记录" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="search"
    />

    <el-dialog v-model="createVisible" title="新建对账调整" width="600px">
      <el-form label-width="130px">
        <el-form-item label="源费用明细 ID" required>
          <el-input v-model="form.sourceEntryId" placeholder="费用台账明细 ID（可在对账单详情中查看）" />
        </el-form-item>
        <el-form-item label="调整金额(元)" required>
          <el-input-number v-model="form.amountYuan" :precision="2" :step="10" />
          <span class="muted ml8">正数为补付、负数为扣减，不能为 0</span>
        </el-form-item>
        <el-form-item label="调整原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="写明业务依据，将随调整一起留痕" />
        </el-form-item>
        <el-form-item label="凭证文件 ID" required>
          <el-input v-model="form.evidenceIds" placeholder='JSON 数组，例如 [201,202]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="审核调整" width="520px">
      <el-form label-width="110px">
        <el-form-item label="结论">
          <el-radio-group v-model="auditForm.approve">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" placeholder="驳回必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliAdjustment" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditFeeAdjustment, createFeeAdjustment, listFeeAdjustments } from '@/api/rongli'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })

const createVisible = ref(false)
const form = reactive<any>({ sourceEntryId: '', amountYuan: 0, reason: '', evidenceIds: '' })

const auditVisible = ref(false)
const auditForm = reactive<any>({ adjustmentId: '', approve: true, opinion: '' })

const KIND_LABEL: Record<string, string> = {
  NORMAL_RETURN: '正常归还',
  CANCEL_RETURN: '取消后归还',
  CANCEL_BEFORE_PICKUP: '接车前取消',
  ADJUSTMENT: '调整条目',
  INITIAL_VALUATION: '初始核价',
  PRICE_DELTA: '改价差额'
}

onMounted(load)

function search() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await listFeeAdjustments({
      status: query.status || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { sourceEntryId: '', amountYuan: 0, reason: '', evidenceIds: '' })
  createVisible.value = true
}

async function submitCreate() {
  if (!form.sourceEntryId.trim()) {
    ElMessage.warning('请填写源费用明细 ID')
    return
  }
  if (!form.amountYuan) {
    ElMessage.warning('调整金额不能为 0')
    return
  }
  if (!form.reason.trim()) {
    ElMessage.warning('请填写调整原因')
    return
  }
  if (!form.evidenceIds.trim()) {
    ElMessage.warning('调整必须提供凭证')
    return
  }
  saving.value = true
  try {
    await createFeeAdjustment({
      sourceEntryId: form.sourceEntryId.trim(),
      amountFen: Math.round(Number(form.amountYuan) * 100),
      reason: form.reason,
      evidenceIds: form.evidenceIds
    })
    ElMessage.success('调整已提交，等待独立审核')
    createVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openAudit(row: any) {
  auditForm.adjustmentId = row.id
  auditForm.approve = true
  auditForm.opinion = ''
  auditVisible.value = true
}

async function submitAudit() {
  if (!auditForm.approve && !auditForm.opinion.trim()) {
    ElMessage.warning('驳回必须填写意见')
    return
  }
  saving.value = true
  try {
    await auditFeeAdjustment(auditForm.adjustmentId, {
      approve: auditForm.approve,
      opinion: auditForm.opinion || undefined
    })
    ElMessage.success('审核完成')
    auditVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function evidenceText(raw: any) {
  if (!raw) return '—'
  if (Array.isArray(raw)) return raw.length ? raw.join(', ') : '—'
  return String(raw).replace(/[[\]"]/g, '') || '—'
}

function entryKindLabel(kind: string) {
  return KIND_LABEL[kind] || kind || '—'
}

function statusTag(v: string) {
  if (v === 'APPROVED') return 'success'
  if (v === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(v: string) {
  if (v === 'APPROVED') return '已通过'
  if (v === 'REJECTED') return '已驳回'
  if (v === 'PENDING') return '待审核'
  return v || '—'
}

function yuan(fen: any) {
  if (fen === null || fen === undefined || fen === '') return '—'
  return (Number(fen) / 100).toFixed(2)
}

function signedYuan(fen: any) {
  const n = Number(fen || 0)
  return `${n > 0 ? '+' : ''}${(n / 100).toFixed(2)}`
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.ml8 { margin-left: 8px; }
.w100 { width: 100%; }
.right { text-align: right; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
.reason { line-height: 1.5; }
.up { color: var(--el-color-danger); font-weight: 600; }
.down { color: var(--el-color-success); font-weight: 600; }
</style>
