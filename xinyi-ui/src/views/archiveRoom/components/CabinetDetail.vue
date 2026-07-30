<template>
  <div class="cabinet-detail">
    <template v-if="cabinet">
      <div class="detail-header">
        <span class="detail-id" :style="{ color: zoneColorStr(cabinet.zone) }">{{ cabinet.label }}</span>
        <span class="detail-zone">{{ zoneLabel(cabinet.zone) }}</span>
      </div>

      <div class="detail-stats">
        <div class="stat-item">
          <div class="stat-label">容量</div>
          <div class="stat-value">{{ cabinet.capacity }} 盒</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已用</div>
          <div class="stat-value">{{ cabinet.used }} 盒</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">使用率</div>
          <div class="stat-value" :style="{ color: usageColor(cabinet.used / cabinet.capacity) }">
            {{ Math.round((cabinet.used / cabinet.capacity) * 100) }}%
          </div>
        </div>
      </div>

      <el-progress
        :percentage="Math.round((cabinet.used / cabinet.capacity) * 100)"
        :color="usageColor(cabinet.used / cabinet.capacity)"
        :stroke-width="8"
        :show-text="false"
        style="margin-bottom: 12px;"
      />

      <!-- 柜内文件列表 -->
      <div class="file-list-header">
        柜内文件（{{ cabinetFiles.length }} 份）
      </div>
      <div class="file-list">
        <div v-for="file in cabinetFiles" :key="file.id" class="file-item" @click="$emit('highlight', file)">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-meta">
            <span>{{ file.boxNumber }}</span>
            <span>{{ file.category }}</span>
            <span :class="['file-status', file.status]">{{ statusText(file.status) }}</span>
          </div>
        </div>
        <div v-if="cabinetFiles.length === 0" class="empty-files">
          该柜暂无档案文件
        </div>
      </div>
    </template>

    <div v-else class="no-selection">
      <svg-icon icon-class="archive" style="font-size: 40px; opacity: 0.2;" />
      <p>点击平面图中的柜子或搜索文件查看详情</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { zoneColor as findZoneColor, zoneLabel as findZoneLabel, type Cabinet, type ArchiveFile, type ZoneConfig } from '../data/mockData'

const props = defineProps<{
  cabinet: Cabinet | null
  files: ArchiveFile[]
  zoneConfigs: ZoneConfig[]
}>()

defineEmits<{
  highlight: [file: ArchiveFile]
}>()

const cabinetFiles = computed(() => {
  if (!props.cabinet) return []
  return props.files.filter(f => f.cabinetId === props.cabinet!.id)
})

function zoneLabel(zone: string): string {
  return findZoneLabel(zone, props.zoneConfigs)
}

function zoneColorStr(zone: string): string {
  return findZoneColor(zone, props.zoneConfigs)
}

function usageColor(ratio: number): string {
  if (ratio >= 0.85) return '#ff4d4f'
  if (ratio >= 0.65) return '#faad14'
  return '#52c41a'
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    normal: '在档',
    borrowed: '已借出',
    missing: '缺失',
  }
  return map[status] || status
}
</script>

<style lang="scss" scoped>
.cabinet-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-id {
  font-size: 20px;
  font-weight: 700;
}

.detail-zone {
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.detail-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  font-size: 11px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f1f1f);
}

.file-list-header {
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
  font-weight: 600;
  margin-top: 4px;
}

.file-list {
  max-height: 220px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-item {
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-bg-color-page, #f5f7fa);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(22, 119, 255, 0.06);
  }
}

.file-name {
  font-size: 12px;
  color: var(--el-text-color-primary, #1f1f1f);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.file-meta {
  display: flex;
  gap: 8px;
  font-size: 10px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.file-status {
  &.normal { color: #52c41a; }
  &.borrowed { color: #faad14; }
  &.missing { color: #ff4d4f; }
}

.empty-files {
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
  padding: 16px 0;
}

.no-selection {
  text-align: center;
  padding: 40px 0;
  color: var(--el-text-color-secondary, #8c8c8c);

  p {
    font-size: 12px;
    margin-top: 12px;
  }
}
</style>
