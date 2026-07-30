<template>
  <div class="archive-plan" ref="containerRef">
    <svg
      :viewBox="panzoom.viewBoxStr.value"
      preserveAspectRatio="xMidYMid meet"
      @wheel.prevent="panzoom.onWheel"
      @mousedown="panzoom.onMouseDown"
      @mousemove="panzoom.onMouseMove"
      @mouseup="panzoom.onMouseUp"
      @mouseleave="panzoom.onMouseUp"
    >
      <!-- 房间外墙 -->
      <rect x="20" y="20" width="960" height="580" class="room-wall" rx="8" />

      <!-- 走廊 -->
      <rect x="40" y="248" width="920" height="64" class="corridor" rx="4" />
      <text x="500" y="286" class="corridor-label" text-anchor="middle">走 廊</text>

      <!-- 门（底部开口 + 弧线） -->
      <line x1="460" y1="600" x2="540" y2="600" class="door-gap" />
      <path d="M 460 600 A 40 40 0 0 1 540 600" class="door-arc" fill="none" />
      <text x="500" y="614" class="door-label" text-anchor="middle">门</text>

      <!-- 窗户（左右墙） -->
      <line x1="20" y1="140" x2="20" y2="200" class="window-line" stroke-width="6" />
      <line x1="20" y1="400" x2="20" y2="460" class="window-line" stroke-width="6" />
      <line x1="980" y1="140" x2="980" y2="200" class="window-line" stroke-width="6" />
      <line x1="980" y1="400" x2="980" y2="460" class="window-line" stroke-width="6" />

      <!-- 区标注 -->
      <text v-for="zc in zoneConfigs" :key="zc.zone"
        :x="zc.startX" :y="zc.startY - 14" class="zone-label" :fill="zc.color">
        {{ zc.label }}
      </text>

      <!-- 柜子 -->
      <g v-for="cab in cabinets" :key="cab.id"
         :class="['cabinet', { highlighted: highlightedId === cab.id }]"
         @click.stop="emit('select', cab)">
        <rect :x="cab.x" :y="cab.y" :width="cab.width" :height="cab.height" rx="5"
              :style="{ fill: zoneColorWithOpacity(cab.zone), stroke: zoneColor(cab.zone) }"
              stroke-width="1.5" />
        <text :x="cab.x + cab.width / 2" :y="cab.y + cab.height / 2 - 4"
              text-anchor="middle" class="cab-id">{{ cab.label }}</text>
        <text :x="cab.x + cab.width / 2" :y="cab.y + cab.height / 2 + 12"
              text-anchor="middle" class="cab-sub">{{ cab.used }}/{{ cab.capacity }}</text>
        <!-- 使用率指示条 -->
        <rect :x="cab.x + 4" :y="cab.y + cab.height - 6" :width="cab.width - 8" height="3" rx="1.5"
              class="usage-bar-bg" />
        <rect :x="cab.x + 4" :y="cab.y + cab.height - 6"
              :width="(cab.width - 8) * (cab.used / cab.capacity)" height="3" rx="1.5"
              :fill="usageColor(cab.used / cab.capacity)" />
      </g>

      <!-- 图例（动态生成，适配不同档案室的区数量） -->
      <g class="legend" :transform="`translate(${legendX}, ${legendY})`">
        <rect x="0" y="0" width="300" :height="legendHeight" class="legend-bg" rx="6" />
        <text x="12" y="18" class="legend-title">图例</text>
        <template v-for="(zc, i) in zoneConfigs" :key="zc.zone">
          <rect :x="i % 2 === 0 ? 12 : 155" :y="28 + Math.floor(i / 2) * 18" width="14" height="10" :fill="zc.color" rx="2" />
          <text :x="(i % 2 === 0 ? 12 : 155) + 18" :y="28 + Math.floor(i / 2) * 18 + 9" class="legend-text">{{ zoneShortLabel(zc.label) }}</text>
        </template>
        <rect x="12" :y="usageY" width="14" height="3" :fill="usageColor(0.5)" rx="1" />
        <text x="30" :y="usageY + 9" class="legend-text">使用率</text>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useSvgPanZoom } from '../composables/useSvgPanZoom'
import { zoneColor as findZoneColor, type Cabinet, type ZoneConfig } from '../data/mockData'

const props = defineProps<{
  cabinets: Cabinet[]
  highlightedId: string | null
  zoneConfigs: ZoneConfig[]
}>()

const emit = defineEmits<{
  select: [cab: Cabinet]
}>()

