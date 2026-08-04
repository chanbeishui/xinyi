<template>
  <div
    class="digital-twin-frame"
    :style="{ '--digital-twin-reserved-height': `${reservedHeight}px` }"
  >
    <iframe
      :src="src"
      :title="title"
      allow="fullscreen"
      allowfullscreen
      referrerpolicy="same-origin"
    />
  </div>
</template>

<script setup lang="ts">
import useSettingsStore from '@/store/modules/settings'

defineProps<{
  src: string
  title: string
}>()

const settingsStore = useSettingsStore()

const reservedHeight = computed(() => {
  const headerHeight = 50 + (settingsStore.tagsView ? 34 : 0)
  const footerHeight = settingsStore.footerVisible ? 36 : 0
  return headerHeight + footerHeight
})
</script>

<style scoped>
.digital-twin-frame {
  width: 100%;
  height: calc(100vh - var(--digital-twin-reserved-height));
  height: calc(100dvh - var(--digital-twin-reserved-height));
  min-height: 520px;
  overflow: hidden;
  background: #040b12;
}

.digital-twin-frame iframe {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
  background: #040b12;
}

@media screen and (max-width: 991px) {
  .digital-twin-frame {
    min-height: 480px;
  }
}
</style>
