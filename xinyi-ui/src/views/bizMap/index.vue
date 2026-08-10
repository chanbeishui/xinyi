<template>
  <div class="biz-map-screen" ref="screenRef" :class="{ 'is-fullscreen': isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="ds-header">
      <div class="ds-header-top">
        <div class="ds-header-left">
          <span class="ds-header-date">{{ currentDate }}</span>
          <span class="ds-header-clock">{{ currentTime }}</span>
        </div>
        <div class="ds-header-center">
          <h1>区域业务态势中心</h1>
          <p>业务点分布图</p>
        </div>
        <div class="ds-header-right">
          <el-button class="ds-ghost-btn" size="small" @click="toggleFullscreen">
            <svg-icon :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" style="margin-right: 4px" />
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
        </div>
      </div>

      <div class="ds-header-switch">
        <span class="switch-label">行政层级：</span>
        <el-radio-group v-model="levelKey" size="small">
          <el-radio-button v-for="lv in mapLevels" :key="lv.key" :value="lv.key">
            {{ lv.name }}
          </el-radio-button>
        </el-radio-group>

        <div class="switch-crumb">
          <span :class="{ 'is-active': levelKey === 'province' }" @click="levelKey = 'province'">全国 · 粤桂区域</span>
          <i>›</i>
          <span :class="{ 'is-active': levelKey === 'city' }" @click="levelKey = 'city'">茂名市</span>
          <i>›</i>
          <em>信宜市（业务中心）</em>
        </div>

        <div class="switch-actions">
          <span class="switch-tour">
            <el-switch v-model="autoTour" size="small" inline-prompt active-text="巡检" inactive-text="巡检" />
          </span>
          <el-button class="ds-ghost-btn" size="small" @click="replay">
            <el-icon style="margin-right: 4px"><Refresh /></el-icon>重播动画
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主体 -->
    <div class="bm-body">
      <!-- 左侧：筛选 + 排行 -->
      <div class="bm-side">
        <div class="bm-panel">
          <div class="bm-panel-title">
            业务类型
          <el-button v-if="activeType" link type="primary" size="small" @click="clearTypeFilter">清除</el-button>
          </div>
          <div class="bm-chips">
            <button
              v-for="item in typeList"
              :key="item.name"
              type="button"
              class="bm-chip is-type"
              :class="{ 'is-active': activeType === item.name }"
              :style="{ '--chip-color': item.color }"
              :aria-pressed="activeType === item.name"
              @click="toggleType(item.name)"
            >
              <i />{{ item.name }}<em>{{ item.count }}</em>
            </button>
          </div>
        </div>

        <div class="bm-panel">
          <div class="bm-panel-title">
            行政区筛选
          <el-button v-if="activeRegion" link type="primary" size="small" @click="clearRegionFilter">清除</el-button>
          </div>
          <div class="bm-chips">
            <button
              v-for="region in regionList"
              :key="region.name"
              type="button"
              class="bm-chip"
              :class="{ 'is-active': activeRegion === region.name, 'is-idle': region.count === 0 }"
              :aria-pressed="activeRegion === region.name"
              @click="toggleRegion(region.name)"
            >
              {{ region.name }}<em>{{ region.count }}</em>
            </button>
          </div>
        </div>

        <div class="bm-panel bm-panel-grow">
          <div class="bm-panel-title">
            业务点排行
            <span class="bm-panel-sub">按合同额</span>
          </div>
          <div class="bm-rank">
            <div
              v-for="(site, i) in rankList"
              :key="site.id"
              class="bm-rank-item"
              :class="{ 'is-active': selectedId === site.id }"
              @click="handleSelect(site)"
            >
              <span class="bm-rank-no" :class="`no-${Math.min(i + 1, 4)}`">{{ i + 1 }}</span>
              <div class="bm-rank-main">
                <div class="bm-rank-name">{{ site.name }}</div>
                <div class="bm-rank-bar">
                  <i :style="{ width: barWidth(site.amount), background: typeMeta[site.type] }" />
                </div>
              </div>
              <div class="bm-rank-value">
                {{ site.amount }}<em>万</em>
              </div>
            </div>
            <div v-if="rankList.length === 0" class="bm-rank-empty">
              <p>{{ emptyText }}</p>
              <el-button link type="primary" size="small" @click="clearFilters">清除筛选条件</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：地图 -->
      <div class="bm-main">
        <div class="bm-map-card">
          <div class="bm-map-head">
            <span class="bm-map-title">
              {{ currentLevel.name }}业务态势
              <em>{{ sites.length }} 个业务点 · 中心：信宜市</em>
            </span>
            <div class="bm-map-tools">
              <el-button-group>
                <el-button size="small" title="放大地图" aria-label="放大地图" @click="mapRef?.zoomIn()">
                  <el-icon><ZoomIn /></el-icon>
                </el-button>
                <el-button size="small" disabled>{{ mapRef?.scalePercent || '100%' }}</el-button>
                <el-button size="small" title="缩小地图" aria-label="缩小地图" @click="mapRef?.zoomOut()">
                  <el-icon><ZoomOut /></el-icon>
                </el-button>
              </el-button-group>
              <el-button size="small" title="重置地图" @click="resetView">
                <el-icon><RefreshRight /></el-icon>
                <span>重置</span>
              </el-button>
            </div>
          </div>
          <div class="bm-map-body">
            <RegionMap
              :key="currentLevel.code"
              ref="mapRef"
              :level="currentLevel"
              :center="CENTER_POINT"
              :sites="sites"
              :selected-id="selectedId"
              :active-region="activeRegion"
              :active-type="activeType"
              :replay-key="replayKey"
              :drill-name="levelKey === 'province' ? '茂名' : undefined"
              @select="handleSelect"
              @drill="handleDrill"
              @region="toggleRegion"
              @interact="pauseAutoTour"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：详情 + 动态 -->
      <div class="bm-side">
        <div class="bm-panel">
          <div class="bm-panel-title">业务点详情</div>
          <SitePanel :site="selectedSite" />
        </div>
        <div class="bm-panel bm-panel-grow">
          <div class="bm-panel-title">
            实时动态
            <span class="bm-panel-live"><i />接入中</span>
          </div>
          <div class="bm-feed">
            <div v-for="item in activities" :key="item.id" class="bm-feed-item" :class="`is-${item.level}`">
              <span class="bm-feed-time">{{ item.time }}</span>
              <span class="bm-feed-region">{{ item.region }}</span>
              <span class="bm-feed-text">{{ item.text }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部指标 -->
    <div class="bm-stats">
      <StatCard
        v-for="kpi in kpis"
        :key="kpi.key"
        :label="kpi.label"
        :value="kpi.value"
        :unit="kpi.unit"
        :icon="kpi.icon"
        :color="kpi.color"
      />
    </div>
  </div>
</template>

<script setup lang="ts" name="BizMap">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import RegionMap from './components/RegionMap.vue'
import SitePanel from './components/SitePanel.vue'
import StatCard from './components/StatCard.vue'
import { mapLevels, provinceLevel, cityLevel, CENTER_POINT } from './data/geoData'
import {
  provinceSites,
  citySites,
  typeMeta,
  bizTypes,
  buildKpis,
  initActivities,
  nextActivity,
  type BizSite,
  type BizType,
  type ActivityItem
} from './data/mockData'

const screenRef = ref<HTMLElement>()
const mapRef = ref<InstanceType<typeof RegionMap>>()
const isFullscreen = ref(false)

// ========== 层级与数据 ==========

const levelKey = ref<'province' | 'city'>('province')
const currentLevel = computed(() => (levelKey.value === 'province' ? provinceLevel : cityLevel))
const sites = computed(() => (levelKey.value === 'province' ? provinceSites : citySites))

const selectedId = ref<string | null>(null)
const activeRegion = ref<string | null>(null)
const activeType = ref<BizType | null>(null)
const replayKey = ref(0)

const selectedSite = computed(() => sites.value.find((s) => s.id === selectedId.value) || null)
const kpis = computed(() => buildKpis(sites.value))

/** 业务类型筛选项，颜色与地图弧线一致 */
const typeList = computed(() =>
  bizTypes.map((name) => ({
    name,
    color: typeMeta[name],
    count: sites.value.filter((s) => s.type === name).length
  }))
)

/**
 * 行政区筛选项：列出本级全部区划（含暂无业务的地市），
 * 有业务的排前面，方便与地图上的分区一一对应。
 */
const regionList = computed(() => {
  const count = new Map<string, number>()
  sites.value.forEach((s) => count.set(s.region, (count.get(s.region) || 0) + 1))
  return currentLevel.value.points
    .map((p) => ({ name: p.name, count: count.get(p.name) || 0 }))
    .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name, 'zh-CN'))
})

