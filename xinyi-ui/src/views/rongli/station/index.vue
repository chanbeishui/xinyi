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
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rl-ops">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="openCapacity(row)">时段容量</el-button>
              <el-button v-if="row.enabled" link type="danger" @click="openDisable(row)">停用预览</el-button>
            </div>
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
        <el-form-item label="归属部门" :required="deptOptions.length > 0">
          <el-tree-select
            v-if="deptOptions.length"
            v-model="form.deptId"
            :data="deptOptions"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择归属部门"
            style="width: 100%"
            :disabled="!!form.id"
          />
          <p class="rl-hint">
            {{
              deptOptions.length
                ? '检测点创建后不再变更归属部门，避免历史订单的数据范围漂移。'
                : '当前账号没有部门列表权限，保存时将按你的任职部门归属。'
            }}
          </p>
        </el-form-item>
        <el-form-item label="地址" required>
          <div class="st-region">
            <el-select
              v-model="form.province"
              filterable
              clearable
              placeholder="省"
              :disabled="!regionReady"
              @change="onProvinceChange"
            >
              <el-option v-for="p in provinceOptions" :key="p" :label="p" :value="p" />
            </el-select>
            <el-select
              v-model="form.city"
              filterable
              clearable
              placeholder="市"
              :disabled="!regionReady || !form.province"
              @change="onCityChange"
            >
              <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
            </el-select>
            <el-select
              v-model="form.district"
              filterable
              clearable
              placeholder="区 / 县"
              :disabled="!regionReady || !form.city"
            >
              <el-option v-for="d in districtOptions" :key="d" :label="d" :value="d" />
            </el-select>
          </div>
          <div class="st-address">
            <el-input
              v-model="form.detail"
              placeholder="详细地址，只填街道 / 门牌，如：站前路1号"
              clearable
              @keyup.enter="searchAddress"
            />
            <el-button type="primary" plain icon="Search" :loading="locating" @click="searchAddress">
              搜索
            </el-button>
          </div>
          <p v-if="fullAddress && (form.province || form.city || form.district)" class="st-preview">
            <span>保存地址</span>
            <b>{{ fullAddress }}</b>
          </p>
          <p v-if="regionError" class="rl-hint rl-hint--warn">{{ regionError }}</p>
          <p v-else class="rl-hint">
            省 / 市 / 区用下拉选择，详细地址只填街道门牌即可；省市区重复写一遍会自动去掉。
          </p>
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
          <MapPicker
            ref="mapRef"
            v-model:lng="form.lng"
            v-model:lat="form.lat"
            :radius="form.serviceRadiusMeter"
            @update:address="onMapAddress"
          />
          <div class="st-coords">
            <el-input v-model="form.lng" placeholder="经度，如 110.925" />
            <el-input v-model="form.lat" placeholder="纬度，如 21.663" />
          </div>
          <p class="rl-hint">虚线圆圈即服务半径覆盖范围，改下方「服务半径」会实时放大缩小。</p>
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
        <div class="st-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存资料</el-button>
        </div>
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
        <div class="st-footer">
          <el-button @click="disableVisible = false">取消</el-button>
          <el-button type="danger" :disabled="!disableConfirmed" :loading="saving" @click="confirmDisable">
            暂停新预约
          </el-button>
        </div>
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
import MapPicker from '@/components/MapPicker/index.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createStation, listCapacity, listStations, updateCapacity, updateStation } from '@/api/rongli'
import { deptTreeSelect } from '@/api/system/user'
import { joinAddress, loadRegionTree, splitAddress, type Region } from '@/utils/region'

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
const mapRef = ref<any>(null)
const locating = ref(false)
const deptOptions = ref<any[]>([])
const form = reactive<any>({
  id: '',
  name: '',
  deptId: null,
  province: '',
  city: '',
  district: '',
  detail: '',
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

/* ---------------------------------------------------------------- 省市区 */
/**
 * 行政区划取自高德（首次约 1 次请求，之后读 localStorage 缓存 30 天）。
 * 取不到时不阻塞录入：下拉置灰，详细地址仍可手写完整地址。
 */
const regions = ref<Region[]>([])
const regionReady = ref(false)
const regionLoading = ref(false)
const regionError = ref('')
/** 打开抽屉的序号：编辑时地址要等行政区划加载完再拆，期间切了记录就放弃回填 */
let editSeq = 0

const provinceOptions = computed(() => regions.value.map((item) => item.name))
const currentProvince = computed(() => regions.value.find((item) => item.name === form.province))
const cityOptions = computed(() => (currentProvince.value?.children || []).map((item) => item.name))
const currentCity = computed(() =>
  (currentProvince.value?.children || []).find((item) => item.name === form.city)
)
const districtOptions = computed(() => (currentCity.value?.children || []).map((item) => item.name))

/** 最终保存/搜索用的完整地址：省市区 + 详细地址，重复的省市区自动去掉 */
const fullAddress = computed(() => joinAddress(form.province, form.city, form.district, form.detail))

async function ensureRegions() {
  if (regionReady.value || regionLoading.value) return
  regionLoading.value = true
  regionError.value = ''
  try {
    regions.value = await loadRegionTree()
    regionReady.value = true
  } catch (e: any) {
    regionError.value = `省 / 市 / 区下拉暂不可用：${e?.message || '行政区划加载失败'}。可直接在详细地址里填写完整地址。`
  } finally {
    regionLoading.value = false
  }
}

function onProvinceChange() {
  form.city = ''
  form.district = ''
}

function onCityChange() {
  form.district = ''
}

function hasAddressInput() {
  return !!(form.province || form.city || form.district || String(form.detail || '').trim())
}

function resetForm() {
  Object.assign(form, {
    id: '',
    name: '',
    deptId: null,
    province: '',
    city: '',
    district: '',
    detail: '',
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
}

/**
 * 地图选点后的逆地理编码结果。
 *
 * 只在地址还没填时回填：站点对外展示的地址通常是运营整理过的写法（"xx路1号"），
 * 直接覆盖成高德返回的长串地址反而不利于车主阅读。现在能拆出省市区就顺手把下拉填上。
 */
function onMapAddress(value: string) {
  if (!value) return
  if (hasAddressInput()) return
  const parts = regions.value.length ? splitAddress(value, regions.value) : null
  if (parts && parts.province) {
    Object.assign(form, parts)
  } else {
    form.detail = value
  }
}

/**
 * 按地址搜索定位。
 *
 * 只把拼好的地址交给地图组件解析，不反向改写下拉和输入框：运营录入的地址是给车主看的
 * 规范写法，被逆地理编码的长串地址覆盖反而更难读。
 */
async function searchAddress() {
  const keyword = fullAddress.value.trim()
  if (!keyword) {
    ElMessage.warning('请先选择省市区或填写详细地址，再点击「搜索」')
    return
  }
  const picker = mapRef.value
  if (!picker || typeof picker.locate !== 'function') {
    ElMessage.warning('地图尚未加载完成，请稍候重试')
    return
  }
  locating.value = true
  try {
    const res = await picker.locate(keyword)
    if (!res || !res.ok) {
      ElMessage.warning(res?.message || '未能定位到该地址，请补充省 / 市 / 区后重试')
      return
    }
    ElMessage.success(res.message || '已在地图上定位')
  } catch (e: any) {
    ElMessage.error(e?.message || '地址搜索失败，请稍后重试')
  } finally {
    locating.value = false
  }
}

const disableVisible = ref(false)
const disableReason = ref('')
const disableConfirmed = ref(false)
const current = ref<any>({})

const capacityVisible = ref(false)
const slots = ref<any[]>([])

/**
 * 归属部门候选。接口要求 system:user:list 权限，检测点管理员不一定有；
 * 取不到时把选择器藏起来，由后端按任职部门兜底，不阻塞创建。
 */
async function loadDeptOptions() {
  try {
    const res: any = await deptTreeSelect()
    deptOptions.value = res?.data || []
  } catch {
    deptOptions.value = []
  }
}

onMounted(() => {
  load()
  loadDeptOptions()
})

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
  editSeq++
  resetForm()
  formVisible.value = true
  // 行政区划按需加载：不进抽屉就不拉这一份数据
  ensureRegions()
}

async function openEdit(row: any) {
  const seq = ++editSeq
  const hours = parseJson(row.business_hours, {}) || {}
  resetForm()
  Object.assign(form, {
    id: String(row.id),
    name: row.name,
    deptId: row.dept_id ? Number(row.dept_id) : null,
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

  // 已有地址是整串文本，尽量拆回省/市/区回填下拉；拆不出来就整串留在详细地址里，信息不丢
  const address = String(row.address || '').trim()
  await ensureRegions()
  if (seq !== editSeq) return
  const parts = regions.value.length
    ? splitAddress(address, regions.value)
    : { province: '', city: '', district: '', detail: address }
  Object.assign(form, parts)
}

async function submit() {
  const address = fullAddress.value.trim()
  if (!form.name || !address) {
    ElMessage.warning('请填写检测点名称与地址')
    return
  }
  if (!String(form.detail || '').trim() && !(form.province && form.city && form.district)) {
    ElMessage.warning('请选择到区县，或填写详细地址')
    return
  }
  if (!form.id && deptOptions.value.length && !form.deptId) {
    ElMessage.warning('请选择归属部门')
    return
  }
  const payload: any = {
    name: form.name,
    address,
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
  // 归属部门只在创建时提交：vi_station.dept_id 是 NOT NULL，后端创建时必须拿到它
  if (!form.id) {
    payload.deptId = form.deptId
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

/* 省 / 市 / 区：三等分一行，下拉可搜索 */
.st-region {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  width: 100%;
}

.st-region .el-select {
  width: 100%;
}

/* 地址行：输入框吃掉剩余宽度，搜索按钮固定宽度，两者同高 */
.st-address {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 8px;
}

.st-address .el-input {
  flex: 1 1 auto;
  min-width: 0;
}

.st-address :deep(.el-button) {
  flex: 0 0 auto;
  height: 32px;
  padding: 0 16px;
  font-weight: 500;
  box-shadow: none;
}

.st-address :deep(.el-button:hover) {
  box-shadow: none;
  transform: none;
}

/* 拼接后的地址预览：让「去重」结果可见，避免保存了才发现重复 */
.st-preview {
  display: flex;
  align-items: baseline;
  gap: 8px;
  width: 100%;
  margin: 10px 0 0;
  font-size: 12px;
  line-height: 18px;
  color: #667085;
}

.st-preview b {
  flex: 1 1 auto;
  min-width: 0;
  font-weight: 500;
  color: #101828;
  word-break: break-all;
}

/* 经纬度手填：两列等宽，避免一长一短 */
.st-coords {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  width: 100%;
  margin-top: 10px;
}

/* 抽屉底部操作区：间距统一，取消与主操作同高同宽 */
.st-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.st-footer :deep(.el-button) {
  height: 32px;
  min-width: 88px;
  margin-left: 0;
  font-weight: 500;
}

.st-footer :deep(.el-button--primary) {
  box-shadow: 0 1px 2px rgba(23, 92, 211, 0.18);
}

.st-footer :deep(.el-button--primary:hover) {
  box-shadow: 0 2px 8px rgba(23, 92, 211, 0.22);
  transform: none;
}
</style>
