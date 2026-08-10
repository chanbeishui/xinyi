<template>
  <div class="bm-map">
    <svg
      class="bm-svg"
      :class="{ 'is-dragging': isDragging }"
      :viewBox="viewBox"
      @wheel.prevent="onMapWheel"
      @mousedown="handleMapMouseDown"
      @mousemove="onMouseMove"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    >
      <defs>
        <pattern id="bmGrid" width="36" height="36" patternUnits="userSpaceOnUse">
          <path d="M36 0H0V36" fill="none" stroke="rgba(93,190,202,0.06)" stroke-width="1" />
        </pattern>
        <pattern id="bmMicroGrid" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="1" cy="1" r="0.55" fill="rgba(98,213,218,0.09)" />
        </pattern>
        <radialGradient id="bmMapField" cx="68%" cy="48%" r="68%">
          <stop offset="0%" stop-color="#12323b" />
          <stop offset="52%" stop-color="#091b22" />
          <stop offset="100%" stop-color="#040a0f" />
        </radialGradient>
        <filter id="bmGlow" x="-80%" y="-80%" width="260%" height="260%">
          <feGaussianBlur stdDeviation="4" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
        <filter id="bmSoftGlow" x="-80%" y="-80%" width="260%" height="260%">
          <feGaussianBlur stdDeviation="8" />
        </filter>
      </defs>

      <rect x="-900" y="-900" width="2800" height="2500" fill="url(#bmMapField)" />
      <rect x="-900" y="-900" width="2800" height="2500" fill="url(#bmGrid)" />
      <rect x="-900" y="-900" width="2800" height="2500" fill="url(#bmMicroGrid)" />

      <!-- 完整中国轮廓仅作为地理背景，内部行政边界只绘制广东和广西。 -->
      <path v-if="level.context" :d="countryPath" class="bm-country-base" />

      <!-- 主视野：省级仅画粤桂地级市，茂名级仅画县级区域。 -->
      <g class="bm-geo" :key="`geo-${level.code}`">
        <path :d="outlinePath" class="bm-outline-halo" filter="url(#bmSoftGlow)" />
        <g class="bm-areas">
          <g
            v-for="area in regionShapes"
            :key="area.name"
            class="bm-area"
            :data-region="area.name"
            :class="{
              'is-active': activeRegion === area.name,
              'is-idle': !area.hasBiz,
              'is-guangdong': area.group === '广东',
              'is-guangxi': area.group === '广西'
            }"
            @click.stop="handleRegionSelect(area.name)"
          >
            <path :d="area.d" class="bm-area-path" />
          </g>
        </g>
        <path :d="outlinePath" class="bm-outline" />

        <g
          v-for="group in groupLabels"
          :key="group.name"
          v-show="showGroupLabels"
          :transform="stableTransform(group.pt)"
        >
          <text :x="group.pt.x" :y="group.pt.y" class="bm-group-watermark">{{ group.name }}</text>
        </g>
      </g>

      <!-- 所有地级市/县级区域都显示地标；茂名地标沿用同一视觉但承担下钻。 -->
      <g class="bm-points" :key="`points-${level.code}`">
        <g
          v-for="p in regionLabels"
          :key="p.code"
          class="bm-point"
          :data-region-point="p.name"
          :class="{
            'is-idle': !p.hasBiz,
            'is-active': activeRegion === p.name,
            'is-drill': p.isDrill
          }"
          :transform="stableTransform(p.pt)"
          @mousedown.stop
          @click.stop="handlePointSelect(p.name, p.isDrill)"
        >
          <title>{{ p.isDrill ? `${p.name}，点击进入市级视图` : p.name }}</title>
          <line
            v-if="p.lead && (showRegionLabels || activeRegion === p.name)"
            :x1="p.pt.x"
            :y1="p.pt.y"
            :x2="p.labelPt.x"
            :y2="p.labelPt.y + 4"
            class="bm-point-lead"
          />
          <circle :cx="p.pt.x" :cy="p.pt.y" r="3.6" class="bm-point-marker" />
          <text
            v-if="showRegionLabels || activeRegion === p.name"
            :x="p.labelPt.x"
            :y="p.labelPt.y"
            class="bm-area-label"
          >
            {{ p.name }}
          </text>
        </g>
      </g>

      <!-- 所有弧线统一从信宜中心扩散。 -->
      <g class="bm-arcs" :key="`arc-${level.code}-${replayKey}`">
        <g
          v-for="(arc, i) in arcShapes"
          :key="arc.site.id"
          class="bm-arc"
          :class="{ 'is-dim': isDimmed(arc.site), 'is-active': isActive(arc.site) }"
          :style="{ '--arc-color': arc.color, '--arc-delay': `${i * 0.1}s` }"
        >
          <path :id="arc.pathId" :d="arc.d" class="bm-arc-base" pathLength="1" :style="{ strokeWidth: arc.width }" />
          <circle :r="1.8 / Math.max(1, scale)" class="bm-comet" :style="{ '--comet-delay': `${0.9 + i * 0.1}s` }">
            <animateMotion :dur="arc.dur" :begin="`${0.9 + i * 0.1}s`" repeatCount="indefinite" calcMode="linear">
              <mpath :href="`#${arc.pathId}`" :xlink:href="`#${arc.pathId}`" />
            </animateMotion>
          </circle>
        </g>
      </g>

      <!-- 县级/乡镇业务点。 -->
      <g class="bm-sites" :key="`site-${level.code}-${replayKey}`">
        <g
          v-for="(node, i) in arcShapes"
          :key="node.site.id"
          class="bm-site"
          :class="{ 'is-dim': isDimmed(node.site), 'is-active': isActive(node.site) }"
          :style="{ '--arc-color': node.color, '--status-color': node.statusColor, '--arc-delay': `${0.72 + i * 0.1}s` }"
          :transform="stableTransform(node.pt)"
          @click.stop="emit('select', node.site)"
        >
          <circle :cx="node.pt.x" :cy="node.pt.y" r="7" class="bm-site-hit" />
          <circle :cx="node.pt.x" :cy="node.pt.y" :r="node.r" class="bm-site-core" />
          <g v-if="showSiteLabels || selectedId === node.site.id" class="bm-site-label">
            <polyline v-if="node.lead" :points="node.leadPoints" class="bm-site-lead" />
            <text
              :x="node.labelX"
              :y="node.labelY"
              :text-anchor="node.labelLeft ? 'end' : 'start'"
              class="bm-site-name"
            >
              {{ node.label }}
            </text>
            <text
              v-if="selectedId === node.site.id"
              :x="node.labelX"
              :y="node.labelY + 12"
              :text-anchor="node.labelLeft ? 'end' : 'start'"
              class="bm-site-value"
            >
              {{ node.site.projects }} 项 · {{ node.site.amount }} 万
            </text>
          </g>
        </g>
      </g>

      <!-- 信宜业务中心。 -->
      <g class="bm-center" :transform="stableTransform(centerPt)">
        <circle :cx="centerPt.x" :cy="centerPt.y" r="4.8" class="bm-center-core" />
        <text :x="centerPt.x" :y="centerPt.y - 11" class="bm-center-name">信宜</text>
      </g>

      <!-- 地标热区置于业务节点之上，避免重叠点位抢走城市点击。 -->
      <g class="bm-point-hit-layer">
        <circle
          v-for="p in regionLabels"
          :key="`hit-${p.code}`"
          :cx="p.pt.x"
          :cy="p.pt.y"
          r="8"
          class="bm-point-hit"
          :transform="stableTransform(p.pt)"
          :aria-label="p.isDrill ? `进入${p.name}地图` : `筛选${p.name}`"
          @mousedown.stop
          @click.stop="handlePointSelect(p.name, p.isDrill)"
        >
          <title>{{ p.isDrill ? `${p.name}，点击进入市级视图` : p.name }}</title>
        </circle>
      </g>
    </svg>

    <div class="bm-legend">
      <div v-if="level.context" class="bm-region-key">
        <span><i class="is-gd" />广东地级市</span>
        <span><i class="is-gx" />广西地级市</span>
      </div>
      <div class="bm-legend-title">业务类型</div>
      <div v-for="item in typeLegend" :key="item.key" class="bm-legend-item">
        <i :style="{ background: item.color }" />{{ item.key }}<em>{{ item.count }}</em>
      </div>
      <div class="bm-legend-title bm-legend-gap">运行状态（数据）</div>
      <div class="bm-legend-row">
        <span v-for="item in statusLegend" :key="item.key"><i :style="{ borderColor: item.color }" />{{ item.label }}</span>
      </div>
      <div class="bm-legend-tip">白色描边统一定位，圆心颜色表示业务类型</div>
    </div>

    <div class="bm-note">
      <span class="bm-note-sub">行政边界为前端演示数据，正式发布前请替换为合规地图数据</span>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { createProjection, arcPath, type Point } from '../composables/useGeoProjection'
