<template>
  <RongliPage title="行驶证登记与复核" subtitle="低置信度字段必须人工核对；完整敏感原图须单独授权。">
    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <el-select v-model="query.stationId" placeholder="全部检测点" clearable filterable class="w100">
          <el-option v-for="s in stations" :key="s.id || s.station_id" :label="s.name" :value="s.id || s.station_id" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-input v-model="query.keyword" placeholder="订单号 / 车牌 / 司机" clearable @keyup.enter="search" />
      </el-col>
      <el-col :span="12" class="right">
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="订单号" prop="orderNo" min-width="180" />
      <el-table-column label="车牌" prop="plateNo" width="110" />
      <el-table-column label="车型" prop="vehicleTypeName" width="120" />
      <el-table-column label="检测点" prop="stationName" min-width="150" show-overflow-tooltip />
      <el-table-column label="司机" width="110">
        <template #default="{ row }">{{ row.driverName || '—' }}</template>
      </el-table-column>
      <el-table-column label="履约状态" width="120">
        <template #default="{ row }">{{ row.fulfillmentStatusLabel }}</template>
      </el-table-column>
      <el-table-column label="行驶证" width="110">
        <template #default="{ row }">
          <el-tag :type="row.licenseStatus === 'PENDING' ? 'warning' : row.licenseStatus === 'VERIFIED' ? 'success' : 'info'">
            {{ row.licenseStatusLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row)">查看与复核</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="没有待核验的行驶证" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="search"
    />

    <el-drawer v-model="drawerVisible" title="行驶证核验" size="680px">
      <div v-loading="detailLoading">
        <el-alert
          class="mb12"
          type="info"
          :closable="false"
          show-icon
          title="证件号码与 VIN 属敏感字段，页面仅展示核验所需的脱敏展示值；原图查看受「敏感访问」权限控制并逐次留痕。"
        />

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ license.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="核验状态">
            <el-tag :type="license.reviewStatus === 'VERIFIED' ? 'success' : license.reviewStatus === 'PENDING' ? 'warning' : 'info'">
              {{ statusLabel(license.reviewStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="复核时间">{{ fmt(license.reviewedAt) }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ license.version ?? '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">证件字段</el-divider>
        <el-form label-width="110px">
          <el-form-item label="所有人"><el-input v-model="form.ownerName" /></el-form-item>
          <el-form-item label="号牌号码"><el-input v-model="form.plateNo" /></el-form-item>
          <el-form-item label="车辆识别代号"><el-input v-model="form.vin" /></el-form-item>
          <el-form-item label="注册日期">
            <el-date-picker v-model="form.registeredOn" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
          </el-form-item>
        </el-form>

        <el-divider content-position="left">证件影像</el-divider>
        <div class="photos">
          <div v-for="side in sides" :key="side.key" class="photo-card">
            <el-image
              v-if="license[side.key]"
              :src="fileUrl(license[side.key])"
              fit="cover"
              class="photo"
              :preview-src-list="[fileUrl(license[side.key])]"
              preview-teleported
            >
              <template #error><div class="photo-fallback">无权限 / 未上传</div></template>
            </el-image>
            <div v-else class="photo-fallback">未上传</div>
            <div class="photo-meta">{{ side.label }}</div>
          </div>
        </div>
        <div class="muted tip">若图片显示「无权限」，说明当前账号缺少敏感影像访问权限，可先按字段人工核对后复核。</div>
      </div>
      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button :loading="saving" @click="save(false)">保存</el-button>
        <el-button type="primary" :loading="saving" @click="save(true)">保存并标记已核验</el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliLicense" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderLicense, listInspectionTodos, listStations, saveOrderLicense } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const stations = ref<any[]>([])
const query = reactive<any>({ stationId: '', keyword: '', pageNum: 1, pageSize: 10 })

const drawerVisible = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const license = ref<any>({})
const form = reactive<any>({ ownerName: '', plateNo: '', vin: '', registeredOn: '' })

const sides = [
  { key: 'mainFileId', label: '主页' },
  { key: 'subFileId', label: '副页' }
]

onMounted(async () => {
  const res: any = await listStations({ pageNum: 1, pageSize: 100 })
  stations.value = (res && res.rows) || []
  load()
})

function search() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await listInspectionTodos({
      stationId: query.stationId || undefined,
      keyword: query.keyword || undefined,
      bucket: 'LICENSE_PENDING',
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

async function openReview(row: any) {
  drawerVisible.value = true
  detailLoading.value = true
  license.value = {}
  try {
    license.value = (await getOrderLicense(row.orderNo)) || {}
    form.ownerName = license.value.ownerName || ''
    form.plateNo = license.value.plateNo || row.plateNo || ''
    form.vin = license.value.vin || ''
    form.registeredOn = license.value.registeredOn || ''
  } finally {
    detailLoading.value = false
  }
}

async function save(verified: boolean) {
  saving.value = true
  try {
    const res: any = await saveOrderLicense(license.value.orderNo, {
      ownerName: form.ownerName,
      plateNo: form.plateNo,
      vin: form.vin,
      registeredOn: form.registeredOn,
      mainFileId: license.value.mainFileId || undefined,
      subFileId: license.value.subFileId || undefined,
      verified
    })
    license.value = res || license.value
    ElMessage.success(verified ? '已保存并标记为已核验' : '已保存')
    if (verified) {
      drawerVisible.value = false
      load()
    }
  } finally {
    saving.value = false
  }
}

function fileUrl(fileId: string) {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return `${base}/api/admin/files/${fileId}/content`
}

function statusLabel(v: string) {
  if (v === 'VERIFIED') return '已核验'
  if (v === 'PENDING') return '待复核'
  return '未登记'
}

function fmt(v: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '—'
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mb12 { margin-bottom: 12px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.w100 { width: 100%; }
.right { text-align: right; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
.tip { margin-top: 8px; line-height: 1.6; }
.photos { display: flex; gap: 12px; }
.photo-card { width: 168px; }
.photo { width: 168px; height: 112px; border-radius: 4px; background: var(--el-fill-color-light); }
.photo-fallback {
  width: 168px; height: 112px; display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: var(--el-text-color-secondary); background: var(--el-fill-color-light);
  border-radius: 4px;
}
.photo-meta { font-size: 12px; margin-top: 4px; text-align: center; }
</style>
