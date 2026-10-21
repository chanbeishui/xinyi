<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="待审核" style="width: 150px" @change="handleQuery">
          <el-option label="待审核" value="SUBMITTED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="代驾" prop="nickname" width="140" />
      <el-table-column label="准驾车型" prop="licenseClass" width="120" />
      <el-table-column label="驾驶证有效期" prop="licenseValidTo" width="150" />
      <el-table-column label="提交时间" prop="submittedAt" width="200" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核意见" prop="reviewOpinion" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看资料</el-button>
          <el-button v-if="row.status === 'SUBMITTED'" link type="success" @click="audit(row, true)">通过</el-button>
          <el-button v-if="row.status === 'SUBMITTED'" link type="danger" @click="audit(row, false)">驳回</el-button>
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

    <el-drawer v-model="detailVisible" title="资格资料" size="42%">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detail.idCardMasked || detail.idCardNo }}</el-descriptions-item>
        <el-descriptions-item label="准驾车型">{{ detail.licenseClass }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ detail.licenseValidTo }}</el-descriptions-item>
        <el-descriptions-item label="活体核验">{{ detail.faceVerified ? '已通过' : '未完成' }}</el-descriptions-item>
        <el-descriptions-item label="培训">{{ detail.trainingPassed ? '已完成' : '未完成' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="RongliQualification" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditDriverQualification, getDriverQualification, listDriverQualifications } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: 'SUBMITTED', pageNum: 1, pageSize: 10 })
const detail = ref<any>(null)
const detailVisible = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listDriverQualifications(query)
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

async function openDetail(row: any) {
  detail.value = await getDriverQualification(row.applicationId)
  detailVisible.value = true
}

async function audit(row: any, approve: boolean) {
  const opinion = approve ? '资料齐全，审核通过' : '证件信息不符，请重新提交'
  await ElMessageBox.confirm(`确认${approve ? '通过' : '驳回'}该资格申请？`, '资格审核', { type: 'warning' })
  await auditDriverQualification(row.applicationId, { approve, opinion })
  ElMessage.success('审核完成')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
