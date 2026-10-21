<template>
  <view class="page">
    <view v-if="order" class="hero">
      <view class="hero-title">{{ order.fulfillmentStatusLabel }}</view>
      <view class="hero-sub">{{ order.plateNo }} · {{ order.vehicleTypeName }}</view>
      <view class="hero-window">上门时段 {{ order.pickupWindowLabel }}</view>
    </view>

    <view v-if="order" class="card">
      <view class="card-title">接车与送检</view>
      <view class="row-between">
        <text class="label">接车地址</text>
        <text class="value" style="max-width: 62%">{{ order.address }}</text>
      </view>
      <view class="row-between">
        <text class="label">检测点</text>
        <text class="value">{{ order.stationName }}</text>
      </view>
      <view class="row-between">
        <text class="label">检测点地址</text>
        <text class="value" style="max-width: 62%">{{ order.stationAddress }}</text>
      </view>
      <view class="row-between">
        <text class="label">联系人</text>
        <text class="value">{{ order.contactName || '-' }}</text>
      </view>
      <view class="row-between">
        <text class="label">联系电话</text>
        <text class="value">{{ order.contactPhone || '履约后可见' }}</text>
      </view>
    </view>

    <view v-if="order" class="card">
      <view class="card-title">履约操作</view>
      <view v-if="!order.allowedActions || !order.allowedActions.length" class="muted">
        当前状态无需操作，请按流程等待下一步。
      </view>
      <view v-for="a in order.allowedActions" :key="a" class="action-row">
        <view class="action-main">
          <view class="value" style="text-align: left">{{ actionLabel(a) }}</view>
          <view class="muted">{{ actionHint(a) }}</view>
        </view>
        <view class="btn small" :class="{ gray: a === 'CANCEL_ASSIGNMENT' }" @tap="runAction(a)">
          {{ actionButton(a) }}
        </view>
      </view>
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
      <view class="row" style="gap: 16rpx; margin-top: 20rpx">
        <view v-if="handover.canViewCode" class="btn ghost small" @tap="loadCode">查看交接码</view>
        <view v-if="handover.canConfirm" class="btn small" @tap="showConfirm = true">核验交接码</view>
      </view>
    </view>

    <!-- 留证 -->
    <view class="card">
      <view class="card-title">车况留证</view>
      <view class="muted">接车、到站、取车、归还四个节点各需 4 张照片（前、后、左、右）与仪表里程。</view>

      <view v-for="g in groups" :key="g.groupId" class="ev-group">
        <view class="row-between">
          <text class="value" style="text-align: left">节点 {{ g.nodeNo }} · {{ g.nodeLabel }}</text>
          <text class="tag" :class="g.status === 'SEALED' ? 'ok' : 'gray'">{{ g.status === 'SEALED' ? '已封存' : '待完成' }}</text>
        </view>
        <view class="muted">照片 {{ g.photoCount }} 张 · 里程 {{ g.mileageKm || '未填写' }}{{ g.mileageKm ? ' km' : '' }}</view>
      </view>

      <view v-if="currentGroup && currentGroup.status !== 'SEALED'" class="ev-editor">
        <view class="divider"></view>
        <view class="muted">当前编辑：节点 {{ currentGroup.nodeNo }} {{ currentGroup.nodeLabel }}</view>
        <view class="photo-grid">
          <view v-for="p in photoTypes" :key="p.code" class="photo" @tap="uploadPhoto(p.code)">
            <view v-if="photos[p.code]" class="photo-img" :style="{ backgroundImage: `url(${photos[p.code]})` }"></view>
            <view v-else class="photo-empty">{{ p.label }}</view>
          </view>
        </view>
        <view class="field" style="margin-top: 12rpx">
          <view class="field-label">仪表里程（km）</view>
          <input v-model="mileage" class="input" type="digit" placeholder="例如 38652" />
        </view>
        <view class="row" style="gap: 16rpx; margin-top: 16rpx">
          <view class="btn gray small" style="flex: 1" @tap="saveMileage">保存里程</view>
          <view class="btn small" style="flex: 1" @tap="sealGroup">提交封存</view>
        </view>
      </view>

      <view v-if="!currentGroup" class="btn ghost" style="margin-top: 20rpx" @tap="createGroup">新建留证</view>
    </view>

    <view class="card">
      <view class="card-title">异常上报</view>
      <view class="field">
        <view class="field-label">异常说明</view>
        <textarea v-model="exceptionDesc" class="textarea" placeholder="如车辆无法启动、地址无法进入等" />
      </view>
      <view class="btn ghost" style="margin-top: 16rpx" @tap="reportException">提交异常</view>
    </view>

    <!-- 交接码核验 -->
    <view v-if="showConfirm" class="mask" @tap="showConfirm = false">
      <view class="sheet" @tap.stop>
        <view class="card-title">核验交接码</view>
        <view class="muted">请向车主索要 4 位交接码，连续 3 次错误将锁定本阶段交接。</view>
        <input v-model="confirmCode" class="input" type="number" maxlength="4" placeholder="4 位数字" style="margin-top: 20rpx" />
        <view class="btn" style="margin-top: 24rpx" @tap="submitConfirm">确认交接</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { commonApi, driverApi, handoverApi } from '@/api'
