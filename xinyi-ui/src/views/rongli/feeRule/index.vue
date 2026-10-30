<template>
  <RongliPage title="代驾费用规则" subtitle="决定代驾应付，不替代车主端的计价规则。">
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" icon="Plus" @click="openCreate">新增费用规则</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>当前规则</h3>
        <span class="rl-muted">报酬规则版本化，发布只影响新产生的费用</span>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="规则" min-width="180">
          <template #default="{ row }">
            <div class="rl-strong">{{ ruleName(row) }}</div>
            <div class="rl-mono">{{ row.ruleCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="适用场景" width="170">
          <template #default="{ row }">{{ scenarioLabel(row.scenario) }}</template>
        </el-table-column>
        <el-table-column label="计费方式" min-width="240">
          <template #default="{ row }">{{ pricingText(row) }}</template>
        </el-table-column>
        <el-table-column label="版本" width="120">
          <template #default="{ row }">
            <span class="rl-mono">{{ row.ruleCode }} / V{{ row.revision }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="150">
          <template #default="{ row }">{{ shortTime(row.effectiveFrom) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              {{ row.status === 'PUBLISHED' ? '查看配置' : '编辑规则' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">还没有费用规则</div>
            <div class="rl-empty__hint">未匹配规则的服务进入「待核价」，不自动给零。</div>
            <el-button type="primary" @click="openCreate">新增费用规则</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-panel__body">
        <p class="rl-hint rl-hint--warn">
          具体费用、取消补偿和适用对象需由运营确认配置。未匹配规则的服务进入「待核价」，不自动给零。
        </p>
      </div>
    </section>

    <el-drawer v-model="formVisible" :title="form.ruleId ? '编辑代驾费用规则' : '新增代驾费用规则'" size="540px">
      <el-form label-position="top">
        <el-form-item label="规则名称（规则编码）" required>
          <el-input v-model="form.ruleCode" placeholder="例如 DEFAULT_NORMAL" :disabled="!!form.ruleId" />
        </el-form-item>
        <el-form-item label="适用场景" required>
          <el-select v-model="form.scenario" style="width: 100%" :disabled="!!form.ruleId">
            <el-option label="正常归还" value="NORMAL_RETURN" />
            <el-option label="已接车取消并实际返还" value="CANCEL_RETURN" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用代驾范围">
          <el-input v-model="form.scopeText" placeholder="本主体已审核代驾" />
        </el-form-item>
        <div class="rl-form-grid">
          <el-form-item label="正常归还基础费（元）" required>
            <el-input v-model="form.baseFee" placeholder="40.00" />
          </el-form-item>
          <el-form-item label="包含里程（公里）" required>
            <el-input v-model="form.includedKm" placeholder="8" />
          </el-form-item>
          <el-form-item label="超里程单价（元 / 公里）" required>
            <el-input v-model="form.unitPrice" placeholder="5.00" />
          </el-form-item>
          <el-form-item label="进位方式">
            <el-select v-model="form.rounding" style="width: 100%">
              <el-option label="向上取整" value="CEIL" />
              <el-option label="实际里程" value="NONE" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="试算约定路线（公里）">
          <div class="rl-trial">
            <el-input v-model="trialKm" placeholder="14" style="width: 160px" />
            <el-button :loading="trialing" @click="runTrial">试算</el-button>
            <span v-if="trialFee !== null" class="rl-trial__result">本次试算应付 {{ money(trialFee) }}</span>
          </div>
        </el-form-item>
        <el-form-item label="变更说明">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="说明本次规则调整原因" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.checked">已核对规则，不追改历史费用快照</el-checkbox>
        </el-form-item>
      </el-form>

      <p class="rl-hint">试算调用服务端已发布规则，不写入账单；发布后新产生的费用按新版本计算。</p>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button :loading="saving" @click="save">保存草稿</el-button>
        <el-button
          type="primary"
          :disabled="!form.checked || !form.ruleId || form.status !== 'DRAFT'"
          :loading="saving"
          @click="publish"
        >
          发布新版本
        </el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliFeeRule" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createFeeRule, listFeeRules, publishFeeRule, trialFeeRule, updateFeeRule } from '@/api/rongli'
import { money, shortTime } from '../format'

const loading = ref(false)
const saving = ref(false)
const trialing = ref(false)
const rows = ref<any[]>([])

const formVisible = ref(false)
const trialKm = ref('14')
const trialFee = ref<number | null>(null)

const form = reactive<any>({
  ruleId: '',
  status: 'DRAFT',
  ruleCode: 'DEFAULT_NORMAL',
  scenario: 'NORMAL_RETURN',
  scopeText: '本主体已审核代驾',
  baseFee: '40.00',
  includedKm: '8',
  unitPrice: '5.00',
  rounding: 'CEIL',
  reason: '',
  checked: false
})

const SCENARIO_LABEL: Record<string, string> = {
  NORMAL_RETURN: '正常归还',
  CANCEL_RETURN: '已接车取消并实际返还'
}

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  PUBLISHED: '已发布',
  DISABLED: '已停用'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listFeeRules()
    rows.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  } finally {
    loading.value = false
  }
}

function scenarioLabel(code: string) {
  return SCENARIO_LABEL[code] || code
}

function statusLabel(code: string) {
  return STATUS_LABEL[code] || code
}

function statusClass(code: string) {
  if (code === 'PUBLISHED') return 'rl-tag--green'
  if (code === 'PENDING_REVIEW') return 'rl-tag--amber'
  if (code === 'DISABLED') return 'rl-tag--gray'
  return 'rl-tag--blue'
}

function ruleName(row: any) {
  return scenarioLabel(row.scenario) + '费用'
}

function pricingText(row: any) {
  const p = row.pricing || {}
  const km = (v: any) => +(Number(v || 0) / 1000).toFixed(2)
  return `基础 ${money(p.baseFen)} + 含 ${km(p.includedMeters)}km，超出 ${money(p.unitPriceFen)}/km`
}

function toFen(yuan: any) {
  const n = Number(yuan)
  return Number.isFinite(n) ? Math.round(n * 100) : 0
}

function openCreate() {
  Object.assign(form, {
    ruleId: '',
    status: 'DRAFT',
    ruleCode: 'DEFAULT_NORMAL',
    scenario: 'NORMAL_RETURN',
    scopeText: '本主体已审核代驾',
    baseFee: '40.00',
    includedKm: '8',
    unitPrice: '5.00',
    rounding: 'CEIL',
    reason: '',
    checked: false
  })
  trialFee.value = null
  formVisible.value = true
}

function openEdit(row: any) {
  const p = row.pricing || {}
  Object.assign(form, {
    ruleId: String(row.ruleId),
    status: row.status,
    ruleCode: row.ruleCode,
    scenario: row.scenario,
    scopeText: (row.scope && row.scope.vehicleTypes ? '本主体已审核代驾' : '') || '本主体已审核代驾',
    baseFee: (Number(p.baseFen || 0) / 100).toFixed(2),
    includedKm: String(+(Number(p.includedMeters || 0) / 1000).toFixed(2)),
    unitPrice: (Number(p.unitPriceFen || 0) / 100).toFixed(2),
    rounding: p.rounding || 'CEIL',
    reason: '',
    checked: false
  })
  trialFee.value = null
  formVisible.value = true
}

async function save() {
  if (!form.ruleCode) {
    ElMessage.warning('请填写规则编码')
    return
  }
  const payload = {
    ruleCode: form.ruleCode,
    scenario: form.scenario,
    scope: JSON.stringify({ note: form.scopeText }),
    pricing: JSON.stringify({
      baseFen: toFen(form.baseFee),
      includedMeters: Math.round(Number(form.includedKm || 0) * 1000),
      unitMeters: 1000,
      unitPriceFen: toFen(form.unitPrice),
      rounding: form.rounding
    })
  }
  saving.value = true
  try {
    if (form.ruleId) {
      await updateFeeRule(form.ruleId, payload)
    } else {
      const created: any = await createFeeRule(payload)
      form.ruleId = String(created?.ruleId || created?.id || '')
      form.status = created?.status || 'DRAFT'
    }
    ElMessage.success('草稿已保存')
    load()
  } finally {
    saving.value = false
  }
}

async function runTrial() {
  const km = Number(trialKm.value)
  if (!Number.isFinite(km) || km <= 0) {
    ElMessage.warning('请填写有效的试算里程')
    return
  }
  trialing.value = true
  try {
    const res: any = await trialFeeRule({ scenario: form.scenario, distanceMeters: Math.round(km * 1000) })
    trialFee.value = Number(res?.feeFen ?? res?.totalFen ?? 0)
  } finally {
    trialing.value = false
  }
}

async function publish() {
  await publishFeeRule(form.ruleId, form.reason)
  ElMessage.success('已发布新版本')
  formVisible.value = false
  load()
}
</script>

<style scoped>
.rl-trial {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.rl-trial__result {
  color: #067647;
  font-size: 12.5px;
}
</style>
