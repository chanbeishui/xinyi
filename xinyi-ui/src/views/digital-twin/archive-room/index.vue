<template>
  <digital-twin-frame :src="archiveRoomUrl" title="数字档案室" />
</template>

<script setup name="ArchiveRoomDigitalTwin" lang="ts">
import DigitalTwinFrame from '@/components/DigitalTwinFrame/index.vue'

const archiveRoomUrl = `${import.meta.env.BASE_URL}digital-twin/archive-room/index.html`
</script>
