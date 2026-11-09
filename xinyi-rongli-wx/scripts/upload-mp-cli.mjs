/**
 * 用微信开发者工具 CLI 上传小程序代码。
 *
 * 与 miniprogram-ci 的差别：CLI 走开发者工具登录态，**不需要代码上传密钥，
 * 也不受 IP 白名单限制**；代价是必须在本机执行，且工具已登录并开启服务端口。
 *
 * 用法：
 *   node scripts/upload-mp-cli.mjs                    # 默认 dist/build/mp-weixin，版本 YYMMDD.1
 *   node scripts/upload-mp-cli.mjs --version 1.1.0 --desc "修复派单超时"
 *   node scripts/upload-mp-cli.mjs --port 9420        # 工具已启动但端口不同时指定
 *
 * 前置（开发者工具内一次性）：
 *   设置 → 安全设置 → 开启「服务端口」（CLI/HTTP 调用），并保持已扫码登录
 */
import { execFileSync, execSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'

const argv = process.argv.slice(2)
const opt = (name, fallback) => {
  const i = argv.indexOf(`--${name}`)
  return i >= 0 && argv[i + 1] ? argv[i + 1] : fallback
}

const project = path.resolve(opt('project', 'dist/build/mp-weixin'))
const port = opt('port', process.env.MP_PORT || '')

const today = new Date()
const stamp = String(today.getFullYear()).slice(2)
  + String(today.getMonth() + 1).padStart(2, '0')
  + String(today.getDate()).padStart(2, '0')
const version = opt('version', `${stamp}.1`)
const desc = opt('desc', `CLI 上传 ${new Date().toISOString().slice(0, 16).replace('T', ' ')}`)

if (!existsSync(path.join(project, 'project.config.json'))) {
  console.error(`[mp-cli] ${project} 里没有 project.config.json，先执行 npm run build:mp-weixin`)
  process.exit(1)
}

const appid = (() => {
  try {
    return JSON.parse(readFileSync(path.join(project, 'project.config.json'), 'utf8')).appid || '(未配置)'
  } catch {
    return '(读取失败)'
  }
})()

const CLI_CANDIDATES = [
  process.env.MP_DEVTOOLS_CLI,
  'C:\\Program Files (x86)\\Tencent\\微信web开发者工具\\cli.bat',
  'C:\\Program Files\\Tencent\\微信web开发者工具\\cli.bat',
  '/Applications/wechatwebdevtools.app/Contents/MacOS/cli',
].filter(Boolean)

const cli = CLI_CANDIDATES.find((p) => existsSync(p))
if (!cli) {
  console.error('[mp-cli] 没找到开发者工具 CLI，请用环境变量 MP_DEVTOOLS_CLI 指定路径')
  process.exit(1)
}

const args = ['upload', '--project', project, '--version', version, '--desc', desc]
if (port) args.push('--port', port)

console.log(`[mp-cli] ${cli}`)
console.log(`[mp-cli] appid=${appid} version=${version}`)
console.log(`[mp-cli] project=${project}`)

try {
  if (process.platform === 'win32') {
    const quoted = [cli, ...args].map((a) => (a.includes(' ') ? `"${a}"` : a)).join(' ')
    execSync(quoted, { stdio: 'inherit', shell: 'cmd.exe' })
  } else {
    execFileSync(cli, args, { stdio: 'inherit' })
  }
} catch (e) {
  console.error('[mp-cli] 上传失败，按顺序排查：')
  console.error('  1) 开发者工具已扫码登录')
  console.error('  2) 设置 → 安全设置 → 已开启「服务端口」')
  console.error('  3) 工具已在运行但端口不同：退出工具重试，或加 --port 指定实际端口')
  console.error('  4) 同一目录已在工具中打开，文件被占用')
  process.exit(e.status || 1)
}

console.log('[mp-cli] 上传完成。后续在公众平台：版本管理 → 设为体验版 → 提审 → 发布')
