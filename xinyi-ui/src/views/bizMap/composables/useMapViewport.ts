import { ref, computed, onBeforeUnmount } from 'vue'

/**
 * SVG viewBox 视口控制：拖拽平移、滚轮缩放、带过渡动画的聚焦/复位。
 * 与 archiveRoom 的 useSvgPanZoom 相比，这里的缩放与聚焦是缓动过渡的，适合大屏演示。
 */
export function useMapViewport(baseW: number, baseH: number, minScale = 0.6, maxScale = 6) {
  const vx = ref(0)
  const vy = ref(0)
  const vw = ref(baseW)
  const vh = ref(baseH)

  const viewBox = computed(() => `${vx.value} ${vy.value} ${vw.value} ${vh.value}`)
  const scale = computed(() => baseW / vw.value)
  const scalePercent = computed(() => Math.round(scale.value * 100) + '%')

  let raf = 0

  function stopAnim() {
    if (raf) {
      cancelAnimationFrame(raf)
      raf = 0
    }
  }

  /** 缓动过渡到目标 viewBox */
  function animateTo(tx: number, ty: number, tw: number, duration = 520) {
    stopAnim()
    const fx = vx.value
    const fy = vy.value
    const fw = vw.value
    const th = tw * (baseH / baseW)
    const fh = vh.value
    const start = performance.now()
    const step = (now: number) => {
      const t = Math.min(1, (now - start) / duration)
      // easeOutCubic
      const e = 1 - Math.pow(1 - t, 3)
      vx.value = fx + (tx - fx) * e
      vy.value = fy + (ty - fy) * e
      vw.value = fw + (tw - fw) * e
      vh.value = fh + (th - fh) * e
      if (t < 1) {
        raf = requestAnimationFrame(step)
      } else {
        raf = 0
      }
    }
    raf = requestAnimationFrame(step)
  }

  function clampW(w: number) {
    return Math.max(baseW / maxScale, Math.min(baseW / minScale, w))
  }

  function onWheel(e: WheelEvent) {
    stopAnim()
    const el = e.currentTarget as SVGElement
    const rect = el.getBoundingClientRect()
    const rx = (e.clientX - rect.left) / rect.width
    const ry = (e.clientY - rect.top) / rect.height
    const mx = vx.value + rx * vw.value
    const my = vy.value + ry * vh.value
    const newW = clampW(vw.value * (e.deltaY > 0 ? 1.12 : 0.89))
    const newH = newW * (baseH / baseW)
    vx.value = mx - rx * newW
    vy.value = my - ry * newH
    vw.value = newW
    vh.value = newH
  }

  let dragging = false
  let lastX = 0
  let lastY = 0
  const isDragging = ref(false)

  function onMouseDown(e: MouseEvent) {
    stopAnim()
    dragging = true
    isDragging.value = true
    lastX = e.clientX
    lastY = e.clientY
  }

  function onMouseMove(e: MouseEvent) {
    if (!dragging) return
    const rect = (e.currentTarget as SVGElement).getBoundingClientRect()
    const ratio = vw.value / rect.width
    vx.value -= (e.clientX - lastX) * ratio
    vy.value -= (e.clientY - lastY) * ratio
    lastX = e.clientX
    lastY = e.clientY
  }

  function onMouseUp() {
    dragging = false
    isDragging.value = false
  }

  function zoomBy(factor: number) {
    const newW = clampW(vw.value * factor)
    const cx = vx.value + vw.value / 2
    const cy = vy.value + vh.value / 2
    animateTo(cx - newW / 2, cy - (newW * (baseH / baseW)) / 2, newW, 260)
  }

  const zoomIn = () => zoomBy(0.78)
  const zoomOut = () => zoomBy(1.28)

  /** 复位到完整视图 */
  function reset(animated = true) {
    if (animated) {
      animateTo(0, 0, baseW)
    } else {
      stopAnim()
      vx.value = 0
      vy.value = 0
      vw.value = baseW
      vh.value = baseH
    }
  }

  /** 聚焦到指定画布坐标 */
  function focusOn(cx: number, cy: number, targetScale = 2.2) {
    const newW = clampW(baseW / targetScale)
    const newH = newW * (baseH / baseW)
    // 视口限制在画布内，避免聚焦到边缘点时露出大片空白
    const tx = newW >= baseW ? (baseW - newW) / 2 : Math.max(0, Math.min(baseW - newW, cx - newW / 2))
    const ty = newH >= baseH ? (baseH - newH) / 2 : Math.max(0, Math.min(baseH - newH, cy - newH / 2))
    animateTo(tx, ty, newW)
  }

  /** Fit a projected map extent into the viewport while preserving its aspect ratio. */
  function fitBounds(
    bounds: { minX: number; minY: number; maxX: number; maxY: number },
    padding = 32,
    duration = 520
  ) {
    const contentW = Math.max(1, bounds.maxX - bounds.minX)
    const contentH = Math.max(1, bounds.maxY - bounds.minY)
    const targetW = clampW(Math.max(contentW + padding * 2, (contentH + padding * 2) * (baseW / baseH)))
    const targetH = targetW * (baseH / baseW)
    const cx = (bounds.minX + bounds.maxX) / 2
    const cy = (bounds.minY + bounds.maxY) / 2
    const tx = targetW >= baseW ? (baseW - targetW) / 2 : Math.max(0, Math.min(baseW - targetW, cx - targetW / 2))
    const ty = targetH >= baseH ? (baseH - targetH) / 2 : Math.max(0, Math.min(baseH - targetH, cy - targetH / 2))
    animateTo(tx, ty, targetW, duration)
  }

  onBeforeUnmount(stopAnim)

  return {
    viewBox,
    scale,
    scalePercent,
    isDragging,
    onWheel,
    onMouseDown,
    onMouseMove,
    onMouseUp,
    zoomIn,
    zoomOut,
    reset,
    focusOn,
    fitBounds
  }
}
