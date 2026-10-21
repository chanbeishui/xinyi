<script setup lang="ts">
import { ref } from 'vue';
import { request } from '../../services/http';
const status = ref('尚未连接真实后端');
async function check() { try { const r = await request<{code:number;data:{companyName:string}}>('GET','/api/public/service-config'); status.value = r.data.companyName + ' · API可达'; } catch(e) { status.value = e instanceof Error ? e.message : '连接失败'; } }
</script>
<template><view class="screen"><text class="title">工程联通检查</text><text class="desc">这是可编译基座，不是已完成的预约首页。业务页面按照任务包接入真实接口。</text><button @click="check">检查后端连接</button><text class="status">{{ status }}</text></view></template>
<style scoped>.screen{padding:28px 20px;display:flex;flex-direction:column;gap:22px}.title{font-size:26px;font-weight:600}.desc,.status{font-size:16px;line-height:1.7}button{margin:0;background:#005fcb;color:white}</style>
