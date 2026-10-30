<template>
  <RongliPage
    title="培训课程"
    subtitle="维护真实服务规范与考核内容。发布版本不能覆盖已产生的学习或答题记录。"
  >
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" icon="Plus" @click="openCreate">维护课程</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-select v-model="query.status" placeholder="全部状态" style="width: 150px" @change="load">
          <el-option label="全部状态" value="" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已停用" value="DISABLED" />
        </el-select>
        <span class="rl-muted">共 {{ total }} 个课程版本</span>
      </div>

      <el-table v-loading="loading" :data="rows" :border="false">
        <el-table-column label="课程" min-width="220">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.title }}</div>
            <div class="rl-mono">{{ row.courseKey }}</div>
          </template>
        </el-table-column>
        <el-table-column label="版本" width="100">
          <template #default="{ row }">
            <span class="rl-mono">V{{ row.courseVersion }}</span>
          </template>
        </el-table-column>
        <el-table-column label="要求学时" width="110">
          <template #default="{ row }">
            {{ Math.round(Number(row.requiredDurationSeconds || 0) / 60) }} 分钟
          </template>
        </el-table-column>
        <el-table-column label="及格分" width="90" align="center">
          <template #default="{ row }">{{ row.passScore }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="rl-tag" :class="statusClass(row.status)">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">{{ shortTime(row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              {{ row.status === 'DRAFT' ? '编辑课程与题目' : '查看课程' }}
            </el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="success" @click="publish(row)">发布</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">还没有课程</div>
            <div class="rl-empty__hint">先维护教材与题库，发布后对新代驾生效。</div>
            <el-button type="primary" @click="openCreate">维护课程</el-button>
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
          只读演示题不代表实际培训政策。正式上线前由业务确定教材、必学时长、分数及重试规则；已发布课程不可修改，调整需新建同编码的新版本。
        </p>
      </div>
    </section>

    <el-drawer v-model="dialogVisible" :title="form.trainingId ? '编辑课程与题目' : '新建课程'" size="780px">
      <div class="rl-panel">
        <div class="rl-panel__head"><h3>课程信息</h3></div>
        <div class="rl-panel__body">
          <el-form label-position="top" class="rl-form-grid">
            <el-form-item label="课程编码">
              <el-input v-model="form.courseKey" placeholder="留空自动生成，例如 SERVICE_BASIC" :disabled="!!form.trainingId" />
            </el-form-item>
            <el-form-item label="课程标题" required>
              <el-input v-model="form.title" maxlength="120" />
            </el-form-item>
            <el-form-item label="要求学时（秒）" required>
              <el-input-number v-model="form.requiredDurationSeconds" :min="30" :step="30" style="width: 100%" />
            </el-form-item>
            <el-form-item label="及格分">
              <el-input-number v-model="form.passScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
            <el-form-item label="考试时限（秒）">
              <el-input-number v-model="form.attemptTimeLimitSeconds" :min="60" :step="60" style="width: 100%" />
            </el-form-item>
            <el-form-item label="视频文件 ID">
              <el-input v-model="form.mediaFileId" placeholder="可选，培训视频的媒体文件 ID" />
            </el-form-item>
          </el-form>
          <el-form-item label="课程说明" label-position="top">
            <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
        </div>
      </div>

      <div class="rl-panel">
        <div class="rl-panel__head">
          <h3>题库</h3>
          <span class="rl-muted">选项一行一个，正确答案填 A/B/C，多选可用 A,B</span>
        </div>
        <div class="rl-panel__body">
          <div v-for="(q, i) in form.questions" :key="i" class="rl-question">
            <div class="rl-question__head">
              <span class="rl-strong">第 {{ i + 1 }} 题</span>
              <el-button link type="danger" :disabled="readonly" @click="removeQuestion(i)">删除</el-button>
            </div>
            <el-form label-position="top" class="rl-form-grid">
              <el-form-item label="题号">
                <el-input v-model="q.questionCode" placeholder="Q1" :disabled="readonly" />
              </el-form-item>
              <el-form-item label="题型">
                <el-select v-model="q.questionType" style="width: 100%" :disabled="readonly">
                  <el-option label="单选" value="SINGLE" />
                  <el-option label="多选" value="MULTIPLE" />
                  <el-option label="判断" value="TRUE_FALSE" />
                </el-select>
              </el-form-item>
              <el-form-item label="题干" style="grid-column: span 2">
                <el-input v-model="q.prompt" placeholder="请输入题干" :disabled="readonly" />
              </el-form-item>
              <el-form-item label="选项（一行一个）">
                <el-input
                  v-model="q.optionsText"
                  type="textarea"
                  :rows="3"
                  :disabled="readonly"
                  placeholder="合格&#10;不合格"
                />
              </el-form-item>
              <el-form-item label="正确答案">
                <el-input v-model="q.correctOptionCodes" placeholder="如 A" :disabled="readonly" />
                <div class="rl-sub">按选项顺序自动编号 A/B/C/D</div>
              </el-form-item>
            </el-form>
          </div>

          <el-button v-if="!readonly" icon="Plus" @click="addQuestion">添加题目</el-button>
          <p v-if="readonly" class="rl-hint">已发布课程不可修改，需要调整请新建同编码的新版本。</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!readonly" type="primary" :loading="saving" @click="submit">保存草稿</el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliTraining" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createTrainingCourse,
  getTrainingCourse,
  listTrainingCourses,
  publishTrainingCourse,
  updateTrainingCourse
} from '@/api/rongli'
import { shortTime } from '../format'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', pageNum: 1, pageSize: 20 })

const dialogVisible = ref(false)
const form = reactive<any>({
  trainingId: '',
  status: 'DRAFT',
  courseKey: '',
  title: '',
  description: '',
  mediaFileId: '',
  requiredDurationSeconds: 300,
  passScore: 80,
  attemptTimeLimitSeconds: 1800,
  questions: [] as any[]
})

const readonly = computed(() => !!form.trainingId && form.status !== 'DRAFT')

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

function statusClass(status: string) {
  if (status === 'PUBLISHED') return 'rl-tag--green'
  if (status === 'DRAFT') return 'rl-tag--amber'
  return 'rl-tag--gray'
}

function resetForm() {
  form.trainingId = ''
  form.status = 'DRAFT'
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
  form.status = detail.status || 'DRAFT'
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
.rl-question {
  border: 1px solid var(--rl-line, #eaecf0);
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 12px;
}

.rl-question__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
</style>
