import { ref, computed } from 'vue'

export function useSvgPanZoom(
  initialW: number,
  initialH: number,
  minScale = 0.4,
  maxScale = 4
) {
  const vx = ref(0)
  const vy = ref(0)
  const vw = ref(initialW)
  const vh = ref(initialH)

  const viewBoxStr = computed(() => `${vx.value} ${vy.value} ${vw.value} ${vh.value}`)
  const scale = computed(() => initialW / vw.value)
  const scalePercent = computed(() => Math.round(scale.value * 100) + '%')

  let dragging = false
  let lastX = 0
  let lastY = 0

  function onWheel(e: WheelEvent) {
    const target = e.currentTarget as SVGElement
    const rect = target.getBoundingClientRect()
    const mx = vx.value + ((e.clientX - rect.left) / rect.width) * vw.value
    const my = vy.value + ((e.clientY - rect.top) / rect.height) * vh.value
    const factor = e.deltaY > 0 ? 1.12 : 0.89
    const newW = Math.max(initialW / maxScale, Math.min(initialW / minScale, vw.value * factor))
    const newH = newW * (initialH / initialW)
    vx.value = mx - ((e.clientX - rect.left) / rect.width) * newW
    vy.value = my - ((e.clientY - rect.top) / rect.height) * newH
    vw.value = newW
    vh.value = newH
  }

  function onMouseDown(e: MouseEvent) {
    dragging = true
    lastX = e.clientX
    lastY = e.clientY
    ;(e.currentTarget as SVGElement).style.cursor = 'grabbing'
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

  function onMouseUp(e?: MouseEvent) {
    dragging = false
    if (e?.currentTarget) {
      ;(e.currentTarget as SVGElement).style.cursor = 'grab'
    }
  }

  function reset() {
    vx.value = 0
    vy.value = 0
    vw.value = initialW
    vh.value = initialH
  }

  function zoomIn() {
    const factor = 0.8
    const newW = Math.max(initialW / maxScale, vw.value * factor)
    const newH = newW * (initialH / initialW)
    const cx = vx.value + vw.value / 2
    const cy = vy.value + vh.value / 2
    vx.value = cx - newW / 2
    vy.value = cy - newH / 2
    vw.value = newW
    vh.value = newH
  }

  function zoomOut() {
    const factor = 1.25
    const newW = Math.min(initialW / minScale, vw.value * factor)
    const newH = newW * (initialH / initialW)
    const cx = vx.value + vw.value / 2
    const cy = vy.value + vh.value / 2
    vx.value = cx - newW / 2
    vy.value = cy - newH / 2
    vw.value = newW
    vh.value = newH
  }

  function focusOn(cx: number, cy: number, targetScale = 2.5) {
    const newW = initialW / targetScale
    const newH = newW * (initialH / initialW)
    vx.value = cx - newW / 2
    vy.value = cy - newH / 2
    vw.value = newW
    vh.value = newH
  }

  return {
    vx, vy, vw, vh,
    viewBoxStr, scale, scalePercent,
    onWheel, onMouseDown, onMouseMove, onMouseUp,
    reset, zoomIn, zoomOut, focusOn,
  }
}
