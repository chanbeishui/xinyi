<template>
  <RongliPage title="车型数据字典" subtitle="框架字典 vehicle_type 的复用界面，不建设第二套车型字典。">
    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="车型复用框架数据字典 vehicle_type，不另建重复字典；修改编码会影响计价规则与站点可服务车型的匹配，历史订单保留下单时的车型快照。"
    />

    <el-row :gutter="12" class="mb8">
      <el-col :span="18">
        <el-button type="primary" icon="Setting" @click="goDict">前往字典维护</el-button>
        <el-button icon="Money" @click="goPrice">前往计价规则</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="字典标签" prop="dictLabel" min-width="160" />
      <el-table-column label="字典键值" prop="dictValue" min-width="160" />
      <el-table-column label="排序" prop="dictSort" width="90" align="center" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
      <template #empty>
        <el-empty description="车型字典还没有配置项" />
      </template>
    </el-table>

    <el-card shadow="never" class="mt16">
      <template #header>关联规则说明</template>
      <ul class="notes">
        <li><b>计价规则</b>：按「车型 × 检测类型」定价，车型编码取自本字典。</li>
        <li><b>检测点可服务车型</b>：检测点配置里引用同一批车型编码，用于下单时的可选站点过滤。</li>
        <li><b>历史订单</b>：车型展示走订单快照，字典改名不会改写既有订单。</li>
      </ul>
    </el-card>
  </RongliPage>
</template>

<script setup name="RongliVehicleType" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDicts } from '@/api/system/dict/data'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await getDicts('vehicle_type')
    rows.value = (res && res.data) || []
  } finally {
    loading.value = false
  }
}

function goDict() {
  router.push({ path: '/system/dict', query: { dictType: 'vehicle_type' } })
}

function goPrice() {
  router.push('/rongli/inspect/price')
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; }
.notes { margin: 0; padding-left: 18px; line-height: 2; }
</style>
