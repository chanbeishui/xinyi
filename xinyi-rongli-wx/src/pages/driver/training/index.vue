<template>
  <view class="page">
    <view class="card">
      <view class="card-title">服务培训与考试</view>
      <view class="muted">
        认证前需完成服务培训并通过知识测验。学时按前台有效播放累计，离开页面或快进不计入。
      </view>
    </view>

    <view v-if="!courses.length" class="empty">暂无已发布课程</view>

    <view v-for="c in courses" :key="c.trainingId" class="card" @tap="openCourse(c)">
      <view class="row-between">
        <text class="value" style="text-align: left">{{ c.title }}</text>
        <text class="tag" :class="statusClass(c.progressStatus)">{{ statusLabel(c.progressStatus) }}</text>
      </view>
      <view class="muted" style="margin-top: 8rpx">{{ c.description }}</view>
      <view class="progress">
        <view class="progress-inner" :style="{ width: percent(c) + '%' }"></view>
      </view>
      <view class="muted" style="margin-top: 6rpx">
        已学 {{ c.verifiedSeconds }} / {{ c.requiredDurationSeconds }} 秒 · 及格 {{ c.passScore }} 分
      </view>
    </view>

    <!-- 课程学习 -->
    <view v-if="selected" class="card">
      <view class="card-title">{{ selected.title }}</view>
      <view class="row-between">
        <text class="label">要求学时</text>
        <text class="value">{{ selected.requiredDurationSeconds }} 秒</text>
      </view>
      <view class="row-between">
        <text class="label">已认可学时</text>
        <text class="value">{{ verified }} 秒</text>
      </view>
      <view class="row-between">
        <text class="label">题目数量</text>
        <text class="value">{{ selected.questionCount }} 题</text>
      </view>
      <view class="progress">
        <view class="progress-inner" :style="{ width: percent(selected) + '%' }"></view>
      </view>
      <view class="muted" style="margin-top: 12rpx">{{ playing ? '学习中，请保持本页在前台…' : selected.canExam ? '学时已达标，可开始考试' : '继续学习以累计有效学时' }}</view>
      <view class="row" style="gap: 16rpx; margin-top: 18rpx">
        <view v-if="!playing" class="btn small" style="flex: 1" @tap="startLearning">开始/继续学习</view>
        <view v-else class="btn small gray" style="flex: 1" @tap="stopLearning">暂停学习</view>
        <view v-if="selected.canExam" class="btn small ghost" style="flex: 1" @tap="startExam">开始考试</view>
      </view>
    </view>

    <!-- 答题 -->
    <view v-if="attempt" class="card">
      <view class="row-between">
        <text class="card-title">第 {{ attempt.attemptNo }} 次考试</text>
        <text class="tag warn">{{ attempt.hint }}</text>
      </view>
      <view v-for="(q, qi) in attempt.questions" :key="q.questionCode" class="field">
        <view class="value" style="text-align: left">{{ qi + 1 }}. {{ q.prompt }}</view>
        <view
          v-for="(opt, oi) in q.options"
          :key="optCode(opt, oi)"
          class="option"
          :class="{ on: answers[q.questionCode] === optCode(opt, oi) }"
          @tap="pick(q.questionCode, optCode(opt, oi))"
        >
          <text class="option-code">{{ optCode(opt, oi) }}</text>
          <text>{{ optText(opt) }}</text>
        </view>
      </view>
      <view class="btn" style="margin-top: 20rpx" @tap="submitExam">提交答卷</view>
    </view>

    <!-- 成绩 -->
    <view v-if="result" class="card">
      <view class="card-title">考试结果</view>
      <view class="row-between">
        <text class="label">得分</text>
        <text class="value" :class="result.status === 'PASSED' ? 'ok-text' : 'danger-text'">
          {{ result.score }} 分（{{ result.statusLabel }}）
        </text>
      </view>
      <view class="row-between">
        <text class="label">正确题数</text>
        <text class="value">{{ result.correctCount }} / {{ result.totalCount }}</text>
      </view>
      <view class="muted" style="margin-top: 10rpx">
        {{ result.status === 'PASSED' ? '已完成该课程要求，可继续其他课程。' : '未达及格线，可重新学习后再次开考。' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onHide, onShow, onUnload } from '@dcloudio/uni-app'
import { driverApi } from '@/api'

const courses = ref<any[]>([])
const selected = ref<any>(null)
const verified = ref(0)
const playing = ref(false)
const sessionId = ref('')
const position = ref(0)
const attempt = ref<any>(null)
const result = ref<any>(null)
const answers = reactive<Record<string, string>>({})

let timer: any = null
const TICK_SECONDS = 15

onShow(() => {
  load()
})

onHide(() => stopLearning())
onUnload(() => stopLearning())

async function load() {
  try {
    const view: any = await driverApi.training()
    courses.value = (view && view.rows) || []
    if (selected.value) {
      const same = courses.value.find((c) => c.trainingId === selected.value.trainingId)
      if (same) selected.value = { ...selected.value, ...same }
    }
  } catch (e) {
    courses.value = []
  }
}

async function openCourse(c: any) {
  try {
    selected.value = await driverApi.trainingCourse(c.trainingId)
    verified.value = selected.value.verifiedSeconds || 0
    position.value = selected.value.lastPositionSeconds || 0
    attempt.value = null
    result.value = null
    stopLearning()
  } catch (e) {
    // 已提示
  }
}

async function startLearning() {
  try {
    const view: any = await driverApi.startPlayback(selected.value.trainingId)
    sessionId.value = view.sessionId
    position.value = view.resumeAtSeconds || position.value
    playing.value = true
    uni.showToast({ title: view.hint || '开始学习', icon: 'none' })
    timer = setInterval(tick, TICK_SECONDS * 1000)
  } catch (e) {
    // 已提示
  }
}

async function tick() {
  if (!playing.value) return
  try {
    const view: any = await driverApi.reportProgress(selected.value.trainingId, {
      sessionId: sessionId.value,
      deltaSeconds: TICK_SECONDS,
      positionSeconds: position.value + TICK_SECONDS
    })
    verified.value = view.verifiedSeconds
    position.value = position.value + TICK_SECONDS
    selected.value.canExam = view.canExam
    selected.value.progressStatus = view.progressStatus
    if (view.canExam) {
      stopLearning()
      uni.showToast({ title: '学时已达标，可以考试了', icon: 'none' })
    }
  } catch (e) {
    stopLearning()
  }
}

function stopLearning() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  playing.value = false
}

