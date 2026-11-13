<template>
  <div class="app-container home">
    <!-- 欢迎横幅：沿用原首页的渐变头，右侧信息按当日待办生成 -->
    <section class="hero">
      <div class="hero-text">
        <p class="hero-kicker">XINYI OPERATIONS</p>
        <h2>{{ greeting }}，{{ displayName }}</h2>
        <p class="hero-sub">今天是 {{ todayText }}{{ heroSummary }}</p>
      </div>
      <img src="@/assets/logo/xinyi-lotus.png" alt="" class="hero-mark" />
    </section>

    <!-- 运营数据：只有具备运营看板权限才展示，避免对纯系统管理员显示空指标 -->
    <template v-if="canOps">
      <el-row :gutter="20" class="stat-row">
        <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="12" :md="6">
          <div class="stat-card is-link" @click="go(card.to)">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <svg-icon :icon-class="card.icon" />
            </div>
            <div class="stat-info">
              <p class="stat-label">{{ card.label }}</p>
              <p class="stat-value">{{ card.value }}</p>
            </div>
            <span v-if="card.alert" class="stat-alert">待处理</span>
            <el-icon v-else class="stat-go"><ArrowRight /></el-icon>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :xs="24" :md="15">
          <el-card shadow="never" class="panel">
            <template #header>
              <div class="panel-head">
                <span>需要先处理</span>
                <el-button link type="primary" icon="Refresh" :loading="loading" @click="load">
                  刷新
                </el-button>
              </div>
            </template>
            <div v-loading="loading" class="todo-list">
              <div
                v-for="item in todos"
                :key="item.title"
                class="todo"
                @click="go(item.to)"
              >
                <span class="todo-icon" :class="'is-' + item.tone">{{ item.icon }}</span>
                <div class="todo-main">
                  <p class="todo-title">{{ item.title }}</p>
                  <p class="todo-sub">{{ item.sub }}</p>
                </div>
                <span class="todo-action">
                  {{ item.action }}
                  <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
              <div v-if="!todos.length" class="todo-empty">
                <p class="todo-empty__title">{{ loaded ? '当前没有待处理事项' : '正在读取待办…' }}</p>
                <p class="todo-empty__hint">审核、返还、派单与异常处理完成后会从这里消失。</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="9">
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
            <p v-if="!quickLinks.length" class="quick-empty">
              当前账号没有可进入的业务或系统页面，请联系管理员分配菜单权限。
            </p>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="canOrder" shadow="never" class="panel">
        <template #header>
          <div class="panel-head">
            <span>当日预约</span>
            <el-button link type="primary" @click="go(RL.order)">查看全部</el-button>
          </div>
        </template>
        <el-table v-loading="loading" :data="todayOrders" :border="false">
          <el-table-column label="订单 / 车辆" min-width="190">
            <template #default="{ row }">
              <div class="cell-title">{{ row.plateNo || '—' }}</div>
              <div class="cell-mono">{{ row.orderNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="上门时段" width="170">
            <template #default="{ row }">{{ row.pickupWindowLabel || shortTime(row.pickupStartsAt) }}</template>
          </el-table-column>
          <el-table-column label="检测点" width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ row.stationName || '—' }}</template>
          </el-table-column>
          <el-table-column label="履约状态" width="140">
            <template #default="{ row }">
              <span class="stage" :class="stageClass(row.fulfillmentStatus)">
                {{ row.fulfillmentStatusLabel || stageLabel(row.fulfillmentStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goOrder(row)">查看</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">
              <p class="table-empty__title">今天还没有预约</p>
              <p class="table-empty__hint">车主下单后会出现在这里，可下拉或点「刷新」重新读取。</p>
              <el-button @click="go(RL.order)">查看全部订单</el-button>
            </div>
          </template>
        </el-table>
      </el-card>

      <el-card shadow="never" class="panel">
        <template #header>
          <div class="panel-head">
            <span>近 7 日预约趋势</span>
            <span class="panel-head__note">按订单创建时间归属自然日</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart chart-trend"></div>
        <p v-if="chartNote" class="chart-note">{{ chartNote }}</p>
      </el-card>
    </template>

    <!-- 无运营权限时的兜底：保留原首页的快捷入口，不显示空指标 -->
    <el-card v-else shadow="never" class="panel">
      <template #header><span>快捷入口</span></template>
      <div class="quick-grid">
        <router-link v-for="entry in quickLinks" :key="entry.path" :to="entry.path" class="quick-item">
          <span class="quick-icon" :style="{ background: entry.bg, color: entry.color }">
            <svg-icon :icon-class="entry.icon" />
          </span>
          <span class="quick-label">{{ entry.label }}</span>
        </router-link>
      </div>
      <p v-if="!quickLinks.length" class="quick-empty">
        当前账号还没有被分配任何菜单权限，请联系管理员在「系统管理 / 用户管理」里授权。
      </p>
      <p v-else class="quick-note">
        当前账号没有运营看板权限，运营数据（预约、售后、派单）不对其展示。
      </p>
    </el-card>
  </div>
</template>

<script setup name="Home" lang="ts">
import * as echarts from 'echarts'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import auth from '@/plugins/auth'
import { getStatistics, listOrders } from '@/api/rongli'
import { RL } from '@/views/rongli/routes'
import { shortTime } from '@/views/rongli/format'
import { stageClass, stageLabel } from '@/views/rongli/status'

const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

const displayName = computed(() => userStore.nickName || userStore.name || '管理员')

// 运营数据依赖统计接口权限；没有该权限的账号只看欢迎语与系统入口
const canOps = computed(() => auth.hasPermi('biz:statistics:list'))
// 当日预约表格单独依赖订单查询权限：只有统计权限的角色不该去拉订单列表（会 403 弹错）
const canOrder = computed(() => auth.hasPermi('biz:order:list'))

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

const overview = ref<any>({})
const dash = ref<any>({})
const todayOrders = ref<any[]>([])
const last7 = ref<any[]>([])
const chartNote = ref('')
const loading = ref(false)
const loaded = ref(false)

function num(v: any): number {
  return Number(v || 0)
}

/** 四张指标卡：与「需要先处理」同源，不重复计数口径，点击直接去对应处理页 */
const statCards = computed(() => [
  {
    key: 'orders_today',
    label: '今日预约',
    value: loaded.value ? num(overview.value.orders_today) : '—',
    icon: 'form',
    color: '#1677ff',
    bg: 'rgba(22,119,255,.1)',
    to: RL.order,
    alert: false
  },
  {
    key: 'aftersale_pending',
    label: '售后待审',
    value: loaded.value ? num(overview.value.aftersale_pending) : '—',
    icon: 'clipboard',
    color: '#f79009',
    bg: 'rgba(247,144,9,.1)',
    to: RL.aftersale,
    alert: num(overview.value.aftersale_pending) > 0
  },
  {
    key: 'cancel_return_waiting',
    label: '取消待返还',
    value: loaded.value ? num(overview.value.cancel_return_waiting) : '—',
    icon: 'money',
    color: '#f04438',
    bg: 'rgba(240,68,56,.1)',
    to: RL.aftersale,
    alert: num(overview.value.cancel_return_waiting) > 0
  },
  {
    key: 'statement_pending',
    label: '待核对账单',
    value: loaded.value ? num(overview.value.statement_pending) : '—',
    icon: 'documentation',
    color: '#7a5af8',
    bg: 'rgba(122,90,248,.1)',
    to: RL.statement,
    alert: num(overview.value.statement_pending) > 0
  }
])

/** 待办只列真正需要动作的项；全部清零时走空态，不堆四行「已清空」 */
const todos = computed(() => {
  const rows: any[] = []
  const aftersale = num(overview.value.aftersale_pending)
  const waiting = num(overview.value.cancel_return_waiting)
  const dispatching = num(dash.value.dispatching)
  const abnormal = num(overview.value.dispute_open) + num(overview.value.exception_open)
  if (aftersale > 0) {
    rows.push({
      icon: '审',
      tone: 'amber',
      title: `${aftersale} 笔取消与退款申请等待审核`,
      sub: '一次审核决定取消结果与退款金额',
      action: '去审核',
      to: RL.aftersale
    })
  }
  if (waiting > 0) {
    rows.push({
      icon: '返',
      tone: 'red',
      title: `${waiting} 台车辆取消已批准但尚未返还`,
      sub: '返还完成前仍需保留保管责任',
      action: '去处理',
      to: RL.aftersale
    })
  }
  if (dispatching > 0) {
    rows.push({
      icon: '派',
      tone: 'blue',
      title: `${dispatching} 个预约等待人工安排`,
      sub: '只邀请当前具备资格与空闲条件的代驾',
      action: '去派单',
      to: RL.dispatch
    })
  }
  if (abnormal > 0) {
    rows.push({
      icon: '异',
      tone: 'red',
      title: `${abnormal} 条异常或争议待处理`,
      sub: '含服务异常、争议受理与检测结果更正',
      action: '去处理',
      to: RL.exception
    })
  }
  return rows
})

const pendingTotal = computed(
  () =>
    num(overview.value.aftersale_pending) +
    num(overview.value.cancel_return_waiting) +
    num(dash.value.dispatching) +
    num(overview.value.dispute_open) +
    num(overview.value.exception_open)
)

const heroSummary = computed(() => {
  if (!canOps.value) return '，欢迎使用『渔驿·荣利检测预约』后台。'
  if (!loaded.value) return '，正在读取今日运营数据…'
  if (pendingTotal.value > 0) return `，有 ${pendingTotal.value} 项待处理需要确认。`
  return '，当前没有待处理事项，可以专注当日业务。'
})

/** 快捷入口：业务优先，按当前账号真实权限过滤，不出现点进去 401 的入口 */
const QUICK_ENTRIES = [
  { label: '预约订单', path: RL.order, perm: 'biz:order:list', icon: 'form', color: '#1677ff', bg: 'rgba(22,119,255,.1)' },
  { label: '调度与派单', path: RL.dispatch, perm: 'biz:dispatch:list', icon: 'job', color: '#7a5af8', bg: 'rgba(122,90,248,.1)' },
  { label: '取消与退款', path: RL.aftersale, perm: 'biz:aftersale:list', icon: 'money', color: '#f79009', bg: 'rgba(247,144,9,.1)' },
  { label: '检测点与容量', path: RL.station, perm: 'biz:station:list', icon: 'tree', color: '#12b76a', bg: 'rgba(18,183,106,.1)' },
  { label: '车辆列表', path: RL.vehicle, perm: 'biz:vehicle:list', icon: 'guide', color: '#0ea5e9', bg: 'rgba(14,165,233,.1)' },
  { label: '业务统计', path: RL.statistics, perm: 'biz:statistics:list', icon: 'chart', color: '#ee46bc', bg: 'rgba(238,70,188,.1)' },
  { label: '用户管理', path: '/system/user', perm: 'system:user:list', icon: 'user', color: '#1677ff', bg: 'rgba(22,119,255,.1)' },
  { label: '角色管理', path: '/system/role', perm: 'system:role:list', icon: 'peoples', color: '#12b76a', bg: 'rgba(18,183,106,.1)' },
  { label: '菜单管理', path: '/system/menu', perm: 'system:menu:list', icon: 'tree-table', color: '#f79009', bg: 'rgba(247,144,9,.1)' },
  { label: '在线用户', path: '/monitor/online', perm: 'monitor:online:list', icon: 'online', color: '#0ea5e9', bg: 'rgba(14,165,233,.1)' }
]

const quickLinks = computed(() => QUICK_ENTRIES.filter((item) => auth.hasPermi(item.perm)).slice(0, 9))

function todayStart(): string {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d.toISOString().slice(0, 19)
}

async function load(): Promise<void> {
  if (!canOps.value) {
    loaded.value = true
    return
  }
  loading.value = true
  try {
    const stat: any = await getStatistics({ days: 7 }).catch(() => null)
    overview.value = (stat && stat.overview) || {}
    dash.value = (stat && stat.dashboard) || {}
    last7.value = (stat && stat.last7) || []
    chartNote.value = (stat && stat.chartNote) || ''
    if (canOrder.value) {
      const res: any = await listOrders({ pageNum: 1, pageSize: 8, from: todayStart() }).catch(() => null)
      todayOrders.value = (res && res.rows) || []
    }
    renderTrendChart()
  } finally {
    loading.value = false
    loaded.value = true
  }
}

function go(path: string): void {
  router.push(path)
}

function goOrder(row: any): void {
  router.push({ path: RL.order, query: { orderNo: row.orderNo } })
}

// ---------------------------------------------------------------- 趋势图
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null

const axisColor = computed(() => (settingsStore.isDark ? '#98a2b3' : '#667085'))
const splitLineColor = computed(() => (settingsStore.isDark ? 'rgba(255,255,255,.08)' : 'rgba(16,24,40,.06)'))

function renderTrendChart(): void {
  if (!trendChart) return
  const labels = last7.value.map((d: any) => d.label || String(d.day || '').slice(5))
  const values = last7.value.map((d: any) => num(d.orders))
  trendChart.setOption(
    {
      color: ['#1677ff'],
      tooltip: { trigger: 'axis' },
      grid: { left: 8, right: 16, top: 18, bottom: 0, containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: labels,
        axisLine: { lineStyle: { color: splitLineColor.value } },
        axisLabel: { color: axisColor.value }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: axisColor.value },
        splitLine: { lineStyle: { color: splitLineColor.value } }
      },
      series: [
        {
          name: '预约量',
          type: 'line',
          smooth: true,
          data: values,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(22,119,255,.22)' },
              { offset: 1, color: 'rgba(22,119,255,0)' }
            ])
          }
        }
      ]
    },
    true
  )
}

