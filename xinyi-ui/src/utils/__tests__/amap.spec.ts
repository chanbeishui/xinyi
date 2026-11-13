import { describe, expect, it } from 'vitest'
import { amapErrorMessage } from '../amapErrors'

const QUOTA = '高德地图基础LBS服务每月使用额度已达上限，请技术人员扩容'

describe('amapErrorMessage', () => {
  it('额度类错误码必须直接说明要扩容，不能兜成“稍后重试”', () => {
    expect(amapErrorMessage({ infocode: '10003' })).toBe(QUOTA)
    expect(amapErrorMessage({ infocode: '10044' })).toBe(QUOTA)
    expect(amapErrorMessage({ infocode: '10045' })).toBe(QUOTA)
  })

  it('只有 info 没有 infocode 时，通过别名表仍能映射', () => {
    expect(amapErrorMessage({ info: 'OVER_QUOTA' })).toBe(QUOTA)
    expect(amapErrorMessage({ info: 'INVALID_USER_KEY' })).toContain('Key 无效')
    expect(amapErrorMessage({ info: 'INVALID_USER_DOMAIN' })).toContain('域名')
  })

  it('错误码表未覆盖但语义是额度的，仍按扩容提示', () => {
    expect(amapErrorMessage('OVER_LIMIT')).toBe(QUOTA)
    expect(amapErrorMessage('SOME_QUOTA_EXCEEDED')).toBe(QUOTA)
  })

  it('完全未知的错误保留原始信息，便于排查', () => {
    expect(amapErrorMessage({ info: 'WEIRD', infocode: '99999' })).toBe(
      '地图服务调用失败（WEIRD / 99999）'
    )
  })

  it('空入参给出通用提示', () => {
    expect(amapErrorMessage(null)).toBe('地图服务调用失败，请稍后重试')
    expect(amapErrorMessage(undefined)).toBe('地图服务调用失败，请稍后重试')
    expect(amapErrorMessage({})).toBe('地图服务调用失败，请稍后重试')
  })
})