import { API_BASE } from '@/utils/config'

const orderNo = ref('')
const order = ref<any>(null)
const handover = ref<any>(null)
const code = ref<any>(null)
const groups = ref<any[]>([])
const showConfirm = ref(false)
const confirmCode = ref('')
const exceptionDesc = ref('')
const mileage = ref('')
const photos = reactive<Record<string, string>>({})

const photoTypes = [
  { code: 'FRONT', label: '车头' },
  { code: 'BACK', label: '车尾' },
  { code: 'LEFT', label: '左侧' },
  { code: 'RIGHT', label: '右侧' }
]

const currentGroup = computed(() => {
  const editable = groups.value.filter((g) => g.status !== 'SEALED')
  return editable.length ? editable[editable.length - 1] : null
})

onLoad((options) => {
  orderNo.value = (options && options.orderNo) || ''
})

onShow(() => {
  if (orderNo.value) load()
})

async function load() {
  try {
    order.value = await driverApi.order(orderNo.value)
  } catch (e) {
    order.value = null
  }
  await loadHandover()
  await loadEvidence()
}

function actionLabel(a: string) {
  const map: Record<string, string> = {
    DEPART: '前往接车',
    ARRIVE_PICKUP: '到达接车点',
    CREATE_HANDOVER: '发起交接',
    ARRIVE_STATION: '到达检测点',
    WAIT_STATION: '等待检测点收车',
    CANCEL_ASSIGNMENT: '取消承接'
  }
  return map[a] || a
}

function actionButton(a: string) {
  const map: Record<string, string> = {
    DEPART: '出发',
    ARRIVE_PICKUP: '已到达',
    CREATE_HANDOVER: '交接',
    ARRIVE_STATION: '已到站',
    WAIT_STATION: '刷新',
    CANCEL_ASSIGNMENT: '取消'
  }
  return map[a] || '执行'
}

function actionHint(a: string) {
  const map: Record<string, string> = {
    DEPART: '点击后开始前往接车地点，系统记录出发时间',
    ARRIVE_PICKUP: '到达后请与车主核对交接码并完成车况留证',
    CREATE_HANDOVER: '按当前节点生成交接码，由核验方确认',
    ARRIVE_STATION: '到站后需与检测点完成收车交接',
    WAIT_STATION: '检测点完成收车与检测后会推进状态',
    CANCEL_ASSIGNMENT: '已接车不可自助解除，取消将记录并影响派单'
  }
  return map[a] || ''
}

async function runAction(a: string) {
  try {
    if (a === 'DEPART') await driverApi.depart(orderNo.value)
    else if (a === 'ARRIVE_PICKUP') await driverApi.arrivePickup(orderNo.value)
    else if (a === 'ARRIVE_STATION') await driverApi.arriveStation(orderNo.value)
    else if (a === 'CREATE_HANDOVER') {
      const type = order.value && order.value.nextHandoverType
      if (!type) {
        uni.showToast({ title: '当前无待交接节点', icon: 'none' })
        return
      }
      handover.value = await handoverApi.create(orderNo.value, type)
      uni.showToast({ title: '已生成交接', icon: 'success' })
      return
    } else if (a === 'CANCEL_ASSIGNMENT') {
      cancelAssignment()
      return
    }
    uni.showToast({ title: '已提交', icon: 'success' })
    await load()
  } catch (e) {
    // 已提示
  }
}

