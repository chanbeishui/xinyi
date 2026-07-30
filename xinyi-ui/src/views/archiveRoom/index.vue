<template>
  <div class="archive-screen" ref="screenRef" :class="{ 'is-fullscreen': isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="ds-header">
      <div class="ds-header-top">
        <div class="ds-header-left">
          <span class="ds-header-date">{{ currentDate }}</span>
        </div>
        <div class="ds-header-center">
          <h1>档案室管理大屏</h1>
          <p>{{ currentRoom.name }} · {{ currentRoom.location }} · {{ currentRoom.description }}</p>
        </div>
        <div class="ds-header-right">
          <el-button class="ds-fullscreen-btn" size="small" @click="toggleFullscreen">
            <svg-icon :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" style="margin-right: 4px;" />
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
        </div>
      </div>
      <!-- 档案室切换栏 -->
      <div class="ds-header-switch">
        <span class="switch-label">切换档案室：</span>
        <el-radio-group
          v-model="currentRoomId"
          size="small"
          @change="handleRoomChange"
        >
          <el-radio-button
            v-for="room in rooms"
            :key="room.id"
            :value="room.id"
          >
            {{ room.name }}
          </el-radio-button>
        </el-radio-group>
        <span class="switch-info">
          {{ currentRoom.zoneConfigs.length }} 个区 · {{ currentRoom.cabinets.length }} 个柜子 · {{ currentRoom.files.length }} 份文件
        </span>
      </div>
    </div>

    <!-- 主体：左面板 + SVG 平面图 -->
    <div class="ar-body">
      <!-- 左侧面板 -->
      <div class="ar-left">
        <div class="ar-panel">
          <div class="ar-panel-title">文件搜索</div>
          <SearchPanel
            :cabinets="cabinets"
            :files="files"
            @highlight="handleHighlight"
          />
        </div>
        <div class="ar-panel">
          <div class="ar-panel-title">柜子详情</div>
          <CabinetDetail
            :cabinet="selectedCabinet"
            :files="files"
            :zone-configs="zoneConfigs"
            @highlight="handleHighlight"
          />
        </div>
      </div>

      <!-- 右侧 SVG 平面图 -->
      <div class="ar-right">
        <div class="ar-plan-card">
          <div class="ar-plan-header">
            <span class="ar-plan-title">{{ currentRoom.name }} — 平面图</span>
            <div class="ar-zoom-bar">
              <el-button-group>
                <el-button size="small" @click="planRef?.zoomIn()">
                  <svg-icon icon-class="search" style="font-size: 12px;" />
                </el-button>
                <el-button size="small" disabled>{{ planRef?.scalePercent?.value || '100%' }}</el-button>
                <el-button size="small" @click="planRef?.zoomOut()">-</el-button>
              </el-button-group>
              <el-button size="small" @click="planRef?.resetView()" style="margin-left: 8px;">重置</el-button>
            </div>
          </div>
          <div class="ar-plan-wrap">
            <ArchivePlan
              ref="planRef"
              :cabinets="cabinets"
              :highlighted-id="highlightedId"
              :zone-configs="zoneConfigs"
              @select="handleSelectCabinet"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 底部统计 -->
    <div class="ar-stats">
      <div class="ar-stat-card">
        <div class="ar-stat-icon" style="background: rgba(22,119,255,0.1); color: #1677ff;">
          <svg-icon icon-class="archive" />
        </div>
        <div class="ar-stat-info">
          <div class="ar-stat-label">档案柜总数</div>
          <div class="ar-stat-value">{{ cabinets.length }}</div>
        </div>
      </div>
      <div class="ar-stat-card">
        <div class="ar-stat-icon" style="background: rgba(82,196,26,0.1); color: #52c41a;">
          <svg-icon icon-class="documentation" />
        </div>
        <div class="ar-stat-info">
          <div class="ar-stat-label">档案文件总数</div>
          <div class="ar-stat-value">{{ files.length }}</div>
        </div>
      </div>
      <div class="ar-stat-card">
        <div class="ar-stat-icon" style="background: rgba(250,173,20,0.1); color: #faad14;">
          <svg-icon icon-class="chart" />
        </div>
        <div class="ar-stat-info">
          <div class="ar-stat-label">平均使用率</div>
          <div class="ar-stat-value">{{ avgUsage }}%</div>
        </div>
      </div>
      <div class="ar-stat-card">
        <div class="ar-stat-icon" style="background: rgba(255,77,79,0.1); color: #ff4d4f;">
          <svg-icon icon-class="list" />
        </div>
        <div class="ar-stat-info">
          <div class="ar-stat-label">借出文件</div>
          <div class="ar-stat-value">{{ borrowedCount }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import ArchivePlan from './components/ArchivePlan.vue'
import SearchPanel from './components/SearchPanel.vue'
import CabinetDetail from './components/CabinetDetail.vue'
import { rooms, type Cabinet, type ArchiveFile, type ZoneConfig } from './data/mockData'

const screenRef = ref<HTMLElement>()
const planRef = ref<InstanceType<typeof ArchivePlan>>()
const isFullscreen = ref(false)

// 档案室切换
const currentRoomId = ref(rooms[0].id)
const currentRoom = computed(() => rooms.find(r => r.id === currentRoomId.value) || rooms[0])
const cabinets = computed(() => currentRoom.value.cabinets)
const files = computed(() => currentRoom.value.files)
const zoneConfigs = computed(() => currentRoom.value.zoneConfigs)

const selectedCabinet = ref<Cabinet | null>(null)
const highlightedId = ref<string | null>(null)

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
})

