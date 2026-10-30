<template>
  <RongliPage title="安全审计" subtitle="查看主体、来源、前后值摘要；敏感访问与业务流水可关联追溯。">
    <template #actions>
      <el-button @click="router.push(RL.governance)">申请导出审计</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="query.action"
          placeholder="动作，例如 ORDER_CREATED"
          clearable
          prefix-icon="Search"
          class="rl-w-260"
          @keyup.enter="handleQuery"
        />
        <el-select v-model="query.aggregateType" placeholder="全部对象类型" style="width: 160px" @change="handleQuery">
          <el-option label="全部对象类型" value="" />
          <el-option label="订单" value="ORDER" />
          <el-option label="售后" value="AFTERSALE" />
          <el-option label="代驾" value="DRIVER" />
          <el-option label="对账" value="LEDGER" />
          <el-option label="争议" value="DISPUTE" />
        </el-select>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ shortTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="180">
          <template #default="{ row }">
            <div>{{ actorText(row) }}</div>
            <div class="rl-sub">{{ row.actingRole || row.actorRealm || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="动作" width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ actionLabel(row.action) }}</div>
            <div class="rl-mono">{{ row.action }}</div>
          </template>
        </el-table-column>
        <el-table-column label="对象" width="160">
          <template #default="{ row }">
            <div>{{ row.aggregateType }}</div>
            <div class="rl-mono">{{ row.orderNo || row.aggregateId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="前后值摘要" min-width="300">
          <template #default="{ row }">
            <span class="rl-cell-sub">{{ summaryText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">{{ row.actorRealm === 'MINI' ? '小程序' : '管理端' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有匹配的审计记录</div>
            <div class="rl-empty__hint">关键业务动作都会留下不可变记录。</div>
            <el-button @click="resetQuery">重置筛选</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>

      <div class="rl-panel__body">
        <p class="rl-hint">不记录或展示密码、支付密钥、明文完整证件及交接码。</p>
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="审计记录详情" size="560px">
      <div class="rl-kv">
        <div><span>时间</span><b>{{ shortTime(current.createdAt) }}</b></div>
        <div><span>动作</span><b>{{ current.action }}</b></div>
        <div><span>对象</span><b>{{ current.aggregateType }} · {{ current.orderNo || current.aggregateId }}</b></div>
        <div><span>操作人</span><b>{{ actorText(current) }}</b></div>
        <div><span>来源</span><b>{{ current.actorRealm === 'MINI' ? '小程序' : '管理端' }}</b></div>
        <div><span>角色</span><b>{{ current.actingRole || '—' }}</b></div>
      </div>
      <div class="rl-panel mt16">
        <div class="rl-panel__head"><h3>变更前</h3></div>
        <div class="rl-panel__body"><pre class="rl-json">{{ pretty(current.beforeSummary) }}</pre></div>
      </div>
      <div class="rl-panel">
        <div class="rl-panel__head"><h3>变更后</h3></div>
        <div class="rl-panel__body"><pre class="rl-json">{{ pretty(current.afterSummary) }}</pre></div>
      </div>
      <p v-if="current.reason" class="rl-hint mt12">原因：{{ current.reason }}</p>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliAudit" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listAuditEvents } from '@/api/rongli'
import { shortTime } from '../format'
import { RL } from '../routes'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ aggregateType: '', action: '', pageNum: 1, pageSize: 20 })

const detailVisible = ref(false)
const current = ref<any>({})

const ACTION_LABEL: Record<string, string> = {
  ORDER_CREATED: '创建订单',
  ORDER_CANCELLED: '订单取消',
  DISPATCH_PLAN_UPDATED: '派单计划变更',
  ASSIGNMENT_ACCEPTED: '代驾接单',
  ASSIGNMENT_RELEASED: '解除承接',
  HANDOVER_CREATED: '创建交接记录',
  HANDOVER_CONFIRMED: '交接确认',
  STATION_CREATED: '新增检测点',
  STATION_UPDATED: '修改检测点',
  RESULT_SUBMITTED: '登记检测结果',
  CORRECTION_APPLIED: '申请结果更正',
  AFTERSALE_APPROVED: '售后审核通过',
  REFUND_EXECUTED: '执行退款'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listAuditEvents(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function actionLabel(code?: string) {
  if (!code) return '—'
  return ACTION_LABEL[code] || code
}

function actorText(row: any) {
  if (!row) return '—'
  if (row.actorAccountId) return `账号 ${row.actorAccountId}`
  if (row.actorUserId) return `员工 ${row.actorUserId}`
  return 'SYSTEM'
}

function summaryText(row: any) {
  const before = compact(row.beforeSummary)
  const after = compact(row.afterSummary)
  if (!before && !after) return '—'
  if (!before) return after
  if (!after) return before
  return `${before} → ${after}`
}

function compact(value: any) {
  if (!value || (typeof value === 'object' && !Object.keys(value).length)) return ''
  if (typeof value !== 'object') return String(value)
  return Object.entries(value)
    .map(([k, v]) => `${k}=${typeof v === 'object' ? JSON.stringify(v) : v}`)
    .join('，')
}

function pretty(value: any) {
  if (!value || (typeof value === 'object' && !Object.keys(value).length)) return '无'
  return JSON.stringify(value, null, 2)
}

function openDetail(row: any) {
  current.value = row
  detailVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  load()
}

function resetQuery() {
  query.aggregateType = ''
  query.action = ''
  query.pageNum = 1
  load()
}
</script>

<style scoped>
.rl-json {
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 19px;
  color: var(--rl-ink-2, #475467);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
