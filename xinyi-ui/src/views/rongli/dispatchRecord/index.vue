<template>
  <RongliPage title="派单记录" subtitle="每次邀约、承接和解除均保留，原单重派不覆盖历史。">
    <el-row :gutter="12" class="mb8">
      <el-col :span="5">
        <el-input v-model="query.keyword" placeholder="订单号 / 车牌 / 司机" clearable @keyup.enter="search" />
      </el-col>
      <el-col :span="4">
        <el-select v-model="query.status" placeholder="全部状态" clearable class="w100">
          <el-option label="等待响应" value="WAIT_RESPONSE" />
          <el-option label="已接单" value="ACCEPTED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已超时" value="EXPIRED" />
          <el-option label="已撤销" value="REVOKED" />
        </el-select>
      </el-col>
      <el-col :span="15" class="right">
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Download" @click="exportCsv">导出</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="派单记录只追加不回写：同一订单多次承接、超时与撤销都会保留原始行，回退不会抹掉旧纪录。"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="批次 / 轮次" width="150">
        <template #default="{ row }">{{ row.roundKey || '—' }}</template>
      </el-table-column>
      <el-table-column label="订单号" prop="orderNo" min-width="170" />
      <el-table-column label="车牌" prop="plateNo" width="110" />
      <el-table-column label="代驾" prop="driverName" width="110" />
      <el-table-column label="策略" width="110">
        <template #default="{ row }">
          <el-tag :type="row.source === 'MANUAL' ? 'warning' : 'info'">{{ row.sourceLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发出时间" width="150">
        <template #default="{ row }">{{ fmt(row.issuedAt) }}</template>
      </el-table-column>
      <el-table-column label="失效时间" width="150">
        <template #default="{ row }">{{ fmt(row.expiresAt) }}</template>
      </el-table-column>
      <el-table-column label="响应" width="150">
        <template #default="{ row }">{{ fmt(row.respondedAt) }}</template>
      </el-table-column>
      <el-table-column label="结果" width="110">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作者" width="110">
        <template #default="{ row }">{{ row.source === 'MANUAL' ? '运营人工' : '系统自动' }}</template>
      </el-table-column>
      <template #empty>
        <el-empty description="没有符合条件的派单记录" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="search"
    />
  </RongliPage>
</template>

<script setup name="RongliDispatchRecord" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listInvites } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ keyword: '', status: '', pageNum: 1, pageSize: 20 })

onMounted(load)

function search() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await listInvites({
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function exportCsv() {
  if (!rows.value.length) {
    ElMessage.warning('当前没有可导出的记录')
    return
  }
  const header = ['批次/轮次', '订单号', '车牌', '代驾', '策略', '发出时间', '失效时间', '响应', '结果', '操作者']
  const lines = [header.join(',')]
  for (const r of rows.value) {
    lines.push(
      [
        r.roundKey || '',
        r.orderNo || '',
        r.plateNo || '',
        r.driverName || '',
        r.sourceLabel || '',
        fmt(r.issuedAt),
        fmt(r.expiresAt),
        fmt(r.respondedAt),
        r.statusLabel || '',
        r.source === 'MANUAL' ? '运营人工' : '系统自动'
      ]
        .map((v) => `"${String(v).replace(/"/g, '""')}"`)
        .join(',')
    )
  }
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `派单记录_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出当前页 ${rows.value.length} 条记录`)
}

function tagType(status: string) {
  if (status === 'ACCEPTED') return 'success'
  if (status === 'WAIT_RESPONSE') return 'warning'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function fmt(v: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '—'
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.w100 { width: 100%; }
.right { text-align: right; }
</style>
