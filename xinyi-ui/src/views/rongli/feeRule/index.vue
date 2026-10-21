<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item>
        <el-button type="primary" icon="Plus" @click="openCreate">新增费用规则</el-button>
        <el-button icon="Refresh" @click="load">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="规则键" prop="ruleKey" min-width="180" show-overflow-tooltip />
      <el-table-column label="版本" prop="revision" width="80" align="center" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'DRAFT' ? 'info' : 'warning'">
            {{ row.statusLabel || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计价配置" min-width="260">
        <template #default="{ row }">
          <span class="mono">{{ JSON.stringify(row.config || {}) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生效时间" prop="effectiveFrom" width="190" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'DRAFT'" link type="success" @click="publish(row)">发布</el-button>
          <el-button link type="primary" @click="openTrial(row)">试算</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="form.ruleId ? '编辑费用规则' : '新增费用规则'" width="600px">
      <el-form label-width="140px">
        <el-form-item label="规则键"><el-input v-model="form.ruleKey" placeholder="例如 DRIVER_FEE_V1" /></el-form-item>
        <el-form-item label="里程单价(分/公里)">
          <el-input-number v-model="form.unitPriceFen" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="基础报酬(分)">
          <el-input-number v-model="form.baseFeeFen" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="单票最低(分)">
          <el-input-number v-model="form.minFeeFen" :min="0" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="trialVisible" title="规则试算" width="560px">
      <el-form label-width="140px">
        <el-form-item label="规则 ID"><el-input v-model="trial.ruleId" placeholder="留空使用已发布规则" /></el-form-item>
        <el-form-item label="里程(公里)"><el-input-number v-model="trial.mileageKm" :min="0" :step="1" /></el-form-item>
        <el-form-item label="订单金额(分)"><el-input-number v-model="trial.orderFen" :min="0" :step="100" /></el-form-item>
      </el-form>
      <el-alert v-if="trialResult" :title="`试算报酬 ${(Number(trialResult.feeFen || 0) / 100).toFixed(2)} 元`" type="success" :closable="false" />
      <template #footer>
        <el-button @click="trialVisible = false">关闭</el-button>
        <el-button type="primary" @click="runTrial">试算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliFeeRule" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createFeeRule, listFeeRules, publishFeeRule, trialFeeRule, updateFeeRule } from '@/api/rongli'

const loading = ref(false)
const rows = ref<any[]>([])
const formVisible = ref(false)
const form = reactive<any>({ ruleId: '', ruleKey: 'DRIVER_FEE_V1', unitPriceFen: 150, baseFeeFen: 1000, minFeeFen: 1500 })

const trialVisible = ref(false)
const trial = reactive<any>({ ruleId: '', mileageKm: 8, orderFen: 20000 })
const trialResult = ref<any>(null)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listFeeRules()
    rows.value = (res && res.rows) || (Array.isArray(res) ? res : [])
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { ruleId: '', ruleKey: 'DRIVER_FEE_V1', unitPriceFen: 150, baseFeeFen: 1000, minFeeFen: 1500 })
  formVisible.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    ruleId: String(row.ruleId),
    ruleKey: row.ruleKey,
    unitPriceFen: row.config?.unitPriceFen || 0,
    baseFeeFen: row.config?.baseFeeFen || 0,
    minFeeFen: row.config?.minFeeFen || 0
  })
  formVisible.value = true
}

async function save() {
  const payload = {
    ruleKey: form.ruleKey,
    config: {
      unitPriceFen: form.unitPriceFen,
      baseFeeFen: form.baseFeeFen,
      minFeeFen: form.minFeeFen
    }
  }
  if (form.ruleId) {
    await updateFeeRule(form.ruleId, payload)
  } else {
    await createFeeRule(payload)
  }
  ElMessage.success('已保存')
  formVisible.value = false
  load()
}

async function publish(row: any) {
  await ElMessageBox.confirm('发布后新产生的费用将按该版本计算，是否继续？', '发布规则', { type: 'warning' })
  await publishFeeRule(row.ruleId)
  ElMessage.success('已发布')
  load()
}

function openTrial(row: any) {
  trial.ruleId = row ? String(row.ruleId) : ''
  trialResult.value = null
  trialVisible.value = true
}

async function runTrial() {
  trialResult.value = await trialFeeRule({
    ruleId: trial.ruleId ? Number(trial.ruleId) : null,
    mileageKm: trial.mileageKm,
    orderFen: trial.orderFen
  })
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mono { font-family: Consolas, Monaco, monospace; font-size: 12px; }
</style>
