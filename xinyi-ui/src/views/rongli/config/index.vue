<template>
  <RongliPage
    title="系统配置"
    subtitle="预约派单分钟数可配；付款有效期固定30分钟，安全约束不能随意关闭。"
  >
    <template #actions>
      <el-button @click="openPlan">预览已有预约重算</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存参数版本</el-button>
      <el-button @click="router.push(RL.audit)">查看变更审计</el-button>
    </template>

    <div class="rl-grid rl-grid--2">
      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>派单参数</h3>
          <span class="rl-muted">只影响新订单</span>
        </div>
        <div class="rl-panel__body">
          <el-form label-position="top" class="rl-form-grid">
            <el-form-item label="上门开始前 N 分钟派单">
              <el-input v-model="form.leadMinutes" type="number" />
            </el-form-item>
            <el-form-item label="单次邀约响应秒数">
              <el-input v-model="form.inviteExpireSeconds" type="number" />
            </el-form-item>
            <el-form-item label="初始半径（km）">
              <el-input v-model="form.radiusKm" type="number" step="0.1" />
            </el-form-item>
            <el-form-item label="扩圈半径（km，逗号分隔）">
              <el-input v-model="form.radiusStepsKm" placeholder="例如 8, 12, 20" />
            </el-form-item>
            <el-form-item label="自动安排总截止（分钟）">
              <el-input v-model="form.totalMinutes" type="number" />
            </el-form-item>
            <el-form-item label="参数作用范围">
              <el-input model-value="只影响新订单" disabled />
            </el-form-item>
          </el-form>
          <p class="rl-hint rl-hint--warn">
            派单顺序和停单一轮定义按 P01 单独评审，不用前端参数替代产品决定。
          </p>
        </div>
      </section>

      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>固定业务规则</h3>
        </div>
        <div class="rl-panel__body">
          <el-form label-position="top" class="rl-form-grid">
            <el-form-item label="待支付有效期（分钟）">
              <el-input :model-value="effective['rongli.pay.expireMinutes'] ?? 30" disabled />
            </el-form-item>
            <el-form-item label="实际归还后服务确认（小时）">
              <el-input :model-value="effective['rongli.confirm.autoHours'] ?? 24" disabled />
            </el-form-item>
            <el-form-item label="定位有效期（秒）">
              <el-input v-model="form.locationFreshSeconds" type="number" />
            </el-form-item>
            <el-form-item label="服务条款版本">
              <el-input v-model="form.termsVersion" />
            </el-form-item>
          </el-form>
          <div class="rl-kv">
            <div><span>司机释放</span><b>实际归还交接完成</b></div>
            <div><span>证据水印 / 脱敏</span><b>强制，不向普通运营开放关闭</b></div>
            <div>
              <span>支付通道</span>
              <b>
                <span class="rl-tag rl-tag--plain" :class="payChannel === 'MOCK' ? 'rl-tag--amber' : 'rl-tag--green'">
                  {{ payChannel }}
                </span>
              </b>
            </div>
            <div><span>交接互认类型</span><b>{{ effective['rongli.handover.mutualConfirmTypes'] || '—' }}</b></div>
          </div>
          <p class="rl-hint">{{ data.hint }}</p>
        </div>
      </section>
    </div>

    <section class="rl-panel">
      <div class="rl-panel__head">
        <h3>已有预约的计划</h3>
        <span class="rl-muted">新参数默认不改旧计划</span>
      </div>
      <div class="rl-panel__body">
        <p class="rl-hint">
          新参数默认不改旧计划。重算未派单预约前，需查看受影响订单并明确确认。
        </p>
        <div class="mt12">
          <el-button @click="openPlan">预览已有预约重算</el-button>
        </div>
      </div>
    </section>

    <!-- 重算预览 -->
    <el-drawer v-model="planVisible" title="重算未派单预约计划" size="620px">
      <p class="rl-hint rl-hint--warn">
        只处理仍未派单的预约。不重派已接受邀约或持有车辆的承接任务。
      </p>
      <div class="rl-kv mt12">
        <div><span>新提前量</span><b>{{ form.leadMinutes }} 分钟</b></div>
        <div><span>符合条件</span><b>{{ plan.affectedCount ?? 0 }} 条</b></div>
      </div>
      <el-table :data="plan.rows || []" class="mt12" :border="false">
        <el-table-column label="订单" prop="orderNo" min-width="150">
          <template #default="{ row }"><span class="rl-mono">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column label="原计划" width="150">
          <template #default="{ row }">{{ shortTime(row.currentDispatchAt) }}</template>
        </el-table-column>
        <el-table-column label="新计划" width="150">
          <template #default="{ row }">{{ shortTime(row.nextDispatchAt) }}</template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">没有需要重算的预约</div>
            <div class="rl-empty__hint">已派单或已被承接的订单不进入更新范围。</div>
          </div>
        </template>
      </el-table>
      <el-form label-width="80px" class="mt12">
        <el-form-item label="重算原因">
          <el-input v-model="planReason" type="textarea" :rows="3" placeholder="说明本次调整业务参数的原因" />
        </el-form-item>
        <el-form-item label=" ">
          <el-checkbox v-model="planConfirmed">已确认只更新未派单预约</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!planConfirmed || !plan.affectedCount" :loading="applying" @click="applyPlan">
          确认更新计划
        </el-button>
      </template>
    </el-drawer>
  </RongliPage>