import { useMapViewport } from '../composables/useMapViewport'
import type { MapLevel } from '../data/geoData'
import { statusMeta, typeMeta, bizTypes, type BizSite, type BizType, type SiteStatus } from '../data/mockData'

const W = 1000
const H = 660

const props = defineProps<{
  level: MapLevel
  center: { name: string; label: string; center: [number, number] }
  sites: BizSite[]
  selectedId: string | null
  activeRegion: string | null
  activeType?: BizType | null
  replayKey: number
  drillName?: string
}>()

const emit = defineEmits<{
  select: [site: BizSite]
  drill: []
  region: [name: string]
  interact: []
}>()

const {
  viewBox,
  scale,
  scalePercent,
  isDragging,
  onWheel: viewportWheel,
  onMouseDown: viewportMouseDown,
  onMouseMove,
  onMouseUp,
  zoomIn: viewportZoomIn,
  zoomOut: viewportZoomOut,
  reset,
  focusOn,
  fitBounds
} =
  useMapViewport(W, H, 0.6, 12)

const projection = computed(() => {
  const detailRings = [...props.level.outlineRings, ...props.level.areas.flatMap((area) => area.rings)]
  const rings = props.level.context?.rings?.length ? props.level.context.rings : detailRings
  return createProjection(rings, W, H, props.level.context ? 50 : 58)
})

