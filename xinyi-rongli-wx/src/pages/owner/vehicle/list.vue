<template>
  <view class="page">
    <view class="tabs">
      <view class="tab" :class="{ on: tab === 'vehicle' }" @tap="tab = 'vehicle'">车辆</view>
      <view class="tab" :class="{ on: tab === 'address' }" @tap="tab = 'address'">接车地址</view>
    </view>

    <!-- 车辆 -->
    <view v-if="tab === 'vehicle'">
      <view v-for="v in vehicles" :key="v.vehicleId" class="card" @tap="choose(v)">
        <view class="row-between">
          <view>
            <view class="plate">{{ v.plateNo }}</view>
            <view class="muted" style="margin-top: 6rpx">
              {{ v.vehicleTypeName }} · {{ v.brandModel || '车型未填写' }}
            </view>
          </view>
          <view class="tag" :class="v.isDefault ? 'ok' : 'gray'">{{ v.isDefault ? '默认' : '普通' }}</view>
        </view>
        <view class="divider"></view>
        <view class="row" style="gap: 16rpx">
          <view class="btn ghost small" @tap.stop="edit(v)">编辑</view>
          <view v-if="!v.isDefault" class="btn gray small" @tap.stop="makeDefault(v)">设为默认</view>
          <view class="btn gray small" @tap.stop="remove(v)">删除</view>
        </view>
      </view>
      <view v-if="!vehicles.length" class="empty">还没有车辆，先添加一辆吧</view>
      <view class="btn" @tap="addVehicle">添加车辆</view>
    </view>

    <!-- 地址 -->
    <view v-if="tab === 'address'">
      <view v-for="a in addresses" :key="a.addressId" class="card">
        <view class="row-between">
          <view style="flex: 1">
            <view class="value" style="text-align: left">{{ a.label || '接车地址' }}</view>
            <view class="muted" style="margin-top: 6rpx">{{ a.address }}</view>
          </view>
          <view class="tag" :class="a.isDefault ? 'ok' : 'gray'">{{ a.isDefault ? '默认' : '普通' }}</view>
        </view>
        <view class="divider"></view>
        <view class="row" style="gap: 16rpx">
          <view class="btn ghost small" @tap="editAddress(a)">编辑</view>
          <view v-if="!a.isDefault" class="btn gray small" @tap="defaultAddress(a)">设为默认</view>
          <view class="btn gray small" @tap="removeAddress(a)">删除</view>
        </view>
      </view>
      <view v-if="!addresses.length" class="empty">还没有接车地址</view>
      <view class="btn" @tap="openAddressForm(null)">新增接车地址</view>
    </view>

    <!-- 地址表单 -->
    <view v-if="addressForm.show" class="mask" @tap="closeAddressForm">
      <view class="sheet" @tap.stop>
        <view class="card-title">{{ addressForm.addressId ? '编辑接车地址' : '新增接车地址' }}</view>
        <view class="field">
          <view class="field-label">地址标签</view>
          <input v-model="addressForm.label" class="input" placeholder="例如 家 / 公司" />
        </view>
        <view class="field">
          <view class="field-label">详细地址</view>
          <textarea v-model="addressForm.address" class="textarea" placeholder="街道、门牌号、小区楼栋" />
        </view>
        <view class="field">
          <view class="field-label">坐标（用于匹配检测点与计费距离）</view>
          <view class="row" style="gap: 12rpx">
            <input v-model="addressForm.lng" class="input" type="digit" placeholder="经度" style="flex: 1" />
            <input v-model="addressForm.lat" class="input" type="digit" placeholder="纬度" style="flex: 1" />
          </view>
          <view class="btn ghost small" style="margin-top: 12rpx" @tap="chooseLocation">地图选点</view>
        </view>
        <view class="row" style="margin-top: 12rpx">
          <text class="label">设为默认地址</text>
          <switch :checked="addressForm.isDefault" @change="(e:any) => (addressForm.isDefault = e.detail.value)" />
        </view>
        <view class="btn" style="margin-top: 24rpx" @tap="saveAddress">保存</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ownerApi } from '@/api'

