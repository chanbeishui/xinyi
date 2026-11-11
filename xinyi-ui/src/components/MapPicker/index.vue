<template>
  <div class="map-picker">
    <div v-if="!configLoaded" class="map-picker__empty">正在读取地图配置…</div>
    <div v-else-if="unsupported" class="map-picker__empty">
      当前地图供应商为「{{ provider }}」，尚未接入；请手动填写经纬度。
    </div>
    <div v-else-if="!hasKey" class="map-picker__empty">
      未配置地图服务（系统参数 rongli.map.provider / rongli.map.key），请手动填写经纬度。
    </div>
    <template v-else>
      <div class="map-picker__stage">
        <div ref="holder" class="map-picker__canvas" :style="{ height }"></div>
        <div v-if="loadFailed" class="map-picker__mask">{{ error }}</div>
      </div>
      <div class="map-picker__bar">
        <span class="map-picker__coords">
          {{ coordText }}
          <em v-if="radiusText" class="map-picker__range">服务范围 {{ radiusText }}</em>
        </span>
        <span class="map-picker__tip" :class="{ 'is-error': !!error }" :title="error || tip">
          {{ error || tip }}
        </span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { amapErrorMessage, readAMapConfig, loadAMap } from '@/utils/amap'

/**
 * 经纬度地图选点 + 服务范围圈。
 *
 * 坐标系必须是 gcj02：小程序端 uni.getLocation 用的是 gcj02，高德地图也是 gcj02，
 * 两者对齐后派单半径判定才不会偏几百米。换成 WGS84 的底图（如 OSM）会直接引入偏移。
 *
 * Key、安全密钥的读取与脚本加载统一收在 utils/amap，省市区下拉也在用同一份配置。
 */
const props = withDefaults(
  defineProps<{
    lng?: string | number
    lat?: string | number
    height?: string
    /** 服务半径（米）。大于 0 时在地图上画服务范围圈 */
    radius?: number | string
  }>(),
  { height: '260px', radius: 0 }
)

const emit = defineEmits<{
  (e: 'update:lng', value: string): void
  (e: 'update:lat', value: string): void
  (e: 'update:address', value: string): void
}>()

type GeoPoint = { lng: number; lat: number; address: string }
type LocateResult = { ok: boolean; message?: string; address?: string }

const holder = ref<HTMLElement>()
const error = ref('')
const tip = ref('点击地图选点，拖动标记微调')
const configLoaded = ref(false)
const loadFailed = ref(false)
const provider = ref('')
const mapKey = ref('')

const unsupported = computed(() => !!provider.value && provider.value !== 'amap')
const hasKey = computed(() => !!mapKey.value && !unsupported.value)

const coordText = computed(() => {
  const lng = props.lng === '' || props.lng === undefined ? '—' : props.lng
  const lat = props.lat === '' || props.lat === undefined ? '—' : props.lat
  return `${lng}, ${lat}`
})

const radiusValue = computed(() => {
  const value = Number(props.radius)
  return Number.isFinite(value) && value > 0 ? value : 0
})

const radiusText = computed(() => {
  const value = radiusValue.value
  if (!value) return ''
  return value >= 1000 ? `${(value / 1000).toFixed(1)} km` : `${Math.round(value)} m`
})

let map: any = null
let marker: any = null
let circle: any = null
let geocoder: any = null
let resizeObserver: ResizeObserver | null = null
let resizeRaf = 0
let fitTimer: any = 0
let creating: Promise<any> | null = null

async function loadConfig() {
  try {
    const config = await readAMapConfig()
    provider.value = config.provider
    mapKey.value = config.key
  } finally {
    configLoaded.value = true
  }
  if (hasKey.value) {
    await nextTick()
    await init()
  }
}

function hasPoint(): boolean {
  return props.lng !== '' && props.lng !== undefined && props.lat !== '' && props.lat !== undefined
}

function currentPosition(): [number, number] {
  return hasPoint() ? [Number(props.lng), Number(props.lat)] : [110.925, 21.663]
}

