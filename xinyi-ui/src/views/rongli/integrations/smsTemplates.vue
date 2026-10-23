<template>
  <div>
    <el-card shadow="never" class="intro">
      <div class="title">短信场景模板（B49）</div>
      <div class="hint">
        每个业务场景绑定一个已审核通过的供应商模板；本地只登记「场景 → 模板 ID → 参数映射」，不替代供应商模板审批。
        短信与订阅消息走两套独立模板，互不替代：短信可覆盖全部场景，订阅消息仅覆盖微信允许的一次性订阅场景。
        模板未发布时该场景的发送会被明确拒绝，不会静默漏发。
      </div>
    </el-card>
    <TemplateWorkbench provider="TENCENT_SMS" />
  </div>
</template>

<script setup lang="ts">
import TemplateWorkbench from './components/TemplateWorkbench.vue'
</script>

<style scoped>
.intro { margin-bottom: 12px; }
.title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.hint { font-size: 12px; line-height: 1.7; color: var(--el-text-color-secondary); }
</style>
