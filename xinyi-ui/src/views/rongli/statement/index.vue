<template>
  <RongliPage title="代驾对账" subtitle="选择代驾与期间，在同一张账单核对服务、填写价格和处理差异。">
    <template #actions>
      <el-button @click="go('/rongli/people/feeRule')">代驾费用规则</el-button>
      <el-button @click="go('/rongli/people/paymentVerify')">已有付款核对</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-select v-model="entity" style="width: 200px">
          <el-option label="集信车检服务主体" value="MAIN" />
        </el-select>
        <el-date-picker v-model="from" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 160px" />
        <el-date-picker v-model="to" type="date" value-format="YYYY-MM-DD" placeholder="结束日期（含）" style="width: 170px" />
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="resetFilter">重置</el-button>
        <div style="flex: 1"></div>
        <el-button type="primary" icon="Plus" @click="openCreate">新建对账单</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedRows" :border="false">
        <el-table-column label="代驾员" min-width="190">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.driverName || '未设置昵称' }}</div>
            <div class="rl-mono">D{{ String(row.driverId).padStart(4, '0') }}</div>
          </template>
        </el-table-column>
        <el-table-column label="本期已结束任务" width="150">
          <template #default="{ row }">{{ row.taskCount }} 笔</template>
        </el-table-column>
        <el-table-column label="待填写金额" width="150">
          <template #default="{ row }">
            <span :class="row.unpriced ? 'rl-strong' : 'rl-muted'">{{ row.unpriced }} 笔</span>
          </template>
        </el-table-column>
        <el-table-column label="当前账单" min-width="230">
          <template #default="{ row }">
            <template v-if="row.statement">
              <div class="rl-strong">{{ row.statement.statementNo }}</div>
              <div class="rl-sub">
                <span class="rl-tag rl-tag--plain" :class="statementTone(row.statement.status)">{{ statementLabel(row.statement.status) }}</span>
                <span style="margin-left: 6px">{{ money(row.statement.amountFen) }} · V{{ row.statement.revision }}</span>
              </div>
            </template>
            <span v-else class="rl-muted">尚未整理</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDriver(row)">个人对账</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">本期没有对账数据</div>
            <div class="rl-empty__hint">调整开始 / 结束日期，或确认该期间存在已结束的服务明细。</div>
            <el-button @click="resetFilter">重置期间</el-button>
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

    <!-- 代驾账单 -->
    <el-drawer v-model="detailVisible" :title="currentName + ' · 个人对账'" size="720px">
      <div class="statement-heading">
        <div>
          <b>集信车检服务主体</b>
          <p class="rl-sub">
            {{ detail?.statementNo || '选择期间后整理；无价格明细也可保存草稿。' }}
            <template v-if="detail"> · {{ shortDate(detail.periodStart) }} 至 {{ shortDate(detail.periodEnd) }}</template>
          </p>
        </div>
        <span class="rl-tag" :class="detail ? statementTone(detail.status) : 'rl-tag--gray'">
          {{ detail ? statementLabel(detail.status) : '尚未建立草稿' }}
        </span>
      </div>

      <div class="rl-detail-strip">
        <div><span>本期已结束任务</span><b>{{ detail?.activeEntryCount ?? current.taskCount }} 笔</b></div>
        <div><span>待填写金额</span><b>{{ detail?.unpricedCount ?? current.unpriced }} 笔</b></div>
        <div><span>当前金额</span><b>{{ money(detail?.amountFen) }}</b></div>
        <div><span>内容版本</span><b>V{{ detail?.revision ?? 1 }}</b></div>
      </div>

      <p v-if="detail && Number(detail.unpricedCount) > 0" class="rl-hint rl-hint--warn">
        还有 {{ detail.unpricedCount }} 条费用未填写。可以保留草稿，补齐并复核后才能发送。
      </p>

      <h3 class="rl-section-title">服务与费用明细</h3>
      <el-table :data="detail?.lines || []" :border="false" size="small" max-height="360">
        <el-table-column label="订单号" prop="orderNo" width="170" />
        <el-table-column label="业务日" width="120">
          <template #default="{ row }">{{ shortDate(row.businessDate) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">{{ money(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column label="说明" prop="note" min-width="160" />
      </el-table>
      <div v-if="detail && !(detail.lines || []).length" class="rl-empty">
        <div class="rl-empty__hint">本期没有可纳入的服务明细。</div>
      </div>

      <h3 class="rl-section-title">核对记录</h3>
      <el-timeline v-if="(detail?.reviews || []).length">
        <el-timeline-item v-for="(r, i) in detail?.reviews || []" :key="i" :timestamp="shortTime(r.reviewedAt)">
          {{ r.decision }} · {{ r.opinion || '无意见' }}
        </el-timeline-item>
      </el-timeline>
      <p v-else class="rl-sub">等待整理服务费用。</p>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detail && detail.status === 'DRAFT'" @click="submitReview(detail)">提交复核</el-button>
        <el-button v-if="detail && detail.status === 'INTERNAL_REVIEW'" type="success" @click="review(detail, true)">复核通过</el-button>
        <el-button v-if="detail && detail.status === 'INTERNAL_REVIEW'" @click="review(detail, false)">退回修改</el-button>
        <el-button v-if="detail && detail.status === 'INTERNAL_REVIEW'" type="primary" @click="send(detail)">发放给代驾</el-button>
        <el-button
          v-if="detail && detail.status !== 'VOID' && detail.status !== 'CONFIRMED'"
          type="danger"
          plain
          @click="voidIt(detail)"
        >作废</el-button>
      </template>
    </el-drawer>

    <!-- 新建对账单 -->
    <el-dialog v-model="createVisible" title="整理本期对账单" width="560px">
      <el-form label-width="120px">
        <el-form-item label="代驾 ID">
          <el-input v-model="createForm.driverId" placeholder="代驾档案 ID" />
        </el-form-item>
        <el-form-item label="费用条目">
          <el-input v-model="createForm.entryIds" type="textarea" :rows="3" placeholder="条目 ID，逗号分隔；留空表示按代驾全部未结算条目生成" />
        </el-form-item>
      </el-form>
      <p class="rl-hint">待核价不阻止建立草稿。新草稿纳入尚未被其他有效账单占用的服务明细。</p>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button @click="doPreview">试算</el-button>
        <el-button type="primary" @click="doCreate">建立草稿</el-button>
      </template>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliStatement" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
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
import { money, shortDate, shortTime } from '../format'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const entity = ref('MAIN')
const from = ref('')
const to = ref('')
const pageNum = ref(1)
const pageSize = 10