const rankList = computed(() => {
  let list = [...sites.value]
  if (activeRegion.value) list = list.filter((s) => s.region === activeRegion.value)
  if (activeType.value) list = list.filter((s) => s.type === activeType.value)
  return list.sort((a, b) => b.amount - a.amount)
})

const emptyText = computed(() => {
  if (activeRegion.value && activeType.value) return `${activeRegion.value}暂无${activeType.value}业务`
  if (activeRegion.value) return `${activeRegion.value}暂未开展业务`
  if (activeType.value) return `本级暂无${activeType.value}业务`
  return '暂无业务点数据'
})

const maxAmount = computed(() => Math.max(...sites.value.map((s) => s.amount)))

function barWidth(amount: number) {
  return `${Math.max(8, Math.round((amount / maxAmount.value) * 100))}%`
}

// ========== 时间 ==========

const now = ref(new Date())
const currentDate = computed(() =>
  now.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
)
const currentTime = computed(() => now.value.toLocaleTimeString('zh-CN', { hour12: false }))

// ========== 交互 ==========

function handleSelect(site: BizSite) {
  // 手动选点时暂停自动巡检，避免视角被抢
  autoTour.value = false
  activeRegion.value = null
  activeType.value = null
  selectedId.value = site.id
}

function toggleRegion(name: string) {
  autoTour.value = false
  selectedId.value = null
  activeType.value = null
  activeRegion.value = activeRegion.value === name ? null : name
}

