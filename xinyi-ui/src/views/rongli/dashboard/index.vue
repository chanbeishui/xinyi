<template>
  <Home />
</template>

<script setup name="RongliDashboard" lang="ts">
/**
 * 运营工作台已并入首页（/index），这里只做旧地址兼容。
 *
 * 侧栏入口只保留「首页」一个；但 /rongli/business/dashboard 仍被角色授权、
 * 收藏夹和历史链接引用，直接删页会让这些入口 404。保留薄壳复用同一个组件，
 * 两个地址看到的是同一份数据和同一套排版，不再各维护一份。
 */
import Home from '@/views/index.vue'
</script>
