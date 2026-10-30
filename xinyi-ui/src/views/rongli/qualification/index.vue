<template>
  <RongliPage
    title="代驾资格审核"
    subtitle="普通申请人和检测员工使用同一标准；无资格不得接单。"
  >
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-select v-model="query.status" placeholder="全部状态" style="width: 160px" @change="handleQuery">
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="SUBMITTED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
        <span class="rl-muted">完整申请与复审合并查看</span>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="申请人" min-width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.nickname || '—' }}</div>
            <div class="rl-sub">代驾编号 {{ row.driverId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="核验资料" min-width="220">
          <template #default="{ row }">
            <div>准驾车型 {{ row.licenseClass || '—' }}</div>
            <div class="rl-sub">证件有效期 {{ row.licenseValidTo || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="最终审核" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ shortTime(row.submittedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReview(row)">
              {{ row.status === 'SUBMITTED' ? '审查申请' : '查看' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有待审核的资格申请</div>
            <div class="rl-empty__hint">小程序提交的首次申请与年度复审都会进入这里。</div>
            <el-button @click="query.status = ''; handleQuery()">查看全部</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="审核代驾申请" size="560px">
      <div class="rl-detail-strip">
        <div><span>申请人</span><b>{{ current.nickname || current.driverName || '—' }}</b></div>
        <div><span>准驾车型</span><b>{{ current.licenseClass || '—' }}</b></div>
        <div><span>证件有效期</span><b>{{ current.licenseValidTo || '—' }}</b></div>
        <div><span>当前状态</span><b>{{ statusLabel(current.status) }}</b></div>
      </div>

      <div class="rl-kv" style="margin-bottom: 14px">
        <div><span>真实姓名（脱敏）</span><b>{{ current.sensitive?.realName || '—' }}</b></div>
        <div><span>身份证号（脱敏）</span><b>{{ current.sensitive?.idCardNo || '—' }}</b></div>
        <div><span>资格状态</span><b>{{ current.qualificationStatus || '—' }}</b></div>
        <div><span>提交时间</span><b>{{ shortTime(current.submittedAt) }}</b></div>
      </div>

      <div class="rl-panel">
        <div class="rl-panel__head"><h3>实际核验要求</h3></div>
        <div class="rl-panel__body">
          <div class="rl-check-row">
            <div>
              <div class="rl-strong">实名与驾驶证</div>
              <div class="rl-sub">证件材料与档案信息一致性</div>
            </div>
            <span class="rl-tag rl-tag--plain" :class="current.sensitive ? 'rl-tag--blue' : 'rl-tag--amber'">
              {{ current.sensitive ? '已见脱敏信息' : '待补资料' }}
            </span>
          </div>
          <div class="rl-check-row">
            <div>
              <div class="rl-strong">活体、背景与培训</div>
              <div class="rl-sub">正式结果必须有可信来源，本地环境为演示数据</div>
            </div>
            <span class="rl-tag rl-tag--plain rl-tag--gray">演示数据</span>
          </div>
          <div class="rl-check-row">
            <div>
              <div class="rl-strong">培训记录</div>
              <div class="rl-sub">已核验学时 {{ trainingSeconds }} 秒</div>
            </div>
            <span class="rl-tag rl-tag--plain" :class="trainingSeconds > 0 ? 'rl-tag--green' : 'rl-tag--amber'">
              {{ trainingSeconds > 0 ? '已有学习记录' : '未开始' }}
            </span>
          </div>
          <p class="rl-hint">{{ current.sensitiveHint || '管理端只展示脱敏信息，原件在独立档案中受控访问。' }}</p>
        </div>
      </div>

      <el-form label-position="top" class="mt16">
        <el-form-item label="审核意见">
          <el-input
            v-model="opinion"
            type="textarea"
            :rows="3"
            placeholder="批准或驳回均填写具体意见"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="checked">已核对所有必需资料及核验依据</el-checkbox>
        </el-form-item>
      </el-form>

      <p class="rl-hint rl-hint--warn">
        本地演示审批不构成真实驾驶资格审核通过；正式环境需接入可信核验来源。
      </p>

      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button
          type="danger"
          :disabled="!canAudit"
          :loading="saving"
          @click="submit(false)"
        >
          驳回补充
        </el-button>
        <el-button type="primary" :disabled="!canAudit || !checked" :loading="saving" @click="submit(true)">
          批准资格
        </el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliQualification" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditDriverQualification, getDriverQualification, listDriverQualifications } from '@/api/rongli'
import { shortTime } from '../format'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: 'SUBMITTED', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const current = ref<any>({})
const opinion = ref('')
const checked = ref(false)

const canAudit = computed(() => current.value.status === 'SUBMITTED')
const trainingSeconds = computed(() => {
  const list = current.value.training || []
  return list.reduce((sum: number, t: any) => sum + Number(t.verifiedSeconds || 0), 0)
})

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '草稿',
  SUBMITTED: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  EXPIRED: '已过期',
  SUSPENDED: '已暂停'
}

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

function statusLabel(code?: string) {
  if (!code) return '—'
  return STATUS_LABEL[code] || code
}

function statusClass(code?: string) {
  if (code === 'APPROVED') return 'rl-tag--green'
  if (code === 'REJECTED' || code === 'EXPIRED') return 'rl-tag--gray'
  return 'rl-tag--amber'
}

async function openReview(row: any) {
  current.value = await getDriverQualification(row.applicationId)
  opinion.value = current.value.reviewOpinion || ''
  checked.value = false
  detailVisible.value = true
}

async function submit(approve: boolean) {
  if (!opinion.value.trim()) {
    ElMessage.warning('批准或驳回均需填写具体意见')
    return
  }
  saving.value = true
  try {
    await auditDriverQualification(current.value.applicationId, { approve, opinion: opinion.value })
    ElMessage.success(approve ? '资格已通过' : '已驳回并要求补充')
    detailVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.rl-check-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid var(--rl-line-soft);
}

.rl-check-row:last-of-type {
  border-bottom: 0;
}
</style>
