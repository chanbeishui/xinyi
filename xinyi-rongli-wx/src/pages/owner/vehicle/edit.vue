<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <view class="field-label">车牌号</view>
        <input v-model="form.plateNo" class="input" placeholder="例如 粤B12345" maxlength="8" />
      </view>
      <view class="field">
        <view class="field-label">车型</view>
        <picker mode="selector" :range="typeLabels" :value="typeIndex" @change="onTypeChange">
          <view class="input picker">{{ typeLabels[typeIndex] || '请选择车型' }}</view>
        </picker>
      </view>
      <view class="field">
        <view class="field-label">品牌型号（选填）</view>
        <input v-model="form.brandModel" class="input" placeholder="例如 大众 朗逸" />
      </view>
      <view class="field">
        <view class="field-label">车架号后 4 位（选填）</view>
        <input v-model="form.vinLast4" class="input" placeholder="用于检测时核对车辆" maxlength="4" />
      </view>
      <view class="field">
        <view class="field-label">注册日期（选填）</view>
        <picker mode="date" :value="form.registeredOn" @change="onDateChange">
          <view class="input picker">{{ form.registeredOn || '请选择注册日期' }}</view>
        </picker>
      </view>
      <view class="field">
        <view class="field-label">使用性质</view>
        <picker mode="selector" :range="useLabels" :value="useIndex" @change="onUseChange">
          <view class="input picker">{{ useLabels[useIndex] }}</view>
        </picker>
      </view>
      <view class="row" style="margin-top: 12rpx">
        <text class="label">设为默认车辆</text>
        <switch :checked="form.isDefault" @change="(e:any) => (form.isDefault = e.detail.value)" />
      </view>
    </view>

    <view class="btn" @tap="save">保存</view>
    <view class="muted" style="margin-top: 16rpx; text-align: center">
      车型影响检测项目与计价方案，请按行驶证选择。
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { commonApi, ownerApi } from '@/api'

const form = reactive<any>({
  vehicleId: '',
  plateNo: '',
  vehicleTypeCode: '',
  brandModel: '',
  vinLast4: '',
  registeredOn: '',
  useType: 'NON_OPERATING',
  isDefault: false
})

const types = ref<Array<{ label: string; value: string }>>([])
const typeIndex = ref(0)
const typeLabels = computed(() => types.value.map((t) => t.label))

const USE = [
  { label: '非营运', value: 'NON_OPERATING' },
  { label: '营运', value: 'OPERATING' }
]
const useLabels = USE.map((u) => u.label)
const useIndex = computed(() => {
  const i = USE.findIndex((u) => u.value === form.useType)
  return i < 0 ? 0 : i
})

onLoad(async (options) => {
  await loadDict()
  if (options && options.vehicleId) {
    form.vehicleId = options.vehicleId
    await loadVehicle(options.vehicleId)
  }
})

async function loadDict() {
  try {
    const list: any = await commonApi.balanceSheet('vehicle_type')
    types.value = (list || []).map((d: any) => ({ label: d.dictLabel, value: d.dictValue }))
    if (!form.vehicleTypeCode && types.value.length) {
      form.vehicleTypeCode = types.value[0].value
    }
  } catch (e) {
    types.value = [
      { label: '小型汽车', value: 'SMALL_CAR' },
      { label: '大型汽车', value: 'LARGE_CAR' }
    ]
  }
}

async function loadVehicle(id: string) {
  const list: any = await ownerApi.vehicles()
  const target = (list || []).find((v: any) => String(v.vehicleId) === String(id))
  if (!target) return
  Object.assign(form, {
    plateNo: target.plateNo,
    vehicleTypeCode: target.vehicleTypeCode,
    brandModel: target.brandModel || '',
    vinLast4: target.vinLast4 || '',
    registeredOn: target.registeredOn || '',
    useType: target.useType || 'NON_OPERATING',
    isDefault: !!target.isDefault
  })
  syncTypeIndex()
}

function syncTypeIndex() {
  const i = types.value.findIndex((t) => t.value === form.vehicleTypeCode)
  typeIndex.value = i < 0 ? 0 : i
}

function onTypeChange(e: any) {
  typeIndex.value = Number(e.detail.value)
  form.vehicleTypeCode = types.value[typeIndex.value].value
}

function onDateChange(e: any) {
  form.registeredOn = e.detail.value
}

function onUseChange(e: any) {
  form.useType = USE[Number(e.detail.value)].value
}

async function save() {
  if (!form.plateNo) {
    uni.showToast({ title: '请填写车牌号', icon: 'none' })
    return
  }
  if (!form.vehicleTypeCode) {
    uni.showToast({ title: '请选择车型', icon: 'none' })
    return
  }
  const payload = {
    plateNo: form.plateNo.toUpperCase().trim(),
    vehicleTypeCode: form.vehicleTypeCode,
    brandModel: form.brandModel,
    vinLast4: form.vinLast4,
    registeredOn: form.registeredOn || null,
    useType: form.useType,
    isDefault: form.isDefault
  }
  try {
    if (form.vehicleId) {
      await ownerApi.updateVehicle(form.vehicleId, payload)
    } else {
      await ownerApi.createVehicle(payload)
    }
    uni.showToast({ title: '已保存', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    // 已提示
  }
}
</script>

<style scoped>
.picker {
  display: flex;
  align-items: center;
  color: var(--text);
}
</style>
