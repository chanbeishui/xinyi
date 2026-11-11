<template>
  <view class="page">
    <view class="seg">
      <view class="seg__item" :class="{ on: tab === 'vehicle' }" hover-class="card-hover" @tap="tab = 'vehicle'">
        车辆
      </view>
      <view class="seg__item" :class="{ on: tab === 'address' }" hover-class="card-hover" @tap="tab = 'address'">
        接车地址
      </view>
    </view>

    <!-- 车辆 -->
    <view v-if="tab === 'vehicle'">
      <view v-for="v in vehicles" :key="v.vehicleId" class="slip" @tap="choose(v)">
        <view class="slip__head">
          <view class="slip__main">
            <view class="slip__plate">{{ v.plateNo }}</view>
            <view class="slip__sub ellipsis">
              {{ v.vehicleTypeName }} · {{ v.brandModel || '车型未填写' }}
            </view>
          </view>
          <view class="status" :class="v.isDefault ? 'status--ok' : 'status--gray'">
            {{ v.isDefault ? '默认' : '普通' }}
          </view>
        </view>

        <view class="ops">
          <text class="op" @tap.stop="edit(v)">编辑</text>
          <text v-if="!v.isDefault" class="op" @tap.stop="makeDefault(v)">设为默认</text>
          <text class="op op--danger" @tap.stop="remove(v)">删除</text>
        </view>
      </view>

      <view v-if="!vehicles.length" class="empty">
        <view class="empty__title">还没有车辆</view>
        <view class="empty__hint">添加车牌与车型后即可预约年检</view>
      </view>
      <view class="btn block" hover-class="btn-hover" @tap="addVehicle">添加车辆</view>
    </view>

    <!-- 地址 -->
    <view v-if="tab === 'address'">
      <view v-for="a in addresses" :key="a.addressId" class="slip">
        <view class="slip__head">
          <view class="slip__main">
            <view class="slip__plate slip__plate--label">{{ a.label || '接车地址' }}</view>
            <view class="slip__sub">{{ a.address }}</view>
          </view>
          <view class="status" :class="a.isDefault ? 'status--ok' : 'status--gray'">
            {{ a.isDefault ? '默认' : '普通' }}
          </view>
        </view>

        <view class="ops">
          <text class="op" @tap="editAddress(a)">编辑</text>
          <text v-if="!a.isDefault" class="op" @tap="defaultAddress(a)">设为默认</text>
          <text class="op op--danger" @tap="removeAddress(a)">删除</text>
        </view>
      </view>

      <view v-if="!addresses.length" class="empty">
        <view class="empty__title">还没有接车地址</view>
        <view class="empty__hint">地址用于匹配检测点与计算上门距离</view>
      </view>
      <view class="btn block" hover-class="btn-hover" @tap="openAddressForm(null)">新增接车地址</view>
    </view>

    <!-- 地址表单 -->
    <view v-if="addressForm.show" class="mask" @tap="closeAddressForm">
      <view class="sheet" @tap.stop>
        <view class="sheet__head">
          <text class="sheet__title">{{ addressForm.addressId ? '编辑接车地址' : '新增接车地址' }}</text>
          <text class="sheet__close" @tap="closeAddressForm">关闭</text>
        </view>

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
          <view class="coord-row">
            <input v-model="addressForm.lng" class="input coord-row__item" type="digit" placeholder="经度" />
            <input v-model="addressForm.lat" class="input coord-row__item" type="digit" placeholder="纬度" />
          </view>
          <view class="btn ghost small btn-gap" hover-class="btn-hover" @tap="chooseLocation">地图选点</view>
        </view>

        <view class="switch-row">
          <text class="switch-row__label">设为默认地址</text>
          <switch :checked="addressForm.isDefault" @change="(e:any) => (addressForm.isDefault = e.detail.value)" />
        </view>

        <view class="btn block btn-gap" hover-class="btn-hover" @tap="saveAddress">保存</view>
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
/* 分段控件：两段白底胶囊，选中填墨，和底部导航的短横语言一致 */
.seg {
  display: flex;
  padding: 6rpx;
  margin-bottom: 20rpx;
  background: #fff;
  border: 1rpx solid var(--line);
  border-radius: 14rpx;
}

.seg__item {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10rpx;
  font-size: 27rpx;
  color: var(--ink-2);
}

.seg__item.on {
  background: var(--ink);
  color: #fff;
  font-weight: 600;
}

.slip__main {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.slip__head .slip__plate--label {
  font-size: 34rpx;
  letter-spacing: 0.5rpx;
}

/* 卡片操作行：文字操作比三个方按钮更安静，也不占卡片宽度 */
.ops {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 14rpx 24rpx;
  border-top: 1rpx solid var(--line-2);
  background: #fafbfc;
}

.op {
  padding: 6rpx 0 6rpx 32rpx;
  font-size: 25rpx;
  color: var(--ink-2);
}

.op--danger {
  color: var(--danger);
}

.coord-row {
  display: flex;
}

.coord-row__item {
  flex: 1;
}

.coord-row__item + .coord-row__item {
  margin-left: 14rpx;
}

.btn-gap {
  margin-top: 18rpx;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--line-2);
}

.switch-row__label {
  font-size: 26rpx;
  color: var(--ink-2);
}
</style>