/** 回写坐标；resolveAddress 为真时同时逆地理编码回填地址 */
function applyPoint(lng: number, lat: number, resolveAddress: boolean) {
  emit('update:lng', lng.toFixed(6))
  emit('update:lat', lat.toFixed(6))
  if (resolveAddress && geocoder) {
    geocoder.getAddress([lng, lat], (status: string, result: any) => {
      if (status === 'complete' && result && result.regeocode) {
        emit('update:address', result.regeocode.formattedAddress)
      }
    })
  }
}

/**
 * 等待容器拿到真实尺寸。
 *
 * 抽屉有 0 → 560px 的展开动画，组件挂载的瞬间容器宽度是 0；此时初始化高德，
 * 画布会按 0 或极小宽度铺出瓦片，之后再变宽也不会自动重绘——这正是"地图只有
 * 细窄一条、标记贴边、右侧一片空白"的成因。
 */
function waitForSize(el: HTMLElement, frames = 30): Promise<boolean> {
  return new Promise((resolve) => {
    let left = frames
    const tick = () => {
      if (el.clientWidth > 0 && el.clientHeight > 0) return resolve(true)
      if (left-- <= 0) return resolve(false)
      requestAnimationFrame(tick)
    }
    tick()
  })
}

/** 容器尺寸变化（抽屉展开/收起、窗口缩放）时重算画布，并把标记拉回视野中心 */
function observeResize() {
  if (!holder.value || typeof ResizeObserver === 'undefined') return
  resizeObserver = new ResizeObserver(() => {
    if (resizeRaf) cancelAnimationFrame(resizeRaf)
    resizeRaf = requestAnimationFrame(() => {
      resizeRaf = 0
      if (!map) return
      map.resize()
      if (hasPoint()) map.setZoomAndCenter(map.getZoom(), [Number(props.lng), Number(props.lat)])
    })
  })
  resizeObserver.observe(holder.value)
}

/** 服务范围圈：半径 ≤ 0 或还没有坐标时摘掉，避免画在默认点上造成误读 */
function syncCircle() {
  if (!circle || !map) return
  const radius = radiusValue.value
  if (!radius || !hasPoint()) {
    circle.setMap(null)
    return
  }
  circle.setMap(map)
  circle.setCenter([Number(props.lng), Number(props.lat)])
  circle.setRadius(radius)
}

/** 把视野收到「刚好装下服务范围」；只在半径变化时做，点选地图时不动用户视野 */
function scheduleFit(delay = 200) {
  if (fitTimer) clearTimeout(fitTimer)
  fitTimer = setTimeout(() => {
    fitTimer = 0
    if (!map || !circle || !hasPoint() || !radiusValue.value) return
    map.setFitView([circle], false, [24, 24, 24, 24], 17)
  }, delay)
}

async function createMap(): Promise<any> {
  if (map) return map
  if (!hasKey.value) return null
  // 挂载时初始化和「搜索」点击可能同时发生，靠同一个 Promise 复用，避免在同一容器建两张图
  if (creating) return creating
  creating = (async () => {
    const AMap = await loadAMap()
    await nextTick()
    const el = holder.value
    if (!el) return null
    await waitForSize(el)
    const center = currentPosition()
    map = new AMap.Map(el, { zoom: 13, center, resizeEnable: true, viewMode: '2D' })
    circle = new AMap.Circle({
      center,
      radius: radiusValue.value || 1,
      strokeColor: '#175cd3',
      strokeWeight: 1.5,
      strokeOpacity: 0.9,
      strokeStyle: 'dashed',
      strokeDasharray: [6, 4],
      fillColor: '#175cd3',
      fillOpacity: 0.1,
      zIndex: 20
    })
    map.add(circle)
    marker = new AMap.Marker({ position: center, draggable: true, cursor: 'move', zIndex: 120 })
    map.add(marker)
    map.on('click', (e: any) => {
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()
      marker.setPosition([lng, lat])
      applyPoint(lng, lat, true)
      tip.value = '已移动到点选位置'
    })
    marker.on('dragend', (e: any) => {
      applyPoint(e.lnglat.getLng(), e.lnglat.getLat(), true)
      tip.value = '已移动到拖拽位置'
    })
    geocoder = new AMap.Geocoder()
    observeResize()
    syncCircle()
    scheduleFit(260)
    return AMap
  })()
  try {
    return await creating
  } finally {
    creating = null
  }
}

