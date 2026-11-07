<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPermi } from '@/utils/permission';
import {
  companyTree,
  disableDriver,
  driverAssignments,
  enableDriver,
  getDriver,
  listDrivers,
  maintainDriver,
  type AssignmentRecord,
  type CompanyNode,
  type DriverRecord,
  type DriverWrite
} from '@/api/rongli/driver';
type Action = 'remark' | 'company' | 'disable' | 'enable';
type Attempt = { body: DriverWrite; key: string };
const query = reactive({ keyword: '', status: '', sort: 'idDesc' });
const rows = ref<DriverRecord[]>([]),
  page = ref(1),
  total = ref(0),
  loading = ref(false),
  error = ref('');
const detail = ref<DriverRecord | null>(null),
  detailOpen = ref(false),
  detailLoading = ref(false),
  detailError = ref('');
const history = ref<AssignmentRecord[]>([]),
  historyPage = ref(1),
  historyTotal = ref(0),
  historyStatus = ref(''),
  historyBusy = ref(false),
  historyError = ref(''),
  historyLoaded = ref(false);
const action = ref<Action>('remark'),
  actionOpen = ref(false),
  actionRow = ref<DriverRecord | null>(null),
  busy = ref(false),
  refreshing = ref(false),
  unknown = ref(false),
  stale = ref(false),
  actionError = ref('');
const input = reactive({ reason: '', internalRemark: '', ownerDeptId: '' });
const companies = ref<CompanyNode[]>([]),
  companyError = ref('');
let listSequence = 0,
  detailSequence = 0,
  historySequence = 0,
  actionSequence = 0;
let attempt: Attempt | null = null;
const pending = new Map<string, Attempt>();
const actionCode: Record<Action, string> = {
  remark: 'EDIT_DRIVER',
  company: 'ASSIGN_DRIVER_COMPANY',
  disable: 'DISABLE_NEW_ASSIGNMENTS',
  enable: 'ENABLE_NEW_ASSIGNMENTS'
};
const permission: Record<Action, string> = {
  remark: 'biz:driver:edit',
  company: 'biz:driver:assigncompany',
  disable: 'biz:driver:disablenewassignments',
  enable: 'biz:driver:enablenewassignments'
};
const titles: Record<Action, string> = {
  remark: '维护内部备注',
  company: '维护公司归属',
  disable: '暂停新邀约',
  enable: '恢复新增承接控制'
};
const allowed = computed(() => {
  const kind: unknown = action.value;
  if (kind !== 'remark' && kind !== 'company' && kind !== 'disable' && kind !== 'enable')
    return false;
  return (
    Boolean(actionRow.value?.allowedActions.includes(actionCode[kind])) &&
    checkPermi([permission[kind]])
  );
});
const description = computed(() =>
  action.value === 'disable'
    ? '仅暂停新增邀约与承接，不关闭本人听单偏好，不解除邀约或承接占用，也不免除在手车辆保管和实际归还责任。'
    : action.value === 'enable'
      ? '只恢复公司行政控制。资格、证照和复审仍须有效；恢复后也不代表已满足位置新鲜度、可达性等实时接单条件。'
      : action.value === 'company'
        ? '明确指定所属公司部门。同公司站点共用该代驾池；转移需覆盖原、新公司权限，且不能存在待回应邀约、未结束承接或在手事实。转移后暂停新增承接。'
        : '仅维护授权运营备注，不修改真实姓名、证件、资格、本人听单偏好或资金记录。'
);
const labels: Record<string, string> = {
  DRAFT: '资料草稿',
  SUBMITTED: '审核中',
  APPROVED: '审核通过',
  REJECTED: '未通过',
  EXPIRED: '已到期',
  SUSPENDED: '已暂停',
  OWNER: '车主',
  DRIVER: '代驾',
  STATION: '检测站',
  RETURNED: '实际归还',
  CANCELLED: '服务取消',
  DRIVER_CANCEL: '代驾退出',
  INVITE: '邀约占用',
  ASSIGNMENT: '承接占用',
  NONE: '无占用'
};
const label = (value: string | null) => (value ? labels[value] || value : '—');
const time = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' })
    : '—';