</template>

<script setup name="RongliConfig" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  applyDispatchPlan,
  getBusinessConfig,
  previewDispatchPlan,
  saveBusinessConfig
} from '@/api/rongli'
import { shortTime } from '../format'
import { RL } from '../routes'

const router = useRouter()

const data = ref<any>({})
const effective = computed<Record<string, any>>(() => data.value.effective || {})
const saving = ref(false)
const applying = ref(false)

const form = reactive({
  leadMinutes: '',
  inviteExpireSeconds: '',
  radiusKm: '',
  radiusStepsKm: '',
  totalMinutes: '',
  locationFreshSeconds: '',
  termsVersion: ''
})

const plan = ref<any>({})
const planVisible = ref(false)
const planReason = ref('')
const planConfirmed = ref(false)

const payChannel = computed(() => effective.value['rongli.payment.channel'] || 'MOCK')

onMounted(load)

async function load() {
  data.value = await getBusinessConfig()
  const e = effective.value
  form.leadMinutes = String(e['rongli.dispatch.leadMinutes'] ?? '')
  form.inviteExpireSeconds = String(e['rongli.invite.expireSeconds'] ?? '')
  form.radiusKm = metersToKm(e['rongli.dispatch.radiusMeter'])
  form.radiusStepsKm = String(e['rongli.dispatch.radiusSteps'] || '')
    .split(/[,;\s]+/)
    .filter(Boolean)
    .map((v) => metersToKm(v))
    .join(', ')
  form.totalMinutes = String(e['rongli.dispatch.totalMinutes'] ?? '')
  form.locationFreshSeconds = String(e['rongli.driver.locationFreshSeconds'] ?? '')
  form.termsVersion = String(e['rongli.service.termsVersion'] ?? '')
}

function metersToKm(v: any) {
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? String(+(n / 1000).toFixed(2)) : ''
}

function kmToMeters(v: string) {
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? String(Math.round(n * 1000)) : ''
}

async function save() {
  saving.value = true
  try {
    await saveBusinessConfig({
      leadMinutes: form.leadMinutes,
      inviteExpireSeconds: form.inviteExpireSeconds,
      radiusMeter: kmToMeters(form.radiusKm),
      radiusSteps: String(form.radiusStepsKm || '')
        .split(/[,;\s]+/)
        .filter(Boolean)
        .map((v) => kmToMeters(v))
        .filter(Boolean)
        .join(','),
      totalMinutes: form.totalMinutes,
      locationFreshSeconds: form.locationFreshSeconds,
      termsVersion: form.termsVersion
    })
    ElMessage.success('已保存，仅影响新订单')
    await load()
  } finally {
    saving.value = false
  }
}

async function openPlan() {
  planConfirmed.value = false
  planReason.value = ''
  plan.value = (await previewDispatchPlan({ leadMinutes: Number(form.leadMinutes) })) || {}
  planVisible.value = true
}

async function applyPlan() {
  if (!planReason.value.trim()) {
    ElMessage.warning('请填写重算原因')
    return
  }
  applying.value = true
  try {
    const res = await applyDispatchPlan(plan.value.planId, Number(form.leadMinutes))
    ElMessage.success(`已更新 ${res?.changedCount ?? 0} 条计划，跳过 ${res?.skippedCount ?? 0} 条`)
    planVisible.value = false
  } finally {
    applying.value = false
  }
}
</script>
