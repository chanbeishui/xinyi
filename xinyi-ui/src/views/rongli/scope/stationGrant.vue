<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="按 sys_user 直接配置站点（员工可未微信登录）；集合版本含空集合，清空即撤权；站点不授予功能码，与组织/部门/功能取交集。" />
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="员工ID" prop="employeeUserId" />
      <el-table-column label="行政站点" prop="ownStationId" />
      <el-table-column label="授权站点" prop="authorizedStationIds">
        <template #default="{ row }">{{ row.authorizedStationIds.join(', ') || '（空）' }}</template>
      </el-table-column>
      <el-table-column label="微信状态" prop="wechatLinkStatus" />
      <el-table-column label="版本" prop="version" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">配置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="配置站点授权" width="480px">
      <el-form label-width="90px">
        <el-form-item label="员工ID">{{ editing?.employeeUserId }}</el-form-item>
        <el-form-item label="授权站点">
          <el-input v-model="idsText" placeholder="站点ID，逗号分隔，可为空" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="reason" placeholder="必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listStationGrants, getStationGrant, putStationGrant, type StationGrantView } from '@/api/rongli/scope'

const loading = ref(false)
const rows = ref<StationGrantView[]>([])
const dialog = ref(false)
const editing = ref<StationGrantView | null>(null)
const idsText = ref('')
const reason = ref('')

async function load() {
  loading.value = true
  try {
    rows.value = (await listStationGrants(1, 50)).data
  } finally {
    loading.value = false
  }
}

async function openEdit(row: StationGrantView) {
  const fresh = await getStationGrant(row.employeeUserId)
  editing.value = fresh.data
  idsText.value = fresh.data.authorizedStationIds.join(',')
  reason.value = ''
  dialog.value = true
}

async function save() {
  if (!editing.value) return
  if (!reason.value.trim()) { ElMessage.warning('原因必填'); return }
  const ids = idsText.value.split(',').map(s => s.trim()).filter(Boolean)
  await putStationGrant(editing.value.employeeUserId, ids, editing.value.version, reason.value.trim())
  ElMessage.success('保存成功')
  dialog.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
</style>