function ringsPath(rings: [number, number][][], value = projection.value) {
  return rings.map((ring) => value.toPath(ring)).join(' ')
}

const outlinePath = computed(() => ringsPath(props.level.outlineRings))
const countryPath = computed(() => ringsPath(props.level.context?.rings || []))
const centerPt = computed(() => projection.value.project(props.center.center))

const overviewFocusBounds = computed(() => {
  const points = (props.level.context?.focusRings || []).flat().map((point) => projection.value.project(point))
  if (!points.length) return null
  return {
    minX: Math.min(...points.map((point) => point.x)),
    minY: Math.min(...points.map((point) => point.y)),
    maxX: Math.max(...points.map((point) => point.x)),
    maxY: Math.max(...points.map((point) => point.y))
  }
})

/** 地图几何正常缩放，标记和文字保持稳定屏幕尺寸，避免放大后字号失控。 */
function stableTransform(pt: Point) {
  const value = 1 / Math.max(1, scale.value)
  return `translate(${pt.x} ${pt.y}) scale(${value}) translate(${-pt.x} ${-pt.y})`
}

/** 全国层按缩放级别逐步展示信息，避免地级市、县级点一次性重叠。 */
const showGroupLabels = computed(() => !!props.level.context && scale.value < 1.65)
const showRegionLabels = computed(() => !props.level.context || scale.value >= 1.65)
const showSiteLabels = computed(() => (props.level.context ? scale.value >= 4.2 : scale.value >= 1.2))

const bizRegions = computed(() => new Set(props.sites.map((site) => site.region)))

const regionShapes = computed(() =>
  props.level.areas.map((area) => ({
    name: area.name,
    group: area.group,
    d: ringsPath(area.rings),
    hasBiz: bizRegions.value.has(area.name)
  }))
)

const groupLabels = computed(() => {
  if (!props.level.context) return []
  return [
    { name: '广西', pt: projection.value.project([108.65, 25.45]) },
    { name: '广东', pt: projection.value.project([113.75, 25.35]) }
  ]
})

interface RegionLabelBox {
  x: number
  y: number
  width: number
}