async function init() {
  if (!hasKey.value || !holder.value) return
  try {
    await createMap()
    loadFailed.value = false
  } catch (e: any) {
    error.value = e?.message || '地图加载失败'
    loadFailed.value = true
  }
}

/** 单次解析超时保护：插件异常时回调可能永远不来，避免按钮一直转圈 */
function withTimeout<T>(task: Promise<T>, ms: number): Promise<T | null> {
  return new Promise((resolve) => {
    const timer = setTimeout(() => resolve(null), ms)
    task
      .then((v) => {
        clearTimeout(timer)
        resolve(v)
      })
      .catch(() => {
        clearTimeout(timer)
        resolve(null)
      })
  })
}

/** 结构化地址优先走地理编码（支持省/市/区 + 详细地址） */
function geocodeByKeyword(AMap: any, keyword: string): Promise<GeoPoint | null> {
  return new Promise((resolve) => {
    try {
      geocoder = geocoder || new AMap.Geocoder({ city: '全国' })
      geocoder.getLocation(keyword, (status: string, result: any) => {
        if (status === 'error') {
          // 额度用尽、Key 无效这类服务级错误直接摆到状态条上，由 locate 原样返回给页面
          error.value = amapErrorMessage(result)
          return resolve(null)
        }
        const geo = result && result.geocodes && result.geocodes[0]
        if (status !== 'complete' || !geo || !geo.location) return resolve(null)
        resolve({
          lng: Number(geo.location.getLng()),
          lat: Number(geo.location.getLat()),
          address: geo.formattedAddress || keyword
        })
      })
    } catch {
      resolve(null)
    }
  })
}

/** 地名/小区等 POI 关键词用 POI 检索兜底 */
function searchByPlace(AMap: any, keyword: string): Promise<GeoPoint | null> {
  return new Promise((resolve) => {
    try {
      const search = new AMap.PlaceSearch({ pageSize: 1, pageIndex: 1, citylimit: false })
      search.search(keyword, (status: string, result: any) => {
        if (status === 'error') {
          error.value = amapErrorMessage(result)
          return resolve(null)
        }
        const poi = result && result.poiList && result.poiList.pois && result.poiList.pois[0]
        if (status !== 'complete' || !poi || !poi.location) return resolve(null)
        resolve({
          lng: Number(poi.location.getLng()),
          lat: Number(poi.location.getLat()),
          address: `${poi.pname || ''}${poi.cityname || ''}${poi.adname || ''}${poi.address || poi.name || ''}`
        })
      })
    } catch {
      resolve(null)
    }
  })
}

