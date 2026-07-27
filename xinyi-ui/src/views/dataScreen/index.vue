<template>
  <div class="data-screen" ref="screenRef" :class="{ 'is-fullscreen': isFullscreen }">
    <!-- 顶部标题栏（含数据区间） -->
    <div class="ds-header">
      <div class="ds-header-top">
        <div class="ds-header-left">
          <span class="ds-header-date">{{ currentDate }}</span>
        </div>
        <div class="ds-header-center">
          <h1>集信国控经营分析数据大屏</h1>
          <p>以建筑工程检测为核心 · 具备公路水运 / 水利工程 / 食品 / 汽车检测等检测资质 — 全维度监控</p>
        </div>
        <div class="ds-header-right">
          <span class="ds-header-org">广东集信国控检测认证技术服务中心股份有限公司</span>
          <el-button class="ds-fullscreen-btn" size="small" @click="toggleFullscreen">
            <svg-icon :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" style="margin-right: 4px;" />
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
        </div>
      </div>
      <div class="ds-header-datebar">
        <div class="ds-header-datebar-left">
          <span class="ds-header-datebar-label">数据区间</span>
          <el-radio-group v-model="dateRangeType" size="small" @change="handleDateTypeChange">
            <el-radio-button value="yesterday">昨日</el-radio-button>
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-if="dateRangeType === 'custom'"
            v-model="customDateRange"
            type="daterange"
            size="small"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="ds-header-datepicker"
            @change="handleCustomDateChange"
          />
        </div>
        <div class="ds-header-datebar-right">
          当前显示：<strong>{{ dateRangeText }}</strong>
        </div>
      </div>
    </div>

    <!-- KPI 指标行 -->
    <div class="ds-kpi-row">
      <div v-for="kpi in kpiCards" :key="kpi.label" class="ds-kpi-card" :class="kpi.color">
        <div class="ds-kpi-label">{{ kpi.label }}</div>
        <div class="ds-kpi-value">{{ kpi.value }}<span class="ds-kpi-unit">{{ kpi.unit }}</span></div>
        <div class="ds-kpi-trend" :class="kpi.trendType">{{ kpi.trend }}</div>
      </div>
    </div>

    <!-- 第一行：趋势 + 行业分布 -->
    <div class="ds-chart-grid">
      <div class="ds-chart-card ds-col-8">
        <div class="ds-chart-title"><span class="dot" style="background:#1677ff"></span>检测报告数量 & 营收趋势</div>
        <div class="ds-chart-subtitle">月度报告出具量与营收（2026年1-7月）— 建筑工程占比 62%</div>
        <div ref="trendChartRef" class="ds-chart-wrap" style="height: 280px;"></div>
      </div>
      <div class="ds-chart-card ds-col-4">
        <div class="ds-chart-title"><span class="dot" style="background:#722ed1"></span>检测业务领域分布</div>
        <div class="ds-chart-subtitle">建筑工程为主导，多领域协同</div>
        <div ref="industryChartRef" class="ds-chart-wrap" style="height: 280px;"></div>
      </div>
    </div>

    <!-- 第二行：质量 + 项目分布 -->
    <div class="ds-chart-grid">
      <div class="ds-chart-card ds-col-6">
        <div class="ds-chart-title"><span class="dot" style="background:#52c41a"></span>建筑工程各检测类别合格率</div>
        <div class="ds-chart-subtitle">合格 vs 不合格 对比（按建工检测子类）</div>
        <div ref="qualityChartRef" class="ds-chart-wrap" style="height: 270px;"></div>
      </div>
      <div class="ds-chart-card ds-col-6">
        <div class="ds-chart-title"><span class="dot" style="background:#1677ff"></span>建筑工程检测项目分布</div>
        <div class="ds-chart-subtitle">各检测项目报告数量占比</div>
        <div ref="constructionChartRef" class="ds-chart-wrap" style="height: 270px;"></div>
      </div>
    </div>

    <!-- 第三行：设备利用率 + 检测周期 -->
    <div class="ds-chart-grid">
      <div class="ds-chart-card ds-col-8">
        <div class="ds-chart-title"><span class="dot" style="background:#ff4d4f"></span>仪器设备利用率监控</div>
        <div class="ds-chart-subtitle">建筑工程主要检测设备运行状态 & 利用率</div>
        <div class="ds-equip-table-wrap">
          <table class="ds-equip-table">
            <thead>
              <tr>
                <th style="width:28%">设备名称</th>
                <th style="width:14%">型号</th>
                <th style="width:8%">数量</th>
                <th style="width:32%">利用率</th>
                <th style="width:10%">状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="equip in equipmentData" :key="equip.name">
                <td>{{ equip.name }}</td>
                <td>{{ equip.model }}</td>
                <td>{{ equip.count }}</td>
                <td>
                  <div class="ds-progress-bar">
                    <div class="ds-progress-fill" :style="{ width: equip.rate + '%', background: equip.barColor }"></div>
                  </div>
                </td>
                <td>
                  <span class="ds-equip-status" :class="equip.statusType">{{ equip.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="ds-chart-card ds-col-4">
        <div class="ds-chart-title"><span class="dot" style="background:#faad14"></span>检测周期各环节耗时</div>
        <div class="ds-chart-subtitle">平均耗时（小时）</div>
        <div ref="cycleChartRef" class="ds-chart-wrap" style="height: 210px;"></div>
        <div class="ds-cycle-compare">
          <div class="ds-section-label">各领域平均周期对比</div>
          <div v-for="item in cycleCompareData" :key="item.label" class="ds-progress-row">
            <div class="ds-progress-header">
              <span class="label">{{ item.label }}</span>
              <span class="value">{{ item.value }}</span>
            </div>
            <div class="ds-progress-bar">
              <div class="ds-progress-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 第四行：TOP客户 + 人员负荷 + 营收构成 -->
    <div class="ds-chart-grid">
      <div class="ds-chart-card ds-col-4">
        <div class="ds-chart-title"><span class="dot" style="background:#13c2c2"></span>TOP 8 客户检测量</div>
        <div class="ds-chart-subtitle">按报告数量排名（建筑工程客户为主）</div>
        <div ref="customerChartRef" class="ds-chart-wrap" style="height: 310px;"></div>
      </div>
      <div class="ds-chart-card ds-col-4">
        <div class="ds-chart-title"><span class="dot" style="background:#1677ff"></span>检测人员负荷分布</div>
        <div class="ds-chart-subtitle">各检测组月度任务量对比</div>
        <div ref="personnelChartRef" class="ds-chart-wrap" style="height: 310px;"></div>
      </div>
      <div class="ds-chart-card ds-col-4">
        <div class="ds-chart-title"><span class="dot" style="background:#722ed1"></span>营收构成分析</div>
        <div class="ds-chart-subtitle">按业务领域拆分月度营收（万元）</div>
        <div ref="revenueChartRef" class="ds-chart-wrap" style="height: 310px;"></div>
      </div>
    </div>

    <!-- 第五行：近期报告 + 风险预警 -->
    <div class="ds-chart-grid">
      <div class="ds-chart-card ds-col-6">
        <div class="ds-chart-title"><span class="dot" style="background:#13c2c2"></span>近期重点检测报告</div>
        <div class="ds-chart-subtitle">最新出具的关键报告状态</div>
        <table class="ds-data-table">
          <thead>
            <tr>
              <th>报告编号</th>
              <th>客户名称</th>
              <th>检测类型</th>
              <th>结果</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="report in reportData" :key="report.id">
              <td>{{ report.id }}</td>
              <td>{{ report.customer }}</td>
              <td>{{ report.type }}</td>
              <td><span class="ds-badge" :class="report.resultType">{{ report.result }}</span></td>
              <td><span class="ds-badge" :class="report.statusType">{{ report.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="ds-chart-card ds-col-6">
        <div class="ds-chart-title"><span class="dot" style="background:#ff4d4f"></span>质量风险预警</div>
        <div class="ds-chart-subtitle">不合格率异常监控 & 超期未出报告</div>
        <div class="ds-risk-section">
          <div class="ds-section-label">不合格率 TOP 类别</div>
          <div v-for="risk in riskData" :key="risk.label" class="ds-progress-row">
            <div class="ds-progress-header">
              <span class="label">{{ risk.label }}</span>
              <span class="value" :style="{ color: risk.color }">{{ risk.value }}</span>
            </div>
            <div class="ds-progress-bar">
              <div class="ds-progress-fill" :style="{ width: risk.percent + '%', background: risk.color }"></div>
            </div>
          </div>
        </div>
        <div class="ds-risk-section" style="margin-top: 14px;">
          <div class="ds-section-label">超期未出报告</div>
          <table class="ds-data-table">
            <thead>
              <tr>
                <th>报告编号</th>
                <th>客户</th>
                <th>检测类型</th>
                <th>超期</th>
                <th>责任人</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="overdue in overdueData" :key="overdue.id">
                <td>{{ overdue.id }}</td>
                <td>{{ overdue.customer }}</td>
                <td>{{ overdue.type }}</td>
                <td><span class="ds-badge" :class="overdue.overdueType">{{ overdue.overdue }}</span></td>
                <td>{{ overdue.owner }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="ds-footer">
      建筑工程检测数据大屏 · 数据为模拟示例 · 生成时间 2026-07-27
    </div>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()

// ========== 全屏控制 ==========
const screenRef = ref<HTMLElement>()
const isFullscreen = ref(false)

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    screenRef.value?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})
onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})

// ========== 当前日期 ==========
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// ========== 数据区间选择 ==========
const dateRangeType = ref<'yesterday' | 'week' | 'month' | 'custom'>('custom')
const customDateRange = ref<[string, string]>(['2026-01-01', '2026-06-30'])

const dateRangeText = computed(() => {
  const fmt = (d: Date) => `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  if (dateRangeType.value === 'yesterday') {
    const d = new Date()
    d.setDate(d.getDate() - 1)
    return `昨日（${fmt(d)}）`
  }
  if (dateRangeType.value === 'week') {
    const now = new Date()
    const day = now.getDay() || 7
    const monday = new Date(now)
    monday.setDate(now.getDate() - day + 1)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    return `本周（${fmt(monday)} - ${fmt(sunday)}）`
  }
  if (dateRangeType.value === 'month') {
    const now = new Date()
    const first = new Date(now.getFullYear(), now.getMonth(), 1)
    const last = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    return `本月（${fmt(first)} - ${fmt(last)}）`
  }
  // custom
  if (customDateRange.value && customDateRange.value.length === 2) {
    const [start, end] = customDateRange.value
    const sd = new Date(start)
    const ed = new Date(end)
    return `自定义时间段：${fmt(sd)} - ${fmt(ed)}`
  }
  return '未选择'
})

function handleDateTypeChange() {
  // 切换区间类型时的回调（数据为静态，仅更新展示文案）
}

function handleCustomDateChange() {
  // 自定义日期变更回调
}

// ========== 静态数据 ==========
const kpiCards = [
  { label: '检测报告总数', value: '12,683', unit: '份', trend: '↑ 9.6% 环比上月', trendType: 'trend-up', color: 'blue' },
  { label: '综合合格率', value: '95.8', unit: '%', trend: '↑ 0.8% 环比上月', trendType: 'trend-up', color: 'green' },
  { label: '检测营收', value: '¥4,256', unit: '万', trend: '↑ 11.2% 环比上月', trendType: 'trend-up', color: 'orange' },
  { label: '平均检测周期', value: '3.8', unit: '天', trend: '↓ 0.4天 缩短', trendType: 'trend-down', color: 'purple' },
  { label: '在检客户数', value: '632', unit: '家', trend: '↑ 18家 新增', trendType: 'trend-up', color: 'cyan' },
  { label: '设备利用率', value: '84.6', unit: '%', trend: '↓ 1.5% 环比上月', trendType: 'trend-down', color: 'red' }
]

const equipmentData = [
  { name: '万能材料试验机', model: 'WDW-1000E', count: '3台', rate: 94, barColor: '#52c41a', status: '正常', statusType: 'status-green' },
  { name: '混凝土压力试验机', model: 'YA-2000C', count: '4台', rate: 91, barColor: '#52c41a', status: '正常', statusType: 'status-green' },
  { name: '水泥胶砂强度试验机', model: 'DKZ-5000', count: '2台', rate: 88, barColor: '#52c41a', status: '正常', statusType: 'status-green' },
  { name: '非金属超声波检测仪', model: 'NM-4B', count: '2台', rate: 85, barColor: '#52c41a', status: '正常', statusType: 'status-green' },
  { name: '恒温恒湿养护箱', model: 'HBY-40B', count: '6台', rate: 83, barColor: '#52c41a', status: '正常', statusType: 'status-green' },
  { name: '钢筋弯曲试验机', model: 'GW-50', count: '3台', rate: 79, barColor: '#faad14', status: '正常', statusType: 'status-orange' },
  { name: '数显回弹仪', model: 'ZC3-A', count: '8台', rate: 76, barColor: '#faad14', status: '正常', statusType: 'status-orange' },
  { name: '沥青针入度仪', model: 'DF-06', count: '2台', rate: 72, barColor: '#faad14', status: '正常', statusType: 'status-orange' },
  { name: '土工击实仪', model: 'DJ-1', count: '2台', rate: 68, barColor: '#faad14', status: '正常', statusType: 'status-orange' },
  { name: '混凝土含气量测定仪', model: 'HC-7L', count: '1台', rate: 61, barColor: '#faad14', status: '正常', statusType: 'status-orange' },
  { name: '钢筋扫描仪（电磁感应）', model: 'Hilti PS 250', count: '2台', rate: 55, barColor: '#ff4d4f', status: '待保养', statusType: 'status-red' },
  { name: '水泥凝结时间测定仪', model: 'ISO-95', count: '1台', rate: 48, barColor: '#ff4d4f', status: '待保养', statusType: 'status-red' }
]

const cycleCompareData = [
  { label: '建筑工程检测', value: '3.8天', percent: 76, color: '#1677ff' },
  { label: '公路水运检测', value: '4.5天', percent: 90, color: '#faad14' },
  { label: '水利工程检测', value: '4.2天', percent: 84, color: '#faad14' },
  { label: '食品检测', value: '2.1天', percent: 42, color: '#52c41a' },
  { label: '汽车检测', value: '1.5天', percent: 30, color: '#52c41a' }
]

const reportData = [
  { id: 'RPT-20260725-032', customer: '中建三局', type: '混凝土抗压强度', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' },
  { id: 'RPT-20260725-028', customer: '中铁建工', type: '钢筋拉伸试验', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' },
  { id: 'RPT-20260725-021', customer: '中交一公局', type: '沥青混合料', result: '不合格', resultType: 'badge-red', status: '复检中', statusType: 'badge-orange' },
  { id: 'RPT-20260724-045', customer: '保利发展', type: '水泥强度检测', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' },
  { id: 'RPT-20260724-039', customer: '万科地产', type: '室内环境检测', result: '部分不合格', resultType: 'badge-orange', status: '审核中', statusType: 'badge-orange' },
  { id: 'RPT-20260724-031', customer: '华润置地', type: '基桩低应变', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' },
  { id: 'RPT-20260724-026', customer: '中建八局', type: '钢筋连接接头', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' },
  { id: 'RPT-20260723-052', customer: '碧桂园', type: '骨料筛分析', result: '合格', resultType: 'badge-green', status: '已出具', statusType: 'badge-green' }
]

const riskData = [
  { label: '混凝土外加剂检测', value: '6.8%', percent: 68, color: '#ff4d4f' },
  { label: '沥青混合料检测', value: '5.5%', percent: 55, color: '#ff4d4f' },
  { label: '钢筋连接接头', value: '4.2%', percent: 42, color: '#faad14' },
  { label: '室内空气质量', value: '3.7%', percent: 37, color: '#faad14' },
  { label: '水泥安定性', value: '2.9%', percent: 29, color: '#faad14' }
]

const overdueData = [
  { id: 'RPT-20260722-015', customer: '中交一公局', type: '沥青针入度', overdue: '3天', overdueType: 'badge-red', owner: '张明' },
  { id: 'RPT-20260721-038', customer: '万科地产', type: '室内环境', overdue: '4天', overdueType: 'badge-red', owner: '李华' },
  { id: 'RPT-20260720-027', customer: '中铁建工', type: '钢筋拉伸', overdue: '2天', overdueType: 'badge-orange', owner: '王芳' },
  { id: 'RPT-20260720-011', customer: '保利发展', type: '水泥强度', overdue: '1天', overdueType: 'badge-orange', owner: '赵强' }
]

// ========== ECharts 图表 ==========
const trendChartRef = ref<HTMLElement>()
const industryChartRef = ref<HTMLElement>()
const qualityChartRef = ref<HTMLElement>()
const constructionChartRef = ref<HTMLElement>()
const cycleChartRef = ref<HTMLElement>()
const customerChartRef = ref<HTMLElement>()
const personnelChartRef = ref<HTMLElement>()
const revenueChartRef = ref<HTMLElement>()

let charts: echarts.ECharts[] = []

// 暗色/亮色适配
const textColor = computed(() => (settingsStore.isDark ? '#a6acb3' : '#595959'))
const axisLineColor = computed(() => (settingsStore.isDark ? 'rgba(255,255,255,0.15)' : '#e8e8e8'))
const splitLineColor = computed(() => (settingsStore.isDark ? 'rgba(255,255,255,0.06)' : '#f0f0f0'))

function initChart(refEl: HTMLElement | undefined): echarts.ECharts | null {
  if (!refEl) return null
  const chart = echarts.init(refEl)
  charts.push(chart)
  return chart
}

function renderTrendChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: {
      data: ['报告数量（份）', '营收（万元）'],
      top: 0,
      right: 10,
      itemGap: 24,
      itemWidth: 16,
      itemHeight: 10,
      textStyle: { color: textColor.value }
    },
    grid: { left: '3%', right: '4%', top: 52, bottom: 5, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: { lineStyle: { color: axisLineColor.value } },
      axisLabel: { color: textColor.value },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '报告数',
        nameTextStyle: { color: textColor.value },
        axisLabel: { color: textColor.value },
        splitLine: { lineStyle: { color: splitLineColor.value } },
        axisLine: { show: false }
      },
      {
        type: 'value',
        name: '营收(万)',
        nameTextStyle: { color: textColor.value },
        axisLabel: { color: textColor.value },
        splitLine: { show: false },
        axisLine: { show: false }
      }
    ],
    series: [
      {
        name: '报告数量（份）',
        type: 'bar',
        data: [1480, 1320, 1680, 1820, 1950, 2060, 2123],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22,119,255,0.8)' },
            { offset: 1, color: 'rgba(22,119,255,0.3)' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '40%'
      },
      {
        name: '营收（万元）',
        type: 'line',
        yAxisIndex: 1,
        data: [498, 445, 568, 612, 658, 696, 779],
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2.5, color: '#ff4d4f' },
        itemStyle: { color: '#ff4d4f' }
      }
    ]
  })
}

function renderIndustryChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
    legend: { orient: 'vertical', right: 5, top: 'center', textStyle: { color: textColor.value, fontSize: 11 }, itemWidth: 12, itemHeight: 12 },
    series: [{
      type: 'pie',
      radius: ['42%', '65%'],
      center: ['38%', '50%'],
      data: [
        { value: 62, name: '建筑工程检测', itemStyle: { color: '#1677ff' } },
        { value: 14, name: '公路水运检测', itemStyle: { color: '#faad14' } },
        { value: 9, name: '水利工程检测', itemStyle: { color: '#13c2c2' } },
        { value: 8, name: '食品检测', itemStyle: { color: '#52c41a' } },
        { value: 4, name: '汽车检测', itemStyle: { color: '#722ed1' } },
        { value: 3, name: '其他', itemStyle: { color: '#8c8c8c' } }
      ],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      itemStyle: { borderColor: settingsStore.isDark ? '#1e2a3a' : '#fff', borderWidth: 2 }
    }]
  })
}

function renderQualityChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['合格', '不合格'], top: 0, right: 0, textStyle: { color: textColor.value } },
    grid: { left: '3%', right: '4%', top: 40, bottom: 5, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['混凝土检测', '钢筋钢材检测', '水泥砂浆检测', '沥青检测', '土工检测', '室内环境'],
      axisLine: { lineStyle: { color: axisLineColor.value } },
      axisLabel: { color: textColor.value, fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { color: textColor.value, formatter: '{value}%' },
      splitLine: { lineStyle: { color: splitLineColor.value } },
      axisLine: { show: false }
    },
    series: [
      {
        name: '合格',
        type: 'bar',
        stack: 'total',
        data: [95.2, 94.6, 96.8, 93.1, 97.2, 92.5],
        itemStyle: { color: 'rgba(82,196,26,0.75)', borderRadius: [0, 0, 0, 0] },
        barWidth: '40%'
      },
      {
        name: '不合格',
        type: 'bar',
        stack: 'total',
        data: [4.8, 5.4, 3.2, 6.9, 2.8, 7.5],
        itemStyle: { color: 'rgba(255,77,79,0.75)', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

function renderConstructionChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
    legend: { orient: 'vertical', right: 5, top: 'center', textStyle: { color: textColor.value, fontSize: 11 }, itemWidth: 12, itemHeight: 12 },
    series: [{
      type: 'pie',
      radius: '65%',
      center: ['38%', '50%'],
      roseType: 'area',
      data: [
        { value: 28, name: '混凝土检测', itemStyle: { color: 'rgba(22,119,255,0.7)' } },
        { value: 22, name: '钢筋钢材检测', itemStyle: { color: 'rgba(82,196,26,0.7)' } },
        { value: 15, name: '水泥砂浆检测', itemStyle: { color: 'rgba(250,173,20,0.7)' } },
        { value: 10, name: '骨料检测', itemStyle: { color: 'rgba(114,46,209,0.7)' } },
        { value: 8, name: '沥青检测', itemStyle: { color: 'rgba(19,194,194,0.7)' } },
        { value: 7, name: '土工检测', itemStyle: { color: 'rgba(255,77,79,0.7)' } },
        { value: 5, name: '基桩检测', itemStyle: { color: 'rgba(245,143,188,0.7)' } },
        { value: 5, name: '室内环境', itemStyle: { color: 'rgba(133,211,166,0.7)' } }
      ],
      label: { color: textColor.value, fontSize: 11 },
      itemStyle: { borderColor: settingsStore.isDark ? '#1e2a3a' : '#fff', borderWidth: 2 }
    }]
  })
}

function renderCycleChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (params: any) => `${params[0].name}: ${params[0].value} 小时` },
    grid: { left: '3%', right: '4%', top: 20, bottom: 5, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['样品接收', '前处理', '上机检测', '数据分析', '报告编制', '审核签发'],
      axisLine: { lineStyle: { color: axisLineColor.value } },
      axisLabel: { color: textColor.value, fontSize: 10 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '小时',
      nameTextStyle: { color: textColor.value },
      axisLabel: { color: textColor.value, fontSize: 10 },
      splitLine: { lineStyle: { color: splitLineColor.value } },
      axisLine: { show: false }
    },
    series: [{
      type: 'bar',
      data: [
        { value: 2.0, itemStyle: { color: 'rgba(22,119,255,0.7)' } },
        { value: 6.5, itemStyle: { color: 'rgba(82,196,26,0.7)' } },
        { value: 14.0, itemStyle: { color: 'rgba(250,173,20,0.7)' } },
        { value: 3.5, itemStyle: { color: 'rgba(114,46,209,0.7)' } },
        { value: 5.0, itemStyle: { color: 'rgba(19,194,194,0.7)' } },
        { value: 3.0, itemStyle: { color: 'rgba(255,77,79,0.7)' } }
      ],
      barWidth: '45%',
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })
}

function renderCustomerChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', top: 10, bottom: 5, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: textColor.value },
      splitLine: { lineStyle: { color: splitLineColor.value } },
      axisLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: ['碧桂园', '中交一公局', '华润置地', '保利发展', '万科地产', '中建八局', '中铁建工', '中建三局'],
      axisLine: { lineStyle: { color: axisLineColor.value } },
      axisLabel: { color: textColor.value },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      data: [72, 88, 96, 105, 118, 132, 145, 168],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: 'rgba(19,194,194,0.4)' },
          { offset: 1, color: 'rgba(19,194,194,0.8)' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: '55%',
      label: { show: true, position: 'right', color: textColor.value, fontSize: 11 }
    }]
  })
}

function renderPersonnelChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { data: ['本月任务量', '上月任务量'], top: 0, right: 0, textStyle: { color: textColor.value } },
    radar: {
      indicator: [
        { name: '混凝土检测组', max: 400 },
        { name: '钢筋检测组', max: 400 },
        { name: '水泥砂浆组', max: 400 },
        { name: '土工检测组', max: 400 },
        { name: '沥青检测组', max: 400 },
        { name: '室内环境组', max: 400 }
      ],
      shape: 'polygon',
      axisName: { color: textColor.value, fontSize: 11 },
      splitLine: { lineStyle: { color: splitLineColor.value } },
      splitArea: { areaStyle: { color: ['transparent'] } },
      axisLine: { lineStyle: { color: axisLineColor.value } }
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: [380, 320, 250, 180, 210, 160],
          name: '本月任务量',
          areaStyle: { color: 'rgba(22,119,255,0.15)' },
          lineStyle: { color: '#1677ff', width: 2 },
          itemStyle: { color: '#1677ff' }
        },
        {
          value: [350, 300, 240, 165, 190, 150],
          name: '上月任务量',
          areaStyle: { color: 'rgba(250,173,20,0.1)' },
          lineStyle: { color: '#faad14', width: 2 },
          itemStyle: { color: '#faad14' }
        }
      ]
    }]
  })
}

function renderRevenueChart(chart: echarts.ECharts) {
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['建筑工程检测', '公路水运检测', '水利工程检测', '食品+汽车+其他'], bottom: 0, textStyle: { color: textColor.value, fontSize: 11 }, itemWidth: 12, itemHeight: 12 },
    grid: { left: '3%', right: '4%', top: 15, bottom: 40, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: { lineStyle: { color: axisLineColor.value } },
      axisLabel: { color: textColor.value },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '万元',
      nameTextStyle: { color: textColor.value },
      axisLabel: { color: textColor.value },
      splitLine: { lineStyle: { color: splitLineColor.value } },
      axisLine: { show: false }
    },
    series: [
      {
        name: '建筑工程检测', type: 'line', stack: 'total', smooth: true,
        data: [310, 278, 352, 380, 408, 432, 483],
        areaStyle: { color: 'rgba(22,119,255,0.3)' },
        lineStyle: { color: '#1677ff', width: 2 },
        itemStyle: { color: '#1677ff' },
        symbol: 'circle', symbolSize: 4
      },
      {
        name: '公路水运检测', type: 'line', stack: 'total', smooth: true,
        data: [72, 64, 80, 86, 92, 98, 110],
        areaStyle: { color: 'rgba(250,173,20,0.3)' },
        lineStyle: { color: '#faad14', width: 2 },
        itemStyle: { color: '#faad14' },
        symbol: 'circle', symbolSize: 4
      },
      {
        name: '水利工程检测', type: 'line', stack: 'total', smooth: true,
        data: [45, 40, 52, 56, 60, 63, 70],
        areaStyle: { color: 'rgba(19,194,194,0.3)' },
        lineStyle: { color: '#13c2c2', width: 2 },
        itemStyle: { color: '#13c2c2' },
        symbol: 'circle', symbolSize: 4
      },
      {
        name: '食品+汽车+其他', type: 'line', stack: 'total', smooth: true,
        data: [71, 63, 84, 90, 98, 103, 116],
        areaStyle: { color: 'rgba(114,46,209,0.3)' },
        lineStyle: { color: '#722ed1', width: 2 },
        itemStyle: { color: '#722ed1' },
        symbol: 'circle', symbolSize: 4
      }
    ]
  })
}

function resizeAllCharts() {
  charts.forEach(c => c?.resize())
}

// 主题切换时重绘
watch(() => settingsStore.isDark, () => {
  nextTick(() => {
    if (trendChartRef.value) renderTrendChart(initOrGet(trendChartRef.value, 0))
    if (industryChartRef.value) renderIndustryChart(initOrGet(industryChartRef.value, 1))
    if (qualityChartRef.value) renderQualityChart(initOrGet(qualityChartRef.value, 2))
    if (constructionChartRef.value) renderConstructionChart(initOrGet(constructionChartRef.value, 3))
    if (cycleChartRef.value) renderCycleChart(initOrGet(cycleChartRef.value, 4))
    if (customerChartRef.value) renderCustomerChart(initOrGet(customerChartRef.value, 5))
    if (personnelChartRef.value) renderPersonnelChart(initOrGet(personnelChartRef.value, 6))
    if (revenueChartRef.value) renderRevenueChart(initOrGet(revenueChartRef.value, 7))
  })
})

function initOrGet(el: HTMLElement, idx: number): echarts.ECharts {
  if (charts[idx]) {
    return charts[idx]
  }
  const c = echarts.init(el)
  charts[idx] = c
  return c
}

onMounted(() => {
  // 初始化所有图表
  if (trendChartRef.value) renderTrendChart(initChart(trendChartRef.value)!)
  if (industryChartRef.value) renderIndustryChart(initChart(industryChartRef.value)!)
  if (qualityChartRef.value) renderQualityChart(initChart(qualityChartRef.value)!)
  if (constructionChartRef.value) renderConstructionChart(initChart(constructionChartRef.value)!)
  if (cycleChartRef.value) renderCycleChart(initChart(cycleChartRef.value)!)
  if (customerChartRef.value) renderCustomerChart(initChart(customerChartRef.value)!)
  if (personnelChartRef.value) renderPersonnelChart(initChart(personnelChartRef.value)!)
  if (revenueChartRef.value) renderRevenueChart(initChart(revenueChartRef.value)!)

  window.addEventListener('resize', resizeAllCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAllCharts)
  charts.forEach(c => c?.dispose())
  charts = []
})
</script>

<style lang="scss" scoped>
.data-screen {
  padding: 16px;
  min-height: 100vh;
  background: var(--el-bg-color-page, #f0f2f5);
  color: var(--el-text-color-primary, #1f1f1f);

  &.is-fullscreen {
    overflow: auto;
  }
}

/* Header */
.ds-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  padding: 18px 28px 14px;
  background: linear-gradient(135deg, #0958d9 0%, #1677ff 50%, #36cfc9 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(9, 88, 217, 0.25);

  .ds-header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .ds-header-left { flex: 0 0 auto; }
  .ds-header-center {
    flex: 1;
    text-align: center;
    h1 { font-size: 22px; font-weight: 700; margin: 0; }
    p { font-size: 13px; opacity: 0.85; margin-top: 4px; }
  }
  .ds-header-right {
    flex: 0 0 auto;
    text-align: right;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
    .ds-header-org { font-size: 12px; opacity: 0.7; }
    .ds-header-date { font-size: 14px; opacity: 0.9; }
    .ds-fullscreen-btn {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
      }
    }
  }

  /* 数据区间选择栏 —— 融入蓝色 banner */
  .ds-header-datebar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
  }
  .ds-header-datebar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .ds-header-datebar-label {
    font-size: 13px;
    font-weight: 600;
    opacity: 0.9;
    white-space: nowrap;
  }
  .ds-header-datebar-right {
    font-size: 13px;
    opacity: 0.85;
    strong {
      color: #fff;
      font-weight: 700;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
    }
  }

  /* radio-button 透明适配蓝底 */
  .ds-header-datebar :deep(.el-radio-button__inner) {
    background: rgba(255, 255, 255, 0.15);
    border-color: rgba(255, 255, 255, 0.3);
    color: #fff;
    box-shadow: none;
    &:hover {
      background: rgba(255, 255, 255, 0.25);
    }
  }
  .ds-header-datebar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: rgba(255, 255, 255, 0.95);
    border-color: rgba(255, 255, 255, 0.95);
    color: #0958d9;
    box-shadow: none;
  }

  /* date-picker 透明适配蓝底 */
  .ds-header-datepicker :deep(.el-range-editor.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.15);
    border-color: rgba(255, 255, 255, 0.3);
    box-shadow: none;
  }
  .ds-header-datepicker :deep(.el-range-editor .el-range-input) {
    color: #fff;
    &::placeholder { color: rgba(255, 255, 255, 0.6); }
  }
  .ds-header-datepicker :deep(.el-range-editor .el-range-separator) {
    color: rgba(255, 255, 255, 0.8);
  }
}

/* KPI Cards */
.ds-kpi-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}
.ds-kpi-card {
  background: var(--el-bg-color, #ffffff);
  border-radius: 10px;
  padding: 16px 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0; top: 0;
    width: 4px; height: 100%;
  }
  &.blue::before { background: #1677ff; }
  &.green::before { background: #52c41a; }
  &.orange::before { background: #faad14; }
  &.red::before { background: #ff4d4f; }
  &.purple::before { background: #722ed1; }
  &.cyan::before { background: #13c2c2; }
}
.ds-kpi-label { font-size: 12px; color: var(--el-text-color-secondary, #8c8c8c); margin-bottom: 6px; }
.ds-kpi-value { font-size: 24px; font-weight: 700; line-height: 1.2; }
.ds-kpi-unit { font-size: 12px; font-weight: 400; color: var(--el-text-color-secondary, #8c8c8c); margin-left: 2px; }
.ds-kpi-trend { font-size: 11px; margin-top: 6px; }
.trend-up { color: #ff4d4f; }
.trend-down { color: #52c41a; }

/* Chart Grid */
.ds-chart-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}
.ds-chart-card {
  background: var(--el-bg-color, #ffffff);
  border-radius: 10px;
  padding: 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.ds-col-3 { grid-column: span 3; }
.ds-col-4 { grid-column: span 4; }
.ds-col-5 { grid-column: span 5; }
.ds-col-6 { grid-column: span 6; }
.ds-col-7 { grid-column: span 7; }
.ds-col-8 { grid-column: span 8; }
.ds-col-12 { grid-column: span 12; }

.ds-chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  .dot {
    width: 8px; height: 8px; border-radius: 50%;
    flex-shrink: 0;
  }
}
.ds-chart-subtitle { font-size: 12px; color: var(--el-text-color-secondary, #8c8c8c); margin-bottom: 12px; }
.ds-chart-wrap { position: relative; width: 100%; }

/* Tables */
.ds-data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  th {
    text-align: left;
    padding: 10px 12px;
    background: rgba(0, 0, 0, 0.02);
    color: var(--el-text-color-secondary, #8c8c8c);
    font-weight: 600;
    font-size: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);
  }
  td {
    padding: 10px 12px;
    border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);
  }
  tr:last-child td { border-bottom: none; }
}

.ds-equip-table-wrap { max-height: 380px; overflow-y: auto; }
.ds-equip-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  th {
    text-align: left; padding: 8px 10px;
    background: rgba(0, 0, 0, 0.02);
    color: var(--el-text-color-secondary, #8c8c8c); font-weight: 600; font-size: 11px;
    border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);
    position: sticky; top: 0; z-index: 1;
  }
  td { padding: 7px 10px; border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0); }
  tr:last-child td { border-bottom: none; }
  tr:hover td { background: rgba(22, 119, 255, 0.03); }
}
.ds-equip-status {
  display: inline-block; padding: 1px 8px; border-radius: 8px; font-size: 10px; font-weight: 500;
  &.status-green { background: #f6ffed; color: #52c41a; border: 1px solid #d9f7be; }
  &.status-red { background: #fff2f0; color: #ff4d4f; border: 1px solid #ffccc7; }
  &.status-orange { background: #fff7e6; color: #faad14; border: 1px solid #ffd591; }
}

/* Badges */
.ds-badge {
  display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: 500;
  &.badge-green { background: #f6ffed; color: #52c41a; border: 1px solid #d9f7be; }
  &.badge-red { background: #fff2f0; color: #ff4d4f; border: 1px solid #ffccc7; }
  &.badge-orange { background: #fff7e6; color: #faad14; border: 1px solid #ffd591; }
}

/* Progress bars */
.ds-progress-row { margin-bottom: 8px; }
.ds-progress-row:last-child { margin-bottom: 0; }
.ds-progress-header {
  display: flex; justify-content: space-between;
  font-size: 12px; margin-bottom: 3px;
  .label { color: var(--el-text-color-primary, #1f1f1f); }
  .value { color: var(--el-text-color-secondary, #8c8c8c); font-weight: 600; }
}
.ds-progress-bar {
  height: 7px; background: rgba(0, 0, 0, 0.06); border-radius: 4px; overflow: hidden;
}
.ds-progress-fill {
  height: 100%; border-radius: 4px;
  transition: width 0.8s ease;
}

.ds-section-label {
  font-size: 13px; font-weight: 600; margin-bottom: 10px; color: var(--el-text-color-secondary, #8c8c8c);
}
.ds-cycle-compare { margin-top: 14px; }
.ds-risk-section { margin-top: 10px; }

/* Footer */
.ds-footer {
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary, #8c8c8c);
  padding: 12px 0;
}

/* 全屏模式样式 */
.data-screen.is-fullscreen {
  background: var(--el-bg-color-page, #f0f2f5);
  padding: 20px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .ds-kpi-row { grid-template-columns: repeat(3, 1fr); }
  .ds-col-3 { grid-column: span 6; }
  .ds-col-4, .ds-col-5 { grid-column: span 6; }
  .ds-col-7, .ds-col-8 { grid-column: span 12; }
}
@media (max-width: 768px) {
  .ds-kpi-row { grid-template-columns: repeat(2, 1fr); }
  .ds-col-3, .ds-col-4, .ds-col-5, .ds-col-6, .ds-col-7, .ds-col-8 { grid-column: span 12; }
  .ds-header-top { flex-direction: column; gap: 10px; text-align: center; }
  .ds-header-right { align-items: center; }
  .ds-header-datebar { flex-direction: column; gap: 10px; text-align: center; }
}
</style>