function toggleType(name: BizType) {
  autoTour.value = false
  selectedId.value = null
  activeRegion.value = null
  activeType.value = activeType.value === name ? null : name
}

function clearRegionFilter() {
  activeRegion.value = null
  selectedId.value = null
}

function clearTypeFilter() {
  activeType.value = null
  selectedId.value = null
}

function clearFilters() {
  activeRegion.value = null
  activeType.value = null
}

function resetView() {
  pauseAutoTour()
  selectedId.value = null
  clearFilters()
  mapRef.value?.reset()
}

function replay() {
  replayKey.value += 1
}

function pauseAutoTour() {
  autoTour.value = false
}

let drillTimer: number | undefined

function handleDrill() {
  autoTour.value = false
  selectedId.value = null
  activeRegion.value = null
  mapRef.value?.focusRegion('茂名', 3.1)
  if (drillTimer) clearTimeout(drillTimer)
  drillTimer = window.setTimeout(() => {
    levelKey.value = 'city'
    drillTimer = undefined
  }, 620)
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    screenRef.value?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

// 切换层级：重置状态并重播弧线动画
watch(levelKey, () => {
  selectedId.value = null
  clearFilters()
  tourIndex = -1
  replayKey.value += 1
  nextTick(() => mapRef.value?.reset(false))
})

// ========== 自动巡检 ==========

const autoTour = ref(true)
let tourIndex = -1
let tourTimer: number | undefined
let tourStartTimer: number | undefined

function tourStep() {
  const list = sites.value
  if (list.length === 0) return
  tourIndex = (tourIndex + 1) % list.length
  const site = list[tourIndex]
  selectedId.value = site.id
  const targetScale = levelKey.value === 'province' ? 11 : 1.7
  mapRef.value?.focusSite(site, targetScale)
}

function startTour() {
  stopTour()
  tourTimer = window.setInterval(() => {
    if (autoTour.value) tourStep()
  }, 4000)
}

function stopTour() {
  if (tourTimer) {
    clearInterval(tourTimer)
    tourTimer = undefined
  }
}

watch(autoTour, (on) => {
  if (on) tourStep()
})

// ========== 实时动态 ==========

const activities = ref<ActivityItem[]>([])
let feedTimer: number | undefined
let clockTimer: number | undefined

onMounted(() => {
  activities.value = initActivities(sites.value)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  clockTimer = window.setInterval(() => (now.value = new Date()), 1000)
  feedTimer = window.setInterval(() => {
    activities.value = [nextActivity(sites.value), ...activities.value].slice(0, 30)
  }, 4200)
  // 首屏弧线绘制完成后再开始巡检
  tourStartTimer = window.setTimeout(startTour, 3200)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  stopTour()
  if (drillTimer) clearTimeout(drillTimer)
  if (tourStartTimer) clearTimeout(tourStartTimer)
  if (feedTimer) clearInterval(feedTimer)
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style lang="scss" scoped>
.biz-map-screen {
  padding: 16px;
  min-height: 100vh;
  background: var(--el-bg-color-page, #f0f2f5);
  color: var(--el-text-color-primary, #1f1f1f);

  &.is-fullscreen {
    overflow: auto;
    padding: 20px;
  }
}

/* ---------- 顶部 banner（沿用 dataScreen / archiveRoom 风格） ---------- */
.ds-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  padding: 18px 28px 14px;
  background: linear-gradient(135deg, #0958d9 0%, #1677ff 50%, #36cfc9 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(9, 88, 217, 0.25);

  .ds-header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .ds-header-left {
    flex: 0 0 auto;
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 13px;
    opacity: 0.9;
  }

  .ds-header-clock {
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 1px;
  }

  .ds-header-center {
    flex: 1;
    text-align: center;

    h1 {
      font-size: 22px;
      font-weight: 700;
      margin: 0;
    }

    p {
      font-size: 13px;
      opacity: 0.85;
      margin-top: 4px;
    }
  }

  .ds-header-right {
    flex: 0 0 auto;
  }
}

.ds-ghost-btn {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;

  &:hover,
  &:focus {
    background: rgba(255, 255, 255, 0.32);
    border-color: rgba(255, 255, 255, 0.5);
    color: #fff;
  }
}

.ds-header-switch {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);

  .switch-label {
    font-size: 13px;
    opacity: 0.85;
    white-space: nowrap;
  }

  :deep(.el-radio-group) {
    .el-radio-button__inner {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(255, 255, 255, 0.25);
      color: #fff;
      box-shadow: none;
    }

    .el-radio-button__original-radio:checked + .el-radio-button__inner {
      background: #fff;
      border-color: #fff;
      color: #1677ff;
      font-weight: 600;
      box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3);
    }
  }
}

.switch-crumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.9;

  span {
    cursor: pointer;
    padding: 1px 4px;
    border-radius: 4px;
    transition: background 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.18);
    }

    &.is-active {
      font-weight: 700;
      background: rgba(255, 255, 255, 0.22);
    }
  }

  i {
    font-style: normal;
    opacity: 0.6;
  }

  em {
    font-style: normal;
    color: #ffe9a8;
  }
}

