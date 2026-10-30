<template>
  <RongliPage title="员工检测点权限" subtitle="复用框架员工账号；所属站点与可操作站点分别记录。">
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <el-button @click="router.push(RL.driver)">员工绑定记录</el-button>
      <el-button type="primary" icon="Plus" @click="openCreate">新增绑定</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>员工与站点授权</h3>
        <span class="rl-muted">跨站授权可单独撤回</span>
      </div>

      <el-table v-loading="loading" :data="employees" :border="false">
        <el-table-column label="框架员工" min-width="190">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.nick_name || row.user_name }}</div>
            <div class="rl-mono">{{ row.user_name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="账号绑定" width="150">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.binding_count > 0 ? 'rl-tag--green' : 'rl-tag--gray'">
              {{ row.binding_count > 0 ? '已受控绑定' : '等待确认' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行政所属" min-width="160">
          <template #default="{ row }">{{ row.dept_name || '未分配部门' }}</template>
        </el-table-column>
        <el-table-column label="可操作站点" min-width="240">
          <template #default="{ row }">
            <template v-if="row.stations.length">
              <span
                v-for="s in row.stations"
                :key="s.stationId"
                class="rl-tag rl-tag--plain"
                :class="s.enabled ? 'rl-tag--blue' : 'rl-tag--gray'"
                style="margin-right: 6px"
              >
                {{ s.stationName }}
              </span>
            </template>
            <span v-else class="rl-tag rl-tag--plain rl-tag--amber">0 个授权</span>
          </template>
        </el-table-column>
        <el-table-column label="工作状态" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="activeCount(row) > 0 ? 'rl-tag--green' : 'rl-tag--amber'">
              {{ activeCount(row) > 0 ? '可工作' : '待开通' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openConfig(row)">配置授权</el-button>
            <el-button link type="primary" @click="openAddStation(row)">加站点</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">还没有站点授权</div>
            <div class="rl-empty__hint">
              员工在小程序用公司登记的手机号完成微信授权后会自动识别身份；检测点授权只在本页维护。
            </div>
            <el-button type="primary" @click="openCreate">新增绑定</el-button>
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

      <div class="rl-panel__body">
        <p class="rl-hint">
          检测员工只能操作被授权的检测点。仅配置检测点不会自动创建账号；员工需在小程序用公司登记的手机号完成微信授权才能识别身份。
        </p>
      </div>
    </section>

    <!-- 新增绑定 -->
    <el-drawer v-model="dialogVisible" title="新增站点-员工绑定" size="520px">
      <el-form label-position="top">
        <el-form-item label="员工">
          <el-select
            v-model="form.userId"
            filterable
            remote
            clearable
            reserve-keyword
            placeholder="输入手机号 / 姓名 / 账号搜索"
            :remote-method="loadCandidates"
            :loading="candidateLoading"
            style="width: 100%"
          >
            <el-option
              v-for="c in candidates"
              :key="c.userId"
              :label="`${c.nickName || c.userName}（${c.phoneMasked || '无手机号'}）`"
              :value="c.userId"
            >
              <span>{{ c.nickName || c.userName }}</span>
              <span class="opt-sub">{{ c.phoneMasked }} · {{ c.deptName || '未分配部门' }} · 已授权 {{ c.grantedStationCount }} 个</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="检测点">
          <el-select v-model="form.stationId" filterable placeholder="选择检测点" style="width: 100%">
            <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="String(s.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="授权说明">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="例如：新入职检测员工，需在茂名集信站上岗" />
        </el-form-item>
      </el-form>
      <p class="rl-hint rl-hint--warn">
        员工需在小程序用公司登记的手机号完成微信授权，才能自动识别员工身份；仅配置检测点不会自动创建账号。
      </p>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确认绑定</el-button>
      </template>
    </el-drawer>

    <!-- 配置授权 -->
    <el-drawer v-model="configVisible" title="配置站点授权" size="520px">
      <div class="rl-kv" style="margin-bottom: 12px">
        <div><span>员工</span><b>{{ currentEmployee.nick_name || currentEmployee.user_name }}</b></div>
        <div><span>行政所属</span><b>{{ currentEmployee.dept_name || '未分配部门' }}</b></div>
      </div>
      <el-table :data="currentEmployee.stations || []" :border="false">
        <el-table-column label="检测点" prop="stationName" min-width="160" />
        <el-table-column label="授权" width="120">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.enabled ? 'rl-tag--green' : 'rl-tag--gray'">
              {{ row.enabled ? '可操作' : '已停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link type="primary" @click="toggle(currentEmployee, row)">
              {{ row.enabled ? '撤回' : '重新授权' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">还没有站点授权</div>
            <div class="rl-empty__hint">为该员工添加至少一个检测点后，才可在小程序上岗。</div>
          </div>
        </template>
      </el-table>
      <p class="rl-hint">可操作范围与功能权限共同生效；工作授权撤回不注销车主身份。</p>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliStationGrant" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listEmployeeCandidates, listStationGrants, listStations, updateStationGrant } from '@/api/rongli'
import { RL } from '../routes'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 20 })

const candidates = ref<any[]>([])
const candidateLoading = ref(false)
const stations = ref<any[]>([])
const dialogVisible = ref(false)
const configVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({ userId: '', stationId: '', reason: '' })
const currentEmployee = ref<any>({})

/** 按员工聚合：一个员工一行，可操作站点以标签呈现 */
const employees = computed(() => {
  const map = new Map<string, any>()
  for (const r of rows.value) {
    const key = String(r.sys_user_id)
    if (!map.has(key)) {
      map.set(key, {
        sys_user_id: key,
        user_name: r.user_name,
        nick_name: r.nick_name,
        dept_name: r.dept_name,
        binding_count: Number(r.binding_count || 0),
        stations: []
      })
    }
    map.get(key).stations.push({
      stationId: String(r.station_id),
      stationName: r.station_name,
      enabled: !!r.enabled
    })
  }
  return Array.from(map.values())
})

onMounted(async () => {
  await load()
  await loadStations()
  await loadCandidates('')
})

async function load() {
  loading.value = true
  try {
    const res: any = await listStationGrants(query)
    rows.value = (res && res.rows) || []
    total.value = rows.value.length
  } finally {
    loading.value = false
  }
}

async function loadStations() {
  try {
    const res: any = await listStations({ pageNum: 1, pageSize: 100 })
    stations.value = (res && res.rows) || []
  } catch {
    stations.value = []
  }
}

async function loadCandidates(kw?: string) {
  candidateLoading.value = true
  try {
    const res: any = await listEmployeeCandidates(kw === undefined ? '' : kw)
    candidates.value = Array.isArray(res) ? res : []
  } catch {
    candidates.value = []
  } finally {
    candidateLoading.value = false
  }
}

function activeCount(row: any) {
  return (row.stations || []).filter((s: any) => s.enabled).length
}

function openCreate() {
  form.userId = ''
  form.stationId = ''
  form.reason = ''
  dialogVisible.value = true
  if (!candidates.value.length) loadCandidates('')
}

function openConfig(row: any) {
  currentEmployee.value = row
  configVisible.value = true
}

function openAddStation(row: any) {
  openCreate()
  form.userId = row.sys_user_id
  const taken = new Set((row.stations || []).map((s: any) => s.stationId))
  const free = stations.value.find((s: any) => !taken.has(String(s.id)))
  form.stationId = free ? String(free.id) : ''
}

async function submit() {
  if (!form.userId || !form.stationId) {
    ElMessage.warning('请选择员工与检测点')
    return
  }
  saving.value = true
  try {
    await updateStationGrant(form.userId, {
      stationId: form.stationId,
      enabled: true,
      reason: form.reason || '后台新增站点授权'
    })
    ElMessage.success('已新增绑定，员工重新打开小程序即可看到该检测点')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggle(employee: any, station: any) {
  await updateStationGrant(employee.sys_user_id, {
    stationId: station.stationId,
    enabled: !station.enabled
  })
  ElMessage.success(station.enabled ? '已撤回授权' : '已重新授权')
  configVisible.value = false
  load()
}
</script>

<style scoped>
.opt-sub {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
