/**
 * 小程序代码上传（体验版前置步骤）。
 *
 * 微信要求上传必须持有「代码上传密钥」，且调用方的出口 IP 在公众平台的
 * IP 白名单里。所以这里只在**本机**执行，不放进 GitHub Actions——
 * Actions 的出口 IP 不固定，无法稳定加白。
 *
 * 用法：
 *   npm run upload:mp                       # 先 build 再上传
 *   MP_VERSION=1.2.0 npm run upload:mp      # 指定版本号
 *
 * 准备（一次性）：
 *   1. 公众平台 → 开发管理 → 开发设置 → 小程序代码上传密钥 → 生成并下载
 *   2. 同一页把「IP 白名单」加上你本机的公网出口 IP
 *   3. 把下载的密钥放到 xinyi-rongli-wx/private.upload.key（已在 .gitignore）
 */
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = path.dirname(path.dirname(fileURLToPath(import.meta.url)))
const manifest = JSON.parse(readFileSync(path.join(root, 'src/manifest.json'), 'utf8'))
const appid = manifest['mp-weixin']?.appid

if (!appid) {
  console.error('[upload] src/manifest.json 里没有 mp-weixin.appid')
  process.exit(1)
}

const dist = path.join(root, 'dist/build/mp-weixin')
if (!existsSync(path.join(dist, 'app.json'))) {
  console.error('[upload] 找不到构建产物，先执行 npm run build:mp-weixin')
  process.exit(1)
}

const keyPath = process.env.MP_PRIVATE_KEY || path.join(root, 'private.upload.key')
if (!existsSync(keyPath)) {
  console.error(`[upload] 缺少上传密钥：${keyPath}`)
  console.error('[upload] 公众平台 → 开发管理 → 开发设置 → 小程序代码上传密钥')
  process.exit(1)
}

// 版本号默认用「YYMMDD.序号」，微信侧只要求单调递增，不强求语义化
const today = new Date()
const stamp = String(today.getFullYear()).slice(2)
  + String(today.getMonth() + 1).padStart(2, '0')
  + String(today.getDate()).padStart(2, '0')
const version = process.env.MP_VERSION || `${stamp}.1`
const desc = process.env.MP_DESC || `uni-app build ${new Date().toISOString().slice(0, 16).replace('T', ' ')}`

console.log(`[upload] appid=${appid} version=${version}`)
console.log(`[upload] 产物=${dist}`)

execFileSync('npx', [
  '--yes', 'miniprogram-ci', 'upload',
  '--pp', dist,
  '--pkp', keyPath,
  '--appid', appid,
  '--uv', version,
  '--ud', desc,
  '--enable-es6', 'true',
  '--threads', '1'
], { stdio: 'inherit', shell: process.platform === 'win32' })

console.log('[upload] 上传完成。到公众平台 → 版本管理 把它设为体验版。')
