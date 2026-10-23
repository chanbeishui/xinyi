<template>
  <div>
    <el-card shadow="never" class="intro">
      <div class="title">客户线下收退款（B52）</div>
      <div class="hint">
        线下付款不等于「不用付」：客户在支付页选择线下后，订单仍处于待支付；必须由后台登记<b>真实收到的钱</b>，
        再由另一位有权人员<b>独立核实全额到账</b>，才计为已支付并进入派单资格。<br />
        红线：登记人不能核实自己登记的收款（系统强制拦截）；本期只接受一次全额收款，少款/多款走人工差错处理；
        原微信收款的订单退款必须走原商户微信退款，不能登记成线下退款。
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.status" clearable placeholder="状态" style="width: 160px">
          <el-option v-for="s in proofStatuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filter.direction" clearable placeholder="方向" style="width: 160px">
          <el-option label="收款 COLLECTION" value="COLLECTION" />
          <el-option label="退款 REFUND" value="REFUND" />
        </el-select>
        <el-button type="primary" size="small" @click="load">查询</el-button>
        <el-button type="success" size="small" @click="openRegister">登记收款/退款</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" size="small" style="margin-top: 12px">
        <el-table-column prop="proofId" label="凭证ID" width="90" />
        <el-table-column label="方向" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.direction === 'COLLECTION' ? 'success' : 'warning'">
              {{ row.direction === 'COLLECTION' ? '收款' : '退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="210" show-overflow-tooltip>
          <template #default="{ row }">{{ row.orderNo || row.refundNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">{{ fen2yuan(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="方式" width="110">
          <template #default="{ row }">{{ row.method === 'CASH' ? '现金' : '银行转账' }}</template>
        </el-table-column>
        <el-table-column prop="externalReferenceMask" label="凭证号/流水" width="150">
          <template #default="{ row }">{{ row.externalReferenceMask || '—' }}</template>
        </el-table-column>
        <el-table-column prop="paidAt" label="到账时刻" width="180" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="proofTag(row.status)">{{ proofLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacityResolution" label="容量处理" width="140" />
        <el-table-column prop="registeredAt" label="登记时刻" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'REGISTERED'" link type="warning" size="small"
                       @click="openVerify(row)">核实</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page.pageNum" :page-size="page.pageSize" :total="page.total"
                     layout="total, prev, pager, next" style="margin-top: 12px" @current-change="load" />
    </el-card>

    <!-- ============================== 登记 -->
    <el-dialog v-model="registerDialog" title="登记客户线下收退款" width="620px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px"
                title="只登记真实已到账的资金。登记后需要另一位有权人员核实才会计为已支付。" />
      <el-form label-width="130px">
        <el-form-item label="方向" required>
          <el-radio-group v-model="form.direction">
            <el-radio value="COLLECTION">收款</el-radio>
            <el-radio value="REFUND">退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.direction === 'COLLECTION' ? '订单号' : '订单号（原收款单）'" required>
          <el-input v-model="form.orderNo" placeholder="例如 RLPRL20261023xxxx" />
        </el-form-item>
        <el-form-item v-if="form.direction === 'REFUND'" label="退款单号" required>
          <el-input v-model="form.refundNo" placeholder="已由统一售后批准的退款单号" />
        </el-form-item>
        <el-form-item label="收款方式" required>
          <el-radio-group v-model="form.method">
            <el-radio value="CASH">现金</el-radio>
            <el-radio value="BANK_TRANSFER">银行转账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实际金额（元）" required>
          <el-input v-model="form.amountYuan" placeholder="必须与应付/已批准退款金额一致" />
          <div class="hint">按元填写，提交前换算为分；本期不接受少款或多款。</div>
        </el-form-item>
        <el-form-item label="到账时刻" required>
          <el-date-picker v-model="form.paidAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ssZ"
                          placeholder="实际到账时刻" style="width: 100%" />
        </el-form-item>
        <el-form-item label="凭证号/银行流水" required>
          <el-input v-model="form.externalReference" maxlength="128"
                    placeholder="主体内唯一；重复登记会被拒绝" />
        </el-form-item>
        <el-form-item label="登记说明" required>
          <el-input v-model="form.note" type="textarea" :rows="3" placeholder="例如：客户到店现金缴纳，收据号 xxx" />
        </el-form-item>
        <el-form-item label="凭证附件ID">
          <el-input v-model="form.voucherFileIds" placeholder="多个用英文逗号分隔（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doRegister">提交登记</el-button>
      </template>
    </el-dialog>

    <!-- ============================== 核实 -->
    <el-dialog v-model="verifyDialog" title="独立核实线下资金" width="520px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px"
                title="核实人不能是登记人。核实通过即计为已支付并尝试进入待派单。" />
      <el-descriptions :column="1" border size="small" style="margin-bottom: 14px">
        <el-descriptions-item label="凭证ID">{{ verifyTarget?.proofId }}</el-descriptions-item>
        <el-descriptions-item label="方向">{{ verifyTarget?.direction === 'COLLECTION' ? '收款' : '退款' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ fen2yuan(verifyTarget?.amount) }}</el-descriptions-item>
        <el-descriptions-item label="凭证号">{{ verifyTarget?.externalReferenceMask }}</el-descriptions-item>
        <el-descriptions-item label="登记人ID">{{ verifyTarget?.registeredBy }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px">
        <el-form-item label="结论" required>
          <el-radio-group v-model="verifyForm.decision">
            <el-radio value="VERIFY">核实通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="verifyForm.reason" type="textarea" :rows="3"
                    placeholder="例如：已核对银行流水与收据，金额一致" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyDialog = false">取消</el-button>
        <el-button type="primary" :loading="verifying" @click="doVerify">提交</el-button>
      </template>
    </el-dialog>

    <!-- ============================== 详情 -->
    <el-drawer v-model="detailDrawer" title="线下资金凭证详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="凭证ID">{{ detail.proofId }}</el-descriptions-item>
        <el-descriptions-item label="方向">{{ detail.direction === 'COLLECTION' ? '收款' : '退款' }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ detail.orderNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="退款单号">{{ detail.refundNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ fen2yuan(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="方式">{{ detail.method === 'CASH' ? '现金' : '银行转账' }}</el-descriptions-item>
        <el-descriptions-item label="凭证号">{{ detail.externalReferenceMask }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ proofLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="容量处理">{{ detail.capacityResolution }}</el-descriptions-item>
        <el-descriptions-item label="登记人">{{ detail.registeredBy }} · {{ detail.registeredAt }}</el-descriptions-item>
        <el-descriptions-item label="核实人">
          {{ detail.verifiedBy ? `${detail.verifiedBy} · ${detail.verifiedAt}` : '未核实' }}
        </el-descriptions-item>
        <el-descriptions-item label="核实原因">{{ detail.verifyReason || '—' }}</el-descriptions-item>
        <el-descriptions-item label="登记说明">{{ detail.registerNote }}</el-descriptions-item>
        <el-descriptions-item label="凭证附件">
          <span v-if="!detail.voucherFiles?.length">无</span>
          <div v-for="f in detail.voucherFiles" :key="f">{{ f }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getOfflinePayment, listOfflinePayments, registerOfflinePayment, verifyOfflinePayment
} from '@/api/rongli/integration'

const proofStatuses = ['REGISTERED', 'VERIFIED', 'REJECTED']

const rows = ref<any[]>([])
const loading = ref(false)
const filter = reactive({ status: '', direction: '' })
const page = reactive({ pageNum: 1, pageSize: 20, total: 0 })

function fen2yuan(fen: number | undefined | null) {
  if (fen === null || fen === undefined) return '—'
  return `¥${(Number(fen) / 100).toFixed(2)}`
}

function proofLabel(status: string) {
  if (status === 'REGISTERED') return '待核实'
  if (status === 'VERIFIED') return '已核实'
  if (status === 'REJECTED') return '已驳回'
  return status || '—'
}

function proofTag(status: string) {
  if (status === 'VERIFIED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

async function load() {
  loading.value = true
  try {
    const res = await listOfflinePayments({
      status: filter.status || undefined,
      direction: filter.direction || undefined,
      pageNum: page.pageNum,
      pageSize: page.pageSize
    })
    rows.value = res.rows || []
    page.total = res.total || 0
  } finally {
    loading.value = false
  }
}

/* ---------------- 登记 ---------------- */
const registerDialog = ref(false)
const saving = ref(false)
const form = reactive({
  direction: 'COLLECTION', orderNo: '', refundNo: '', method: 'CASH',
  amountYuan: '', paidAt: '', externalReference: '', note: '', voucherFileIds: ''
})

function openRegister() {
  form.direction = 'COLLECTION'
  form.orderNo = ''
  form.refundNo = ''
  form.method = 'CASH'
  form.amountYuan = ''
  form.paidAt = ''
  form.externalReference = ''
  form.note = ''
  form.voucherFileIds = ''
  registerDialog.value = true
}

async function doRegister() {
  const yuan = Number(form.amountYuan)
  if (!Number.isFinite(yuan) || yuan <= 0) {
    ElMessage.warning('请填写正确的实际金额')
    return
  }
  saving.value = true
  try {
    const ids = form.voucherFileIds.split(',').map((s) => s.trim()).filter(Boolean)
    await registerOfflinePayment({
      direction: form.direction,
      orderNo: form.orderNo,
      refundNo: form.direction === 'REFUND' ? form.refundNo : undefined,
      method: form.method,
      actualAmount: Math.round(yuan * 100),
      paidAt: form.paidAt || undefined,
      externalReference: form.externalReference,
      note: form.note,
      voucherFileIds: ids
    })
    ElMessage.success('已登记，等待另一位有权人员核实')
    registerDialog.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '登记失败')
  } finally {
    saving.value = false
  }
}

/* ---------------- 核实 ---------------- */
const verifyDialog = ref(false)
const verifying = ref(false)
const verifyTarget = ref<any>(null)
const verifyForm = reactive({ decision: 'VERIFY', reason: '' })

function openVerify(row: any) {
  verifyTarget.value = row
  verifyForm.decision = 'VERIFY'
  verifyForm.reason = ''
  verifyDialog.value = true
}

async function doVerify() {
  if (verifyForm.reason.trim().length < 2) {
    ElMessage.warning('请填写核实/驳回原因')
    return
  }
  verifying.value = true
  try {
    const res = await verifyOfflinePayment(verifyTarget.value.proofId, {
      expectedVersion: verifyTarget.value.version,
      decision: verifyForm.decision,
      reason: verifyForm.reason.trim()
    })
    ElMessage.success(res.result || '已处理')
    verifyDialog.value = false
    await load()
  } catch (e: any) {
    // 后端会明确拒绝“登记人核实自己”的情况，这里原样提示，不做前端兜底放行
    ElMessage.error(e?.message || '核实失败')
  } finally {
    verifying.value = false
  }
}

/* ---------------- 详情 ---------------- */
const detailDrawer = ref(false)
const detail = ref<any>(null)

async function openDetail(row: any) {
  detail.value = await getOfflinePayment(row.proofId)
  detailDrawer.value = true
}

onMounted(load)
</script>

<style scoped>
.intro { margin-bottom: 12px; }
.title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.hint { font-size: 12px; line-height: 1.7; color: var(--el-text-color-secondary); }
.bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>
