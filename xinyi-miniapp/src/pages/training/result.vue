<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import {
  returnToTraining,
  getAttempt,
  trainingScope,
  statusLabel,
  displayTime,
  type Attempt
} from '../../features/training/api';
const value = ref<Attempt | null>(null),
  loading = ref(false),
  error = ref('');
let attemptId = '',
  sequence = 0,
  visible = false;
onLoad((query) => {
  attemptId = String(query?.attemptId || '');
});
onShow(() => {
  visible = true;
  load();
});
function clear() {
  visible = false;
  sequence++;
  value.value = null;
  loading.value = false;
  error.value = '';
}
onHide(clear);
onUnload(clear);
async function load() {
  const run = ++sequence;
  loading.value = true;
  error.value = '';
  value.value = null;
  try {
    const scope = trainingScope(),
      attempt = await getAttempt(attemptId);
    if (!visible || run !== sequence) return;
    if (scope !== trainingScope()) throw new Error('身份已变化，请重新进入培训');
    value.value = attempt;
  } catch (failure) {
    if (visible && run === sequence)
      error.value = failure instanceof Error ? failure.message : '考试结果读取失败';
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}
function open(path: string) {
  returnToTraining(path);
}
</script>
<template>
  <view class="training-screen"
    ><text class="title">测验结果</text
    ><text v-if="loading" class="muted">正在核对服务端原考试结果…</text
    ><view v-if="value" class="card"
      ><text class="section">{{ statusLabel(value.status) }}</text
      ><text class="muted"
        >尝试编号 {{ value.attemptId }} · 第 {{ value.attemptNo }} 次 · 固定课程 V{{
          value.courseVersion
        }}</text
      ><text>分数：{{ value.score === null ? '尚无最终分数' : value.score + ' 分' }}</text
      ><text>通过标准：{{ value.passScore }} 分</text
      ><text
        >实际通过结果：{{
          value.passed === null ? '尚未形成结论' : value.passed ? '已通过' : '未通过'
        }}</text
      ><text class="muted"
        >截止：{{ displayTime(value.expiresAt) }} · 提交：{{ displayTime(value.submittedAt) }}</text
      ><text class="note"
        >失败和超时计入累计次数，修改材料不会重置。能否重考及下次可考时间，请返回课程查看当前服务端状态。</text
      ><text class="muted"
        >本页复用原尝试结果，不重新判卷、不展示正确答案；课程通过不等于资格审核通过。</text
      ><button
        v-if="value.status === 'OPEN'"
        class="primary"
        @click="open('/pages/training/exam?attemptId=' + value.attemptId)"
      >
        继续原考试</button
      ><button @click="open('/pages/training/course?trainingId=' + value.trainingId)">
        返回本课程与重考条件
      </button></view
    ><text v-if="error" class="error" role="alert">{{ error }}</text
    ><button :disabled="loading" @click="load">刷新原考试结果</button
    ><button @click="open('/pages/qualification/index')">返回认证中心</button></view
  >
</template>
<style src="./training.css"></style>
