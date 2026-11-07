<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import RefundDrawer from './RefundDrawer.vue';
const route = useRoute();
const refundNo = ref(''),
  selected = ref(''),
  open = ref(false),
  error = ref('');
watch(
  () => route.query.refundNo,
  (value: unknown) => {
    if (typeof value === 'string' && value) {
      refundNo.value = value;
      show();
    }
  },
  { immediate: true }
);
function show() {
  error.value = '';
  const no = refundNo.value.trim();
  if (!no || no.length > 64) {
    error.value = '请填写有效的原退款号';
    return;
  }
  selected.value = no;
  open.value = true;
}
</script>
<template>
  <div class="app-container refund-page">
    <h2>退款执行查询</h2>
    <p class="muted">按原退款号核对已批准的退款决定、渠道状态和资金预留。</p>
    <el-alert
      title="退款执行沿用原支付方法、原收款主体与原退款号。本页不新增人工退款审核；线下实际退款凭证由独立核实流程处理。"
      type="info"
      :closable="false"
      show-icon
    />
    <el-form class="search" label-position="top" @submit.prevent="show"
      ><el-form-item label="原退款号"
        ><el-input
          v-model="refundNo"
          maxlength="64"
          placeholder="从售后决定查看或输入完整退款号"
          clearable /></el-form-item
      ><el-button v-hasPermi="['biz:refund:query']" type="primary" native-type="submit"
        >读取原退款单</el-button
      ></el-form
    >
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <RefundDrawer v-model="open" :refund-no="selected" />
  </div>
</template>
<style scoped>
.refund-page {
  max-width: 1040px;
}
.muted {
  color: #64748b;
  line-height: 1.7;
}
.search {
  max-width: 620px;
  margin-top: 24px;
}
</style>
