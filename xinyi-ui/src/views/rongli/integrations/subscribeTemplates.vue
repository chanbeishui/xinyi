<template>
  <div>
    <el-card shadow="never" class="intro">
      <div class="title">订阅消息模板（B50）</div>
      <div class="hint">
        订阅消息依赖客户端在具体操作后主动授权，一次授权只对应一次发送机会：后台不能凭空补发，也不能把「已展示授权框」当作已授权。
        客户端回传的 accept/reject 仅作为授权结果记录（绑定 accountId + AppID + 模板版本 + nonce，短时有效、只消费一次），
        用户拒绝不阻断业务，业务结果仍照常返回，改用短信或站内通知兜底。
        模板 ID 必须是微信后台已审核通过的正式模板 ID，测试模板 ID 只能在测试环境白名单内使用。
      </div>
    </el-card>
    <TemplateWorkbench provider="WECHAT_SUBSCRIBE" />
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