async function startExam() {
  try {
    stopLearning()
    const view: any = await driverApi.createAttempt(selected.value.trainingId)
    attempt.value = view
    result.value = null
    const questions = (view && view.questions) || []
    for (const q of questions) {
      if (!answers[q.questionCode]) answers[q.questionCode] = ''
    }
  } catch (e) {
    // 已提示
  }
}

function pick(questionCode: string, code: string) {
  answers[questionCode] = code
}

/** 兼容两种选项形态：规范化后的 {code,text} 与历史字符串数组 */
function optCode(opt: any, index: number) {
  if (opt && typeof opt === 'object' && opt.code) return String(opt.code)
  return String.fromCharCode(65 + index)
}

function optText(opt: any) {
  if (opt && typeof opt === 'object') return opt.text || ''
  return String(opt)
}

async function submitExam() {
  const payload: Record<string, string> = {}
  let unanswered = 0
  for (const q of attempt.value.questions || []) {
    const v = answers[q.questionCode] || ''
    if (!v) unanswered++
    payload[q.questionCode] = v
  }
  if (unanswered > 0) {
    const confirm: any = await new Promise((resolve) => {
      uni.showModal({
        title: '还有未作答题目',
        content: `尚有 ${unanswered} 题未作答，提交后不可修改，确认提交？`,
        success: resolve,
        fail: () => resolve({ confirm: false })
      })
    })
    if (!confirm || !confirm.confirm) return
  }
  try {
    result.value = await driverApi.submitAttempt({
      attemptId: Number(attempt.value.attemptId),
      answers: payload
    })
    attempt.value = null
    load()
  } catch (e) {
    // 已提示
  }
}

function percent(c: any) {
  const required = Number(c.requiredDurationSeconds || 0)
  const done = Number(c.verifiedSeconds || 0)
  if (required <= 0) return 100
  return Math.min(100, Math.round((done / required) * 100))
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '学习中',
    READY_FOR_EXAM: '待考试',
    PASSED: '已通过'
  }
  return map[status || ''] || status || '未开始'
}

function statusClass(status: string) {
  if (status === 'PASSED') return 'ok'
  if (status === 'READY_FOR_EXAM') return 'warn'
  return 'gray'
}
</script>

<style scoped>
.progress {
  height: 12rpx;
  border-radius: 6rpx;
  background: #f0f1f4;
  overflow: hidden;
  margin-top: 14rpx;
}

.progress-inner {
  height: 100%;
  background: var(--brand);
}

.option {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 18rpx;
  border: 1rpx solid var(--line);
  border-radius: 12rpx;
  margin-top: 12rpx;
  font-size: 28rpx;
}

.option.on {
  border-color: var(--brand);
  background: var(--brand-soft);
}

.option-code {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #f0f1f4;
  color: var(--sub);
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.option.on .option-code {
  background: var(--brand);
  color: #fff;
}

.ok-text {
  color: var(--ok);
}

.danger-text {
  color: var(--danger);
}
</style>
