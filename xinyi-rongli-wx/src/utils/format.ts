/** 金额与时间展示工具：后端金额统一为整数分、时间统一为 ISO8601 UTC 字符串。 */

export function fen2yuan(fen: number | string | null | undefined): string {
  const value = Number(fen || 0)
  if (!isFinite(value)) return '0.00'
  return (value / 100).toFixed(2)
}

export function yuan2fen(yuan: number | string): number {
  const value = Number(yuan || 0)
  if (!isFinite(value)) return 0
  return Math.round(value * 100)
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`
}

/** ISO8601 UTC 转北京时间 Date */
export function toDate(iso?: string | null): Date | null {
  if (!iso) return null
  let normalized = iso
  if (!/Z$|[+-]\d{2}:?\d{2}$/.test(normalized)) {
    // 后端大部分字段带 Z；少数历史接口返回裸 LocalDateTime，分量同样是 UTC，必须补 Z，
    // 否则小程序会按本地时间解析，展示结果整体早 8 小时
    normalized = `${normalized.replace(' ', 'T')}${normalized.length === 10 ? 'T00:00:00' : ''}Z`
  }
  const date = new Date(normalized)
  return isNaN(date.getTime()) ? null : date
}

/** yyyy-MM-dd HH:mm（北京时间） */
export function dateTime(iso?: string | null): string {
  const d = toDate(iso)
  if (!d) return '-'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** MM-dd HH:mm */
export function shortTime(iso?: string | null): string {
  const d = toDate(iso)
  if (!d) return '-'
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** yyyy-MM-dd */
export function dateOnly(iso?: string | null): string {
  const d = toDate(iso)
  if (!d) return '-'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 相对剩余时间描述 */
export function countdownText(iso?: string | null): string {
  const d = toDate(iso)
  if (!d) return ''
  const diff = Math.floor((d.getTime() - Date.now()) / 1000)
  if (diff <= 0) return '已过期'
  if (diff < 60) return `${diff} 秒后过期`
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟后过期`
  return `${Math.floor(diff / 3600)} 小时后过期`
}

/** 时段展示：把 startsAt/endsAt 压成 "MM-dd HH:mm - HH:mm" */
export function windowLabel(startsAt?: string | null, endsAt?: string | null): string {
  const s = toDate(startsAt)
  const e = toDate(endsAt)
  if (!s) return '-'
  const head = `${pad(s.getMonth() + 1)}-${pad(s.getDate())} ${pad(s.getHours())}:${pad(s.getMinutes())}`
  if (!e) return head
  return `${head} - ${pad(e.getHours())}:${pad(e.getMinutes())}`
}

/** 车牌脱敏展示（仅用于非归属方视角） */
export function maskPlate(plate?: string | null): string {
  if (!plate || plate.length < 4) return plate || '-'
  return `${plate.slice(0, 2)}***${plate.slice(-2)}`
}
