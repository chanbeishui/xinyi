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
        <el-table-column label="接单状态" width="130">
          <template #default="{ row }">
            <span class="rl-tag" :class="listenState(row).cls">{{ listenState(row).text }}</span>
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
            <template v-if="loadError">
              <div class="rl-empty__title">代驾列表加载失败</div>
              <div class="rl-empty__hint">{{ loadError }}</div>
              <el-button @click="load">重新加载</el-button>
            </template>
            <template v-else>
              <div class="rl-empty__title">未找到匹配代驾</div>
              <div class="rl-empty__hint">请调整关键词或资格状态后重新查询。</div>
              <el-button @click="resetQuery">重置筛选</el-button>
            </template>
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
            <div><span>代驾编号</span><b>{{ driverCode(detail) }}</b></div>
            <div><span>手机号</span><b>{{ detail.phoneMasked || '未绑定' }}</b></div>
            <div><span>账号状态</span><b>{{ accountStatusLabel(detail.accountStatus) }}</b></div>
            <div><span>注册时间</span><b>{{ shortTime(detail.accountCreatedAt) }}</b></div>
            <div><span>归属部门</span><b>{{ detail.deptName || '全公司共享' }}</b></div>
            <div><span>建档时间</span><b>{{ shortTime(detail.createdAt) }}</b></div>
          </div>

          <el-divider content-position="left">资格与证件</el-divider>
          <div class="rl-kv">
            <div><span>资格状态</span><b>{{ qualificationLabel(detail.qualificationStatus) }}</b></div>
            <div><span>准驾车型</span><b>{{ detail.licenseClass || '未登记' }}</b></div>
            <div>
              <span>驾驶证有效期</span>
              <b>{{ detail.licenseLongTerm ? '长期有效' : shortDate(detail.licenseValidTo) }}</b>
            </div>
            <div><span>复审日期</span><b>{{ shortDate(detail.reviewDueAt) }}</b></div>
            <div><span>资格版本</span><b>第 {{ Number(detail.qualificationRevision || 0) }} 版</b></div>
            <div>
              <span>最近审核</span>
              <b>{{ detail.latestApplicationStatusLabel || '—' }}</b>
            </div>
            <div><span>提交时间</span><b>{{ shortTime(detail.latestApplicationSubmittedAt) }}</b></div>
            <div><span>审核时间</span><b>{{ shortTime(detail.latestApplicationReviewedAt) }}</b></div>
          </div>
          <div v-if="detail.latestApplicationOpinion" class="rl-note">
            审核意见：{{ detail.latestApplicationOpinion }}
          </div>

          <el-divider content-position="left">接单与派单</el-divider>
          <div class="rl-kv">
            <div>
              <span>派单资格</span>
              <b :class="detail.canReceive ? 'rl-ok' : 'rl-warn'">{{ detail.canReceive ? '可派单' : assignBlockReason(detail) }}</b>
            </div>
            <div><span>听单状态</span><b>{{ detail.listeningEnabled ? '已开启' : '已关闭' }}</b></div>
            <div><span>接受新邀约</span><b>{{ detail.newAssignmentEnabled ? '允许' : '运营已停派' }}</b></div>
            <div><span>在手任务</span><b>{{ Number(detail.activeAssignments || 0) }} 单</b></div>
            <div><span>累计服务</span><b>{{ Number(detail.totalAssignments || 0) }} 单</b></div>
            <div><span>最近完单</span><b>{{ shortTime(detail.lastAssignmentAt) }}</b></div>
            <div><span>最近定位</span><b>{{ shortTime(detail.lastLocationAt) }}</b></div>
            <div><span>派单依据</span><b>听单与接单开关决定能否派单；有定位按距离就近，无定位兜底</b></div>
          </div>

          <div v-if="detail.internalRemark" class="rl-note">内部备注：{{ detail.internalRemark }}</div>

          <el-divider content-position="left">接单控制</el-divider>
          <div class="rl-switch-row">
            <div>
              <div class="rl-strong">接受新邀约</div>
              <div class="rl-sub">关闭后仍需完成当前在手任务；听单开关由代驾在小程序首页控制</div>
            </div>
            <el-switch
              :model-value="!!Number(detail.newAssignmentEnabled ?? detail.new_assignment_enabled ?? 1)"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { disableDriverNewAssignments, getDriver, getDriverFeeLedger, listDriverAssignments, listDrivers } from '@/api/rongli'
