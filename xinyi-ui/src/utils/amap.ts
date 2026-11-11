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

const QUOTA_MESSAGE = '高德地图基础LBS服务每月使用额度已达上限，请技术人员扩容'

/**
 * 高德错误码 → 给业务人员看的说明。
 *
 * 外部接口的额度/权限问题最忌讳兜一句「加载失败，请稍后重试」——运营只会反复刷新，
 * 真正要行动的人（技术人员扩容、加白名单、换 Key）永远看不到信号。所以这里按错误码
 * 把该找谁、要做什么直接写进提示语。
 */
const AMAP_ERROR_MESSAGES: Record<string, string> = {
  '10001': '高德地图 Key 无效，请技术人员检查系统参数 rongli.map.key',
  '10002': '高德地图服务暂不可用，请稍后重试',
  '10003': QUOTA_MESSAGE,
  '10004': '高德地图调用过于频繁，请稍后重试',
  '10005': '高德地图安全密钥（jscode）不正确，请技术人员检查 rongli.map.securityCode',
  '10006': '当前服务器 IP 不在高德地图 Key 的白名单内，请技术人员配置',
  '10007': '当前域名未加入高德地图 Key 的白名单，请技术人员配置',
  '10008': '高德地图数字签名校验失败，请技术人员检查 Key 配置',
  '10009': '高德地图 Key 与当前应用不匹配，请技术人员确认 Key 类型',
  '10010': '高德地图 Key 与平台不匹配，需使用「Web 端（JS API）」类型的 Key',
  '10011': '当前服务器不在高德地图 Key 的白名单内，请技术人员配置',
  '10012': '高德地图 Key 未开通该服务或配额不足，请技术人员在控制台确认并扩容',
  '10013': '高德地图 Key 已被删除，请技术人员更换',
  '10014': '高德地图安全密钥校验失败，请技术人员检查 rongli.map.securityCode',
  '10044': QUOTA_MESSAGE,
  '10045': QUOTA_MESSAGE,
  '20000': '高德地图服务异常，请稍后重试',
  '20001': '高德地图请求参数有误，请检查地址内容'
}

/** 部分回调只带 info 不带 infocode，这里补一层映射 */
const AMAP_ERROR_CODES: Record<string, string> = {
  INVALID_USER_KEY: '10001',
  SERVICE_NOT_AVAILABLE: '10002',
  DAILY_QUERY_OVER_LIMIT: '10003',
  ACCESS_TOO_FREQUENT: '10004',
  INVALID_USER_SCODE: '10005',
  INVALID_USER_IP: '10006',
  INVALID_USER_DOMAIN: '10007',
  INVALID_USER_SIGNATURE: '10008',
  INVALID_USER_PID: '10009',
  USERKEY_PLAT_NOMATCH: '10010',
  INVALID_USER_SERVER: '10011',
  INSUFFICIENT_PRIVILEGES: '10012',
  USER_KEY_RECYCLED: '10013',
  SECURITY_CODE_ERROR: '10014',
  USER_DAILY_QUERY_OVER_LIMIT: '10044',
  OVER_QUOTA: '10003',
  UNKNOWN_ERROR: '20000',
  INVALID_PARAMS: '20001'
}

/** 把高德回调里的 status/result 转成可直接展示的中文提示 */
export function amapErrorMessage(result?: { info?: string; infocode?: string } | string | null): string {
  const info = typeof result === 'string' ? result : String(result?.info || '')
  const infocode = typeof result === 'string' ? '' : String(result?.infocode || '')
  const mapped = AMAP_ERROR_MESSAGES[infocode] || AMAP_ERROR_MESSAGES[AMAP_ERROR_CODES[info.toUpperCase()]]
  if (mapped) return mapped
  // 兜底：错误码表没覆盖的新码，只要语义是额度，就仍按「扩容」提示
  if (/OVER_LIMIT|OVER_QUOTA|QUOTA|LIMIT_EXCEEDED/.test(info.toUpperCase())) return QUOTA_MESSAGE
  if (info || infocode) return `地图服务调用失败（${info || '未知错误'}${infocode ? ` / ${infocode}` : ''}）`
  return '地图服务调用失败，请稍后重试'
}
