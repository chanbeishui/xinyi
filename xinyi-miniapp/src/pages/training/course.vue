<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import {
  returnToTraining,
  getTraining,
  getUnit,
  startPlayback,
  reportProgress,
  createAttempt,
  getAttempt,
  getMediaAccess,
  pendingWrite,
  newWrite,
  trainingScope,
  unknownWrite,
  statusLabel,
  displayTime,
  type Training,
  type TrainingUnit,
  type Playback,
  type PlaybackInput,
  type ProgressInput,
  type AttemptInput
} from '../../features/training/api';
import { downloadTrainingMedia, releaseTrainingMedia } from '../../features/training/media';
const training = ref<Training | null>(null),
  unit = ref<TrainingUnit | null>(null),
  playback = ref<Playback | null>(null);
const loading = ref(false),
  starting = ref(false),
  downloading = ref(false),
  downloadProgress = ref(0);
const mediaPath = ref(''),
  playing = ref(false),
  baselineReady = ref(false),
  position = ref(0);
const reporting = ref(false),
  reportUnknown = ref(false),
  playbackUnknown = ref(false),
  attemptUnknown = ref(false);
const busyExam = ref(false),
  error = ref(''),
  notice = ref('');
let trainingId = '',
  sequence = 0,
  visible = false,
  nextHeartbeat = 1;
