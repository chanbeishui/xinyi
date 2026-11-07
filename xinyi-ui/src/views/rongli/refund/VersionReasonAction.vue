<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { VersionReason } from '@/api/rongli/refund';
const props = defineProps<{
  modelValue: boolean;
  title: string;
  description: string;
  targetKey: string;
  version: number;
  canSubmit: boolean;
  submit: (body: VersionReason, key: string) => Promise<unknown>;
  reload: () => Promise<void>;
}>();
const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'done'): void;
}>();
const reason = ref(''),
  busy = ref(false),
  reloading = ref(false),
  unknown = ref(false),
  stale = ref(false),
  error = ref('');
let sequence = 0;
type Attempt = { body: VersionReason; key: string };
let attempt: Attempt | null = null;
// 暂离未知结果弹窗仍保留本页原意图；返回同一动作时不能悄悄生成新键或新退款号。
const pending = new Map<string, Attempt>();
watch(
  () => props.modelValue,
  (open: boolean) => {
    if (!open) return;
    sequence++;
    attempt = pending.get(props.targetKey) || null;
    reason.value = attempt?.body.reason || '';
    busy.value = false;
    reloading.value = false;
    unknown.value = Boolean(attempt);
    stale.value = false;
    error.value = '';
  }
);
onBeforeUnmount(() => {
  sequence++;
});
async function submit() {
  if (busy.value || reloading.value || stale.value || !props.canSubmit) return;
  error.value = '';
  const run = sequence,
    target = props.targetKey;
  try {
    if (!attempt) {
      const value = reason.value.trim();
      if (!value || value.length > 500) throw new Error('请填写 1 至 500 字操作原因');
      attempt = {
        body: { expectedVersion: props.version, reason: value },
        key: crypto.randomUUID()
      };
    }
    busy.value = true;
    await props.submit(attempt.body, attempt.key);
    if (run !== sequence || target !== props.targetKey) return;
    pending.delete(target);
    attempt = null;
    unknown.value = false;
    emit('update:modelValue', false);
    emit('done');
  } catch (failure: any) {
    if (run !== sequence || target !== props.targetKey) return;
    error.value = failure.msg || failure.message || '操作结果尚未确认，请先核对原记录';
    if (failure.httpStatus && failure.httpStatus < 500) {
      pending.delete(target);
      attempt = null;
      unknown.value = false;
      if (failure.httpStatus === 409) stale.value = true;
    } else if (attempt) {
      // 网络或服务端结果未知时冻结原意图；重试必须保留原号、原版本、原原因和幂等键。
      pending.set(target, attempt);
      unknown.value = true;
    }
  } finally {
    if (run === sequence) busy.value = false;
  }
}
async function refresh() {
  if (busy.value || reloading.value) return;
  const run = sequence;
  reloading.value = true;
  error.value = '';
  try {
    await props.reload();
    if (run !== sequence) return;
    // 409已明确拒绝才可用新版本重新提交；未知结果仍沿用原请求，刷新不能悄悄换键。
    if (!unknown.value) {
      stale.value = false;
      attempt = null;
    }
  } catch (failure: any) {
    if (run === sequence) error.value = failure.msg || failure.message || '重新读取失败';
  } finally {
    if (run === sequence) reloading.value = false;
  }
}
function close(done: () => void) {
  if (busy.value || reloading.value) {
    ElMessage.warning('请求处理中，请等待回执后继续核查');
    return;
  }
  sequence++;
  done();
  emit('update:modelValue', false);
}
</script>
<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="min(620px, 96vw)"
    append-to-body
    :before-close="close"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-alert :title="description" type="info" :closable="false" show-icon />
    <p class="target">原记录：{{ targetKey }}</p>
    <el-form label-position="top" @submit.prevent="submit">
      <el-form-item label="操作原因（必填）"
        ><el-input
          v-model="reason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="填写本次查询、重试或办结的原因"
          :disabled="busy || unknown || reloading"
      /></el-form-item>
    </el-form>
    <el-alert
      v-if="stale"
      title="记录版本已变化，原因已保留。请先重新读取当前事实，再决定是否提交。"
      type="warning"
      :closable="false"
    />
    <el-alert
      v-if="unknown"
      title="结果尚未确认。请先读取原记录核对；如需重试，本页只会发送原来的同一请求，不更换退款号或幂等键。"
      type="warning"
      :closable="false"
    />
    <el-alert
      v-if="!canSubmit"
      title="当前服务端未允许此操作。可返回原记录，按允许的操作继续核查；本页已保留尚未确认的原请求。"
      type="warning"
      :closable="false"
    />
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <template #footer>
      <el-button :loading="reloading" :disabled="busy" @click="refresh">重新读取当前事实</el-button>
      <el-button
        type="primary"
        :loading="busy"
        :disabled="stale || reloading || !canSubmit"
        @click="submit"
        >{{ unknown ? '沿用原请求重试' : '确认提交' }}</el-button
      >
    </template>
  </el-dialog>
</template>
<style scoped>
.target {
  margin: 18px 0;
  overflow-wrap: anywhere;
  color: #64748b;
}
.el-alert + .el-alert {
  margin-top: 12px;
}
</style>