const avgUsage = computed(() => {
  if (cabinets.value.length === 0) return 0
  const total = cabinets.value.reduce((sum, c) => sum + (c.used / c.capacity), 0)
  return Math.round((total / cabinets.value.length) * 100)
})

const borrowedCount = computed(() => {
  return files.value.filter(f => f.status === 'borrowed').length
})

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

function handleRoomChange() {
  // 切换档案室时重置状态
  selectedCabinet.value = null
  highlightedId.value = null
  nextTick(() => {
    planRef.value?.resetView()
  })
}

function handleHighlight(file: ArchiveFile) {
  highlightedId.value = file.cabinetId
  const cab = cabinets.value.find(c => c.id === file.cabinetId)
  if (cab) {
    selectedCabinet.value = cab
    nextTick(() => {
      planRef.value?.focusOn(cab.x + cab.width / 2, cab.y + cab.height / 2, 2.5)
    })
  }
}

function handleSelectCabinet(cab: Cabinet) {
  selectedCabinet.value = cab
  highlightedId.value = cab.id
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style lang="scss" scoped>
.archive-screen {
  padding: 16px;
  min-height: 100vh;
  background: var(--el-bg-color-page, #f0f2f5);
  color: var(--el-text-color-primary, #1f1f1f);

  &.is-fullscreen {
    overflow: auto;
    padding: 20px;
  }
}

/* 蓝色渐变 banner（复用 dataScreen 风格） */
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
  .ds-header-left { flex: 0 0 auto; }
  .ds-header-center {
    flex: 1;
    text-align: center;
    h1 { font-size: 22px; font-weight: 700; margin: 0; }
    p { font-size: 13px; opacity: 0.85; margin-top: 4px; }
  }
  .ds-header-right {
    flex: 0 0 auto;
    text-align: right;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
    .ds-header-date { font-size: 14px; opacity: 0.9; }
    .ds-fullscreen-btn {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
      }
    }
  }
}

/* 档案室切换栏 */
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

  .switch-info {
    font-size: 12px;
    opacity: 0.7;
    margin-left: auto;
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

/* 主体布局 */
.ar-body {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.ar-left {
  flex: 0 0 340px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.ar-panel {
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.ar-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1f1f1f);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);
}

.ar-right {
  flex: 1;
  min-width: 0;
}

.ar-plan-card {
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ar-plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ar-plan-title {
  font-size: 14px;
  font-weight: 600;
}

.ar-zoom-bar {
  display: flex;
  align-items: center;
}

.ar-plan-wrap {
  flex: 1;
  min-height: 520px;
  background: var(--el-bg-color-page, #f5f7fa);
  border-radius: 8px;
  overflow: hidden;
}

/* 底部统计 */
.ar-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.ar-stat-card {
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 14px;
}

.ar-stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.ar-stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.ar-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f1f1f);
}

/* 响应式 */
@media (max-width: 1200px) {
  .ar-body { flex-direction: column; }
  .ar-left { flex: none; max-height: none; }
  .ar-stats { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .ds-header-top { flex-direction: column; gap: 10px; text-align: center; }
  .ds-header-right { align-items: center; }
  .ds-header-switch { flex-direction: column; gap: 8px; }
  .ar-stats { grid-template-columns: 1fr; }
}
</style>
