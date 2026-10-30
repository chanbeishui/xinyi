<template>
  <RongliPage title="调度工作台" subtitle="计划派单与到期队列分开；人工邀约仍需司机接受。">
    <template #actions>
      <el-button @click="previewPlan">重算未派单计划</el-button>
      <el-button @click="openInvites">派单记录</el-button>
      <el-button type="primary" @click="settingsVisible = true">派单设置</el-button>
    </template>

    <div class="dp-grid">
      <!-- 待安排队列 -->
      <section class="rl-panel dp-col">
        <div class="rl-panel__head">
          <h3>待安排队列</h3>
          <span class="rl-sub">{{ queue.length }} 单</span>
        </div>
        <div class="dp-queue">
          <button
            v-for="q in queue"
            :key="q.orderNo"
            class="dp-queue__item"
            :class="{ 'is-active': current && current.orderNo === q.orderNo }"
            @click="selectOrder(q)"
          >
            <div class="dp-queue__top">
              <span class="rl-strong">{{ q.plateNo || '—' }}</span>
              <span class="rl-tag" :class="stageClass(q.fulfillmentStatus)">{{ stageLabel(q.fulfillmentStatus) }}</span>
            </div>
            <div class="rl-sub">上门 {{ q.pickupWindowLabel || '待定' }}</div>
            <div class="rl-mono">{{ q.orderNo }}</div>
          </button>
          <div v-if="!queue.length && !queueLoading" class="rl-empty" style="padding: 32px 0">
            <div class="rl-empty__title">当前没有待安排订单</div>
            <div class="rl-empty__hint">自动派单会按预约派单时点进入队列</div>
          </div>
        </div>
      </section>

      <!-- 候选代驾 -->
      <section class="rl-panel dp-col">
        <div class="rl-panel__head">
          <h3>候选代驾</h3>
          <span class="rl-sub">{{ current ? '为 ' + (current.plateNo || current.orderNo) + ' 选择承接' : '先选择左侧订单' }}</span>
        </div>
        <el-table v-loading="candidateLoading" :data="candidates" height="520">
          <el-table-column label="代驾" min-width="150">
            <template #default="{ row }">
              <div class="rl-strong">{{ row.nickname || '未命名' }}</div>
              <div class="rl-sub">资格 {{ row.qualificationStatus || '未认证' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="听单 / 占用" width="130">
            <template #default="{ row }">
              <span class="rl-tag" :class="row.busy ? 'rl-tag--amber' : row.listeningEnabled ? 'rl-tag--green' : 'rl-tag--gray'">
                {{ row.busy ? '在手任务' : row.listeningEnabled ? '听单中' : '未听单' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="定位更新" width="160">
            <template #default="{ row }">
              <span class="rl-sub">{{ shortTime(row.lastLocationAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="!current || row.busy" @click="openInvite(row)">
                发出邀约
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="rl-empty" style="padding: 30px 0">
              <div class="rl-empty__title">没有符合条件的代驾</div>
              <div class="rl-empty__hint">需资格有效、听单开启且当前无占用</div>
            </div>
          </template>
        </el-table>
      </section>

      <!-- 派单策略与当前订单 -->
      <section class="dp-side">
        <div class="rl-panel">
          <div class="rl-panel__head"><h3>派单策略</h3></div>
          <div class="rl-panel__body">
            <div class="dp-kv"><span>自动策略</span><b>全局轮转</b></div>
            <div class="dp-kv"><span>预约派单时点</span><b>上门开始前 {{ config.leadMinutes }} 分钟</b></div>
            <div class="dp-kv"><span>邀约有效</span><b>{{ config.inviteExpireSeconds }} 秒</b></div>
            <div class="dp-kv"><span>派单半径</span><b>{{ config.radiusMeter }} 米</b></div>
            <div class="dp-kv"><span>单轮总时长</span><b>{{ config.totalMinutes }} 分钟</b></div>
            <el-button link type="primary" class="dp-link" @click="settingsVisible = true">查看参数与计划影响</el-button>
          </div>
        </div>

        <div class="rl-panel">
          <div class="rl-panel__head"><h3>当前订单</h3></div>
          <div class="rl-panel__body">
            <template v-if="current">
              <div class="dp-kv"><span>车辆</span><b>{{ current.plateNo || '—' }}</b></div>
              <div class="dp-kv"><span>上门时段</span><b>{{ current.pickupWindowLabel || '—' }}</b></div>
              <div class="dp-kv"><span>检测点</span><b>{{ current.stationName || '—' }}</b></div>
              <div class="dp-kv"><span>派单时点</span><b>{{ shortTime(current.dispatchAt) }}</b></div>
              <div class="dp-actions">
                <el-button size="small" @click="openInvite(null)">人工邀约</el-button>
                <el-button size="small" type="primary" plain @click="dispatchNow">立即派单</el-button>
              </div>
            </template>
            <div v-else class="rl-sub">从左侧队列选择一笔订单后处理。</div>
          </div>
        </div>
      </section>
    </div>

    <!-- 派单设置 -->
    <el-drawer v-model="settingsVisible" title="派单设置" size="420px">
      <el-form label-width="150px">
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
          <span class="rl-sub" style="margin-left: 10px">仅影响新订单</span>
        </el-form-item>
      </el-form>
    </el-drawer>

    <!-- 派单计划 -->
    <el-drawer v-model="planVisible" title="派单计划预览" size="640px">
      <el-alert type="info" :closable="false" show-icon title="计划预览不写库，确认后才会按计划发起邀约。" class="mb8" />
      <el-table :data="plan.rows || []" size="small">
        <el-table-column label="订单号" prop="orderNo" min-width="170" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">{{ stageLabel(row.fulfillmentStatus) }}</template>
        </el-table-column>
        <el-table-column label="当前派单时间" prop="currentDispatchAt" min-width="170" />
        <el-table-column label="调整后派单时间" prop="nextDispatchAt" min-width="170" />
      </el-table>
      <el-alert v-if="plan.hint" :title="plan.hint" type="warning" :closable="false" class="mt16" />
      <div class="mt16">
        <el-button v-if="plan.planId" type="primary" @click="applyPlan">应用计划</el-button>
      </div>
    </el-drawer>

    <!-- 派单记录 -->
    <el-drawer v-model="invitesVisible" title="派单记录" size="720px">
      <el-table v-loading="inviteLoading" :data="invites" size="small">
        <el-table-column label="邀约 / 订单" min-width="200">
          <template #default="{ row }">
            <div class="rl-mono">{{ row.inviteId }}</div>
            <div class="rl-sub">{{ row.orderNo }} · 第 {{ row.roundNo || 1 }} 轮</div>
          </template>
        </el-table-column>
        <el-table-column label="代驾" prop="driverName" width="120" />
        <el-table-column label="发出时间" width="160">
          <template #default="{ row }">{{ shortTime(row.issuedAt) }}</template>
        </el-table-column>
        <el-table-column label="响应" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="inviteTone(row.status)">{{ row.statusLabel || inviteLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" prop="sourceLabel" width="100" />
      </el-table>
    </el-drawer>

    <el-dialog v-model="inviteVisible" title="人工邀约" width="560px">
      <el-form label-width="100px">
        <el-form-item label="订单">
          <span class="rl-mono">{{ current && current.orderNo }}</span>
        </el-form-item>
        <el-form-item label="候选代驾">
          <el-select v-model="inviteDriverId" filterable placeholder="选择代驾" style="width: 100%">
            <el-option
              v-for="d in candidates"
              :key="d.driverId"
              :label="`${d.nickname}（${d.busy ? '在手任务' : d.listeningEnabled ? '听单中' : '未听单'}）`"
              :value="d.driverId"
              :disabled="d.busy"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="人工介入原因">
          <el-input v-model="inviteReason" type="textarea" :rows="2" placeholder="请说明为何需要人工邀约" />
        </el-form-item>
      </el-form>
      <div class="rl-sub">发送只建立待响应邀约，不视为司机已经接单。</div>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInvite">确认发送邀约</el-button>
      </template>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliDispatch" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { stageClass, stageLabel } from '../status'
import {
  applyDispatchPlan,
  dispatchOrderNow,
  getDispatchConfig,
  listCandidates,
  listDispatchQueue,
  listInvites,
  manualInvite,
  previewDispatchPlan,
  saveDispatchConfig
} from '@/api/rongli'

const queue = ref<any[]>([])
const queueLoading = ref(false)
const current = ref<any>(null)
const candidates = ref<any[]>([])
const candidateLoading = ref(false)
const plan = ref<any>({})
const config = reactive<any>({
  leadMinutes: 30,
  inviteExpireSeconds: 60,
  radiusMeter: 8000,
  totalMinutes: 5
})

const settingsVisible = ref(false)
const planVisible = ref(false)
const invitesVisible = ref(false)
const inviteVisible = ref(false)
const inviteLoading = ref(false)
const invites = ref<any[]>([])
const inviteDriverId = ref('')
const inviteReason = ref('人工干预派单')

const INVITE_LABEL: Record<string, string> = {
  WAIT_RESPONSE: '待响应',
  ACCEPTED: '已接受',
  REJECTED: '已拒绝',
  EXPIRED: '已超时',
  CANCELLED: '已撤销'
}

function inviteLabel(status: string) {
  return INVITE_LABEL[status] || status || '—'
}

function inviteTone(status: string) {
  if (status === 'ACCEPTED') return 'rl-tag--green'
  if (status === 'WAIT_RESPONSE') return 'rl-tag--amber'
  return 'rl-tag--gray'
}

function shortTime(value?: string) {
  if (!value) return '—'
  return String(value).replace('T', ' ').replace('Z', '').slice(0, 16)
}

onMounted(async () => {
  await loadQueue()
  await loadConfig()
  await loadCandidates()
})

async function loadQueue() {
  queueLoading.value = true
  try {
    const res: any = await listDispatchQueue()
    queue.value = (res && res.rows) || []
    if (!current.value || !queue.value.some((q) => q.orderNo === current.value.orderNo)) {
      current.value = queue.value[0] || null
    }
  } finally {
    queueLoading.value = false
  }
}

async function loadCandidates() {
  candidateLoading.value = true
  try {
    const res: any = await listCandidates('')
    candidates.value = (res && res.rows) || []
  } finally {
    candidateLoading.value = false
  }
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

function selectOrder(row: any) {
  current.value = row
}

async function saveConfig() {
  await saveDispatchConfig({ ...config })
  ElMessage.success('参数已保存，仅影响新订单')
  settingsVisible.value = false
}

async function previewPlan() {
  plan.value = await previewDispatchPlan({ leadMinutes: config.leadMinutes })
  planVisible.value = true
}

async function applyPlan() {
  await applyDispatchPlan(plan.value.planId, plan.value.leadMinutes ?? config.leadMinutes)
  ElMessage.success('已按计划调整派单时间')
  planVisible.value = false
  await loadQueue()
}

async function openInvites() {
  invitesVisible.value = true
  inviteLoading.value = true
  try {
    const res: any = await listInvites({ pageNum: 1, pageSize: 50 })
    invites.value = (res && res.rows) || []
  } finally {
    inviteLoading.value = false
  }
}

function openInvite(row: any) {
  if (!current.value) {
    ElMessage.warning('请先选择一笔待安排订单')
    return
  }
  inviteDriverId.value = row && row.driverId ? row.driverId : ''
  inviteReason.value = '人工干预派单'
  inviteVisible.value = true
}

async function submitInvite() {
  if (!inviteDriverId.value) {
    ElMessage.warning('请选择司机')
    return
  }
  await manualInvite({
    orderNo: current.value.orderNo,
    driverId: Number(inviteDriverId.value),
    reason: inviteReason.value
  })
  ElMessage.success('已发起邀约')
  inviteVisible.value = false
  await loadQueue()
  await loadCandidates()
}

/** 立即派单：跳过「接车前提前量」等待，按自动规则当场发起一轮邀约 */
async function dispatchNow() {
  if (!current.value) return
  await ElMessageBox.confirm(
    `将跳过等待立即为订单 ${current.value.orderNo} 发起自动邀约，是否继续？`,
    '立即派单',
    { type: 'warning' }
  )
  const res: any = await dispatchOrderNow(current.value.orderNo)
  ElMessage.success(res.hint || '已发起派单')
  await loadQueue()
  await loadCandidates()
}
</script>

<style scoped>
.dp-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.dp-col {
  min-width: 0;
}

.dp-side {
  display: grid;
  gap: 16px;
}

.dp-queue {
  padding: 10px;
  max-height: 560px;
  overflow: auto;
}

.dp-queue__item {
  display: block;
  width: 100%;
  text-align: left;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.dp-queue__item:hover {
  border-color: #b2ddff;
  background: #f9fbff;
}

.dp-queue__item.is-active {
  border-color: #175cd3;
  background: #eff8ff;
}

.dp-queue__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.dp-kv {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed #f2f4f7;
  font-size: 12.5px;
}

.dp-kv:last-of-type {
  border-bottom: 0;
}

.dp-kv span {
  color: #667085;
}

.dp-kv b {
  font-weight: 500;
  color: #101828;
  text-align: right;
}

.dp-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.dp-link {
  margin-top: 8px;
  padding: 0;
}

.mb8 {
  margin-bottom: 8px;
}

.mt16 {
  margin-top: 16px;
}

@media (max-width: 1400px) {
  .dp-grid {
    grid-template-columns: 260px minmax(0, 1fr);
  }
  .dp-side {
    grid-column: 1 / -1;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