function resizeCharts(): void {
  trendChart?.resize()
}

onMounted(async () => {
  if (trendChartRef.value) trendChart = echarts.init(trendChartRef.value)
  window.addEventListener('resize', resizeCharts)
  await load()
})

watch(() => settingsStore.isDark, () => {
  renderTrendChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  trendChart = null
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

    &.is-link { cursor: pointer; }
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
  .stat-label {
    margin: 0;
    font-size: 13px;
    color: var(--xy-muted, #667085);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .stat-value { margin: 4px 0 0; font-size: 24px; font-weight: 700; letter-spacing: -.02em; color: var(--xy-ink, #101828); }
  .stat-alert {
    flex-shrink: 0;
    padding: 3px 8px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    color: #f79009;
    background: rgba(247, 144, 9, .12);
  }
  .stat-go { color: #c3ccd8; }

  .panel { margin-bottom: 16px; }
  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
  .panel-head__note { font-size: 12px; font-weight: 400; color: var(--xy-muted, #667085); }

  /* 待办：一行一件事，右侧给动作词，整行可点 */
  .todo-list { min-height: 96px; }
  .todo {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 0;
    border-bottom: 1px solid var(--xy-line, rgba(16, 24, 40, .09));
    cursor: pointer;

    &:first-child { padding-top: 2px; }
    &:last-child { border-bottom: 0; padding-bottom: 2px; }
    &:hover .todo-title { color: #1677ff; }
  }
  .todo-icon {
    flex: none;
    width: 34px;
    height: 34px;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
    background: #f2f4f7;
    color: #475467;

    &.is-amber { background: #fffaeb; color: #b54708; }
    &.is-blue { background: #eff8ff; color: #175cd3; }
    &.is-red { background: #fef3f2; color: #b42318; }
  }
  .todo-main { flex: 1; min-width: 0; }
  .todo-title { margin: 0; font-size: 14px; font-weight: 600; color: var(--xy-ink, #101828); }
  .todo-sub { margin: 4px 0 0; font-size: 12px; color: var(--xy-muted, #667085); }
  .todo-action {
    flex: none;
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    font-weight: 600;
    color: #1677ff;
  }
  .todo-empty {
    padding: 28px 0 24px;
    text-align: center;
  }
  .todo-empty__title { margin: 0; font-size: 14px; font-weight: 600; color: var(--xy-ink, #101828); }
  .todo-empty__hint { margin: 6px 0 0; font-size: 12px; color: var(--xy-muted, #667085); }

  .quick-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
    gap: 12px;
  }
  .quick-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
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
    width: 34px;
    height: 34px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 17px;
    flex-shrink: 0;
  }
  .quick-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--xy-ink, #101828);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .quick-empty { margin: 12px 0 0; font-size: 12px; line-height: 1.7; color: var(--xy-muted, #667085); }
  .quick-note { margin: 14px 0 0; font-size: 12px; line-height: 1.7; color: var(--xy-muted, #667085); }

  .cell-title { font-weight: 500; }
  .cell-mono {
    margin-top: 2px;
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    font-size: 12px;
    color: var(--xy-muted, #667085);
  }

  /* 状态标：与荣利页面同一套语义色，但颜色写在本页，避免依赖别的页面挂载样式 */
  .stage {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 22px;
    padding: 0 8px;
    border-radius: 6px;
    font-size: 12px;
    line-height: 22px;
    white-space: nowrap;
    border: 1px solid transparent;

    &::before {
      content: '';
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: currentColor;
    }

    &.rl-tag--amber { color: #b54708; background: #fffaeb; border-color: #fedf89; }
    &.rl-tag--blue { color: #175cd3; background: #eff8ff; border-color: #b2ddff; }
    &.rl-tag--green { color: #067647; background: #ecfdf3; border-color: #abefc6; }
    &.rl-tag--gray { color: #475467; background: #f2f4f7; border-color: #e4e7ec; }
    &.rl-tag--red { color: #b42318; background: #fef3f2; border-color: #fecdca; }
  }

  .table-empty { padding: 22px 0; }
  .table-empty__title { margin: 0; font-size: 14px; font-weight: 600; color: var(--xy-ink, #101828); }
  .table-empty__hint { margin: 6px 0 14px; font-size: 12px; color: var(--xy-muted, #667085); }

  .chart { width: 100%; }
  .chart-trend { height: 260px; }
  .chart-note { margin: 10px 0 0; font-size: 12px; line-height: 1.6; color: var(--xy-muted, #667085); }
}

@media (max-width: 768px) {
  .home {
    .hero { padding: 22px; }
    .hero-mark { display: none; }
    .quick-grid { grid-template-columns: repeat(2, 1fr); }

    /* 窄屏两列卡片里，图标 + 标签 + 数值已经占满宽度，
       「待处理」标和箭头继续占位会把标签压成竖排，这里只留数字本身 */
    .stat-card { padding: 14px; gap: 10px; }
    .stat-icon { width: 36px; height: 36px; font-size: 18px; }
    .stat-value { font-size: 20px; }
    .stat-alert,
    .stat-go { display: none; }
  }
}
</style>
