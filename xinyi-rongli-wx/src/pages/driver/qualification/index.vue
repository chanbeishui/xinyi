<template>
  <view class="page">
    <view class="card">
      <view class="row-between">
        <view class="card-title" style="margin: 0">代驾资格</view>
        <view class="tag" :class="statusClass">{{ statusLabel }}</view>
      </view>
      <view class="muted" style="margin-top: 10rpx">
        需提交实名信息、驾驶证资料并通过审核后方可接单；准驾车型不符或证件过期将无法通过审核。
      </view>
      <view v-if="view.reviewOpinion" class="muted" style="margin-top: 10rpx">审核意见：{{ view.reviewOpinion }}</view>
      <view v-if="view.reviewDueAt" class="muted" style="margin-top: 6rpx">下次复审：{{ dateOnly(view.reviewDueAt) }}</view>
    </view>

    <view class="card">
      <view class="card-title">实名与证件</view>
      <view class="field">
        <view class="field-label">真实姓名</view>
        <input v-model="form.realName" class="input" placeholder="与身份证一致" :disabled="!view.canEdit" />
      </view>
      <view class="field">
        <view class="field-label">身份证号</view>
        <input v-model="form.idCardNo" class="input" maxlength="18" placeholder="18 位身份证号" :disabled="!view.canEdit" />
      </view>
      <view class="field">
        <view class="field-label">准驾车型</view>
        <picker mode="selector" :range="licenseClasses" :value="licenseIndex" @change="onLicenseChange" :disabled="!view.canEdit">
          <view class="input picker">{{ form.licenseClass || '请选择准驾车型' }}</view>
        </picker>
      </view>
      <view class="field">
        <view class="field-label">驾驶证有效期至</view>
        <picker mode="date" :value="form.licenseValidTo" @change="onDateChange" :disabled="!view.canEdit">
          <view class="input picker">{{ form.licenseValidTo || '请选择有效期' }}</view>
        </picker>
      </view>
      <view class="row" style="margin-top: 12rpx">
        <text class="label">长期有效</text>
        <switch :checked="form.licenseLongTerm" :disabled="!view.canEdit" @change="(e:any) => (form.licenseLongTerm = e.detail.value)" />
      </view>
    </view>

    <view class="card">
      <view class="card-title">证件照片</view>
      <view class="muted">需上传身份证正面与驾驶证正页，仅用于资格审核。</view>
      <view class="photos">
        <view class="photo" @tap="pickPhoto('idCard')">
          <view v-if="photoUrls.idCard" class="photo-img" :style="{ backgroundImage: `url(${photoUrls.idCard})` }"></view>
          <view v-else class="photo-empty">身份证</view>
        </view>
        <view class="photo" @tap="pickPhoto('license')">
          <view v-if="photoUrls.license" class="photo-img" :style="{ backgroundImage: `url(${photoUrls.license})` }"></view>
          <view v-else class="photo-empty">驾驶证</view>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">实名活体核验</view>
      <view class="muted">活体核验用于确认本人操作，联调环境可模拟完成。</view>
      <view class="row" style="gap: 16rpx; margin-top: 18rpx">
        <view class="btn ghost small" style="flex: 1" @tap="startFace">发起核验</view>
        <view class="btn small" style="flex: 1" @tap="mockFace">模拟完成</view>
      </view>
      <view v-if="faceInfo" class="muted" style="margin-top: 12rpx">
        核验会话 {{ faceInfo.verificationSessionId }} · {{ faceInfo.hint }}
      </view>
    </view>

    <view class="action-bar">
      <view v-if="view.canEdit" class="btn gray" style="flex: 1" @tap="save">保存草稿</view>
      <view v-if="view.canSubmit" class="btn" style="flex: 1" @tap="submit">提交审核</view>
      <view v-if="!view.canEdit && !view.canSubmit" class="btn gray" style="flex: 1" @tap="load">刷新状态</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { commonApi, driverApi } from '@/api'
import { API_BASE } from '@/utils/config'
import { dateOnly } from '@/utils/format'

const view = ref<any>({})
const faceInfo = ref<any>(null)
const licenseClasses = ['C1', 'C2', 'B1', 'B2', 'A1', 'A2']
const fileIds = reactive<{ idCard: string; license: string }>({ idCard: '', license: '' })
const photoUrls = reactive<{ idCard: string; license: string }>({ idCard: '', license: '' })

