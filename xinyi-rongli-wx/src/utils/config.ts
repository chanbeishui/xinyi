/**
 * 小程序端运行时配置。
 *
 * 通过 .env 注入：VITE_API_BASE 指向后端服务根地址，末尾不要带斜杠。
 */
export const API_BASE: string = (import.meta.env.VITE_API_BASE as string) || 'http://localhost:8080'

/** 联调环境微信登录 code 前缀（后端 wechat.mock=true 时，code 直接作为稳定标识） */
export const MOCK_LOGIN_CODE: string = (import.meta.env.VITE_LOGIN_CODE as string) || 'dev_owner_001'

/**
 * 是否使用演示身份登录。
 *
 * 后端 rongli.wechat.mock=true 时会把 code 直接当 openId 用，而真机 wx.login 每次返回的
 * code 都不同，若直接透传会每次都注册一个新账号（订单、车辆全为空）。演示/联调必须走
 * 固定标识，因此这里默认开启；接入真实 code2session 后把它改成 false 并删除 VITE_LOGIN_CODE。
 */
export const USE_MOCK_LOGIN: boolean = (import.meta.env.VITE_LOGIN_MODE || 'mock') !== 'wechat'

export const STORAGE = {
  access: 'rongli_access_token',
  refresh: 'rongli_refresh_token',
  role: 'rongli_active_role',
  lastRole: 'rongli_last_role',
  profile: 'rongli_profile'
} as const
