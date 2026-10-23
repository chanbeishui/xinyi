<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="车牌/车架号">
        <el-input v-model="query.keyword" placeholder="车牌号或车架号后四位" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="车牌" prop="plateNo" width="130" />
      <el-table-column label="车型" prop="vehicleTypeName" width="140" />
      <el-table-column label="品牌型号" prop="brandModel" min-width="160" show-overflow-tooltip />
      <el-table-column label="车架号后四位" prop="vinLast4" width="130" />
      <el-table-column label="车主" prop="ownerName" width="120" />
      <el-table-column label="核对状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.verificationStatus === 'VERIFIED' ? 'success' : 'info'">{{ row.verificationStatus || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单数" prop="orderCount" width="100" align="center" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">档案</el-button>
          <el-button link type="primary" @click="openOrders(row)">订单</el-button>
          <el-button link type="primary" @click="openResults(row)">检测记录</el-button>
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

    <el-dialog v-model="orderVisible" title="车辆订单" width="760px">
      <el-table :data="orders" border max-height="420">
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="状态" prop="fulfillmentStatusLabel" width="140" />
        <el-table-column label="上门时段" prop="pickupWindowLabel" min-width="190" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="resultVisible" title="检测记录" width="760px">
      <el-table :data="results" border max-height="420">
        <el-table-column label="检测时间" prop="inspectedAt" width="200" />
        <el-table-column label="结论" prop="conclusion" width="120" />
        <el-table-column label="站点" prop="stationName" min-width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="RongliVehicle" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getVehicle, listVehicleOrders, listVehicleResults, listVehicles } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ keyword: '', pageNum: 1, pageSize: 10 })
const orders = ref<any[]>([])
const results = ref<any[]>([])
const orderVisible = ref(false)
const resultVisible = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listVehicles(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  load()
}

function resetQuery() {
  query.keyword = ''
  query.pageNum = 1
  load()
}

async function openDetail(row: any) {
  const view: any = await getVehicle(row.vehicleId)
  rows.value = rows.value.map((r) => (r.vehicleId === row.vehicleId ? { ...r, ...view } : r))
  orderVisible.value = false
  resultVisible.value = false
}

async function openOrders(row: any) {
  const res: any = await listVehicleOrders(row.vehicleId)
  orders.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  orderVisible.value = true
}

async function openResults(row: any) {
  const res: any = await listVehicleResults(row.vehicleId)
  results.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  resultVisible.value = true
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
