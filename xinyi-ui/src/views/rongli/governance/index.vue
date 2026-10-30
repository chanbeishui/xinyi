<template>
  <RongliPage title="敏感访问与导出审批" subtitle="申请说明用途与范围；批准后下载仍需校验当时权限。">
    <template #actions>
      <el-button icon="Refresh" @click="loadAll">刷新</el-button>
      <el-button type="primary" icon="Plus" @click="openExportApply">新建申请</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>授权申请</h3>
        <span class="rl-muted">导出数据一律脱敏</span>
      </div>

      <div class="rl-filters">
        <el-select v-model="exportQuery.status" placeholder="全部状态" style="width: 150px" @change="loadExports">
          <el-option label="全部状态" value="" />
          <el-option label="已申请" value="APPLIED" />
          <el-option label="已批准" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
        <span class="rl-muted">批准后 24 小时内可查看，超时自动失效</span>
      </div>

      <el-table v-loading="loading" :data="exportRows" :border="false">
        <el-table-column label="申请号 / 类型" min-width="190">
          <template #default="{ row }">
            <div class="rl-strong">EXPORT-{{ row.caseId }}</div>
            <div class="rl-sub">订单字段导出</div>
          </template>
        </el-table-column>
        <el-table-column label="用途与范围" min-width="240">
          <template #default="{ row }">
            <span class="rl-cell-sub">{{ row.reason || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">{{ row.requesterUserId || '—' }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="150">
          <template #default="{ row }">
            <span v-if="row.status === 'APPROVED'">至 {{ shortTime(row.expiresAt) }}</span>
            <span v-else class="rl-muted">按批准时限</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="exportClass(row.status)">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 'APPLIED'" @click="openExportAudit(row)">
              审查授权
            </el-button>
            <el-button link type="primary" :disabled="row.status !== 'APPROVED'" @click="viewExport(row)">
              {{ row.status === 'APPROVED' ? '下载演示文件' : '查看数据' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有授权申请</div>
            <div class="rl-empty__hint">申请说明用途与范围，批准后下载仍需校验当时权限。</div>
            <el-button type="primary" @click="openExportApply">新建申请</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="exportQuery.pageNum"
          v-model:page-size="exportQuery.pageSize"
          :total="exportTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="loadExports"
          @size-change="loadExports"
        />
      </div>

      <div class="rl-panel__body">
        <p class="rl-hint rl-hint--warn">
          导出数据一律脱敏：车牌保留前后各 2 位，联系人只保留姓氏；不导出完整证件、密钥或交接码。
        </p>
      </div>
    </section>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>隐私与数据请求</h3>
        <span class="rl-muted">按处理依据回复，不直接删除必要业务记录</span>
      </div>

      <div class="rl-filters">
        <el-select v-model="privacyQuery.status" placeholder="全部状态" style="width: 150px" @change="loadPrivacy">
          <el-option label="全部状态" value="" />
          <el-option label="已提交" value="APPLIED" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已处理" value="COMPLETED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </div>

      <el-table :data="privacyRows" :border="false">
        <el-table-column label="案件号" width="110">
          <template #default="{ row }">
            <span class="rl-mono">CASE-{{ row.caseId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联订单" width="170">
          <template #default="{ row }"><span class="rl-mono">{{ row.orderNo || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="请求内容" min-width="240">
          <template #default="{ row }"><span class="rl-cell-sub">{{ row.reason || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="请求账号" width="100">
          <template #default="{ row }">{{ row.requesterAccountId || '—' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="150">
          <template #default="{ row }">{{ shortTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="privacyClass(row.status)">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status === 'COMPLETED' || row.status === 'REJECTED'"
              @click="openPrivacy(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有隐私请求</div>
            <div class="rl-empty__hint">对应 U03 人工受理闭环，按处理依据回复。</div>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="privacyQuery.pageNum"
          v-model:page-size="privacyQuery.pageSize"
          :total="privacyTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPrivacy"
          @size-change="loadPrivacy"
        />
      </div>
    </section>

    <!-- 隐私处理 -->
    <el-drawer v-model="privacyVisible" title="处理隐私请求" size="480px">
      <div class="rl-kv" style="margin-bottom: 12px">
        <div><span>案件号</span><b>CASE-{{ privacyForm.caseId }}</b></div>
      </div>
      <el-form label-position="top">
        <el-form-item label="处理结果">
          <el-select v-model="privacyForm.status" style="width: 100%">
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已处理" value="COMPLETED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="privacyForm.conclusion" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="privacyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPrivacy">提交</el-button>
      </template>
    </el-drawer>

    <!-- 导出审批 -->
    <el-drawer v-model="exportAuditVisible" title="审查导出授权" size="480px">
      <div class="rl-kv" style="margin-bottom: 12px">
        <div><span>任务号</span><b>EXPORT-{{ exportForm.caseId }}</b></div>
      </div>
      <el-form label-position="top">
        <el-form-item label="审批结果">
          <el-radio-group v-model="exportForm.approve">
            <el-radio :value="true">批准</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="exportForm.opinion" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <p class="rl-hint">批准后下载仍需校验当时权限，超时自动失效。</p>
      <template #footer>
        <el-button @click="exportAuditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExportAudit">提交</el-button>
      </template>
    </el-drawer>

    <!-- 申请导出 -->
    <el-drawer v-model="exportApplyVisible" title="申请敏感访问 / 导出" size="520px">
      <el-form label-position="top">
        <el-form-item label="申请类型">
          <el-input model-value="订单字段导出" disabled />
        </el-form-item>
        <el-form-item label="申请用途" required>
          <el-input
            v-model="exportApply.reason"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="说明为何必要，避免批量超范围索取"
          />
        </el-form-item>
        <el-form-item label="业务范围">
          <div class="rl-date-range">
            <el-date-picker v-model="exportApply.from" type="date" value-format="YYYY-MM-DD" placeholder="起始日期" />
            <span class="rl-muted">至</span>
            <el-date-picker v-model="exportApply.to" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
          </div>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-input v-model="exportApply.status" placeholder="留空表示全部，例如 COMPLETED" />
        </el-form-item>
      </el-form>
      <div class="rl-kv">
        <div><span>拟导出字段</span><b>订单号 / 车辆、服务状态 / 站点、支付金额（无支付秘密）</b></div>
      </div>
      <p class="rl-hint rl-hint--warn">不导出完整证件、密钥或交接码。下载前再次校验范围与有效期。</p>
      <template #footer>
        <el-button @click="exportApplyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExportApply">提交授权申请</el-button>
      </template>
    </el-drawer>

    <!-- 导出数据预览 -->
    <el-dialog v-model="exportViewVisible" title="导出数据（已脱敏）" width="880px">
      <el-alert
        v-if="exportView.maskingRule"
        :title="exportView.maskingRule"
        type="info"
        :closable="false"
        show-icon
        class="mb12"
      />
      <el-table :data="exportView.rows || []" :border="false" max-height="420">
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="车牌" prop="plateNo" width="120" />
        <el-table-column label="车主" prop="ownerName" width="100" />
        <el-table-column label="状态" prop="fulfillmentStatus" width="140" />
        <el-table-column label="上门时间" width="160">
          <template #default="{ row }">{{ shortTime(row.pickupStartsAt) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalFen) }}</template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有可导出数据</div>
            <div class="rl-empty__hint">检查申请范围与有效期后重试。</div>
          </div>
        </template>
      </el-table>
      <div class="rl-sub mt8">
        共 {{ exportView.rowCount || 0 }} 条，数据有效期至 {{ shortTime(exportView.expiresAt) }}
      </div>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliGovernance" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  auditExport,
  getExportTask,
  listExports,
  listPrivacyCases,
  requestExport,
  resolvePrivacyCase
} from '@/api/rongli'
import { money, shortTime } from '../format'

const loading = ref(false)
const saving = ref(false)

const privacyRows = ref<any[]>([])
const privacyTotal = ref(0)
const privacyQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const privacyVisible = ref(false)
const privacyForm = reactive<any>({ caseId: '', status: 'COMPLETED', conclusion: '' })

const exportRows = ref<any[]>([])
const exportTotal = ref(0)
const exportQuery = reactive<any>({ status: '', pageNum: 1, pageSize: 10 })
const exportAuditVisible = ref(false)
const exportForm = reactive<any>({ caseId: '', approve: true, opinion: '' })
const exportApplyVisible = ref(false)
const exportApply = reactive<any>({ from: '', to: '', status: '', reason: '' })
const exportViewVisible = ref(false)
const exportView = ref<any>({})

onMounted(loadAll)

async function loadAll() {
  await Promise.all([loadPrivacy(), loadExports()])
}

function exportClass(status: string) {
  if (status === 'APPROVED') return 'rl-tag--green'
  if (status === 'REJECTED' || status === 'EXPIRED') return 'rl-tag--gray'
  return 'rl-tag--amber'
}

function privacyClass(status: string) {
  if (status === 'COMPLETED') return 'rl-tag--green'
  if (status === 'REJECTED') return 'rl-tag--gray'
  return 'rl-tag--amber'
}

async function loadPrivacy() {
  const res: any = await listPrivacyCases(privacyQuery).catch(() => null)
  privacyRows.value = (res && res.rows) || []
  privacyTotal.value = Number((res && res.total) || 0)
}

function openPrivacy(row: any) {
  privacyForm.caseId = row.caseId
  privacyForm.status = row.status === 'APPLIED' ? 'PROCESSING' : 'COMPLETED'
  privacyForm.conclusion = ''
  privacyVisible.value = true
}

async function submitPrivacy() {
  saving.value = true
  try {
    await resolvePrivacyCase(privacyForm.caseId, {
      status: privacyForm.status,
      conclusion: privacyForm.conclusion
    })
    ElMessage.success('已提交处理结果')
    privacyVisible.value = false
    loadPrivacy()
  } finally {
    saving.value = false
  }
}

async function loadExports() {
  loading.value = true
  try {
    const res: any = await listExports(exportQuery)
    exportRows.value = (res && res.rows) || []
    exportTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function openExportAudit(row: any) {
  exportForm.caseId = row.caseId
  exportForm.approve = true
  exportForm.opinion = ''
  exportAuditVisible.value = true
}

async function submitExportAudit() {
  saving.value = true
  try {
    await auditExport(exportForm.caseId, { approve: exportForm.approve, opinion: exportForm.opinion })
    ElMessage.success(exportForm.approve ? '已批准' : '已驳回')
    exportAuditVisible.value = false
    loadExports()
  } finally {
    saving.value = false
  }
}

function openExportApply() {
  exportApply.from = ''
  exportApply.to = ''
  exportApply.status = ''
  exportApply.reason = ''
  exportApplyVisible.value = true
}

async function submitExportApply() {
  if (!exportApply.reason) {
    ElMessage.warning('请填写申请用途')
    return
  }
  saving.value = true
  try {
    await requestExport({ ...exportApply })
    ElMessage.success('已提交，等待另一位管理员审批')
    exportApplyVisible.value = false
    loadExports()
  } finally {
    saving.value = false
  }
}

async function viewExport(row: any) {
  const res: any = await getExportTask(row.caseId)
  exportView.value = res || {}
  exportViewVisible.value = true
}
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}

.rl-date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
