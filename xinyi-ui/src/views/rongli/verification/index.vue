<script setup lang="ts">
import { ref } from 'vue';
import ManualRecords from './ManualRecords.vue';
import DeviceSessions from './DeviceSessions.vue';
const tab = ref('manual');
</script>
<template>
  <div class="app-container verification-page">
    <h2>本机认证与人工核验</h2>
    <p class="description">
      本机生物认证仅确认当前设备使用者；背景与驾驶证由经办确认后交另一位有权限的人员复核，之后仍需最终资格审核。
    </p>
    <el-tabs v-model="tab">
      <el-tab-pane label="人工核验记录" name="manual"><ManualRecords /></el-tab-pane>
      <el-tab-pane label="本机认证会话" name="device"><DeviceSessions /></el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
.description {
  color: #606266;
  line-height: 1.8;
}
.verification-page {
  min-width: 0;
}
h2 {
  margin-top: 0;
}
</style>
