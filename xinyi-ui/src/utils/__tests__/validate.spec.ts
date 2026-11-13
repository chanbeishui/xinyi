import { describe, expect, it } from 'vitest'
import { isEmpty, isExternal, isHttp, isPathMatch, validEmail, validUsername } from '../validate'

describe('isPathMatch', () => {
  it('单星号只匹配一段路径', () => {
    expect(isPathMatch('/system/*', '/system/user')).toBe(true)
    expect(isPathMatch('/system/*', '/system/user/detail')).toBe(false)
  })

  it('双星号匹配多段', () => {
    expect(isPathMatch('/system/**', '/system/user/detail')).toBe(true)
  })

  it('不含通配符时按全等匹配', () => {
    expect(isPathMatch('/a/b', '/a/b')).toBe(true)
    expect(isPathMatch('/a/b', '/a/bc')).toBe(false)
  })

  it('正则元字符被当作普通字符', () => {
    expect(isPathMatch('/a.b', '/a.b')).toBe(true)
    expect(isPathMatch('/a.b', '/axb')).toBe(false)
  })
})

describe('isEmpty', () => {
  it('null / undefined / 空串 / 字符串 undefined 视为空', () => {
    expect(isEmpty(null)).toBe(true)
    expect(isEmpty(undefined)).toBe(true)
    expect(isEmpty('')).toBe(true)
    expect(isEmpty('undefined')).toBe(true)
  })

  it('数字 0 被判为空 —— 松散比较（0 == ""）导致，判断数字时不要用本函数', () => {
    expect(isEmpty(0)).toBe(true)
    expect(isEmpty(1)).toBe(false)
    expect(isEmpty(' ')).toBe(false)
  })
})

describe('isHttp / isExternal', () => {
  it('isHttp 识别 http 与 https', () => {
    expect(isHttp('http://example.com')).toBe(true)
    expect(isHttp('https://example.com')).toBe(true)
    expect(isHttp('/system/user')).toBe(false)
  })

  it('isExternal 识别外链协议', () => {
    expect(isExternal('https://example.com')).toBe(true)
    expect(isExternal('mailto:a@b.com')).toBe(true)
    expect(isExternal('tel:10086')).toBe(true)
    expect(isExternal('/system/user')).toBe(false)
    expect(isExternal('system/user')).toBe(false)
  })
})

describe('validUsername / validEmail', () => {
  it('用户名只放行内置白名单，且忽略首尾空格', () => {
    expect(validUsername('admin')).toBe(true)
    expect(validUsername(' admin ')).toBe(true)
    expect(validUsername('someone')).toBe(false)
  })

  it('邮箱校验', () => {
    expect(validEmail('a@b.com')).toBe(true)
    expect(validEmail('a@b')).toBe(false)
    expect(validEmail('not-an-email')).toBe(false)
  })
})
