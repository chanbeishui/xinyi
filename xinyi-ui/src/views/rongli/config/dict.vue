<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="车型/检测类型字典复用框架字典（sys_dict_data），不另造重复字典；字典维护跳转框架页面。变更保留订单标签快照；新增车型须先配置已发布价格方可下单（价格守卫，防 0 元）。" />

    <div class="toolbar mb12">
      <el-button type="primary" plain @click="goDict">字典维护（复用框架）</el-button>
      <el-button :disabled="loading" @click="load">刷新</el-button>
    </div>

    <el-divider content-position="left">车型字典 · vi_vehicle_type</el-divider>
    <el-table v-loading="loading" :data="vehicleTypes" size="small">
      <el-table-column label="编码" prop="dictValue" />
      <el-table-column label="标签" prop="dictLabel" />
      <el-table-column label="排序" prop="dictSort" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-divider content-position="left">检测类型 · vi_inspection_type</el-divider>
    <el-table v-loading="loading" :data="inspectionTypes" size="small">
      <el-table-column label="编码" prop="dictValue" />
      <el-table-column label="标签" prop="dictLabel" />
      <el-table-column label="排序" prop="dictSort" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDicts } from '@/api/system/dict/data'
import type { SysDictData } from '@/types/api/system/dict'

const router = useRouter()
const loading = ref(false)
const vehicleTypes = ref<SysDictData[]>([])
const inspectionTypes = ref<SysDictData[]>([])

async function load() {
  loading.value = true
  try {
    vehicleTypes.value = (await getDicts('vi_vehicle_type')).data || []
    inspectionTypes.value = (await getDicts('vi_inspection_type')).data || []
  } finally {
    loading.value = false
  }
}

// B33-edit：复用框架字典维护页，不在本域另造编辑器
function goDict() {
  router.push('/system/dict')
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.toolbar { display: flex; align-items: center; gap: 8px; }
</style>