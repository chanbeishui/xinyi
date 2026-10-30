<template>
  <RongliPage title="运营工作台" subtitle="先处理待返还车辆和人工待办，再关注当日业务概况。">
    <template #actions>
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <el-button @click="go(RL.order)">查看预约订单</el-button>
    </template>

    <section class="rl-panel">
      <div class="rl-quiet-stats">
        <div>
          <span>今日预约</span>
          <b>{{ num(overview.orders_today) }}</b>
        </div>
        <div>
          <span>统一售后待审</span>
          <b :class="{ 'is-warn': num(overview.aftersale_pending) > 0 }">{{ num(overview.aftersale_pending) }}</b>
        </div>
        <div>
          <span>取消待返还</span>
          <b :class="{ 'is-warn': num(overview.cancel_return_waiting) > 0 }">{{ num(overview.cancel_return_waiting) }}</b>
        </div>
        <div>
          <span>待核对账单</span>
          <b :class="{ 'is-warn': num(overview.statement_pending) > 0 }">{{ num(overview.statement_pending) }}</b>
        </div>
      </div>
    </section>

    <div class="rl-grid rl-grid--2 mt16">
      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>需要先处理</h3>
        </div>
        <div class="rl-panel__body rl-todo">
          <div v-for="item in todos" :key="item.title" class="rl-todo__row">
            <div class="rl-todo__icon" :class="'is-' + item.tone">{{ item.icon }}</div>
            <div class="rl-todo__body">
              <div class="rl-todo__title">{{ item.title }}</div>
              <div class="rl-todo__sub">{{ item.sub }}</div>
            </div>
            <el-button link type="primary" @click="go(item.to)">{{ item.action }}</el-button>
          </div>
        </div>
      </section>

      <section class="rl-panel">
        <div class="rl-panel__head">
          <h3>资料与人员</h3>
        </div>
        <div class="rl-panel__body rl-todo">
          <div v-for="item in entries" :key="item.title" class="rl-todo__row is-link" @click="go(item.to)">
            <div class="rl-todo__icon is-gray">{{ item.icon }}</div>
            <div class="rl-todo__body">
              <div class="rl-todo__title">{{ item.title }}</div>
              <div class="rl-todo__sub">{{ item.sub }}</div>
            </div>
            <el-icon class="rl-todo__arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </section>
    </div>

    <section class="rl-panel mt16">
      <div class="rl-panel__head">
        <h3>当日预约</h3>
        <el-button link type="primary" @click="go(RL.order)">查看全部</el-button>
      </div>
      <el-table v-loading="loading" :data="todayOrders" :border="false">
        <el-table-column label="订单 / 车辆" min-width="190">
          <template #default="{ row }">
            <div class="rl-cell-title">{{ row.plateNo || '—' }}</div>
            <div class="rl-mono">{{ row.orderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="上门时段" width="170">
          <template #default="{ row }">{{ row.pickupWindowLabel || shortTime(row.pickupStartsAt) }}</template>
        </el-table-column>
        <el-table-column label="检测点" width="150">
          <template #default="{ row }">{{ row.stationName || '—' }}</template>
        </el-table-column>
        <el-table-column label="履约状态" width="130">
          <template #default="{ row }">
            <span class="rl-tag" :class="stageClass(row.fulfillmentStatus)">
              {{ row.fulfillmentStatusLabel || row.fulfillmentStatus }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goOrder(row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rl-empty">
            <div class="rl-empty__title">今天还没有预约</div>
            <div class="rl-empty__hint">当日下单的订单会出现在这里。</div>
            <el-button @click="go(RL.order)">查看全部订单</el-button>
          </div>
        </template>
      </el-table>
    </section>
  </RongliPage>
</template>

<script setup name="RongliDashboard" lang="ts">
import RongliPage from '../components/RongliPage.vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getStatistics, listOrders } from '@/api/rongli'
import { shortTime } from '../format'
import { RL } from '../routes'
import { stageClass } from '../status'

const router = useRouter()
const overview = ref<any>({})
const dash = ref<any>({})
const todayOrders = ref<any[]>([])
const loading = ref(false)

const todos = ref<any[]>([])
const entries = [
  { icon: '车', title: '车辆列表', sub: '查看资料、关联车主与检测历史', to: RL.vehicle },
  { icon: '主', title: '车主列表', sub: '维护必要资料与服务备注', to: RL.owner },
  { icon: '驾', title: '代驾员列表', sub: '资格、任务与个人对账', to: RL.driver }
]

onMounted(load)

async function load() {
  loading.value = true
  try {
    const stat: any = await getStatistics({ days: 14 }).catch(() => null)
    overview.value = (stat && stat.overview) || {}
    dash.value = (stat && stat.dashboard) || {}
    const o = overview.value
    const res: any = await listOrders({ pageNum: 1, pageSize: 8, from: todayStart() }).catch(() => null)
    todayOrders.value = (res && res.rows) || []
    todos.value = [
      {
        icon: '返',
        tone: 'amber',
        title: num(o.cancel_return_waiting) > 0 ? '取消已批准，车辆待返还' : '当前没有待返还车辆',
        sub: num(o.cancel_return_waiting) > 0 ? '返还完成前仍需保留保管责任' : '取消批准后会自动进入此处',
        action: '查看返还',
        to: RL.aftersale
      },
      {
        icon: '审',
        tone: 'amber',
        title: num(o.aftersale_pending) > 0 ? '取消与退款申请等待审核' : '售后申请已清空',
        sub: '一次审核决定取消结果与退款金额',
        action: '进入工作台',
        to: RL.aftersale
      },
      {
        icon: '派',
        tone: 'blue',
        title: num(dash.value.dispatching) > 0 ? '有预约等待人工安排' : '自动派单队列为空',
        sub: '只邀请当前具备资格和空闲条件的代驾',
        action: '处理派单',
        to: RL.dispatch
      }
    ]
  } finally {
    loading.value = false
  }
}

function todayStart() {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d.toISOString().slice(0, 19)
}

function num(v: any) {
  return Number(v || 0)
}

function go(path: string) {
  router.push(path)
}

function goOrder(row: any) {
  router.push({ path: RL.order, query: { orderNo: row.orderNo } })
}
</script>

<style scoped>
.rl-quiet-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.rl-quiet-stats > div {
  padding: 18px 20px;
  border-right: 1px solid var(--rl-line-soft);
}

.rl-quiet-stats > div:last-child {
  border-right: 0;
}

.rl-quiet-stats span {
  display: block;
  font-size: 12.5px;
  color: var(--rl-ink-3);
  margin-bottom: 6px;
}

.rl-quiet-stats b {
  font-size: 26px;
  line-height: 32px;
  font-weight: 600;
  color: var(--rl-ink);
  letter-spacing: 0.4px;
}

.rl-quiet-stats b.is-warn {
  color: #b54708;
}

.rl-todo__row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--rl-line-soft);
}

.rl-todo__row:first-child {
  padding-top: 0;
}

.rl-todo__row:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.rl-todo__row.is-link {
  cursor: pointer;
}

.rl-todo__row.is-link:hover .rl-todo__title {
  color: var(--rl-primary);
}

.rl-todo__icon {
  flex: none;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12.5px;
  background: #f2f4f7;
  color: var(--rl-ink-2);
}

.rl-todo__icon.is-amber {
  background: #fffaeb;
  color: #b54708;
}

.rl-todo__icon.is-blue {
  background: #eff8ff;
  color: #175cd3;
}

.rl-todo__body {
  flex: 1;
  min-width: 0;
}

.rl-todo__title {
  font-size: 13px;
  color: var(--rl-ink);
  font-weight: 500;
}

.rl-todo__sub {
  font-size: 12px;
  color: var(--rl-ink-3);
  margin-top: 2px;
}

.rl-todo__arrow {
  color: var(--rl-ink-4);
}

.rl-cell-title {
  font-weight: 500;
}
</style>
