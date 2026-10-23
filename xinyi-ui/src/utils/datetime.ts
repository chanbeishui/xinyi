/**
 * 时间展示统一入口。
 *
 * 后端时间契约（见 docs/implementation/g01-g06-delivery.md 与 ViTime.java）：
 *  1. 业务表（vi_*）统一存 UTC 分量，接口输出 ISO8601 带 Z，如 2026-10-21T05:00:00.000Z；
 *  2. 少数直接返回 MyBatis 行对象的历史接口没有 Z，但分量同样是 UTC，
 *     浏览器会按「本地时间」解析，必须显式补 Z 才能得到正确时刻；
 *  3. 若依系统表（sys_*）是 java.util.Date，输出 "yyyy-MM-dd HH:mm:ss"，本身就是本地时间。
 *
 * 本模块只做一件事：把上面三种形状统一成北京时间的展示文本，且对已格式化的文本幂等，
 * 因此可以在 axios 响应拦截器里全局跑一遍，页面里再调用也不会二次偏移。
 */

/** 北京时间相对 UTC 的固定偏移（中国无夏令时） */
const SHANGHAI_OFFSET_MS = 8 * 60 * 60 * 1000

/** yyyy-MM-ddTHH:mm[:ss][.SSS]，无时区后缀 —— 后端 LocalDateTime 直出，分量是 UTC */
const NAIVE_ISO = /^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2}))?(?:\.(\d{1,9}))?$/
/** 带 Z 或 ±HH:mm 偏移 —— 绝对时刻 */
const HAS_OFFSET = /(?:Z|[+-]\d{2}:?\d{2})$/i
/** 已经是本地时间的展示文本，原样返回 */
const LOCAL_TEXT = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/

function pad(value: number, size = 2): string {
  return String(value).padStart(size, '0')
}

/**
 * 把后端时间值解析成绝对时刻。
 *
 * @param value ISO8601 字符串（带或不带时区）、时间戳（秒/毫秒）、Date 或 [y,m,d,h,i,s]
 * @returns 解析失败返回 null
 */
export function toInstant(value: any): Date | null {
  if (value === null || value === undefined || value === '' || value === '-') {
    return null
  }
  if (value instanceof Date) {
    return isNaN(value.getTime()) ? null : value
  }
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1, h = 0, i = 0, s = 0] = value
    const fromArray = new Date(Date.UTC(+y, +m - 1, +d, +h, +i, +s))
    return isNaN(fromArray.getTime()) ? null : fromArray
  }
  if (typeof value === 'number') {
    const ms = value < 1e12 ? value * 1000 : value
    const fromNumber = new Date(ms)
    return isNaN(fromNumber.getTime()) ? null : fromNumber
  }
  if (typeof value !== 'string') {
    return null
  }
  const text = value.trim()
  if (HAS_OFFSET.test(text)) {
    const withOffset = new Date(text)
    return isNaN(withOffset.getTime()) ? null : withOffset
  }
  const naive = text.match(NAIVE_ISO)
  if (naive) {
    // 无时区后缀：按后端约定分量即 UTC，用 Date.UTC 直接构造，避免被浏览器按本地时间解释
    const milli = Number((naive[7] || '0').slice(0, 3).padEnd(3, '0'))
    const utc = Date.UTC(+naive[1], +naive[2] - 1, +naive[3], +naive[4], +naive[5],
      naive[6] ? +naive[6] : 0, milli)
    const fromNaive = new Date(utc)
    return isNaN(fromNaive.getTime()) ? null : fromNaive
  }
  const fallback = new Date(text)
  return isNaN(fallback.getTime()) ? null : fallback
}

/**
 * 格式化为北京时间文本。
 *
 * @param value  后端时间值，或已是 "yyyy-MM-dd HH:mm[:ss]" 的展示文本
 * @param pattern 支持 {y}{m}{d}{h}{i}{s} 与 YYYY/MM/DD/HH/mm/ss 两种占位
 */
export function formatDateTime(value: any, pattern = 'YYYY-MM-DD HH:mm:ss'): string {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  if (typeof value === 'string' && LOCAL_TEXT.test(value.trim())) {
    // 已是对外展示的本地时间，保持幂等
    return value.trim()
  }
  const instant = toInstant(value)
  if (!instant) {
    return String(value)
  }
  // 先平移到东八区，再用 UTC 取值，展示结果不依赖运行机器的时区
  const shifted = new Date(instant.getTime() + SHANGHAI_OFFSET_MS)
  const parts: Record<string, number> = {
    YYYY: shifted.getUTCFullYear(),
    MM: shifted.getUTCMonth() + 1,
    DD: shifted.getUTCDate(),
    HH: shifted.getUTCHours(),
    mm: shifted.getUTCMinutes(),
    ss: shifted.getUTCSeconds()
  }
  const map: Record<string, string> = {
    y: 'YYYY', m: 'MM', d: 'DD', h: 'HH', i: 'mm', s: 'ss'
  }
  return pattern
    .replace(/YYYY|MM|DD|HH|mm|ss/g, (token) => pad(parts[token]))
    .replace(/\{([ymdhis])\}/g, (_all, key) => pad(parts[map[key]]))
}

/** 只保留日期：2026-10-21 */
export function formatDate(value: any): string {
  return formatDateTime(value, 'YYYY-MM-DD')
}

/** 表格里紧凑展示：2026-10-21 13:00 */
export function formatMinute(value: any): string {
  return formatDateTime(value, 'YYYY-MM-DD HH:mm')
}

/**
 * 递归把响应体里的 ISO8601 时间串转成北京时间文本。
 *
 * 只处理以 yyyy-MM-ddT 开头、能被 {@link formatDateTime} 解析的字符串，
 * 普通文本、日期串（yyyy-MM-dd）、JSON 串内容都不会被改动。
 */
export function normalizeTimeStrings<T>(input: T): T {
  return walk(input) as T
}

function walk(node: any): any {
  if (Array.isArray(node)) {
    for (let i = 0; i < node.length; i += 1) {
      node[i] = walk(node[i])
    }
    return node
  }
  if (node && typeof node === 'object' && !(node instanceof Date)) {
    const record = node as Record<string, any>
    for (const key of Object.keys(record)) {
      record[key] = walk(record[key])
    }
    return node
  }
  if (typeof node === 'string' && node.length >= 19 && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(node)) {
    return formatDateTime(node)
  }
  return node
}
