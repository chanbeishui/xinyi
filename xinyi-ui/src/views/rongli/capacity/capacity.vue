<template>
  <div class="app-container">
    <el-alert title="每个站点独立维护上门接车和站点接收额度。上门时间不等于到站时间；停约或缩容不会取消已有预约。" type="info" :closable="false" />
    <div class="toolbar">
      <el-select v-model="stationFilter" clearable placeholder="全部可见站点" @change="pageNum = 1; load()">
        <el-option v-for="s in stations" :key="s.stationId" :label="s.name" :value="s.stationId" />
      </el-select>
      <el-button type="primary" @click="openCreate">新增时段</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" v-loading="loading">
      <el-table-column label="站点" min-width="140"><template #default="{ row }">{{ stationName(row.stationId) }}</template></el-table-column>
      <el-table-column label="额度类型" width="130"><template #default="{ row }">{{ kind(row.quotaType) }}</template></el-table-column>
      <el-table-column label="时间区间" min-width="290"><template #default="{ row }">{{ time(row.startAt) }} 至 {{ time(row.endAt) }}</template></el-table-column>
      <el-table-column v-for="c in columns" :key="c.key" :label="c.label" :prop="c.key" width="80" />
      <el-table-column label="状态" width="80"><template #default="{ row }">{{ row.enabled ? '可预约' : '停约' }}</template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" :disabled="Date.parse(row.startAt) <= Date.now()" @click="openChange(row)">调整额度</el-button></template></el-table-column>
    </el-table>
    <el-pagination :total="total" :current-page="pageNum" :page-size="20" layout="total, prev, pager, next" @current-change="p => { pageNum = p; load() }" />
    <el-dialog v-model="dialog" :title="editing ? '调整容量' : '新增供给时段'" width="min(560px, 96vw)">
      <el-form label-width="110px">
        <template v-if="!editing">
          <el-form-item label="归属站点" required><el-select v-model="form.stationId"><el-option v-for="s in stations.filter(s => s.enabled)" :key="s.stationId" :label="s.name" :value="s.stationId" /></el-select></el-form-item>
          <el-form-item label="额度类型" required><el-select v-model="form.quotaType"><el-option label="上门接车" value="PICKUP" /><el-option label="站点接收" value="STATION_RECEIVE" /></el-select></el-form-item>
          <el-form-item label="开始时间" required><el-date-picker v-model="form.start" type="datetime" placeholder="本地时间" /></el-form-item>
          <el-form-item label="结束时间" required><el-date-picker v-model="form.end" type="datetime" placeholder="本地时间" /></el-form-item>
        </template>
        <el-form-item label="总额度" required><el-input-number v-model="form.total" :min="0" :max="2147483647" :precision="0" /></el-form-item>
        <el-form-item v-if="editing" label="允许新预约"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item label="变更原因" required><el-input v-model="form.reason" type="textarea" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">{{ editing ? '预览影响' : '创建时段' }}</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listStations, type StationRecord } from '@/api/rongli/station'
import { listCapacity, createCapacity, changeCapacity, type Capacity, type CapacityChange } from '@/api/rongli/capacity'
const rows = ref<Capacity[]>([])
const stations = ref<StationRecord[]>([])
const stationFilter = ref<string>()
const total = ref(0), pageNum = ref(1), loading = ref(false), saving = ref(false), dialog = ref(false)
const editing = ref<Capacity | null>(null)
const form = reactive({ stationId: '', quotaType: 'PICKUP' as Capacity['quotaType'], start: null as Date | null, end: null as Date | null, total: 1, reason: '', enabled: true })
let attempt: { body: string; key: string } | null = null
let pendingChange: { intent: string; input: CapacityChange; key: string } | null = null
const columns = [{ key: 'total', label: '总量' }, { key: 'held', label: '预占' }, { key: 'confirmed', label: '正式' }, { key: 'consumed', label: '已用' }, { key: 'available', label: '可用' }]
const kind = (v: string) => v === 'PICKUP' ? '上门接车' : '站点接收'
const time = (v: string) => new Date(v).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
const stationName = (id: string) => stations.value.find(s => s.stationId === id)?.name || id
function key(body: object) {
  const json = JSON.stringify(body)
  if (!attempt || attempt.body !== json) attempt = { body: json, key: crypto.randomUUID() }
  return attempt.key
}
async function load() {
  loading.value = true
  try { const r = await listCapacity({ pageNum: pageNum.value, stationId: stationFilter.value }); rows.value = r.rows; total.value = r.total }
  finally { loading.value = false }
}
function openCreate() {
  editing.value = null; attempt = null; pendingChange = null
  Object.assign(form, { stationId: stationFilter.value || '', quotaType: 'PICKUP', start: null, end: null, total: 1, reason: '', enabled: true })
  dialog.value = true
}
function openChange(row: Capacity) {
  editing.value = row; attempt = null; pendingChange = null
  Object.assign(form, { total: row.total, enabled: row.enabled, reason: '' })
  dialog.value = true
}
async function save() {
  if (!form.reason.trim()) { ElMessage.warning('请填写变更原因'); return }
  saving.value = true
  try {
    if (editing.value) {
      const intent = JSON.stringify({ id: editing.value.slotId, total: form.total, enabled: form.enabled, reason: form.reason.trim() })
      if (!pendingChange || pendingChange.intent !== intent) {
        const preview = (await listCapacity({ slotId: editing.value.slotId, proposedTotal: form.total })).rows[0]
        if (!preview.canApply) { ElMessage.warning('额度不足以覆盖现有占用，或时段已开始'); return }
        await ElMessageBox.confirm(`预占 ${preview.held}，正式预约 ${preview.confirmed}，已消耗 ${preview.consumed}；总量 ${preview.total} → ${form.total}。${form.enabled ? '保留新预约入口。' : '停止新预约，保留已有占用。'}`, '容量调整影响预览', { type: 'warning' })
        const input = { total: form.total, enabled: form.enabled, reason: form.reason.trim(), expectedVersion: preview.version }
        pendingChange = { intent, input, key: crypto.randomUUID() }
      }
      await changeCapacity(editing.value.slotId, pendingChange.input, pendingChange.key)
    } else {
      if (!form.stationId || !form.start || !form.end) { ElMessage.warning('请选择站点及完整时间区间'); return }
      const input = { stationId: form.stationId, quotaType: form.quotaType, startAt: form.start.toISOString(), endAt: form.end.toISOString(), total: form.total, reason: form.reason.trim() }
      await createCapacity(input, key(input))
    }
    attempt = null; pendingChange = null; dialog.value = false; ElMessage.success('容量已保存'); await load()
  } catch (error) { if (error !== 'cancel' && error !== 'close') await load() }
  finally { saving.value = false }
}
onMounted(async () => { await load(); stations.value = (await listStations(1, 100)).rows })
</script>
<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; margin: 18px 0; }
.el-pagination { margin-top: 16px; }
</style>
