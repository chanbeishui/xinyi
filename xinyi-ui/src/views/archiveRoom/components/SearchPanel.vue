<template>
  <div class="search-panel">
    <!-- 搜索框 -->
    <div class="search-box">
      <el-input
        v-model="keyword"
        placeholder="输入文件名称搜索..."
        size="large"
        clearable
        @keyup.enter="doSearch"
        @clear="clearSearch"
      >
        <template #prefix>
          <svg-icon icon-class="search" />
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="doSearch" style="margin-left: 8px;">
        搜索
      </el-button>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" class="search-results">
      <div class="results-header">
        搜索结果（{{ searchResults.length }} 条）
      </div>
      <div class="results-list">
        <div
          v-for="file in searchResults"
          :key="file.id"
          class="result-item"
          @click="selectFile(file)"
        >
          <div class="result-name">{{ file.name }}</div>
          <div class="result-meta">
            <span class="result-cabinet">
              <svg-icon icon-class="archive" style="margin-right: 2px;" />
              {{ file.cabinetId }}
            </span>
            <span class="result-category">{{ file.category }}</span>
            <span :class="['result-status', file.status]">{{ statusText(file.status) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 无结果提示 -->
    <div v-else-if="hasSearched" class="no-result">
      <el-empty description="未找到匹配的文件" :image-size="60" />
    </div>

    <!-- 初始提示 -->
    <div v-else class="search-hint">
      <svg-icon icon-class="search" style="font-size: 32px; opacity: 0.3;" />
      <p>输入文件名称可快速定位档案所在柜子</p>
      <p class="hint-sub">支持模糊搜索，如"混凝土"、"检测报告"等</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Fuse from 'fuse.js'
import { type ArchiveFile } from '../data/mockData'

const props = defineProps<{
  cabinets: import('../data/mockData').Cabinet[]
  files: ArchiveFile[]
}>()

const emit = defineEmits<{
  highlight: [file: ArchiveFile]
}>()

const keyword = ref('')
const searchResults = ref<ArchiveFile[]>([])
const hasSearched = ref(false)

let fuse = new Fuse(props.files, {
  threshold: 0.35,
  minMatchCharLength: 1,
  keys: ['name', 'id', 'category', 'cabinetId'],
})

// 档案室切换时重建搜索索引
watch(() => props.files, (newFiles) => {
  fuse = new Fuse(newFiles, {
    threshold: 0.35,
    minMatchCharLength: 1,
    keys: ['name', 'id', 'category', 'cabinetId'],
  })
  clearSearch()
})

function doSearch() {
  if (!keyword.value.trim()) {
    clearSearch()
    return
  }
  hasSearched.value = true
  searchResults.value = fuse.search(keyword.value).map(r => r.item)
}

function clearSearch() {
  keyword.value = ''
  searchResults.value = []
  hasSearched.value = false
}

function selectFile(file: ArchiveFile) {
  emit('highlight', file)
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
.search-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
}

.search-results {
  .results-header {
    font-size: 12px;
    color: var(--el-text-color-secondary, #8c8c8c);
    margin-bottom: 8px;
    font-weight: 600;
  }
}

.results-list {
  max-height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.result-item {
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-bg-color-page, #f5f7fa);
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;

  &:hover {
    background: rgba(22, 119, 255, 0.06);
    border-color: rgba(22, 119, 255, 0.2);
  }
}

.result-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary, #1f1f1f);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.result-cabinet {
  color: var(--el-color-primary, #1677ff);
  font-weight: 600;
}

.result-category {
  color: var(--el-text-color-secondary, #8c8c8c);
}

.result-status {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;

  &.normal { background: #f6ffed; color: #52c41a; }
  &.borrowed { background: #fff7e6; color: #faad14; }
  &.missing { background: #fff2f0; color: #ff4d4f; }
}

.no-result {
  padding: 20px 0;
}

.search-hint {
  text-align: center;
  padding: 24px 0;
  color: var(--el-text-color-secondary, #8c8c8c);

  p {
    font-size: 13px;
    margin-top: 8px;
  }

  .hint-sub {
    font-size: 11px;
    opacity: 0.7;
  }
}
</style>
