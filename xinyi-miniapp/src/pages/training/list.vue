<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onHide, onUnload } from '@dcloudio/uni-app';
import {
  returnToTraining,
  getTraining,
  trainingScope,
  statusLabel,
  displayTime,
  type Training
} from '../../features/training/api';
const value = ref<Training | null>(null),
  loading = ref(false),
  error = ref('');
let sequence = 0,
  visible = false;
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
  value.value = null;
  error.value = '';
  loading.value = true;
  try {
    const scope = trainingScope(),
      result = await getTraining();
    if (!visible || run !== sequence) return;
    if (scope !== trainingScope()) throw new Error('身份已切换，请重新进入培训');
    value.value = result;
  } catch (failure) {
    if (visible && run === sequence)
      error.value = failure instanceof Error ? failure.message : '培训方案读取失败';
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}
function open(path: string) {
  if (path === '/pages/qualification/index') returnToTraining(path);
  else uni.navigateTo({ url: path });
}
</script>
<template>
  <view class="training-screen">
    <text class="title">服务培训</text>
    <text class="muted"
      >公司明确必修课程。首次开始学习后冻结本申请的课程版本，之后发布的新课程不会替换已固定课表。</text
    >
    <text v-if="loading" class="muted">正在核对本人申请与培训方案…</text>
    <template v-if="value">
      <view class="card"
        ><text class="section">{{ value.companyName || '所属公司尚未配置' }}</text
        ><text>本申请：{{ value.applicationId }}</text
        ><text class="muted">{{
          value.planId ? '已固定课表 · ' + value.planId : '尚未开始：显示当前正式方案预览'
        }}</text
        ><text>{{
          value.complete ? '当前材料的全部必修培训已通过' : '尚未完成当前材料的全部必修培训'
        }}</text
        ><text class="muted">培训完成不等于代驾资格审核通过，也不会开启听单。</text></view
      >
      <view v-if="value.blockers.length" class="card"
        ><text class="section">开始前还需完成</text
        ><text v-for="(item, index) in value.blockers" :key="index" class="note">{{
          item.message
        }}</text></view
      >
      <view v-if="!value.units.length" class="card"
        ><text>尚无可开始的正式必修课程。</text
        ><text class="muted"
          >空课程列表不会视为培训通过，请联系公司管理员核对正式培训方案。</text
        ></view
      >
      <view v-for="unit in value.units" :key="unit.trainingId" class="card"
        ><view class="row"
          ><text class="section">{{ unit.title }}</text
          ><text>{{ statusLabel(unit.status) }}</text></view
        ><text class="muted">固定内容版本 V{{ unit.courseVersion }}</text
        ><text
          >有效已学 {{ (unit.verifiedDurationMillis / 1000).toFixed(1) }} / 必学
          {{ unit.requiredDurationSeconds }} 秒</text
        ><text>已用 {{ unit.attemptsUsed }} / 最多 {{ unit.maxAttempts }} 次考试（含首次）</text
        ><text v-if="unit.retryAvailableAt" class="muted"
          >下次可考时间：{{ displayTime(unit.retryAvailableAt) }}</text
        ><button
          v-if="unit.allowedActions.includes('START_PLAYBACK')"
          class="primary"
          @click="open('/pages/training/course?trainingId=' + unit.trainingId)"
        >
          学习此版本课程</button
        ><button v-else @click="open('/pages/training/course?trainingId=' + unit.trainingId)">
          查看课程与进度</button
        ><button
          v-if="unit.latestAttemptId && unit.allowedActions.includes('VIEW_TRAINING_ATTEMPT')"
          @click="open('/pages/training/exam?attemptId=' + unit.latestAttemptId)"
        >
          查看本次考试
        </button></view
      >
    </template>
    <view v-if="!loading && !value && !error" class="card"
      ><text>请先建立本人的代驾资格申请，再开始培训。</text
      ><button class="primary" @click="open('/pages/qualification/index')">
        进入认证中心
      </button></view
    >
    <text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="loading" @click="load">刷新课程与方案</button>
    <button @click="open('/pages/qualification/index')">返回认证中心</button>
  </view>
</template>
<style src="./training.css"></style>
