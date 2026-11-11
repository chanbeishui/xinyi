<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { restoreSession } from '@/store/session'
import { commonApi } from '@/api'
import { STORAGE } from '@/utils/config'

onLaunch(() => {
  restoreSession()
})

onShow(() => {
  // 已登录时静默校验会话；令牌失效由请求层统一处理跳转
  if (!uni.getStorageSync(STORAGE.access)) return
  commonApi.session().catch(() => {})
})
</script>

<style>
/* ============================================================================
 * 设计体系：检验单（Inspection Slip）
 *
 * 这套小程序流转的其实是一张检测工单：车主下单 → 代驾接车 → 到站检测 → 结果回传。
 * 所以界面按「工单」来组织：纸面白卡、发丝分割线、等宽数字的单号与金额、印章式的
 * 状态标注、深墨色的状态抬头。颜色只用一套：墨（文字）+ 纸（卡片）+ 检测蓝（品牌与
 * 主操作），红/琥珀只留给待支付与风险。
 *
 * 兼容约束（主流小程序环境）：
 *  - 不使用 flex gap（部分安卓 WebView 不生效），间距一律用 margin；
 *  - 不使用 inset 简写（iOS 14.5 以下不支持），定位写全四条边；
 *  - 不使用 grid / backdrop-filter / :has()；
 *  - 尺寸一律 rpx，配合 safe-area 变量，iPhone SE(320) 到 Max(430) 等比适配。
 * ========================================================================== */

page {
  /* 墨与纸 */
  --ink: #0e1726;
  --ink-2: #475467;
  --ink-3: #7b8794;
  --line: #e4e8ef;
  --line-2: #f1f3f7;
  --bg: #edf0f4;
  --card: #ffffff;

  /* 品牌：检测蓝 */
  --brand: #1b4fd8;
  --brand-dark: #1540a8;
  --brand-soft: #eef3ff;

  /* 语义色：只用于状态，不用于装饰 */
  --ok: #12905b;
  --ok-soft: #e9f7f0;
  --warn: #b45309;
  --warn-soft: #fdf4e7;
  --danger: #c0392b;
  --danger-soft: #fceceb;

  /* 兼容旧变量名：已有页面大量使用 var(--text/--sub/--muted) */
  --text: var(--ink);
  --sub: var(--ink-2);
  --muted: var(--ink-3);

  background-color: var(--bg);
  color: var(--ink);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  line-height: 1.55;
}

view,
text,
scroll-view,
input,
textarea,
button {
  box-sizing: border-box;
}

/* 数字统一等宽，金额、单号、车牌、倒计时对齐不跳动 */
.num {
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum';
}

.ellipsis {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ---------------------------------------------------------------- 页面骨架 */
.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 200rpx;
}

/* 无底部 tab 的页面用它收尾，避免最后一张卡贴着底 */
.page--plain {
  padding-bottom: 60rpx;
}

.footer-space {
  height: 160rpx;
}

/* 小屏（iPhone SE 一代 320pt）收紧边距，大屏不放大留白 */
@media (max-width: 340px) {
  .page {
    padding-left: 20rpx;
    padding-right: 20rpx;
  }
}

/* ---------------------------------------------------------------- 卡片 */
.card {
  background: var(--card);
  border: 1rpx solid var(--line);
  border-radius: 20rpx;
  padding: 26rpx 24rpx;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  letter-spacing: 0.3rpx;
  margin-bottom: 8rpx;
}

/* 小节抬头：细小字 + 发丝线，用来给卡片分组，不抢内容 */
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 32rpx 4rpx 16rpx;
}

.section-head__title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--ink);
}

.section-head__meta {
  font-size: 22rpx;
  color: var(--ink-3);
  letter-spacing: 0.6rpx;
}

.eyebrow {
  font-size: 21rpx;
  line-height: 1.4;
  letter-spacing: 2rpx;
  color: var(--ink-3);
  text-transform: uppercase;
}

/* ---------------------------------------------------------------- 墨色抬头 */
/* 三种角色首页的「当前状态」都用这块：它回答“现在是什么状态、下一步做什么” */
.ink-panel {
  position: relative;
  overflow: hidden;
  background: #0e1726;
  border-radius: 20rpx;
  padding: 30rpx 26rpx 26rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.ink-panel__eyebrow {
  font-size: 21rpx;
  letter-spacing: 2.4rpx;
  color: rgba(255, 255, 255, 0.55);
}

.ink-panel__title {
  margin-top: 12rpx;
  font-size: 40rpx;
  line-height: 1.32;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}

.ink-panel__desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.66);
}

.ink-panel__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 26rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.12);
}

