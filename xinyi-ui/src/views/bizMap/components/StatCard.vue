<template>
  <div class="bm-stat" :style="{ '--stat-color': color }">
    <div class="bm-stat-icon">
      <svg-icon :icon-class="icon" />
    </div>
    <div class="bm-stat-info">
      <div class="bm-stat-label">{{ label }}</div>
      <div class="bm-stat-value">
        {{ display }}<em>{{ unit }}</em>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'

const props = withDefaults(
  defineProps<{
    label: string
    value: number
    unit?: string
    icon?: string
    color?: string
    duration?: number
  }>(),
  { unit: '', icon: 'chart', color: '#1677ff', duration: 900 }
)

const display = ref(0)
let raf = 0

/** 数字滚动动画：从当前值缓动到目标值 */
function runTween(target: number) {
  cancelAnimationFrame(raf)
  const from = display.value
  const start = performance.now()
  const step = (now: number) => {
    const t = Math.min(1, (now - start) / props.duration)
    const e = 1 - Math.pow(1 - t, 3)
    display.value = Math.round(from + (target - from) * e)
    if (t < 1) raf = requestAnimationFrame(step)
  }
  raf = requestAnimationFrame(step)
}

onMounted(() => runTween(props.value))
watch(() => props.value, runTween)
onBeforeUnmount(() => cancelAnimationFrame(raf))
</script>

<style lang="scss" scoped>
.bm-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--el-bg-color, #fff);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-left: 3px solid var(--stat-color);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }
}

.bm-stat-icon {
  position: relative;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--stat-color);

  /* 用半透明遮层代替 rgba 混色，指标色只需传一个变量 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 10px;
    background: var(--stat-color);
    opacity: 0.12;
  }

  :deep(.svg-icon) {
    position: relative;
  }
}

.bm-stat-info {
  min-width: 0;
}

.bm-stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.bm-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f1f1f);
  line-height: 1.2;

  em {
    font-style: normal;
    font-size: 12px;
    font-weight: 400;
    margin-left: 3px;
    color: var(--el-text-color-secondary, #8c8c8c);
  }
}
</style>

<style lang="scss" scoped>
.bm-stat {
  position: relative;
  min-width: 0;
  min-height: 78px;
  gap: 10px;
  padding: 12px 14px;
  border: 0;
  border-right: 1px solid rgba(103, 184, 192, 0.14);
  border-left: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;

  &::after {
    position: absolute;
    right: 14px;
    bottom: 0;
    left: 14px;
    height: 1px;
    content: '';
    background: linear-gradient(90deg, var(--stat-color), transparent 72%);
    opacity: 0.7;
  }

  &:hover {
    z-index: 1;
    transform: none;
    background: color-mix(in srgb, var(--stat-color) 6%, transparent);
    box-shadow: inset 0 0 22px color-mix(in srgb, var(--stat-color) 5%, transparent);
  }
}

.bm-stat-icon {
  width: 34px;
  height: 34px;
  flex-basis: 34px;
  border: 1px solid color-mix(in srgb, var(--stat-color) 38%, transparent);
  border-radius: 3px;
  font-size: 16px;

  &::before {
    border-radius: 2px;
    opacity: 0.09;
  }
}

.bm-stat-label {
  overflow: hidden;
  color: #789196;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bm-stat-value {
  margin-top: 2px;
  overflow: hidden;
  color: #eef7f7;
  font-size: 22px;
  font-variant-numeric: tabular-nums;
  text-overflow: ellipsis;
  white-space: nowrap;

  em {
    color: #718a8e;
    font-size: 11px;
  }
}

@media (max-width: 1520px) {
  .bm-stat:nth-child(4n) {
    border-right: 0;
  }
}

@media (max-width: 760px) {
  .bm-stat:nth-child(2n) {
    border-right: 0;
  }
}
</style>
