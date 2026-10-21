<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="订单状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 180px" @change="handleQuery">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="车牌" prop="plateNo" width="120" />
      <el-table-column label="车主" prop="ownerName" width="120" />
      <el-table-column label="检测点" prop="stationName" min-width="160" show-overflow-tooltip />
      <el-table-column label="上门时段" prop="pickupWindow" width="180" />
      <el-table-column label="状态" width="150">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.fulfillmentStatus)">{{ row.fulfillmentStatusLabel || row.fulfillmentStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额(元)" width="110" align="right">
        <template #default="{ row }">{{ row.totalYuan }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openResult(row)">结果</el-button>
          <el-button link type="primary" @click="notify(row)">通知取车</el-button>
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

    <el-drawer v-model="detailVisible" title="订单详情" size="52%">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.fulfillmentStatusLabel }}</el-descriptions-item>
        <el-descriptions-item label="车牌">{{ detail.plateNo }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ detail.vehicleTypeName }}</el-descriptions-item>
        <el-descriptions-item label="检测点">{{ detail.stationName }}</el-descriptions-item>
        <el-descriptions-item label="上门时段">{{ detail.pickupWindow }}</el-descriptions-item>
        <el-descriptions-item label="车主账号">{{ detail.ownerAccountId }}</el-descriptions-item>
        <el-descriptions-item label="金额(元)">{{ detail.totalYuan }}</el-descriptions-item>
        <el-descriptions-item label="证件核对">{{ detail.licenseStatus }}</el-descriptions-item>
        <el-descriptions-item label="代驾">{{ detail.driverName || '未分配' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">时间线</el-divider>
      <el-timeline>
        <el-timeline-item v-for="(n, i) in timeline" :key="i" :timestamp="n.at">
          {{ n.label || n.action }}<span v-if="n.reason"> · {{ n.reason }}</span>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>

    <el-dialog v-model="resultVisible" title="检测结果" width="620px">
      <el-descriptions v-if="result && result.conclusion" :column="2" border>
        <el-descriptions-item label="结论">{{ result.conclusionLabel }}</el-descriptions-item>
        <el-descriptions-item label="检测时间">{{ result.inspectedAt }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px" style="margin-top: 16px">
        <el-form-item label="结论">
          <el-radio-group v-model="resultForm.conclusion">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="检测项目">
          <el-input v-model="resultForm.items" type="textarea" :rows="5" placeholder='JSON，例如 {"制动":"合格"}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResult">提交结果</el-button>
        <el-button type="warning" @click="submitCorrection">申请更正</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliOrder" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrder, getOrderResults, listOrders, notifyPickup, submitOrderResult, applyResultCorrection } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const detail = ref<any>(null)
const timeline = ref<any[]>([])

const resultVisible = ref(false)
const result = ref<any>(null)
const resultForm = reactive<any>({ conclusion: 'PASS', items: '{}' })
const currentOrderNo = ref('')

const statusOptions = [
  { label: '待支付', value: 'PENDING_PAY' },
  { label: '待派单', value: 'PENDING_DISPATCH' },
  { label: '派单中', value: 'DISPATCHING' },
  { label: '待人工安排', value: 'MANUAL_DISPATCH' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '已接车', value: 'PICKED_UP' },
  { label: '已到站', value: 'ARRIVED_STATION' },
  { label: '检测中', value: 'INSPECTING' },
  { label: '待取车', value: 'INSPECTED' },
  { label: '待确认', value: 'WAIT_CONFIRM' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

function statusTag(status: string) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED' || status === 'CLOSED') return 'info'
  if (status === 'PENDING_PAY' || status === 'MANUAL_DISPATCH') return 'warning'
  return 'primary'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listOrders(query)
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
  query.status = ''
  query.pageNum = 1
  load()
}

async function openDetail(row: any) {
  currentOrderNo.value = row.orderNo
  detail.value = await getOrder(row.orderNo)
  timeline.value = (detail.value && detail.value.timeline) || []
  detailVisible.value = true
}

async function openResult(row: any) {
  currentOrderNo.value = row.orderNo
  result.value = await getOrderResults(row.orderNo)
  if (result.value && result.value.conclusion) {
    resultForm.conclusion = result.value.conclusion
    resultForm.items = JSON.stringify(result.value.items || {}, null, 2)
  } else {
    resultForm.items = '{}'
  }
  resultVisible.value = true
}

async function submitResult() {
  try {
    JSON.parse(resultForm.items || '{}')
  } catch (e) {
    ElMessage.error('检测项目需为合法 JSON')
    return
  }
  await submitOrderResult(currentOrderNo.value, {
    conclusion: resultForm.conclusion,
    items: resultForm.items,
    inspectedAt: new Date().toISOString()
  })
  ElMessage.success('已提交')
  resultVisible.value = false
  load()
}

async function submitCorrection() {
  await applyResultCorrection(currentOrderNo.value, {
    conclusion: resultForm.conclusion,
    items: resultForm.items,
    reason: '管理端更正申请'
  })
  ElMessage.success('更正申请已提交，需独立复核')
  resultVisible.value = false
}

async function notify(row: any) {
  await notifyPickup(row.orderNo)
  ElMessage.success('已通知车主取车')
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.mt16 {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
