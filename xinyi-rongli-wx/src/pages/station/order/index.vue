<template>
  <view class="page">
    <view v-if="order" class="hero">
      <view class="hero-title">{{ order.fulfillmentStatusLabel }}</view>
      <view class="hero-sub">{{ order.plateNo }} · {{ order.vehicleTypeName }}</view>
      <view class="hero-window">上门 {{ windowLabel(order.pickupStartsAt) }}</view>
    </view>

    <view v-if="order" class="card">
      <view class="card-title">订单信息</view>
      <view class="row-between">
        <text class="label">订单号</text>
        <text class="value">{{ order.orderNo }}</text>
      </view>
      <view class="row-between">
        <text class="label">车主</text>
        <text class="value">{{ order.ownerName || order.contactName || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">代驾</text>
        <text class="value">{{ order.driverName || '未分配' }}</text>
      </view>
      <view class="row-between">
        <text class="label">车辆保管</text>
        <text class="value">{{ order.custodyType || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">证件核对</text>
        <text class="tag" :class="order.licenseStatus === 'VERIFIED' ? 'ok' : 'warn'">{{ order.licenseStatus }}</text>
      </view>
    </view>

    <view v-if="order" class="card">
      <view class="card-title">处理动作</view>
      <view v-for="a in order.allowedActions" :key="a" class="action-row">
        <view class="action-main">
          <view class="value" style="text-align: left">{{ actionLabel(a) }}</view>
          <view class="muted">{{ actionHint(a) }}</view>
        </view>
        <view class="btn small" @tap="runAction(a)">{{ actionButton(a) }}</view>
      </view>
    </view>

    <!-- 证件核对 -->
    <view v-if="license" class="card">
      <view class="card-title">行驶证核对</view>
      <view class="field">
        <view class="field-label">车主姓名</view>
        <input v-model="licenseForm.ownerName" class="input" placeholder="行驶证登记所有人" />
      </view>
      <view class="field">
        <view class="field-label">车牌号</view>
        <input v-model="licenseForm.plateNo" class="input" placeholder="与行驶证一致" />
      </view>
      <view class="field">
        <view class="field-label">车架号</view>
        <input v-model="licenseForm.vin" class="input" placeholder="VIN" />
      </view>
      <view class="field">
        <view class="field-label">行驶证照片</view>
        <view class="row" style="gap: 16rpx; margin-top: 8rpx">
          <view class="btn ghost small" @tap="pickLicensePhoto('main')">
            {{ mainFileId ? '重拍正页' : '拍摄正页' }}
          </view>
          <view class="btn ghost small" @tap="pickLicensePhoto('sub')">
            {{ subFileId ? '重拍副页' : '拍摄副页' }}
          </view>
        </view>
        <view class="muted" style="margin-top: 10rpx">
          正页 {{ mainFileId ? '已上传' : '未上传' }} · 副页 {{ subFileId ? '已上传' : '未上传' }}
        </view>
      </view>
      <view class="row" style="margin-top: 12rpx">
        <text class="label">已核对无误</text>
        <switch :checked="licenseForm.verified" @change="(e:any) => (licenseForm.verified = e.detail.value)" />
      </view>
      <view class="btn ghost" style="margin-top: 18rpx" @tap="saveLicense">保存核对结果</view>
    </view>

    <!-- 结果登记 -->
    <view class="card">
      <view class="card-title">检测结果</view>
      <view v-if="result" class="row-between">
        <text class="label">当前结论</text>
        <text class="tag" :class="result.conclusion === 'PASS' ? 'ok' : 'danger'">
          {{ result.conclusionLabel || result.conclusion }}
        </text>
      </view>
      <view class="field">
        <view class="field-label">检测结论</view>
        <picker mode="selector" :range="conclusions" :value="resultIndex" @change="onConclusionChange">
          <view class="input picker">{{ conclusions[resultIndex] }}</view>
        </picker>
      </view>
      <view class="field">
        <view class="field-label">检测项目（JSON）</view>
        <!-- 属性值内不能出现双引号：uni 编译 WXML 时会统一用双引号包裹属性，内层引号会截断属性导致编译失败 -->
        <textarea v-model="resultForm.items" class="textarea" placeholder="例如 制动:合格,灯光:合格" />
        <view class="muted" style="margin-top: 8rpx">需填 JSON 对象，键和值都要加双引号，如 {"制动":"合格"}</view>
      </view>
      <view class="btn ghost" style="margin-top: 16rpx" @tap="submitResult">提交检测结果</view>
      <view class="btn gray" style="margin-top: 12rpx" @tap="notifyPickup">通知取车</view>
    </view>

    <!-- 交接 -->
    <view v-if="handover" class="card">
      <view class="card-title">{{ handover.handoverTypeLabel }}</view>
      <view class="row-between">
        <text class="label">状态</text>
        <text class="tag" :class="handover.status === 'CONFIRMED' ? 'ok' : 'warn'">{{ handover.statusLabel }}</text>
      </view>
      <view v-if="handover.actionHint" class="muted" style="margin-top: 12rpx">{{ handover.actionHint }}</view>
      <view v-if="code" class="code-box">
        <view class="code">{{ code.code }}</view>
        <view class="muted">{{ code.displayHint }}</view>
      </view>
      <view class="row" style="gap: 16rpx; margin-top: 18rpx">
        <view v-if="handover.canViewCode" class="btn ghost small" @tap="loadCode">查看交接码</view>
        <view v-if="handover.canConfirm" class="btn small" @tap="showConfirm = true">核验交接码</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">异常上报</view>
      <view class="field">
        <view class="field-label">异常说明</view>
        <textarea v-model="exceptionDesc" class="textarea" placeholder="如证件不符、车辆异常等" />
      </view>
      <view class="btn ghost" style="margin-top: 16rpx" @tap="reportException">提交异常</view>
    </view>

    <view v-if="showConfirm" class="mask" @tap="showConfirm = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">核验交接码</view>
        <view class="muted">请输入代驾出示的 4 位交接码。</view>
        <input v-model="confirmCode" class="input" type="number" maxlength="4" placeholder="4 位数字" style="margin-top: 20rpx" />
        <view class="btn" style="margin-top: 24rpx" @tap="submitConfirm">确认收车</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { handoverApi, commonApi, stationApi } from '@/api'
import { windowLabel } from '@/utils/format'

const orderNo = ref('')
const order = ref<any>(null)
const license = ref<any>(null)
const result = ref<any>(null)
const handover = ref<any>(null)
const code = ref<any>(null)
const showConfirm = ref(false)
const confirmCode = ref('')
const exceptionDesc = ref('')
const mainFileId = ref('')
const subFileId = ref('')

const conclusions = ['合格', '不合格']
const conclusionCodes = ['PASS', 'FAIL']
const resultIndex = ref(0)

const licenseForm = reactive<any>({ ownerName: '', plateNo: '', vin: '', verified: false })
const resultForm = reactive<any>({ items: '' })

const currentHandoverType = computed(() => {
  const s = order.value && order.value.fulfillmentStatus
  if (s === 'ARRIVED_STATION') return 'DRIVER_TO_STATION'
  if (s === 'INSPECTED' || s === 'CAR_TAKEN') return 'STATION_TO_DRIVER'
  return null
})

onLoad((options) => {
  orderNo.value = (options && options.orderNo) || ''
})

onShow(() => {
  if (orderNo.value) load()
})

async function load() {
  try {
    order.value = await stationApi.order(orderNo.value)
  } catch (e) {
    order.value = null
  }
  await Promise.all([loadLicense(), loadResult(), loadHandover()])
}

async function loadLicense() {
  try {
    license.value = await stationApi.license(orderNo.value)
    if (license.value) {
      licenseForm.ownerName = license.value.ownerName || ''
      licenseForm.plateNo = license.value.plateNo || (order.value && order.value.plateNo) || ''
      licenseForm.vin = license.value.vin || ''
      licenseForm.verified = license.value.reviewStatus === 'VERIFIED'
      mainFileId.value = license.value.mainFileId ? String(license.value.mainFileId) : ''
      subFileId.value = license.value.subFileId ? String(license.value.subFileId) : ''
    }
  } catch (e) {
    license.value = null
  }
}

/** 拍摄/重拍行驶证正页或副页，上传后即刻可用于核验留档 */
async function pickLicensePhoto(side: 'main' | 'sub') {
  const chosen: any = await new Promise((resolve) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: resolve,
      fail: () => resolve(null)
    })
  })
  const path = chosen && chosen.tempFilePaths && chosen.tempFilePaths[0]
  if (!path) return
  try {
    const file: any = await commonApi.upload(path, 'LICENSE_PHOTO', orderNo.value)
    if (side === 'main') {
      mainFileId.value = String(file.fileId || '')
    } else {
      subFileId.value = String(file.fileId || '')
    }
    uni.showToast({ title: '照片已上传', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}

async function loadResult() {
  try {
    const view: any = await stationApi.results(orderNo.value)
    result.value = view && view.current ? view.current : view && view.conclusion ? view : null
    if (result.value && result.value.conclusion) {
      const i = conclusionCodes.indexOf(result.value.conclusion)
      resultIndex.value = i < 0 ? 0 : i
    }
  } catch (e) {
    result.value = null
  }
}

async function loadHandover() {
  const type = currentHandoverType.value
  if (!type) {
    handover.value = null
    return
  }
  try {
    handover.value = await handoverApi.create(orderNo.value, type)
  } catch (e) {
    handover.value = null
  }
}

function actionLabel(a: string) {
  const map: Record<string, string> = {
    RECEIVE_CAR: '收车交接',
    REGISTER_RESULT: '登记检测结果',
    RELEASE_CAR: '放车交接',
    REVIEW_LICENSE: '行驶证核对',
    VIEW_EVIDENCE: '查看留证'
  }
  return map[a] || a
}

function actionHint(a: string) {
  const map: Record<string, string> = {
    RECEIVE_CAR: '代驾到站后核对交接码并收车',
    REGISTER_RESULT: '检测完成后登记结论与项目',
    RELEASE_CAR: '生成取车交接码，由代驾核验后放车',
    REVIEW_LICENSE: '核对行驶证信息与车辆一致性',
    VIEW_EVIDENCE: '查看代驾上传的车况留证'
  }
  return map[a] || ''
}

function actionButton(a: string) {
  const map: Record<string, string> = {
    RECEIVE_CAR: '收车',
    REGISTER_RESULT: '登记',
    RELEASE_CAR: '放车',
    REVIEW_LICENSE: '核对',
    VIEW_EVIDENCE: '查看'
  }
  return map[a] || '执行'
}

async function runAction(a: string) {
  if (a === 'RECEIVE_CAR' || a === 'RELEASE_CAR') {
    const type = a === 'RECEIVE_CAR' ? 'DRIVER_TO_STATION' : 'STATION_TO_DRIVER'
    try {
      handover.value = await handoverApi.create(orderNo.value, type)
      uni.showToast({ title: '已生成交接', icon: 'success' })
    } catch (e) {
      // 已提示
    }
    return
  }
  if (a === 'REVIEW_LICENSE') {
    uni.pageScrollTo({ selector: '.card', duration: 200 })
    return
  }
  if (a === 'REGISTER_RESULT') {
    uni.pageScrollTo({ scrollTop: 600, duration: 200 })
    return
  }
  if (a === 'VIEW_EVIDENCE') {
    uni.showToast({ title: '留证由代驾上传，可在详情查看时间线', icon: 'none' })
  }
}

function onConclusionChange(e: any) {
  resultIndex.value = Number(e.detail.value)
}

async function saveLicense() {
  try {
    await stationApi.saveLicense(orderNo.value, {
      ownerName: licenseForm.ownerName,
      plateNo: licenseForm.plateNo,
      vin: licenseForm.vin,
      registeredOn: null,
      mainFileId: mainFileId.value ? Number(mainFileId.value) : null,
      subFileId: subFileId.value ? Number(subFileId.value) : null,
      verified: licenseForm.verified
    })
    uni.showToast({ title: '已保存', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}

async function submitResult() {
  let items = '{}'
  if (resultForm.items) {
    try {
      JSON.parse(resultForm.items)
      items = resultForm.items
    } catch (e) {
      uni.showToast({ title: '检测项目需为合法 JSON', icon: 'none' })
      return
    }
  }
  try {
    await stationApi.submitResult(orderNo.value, {
      conclusion: conclusionCodes[resultIndex.value],
      items,
      inspectedAt: new Date().toISOString()
    })
    uni.showToast({ title: '已登记', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}

async function notifyPickup() {
  try {
    await stationApi.pickupNotification(orderNo.value)
    uni.showToast({ title: '已通知取车', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}

async function loadCode() {
  try {
    code.value = await handoverApi.secret(orderNo.value, handover.value.handoverId)
  } catch (e) {
    // 已提示
  }
}

async function submitConfirm() {
  if (!/\d{4}/.test(confirmCode.value)) {
    uni.showToast({ title: '请输入 4 位交接码', icon: 'none' })
    return
  }
  try {
    await handoverApi.confirm(orderNo.value, handover.value.handoverId, {
      code: confirmCode.value,
      version: order.value.version,
      evidence: null
    })
    showConfirm.value = false
    confirmCode.value = ''
    uni.showToast({ title: '交接完成', icon: 'success' })
    await load()
  } catch (e) {
    // 已提示
  }
}

async function reportException() {
  if (!exceptionDesc.value) {
    uni.showToast({ title: '请填写异常说明', icon: 'none' })
    return
  }
  try {
    await stationApi.reportException(orderNo.value, { type: 'SERVICE', description: exceptionDesc.value })
    exceptionDesc.value = ''
    uni.showToast({ title: '已提交', icon: 'success' })
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f6feb 0%, #3f8bff 100%);
  border-radius: 28rpx;
  padding: 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
}

.hero-title {
  font-size: 36rpx;
  font-weight: 600;
}

.hero-sub {
  margin-top: 8rpx;
  font-size: 25rpx;
  opacity: 0.9;
}

.hero-window {
  margin-top: 10rpx;
  font-size: 23rpx;
  opacity: 0.85;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--line);
  gap: 20rpx;
}

.action-row:last-child {
  border-bottom: none;
}

.action-main {
  flex: 1;
}

.picker {
  display: flex;
  align-items: center;
}

.code-box {
  margin-top: 18rpx;
  background: var(--brand-soft);
  border-radius: 16rpx;
  padding: 22rpx;
  text-align: center;
}

.code {
  font-size: 60rpx;
  font-weight: 700;
  letter-spacing: 12rpx;
  color: var(--brand);
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
}
</style>