const form = reactive<any>({
  realName: '',
  idCardNo: '',
  licenseClass: '',
  licenseValidTo: '',
  licenseLongTerm: false
})

const licenseIndex = computed(() => {
  const i = licenseClasses.indexOf(form.licenseClass)
  return i < 0 ? 0 : i
})

const statusLabel = computed(() => view.value.applicationStatusLabel || mapStatus(view.value.qualificationStatus))
const statusClass = computed(() => {
  const s = view.value.qualificationStatus
  if (s === 'APPROVED') return 'ok'
  if (s === 'REJECTED' || s === 'EXPIRED') return 'danger'
  return 'warn'
})

function mapStatus(s: string) {
  const map: Record<string, string> = {
    DRAFT: '待完善',
    SUBMITTED: '审核中',
    APPROVED: '已通过',
    REJECTED: '未通过',
    EXPIRED: '已到期'
  }
  return map[s] || '未开始'
}

onShow(() => {
  load()
})

async function load() {
  try {
    view.value = await driverApi.qualification()
    const draft = view.value.draft || {}
    form.realName = draft.realName || ''
    form.idCardNo = draft.idCardNo || ''
    form.licenseClass = draft.licenseClass || view.value.licenseClass || ''
    form.licenseValidTo = draft.licenseValidTo || view.value.licenseValidTo || ''
    form.licenseLongTerm = !!draft.licenseLongTerm
  } catch (e) {
    // 已提示
  }
}

function onLicenseChange(e: any) {
  form.licenseClass = licenseClasses[Number(e.detail.value)]
}

function onDateChange(e: any) {
  form.licenseValidTo = e.detail.value
}

function pickPhoto(kind: 'idCard' | 'license') {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const path = res.tempFilePaths[0]
      try {
        const file: any = await commonApi.upload(path, 'DRIVER_QUALIFICATION')
        fileIds[kind] = String(file.fileId)
        const link: any = await commonApi.fileAccess(file.fileId)
        photoUrls[kind] = `${API_BASE}${link.url}`
        uni.showToast({ title: '已上传', icon: 'success' })
      } catch (e) {
        // 已提示
      }
    }
  })
}

function evidenceJson(): string | null {
  const ids = [fileIds.idCard, fileIds.license].filter((x) => !!x)
  return ids.length ? JSON.stringify(ids) : null
}

async function save() {
  if (!form.realName || !form.idCardNo || !form.licenseClass) {
    uni.showToast({ title: '请填写姓名、身份证号与准驾车型', icon: 'none' })
    return
  }
  try {
    await driverApi.updateQualification({
      realName: form.realName,
      idCardNo: form.idCardNo,
      licenseClass: form.licenseClass,
      licenseValidTo: form.licenseLongTerm ? null : form.licenseValidTo || null,
      licenseLongTerm: form.licenseLongTerm,
      evidenceFileIds: evidenceJson()
    })
    uni.showToast({ title: '已保存', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}

async function submit() {
  uni.showModal({
    title: '提交审核',
    content: '提交后资料将进入审核，审核期间不可修改，是否继续？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await driverApi.submitQualification()
        uni.showToast({ title: '已提交审核', icon: 'success' })
        load()
      } catch (e) {
        // 已提示
      }
    }
  })
}

async function startFace() {
  try {
    faceInfo.value = await driverApi.createFaceSession()
    uni.showToast({ title: '已创建核验会话', icon: 'none' })
  } catch (e) {
    // 已提示
  }
}

async function mockFace() {
  if (!faceInfo.value) {
    uni.showToast({ title: '请先发起核验', icon: 'none' })
    return
  }
  try {
    await driverApi.mockFacePass(faceInfo.value.verificationSessionId)
    uni.showToast({ title: '核验已完成', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.picker {
  display: flex;
  align-items: center;
}

.photos {
  display: flex;
  gap: 20rpx;
  margin-top: 18rpx;
}

.photo {
  width: 200rpx;
  height: 140rpx;
  border-radius: 14rpx;
  overflow: hidden;
  border: 1rpx dashed var(--line);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}

.photo-img {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.photo-empty {
  color: var(--muted);
  font-size: 24rpx;
}
</style>
