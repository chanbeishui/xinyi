<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { checkPermi } from '@/utils/permission';
import { getToken } from '@/utils/auth';
import {
  readQualificationEvidence,
  type QualificationDetail,
  type EvidenceField
} from '@/api/rongli/qualification';
const props = defineProps<{ detail: QualificationDetail }>();
const emit = defineEmits<{ (event: 'permission-lost', message: string): void }>();
const purpose = ref(''),
  error = ref(''),
  url = ref(''),
  selectedLabel = ref(''),
  busy = ref(false);
const fields: Array<{ field: EvidenceField; label: string }> = [
  { field: 'idCardFrontFileId', label: '身份证人像面' },
  { field: 'idCardBackFileId', label: '身份证国徽面' },
  { field: 'licenseMainFileId', label: '驾驶证主页' },
  { field: 'licenseSubFileId', label: '驾驶证副页' }
];
// 框架权限数组表示“任一满足”；原图须四项全部满足，服务端仍逐项检查原公司的真实范围。
const allowed = computed(
  () =>
    props.detail.hasTrustedSubmission &&
    Boolean(props.detail.draft) &&
    [
      'biz:driverqualification:query',
      'biz:driverqualification:sensitive',
      'biz:files:query',
      'biz:files:access'
    ].every((value) => checkPermi([value]))
);
const token = getToken();
let sequence = 0,
  disposed = false;
function clearImage() {
  sequence++;
  if (url.value) URL.revokeObjectURL(url.value);
  url.value = '';
  selectedLabel.value = '';
  busy.value = false;
}
watch(
  () => [props.detail.applicationId, props.detail.submissionId, props.detail.version],
  () => {
    clearImage();
    purpose.value = '';
    error.value = '';
  }
);
watch(allowed, (value: boolean) => {
  if (!value) {
    clearImage();
    purpose.value = '';
  }
});
async function preview(field: EvidenceField, label: string) {
  if (!allowed.value || busy.value) return;
  clearImage();
  error.value = '';
  if (getToken() !== token) {
    error.value = '登录已变化，请重新读取审核材料';
    emit('permission-lost', error.value);
    return;
  }
  if (!purpose.value.trim()) {
    error.value = '查看证件原图前，请填写本次用途与原因';
    return;
  }
  const id = props.detail.draft?.[field];
  if (!id) {
    error.value = '本次可信提交未包含此证件';
    return;
  }
  const run = sequence,
    applicationId = props.detail.applicationId,
    submissionId = props.detail.submissionId;
  busy.value = true;
  try {
    const result = await readQualificationEvidence(id, purpose.value.trim());
    if (
      disposed ||
      run !== sequence ||
      getToken() !== token ||
      props.detail.applicationId !== applicationId ||
      props.detail.submissionId !== submissionId ||
      !allowed.value
    ) {
      URL.revokeObjectURL(result);
      return;
    }
    url.value = result;
    selectedLabel.value = label;
  } catch (e) {
    if (!disposed && run === sequence) {
      error.value =
        (e as { msg?: string; message?: string })?.msg ||
        (e as { message?: string })?.message ||
        '证件读取失败';
      if ([401, 403, 404].includes(Number((e as { httpStatus?: number })?.httpStatus)))
        emit('permission-lost', error.value);
    }
  } finally {
    if (!disposed && run === sequence) busy.value = false;
  }
}
onBeforeUnmount(() => {
  clearImage();
  purpose.value = '';
  disposed = true;
});
</script>
<template>
  <section class="qualification-evidence">
    <h3>本次冻结提交的证件</h3>
    <p v-if="!detail.hasTrustedSubmission || !detail.draft" class="muted">
      暂无可认证的提交快照，不能展示草稿或猜测历史材料。
    </p>
    <template v-else>
      <p class="muted">
        证件使用独立敏感权限和原公司范围授权。请记录本次用途；图片仅供当前查看，关闭或换申请时清除。
      </p>
      <p v-if="!allowed" class="muted">
        查看原图需资格查询、敏感查看及文件查询、访问权限，当前未全部具备。
      </p>
      <el-input
        v-model="purpose"
        :disabled="!allowed || busy"
        type="textarea"
        maxlength="500"
        placeholder="填写本次证件查看用途与原因，最多 500 字"
      />
      <div class="buttons">
        <el-button
          v-for="item in fields"
          :key="item.field"
          :disabled="!allowed || busy || !detail.draft?.[item.field]"
          @click="preview(item.field, item.label)"
          >查看{{ item.label }}</el-button
        >
      </div>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <p v-if="busy" class="muted">正在获取受控临时授权并读取证件…</p>
      <div v-if="url" class="image-view">
        <div class="image-heading">
          <strong>{{ selectedLabel }}</strong
          ><el-button @click="clearImage">结束本次查看</el-button>
        </div>
        <img :src="url" :alt="selectedLabel" />
      </div>
    </template>
  </section>
</template>
<style scoped>
.qualification-evidence {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
h3 {
  margin: 0;
  font-size: 17px;
}
.muted {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.buttons :deep(.el-button) {
  margin-left: 0;
}
.error {
  color: #b42318;
  line-height: 1.8;
}
.image-view {
  padding: 14px;
  background: #f5f7fa;
  border-radius: 8px;
}
.image-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
img {
  display: block;
  max-width: 100%;
  max-height: 620px;
  margin: 0 auto;
  object-fit: contain;
}
</style>
