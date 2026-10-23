<template>
  <div class="app-container rl-reconcile">
    <div class="page-head">
      <h2 class="page-title">支付退款对账异常</h2>
      <p class="page-desc">核实订单付款与渠道退款状态。</p>
    </div>

    <div class="panel">
      <div class="panel-title">待核实记录</div>
      <el-form :inline="true" class="mb8" @submit.prevent>
        <el-form-item label="订单/交易号">
          <el-input
            v-model="query.keyword"
            placeholder="输入关键词"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option label="待核实" value="OPEN" />
            <el-option label="核查中" value="CHECKING" />
            <el-option label="已解决" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" border size="large">
        <el-table-column label="业务订单" prop="orderNo" min-width="180">
          <template #default="{ row }">{{ row.orderNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="关联交易" min-width="170">
          <template #default="{ row }">{{ row.relatedTradeNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="问题类型" min-width="180">
          <template #default="{ row }">{{ row.problemType || row.statusLabel || '-' }}</template>
        </el-table-column>
        <el-table-column label="业务 / 渠道" min-width="170">
          <template #default="{ row }">{{ row.bizStatusLabel || '-' }} / {{ row.channelStatus || '-' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">¥{{ fen2yuan(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAction(row)">
              {{ row.relatedTradeKind === 'REFUND' ? '核对退款' : '查询原交易' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[6, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-alert
        v-if="current"
        :title="`${current.orderNo || '-'} · ${current.relatedTradeNo || '无关联交易'} · ¥${fen2yuan(current.amountFen)}`"
        type="info"
        :closable="false"
        class="mb12"
      />
      <p v-if="queried" class="tip">已发起渠道重查，请以渠道流水为准登记结论。</p>
      <el-form label-width="96px">
        <el-form-item label="核对结论" required>
          <el-input
            v-model="conclusion"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="写明渠道实际状态、依据的单据号与处理方式"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitConclusion">提交结论</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliPaymentReconcile" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listReconciliation, queryReconciliation, resolveReconciliation } from '@/api/rongli'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
// 设计稿默认「全部」，不预置状态筛选
const query = reactive<any>({ keyword: '', status: '', pageNum: 1, pageSize: 6 })

const dialogVisible = ref(false)
const dialogTitle = ref('查询原交易')
const queried = ref(false)
const conclusion = ref('')
const current = ref<any>(null)

function fen2yuan(fen: any) {
  return (Number(fen || 0) / 100).toFixed(2)
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listReconciliation(query)
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

function resetQuery() {
  query.keyword = ''
  query.status = ''
  query.pageNum = 1
  load()
}

async function openAction(row: any) {
  current.value = row
  conclusion.value = ''
  queried.value = false
  const isRefund = row.relatedTradeKind === 'REFUND'
  dialogTitle.value = isRefund ? '核对退款' : '查询原交易'
  dialogVisible.value = true
  if (!isRefund) {
    try {
      await queryReconciliation(row.caseId)
      queried.value = true
      ElMessage.success('已发起渠道重查')
      load()
    } catch (e) {
      dialogVisible.value = false
      throw e
    }
  }
}

async function submitConclusion() {
  if (!current.value) return
  if (!conclusion.value.trim()) {
    ElMessage.warning('请填写核对结论与依据')
    return
  }
  saving.value = true
  try {
    await resolveReconciliation(current.value.caseId, { conclusion: conclusion.value.trim() })
    ElMessage.success('核对结论已登记')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.rl-reconcile { padding-bottom: 24px; }
.page-head { margin-bottom: 16px; }
.page-title { margin: 0 0 6px; font-size: 22px; font-weight: 600; }
.page-desc { margin: 0; color: #909399; font-size: 13px; }
.panel { border: 1px solid #ebeef5; border-radius: 10px; background: #fff; padding: 16px 18px 12px; }
.panel-title { font-size: 15px; font-weight: 600; color: #1f2d3d; margin-bottom: 12px; }
.mb8 { margin-bottom: 8px; }
.mb12 { margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; margin: 0 0 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