let timer: ReturnType<typeof setInterval> | null = null;
let download: ReturnType<typeof downloadTrainingMedia> | null = null;
let inFlight: Promise<boolean> | null = null;
let finishing: Promise<void> | null = null;
let player: UniApp.VideoContext | null = null;
const locked = computed(
  () =>
    starting.value ||
    downloading.value ||
    reporting.value ||
    busyExam.value ||
    reportUnknown.value ||
    playbackUnknown.value
);
const canStart = computed(() =>
  Boolean(
    training.value?.allowedActions.includes('START_TRAINING') &&
      unit.value?.allowedActions.includes('START_PLAYBACK')
  )
);
onLoad((query) => {
  trainingId = String(query?.trainingId || '');
});
onShow(() => {
  visible = true;
  load();
});
function stopTimer() {
  if (timer) clearInterval(timer);
  timer = null;
}
function stopLocal() {
  playing.value = false;
  stopTimer();
  player?.pause();
}
function clearForbidden(failure: unknown) {
  const status = (failure as { status?: number }).status;
  if (![401, 403, 404].includes(status || 0)) return;
  stopLocal();
  const old = mediaPath.value;
  mediaPath.value = '';
  releaseTrainingMedia(old);
  playback.value = null;
  baselineReady.value = false;
  training.value = null;
  unit.value = null;
}
function clear() {
  visible = false;
  sequence++;
  stopLocal();
  download?.cancel();
  download = null;
  const old = mediaPath.value;
  mediaPath.value = '';
  releaseTrainingMedia(old);
  training.value = null;
  unit.value = null;
  playback.value = null;
  baselineReady.value = false;
  loading.value = false;
  starting.value = false;
  downloading.value = false;
  reporting.value = false;
  busyExam.value = false;
  player = null;
  inFlight = null;
  finishing = null;
  position.value = 0;
  error.value = '';
  notice.value = '';
  // 离页停止、解绑并清临时文件；不在恢复页面时补报离线时长。
}
onHide(clear);
onUnload(clear);
async function load() {
  if (starting.value || downloading.value || busyExam.value || reporting.value) return;
  stopLocal();
  const run = ++sequence;
  const old = mediaPath.value;
  mediaPath.value = '';
  releaseTrainingMedia(old);
  playback.value = null;
  baselineReady.value = false;
  position.value = 0;
  error.value = '';
  loading.value = true;
  try {
    const scope = trainingScope(),
      collection = await getTraining();
    if (!collection) throw new Error('请先建立本人资格申请');
    const course = await getUnit(trainingId);
    if (!visible || run !== sequence) return;
    if (
      scope !== trainingScope() ||
      !collection.units.some(
        (item) =>
          item.trainingId === course.trainingId && item.courseVersion === course.courseVersion
      )
    )
      throw new Error('本人课表已变化，请返回课程列表重新核对');
    training.value = collection;
    unit.value = course;
    reportUnknown.value = Boolean(pendingWrite<ProgressInput>('PROGRESS:' + trainingId));
    playbackUnknown.value = Boolean(pendingWrite<PlaybackInput>('PLAYBACK:' + trainingId));
    attemptUnknown.value = Boolean(pendingWrite<AttemptInput>('ATTEMPT:' + trainingId));
  } catch (failure) {
    if (visible && run === sequence) {
      training.value = null;
      unit.value = null;
      error.value = failure instanceof Error ? failure.message : '课程读取失败';
    }
  } finally {
    if (visible && run === sequence) loading.value = false;
  }
}
async function prepare() {
  if (starting.value || downloading.value || reportUnknown.value || !training.value || !unit.value)
    return;
  const mapKey = 'PLAYBACK:' + trainingId;
  let intent = pendingWrite<PlaybackInput>(mapKey);
  if (!intent && !canStart.value) return;
  const run = sequence,
    scope = trainingScope();
  starting.value = true;
  error.value = '';
  notice.value = '';
  stopLocal();
  const previousMedia = mediaPath.value;
  mediaPath.value = '';
  releaseTrainingMedia(previousMedia);
  baselineReady.value = false;
  try {
    if (!intent) {
      const state = training.value;
      if (!state.policyRevisionId) throw new Error('公司尚未发布正式培训方案');
      intent = newWrite({
        applicationId: state.applicationId,
        expectedApplicationVersion: state.version,
        expectedCompanyRevision: state.companyRevision,
        expectedMaterialRevision: state.materialRevision,
        expectedPolicyRevisionId: state.policyRevisionId,
        planId: state.planId,
        runId: state.runId,
        courseVersion: unit.value.courseVersion,
        expectedVersion: unit.value.version
      });
      pendingWrite(mapKey, intent);
    }
    const session = await startPlayback(trainingId, intent.body, intent.key);
    if (!visible || run !== sequence || scope !== trainingScope()) return;
    pendingWrite(mapKey, null);
    playbackUnknown.value = false;
    const current = await getTraining(),
      course = await getUnit(trainingId);
    if (!visible || run !== sequence) return;
    if (
      !current ||
      current.applicationId !== session.applicationId ||
      current.runId !== session.runId ||
      current.planId !== session.planId ||
      current.materialRevision !== intent.body.expectedMaterialRevision ||
      current.companyRevision !== intent.body.expectedCompanyRevision
    )
      throw new Error('资格材料或公司归属已变化，请重读当前课表');
    training.value = current;
    unit.value = course;
    playback.value = session;
    nextHeartbeat = 1;
    position.value = 0;
    baselineReady.value = false;
    const ticket = await getMediaAccess(course.mediaFileId);
    if (!visible || run !== sequence) return;
    starting.value = false;
    downloading.value = true;
    downloadProgress.value = 0;
    download = downloadTrainingMedia(ticket, (value) => {
      if (visible && run === sequence) downloadProgress.value = value;
    });
    const path = await download.promise;
    if (!visible || run !== sequence) {
      releaseTrainingMedia(path);
      return;
    }
    const old = mediaPath.value;
    mediaPath.value = path;
    releaseTrainingMedia(old);
    downloading.value = false;
    download = null;
    player = uni.createVideoContext('training-video');
    // 下载不算学习；播放前先报0位置的暂停基线，服务端确认后才允许播放。
    const baseline = await send(false, 0);
    if (!visible || run !== sequence) return;
    baselineReady.value = baseline;
    if (baselineReady.value)
      notice.value = '教材已准备，请点击开始播放。有效学时以服务端确认为准。';
  } catch (failure) {
    if (!visible || run !== sequence) return;
    playbackUnknown.value = Boolean(pendingWrite<PlaybackInput>(mapKey)) && unknownWrite(failure);
    if (!unknownWrite(failure)) pendingWrite(mapKey, null);
    error.value = failure instanceof Error ? failure.message : '学习准备未完成';
    clearForbidden(failure);
    stopLocal();
  } finally {
    if (visible && run === sequence) {
      starting.value = false;
      downloading.value = false;
    }
  }
}
function send(playingState: boolean, millis: number): Promise<boolean> {
  if (inFlight) return inFlight;
  const session = playback.value,
    course = unit.value;
  if (!session || !course || !visible) return Promise.resolve(false);
  const resource = 'PROGRESS:' + trainingId,
    run = sequence,
    scope = trainingScope();
  const intent =
    pendingWrite<ProgressInput>(resource) ||
    newWrite({
      applicationId: session.applicationId,
      runId: session.runId,
      courseVersion: session.courseVersion,
      expectedVersion: course.version,
      playbackSessionId: session.playbackSessionId,
      sequence: nextHeartbeat,
      positionMillis: Math.max(0, Math.min(86400000, Math.floor(millis))),
      playing: playingState
    });
  pendingWrite(resource, intent);
  reporting.value = true;
  const work = async () => {
    try {
      const value = await reportProgress(trainingId, intent.body, intent.key);
      if (!visible || run !== sequence || scope !== trainingScope()) return false;
      unit.value = value;
      pendingWrite(resource, null);
      reportUnknown.value = false;
      nextHeartbeat = intent.body.sequence + 1;
      return true;
    } catch (failure) {
      if (visible && run === sequence) {
        reportUnknown.value = unknownWrite(failure);
        if (!reportUnknown.value) {
          pendingWrite(resource, null);
          baselineReady.value = false;
          const old = mediaPath.value;
          mediaPath.value = '';
          releaseTrainingMedia(old);
          playback.value = null;
        }
        error.value = failure instanceof Error ? failure.message : '学习进度结果未确认';
        clearForbidden(failure);
        stopLocal();
      }
      return false;
    } finally {
      if (visible && run === sequence) reporting.value = false;
    }
  };
  const promise = work();
  inFlight = promise.finally(() => {
    if (inFlight === result) inFlight = null;
  });
  const result = inFlight;
  return result;
}
function begin() {
  if (baselineReady.value && !locked.value && mediaPath.value) player?.play();
}
function onTime(event: unknown) {
  const seconds = (event as { detail?: { currentTime?: unknown } }).detail?.currentTime;
  if (visible && typeof seconds === 'number' && Number.isFinite(seconds))
    position.value = Math.max(
      0,
      Math.min(unit.value?.mediaDurationMillis ?? 86400000, Math.floor(seconds * 1000))
    );
}
async function onPlay() {
  if (!baselineReady.value || reportUnknown.value) {
    stopLocal();
    return;
  }
  const run = sequence;
  playing.value = true;
  await send(true, position.value);
  if (!visible || run !== sequence || !playing.value || reportUnknown.value) return;
  stopTimer();
  // 3秒是技术心跳节奏，低于服务端15秒上限；绝不把本地计时直接累计为有效学时。
  timer = setInterval(() => {
    if (playing.value && !reporting.value && !reportUnknown.value) send(true, position.value);
  }, 3000);
}
function finishSegment(): Promise<void> {
  if (finishing) return finishing;
  const run = sequence;
  const work = async () => {
    const wasPlaying = playing.value;
    playing.value = false;
    stopTimer();
    if (inFlight) await inFlight;
    if (!visible || run !== sequence || !baselineReady.value || reportUnknown.value || !wasPlaying)
      return;
    // 最后一段真实播放与暂停确认共用同一串行任务，原生pause事件不能与进入考试争用版本。
    if (await send(true, position.value)) {
      if (visible && run === sequence) await send(false, position.value);
    }
  };
  const promise = work();
  const result = promise.finally(() => {
    if (finishing === result) finishing = null;
  });
  finishing = result;
  return result;
}
async function pause() {
  player?.pause();
  await finishSegment();
}
function onEnded() {
  // 原生结束事件给出完整播放边界；仍须服务器墙钟及片段去重核验，不能直接宣布学完。
  if (unit.value) position.value = unit.value.mediaDurationMillis;
  return finishSegment();
}
function onVideoError() {
  stopLocal();
  error.value = '教材播放失败，请重新准备教材';
}
async function retryProgress() {
  if (reporting.value || starting.value) return;
  const resource = 'PROGRESS:' + trainingId,
    intent = pendingWrite<ProgressInput>(resource);
  if (!intent) return;
  if (playback.value) {
    const run = sequence;
    const confirmed = await send(intent.body.playing, intent.body.positionMillis);
    if (visible && run === sequence) baselineReady.value = confirmed;
    return;
  }
  const run = sequence;
  reporting.value = true;
  try {
    const value = await reportProgress(trainingId, intent.body, intent.key);
    if (!visible || run !== sequence) return;
    unit.value = value;
    pendingWrite(resource, null);
    reportUnknown.value = false;
    notice.value = '原进度已核对，请重新准备教材后播放。';
  } catch (failure) {
    if (visible && run === sequence) {
      if (!unknownWrite(failure)) {
        pendingWrite(resource, null);
        reportUnknown.value = false;
      }
      error.value = failure instanceof Error ? failure.message : '原进度尚未确认';
    }
  } finally {
    if (visible && run === sequence) reporting.value = false;
  }
}
async function exam() {
  if (busyExam.value || reporting.value || downloading.value || !training.value || !unit.value)
    return;
  const resource = 'ATTEMPT:' + trainingId,
    run = sequence;
  busyExam.value = true;
  error.value = '';
  try {
    await pause();
    if (!visible || run !== sequence || reportUnknown.value) return;
    const current = await getUnit(trainingId);
    if (!visible || run !== sequence) return;
    unit.value = current;
    let intent = pendingWrite<AttemptInput>(resource);
    if (intent && current.latestAttemptId) {
      const existing = await getAttempt(current.latestAttemptId);
      if (!visible || run !== sequence) return;
      if (
        existing.applicationId === intent.body.applicationId &&
        existing.runId === intent.body.runId &&
        existing.trainingId === trainingId
      ) {
        pendingWrite(resource, null);
        attemptUnknown.value = false;
        openExam(existing.attemptId);
        return;
      }
    }
    if (!intent) {
      if (!training.value.runId || !current.allowedActions.includes('CREATE_TRAINING_ATTEMPT'))
        throw new Error('当前尚不能开始考试，请核对有效学时、次数与间隔');
      intent = newWrite({
        applicationId: training.value.applicationId,
        runId: training.value.runId,
        courseVersion: current.courseVersion,
        expectedVersion: current.version
      });
      pendingWrite(resource, intent);
    }
    const value = await createAttempt(trainingId, intent.body, intent.key);
    if (!visible || run !== sequence) return;
    pendingWrite(resource, null);
    attemptUnknown.value = false;
    openExam(value.attemptId);
  } catch (failure) {
    if (visible && run === sequence) {
      attemptUnknown.value = Boolean(pendingWrite(resource)) && unknownWrite(failure);
      if (!unknownWrite(failure)) pendingWrite(resource, null);
      error.value = failure instanceof Error ? failure.message : '考试建立结果尚未确认';
      clearForbidden(failure);
    }
  } finally {
    if (visible && run === sequence) busyExam.value = false;
  }
}
function openExam(id: string) {
  uni.navigateTo({ url: '/pages/training/exam?attemptId=' + id });
}
function cancelDownload() {
  download?.cancel();
}
function backList() {
  returnToTraining('/pages/training/list');
}
</script>
<template>
  <view class="training-screen">
    <text class="title">课程学习</text
    ><text v-if="loading" class="muted">正在核对固定课程与本人进度…</text>
    <template v-if="training && unit">
      <view class="card"
        ><text class="section">{{ unit.title }}</text
        ><text class="muted"
          >{{ training.companyName || '所属公司待配置' }} · 固定课程 V{{ unit.courseVersion }}</text
        ><text v-if="unit.description">{{ unit.description }}</text
        ><text>{{ statusLabel(unit.status) }}</text
        ><text
          >有效已学 {{ (unit.verifiedDurationMillis / 1000).toFixed(1) }} / 必学
          {{ unit.requiredDurationSeconds }} 秒</text
        ><text class="muted"
          >本地播放位置
          {{ (position / 1000).toFixed(1) }}
          秒，不能代替有效学时。跳进、倍速、重复片段及离线时间不会直接增加学时。</text
        ></view
      >
      <view v-for="(item, index) in training.blockers" :key="index" class="note">{{
        item.message
      }}</view>
      <view class="card"
        ><text class="section">受保护教材</text
        ><text class="muted"
          >先下载临时教材再播放，最大
          100MiB。下载过程不计学习时长，离开页面会停止并清理临时教材。</text
        ><template v-if="downloading"
          ><progress :percent="downloadProgress" show-info /><button @click="cancelDownload">
            取消教材下载
          </button></template
        ><button
          v-if="canStart || playbackUnknown"
          :disabled="starting || downloading || reporting || reportUnknown || busyExam"
          :loading="starting"
          @click="prepare"
        >
          {{
            playbackUnknown
              ? '沿用原播放请求核对'
              : mediaPath
                ? '重新准备教材与会话'
                : '准备教材并开始学习'
          }}</button
        ><video
          v-if="mediaPath"
          id="training-video"
          :src="mediaPath"
          :controls="baselineReady && !reportUnknown"
          :show-center-play-btn="false"
          :enable-progress-gesture="false"
          :show-progress="false"
          @play="onPlay"
          @pause="finishSegment"
          @ended="onEnded"
          @timeupdate="onTime"
          @error="onVideoError"
        /><button
          v-if="mediaPath"
          class="primary"
          :disabled="!baselineReady || locked"
          @click="begin"
        >
          开始播放</button
        ><button v-if="mediaPath" :disabled="!playing" @click="pause">暂停学习</button
        ><text v-if="reporting" class="muted">正在确认本段进度…</text
        ><text v-if="reportUnknown" class="note"
          >原心跳结果尚未确认，播放已暂停；不会换序号或新键追报。</text
        ><button v-if="reportUnknown" :disabled="reporting" @click="retryProgress">
          按原进度请求核对重试
        </button></view
      >
      <view class="card"
        ><text class="section">课程考试</text
        ><text
          >已用 {{ unit.attemptsUsed }} / 最多
          {{ unit.maxAttempts }} 次（含首次）。失败和超时计次，修改材料不会重置次数。</text
        ><text v-if="unit.retryAvailableAt" class="muted"
          >下次可考：{{ displayTime(unit.retryAvailableAt) }}</text
        ><button
          v-if="unit.allowedActions.includes('CREATE_TRAINING_ATTEMPT') || attemptUnknown"
          class="primary"
          :disabled="locked || busyExam"
          :loading="busyExam"
          @click="exam"
        >
          {{ attemptUnknown ? '核对原考试建立结果' : '开始本课程考试' }}</button
        ><button
          v-if="unit.latestAttemptId && unit.allowedActions.includes('VIEW_TRAINING_ATTEMPT')"
          @click="openExam(unit.latestAttemptId)"
        >
          查看本次考试</button
        ><text v-if="!unit.allowedActions.includes('CREATE_TRAINING_ATTEMPT')" class="muted"
          >考试开放以实际有效学时、累计次数与冻结政策间隔为准。</text
        ></view
      >
    </template>
    <text v-if="notice" class="success" role="status">{{ notice }}</text
    ><text v-if="error" class="error" role="alert">{{ error }}</text>
    <button :disabled="starting || downloading || busyExam || reporting" @click="load">
      重读当前课程</button
    ><button @click="backList">返回必修课程</button>
  </view>
</template>
<style src="./training.css"></style>
