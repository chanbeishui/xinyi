// 机动车检测预约 · 业务配置（C01，B21/B21-edit，ADMIN 域 API_155/156）
// 仅站内消息模板/公开内容/客服配置（vi.biz.* 白名单）；第三方凭据/支付开关/外部短信订阅模板走 integrations（B49/B50）。
// 固定安全项（支付上限/权限注册/水印）服务端拒绝普通配置修改；CAS 版本冲突 409 须刷新。
import { viGet, viPut } from './request'

export interface ConfigItem {
  key: string
  value: string
}

export interface ConfigPatchView {
  items: ConfigItem[]
  version: number
}

export interface ConfigAck {
  accepted: boolean
  requestId?: string | null
  version?: number | null
}

// 固定安全项：不可由普通配置绕过（与服务端 PROTECTED_KEYS 一致，前端仅做展示禁用，授权以服务端为准）
export const PROTECTED_CONFIG_KEYS = [
  'vi.biz.payment.cap.minutes',
  'vi.biz.permission.registry',
  'vi.biz.watermark'
]

export function getConfig() {
  return viGet<{ code: number; msg: string; data: ConfigPatchView }>('/api/admin/config')
}

export function putConfig(items: ConfigItem[], expectedVersion: number, reason: string, idempotencyKey: string) {
  return viPut<{ code: number; msg: string; data: ConfigAck }>(
    '/api/admin/config',
    { items, expectedVersion, reason },
    { idempotencyKey }
  )
}