function cancelAssignment() {
  uni.showModal({
    title: '取消承接',
    content: '取消后将释放本次派单并记录，是否继续？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await driverApi.cancelAssignment(orderNo.value, '司机端取消承接')
        uni.showToast({ title: '已取消', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 600)
      } catch (e) {
        // 已提示
      }
    }
  })
}

async function loadHandover() {
  const type = order.value && order.value.nextHandoverType
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
      evidence: currentGroup.value ? { nodeNo: currentGroup.value.nodeNo } : null
    })
    showConfirm.value = false
    confirmCode.value = ''
    uni.showToast({ title: '交接完成', icon: 'success' })
    await load()
  } catch (e) {
    // 已提示
  }
}

async function loadEvidence() {
  try {
    const view: any = await driverApi.evidence(orderNo.value)
    groups.value = (view && view.groups) || []
    const g = currentGroup.value
    if (g && g.mileageKm) mileage.value = String(g.mileageKm)
  } catch (e) {
    groups.value = []
  }
}

function nextNodeNo(): number {
  const s = order.value && order.value.fulfillmentStatus
  if (s === 'ACCEPTED' || s === 'GOING_PICKUP' || s === 'ARRIVED_PICKUP') return 1
  if (s === 'PICKED_UP' || s === 'ARRIVED_STATION') return 2
  if (s === 'INSPECTED') return 3
  return 4
}

async function createGroup() {
  try {
    await driverApi.createEvidence(orderNo.value, { nodeNo: nextNodeNo() })
    uni.showToast({ title: '已创建留证', icon: 'success' })
    await loadEvidence()
  } catch (e) {
    // 已提示
  }
}

function uploadPhoto(typeCode: string) {
  if (!currentGroup.value) {
    uni.showToast({ title: '请先新建留证', icon: 'none' })
    return
  }
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      try {
        const file: any = await commonApi.upload(res.tempFilePaths[0], 'EVIDENCE_PHOTO', orderNo.value)
        const link: any = await commonApi.fileAccess(file.fileId)
        photos[typeCode] = `${API_BASE}${link.url}`
        await driverApi.addPhoto(orderNo.value, currentGroup.value.groupId, {
          photoType: typeCode,
          fileId: Number(file.fileId),
          clientCaptureId: `${typeCode}-${Date.now()}`,
          capturedAt: new Date().toISOString()
        })
        uni.showToast({ title: '已上传', icon: 'success' })
        await loadEvidence()
      } catch (e) {
        // 已提示
      }
    }
  })
}

async function saveMileage() {
  if (!currentGroup.value) return
  if (!mileage.value) {
    uni.showToast({ title: '请填写里程', icon: 'none' })
    return
  }
  try {
    await driverApi.setMileage(orderNo.value, currentGroup.value.groupId, {
      mileageKm: mileage.value,
      source: 'DASHBOARD',
      reason: '司机端录入'
    })
    uni.showToast({ title: '已保存', icon: 'success' })
    await loadEvidence()
  } catch (e) {
    // 已提示
  }
}

async function sealGroup() {
  if (!currentGroup.value) return
  try {
    await driverApi.evidenceReady(orderNo.value, currentGroup.value.groupId)
    uni.showToast({ title: '已封存', icon: 'success' })
    await loadEvidence()
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
    await driverApi.reportException(orderNo.value, {
      type: 'SERVICE',
      description: exceptionDesc.value
    })
    exceptionDesc.value = ''
    uni.showToast({ title: '已上报', icon: 'success' })
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
  font-size: 38rpx;
  font-weight: 600;
}

.hero-sub {
  margin-top: 8rpx;
  font-size: 26rpx;
  opacity: 0.92;
}

.hero-window {
  margin-top: 12rpx;
  font-size: 24rpx;
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

.ev-group {
  padding: 14rpx 0;
  border-bottom: 1rpx solid var(--line);
}

.photo-grid {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}

.photo {
  flex: 1;
  height: 130rpx;
  border-radius: 12rpx;
  overflow: hidden;
  border: 1rpx dashed var(--line);
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.photo-img {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.photo-empty {
  font-size: 22rpx;
  color: var(--muted);
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
