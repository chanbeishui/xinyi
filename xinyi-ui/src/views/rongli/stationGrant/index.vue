<template>
  <div class="app-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="检测员工只能操作被授权的检测点；授权变更后小程序端需重新切换视角才会生效。"
      class="mb8"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="员工账号" prop="user_name" min-width="140" />
      <el-table-column label="姓名" prop="nick_name" width="120" />
      <el-table-column label="部门" prop="dept_name" min-width="160" show-overflow-tooltip />
      <el-table-column label="检测点" prop="station_name" min-width="160" />
      <el-table-column label="授权" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '已授权' : '未授权' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggle(row)">{{ row.enabled ? '取消授权' : '授权' }}</el-button>
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

<script setup name="RongliStationGrant" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStationGrants, updateStationGrant } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 20 })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listStationGrants(query)
    rows.value = (res && res.rows) || []
    total.value = rows.value.length
  } finally {
    loading.value = false
  }
}

async function toggle(row: any) {
  await updateStationGrant(row.employee_user_id || row.user_id, {
    stationId: row.station_id,
    enabled: !row.enabled
  })
  ElMessage.success('已更新授权')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
