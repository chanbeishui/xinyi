<template>
  <div class="rl-page">
    <header class="rl-page__head">
      <div class="rl-page__titles">
        <h1>{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <div class="rl-page__actions">
        <slot name="actions" />
      </div>
    </header>
    <slot />
  </div>
</template>

<script setup name="RongliPage" lang="ts">
/**
 * 荣利后台页面统一外壳：标题 + 副标题 + 右侧操作区，视觉对齐《全量高保真 UI V5》桌面原型。
 * 样式非 scoped，但全部挂在 .rl-page 下，避免影响框架其它页面。
 */
defineProps<{ title: string; subtitle?: string }>()
</script>

<style>
.rl-page {
  --rl-ink: #101828;
  --rl-ink-2: #475467;
  --rl-ink-3: #667085;
  --rl-ink-4: #98a2b3;
  --rl-line: #eaecf0;
  --rl-line-soft: #f2f4f7;
  --rl-bg: #f7f8fa;
  --rl-primary: #175cd3;
  --rl-radius: 10px;
  padding: 20px 24px 32px;
  background: var(--rl-bg);
  min-height: calc(100vh - 84px);
  color: var(--rl-ink);
  font-size: 13px;
}

.rl-page__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 16px;
}

.rl-page__titles h1 {
  margin: 0;
  font-size: 20px;
  line-height: 28px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: var(--rl-ink);
}

.rl-page__titles p {
  margin: 4px 0 0;
  font-size: 12.5px;
  line-height: 18px;
  color: var(--rl-ink-3);
}

.rl-page__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 2px;
}

/* ---------------------------------------------------------------- 面板 */
.rl-panel {
  background: #fff;
  border: 1px solid var(--rl-line);
  border-radius: var(--rl-radius);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  overflow: hidden;
}

.rl-panel + .rl-panel {
  margin-top: 16px;
}

.rl-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--rl-line-soft);
}

.rl-panel__head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--rl-ink);
}

.rl-panel__body {
  padding: 16px 18px;
}

/* ---------------------------------------------------------------- 筛选条 */
.rl-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 18px;
  border-bottom: 1px solid var(--rl-line-soft);
}

.rl-filters .el-input,
.rl-filters .el-select {
  width: 200px;
}

.rl-filters .el-input.rl-w-260 {
  width: 260px;
}

.rl-filters .el-button + .el-button {
  margin-left: 0;
}

/* ---------------------------------------------------------------- 表格 */
.rl-page .el-table {
  --el-table-border-color: var(--rl-line-soft);
  --el-table-header-bg-color: #fafbfc;
  --el-table-row-hover-bg-color: #f9fafb;
  font-size: 13px;
}

.rl-page .el-table th.el-table__cell {
  background: #fafbfc;
  color: var(--rl-ink-3);
  font-weight: 500;
  font-size: 12.5px;
  height: 44px;
  padding: 0;
}

.rl-page .el-table td.el-table__cell {
  padding: 12px 0;
  border-bottom: 1px solid var(--rl-line-soft);
  vertical-align: middle;
}

.rl-page .el-table .cell {
  line-height: 20px;
}

.rl-strong {
  color: var(--rl-ink);
  font-weight: 500;
}

.rl-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 11.5px;
  color: var(--rl-ink-4);
  letter-spacing: 0.2px;
}

.rl-sub {
  color: var(--rl-ink-3);
  font-size: 12px;
}

.rl-muted {
  color: var(--rl-ink-4);
}

/* ---------------------------------------------------------------- 状态标签 */
.rl-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 22px;
  white-space: nowrap;
  border: 1px solid transparent;
}

.rl-tag::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.rl-tag--plain::before {
  display: none;
}

.rl-tag--amber {
  color: #b54708;
  background: #fffaeb;
  border-color: #fedf89;
}

.rl-tag--blue {
  color: #175cd3;
  background: #eff8ff;
  border-color: #b2ddff;
}

.rl-tag--green {
  color: #067647;
  background: #ecfdf3;
  border-color: #abefc6;
}

.rl-tag--gray {
  color: #475467;
  background: #f2f4f7;
  border-color: #e4e7ec;
}

.rl-tag--red {
  color: #b42318;
  background: #fef3f2;
  border-color: #fecdca;
}

/* ---------------------------------------------------------------- 通用容器化 */
/* 直接把页面顶层的行内表单识别为筛选条，避免每页重复写结构 */
.rl-page > .el-form--inline {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 10px;
  background: #fff;
  border: 1px solid var(--rl-line);
  border-radius: var(--rl-radius) var(--rl-radius) 0 0;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  padding: 14px 18px;
  margin-bottom: 0;
}

/* 筛选条 + 表格 + 分页在视觉上合成一张卡片，贴合原型 .panel 结构 */
.rl-page > .el-form--inline:has(+ .el-table),
.rl-page > .el-form--inline:has(+ .el-tabs) {
  border-bottom-color: var(--rl-line-soft);
}

