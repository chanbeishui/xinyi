<template>
  <div class="app-container pricing">
    <el-alert type="info" :closable="false" title="同公司统一计价，旗下站点共用。草稿独立审核后发布；试算不创建订单，不修改历史订单金额。" />
    <div class="toolbar">
      <el-tag :type="environment === 'LIVE' ? 'danger' : 'info'">{{ environment === 'LIVE' ? '生产环境' : '测试环境' }}</el-tag>
      <el-select v-model="filter.ownerDeptId" clearable placeholder="全部可见公司" @change="pageNum = 1; load()"><el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" /></el-select>
      <el-select v-model="filter.status" clearable placeholder="全部状态" @change="pageNum = 1; load()"><el-option v-for="(label, key) in statusNames" :key="key" :label="label" :value="key" /></el-select>
      <el-button v-hasPermi="['biz:price:create']" type="primary" @click="openForm()">新增计价方案</el-button>
      <el-button @click="openMatch">生效价格试算</el-button><el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="公司" prop="companyName" min-width="145" />
      <el-table-column label="计价方案" min-width="170"><template #default="{ row }">{{ row.config?.name || '内容异常，请核实' }}</template></el-table-column>
      <el-table-column label="适用车型 / 检测类型" min-width="170"><template #default="{ row }">{{ labelFor(vehicleTypes, row.config?.vehicleTypeCode) }} / {{ labelFor(inspectionTypes, row.config?.inspectionTypeCode) }}</template></el-table-column>
      <el-table-column label="检测费 / 起步价（元）" min-width="175"><template #default="{ row }">{{ money(row.config?.inspectionFee) }} / {{ money(row.config?.driverBaseFee) }}</template></el-table-column>
      <el-table-column label="修订 / 状态" width="150"><template #default="{ row }">第 {{ row.revision }} 版 · {{ statusNames[row.status as keyof typeof statusNames] }}</template></el-table-column>
      <el-table-column label="生效时间" min-width="200"><template #default="{ row }">{{ time(row.config?.effectiveFrom) }}<br />至 {{ time(row.config?.effectiveTo) }}</template></el-table-column>
      <el-table-column label="操作" min-width="300"><template #default="{ row }">
        <el-button v-if="row.config" link type="primary" @click="openTrial(row)">试算</el-button>
        <el-button v-if="row.status === 'DRAFT' && row.config" v-hasPermi="['biz:price:edit']" link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button v-if="row.status === 'DRAFT'" v-hasPermi="['biz:price:submit']" link type="primary" @click="openAction(row, 'submit')">提交审核</el-button>
        <el-button v-if="row.status === 'IN_REVIEW'" v-hasPermi="['biz:price:audit']" link type="primary" @click="openAction(row, 'audit')">独立审核</el-button>
        <el-button v-if="row.status === 'APPROVED'" v-hasPermi="['biz:price:publish']" link type="primary" @click="openAction(row, 'publish')">发布</el-button>
        <el-button v-if="['PUBLISHED', 'DISABLED'].includes(row.status) && row.config" v-hasPermi="['biz:price:create']" link type="primary" @click="openForm(row, true)">派生新版本</el-button>
        <el-button v-if="['PUBLISHED', 'APPROVED'].includes(row.status)" v-hasPermi="['biz:price:publish']" link type="danger" @click="openAction(row, 'disable')">停用</el-button>
      </template></el-table-column>
    </el-table>
    <el-pagination layout="total, prev, pager, next" :total="total" :page-size="20" :current-page="pageNum" @current-change="p => { pageNum = p; load() }" />

    <el-dialog v-model="formOpen" :title="form.ruleId ? '编辑计价草稿' : form.sourceRuleId ? '派生计价新版本' : '新增计价方案'" width="min(760px, 96vw)" :close-on-click-modal="false">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="公司部门" required><el-select v-model="form.ownerDeptId" filterable :disabled="!!form.ruleId || !!form.sourceRuleId" placeholder="明确选择归属公司"><el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" /></el-select></el-form-item>
          <el-form-item label="方案名称" required><el-input v-model="form.name" maxlength="100" /></el-form-item>
          <el-form-item label="适用车型" required><el-select v-model="form.vehicleTypeCode" :disabled="!!form.ruleId || !!form.sourceRuleId"><el-option v-for="v in vehicleTypes" :key="v.value" :label="v.label" :value="v.value" /></el-select></el-form-item>
          <el-form-item label="检测类型" required><el-select v-model="form.inspectionTypeCode" :disabled="!!form.ruleId || !!form.sourceRuleId"><el-option v-for="v in inspectionTypes" :key="v.value" :label="v.label" :value="v.value" /></el-select></el-form-item>
          <el-form-item label="检测费（元）" required><el-input v-model="form.inspectionFee" inputmode="decimal" placeholder="最多两位小数" /></el-form-item>
          <el-form-item label="代驾起步价（元）" required><el-input v-model="form.driverBaseFee" inputmode="decimal" /></el-form-item>
          <el-form-item label="包含里程（米）" required><el-input-number v-model="form.includedDistanceMeter" :min="0" :max="2147483647" :precision="0" /></el-form-item>
          <el-form-item label="超里程计费单位（米）" required><el-input-number v-model="form.distanceUnitMeter" :min="1" :max="2147483647" :precision="0" /></el-form-item>
          <el-form-item label="每单位超里程费（元）" required><el-input v-model="form.distanceUnitFee" inputmode="decimal" /></el-form-item>
          <el-form-item label="进位方式" required><el-select v-model="form.distanceRounding"><el-option label="不足一单位按整单位进位" value="CEIL_UNIT" /><el-option label="按实际比例计费，四舍五入到分" value="EXACT_PRORATA" /></el-select></el-form-item>
          <el-form-item label="路线口径" required><el-select v-model="form.routeBasis"><el-option label="单程" value="ONE_WAY" /><el-option label="往返：单程距离乘二" value="ROUND_TRIP" /></el-select></el-form-item>
          <el-form-item label="环境"><el-tag>{{ environment === 'LIVE' ? '生产' : '测试' }}（由当前部署确定）</el-tag></el-form-item>
          <el-form-item label="开始生效（提交审核前必填）"><el-date-picker v-model="form.start" type="datetime" placeholder="本地时间" /></el-form-item>
          <el-form-item label="结束生效（不填则持续有效）"><el-date-picker v-model="form.end" type="datetime" placeholder="本地时间" /></el-form-item>
        </div>
        <el-alert type="info" :closable="false" title="等待费尚未开放；保存草稿不会生效。审核通过后不能覆盖价格内容，需派生新版本。" />
      </el-form>
      <template #footer><el-button @click="formOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveForm">保存草稿</el-button></template>
    </el-dialog>

    <el-dialog v-model="actionOpen" :title="actionTitles[actionKind]" width="min(650px, 96vw)" :close-on-click-modal="false">
      <el-descriptions v-if="actionRow" :column="1" border>
        <el-descriptions-item label="公司 / 方案">{{ actionRow.companyName }} / {{ actionRow.config?.name || '内容异常' }}</el-descriptions-item>
        <el-descriptions-item label="修订">第 {{ actionRow.revision }} 版</el-descriptions-item>
        <el-descriptions-item label="费用">检测费 {{ money(actionRow.config?.inspectionFee) }} 元；起步价 {{ money(actionRow.config?.driverBaseFee) }} 元；每单位超里程 {{ money(actionRow.config?.distanceUnitFee) }} 元</el-descriptions-item>
        <el-descriptions-item label="计费方式">包含 {{ actionRow.config?.includedDistanceMeter }} 米，每 {{ actionRow.config?.distanceUnitMeter }} 米一单位；{{ actionRow.config?.routeBasis === 'ROUND_TRIP' ? '往返' : '单程' }}；{{ actionRow.config?.distanceRounding === 'EXACT_PRORATA' ? '实际比例四舍五入到分' : '整单位进位' }}</el-descriptions-item>
        <el-descriptions-item label="生效区间">{{ time(actionRow.config?.effectiveFrom) }} 至 {{ time(actionRow.config?.effectiveTo) }}</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="actionKind === 'audit'" type="warning" :closable="false" title="提交人和该草稿的编辑参与人不能审核；通过将绑定本次完整内容。" />
      <el-alert v-if="actionKind === 'disable'" type="warning" :closable="false" title="停用将阻止新的价格匹配；不会修改历史订单和已保存报价金额。" />
      <el-form label-position="top">
        <el-form-item v-if="actionKind === 'audit'" label="审核决定"><el-radio-group v-model="decision"><el-radio value="APPROVE">通过</el-radio><el-radio value="REJECT">退回草稿</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="actionKind === 'publish'" label="是否立即替换已发布版本"><el-select v-model="replacesId" clearable placeholder="不替换：必须与已有生效区间不重叠"><el-option v-for="p in replacementChoices" :key="p.ruleId" :label="`${p.config?.name} · 第${p.revision}版`" :value="p.ruleId" /></el-select></el-form-item>
        <el-form-item :label="actionKind === 'audit' ? '审核意见' : '操作原因'" required><el-input v-model="reason" type="textarea" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="actionOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveAction">{{ actionTitles[actionKind] }}</el-button></template>
    </el-dialog>

    <el-dialog v-model="trialOpen" :title="trialRow ? '已保存规则试算' : '生效价格匹配试算'" width="min(650px, 96vw)">
      <el-alert :closable="false" type="info" :title="trialRow ? '仅试算已保存版本；草稿试算通过不代表已可用于预约。' : '根据真实站点、公司、车型、检测类型和服务时间匹配唯一已发布规则。'" />
      <el-form label-position="top">
        <template v-if="!trialRow">
          <el-form-item label="服务站点" required><el-select v-model="match.stationId" filterable><el-option v-for="s in stations" :key="s.stationId" :label="s.name" :value="s.stationId" /></el-select></el-form-item>
          <el-form-item label="车型" required><el-select v-model="match.vehicleTypeCode"><el-option v-for="v in vehicleTypes" :key="v.value" :label="v.label" :value="v.value" /></el-select></el-form-item>
          <el-form-item label="检测类型" required><el-select v-model="match.inspectionTypeCode"><el-option v-for="v in inspectionTypes" :key="v.value" :label="v.label" :value="v.value" /></el-select></el-form-item>
          <el-form-item label="服务时间"><el-date-picker v-model="match.at" type="datetime" placeholder="不填则为当前时刻" /></el-form-item>
        </template>
        <el-form-item label="单程路线距离（米）" required><el-input-number v-model="trialDistance" :min="0" :max="2147483647" :precision="0" /></el-form-item>
        <el-button type="primary" :loading="trialLoading" @click="runTrial">开始试算</el-button>
      </el-form>
      <el-descriptions v-if="trialResult" :column="1" border class="trial-result">
        <el-descriptions-item label="使用版本">第 {{ trialResult.revision }} 版 · {{ trialResult.matchedPublished ? '唯一生效匹配' : '已保存版本预览' }}</el-descriptions-item>
        <el-descriptions-item label="距离">计费路线 {{ trialResult.billableDistanceMeter }} 米；超出 {{ trialResult.excessDistanceMeter }} 米</el-descriptions-item>
        <el-descriptions-item label="费用明细">检测 {{ money(trialResult.inspectionFee) }} 元 + 起步 {{ money(trialResult.driverBaseFee) }} 元 + 超里程 {{ money(trialResult.distanceFee) }} 元</el-descriptions-item>
        <el-descriptions-item label="试算合计"><strong>{{ money(trialResult.totalFen) }} 元</strong></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDept } from '@/api/system/dept'