const can = (row: DriverRecord, kind: Action) =>
  row.allowedActions.includes(actionCode[kind]) && checkPermi([permission[kind]]);
onMounted(() => load());
onBeforeUnmount(() => {
  listSequence++;
  detailSequence++;
  historySequence++;
  actionSequence++;
});
function acknowledge(row: DriverRecord) {
  // 后续可信读取已达到原意图时结束本地未知态；避免日后另一轮操作误用上一轮旧键。
  for (const kind of ['remark', 'company', 'disable', 'enable'] as Action[]) {
    const key = row.driverId + ':' + kind,
      saved = pending.get(key);
    if (!saved) continue;
    if (
      (kind === 'disable' && !row.newAssignmentEnabled) ||
      (kind === 'enable' && row.newAssignmentEnabled) ||
      (kind === 'company' && row.ownerDeptId === saved.body.ownerDeptId) ||
      (kind === 'remark' && (row.internalRemark || '') === (saved.body.internalRemark || ''))
    )
      pending.delete(key);
  }
}
async function load(reset = false) {
  const run = ++listSequence;
  if (reset) page.value = 1;
  rows.value = [];
  total.value = 0;
  error.value = '';
  loading.value = true;
  try {
    const params: Record<string, string | number> = {
      pageNum: page.value,
      pageSize: 20,
      sort: query.sort
    };
    if (query.keyword.trim()) params.keyword = query.keyword.trim();
    if (query.status) params.status = query.status;
    const result = await listDrivers(params);
    if (run === listSequence) {
      rows.value = result.rows;
      total.value = result.total;
      result.rows.forEach(acknowledge);
    }
  } catch (failure: any) {
    if (run === listSequence) error.value = failure.msg || failure.message || '代驾档案读取失败';
  } finally {
    if (run === listSequence) loading.value = false;
  }
}
function reset() {
  Object.assign(query, { keyword: '', status: '', sort: 'idDesc' });
  load(true);
}
async function show(id: string) {
  const run = ++detailSequence;
  historySequence++;
  detail.value = null;
  history.value = [];
  historyTotal.value = 0;
  historyPage.value = 1;
  historyStatus.value = '';
  historyError.value = '';
  historyBusy.value = false;
  historyLoaded.value = false;
  detailOpen.value = true;
  detailLoading.value = true;
  detailError.value = '';
  try {
    const result = await getDriver(id);
    if (run === detailSequence) {
      detail.value = result.data;
      acknowledge(result.data);
    }
  } catch (failure: any) {
    if (run === detailSequence)
      detailError.value = failure.msg || failure.message || '档案详情读取失败';
  } finally {
    if (run === detailSequence) detailLoading.value = false;
  }
}
async function loadHistory(reset = false) {
  if (!detail.value || historyBusy.value) return;
  const id = detail.value.driverId,
    run = ++historySequence;
  if (reset) historyPage.value = 1;
  history.value = [];
  historyTotal.value = 0;
  historyError.value = '';
  historyBusy.value = true;
  try {
    const params: Record<string, string | number> = {
      pageNum: historyPage.value,
      pageSize: 10,
      sort: 'acceptedAtDesc'
    };
    if (historyStatus.value) params.status = historyStatus.value;
    const result = await driverAssignments(id, params);
    if (run === historySequence && detail.value?.driverId === id) {
      history.value = result.rows;
      historyTotal.value = result.total;
      historyLoaded.value = true;
    }
  } catch (failure: any) {
    if (run === historySequence)
      historyError.value = failure.msg || failure.message || '承接记录读取失败';
  } finally {
    if (run === historySequence) historyBusy.value = false;
  }
}
function normalize(nodes: CompanyNode[]): CompanyNode[] {
  return nodes.map((node) => {
    if (typeof node.id === 'number' && !Number.isSafeInteger(node.id))
      throw new Error('公司目录包含无法安全读取的部门编号，请联系管理员核对');
    return {
      ...node,
      id: String(node.id),
      children: node.children ? normalize(node.children) : undefined
    };
  });
}
async function showAction(row: DriverRecord, kind: Action) {
  actionSequence++;
  actionRow.value = row;
  action.value = kind;
  actionError.value = '';
  companyError.value = '';
  stale.value = false;
  refreshing.value = false;
  busy.value = false;
  attempt = pending.get(row.driverId + ':' + kind) || null;
  unknown.value = Boolean(attempt);
  Object.assign(input, {
    reason: attempt?.body.reason || '',
    internalRemark: attempt?.body.internalRemark ?? row.internalRemark ?? '',
    ownerDeptId: attempt?.body.ownerDeptId || ''
  });
  actionOpen.value = true;
  if (kind === 'company') {
    const run = actionSequence;
    companies.value = [];
    if (!checkPermi(['system:dept:list'])) {
      companyError.value = '读取公司候选需要部门列表权限，请联系管理员配置。';
      return;
    }
    try {
      const result = await companyTree();
      if (run !== actionSequence) return;
      if (result.code !== 200 || !Array.isArray(result.data))
        throw new Error('公司部门目录读取失败');
      companies.value = normalize(result.data);
    } catch (failure: any) {
      if (run === actionSequence)
        companyError.value = failure.msg || failure.message || '公司部门目录不可读取，请核对权限';
    }
  }
}
async function refreshAction() {
  if (!actionRow.value || busy.value || refreshing.value) return;
  const id = actionRow.value.driverId,
    run = actionSequence;
  refreshing.value = true;
  actionError.value = '';
  try {
    const result = await getDriver(id);
    if (run !== actionSequence) return;
    actionRow.value = result.data;
    acknowledge(result.data);
    const saved = pending.get(id + ':' + action.value);
    if (!saved) {
      unknown.value = false;
      attempt = null;
    }
    if (!unknown.value) {
      stale.value = false;
      attempt = null;
    }
  } catch (failure: any) {
    if (run === actionSequence)
      actionError.value = failure.msg || failure.message || '档案重新读取失败';
  } finally {
    if (run === actionSequence) refreshing.value = false;
  }
}
async function submit() {
  if (!actionRow.value || !allowed.value || busy.value || refreshing.value || stale.value) return;
  const row = actionRow.value,
    kind = action.value,
    key = row.driverId + ':' + kind,
    run = actionSequence;
  actionError.value = '';
  try {
    if (!attempt) {
      const reason = input.reason.trim();
      if (!reason || reason.length > 500) throw new Error('请填写 1 至 500 字操作原因');
      const body: DriverWrite = { expectedVersion: row.version, reason };
      if (kind === 'remark') {
        if (input.internalRemark.length > 1000) throw new Error('内部备注不能超过 1000 字');
        body.internalRemark = input.internalRemark;
      }
      if (kind === 'company') {
        if (!/^[1-9][0-9]{0,18}$/.test(input.ownerDeptId))
          throw new Error('请明确选择所属公司部门');
        body.ownerDeptId = input.ownerDeptId;
      }
      attempt = { body, key: crypto.randomUUID() };
    }
    busy.value = true;
    if (kind === 'disable') await disableDriver(row.driverId, attempt.body, attempt.key);
    else if (kind === 'enable') await enableDriver(row.driverId, attempt.body, attempt.key);
    else await maintainDriver(row.driverId, attempt.body, attempt.key);
    if (run !== actionSequence) return;
    pending.delete(key);
    attempt = null;
    unknown.value = false;
    actionOpen.value = false;
    ElMessage.success('操作已保存，正在重读当前档案事实');
    // 启停接口返回Availability，不能拿它覆盖DriverRecord或猜测听单/占用，成功后分别重读。
    await load();
    if (detailOpen.value && detail.value?.driverId === row.driverId) await show(row.driverId);
  } catch (failure: any) {
    if (run !== actionSequence) return;
    actionError.value = failure.msg || failure.message || '操作结果尚未确认，请重新核对';
    if (failure.httpStatus && failure.httpStatus < 500) {
      pending.delete(key);
      attempt = null;
      unknown.value = false;
      if (failure.httpStatus === 409) stale.value = true;
    } else if (attempt) {
      pending.set(key, attempt);
      unknown.value = true;
    }
  } finally {
    if (run === actionSequence) busy.value = false;
  }
}
function closeAction(done: () => void) {
  if (busy.value || refreshing.value) {
    ElMessage.warning('请求处理中，请等待回执');
    return;
  }
  actionSequence++;
  done();
  actionOpen.value = false;
}
function closeDetail(done: () => void) {
  if (actionOpen.value) return;
  detailSequence++;
  historySequence++;
  detail.value = null;
  history.value = [];
  done();
}
</script>
<template>
  <div class="app-container driver-page">
    <div class="heading">
      <div>
        <h2>代驾员档案与接单控制</h2>
        <p>同公司站点共用代驾池。资格、本人听单意愿和新增承接控制分别展示。</p>
      </div>
      <el-button :loading="loading" @click="load()">刷新列表</el-button>
    </div>
    <el-form class="filters" inline label-position="top" @submit.prevent="load(true)">
      <el-form-item label="关键词"
        ><el-input
          v-model="query.keyword"
          placeholder="昵称、代驾编号或完整手机号"
          clearable
          style="width: 260px"
      /></el-form-item>
      <el-form-item label="资格状态"
        ><el-select v-model="query.status" clearable placeholder="全部资格状态" style="width: 160px"
          ><el-option
            v-for="status in ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'EXPIRED', 'SUSPENDED']"
            :key="status"
            :label="label(status)"
            :value="status" /></el-select
      ></el-form-item>
      <el-form-item label="排序"
        ><el-select v-model="query.sort" style="width: 150px"
          ><el-option label="档案编号倒序" value="idDesc" /><el-option
            label="账号昵称升序"
            value="nameAsc" /></el-select
      ></el-form-item>
      <el-form-item label="操作"
        ><el-button type="primary" native-type="submit" :loading="loading">查询</el-button
        ><el-button @click="reset">重置</el-button></el-form-item
      >
    </el-form>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-table v-loading="loading" :data="rows" row-key="driverId" empty-text="当前范围暂无代驾档案">
      <el-table-column label="账号昵称 / 电话" min-width="160"
        ><template #default="{ row }"
          ><div>{{ row.name || '未设置昵称' }}</div>
          <small>{{ row.phoneMask || '暂无核验手机号' }}</small></template
        ></el-table-column
      >
      <el-table-column label="所属公司部门" min-width="170"
        ><template #default="{ row }">{{
          row.companyName || (row.ownershipUnassigned ? '待认领归属' : '—')
        }}</template></el-table-column
      >
      <el-table-column label="资格" width="110"
        ><template #default="{ row }"
          ><el-tag :type="row.qualificationStatus === 'APPROVED' ? 'success' : 'warning'">{{
            label(row.qualificationStatus)
          }}</el-tag></template
        ></el-table-column
      >
      <el-table-column label="新增承接控制" width="130"
        ><template #default="{ row }">{{
          row.newAssignmentEnabled ? '行政控制已恢复' : '已暂停'
        }}</template></el-table-column
      >
      <el-table-column label="本人听单意愿" width="125"
        ><template #default="{ row }">{{
          row.listening ? '愿意听单' : '未开启'
        }}</template></el-table-column
      >
      <el-table-column label="任务占用" width="150"
        ><template #default="{ row }"
          ><div>{{ row.occupied ? '有资源占用' : '无资源占用' }}</div>
          <small v-if="row.assignmentId">承接 #{{ row.assignmentId }}</small
          ><small v-else-if="row.occupied">关联编号按权限隐藏</small></template
        ></el-table-column
      >
      <el-table-column label="已实际归还承接" min-width="145"
        ><template #default="{ row }">{{
          typeof row.completedAssignmentCount === 'number'
            ? row.completedAssignmentCount
            : '未授权查看'
        }}</template></el-table-column
      >
      <el-table-column label="评分" min-width="145"
        ><template #default="{ row }">{{
          row.score ?? '暂无可追溯评分'
        }}</template></el-table-column
      >
      <el-table-column label="操作" width="260" fixed="right"
        ><template #default="{ row }"
          ><el-button
            v-if="row.allowedActions.includes('VIEW_DRIVER')"
            v-hasPermi="['biz:driver:query']"
            link
            type="primary"
            @click="show(row.driverId)"
            >查看档案</el-button
          ><el-button
            v-if="can(row, 'disable')"
            link
            type="warning"
            @click="showAction(row, 'disable')"
            >暂停新邀约</el-button
          ><el-button
            v-if="can(row, 'enable')"
            link
            type="primary"
            @click="showAction(row, 'enable')"
            >恢复控制</el-button
          ><el-button
            v-if="can(row, 'company') && row.canAssignCompany"
            link
            type="primary"
            @click="showAction(row, 'company')"
            >{{ row.ownershipUnassigned ? '认领公司' : '公司归属' }}</el-button
          ><el-button
            v-if="can(row, 'remark')"
            link
            type="primary"
            @click="showAction(row, 'remark')"
            >备注</el-button
          ></template
        ></el-table-column
      >
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :page-size="20"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load()"
    />
    <el-drawer
      v-model="detailOpen"
      title="代驾档案与承接记录"
      size="min(1000px, 100vw)"
      :before-close="closeDetail"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail">
        <el-alert v-if="detailError" :title="detailError" type="error" :closable="false" />
        <template v-if="detail">
          <div class="heading">
            <h2>{{ detail.name || '未设置昵称' }}</h2>
            <el-button @click="show(detail.driverId)">刷新档案</el-button>
          </div>
          <p class="muted">
            此处姓名为账号昵称，不作为实名核验结论。证件和资格结论只读，备注维护不会修改认证资料。
          </p>
          <el-descriptions :column="2" border
            ><el-descriptions-item label="档案编号">{{ detail.driverId }}</el-descriptions-item
            ><el-descriptions-item label="账号编号">{{ detail.accountId }}</el-descriptions-item
            ><el-descriptions-item label="所属公司部门">{{
              detail.companyName || '待认领归属'
            }}</el-descriptions-item
            ><el-descriptions-item label="核验手机号">{{
              detail.phoneMask || '暂无'
            }}</el-descriptions-item
            ><el-descriptions-item label="资格状态">{{
              label(detail.qualificationStatus)
            }}</el-descriptions-item
            ><el-descriptions-item label="资格版本">{{
              detail.qualificationRevision ?? '暂无生效版本'
            }}</el-descriptions-item
            ><el-descriptions-item label="准驾类别">{{
              detail.licenseClass || '未登记'
            }}</el-descriptions-item
            ><el-descriptions-item label="驾驶证有效期">{{
              detail.licenseLongTerm ? '长期有效' : detail.licenseValidTo || '未登记'
            }}</el-descriptions-item
            ><el-descriptions-item
              :label="detail.reviewDueAtInstant ? '复审截止时间' : '复审到期日'"
              >{{
                detail.reviewDueAtInstant
                  ? time(detail.reviewDueAtInstant)
                  : detail.reviewDueAt || '未明确'
              }}</el-descriptions-item
            ><el-descriptions-item label="记录版本">{{ detail.version }}</el-descriptions-item
            ><el-descriptions-item label="新增承接控制">{{
              detail.newAssignmentEnabled ? '行政控制已恢复' : '已暂停'
            }}</el-descriptions-item
            ><el-descriptions-item label="本人听单意愿">{{
              detail.listening ? '愿意听单' : '未开启'
            }}</el-descriptions-item
            ><el-descriptions-item label="任务占用">{{
              detail.occupied ? '有资源占用' : '无资源占用'
            }}</el-descriptions-item
            ><el-descriptions-item label="当前承接">{{
              detail.assignmentId || (detail.occupied ? '关联编号按权限隐藏' : '无当前承接')
            }}</el-descriptions-item
            ><el-descriptions-item label="已实际归还承接">{{
              typeof detail.completedAssignmentCount === 'number'
                ? detail.completedAssignmentCount
                : '未授权查看'
            }}</el-descriptions-item
            ><el-descriptions-item label="评分">{{
              detail.score ?? '暂无可追溯评分'
            }}</el-descriptions-item></el-descriptions
          >
          <el-alert
            title="行政控制恢复不代表已可接新单。实时位置、可达性及其它准入条件仍须单独核验；暂停或转移归属都不能解除在手车辆责任。"
            type="info"
            :closable="false"
            show-icon
          />
          <section>
            <h3>内部运营备注</h3>
            <p class="preserve">{{ detail.internalRemark || '暂无备注' }}</p>
            <div class="actions">
              <el-button v-if="can(detail, 'remark')" @click="showAction(detail, 'remark')"
                >维护内部备注</el-button
              ><el-button
                v-if="can(detail, 'disable')"
                type="warning"
                @click="showAction(detail, 'disable')"
                >暂停新邀约</el-button
              ><el-button
                v-if="can(detail, 'enable')"
                type="primary"
                @click="showAction(detail, 'enable')"
                >恢复新增承接控制</el-button
              ><el-button
                v-if="can(detail, 'company') && detail.canAssignCompany"
                @click="showAction(detail, 'company')"
                >维护公司归属</el-button
              >
            </div>
          </section>
          <section
            v-if="
              detail.allowedActions.includes('VIEW_ASSIGNMENTS') &&
              checkPermi(['biz:assignment:list'])
            "
          >
            <h3>承接服务记录</h3>
            <p class="muted">
              只显示原订单归属范围内可查看的承接，档案公司权限不自动开放跨公司历史。
            </p>
            <div class="actions">
              <el-select
                v-model="historyStatus"
                :disabled="historyBusy"
                clearable
                placeholder="全部承接状态"
                style="width: 180px"
                @change="loadHistory(true)"
                ><el-option label="未结束承接" value="ACTIVE" /><el-option
                  label="已结束承接"
                  value="ENDED" /><el-option label="已实际归还" value="RETURNED" /></el-select
              ><el-button :loading="historyBusy" @click="loadHistory(true)">读取承接记录</el-button>
            </div>
            <el-alert
              v-if="historyError"
              :title="historyError"
              type="error"
              :closable="false"
            /><el-table
              v-loading="historyBusy"
              :data="history"
              :empty-text="historyLoaded ? '当前授权范围暂无承接记录' : '尚未读取承接记录'"
              ><el-table-column
                label="承接编号"
                prop="assignmentId"
                min-width="140"
              /><el-table-column label="订单号" prop="orderNo" min-width="270" /><el-table-column
                label="接受时间"
                min-width="180"
                ><template #default="{ row }">{{ time(row.acceptedAt) }}</template></el-table-column
              ><el-table-column label="结束时间" min-width="180"
                ><template #default="{ row }">{{ time(row.endedAt) }}</template></el-table-column
              ><el-table-column label="结束原因" min-width="120"
                ><template #default="{ row }">{{ label(row.endReason) }}</template></el-table-column
              ><el-table-column label="订单当前保管" min-width="100"
                ><template #default="{ row }">{{ label(row.custody) }}</template></el-table-column
              ><el-table-column label="本承接释放" width="100"
                ><template #default="{ row }">{{
                  row.released ? '已释放' : '未释放'
                }}</template></el-table-column
              ></el-table
            ><el-pagination
              v-if="historyLoaded"
              v-model:current-page="historyPage"
              :page-size="10"
              :total="historyTotal"
              layout="total, prev, pager, next"
              @current-change="loadHistory()"
            />
          </section>
          <section>
            <h3>个人对账</h3>
            <p class="muted">
              个人对账、费用台账与付款摘要暂未开放。本页不推算收入，不提供本人费用审批。
            </p>
            <el-button disabled>个人对账暂不可用</el-button>
          </section>
        </template>
      </div>
    </el-drawer>
    <el-dialog
      v-model="actionOpen"
      :title="titles[action]"
      width="min(700px, 96vw)"
      append-to-body
      :before-close="closeAction"
      destroy-on-close
    >
      <template v-if="actionRow">
        <el-alert :title="description" type="info" :closable="false" show-icon />
        <p>{{ actionRow.name || '未设置昵称' }} · 档案 {{ actionRow.driverId }}</p>
        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item v-if="action === 'remark'" label="内部备注"
            ><el-input
              v-model="input.internalRemark"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              placeholder="仅维护授权运营备注"
              :disabled="busy || unknown || refreshing"
          /></el-form-item>
          <template v-if="action === 'company'">
            <p>原归属：{{ actionRow.companyName || '尚未认领' }}</p>
            <el-alert
              v-if="actionRow.ownershipUnassigned"
              title="未归属档案仅可由具备全局归属授权的管理员认领，不能默认分给创建人或当前主部门。"
              type="warning"
              :closable="false"
            />
            <el-form-item label="明确选择所属公司部门"
              ><el-tree-select
                v-model="input.ownerDeptId"
                :data="companies"
                :props="{ value: 'id', label: 'label', children: 'children', disabled: 'disabled' }"
                node-key="id"
                check-strictly
                filterable
                clearable
                placeholder="选择实际所属公司部门"
                :disabled="busy || unknown || refreshing"
                style="width: 100%"
            /></el-form-item>
            <el-alert v-if="companyError" :title="companyError" type="error" :closable="false" />
          </template>
          <el-form-item label="操作原因（必填）"
            ><el-input
              v-model="input.reason"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请填写本次维护或接单控制原因"
              :disabled="busy || unknown || refreshing"
          /></el-form-item>
        </el-form>
        <el-alert
          v-if="stale"
          title="档案或占用事实已变化，填写内容已保留。请先重读当前档案，再决定是否提交。"
          type="warning"
          :closable="false"
        />
        <el-alert
          v-if="unknown"
          title="上次结果尚未确认，已冻结原请求。重试沿用原内容；可先重新读取档案核对是否已经生效。"
          type="warning"
          :closable="false"
        />
        <el-alert
          v-if="!allowed"
          title="当前服务端未允许此动作，请按最新权限和占用事实处理。"
          type="warning"
          :closable="false"
        />
        <el-alert v-if="actionError" :title="actionError" type="error" :closable="false" />
      </template>
      <template #footer
        ><el-button :loading="refreshing" :disabled="busy" @click="refreshAction"
          >重读当前档案</el-button
        ><el-button
          type="primary"
          :loading="busy"
          :disabled="!allowed || stale || refreshing || (action === 'company' && !companies.length)"
          @click="submit"
          >{{ unknown ? '沿用原请求重试' : '确认保存' }}</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>
<style scoped>
.heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.heading p,
.muted,
small {
  color: #64748b;
  line-height: 1.7;
}
.filters {
  margin-top: 20px;
  padding: 18px 18px 0;
  background: #f8fafc;
  border-radius: 8px;
}
.el-pagination {
  margin-top: 20px;
}
.detail {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.detail h2,
.detail h3 {
  margin: 0;
}
.detail section {
  border-top: 1px solid #e2e8f0;
  padding-top: 20px;
}
.preserve {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.8;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin: 14px 0;
}
.actions .el-button {
  margin-left: 0;
}
:deep(.el-descriptions__content) {
  overflow-wrap: anywhere;
}
@media (max-width: 650px) {
  .heading {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