.rl-page > .el-form--inline + .el-table {
  border-top-left-radius: 0;
  border-top-right-radius: 0;
  border-top-color: var(--rl-line-soft);
  box-shadow: none;
}

.rl-page > .el-table:has(+ .el-pagination) {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-bottom: 0;
  box-shadow: none;
}

.rl-page > .el-table + .el-pagination {
  margin-top: 0;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid var(--rl-line);
  border-top: 1px solid var(--rl-line-soft);
  border-radius: 0 0 var(--rl-radius) var(--rl-radius);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.rl-page > .el-form--inline .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
}

.rl-page > .el-form--inline .el-form-item__label {
  font-size: 12.5px;
  color: var(--rl-ink-3);
  padding-right: 8px;
}

/* 原型状态标签：浅底、无描边、12px，避免 Element 默认 12px 的胶囊样式 */
.rl-page .el-tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 400;
  line-height: 20px;
}

.rl-page .el-tag--success {
  color: #067647;
  background: #ecfdf3;
}

.rl-page .el-tag--warning {
  color: #b54708;
  background: #fffaeb;
}

.rl-page .el-tag--danger {
  color: #b42318;
  background: #fef3f2;
}

.rl-page .el-tag--info {
  color: #475467;
  background: #f2f4f7;
}

.rl-page .el-tag--primary {
  color: #175cd3;
  background: #eff8ff;
}

.rl-page > .el-table {
  border: 1px solid var(--rl-line);
  border-radius: var(--rl-radius);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.rl-page > .el-table.el-table--border {
  border-right: 0;
  border-bottom: 0;
}

.rl-page > .el-pagination {
  justify-content: flex-end;
  margin-top: 12px;
  --el-pagination-font-size: 12.5px;
}

.rl-page > .el-tabs {
  background: #fff;
  border: 1px solid var(--rl-line);
  border-radius: var(--rl-radius);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  padding: 4px 16px 16px;
}

.rl-page > .el-tabs > .el-tabs__header {
  margin-bottom: 12px;
}

.rl-page > .el-tabs .el-tabs__item {
  font-size: 13px;
  height: 44px;
}

.rl-page > .el-tabs .el-form--inline {
  padding: 0 0 4px;
  border: 0;
  box-shadow: none;
  background: transparent;
}

/* ---------------------------------------------------------------- 布局栅格 */
.rl-grid {
  display: grid;
  gap: 16px;
}

.rl-grid--2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.rl-grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.rl-grid > .rl-panel + .rl-panel {
  margin-top: 0;
}

.rl-grid + .rl-panel,
.rl-panel + .rl-grid {
  margin-top: 16px;
}

.rl-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 20px;
}

.rl-form-grid .el-form-item {
  margin-bottom: 14px;
}

.rl-form-grid .el-form-item__label {
  padding-bottom: 2px;
  font-size: 12.5px;
  line-height: 18px;
  color: var(--rl-ink-3);
}

.rl-hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 18px;
  color: var(--rl-ink-3);
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px 12px;
}

.rl-hint--warn {
  background: #fffaeb;
  color: #b54708;
}

.mt8 {
  margin-top: 8px;
}

.mt12 {
  margin-top: 12px;
}

.mt16 {
  margin-top: 16px;
}

.rl-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.rl-cell-title {
  color: var(--rl-ink);
  font-weight: 500;
}

.rl-cell-sub {
  color: var(--rl-ink-3);
  font-size: 12px;
  line-height: 18px;
}

/* ---------------------------------------------------------------- 分页 */
.rl-pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 18px;
  border-top: 1px solid var(--rl-line-soft);
}

.rl-pager .el-pagination {
  --el-pagination-font-size: 12.5px;
}

/* ---------------------------------------------------------------- 空态 */
.rl-empty {
  padding: 44px 0 40px;
  text-align: center;
  color: var(--rl-ink-3);
}

.rl-empty__title {
  font-size: 13.5px;
  color: var(--rl-ink-2);
  margin-bottom: 6px;
}

.rl-empty__hint {
  font-size: 12.5px;
  color: var(--rl-ink-4);
  margin-bottom: 16px;
}

/* ---------------------------------------------------------------- 详情 */
.rl-detail-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  background: var(--rl-line-soft);
  border: 1px solid var(--rl-line);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.rl-detail-strip > div {
  background: #fff;
  padding: 12px 14px;
}

.rl-detail-strip span {
  display: block;
  font-size: 12px;
  color: var(--rl-ink-4);
  margin-bottom: 4px;
}

.rl-detail-strip b {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--rl-ink);
}

.rl-kv {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 24px;
}

.rl-kv > div span {
  display: block;
  font-size: 12px;
  color: var(--rl-ink-4);
  margin-bottom: 2px;
}

.rl-kv > div b {
  font-weight: 500;
  color: var(--rl-ink);
  font-size: 13px;
  word-break: break-all;
}

@media (max-width: 1280px) {
  .rl-filters .el-input,
  .rl-filters .el-select,
  .rl-filters .el-input.rl-w-260 {
    width: 168px;
  }
}
</style>
