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
        <span class="map-picker__tip">{{ error || '点击地图选点，拖动标记微调' }}</span>
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

const holder = ref<HTMLElement>()
const error = ref('')
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
    init()
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
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${mapKey.value}&plugin=AMap.Geocoder`
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

async function init() {
  if (!hasKey.value || !holder.value) return
  try {
    const AMap = await loadAMap()
    const center = currentPosition()
    map = new AMap.Map(holder.value, { zoom: 13, center, resizeEnable: true })
    marker = new AMap.Marker({ position: center, draggable: true })
    map.add(marker)
    map.on('click', (e: any) => {
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()
      marker.setPosition([lng, lat])
      applyPoint(lng, lat, true)
    })
    marker.on('dragend', (e: any) => {
      applyPoint(e.lnglat.getLng(), e.lnglat.getLat(), true)
    })
    geocoder = new AMap.Geocoder()
  } catch (e: any) {
    error.value = e?.message || '地图加载失败'
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
  if (map) {
    map.destroy()
    map = null
    marker = null
    geocoder = null
  }
})
</script>

<style scoped>
.map-picker__canvas {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}
.map-picker__bar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.map-picker__coords {
  font-family: monospace;
  color: var(--el-text-color-primary);
}
.map-picker__empty {
  padding: 14px;
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
