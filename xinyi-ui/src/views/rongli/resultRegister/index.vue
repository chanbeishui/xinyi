<template>
  <RongliPage title="登记最终检测结果" subtitle="与检测工作小程序同源；双端并发只保留一份有效提交。">
    <el-page-header class="mb12" @back="router.push('/rongli/inspect/inspectionTodo')">
      <template #content>
        <span>检测结果登记</span>
        <el-tag v-if="order.orderNo" class="ml8" type="info">{{ order.orderNo }}</el-tag>
      </template>
    </el-page-header>

    <el-empty v-if="!queryOrderNo" description="请从「检测结果待办」进入并选择订单">
      <el-button type="primary" @click="router.push('/rongli/inspect/inspectionTodo')">前往待办</el-button>
    </el-empty>

    <template v-else>
      <el-card shadow="never" class="mb12" v-loading="loading">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="车牌">{{ order.plateNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="车型">{{ order.vehicleTypeName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="检测点">{{ order.stationName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="履约状态">{{ order.fulfillmentStatusLabel || '—' }}</el-descriptions-item>
          <el-descriptions-item label="行驶证">
            <el-tag :type="order.licenseStatus === 'VERIFIED' ? 'success' : 'warning'">
              {{ order.licenseStatus === 'VERIFIED' ? '已核验' : '待核验' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="司机">{{ order.driverName || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-alert
        v-if="order.licenseStatus !== 'VERIFIED'"
        class="mb12"
        type="warning"
        show-icon
        :closable="false"
        title="行驶证尚未核验，按规则不能登记检测结果。请先完成核验。"
      >
        <el-button link type="primary" @click="router.push({ path: '/rongli/inspect/license', query: { orderNo: queryOrderNo } })">
          前往行驶证核验
        </el-button>
      </el-alert>

      <el-row :gutter="12">
        <el-col :span="14">
          <el-card shadow="never">
            <template #header>
              <span>{{ isCorrection ? '发起更正' : '登记检测结果' }}</span>
              <el-tag v-if="current" class="ml8" type="info">当前版本 v{{ current.revision }}</el-tag>
            </template>

            <el-form label-width="100px">
              <el-form-item label="检测结论" required>
                <el-radio-group v-model="form.conclusion">
                  <el-radio-button label="PASS">合格</el-radio-button>
                  <el-radio-button label="FAIL">不合格</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="检测时间">
                <el-date-picker
                  v-model="form.inspectedAt"
                  type="datetime"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  placeholder="默认取当前时间"
                />
              </el-form-item>

              <el-form-item label="检测项目">
                <div class="items">
                  <div v-for="(item, idx) in form.items" :key="idx" class="item-row">
                    <el-input v-model="item.name" placeholder="项目名，例如 制动" style="width: 200px" />
                    <el-select v-model="item.result" style="width: 120px">
                      <el-option label="合格" value="合格" />
                      <el-option label="不合格" value="不合格" />
                    </el-select>
                    <el-button link type="danger" @click="removeItem(idx)">删除</el-button>
                  </div>
                  <el-button link type="primary" @click="addItem">+ 添加项目</el-button>
                </div>
              </el-form-item>

              <el-form-item v-if="isCorrection" label="更正原因" required>
                <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="请说明为什么要更正已登记的结果" />
              </el-form-item>
            </el-form>

            <div class="footer">
              <el-button @click="router.push('/rongli/inspect/inspectionTodo')">返回</el-button>
              <el-button
                v-if="current"
                type="warning"
                :loading="saving"
                :disabled="order.licenseStatus !== 'VERIFIED'"
                @click="submitCorrection"
              >
                发起更正
              </el-button>
              <el-button
                v-else
                type="primary"
                :loading="saving"
                :disabled="order.licenseStatus !== 'VERIFIED' || order.fulfillmentStatus !== 'INSPECTING'"
                @click="submitResult"
              >
                提交结果
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="10">
          <el-card shadow="never">
            <template #header>历史版本</template>
            <el-timeline v-if="versions.length">
              <el-timeline-item
                v-for="v in versions"
                :key="v.resultId"
                :timestamp="fmt(v.submittedAt)"
                placement="top"
                :type="v.conclusion === 'PASS' ? 'success' : 'danger'"
              >
                <div class="ver-title">
                  v{{ v.revision }} · {{ v.conclusionLabel }}
                  <el-tag v-if="v.supersededAt" size="small" type="info">已被更正</el-tag>
                </div>
                <div class="ver-items">
                  <span v-for="(val, key) in (v.items || {})" :key="key" class="ver-item">
                    {{ key }}：{{ val }}
                  </span>
                </div>
                <div class="muted">来源 {{ v.source || '—' }} · 检测时间 {{ fmt(v.inspectedAt) }}</div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="尚未登记过结果" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>
    </template>
  </RongliPage>
</template>

<script setup name="RongliResultRegister" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyResultCorrection, getOrder, getOrderResults, submitOrderResult } from '@/api/rongli'

const route = useRoute()
const router = useRouter()
const queryOrderNo = String(route.query.orderNo || '')

const loading = ref(false)
const saving = ref(false)
const order = ref<any>({})
const versions = ref<any[]>([])
const current = ref<any>(null)

const form = reactive<any>({
  conclusion: 'PASS',
  inspectedAt: '',
  reason: '',
  items: [{ name: '', result: '合格' }]
})

const isCorrection = computed(() => !!current.value)

onMounted(async () => {
  if (!queryOrderNo) return
  await load()
})

async function load() {
  loading.value = true
  try {
    order.value = (await getOrder(queryOrderNo)) || {}
    const res: any = await getOrderResults(queryOrderNo)
    versions.value = (res && res.versions) || []
    current.value = (res && res.current) || null
    if (current.value) {
      form.conclusion = current.value.conclusion || 'PASS'
      form.items = Object.entries(current.value.items || {}).map(([name, result]) => ({
        name,
        result: String(result)
      }))
      if (!form.items.length) form.items = [{ name: '', result: '合格' }]
    }
  } finally {
    loading.value = false
  }
}

function addItem() {
  form.items.push({ name: '', result: '合格' })
}

function removeItem(idx: number) {
  form.items.splice(idx, 1)
}

function buildItems() {
  const obj: Record<string, string> = {}
  for (const it of form.items) {
    const name = String(it.name || '').trim()
    if (!name) continue
    obj[name] = it.result
  }
  return JSON.stringify(obj)
}

async function submitResult() {
  saving.value = true
  try {
    await submitOrderResult(queryOrderNo, {
      conclusion: form.conclusion,
      items: buildItems(),
      inspectedAt: form.inspectedAt || undefined
    })
    ElMessage.success('检测结果已登记')
    await load()
  } finally {
    saving.value = false
  }
}

async function submitCorrection() {
  if (!form.reason.trim()) {
    ElMessage.warning('请填写更正原因')
    return
  }
  saving.value = true
  try {
    await applyResultCorrection(queryOrderNo, {
      conclusion: form.conclusion,
      items: buildItems(),
      reason: form.reason
    })
    ElMessage.success('更正申请已提交，等待审核')
    form.reason = ''
    await load()
  } finally {
    saving.value = false
  }
}

function fmt(v: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '—'
}
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.ml8 { margin-left: 8px; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; margin-top: 4px; }
.items { width: 100%; }
.item-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.footer { margin-top: 12px; text-align: right; }
.ver-title { font-weight: 600; display: flex; align-items: center; gap: 8px; }
.ver-items { margin-top: 4px; display: flex; flex-wrap: wrap; gap: 8px; font-size: 12px; }
.ver-item { background: var(--el-fill-color-light); padding: 1px 6px; border-radius: 3px; }
</style>
