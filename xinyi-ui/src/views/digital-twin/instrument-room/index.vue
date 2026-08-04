<template>
  <digital-twin-frame :src="instrumentRoomUrl" title="智慧仪器室" />
</template>

<script setup name="InstrumentRoomDigitalTwin" lang="ts">
import DigitalTwinFrame from '@/components/DigitalTwinFrame/index.vue'

const instrumentRoomUrl = `${import.meta.env.BASE_URL}digital-twin/instrument-room/index.html`
</script>