/** 按地址搜索并定位；返回值交给调用方决定用什么文案提示 */
async function locate(address: string): Promise<LocateResult> {
  const keyword = String(address ?? '').replace(/\s+/g, ' ').trim()
  if (!keyword) {
    return { ok: false, message: '请先输入要搜索的地址，再点击「搜索」' }
  }
  if (!configLoaded.value) {
    return { ok: false, message: '地图配置还在读取，请稍候重试' }
  }
  if (unsupported.value) {
    return { ok: false, message: `当前地图供应商「${provider.value}」尚未接入，无法搜索定位` }
  }
  if (!hasKey.value) {
    return { ok: false, message: '未配置地图服务，无法搜索定位，请手动填写经纬度' }
  }
  error.value = ''
  tip.value = '正在搜索该地址…'
  try {
    const AMap = await createMap()
    if (!AMap || !map || !marker) {
      tip.value = '点击地图选点，拖动标记微调'
      return { ok: false, message: '地图尚未就绪，请稍候重试' }
    }
    const geocoded = await withTimeout(geocodeByKeyword(AMap, keyword), 8000)
    // 地理编码是服务级错误（额度/Key）时不再拿 POI 检索重试，否则只会再错一次
    const hit = geocoded || (error.value ? null : await withTimeout(searchByPlace(AMap, keyword), 8000))
    if (!hit || !Number.isFinite(hit.lng) || !Number.isFinite(hit.lat)) {
      tip.value = '点击地图选点，拖动标记微调'
      return {
        ok: false,
        message: error.value || `未能定位到「${keyword}」，请补充省 / 市 / 区或换用更完整的地址`
      }
    }
    marker.setPosition([hit.lng, hit.lat])
    if (circle) {
      circle.setCenter([hit.lng, hit.lat])
      circle.setRadius(radiusValue.value || 1)
    }
    map.setZoomAndCenter(15, [hit.lng, hit.lat])
    emit('update:lng', hit.lng.toFixed(6))
    emit('update:lat', hit.lat.toFixed(6))
    emit('update:address', hit.address)
    tip.value = `已定位到：${hit.address}`
    // 配了半径就把视野收到服务范围，否则停在街道级
    if (radiusValue.value) scheduleFit(120)
    // 抽屉里地图在地址输入框下方，定位成功顺手滚到可见位置
    holder.value?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    return { ok: true, address: hit.address, message: `已在地图上定位到「${hit.address}」` }
  } catch (e: any) {
    const message = e?.message || '地址解析失败，请检查网络后重试'
    error.value = message
    tip.value = '点击地图选点，拖动标记微调'
    return { ok: false, message }
  }
}

// 外部（点选、拖拽、搜索、手动输入）改了坐标时，让标记与服务范围圈跟着走
watch(
  () => [props.lng, props.lat],
  () => {
    if (!map || !marker || !hasPoint()) return
    marker.setPosition([Number(props.lng), Number(props.lat)])
    map.setCenter([Number(props.lng), Number(props.lat)])
    syncCircle()
  }
)

// 服务半径随表单实时变化：圈跟着放大缩小，并把视野收到刚好能看到整个范围
watch(
  () => props.radius,
  () => {
    syncCircle()
    scheduleFit()
  }
)

onMounted(loadConfig)

onBeforeUnmount(() => {
  if (resizeRaf) cancelAnimationFrame(resizeRaf)
  if (fitTimer) clearTimeout(fitTimer)
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (map) {
    map.destroy()
    map = null
    marker = null
    circle = null
    geocoder = null
  }
})

defineExpose({ locate })
</script>

<style scoped>
.map-picker {
  width: 100%;
  min-width: 0;
}

.map-picker__stage {
  position: relative;
  width: 100%;
}

.map-picker__canvas {
  display: block;
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: #f2f4f7;
}

/* 地图彻底起不来（Key 无效、域名未白名单、额度用尽）时，把原因铺在画布上，
   不要只留一行小字——这种错误运营自己解决不了，必须一眼能看见该找谁 */
.map-picker__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px 20px;
  border: 1px solid #fecdca;
  border-radius: 8px;
  background: #fef3f2;
  color: #b42318;
  font-size: 12.5px;
  line-height: 20px;
  text-align: center;
}

.map-picker__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
  font-size: 12px;
  line-height: 18px;
  color: var(--el-text-color-secondary);
}

.map-picker__coords {
  flex: 0 0 auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 11.5px;
  color: var(--el-text-color-primary);
}

.map-picker__range {
  margin-left: 10px;
  font-style: normal;
  font-size: 12px;
  color: #175cd3;
}

.map-picker__tip {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.map-picker__tip.is-error {
  color: var(--el-color-danger);
}

.map-picker__empty {
  padding: 14px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  font-size: 12px;
  line-height: 18px;
  color: var(--el-text-color-secondary);
}
</style>
