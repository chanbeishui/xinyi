<template>
  <RongliPage title="普通服务异常与争议" subtitle="记录服务事实、当前保管方与人工处置，不提供事故理赔或自动定责。">
    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="keyword"
          placeholder="工单 / 订单 / 车牌"
          clearable
          prefix-icon="Search"
          class="rl-w-260"
        />
        <el-select v-model="kindFilter" placeholder="全部类型" style="width: 160px">
          <el-option label="全部类型" value="" />
          <el-option label="服务异常" value="EXCEPTION" />
          <el-option label="服务争议" value="DISPUTE" />
          <el-option label="结果更正" value="CORRECTION" />
          <el-option label="支付对账" value="RECONCILE" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px">
          <el-option label="全部状态" value="" />
          <el-option label="待响应" value="OPEN" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="核实中" value="CHECKING" />
          <el-option label="待审核" value="APPLIED" />
          <el-option label="已办结" value="RESOLVED" />
        </el-select>
        <el-button icon="Refresh" @click="loadAll">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedRows" :border="false">
        <el-table-column label="工单 / 车辆" min-width="190">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.code }}</div>
            <div class="rl-mono">{{ row.plateNo || row.orderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="150">
          <template #default="{ row }">
            <div>{{ row.kindLabel }}</div>
            <div class="rl-sub">{{ row.typeLabel || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="当前保管方" width="130">
          <template #default="{ row }">{{ custodyLabel(row.custodyType) }}</template>
        </el-table-column>
        <el-table-column label="处理状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row)">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最近更新" width="160">
          <template #default="{ row }">{{ shortTime(row.changedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">
              {{ isClosed(row) ? '查看' : '处理' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">暂无待办工单</div>
            <div class="rl-empty__hint">异常、争议、结果更正与对账异常都会汇入这里。</div>
            <el-button @click="resetFilter">重置筛选</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="filteredRows.length"
          layout="total, prev, pager, next"
        />
      </div>
    </section>

    <!-- 处置抽屉 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px">
      <div class="rl-detail-strip">
        <div><span>工单</span><b>{{ current.code }}</b></div>
        <div><span>车辆</span><b>{{ current.plateNo || '—' }}</b></div>
        <div><span>当前保管方</span><b>{{ custodyLabel(current.custodyType) }}</b></div>
        <div><span>处理状态</span><b>{{ current.statusLabel }}</b></div>
      </div>

      <div class="rl-kv" style="margin-bottom: 16px">
        <div><span>关联订单</span><b class="rl-mono">{{ current.orderNo || '—' }}</b></div>
        <div><span>类型</span><b>{{ current.kindLabel }} · {{ current.typeLabel || '—' }}</b></div>
        <div style="grid-column: span 2"><span>申请说明</span><b>{{ current.description || '暂无说明' }}</b></div>
      </div>

      <template v-if="current.kind === 'CORRECTION'">
        <el-form label-width="90px">
          <el-form-item label="审核结论">
            <el-radio-group v-model="approve">
              <el-radio :value="true">批准更正</el-radio>
              <el-radio :value="false">驳回更正</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见">
            <el-input v-model="note" type="textarea" :rows="3" placeholder="批准或驳回均填写具体意见" />
          </el-form-item>
        </el-form>
        <p class="rl-hint">审核通过后发布新有效版本并通知相关方，不重新收车、放行或释放司机。</p>
      </template>

      <template v-else>
        <el-form label-width="90px">
          <el-form-item label="核实情况">
            <el-input v-model="note" type="textarea" :rows="4" placeholder="记录联系结果、当前事实及拟采取的业务动作" />
          </el-form-item>
          <el-form-item label="下一步">
            <el-select v-model="nextStep" style="width: 100%">
              <el-option label="联系协助" value="CONTACT" />
              <el-option label="转取消审核" value="CANCEL_REVIEW" />
              <el-option label="转退款审核" value="REFUND_REVIEW" />
              <el-option label="记录已解决结论" value="RESOLVED" />
            </el-select>
          </el-form-item>
        </el-form>
        <p class="rl-hint rl-hint--warn">只记录处置并进入合法业务流程，不可无交接证据强制完成订单。</p>
      </template>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="isClosed(current)" @click="submit">
          {{ current.kind === 'CORRECTION' ? '提交审核' : '保存处置记录' }}
        </el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliException" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  auditCorrection,
  listCorrections,
  listDisputes,
  listExceptions,
  listReconciliation,
  resolveDispute,
  resolveException,
  resolveReconciliation
} from '@/api/rongli'
import { custodyLabel } from '../status'
import { shortTime } from '../format'

const loading = ref(false)
const saving = ref(false)
const allRows = ref<any[]>([])
const keyword = ref('')
const kindFilter = ref('')
const statusFilter = ref('')
const pageNum = ref(1)
const pageSize = 10

const drawerVisible = ref(false)
const current = ref<any>({})
const note = ref('')
const nextStep = ref('CONTACT')
const approve = ref(true)

const KIND_LABEL: Record<string, string> = {
  EXCEPTION: '服务异常',
  DISPUTE: '服务争议',
  CORRECTION: '结果更正',
  RECONCILE: '支付对账'
}

const filteredRows = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return allRows.value.filter((r) => {
    if (kindFilter.value && r.kind !== kindFilter.value) return false
    if (statusFilter.value) {
      const closed = ['RESOLVED', 'CLOSED', 'APPROVED', 'REJECTED'].includes(r.status)
      if (statusFilter.value === 'RESOLVED' ? !closed : r.status !== statusFilter.value) return false
    }
    if (!kw) return true
    return [r.code, r.orderNo, r.plateNo].some((v) => String(v || '').toLowerCase().includes(kw))
  })
})

const pagedRows = computed(() =>
  filteredRows.value.slice((pageNum.value - 1) * pageSize, pageNum.value * pageSize)
)

const drawerTitle = computed(() =>
  current.value.kind === 'CORRECTION' ? '检测结果更正审核' : '记录服务核实与处置'
)

onMounted(loadAll)

function isClosed(row: any) {
  return ['RESOLVED', 'CLOSED', 'APPROVED', 'REJECTED'].includes(row.status)
}

function statusClass(row: any) {
  if (isClosed(row)) return 'rl-tag--green'
  if (row.status === 'CHECKING') return 'rl-tag--blue'
  return 'rl-tag--amber'
}

async function loadAll() {
  loading.value = true
  try {
    const [exc, dis, cor, rec] = await Promise.all([
      listExceptions({ pageNum: 1, pageSize: 100 }).catch(() => null),
      listDisputes({ pageNum: 1, pageSize: 100 }).catch(() => null),
      listCorrections({ pageNum: 1, pageSize: 100 }).catch(() => null),
      listReconciliation({ pageNum: 1, pageSize: 100 }).catch(() => null)
    ])
    const rows: any[] = []
    for (const e of (exc && exc.rows) || []) {
      rows.push({
        ...e,
        kind: 'EXCEPTION',
        kindLabel: KIND_LABEL.EXCEPTION,
        code: 'CASE-' + String(e.exceptionId).padStart(4, '0'),
        typeLabel: e.type
      })
    }
    for (const d of (dis && dis.rows) || []) {
      rows.push({
        ...d,
        kind: 'DISPUTE',
        kindLabel: KIND_LABEL.DISPUTE,
        code: 'DIS-' + String(d.disputeId).padStart(4, '0'),
        typeLabel: d.type
      })
    }
    for (const c of (cor && cor.rows) || []) {
      rows.push({
        ...c,
        kind: 'CORRECTION',
        kindLabel: KIND_LABEL.CORRECTION,
        code: 'COR-' + String(c.correctionId).padStart(4, '0'),
        typeLabel: '检测结果更正',
        description: c.reason
      })
    }
    for (const r of (rec && rec.rows) || []) {
      rows.push({
        ...r,
        kind: 'RECONCILE',
        kindLabel: KIND_LABEL.RECONCILE,
        code: 'REC-' + String(r.caseId).padStart(4, '0'),
        typeLabel: '支付退款对账',
        description: r.reason
      })
    }
    rows.sort((a, b) => String(b.changedAt || b.createdAt || '').localeCompare(String(a.changedAt || a.createdAt || '')))
    allRows.value = rows
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  keyword.value = ''
  kindFilter.value = ''
  statusFilter.value = ''
  pageNum.value = 1
}

function open(row: any) {
  current.value = row
  note.value = row.conclusion || row.resolution || ''
  nextStep.value = isClosed(row) ? 'RESOLVED' : 'CONTACT'
  approve.value = true
  drawerVisible.value = true
}

async function submit() {
  if (!note.value) {
    ElMessage.warning(current.value.kind === 'CORRECTION' ? '请填写审核意见' : '请填写核实情况')
    return
  }
  saving.value = true
  try {
    const row = current.value
    if (row.kind === 'CORRECTION') {
      await auditCorrection(row.correctionId, { approve: approve.value, opinion: note.value })
    } else if (row.kind === 'EXCEPTION') {
      await resolveException(row.exceptionId, { resolution: note.value })
    } else if (row.kind === 'DISPUTE') {
      await resolveDispute(row.disputeId, { conclusion: note.value })
    } else {
      await resolveReconciliation(row.caseId, { conclusion: note.value })
    }
    ElMessage.success('处置记录已保存')
    drawerVisible.value = false
    loadAll()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.rl-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--rl-ink-3, #667085);
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px 12px;
}
.rl-hint--warn {
  background: #fffaeb;
  color: #b54708;
}
</style>
