<template>
  <RongliPage title="车主列表" subtitle="管理车主资料，查看关联车辆、预约记录与售后情况。">
    <template #actions>
      <el-button icon="Download" @click="applyExport">导出申请</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-filters">
        <el-input
          v-model="query.keyword"
          placeholder="姓名 / 手机尾号 / 编号"
          clearable
          prefix-icon="Search"
          class="rl-w-260"
          @keyup.enter="handleQuery"
        />
        <el-select v-model="query.serviceFlag" placeholder="全部" style="width: 150px" @change="handleQuery">
          <el-option label="全部" value="" />
          <el-option label="正常" value="NORMAL" />
          <el-option label="需二次核实" value="VERIFY_AGAIN" />
        </el-select>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Sort" @click="sortDesc = !sortDesc">{{ sortDesc ? '降序' : '升序' }}</el-button>
      </div>

      <el-table v-loading="loading" :data="sortedRows" :border="false">
        <el-table-column label="车主" min-width="190">
          <template #default="{ row }">
            <div class="rl-person">
              <span class="rl-avatar">{{ initial(row) }}</span>
              <div>
                <div class="rl-strong">{{ row.serviceName || row.nickname || '未登记称呼' }}</div>
                <div class="rl-mono">{{ ownerCode(row) }} · {{ row.nickname || '未设置昵称' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="已验证手机号" width="150">
          <template #default="{ row }">
            <span v-if="row.phoneMasked" class="rl-mono">{{ row.phoneMasked }}</span>
            <span v-else class="rl-muted">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="关联车辆" width="110">
          <template #default="{ row }">{{ row.vehicleCount || 0 }} 辆</template>
        </el-table-column>
        <el-table-column label="累计预约" width="110">
          <template #default="{ row }">{{ row.orderCount || 0 }} 单</template>
        </el-table-column>
        <el-table-column label="最近服务" width="150">
          <template #default="{ row }">
            <span v-if="row.lastOrderAt">{{ shortDate(row.lastOrderAt) }}</span>
            <span v-else class="rl-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="服务标记" width="130">
          <template #default="{ row }">
            <span class="rl-tag" :class="row.serviceFlag === 'VERIFY_AGAIN' ? 'rl-tag--amber' : 'rl-tag--green'">
              {{ row.serviceFlag === 'VERIFY_AGAIN' ? '需二次核实' : '正常' }}
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
            <div class="rl-empty__title">未找到匹配车主</div>
            <div class="rl-empty__hint">请调整关键词或服务标记后重新查询。</div>
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

    <!-- 车主档案 -->
    <el-drawer v-model="detailVisible" title="车主档案" size="640px">
      <div class="profile-summary">
        <span class="rl-avatar rl-avatar--lg">{{ initial(detail) }}</span>
        <div class="profile-summary__main">
          <div class="rl-strong" style="font-size: 15px">{{ detail.serviceName || detail.nickname || '未登记称呼' }}</div>
          <div class="rl-sub">{{ detail.phoneMasked || '未绑定手机号' }}</div>
        </div>
        <span class="rl-tag" :class="detail.serviceFlag === 'VERIFY_AGAIN' ? 'rl-tag--amber' : 'rl-tag--green'">
          {{ detail.serviceFlag === 'VERIFY_AGAIN' ? '需二次核实' : '正常' }}
        </span>
      </div>

      <el-tabs v-model="tab">
        <el-tab-pane label="基本资料" name="base">
          <div class="rl-kv">
            <div><span>账号编号</span><b class="rl-mono">{{ ownerCode(detail) }}</b></div>
            <div><span>称呼</span><b>{{ detail.serviceName || '—' }}</b></div>
            <div><span>昵称</span><b>{{ detail.nickname || '—' }}</b></div>
            <div><span>已验证手机</span><b>{{ detail.phoneMasked || '未绑定' }}</b></div>
            <div><span>服务标记</span><b>{{ detail.serviceFlag === 'VERIFY_AGAIN' ? '需二次核实' : '正常' }}</b></div>
            <div><span>内部备注</span><b>{{ detail.internalRemark || '暂无备注' }}</b></div>
            <div><span>账号状态</span><b>{{ detail.status === 'LOCKED' ? '已锁定' : '正常' }}</b></div>
            <div><span>注册时间</span><b>{{ shortTime(detail.createdAt) }}</b></div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="关联车辆" name="vehicles">
          <div v-for="v in vehicles" :key="v.id" class="rl-listrow">
            <el-icon><Van /></el-icon>
            <div class="rl-listrow__main">
              <div class="rl-strong">{{ v.plateNo }}</div>
              <div class="rl-sub">{{ v.brandModel || v.vehicleTypeName || '未登记品牌型号' }}</div>
            </div>
            <span class="rl-tag rl-tag--plain" :class="v.verificationStatus === 'VERIFIED' ? 'rl-tag--green' : 'rl-tag--amber'">
              {{ v.verificationStatus === 'VERIFIED' ? '资料已核验' : '资料待核验' }}
            </span>
          </div>
          <div v-if="!vehicles.length" class="rl-empty"><div class="rl-empty__hint">暂无关联车辆。</div></div>
        </el-tab-pane>
        <el-tab-pane label="预约记录" name="orders">
          <el-table :data="orders" size="small">
            <el-table-column label="订单号" prop="orderNo" width="180" />
            <el-table-column label="履约状态" prop="fulfillmentStatusLabel" width="130" />
            <el-table-column label="上门时段" prop="pickupWindowLabel" min-width="170" />
          </el-table>
          <div v-if="!orders.length" class="rl-empty"><div class="rl-empty__hint">暂无预约记录。</div></div>
        </el-tab-pane>
        <el-tab-pane label="售后记录" name="cases">
          <el-table :data="cases" size="small">
            <el-table-column label="售后单号" prop="caseNo" width="170" />
            <el-table-column label="类型" prop="kindLabel" width="120" />
            <el-table-column label="处理状态" prop="caseStatusLabel" width="130" />
            <el-table-column label="申请时间" width="150">
              <template #default="{ row }">{{ shortDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!cases.length" class="rl-empty"><div class="rl-empty__hint">暂无售后申请。</div></div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEdit">维护备注</el-button>
      </template>
    </el-drawer>

    <!-- 维护车主信息 -->
    <el-dialog v-model="editVisible" title="维护车主信息" width="560px">
      <div class="rl-kv" style="margin-bottom: 16px">
        <div><span>车主</span><b>{{ detail.serviceName || detail.nickname }} · {{ ownerCode(detail) }}</b></div>
        <div><span>已验证手机号</span><b>{{ detail.phoneMasked || '未绑定' }}</b></div>
      </div>
      <el-form label-width="100px">
        <el-form-item label="服务称呼">
          <el-input v-model="editForm.serviceName" placeholder="用于客服称呼" />
        </el-form-item>
        <el-form-item label="服务标记">
          <el-select v-model="editForm.serviceFlag" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="需二次核实" value="VERIFY_AGAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="内部备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="仅授权运营人员可见" />
        </el-form-item>
      </el-form>
      <p class="rl-muted" style="font-size: 12px">
        标记为“需二次核实”不禁止下单。手机号变更需要本人验证，不能在此直接覆盖。
      </p>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </RongliPage>
</template>

<script setup name="RongliOwner" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Van } from '@element-plus/icons-vue'
import { getOwner, listOwnerAfterSales, listOwnerOrders, listOwners, updateOwner } from '@/api/rongli'
import { shortDate, shortTime } from '../format'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const sortDesc = ref(true)
const query = reactive<any>({ keyword: '', serviceFlag: '', pageNum: 1, pageSize: 10 })

const sortedRows = computed(() => {
  const list = rows.value.filter((r) => !query.serviceFlag || r.serviceFlag === query.serviceFlag)
  return [...list].sort((a, b) => Number(b.orderId || b.ownerId || 0) - Number(a.orderId || a.ownerId || 0))
})

const detailVisible = ref(false)
const editVisible = ref(false)
const tab = ref('base')
const detail = ref<any>({})
const orders = ref<any[]>([])
const cases = ref<any[]>([])
const editForm = reactive<any>({ serviceName: '', serviceFlag: 'NORMAL', remark: '' })

const vehicles = computed(() => detail.value.vehicles || [])

onMounted(load)

function ownerCode(row: any) {
  const id = row.ownerId ?? row.owner_id ?? row.id
  return id ? 'OWN' + String(id).padStart(6, '0') : '—'
}

function initial(row: any) {
  const name = row.serviceName || row.nickname || ''
  return name ? name.slice(0, 1) : '车'
}

async function load() {
  loading.value = true
  try {
    const res: any = await listOwners(query)
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
  query.serviceFlag = ''
  query.pageNum = 1
  load()
}

function ownerId(row: any) {
  return row.ownerId ?? row.owner_id ?? row.accountId ?? row.account_id
}

async function openDetail(row: any) {
  const id = ownerId(row)
  if (!id) return
  tab.value = 'base'
  detail.value = { ...row }
  orders.value = []
  cases.value = []
  detailVisible.value = true
  const [view, ord, afters] = await Promise.all([
    getOwner(id).catch(() => null),
    listOwnerOrders(id).catch(() => null),
    listOwnerAfterSales(id).catch(() => null)
  ])
  if (view) detail.value = { ...detail.value, ...view }
  orders.value = (ord && (ord.rows || ord)) || []
  cases.value = (afters && (afters.rows || afters)) || []
}

function openEdit() {
  editForm.serviceName = detail.value.serviceName || ''
  editForm.serviceFlag = detail.value.serviceFlag || 'NORMAL'
  editForm.remark = detail.value.internalRemark || ''
  editVisible.value = true
}

async function saveEdit() {
  await updateOwner(ownerId(detail.value), {
    serviceName: editForm.serviceName,
    serviceFlag: editForm.serviceFlag,
    remark: editForm.remark
  })
  ElMessage.success('车主资料已更新')
  editVisible.value = false
  detailVisible.value = false
  load()
}

async function applyExport() {
  await ElMessageBox.alert('导出申请将进入「敏感访问与导出审批」，批准后下载仍需校验当时权限。', '导出申请')
}
</script>

<style scoped>
.rl-person {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rl-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eff8ff;
  color: #175cd3;
  font-size: 12px;
  flex: none;
}
.rl-avatar--lg {
  width: 40px;
  height: 40px;
  font-size: 15px;
}
.profile-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 0 16px;
}
.profile-summary__main {
  flex: 1;
}
.rl-listrow {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--rl-line-soft);
}
.rl-listrow__main {
  flex: 1;
}
</style>