const createVisible = ref(false)
const createForm = reactive<any>({ driverId: '', entryIds: '' })

const detailVisible = ref(false)
const detail = ref<any>(null)
const current = ref<any>({})
const currentName = ref('')

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '草稿',
  INTERNAL_REVIEW: '内部复核',
  SENT: '已发给代驾核对',
  DISPUTED: '代驾提出差异',
  CONFIRMED: '已确认',
  VOID: '已作废'
}

/** 按代驾聚合：任务数、待填价数与当前有效账单 */
const driverRows = computed(() => {
  const map = new Map<string, any>()
  for (const row of rows.value) {
    const key = String(row.driverId)
    if (!map.has(key)) {
      map.set(key, {
        driverId: row.driverId,
        driverName: row.driverName,
        taskCount: 0,
        unpriced: 0,
        statement: null
      })
    }
    const item = map.get(key)
    item.taskCount += Number(row.activeEntryCount || 0)
    item.unpriced += Number(row.unpricedCount || 0)
    if (row.status !== 'VOID' && !item.statement) item.statement = row
  }
  return [...map.values()]
})

const filteredRows = computed(() =>
  driverRows.value.filter((r) => {
    if (!r.statement) return true
    const start = (r.statement.periodStart || '').slice(0, 10)
    const end = (r.statement.periodEnd || '').slice(0, 10)
    if (from.value && end && end < from.value) return false
    if (to.value && start && start > to.value) return false
    return true
  })
)

const pagedRows = computed(() => filteredRows.value.slice((pageNum.value - 1) * pageSize, pageNum.value * pageSize))

onMounted(load)

function statementLabel(value?: string) {
  return STATUS_LABEL[value || ''] || value || '—'
}

function statementTone(value?: string) {
  if (value === 'CONFIRMED') return 'rl-tag--green'
  if (value === 'INTERNAL_REVIEW' || value === 'DISPUTED') return 'rl-tag--amber'
  if (value === 'SENT') return 'rl-tag--blue'
  if (value === 'VOID') return 'rl-tag--gray'
  return 'rl-tag--gray'
}

async function load() {
  loading.value = true
  try {
    const res: any = await listStatements({ pageNum: 1, pageSize: 100 })
    rows.value = (res && res.rows) || []
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  from.value = ''
  to.value = ''
  pageNum.value = 1
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
  ElMessage.success(`试算金额 ${money(res.amountFen)}，共 ${(res.lines || []).length} 条`)
}

async function doCreate() {
  if (!createForm.driverId) {
    ElMessage.warning('请填写代驾 ID')
    return
  }
  await createStatement(buildBody())
  ElMessage.success('对账单已创建')
  createVisible.value = false
  load()
}

async function openDriver(row: any) {
  current.value = row
  currentName.value = row.driverName || '代驾'
  detail.value = null
  detailVisible.value = true
  if (row.statement) {
    detail.value = await getStatement(row.statement.statementId)
  }
}

async function submitReview(row: any) {
  await submitStatementReview(row.statementId)
  ElMessage.success('已提交内部复核')
  detailVisible.value = false
  load()
}

async function review(row: any, approve: boolean) {
  await reviewStatement(row.statementId, { approve, opinion: approve ? '复核无误' : '存在差异，退回修改' })
  ElMessage.success('复核完成')
  detailVisible.value = false
  load()
}

async function send(row: any) {
  await ElMessageBox.confirm('发出后代驾可在小程序核对并确认，是否继续？', '发出对账单', { type: 'warning' })
  await sendStatement(row.statementId)
  ElMessage.success('已发出')
  detailVisible.value = false
  load()
}

async function voidIt(row: any) {
  await ElMessageBox.confirm('作废后不可恢复，是否继续？', '作废对账单', { type: 'warning' })
  await voidStatement(row.statementId)
  ElMessage.success('已作废')
  detailVisible.value = false
  load()
}

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.statement-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 0 0 14px;
}
.statement-heading b {
  font-size: 14px;
}
.statement-heading p {
  margin: 4px 0 0;
}
.rl-section-title {
  margin: 18px 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--rl-ink, #101828);
}
.rl-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: #475467;
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px 12px;
}
.rl-hint--warn {
  background: #fffaeb;
  color: #b54708;
}
</style>