const containerRef = ref<HTMLElement>()
const panzoom = useSvgPanZoom(1000, 620)

function zoneColor(zone: string): string {
  return findZoneColor(zone, props.zoneConfigs)
}

function zoneColorWithOpacity(zone: string): string {
  const c = zoneColor(zone)
  const hexMap: Record<string, string> = {
    '#1677ff': 'rgba(22,119,255,0.12)',
    '#faad14': 'rgba(250,173,20,0.12)',
    '#13c2c2': 'rgba(19,194,194,0.12)',
    '#722ed1': 'rgba(114,46,209,0.12)',
  }
  return hexMap[c] || 'rgba(140,140,140,0.12)'
}

// 图例动态布局
const legendRows = computed(() => Math.ceil(props.zoneConfigs.length / 2))
const legendHeight = computed(() => 28 + legendRows.value * 18 + 20)
const legendY = computed(() => 600 - legendHeight.value - 10)
const legendX = 680
const usageY = computed(() => 28 + legendRows.value * 18)

function zoneShortLabel(label: string): string {
  // "A 区 · 材料检测部" → "材料检测部"
  const parts = label.split('·')
  return parts.length > 1 ? parts[1].trim() : label
}

function usageColor(ratio: number): string {
  if (ratio >= 0.85) return '#ff4d4f'
  if (ratio >= 0.65) return '#faad14'
  return '#52c41a'
}

defineExpose({
  focusOn: (cx: number, cy: number, s?: number) => panzoom.focusOn(cx, cy, s),
  zoomIn: () => panzoom.zoomIn(),
  zoomOut: () => panzoom.zoomOut(),
  resetView: () => panzoom.reset(),
  scalePercent: panzoom.scalePercent,
})
</script>

<style lang="scss" scoped>
.archive-plan {
  width: 100%;
  height: 100%;
  min-height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 100%;
    height: 100%;
    cursor: grab;
    user-select: none;
  }
}

.room-wall {
  fill: var(--el-bg-color, #fff);
  stroke: var(--el-border-color, #d9d9d9);
  stroke-width: 3;
}

.corridor {
  fill: rgba(22, 119, 255, 0.04);
  stroke: rgba(22, 119, 255, 0.15);
  stroke-width: 1;
  stroke-dasharray: 6 4;
}

.corridor-label {
  fill: var(--el-text-color-secondary, #8c8c8c);
  font-size: 14px;
  letter-spacing: 4px;
}

.door-gap {
  stroke: var(--el-bg-color, #fff);
  stroke-width: 5;
}

.door-arc {
  stroke: var(--el-text-color-secondary, #8c8c8c);
  stroke-width: 1.5;
  stroke-dasharray: 3 2;
}

.door-label {
  fill: var(--el-text-color-secondary, #8c8c8c);
  font-size: 11px;
}

.window-line {
  stroke: rgba(22, 119, 255, 0.3);
  stroke-linecap: round;
}

.zone-label {
  font-size: 13px;
  font-weight: 700;
}

.cabinet {
  rect {
    transition: all 0.3s ease;
    cursor: pointer;
  }

  &:hover rect {
    filter: brightness(1.15);
    stroke-width: 2.5;
  }

  &.highlighted rect {
    stroke: #ff4d4f !important;
    stroke-width: 3.5 !important;
    animation: pulse-highlight 1s ease-in-out 3;
  }
}

.cab-id {
  fill: var(--el-text-color-primary, #1f1f1f);
  font-size: 14px;
  font-weight: 700;
  pointer-events: none;
}

.cab-sub {
  fill: var(--el-text-color-secondary, #8c8c8c);
  font-size: 10px;
  pointer-events: none;
}

.usage-bar-bg {
  fill: rgba(0, 0, 0, 0.06);
}

.legend-bg {
  fill: var(--el-bg-color, #fff);
  stroke: var(--el-border-color-lighter, #f0f0f0);
  stroke-width: 1;
}

.legend-title {
  fill: var(--el-text-color-secondary, #8c8c8c);
  font-size: 11px;
  font-weight: 600;
}

.legend-text {
  fill: var(--el-text-color-secondary, #8c8c8c);
  font-size: 10px;
}

@keyframes pulse-highlight {
  0%, 100% {
    stroke-opacity: 1;
    filter: drop-shadow(0 0 0 transparent);
  }
  50% {
    stroke-opacity: 0.6;
    filter: drop-shadow(0 0 10px #ff4d4f);
  }
}
</style>
