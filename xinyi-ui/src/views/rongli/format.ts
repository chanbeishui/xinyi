/** 荣利后台通用格式化：时间统一按后端 ISO 字符串裁剪，金额统一为分转元。 */

export function shortTime(value?: string | null): string {
  if (!value) return '—'
  return String(value).replace('T', ' ').replace('Z', '').slice(0, 16)
}

export function shortDate(value?: string | null): string {
  if (!value) return '—'
  return String(value).replace('T', ' ').replace('Z', '').slice(0, 10)
}

export function shortClock(value?: string | null): string {
  if (!value) return '—'
  return String(value).replace('T', ' ').replace('Z', '').slice(11, 16)
}

export function money(fen?: number | string | null): string {
  return '¥' + (Number(fen || 0) / 100).toFixed(2)
}

export function yuan(fen?: number | string | null): string {
  return (Number(fen || 0) / 100).toFixed(2)
}

export function revisionLabel(revision?: number | string | null): string {
  return 'R' + String(revision ?? 1).padStart(2, '0')
}
