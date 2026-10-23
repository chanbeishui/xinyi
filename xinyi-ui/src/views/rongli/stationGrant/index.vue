<template>
  <div class="app-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="检测员工只能操作被授权的检测点。员工在小程序用公司登记的手机号授权登录后会自动识别身份；检测点与员工的绑定关系只在本页维护。"
      class="mb8"
    />

    <el-form :inline="true" class="mb8">
      <el-form-item>
        <el-input
          v-model="keyword"
          placeholder="按手机号 / 姓名 / 账号搜索员工"
          clearable
          style="width: 280px"
          @keyup.enter="loadCandidates"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="openCreate">新增绑定</el-button>
      </el-form-item>
      <el-form-item>
        <el-button @click="load">刷新列表</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="员工账号" prop="user_name" min-width="140" />
      <el-table-column label="姓名" prop="nick_name" width="120" />
      <el-table-column label="部门" prop="dept_name" min-width="160" show-overflow-tooltip />
      <el-table-column label="检测点" prop="station_name" min-width="160" />
      <el-table-column label="授权" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '已授权' : '已停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updated_at" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggle(row)">{{ row.enabled ? '取消授权' : '重新授权' }}</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增站点-员工绑定" width="560px">
      <el-form :model="form" label-width="96px">
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
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="员工需在小程序用公司登记的手机号完成微信授权，才能自动识别员工身份；仅配置检测点不会自动创建账号。"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确认绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliStationGrant" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listEmployeeCandidates, listStationGrants, listStations, updateStationGrant } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 20 })

const keyword = ref('')
const candidates = ref<any[]>([])
const candidateLoading = ref(false)
const stations = ref<any[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({ userId: '', stationId: '', reason: '' })

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
  } catch (e) {
    stations.value = []
  }
}

async function loadCandidates(kw?: string) {
  candidateLoading.value = true
  try {
    const res: any = await listEmployeeCandidates(kw === undefined ? keyword.value : kw)
    candidates.value = Array.isArray(res) ? res : []
  } catch (e) {
    candidates.value = []
  } finally {
    candidateLoading.value = false
  }
}

function openCreate() {
  form.userId = ''
  form.stationId = ''
  form.reason = ''
  dialogVisible.value = true
  if (!candidates.value.length) loadCandidates('')
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

async function toggle(row: any) {
  await updateStationGrant(row.employee_user_id || row.user_id, {
    stationId: row.station_id,
    enabled: !row.enabled
  })
  ElMessage.success('已更新授权')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.opt-sub { margin-left: 12px; color: var(--el-text-color-secondary); font-size: 12px; }
</style>
