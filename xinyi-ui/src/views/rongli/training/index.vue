<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px" @change="load">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Plus" @click="openCreate">新建课程</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="课程按版本管理：已发布课程不可修改（学时与题库已对代驾生效），需要调整请新建同 courseKey 的新版本。"
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="ID" prop="trainingId" width="80" />
      <el-table-column label="课程编码" prop="courseKey" width="200" show-overflow-tooltip />
      <el-table-column label="版本" prop="courseVersion" width="80" />
      <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="要求学时" width="120">
        <template #default="{ row }">{{ Math.round(Number(row.requiredDurationSeconds || 0) / 60) }} 分钟</template>
      </el-table-column>
      <el-table-column label="及格分" prop="passScore" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'DRAFT' ? 'warning' : 'info'">
            {{ row.statusLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="publishedAt" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status !== 'DRAFT'" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" :disabled="row.status !== 'DRAFT'" @click="publish(row)">发布</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.trainingId ? '编辑课程' : '新建课程'" width="820px" top="6vh">
      <el-form label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="课程编码">
              <el-input v-model="form.courseKey" placeholder="留空自动生成，例如 SERVICE_BASIC" :disabled="!!form.trainingId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程标题" required>
              <el-input v-model="form.title" maxlength="120" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="要求学时(秒)" required>
              <el-input-number v-model="form.requiredDurationSeconds" :min="30" :step="30" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格分">
              <el-input-number v-model="form.passScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试时限(秒)">
              <el-input-number v-model="form.attemptTimeLimitSeconds" :min="60" :step="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="视频文件 ID">
              <el-input v-model="form.mediaFileId" placeholder="可选，培训视频的媒体文件 ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程说明">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">题库（选项一行一个，正确答案填 A/B/C，多选可用 A,B）</el-divider>

      <div v-for="(q, i) in form.questions" :key="i" class="question-block">
        <el-row :gutter="8">
          <el-col :span="4">
            <el-input v-model="q.questionCode" placeholder="题号" />
          </el-col>
          <el-col :span="6">
            <el-select v-model="q.questionType" style="width: 100%">
              <el-option label="单选" value="SINGLE" />
              <el-option label="多选" value="MULTIPLE" />
              <el-option label="判断" value="TRUE_FALSE" />
            </el-select>
          </el-col>
          <el-col :span="10">
            <el-input v-model="q.prompt" placeholder="题干" />
          </el-col>
          <el-col :span="4">
            <el-button type="danger" plain @click="removeQuestion(i)">删除</el-button>
          </el-col>
        </el-row>
        <el-row :gutter="8" class="mt8">
          <el-col :span="16">
            <el-input v-model="q.optionsText" type="textarea" :rows="3" placeholder="选项，一行一个&#10;合格&#10;不合格" />
          </el-col>
          <el-col :span="8">
            <el-input v-model="q.correctOptionCodes" placeholder="正确答案，如 A" />
            <div class="muted mt8">按选项顺序自动编号 A/B/C/D</div>
          </el-col>
        </el-row>
      </div>

      <el-button class="mt8" icon="Plus" @click="addQuestion">添加题目</el-button>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliTraining" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createTrainingCourse,
  getTrainingCourse,
  listTrainingCourses,
  publishTrainingCourse,
  updateTrainingCourse
} from '@/api/rongli'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 20 })

const dialogVisible = ref(false)
const form = reactive<any>({
  trainingId: '',
  courseKey: '',
  title: '',
  description: '',
  mediaFileId: '',
  requiredDurationSeconds: 300,
  passScore: 80,
  attemptTimeLimitSeconds: 1800,
  questions: [] as any[]
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const params: any = { ...query }
    if (!params.status) delete params.status
    const res: any = await listTrainingCourses(params)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.trainingId = ''
  form.courseKey = ''
  form.title = ''
  form.description = ''
  form.mediaFileId = ''
  form.requiredDurationSeconds = 300
  form.passScore = 80
  form.attemptTimeLimitSeconds = 1800
  form.questions = []
}

function openCreate() {
  resetForm()
  addQuestion()
  dialogVisible.value = true
}

async function openEdit(row: any) {
  const detail: any = await getTrainingCourse(row.trainingId)
  resetForm()
  form.trainingId = detail.trainingId
  form.courseKey = detail.courseKey
  form.title = detail.title
  form.description = detail.description
  form.mediaFileId = detail.mediaFileId || ''
  form.requiredDurationSeconds = Number(detail.requiredDurationSeconds || 300)
  form.passScore = Number(detail.passScore || 80)
  form.attemptTimeLimitSeconds = Number(detail.attemptTimeLimitSeconds || 1800)
  form.questions = (detail.questions || []).map((q: any) => ({
    questionCode: q.questionCode,
    questionType: q.questionType || 'SINGLE',
    prompt: q.prompt,
    optionsText: (q.options || []).map((o: any) => (typeof o === 'string' ? o : o.text)).join('\n'),
    correctOptionCodes: q.correctOptionCodes || 'A',
    sortNo: q.sortNo
  }))
  if (!form.questions.length) addQuestion()
  dialogVisible.value = true
}

function addQuestion() {
  form.questions.push({
    questionCode: 'Q' + (form.questions.length + 1),
    questionType: 'SINGLE',
    prompt: '',
    optionsText: '',
    correctOptionCodes: 'A'
  })
}

function removeQuestion(index: number) {
  form.questions.splice(index, 1)
}

async function submit() {
  if (!form.title) {
    ElMessage.warning('请填写课程标题')
    return
  }
  const payload: any = {
    courseKey: form.courseKey || undefined,
    title: form.title,
    description: form.description,
    mediaFileId: form.mediaFileId ? Number(form.mediaFileId) : null,
    requiredDurationSeconds: Number(form.requiredDurationSeconds),
    passScore: Number(form.passScore),
    attemptTimeLimitSeconds: Number(form.attemptTimeLimitSeconds),
    questions: form.questions
      .filter((q: any) => q.prompt && q.correctOptionCodes)
      .map((q: any, i: number) => ({
        questionCode: q.questionCode || 'Q' + (i + 1),
        questionType: q.questionType,
        prompt: q.prompt,
        options: String(q.optionsText || '')
          .split('\n')
          .map((s) => s.trim())
          .filter((s) => !!s),
        correctOptionCodes: q.correctOptionCodes,
        sortNo: i + 1
      }))
  }
  saving.value = true
  try {
    if (form.trainingId) {
      await updateTrainingCourse(form.trainingId, payload)
    } else {
      await createTrainingCourse(payload)
    }
    ElMessage.success('已保存草稿')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function publish(row: any) {
  await ElMessageBox.confirm(
    '发布后该课程学时与题库对代驾生效，且不可再修改。确认发布？',
    '发布课程',
    { type: 'warning' }
  )
  await publishTrainingCourse(row.trainingId)
  ElMessage.success('已发布')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt8 { margin-top: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
.muted { color: #909399; font-size: 12px; }
.question-block { border: 1px dashed #dcdfe6; border-radius: 6px; padding: 10px; margin-bottom: 10px; }
</style>
