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
      <div ref="holder" class="map-picker__canvas" :style="{ height }"></div>
      <div class="map-picker__bar">
        <span class="map-picker__coords">{{ coordText }}</span>
        <span class="map-picker__tip" :class="{ 'is-error': !!error }" :title="error || tip">
          {{ error || tip }}
        </span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getConfigKey } from '@/api/system/config'

/**
 * 经纬度地图选点。
 *
 * 坐标系必须是 gcj02：小程序端 uni.getLocation 用的是 gcj02，高德地图也是 gcj02，
 * 两者对齐后派单半径判定才不会偏几百米。换成 WGS84 的底图（如 OSM）会直接引入偏移。
 *
 * 供应商与凭据走系统参数，便于运营在「参数设置」里改，不必重新打包：
 *   rongli.map.provider      amap（当前仅支持）/ 留空表示未启用
 *   rongli.map.key           高德 Web 端（JS API）Key
 *   rongli.map.securityCode  JS API 2.0 安全密钥（jscode）
 */
const props = withDefaults(
  defineProps<{
    lng?: string | number
    lat?: string | number
    height?: string
  }>(),
  { height: '260px' }
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
const provider = ref('')
const mapKey = ref('')
const securityCode = ref('')

const unsupported = computed(() => !!provider.value && provider.value !== 'amap')
const hasKey = computed(() => !!mapKey.value && !unsupported.value)

const coordText = computed(() => {
  const lng = props.lng === '' || props.lng === undefined ? '—' : props.lng
  const lat = props.lat === '' || props.lat === undefined ? '—' : props.lat
  return `${lng}, ${lat}`
})

let map: any = null
let marker: any = null
let geocoder: any = null
let loader: Promise<any> | null = null
let resizeObserver: ResizeObserver | null = null
let resizeRaf = 0
let creating: Promise<any> | null = null

function readConfig(key: string): Promise<string> {
  return getConfigKey(key)
    .then((res: any) => String((res && (res.msg ?? res.data)) ?? '').trim())
    .catch(() => '')
}

async function loadConfig() {
  const [p, k, s] = await Promise.all([
    readConfig('rongli.map.provider'),
    readConfig('rongli.map.key'),
    readConfig('rongli.map.securityCode')
  ])
  provider.value = p.toLowerCase()
  mapKey.value = k
  securityCode.value = s
  configLoaded.value = true
  if (hasKey.value) {
    await nextTick()
    await init()
  }
}

function loadAMap(): Promise<any> {
  if (loader) return loader
  loader = new Promise((resolve, reject) => {
    const existing = (window as any).AMap
    if (existing) {
      resolve(existing)
      return
    }
    // JS API 2.0 的安全密钥；只在前端使用，需在高德控制台配置域名白名单
    if (securityCode.value) {
      ;(window as any)._AMapSecurityConfig = { securityJsCode: securityCode.value }
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${mapKey.value}&plugin=AMap.Geocoder,AMap.PlaceSearch`
    script.async = true
    script.onload = () => resolve((window as any).AMap)
    script.onerror = () => reject(new Error('高德地图脚本加载失败，请检查 Key 与网络'))
    document.head.appendChild(script)
  })
  return loader
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
  } catch (e: any) {
    error.value = e?.message || '地图加载失败'
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
    const hit =
      (await withTimeout(geocodeByKeyword(AMap, keyword), 8000)) ||
      (await withTimeout(searchByPlace(AMap, keyword), 8000))
    if (!hit || !Number.isFinite(hit.lng) || !Number.isFinite(hit.lat)) {
      tip.value = '点击地图选点，拖动标记微调'
      return { ok: false, message: `未能定位到「${keyword}」，请补充省 / 市 / 区或换用更完整的地址` }
    }
    marker.setPosition([hit.lng, hit.lat])
    map.setZoomAndCenter(15, [hit.lng, hit.lat])
    emit('update:lng', hit.lng.toFixed(6))
    emit('update:lat', hit.lat.toFixed(6))
    emit('update:address', hit.address)
    tip.value = `已定位到：${hit.address}`
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

// 外部（手动输入或切换记录）改了坐标时，让标记跟随，不反向触发地址回填
watch(
  () => [props.lng, props.lat],
  () => {
    if (!map || !marker || !hasPoint()) return
    marker.setPosition([Number(props.lng), Number(props.lat)])
    map.setCenter([Number(props.lng), Number(props.lat)])
  }
)

onMounted(loadConfig)

onBeforeUnmount(() => {
  if (resizeRaf) cancelAnimationFrame(resizeRaf)
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (map) {
    map.destroy()
    map = null
    marker = null
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

.map-picker__canvas {
  display: block;
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: #f2f4f7;
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
