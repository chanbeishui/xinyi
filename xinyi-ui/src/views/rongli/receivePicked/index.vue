<template>
  <div class="app-container">
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

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="收车必须核对司机口述的 4 位交接码，核验规则与代驾端完全一致；连续 3 次错误会锁定本阶段交接。"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="订单号" prop="orderNo" min-width="180" />
      <el-table-column label="车牌" prop="plateNo" width="110" />
      <el-table-column label="车型" prop="vehicleTypeName" width="120" />
      <el-table-column label="检测点" prop="stationName" min-width="150" show-overflow-tooltip />
      <el-table-column label="司机" width="110">
        <template #default="{ row }">{{ row.driverName || '—' }}</template>
      </el-table-column>
      <el-table-column label="车主" prop="ownerName" width="110" />
      <el-table-column label="上门时间" width="160">
        <template #default="{ row }">{{ fmt(row.pickupStartsAt) }}</template>
      </el-table-column>
      <el-table-column label="交接码" width="130">
        <template #default="{ row }">
          <el-tag v-if="!row.handover" type="info">司机未发起</el-tag>
          <el-tag v-else-if="row.handover.status === 'PENDING'" :type="row.handover.expired ? 'warning' : 'success'">
            {{ row.handover.expired ? '已过期' : '待核验' }}
          </el-tag>
          <el-tag v-else type="info">{{ row.handover.status === 'CONFIRMED' ? '已完成' : row.handover.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">收车登记</el-button>
          <el-button link type="primary" @click="openOrder(row)">订单详情</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有待收车订单" />
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

    <el-drawer v-model="drawerVisible" title="站点收车登记" size="620px">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="履约状态">{{ detail.fulfillmentStatusLabel }}</el-descriptions-item>
          <el-descriptions-item label="生效承接">{{ detail.hasAssignment ? '有' : '无（未派单）' }}</el-descriptions-item>
          <el-descriptions-item label="交接码">
            <el-tag v-if="!handover" type="info">司机尚未发起交接</el-tag>
            <el-tag v-else-if="handover.status === 'PENDING'" :type="handover.expired ? 'warning' : 'success'">
              {{ handover.expired ? '已过期' : '待核验（第 ' + handover.generationNo + ' 代）' }}
            </el-tag>
            <el-tag v-else type="info">{{ handover.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">节点 2 送车证据</el-divider>
        <template v-if="detail.evidence">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="证据状态">{{ detail.evidence.status }}</el-descriptions-item>
            <el-descriptions-item label="里程">{{ detail.evidence.mileageKm ?? '—' }} km</el-descriptions-item>
            <el-descriptions-item label="里程来源">{{ detail.evidence.mileageSource || '—' }}</el-descriptions-item>
            <el-descriptions-item label="补录原因">{{ detail.evidence.mileageReason || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div class="photos">
            <div v-for="p in detail.evidence.photos" :key="p.fileId + p.photoType" class="photo-card">
              <el-image
                v-if="p.fileId"
                :src="fileUrl(p.fileId)"
                fit="cover"
                class="photo"
                :preview-src-list="[fileUrl(p.fileId)]"
                preview-teleported
              >
                <template #error>
                  <div class="photo-fallback">需授权</div>
                </template>
              </el-image>
              <div class="photo-meta">
                <span>{{ photoLabel(p.photoType) }}</span>
                <span class="muted">{{ fmt(p.capturedAt) }}</span>
              </div>
            </div>
            <el-empty v-if="!detail.evidence.photos.length" description="司机尚未上传节点 2 照片" :image-size="60" />
          </div>
          <div class="muted tip">证件与车况原图属敏感影像，需具备「敏感访问」权限，且每次查看都会写入审计留痕。</div>
        </template>
        <el-empty v-else description="暂无节点 2 证据" :image-size="60" />

        <el-divider content-position="left">确认收车</el-divider>
        <el-form label-width="110px">
          <el-form-item label="4 位交接码" required>
            <el-input
              v-model="form.code"
              maxlength="4"
              placeholder="请向司机索取后录入"
              style="width: 180px"
              :disabled="!canConfirm"
            />
          </el-form-item>
          <el-form-item label="补录里程">
            <el-input-number v-model="form.mileageKm" :min="0" :step="1" :disabled="!canConfirm" />
            <span class="muted ml8">留空则不补录，仅在司机漏拍仪表盘时使用</span>
          </el-form-item>
        </el-form>
        <el-alert
          v-if="!canConfirm"
          type="warning"
          :closable="false"
          show-icon
          :title="handoverHint"
        />
      </div>
      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!canConfirm" :loading="submitting" @click="submit">
          确认收车
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="RongliReceivePicked" lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  confirmReceiving,
  getReceivingDetail,
  listReceivingPending,
  listStations
} from '@/api/rongli'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const stations = ref<any[]>([])
const query = reactive<any>({ stationId: '', keyword: '', pageNum: 1, pageSize: 10 })

const drawerVisible = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const detail = ref<any>({})
const form = reactive<any>({ code: '', mileageKm: undefined })

const handover = computed(() => detail.value?.handover || null)
const canConfirm = computed(() => !!handover.value && handover.value.canConfirm === true)
const handoverHint = computed(() => {
  if (!detail.value?.hasAssignment) {
    return '该订单当前没有生效承接，无法发起站点侧交接'
  }
  if (!handover.value) {
    return '等待司机在代驾端发起「送车到站」交接，发起后才能录入交接码'
  }
  if (handover.value.expired) {
    return '交接码已过期，请让司机重新发起一次交接'
  }
  return '当前交接状态不可确认，请刷新后重试'
})

const PHOTO_LABEL: Record<string, string> = {
  FRONT: '车头',
  BACK: '车尾',
  LEFT: '左侧',
  RIGHT: '右侧',
  DASHBOARD: '仪表盘',
  INTERIOR: '内饰',
  EXTRA: '补充照'
}

onMounted(async () => {
  await loadStations()
  load()
})

async function loadStations() {
  const res: any = await listStations({ pageNum: 1, pageSize: 100 })
  stations.value = (res && res.rows) || []
}

function search() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await listReceivingPending({
      stationId: query.stationId || undefined,
      keyword: query.keyword || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

async function openDetail(row: any) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = {}
  form.code = ''
  form.mileageKm = undefined
  try {
    detail.value = (await getReceivingDetail(row.orderNo)) || {}
  } finally {
    detailLoading.value = false
  }
}

async function submit() {
  if (!/^\d{4}$/.test(form.code)) {
    ElMessage.warning('请输入 4 位数字交接码')
    return
  }
  submitting.value = true
  try {
    const payload: any = { code: form.code }
    if (form.mileageKm !== undefined && form.mileageKm !== null) {
      payload.evidence = { nodeNo: 2, mileageKm: Number(form.mileageKm), mileageSource: 'MANUAL' }
    }
    await confirmReceiving(detail.value.orderNo, handover.value.handoverId, payload)
    ElMessage.success('已确认收车，订单进入检测中')
    drawerVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

function openOrder(row: any) {
  router.push({ path: '/rongli/business/order', query: { orderNo: row.orderNo } })
}

function fileUrl(fileId: string) {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return `${base}/api/admin/files/${fileId}/content`
}

function fmt(v: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '—'
}

function photoLabel(t: string) {
  return PHOTO_LABEL[t] || t
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.ml8 { margin-left: 8px; }
.w100 { width: 100%; }
.right { text-align: right; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
.tip { margin-top: 8px; line-height: 1.6; }
.photos { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
.photo-card { width: 108px; }
.photo { width: 108px; height: 78px; border-radius: 4px; background: var(--el-fill-color-light); }
.photo-fallback {
  width: 108px; height: 78px; display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: var(--el-text-color-secondary); background: var(--el-fill-color-light);
}
.photo-meta { display: flex; flex-direction: column; font-size: 12px; line-height: 1.5; }
</style>
