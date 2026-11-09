<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import {
  returnToTraining,
  getAttempt,
  submitAttempt,
  getTraining,
  newWrite,
  pendingWrite,
  trainingScope,
  unknownWrite,
  displayTime,
  type Attempt,
  type SubmitInput
} from '../../features/training/api';
import { currentSession } from '../../services/http';
const value = ref<Attempt | null>(null),
  answers = ref<Record<string, string[]>>({});
const loading = ref(false),
  busy = ref(false),
  unknown = ref(false),
  stale = ref(false),
  error = ref('');
const now = ref(Date.now());
let attemptId = '',
  sequence = 0,
  visible = false,
  timer: ReturnType<typeof setInterval> | null = null,
  draftKey = '';
const expiredLocally = computed(() =>
  value.value ? now.value >= new Date(value.value.expiresAt).getTime() : false
);
const secondsLeft = computed(() =>
  value.value
    ? Math.max(0, Math.ceil((new Date(value.value.expiresAt).getTime() - now.value) / 1000))
    : 0
);
const canSubmit = computed(() =>
  Boolean(
    value.value?.allowedActions.includes('SUBMIT_TRAINING_ATTEMPT') && value.value.status === 'OPEN'
  )
);
const lock = computed(
  () => loading.value || busy.value || unknown.value || stale.value || !canSubmit.value
);
onLoad((query) => {
  attemptId = String(query?.attemptId || '');
});
onShow(() => {
  visible = true;
  load();
  timer = setInterval(() => {
    now.value = Date.now();
  }, 1000);
});
function saveDraft() {
  if (visible && draftKey && value.value?.status === 'OPEN' && !unknown.value)
    uni.setStorageSync(draftKey, { answers: answers.value });
}
function clear() {
  saveDraft();
  visible = false;
  sequence++;
  if (timer) clearInterval(timer);
  timer = null;
  value.value = null;
  answers.value = {};
  draftKey = '';
  loading.value = false;
  busy.value = false;
  error.value = '';
}
onHide(clear);
onUnload(clear);
function readAnswers(attempt: Attempt, input: unknown) {
  const source = input && typeof input === 'object' ? (input as Record<string, unknown>) : {};
  const result: Record<string, string[]> = {};
  for (const question of attempt.questions) {
    const allowed = new Set(question.options.map((option) => option.code));
    const selected = Array.isArray(source[question.questionId])
      ? (source[question.questionId] as unknown[])
      : [];
    const valid = selected.filter(
      (item): item is string => typeof item === 'string' && allowed.has(item)
    );
    result[question.questionId] = [...new Set(valid)].slice(
      0,
      question.type === 'MULTIPLE' ? 10 : 1
    );
  }
  return result;
}
async function load() {
  if (busy.value) return;
  const run = ++sequence,
    old = answers.value;
  loading.value = true;
  error.value = '';
  try {
    const scope = trainingScope(),
      attempt = await getAttempt(attemptId),
      collection = await getTraining();
    if (!visible || run !== sequence) return;
    if (
      scope !== trainingScope() ||
      !collection ||
      collection.applicationId !== attempt.applicationId ||
      collection.runId !== attempt.runId
    )
      throw new Error('申请材料或培训记录已变化，请返回本人课程重新核对');
    if (attempt.status !== 'OPEN') {
      const session = currentSession();
      if (session)
        uni.removeStorageSync(
          'vi.training.answers.' +
            session.accountId +
            '.' +
            attempt.applicationId +
            '.' +
            attempt.runId +
            '.' +
            attempt.attemptId
        );
      pendingWrite('SUBMIT:' + attemptId, null);
      unknown.value = false;
      value.value = null;
      answers.value = {};
      uni.redirectTo({ url: '/pages/training/result?attemptId=' + attemptId });
      return;
    }
    value.value = attempt;
    const session = currentSession();
    if (!session) throw new Error('请先登录');
    draftKey =
      'vi.training.answers.' +
      session.accountId +
      '.' +
      attempt.applicationId +
      '.' +
      attempt.runId +
      '.' +
      attempt.attemptId;
    const stored = uni.getStorageSync(draftKey);
    const intent = pendingWrite<SubmitInput>('SUBMIT:' + attemptId);
    unknown.value = Boolean(intent);
    const saved = intent
      ? Object.fromEntries(intent.body.answers.map((item) => [item.questionId, item.answerCodes]))
      : Object.keys(old).length
        ? old
        : stored?.answers;
    answers.value = readAnswers(attempt, saved);
    stale.value = false;
    now.value = Date.now();
  } catch (failure) {
    if (visible && run === sequence) {
      value.value = null;
      answers.value = {};
      error.value = failure instanceof Error ? failure.message : '题卷读取失败';
    }
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}
function selectOne(id: string, event: { detail: { value: string } }) {
  if (!lock.value) {
    answers.value[id] = [event.detail.value];
    saveDraft();
  }
}
function selectMany(id: string, event: { detail: { value: string[] } }) {
  if (!lock.value) {
    answers.value[id] = [...new Set(event.detail.value)];
    saveDraft();
  }
}
async function submit() {
  if (!value.value || busy.value || loading.value || stale.value) return;
  const resource = 'SUBMIT:' + attemptId,
    run = sequence;
  let intent = pendingWrite<SubmitInput>(resource);
  error.value = '';
  try {
    // 未知结果先读原尝试；服务端已判分则复用，不另建尝试、不更换答案再判。
    if (intent) {
      busy.value = true;
      const current = await getAttempt(attemptId);
      if (!visible || run !== sequence) return;
      if (current.status !== 'OPEN') {
        pendingWrite(resource, null);
        unknown.value = false;
        uni.removeStorageSync(draftKey);
        value.value = null;
        answers.value = {};
        uni.redirectTo({ url: '/pages/training/result?attemptId=' + attemptId });
        return;
      }
    } else {
      if (!canSubmit.value) throw new Error('当前尝试不可提交，请刷新读取服务端最终状态');
      if (
        value.value.questions.some((question) => !(answers.value[question.questionId] || []).length)
      )
        throw new Error('请完成全部题目后再提交');
      const confirmed = await new Promise<boolean>((resolve) =>
        uni.showModal({
          title: '确认提交本次考试',
          content: '提交后本次答案不可修改，分数与通过结果由服务端统一判定。',
          success: (result) => resolve(result.confirm === true),
          fail: () => resolve(false)
        })
      );
      if (!confirmed || !visible || run !== sequence) return;
      intent = newWrite({
        applicationId: value.value.applicationId,
        runId: value.value.runId,
        attemptId,
        expectedVersion: value.value.version,
        answers: value.value.questions.map((question) => ({
          questionId: question.questionId,
          answerCodes: [...(answers.value[question.questionId] || [])]
        }))
      });
      pendingWrite(resource, intent);
    }
    busy.value = true;
    unknown.value = true;
    await submitAttempt(intent.body, intent.key);
    if (!visible || run !== sequence) return;
    pendingWrite(resource, null);
    unknown.value = false;
    uni.removeStorageSync(draftKey);
    value.value = null;
    answers.value = {};
    uni.redirectTo({ url: '/pages/training/result?attemptId=' + attemptId });
  } catch (failure) {
    if (visible && run === sequence) {
      if (!unknownWrite(failure)) {
        pendingWrite(resource, null);
        unknown.value = false;
        stale.value = (failure as { status?: number }).status === 409;
      } else unknown.value = Boolean(pendingWrite(resource));
      if ([401, 403, 404].includes((failure as { status?: number }).status || 0)) {
        // 读取后撤权或运行失效时，不能继续保留旧题卷和本地答题缓存。
        uni.removeStorageSync(draftKey);
        value.value = null;
        answers.value = {};
        draftKey = '';
      }
      error.value = failure instanceof Error ? failure.message : '提交结果尚未确认';
    }
  } finally {
    if (visible && run === sequence) busy.value = false;
  }
}
function back() {
  returnToTraining('/pages/training/list');
}
</script>
<template>
  <view class="training-screen"
    ><text class="title">服务知识测验</text
    ><text v-if="loading" class="muted">正在读取本人的固定题卷…</text>
    <template v-if="value"
      ><view class="card"
        ><text class="section">第 {{ value.attemptNo }} 次考试</text
        ><text class="muted">尝试编号 {{ value.attemptId }} · 课程 V{{ value.courseVersion }}</text
        ><text>通过分数 {{ value.passScore }} 分</text
        ><text>截止：{{ displayTime(value.expiresAt) }}</text
        ><text>本地剩余 {{ secondsLeft }} 秒（最终以服务端截止状态为准）</text
        ><text class="muted"
          >每题等分，最后总分四舍五入到整数后判定。多选须完整匹配，不计部分分；页面不提供标准答案。</text
        ></view
      >
      <view v-for="(question, index) in value.questions" :key="question.questionId" class="card"
        ><text class="section"
          >第 {{ index + 1 }} 题 ·
          {{
            question.type === 'MULTIPLE' ? '多选' : question.type === 'TRUE_FALSE' ? '判断' : '单选'
          }}</text
        ><text>{{ question.prompt }}</text
        ><checkbox-group
          v-if="question.type === 'MULTIPLE'"
          class="options"
          @change="selectMany(question.questionId, $event)"
          ><label v-for="option in question.options" :key="option.code" class="option"
            ><checkbox
              :value="option.code"
              :checked="(answers[question.questionId] || []).includes(option.code)"
              :disabled="lock"
            /><text>{{ option.code }}. {{ option.text }}</text></label
          ></checkbox-group
        ><radio-group v-else class="options" @change="selectOne(question.questionId, $event)"
          ><label v-for="option in question.options" :key="option.code" class="option"
            ><radio
              :value="option.code"
              :checked="(answers[question.questionId] || []).includes(option.code)"
              :disabled="lock"
            /><text>{{ option.code }}. {{ option.text }}</text></label
          ></radio-group
        ></view
      >
      <text v-if="expiredLocally" class="note"
        >本地显示已到截止，请刷新核对服务端状态；最终是否接收答案及实际结果均由服务端判定。</text
      ><text v-if="stale" class="note">尝试版本已变化，答案仍保留，请先刷新题卷状态。</text
      ><text v-if="unknown" class="note"
        >原提交结果尚未确认，答案已锁定；先查询原尝试，仍待提交时沿用原答案与原幂等键。</text
      >
      <button
        class="primary"
        :loading="busy"
        :disabled="busy || loading || stale || (!unknown && !canSubmit)"
        @click="submit"
      >
        {{ unknown ? '核对并重试原提交' : '确认提交本次考试' }}
      </button> </template
    ><text v-if="error" class="error" role="alert">{{ error }}</text
    ><button :disabled="busy || loading" @click="load">刷新本次题卷与状态</button
    ><button :disabled="busy" @click="back">返回必修课程</button>
  </view>
</template>
<style src="./training.css"></style>