import { getDicts } from '@/api/system/dict/data'
import { listStations, type StationRecord } from '@/api/rongli/station'
import { listPrices, createPrice, updatePrice, priceAction, trialPrice, matchPrice, type PriceContent, type PriceRule, type PriceTrial, type PriceWrite } from '@/api/rongli/pricing'
const statusNames = { DRAFT: '草稿', IN_REVIEW: '待审核', APPROVED: '已批准', PUBLISHED: '已发布', DISABLED: '已停用' }
const rows = ref<PriceRule[]>([]), total = ref(0), pageNum = ref(1), loading = ref(false), saving = ref(false)
const environment = ref<'TEST' | 'LIVE'>('TEST')
const departments = ref<{ id: string; name: string }[]>([])
interface DictItem { value: string; label: string }
const vehicleTypes = ref<DictItem[]>([]), inspectionTypes = ref<DictItem[]>([]), stations = ref<StationRecord[]>([])
const filter = reactive({ ownerDeptId: undefined as string | undefined, status: undefined as string | undefined })
const formOpen = ref(false), actionOpen = ref(false), trialOpen = ref(false), trialLoading = ref(false)
const form = reactive({ ruleId: '', sourceRuleId: '', ownerDeptId: '', version: 0, name: '', vehicleTypeCode: '', inspectionTypeCode: '', inspectionFee: '', driverBaseFee: '', includedDistanceMeter: 0, distanceUnitMeter: 1000, distanceUnitFee: '', distanceRounding: 'CEIL_UNIT' as PriceContent['distanceRounding'], routeBasis: 'ONE_WAY' as PriceContent['routeBasis'], start: null as Date | null, end: null as Date | null })
let formAttempt: { body: string; key: string } | null = null
let actionAttempt: { body: string; key: string } | null = null
const actionTitles = { submit: '提交审核', audit: '确认独立审核', publish: '确认发布', disable: '确认停用' }
const actionKind = ref<keyof typeof actionTitles>('submit'), actionRow = ref<PriceRule | null>(null)
const reason = ref(''), decision = ref('APPROVE'), replacesId = ref<string>(), replacementChoices = ref<PriceRule[]>([])
const trialRow = ref<PriceRule | null>(null), trialDistance = ref(0), trialResult = ref<PriceTrial | null>(null)
const match = reactive({ stationId: '', vehicleTypeCode: '', inspectionTypeCode: '', at: null as Date | null })
function money(fen?: number | null): string {
  if (fen == null) return '—'
  const n = BigInt(fen)
  return `${n / 100n}.${String(n % 100n).padStart(2, '0')}`
}
function parseMoney(yuan: string): number {
  if (!/^(0|[1-9]\d*)(\.\d{1,2})?$/.test(yuan.trim())) throw new Error('金额须为非负数且最多两位小数')
  const [whole, decimal = ''] = yuan.trim().split('.')
  const value = BigInt(whole) * 100n + BigInt(decimal.padEnd(2, '0'))
  if (value > BigInt(Number.MAX_SAFE_INTEGER)) throw new Error('金额超出安全范围')
  return Number(value)
}
const labelFor = (items: DictItem[], value?: string) => items.find(x => x.value === value)?.label || value || '—'
const time = (value?: string | null) => value ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false }) : '未设置 / 持续有效'
async function load() {
  loading.value = true
  try { const r = await listPrices({ pageNum: pageNum.value, ...filter }); rows.value = r.rows; total.value = r.total; environment.value = r.environment }
  catch { rows.value = []; total.value = 0 }
  finally { loading.value = false }
}
function openForm(row?: PriceRule, clone = false) {
  if (row && !row.config) return
  const c = row?.config
  Object.assign(form, { ruleId: row && !clone ? row.ruleId : '', sourceRuleId: clone ? row?.ruleId : '', ownerDeptId: row?.ownerDeptId || '', version: row?.version || 0, name: c?.name || '', vehicleTypeCode: c?.vehicleTypeCode || '', inspectionTypeCode: c?.inspectionTypeCode || '', inspectionFee: c ? money(c.inspectionFee) : '', driverBaseFee: c ? money(c.driverBaseFee) : '', includedDistanceMeter: c?.includedDistanceMeter || 0, distanceUnitMeter: c?.distanceUnitMeter || 1000, distanceUnitFee: c ? money(c.distanceUnitFee) : '', distanceRounding: c?.distanceRounding || 'CEIL_UNIT', routeBasis: c?.routeBasis || 'ONE_WAY', start: c?.effectiveFrom ? new Date(c.effectiveFrom) : null, end: c?.effectiveTo ? new Date(c.effectiveTo) : null })
  formAttempt = null; formOpen.value = true
}
async function saveForm() {
  if (!form.ownerDeptId || !form.name.trim() || !form.vehicleTypeCode || !form.inspectionTypeCode) { ElMessage.warning('请选择公司、车型、检测类型并填写方案名称'); return }
  saving.value = true
  try {
    const body: PriceWrite = { ownerDeptId: form.ownerDeptId, environment: environment.value, name: form.name.trim(), vehicleTypeCode: form.vehicleTypeCode, inspectionTypeCode: form.inspectionTypeCode, inspectionFee: parseMoney(form.inspectionFee), driverBaseFee: parseMoney(form.driverBaseFee), includedDistanceMeter: form.includedDistanceMeter, distanceUnitMeter: form.distanceUnitMeter, distanceUnitFee: parseMoney(form.distanceUnitFee), distanceRounding: form.distanceRounding, routeBasis: form.routeBasis, waitingFeeEnabled: false, waitingFreeSeconds: 0, waitingUnitSeconds: 0, waitingUnitFee: 0, effectiveFrom: form.start?.toISOString(), effectiveTo: form.end?.toISOString(), ...(form.ruleId ? { expectedVersion: form.version } : form.sourceRuleId ? { sourceRuleId: form.sourceRuleId } : {}) }
    const intent = JSON.stringify({ id: form.ruleId, body })
    if (!formAttempt || formAttempt.body !== intent) formAttempt = { body: intent, key: crypto.randomUUID() }
    if (form.ruleId) await updatePrice(form.ruleId, body, formAttempt.key)
    else await createPrice(body, formAttempt.key)
    formOpen.value = false; formAttempt = null; ElMessage.success('草稿已保存，尚未生效'); await load()
  } catch (error: any) { if (!error?.errorCode) ElMessage.warning(error.message || '保存失败') }
  finally { saving.value = false }
}
async function openAction(row: PriceRule, action: keyof typeof actionTitles) {
  actionRow.value = row; actionKind.value = action; reason.value = ''; decision.value = 'APPROVE'; replacesId.value = undefined; replacementChoices.value = []; actionAttempt = null
  try {
    if (action === 'publish' && row.config) {
      const r = await listPrices({ ownerDeptId: row.ownerDeptId, status: 'PUBLISHED', vehicleTypeCode: row.config.vehicleTypeCode, inspectionTypeCode: row.config.inspectionTypeCode, pageSize: 100 })
      replacementChoices.value = r.rows
    }
  } catch { return }
  actionOpen.value = true
}
async function saveAction() {
  const row = actionRow.value
  if (!row || !reason.value.trim()) { ElMessage.warning('请填写原因或审核意见'); return }
  saving.value = true
  try {
    const old = replacementChoices.value.find(x => x.ruleId === replacesId.value)
    const body = { expectedVersion: row.version, ...(actionKind.value === 'audit' ? { decision: decision.value, opinion: reason.value.trim() } : { reason: reason.value.trim() }), ...(actionKind.value === 'publish' && old ? { replacesRuleId: old.ruleId, replacesExpectedVersion: old.version } : {}) }
    const intent = JSON.stringify({ id: row.ruleId, action: actionKind.value, body })
    if (!actionAttempt || actionAttempt.body !== intent) actionAttempt = { body: intent, key: crypto.randomUUID() }
    await priceAction(row.ruleId, actionKind.value, body, actionAttempt.key)
    actionOpen.value = false; actionAttempt = null; ElMessage.success('操作已完成'); await load()
  } catch { /* 请求层已提示错误，保留原意图和幂等键供重试。 */ }
  finally { saving.value = false }
}
function openTrial(row: PriceRule) { trialRow.value = row; trialDistance.value = 0; trialResult.value = null; trialOpen.value = true }
async function openMatch() {
  trialRow.value = null; trialResult.value = null; trialDistance.value = 0
  try { stations.value = (await listStations(1, 100)).rows; trialOpen.value = true } catch { /* 统一请求层已展示错误。 */ }
}
async function runTrial() {
  if (!trialRow.value && (!match.stationId || !match.vehicleTypeCode || !match.inspectionTypeCode)) { ElMessage.warning('请选择站点、车型和检测类型'); return }
  trialLoading.value = true; trialResult.value = null
  try {
    trialResult.value = trialRow.value ? (await trialPrice(trialRow.value.ruleId, trialDistance.value)).data : (await matchPrice({ stationId: match.stationId, vehicleTypeCode: match.vehicleTypeCode, inspectionTypeCode: match.inspectionTypeCode, distanceMeter: trialDistance.value, serviceAt: match.at?.toISOString() })).data
  } catch { /* 无匹配或冲突时不保留旧试算金额。 */ }
  finally { trialLoading.value = false }
}
onMounted(async () => {
  await load()
  const data = await Promise.allSettled([listDept(), getDicts('vi_vehicle_type'), getDicts('vi_inspection_type')])
  if (data[0].status === 'fulfilled') departments.value = (data[0].value.data || []).filter(x => x.status === '0' && x.deptId != null && x.deptName).map(x => ({ id: String(x.deptId), name: x.deptName! }))
  else ElMessage.warning('无法加载公司部门目录，请核实部门查询权限')
  if (data[1].status === 'fulfilled') vehicleTypes.value = (data[1].value.data || []).filter(x => x.dictValue && x.dictLabel).map(x => ({ value: x.dictValue!, label: x.dictLabel! }))
  if (data[2].status === 'fulfilled') inspectionTypes.value = (data[2].value.data || []).filter(x => x.dictValue && x.dictLabel).map(x => ({ value: x.dictValue!, label: x.dictLabel! }))
})
</script>
<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin: 18px 0; }
.toolbar .el-select { width: 190px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.el-select, .el-date-editor, .el-input-number { max-width: 100%; }
.el-pagination, .trial-result { margin-top: 18px; }
.el-alert + .el-form, .el-descriptions + .el-alert { margin-top: 16px; }
@media (max-width: 600px) { .form-grid { grid-template-columns: minmax(0, 1fr); } }
</style>