/* 细网格底纹：像工单纸上的印刷线，只有墨色抬头用 */
.ink-panel__rule {
  position: absolute;
  top: 0;
  right: 0;
  width: 220rpx;
  height: 100%;
  background-image: linear-gradient(rgba(255, 255, 255, 0.07) 1rpx, transparent 1rpx);
  background-size: 100% 28rpx;
  pointer-events: none;
}

/* ---------------------------------------------------------------- 工单卡 */
.slip {
  background: var(--card);
  border: 1rpx solid var(--line);
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.slip__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24rpx 24rpx 20rpx;
}

.slip__plate {
  font-size: 40rpx;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: 3rpx;
  font-variant-numeric: tabular-nums;
}

.slip__sub {
  margin-top: 8rpx;
  font-size: 23rpx;
  color: var(--ink-3);
}

.slip__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 24rpx;
  border-top: 1rpx solid var(--line-2);
}

/* 卡片首行不再画上边线，否则和卡片自身的边框叠成双线 */
.slip__row:first-child {
  border-top: none;
}

.slip__row--tap:active {
  background: #fafbfc;
}

.slip__key {
  flex: 0 0 auto;
  width: 132rpx;
  font-size: 24rpx;
  color: var(--ink-3);
  letter-spacing: 0.4rpx;
}

.slip__val {
  flex: 1;
  min-width: 0;
  padding-left: 16rpx;
  text-align: right;
  font-size: 27rpx;
  color: var(--ink);
}

.slip__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 24rpx;
  border-top: 1rpx solid var(--line);
  background: #fafbfc;
}

.slip__no {
  font-size: 22rpx;
  color: var(--ink-3);
  letter-spacing: 0.8rpx;
  font-variant-numeric: tabular-nums;
}

/* ---------------------------------------------------------------- 状态章 */
/* 描边小章：颜色只表达状态，不做装饰性着色 */
.status {
  display: inline-flex;
  align-items: center;
  height: 40rpx;
  padding: 0 14rpx;
  border: 1rpx solid currentColor;
  border-radius: 4rpx;
  font-size: 21rpx;
  line-height: 1;
  letter-spacing: 1rpx;
  white-space: nowrap;
  color: var(--ink-2);
  background: #fff;
}

.status--ok {
  color: var(--ok);
  background: var(--ok-soft);
}

.status--warn {
  color: var(--warn);
  background: var(--warn-soft);
}

.status--danger {
  color: var(--danger);
  background: var(--danger-soft);
}

.status--brand {
  color: var(--brand);
  background: var(--brand-soft);
}

.status--gray {
  color: var(--ink-3);
  background: #f4f6f9;
  border-color: var(--line);
}

/* ---------------------------------------------------------------- 步骤条 */
/* 只在真正有序的流程上用：选车 → 时段 → 支付 → 接车 */
.steps {
  display: flex;
  align-items: flex-start;
  margin-top: 26rpx;
}

.step {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.step__no {
  font-size: 20rpx;
  letter-spacing: 1.4rpx;
  color: rgba(255, 255, 255, 0.42);
  font-variant-numeric: tabular-nums;
}

.step--done .step__no,
.step--now .step__no {
  color: #7fa3ff;
}

.step__label {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.6);
}

.step--now .step__label {
  color: #fff;
  font-weight: 600;
}

/* 连线对齐数字行的中轴，而不是压在标签上 */
.step__link {
  flex: 1;
  height: 1rpx;
  margin: 14rpx 12rpx 0;
  background: rgba(255, 255, 255, 0.22);
}

/* ---------------------------------------------------------------- 文本层级 */
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 0;
}

.label {
  color: var(--ink-2);
  font-size: 25rpx;
}

.value {
  font-size: 27rpx;
  text-align: right;
  color: var(--ink);
}

.muted {
  color: var(--ink-3);
  font-size: 23rpx;
  line-height: 1.6;
}

