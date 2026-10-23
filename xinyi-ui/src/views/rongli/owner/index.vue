<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="昵称/手机号">
        <el-input v-model="query.keyword" placeholder="车主昵称或手机号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="车主" prop="nickname" width="140" />
      <el-table-column label="称呼" prop="serviceName" width="120" />
      <el-table-column label="手机号" width="140">
        <template #default="{ row }">{{ row.phoneMasked || (row.phoneBound ? '已绑定' : '未绑定') }}</template>
      </el-table-column>
      <el-table-column label="车辆数" prop="vehicleCount" width="100" align="center" />
      <el-table-column label="订单数" prop="orderCount" width="100" align="center" />
      <el-table-column label="注册时间" prop="createdAt" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">档案</el-button>
          <el-button link type="primary" @click="openOrders(row)">订单</el-button>
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

    <el-drawer v-model="detailVisible" title="车主档案" size="42%">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="称呼">{{ detail.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phoneMasked || (detail.phoneBound ? '已绑定' : '未绑定') }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ (detail.vehicles || []).length }} 辆</el-descriptions-item>
        <el-descriptions-item label="账号状态">{{ detail.status }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">车辆</el-divider>
      <el-table :data="detail?.vehicles || []" border size="small">
        <el-table-column label="车牌" prop="plateNo" />
        <el-table-column label="车型" prop="vehicleTypeName" />
      </el-table>
    </el-drawer>

    <el-dialog v-model="orderVisible" title="车主订单" width="760px">
      <el-table :data="orders" border max-height="420">
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="状态" prop="fulfillmentStatusLabel" width="140" />
        <el-table-column label="金额(元)" prop="totalYuan" width="110" />
        <el-table-column label="上门时段" prop="pickupWindowLabel" min-width="190" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="RongliOwner" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getOwner, listOwnerOrders, listOwners } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ keyword: '', pageNum: 1, pageSize: 10 })
const detail = ref<any>(null)
const detailVisible = ref(false)
const orders = ref<any[]>([])
const orderVisible = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listOwners(query)
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
  // 后端列表返回的是 owner_id（下划线），早期这里读的是 row.ownerId，
  // 导致点「档案」时请求 /api/admin/owners/undefined 并 500。
  const id = row.owner_id ?? row.ownerId ?? row.account_id
  if (!id) {
    return
  }
  detail.value = await getOwner(id)
  detailVisible.value = true
}

async function openOrders(row: any) {
  const id = row.owner_id ?? row.ownerId ?? row.account_id
  if (!id) {
    return
  }
  const res: any = await listOwnerOrders(id)
  orders.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  orderVisible.value = true
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
