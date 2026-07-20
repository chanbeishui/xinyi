<template>
  <div class="app-container home">
    <!-- 欢迎横幅 -->
    <section class="hero">
      <div class="hero-text">
        <p class="hero-kicker">XINYI WORKSPACE</p>
        <h2>{{ greeting }}，{{ userStore.nickName || userStore.name || '管理员' }}</h2>
        <p class="hero-sub">今天是 {{ todayText }}，欢迎使用{{ title }}。</p>
      </div>
      <img src="@/assets/logo/xinyi-lotus.png" alt="" class="hero-mark" />
    </section>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col v-for="card in statCards" :key="card.label" :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
            <svg-icon :icon-class="card.icon" />
          </div>
          <div class="stat-info">
            <p class="stat-label">{{ card.label }}</p>
            <p class="stat-value">{{ card.value }}</p>
          </div>
          <span class="stat-trend" :class="card.trend >= 0 ? 'up' : 'down'">
            {{ card.trend >= 0 ? '+' : '' }}{{ card.trend }}%
          </span>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="panel">
          <template #header><span>近 7 日访问趋势</span></template>
          <div ref="lineChartRef" class="chart chart-line"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel">
          <template #header><span>访问来源</span></template>
          <div ref="pieChartRef" class="chart chart-pie"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口与更新日志 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="panel">
          <template #header><span>快捷入口</span></template>
          <div class="quick-grid">
            <router-link v-for="entry in quickLinks" :key="entry.path" :to="entry.path" class="quick-item">
              <span class="quick-icon" :style="{ background: entry.bg, color: entry.color }">
                <svg-icon :icon-class="entry.icon" />
              </span>
              <span class="quick-label">{{ entry.label }}</span>
            </router-link>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel">
          <template #header><span>更新日志</span></template>
          <el-timeline class="changelog">
            <el-timeline-item
              v-for="log in changelogs"
              :key="log.version"
              :timestamp="log.date"
              :type="log.primary ? 'primary' : ''"
            >
              <b>{{ log.version }}</b> {{ log.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'

const title = import.meta.env.VITE_APP_TITLE
const userStore = useUserStore()
const settingsStore = useSettingsStore()

// 按时段生成问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const statCards = [
  { label: '今日访问量', value: '12,480', trend: 12.5, icon: 'chart', color: '#1677ff', bg: 'rgba(22,119,255,.1)' },
  { label: '在线用户', value: '268', trend: 4.2, icon: 'online', color: '#12b76a', bg: 'rgba(18,183,106,.1)' },
  { label: '新增用户', value: '36', trend: -2.1, icon: 'peoples', color: '#f79009', bg: 'rgba(247,144,9,.1)' },
  { label: '待办事项', value: '8', trend: 2.0, icon: 'clipboard', color: '#7a5af8', bg: 'rgba(122,90,248,.1)' }
]

const quickLinks = [
  { label: '用户管理', path: '/system/user', icon: 'user', color: '#1677ff', bg: 'rgba(22,119,255,.1)' },
  { label: '角色管理', path: '/system/role', icon: 'peoples', color: '#12b76a', bg: 'rgba(18,183,106,.1)' },
  { label: '菜单管理', path: '/system/menu', icon: 'tree-table', color: '#f79009', bg: 'rgba(247,144,9,.1)' },
  { label: '在线用户', path: '/monitor/online', icon: 'online', color: '#0ea5e9', bg: 'rgba(14,165,233,.1)' },
  { label: '定时任务', path: '/monitor/job', icon: 'job', color: '#7a5af8', bg: 'rgba(122,90,248,.1)' },
  { label: '代码生成', path: '/tool/gen', icon: 'code', color: '#ee46bc', bg: 'rgba(238,70,188,.1)' }
]

const changelogs = [
  { version: 'v3.9.2', date: '2026-03-26', text: '新增锁定屏幕、首页消息提醒，全面升级依赖版本', primary: true },
  { version: 'v3.9.1', date: '2025-12-18', text: '菜单导航支持纯顶部模式，修复多个已知问题', primary: false },
  { version: 'v3.9.0', date: '2025-09-30', text: '代码生成支持 TypeScript 模板，优化数据权限', primary: false }
]

const lineChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 暗色模式下坐标轴与文字颜色
const axisColor = computed(() => (settingsStore.isDark ? '#98a2b3' : '#667085'))
const splitLineColor = computed(() => (settingsStore.isDark ? 'rgba(255,255,255,.08)' : 'rgba(16,24,40,.06)'))

function renderLineChart(): void {
  if (!lineChart) return
  lineChart.setOption({
    color: ['#1677ff', '#44b899'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['访问量', '登录数'], textStyle: { color: axisColor.value }, top: 0 },
    grid: { left: 8, right: 12, top: 36, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: splitLineColor.value } },
      axisLabel: { color: axisColor.value }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: axisColor.value },
      splitLine: { lineStyle: { color: splitLineColor.value } }
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: [1820, 2132, 1991, 2534, 2790, 2330, 3120],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22,119,255,.22)' },
            { offset: 1, color: 'rgba(22,119,255,0)' }
          ])
        },
        showSymbol: false
      },
      {
        name: '登录数',
        type: 'line',
        smooth: true,
        data: [620, 732, 701, 834, 990, 830, 1120],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(68,184,153,.18)' },
            { offset: 1, color: 'rgba(68,184,153,0)' }
          ])
        },
        showSymbol: false
      }
    ]
  })
}