.switch-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch-tour :deep(.el-switch__label) {
  color: #fff;
}

/* ---------- 主体三栏 ---------- */
.bm-body {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  align-items: stretch;
}

.bm-side {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.bm-main {
  flex: 1;
  min-width: 0;
}

.bm-panel {
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.bm-panel-grow {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.bm-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);

  .bm-panel-sub {
    font-size: 11px;
    font-weight: 400;
    color: var(--el-text-color-secondary, #8c8c8c);
  }

  :deep(.el-button) {
    margin-left: auto;
  }
}

.bm-panel-live {
  margin-left: auto;
  font-size: 11px;
  font-weight: 400;
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 4px;

  i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #52c41a;
    animation: bm-blink 1.4s ease-in-out infinite;
  }
}

@keyframes bm-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.2;
  }
}

/* ---------- 行政区筛选 ---------- */
.bm-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  /* 省级 21 个地市较多，超出后内部滚动，避免挤压地图高度 */
  max-height: 128px;
  overflow-y: auto;
}

.bm-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  background: var(--el-bg-color-page, #f5f7fa);
  color: var(--el-text-color-regular, #606266);
  border: 1px solid transparent;
  transition: all 0.2s ease;

  em {
    font-style: normal;
    font-size: 10px;
    opacity: 0.65;
  }

  &:hover {
    border-color: #1677ff;
    color: #1677ff;
  }

  &.is-active {
    background: #1677ff;
    border-color: #1677ff;
    color: #fff;
  }

  /* 暂无业务的地市仍可点击（地图上有分区），弱化显示 */
  &.is-idle {
    color: var(--el-text-color-placeholder, #a8abb2);

    em {
      opacity: 0.45;
    }
  }

  /* 业务类型 chip 用类型色标识 */
  &.is-type {
    i {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--chip-color);
    }

    &:hover {
      border-color: var(--chip-color);
      color: var(--chip-color);
    }

    &.is-active {
      background: var(--chip-color);
      border-color: var(--chip-color);
      color: #fff;

      i {
        background: #fff;
      }
    }
  }
}

/* ---------- 排行榜 ---------- */
.bm-rank {
  flex: 1;
  overflow-y: auto;
  max-height: 300px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bm-rank-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(22, 119, 255, 0.06);
  }

  &.is-active {
    background: rgba(22, 119, 255, 0.12);
  }
}

