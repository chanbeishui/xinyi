<script setup lang="ts">
import { useIdentityLogin } from '../../features/identity/useIdentityLogin';
const { state, message, session, employeeHint, login, enterApp } = useIdentityLogin();
</script>

<template>
  <view class="screen">
    <text class="title">机动车检测预约</text>
    <text class="desc">微信一键登录，首次进入默认为车主。代驾需申请审核，检测员工由后台配置站点授权。</text>

    <block v-if="state === 'IDLE' || state === 'DENIED' || state === 'ERROR'">
      <button class="primary" :disabled="false" @click="login">微信登录</button>
      <text v-if="state === 'DENIED'" class="warn">你已取消授权，可重新登录。</text>
      <text v-if="state === 'ERROR'" class="warn">{{ message || '登录失败，请重试' }}</text>
    </block>

    <block v-else-if="state === 'PROCESSING'">
      <text class="status">正在登录…</text>
    </block>

    <block v-else-if="state === 'SUCCESS' && session">
      <text class="status">登录成功</text>
      <text class="kv">当前视角：{{ session.activeRole }}</text>
      <text class="kv">可用身份：{{ session.availableRoles.join(' / ') }}</text>
      <text v-if="employeeHint" class="kv">{{ employeeHint }}</text>
      <button class="primary" @click="enterApp">进入车主首页</button>
    </block>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 26px; font-weight: 600; }
.desc, .status, .kv { font-size: 16px; line-height: 1.7; color: #333; }
.warn { font-size: 15px; color: #c0392b; line-height: 1.6; }
.primary { margin: 8px 0; background: #005fcb; color: #fff; }
</style>
