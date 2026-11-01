<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="检测点公开资料、资质、营业时间、支持车型与服务范围。联系电话对外仅脱敏；版本必填 CAS 更新；停用前确认对在手订单的影响；迁部门存在在手订单将被受控拒绝。" />

    <div class="toolbar mb12">
      <el-button type="primary" :disabled="loading" @click="openCreate">新增检测点</el-button>
      <el-button :disabled="loading" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows">
      <el-table-column label="名称" prop="name" min-width="160" />
      <el-table-column label="地址" prop="address" min-width="200" />
      <el-table-column label="电话" prop="phoneMask" width="130" />
      <el-table-column label="支持车型" min-width="160">
        <template #default="{ row }">{{ (row.supportedVehicleTypeCodes || []).join('、') }}</template>
      </el-table-column>
      <el-table-column label="营业时间" prop="businessHours" min-width="160" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.enabled ? 'warning' : 'success'" @click="toggleEnabled(row)">
            {{ row.enabled ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="mt12" layout="total, prev, pager, next" :total="total"
      :page-size="pageSize" :current-page="pageNum" @current-change="onPage" />

    <el-drawer v-model="drawer" :title="form && form.stationId ? '编辑检测点' : '新增检测点'" size="min(520px, 100vw)">
      <el-form v-if="form" label-width="100px">
        <el-form-item label="归属部门" required>
          <el-select v-model="form.deptId" filterable placeholder="请选择站点归属部门" style="width: 100%">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址" required><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="经度(GCJ02)" required><el-input v-model.number="form.lng" /></el-form-item>
        <el-form-item label="纬度(GCJ02)" required><el-input v-model.number="form.lat" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="留空则不修改" /></el-form-item>
        <el-form-item label="支持车型" required>
          <el-input v-model="form.supportedCodes" placeholder="逗号分隔，如 SMALL_CAR,SUV" />
        </el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
        <el-form-item label="服务半径(m)"><el-input v-model.number="form.serviceRadiusMeter" /></el-form-item>
        <el-form-item label="资质文件ID">
          <el-input v-model="form.qualificationFileIds" placeholder="逗号分隔的不透明ID" />
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listStations, createStation, updateStation, type StationRecord, type StationWrite } from '@/api/rongli/station'
import { isVersionConflict } from '@/api/rongli/viError'
import { listDept } from '@/api/system/dept'

interface FormModel {
  stationId: string
  deptId: string
  name: string
  address: string
  lng: number
  lat: number
  contactPhone: string
  supportedCodes: string
  businessHours: string
  serviceRadiusMeter: number
  qualificationFileIds: string
  enabled: boolean
  version: number
}

const departments = ref<{ id: string; name: string }[]>([])
let saveAttempt: { body: string; key: string } | null = null
const loading = ref(false)
const saving = ref(false)
const rows = ref<StationRecord[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const drawer = ref(false)
const form = ref<FormModel | null>(null)

function uuid(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16)
  })
}

async function load() {
  loading.value = true
  try {
    const res = await listStations(pageNum.value, pageSize.value)
    rows.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPage(p: number) { pageNum.value = p; load() }

function openCreate() {
  saveAttempt = null
  form.value = { stationId: '', deptId: '', name: '', address: '', lng: 120.0, lat: 30.0, contactPhone: '', supportedCodes: 'SMALL_CAR', businessHours: '', serviceRadiusMeter: 5000, qualificationFileIds: '', enabled: false, version: 0 }
  drawer.value = true
}

function openEdit(row: StationRecord) {
  saveAttempt = null
  form.value = {
    deptId: row.deptId, stationId: row.stationId, name: row.name, address: row.address,
    lng: row.location?.lng ?? 0, lat: row.location?.lat ?? 0, contactPhone: '',
    supportedCodes: (row.supportedVehicleTypeCodes || []).join(','), businessHours: row.businessHours || '',
    serviceRadiusMeter: row.serviceRadiusMeter ?? 0, qualificationFileIds: '', enabled: row.enabled, version: row.version
  }
  drawer.value = true
}

function buildWrite(): StationWrite {
  const f = form.value!
  return {
    deptId: f.deptId, name: f.name.trim(), address: f.address.trim(),
    location: { lng: Number(f.lng), lat: Number(f.lat), coordinateSystem: 'GCJ02' },
    contactPhone: f.contactPhone.trim() || undefined,
    enabled: f.enabled,
    supportedVehicleTypeCodes: f.supportedCodes.split(',').map(s => s.trim()).filter(Boolean),
    businessHours: f.businessHours || undefined,
    serviceRadiusMeter: f.serviceRadiusMeter,
    qualificationFileIds: f.qualificationFileIds ? f.qualificationFileIds.split(',').map(s => s.trim()).filter(Boolean) : undefined,
    expectedVersion: f.stationId ? f.version : undefined
  }
}

async function save() {
  if (!form.value) return
  const f = form.value
  if (!f.deptId) { ElMessage.warning('请选择站点归属部门'); return }
  if (!f.name.trim() || !f.address.trim()) { ElMessage.warning('名称与地址必填'); return }
  saving.value = true
  try {
    const input = buildWrite()
    const body = JSON.stringify(input)
    if (!saveAttempt || saveAttempt.body !== body) saveAttempt = { body, key: uuid() }
    if (f.stationId) {
      await updateStation(f.stationId, input, saveAttempt.key)
    } else {
      await createStation(input, saveAttempt.key)
    }
    saveAttempt = null
    ElMessage.success('保存成功')
    drawer.value = false
    await load()
  } catch (e: any) {
    if (isVersionConflict(e)) await load()
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row: StationRecord) {
  if (row.enabled) {
    // B17-disable：停用影响确认
    await ElMessageBox.confirm(
      `停用「${row.name}」后该站点将不可被新预约选择；在手订单不受影响但需人工跟进。确认停用？`,
      '停用检测点影响确认', { confirmButtonText: '确认停用', cancelButtonText: '取消', type: 'warning' }
    )
  }
  const write: StationWrite = {
    name: row.name, address: row.address,
    location: { lng: row.location.lng, lat: row.location.lat, coordinateSystem: 'GCJ02' },
    supportedVehicleTypeCodes: row.supportedVehicleTypeCodes,
    businessHours: row.businessHours || undefined,
    serviceRadiusMeter: row.serviceRadiusMeter,
    enabled: !row.enabled, expectedVersion: row.version
  }
  try {
    await updateStation(row.stationId, write, uuid())
    ElMessage.success(row.enabled ? '已停用' : '已启用')
    await load()
  } catch (e: any) {
    if (isVersionConflict(e)) await load()
  }
}

onMounted(async () => {
  await load()
  try {
    const res = await listDept()
    departments.value = (res.data || []).filter(dept => dept.status === '0' && dept.deptId != null && dept.deptName)
      .map(dept => ({ id: String(dept.deptId), name: dept.deptName! }))
  } catch {
    ElMessage.warning('无法加载部门目录，请联系管理员授予部门查询权限')
  }
})
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.mt12 { margin-top: 12px; }
.toolbar { display: flex; gap: 8px; }
</style>
