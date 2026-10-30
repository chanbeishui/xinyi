<template>
  <RongliPage title="代驾限制申诉" subtitle="从本人处罚记录产生的申诉；查询、审核和公开说明闭合。">
    <template #actions>
      <el-button icon="Refresh" @click="loadAll">刷新</el-button>
      <el-button @click="router.push(RL.driver)">返回代驾员</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>申诉列表</h3>
        <el-select v-model="appealQuery.status" placeholder="全部状态" style="width: 150px" @change="loadAppeals">
          <el-option label="全部状态" value="" />
          <el-option label="待处理" value="SUBMITTED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="未通过" value="REJECTED" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="appealRows" :border="false" highlight-current-row @current-change="pick">
        <el-table-column label="申诉" width="160">
          <template #default="{ row }">
            <div class="rl-strong">APPEAL-{{ row.appealId }}</div>
            <div class="rl-mono">处罚 {{ row.penaltyId || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="代驾 / 处罚" width="180">
          <template #default="{ row }">
            <div>{{ row.driverName || '—' }}</div>
            <div class="rl-sub">{{ row.penaltyTypeLabel || row.penaltyType || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="240">
          <template #default="{ row }">
            <div class="rl-cell-sub">{{ row.reason || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="appealClass(row.status)">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="150">
          <template #default="{ row }">{{ shortTime(row.submittedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="pick(row)">处理</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有申诉</div>
            <div class="rl-empty__hint">申诉只能从本人处罚记录产生，不能由后台代建。</div>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="appealQuery.pageNum"
          v-model:page-size="appealQuery.pageSize"
          :total="appealTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="loadAppeals"
          @size-change="loadAppeals"
        />
      </div>
    </section>

    <div class="rl-grid rl-grid--2 mt16">
      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>审核处理</h3>
          <span class="rl-muted">{{ current.appealId ? 'APPEAL-' + current.appealId : '未选择申诉' }}</span>
        </div>
        <div class="rl-panel__body">
          <template v-if="current.appealId">
            <div class="rl-kv" style="margin-bottom: 12px">
              <div><span>代驾</span><b>{{ current.driverName || '—' }}</b></div>
              <div><span>原判定原因</span><b>{{ current.penaltyReason || '—' }}</b></div>
              <div style="grid-column: span 2"><span>申诉说明</span><b>{{ current.reason || '—' }}</b></div>
            </div>
            <el-form label-position="top">
              <el-form-item label="对代驾公开的处理说明">
                <el-input
                  v-model="opinion"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="说明核实结论与处理依据，代驾端可见"
                />
              </el-form-item>
            </el-form>
            <p class="rl-hint">
              通过不删除原处罚事件；记录撤销及理由。不能自审自己的申诉，实际权限与同人检查由服务端执行。
            </p>
            <div class="rl-toolbar">
              <el-button type="danger" :disabled="decided" :loading="saving" @click="submit(false)">驳回</el-button>
              <el-button type="primary" :disabled="decided" :loading="saving" @click="submit(true)">通过申诉</el-button>
            </div>
          </template>
          <div v-else class="rl-empty">
            <div class="rl-empty__title">先选择一条申诉</div>
            <div class="rl-empty__hint">选择后在这里填写对代驾公开的处理说明。</div>
          </div>
        </div>
      </section>

      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>接单限制记录</h3>
          <span class="rl-muted">共 {{ penaltyTotal }} 条</span>
        </div>
        <el-table :data="penaltyRows" :border="false" max-height="240">
          <el-table-column label="代驾" width="110">
            <template #default="{ row }">{{ row.driverName || row.driverId }}</template>
          </el-table-column>
          <el-table-column label="限制类型" width="110">
            <template #default="{ row }">{{ row.penaltyTypeLabel || row.penaltyType }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="rl-tag" :class="row.status === 'ACTIVE' ? 'rl-tag--red' : 'rl-tag--gray'">
                {{ row.statusLabel }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="生效至" width="150">
            <template #default="{ row }">{{ shortTime(row.effectiveUntil) }}</template>
          </el-table-column>
          <el-table-column label="原因" min-width="160">
            <template #default="{ row }"><span class="rl-cell-sub">{{ row.reason }}</span></template>
          </el-table-column>
          <template #empty>
            <div class="rl-empty">
              <div class="rl-empty__title">没有限制记录</div>
              <div class="rl-empty__hint">接单限制由处罚产生，生效期间不得接单。</div>
            </div>
          </template>
        </el-table>
      </section>
    </div>
  </RongliPage>
</template>

<script setup name="RongliPenalty" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { auditAppeal, listAppeals, listPenalties } from '@/api/rongli'
import { shortTime } from '../format'
import { RL } from '../routes'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const penaltyRows = ref<any[]>([])
const penaltyTotal = ref(0)
const penaltyQuery = reactive<any>({ driverId: '', status: '', pageNum: 1, pageSize: 10 })

const appealRows = ref<any[]>([])
const appealTotal = ref(0)
const appealQuery = reactive<any>({ driverId: '', status: '', pageNum: 1, pageSize: 10 })

const current = ref<any>({})
const opinion = ref('')

const decided = computed(() => !!current.value.status && current.value.status !== 'SUBMITTED')

onMounted(loadAll)

async function loadAll() {
  await Promise.all([loadAppeals(), loadPenalties()])
}

/** driverId 是数字参数，空串会被后端类型转换拒绝，需剔除 */
function clean(query: any) {
  const params: any = { ...query }
  Object.keys(params).forEach((k) => {
    if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
  })
  return params
}

async function loadPenalties() {
  const res: any = await listPenalties(clean(penaltyQuery)).catch(() => null)
  penaltyRows.value = (res && res.rows) || []
  penaltyTotal.value = Number((res && res.total) || 0)
}

async function loadAppeals() {
  loading.value = true
  try {
    const res: any = await listAppeals(clean(appealQuery))
    appealRows.value = (res && res.rows) || []
    appealTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function appealClass(status: string) {
  if (status === 'APPROVED') return 'rl-tag--green'
  if (status === 'REJECTED') return 'rl-tag--gray'
  return 'rl-tag--amber'
}

function pick(row: any) {
  if (!row) return
  current.value = row
  opinion.value = row.publicOpinion || ''
}

async function submit(approve: boolean) {
  if (!opinion.value.trim()) {
    ElMessage.warning('请填写对代驾公开的处理说明')
    return
  }
  saving.value = true
  try {
    await auditAppeal(current.value.appealId, { approve, opinion: opinion.value })
    ElMessage.success(approve ? '已通过，限制已撤销' : '已驳回')
    current.value = {}
    opinion.value = ''
    await loadAll()
  } finally {
    saving.value = false
  }
}
</script>
