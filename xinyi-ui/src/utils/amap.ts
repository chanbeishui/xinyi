import { getConfigKey } from '@/api/system/config'

/**
 * 高德 JS API 的统一加载入口。
 *
 * 地图组件和「省市区下拉」都要用高德（前者画图、后者取行政区划），但浏览器的
 * 脚本只能插一次、Key 也只能读一次，所以配置读取与脚本加载都收在这里，并按
 * Promise 缓存，避免两个组件各自插一份 script。
 *
 * 供应商与凭据仍走系统参数，运营在「参数设置」里改完刷新即可，不必重新打包：
 *   rongli.map.provider      amap（当前仅支持）/ 留空表示未启用
 *   rongli.map.key           高德 Web 端（JS API）Key
 *   rongli.map.securityCode  JS API 2.0 安全密钥（jscode）
 */
export type AMapConfig = {
  provider: string
  key: string
  securityCode: string
  /** provider 为空（未启用）或就是 amap 时为真 */
  supported: boolean
}

/** 一次性把需要的高德插件都带上，后续调用方不必再 AMap.plugin 等待 */
const PLUGINS = ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.DistrictSearch']

let configTask: Promise<AMapConfig> | null = null
let scriptTask: Promise<any> | null = null

function readConfig(key: string): Promise<string> {
  return getConfigKey(key)
    .then((res: any) => String((res && (res.msg ?? res.data)) ?? '').trim())
    .catch(() => '')
}

export function readAMapConfig(): Promise<AMapConfig> {
  if (!configTask) {
    configTask = Promise.all([
      readConfig('rongli.map.provider'),
      readConfig('rongli.map.key'),
      readConfig('rongli.map.securityCode')
    ]).then(([provider, key, securityCode]) => {
      const name = provider.toLowerCase()
      return { provider: name, key, securityCode, supported: !name || name === 'amap' }
    })
    // 读配置失败不该被永久缓存：下次进页面还能重试
    configTask.catch(() => {
      configTask = null
    })
  }
  return configTask
}

export function loadAMap(): Promise<any> {
  if (scriptTask) return scriptTask
  scriptTask = (async () => {
    const config = await readAMapConfig()
    if (!config.supported) {
      throw new Error(`当前地图供应商「${config.provider}」尚未接入`)
    }
    if (!config.key) {
      throw new Error('未配置地图服务（系统参数 rongli.map.key）')
    }
    const existing = (window as any).AMap
    if (existing) return existing
    return new Promise((resolve, reject) => {
      // JS API 2.0 的安全密钥；只在前端使用，需在高德控制台配置域名白名单
      if (config.securityCode) {
        ;(window as any)._AMapSecurityConfig = { securityJsCode: config.securityCode }
      }
      const script = document.createElement('script')
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${config.key}&plugin=${PLUGINS.join(',')}`
      script.async = true
      script.onload = () => resolve((window as any).AMap)
      // 高德在 Key 无效 / 域名未白名单 / 额度用尽时会直接拒绝脚本，浏览器只报 onerror，
      // 拿不到错误码，所以把三种可能都写出来，别让运营以为是网络问题一直刷新
      script.onerror = () =>
        reject(
          new Error('高德地图脚本加载失败：可能是 Key 无效、域名未加入白名单，或本月调用额度已用尽（额度用尽请联系技术人员扩容）')
        )
      document.head.appendChild(script)
    })
  })()
  // 加载失败后允许重试（例如刚配好 Key、网络恢复）
  scriptTask.catch(() => {
    scriptTask = null
  })
  return scriptTask
}

export { amapErrorMessage } from './amapErrors'
