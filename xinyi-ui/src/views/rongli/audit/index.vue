<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="聚合类型">
        <el-select v-model="query.aggregateType" clearable placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="订单" value="ORDER" />
          <el-option label="售后" value="AFTERSALE" />
          <el-option label="代驾" value="DRIVER" />
          <el-option label="对账" value="LEDGER" />
          <el-option label="争议" value="DISPUTE" />
        </el-select>
      </el-form-item>
      <el-form-item label="动作">
        <el-input v-model="query.action" placeholder="例如 ORDER_CREATED" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Lock" @click="router.push('/rongli/runtime/governance')">治理与导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="时间" prop="createdAt" width="190" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="聚合" prop="aggregateType" width="110" />
      <el-table-column label="动作" prop="action" width="200" />
      <el-table-column label="域" prop="actorRealm" width="90" />
      <el-table-column label="角色" prop="actingRole" width="100" />
      <el-table-column label="账号/用户" width="150">
        <template #default="{ row }">{{ row.actorAccountId || row.actorUserId || 'SYSTEM' }}</template>
      </el-table-column>
      <el-table-column label="原因" prop="reason" min-width="180" show-overflow-tooltip />
      <el-table-column label="变更后" min-width="220">
        <template #default="{ row }">
          <span class="mono">{{ row.afterSummary ? JSON.stringify(row.afterSummary) : '-' }}</span>
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
  </div>
</template>

<script setup name="RongliAudit" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listAuditEvents } from '@/api/rongli'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ aggregateType: '', action: '', pageNum: 1, pageSize: 20 })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listAuditEvents(query)
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
  query.aggregateType = ''
  query.action = ''
  query.pageNum = 1
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.mono { font-family: Consolas, Monaco, monospace; font-size: 12px; word-break: break-all; }
</style>
