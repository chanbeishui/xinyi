<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" class="mb12"
      title="仅维护站内消息模板、公开内容与客服配置（vi.biz.* 白名单）。第三方凭据、支付开关、外部短信/订阅模板请走 integrations（B49/B50）；固定安全项（支付上限/权限注册/水印）不可由普通配置修改。保存按版本 CAS，冲突须刷新。" />

    <div class="toolbar mb12">
      <el-tag type="info">配置版本：{{ version }}</el-tag>
      <el-button type="primary" plain :disabled="loading" @click="addRow">新增配置项</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      <el-button :disabled="loading" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows">
      <el-table-column label="配置键" min-width="260">
        <template #default="{ row }">
          <el-input v-if="row.isNew" v-model="row.key" placeholder="vi.biz.xxx" />
          <span v-else>{{ row.key }}</span>
          <el-tag v-if="row.protected" type="warning" size="small" class="ml8">固定安全项</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="配置值" min-width="360">
        <template #default="{ row }">
          <el-input v-model="row.value" type="textarea" :rows="2" :disabled="row.protected"
            :placeholder="row.protected ? '固定安全项不可修改' : '请输入配置值'" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row, $index }">
          <el-button v-if="row.isNew" link type="danger" @click="rows.splice($index, 1)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfig, putConfig, PROTECTED_CONFIG_KEYS, type ConfigItem } from '@/api/rongli/config'
import { isVersionConflict } from '@/api/rongli/viError'

interface Row extends ConfigItem { isNew: boolean; protected: boolean }

const loading = ref(false)
const saving = ref(false)
const version = ref(0)
const rows = ref<Row[]>([])

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
    const view = (await getConfig()).data
    version.value = view.version
    rows.value = view.items.map(it => ({ key: it.key, value: it.value, isNew: false, protected: PROTECTED_CONFIG_KEYS.includes(it.key) }))
  } finally {
    loading.value = false
  }
}

function addRow() {
  rows.value.push({ key: '', value: '', isNew: true, protected: false })
}

async function save() {
  const editable = rows.value.filter(r => !r.protected)
  const seen = new Set<string>()
  for (const r of editable) {
    const key = r.key.trim()
    if (!key) { ElMessage.warning('配置键不能为空'); return }
    if (!key.startsWith('vi.biz.')) { ElMessage.warning('仅允许 vi.biz.* 白名单键：' + key); return }
    if (PROTECTED_CONFIG_KEYS.includes(key)) { ElMessage.warning('固定安全项不可修改：' + key); return }
    if (seen.has(key)) { ElMessage.warning('配置键重复：' + key); return }
    seen.add(key)
  }
  const { value: reason } = await ElMessageBox.prompt('请填写本次配置变更原因', '保存配置', {
    confirmButtonText: '保存', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '原因必填'
  })
  const items: ConfigItem[] = editable.map(r => ({ key: r.key.trim(), value: r.value }))
  const idem = uuid()
  saving.value = true
  try {
    const ack = (await putConfig(items, version.value, reason, idem)).data
    ElMessage.success('保存成功，新版本 ' + (ack.version ?? ''))
    await load()
  } catch (e: any) {
    // 版本冲突：刷新取最新版本，不静默覆盖（拦截器已提示）
    if (isVersionConflict(e)) await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.ml8 { margin-left: 8px; }
.toolbar { display: flex; align-items: center; gap: 8px; }
</style>