function renderPieChart(): void {
  if (!pieChart) return
  pieChart.setOption({
    color: ['#1677ff', '#44b899', '#f79009', '#7a5af8', '#98a2b3'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: axisColor.value }, icon: 'circle' },
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 6, borderColor: settingsStore.isDark ? '#161d29' : '#fff', borderWidth: 2 },
        label: { show: false },
        data: [
          { value: 4380, name: '直接访问' },
          { value: 2860, name: '搜索引擎' },
          { value: 1730, name: '外部链接' },
          { value: 960, name: '邮件营销' },
          { value: 550, name: '其他' }
        ]
      }
    ]
  })
}

function resizeCharts(): void {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  if (lineChartRef.value) lineChart = echarts.init(lineChartRef.value)
  if (pieChartRef.value) pieChart = echarts.init(pieChartRef.value)
  renderLineChart()
  renderPieChart()
  window.addEventListener('resize', resizeCharts)
})

// 切换明暗主题后重绘图表
watch(() => settingsStore.isDark, () => {
  renderLineChart()
  renderPieChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  lineChart?.dispose()
  pieChart?.dispose()
  lineChart = null
  pieChart = null
})
</script>

<style lang="scss" scoped>
.home {
  .hero {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30px 36px;
    margin-bottom: 20px;
    border-radius: 16px;
    overflow: hidden;
    color: #fff;
    background: linear-gradient(120deg, #1677ff 0%, #3b8cff 55%, #69a9ff 100%);
    box-shadow: 0 14px 34px rgba(22, 119, 255, .24);
  }
  .hero-kicker {
    margin: 0 0 10px;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: .18em;
    opacity: .82;
  }
  .hero-text h2 {
    margin: 0;
    font-size: 26px;
    font-weight: 650;
    letter-spacing: -.02em;
  }
  .hero-sub {
    margin: 10px 0 0;
    font-size: 13px;
    opacity: .85;
  }
  .hero-mark {
    width: 86px;
    height: 86px;
    border-radius: 22px;
    background: rgba(255, 255, 255, .9);
    padding: 8px;
    box-shadow: 0 10px 24px rgba(8, 42, 102, .25);
  }

  .stat-row { margin-bottom: 4px; }
  .stat-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 20px;
    margin-bottom: 16px;
    border-radius: 14px;
    background: var(--xy-surface, #fff);
    border: 1px solid var(--xy-line, rgba(16, 24, 40, .09));
    transition: transform .18s ease, box-shadow .18s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 26px rgba(16, 24, 40, .08);
    }
  }
  .stat-icon {
    width: 46px;
    height: 46px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
  }
  .stat-info { flex: 1; min-width: 0; }
  .stat-label { margin: 0; font-size: 13px; color: var(--xy-muted, #667085); }
  .stat-value { margin: 4px 0 0; font-size: 24px; font-weight: 700; letter-spacing: -.02em; color: var(--xy-ink, #101828); }
  .stat-trend {
    font-size: 12px;
    font-weight: 700;
    padding: 3px 8px;
    border-radius: 999px;
    &.up { color: #12b76a; background: rgba(18, 183, 106, .1); }
    &.down { color: #f04438; background: rgba(240, 68, 56, .1); }
  }

  .chart-row { margin-bottom: 4px; }
  .panel { margin-bottom: 16px; }
  .chart { width: 100%; }
  .chart-line { height: 320px; }
  .chart-pie { height: 320px; }

  .quick-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
  }
  .quick-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-radius: 12px;
    border: 1px solid var(--xy-line, rgba(16, 24, 40, .09));
    transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease;

    &:hover {
      border-color: rgba(22, 119, 255, .4);
      box-shadow: 0 8px 20px rgba(22, 119, 255, .1);
      transform: translateY(-2px);
    }
  }
  .quick-icon {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }
  .quick-label { font-size: 14px; font-weight: 600; color: var(--xy-ink, #101828); }

  .changelog { padding-left: 4px; }
}

@media (max-width: 768px) {
  .home {
    .hero { padding: 22px; }
    .hero-mark { display: none; }
    .quick-grid { grid-template-columns: repeat(2, 1fr); }
  }
}
</style>
