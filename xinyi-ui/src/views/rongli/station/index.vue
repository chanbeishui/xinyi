<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :span="18">
        <el-button type="primary" icon="Plus" @click="openCreate">新增检测点</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="检测点" prop="name" min-width="180" />
      <el-table-column label="地址" prop="address" min-width="220" show-overflow-tooltip />
      <el-table-column label="服务半径(米)" prop="service_radius_meter" width="130" align="center" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="openCapacity(row)">时段容量</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="load"
    />

    <el-dialog v-model="formVisible" :title="form.id ? '编辑检测点' : '新增检测点'" width="560px">
      <el-form label-width="120px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="经度"><el-input v-model="form.lng" placeholder="例如 114.0579" /></el-form-item>
        <el-form-item label="纬度"><el-input v-model="form.lat" placeholder="例如 22.5431" /></el-form-item>
        <el-form-item label="服务半径(米)"><el-input-number v-model="form.serviceRadiusMeter" :min="500" :step="500" /></el-form-item>
        <el-form-item label="支持车型">
          <el-input v-model="form.supportedVehicleCodes" placeholder='JSON 数组，例如 ["SMALL_CAR"]' />
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="capacityVisible" title="时段容量" width="760px">
      <el-table :data="slots" border max-height="420">
        <el-table-column label="时段" prop="label" min-width="160" />
        <el-table-column label="总容量" width="100" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.totalQuota" :min="0" size="small" @change="(v:any) => changeCapacity(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="已占用" prop="usedQuota" width="100" align="center" />
        <el-table-column label="启用" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="(v:any) => changeEnabled(row, v)" />
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="RongliStation" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createStation, listCapacity, listStations, updateCapacity, updateStation } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 10 })

const formVisible = ref(false)
const form = reactive<any>({
  id: '',
  name: '',
  address: '',
  lng: '',
  lat: '',
  serviceRadiusMeter: 8000,
  supportedVehicleCodes: '["SMALL_CAR"]',
  enabled: true
})

const capacityVisible = ref(false)
const slots = ref<any[]>([])
const currentStationId = ref('')

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listStations(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    id: '',
    name: '',
    address: '',
    lng: '',
    lat: '',
    serviceRadiusMeter: 8000,
    supportedVehicleCodes: '["SMALL_CAR"]',
    enabled: true
  })
  formVisible.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    id: String(row.id || row.station_id),
    name: row.name,
    address: row.address,
    lng: row.lng,
    lat: row.lat,
    serviceRadiusMeter: row.service_radius_meter || 8000,
    supportedVehicleCodes: row.supported_vehicle_codes || '["SMALL_CAR"]',
    enabled: !!row.enabled
  })
  formVisible.value = true
}

async function submit() {
  const payload = {
    name: form.name,
    address: form.address,
    lng: Number(form.lng),
    lat: Number(form.lat),
    serviceRadiusMeter: form.serviceRadiusMeter,
    supportedVehicleCodes: form.supportedVehicleCodes,
    businessHours: '[]',
    enabled: form.enabled
  }
  if (form.id) {
    await updateStation(form.id, payload)
  } else {
    await createStation(payload)
  }
  ElMessage.success('已保存')
  formVisible.value = false
  load()
}

async function openCapacity(row: any) {
  currentStationId.value = String(row.id || row.station_id)
  const res: any = await listCapacity(currentStationId.value)
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
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
