<template>
  <view class="page">
    <view v-if="lastError" class="error-bar" @tap="lastError = ''">{{ lastError }}（点击关闭）</view>
    <view class="card">
      <view class="row-between">
        <view class="card-title" style="margin: 0">代驾资格</view>
        <view class="status" :class="statusClass">{{ statusLabel }}</view>
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
          <view class="input picker"><text class="picker__text">{{ form.licenseClass || '请选择准驾车型' }}</text><view class="chev"></view></view>
        </picker>
      </view>
      <view class="field">
        <view class="field-label">驾驶证有效期至</view>
        <picker mode="date" :value="form.licenseValidTo" @change="onDateChange" :disabled="!view.canEdit">
          <view class="input picker"><text class="picker__text">{{ form.licenseValidTo || '请选择有效期' }}</text><view class="chev"></view></view>
        </picker>
      </view>
      <view class="switch-row">
        <text class="switch-row__label">长期有效</text>
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
      <view class="btn-row">
        <view class="btn ghost small btn-row__item" hover-class="btn-hover" @tap="startFace">发起核验</view>
        <view class="btn small btn-row__item" hover-class="btn-hover" @tap="mockFace">模拟完成</view>
      </view>
      <view v-if="faceInfo" class="muted" style="margin-top: 12rpx">
        核验会话 {{ faceInfo.verificationSessionId }} · {{ faceInfo.hint }}
      </view>
    </view>

    <view class="action-bar">
      <view v-if="view.canEdit" class="btn gray" hover-class="btn-hover" @tap="save">保存草稿</view>
      <view v-if="view.canSubmit" class="btn" hover-class="btn-hover" @tap="submit">提交审核</view>
      <view v-if="!view.canEdit && !view.canSubmit" class="btn gray" hover-class="btn-hover" @tap="load">刷新状态</view>
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
const lastError = ref('')
const licenseClasses = ['C1', 'C2', 'B1', 'B2', 'A1', 'A2']
const fileIds = reactive<{ idCard: string; license: string }>({ idCard: '', license: '' })
const photoUrls = reactive<{ idCard: string; license: string }>({ idCard: '', license: '' })

/**
 * 展示用的图片地址。
 *
 * 后端可能直接给 OSS 绝对地址，也可能给站内相对路径，统一在这里处理：
 * 已是绝对地址就不再拼 API_BASE（曾出现 prod-apihttp://... 这种双重前缀），
 * 并把 http 升级成 https——小程序页面本身是 https，http 图片会被 Mixed Content 拦掉。
 */
function toDisplayUrl(url: string): string {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url.replace(/^http:\/\//i, 'https://')
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

function errorText(e: any): string {
  if (!e) return '操作失败，请稍后重试'
  if (typeof e === 'string') return e
  return e.message || e.errMsg || e.msg || '操作失败，请稍后重试'
}

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
  if (s === 'APPROVED') return 'status--ok'
  if (s === 'REJECTED' || s === 'EXPIRED') return 'status--danger'
  return 'status--warn'
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
    lastError.value = errorText(e)
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
        photoUrls[kind] = toDisplayUrl(link.url)
        uni.showToast({ title: '已上传', icon: 'success' })
      } catch (e) {
        lastError.value = errorText(e)
      }
    }
  })
}

function evidenceJson(): string | null {
  const ids = [fileIds.idCard, fileIds.license].filter((x) => !!x)
  return ids.length ? JSON.stringify(ids) : null
}

/**
 * 落草稿，返回是否成功。
 *
 * 提交审核前必须先调一次：后端校验的是草稿里的 evidence_file_ids，
 * 只上传照片不保存就提交，会报「请上传驾驶证与身份证照片后再提交」。
 */
async function persist(showToast = true): Promise<boolean> {
  if (!form.realName || !form.idCardNo || !form.licenseClass) {
    uni.showToast({ title: '请填写姓名、身份证号与准驾车型', icon: 'none' })
    return false
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
    if (showToast) uni.showToast({ title: '已保存', icon: 'success' })
    return true
  } catch (e) {
    lastError.value = errorText(e)
    return false
  }
}

async function save() {
  if (await persist()) load()
}

async function submit() {
  if (!(await persist(false))) return
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
        lastError.value = errorText(e)
      }
    }
  })
}

async function startFace() {
  try {
    faceInfo.value = await driverApi.createFaceSession()
    uni.showToast({ title: '已创建核验会话', icon: 'none' })
  } catch (e) {
    lastError.value = errorText(e)
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
    lastError.value = errorText(e)
  }
}
</script>

<style scoped>
.picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  line-height: 78rpx;
}

.picker__text {
  font-size: 28rpx;
  color: var(--ink);
}

.btn-row {
  display: flex;
  margin-top: 18rpx;
}

.btn-row__item {
  flex: 1;
}

.btn-row__item + .btn-row__item {
  margin-left: 16rpx;
}

.photos {
  display: flex;
  margin-top: 18rpx;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0 4rpx;
}

.switch-row__label {
  font-size: 26rpx;
  color: var(--ink-2);
}

.photo + .photo {
  margin-left: 20rpx;
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

/* 失败原因常驻在页面顶部：toast 一闪而过，400 的业务提示容易错过 */
.error-bar {
  margin-bottom: 16rpx;
  padding: 18rpx 22rpx;
  border-radius: 12rpx;
  background: #fdecea;
  border: 1rpx solid #f5c2c0;
  color: #a4262c;
  font-size: 24rpx;
  line-height: 1.5;
}
</style>
