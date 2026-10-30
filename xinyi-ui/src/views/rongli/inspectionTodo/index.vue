<template>
  <RongliPage title="检测结果待办" subtitle="仅处理已收车订单的资料核验和最终结果，不另建检测设备任务。">
    <el-row :gutter="12" class="mb8">
      <el-col :span="5">
        <el-select v-model="query.stationId" placeholder="全部检测点" clearable filterable class="w100">
          <el-option v-for="s in stations" :key="s.id || s.station_id" :label="s.name" :value="s.id || s.station_id" />
        </el-select>
      </el-col>
      <el-col :span="5">
        <el-input v-model="query.keyword" placeholder="订单号 / 车牌 / 司机" clearable @keyup.enter="search" />
      </el-col>
      <el-col :span="14" class="right">
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-tabs v-model="bucket" class="mb8" @tab-change="search">
      <el-tab-pane label="全部待办" name="ALL" />
      <el-tab-pane label="资料待核验" name="LICENSE_PENDING" />
      <el-tab-pane label="结果待登记" name="RESULT_PENDING" />
    </el-tabs>

    <el-row :gutter="12" class="mb12">
      <el-col :span="8">
        <el-card shadow="never" class="stat">
          <div class="stat-num">{{ counts.all }}</div>
          <div class="stat-label">在站订单</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat">
          <div class="stat-num warn">{{ counts.licensePending }}</div>
          <div class="stat-label">资料待核验</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat">
          <div class="stat-num primary">{{ counts.resultPending }}</div>
          <div class="stat-label">结果待登记</div>
        </el-card>
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
      <el-table-column label="行驶证" width="100">
        <template #default="{ row }">
          <el-tag :type="row.licenseStatus === 'VERIFIED' ? 'success' : 'warning'">{{ row.licenseStatusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果版本" width="100" align="center">
        <template #default="{ row }">{{ row.resultCount > 0 ? 'v' + row.resultRevision : '未登记' }}</template>
      </el-table-column>
      <el-table-column label="下一步" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.bucket === 'LICENSE_PENDING'" type="warning">先核行驶证</el-tag>
          <el-tag v-else-if="row.bucket === 'RESULT_PENDING'" type="primary">登记结果</el-tag>
          <el-tag v-else type="success">已完成</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.licenseStatus !== 'VERIFIED'"
            link
            type="primary"
            @click="goLicense(row)"
          >
            去核验
          </el-button>
          <el-button
            v-else
            link
            type="primary"
            :disabled="row.fulfillmentStatus !== 'INSPECTING' && row.resultCount === 0"
            @click="goRegister(row)"
          >
            {{ row.resultCount > 0 ? '查看 / 更正' : '登记结果' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有检测待办" />
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
  </RongliPage>
</template>

<script setup name="RongliInspectionTodo" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listInspectionTodos, listStations } from '@/api/rongli'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const stations = ref<any[]>([])
const bucket = ref('ALL')
const query = reactive<any>({ stationId: '', keyword: '', pageNum: 1, pageSize: 10 })
const counts = reactive({ all: 0, licensePending: 0, resultPending: 0 })

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
      bucket: bucket.value === 'ALL' ? undefined : bucket.value,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
  loadCounts()
}

/** 三档计数用 total 取，避免只统计当前页造成误读 */
async function loadCounts() {
  const base = { stationId: query.stationId || undefined, keyword: query.keyword || undefined, pageNum: 1, pageSize: 1 }
  const [all, licensePending, resultPending] = await Promise.all([
    listInspectionTodos(base),
    listInspectionTodos({ ...base, bucket: 'LICENSE_PENDING' }),
    listInspectionTodos({ ...base, bucket: 'RESULT_PENDING' })
  ])
  counts.all = Number((all && all.total) || 0)
  counts.licensePending = Number((licensePending && licensePending.total) || 0)
  counts.resultPending = Number((resultPending && resultPending.total) || 0)
}

function goLicense(row: any) {
  router.push({ path: '/rongli/inspect/license', query: { orderNo: row.orderNo } })
}

function goRegister(row: any) {
  router.push({ path: '/rongli/inspect/resultRegister', query: { orderNo: row.orderNo } })
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mb12 { margin-bottom: 12px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.w100 { width: 100%; }
.right { text-align: right; }
.stat { text-align: center; }
.stat-num { font-size: 26px; font-weight: 600; line-height: 1.3; }
.stat-num.warn { color: var(--el-color-warning); }
.stat-num.primary { color: var(--el-color-primary); }
.stat-label { font-size: 12px; color: var(--el-text-color-secondary); }
</style>
