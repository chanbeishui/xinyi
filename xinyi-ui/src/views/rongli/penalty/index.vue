<template>
  <div class="app-container">
    <el-tabs v-model="active" @tab-change="onTabChange">
      <el-tab-pane label="接单限制记录" name="penalty">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="penaltyQuery.status" clearable placeholder="全部" style="width: 140px" @change="loadPenalties">
              <el-option label="生效中" value="ACTIVE" />
              <el-option label="已过期" value="EXPIRED" />
              <el-option label="已撤销" value="REVOKED" />
            </el-select>
          </el-form-item>
          <el-form-item label="代驾 ID">
            <el-input v-model="penaltyQuery.driverId" placeholder="可选" clearable style="width: 130px" @keyup.enter="loadPenalties" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadPenalties">查询</el-button>
            <el-button icon="Refresh" @click="resetPenalty">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="penaltyRows" border>
          <el-table-column label="编号" prop="penaltyId" width="90" />
          <el-table-column label="代驾" prop="driverName" width="130" />
          <el-table-column label="限制类型" prop="penaltyTypeLabel" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'danger' : 'info'">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="判定原因" prop="reason" min-width="200" show-overflow-tooltip />
          <el-table-column label="关联订单" prop="orderNo" width="180" />
          <el-table-column label="业务日" prop="businessDate" width="120" />
          <el-table-column label="生效至" prop="effectiveUntil" width="180" />
          <el-table-column label="规则版本" prop="ruleVersion" width="110" />
        </el-table>

        <el-pagination
          v-model:current-page="penaltyQuery.pageNum"
          v-model:page-size="penaltyQuery.pageSize"
          :total="penaltyTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadPenalties"
          @size-change="loadPenalties"
        />
      </el-tab-pane>

      <el-tab-pane label="申诉处理" name="appeal">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="appealQuery.status" clearable placeholder="全部" style="width: 140px" @change="loadAppeals">
              <el-option label="待处理" value="SUBMITTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="未通过" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="Refresh" @click="loadAppeals">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="申诉通过即撤销对应限制记录；未通过需填写处理意见，代驾端可见。"
        />

        <el-table v-loading="loading" :data="appealRows" border>
          <el-table-column label="编号" prop="appealId" width="90" />
          <el-table-column label="代驾" prop="driverName" width="130" />
          <el-table-column label="限制类型" prop="penaltyType" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'info' : 'warning'">
                {{ row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="原判定原因" prop="penaltyReason" min-width="180" show-overflow-tooltip />
          <el-table-column label="申诉理由" prop="reason" min-width="220" show-overflow-tooltip />
          <el-table-column label="处理意见" prop="publicOpinion" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="submittedAt" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'SUBMITTED'" @click="openAudit(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="appealQuery.pageNum"
          v-model:page-size="appealQuery.pageSize"
          :total="appealTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadAppeals"
          @size-change="loadAppeals"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="auditVisible" title="申诉审核" width="500px">
      <el-form label-width="90px">
        <el-form-item label="代驾">
          <span>{{ auditForm.driverName }}</span>
        </el-form-item>
        <el-form-item label="申诉理由">
          <span class="wrap">{{ auditForm.reason }}</span>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.approve">
            <el-radio :value="true">通过并撤销限制</el-radio>
            <el-radio :value="false">未通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliPenalty" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditAppeal, listAppeals, listPenalties } from '@/api/rongli'

const active = ref('penalty')
const loading = ref(false)
const saving = ref(false)

const penaltyRows = ref<any[]>([])
const penaltyTotal = ref(0)
const penaltyQuery = reactive<any>({ driverId: '', status: '', pageNum: 1, pageSize: 10 })

const appealRows = ref<any[]>([])
const appealTotal = ref(0)
const appealQuery = reactive<any>({ driverId: '', status: '', pageNum: 1, pageSize: 10 })

const auditVisible = ref(false)
const auditForm = reactive<any>({ appealId: '', driverName: '', reason: '', approve: true, opinion: '' })

onMounted(() => {
  loadPenalties()
})

function onTabChange(name: string) {
  if (name === 'penalty') loadPenalties()
  if (name === 'appeal') loadAppeals()
}

async function loadPenalties() {
  loading.value = true
  try {
    const res: any = await listPenalties(clean(penaltyQuery))
    penaltyRows.value = (res && res.rows) || []
    penaltyTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

/** driverId 是数字参数，空串会被后端类型转换拒绝，需剔除 */
function clean(query: any) {
  const params: any = { ...query }
  Object.keys(params).forEach((k) => {
    if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
  })
  return params
}

function resetPenalty() {
  penaltyQuery.driverId = ''
  penaltyQuery.status = ''
  penaltyQuery.pageNum = 1
  loadPenalties()
}

async function loadAppeals() {
  loading.value = true
  try {
    const res: any = await listAppeals(clean(appealQuery))
    appealRows.value = (res && res.rows) || []
    appealTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openAudit(row: any) {
  auditForm.appealId = row.appealId
  auditForm.driverName = row.driverName || '-'
  auditForm.reason = row.reason || ''
  auditForm.approve = true
  auditForm.opinion = ''
  auditVisible.value = true
}

async function submitAudit() {
  if (!auditForm.approve && !auditForm.opinion) {
    ElMessage.warning('未通过时请填写处理意见')
    return
  }
  saving.value = true
  try {
    await auditAppeal(auditForm.appealId, { approve: auditForm.approve, opinion: auditForm.opinion })
    ElMessage.success(auditForm.approve ? '已通过，限制已撤销' : '已驳回')
    auditVisible.value = false
    loadAppeals()
    loadPenalties()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.wrap { white-space: pre-wrap; word-break: break-all; }
</style>
