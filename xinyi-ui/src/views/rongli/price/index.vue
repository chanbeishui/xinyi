<template>
  <RongliPage title="计价规则" subtitle="车型字典、服务支持与计价版本共同决定报价。">
    <template #actions>
      <el-button @click="router.push(RL.vehicleType)">车型字典</el-button>
      <el-button type="primary" icon="Plus" @click="openCreate">新增计价草稿</el-button>
    </template>

    <section v-if="editing" class="rl-panel">
      <div class="rl-panel__head">
        <h3>编辑计价规则</h3>
        <span class="rl-muted">金额为本地演示配置；发布版本只作用于新订单。</span>
      </div>
      <div class="rl-panel__body">
        <div class="rl-grid rl-grid--2">
          <div>
            <div class="rl-panel__head" style="padding: 0 0 10px; border: 0">
              <h3>适用范围与费用</h3>
            </div>
            <el-form label-position="top" class="rl-form-grid">
              <el-form-item label="车型">
                <el-select v-model="form.vehicleTypeCode" placeholder="选择车型" style="width: 100%">
                  <el-option v-for="t in vehicleType" :key="t.value" :label="t.label" :value="t.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="检测类型">
                <el-input v-model="form.inspectionTypeCode" placeholder="ANNUAL" />
              </el-form-item>
              <el-form-item label="检测费（元）">
                <el-input v-model="form.inspectionFee" placeholder="300.00" />
              </el-form-item>
              <el-form-item label="起步费（元）">
                <el-input v-model="form.baseFee" placeholder="30.00" />
              </el-form-item>
              <el-form-item label="包含里程（km）">
                <el-input v-model="form.includedKm" placeholder="5" />
              </el-form-item>
              <el-form-item label="超里程单价（元 / km）">
                <el-input v-model="form.unitPrice" placeholder="3.00" />
              </el-form-item>
              <el-form-item label="进位方式">
                <el-select v-model="form.rounding" style="width: 100%">
                  <el-option label="向上取整" value="CEIL" />
                  <el-option label="实际里程" value="NONE" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务范围">
                <el-input v-model="form.scope" placeholder="已开放服务区域" />
              </el-form-item>
            </el-form>
          </div>

          <div>
            <div class="rl-panel__head" style="padding: 0 0 10px; border: 0">
              <h3>试算本次费用</h3>
            </div>
            <el-form label-position="top">
              <el-form-item label="示例单程距离（km）">
                <el-input v-model="trialKm" placeholder="6.4" />
              </el-form-item>
              <el-button style="width: 100%" @click="calc">按当前草稿试算</el-button>
            </el-form>
            <div class="rl-kv mt16">
              <div><span>试算总额</span><b>{{ trialTotal === null ? '待试算' : money(trialTotal) }}</b></div>
              <div><span>基础检测与起步</span><b>{{ money(baseSum) }}</b></div>
              <div><span>超里程费用</span><b>{{ trialTotal === null ? '—' : money(trialTotal - baseSum) }}</b></div>
              <div><span>订单规则</span><b>历史订单仍使用原版本</b></div>
            </div>
            <p class="rl-hint mt12">试算只按当前草稿参数本地计算，不写入任何订单或报价记录。</p>
          </div>
        </div>

        <div class="rl-toolbar">
          <el-button @click="cancelEdit">返回规则</el-button>
          <el-button :loading="saving" @click="save">保存草稿</el-button>
          <el-button v-if="form.ruleId && form.status === 'DRAFT'" type="primary" @click="submit(form)">提交审核</el-button>
        </div>
      </div>
    </section>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>计价方案与生效版本</h3>
        <span class="rl-muted">按车型汇总最新版本</span>
      </div>

      <el-table v-loading="loading" :data="grouped" :border="false">
        <el-table-column label="方案 / 车型" min-width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.typeLabel }}年检</div>
            <div class="rl-mono">{{ row.vehicleTypeCode }}<template v-if="row.rule"> · R{{ row.rule.revision }}</template></div>
          </template>
        </el-table-column>
        <el-table-column label="检测费" width="120" align="right">
          <template #default="{ row }">{{ row.rule ? money(row.rule.config?.inspectionFeeFen) : '未配置' }}</template>
        </el-table-column>
        <el-table-column label="起步 / 含里程" width="160">
          <template #default="{ row }">
            <template v-if="row.rule">
              {{ money(row.rule.config?.baseFeeFen) }} / {{ km(row.rule.config?.includedMeters) }}km
            </template>
            <span v-else class="rl-muted">未配置</span>
          </template>
        </el-table-column>
        <el-table-column label="超里程" width="140">
          <template #default="{ row }">
            <template v-if="row.rule">{{ money(row.rule.config?.unitPriceFen) }} / km</template>
            <span v-else class="rl-muted">未配置</span>
          </template>
        </el-table-column>
        <el-table-column label="进位" width="110">
          <template #default="{ row }">
            {{ row.rule ? (row.rule.config?.rounding === 'CEIL' ? '向上取整' : '实际里程') : '待配置' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row.rule?.status)">
              {{ row.rule ? statusLabel(row.rule.status) : '不可报价' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              {{ row.rule ? '编辑草稿' : '配置规则' }}
            </el-button>
            <el-button
              v-if="row.rule && row.rule.status === 'PENDING_REVIEW'"
              link
              type="primary"
              @click="openReview(row.rule)"
            >
              审核版本
            </el-button>
            <el-button v-else-if="row.rule && row.rule.status === 'APPROVED'" link type="success" @click="publish(row.rule)">
              发布
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">还没有车型</div>
            <div class="rl-empty__hint">先在车型字典维护车型，再为每个车型配置计价规则。</div>
            <el-button @click="router.push(RL.vehicleType)">前往车型字典</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-panel__body">
        <p class="rl-hint rl-hint--warn">
          禁止随机命中多条规则、未配置按 0 元下单、或新价格追改已创建支付单。
        </p>
      </div>
    </section>

    <!-- 审核并发布 -->
    <el-drawer v-model="reviewVisible" title="审核并发布计价版本" size="520px">
      <div class="rl-kv">
        <div><span>待发布版本</span><b>{{ reviewTarget.ruleKey }} / R{{ reviewTarget.revision }}</b></div>
        <div><span>适用车型</span><b>{{ typeName(reviewTarget.vehicleTypeCode) }}</b></div>
        <div><span>检测费用</span><b>{{ money(reviewTarget.config?.inspectionFeeFen) }}</b></div>
        <div><span>起步费用</span><b>{{ money(reviewTarget.config?.baseFeeFen) }}</b></div>
        <div><span>生效时间</span><b>{{ shortTime(reviewTarget.effectiveFrom) }}</b></div>
        <div><span>适用订单</span><b>生效后新建订单</b></div>
      </div>
      <el-form label-position="top" class="mt16">
        <el-form-item label="审核意见">
          <el-input v-model="reviewOpinion" type="textarea" :rows="3" placeholder="填写审核意见" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="reviewConfirmed">确认不改动历史订单价格</el-checkbox>
        </el-form-item>
      </el-form>
      <p class="rl-hint">提交人与审核人的实际权限在服务端校验；本页面只演示两个角色的审批界面。</p>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!reviewOpinion" @click="audit(false)">退回修改</el-button>
        <el-button type="primary" :disabled="!reviewOpinion || !reviewConfirmed" @click="audit(true)">
          审核通过
        </el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliPrice" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditPrice, createPrice, listPrices, publishPrice, submitPrice, updatePrice } from '@/api/rongli'
import { money, shortTime } from '../format'
import { RL } from '../routes'

const router = useRouter()
const { vehicle_type } = useDict('vehicle_type')
const vehicleType = vehicle_type as any

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])

const editing = ref(false)
const trialKm = ref('6.4')
const trialTotal = ref<number | null>(null)

const reviewVisible = ref(false)
const reviewTarget = ref<any>({})
const reviewOpinion = ref('')
const reviewConfirmed = ref(false)

const form = reactive<any>({
  ruleId: '',
  status: 'DRAFT',
  vehicleTypeCode: 'sedan',
  inspectionTypeCode: 'ANNUAL',
  inspectionFee: '300.00',
  baseFee: '30.00',
  includedKm: '5',
  unitPrice: '3.00',
  rounding: 'CEIL',
  scope: '已开放服务区域'
})

/** 每个车型取最新版本，未配置的车型也列出来，避免「漏配当 0 元」 */
const grouped = computed(() => {
  const types = (vehicleType.value || []).length
    ? vehicleType.value
    : [{ value: 'sedan', label: '轿车' }]
  return types.map((t: any) => {
    const list = rows.value.filter((r) => r.vehicleTypeCode === t.value)
    list.sort((a, b) => Number(b.revision || 0) - Number(a.revision || 0))
    const published = list.find((r) => r.status === 'PUBLISHED')
    return {
      vehicleTypeCode: t.value,
      typeLabel: t.label,
      rule: published || list[0] || null
    }
  })
})

const baseSum = computed(() => toFen(form.inspectionFee) + toFen(form.baseFee))

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listPrices({ pageNum: 1, pageSize: 100 })
    rows.value = (res && res.rows) || []
  } finally {
    loading.value = false
  }
}

