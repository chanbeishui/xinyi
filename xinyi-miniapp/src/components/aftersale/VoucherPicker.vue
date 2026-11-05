<script setup lang="ts">
import { ref } from 'vue';
import { chooseAndUploadVoucher, previewVoucher, type Voucher } from '../../features/file/api';
const props = defineProps<{ modelValue: Voucher[]; orderNo: string; disabled?: boolean }>();
const emit = defineEmits<{
  (event: 'update:modelValue', value: Voucher[]): void;
  (event: 'busy', value: boolean): void;
}>();
const uploading = ref(false),
  error = ref('');
async function add() {
  if (uploading.value || props.disabled || props.modelValue.length >= 9) return;
  uploading.value = true;
  emit('busy', true);
  error.value = '';
  const orderNo = props.orderNo;
  try {
    const file = await chooseAndUploadVoucher(orderNo);
    if (orderNo === props.orderNo && !props.disabled)
      emit('update:modelValue', [...props.modelValue, file]);
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : '上传失败';
  } finally {
    uploading.value = false;
    emit('busy', false);
  }
}
async function preview(fileId: string) {
  error.value = '';
  try {
    await previewVoucher(fileId, props.orderNo);
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : '查看失败';
  }
}
function remove(fileId: string) {
  if (!props.disabled && !uploading.value)
    emit(
      'update:modelValue',
      props.modelValue.filter((file) => file.fileId !== fileId)
    );
}
</script>
<template>
  <view class="vouchers">
    <text class="label">凭证图片（选填）</text>
    <text class="muted">JPEG / PNG，每张不超过 10MB，最多 9 张。上传核验完成后才能提交。</text>
    <view v-for="(file, index) in modelValue" :key="file.fileId" class="file-row">
      <button class="file-name" @click="preview(file.fileId)">查看凭证 {{ index + 1 }}</button>
      <button :disabled="disabled || uploading" class="remove" @click="remove(file.fileId)">
        移除
      </button>
    </view>
    <button :disabled="disabled || uploading || modelValue.length >= 9" @click="add">
      {{ uploading ? '正在上传并核验…' : '添加凭证图片' }}
    </button>
    <text v-if="error" class="error" role="alert">{{ error }}</text>
  </view>
</template>
<style scoped>
.vouchers {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.label {
  font-weight: 600;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}
.file-row {
  display: flex;
  gap: 10px;
}
.file-name {
  flex: 1;
  color: #005fcb;
}
button {
  font-size: 14px;
  margin: 0;
}
.remove {
  color: #b42318;
}
.error {
  color: #b42318;
  font-size: 14px;
}
</style>
