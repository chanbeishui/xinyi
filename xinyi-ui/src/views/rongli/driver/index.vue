<template>
  <RongliPage title="代驾员列表" subtitle="资格、接单状态、在手任务与个人对账集中管理。">
    <template #actions>
      <el-button @click="router.push('/rongli/people/qualification')">资格审核</el-button>
      <el-button type="primary" @click="router.push('/rongli/settlement/statement')">全部对账</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="query.keyword"
          placeholder="姓名 / 手机尾号 / 编号"
          clearable
          prefix-icon="Search"
          class="rl-w-260"
          @keyup.enter="handleQuery"
        />
        <el-select v-model="query.status" placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="全部" value="" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="复审中" value="SUBMITTED" />
          <el-option label="待审核" value="DRAFT" />
          <el-option label="未通过" value="REJECTED" />
          <el-option label="已到期" value="EXPIRED" />
        </el-select>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="代驾员" min-width="190">
          <template #default="{ row }">
            <div class="rl-person">
              <span class="rl-avatar">{{ initial(row) }}</span>
              <div>
                <div class="rl-strong">{{ row.nickname || '未设置昵称' }}</div>
                <div class="rl-mono">{{ driverCode(row) }} · {{ row.license_class || '准驾车型未登记' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="资格" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.qualification_status === 'APPROVED' ? 'rl-tag--green' : 'rl-tag--amber'">
              {{ qualificationLabel(row.qualification_status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="接单状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.listening_enabled && row.new_assignment_enabled ? 'rl-tag--blue' : 'rl-tag--gray'">
              {{ row.listening_enabled && row.new_assignment_enabled ? '听单中' : '暂停新单' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="在手任务" width="130">
          <template #default="{ row }">
            <span v-if="Number(row.active_assignments || 0) > 0" class="rl-tag rl-tag--amber">履约中</span>
            <span v-else class="rl-muted">无在手任务</span>
          </template>
        </el-table-column>
        <el-table-column label="累计服务" width="110">
          <template #default="{ row }">{{ Number(row.total_assignments || 0) }} 单</template>
        </el-table-column>
        <el-table-column label="评分" width="90">
          <template #default><span class="rl-muted">—</span></template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="goStatement(row)">对账</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">未找到匹配代驾</div>
            <div class="rl-empty__hint">请调整关键词或资格状态后重新查询。</div>
            <el-button @click="resetQuery">重置筛选</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="handleQuery"
        />
      </div>
    </section>

    <!-- 代驾员档案 -->
    <el-drawer v-model="detailVisible" title="代驾员档案" size="640px">
      <div class="profile-summary">
        <span class="rl-avatar rl-avatar--lg">{{ initial(detail) }}</span>
        <div class="profile-summary__main">
          <div class="rl-strong" style="font-size: 15px">{{ detail.nickname || '未设置昵称' }}</div>
          <div class="rl-sub">{{ driverCode(detail) }} · {{ detail.licenseClass || detail.license_class || '准驾车型未登记' }}</div>
        </div>
        <span class="rl-tag" :class="detail.qualificationStatus === 'APPROVED' || detail.qualification_status === 'APPROVED' ? 'rl-tag--green' : 'rl-tag--amber'">
          {{ qualificationLabel(detail.qualificationStatus || detail.qualification_status) }}
        </span>
      </div>

      <el-tabs v-model="tab">
        <el-tab-pane label="资料与资格" name="base">
          <div class="rl-kv">
            <div><span>手机号</span><b>{{ detail.phoneMasked || '未绑定' }}</b></div>
            <div><span>人员类型</span><b>{{ detail.employee ? '员工申请' : '普通用户' }}</b></div>
            <div><span>准驾车型</span><b>{{ detail.licenseClass || detail.license_class || '—' }}</b></div>
            <div><span>驾驶证有效期</span><b>{{ shortDate(detail.licenseValidTo || detail.license_valid_to) }}</b></div>
            <div><span>复审日期</span><b>{{ shortDate(detail.reviewDueAt || detail.review_due_at) }}</b></div>
            <div><span>累计服务</span><b>{{ Number(detail.totalAssignments || detail.total_assignments || 0) }} 单</b></div>
          </div>
          <el-divider content-position="left">接单控制</el-divider>
          <div class="rl-switch-row">
            <div>
              <div class="rl-strong">接受新邀约</div>
              <div class="rl-sub">关闭后仍需完成当前在手任务</div>
            </div>
            <el-switch
              :model-value="!!(detail.listeningEnabled ?? detail.listening_enabled)"
              @change="(val: any) => toggleNew(detail, !!val)"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane label="接送记录" name="assignments">
          <el-table :data="assignments" size="small">
            <el-table-column label="承接任务" width="120">
              <template #default="{ row }">{{ taskCode(row) }}</template>
            </el-table-column>
            <el-table-column label="车辆" min-width="130">
              <template #default="{ row }">{{ row.plateNo || row.plate_no || '—' }}</template>
            </el-table-column>
            <el-table-column label="任务结束" width="160">
              <template #default="{ row }">{{ shortTime(row.endedAt || row.ended_at) }}</template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">{{ row.endedAt || row.ended_at ? '已结束' : '进行中' }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!assignments.length" class="rl-empty"><div class="rl-empty__hint">暂无接送记录。</div></div>
        </el-tab-pane>
        <el-tab-pane label="个人对账" name="ledger">
          <div class="rl-kv">
            <div><span>待对账服务</span><b>{{ Number(ledger.pendingCount || 0) }} 条</b></div>
            <div><span>可核对金额</span><b>{{ money(ledger.payableFen) }}</b></div>
            <div><span>历史对账单</span><b>{{ Number(ledger.billCount || 0) }} 张</b></div>
            <div><span>已核对金额</span><b>{{ money(ledger.paidFen) }}</b></div>
          </div>
          <div style="margin-top: 16px">
            <el-button type="primary" @click="goStatement(detail)">进入个人对账</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goStatement(detail)">查看个人对账</el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliDriver" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { disableDriverNewAssignments, getDriver, getDriverFeeLedger, listDriverAssignments, listDrivers } from '@/api/rongli'
import { money, shortDate, shortTime } from '../format'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ keyword: '', status: '', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const tab = ref('base')
const detail = ref<any>({})
const assignments = ref<any[]>([])
const ledger = ref<any>({})

const QUALIFICATION: Record<string, string> = {
  DRAFT: '待完善',
  SUBMITTED: '待审核',
  APPROVED: '已通过',
  REJECTED: '未通过',
  EXPIRED: '已到期'
}

onMounted(load)

function qualificationLabel(value?: string) {
  return QUALIFICATION[value || ''] || value || '待审核'
}

function driverId(row: any) {
  return row.driverId ?? row.driver_id ?? row.id
}

function driverCode(row: any) {
  const id = driverId(row)
  return id ? 'D' + String(id).padStart(4, '0') : '—'
}

function taskCode(row: any) {
  const id = row.assignmentId ?? row.assignment_id ?? row.id
  return id ? 'ASG' + String(id).padStart(5, '0') : '—'
}

function initial(row: any) {
  const name = row.nickname || ''
  return name ? name.slice(0, 1) : '驾'
}

async function load() {
  loading.value = true
  try {
    const res: any = await listDrivers(query)
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

function resetQuery() {
  query.keyword = ''
  query.status = ''
  query.pageNum = 1
  load()
}

async function openDetail(row: any) {
  const id = driverId(row)
  if (!id) return
  tab.value = 'base'
  detail.value = { ...row }
  assignments.value = []
  ledger.value = {}
  detailVisible.value = true
  const [view, asg, led] = await Promise.all([
    getDriver(id).catch(() => null),
    listDriverAssignments(id).catch(() => null),
    getDriverFeeLedger(id).catch(() => null)
  ])
  if (view) detail.value = { ...detail.value, ...view }
  assignments.value = (asg && (asg.rows || asg)) || []
  ledger.value = led || {}
}

async function toggleNew(row: any, value: boolean) {
  const id = driverId(row)
  if (!id) return
  await disableDriverNewAssignments(id, !value)
  detail.value = { ...detail.value, listeningEnabled: value, listening_enabled: value }
  ElMessage.success('已更新接单状态')
  load()
}

function goStatement(row: any) {
  router.push({ path: '/rongli/settlement/statement', query: { driverId: String(driverId(row) || '') } })
}
</script>

<style scoped>
.rl-person { display: flex; align-items: center; gap: 10px; }
.rl-avatar {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  background: #eff8ff; color: #175cd3; font-size: 12px; flex: none;
}
.rl-avatar--lg { width: 40px; height: 40px; font-size: 15px; }
.profile-summary { display: flex; align-items: center; gap: 12px; padding: 0 0 16px; }
.profile-summary__main { flex: 1; }
.rl-switch-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0; border-bottom: 1px solid var(--rl-line-soft);
}
</style>