function layoutRegionLabel(pt: Point, name: string, placed: RegionLabelBox[]) {
  const zoom = Math.max(1, scale.value)
  const origin = { x: pt.x * zoom, y: pt.y * zoom }
  const width = Math.max(26, name.length * 10)
  const candidates = [
    { x: origin.x, y: origin.y - 9 },
    { x: origin.x, y: origin.y + 15 },
    { x: origin.x - width * 0.58, y: origin.y - 5 },
    { x: origin.x + width * 0.58, y: origin.y - 5 },
    { x: origin.x, y: origin.y - 21 },
    { x: origin.x, y: origin.y + 27 }
  ]
  const value = candidates.find(
    (candidate) =>
      !placed.some(
        (item) => Math.abs(item.x - candidate.x) < (item.width + width) / 2 + 3 && Math.abs(item.y - candidate.y) < 11
      )
  ) || candidates[candidates.length - 1]
  placed.push({ ...value, width })
  const labelPt = { x: pt.x + value.x - origin.x, y: pt.y + value.y - origin.y }
  return { labelPt, lead: Math.abs(value.x - origin.x) > 4 || Math.abs(value.y - (origin.y - 9)) > 4 }
}

const regionLabels = computed(() => {
  const placed: RegionLabelBox[] = []
  return props.level.points
    .filter((point) => point.name !== props.center.name)
    .map((point) => ({ ...point, pt: projection.value.project(point.center) }))
    .sort((a, b) => a.pt.y - b.pt.y || a.pt.x - b.pt.x)
    .map((point) => ({
      ...point,
      ...layoutRegionLabel(point.pt, point.name, placed),
      hasBiz: bizRegions.value.has(point.name),
      isDrill: point.name === props.drillName
    }))
})

const amountRange = computed(() => {
  const values = props.sites.map((site) => site.amount)
  return { min: Math.min(...values), max: Math.max(...values) }
})

function normalize(amount: number) {
  const { min, max } = amountRange.value
  return max === min ? 0.5 : (amount - min) / (max - min)
}

interface LabelBox {
  x: number
  y: number
  width: number
  height: number
}

function layoutSiteLabel(pt: Point, radius: number, labelWidth: number, preferLeft: boolean, placed: LabelBox[]) {
  const zoom = Math.max(1, scale.value)
  const origin = { x: pt.x * zoom, y: pt.y * zoom }
  const width = labelWidth
  const height = 15
  const sides = preferLeft ? [true, false] : [false, true]
  let best: { box: LabelBox; left: boolean; dy: number; score: number } | null = null
  for (const dy of [0, -16, 16, -32, 32, -48, 48, -64, 64, -80, 80, -96, 96]) {
    for (const left of sides) {
      const anchor = origin.x + (left ? -(radius + 8) : radius + 8)
      const box = { x: left ? anchor - width / 2 : anchor + width / 2, y: origin.y + dy, width, height }
      const score = placed.reduce((total, item) => {
        const overlapX = Math.max(0, (item.width + width) / 2 + 4 - Math.abs(item.x - box.x))
        const overlapY = Math.max(0, (item.height + height) / 2 + 4 - Math.abs(item.y - box.y))
        return total + overlapX * overlapY
      }, 0)
      if (score === 0) {
        placed.push(box)
        return { labelLeft: left, labelDy: dy, lead: Math.abs(dy) > 12 }
      }
      if (!best || score < best.score) best = { box, left, dy, score }
    }
  }
  const fallback = best || {
    box: { x: origin.x, y: origin.y, width, height },
    left: preferLeft,
    dy: 0,
    score: 0
  }
  placed.push(fallback.box)
  return { labelLeft: fallback.left, labelDy: fallback.dy, lead: Math.abs(fallback.dy) > 12 }
}

const arcShapes = computed(() => {
  const nodes = props.sites.map((site) => {
    const k = normalize(site.amount)
    return { site, pt: projection.value.project(site.center), k, r: 2.6 + k * 1.5 }
  })
  const zoom = Math.max(1, scale.value)
  const placed: LabelBox[] = showRegionLabels.value
    ? regionLabels.value.map((label) => ({
        x: label.pt.x * zoom + label.labelPt.x - label.pt.x,
        y: label.pt.y * zoom + label.labelPt.y - label.pt.y,
        width: Math.max(38, label.name.length * 14),
        height: 16
      }))
    : []
  placed.push({
    x: centerPt.value.x * zoom,
    y: centerPt.value.y * zoom - 11,
    width: 76,
    height: 16
  })
  const layouts = new Map<string, ReturnType<typeof layoutSiteLabel>>()
  ;[...nodes]
    .sort((a, b) => a.pt.y - b.pt.y)
    .forEach((node) =>
      layouts.set(
        node.site.id,
        layoutSiteLabel(node.pt, node.r, Math.max(48, node.site.county.length * 14), node.pt.x > W * 0.68, placed)
      )
    )

  return nodes.map(({ site, pt, k, r }) => {
    const layout = layouts.get(site.id)!
    const labelX = pt.x + (layout.labelLeft ? -(r + 9) : r + 9)
    const labelY = pt.y - 2 + layout.labelDy
    const leadX = pt.x + (layout.labelLeft ? -(r + 5) : r + 5)
    return {
      site,
      pathId: `bm-arc-${site.id}`,
      d: arcPath(centerPt.value, pt, props.level.context ? 0.2 : 0.26),
      pt,
      color: typeMeta[site.type],
      statusColor: statusMeta[site.status].color,
      r,
      width: 1 + k * 1.5,
      dur: `${(2.2 + site.distance / 230).toFixed(2)}s`,
      ...layout,
      labelX,
      labelY,
      leadPoints: `${leadX},${pt.y} ${leadX},${labelY} ${labelX},${labelY}`,
      label: site.county
    }
  })
})

