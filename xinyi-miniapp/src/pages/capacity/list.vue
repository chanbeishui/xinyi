<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { listCapacity, changeCapacity, type Capacity } from '../../features/capacity/api';
const rows = ref<Capacity[]>([]), total = ref(0), page = ref(1), message = ref('');
const editing = ref<Capacity | null>(null), target = ref(0), reason = ref(''), enabled = ref(true), saving = ref(false);
let pending: { intent: string; input: { total: number; expectedVersion: number; reason: string; enabled: boolean }; key: string } | null = null;
function toggle(event: Event) { enabled.value = (event as unknown as { detail: { value: boolean } }).detail.value; }
const time = (value: string) => new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false });
async function load() {
  try { const r = await listCapacity(undefined, undefined, page.value); rows.value = r.rows; total.value = r.total; message.value = ''; }
  catch (error) { rows.value = []; message.value = error instanceof Error ? error.message : '无容量管理权限'; }
}
function edit(row: Capacity) {
  editing.value = row; target.value = row.total; reason.value = ''; enabled.value = row.enabled; pending = null;
}
function uuid() { return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => { const r = Math.random() * 16 | 0; return (c === 'x' ? r : (r & 3) | 8).toString(16); }); }
async function save() {
  const row = editing.value;
  if (!row || saving.value) return;
  if (!reason.value.trim() || !Number.isInteger(Number(target.value)) || Number(target.value) < 0) { message.value = '请填写非负整数额度及变更原因'; return; }
  saving.value = true;
  try {
    const intent = JSON.stringify([row.slotId, Number(target.value), enabled.value, reason.value.trim()]);
    if (!pending || pending.intent !== intent) {
      const preview = (await listCapacity(row.slotId, Number(target.value))).rows[0];
      if (!preview.canApply) { message.value = '不能低于已有占用，或时段已开始'; return; }
      const confirm = await uni.showModal({ title: '容量调整影响', content: `预占${preview.held}，正式${preview.confirmed}，已用${preview.consumed}。总量${preview.total}改为${target.value}；${enabled.value ? '允许新预约' : '停约保留已有占用'}。` });
      if (!confirm.confirm) return;
      pending = { intent, key: uuid(), input: { total: Number(target.value), expectedVersion: preview.version, reason: reason.value.trim(), enabled: enabled.value } };
    }
    await changeCapacity(row.slotId, pending.input, pending.key);
    pending = null; editing.value = null; await load();
  } catch (error) { message.value = error instanceof Error ? error.message : '保存失败；可重试，版本冲突请取消后重新预览'; }
  finally { saving.value = false; }
}
onShow(load);
</script>
<template>
  <view class="screen">
    <text class="title">营业时段与预约容量</text>
    <text>接车和接收额度由本站点独立维护；上门时间不代表到站时间。</text>
    <view v-for="row in rows" :key="row.slotId" class="card">
      <text>{{ row.quotaType === 'PICKUP' ? '上门接车' : '站点接收' }} · {{ row.enabled ? '可预约' : '停约' }}</text>
      <text>{{ time(row.startAt) }} 至 {{ time(row.endAt) }}</text>
      <text>总量{{ row.total }} / 预占{{ row.held }} / 正式{{ row.confirmed }} / 已用{{ row.consumed }} / 可用{{ row.available }}</text>
      <button :disabled="Date.parse(row.startAt) <= Date.now()" @click="edit(row)">调整容量</button>
    </view>
    <view v-if="editing" class="card">
      <input v-model.number="target" type="number" placeholder="总额度" />
      <input v-model="reason" placeholder="变更原因" maxlength="500" />
      <view>允许新预约 <switch :checked="enabled" @change="toggle" /></view>
      <button :loading="saving" @click="save">预览影响并保存</button><button @click="editing = null; pending = null">取消</button>
    </view>
    <text v-if="message">{{ message }}</text>
    <view class="pages"><button :disabled="page <= 1" @click="page--; load()">上一页</button><text>{{ page }}</text><button :disabled="page * 20 >= total" @click="page++; load()">下一页</button></view>
  </view>
</template>
<style scoped>
.screen { padding: 24px 20px; display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 24px; font-weight: 600; }
.card { background: white; border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 10px; }
input { padding: 10px; border: 1px solid #ddd; border-radius: 8px; }
.pages { display: flex; align-items: center; gap: 12px; }
</style>
