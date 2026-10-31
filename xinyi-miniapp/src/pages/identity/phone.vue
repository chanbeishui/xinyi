<script setup lang="ts">
import { ref } from 'vue';
import { bindPhone, queryPhoneVerification, getProfile } from '../../features/identity/api';
import type { PhoneBindResult } from '../../api/types';

// O25 手机号授权：用户主动 getPhoneNumber → 服务端核验 + 自动识别员工；结果不明查回执，不重复消费凭据。
type State = 'IDLE' | 'PROCESSING' | 'VERIFIED' | 'FAILED' | 'UNKNOWN' | 'DENIED';
const state = ref<State>('IDLE');
const message = ref('');
const result = ref<PhoneBindResult | null>(null);
let requestId = '';

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

async function onGetPhone(e: any) {
  const phoneCode: string | undefined = e?.detail?.code;
  if (!phoneCode) {
    state.value = 'DENIED';
    message.value = '已取消手机号授权，可稍后重试。';
    return;
  }
  state.value = 'PROCESSING';
  message.value = '';
  requestId = uuid();
  try {
    const profile = await getProfile();
    const r = await bindPhone(phoneCode, profile.version, requestId);
    result.value = r;
    state.value = r.verificationStatus as State;
    if (r.verificationStatus === 'PROCESSING') {
      await poll();
    }
  } catch (err) {
    state.value = 'FAILED';
    message.value = err instanceof Error ? err.message : '手机号授权失败，请重试';
  }
}

async function poll() {
  for (let i = 0; i < 5 && state.value === 'PROCESSING'; i++) {
    await new Promise((r) => setTimeout(r, 1000));
    await refresh();
  }
}

async function refresh() {
  if (!requestId) return;
  try {
    const r = await queryPhoneVerification(requestId);
    result.value = r;
    state.value = r.verificationStatus as State;
  } catch (err) {
    message.value = err instanceof Error ? err.message : '查询失败';
  }
}

function back() {
  uni.reLaunch({ url: '/pages/index/index' });
}
</script>

<template>
  <view class="screen">
    <text class="title">手机号授权</text>
    <text class="desc">用于识别你是否为检测员工。手机号核验与员工关联结果分开，不会影响你的车主身份。</text>

    <block v-if="state === 'IDLE' || state === 'DENIED' || state === 'FAILED'">
      <button class="primary" open-type="getPhoneNumber" @getphonenumber="onGetPhone">微信手机号一键授权</button>
      <text v-if="state === 'DENIED'" class="warn">{{ message }}</text>
      <text v-if="state === 'FAILED'" class="warn">{{ message || '授权失败，请重试' }}</text>
    </block>

    <block v-else-if="state === 'PROCESSING'">
      <text class="status">正在核验…</text>
      <button class="plain" @click="refresh">查询处理结果</button>
    </block>

    <block v-else-if="state === 'UNKNOWN'">
      <text class="status">结果尚未确认</text>
      <text class="desc">不会重复消费授权凭据，可查询本次结果或重新授权。</text>
      <button class="plain" @click="refresh">查询处理结果</button>
    </block>

    <block v-else-if="state === 'VERIFIED' && result">
      <text class="status">手机号已核验 {{ result.phoneMask || '' }}</text>
      <text class="kv">员工识别：{{ result.employeeLink.status }}</text>
      <button class="primary" @click="back">完成</button>
    </block>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 18px; }
.title { font-size: 24px; font-weight: 600; }
.desc, .status, .kv { font-size: 16px; line-height: 1.7; color: #333; }
.warn { font-size: 15px; color: #c0392b; line-height: 1.6; }
.primary { margin: 8px 0; background: #005fcb; color: #fff; }
.plain { margin: 8px 0; background: #eef2f7; color: #005fcb; }
</style>
