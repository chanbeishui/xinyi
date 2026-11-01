<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { listAddresses, createAddress, setDefaultAddress, removeAddress, type Address } from '../../features/address/api';

// A02 地址簿：地图失败可手填地址+坐标；设常用/删除；临时地址不强制先保存。
const addresses = ref<Address[]>([]);
const message = ref('');
const showForm = ref(false);
const label = ref('');
const text = ref('');
const lng = ref('120.0');
const lat = ref('30.0');

onShow(load);

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

async function load() {
  try {
    addresses.value = await listAddresses();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '加载地址失败';
  }
}

async function add() {
  try {
    await createAddress({ label: label.value || undefined, address: text.value,
      location: { lng: Number(lng.value), lat: Number(lat.value), coordinateSystem: 'GCJ02' } }, uuid());
    showForm.value = false;
    text.value = '';
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '保存失败';
  }
}

async function setDefault(a: Address) {
  try {
    await setDefaultAddress(a.addressId, a.version, '设为常用', uuid());
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '设置失败';
  }
}

async function remove(a: Address) {
  try {
    await removeAddress(a.addressId, a.version, '删除地址', uuid());
    await load();
  } catch (err) {
    message.value = err instanceof Error ? err.message : '删除失败';
  }
}
</script>

<template>
  <view class="screen">
    <text class="title">地址簿</text>
    <view v-for="a in addresses" :key="a.addressId" class="card">
      <text class="addr">{{ a.label ? a.label + ' · ' : '' }}{{ a.address }}</text>
      <text v-if="a.isDefault" class="tag">常用</text>
      <view class="ops">
        <text v-if="!a.isDefault" class="op" @click="setDefault(a)">设常用</text>
        <text class="op del" @click="remove(a)">删除</text>
      </view>
    </view>

    <button class="plain" @click="showForm = !showForm">新增地址</button>
    <view v-if="showForm" class="card">
      <input class="input" v-model="label" placeholder="标签（家/公司，选填）" />
      <input class="input" v-model="text" placeholder="详细地址（地图失败可手填）" />
      <input class="input" v-model="lng" placeholder="经度" type="digit" />
      <input class="input" v-model="lat" placeholder="纬度" type="digit" />
      <button class="primary" @click="add">保存</button>
    </view>
    <text v-if="message" class="warn">{{ message }}</text>
  </view>
</template>

<style scoped>
.screen { padding: 32px 22px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 8px; }
.addr { font-size: 16px; font-weight: 600; }
.tag { color: #005fcb; font-size: 13px; }
.ops { display: flex; gap: 16px; }
.op { color: #005fcb; font-size: 14px; }
.del { color: #c0392b; }
.input { border: 1px solid #ddd; border-radius: 8px; padding: 8px; font-size: 15px; }
.warn { font-size: 15px; color: #c0392b; }
.primary { background: #005fcb; color: #fff; }
.plain { background: #eef2f7; color: #005fcb; }
</style>