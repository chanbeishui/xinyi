<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import { readVoucher, uploadVoucher, type Voucher } from '@/api/rongli/privateFile';
const props = defineProps<{
  orderNo: string;
  modelValue?: Voucher[];
  fileIds?: string[];
  editable?: boolean;
  disabled?: boolean;
}>();
const emit = defineEmits<{
  (event: 'update:modelValue', value: Voucher[]): void;
  (event: 'busy', value: boolean): void;
}>();
const busy = ref(false),
  error = ref(''),
  image = ref(''),
  imageOpen = ref(false);
let sequence = 0;
onBeforeUnmount(() => {
  sequence++;
  release();
});
function release() {
  if (image.value) URL.revokeObjectURL(image.value);
  image.value = '';
}
async function preview(id: string) {
  if (busy.value) return;
  const run = ++sequence;
  error.value = '';
  try {
    const url = await readVoucher(id, props.orderNo);
    if (run !== sequence) {
      URL.revokeObjectURL(url);
      return;
    }
    release();
    image.value = url;
    imageOpen.value = true;
  } catch (failure: any) {
    if (run === sequence) error.value = failure.msg || failure.message || '凭证读取失败';
  }
}
async function select(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file || busy.value || props.disabled || (props.modelValue?.length || 0) >= 9) return;
  busy.value = true;
  emit('busy', true);
  error.value = '';
  const run = ++sequence;
  try {
    const voucher = await uploadVoucher(props.orderNo, file);
    if (run === sequence) emit('update:modelValue', [...(props.modelValue || []), voucher]);
  } catch (failure: any) {
    if (run === sequence) error.value = failure.msg || failure.message || '上传失败';
  } finally {
    if (run === sequence) {
      busy.value = false;
      emit('busy', false);
    }
  }
}
function remove(id: string) {
  emit(
    'update:modelValue',
    (props.modelValue || []).filter((file: Voucher) => file.fileId !== id)
  );
}
</script>
<template>
  <div class="voucher-files">
    <p v-if="!fileIds?.length && !modelValue?.length" class="muted">未附凭证图片</p>
    <div v-for="(id, index) in fileIds" :key="id" class="voucher-row">
      <el-button v-hasPermi="['biz:files:access']" link type="primary" @click="preview(id)"
        >查看凭证 {{ index + 1 }}</el-button
      >
    </div>
    <div v-for="(file, index) in modelValue" :key="file.fileId" class="voucher-row">
      <el-button v-hasPermi="['biz:files:access']" link type="primary" @click="preview(file.fileId)"
        >查看已上传凭证 {{ index + 1 }}</el-button
      >
      <el-button
        v-if="editable"
        link
        type="danger"
        :disabled="disabled || busy"
        @click="remove(file.fileId)"
        >移除</el-button
      >
    </div>
    <template v-if="editable">
      <label class="upload-control">
        <span>{{ busy ? '上传并核验中…' : '选择凭证图片' }}</span>
        <input
          type="file"
          accept="image/jpeg,image/png"
          :disabled="disabled || busy || (modelValue?.length || 0) >= 9"
          aria-label="选择凭证图片"
          @change="select"
        />
      </label>
      <p class="muted">JPEG / PNG，每张不超过 10MB，最多 9 张。服务端核验通过后才能提交。</p>
    </template>
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <el-dialog
      v-model="imageOpen"
      title="私有业务凭证"
      width="min(820px, 94vw)"
      append-to-body
      @closed="release"
    >
      <img v-if="image" :src="image" class="private-image" alt="当前售后凭证" />
    </el-dialog>
  </div>
</template>
<style scoped>
.voucher-files {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.voucher-row {
  display: flex;
  gap: 16px;
  align-items: center;
}
.muted {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
  margin: 0;
}
.upload-control {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 14px;
}
.upload-control input {
  max-width: 100%;
}
.private-image {
  display: block;
  max-width: 100%;
  max-height: 70vh;
  margin: 0 auto;
  object-fit: contain;
}
</style>
