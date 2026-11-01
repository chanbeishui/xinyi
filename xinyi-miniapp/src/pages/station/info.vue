<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getOwnStation, updateOwnStation, type Station } from '../../features/station/api';

// C02 · S16 站点资料与资质：获授权者（切换到本站点工作身份的员工）查看/编辑；电话脱敏；版本 CAS。
const station = ref<Station | null>(null);
const message = ref('');
const editing = ref(false);
const name = ref('');
const address = ref('');
const businessHours = ref('');
const supportedCodes = ref('');
const radius = ref(0);

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

async function load() {
  try {
    station.value = await getOwnStation();
    message.value = '';
  } catch (err) {
    station.value = null;
    message.value = err instanceof Error ? err.message : '需切换到站点工作身份后查看';
  }
}

function startEdit() {
  const s = station.value;
  if (!s) return;
  name.value = s.name;
  address.value = s.address;
  businessHours.value = s.businessHours || '';
  supportedCodes.value = (s.supportedVehicleTypeCodes || []).join(',');
  radius.value = s.serviceRadiusMeter || 0;
  editing.value = true;
}

async function save() {
  const s = station.value;
  if (!s) return;
  try {
    station.value = await updateOwnStation({
      name: name.value.trim(),
      address: address.value.trim(),
      location: { lng: s.location.lng, lat: s.location.lat, coordinateSystem: 'GCJ02' },
      supportedVehicleTypeCodes: supportedCodes.value.split(',').map(x => x.trim()).filter(Boolean),
      businessHours: businessHours.value || undefined,
      serviceRadiusMeter: Number(radius.value),
      expectedVersion: s.version
    }, uuid());
    editing.value = false;
    message.value = '';
  } catch (err) {
    message.value = err instanceof Error ? err.message : '保存失败，请刷新后重试';
    await load();
  }
}

onShow(load);
</script>

<template>
  <view class="screen">
    <text class="title">站点资料与资质</text>
    <navigator url="/pages/capacity/list">营业时段与预约容量</navigator>
    <view v-if="station" class="card">
      <text class="row">名称：{{ station.name }}</text>
      <text class="row">地址：{{ station.address }}</text>
      <text class="row">电话：{{ station.phoneMask || '—' }}</text>
      <text class="row">营业时间：{{ station.businessHours || '—' }}</text>
      <text class="row">支持车型：{{ (station.supportedVehicleTypeCodes || []).join('、') }}</text>
      <text class="row">服务半径：{{ station.serviceRadiusMeter || 0 }} m</text>
      <text class="row">状态：{{ station.enabled ? '启用' : '停用' }}</text>
    </view>

    <view v-if="station && !editing" class="ops">
      <button class="primary" @click="startEdit">编辑资料</button>
    </view>

    <view v-if="editing" class="card">
      <input class="input" v-model="name" placeholder="名称" />
      <input class="input" v-model="address" placeholder="地址" />
      <input class="input" v-model="businessHours" placeholder="营业时间" />
      <input class="input" v-model="supportedCodes" placeholder="支持车型（逗号分隔）" />
      <input class="input" v-model="radius" type="number" placeholder="服务半径(m)" />
      <button class="primary" @click="save">保存</button>
      <button class="plain" @click="editing = false">取消</button>
    </view>

    <text v-if="message" class="warn">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 8px; }
.row { font-size: 15px; color: #333; }
.ops { display: flex; gap: 16px; }
.input { border: 1px solid #ddd; border-radius: 8px; padding: 8px; font-size: 15px; }
.warn { font-size: 15px; color: #c0392b; }
.primary { background: #005fcb; color: #fff; }
.plain { background: #eef2f7; color: #005fcb; }
</style>