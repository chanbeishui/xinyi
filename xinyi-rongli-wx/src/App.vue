<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { restoreSession } from '@/store/session'
import { commonApi } from '@/api'

onLaunch(() => {
  restoreSession()
})

onShow(() => {
  // 已登录时静默校验会话；令牌失效由请求层统一处理跳转
  if (!uni.getStorageSync('rongli_access_token')) return
  commonApi.session().catch(() => {})
})
</script>

<style>
page {
  --brand: #1f6feb;
  --brand-dark: #1758c4;
  --brand-soft: #eaf1ff;
  --bg: #f5f6f8;
  --card: #ffffff;
  --text: #1a1d24;
  --sub: #6b7280;
  --muted: #9aa1ac;
  --line: #edeff3;
  --ok: #12b76a;
  --warn: #f79009;
  --danger: #e5484d;
  background-color: var(--bg);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
  font-size: 28rpx;
  line-height: 1.5;
}

view,
text,
scroll-view,
input,
textarea {
  box-sizing: border-box;
}

.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 200rpx;
}

.card {
  background: var(--card);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(17, 24, 39, 0.04);
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

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
  color: var(--sub);
  font-size: 26rpx;
}

.value {
  font-size: 28rpx;
  text-align: right;
}

.muted {
  color: var(--muted);
  font-size: 24rpx;
}

.price {
  color: var(--danger);
  font-size: 36rpx;
  font-weight: 600;
}

.divider {
  height: 1rpx;
  background: var(--line);
  margin: 20rpx 0;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 16rpx;
  background: var(--brand);
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
}

.btn.ghost {
  background: #fff;
  color: var(--brand);
  border: 1rpx solid var(--brand);
}

.btn.gray {
  background: #f0f1f4;
  color: var(--sub);
}

.btn.danger {
  background: var(--danger);
}

.btn.small {
  height: 64rpx;
  font-size: 26rpx;
  padding: 0 28rpx;
  border-radius: 12rpx;
}

.btn.disabled {
  opacity: 0.45;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  background: var(--brand-soft);
  color: var(--brand);
}

.tag.gray {
  background: #f2f3f5;
  color: var(--sub);
}

.tag.ok {
  background: #e7f8ef;
  color: var(--ok);
}

.tag.warn {
  background: #fff5e6;
  color: var(--warn);
}

.tag.danger {
  background: #fdeaea;
  color: var(--danger);
}

.field {
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.field:last-child {
  border-bottom: none;
}

.field-label {
  font-size: 26rpx;
  color: var(--sub);
  margin-bottom: 10rpx;
}

.input {
  height: 76rpx;
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.textarea {
  width: 100%;
  min-height: 150rpx;
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.empty {
  padding: 120rpx 0;
  text-align: center;
  color: var(--muted);
  font-size: 26rpx;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid var(--line);
  display: flex;
  gap: 16rpx;
}

.footer-space {
  height: 160rpx;
}
</style>
