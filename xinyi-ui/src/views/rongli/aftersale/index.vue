<template>
  <RongliPage title="取消与退款" subtitle="一次申请、一次审核，车辆返还与退款执行在同一处跟踪。">
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
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" row-key="caseId">
      <el-table-column label="售后 / 订单" min-width="190">
        <template #default="{ row }">
          <div class="rl-strong">{{ row.caseNo }}</div>
          <div class="rl-mono">{{ row.orderNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="车辆 / 保管" width="150">
        <template #default="{ row }">
          <div class="rl-strong">{{ row.plateNo || '—' }}</div>
          <div class="rl-sub">{{ row.custodyLabel || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="申请类型" prop="kindLabel" width="110" />
      <el-table-column label="取消决定" width="110">
        <template #default="{ row }">
          <span class="rl-tag" :class="decisionTone(row.reviewStatus)">{{ row.decisionLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="150">
        <template #default="{ row }">
          <div class="rl-strong">¥{{ fen2yuan(row.requestedFen) }}</div>
          <div class="rl-sub">批准 ¥{{ fen2yuan(row.approvedFen) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="返还进度" width="110">
        <template #default="{ row }">
          <span class="rl-tag" :class="returnTone(row.returnState)">{{ row.returnStateLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款进度" width="130">
        <template #default="{ row }">
          <span class="rl-tag" :class="refundTone(row.refundState)">{{ row.refundStateLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" width="150">
        <template #default="{ row }">{{ shortTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="原因" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row)">审核</el-button>
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="rl-empty">
          <div class="rl-empty__title">没有符合条件的售后申请</div>
          <div class="rl-empty__hint">取消与退款统一在一份申请中处理，可调整筛选后重试。</div>
          <el-button @click="resetQuery">重置筛选</el-button>
        </div>
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      class="mt16"
      background
      @current-change="load"
      @size-change="handleQuery"
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
  </RongliPage>
</template>

<script setup name="RongliAfterSale" lang="ts">
import RongliPage from '../components/RongliPage.vue'
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

function shortTime(value?: string) {
  if (!value) return '—'
  return String(value).replace('T', ' ').replace('Z', '').slice(0, 16)
}

function decisionTone(review?: string) {
  if (review === 'APPROVED') return 'rl-tag--green'
  if (review === 'REJECTED') return 'rl-tag--red'
  return 'rl-tag--amber'
}

function returnTone(state?: string) {
  if (state === 'RETURNED' || state === 'NOT_REQUIRED') return 'rl-tag--green'
  if (state === 'WAIT_RETURN') return 'rl-tag--amber'
  return 'rl-tag--gray'
}

function refundTone(state?: string) {
  if (state === 'SUCCESS' || state === 'NOT_REQUIRED') return 'rl-tag--green'
  if (state === 'ABNORMAL' || state === 'UNKNOWN') return 'rl-tag--red'
  if (state === 'PROCESSING' || state === 'READY' || state === 'WAIT_RETURN') return 'rl-tag--amber'
  return 'rl-tag--gray'
}

function resetQuery() {
  query.status = ''
  query.kind = ''
  query.pageNum = 1
  load()
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
