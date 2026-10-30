<template>
  <RongliPage title="预约订单" subtitle="按当前获授权范围查询；履约、取消与资金状态分别展示。">
    <template #actions>
      <el-button @click="goExport">申请导出</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="query.keyword"
          class="rl-w-260"
          placeholder="车牌 / 订单号 / 姓名"
          clearable
          @keyup.enter="handleQuery"
        />
        <el-select v-model="query.stationId" placeholder="全部授权站点" clearable @change="handleQuery">
          <el-option label="全部授权站点" value="" />
          <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="query.group" placeholder="全部" @change="handleQuery">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="DONE" />
        </el-select>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" row-key="orderNo">
        <el-table-column label="订单 / 车辆" min-width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.plateNo || '—' }}</div>
            <div class="rl-mono">{{ row.orderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="上门时段" width="180">
          <template #default="{ row }">{{ row.pickupWindowLabel || '—' }}</template>
        </el-table-column>
        <el-table-column label="检测点" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.stationName || '—' }}</template>
        </el-table-column>
        <el-table-column label="当前保管方" width="120">
          <template #default="{ row }">
            <span class="rl-custody">{{ row.custodyLabel || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="履约状态" width="160">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row.fulfillmentStatus)">
              {{ row.fulfillmentStatusLabel || row.fulfillmentStatus }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="取消 / 资金" width="150">
          <template #default="{ row }">
            <div class="rl-tag rl-tag--plain" :class="cancelClass(row.cancelStatus)">
              {{ row.cancelStatusLabel || '无申请' }}
            </div>
            <div class="rl-sub" style="margin-top: 4px">
              {{ row.payStatusLabel || '待支付' }} · ¥{{ row.totalYuan ?? '0.00' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">未找到匹配的预约订单</div>
            <div class="rl-empty__hint">请修改筛选条件，或重置后查看全部授权范围内订单。</div>
            <el-button @click="resetQuery">重置筛选</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="load"
          @size-change="handleQuery"
        />
      </div>
    </section>

    <!-- B04 订单详情 -->
    <el-drawer v-model="detailVisible" size="56%" :with-header="false" class="rl-drawer">
      <div v-if="detail" style="padding: 4px 6px 24px">
        <div class="rl-page__head" style="margin-bottom: 14px">
          <div class="rl-page__titles">
            <h1>{{ detail.plateNo || '预约详情' }}</h1>
            <p>{{ detail.orderNo }} · {{ detail.stationName || '未指定检测点' }}</p>
          </div>
          <div class="rl-page__actions">
            <el-button @click="notify(detail)">通知取车</el-button>
            <el-button type="primary" @click="openResult(detail)">检测结果</el-button>
          </div>
        </div>

        <div class="rl-detail-strip">
          <div>
            <span>履约状态</span>
            <b>{{ detail.fulfillmentStatusLabel || '—' }}</b>
          </div>
          <div>
            <span>当前保管方</span>
            <b>{{ detail.custodyLabel || '—' }}</b>
          </div>
          <div>
            <span>承接任务</span>
            <b>{{ detail.driverName ? detail.driverName + ' · 在手' : '未承接' }}</b>
          </div>
          <div>
            <span>资金</span>
            <b>¥{{ detail.totalYuan ?? '0.00' }}</b>
          </div>
        </div>

        <section class="rl-panel">
          <div class="rl-panel__head"><h3>预约与车辆信息</h3></div>
          <div class="rl-panel__body">
            <div class="rl-kv">
              <div><span>订单号</span><b class="rl-mono">{{ detail.orderNo }}</b></div>
              <div><span>上门时段</span><b>{{ detail.pickupWindowLabel || '—' }}</b></div>
              <div><span>接车地点</span><b>{{ detail.address || '—' }}</b></div>
              <div><span>检测点</span><b>{{ detail.stationName || '—' }}</b></div>
              <div><span>车型</span><b>{{ detail.vehicleTypeName || '—' }}</b></div>
              <div><span>车主账号</span><b>{{ detail.ownerAccountId || '—' }}</b></div>
              <div><span>证件核对</span><b>{{ detail.licenseStatus || '未核对' }}</b></div>
              <div><span>代驾</span><b>{{ detail.driverName || '未分配' }}</b></div>
            </div>
          </div>
        </section>

        <section class="rl-panel">
          <div class="rl-panel__head"><h3>业务时间轴</h3></div>
          <div class="rl-panel__body">
            <el-timeline v-if="timeline.length">
              <el-timeline-item v-for="(n, i) in timeline" :key="i" :timestamp="n.at">
                {{ n.label || n.action }}<span v-if="n.reason" class="rl-muted"> · {{ n.reason }}</span>
              </el-timeline-item>
            </el-timeline>
            <div v-else class="rl-empty" style="padding: 20px 0">暂无业务节点</div>
          </div>
        </section>
      </div>
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
  </RongliPage>
</template>

<script setup name="RongliOrder" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RongliPage from '../components/RongliPage.vue'
import {
  getOrder,
  getOrderResults,
  listOrders,
  listStations,
  notifyPickup,
  submitOrderResult,
  applyResultCorrection
} from '@/api/rongli'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const stations = ref<any[]>([])
const query = reactive<any>({ keyword: '', stationId: '', group: '', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const detail = ref<any>(null)
const timeline = ref<any[]>([])

const resultVisible = ref(false)
const result = ref<any>(null)
const resultForm = reactive<any>({ conclusion: 'PASS', items: '{}' })
const currentOrderNo = ref('')

/** 状态色板与原型一致：待处理橙、处理中蓝、已完成/检测中绿、终止灰 */
const STATUS_TONE: Record<string, string> = {
  PENDING_PAY: 'amber',
  SCHEDULED: 'blue',
  PENDING_DISPATCH: 'amber',
  DISPATCHING: 'amber',
  MANUAL_DISPATCH: 'amber',
  ACCEPTED: 'blue',
  GOING_PICKUP: 'blue',
  ARRIVED_PICKUP: 'blue',
  PICKED_UP: 'blue',
  ARRIVED_STATION: 'blue',
  INSPECTING: 'green',
  INSPECTED: 'blue',
  CAR_TAKEN: 'green',
  WAIT_CONFIRM: 'green',
  COMPLETED: 'green',
  CANCELLED: 'gray',
  CLOSED: 'gray'
}

function statusClass(status: string) {
  return `rl-tag--${STATUS_TONE[status] || 'gray'}`
}

function cancelClass(cancel: string) {
  if (cancel === 'APPLIED' || cancel === 'APPROVED_WAIT_RETURN') return 'rl-tag--amber'
  if (cancel === 'REJECTED') return 'rl-tag--red'
  if (cancel === 'COMPLETED') return 'rl-tag--gray'
  return 'rl-tag--gray'
}

onMounted(async () => {
  load()
  loadStations()
})

async function loadStations() {
  try {
    const res: any = await listStations({ pageNum: 1, pageSize: 100 })
    stations.value = (res && res.rows) || []
  } catch (e) {
    stations.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const params = {
      keyword: query.keyword || undefined,
      stationId: query.stationId || undefined,
      group: query.group || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    const res: any = await listOrders(params)
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
  query.stationId = ''
  query.group = ''
  query.pageNum = 1
  load()
}

function goExport() {
  router.push('/rongli/governance')
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
.rl-custody {
  color: #475467;
}
.rl-drawer :deep(.el-drawer__body) {
  padding: 20px 24px;
}
</style>
