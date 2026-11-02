<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { listOrders, getOrder, type AdminOrderSummary, type AdminOrderView } from '@/api/rongli/order'
const rows = ref<AdminOrderSummary[]>([]), detail = ref<AdminOrderView | null>(null), total = ref(0), page = ref(1), loading = ref(false), detailLoading = ref(false), open = ref(false), error = ref(''), detailError = ref('')
let sequence = 0, detailSequence = 0
const time = (v: string | null) => v ? new Date(v).toLocaleString('zh-CN', { hour12: false }) : '—'
const yuan = (v: number) => (v / 100).toFixed(2)
onMounted(load)
onBeforeUnmount(() => { sequence++; detailSequence++; detail.value = null })
async function load() {
  const run = ++sequence; loading.value = true; rows.value = []; error.value = ''
  try { const res = await listOrders(page.value, 20); if (run === sequence) { rows.value = res.rows; total.value = res.total } }
  catch (e: any) { if (run === sequence) error.value = e.msg || e.message || '订单加载失败' }
  finally { if (run === sequence) loading.value = false }
}
async function show(orderNo: string) {
  const run = ++detailSequence; open.value = true; detail.value = null; detailError.value = ''; detailLoading.value = true
  try { const res = await getOrder(orderNo); if (run === detailSequence) detail.value = res.data }
  catch (e: any) { if (run === detailSequence) detailError.value = e.msg || e.message || '详情加载失败' }
  finally { if (run === detailSequence) detailLoading.value = false }
}
function close() { detailSequence++; detail.value = null; detailError.value = ''; detailLoading.value = false }
const statusLabel = (value: string) => ({'PENDING': '待处理','PAID': '已付款','CLOSED': '已关闭','NONE': '无','NOT_REQUIRED': '无需退款','WAIT_RETURN': '待还车','READY': '待执行','PROCESSING': '处理中','SUCCESS': '已完成','UNKNOWN': '结果待确认','ABNORMAL': '待核查','PENDING_REVIEW': '待审核','EXECUTING': '执行中','REJECTED': '未通过','MISSING': '未提交','VERIFIED': '已核验','PASS': '合格','FAIL': '不合格'} as Record<string, string>)[value] || value;
</script>
<template>
  <div class="app-container order-page">
    <div class="heading"><div><h2>客户订单</h2><p>按订单归属部门展示；金额与接车地址按专项权限开放。</p></div><el-button :loading="loading" @click="load">刷新列表</el-button></div>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-table v-loading="loading" :data="rows" empty-text="暂无可查看的订单" row-key="orderNo">
      <el-table-column prop="orderNo" label="订单号" min-width="290" />
      <el-table-column prop="vehicle.plateNo" label="车牌" width="120" />
      <el-table-column prop="station.name" label="服务站点" min-width="150" />
      <el-table-column prop="displayStatus" label="订单状态" width="130" />
      <el-table-column label="接车时间" min-width="180"><template #default="{ row }">{{ time(row.slotStartAt) }}</template></el-table-column>
      <el-table-column label="操作" fixed="right" width="100"><template #default="{ row }"><el-button v-if="row.allowedActions.includes('VIEW_DETAIL')" link type="primary" @click="show(row.orderNo)">查看详情</el-button><span v-else>—</span></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="load" />
    <el-drawer v-model="open" title="订单详情" size="min(720px, 100vw)" destroy-on-close @close="close">
      <div v-loading="detailLoading" class="detail">
        <el-alert v-if="detailError" :title="detailError" type="error" :closable="false" />
        <template v-if="detail">
          <h2>{{ detail.displayStatus }}</h2><p class="number">{{ detail.orderNo }}</p>
          <el-descriptions :column="1" border><el-descriptions-item label="车辆">{{ detail.vehicle.plateNo }} · {{ detail.vehicle.vehicleTypeLabel }}</el-descriptions-item><el-descriptions-item label="站点">{{ detail.station.name }}</el-descriptions-item><el-descriptions-item label="预约接车">{{ time(detail.slotStartAt) }} — {{ time(detail.slotEndAt) }}</el-descriptions-item><el-descriptions-item label="客户">{{ detail.ownerContact.displayName }} {{ detail.ownerContact.phoneMask }}</el-descriptions-item><el-descriptions-item label="已还车">{{ time(detail.returnedAt) }}</el-descriptions-item><el-descriptions-item label="支付 / 退款">{{ statusLabel(detail.paymentStatus) }} / {{ statusLabel(detail.refundStatus) }}</el-descriptions-item></el-descriptions>
          <section v-if="detail.driver"><h3>当前代驾</h3><p>{{ detail.driver.displayName }} · {{ detail.driver.driverCode }}</p></section>
          <section><h3>服务与争议</h3><p>保管方：{{ ({ OWNER: '车主', DRIVER: '代驾', STATION: '检测站' } as Record<string,string>)[detail.custody] || detail.custody }}</p><p>争议：{{ ({ NONE: '无', OPEN: '待处理', PROCESSING: '处理中', RESOLVED: '已解决' } as Record<string,string>)[detail.disputeStatus] || detail.disputeStatus }}</p></section>
          <section v-if="detail.money"><h3>客户费用</h3><p v-for="item in detail.money.items" :key="item.feeCode">{{ item.name }}：¥{{ yuan(item.amount) }}</p><p>应付 ¥{{ yuan(detail.money.totalAmount) }} · 已付 ¥{{ yuan(detail.money.paidAmount) }} · 已退 ¥{{ yuan(detail.money.refundedAmount) }}</p></section>
          <section v-if="detail.pickupAddress"><h3>接车地址</h3><p>{{ detail.pickupAddress.address }}</p></section>
          <section v-if="detail.returnAddress"><h3>还车地址</h3><p>{{ detail.returnAddress.address }}</p></section>
          <section><h3>交接里程</h3><p>接车 {{ detail.mileage.pickupKm ?? '未记录' }} · 还车 {{ detail.mileage.returnKm ?? '未记录' }}</p><p v-if="detail.mileage.publicExplanation">{{ detail.mileage.publicExplanation }}</p></section>
          <section v-if="detail.afterSale"><h3>售后进度</h3><p>{{ statusLabel(detail.afterSale.caseStatus) }} · {{ statusLabel(detail.afterSale.refundState) }}</p><p v-if="detail.afterSale.approvedAmount !== null">核准金额 ¥{{ yuan(detail.afterSale.approvedAmount) }}</p></section>
          <section><h3>服务节点</h3><p>付款截止：{{ time(detail.payExpireAt) }}</p><p>派单：{{ time(detail.dispatchAt) }}</p><p>自动确认：{{ time(detail.autoConfirmAt) }}</p></section>
          <el-button @click="show(detail.orderNo)">刷新详情</el-button>
        </template>
      </div>
    </el-drawer>
  </div>
</template>
<style scoped>
.heading { display: flex; align-items: center; justify-content: space-between; gap: 20px; }.heading p { color: #64748b; margin-bottom: 24px; }.el-pagination { margin-top: 20px; }.detail { min-height: 180px; }.detail section { margin-top: 24px; padding-top: 8px; border-top: 1px solid #e2e8f0; }.number { overflow-wrap: anywhere; color: #64748b; }@media(max-width: 640px) { .heading { align-items: flex-start; }.app-container { padding: 14px; } }
</style>
