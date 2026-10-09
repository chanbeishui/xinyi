<template>
  <div class="bm-site-panel">
    <template v-if="site">
      <div class="sp-head">
        <span
          class="sp-dot"
          :style="{ background: statusMeta[site.status].color, color: statusMeta[site.status].color }"
        />
        <span class="sp-name">{{ site.name }}</span>
      </div>
      <div class="sp-tags">
        <el-tag size="small" effect="plain">{{ site.region }} · {{ site.county }}</el-tag>
        <el-tag size="small" effect="plain" :style="{ color: typeMeta[site.type], borderColor: typeMeta[site.type] }">
          {{ site.type }}
        </el-tag>
        <el-tag
          size="small"
          effect="dark"
          :style="{ background: statusMeta[site.status].color, borderColor: statusMeta[site.status].color }"
        >
          {{ statusMeta[site.status].label }}
        </el-tag>
      </div>

      <div class="sp-grid">
        <div class="sp-cell">
          <span>在检项目</span>
          <b>{{ site.projects }}</b>
        </div>
        <div class="sp-cell">
          <span>送检样品</span>
          <b>{{ site.samples }}</b>
        </div>
        <div class="sp-cell">
          <span>已出报告</span>
          <b>{{ site.reports }}</b>
        </div>
        <div class="sp-cell">
          <span>合同额</span>
          <b>{{ site.amount }}<i>万</i></b>
        </div>
        <div class="sp-cell">
          <span>驻场人员</span>
          <b>{{ site.staff }}<i>人</i></b>
        </div>
        <div class="sp-cell">
          <span>距信宜</span>
          <b>{{ site.distance }}<i>km</i></b>
        </div>
      </div>

      <div class="sp-progress">
        <div class="sp-progress-label">
          <span>项目进度</span>
          <b>{{ site.progress }}%</b>
        </div>
        <el-progress
          :percentage="site.progress"
          :color="statusMeta[site.status].color"
          :stroke-width="8"
          :show-text="false"
        />
      </div>

      <div class="sp-meta">
        <span>负责人：{{ site.manager }}</span>
        <span>进场：{{ site.startDate }}</span>
      </div>

      <div class="sp-list-title">近期任务（{{ site.tasks.length }}）</div>
      <div class="sp-list">
        <div v-for="task in site.tasks" :key="task.name" class="sp-task">
          <span class="sp-task-name">{{ task.name }}</span>
          <span class="sp-task-date">{{ task.date }}</span>
          <span class="sp-task-status" :class="taskClass(task.status)">{{ task.status }}</span>
        </div>
      </div>
    </template>

    <div v-else class="sp-empty">
      <svg-icon icon-class="guide" style="font-size: 38px; opacity: 0.2" />
      <p>点击地图业务点或左侧排行查看详情</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { statusMeta, typeMeta, type BizSite, type SiteTask } from '../data/mockData'

defineProps<{
  site: BizSite | null
}>()

function taskClass(status: SiteTask['status']) {
  const map: Record<SiteTask['status'], string> = {
    进行中: 'is-running',
    待复核: 'is-pending',
    已完成: 'is-done',
    已预警: 'is-warning'
  }
  return map[status]
}
</script>

<style lang="scss" scoped>
.bm-site-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sp-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sp-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 8px;
}

.sp-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f1f1f);
  line-height: 1.4;
}

