/**
 * 地市分区剖分：按"种子点"把一个区域切成互不重叠、无缝拼接的多边形。
 *
 * 项目内没有真实 GeoJSON，手绘 21 个地级市的相邻边界既费力又容易留缝/重叠，
 * 因此用 Voronoi（最近邻剖分）自动生成分区面：
 *   - 每个地市给 1~3 个种子点控制形状，同一 owner 的多个单元合并成该地市的分区；
 *   - 生成结果天然无缝、无重叠，再用 SVG clipPath 裁剪到省界内即可；
 *   - 属示意性划分，后期若拿到真实边界，直接给 MapLevel.areas 赋值即可绕过本模块。
 */

import type { Point } from './useGeoProjection'

export interface Bounds {
  x: number
  y: number
  w: number
  h: number
}

export interface VoronoiCell {
  /** 所属区划名（同一 owner 可能有多个单元） */
  owner: string
  ring: Point[]
}

/**
 * 用 a、b 两点的中垂线裁剪凸多边形，只保留距 a 更近的一侧。
 * Voronoi 单元始终是凸的，因此逐个半平面裁剪的结果精确。
 */
function clipHalfPlane(poly: Point[], a: Point, b: Point): Point[] {
  const dx = b.x - a.x
  const dy = b.y - a.y
  const mx = (a.x + b.x) / 2
  const my = (a.y + b.y) / 2
  // 到 a 更近 <=> 在中垂线的 a 侧 <=> (p - m)·(b - a) <= 0
  const side = (p: Point) => (p.x - mx) * dx + (p.y - my) * dy
  const out: Point[] = []

  for (let i = 0; i < poly.length; i++) {
    const cur = poly[i]
    const nxt = poly[(i + 1) % poly.length]
    const sc = side(cur)
    const sn = side(nxt)
    if (sc <= 0) out.push(cur)
    if ((sc <= 0) !== (sn <= 0)) {
      const denom = sn - sc
      if (denom !== 0) {
        const t = -sc / denom
        out.push({ x: cur.x + t * (nxt.x - cur.x), y: cur.y + t * (nxt.y - cur.y) })
      }
    }
  }
  return out
}

/** 生成每个种子点的 Voronoi 单元（画布坐标） */
export function voronoiCells(seeds: { owner: string; pt: Point }[], bounds: Bounds): VoronoiCell[] {
  const frame: Point[] = [
    { x: bounds.x, y: bounds.y },
    { x: bounds.x + bounds.w, y: bounds.y },
    { x: bounds.x + bounds.w, y: bounds.y + bounds.h },
    { x: bounds.x, y: bounds.y + bounds.h }
  ]

  return seeds.map((seed, i) => {
    let ring = frame
    for (let j = 0; j < seeds.length; j++) {
      if (j === i || ring.length === 0) continue
      ring = clipHalfPlane(ring, seed.pt, seeds[j].pt)
    }
    return { owner: seed.owner, ring }
  })
}

/** 多边形环转 SVG path */
export function ringToPath(ring: Point[]): string {
  if (ring.length === 0) return ''
  return `${ring.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')}Z`
}

/**
 * 只保留"不同地市之间"的分界线，丢弃同一地市内部相邻单元的接缝。
 * 判定方式：取边的中点，找距它最近的两个种子点（中点落在两者中垂线上），
 * 两者 owner 不同则是地市界。
 */
export function cityBorderPath(cells: VoronoiCell[], seeds: { owner: string; pt: Point }[]): string {
  const segs: string[] = []
  const seen = new Set<string>()

  cells.forEach((cell) => {
    const ring = cell.ring
    for (let i = 0; i < ring.length; i++) {
      const a = ring[i]
      const b = ring[(i + 1) % ring.length]
      const mid = { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2 }

      // 找中点最近的两个种子
      let n1 = -1
      let n2 = -1
      let d1 = Infinity
      let d2 = Infinity
      seeds.forEach((s, idx) => {
        const d = (s.pt.x - mid.x) ** 2 + (s.pt.y - mid.y) ** 2
        if (d < d1) {
          d2 = d1
          n2 = n1
          d1 = d
          n1 = idx
        } else if (d < d2) {
          d2 = d
          n2 = idx
        }
      })
      if (n1 < 0 || n2 < 0) continue
      if (seeds[n1].owner === seeds[n2].owner) continue

      // 同一条界线会被相邻两个单元各生成一次，去重
      const key = [
        `${Math.round(a.x)},${Math.round(a.y)}`,
        `${Math.round(b.x)},${Math.round(b.y)}`
      ]
        .sort()
        .join('|')
      if (seen.has(key)) continue
      seen.add(key)

      segs.push(`M${a.x.toFixed(1)},${a.y.toFixed(1)}L${b.x.toFixed(1)},${b.y.toFixed(1)}`)
    }
  })

  return segs.join(' ')
}

/** 环的质心（用于给分区打标签，比外接框中心更稳） */
export function ringCentroid(ring: Point[]): Point {
  let area = 0
  let cx = 0
  let cy = 0
  for (let i = 0; i < ring.length; i++) {
    const a = ring[i]
    const b = ring[(i + 1) % ring.length]
    const cross = a.x * b.y - b.x * a.y
    area += cross
    cx += (a.x + b.x) * cross
    cy += (a.y + b.y) * cross
  }
  if (area === 0) return ring[0] ?? { x: 0, y: 0 }
  return { x: cx / (3 * area), y: cy / (3 * area) }
}
