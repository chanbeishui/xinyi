<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审核" value="PENDING_REVIEW" />
          <el-option label="已审核" value="APPROVED" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="车型">
        <el-input v-model="query.vehicleTypeCode" placeholder="SMALL_CAR" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新增方案</el-button>
        <el-button icon="Education" @click="router.push('/rongli/inspect/vehicleType')">车型字典</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="规则键" prop="ruleKey" min-width="180" show-overflow-tooltip />
      <el-table-column label="版本" prop="revision" width="80" align="center" />
      <el-table-column label="车型" prop="vehicleTypeName" width="130" />
      <el-table-column label="检测类型" prop="inspectionTypeCode" width="130" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="起步价(元)" width="120" align="right">
        <template #default="{ row }">{{ fen2yuan(row.config?.baseFeeFen) }}</template>
      </el-table-column>
      <el-table-column label="检测费(元)" width="120" align="right">
        <template #default="{ row }">{{ fen2yuan(row.config?.inspectionFeeFen) }}</template>
      </el-table-column>
      <el-table-column label="生效时间" prop="effectiveFrom" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="submit(row)">提交审核</el-button>
          <el-button v-if="row.status === 'PENDING_REVIEW'" link type="primary" @click="audit(row, true)">审核通过</el-button>
          <el-button v-if="row.status === 'PENDING_REVIEW'" link type="danger" @click="audit(row, false)">驳回</el-button>
          <el-button v-if="row.status === 'APPROVED'" link type="success" @click="publish(row)">发布</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      class="mt16"
      @current-change="load"
      @size-change="load"
    />

    <el-dialog v-model="formVisible" :title="form.ruleId ? '编辑计价方案' : '新增计价方案'" width="600px">
      <el-form label-width="130px">
        <el-form-item label="车型">
          <el-input v-model="form.vehicleTypeCode" placeholder="SMALL_CAR" />
        </el-form-item>
        <el-form-item label="检测类型">
          <el-input v-model="form.inspectionTypeCode" placeholder="ANNUAL" />
        </el-form-item>
        <el-form-item label="检测服务费(分)">
          <el-input-number v-model="form.inspectionFeeFen" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="起步价(分)">
          <el-input-number v-model="form.baseFeeFen" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="包含里程(米)">
          <el-input-number v-model="form.includedMeters" :min="0" :step="1000" />
        </el-form-item>
        <el-form-item label="超里程单价(分/公里)">
          <el-input-number v-model="form.unitPriceFen" :min="0" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RongliPrice" lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditPrice, createPrice, listPrices, publishPrice, submitPrice, updatePrice } from '@/api/rongli'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ status: '', vehicleTypeCode: '', pageNum: 1, pageSize: 10 })
const formVisible = ref(false)
const form = reactive<any>({
  ruleId: '',
  vehicleTypeCode: 'SMALL_CAR',
  inspectionTypeCode: 'ANNUAL',
  inspectionFeeFen: 20000,
  baseFeeFen: 3000,
  includedMeters: 5000,
  unitPriceFen: 200
})

function fen2yuan(fen: any) {
  return ((Number(fen || 0)) / 100).toFixed(2)
}

function tagType(status: string) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'PENDING_REVIEW') return 'warning'
  if (status === 'DISABLED') return 'info'
  return ''
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listPrices(query)
    rows.value = (res && res.rows) || []
    total.value = Number((res && res.total) || 0)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  load()
}

function openCreate() {
  Object.assign(form, {
    ruleId: '',
    vehicleTypeCode: 'SMALL_CAR',
    inspectionTypeCode: 'ANNUAL',
    inspectionFeeFen: 20000,
    baseFeeFen: 3000,
    includedMeters: 5000,
    unitPriceFen: 200
  })
  formVisible.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    ruleId: String(row.ruleId),
    vehicleTypeCode: row.vehicleTypeCode,
    inspectionTypeCode: row.inspectionTypeCode,
    inspectionFeeFen: row.config?.inspectionFeeFen || 0,
    baseFeeFen: row.config?.baseFeeFen || 0,
    includedMeters: row.config?.includedMeters || 0,
    unitPriceFen: row.config?.unitPriceFen || 0
  })
  formVisible.value = true
}

async function save() {
  const payload = {
    vehicleTypeCode: form.vehicleTypeCode,
    inspectionTypeCode: form.inspectionTypeCode,
    config: {
      inspectionFeeFen: form.inspectionFeeFen,
      baseFeeFen: form.baseFeeFen,
      includedMeters: form.includedMeters,
      unitPriceFen: form.unitPriceFen
    }
  }
  if (form.ruleId) {
    await updatePrice(form.ruleId, payload)
  } else {
    await createPrice(payload)
  }
  ElMessage.success('已保存')
  formVisible.value = false
  load()
}

async function submit(row: any) {
  await submitPrice(row.ruleId)
  ElMessage.success('已提交审核')
  load()
}

async function audit(row: any, approve: boolean) {
  const opinion = approve ? '审核通过' : '价格不合理，请调整'
  await auditPrice(row.ruleId, { approve, opinion })
  ElMessage.success(approve ? '已审核通过' : '已驳回')
  load()
}

async function publish(row: any) {
  await ElMessageBox.confirm('发布后新订单将使用该版本，是否继续？', '发布计价方案', { type: 'warning' })
  await publishPrice(row.ruleId)
  ElMessage.success('已发布')
  load()
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt16 { margin-top: 16px; justify-content: flex-end; }
</style>