const typeLegend = computed(() =>
  bizTypes.map((key) => ({ key, color: typeMeta[key], count: props.sites.filter((site) => site.type === key).length }))
)

const statusLegend = computed(() =>
  (Object.keys(statusMeta) as SiteStatus[]).map((key) => ({ key, label: statusMeta[key].label, color: statusMeta[key].color }))
)

function isActive(site: BizSite) {
  if (props.selectedId) return props.selectedId === site.id
  if (props.activeRegion) return site.region === props.activeRegion
  if (props.activeType) return site.type === props.activeType
  return false
}

function isDimmed(site: BizSite) {
  if (props.activeType && site.type !== props.activeType) return true
  if (props.selectedId) return props.selectedId !== site.id
  if (props.activeRegion) return site.region !== props.activeRegion
  return false
}

function handleRegionSelect(name: string) {
  emit('region', name)
}

function handlePointSelect(name: string, isDrill: boolean) {
  if (isDrill) emit('drill')
  else emit('region', name)
}

function zoomIn() {
  emit('interact')
  if (props.level.context && scale.value < 2.2) {
    focusProvinceOverview()
    return
  }
  viewportZoomIn()
}

function zoomOut() {
  emit('interact')
  viewportZoomOut()
}

function focusProvinceOverview() {
  if (overviewFocusBounds.value) fitBounds(overviewFocusBounds.value, 26, 460)
}

function onMapWheel(event: WheelEvent) {
  emit('interact')
  if (props.level.context && scale.value < 2.2 && event.deltaY < 0) {
    focusProvinceOverview()
    return
  }
  viewportWheel(event)
}

function handleMapMouseDown(event: MouseEvent) {
  emit('interact')
  viewportMouseDown(event)
}

function focusSite(site: BizSite, targetScale = 2.1) {
  const pt = projection.value.project(site.center)
  focusOn(pt.x, pt.y, targetScale)
}

function focusRegion(name: string, targetScale = 2.8) {
  const region = props.level.points.find((point) => point.name === name)
  if (!region) return
  const pt = projection.value.project(region.center)
  focusOn(pt.x, pt.y, targetScale)
}

defineExpose({ zoomIn, zoomOut, reset, focusSite, focusRegion, scalePercent })
</script>

<style lang="scss" scoped>
.bm-map {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 520px;
  overflow: hidden;
  border: 1px solid rgba(86, 193, 204, 0.2);
  border-radius: 3px;
  background: #040a0f;

  &::before {
    position: absolute;
    z-index: 1;
    inset: 0;
    content: '';
    pointer-events: none;
    box-shadow: inset 0 0 56px rgba(0, 0, 0, 0.42);
  }
}

.bm-svg {
  display: block;
  width: 100%;
  height: 100%;
  cursor: grab;

  :focus {
    outline: none !important;
  }

  &.is-dragging {
    cursor: grabbing;
  }
}

.bm-country-base {
  fill: rgba(23, 52, 62, 0.5);
  stroke: rgba(108, 176, 184, 0.72);
  stroke-width: 1.05;
  stroke-linejoin: round;
  vector-effect: non-scaling-stroke;
  pointer-events: none;
  filter: drop-shadow(0 0 8px rgba(55, 157, 164, 0.12));
}

.bm-geo,
.bm-points {
  animation: bm-map-enter 0.62s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}