function typeName(code: string) {
  return (vehicleType.value || []).find((t: any) => t.value === code)?.label || code
}

function km(meters: any) {
  const n = Number(meters || 0)
  return n ? +(n / 1000).toFixed(2) : 0
}

function toFen(yuan: any) {
  const n = Number(yuan)
  return Number.isFinite(n) ? Math.round(n * 100) : 0
}

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  APPROVED: '已审核',
  PUBLISHED: '已发布',
  DISABLED: '已停用'
}

function statusLabel(code?: string) {
  if (!code) return '—'
  return STATUS_LABEL[code] || code
}

function statusClass(code?: string) {
  if (code === 'PUBLISHED') return 'rl-tag--green'
  if (code === 'PENDING_REVIEW' || code === 'APPROVED') return 'rl-tag--amber'
  if (code === 'DISABLED') return 'rl-tag--gray'
  return 'rl-tag--blue'
}

function openCreate() {
  Object.assign(form, {
    ruleId: '',
    status: 'DRAFT',
    vehicleTypeCode: 'sedan',
    inspectionTypeCode: 'ANNUAL',
    inspectionFee: '300.00',
    baseFee: '30.00',
    includedKm: '5',
    unitPrice: '3.00',
    rounding: 'CEIL',
    scope: '已开放服务区域'
  })
  trialTotal.value = null
  editing.value = true
}

