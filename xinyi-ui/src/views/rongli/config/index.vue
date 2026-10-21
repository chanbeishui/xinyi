<template>
  <div class="app-container">
    <el-alert :title="data.hint || ''" type="info" :closable="false" show-icon class="mb8" />

    <el-card shadow="never" class="mb8">
      <template #header>业务参数</template>
      <el-form label-width="240px" style="max-width: 720px">
        <el-form-item v-for="item in editable" :key="item.key" :label="item.label">
          <el-input v-model="form[item.key]" style="width: 220px" />
        </el-form-item>
        <el-form-item label="支付通道">
          <el-tag type="warning">{{ data.effective?.['rongli.payment.channel'] || '-' }}</el-tag>
          <span class="muted">（需在配置文件切换，管理端不可修改）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存参数</el-button>
          <el-button icon="Refresh" @click="load">重新读取</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>当前生效值</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="(v, k) in data.effective || {}" :key="k" :label="k">{{ v }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup name="RongliConfig" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getBusinessConfig, saveBusinessConfig } from '@/api/rongli'

const data = ref<any>({})
const form = reactive<any>({})

const editable = [
  { key: 'rongli.dispatch.leadMinutes', label: '提前派单(分钟)' },
  { key: 'rongli.dispatch.radiusMeter', label: '派单半径(米)' },
  { key: 'rongli.dispatch.totalMinutes', label: '派单总时长(分钟)' },
  { key: 'rongli.invite.expireSeconds', label: '单次邀约有效(秒)' },
  { key: 'rongli.pay.expireMinutes', label: '支付时限(分钟)' },
  { key: 'rongli.confirm.autoHours', label: '自动确认完成(小时)' },
  { key: 'rongli.driver.locationFreshSeconds', label: '定位有效期(秒)' },
  { key: 'rongli.service.termsVersion', label: '服务条款版本' }
]

onMounted(load)

async function load() {
  data.value = await getBusinessConfig()
  const effective = data.value.effective || {}
  for (const item of editable) {
    form[item.key] = String(effective[item.key] ?? '')
  }
}

async function save() {
  const payload: Record<string, string> = {}
  for (const item of editable) {
    const shortKey = item.key.replace('rongli.', '')
    payload[shortKey.split('.').pop() as string] = String(form[item.key])
  }
  await saveBusinessConfig(payload)
  ElMessage.success('已保存，仅影响新订单')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.muted { color: #909399; margin-left: 10px; font-size: 12px; }
</style>