@keyframes bm-map-enter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bm-outline-halo {
  fill: none;
  stroke: rgba(89, 224, 228, 0.5);
  stroke-width: 3;
  pointer-events: none;
}

.bm-outline {
  fill: none;
  stroke: #68e5e4;
  stroke-width: 1.55;
  stroke-linejoin: round;
  vector-effect: non-scaling-stroke;
  pointer-events: none;
}

.bm-area {
  cursor: pointer;
  outline: none !important;

  .bm-area-path {
    fill: rgba(28, 78, 96, 0.56);
    stroke: rgba(114, 188, 198, 0.56);
    stroke-width: 0.9;
    vector-effect: non-scaling-stroke;
    transition: fill 0.22s ease, opacity 0.22s ease, stroke 0.22s ease, filter 0.22s ease;
  }

  &.is-guangxi .bm-area-path {
    fill: rgba(24, 89, 78, 0.55);
    stroke: rgba(99, 193, 170, 0.55);
  }

  &.is-idle .bm-area-path {
    opacity: 0.7;
  }

  &:hover .bm-area-path {
    fill: rgba(48, 153, 165, 0.58);
    stroke: #b9fffb;
    stroke-width: 1.4;
    filter: drop-shadow(0 0 5px rgba(82, 216, 222, 0.42));
  }

  &.is-active .bm-area-path {
    fill: rgba(244, 184, 63, 0.56);
    stroke: #ffe6a0;
    stroke-width: 1.7;
    filter: drop-shadow(0 0 8px rgba(244, 184, 63, 0.48));
  }
}

.bm-group-watermark {
  fill: rgba(211, 245, 244, 0.78);
  font-size: 12px;
  font-weight: 650;
  text-anchor: middle;
  pointer-events: none;
  paint-order: stroke;
  stroke: rgba(3, 15, 20, 0.9);
  stroke-width: 2;
  letter-spacing: 0;
  text-rendering: geometricPrecision;
}

.bm-point {
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover,
  &.is-active {
    .bm-point-marker {
      fill: #ffd166;
      stroke: #fff;
      filter: drop-shadow(0 0 4px rgba(255, 209, 102, 0.76));
    }

    .bm-area-label {
      fill: #fff;
      font-weight: 750;
    }
  }

  &.is-idle:not(:hover):not(.is-active) {
    .bm-point-marker {
      fill: rgba(34, 56, 62, 0.9);
      stroke: #fff;
    }
  }

}

.bm-point-marker {
  fill: #75e6e1;
  stroke: #fff;
  stroke-width: 1;
  transition: fill 0.2s ease, stroke 0.2s ease, filter 0.2s ease;
}

.bm-point-hit {
  fill: transparent;
  pointer-events: none;
}

.bm-point-lead {
  stroke: rgba(144, 208, 211, 0.46);
  stroke-width: 0.8;
  vector-effect: non-scaling-stroke;
}

.bm-area-label {
  fill: rgba(239, 252, 252, 0.96);
  font-size: 10.5px;
  font-weight: 680;
  text-anchor: middle;
  pointer-events: none;
  paint-order: stroke;
  stroke: rgba(2, 12, 17, 0.88);
  stroke-width: 2;
  stroke-linejoin: round;
  letter-spacing: 0;
  text-rendering: geometricPrecision;
}

.bm-arc {
  pointer-events: none;
  transition: opacity 0.28s ease;

  &.is-dim {
    opacity: 0.24;
  }

  &.is-active .bm-arc-base {
    stroke-opacity: 1;
    filter: drop-shadow(0 0 6px var(--arc-color));
  }
}

.bm-arc-base {
  fill: none;
}

.bm-arc-base {
  stroke: var(--arc-color);
  stroke-opacity: 0.64;
  stroke-linecap: round;
  vector-effect: non-scaling-stroke;
  animation: bm-fade-in 0.45s ease-out var(--arc-delay) backwards;
}

.bm-comet {
  fill: #fff;
  filter: drop-shadow(0 0 6px var(--arc-color));
  animation: bm-fade-in 0.4s ease-out var(--comet-delay) backwards;
}

