<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="代驾">
        <el-select
          v-model="driverId"
          filterable
          clearable
          placeholder="选择代驾（可按姓名搜索）"
          style="width: 240px"
          @change="onDriverChange"
        >
          <el-option
            v-for="d in drivers"
            :key="d.driver_id"
            :label="`${d.nickname || '未命名'} · ${d.driver_id}`"
            :value="String(d.driver_id)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="服务日期">
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="loadEntries">查询明细</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="日期只做筛选，不改变归属：账单始终按同一代驾 + 同一付款主体 + 同一币种生成；已纳入其他账单的明细不会再出现，禁止重复纳入。"
    />

    <el-empty v-if="!driverId" description="请先选择代驾" />

    <template v-else>
      <el-row :gutter="12" class="mb12">
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num">{{ overview.pendingEntryCount ?? 0 }}</div>
            <div class="stat-label">可纳入明细</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num warn">{{ overview.unpricedCount ?? 0 }}</div>
            <div class="stat-label">待核价</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num">{{ yuan(overview.confirmedFen) }}</div>
            <div class="stat-label">已确认金额(元)</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num primary">{{ yuan(overview.confirmedUnpaidFen) }}</div>
            <div class="stat-label">已确认未付(元)</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num">{{ yuan(overview.pendingEvidenceFen) }}</div>
            <div class="stat-label">凭证待核实(元)</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat">
            <div class="stat-num">{{ yuan(overview.verifiedPaidFen) }}</div>
            <div class="stat-label">已核实已付(元)</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="mb12">
        <template #header>
          <div class="card-head">
            <span>费用明细（勾选后可生成对账单）</span>
            <div>
              <span class="muted">已选 {{ selected.length }} 条 · 合计 {{ yuan(selectedFen) }} 元</span>
              <el-button class="ml8" size="small" @click="preview">预览</el-button>
              <el-button class="ml8" size="small" type="primary" :disabled="!selected.length" @click="generate">
                生成对账单
              </el-button>
            </div>
          </div>
        </template>
        <el-table
          ref="tableRef"
          v-loading="loadingEntries"
          :data="entries"
          border
          row-key="entryId"
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="46" :selectable="(row: any) => row.selectable" />
          <el-table-column label="订单号" prop="orderNo" min-width="170" />
          <el-table-column label="车牌" prop="plateNo" width="110" />
          <el-table-column label="服务结束" width="150">
            <template #default="{ row }">{{ fmt(row.serviceEndedAt) }}</template>
          </el-table-column>
          <el-table-column label="条目类型" width="130">
            <template #default="{ row }">{{ row.entryKindLabel }}</template>
          </el-table-column>
          <el-table-column label="核价状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.pricingStatus === 'READY' ? 'success' : 'warning'" size="small">
                {{ row.pricingStatus === 'READY' ? '已核价' : '待核价' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="费用(元)" width="110" align="right">
            <template #default="{ row }">{{ yuan(row.amountFen) }}</template>
          </el-table-column>
          <el-table-column label="已入账单" min-width="160">
            <template #default="{ row }">
              <span v-if="row.statementNo">{{ row.statementNo }}（{{ row.statementStatus }}）</span>
              <span v-else class="muted">未纳入</span>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="该代驾在筛选区间内没有费用明细" />
          </template>
        </el-table>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-head">
            <span>账单历史与付款状态</span>
            <el-button size="small" icon="Refresh" @click="loadStatements">刷新</el-button>
          </div>
        </template>
        <el-table v-loading="loadingStatements" :data="statements" border>
          <el-table-column label="对账单号" prop="statementNo" width="190" />
          <el-table-column label="周期" min-width="190">
            <template #default="{ row }">{{ row.periodStart || '—' }} ~ {{ row.periodEnd || '—' }}</template>
          </el-table-column>
          <el-table-column label="版本" prop="revision" width="80" align="center" />
          <el-table-column label="金额(元)" width="120" align="right">
            <template #default="{ row }">{{ yuan(row.amountFen) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="stmtTag(row.status)">{{ stmtLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openStatement(row)">查看</el-button>
              <el-button
                v-if="row.status === 'CONFIRMED'"
                link
                type="success"
                @click="goPayment(row)"
              >
                登记付款
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="该代驾还没有对账单" />
          </template>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="previewVisible" title="对账单预览" width="620px">
      <el-descriptions v-if="previewData" :column="2" border size="small">
        <el-descriptions-item label="代驾">{{ overview.driverName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="明细条数">{{ previewData.count ?? selected.length }}</el-descriptions-item>
        <el-descriptions-item label="已知合计(元)">{{ yuan(previewData.knownFen) }}</el-descriptions-item>
        <el-descriptions-item label="待核价条数">
          <span :class="previewData.unpricedCount > 0 ? 'warn-text' : ''">{{ previewData.unpricedCount ?? 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-table v-if="previewData && previewData.lines" :data="previewData.lines" border size="small" class="mt8" max-height="260">
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="车牌" prop="plateNo" width="100" />
        <el-table-column label="类型" prop="entryKindLabel" width="120" />
        <el-table-column label="金额(元)" width="100" align="right">
          <template #default="{ row }">{{ yuan(row.amountFen) }}</template>
        </el-table-column>
      </el-table>
      <div class="muted mt8">预览不落库；待核价条目不会计入合计，需先完成核价再组单。</div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="generate">生成对账单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliReconcileSummary" lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createStatement,
  driverReconciliationEntries,
  driverReconciliationOverview,
  listDrivers,
  listStatements,
  previewStatement
} from '@/api/rongli'

const router = useRouter()
const drivers = ref<any[]>([])
const driverId = ref('')
const range = ref<any>(null)
const overview = ref<any>({})
const entries = ref<any[]>([])
const statements = ref<any[]>([])
const selected = ref<any[]>([])
const tableRef = ref()

const loadingEntries = ref(false)
const loadingStatements = ref(false)
const previewVisible = ref(false)
const previewData = ref<any>(null)

const selectedFen = computed(() =>
  selected.value.reduce((sum, r) => sum + Number(r.amountFen || 0), 0)
)

onMounted(async () => {
  const res: any = await listDrivers({ pageNum: 1, pageSize: 200 })
  drivers.value = (res && res.rows) || []
})

async function onDriverChange() {
  selected.value = []
  entries.value = []
  statements.value = []
  overview.value = {}
  if (!driverId.value) return
  overview.value = (await driverReconciliationOverview(driverId.value)) || {}
  await Promise.all([loadEntries(), loadStatements()])
}

async function loadEntries() {
  if (!driverId.value) return
  loadingEntries.value = true
  try {
    const res: any = await driverReconciliationEntries(driverId.value, {
      from: range.value ? range.value[0] : undefined,
      to: range.value ? range.value[1] : undefined,
      onlySelectable: false
    })
    entries.value = (res && res.rows) || []
  } finally {
    loadingEntries.value = false
  }
}

async function loadStatements() {
  if (!driverId.value) return
  loadingStatements.value = true
  try {
    const res: any = await listStatements({ driverId: driverId.value, pageNum: 1, pageSize: 50 })
    statements.value = (res && res.rows) || []
  } finally {
    loadingStatements.value = false
  }
}

function onSelectionChange(rows: any[]) {
  selected.value = rows
}

function entryIds() {
  return selected.value.map((r) => r.entryId)
}

async function preview() {
  if (!selected.value.length) {
    ElMessage.warning('请先勾选明细')
    return
  }
  previewData.value = await previewStatement({ driverId: driverId.value, entryIds: entryIds() })
  previewVisible.value = true
}

async function generate() {
  if (!selected.value.length) {
    ElMessage.warning('请先勾选明细')
    return
  }
  await createStatement({ driverId: driverId.value, entryIds: entryIds() })
  ElMessage.success('对账单草稿已生成，请到「所选服务对账单」继续复核与发出')
  previewVisible.value = false
  selected.value = []
  await Promise.all([onDriverChange(), loadStatements()])
}

function openStatement(row: any) {
  router.push({ path: '/rongli/people/statement', query: { statementId: row.statementId } })
}

function goPayment(row: any) {
  router.push({ path: '/rongli/people/paymentVerify', query: { statementId: row.statementId } })
}

function stmtTag(v: string) {
  if (v === 'CONFIRMED') return 'success'
  if (v === 'VOID') return 'info'
  if (v === 'INTERNAL_REVIEW') return 'warning'
  return ''
}

function stmtLabel(v: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    INTERNAL_REVIEW: '内部复核',
    SENT: '已发出',
    CONFIRMED: '已确认',
    VOID: '已作废'
  }
  return map[v] || v || '—'
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
.mb12 { margin-bottom: 12px; }
.mt8 { margin-top: 8px; }
.ml8 { margin-left: 8px; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
.stat { text-align: center; }
.stat-num { font-size: 20px; font-weight: 600; line-height: 1.4; }
.stat-num.warn { color: var(--el-color-warning); }
.stat-num.primary { color: var(--el-color-primary); }
.stat-label { font-size: 12px; color: var(--el-text-color-secondary); }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.warn-text { color: var(--el-color-warning); font-weight: 600; }
</style>