const tab = ref<'vehicle' | 'address'>('vehicle')
const selectMode = ref(false)
const vehicles = ref<any[]>([])
const addresses = ref<any[]>([])

const addressForm = reactive<any>({
  show: false,
  addressId: '',
  label: '',
  address: '',
  lng: '',
  lat: '',
  isDefault: false
})

onLoad((options) => {
  if (options && options.tab === 'address') tab.value = 'address'
  if (options && options.select === '1') selectMode.value = true
})

onShow(() => {
  load()
})

async function load() {
  try {
    const [v, a] = await Promise.all([ownerApi.vehicles(), ownerApi.addresses()])
    vehicles.value = (v as any) || []
    addresses.value = (a as any) || []
  } catch (e) {
    // 已提示
  }
}

function choose(v: any) {
  if (!selectMode.value) return
  uni.$emit('vehicle:selected', v.vehicleId)
  uni.navigateBack()
}

function addVehicle() {
  uni.navigateTo({ url: '/pages/owner/vehicle/edit' })
}

function edit(v: any) {
  uni.navigateTo({ url: `/pages/owner/vehicle/edit?vehicleId=${v.vehicleId}` })
}

async function makeDefault(v: any) {
  await ownerApi.defaultVehicle(v.vehicleId)
  load()
}

function remove(v: any) {
  uni.showModal({
    title: '删除车辆',
    content: `确认删除车辆 ${v.plateNo}？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await ownerApi.deleteVehicle(v.vehicleId)
        load()
      } catch (e) {
        // 已提示
      }
    }
  })
}

function openAddressForm(a: any) {
  if (a) {
    addressForm.addressId = a.addressId
    addressForm.label = a.label || ''
    addressForm.address = a.address
    addressForm.lng = String(a.lng)
    addressForm.lat = String(a.lat)
    addressForm.isDefault = !!a.isDefault
  } else {
    addressForm.addressId = ''
    addressForm.label = ''
    addressForm.address = ''
    addressForm.lng = ''
    addressForm.lat = ''
    addressForm.isDefault = addresses.value.length === 0
  }
  addressForm.show = true
}

function editAddress(a: any) {
  openAddressForm(a)
}

function closeAddressForm() {
  addressForm.show = false
}

function chooseLocation() {
  uni.chooseLocation({
    success: (res) => {
      addressForm.address = res.address || res.name
      addressForm.lng = String(res.longitude)
      addressForm.lat = String(res.latitude)
    },
    fail: () => {
      uni.showToast({ title: '未获取到地图选点，请手动填写坐标', icon: 'none' })
    }
  })
}

async function saveAddress() {
  if (!addressForm.address || !addressForm.lng || !addressForm.lat) {
    uni.showToast({ title: '请填写地址与坐标', icon: 'none' })
    return
  }
  const payload = {
    label: addressForm.label,
    address: addressForm.address,
    lng: Number(addressForm.lng),
    lat: Number(addressForm.lat),
    isDefault: addressForm.isDefault
  }
  try {
    if (addressForm.addressId) {
      await ownerApi.updateAddress(addressForm.addressId, payload)
    } else {
      await ownerApi.createAddress(payload)
    }
    addressForm.show = false
    load()
  } catch (e) {
    // 已提示
  }
}

async function defaultAddress(a: any) {
  await ownerApi.defaultAddress(a.addressId)
  load()
}

function removeAddress(a: any) {
  uni.showModal({
    title: '删除地址',
    content: '确认删除该接车地址？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await ownerApi.deleteAddress(a.addressId)
        load()
      } catch (e) {
        // 已提示
      }
    }
  })
}
</script>

<style scoped>
.tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 20rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 27rpx;
  color: var(--sub);
  border-radius: 12rpx;
}

.tab.on {
  background: var(--brand-soft);
  color: var(--brand);
  font-weight: 600;
}

.plate {
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
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
  max-height: 88vh;
  overflow-y: auto;
}
</style>
