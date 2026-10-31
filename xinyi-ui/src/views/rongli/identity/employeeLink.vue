<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="员工微信关联由手机号自动匹配产生；暂停保留唯一占用，解除禁止自动复绑，允许重核须本人新微信手机号核验。" />
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="员工" prop="employeeName" />
      <el-table-column label="手机号" prop="phoneMask" />
      <el-table-column label="微信标识" prop="openIdMask" />
      <el-table-column label="状态" prop="status">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'SUSPENDED' ? 'warning' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="禁止复绑" prop="autoRebindBlocked">
        <template #default="{ row }">{{ row.autoRebindBlocked ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button v-if="row.status === 'ACTIVE'" link type="warning" @click="act(row, 'suspend')">暂停</el-button>
          <el-button v-if="row.status !== 'REVOKED'" link type="danger" @click="act(row, 'revoke')">解除</el-button>
          <el-button v-if="row.status !== 'ACTIVE'" link type="primary" @click="act(row, 'rematch')">允许重核</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listEmployeeLinks, suspendEmployeeLink, revokeEmployeeLink, allowEmployeeRematch } from '@/api/rongli/employeeLink'
import type { EmployeeLinkAdmin } from '@/api/rongli/identity'

const loading = ref(false)
const rows = ref<EmployeeLinkAdmin[]>([])

async function load() {
  loading.value = true
  try {
    const r = await listEmployeeLinks()
    rows.value = r.data
  } finally {
    loading.value = false
  }
}

async function act(row: EmployeeLinkAdmin, kind: 'suspend' | 'revoke' | 'rematch') {
  const title = kind === 'suspend' ? '暂停关联' : kind === 'revoke' ? '解除关联' : '允许重新核验'
  const { value } = await ElMessageBox.prompt(`对员工 ${row.employeeName} 执行「${title}」，请填写原因`, title, {
    confirmButtonText: '确认', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '原因必填'
  })
  if (kind === 'suspend') await suspendEmployeeLink(row.bindingId, row.version, value)
  else if (kind === 'revoke') await revokeEmployeeLink(row.bindingId, row.version, value)
  else await allowEmployeeRematch(row.bindingId, row.version, value)
  ElMessage.success('操作成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
</style>