function openEdit(row: any) {
  const r = row.rule
  if (!r) {
    openCreate()
    form.vehicleTypeCode = row.vehicleTypeCode
    return
  }
  const c = r.config || {}
  Object.assign(form, {
    ruleId: String(r.ruleId),
    status: r.status,
    vehicleTypeCode: r.vehicleTypeCode,
    inspectionTypeCode: r.inspectionTypeCode,
    inspectionFee: (Number(c.inspectionFeeFen || 0) / 100).toFixed(2),
    baseFee: (Number(c.baseFeeFen || 0) / 100).toFixed(2),
    includedKm: String(km(c.includedMeters)),
    unitPrice: (Number(c.unitPriceFen || 0) / 100).toFixed(2),
    rounding: c.rounding || 'CEIL',
    scope: c.scope || '已开放服务区域'
  })
  trialTotal.value = null
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

function calc() {
  const distanceM = Number(trialKm.value) * 1000
  if (!Number.isFinite(distanceM) || distanceM <= 0) {
    ElMessage.warning('请填写有效的示例距离')
    return
  }
  const included = Number(form.includedKm) * 1000
  const over = Math.max(0, distanceM - included)
  let extra = Math.round((over / 1000) * toFen(form.unitPrice))
  if (form.rounding === 'CEIL' && over > 0) {
    extra = Math.ceil(over / 1000) * toFen(form.unitPrice)
  }
  trialTotal.value = toFen(form.inspectionFee) + toFen(form.baseFee) + extra
}

async function save() {
  if (!form.vehicleTypeCode) {
    ElMessage.warning('请选择车型')
    return
  }
  const payload = {
    vehicleTypeCode: form.vehicleTypeCode,
    inspectionTypeCode: form.inspectionTypeCode || 'ANNUAL',
    config: {
      inspectionFeeFen: toFen(form.inspectionFee),
      baseFeeFen: toFen(form.baseFee),
      includedMeters: Math.round(Number(form.includedKm || 0) * 1000),
      unitMeters: 1000,
      unitPriceFen: toFen(form.unitPrice),
      rounding: form.rounding
    }
  }
  saving.value = true
  try {
    if (form.ruleId) {
      await updatePrice(form.ruleId, payload)
    } else {
      await createPrice(payload)
    }
    ElMessage.success('草稿已保存')
    editing.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function submit(row: any) {
  await submitPrice(row.ruleId)
  ElMessage.success('已提交审核')
  editing.value = false
  load()
}

function openReview(row: any) {
  reviewTarget.value = row
  reviewOpinion.value = ''
  reviewConfirmed.value = false
  reviewVisible.value = true
}

async function audit(approve: boolean) {
  await auditPrice(reviewTarget.value.ruleId, { approve, opinion: reviewOpinion.value })
  ElMessage.success(approve ? '已审核通过，可发布' : '已退回修改')
  reviewVisible.value = false
  load()
}

async function publish(row: any) {
  await ElMessageBox.confirm('发布后新订单将使用该版本，历史订单保留原价格快照。', '发布计价版本', {
    type: 'warning'
  })
  await publishPrice(row.ruleId)
  ElMessage.success('已发布')
  load()
}
</script>
