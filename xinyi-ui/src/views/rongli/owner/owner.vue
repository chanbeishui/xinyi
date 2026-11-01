<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="车主运营档案（非另一套登录账户）；手机号脱敏；按订单权威部门数据范围，跨公司历史不因一条订单暴露。VERIFY_AGAIN 仅二次提醒不封禁下单；维护只改服务称呼/标记/备注，不覆盖微信标识、已验证手机与历史订单快照。" />

    <el-form :inline="true" class="mb12">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="昵称/服务称呼" clearable @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="标记">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
          <el-option label="正常" value="NORMAL" />
          <el-option label="二次核验" value="VERIFY_AGAIN" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="loading" @click="search">查询</el-button>
        <el-button :disabled="loading" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows">
      <el-table-column label="服务称呼" prop="serviceName" />
      <el-table-column label="昵称" prop="nickname" />
      <el-table-column label="手机号" prop="phoneMask" />
      <el-table-column label="核验" width="80">
        <template #default="{ row }">
          <el-tag :type="row.phoneVerified ? 'success' : 'info'" size="small">{{ row.phoneVerified ? '已核验' : '未核验' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="车辆" prop="vehicleCount" width="70" />
      <el-table-column label="订单" prop="orderCount" width="70" />
      <el-table-column label="最近服务" prop="lastServiceAt" width="200" />
      <el-table-column label="标记" width="100">
        <template #default="{ row }">
          <el-tag :type="row.serviceFlag === 'VERIFY_AGAIN' ? 'warning' : 'success'" size="small">
            {{ row.serviceFlag === 'VERIFY_AGAIN' ? '二次核验' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="warning" @click="openEdit(row)">维护</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="mt12" layout="total, prev, pager, next" :total="total"
      :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />

    <el-dialog v-model="detailDialog" title="车主档案详情" width="640px">
      <template v-if="detail">
        <p>服务称呼：{{ detail.serviceName || '—' }}（昵称 {{ detail.nickname || '—' }}）</p>
        <p>手机号：{{ detail.phoneMask || '—' }} {{ detail.phoneVerified ? '已核验' : '未核验' }}</p>
        <p>标记：{{ detail.serviceFlag === 'VERIFY_AGAIN' ? '二次核验（不封禁下单）' : '正常' }}　备注：{{ detail.internalRemark || '—' }}</p>
        <el-divider>关联车辆</el-divider>
        <el-table :data="vehicles" size="small">
          <el-table-column label="车牌" prop="plateNo" />
          <el-table-column label="车型" prop="vehicleTypeCode" />
          <el-table-column label="VIN" prop="vinMask" />
          <el-table-column label="核验" prop="verificationStatus" />
        </el-table>
        <el-divider>关联订单（本公司范围）</el-divider>
        <el-table :data="orders" size="small">
          <el-table-column label="订单号" prop="orderNo" />
          <el-table-column label="状态" prop="displayStatus" />
          <el-table-column label="检测点" prop="station.name" />
          <el-table-column label="支付" prop="paymentStatus" />
        </el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialog" title="维护车主档案" width="520px">
      <el-form v-if="form" label-width="90px">
        <el-form-item label="服务称呼">
          <el-input v-model="form.serviceName" />
        </el-form-item>
        <el-form-item label="标记">
          <el-select v-model="form.serviceFlag" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="二次核验" value="VERIFY_AGAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="内部备注">
          <el-input v-model="form.internalRemark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="更新原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="必填；敏感维护须留痕" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listOwners, getOwner, maintainOwner, ownerVehicles, ownerOrders, type OwnerRecord, type OwnerVehicleRef, type OwnerOrderRef } from '@/api/rongli/owner'
import { isVersionConflict } from '@/api/rongli/viError'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OwnerRecord[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', status: '' })

const detailDialog = ref(false)
const detail = ref<OwnerRecord | null>(null)
const vehicles = ref<OwnerVehicleRef[]>([])
const orders = ref<OwnerOrderRef[]>([])

const editDialog = ref(false)
const editId = ref('')
const form = ref<{ serviceName: string; serviceFlag: string; internalRemark: string; reason: string; version: number } | null>(null)

function uuid(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16)
  })
}

async function load() {
  loading.value = true
  try {
    const res = await listOwners(query.pageNum, query.pageSize, query.keyword || undefined, query.status || undefined)
    rows.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.keyword = ''; query.status = ''; query.pageNum = 1; load() }
function onPage(p: number) { query.pageNum = p; load() }

async function openDetail(row: OwnerRecord) {
  detail.value = (await getOwner(row.ownerId)).data
  vehicles.value = (await ownerVehicles(row.ownerId)).rows
  orders.value = (await ownerOrders(row.ownerId)).rows
  detailDialog.value = true
}

async function openEdit(row: OwnerRecord) {
  const fresh = (await getOwner(row.ownerId)).data
  editId.value = row.ownerId
  form.value = {
    serviceName: fresh.serviceName || '',
    serviceFlag: fresh.serviceFlag || 'NORMAL',
    internalRemark: fresh.internalRemark || '',
    reason: '',
    version: fresh.version
  }
  editDialog.value = true
}

async function save() {
  if (!form.value) return
  if (!form.value.reason.trim()) { ElMessage.warning('更新原因必填'); return }
  saving.value = true
  try {
    await maintainOwner(editId.value, {
      expectedVersion: form.value.version,
      reason: form.value.reason.trim(),
      serviceName: form.value.serviceName,
      serviceFlag: form.value.serviceFlag,
      internalRemark: form.value.internalRemark
    }, uuid())
    ElMessage.success('维护成功')
    editDialog.value = false
    await load()
  } catch (e: any) {
    if (isVersionConflict(e)) await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.mt12 { margin-top: 12px; }
</style>