import { money, shortDate, shortTime } from '../format'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const loadError = ref('')
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

/**
 * 接单状态必须分三档，不能只看「能不能接单」。
 * 资格通过后 listening_enabled 仍是 0（等代驾自己在小程序上线听单），
 * 原来一律显示「暂停新单」，看起来就像审核没生效。
 */
function listenState(row: any) {
  const newAssignment = Number(row.new_assignment_enabled ?? 1)
  const listening = Number(row.listening_enabled ?? 0)
  if (!newAssignment) return { text: '运营停派', cls: 'rl-tag--amber' }
  if (!listening) return { text: '未开启听单', cls: 'rl-tag--gray' }
  return { text: '听单中', cls: 'rl-tag--blue' }
}

/** 后端错误体统一在 response.data.msg，axios 只把状态码包成 message */
function errText(e: any) {
  return e?.response?.data?.msg || e?.message || '操作失败，请稍后重试'
}

function accountStatusLabel(status?: string) {
  if (!status) return '—'
  return status === 'ACTIVE' ? '正常' : status === 'DISABLED' ? '已停用' : status
}

/** 派单资格不满足时，直接说清卡在哪一条，运营不用再去猜 */
function assignBlockReason(row: any) {
  if ((row.qualificationStatus || row.qualification_status) !== 'APPROVED') return '资格未通过'
  if ((row.accountStatus || row.status) !== 'ACTIVE') return '账号已停用'
  if (!Number(row.newAssignmentEnabled ?? row.new_assignment_enabled ?? 1)) return '运营已停派'
  if (!Number(row.listeningEnabled ?? row.listening_enabled ?? 0)) return '代驾未开启听单'
  return '不可派单'
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
  loadError.value = ''
  try {
    const res: any = await listDrivers(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } catch (e: any) {
    // 原来是 try/finally，请求失败会静默留下空表，看起来像「没有数据」
    rows.value = []
    total.value = 0
    loadError.value = errText(e)
    ElMessage.error(`代驾列表加载失败：${loadError.value}`)
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
  const disabled = !value
  // 后端把 reason 当必填（要留痕，也会推送给代驾），不填直接 400。
  // 之前这里不弹窗、不带原因，所以每次拨动开关都报 Request failed with status code 400。
  let reason = ''
  try {
    const { value: input } = (await ElMessageBox.prompt(
      disabled
        ? '停派后该代驾不会再收到新邀约，在手任务不受影响。请填写原因，会记入档案并通知本人。'
        : '恢复后该代驾会重新进入派单池。请填写恢复原因，会记入档案。',
      disabled ? '暂停接收新单' : '恢复接收新单',
      {
        inputPlaceholder: disabled ? '例如：连续两单超时未接' : '例如：申诉成立，恢复派单',
        inputValidator: (v: string) => (v && v.trim() ? true : '原因不能为空'),
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )) as any
    reason = String(input || '').trim()
  } catch {
    return // 用户取消，不改状态
  }
  try {
    await disableDriverNewAssignments(id, disabled, reason)
    detail.value = {
      ...detail.value,
      newAssignmentEnabled: value,
      new_assignment_enabled: value ? 1 : 0
    }
    ElMessage.success(disabled ? '已暂停接收新单' : '已恢复接收新单')
    load()
  } catch (e: any) {
    ElMessage.error(errText(e))
  }
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
.rl-note {
  margin-top: 10px; padding: 10px 12px; border-radius: 6px;
  background: #f6f8fb; color: #475467; font-size: 12.5px; line-height: 1.6;
}
.rl-ok { color: #067647; }
.rl-warn { color: #b54708; }
</style>
