/**
 * 荣利后台状态字典：文案与配色统一在此维护，避免各页面各写一套 el-tag 判断。
 * 文案与《全量页面与交互规格》及小程序侧保持一致。
 */
export const STAGE_LABEL: Record<string, string> = {
  PENDING_PAY: '待支付',
  SCHEDULED: '已排期',
  PENDING_DISPATCH: '待派单',
  DISPATCHING: '派单中',
  MANUAL_DISPATCH: '待人工安排',
  ACCEPTED: '已接单',
  GOING_PICKUP: '前往接车',
  ARRIVED_PICKUP: '已到达接车点',
  PICKED_UP: '已接车',
  ARRIVED_STATION: '已到站',
  INSPECTING: '检测中',
  INSPECTED: '检测完成 · 待取车',
  CAR_TAKEN: '已取车',
  WAIT_CONFIRM: '待服务确认',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '已关闭'
}

/** 待处理橙、处理中蓝、检测/完成绿、终止灰，对应原型状态色板 */
const STAGE_TONE: Record<string, string> = {
  PENDING_PAY: 'amber',
  SCHEDULED: 'blue',
  PENDING_DISPATCH: 'amber',
  DISPATCHING: 'amber',
  MANUAL_DISPATCH: 'amber',
  ACCEPTED: 'blue',
  GOING_PICKUP: 'blue',
  ARRIVED_PICKUP: 'blue',
  PICKED_UP: 'blue',
  ARRIVED_STATION: 'blue',
  INSPECTING: 'green',
  INSPECTED: 'blue',
  CAR_TAKEN: 'green',
  WAIT_CONFIRM: 'green',
  COMPLETED: 'green',
  CANCELLED: 'gray',
  CLOSED: 'gray'
}

export function stageLabel(code?: string | null): string {
  if (!code) return '—'
  return STAGE_LABEL[code] || code
}

export function stageTone(code?: string | null): string {
  if (!code) return 'gray'
  return STAGE_TONE[code] || 'gray'
}

export function stageClass(code?: string | null): string {
  return `rl-tag--${stageTone(code)}`
}

export const CUSTODY_LABEL: Record<string, string> = {
  OWNER: '车主保管',
  DRIVER: '代驾保管',
  STATION: '检测点保管'
}

export function custodyLabel(code?: string | null): string {
  if (!code) return '—'
  return CUSTODY_LABEL[code] || code
}

export const CANCEL_LABEL: Record<string, string> = {
  NONE: '无申请',
  APPLIED: '取消申请',
  REJECTED: '已拒绝取消',
  APPROVED_WAIT_RETURN: '待返还车辆',
  COMPLETED: '已取消'
}

export function cancelLabel(code?: string | null): string {
  return CANCEL_LABEL[code || 'NONE'] || code || '无申请'
}

export function cancelTone(code?: string | null): string {
  if (code === 'APPLIED' || code === 'APPROVED_WAIT_RETURN') return 'amber'
  if (code === 'REJECTED') return 'red'
  return 'gray'
}

/** 退款状态：与 B13/B14 的审核结论一致 */
export const REFUND_LABEL: Record<string, string> = {
  NONE: '无退款',
  RESERVED: '退款预留',
  PROCESSING: '退款中',
  SUCCESS: '已退款',
  FAILED: '退款失败',
  UNKNOWN: '待核实'
}

export function refundLabel(code?: string | null): string {
  if (!code) return '无退款'
  return REFUND_LABEL[code] || code
}

export function refundTone(code?: string | null): string {
  if (code === 'SUCCESS') return 'green'
  if (code === 'FAILED' || code === 'UNKNOWN') return 'red'
  if (code === 'RESERVED' || code === 'PROCESSING') return 'amber'
  return 'gray'
}