.price {
  color: var(--ink);
  font-size: 34rpx;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.price__unit {
  font-size: 22rpx;
  font-weight: 500;
  margin-right: 2rpx;
  color: var(--ink-2);
}

.divider {
  height: 1rpx;
  background: var(--line-2);
  margin: 18rpx 0;
}

/* ---------------------------------------------------------------- 列表行 */
.list {
  background: var(--card);
  border: 1rpx solid var(--line);
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.list-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 96rpx;
  padding: 18rpx 24rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.list-row:last-child {
  border-bottom: none;
}

.list-row__key {
  font-size: 27rpx;
  color: var(--ink);
}

.list-row__hint {
  font-size: 23rpx;
  color: var(--ink-3);
}

/* 箭头用两条边线画，不依赖「›」字形：不同机型字体宽高不一，文字箭头容易被裁或偏移 */
.chev {
  flex: 0 0 auto;
  width: 13rpx;
  height: 13rpx;
  margin-left: 14rpx;
  margin-right: 2rpx;
  border-top: 2rpx solid #c3ccd8;
  border-right: 2rpx solid #c3ccd8;
  transform: rotate(45deg);
}

/* ---------------------------------------------------------------- 按钮 */
.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  padding: 0 32rpx;
  border-radius: 14rpx;
  background: var(--brand);
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: 0.5rpx;
}

.btn.ghost {
  background: #fff;
  color: var(--brand);
  border: 1rpx solid var(--brand);
}

.btn.gray {
  background: #f2f4f8;
  color: var(--ink-2);
}

.btn.danger {
  background: var(--danger);
}

.btn.small {
  height: 66rpx;
  font-size: 25rpx;
  padding: 0 26rpx;
  border-radius: 10rpx;
}

.btn.block {
  width: 100%;
}

/* 按压反馈统一走 hover-class，避免 :active 在部分机型上不触发 */
.btn-hover {
  opacity: 0.86;
}

.card-hover {
  background: #fafbfc;
}

.btn.disabled {
  opacity: 0.4;
}

/* 说明性小字，跟在按钮或表单下面 */
.form-hint {
  margin-top: 14rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: var(--ink-3);
  text-align: center;
}

/* ---------------------------------------------------------------- 分区标签 */
.tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
  font-size: 21rpx;
  background: var(--brand-soft);
  color: var(--brand);
}

.tag.gray {
  background: #f2f4f8;
  color: var(--ink-2);
}

.tag.ok {
  background: var(--ok-soft);
  color: var(--ok);
}

.tag.warn {
  background: var(--warn-soft);
  color: var(--warn);
}

.tag.danger {
  background: var(--danger-soft);
  color: var(--danger);
}

/* 筛选 chip（横向滚动那排） */
.chip {
  flex-shrink: 0;
  height: 60rpx;
  padding: 0 24rpx;
  border-radius: 30rpx;
  background: #fff;
  color: var(--ink-2);
  font-size: 25rpx;
  line-height: 58rpx;
  border: 1rpx solid var(--line);
}

.chip.on {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

.chip-row {
  display: flex;
  padding: 4rpx 0 12rpx;
}

.chip-row__item {
  margin-right: 12rpx;
}

/* ---------------------------------------------------------------- 表单 */
.field {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--line-2);
}

.field:last-child {
  border-bottom: none;
}

.field-label {
  font-size: 24rpx;
  color: var(--ink-3);
  margin-bottom: 10rpx;
  letter-spacing: 0.4rpx;
}

.input {
  height: 80rpx;
  background: #f6f8fb;
  border: 1rpx solid var(--line-2);
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: var(--ink);
}

.textarea {
  width: 100%;
  min-height: 150rpx;
  background: #f6f8fb;
  border: 1rpx solid var(--line-2);
  border-radius: 10rpx;
  padding: 18rpx 20rpx;
  font-size: 27rpx;
  color: var(--ink);
}

/* ---------------------------------------------------------------- 空态 */
.empty {
  padding: 96rpx 40rpx;
  text-align: center;
}

.empty__title {
  font-size: 28rpx;
  color: var(--ink-2);
}

.empty__hint {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: var(--ink-3);
}

.empty__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 28rpx;
  height: 72rpx;
  padding: 0 36rpx;
  border-radius: 10rpx;
  border: 1rpx solid var(--brand);
  color: var(--brand);
  font-size: 26rpx;
}

/* 骨架屏：加载时占位，避免页面从“加载中…”直接跳到内容 */
.skeleton {
  background: #eef1f5;
  border-radius: 8rpx;
  animation: skeleton-pulse 1.4s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
  100% {
    opacity: 1;
  }
}

/* ---------------------------------------------------------------- 底部操作条 */
.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid var(--line);
}

.action-bar .btn {
  flex: 1;
}

.action-bar .btn + .btn {
  margin-left: 16rpx;
}

/* ---------------------------------------------------------------- 弹层 */
.mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 90;
  background: rgba(14, 23, 38, 0.45);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  max-height: 86vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 28rpx 26rpx;
  padding-bottom: calc(28rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
}

.sheet__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid var(--line-2);
}

.sheet__title {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 0.4rpx;
}

.sheet__close {
  font-size: 24rpx;
  color: var(--ink-3);
  padding: 6rpx 0 6rpx 20rpx;
}
</style>
