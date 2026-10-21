<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb12">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">进行中订单</div>
          <div class="stat-value">{{ num(overview.active_orders) }}</div>
          <div class="stat-hint">未完成、未取消</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日新增预约</div>
          <div class="stat-value">{{ num(overview.orders_today) }}</div>
          <div class="stat-hint">按自然日（Asia/Shanghai）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">待审售后</div>
          <div class="stat-value warn">{{ num(overview.aftersale_pending) }}</div>
          <div class="stat-hint">待运营一次性审核</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">GMV（未取消）</div>
          <div class="stat-value">{{ yuan(demand.gmv_fen) }}</div>
          <div class="stat-hint">订单应收金额合计</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb12">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>需要处理</span>
              <el-button icon="Refresh" text @click="load">刷新</el-button>
            </div>
          </template>
          <div class="todo-grid">
            <div class="todo" @click="go('/rongli/dispatch')">
              <div class="todo-num">{{ num(demand.dispatching) }}</div>
              <div class="todo-label">待派单/派单中</div>
            </div>
            <div class="todo" @click="go('/rongli/aftersale')">
              <div class="todo-num">{{ num(overview.aftersale_pending) }}</div>
              <div class="todo-label">售后待审</div>
            </div>
            <div class="todo" @click="go('/rongli/exception')">
              <div class="todo-num">{{ num(overview.dispute_open) }}</div>
              <div class="todo-label">争议未结</div>
            </div>
            <div class="todo" @click="go('/rongli/exception')">
              <div class="todo-num">{{ num(overview.exception_open) }}</div>
              <div class="todo-label">服务异常</div>
            </div>
            <div class="todo" @click="go('/rongli/exception')">
              <div class="todo-num">{{ num(overview.correction_pending) }}</div>
              <div class="todo-label">结果更正</div>
            </div>
            <div class="todo" @click="go('/rongli/penalty')">
              <div class="todo-num">{{ num(overview.appeal_pending) }}</div>
              <div class="todo-label">申诉待处理</div>
            </div>
            <div class="todo" @click="go('/rongli/qualification')">
              <div class="todo-num">{{ num(overview.qualification_pending) }}</div>
              <div class="todo-label">资格待审</div>
            </div>
            <div class="todo" @click="go('/rongli/order')">
              <div class="todo-num">{{ num(demand.wait_confirm) }}</div>
              <div class="todo-label">待车主确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>履约分布</span>
              <el-radio-group v-model="days" size="small" @change="load">
                <el-radio-button :value="7">近 7 天</el-radio-button>
                <el-radio-button :value="14">近 14 天</el-radio-button>
                <el-radio-button :value="30">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="在途取车">{{ num(demand.on_the_way) }}</el-descriptions-item>
            <el-descriptions-item label="服务中">{{ num(demand.in_service) }}</el-descriptions-item>
            <el-descriptions-item label="已检测">{{ num(demand.inspected) }}</el-descriptions-item>
            <el-descriptions-item label="返程中">{{ num(demand.returning) }}</el-descriptions-item>
            <el-descriptions-item label="待确认">{{ num(demand.wait_confirm) }}</el-descriptions-item>
            <el-descriptions-item label="已完成">{{ num(demand.completed) }}</el-descriptions-item>
            <el-descriptions-item label="已取消">{{ num(demand.cancelled) }}</el-descriptions-item>
            <el-descriptions-item label="累计订单">{{ num(demand.total) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mb12">
      <template #header>
        <div class="card-head">
          <span>下单与金额趋势（近 {{ rangeDays }} 天）</span>
          <el-tag type="info" effect="plain">{{ rangeText }}</el-tag>
        </div>
      </template>
      <div ref="chartRef" class="chart"></div>
    </el-card>

    <el-card shadow="never">
      <template #header>逐日明细</template>
      <el-table :data="daily" border size="small" max-height="360">
        <el-table-column label="日期" prop="day" width="140" />
        <el-table-column label="下单量" prop="orders" width="120" align="right" />
        <el-table-column label="应收金额" width="160" align="right">
          <template #default="{ row }">{{ yuan(row.amount_fen) }}</template>
        </el-table-column>
      </el-table>
      <el-alert class="mt12" type="info" :closable="false" show-icon>
        <template #title>
          <div v-for="(d, i) in definitions" :key="i">{{ d }}</div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup name="RongliDashboard" lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getStatistics } from '@/api/rongli'

const router = useRouter()
const days = ref(14)
const rangeDays = ref(14)
const rangeText = ref('')
const overview = ref<any>({})
const demand = ref<any>({})
const daily = ref<any[]>([])
const definitions = ref<string[]>([])
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})

watch(days, () => load())

async function load() {
  const res: any = await getStatistics({ days: days.value })
  overview.value = (res && res.overview) || {}
  demand.value = (res && res.dashboard) || {}
  daily.value = (res && res.daily) || []
  definitions.value = (res && res.definitions) || []
  rangeDays.value = Number((res && res.rangeDays) || days.value)
  rangeText.value = String((res && res.from) || '').slice(0, 10)
  await nextTick()
  render()
}

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const labels = daily.value.map((d) => d.day)
  const orders = daily.value.map((d) => Number(d.orders || 0))
  const amounts = daily.value.map((d) => Number(d.amount_fen || 0) / 100)
  chart.setOption(
    {
      tooltip: { trigger: 'axis' },
      legend: { data: ['下单量', '应收金额(元)'] },
      grid: { left: 48, right: 56, top: 40, bottom: 32 },
      xAxis: { type: 'category', data: labels },
      yAxis: [
        { type: 'value', name: '单' },
        { type: 'value', name: '元' }
      ],
      series: [
        { name: '下单量', type: 'bar', data: orders, barMaxWidth: 26, itemStyle: { color: '#1f6feb' } },
        { name: '应收金额(元)', type: 'line', yAxisIndex: 1, smooth: true, data: amounts, itemStyle: { color: '#e5484d' } }
      ]
    },
    true
  )
  chart.resize()
}

function resize() {
  if (chart) chart.resize()
}

function go(path: string) {
  router.push(path)
}

function num(v: any) {
  return Number(v || 0)
}

function yuan(v: any) {
  return '¥' + (Number(v || 0) / 100).toFixed(2)
}
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.mt12 { margin-top: 12px; }
.stat-card { border-left: 3px solid #1f6feb; }
.stat-label { color: #6b7280; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 600; margin-top: 6px; }
.stat-value.warn { color: #e6a23c; }
.stat-hint { color: #9aa1ac; font-size: 12px; margin-top: 4px; }
.card-head { display: flex; align-items: center; justify-content: space-between; }
.todo-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.todo { border: 1px solid #edeff3; border-radius: 8px; padding: 12px; text-align: center; cursor: pointer; transition: all 0.15s; }
.todo:hover { border-color: #1f6feb; background: #f5f9ff; }
.todo-num { font-size: 22px; font-weight: 600; color: #1f6feb; }
.todo-label { color: #6b7280; font-size: 12px; margin-top: 4px; }
.chart { height: 300px; width: 100%; }
</style>
