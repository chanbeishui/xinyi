<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app';
import { getOrder, orderTime, yuan } from '../../features/order/api';
import {
  paymentOptions,
  prepay,
  offlinePayment,
  trustedPayment,
  closeUnpaid,
  type PaymentOptions,
  type PaymentIntent
} from '../../features/payment/api';
import { currentSession } from '../../services/http';
import type { OwnerOrderView, Prepay, Payment } from '../../api/types';
const result = ref<Payment | null>(null);
let closing: { input: { expectedVersion: number; reason: string }; key: string } | null = null;
const conflict = computed(() => result.value?.capacityResolution === 'MANUAL_CONFLICT');
const order = ref<OwnerOrderView | null>(null),
  options = ref<PaymentOptions | null>(null),
  loading = ref(false),
  busy = ref(false),
  error = ref(''),
  notice = ref('');
let orderNo = '',
  seq = 0,
  alive = true,
  attempt: { input: PaymentIntent; key: string; method: 'WECHAT' | 'OFFLINE' } | null = null;
const offlineOption = computed(() => options.value?.options.find((v) => v.method === 'OFFLINE'));
const closed = computed(
  () => order.value?.paymentStatus === 'CLOSED' || order.value?.fulfillmentStatus === 'CLOSED'
);
const paid = computed(() => order.value?.paymentStatus === 'SUCCESS');
const available = computed(
  () =>
    !closed.value &&
    !paid.value &&
    options.value?.newPaymentAllowed &&
    options.value.options.some((v) => v.method === 'WECHAT' && v.available)
);
const title = computed(() =>
  conflict.value
    ? '付款需核实'
    : closed.value
      ? '订单已关闭'
      : paid.value
        ? '预约成功'
        : order.value?.paymentStatus === 'UNKNOWN' ||
            notice.value === '支付确认中' ||
            notice.value.includes('结果待确认')
          ? '支付确认中'
          : notice.value.startsWith('支付未完成')
            ? '支付未完成'
            : options.value?.lockedMethod === 'OFFLINE'
              ? '线下待付款'
              : '确认付款'
);
onLoad((q) => {
  orderNo = String(q?.orderNo || '');
});
onShow(() => {
  if (!busy.value) load();
});
onUnload(() => {
  alive = false;
  seq++;
  attempt = null;
  order.value = null;
  options.value = null;
  result.value = null;
  closing = null;
});
// 先读取可能触发渠道对账的支付结果，再读订单，避免把对账前的旧订单展示为最终状态。
async function load() {
  const run = ++seq;
  loading.value = true;
  error.value = '';
  order.value = null;
  options.value = null;
  try {
    const payment = await trustedPayment(orderNo);
    if (!alive || run !== seq) return;
    result.value = payment;
    const data = await getOrder(orderNo);
    if (!alive || run !== seq) return;
    if (data.view !== 'OWNER') throw new Error('请使用车主身份查看付款');
    order.value = data;
    if (data.paymentStatus !== 'SUCCESS' && data.paymentStatus !== 'CLOSED') {
      const methods = await paymentOptions(orderNo);
      if (alive && run === seq) options.value = methods;
    }
  } catch (e) {
    if (alive && run === seq) error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    if (alive && run === seq) loading.value = false;
  }
}
function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const n = Math.floor(Math.random() * 16);
    return (c === 'x' ? n : (n & 3) | 8).toString(16);
  });
}
async function pay() {
  if (busy.value || !available.value || !options.value) return;
  const selected = options.value;
  // #ifndef MP-WEIXIN
  notice.value = '请在微信小程序内完成支付';
  return;
  // #endif
  // #ifdef MP-WEIXIN
  busy.value = true;
  error.value = '';
  const session = currentSession();
  if (attempt?.method !== 'WECHAT')
    attempt = {
      method: 'WECHAT',
      input: {
        expectedVersion: selected!.paymentVersion,
        expectedPolicyRevisionId: selected!.policyRevisionId
      },
      key: uuid()
    };
  try {
    const params = await prepay(orderNo, attempt!.input, attempt!.key);
    if (
      !alive ||
      currentSession()?.sessionId !== session?.sessionId ||
      currentSession()?.contextVersion !== session?.contextVersion
    )
      return;
    // 微信客户端成功仅表示交互结束，资金事实仍由后续 load() 向服务端核实。
    await launch(params);
    notice.value = '支付确认中';
  } catch (e) {
    const failure = e as { errorCode?: string; errMsg?: string; message?: string };
    if (failure.errorCode === 'VI_CONFIG_CHANGED' || failure.errorCode === 'VI_STATE_CONFLICT')
      attempt = null;
    notice.value = failure.errMsg?.includes('cancel')
      ? '支付未完成，可刷新订单后继续付款'
      : '支付结果待确认，请刷新订单';
    error.value = failure.message || '';
  } finally {
    if (alive) {
      busy.value = false;
      await load();
    }
  }
  // #endif
}
async function chooseOffline() {
  const selected = options.value;
  if (
    busy.value ||
    !selected ||
    selected.lockedMethod !== 'UNSELECTED' ||
    !offlineOption.value?.available
  )
    return;
  const confirmation = await new Promise<boolean>((resolve) =>
    uni.showModal({
      title: '选择企业线下付款',
      content:
        '选择后本订单不能直接改为微信支付。请按公司收款说明付款，实际收款需由财务独立核实，选择本身不算付款。',
      success: (r) => resolve(r.confirm),
      fail: () => resolve(false)
    })
  );
  if (!confirmation || !alive) return;
  busy.value = true;
  error.value = '';
  if (!attempt || attempt.method !== 'OFFLINE')
    attempt = {
      method: 'OFFLINE',
      input: {
        expectedVersion: selected.paymentVersion,
        expectedPolicyRevisionId: selected.policyRevisionId
      },
      key: uuid()
    };
  try {
    await offlinePayment(orderNo, attempt.input, attempt.key);
    notice.value = '已选择企业线下付款，尚未确认实收。请按原收款信息办理。';
  } catch (e) {
    const failure = e as { errorCode?: string; message?: string };
    if (failure.errorCode === 'VI_CONFIG_CHANGED' || failure.errorCode === 'VI_STATE_CONFLICT')
      attempt = null;
    notice.value = failure.message || '选择结果待确认，请刷新订单后再操作';
  } finally {
    if (alive) {
      busy.value = false;
      await load();
    }
  }
}
// 未确认结果保留同一关闭请求的幂等键；刷新后的服务端状态决定是否关闭，客户端不推断未扣款。
async function closeOrder() {
  if (busy.value || loading.value || !order.value || paid.value || closed.value) return;
  const version = order.value.version;
  const confirmed = await new Promise<boolean>((resolve) =>
    uni.showModal({
      title: '关闭未付款预约',
      content: '系统会先核实支付结果。渠道结果未确认时保留名额，不能直接认定未扣款。确认关闭？',
      success: (r) => resolve(r.confirm),
      fail: () => resolve(false)
    })
  );
  if (!confirmed || !alive || busy.value) return;
  busy.value = true;
  if (!closing)
    closing = {
      input: { expectedVersion: version, reason: '车主主动关闭未付款预约' },
      key: uuid()
    };
  try {
    const latest = await closeUnpaid(orderNo, closing.input, closing.key);
    notice.value =
      latest.fulfillmentStatus === 'CLOSED'
        ? '未付款预约已关闭，名额已释放。'
        : latest.paymentStatus === 'SUCCESS'
          ? '已核实付款成功，未按未付订单关闭。'
          : '关闭结果待确认，请稍后刷新；不能认定未扣款。';
  } catch (e) {
    const failure = e as { errorCode?: string; message?: string };
    if (failure.errorCode === 'VI_VERSION_CONFLICT' || failure.errorCode === 'VI_STATE_CONFLICT')
      closing = null;
    notice.value = failure.message || '关闭结果待确认，请刷新订单';
  } finally {
    if (alive) {
      busy.value = false;
      await load();
    }
  }
}
function launch(p: Prepay): Promise<void> {
  return new Promise((resolve, reject) =>
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: p.timeStamp,
      nonceStr: p.nonceStr,
      package: p.package,
      signType: 'RSA',
      paySign: p.paySign,
      success: () => resolve(),
      fail: reject
    })
  );
}
function detail() {
  uni.navigateTo({ url: '/pages/order/detail?orderNo=' + encodeURIComponent(orderNo) });
}
function bookAgain() {
  uni.navigateTo({ url: '/pages/booking/start' });
}
</script>
<template>
  <view class="screen">
    <text class="heading">{{ title }}</text>
    <text v-if="loading" class="muted">正在读取订单…</text>
    <view v-if="order" class="card">
      <text class="muted">检测预约费用</text
      ><text class="amount">¥{{ yuan(order.money.totalAmount) }}</text>
      <text>{{ order.vehicle.plateNo }} · {{ order.station.name }}</text>
      <text class="muted">接车时间：{{ orderTime(order.slotStartAt) }}</text>
      <text v-if="!paid && !closed" class="deadline"
        >付款截止：{{ orderTime(order.payExpireAt) }}</text
      >
      <text v-if="paid && !conflict">付款已确认，可在订单详情查看后续服务安排。</text>
      <text v-if="closed && !conflict">此订单已关闭，如仍需服务，请重新预约。</text>
    </view>
    <view v-if="conflict" class="notice"
      >付款已到账，但预约额度已释放或需人工核查。请联系企业处理，当前不会自动恢复预约。</view
    >
    <view v-if="notice && !paid && !closed" class="notice">{{ notice }}</view>
    <view v-if="order?.paymentStatus === 'UNKNOWN' && !closed" class="notice"
      >正在确认付款结果。请勿重复创建订单，稍后刷新查看。</view
    >
    <view v-if="options && !paid && !closed" class="card">
      <text class="section">支付方式</text>
      <view v-for="method in options.options" :key="method.method" class="method"
        ><text>{{ method.label }}</text
        ><text class="muted">{{
          method.available
            ? options.lockedMethod === method.method
              ? '已选择'
              : '可使用'
            : '不可使用'
        }}</text></view
      >
      <text v-if="!options.newPaymentAllowed" class="notice"
        >当前不能发起付款，请刷新查看订单结果。</text
      >
      <button
        v-if="available"
        class="primary"
        :disabled="busy || loading"
        :loading="busy"
        @click="pay"
      >
        {{ options.lockedMethod === 'WECHAT' ? '继续微信支付' : '微信支付' }}
      </button>
      <button
        v-if="offlineOption?.available && options.lockedMethod === 'UNSELECTED'"
        :disabled="busy || loading"
        @click="chooseOffline"
      >
        选择企业线下付款
      </button>
      <view v-if="options.lockedMethod === 'OFFLINE' && offlineOption" class="offline-info">
        <text>企业收款主体：{{ offlineOption.receiverName }}</text
        ><text>{{ offlineOption.instructions }}</text>
        <text v-if="offlineOption.offlineMethods?.includes('CASH')"
          >可在公司柜台支付现金，不允许代驾个人代收。</text
        >
        <template v-if="offlineOption.receiverAccountDisplay"
          ><text>对公户名：{{ offlineOption.bankAccountName }}</text
          ><text>开户行：{{ offlineOption.bankName }}</text
          ><text class="account"
            >企业账号：{{ offlineOption.receiverAccountDisplay }}</text
          ></template
        >
        <text>企业联系人：{{ offlineOption.contactName }} {{ offlineOption.contactPhone }}</text>
        <text class="notice"
          >线下选择不等于付款成功，实际收款凭证须由财务登记并独立核实。请在原付款期限内办理。</text
        >
      </view>
      <text class="muted">付款结果以订单确认状态为准。</text>
    </view>
    <text v-if="error" class="error">{{ error }}</text>
    <button :disabled="busy || loading" @click="load">刷新订单状态</button>
    <button v-if="order" @click="detail">查看订单详情</button>
    <button
      v-if="
        order?.fulfillmentStatus === 'PENDING_PAY' &&
        !paid &&
        order?.paymentStatus !== 'REVIEW_PENDING'
      "
      :disabled="busy || loading"
      @click="closeOrder"
    >
      关闭未付款预约
    </button>
    <button v-if="closed && !conflict" @click="bookAgain">重新预约</button>
  </view>
</template>
<style scoped>
.offline-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.account {
  overflow-wrap: anywhere;
}
.screen {
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.heading {
  font-size: 26px;
  font-weight: 600;
}
.card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 22px 18px;
  border-radius: 14px;
  background: white;
  line-height: 1.6;
  font-size: 15px;
}
.amount {
  font-size: 40px;
  font-weight: 600;
  color: #005fcb;
}
.muted {
  color: #64748b;
  font-size: 13px;
}
.deadline {
  color: #854d0e;
}
.section {
  font-size: 17px;
  font-weight: 600;
}
.method {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.notice {
  padding: 14px;
  background: #fff4e5;
  border-radius: 10px;
  color: #854d0e;
  line-height: 1.6;
}
.primary {
  background: #005fcb;
  color: white;
}
.error {
  color: #b42318;
}
button {
  width: 100%;
  font-size: 15px;
}
</style>
