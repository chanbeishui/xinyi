<template>
  <RongliPage title="外部付款核对" subtitle="记录已有付款凭证；这里不会发起银行或微信转账。">
    <el-row :gutter="12" class="mb8">
      <el-col :span="5">
        <el-select v-model="query.status" placeholder="全部状态" clearable class="w100">
          <el-option label="待核实" value="PENDING" />
          <el-option label="已核实" value="VERIFIED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-col>
      <el-col :span="19" class="right">
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button type="success" icon="Plus" @click="openRegister">登记付款证据</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-alert
      class="mb8"
      type="warning"
      :closable="false"
      show-icon
      title="本系统不发起打款：这里只登记外部已发生的付款证据。未上传凭证或未通过独立复核的记录，一律不计入「已付」。"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="对账单号" prop="statementNo" width="190" />
      <el-table-column label="代驾" prop="driverName" width="120" />
      <el-table-column label="账单版本" width="90" align="center">
        <template #default="{ row }">v{{ row.statement_revision ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="付款金额(元)" width="130" align="right">
        <template #default="{ row }">{{ yuan(row.amount_fen) }}</template>
      </el-table-column>
      <el-table-column label="付款时间" width="150">
        <template #default="{ row }">{{ fmt(row.paid_at) }}</template>
      </el-table-column>
      <el-table-column label="外部付款引用" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">已加密存储（不可回显）</template>
      </el-table-column>
      <el-table-column label="凭证" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="hasEvidence(row) ? 'success' : 'danger'" size="small">
            {{ hasEvidence(row) ? '已附' : '缺失' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登记 / 核实" width="140">
        <template #default="{ row }">
          <div class="muted">登记 {{ row.registered_by || '—' }}</div>
          <div class="muted">核实 {{ row.verified_by || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看记录</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="success"
            @click="openVerify(row)"
          >
            独立复核
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无付款证据记录" />
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

    <el-dialog v-model="registerVisible" title="登记外部付款证据" width="600px">
      <el-form label-width="120px">
        <el-form-item label="对账单" required>
          <el-select v-model="form.statementId" filterable placeholder="选择已确认对账单" class="w100">
            <el-option
              v-for="s in statements"
              :key="s.statementId"
              :label="`${s.statementNo} · ${s.driverName} · ${yuan(s.amountFen)} 元`"
              :value="s.statementId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付款金额(元)" required>
          <el-input-number v-model="form.amountYuan" :min="0.01" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="付款时间">
          <el-date-picker v-model="form.paidAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="外部付款引用">
          <el-input v-model="form.reference" placeholder="银行流水号 / 转账备注等可追溯编号" />
        </el-form-item>
        <el-form-item label="凭证文件 ID" required>
          <el-input v-model="form.evidenceIds" placeholder='JSON 数组，例如 [101,102]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRegister">登记</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="verifyVisible" title="独立复核" width="520px">
      <el-form label-width="110px">
        <el-form-item label="处理结论">
          <el-radio-group v-model="verifyForm.verified">
            <el-radio :label="true">核实通过</el-radio>
            <el-radio :label="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="verifyForm.reason" type="textarea" :rows="3" placeholder="驳回必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitVerify">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="付款记录" size="480px">
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="记录号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="对账单">{{ detail.statementId }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ yuan(detail.amountFen) }} 元</el-descriptions-item>
        <el-descriptions-item label="付款时间">{{ fmt(detail.paid_at) }}</el-descriptions-item>
        <el-descriptions-item label="外部引用">已加密存储（不可回显）</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="说明">{{ detail.reason || '—' }}</el-descriptions-item>
        <el-descriptions-item label="提示">{{ detail.hint }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliPaymentVerify" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createDriverPayment,
  getDriverPayment,
  listDriverPayments,
  listStatements,
  verifyDriverPayment
} from '@/api/rongli'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const statements = ref<any[]>([])
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })

const registerVisible = ref(false)
const form = reactive<any>({ statementId: '', amountYuan: 0, paidAt: '', reference: '', evidenceIds: '' })

const verifyVisible = ref(false)
const verifyForm = reactive<any>({ recordId: '', verified: true, reason: '' })

const detailVisible = ref(false)
const detail = ref<any>(null)

onMounted(async () => {
  await loadStatements()
  load()
})

async function loadStatements() {
  const res: any = await listStatements({ status: 'CONFIRMED', pageNum: 1, pageSize: 100 })
  statements.value = (res && res.rows) || []
}

function search() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await listDriverPayments({
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

function openRegister() {
  Object.assign(form, { statementId: '', amountYuan: 0, paidAt: '', reference: '', evidenceIds: '' })
  registerVisible.value = true
}

async function submitRegister() {
  if (!form.statementId) {
    ElMessage.warning('请选择对账单')
    return
  }
  if (!form.amountYuan) {
    ElMessage.warning('请填写付款金额')
    return
  }
  if (!form.evidenceIds.trim()) {
    ElMessage.warning('必须提供凭证文件 ID，无凭证不计入已付')
    return
  }
  saving.value = true
  try {
    await createDriverPayment({
      statementId: form.statementId,
      amountFen: Math.round(Number(form.amountYuan) * 100),
      paidAt: form.paidAt || undefined,
      reference: form.reference || undefined,
      evidenceIds: form.evidenceIds
    })
    ElMessage.success('付款证据已登记，等待独立复核')
    registerVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openVerify(row: any) {
  verifyForm.recordId = row.id
  verifyForm.verified = true
  verifyForm.reason = ''
  verifyVisible.value = true
}

async function submitVerify() {
  if (!verifyForm.verified && !verifyForm.reason.trim()) {
    ElMessage.warning('驳回必须填写说明')
    return
  }
  saving.value = true
  try {
    await verifyDriverPayment(verifyForm.recordId, {
      verified: verifyForm.verified,
      reason: verifyForm.reason || undefined
    })
    ElMessage.success('复核完成')
    verifyVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function openDetail(row: any) {
  detail.value = await getDriverPayment(row.id)
  detailVisible.value = true
}

function hasEvidence(row: any) {
  const raw = row.evidence_ids
  if (!raw) return false
  if (Array.isArray(raw)) return raw.length > 0
  const text = String(raw).trim()
  return text !== '' && text !== '[]' && text !== 'null'
}

function statusTag(v: string) {
  if (v === 'VERIFIED') return 'success'
  if (v === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(v: string) {
  if (v === 'VERIFIED') return '已核实'
  if (v === 'REJECTED') return '已驳回'
  if (v === 'PENDING') return '待核实'
  return v || '—'
}

function yuan(fen: any) {
  if (fen === null || fen === undefined || fen === '') return '—'
  return (Number(fen) / 100).toFixed(2)
}

function fmt(v: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '—'
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.w100 { width: 100%; }
.right { text-align: right; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
</style>
