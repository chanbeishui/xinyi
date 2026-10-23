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
    const raw: any = await commonApi.balanceSheet('vehicle_type')
    // 后端 /api/common/dict/{type} 返回 { dictType, total, items: [{label,value}] }，
    // 早期这里把整个对象当数组直接 .map，抛 TypeError 后掉进 catch，
    // 用了字典里并不存在的 SMALL_CAR，提交就被后端判成「车型不在可选范围内」。
    // 这里同时兼容 items / data / rows 三种包裹形式与驼峰字段。
    const list: any[] = Array.isArray(raw)
      ? raw
      : (raw && (raw.items || raw.data || raw.rows)) || []
    types.value = list
      .map((d: any) => ({
        label: d.label ?? d.dictLabel ?? '',
        value: d.value ?? d.dictValue ?? ''
      }))
      .filter((t) => t.value)
    if (!form.vehicleTypeCode && types.value.length) {
      form.vehicleTypeCode = types.value[0].value
    }
  } catch (e) {
    // 兜底值必须与 sys_dict_data(vehicle_type) 保持一致，
    // 否则能选中但提交必被拒，报错还会误导成「车型不在可选范围内」。
    types.value = [
      { label: '轿车', value: 'sedan' },
      { label: 'SUV', value: 'suv' },
      { label: '商务车', value: 'mpv' },
      { label: '皮卡', value: 'pickup' },
      { label: '面包车', value: 'van' }
    ]
  }
  syncTypeIndex()
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
