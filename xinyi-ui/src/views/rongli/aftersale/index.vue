<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="待审核" value="PENDING_REVIEW" />
          <el-option label="执行中" value="EXECUTING" />
          <el-option label="已关闭" value="CLOSED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.kind" clearable placeholder="全部" style="width: 160px" @change="handleQuery">
          <el-option label="取消并退款" value="CANCEL_REFUND" />
          <el-option label="仅退款" value="REFUND_ONLY" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="售后单号" prop="caseNo" width="180" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="类型" prop="kindLabel" width="120" />
      <el-table-column label="申请(元)" width="110" align="right">
        <template #default="{ row }">{{ fen2yuan(row.requestedFen) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="tagType(row.caseStatus)">{{ row.caseStatusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="还车" prop="returnState" width="130" />
      <el-table-column label="退款" prop="refundState" width="130" />
      <el-table-column label="原因" prop="reason" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row)">审核</el-button>
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

    <el-dialog v-model="reviewVisible" title="售后审核（一次审核完成）" width="600px">
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="一次审核同时决定：是否同意取消、是否需要实还车辆、批准退款金额。审核后按结论自动推进退款。"
        class="mb8"
      />
      <el-descriptions v-if="preview" :column="2" border class="mb8">
        <el-descriptions-item label="可退金额">{{ fen2yuan(preview.refundableFen) }}</el-descriptions-item>
        <el-descriptions-item label="车辆保管">{{ preview.custodyType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ fen2yuan(preview.requestedFen) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ preview.orderStatus }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="130px">
        <el-form-item label="审核结论">
          <el-radio-group v-model="review.approve">
            <el-radio :value="true">同意</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批准退款(元)">
          <el-input-number v-model="review.approvedYuan" :min="0" :step="10" :precision="2" />
        </el-form-item>
        <el-form-item label="是否需要还车">
          <el-switch v-model="review.returnRequired" />
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="review.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交审核</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="售后详情" size="46%">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="售后单号">{{ detail.caseNo }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.kindLabel }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.caseStatusLabel }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ fen2yuan(detail.requestedFen) }}</el-descriptions-item>
        <el-descriptions-item label="批准金额">{{ fen2yuan(detail.approvedFen) }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">{{ detail.refundState }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">审核记录</el-divider>
      <el-timeline>
        <el-timeline-item v-for="r in detail?.reviews || []" :key="r.revision" :timestamp="r.reviewedAt">
          第 {{ r.revision }} 次 · {{ r.decision }} · 批准 {{ fen2yuan(r.approvedFen) }}
        </el-timeline-item>
      </el-timeline>
      <el-input v-model="note" type="textarea" :rows="3" placeholder="补充说明" style="margin-top: 12px" />
      <el-button type="primary" style="margin-top: 12px" @click="addNote">提交说明</el-button>
    </el-drawer>
  </div>
</template>

<script setup name="RongliAfterSale" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addAfterSaleNote,
  getAfterSale,
  listAfterSales,
  reviewAfterSale,
  reviewPreview
} from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', kind: '', pageNum: 1, pageSize: 10 })

const reviewVisible = ref(false)
const preview = ref<any>(null)
const currentCaseId = ref('')
const review = reactive<any>({ approve: true, approvedYuan: 0, returnRequired: false, opinion: '' })

const detailVisible = ref(false)
const detail = ref<any>(null)
const note = ref('')

function fen2yuan(fen: any) {
  return (Number(fen || 0) / 100).toFixed(2)
}

function tagType(s: string) {
  if (s === 'CLOSED') return 'success'
  if (s === 'REJECTED') return 'info'
  if (s === 'EXECUTING') return 'warning'
  return ''
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listAfterSales(query)
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

async function openReview(row: any) {
  currentCaseId.value = row.caseId
  preview.value = await reviewPreview(row.caseId, {})
  review.approve = true
  review.approvedYuan = Number(preview.value.refundableFen || row.requestedFen || 0) / 100
  review.returnRequired = preview.value.returnState === 'WAIT_RETURN'
  review.opinion = ''
  reviewVisible.value = true
}

async function submitReview() {
  await reviewAfterSale(currentCaseId.value, {
    approve: review.approve,
    approvedFen: Math.round(Number(review.approvedYuan || 0) * 100),
    returnRequired: review.returnRequired,
    opinion: review.opinion
  })
  ElMessage.success('审核已提交')
  reviewVisible.value = false
  load()
}

async function openDetail(row: any) {
  detail.value = await getAfterSale(row.caseId)
  note.value = ''
  detailVisible.value = true
}

async function addNote() {
  if (!note.value) return
  await addAfterSaleNote(currentCaseId.value || detail.value.caseId, note.value)
  ElMessage.success('已记录说明')
  note.value = ''
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
