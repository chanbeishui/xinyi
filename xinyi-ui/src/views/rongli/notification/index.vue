<template>
  <div class="app-container">
    <el-tabs v-model="active" @tab-change="onTabChange">
      <el-tab-pane label="通知模板" name="template">
        <el-alert
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="模板变量用 {} 包裹，保存后仅影响之后产生的消息，历史消息内容不变。"
        />

        <el-table v-loading="loading" :data="templates" border>
          <el-table-column label="场景" prop="scene" width="200" />
          <el-table-column label="内容" min-width="420">
            <template #default="{ row }">
              <el-input v-model="row.content" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </template>
          </el-table-column>
          <el-table-column label="可用变量" min-width="180">
            <template #default="{ row }">
              <el-tag v-for="v in row.variables || []" :key="v" size="small" class="tag-space">{{ '{' + v + '}' }}</el-tag>
              <span v-if="!row.variables || !row.variables.length" class="muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.customized ? 'success' : 'info'">{{ row.customized ? '已自定义' : '默认模板' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :loading="saving" @click="saveTemplate(row)">保存</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="通知发送与重试" name="job">
        <el-form :inline="true" class="mb8">
          <el-form-item>
            <el-button icon="Refresh" @click="loadJobs">刷新</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="通知与事件写入 outbox 后由后台任务投递；失败会自动退避重试，也可在此手工重试单条。"
        />

        <el-table v-loading="loading" :data="jobRows" border>
          <el-table-column label="任务号" prop="taskId" width="90" />
          <el-table-column label="事件键" prop="eventKey" width="220" show-overflow-tooltip />
          <el-table-column label="事件类型" prop="eventType" width="190" show-overflow-tooltip />
          <el-table-column label="聚合" prop="aggregateType" width="110" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'DONE' || row.status === 'SENT' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="重试次数" prop="attempts" width="100" align="right" />
          <el-table-column label="下次重试" prop="nextAttemptAt" width="180" />
          <el-table-column label="创建时间" prop="createdAt" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 'DONE' || row.status === 'SENT'" @click="retry(row)">
                重试
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="jobQuery.pageNum"
          v-model:page-size="jobQuery.pageSize"
          :total="jobTotal"
          layout="total, sizes, prev, pager, next"
          class="mt16"
          @current-change="loadJobs"
          @size-change="loadJobs"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="RongliNotification" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listJobs, listNotificationTemplates, retryJob, updateNotificationTemplate } from '@/api/rongli'

const active = ref('template')
const loading = ref(false)
const saving = ref(false)

const templates = ref<any[]>([])

const jobRows = ref<any[]>([])
const jobTotal = ref(0)
const jobQuery = reactive<any>({ pageNum: 1, pageSize: 20 })

onMounted(() => {
  loadTemplates()
})

function onTabChange(name: string) {
  if (name === 'template') loadTemplates()
  if (name === 'job') loadJobs()
}

async function loadTemplates() {
  loading.value = true
  try {
    const res: any = await listNotificationTemplates()
    templates.value = (res && res.rows) || []
  } finally {
    loading.value = false
  }
}

async function saveTemplate(row: any) {
  if (!row.content || !String(row.content).trim()) {
    ElMessage.warning('模板内容不能为空')
    return
  }
  saving.value = true
  try {
    await updateNotificationTemplate(row.templateId, { content: row.content })
    ElMessage.success('已保存')
    loadTemplates()
  } finally {
    saving.value = false
  }
}

async function loadJobs() {
  loading.value = true
  try {
    const res: any = await listJobs(jobQuery)
    jobRows.value = (res && res.rows) || []
    jobTotal.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

async function retry(row: any) {
  saving.value = true
  try {
    await retryJob(row.taskId)
    ElMessage.success('已提交重试')
    loadJobs()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.muted { color: #909399; font-size: 12px; }
.tag-space { margin-right: 6px; margin-bottom: 4px; }
</style>