@keyframes bm-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.bm-site {
  cursor: pointer;
  animation: bm-pop 0.48s ease-out var(--arc-delay) backwards;
  transition: opacity 0.28s ease;

  &.is-dim { opacity: 0.16; }

  &.is-active,
  &:hover {
    .bm-site-core { filter: drop-shadow(0 0 8px var(--arc-color)); }
    .bm-site-name { fill: #fff; font-weight: 750; }
    .bm-site-value { opacity: 1; }
  }
}

.bm-site-core {
  fill: var(--arc-color);
  stroke: #fff;
  stroke-width: 1.35;
}

.bm-site-hit {
  fill: transparent;
  pointer-events: all;
}

.bm-site-lead {
  fill: none;
  stroke: rgba(150, 214, 230, 0.58);
  stroke-width: 0.8;
  pointer-events: none;
}

.bm-site-name,
.bm-site-value {
  pointer-events: visiblePainted;
  paint-order: stroke;
  stroke: rgba(2, 11, 17, 0.9);
  stroke-width: 2;
  stroke-linejoin: round;
  letter-spacing: 0;
  text-rendering: geometricPrecision;
}

.bm-site-name {
  fill: rgba(244, 253, 255, 0.98);
  font-size: 10.5px;
  font-weight: 680;
}

.bm-site-value {
  fill: rgba(157, 215, 230, 0.88);
  font-size: 8.5px;
  opacity: 0.9;
  transition: opacity 0.18s ease;
}

@keyframes bm-pop {
  from { opacity: 0; }
  to { opacity: 1; }
}

.bm-center-core {
  fill: #ffd166;
  stroke: #fff;
  stroke-width: 1.4;
  filter: drop-shadow(0 0 5px rgba(255, 209, 102, 0.78));
}

.bm-center-name {
  fill: #ffe5a0;
  text-anchor: middle;
  paint-order: stroke;
  stroke: rgba(2, 11, 17, 0.9);
  stroke-width: 2;
  stroke-linejoin: round;
  pointer-events: none;
  letter-spacing: 0;
  text-rendering: geometricPrecision;
}

.bm-center-name { font-size: 11px; font-weight: 720; }

.bm-legend {
  position: absolute;
  z-index: 2;
  top: 14px;
  left: 14px;
  width: 188px;
  padding: 10px 12px;
  border: 1px solid rgba(91, 201, 208, 0.24);
  border-radius: 4px;
  color: rgba(224, 243, 244, 0.86);
  background: rgba(5, 17, 22, 0.88);
  backdrop-filter: blur(8px);
  font-size: 10.5px;
  line-height: 1.72;
  pointer-events: none;
}

.bm-region-key {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 5px;
  margin-bottom: 7px;
  padding-bottom: 7px;
  border-bottom: 1px solid rgba(100, 182, 190, 0.18);

  span { display: flex; align-items: center; gap: 5px; }
  i { width: 11px; height: 7px; border: 1px solid transparent; }
  .is-gd { background: rgba(28, 78, 96, 0.8); border-color: rgba(114, 188, 198, 0.72); }
  .is-gx { background: rgba(24, 89, 78, 0.8); border-color: rgba(99, 193, 170, 0.72); }
}

.bm-legend-title {
  margin-bottom: 2px;
  color: rgba(145, 213, 216, 0.78);
  font-size: 10px;
  font-weight: 650;
}

.bm-legend-gap { margin-top: 6px; }

.bm-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;

  > i { width: 7px; height: 7px; border-radius: 50%; box-shadow: 0 0 6px currentColor; }
  em { margin-left: auto; color: #f2fbfb; font-style: normal; font-weight: 700; }
}

.bm-legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 3px 8px;

  span { display: flex; align-items: center; gap: 4px; }
  i { width: 7px; height: 7px; border: 1px solid; border-radius: 50%; }
}

.bm-legend-tip {
  margin-top: 6px;
  padding-top: 5px;
  border-top: 1px dashed rgba(109, 184, 190, 0.2);
  color: rgba(143, 175, 181, 0.72);
  font-size: 9px;
}

.bm-note {
  position: absolute;
  right: 14px;
  bottom: 12px;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  color: rgba(158, 191, 196, 0.62);
  font-size: 10px;
  line-height: 1.6;
  pointer-events: none;
}

.bm-note-sub { color: rgba(123, 154, 160, 0.5); }

@media (max-width: 1180px) {
  .bm-map { min-height: 500px; }
  .bm-note { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .bm-geo,
  .bm-points,
  .bm-site,
  .bm-arc-base,
  .bm-comet {
    animation: none !important;
  }
}
</style>
