<template>
  <RongliPage title="检测点管理" subtitle="维护公开资料、服务范围、支持车型和营业配置。">
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" icon="Plus" @click="openCreate">维护检测点</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>当前获授权检测点</h3>
        <span class="rl-muted">共 {{ total }} 个</span>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="检测点" min-width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.name }}</div>
            <div class="rl-mono">STATION-{{ row.id }}</div>
          </template>
        </el-table-column>
        <el-table-column label="地址 / 营业时间" min-width="280">
          <template #default="{ row }">
            <div>{{ row.address || '—' }}</div>
            <div class="rl-sub">{{ hoursLabel(row) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="服务车型" width="170">
          <template #default="{ row }">{{ typeLabel(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.enabled ? 'rl-tag--green' : 'rl-tag--gray'">
              {{ row.enabled ? '启用' : '暂停新预约' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="承诺名额" width="120">
          <template #default="{ row }">
            <span>{{ quotaText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openCapacity(row)">时段容量</el-button>
            <el-button v-if="row.enabled" link type="danger" @click="openDisable(row)">停用预览</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有检测点</div>
            <div class="rl-empty__hint">先维护检测点公开资料，再配置服务范围与容量。</div>
            <el-button type="primary" @click="openCreate">维护检测点</el-button>
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
    </section>

    <!-- 编辑检测点资料 -->
    <el-drawer v-model="formVisible" :title="form.id ? '编辑检测点资料' : '维护检测点'" size="560px">
      <el-form label-position="top">
        <el-form-item label="检测点名称" required>
          <el-input v-model="form.name" placeholder="请输入检测点名称" />
        </el-form-item>
        <el-form-item label="地址" required>
          <el-input v-model="form.address" placeholder="请输入公开地址" />
        </el-form-item>
        <el-form-item label="营业时间" required>
          <div class="rl-hours">
            <el-select v-model="form.weekdays" multiple collapse-tags placeholder="营业日" style="width: 100%">
              <el-option v-for="d in WEEK" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
            <div class="rl-hours__range">
              <el-input-number v-model="form.startHour" :min="0" :max="23" controls-position="right" />
              <span class="rl-muted">至</span>
              <el-input-number v-model="form.endHour" :min="1" :max="24" controls-position="right" />
              <span class="rl-muted">点</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="服务区域">
          <el-input v-model="form.area" type="textarea" :rows="2" placeholder="描述已开放服务区域，仅用于展示" />
        </el-form-item>
        <el-form-item label="经纬度（用于派单半径判定）">
          <div class="rl-hours__range">
            <el-input v-model="form.lng" placeholder="经度 110.925" />
            <el-input v-model="form.lat" placeholder="纬度 21.663" />
          </div>
        </el-form-item>
        <el-form-item label="服务半径（米）">
          <el-input-number v-model="form.serviceRadiusMeter" :min="500" :step="500" />
        </el-form-item>
        <el-form-item label="支持的车型">
          <el-checkbox-group v-model="form.types">
            <el-checkbox v-for="t in vehicleType" :key="t.value" :value="t.value">{{ t.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <p class="rl-hint">车型字典新增后，仍需配置站点服务支持与价格。</p>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存资料</el-button>
      </template>
    </el-drawer>

    <!-- 停用预览 -->
    <el-drawer v-model="disableVisible" title="暂停检测点新预约前确认" size="520px">
      <p class="rl-hint rl-hint--warn">
        停止接收新预约，不删除已付款订单，不解除在手车辆保管。
      </p>
      <div class="rl-kv mt12">
        <div><span>检测点</span><b>{{ current.name }}</b></div>
        <div><span>在站车辆</span><b>{{ current.activeOrderCount ?? 0 }} 辆</b></div>
        <div><span>未来 7 天承诺名额</span><b>{{ quotaText(current) }}</b></div>
        <div><span>仍需保留能力</span><b>交接、结果、取消返还与必要退款</b></div>
      </div>
      <el-form label-position="top" class="mt12">
        <el-form-item label="停约原因" required>
          <el-input v-model="disableReason" type="textarea" :rows="3" placeholder="说明停约原因与恢复计划" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="disableConfirmed">已安排人员处理在手业务</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disableVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!disableConfirmed" :loading="saving" @click="confirmDisable">
          暂停新预约
        </el-button>
      </template>
    </el-drawer>

    <!-- 时段容量 -->
    <el-dialog v-model="capacityVisible" title="时段容量（未来 7 天）" width="720px">
      <el-table :data="slots" :border="false" max-height="420">
        <el-table-column label="时段" prop="label" min-width="170" />
        <el-table-column label="总容量" width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.totalQuota"
              :min="0"
              size="small"
              @change="(v: any) => changeCapacity(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="已占用" prop="usedQuota" width="90" align="center" />
        <el-table-column label="启用" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="(v: any) => changeEnabled(row, v)" />
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">暂无时段</div>
            <div class="rl-empty__hint">容量按站点接收时段生成，打开该检测点后自动补齐。</div>
          </div>
        </template>
      </el-table>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliStation" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createStation, listCapacity, listStations, updateCapacity, updateStation } from '@/api/rongli'

const { vehicle_type } = useDict('vehicle_type')
const vehicleType = vehicle_type as any

const WEEK = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 10 })
const capacityMap = reactive<Record<string, any>>({})

const formVisible = ref(false)
const form = reactive<any>({
  id: '',
  name: '',
  address: '',
  area: '',
  lng: '',
  lat: '',
  weekdays: [1, 2, 3, 4, 5, 6],
  startHour: 8,
  endHour: 18,
  serviceRadiusMeter: 8000,
  types: ['sedan'],
  enabled: true
})

const disableVisible = ref(false)
const disableReason = ref('')
const disableConfirmed = ref(false)
const current = ref<any>({})

const capacityVisible = ref(false)
const slots = ref<any[]>([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listStations(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
    rows.value.forEach(loadQuota)
  } finally {
    loading.value = false
  }
}

async function loadQuota(row: any) {
  try {
    const res: any = await listCapacity(String(row.id))
    const list = (res && res.slots) || []
    capacityMap[String(row.id)] = {
      total: list.reduce((s: number, x: any) => s + Number(x.totalQuota || 0), 0),
      used: list.reduce((s: number, x: any) => s + Number(x.usedQuota || 0), 0)
    }
  } catch {
    capacityMap[String(row.id)] = null
  }
}

function quotaText(row: any) {
  const q = capacityMap[String(row.id)]
  if (!q) return '—'
  return `${q.used} / ${q.total}`
}

function typeLabel(row: any) {
  const codes = parseJson(row.supported_vehicle_codes, [])
  if (!codes.length) return '—'
  return codes
    .map((c: string) => (vehicleType.value || []).find((t: any) => t.value === c)?.label || c)
    .join('、')
}

function hoursLabel(row: any) {
  const h = parseJson(row.business_hours, null)
  if (!h) return '—'
  const days = (h.weekdays || []).map((d: number) => WEEK.find((w) => w.value === Number(d))?.label).filter(Boolean)
  const range = `${String(h.startHour ?? 8).padStart(2, '0')}:00 - ${String(h.endHour ?? 18).padStart(2, '0')}:00`
  return days.length === 7 ? `每日 ${range}` : `${days.join('、') || '未设置'} ${range}`
}

function parseJson(raw: any, fallback: any) {
  if (!raw) return fallback
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return fallback
  }
}

function openCreate() {
  Object.assign(form, {
    id: '',
    name: '',
    address: '',
    area: '',
    lng: '',
    lat: '',
    weekdays: [1, 2, 3, 4, 5, 6],
    startHour: 8,
    endHour: 18,
    serviceRadiusMeter: 8000,
    types: ['sedan'],
    enabled: true
  })
  formVisible.value = true
}

function openEdit(row: any) {
  const hours = parseJson(row.business_hours, {}) || {}
  Object.assign(form, {
    id: String(row.id),
    name: row.name,
    address: row.address,
    area: row.area || '',
    lng: String(row.lng ?? ''),
    lat: String(row.lat ?? ''),
    weekdays: (hours.weekdays || [1, 2, 3, 4, 5, 6]).map(Number),
    startHour: Number(hours.startHour ?? 8),
    endHour: Number(hours.endHour ?? 18),
    serviceRadiusMeter: Number(row.service_radius_meter || 8000),
    types: parseJson(row.supported_vehicle_codes, []),
    enabled: !!row.enabled
  })
  formVisible.value = true
}

async function submit() {
  if (!form.name || !form.address) {
    ElMessage.warning('请填写检测点名称与地址')
    return
  }
  const payload = {
    name: form.name,
    address: form.address,
    lng: Number(form.lng || 0),
    lat: Number(form.lat || 0),
    serviceRadiusMeter: form.serviceRadiusMeter,
    supportedVehicleCodes: JSON.stringify(form.types || []),
    businessHours: JSON.stringify({
      weekdays: form.weekdays,
      startHour: form.startHour,
      endHour: form.endHour
    }),
    enabled: form.enabled
  }
  saving.value = true
  try {
    if (form.id) {
      await updateStation(form.id, payload)
    } else {
      await createStation(payload)
    }
    ElMessage.success('已保存')
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openDisable(row: any) {
  current.value = row
  disableReason.value = ''
  disableConfirmed.value = false
  disableVisible.value = true
}

async function confirmDisable() {
  if (!disableReason.value.trim()) {
    ElMessage.warning('请填写停约原因')
    return
  }
  saving.value = true
  try {
    await updateStation(String(current.value.id), {
      name: current.value.name,
      address: current.value.address,
      lng: Number(current.value.lng || 0),
      lat: Number(current.value.lat || 0),
      serviceRadiusMeter: Number(current.value.service_radius_meter || 8000),
      supportedVehicleCodes: current.value.supported_vehicle_codes,
      businessHours: current.value.business_hours,
      enabled: false
    })
    ElMessage.success('已暂停新预约')
    disableVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function openCapacity(row: any) {
  const res: any = await listCapacity(String(row.id))
  slots.value = (res && res.slots) || []
  capacityVisible.value = true
}

async function changeCapacity(row: any, value: any) {
  await updateCapacity(row.slotId, { totalQuota: Number(value), enabled: row.enabled })
  ElMessage.success('容量已更新')
}

async function changeEnabled(row: any, value: any) {
  await updateCapacity(row.slotId, { totalQuota: row.totalQuota, enabled: !!value })
  ElMessage.success('已更新')
}
</script>

<style scoped>
.rl-hours {
  width: 100%;
}

.rl-hours__range {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
</style>
