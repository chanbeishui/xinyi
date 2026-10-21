<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 170px" @change="handleQuery">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="内部复核" value="INTERNAL_REVIEW" />
          <el-option label="已发出" value="SENT" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已作废" value="VOID" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新建对账单</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="对账单号" prop="statementNo" width="190" />
      <el-table-column label="代驾" prop="driverName" width="130" />
      <el-table-column label="周期" min-width="200">
        <template #default="{ row }">{{ row.periodStart || '-' }} ~ {{ row.periodEnd || '-' }}</template>
      </el-table-column>
      <el-table-column label="版本" prop="revision" width="80" align="center" />
      <el-table-column label="金额(元)" width="120" align="right">
        <template #default="{ row }">{{ fen2yuan(row.amountFen) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="submitReview(row)">提交复核</el-button>
          <el-button v-if="row.status === 'INTERNAL_REVIEW'" link type="success" @click="review(row, true)">复核通过</el-button>
          <el-button v-if="row.status === 'INTERNAL_REVIEW'" link type="danger" @click="review(row, false)">退回</el-button>
          <el-button v-if="row.status === 'INTERNAL_REVIEW'" link type="primary" @click="send(row)">发出</el-button>
          <el-button v-if="row.status !== 'VOID' && row.status !== 'CONFIRMED'" link type="warning" @click="voidIt(row)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="load"
    />

    <el-dialog v-model="createVisible" title="新建对账单" width="620px">
      <el-form label-width="120px">
        <el-form-item label="代驾 ID">
          <el-input v-model="createForm.driverId" placeholder="代驾档案 ID" />
        </el-form-item>
        <el-form-item label="费用条目">
          <el-input
            v-model="createForm.entryIds"
            type="textarea"
            :rows="3"
            placeholder="条目 ID，逗号分隔；留空表示按代驾全部未结算条目生成"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button @click="doPreview">试算</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="对账单详情" size="52%">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="对账单号">{{ detail.statementNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="金额(元)">{{ fen2yuan(detail.amountFen) }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.revision }}</el-descriptions-item>
        <el-descriptions-item label="快照哈希" :span="2">{{ detail.snapshotHash || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">明细</el-divider>
      <el-table :data="detail?.lines || []" border size="small" max-height="420">
        <el-table-column label="订单号" prop="orderNo" width="170" />
        <el-table-column label="业务日" prop="businessDate" width="120" />
        <el-table-column label="金额(元)" width="110" align="right">
          <template #default="{ row }">{{ fen2yuan(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column label="说明" prop="note" min-width="160" />
      </el-table>
      <el-divider content-position="left">复核记录</el-divider>
      <el-timeline>
        <el-timeline-item v-for="(r, i) in detail?.reviews || []" :key="i" :timestamp="r.reviewedAt">
          {{ r.decision }} · {{ r.opinion || '' }}
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup name="RongliStatement" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createStatement,
  getStatement,
  listStatements,
  previewStatement,
  reviewStatement,
  sendStatement,
  submitStatementReview,
  voidStatement
} from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })

const createVisible = ref(false)
const createForm = reactive<any>({ driverId: '', entryIds: '' })

const detailVisible = ref(false)
const detail = ref<any>(null)

function fen2yuan(fen: any) {
  return (Number(fen || 0) / 100).toFixed(2)
}

function statusLabel(s: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    INTERNAL_REVIEW: '内部复核',
    SENT: '已发出',
    CONFIRMED: '已确认',
    VOID: '已作废'
  }
  return map[s] || s || '-'
}

function tagType(s: string) {
  if (s === 'CONFIRMED') return 'success'
  if (s === 'VOID') return 'info'
  if (s === 'INTERNAL_REVIEW') return 'warning'
  if (s === 'SENT') return 'primary'
  return ''
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listStatements(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  load()
}

function openCreate() {
  createForm.driverId = ''
  createForm.entryIds = ''
  createVisible.value = true
}

function buildBody() {
  const body: any = { driverId: createForm.driverId }
  if (createForm.entryIds) {
    body.entryIds = createForm.entryIds
      .split(/[,，\s]+/)
      .filter((x: string) => !!x)
      .map((x: string) => Number(x))
  }
  return body
}

async function doPreview() {
  const res: any = await previewStatement(buildBody())
  ElMessage.success(`试算金额 ${(Number(res.amountFen || 0) / 100).toFixed(2)} 元，共 ${(res.lines || []).length} 条`)
}

async function doCreate() {
  await createStatement(buildBody())
  ElMessage.success('对账单已创建')
  createVisible.value = false
  load()
}

async function openDetail(row: any) {
  detail.value = await getStatement(row.statementId)
  detailVisible.value = true
}

async function submitReview(row: any) {
  await submitStatementReview(row.statementId)
  ElMessage.success('已提交内部复核')
  load()
}

async function review(row: any, approve: boolean) {
  await reviewStatement(row.statementId, { approve, opinion: approve ? '复核无误' : '存在差异，退回修改' })
  ElMessage.success('复核完成')
  load()
}

async function send(row: any) {
  await ElMessageBox.confirm('发出后代驾可在小程序核对并确认，是否继续？', '发出对账单', { type: 'warning' })
  await sendStatement(row.statementId)
  ElMessage.success('已发出')
  load()
}

async function voidIt(row: any) {
  await ElMessageBox.confirm('作废后不可恢复，是否继续？', '作废对账单', { type: 'warning' })
  await voidStatement(row.statementId)
  ElMessage.success('已作废')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
