<template>
  <RongliPage title="车辆列表" subtitle="查找车辆，查看资料、关联车主和历次检测记录。">
    <template #actions>
      <el-button icon="Download" @click="applyExport">导出申请</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="query.keyword"
          placeholder="车牌 / 车型 / 品牌 / 车主"
          clearable
          prefix-icon="Search"
          class="rl-w-260"
          @keyup.enter="handleQuery"
        />
        <el-select v-model="query.verificationStatus" placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="全部" value="" />
          <el-option label="资料已核验" value="VERIFIED" />
          <el-option label="资料待核验" value="PENDING" />
        </el-select>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Sort" @click="toggleSort">{{ sortDesc ? '降序' : '升序' }}</el-button>
      </div>

      <el-table v-loading="loading" :data="sortedRows" :border="false" style="width: 100%">
        <el-table-column label="车辆" min-width="200">
          <template #default="{ row }">
            <div class="rl-strong">{{ row.plateNo }}</div>
            <div class="rl-sub">{{ row.brandModel || '未登记品牌型号' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="计价车型" width="140">
          <template #default="{ row }">{{ row.vehicleTypeName || row.vehicleTypeCode || '—' }}</template>
        </el-table-column>
        <el-table-column label="关联车主" width="150">
          <template #default="{ row }">
            <el-button v-if="row.ownerName" link type="primary" @click="openOwner(row)">{{ row.ownerName }}</el-button>
            <span v-else class="rl-muted">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="最近检测" min-width="170">
          <template #default="{ row }">
            <template v-if="row.lastInspectedAt">
              <div class="rl-strong">{{ shortDate(row.lastInspectedAt) }}</div>
              <div class="rl-sub">登记结果 {{ row.lastConclusion === 'PASS' ? '通过' : row.lastConclusion === 'FAIL' ? '不通过' : '—' }}</div>
            </template>
            <span v-else class="rl-muted">暂无检测记录</span>
          </template>
        </el-table-column>
        <el-table-column label="资料状态" width="130">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.verificationStatus === 'VERIFIED' ? 'rl-tag--green' : 'rl-tag--amber'">
              {{ row.verificationStatus === 'VERIFIED' ? '资料已核验' : '资料待核验' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看档案</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">未找到匹配车辆</div>
            <div class="rl-empty__hint">请调整关键词或资料状态后重新查询。</div>
            <el-button @click="resetQuery">重置筛选</el-button>
          </div>
        </template>
      </el-table>

      <div class="rl-pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="handleQuery"
        />
      </div>
    </section>

    <!-- 车辆档案 -->
    <el-drawer v-model="detailVisible" :title="detail.plateNo || '车辆档案'" size="620px">
      <div class="rl-detail-strip">
        <div><span>资料状态</span><b>{{ detail.verificationStatus === 'VERIFIED' ? '资料已核验' : '资料待核验' }}</b></div>
        <div><span>计价车型</span><b>{{ detail.vehicleTypeName || detail.vehicleTypeCode || '—' }}</b></div>
        <div><span>关联车主</span><b>{{ detail.ownerName || '未关联' }}</b></div>
        <div><span>最近检测</span><b>{{ detail.lastInspectedAt ? shortDate(detail.lastInspectedAt) : '暂无' }}</b></div>
      </div>

      <el-tabs v-model="tab">
        <el-tab-pane label="基本资料" name="base">
          <div class="rl-kv">
            <div><span>车牌号码</span><b>{{ detail.plateNo || '—' }}</b></div>
            <div><span>计价车型</span><b>{{ detail.vehicleTypeName || detail.vehicleTypeCode || '—' }}</b></div>
            <div><span>品牌型号</span><b>{{ detail.brandModel || '—' }}</b></div>
            <div><span>车辆识别代号</span><b>{{ detail.vin || '—' }}</b></div>
            <div><span>注册日期</span><b>{{ detail.registerDate || '—' }}</b></div>
            <div><span>资料状态</span><b>{{ detail.verificationStatus === 'VERIFIED' ? '资料已核验' : '资料待核验' }}</b></div>
            <div><span>内部备注</span><b>{{ detail.internalRemark || '暂无备注' }}</b></div>
            <div><span>创建时间</span><b>{{ shortDate(detail.createdAt) }}</b></div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="预约记录" name="orders">
          <el-table :data="orders" size="small">
            <el-table-column label="订单号" prop="orderNo" width="180" />
            <el-table-column label="履约状态" prop="fulfillmentStatusLabel" width="130" />
            <el-table-column label="上门时段" prop="pickupWindowLabel" min-width="170" />
          </el-table>
          <div v-if="!orders.length" class="rl-empty"><div class="rl-empty__hint">暂无预约记录。</div></div>
        </el-tab-pane>
        <el-tab-pane label="检测记录" name="results">
          <el-table :data="results" size="small">
            <el-table-column label="检测时间" width="170">
              <template #default="{ row }">{{ shortDate(row.inspectedAt) }}</template>
            </el-table-column>
            <el-table-column label="结论" width="100">
              <template #default="{ row }">{{ conclusionLabel(row.conclusion) }}</template>
            </el-table-column>
            <el-table-column label="版本" width="80">
              <template #default="{ row }">{{ revisionLabel(row.revision) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!results.length" class="rl-empty"><div class="rl-empty__hint">暂无检测记录。</div></div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEdit">编辑资料</el-button>
      </template>
    </el-drawer>

    <!-- 编辑资料 -->
    <el-dialog v-model="editVisible" title="编辑车辆资料" width="560px">
      <div class="rl-kv" style="margin-bottom: 16px">
        <div><span>车辆编号</span><b class="rl-mono">{{ detail.id }}</b></div>
        <div><span>车牌</span><b>{{ detail.plateNo }}</b></div>
        <div><span>关联车主</span><b>{{ detail.ownerName || '未关联' }}</b></div>
        <div><span>资料状态</span><b>{{ detail.verificationStatus === 'VERIFIED' ? '资料已核验' : '资料待核验' }}</b></div>
      </div>
      <el-form label-width="110px">
        <el-form-item label="资料核验">
          <el-select v-model="editForm.verificationStatus" style="width: 100%">
            <el-option label="资料待核验" value="PENDING" />
            <el-option label="资料已核验" value="VERIFIED" />
            <el-option label="资料不通过" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="资料变更原因">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="说明核实依据，不改写历史订单资料" />
        </el-form-item>
      </el-form>
      <p class="rl-muted" style="font-size: 12px">本次编辑只影响车辆档案；已生成订单继续使用当时的信息。</p>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliVehicle" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVehicle, listVehicleOrders, listVehicleResults, listVehicles, updateVehicle } from '@/api/rongli'
import { shortDate, revisionLabel } from '../format'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const sortDesc = ref(true)
const query = reactive<any>({ keyword: '', verificationStatus: '', pageNum: 1, pageSize: 10 })

const sortedRows = computed(() => {
  const list = [...rows.value]
  list.sort((a, b) => String(a.plateNo || '').localeCompare(String(b.plateNo || ''), 'zh-CN') * (sortDesc.value ? -1 : 1))
  return list
})

const detailVisible = ref(false)
const editVisible = ref(false)
const tab = ref('base')
const detail = ref<any>({})
const orders = ref<any[]>([])
const results = ref<any[]>([])
const editForm = reactive<any>({ verificationStatus: 'PENDING', remark: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await listVehicles(query)
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

function resetQuery() {
  query.keyword = ''
  query.verificationStatus = ''
  query.pageNum = 1
  load()
}

function toggleSort() {
  sortDesc.value = !sortDesc.value
}

function conclusionLabel(value?: string) {
  if (value === 'PASS') return '通过'
  if (value === 'FAIL') return '不通过'
  return '—'
}

async function openDetail(row: any) {
  tab.value = 'base'
  detail.value = { ...row }
  orders.value = []
  results.value = []
  detailVisible.value = true
  const [view, ord, res] = await Promise.all([
    getVehicle(row.id || row.vehicleId).catch(() => null),
    listVehicleOrders(row.id || row.vehicleId).catch(() => null),
    listVehicleResults(row.id || row.vehicleId).catch(() => null)
  ])
  if (view) detail.value = { ...detail.value, ...(view.rows ? view.rows[0] || view : view) }
  orders.value = (ord && (ord.rows || ord)) || []
  results.value = (res && (res.rows || res)) || []
}

function openOwner(row: any) {
  ElMessage.info(`车主 ${row.ownerName} 的档案在「车主列表」查看`)
}

function openEdit() {
  editForm.verificationStatus = detail.value.verificationStatus || 'PENDING'
  editForm.remark = detail.value.internalRemark || ''
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.remark) {
    ElMessage.warning('请填写资料变更原因')
    return
  }
  await updateVehicle(detail.value.id || detail.value.vehicleId, {
    verificationStatus: editForm.verificationStatus,
    remark: editForm.remark
  })
  ElMessage.success('车辆资料已更新')
  editVisible.value = false
  detailVisible.value = false
  load()
}

async function applyExport() {
  await ElMessageBox.alert('导出申请将进入「敏感访问与导出审批」，批准后下载仍需校验当时权限。', '导出申请')
}
</script>