.bm-rank-empty {
  padding: 28px 0;
  text-align: center;
  color: var(--el-text-color-secondary, #8c8c8c);

  p {
    font-size: 12px;
    margin-bottom: 4px;
  }
}

.bm-rank-no {
  flex: 0 0 18px;
  height: 18px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #bfbfbf;

  &.no-1 {
    background: #ff4d4f;
  }
  &.no-2 {
    background: #faad14;
  }
  &.no-3 {
    background: #36cfc9;
  }
}

.bm-rank-main {
  flex: 1;
  min-width: 0;
}

.bm-rank-name {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.bm-rank-bar {
  height: 4px;
  border-radius: 2px;
  background: var(--el-bg-color-page, #f0f2f5);
  overflow: hidden;

  i {
    display: block;
    height: 100%;
    border-radius: 2px;
    transition: width 0.6s ease;
  }
}

.bm-rank-value {
  flex: 0 0 auto;
  font-size: 13px;
  font-weight: 700;

  em {
    font-style: normal;
    font-size: 10px;
    font-weight: 400;
    color: var(--el-text-color-secondary, #8c8c8c);
    margin-left: 1px;
  }
}

/* ---------- 地图卡片 ---------- */
.bm-map-card {
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  padding: 14px 16px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bm-map-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bm-map-title {
  font-size: 14px;
  font-weight: 600;

  em {
    font-style: normal;
    font-size: 11px;
    font-weight: 400;
    color: var(--el-text-color-secondary, #8c8c8c);
    margin-left: 8px;
  }
}

.bm-map-body {
  flex: 1;
  min-height: 560px;
}

/* ---------- 实时动态 ---------- */
.bm-feed {
  flex: 1;
  overflow-y: auto;
  max-height: 240px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bm-feed-item {
  padding: 6px 8px;
  border-radius: 6px;
  background: var(--el-bg-color-page, #f5f7fa);
  border-left: 2px solid #1677ff;
  font-size: 12px;
  line-height: 1.5;
  animation: bm-feed-in 0.35s ease-out;

  &.is-success {
    border-left-color: #52c41a;
  }
  &.is-warning {
    border-left-color: #ff4d4f;
  }
}

.bm-feed-time {
  color: var(--el-text-color-secondary, #8c8c8c);
  margin-right: 6px;
  font-size: 11px;
}

.bm-feed-region {
  color: #1677ff;
  margin-right: 6px;
}

.bm-feed-text {
  color: var(--el-text-color-regular, #606266);
}

@keyframes bm-feed-in {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ---------- 底部指标 ---------- */
.bm-stats {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 1600px) {
  .bm-stats {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1400px) {
  .bm-body {
    flex-wrap: wrap;
  }

  .bm-side {
    flex: 1 1 320px;
    order: 2;
  }

  .bm-main {
    flex: 1 1 100%;
    order: 1;
  }
}

@media (max-width: 992px) {
  .bm-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .ds-header-top {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }

  .ds-header-switch {
    flex-wrap: wrap;
  }

  .switch-actions {
    margin-left: 0;
  }
}
</style>

<style lang="scss" scoped>
/* Technology command-center theme */
.biz-map-screen {
  --bm-bg: #071014;
  --bm-surface: rgba(12, 23, 27, 0.94);
  --bm-surface-strong: #0d1a1f;
  --bm-line: rgba(103, 184, 192, 0.2);
  --bm-line-strong: rgba(82, 216, 222, 0.48);
  --bm-text: #eef8f8;
  --bm-muted: #8ba4a8;
  --bm-cyan: #55d8de;
  --bm-blue: #4a97f2;
  --bm-amber: #f4c45b;
  position: relative;
  min-height: 100vh;
  padding: 12px;
  overflow: hidden;
  color: var(--bm-text);
  background-color: var(--bm-bg);
  background-image:
    linear-gradient(rgba(76, 137, 144, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(76, 137, 144, 0.045) 1px, transparent 1px),
    linear-gradient(145deg, #071014 0%, #0b1519 52%, #080f14 100%);
  background-size: 32px 32px, 32px 32px, auto;

  &.is-fullscreen {
    padding: 14px;
    overflow: auto;
  }
}

.ds-header {
  position: relative;
  margin-bottom: 10px;
  padding: 12px 18px 10px;
  overflow: hidden;
  border: 1px solid var(--bm-line);
  border-radius: 6px;
  color: var(--bm-text);
  background: linear-gradient(110deg, rgba(15, 31, 35, 0.98), rgba(8, 19, 24, 0.98));
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.28), inset 0 1px rgba(255, 255, 255, 0.025);

  &::before {
    position: absolute;
    top: -1px;
    right: 16%;
    left: 16%;
    height: 2px;
    content: '';
    background: linear-gradient(90deg, transparent, var(--bm-cyan), var(--bm-amber), transparent);
    opacity: 0.82;
  }

  &::after {
    position: absolute;
    top: 0;
    bottom: 0;
    left: -28%;
    width: 18%;
    content: '';
    pointer-events: none;
    background: linear-gradient(90deg, transparent, rgba(85, 216, 222, 0.055), transparent);
    transform: skewX(-18deg);
    animation: bm-header-scan 8s linear infinite;
  }

  .ds-header-top {
    min-height: 52px;
  }

  .ds-header-left {
    min-width: 164px;
    gap: 4px;
    color: var(--bm-muted);
    font-family: Consolas, 'Microsoft YaHei', sans-serif;
    font-size: 12px;
    font-variant-numeric: tabular-nums;
  }

  .ds-header-date {
    color: #a8bcbf;
  }

  .ds-header-clock {
    color: var(--bm-cyan);
    font-size: 20px;
    letter-spacing: 0;
    text-shadow: 0 0 14px rgba(85, 216, 222, 0.3);
  }

  .ds-header-center h1 {
    margin: 0;
    color: #fff;
    font-size: 24px;
    font-weight: 760;
    letter-spacing: 0;
  }

  .ds-header-center p {
    margin: 5px 0 0;
    color: #8fa8ac;
    font-size: 12px;
    opacity: 1;
  }

  .ds-header-right {
    min-width: 164px;
    text-align: right;
  }
}

@keyframes bm-header-scan {
  to {
    left: 112%;
  }
}

.ds-ghost-btn {
  height: 30px;
  border-color: rgba(111, 187, 195, 0.28);
  border-radius: 4px;
  color: #cce3e5;
  background: rgba(63, 111, 119, 0.12);

  &:hover,
  &:focus {
    border-color: var(--bm-cyan);
    color: #fff;
    background: rgba(85, 216, 222, 0.12);
  }
}

.ds-header-switch {
  position: relative;
  z-index: 1;
  min-height: 34px;
  margin-top: 8px;
  padding-top: 9px;
  border-top-color: rgba(103, 184, 192, 0.14);

  .switch-label {
    color: var(--bm-muted);
    font-size: 12px;
    opacity: 1;
  }

  :deep(.el-radio-button__inner) {
    min-width: 76px;
    height: 28px;
    padding: 6px 12px;
    border-color: rgba(111, 187, 195, 0.22) !important;
    color: #a7bdc0 !important;
    background: #0a161b !important;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    border-color: var(--bm-cyan) !important;
    color: #061216 !important;
    background: var(--bm-cyan) !important;
    box-shadow: 0 0 16px rgba(85, 216, 222, 0.18) !important;
  }
}

.switch-crumb {
  color: #829a9e;
  font-size: 12px;
  opacity: 1;

  span {
    border-radius: 3px;

    &:hover,
    &.is-active {
      color: #dff8f8;
      background: rgba(85, 216, 222, 0.1);
    }
  }

  em {
    color: var(--bm-amber);
  }
}

.switch-tour {
  :deep(.el-switch__core) {
    border-color: rgba(111, 187, 195, 0.28);
    background: #14272c;
  }

  :deep(.el-switch.is-checked .el-switch__core) {
    border-color: #3eb8be;
    background: #237a80;
  }
}

.bm-body {
  display: grid;
  grid-template-columns: clamp(232px, 18vw, 286px) minmax(0, 1fr) clamp(252px, 19vw, 306px);
  align-items: stretch;
  gap: 10px;
  margin-bottom: 10px;
}

.bm-side {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
  order: 0;
}

.bm-main {
  min-width: 0;
  order: 0;
}

.bm-panel,
.bm-map-card {
  position: relative;
  border: 1px solid var(--bm-line);
  border-radius: 6px;
  background: var(--bm-surface);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.22), inset 0 1px rgba(255, 255, 255, 0.018);

  &::before {
    position: absolute;
    top: -1px;
    left: 14px;
    width: 56px;
    height: 1px;
    content: '';
    background: var(--bm-cyan);
    box-shadow: 0 0 9px rgba(85, 216, 222, 0.65);
  }
}

.bm-panel {
  padding: 12px 13px;
}

.bm-panel-title {
  min-height: 28px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom-color: rgba(103, 184, 192, 0.13);
  color: #e8f4f4;
  font-size: 14px;
  font-weight: 680;

  .bm-panel-sub {
    color: #6f878b;
    font-size: 11px;
  }

  :deep(.el-button) {
    color: var(--bm-cyan);
  }
}

.bm-panel-live {
  color: #68d795;

  i {
    background: #68d795;
    box-shadow: 0 0 9px rgba(104, 215, 149, 0.65);
  }
}

.bm-chips {
  max-height: 132px;
  gap: 6px;
  padding-right: 2px;
  scrollbar-color: rgba(85, 216, 222, 0.34) transparent;
  scrollbar-width: thin;
}

.bm-chip {
  min-height: 27px;
  padding: 3px 8px;
  border: 1px solid rgba(111, 187, 195, 0.16);
  border-radius: 3px;
  color: #9eb3b6;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0;
  background: rgba(54, 83, 88, 0.12);
  box-shadow: none;

  em {
    color: #668187;
    font-variant-numeric: tabular-nums;
    opacity: 1;
  }

  &:hover {
    border-color: rgba(85, 216, 222, 0.64);
    color: #dff7f7;
    background: rgba(85, 216, 222, 0.08);
  }

  &.is-active {
    border-color: var(--bm-cyan);
    color: #041115;
    background: var(--bm-cyan);
    box-shadow: 0 0 14px rgba(85, 216, 222, 0.16);

    em {
      color: #17373a;
    }
  }

  &.is-idle:not(.is-active) {
    color: #596f73;
    background: rgba(46, 65, 69, 0.08);
  }

  &.is-type {
    i {
      width: 7px;
      height: 7px;
      box-shadow: 0 0 7px var(--chip-color);
    }

    &.is-active {
      border-color: var(--chip-color);
      color: #fff;
      background: color-mix(in srgb, var(--chip-color) 30%, #0b171b);
      box-shadow: inset 0 0 18px color-mix(in srgb, var(--chip-color) 18%, transparent);

      em {
        color: rgba(255, 255, 255, 0.72);
      }
    }
  }
}

.bm-rank {
  max-height: 328px;
  gap: 3px;
  padding-right: 2px;
  scrollbar-color: rgba(85, 216, 222, 0.34) transparent;
  scrollbar-width: thin;
}

.bm-rank-item {
  min-height: 42px;
  padding: 7px 7px;
  border: 1px solid transparent;
  border-radius: 4px;

  &:hover {
    border-color: rgba(85, 216, 222, 0.2);
    background: rgba(85, 216, 222, 0.055);
  }

  &.is-active {
    border-color: rgba(85, 216, 222, 0.36);
    background: rgba(85, 216, 222, 0.1);
    box-shadow: inset 3px 0 var(--bm-cyan);
  }
}

.bm-rank-no {
  width: 21px;
  height: 21px;
  flex-basis: 21px;
  border: 1px solid rgba(139, 166, 170, 0.26);
  border-radius: 3px;
  color: #8ba0a4;
  background: rgba(65, 86, 90, 0.2);

  &.no-1 {
    border-color: rgba(255, 105, 112, 0.5);
    color: #ff8a90;
    background: rgba(255, 82, 91, 0.1);
  }

  &.no-2 {
    border-color: rgba(244, 196, 91, 0.5);
    color: var(--bm-amber);
    background: rgba(244, 196, 91, 0.1);
  }

  &.no-3 {
    border-color: rgba(85, 216, 222, 0.48);
    color: var(--bm-cyan);
    background: rgba(85, 216, 222, 0.08);
  }
}

.bm-rank-name {
  color: #c9d8da;
  font-size: 12px;
}

.bm-rank-bar {
  height: 3px;
  background: rgba(105, 139, 145, 0.14);
}

.bm-rank-value {
  color: #f3f8f8;
  font-size: 14px;
  font-variant-numeric: tabular-nums;

  em {
    color: #789095;
  }
}

.bm-rank-empty {
  color: var(--bm-muted);
}

.bm-map-card {
  height: 100%;
  padding: 11px 12px 12px;
}

.bm-map-head {
  min-height: 35px;
  margin-bottom: 8px;
  padding: 0 2px;
}

.bm-map-title {
  color: #edf8f8;
  font-size: 15px;

  em {
    color: #708a8f;
    font-size: 11px;
  }
}

.bm-map-tools {
  display: flex;
  align-items: center;
  gap: 6px;

  :deep(.el-button) {
    height: 28px;
    margin-left: 0;
    border-color: rgba(111, 187, 195, 0.2);
    border-radius: 3px;
    color: #adc2c4;
    background: rgba(38, 67, 72, 0.18);

    &:hover:not(:disabled) {
      border-color: var(--bm-cyan);
      color: #fff;
      background: rgba(85, 216, 222, 0.1);
    }

    &:disabled {
      min-width: 54px;
      color: #6f888d;
      background: #0a1519;
    }

    span {
      margin-left: 4px;
    }
  }
}

.bm-map-body {
  min-height: 590px;
  overflow: hidden;
  border: 1px solid rgba(82, 216, 222, 0.14);
  border-radius: 4px;
  background: #050d11;
}

.bm-feed {
  max-height: 264px;
  gap: 5px;
  scrollbar-color: rgba(85, 216, 222, 0.34) transparent;
  scrollbar-width: thin;
}

.bm-feed-item {
  padding: 7px 8px;
  border: 1px solid rgba(99, 144, 151, 0.1);
  border-left: 2px solid var(--bm-blue);
  border-radius: 3px;
  color: #aabdbf;
  background: rgba(44, 68, 73, 0.12);

  &.is-success {
    border-left-color: #68d795;
  }

  &.is-warning {
    border-left-color: #ff6b72;
  }
}

.bm-feed-time {
  color: #657f84;
  font-family: Consolas, monospace;
}

.bm-feed-region {
  color: var(--bm-cyan);
}

.bm-feed-text {
  color: #aabdbf;
}

.bm-stats {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 0;
  overflow: hidden;
  border: 1px solid var(--bm-line);
  border-radius: 6px;
  background: rgba(11, 22, 26, 0.96);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.2);
}

.biz-map-screen.is-fullscreen {
  display: flex;
  height: 100vh;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;

  .ds-header {
    flex: 0 0 auto;
  }

  .bm-body {
    min-height: 0;
    flex: 1 1 auto;
  }

  .bm-side,
  .bm-main,
  .bm-map-card {
    min-height: 0;
    height: 100%;
  }

  .bm-map-body {
    min-height: 0;
  }

  .bm-panel-grow,
  .bm-rank,
  .bm-feed {
    min-height: 0;
    max-height: none;
  }

  .bm-stats {
    flex: 0 0 78px;
  }
}

@media (max-width: 1520px) {
  .bm-body {
    grid-template-columns: 238px minmax(0, 1fr) 264px;
  }

  .bm-map-body {
    min-height: 550px;
  }

  .bm-stats {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

// 若依左侧导航会压缩实际内容区，提前切换成“地图优先”的两段布局。
@media (max-width: 1420px) {
  .biz-map-screen {
    overflow: auto;
  }

  .bm-body {
    grid-template-columns: 1fr 1fr;
  }

  .bm-main {
    grid-column: 1 / -1;
    grid-row: 1;
  }

  .bm-side {
    grid-row: 2;
  }

  .bm-map-body {
    min-height: 520px;
  }
}

@media (max-width: 760px) {
  .biz-map-screen {
    padding: 8px;
  }

  .ds-header {
    padding: 12px;

    .ds-header-top {
      flex-direction: column;
      gap: 8px;
    }

    .ds-header-left,
    .ds-header-right {
      min-width: 0;
      text-align: center;
    }
  }

  .ds-header-switch,
  .switch-actions {
    justify-content: center;
  }

  .switch-crumb {
    display: none;
  }

  .bm-body {
    grid-template-columns: 1fr;
  }

  .bm-main,
  .bm-side {
    grid-column: 1;
    grid-row: auto;
  }

  .bm-main {
    order: 1;
  }

  .bm-side {
    order: 2;
  }

  .bm-map-head {
    align-items: flex-start;
    gap: 8px;
  }

  .bm-map-title em {
    display: block;
    margin: 3px 0 0;
  }

  .bm-map-tools :deep(.el-button span) {
    display: none;
  }

  .bm-map-body {
    min-height: 440px;
  }

  .bm-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (prefers-reduced-motion: reduce) {
  .ds-header::after {
    animation: none;
  }
}
</style>
