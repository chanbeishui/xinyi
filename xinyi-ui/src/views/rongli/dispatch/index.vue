<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb16">
      <el-col :span="6" v-for="k in kpis" :key="k.key">
        <el-card shadow="never">
          <div class="kpi-label">{{ k.label }}</div>
          <div class="kpi-value">{{ dashboard[k.key] || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="tab">
      <el-tab-pane label="待派单队列" name="queue">
        <el-button icon="Refresh" class="mb8" @click="loadQueue">刷新</el-button>
        <el-button type="primary" icon="Promotion" class="mb8" @click="preview">生成派单计划</el-button>
        <el-table :data="queue" border>
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="车牌" prop="plateNo" width="120" />
          <el-table-column label="检测点" prop="stationName" min-width="160" />
          <el-table-column label="状态" prop="fulfillmentStatus" width="150" />
          <el-table-column label="上门时段" prop="pickupWindowLabel" width="200" />
          <el-table-column label="派单时间" prop="dispatchAt" width="200" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openInvite(row)">人工邀约</el-button>
              <el-button link type="warning" @click="dispatchNow(row)">立即派单</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="派单参数" name="config">
        <el-form label-width="180px" style="max-width: 620px">
          <el-form-item label="提前派单(分钟)">
            <el-input-number v-model="config.leadMinutes" :min="0" :step="5" />
          </el-form-item>
          <el-form-item label="单次邀约有效(秒)">
            <el-input-number v-model="config.inviteExpireSeconds" :min="10" :step="10" />
          </el-form-item>
          <el-form-item label="派单半径(米)">
            <el-input-number v-model="config.radiusMeter" :min="500" :step="500" />
          </el-form-item>
          <el-form-item label="派单总时长(分钟)">
            <el-input-number v-model="config.totalMinutes" :min="1" :step="1" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig">保存参数</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="派单计划" name="plan">
        <el-alert type="info" :closable="false" show-icon title="计划预览不写库，确认后才会按计划发起邀约。" class="mb8" />
        <el-table :data="plan.rows || []" border>
          <el-table-column label="订单号" prop="orderNo" width="180" />
          <el-table-column label="状态" prop="fulfillmentStatus" width="150" />
          <el-table-column label="当前派单时间" prop="currentDispatchAt" min-width="200" />
          <el-table-column label="调整后派单时间" prop="nextDispatchAt" min-width="200" />
        </el-table>
        <el-alert v-if="plan.hint" :title="plan.hint" type="warning" :closable="false" class="mt16" />
        <el-button v-if="plan.planId" type="primary" class="mt16" @click="applyPlan">应用计划</el-button>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="inviteVisible" title="人工邀约" width="560px">
      <el-form label-width="100px">
        <el-form-item label="订单号">{{ currentOrderNo }}</el-form-item>
        <el-form-item label="候选司机">
          <el-select v-model="inviteDriverId" filterable placeholder="选择司机" style="width: 100%">
            <el-option v-for="d in candidates" :key="d.driverId" :label="`${d.nickname}（${d.holderType || '空闲'}）`" :value="d.driverId" />
          </el-select>
        </el-form-item>
        <el-form-item label="邀约原因">
          <el-input v-model="inviteReason" type="textarea" :rows="2" placeholder="人工派单原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInvite">发起邀约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliDispatch" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyDispatchPlan,
  dispatchOrderNow,
  getDashboard,
  getDispatchConfig,
  listCandidates,
  listDispatchQueue,
  manualInvite,
  previewDispatchPlan,
  saveDispatchConfig
} from '@/api/rongli'

const tab = ref('queue')
const dashboard = ref<any>({})
const queue = ref<any[]>([])
const candidates = ref<any[]>([])
const plan = ref<any>({})
const config = reactive<any>({
  leadMinutes: 30,
  inviteExpireSeconds: 60,
  radiusMeter: 8000,
  totalMinutes: 5
})

const inviteVisible = ref(false)
const currentOrderNo = ref('')
const inviteDriverId = ref('')
const inviteReason = ref('')

const kpis = [
  { key: 'activeOrders', label: '进行中订单' },
  { key: 'dispatching', label: '派单中' },
  { key: 'manualDispatch', label: '待人工安排' },
  { key: 'aftersalePending', label: '售后待审核' }
]

onMounted(async () => {
  await loadDashboard()
  await loadQueue()
  await loadConfig()
})

async function loadDashboard() {
  dashboard.value = await getDashboard()
}

async function loadQueue() {
  const res: any = await listDispatchQueue()
  queue.value = (res && res.rows) || []
}

async function loadConfig() {
  const res: any = await getDispatchConfig()
  Object.assign(config, {
    leadMinutes: res.leadMinutes ?? 30,
    inviteExpireSeconds: res.inviteExpireSeconds ?? 60,
    radiusMeter: res.radiusMeter ?? 8000,
    totalMinutes: res.totalMinutes ?? 5
  })
}

async function saveConfig() {
  await saveDispatchConfig({ ...config })
  ElMessage.success('参数已保存，仅影响新订单')
}

async function preview() {
  plan.value = await previewDispatchPlan({ leadMinutes: config.leadMinutes })
  tab.value = 'plan'
}

async function applyPlan() {
  await applyDispatchPlan(plan.value.planId, plan.value.leadMinutes ?? config.leadMinutes)
  ElMessage.success('已按计划调整派单时间')
  await loadQueue()
}

async function openInvite(row: any) {
  currentOrderNo.value = row.orderNo
  inviteDriverId.value = ''
  inviteReason.value = '人工干预派单'
  const res: any = await listCandidates('')
  candidates.value = (res && res.rows) || []
  inviteVisible.value = true
}

async function submitInvite() {
  if (!inviteDriverId.value) {
    ElMessage.warning('请选择司机')
    return
  }
  await manualInvite({
    orderNo: currentOrderNo.value,
    driverId: Number(inviteDriverId.value),
    reason: inviteReason.value
  })
  ElMessage.success('已发起邀约')
  inviteVisible.value = false
  loadQueue()
}

/** 立即派单：跳过「接车前提前量」等待，按自动规则当场发起一轮邀约 */
async function dispatchNow(row: any) {
  await ElMessageBox.confirm(
    `将跳过等待立即为订单 ${row.orderNo} 发起自动邀约，是否继续？`,
    '立即派单',
    { type: 'warning' }
  )
  const res: any = await dispatchOrderNow(row.orderNo)
  ElMessage.success(res.hint || '已发起派单')
  await loadQueue()
  await loadDashboard()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.kpi-label { font-size: 13px; color: #909399; }
.kpi-value { font-size: 26px; font-weight: 600; margin-top: 6px; }
</style>