.sp-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sp-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.sp-cell {
  padding: 8px 4px;
  border-radius: 6px;
  background: var(--el-bg-color-page, #f5f7fa);
  text-align: center;

  span {
    display: block;
    font-size: 11px;
    color: var(--el-text-color-secondary, #8c8c8c);
  }

  b {
    font-size: 16px;
    color: var(--el-text-color-primary, #1f1f1f);
  }

  i {
    font-style: normal;
    font-size: 11px;
    font-weight: 400;
    color: var(--el-text-color-secondary, #8c8c8c);
    margin-left: 2px;
  }
}

.sp-progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
  margin-bottom: 6px;

  b {
    color: var(--el-text-color-primary, #1f1f1f);
  }
}

.sp-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.sp-list-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #8c8c8c);
  padding-top: 6px;
  border-top: 1px solid var(--el-border-color-lighter, #f0f0f0);
}

.sp-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 160px;
  overflow-y: auto;
}

.sp-task {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  background: var(--el-bg-color-page, #f5f7fa);
  font-size: 12px;
}

.sp-task-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-primary, #1f1f1f);
}

.sp-task-date {
  font-size: 11px;
  color: var(--el-text-color-secondary, #8c8c8c);
}

.sp-task-status {
  font-size: 11px;

  &.is-running {
    color: #1677ff;
  }
  &.is-pending {
    color: #faad14;
  }
  &.is-done {
    color: #52c41a;
  }
  &.is-warning {
    color: #ff4d4f;
  }
}

.sp-empty {
  text-align: center;
  padding: 32px 0;
  color: var(--el-text-color-secondary, #8c8c8c);

  p {
    font-size: 12px;
    margin-top: 10px;
  }
}
</style>

<style lang="scss" scoped>
.bm-site-panel {
  gap: 11px;
  color: #dcebec;
}

.sp-head {
  min-height: 30px;
}

.sp-dot {
  box-shadow: 0 0 10px currentColor;
}

.sp-name {
  color: #f1f8f8;
  font-size: 15px;
  font-weight: 720;
}

.sp-tags {
  gap: 5px;

  :deep(.el-tag) {
    height: 24px;
    border-radius: 3px;
    border-color: rgba(119, 180, 186, 0.26);
    color: #a9c0c3;
    background: rgba(53, 84, 89, 0.12);
    font-size: 11px;
  }
}

.sp-grid {
  gap: 0;
  overflow: hidden;
  border-top: 1px solid rgba(103, 184, 192, 0.15);
  border-left: 1px solid rgba(103, 184, 192, 0.15);
  border-radius: 3px;
}

.sp-cell {
  min-height: 56px;
  padding: 8px 4px;
  border-right: 1px solid rgba(103, 184, 192, 0.15);
  border-bottom: 1px solid rgba(103, 184, 192, 0.15);
  border-radius: 0;
  background: rgba(41, 68, 73, 0.1);

  span {
    color: #728c91;
    font-size: 11px;
  }

  b {
    color: #f1f8f8;
    font-size: 17px;
    font-variant-numeric: tabular-nums;
  }

  i {
    color: #708a8f;
  }
}

.sp-progress-label {
  color: #7f999d;

  b {
    color: #dceced;
  }
}

.sp-progress :deep(.el-progress-bar__outer) {
  border-radius: 2px;
  background: rgba(106, 143, 148, 0.14);
}

.sp-progress :deep(.el-progress-bar__inner) {
  border-radius: 2px;
  box-shadow: 0 0 8px currentColor;
}

.sp-meta {
  padding: 8px 0;
  border-top: 1px solid rgba(103, 184, 192, 0.1);
  border-bottom: 1px solid rgba(103, 184, 192, 0.1);
  color: #879fa3;
}

.sp-list-title {
  padding-top: 0;
  border-top: 0;
  color: #9bb0b3;
  font-size: 12px;
}

.sp-list {
  gap: 5px;
  max-height: 174px;
  padding-right: 2px;
  scrollbar-color: rgba(85, 216, 222, 0.34) transparent;
  scrollbar-width: thin;
}

.sp-task {
  min-height: 34px;
  padding: 6px 8px;
  border: 1px solid rgba(103, 184, 192, 0.1);
  border-radius: 3px;
  background: rgba(41, 68, 73, 0.1);
}

.sp-task-name {
  color: #c7d7d9;
}

.sp-task-date {
  color: #6e878b;
  font-family: Consolas, monospace;
}

.sp-empty {
  min-height: 230px;
  display: grid;
  place-content: center;
  color: #6f878b;

  p {
    max-width: 190px;
    line-height: 1.65;
  }
}
</style>
