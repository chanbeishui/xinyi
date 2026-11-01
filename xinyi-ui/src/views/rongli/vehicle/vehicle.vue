<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="客户-车辆关系；不凭同车牌跨账户合并；VIN/手机号脱敏；记录维护不追改订单历史快照。" />
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="车牌" prop="plateNo" />
      <el-table-column label="车型" prop="vehicleTypeCode" />
      <el-table-column label="品牌型号" prop="brandModel" />
      <el-table-column label="VIN" prop="vinMask" />
      <el-table-column label="车主" prop="ownerRelation.ownerId" />
      <el-table-column label="核验" prop="verificationStatus" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="warning" @click="maintain(row)">维护</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="车辆详情" width="560px">
      <template v-if="detail">
        <p>车牌：{{ detail.plateNo }}（VIN {{ detail.vinMask }}）</p>
        <p>车主：{{ detail.ownerRelation?.ownerId }} {{ detail.ownerRelation?.phoneMask || '' }}</p>
        <p>最近结果：{{ detail.lastResult || '—' }} {{ detail.lastInspectAt || '' }}</p>
        <el-divider>关联订单</el-divider>
        <el-table :data="orders" size="small">
          <el-table-column label="订单号" prop="orderNo" />
          <el-table-column label="状态" prop="fulfillmentStatus" />
        </el-table>
        <el-divider>检测结果</el-divider>
        <el-table :data="results" size="small">
          <el-table-column label="订单号" prop="orderNo" />
          <el-table-column label="结论" prop="conclusion" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listVehicles, getVehicle, maintainVehicle, vehicleOrders, vehicleResults, type VehicleRecord } from '@/api/rongli/vehicle'

const loading = ref(false)
const rows = ref<VehicleRecord[]>([])
const dialog = ref(false)
const detail = ref<VehicleRecord | null>(null)
const orders = ref<any[]>([])
const results = ref<any[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = (await listVehicles(1, 50)).data
  } finally {
    loading.value = false
  }
}

async function openDetail(row: VehicleRecord) {
  detail.value = (await getVehicle(row.vehicleId)).data
  orders.value = (await vehicleOrders(row.vehicleId)).data
  results.value = (await vehicleResults(row.vehicleId)).data
  dialog.value = true
}

async function maintain(row: VehicleRecord) {
  const { value } = await ElMessageBox.prompt(`维护车辆 ${row.plateNo}，填写原因`, '车辆维护', {
    confirmButtonText: '下一步', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '原因必填'
  })
  const fresh = await getVehicle(row.vehicleId)
  await maintainVehicle(row.vehicleId, { expectedVersion: fresh.data.version, reason: value })
  ElMessage.success('维护成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
</style>