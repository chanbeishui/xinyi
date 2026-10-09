import type { LngLat } from '../data/geoData'

/** 投影后的画布坐标 */
export interface Point {
  x: number
  y: number
}

export interface Projection {
  /** 经纬度 → SVG 坐标 */
  project: (p: LngLat) => Point
  /** 经纬度环 → SVG path 的 d 属性 */
  toPath: (ring: LngLat[], close?: boolean) => string
}

/**
 * 等距圆柱投影 + 纬度余弦修正，并自动缩放平移到给定画布内。
 * 不依赖任何地图库，边界数据换成真实 GeoJSON 坐标后同样适用。
 */
export function createProjection(
  rings: LngLat[][],
  width: number,
  height: number,
  padding = 40
): Projection {
  const all = rings.flat()
  const lngs = all.map((p) => p[0])
  const lats = all.map((p) => p[1])
  const minLng = Math.min(...lngs)
  const maxLng = Math.max(...lngs)
  const minLat = Math.min(...lats)
  const maxLat = Math.max(...lats)

  // 中心纬度的余弦，用于修正高纬度方向上的横向压缩
  const kx = Math.cos((((minLat + maxLat) / 2) * Math.PI) / 180)

  const rawW = (maxLng - minLng) * kx
  const rawH = maxLat - minLat
  const innerW = width - padding * 2
  const innerH = height - padding * 2
  const scale = Math.min(innerW / rawW, innerH / rawH)

  // 居中偏移
  const offsetX = padding + (innerW - rawW * scale) / 2
  const offsetY = padding + (innerH - rawH * scale) / 2

  function project(p: LngLat): Point {
    return {
      x: offsetX + (p[0] - minLng) * kx * scale,
      y: offsetY + (maxLat - p[1]) * scale
    }
  }

  function toPath(ring: LngLat[], close = true): string {
    if (ring.length === 0) return ''
    const d = ring
      .map((p, i) => {
        const { x, y } = project(p)
        return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`
      })
      .join(' ')
    return close ? `${d} Z` : d
  }

  return { project, toPath }
}

/**
 * 生成从中心点到目标点的二次贝塞尔弧线。
 * 控制点沿弦的中垂线偏移，距离越远弧度越明显。
 */
export function arcPath(from: Point, to: Point, curvature = 0.28): string {
  const dx = to.x - from.x
  const dy = to.y - from.y
  const dist = Math.sqrt(dx * dx + dy * dy)
  const mx = (from.x + to.x) / 2
  const my = (from.y + to.y) / 2
  // 中垂线单位向量（顺时针旋转 90°）
  const nx = -dy / (dist || 1)
  const ny = dx / (dist || 1)
  // 曲率方向按目标点在中心点的左右侧翻转，保证弧线统一向上"抬起"
  const dir = dx >= 0 ? -1 : 1
  const offset = dist * curvature * dir
  const cx = mx + nx * offset
  const cy = my + ny * offset
  return `M${from.x.toFixed(1)},${from.y.toFixed(1)} Q${cx.toFixed(1)},${cy.toFixed(1)} ${to.x.toFixed(
    1
  )},${to.y.toFixed(1)}`
}
