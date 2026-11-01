<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { listVehicles, createVehicle, setDefaultVehicle, removeVehicle, type Vehicle } from '../../features/vehicle/api';

// A01 本人车辆（O04/O05）：新增/常用/软删；同车牌冲突提示；历史快照不追改。
const vehicles = ref<Vehicle[]>([]);
const message = ref('');
const showForm = ref(false);
const plate = ref('');
const typeCode = ref('SMALL_CAR');

onShow(load);

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

async function load() {
  try {
    vehicles.value = await listVehicles();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载车辆失败';
  }
}

async function add() {
  try {
    await createVehicle({ plateNo: plate.value, vehicleTypeCode: typeCode.value }, uuid());
    showForm.value = false;
    plate.value = '';
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '保存失败（车牌可能已存在）';
  }
}

async function setDefault(v: Vehicle) {
  try {
    await setDefaultVehicle(v.vehicleId, v.version, '设为常用', uuid());
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '设置失败';
  }
}

async function remove(v: Vehicle) {
  try {
    await removeVehicle(v.vehicleId, v.version, '删除车辆', uuid());
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '删除失败';
  }
}
</script>

<template>
  <view class="screen">
    <text class="title">我的车辆</text>
    <view v-for="v in vehicles" :key="v.vehicleId" class="card">
      <text class="plate">{{ v.plateNo }}</text>
      <text class="sub">{{ v.vehicleTypeLabel || v.vehicleTypeCode }} {{ v.brandModel || '' }}</text>
      <text v-if="v.isDefault" class="tag">常用</text>
      <view class="ops">
        <text v-if="!v.isDefault" class="op" @click="setDefault(v)">设常用</text>
        <text class="op del" @click="remove(v)">删除</text>
      </view>
    </view>

    <button class="plain" @click="showForm = !showForm">新增车辆</button>
    <view v-if="showForm" class="card">
      <input class="input" v-model="plate" placeholder="车牌号" />
      <input class="input" v-model="typeCode" placeholder="车型代码" />
      <button class="primary" @click="add">保存</button>
    </view>
    <text v-if="message" class="warn">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 8px; }
.plate { font-size: 18px; font-weight: 600; }
.sub { font-size: 14px; color: #666; }
.tag { color: #005fcb; font-size: 13px; }
.ops { display: flex; gap: 16px; }
.op { color: #005fcb; font-size: 14px; }
.del { color: #c0392b; }
.input { border: 1px solid #ddd; border-radius: 8px; padding: 8px; font-size: 15px; }
.warn { font-size: 15px; color: #c0392b; }
.primary { background: #005fcb; color: #fff; }
.plain { background: #eef2f7; color: #005fcb; }
</style>