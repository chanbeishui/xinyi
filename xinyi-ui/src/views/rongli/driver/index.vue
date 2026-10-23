<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="昵称/openid">
        <el-input v-model="query.keyword" placeholder="昵称或 openId" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="资格状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="已通过" value="APPROVED" />
          <el-option label="审核中" value="SUBMITTED" />
          <el-option label="未通过" value="REJECTED" />
          <el-option label="已到期" value="EXPIRED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Check" @click="router.push('/rongli/people/qualification')">代驾认证审核</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="代驾" prop="nickname" width="130" />
      <el-table-column label="资格" width="110">
        <template #default="{ row }">
          <el-tag :type="row.qualification_status === 'APPROVED' ? 'success' : 'warning'">
            {{ statusLabel(row.qualification_status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="准驾车型" prop="license_class" width="110" />
      <el-table-column label="按听单" width="90" align="center">
        <template #default="{ row }">{{ row.listening_enabled ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="新邀约" width="90" align="center">
        <template #default="{ row }">{{ row.new_assignment_enabled ? '允许' : '暂停' }}</template>
      </el-table-column>
      <el-table-column label="在手任务" prop="active_assignments" width="100" align="center" />
      <el-table-column label="累计任务" prop="total_assignments" width="100" align="center" />
      <el-table-column label="复审到期" width="130">
        <template #default="{ row }">{{ fmt(row.review_due_at) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openAssignments(row)">承接记录</el-button>
          <el-button link type="warning" @click="toggleNew(row)">
            {{ row.new_assignment_enabled ? '暂停接单' : '恢复接单' }}
          </el-button>
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

    <el-drawer v-model="detailVisible" title="代驾档案" size="42%">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="资格状态">{{ statusLabel(detail.qualification_status) }}</el-descriptions-item>
        <el-descriptions-item label="准驾车型">{{ detail.license_class }}</el-descriptions-item>
        <el-descriptions-item label="驾驶证有效期">{{ detail.license_valid_to }}</el-descriptions-item>
        <el-descriptions-item label="最近定位">{{ detail.last_location_at || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="assignVisible" title="承接记录" width="760px">
      <el-table :data="assignments" border max-height="420">
        <el-table-column label="订单号" prop="order_no" width="180" />
        <el-table-column label="状态" prop="fulfillment_status" width="150" />
        <el-table-column label="结束时间" min-width="160">
          <template #default="{ row }">{{ row.ended_at || '进行中' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="RongliDriver" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { disableDriverNewAssignments, getDriver, listDriverAssignments, listDrivers } from '@/api/rongli'

const router = useRouter()

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ keyword: '', status: '', pageNum: 1, pageSize: 10 })
const detail = ref<any>(null)
const detailVisible = ref(false)
const assignments = ref<any[]>([])
const assignVisible = ref(false)

function statusLabel(s: string) {
  const map: Record<string, string> = {
    DRAFT: '待完善',
    SUBMITTED: '审核中',
    APPROVED: '已通过',
    REJECTED: '未通过',
    EXPIRED: '已到期'
  }
  return map[s] || s || '-'
}

function fmt(v: string) {
  return v ? String(v).slice(0, 10) : '-'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listDrivers(query)
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
  query.status = ''
  query.pageNum = 1
  load()
}

async function openDetail(row: any) {
  detail.value = await getDriver(row.driver_id)
  detailVisible.value = true
}

async function openAssignments(row: any) {
  const res: any = await listDriverAssignments(row.driver_id)
  assignments.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  assignVisible.value = true
}

async function toggleNew(row: any) {
  await disableDriverNewAssignments(row.driver_id, !!row.new_assignment_enabled)
  ElMessage.success('已更新接单状态')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
