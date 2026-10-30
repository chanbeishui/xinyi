<template>
  <RongliPage title="业务统计" subtitle="区分订单完成与司机任务结束、上门准时与到站延误、成功收款与退款。">
    <div class="page-head">
      <div>
        <h2 class="page-title">业务统计</h2>
        <p class="page-desc">查看预约、履约及资金核对统计。</p>
      </div>
      <div class="head-actions">
        <span class="range-label">统计范围</span>
        <el-select v-model="range" style="width: 96px" @change="load">
          <el-option label="日" value="DAY" />
          <el-option label="周" value="WEEK" />
          <el-option label="月" value="MONTH" />
        </el-select>
        <el-button :loading="exporting" @click="applyExport">申请导出</el-button>
      </div>
    </div>

    <el-row :gutter="12" class="cards">
      <el-col v-for="card in cards" :key="card.key" :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-hint">{{ card.hint }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mid">
      <el-col :xs="24" :md="16">
        <div class="panel">
          <div class="panel-title">近7日订单量</div>
          <div ref="chartRef" class="chart"></div>
          <p class="chart-note">{{ chartNote }}</p>
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="panel metric-panel">
          <div class="panel-title">指标口径</div>
          <div v-for="m in metrics" :key="m.label" class="metric-row">
            <span class="metric-label">{{ m.label }}</span>
            <span class="metric-value">{{ m.value }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="panel">
      <div class="panel-title">按站点服务概况</div>
      <el-table v-loading="loading" :data="stationPage" border size="large">
        <el-table-column label="检测点" prop="stationName" min-width="180" />
        <el-table-column label="预约" prop="orders" width="110" align="center" />
        <el-table-column label="已付款" prop="paid" width="110" align="center" />
        <el-table-column label="未付款" prop="unpaid" width="110" align="center" />
        <el-table-column label="在手车辆" prop="inHand" width="120" align="center" />
        <el-table-column label="已归还" prop="returned" width="110" align="center" />
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="stationStats.length"
        :page-sizes="[6, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="pager"
      />
    </div>
  </RongliPage>
</template>

<script setup name="RongliStatistics" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatistics, requestExport } from '@/api/rongli'

const loading = ref(false)
const exporting = ref(false)
const range = ref('DAY')
const cards = ref<any[]>([])
const metrics = ref<any[]>([])
const stationStats = ref<any[]>([])
const chartNote = ref('')
const rangeFrom = ref('')
const rangeDays = ref(7)

const query = reactive<any>({ pageNum: 1, pageSize: 6 })
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null
let last7: any[] = []

const stationPage = computed(() => {
  const start = (query.pageNum - 1) * query.pageSize
  return stationStats.value.slice(start, start + query.pageSize)
})

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

function resize() {
  if (chart) chart.resize()
}

async function load() {
  loading.value = true
  try {
    const res: any = await getStatistics({ range: range.value })
    cards.value = (res && res.cards) || []
    metrics.value = (res && res.metricDefinitions) || []
    stationStats.value = (res && res.stationStats) || []
    chartNote.value = (res && res.chartNote) || ''
    last7 = (res && res.last7) || []
    rangeFrom.value = String((res && res.from) || '').slice(0, 10)
    rangeDays.value = Number((res && res.rangeDays) || 7)
    query.pageNum = 1
    await nextTick()
    render()
  } finally {
    loading.value = false
  }
}

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: 42, right: 20, top: 24, bottom: 28 },
      xAxis: {
        type: 'category',
        data: last7.map((d) => d.label),
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#f0f2f5' } },
        axisLabel: { color: '#909399' }
      },
      series: [
        {
          name: '下单量',
          type: 'bar',
          barMaxWidth: 34,
          data: last7.map((d) => Number(d.orders || 0)),
          label: { show: true, position: 'top', color: '#606266' },
          itemStyle: { color: '#2f6bff', borderRadius: [4, 4, 0, 0] }
        }
      ]
    },
    true
  )
  chart.resize()
}

async function applyExport() {
  const to = new Date().toISOString().slice(0, 10)
  const { value } = await ElMessageBox.prompt(
    `将按当前统计范围（近 ${rangeDays.value} 天）申请脱敏导出，请填写用途以便审批。`,
    '申请导出',
    { inputPlaceholder: '如：月度经营分析（不少于 4 个字）', inputValidator: (v: string) => (v && v.trim().length >= 4) || '请填写导出用途' }
  )
  exporting.value = true
  try {
    await requestExport({ from: rangeFrom.value || to, to, reason: value })
    ElMessage.success('导出申请已提交，审批通过后可在「审计与访问 → 治理与导出」生成')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.rl-stats { padding-bottom: 24px; }
.page-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 16px; }
.page-title { margin: 0 0 6px; font-size: 22px; font-weight: 600; }
.page-desc { margin: 0; color: #909399; font-size: 13px; }
.head-actions { display: flex; align-items: center; gap: 8px; }
.range-label { color: #606266; font-size: 13px; }
.cards { margin-bottom: 12px; }
.stat-card { border: 1px solid #ebeef5; border-radius: 10px; padding: 16px 18px; background: #fff; min-height: 108px; }
.stat-label { color: #909399; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 600; margin: 8px 0 4px; color: #1f2d3d; }
.stat-hint { color: #a8abb2; font-size: 12px; }
.mid { margin-bottom: 12px; }
.panel { border: 1px solid #ebeef5; border-radius: 10px; background: #fff; padding: 16px 18px 12px; }
.panel-title { font-size: 15px; font-weight: 600; color: #1f2d3d; margin-bottom: 12px; }
.chart { width: 100%; height: 260px; }
.chart-note { color: #a8abb2; font-size: 12px; margin: 6px 0 0; }
.metric-panel { min-height: 100%; }
.metric-row { display: flex; align-items: center; justify-content: space-between; padding: 11px 0; border-bottom: 1px dashed #f0f2f5; font-size: 13px; }
.metric-row:last-child { border-bottom: none; }
.metric-label { color: #606266; }
.metric-value { color: #1f2d3d; font-weight: 500; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
