# E05 PC 框架接入和双域 API 客户端 · 报告

状态：VERIFIED（REVIEWED 待独立复核人）。执行者：Qoder-Agent。

## 交付
复用 xinyi-ui 布局与权限路由，**新增独立 vi 请求适配，不改旧 sys 接口封装**。新增文件仅在 `xinyi-ui/src/api/rongli/`：
- `viError.ts`（纯模块，无框架依赖）：V6 ApiError 契约映射。`mapViError(httpStatus, body, retryAfterHeader)` → 规范 `ViApiError{httpStatus,errorCode(VI_*),msg,requestId,retryable,retryAfterSeconds,fieldErrors,conflicts}`；`VI_ERROR_HINTS` 28 个 VI_* 码→提示；`isAuthRequired/isVersionConflict/isForbidden/isNetwork` 判定。**客户端按 errorCode 分支，不用中文 msg 分支**（契约红线）。
- `request.ts`：独立 axios 实例 `viService`（**不导入、不修改 @/utils/request.ts**）。baseURL 复用 `VITE_APP_BASE_API`（经 vite 代理 rewrite），vi 路径自带 `/api`→**全链路 /api 只出现一次**。请求拦截：复用框架 `getToken()` 注入 `Authorization: Bearer`（PC=ADMIN 域=现有 sys_user JWT）+ `X-Client-Type: admin`（仅审计不增权）+ 透传 `Idempotency-Key`。响应拦截：2xx 返回契约信封 {code,msg,data|rows,total}；非 2xx 走 `mapViError`，401/VI_AUTH_REQUIRED 触发重新登录（独立 relogin 标志，不复用 request.ts 的 isRelogin），403 warning、其余 error 提示，reject ViApiError 供调用方按 errorCode 精细分支。导出 `viRequest/viGet/viPost/viPut/viPatch/viDelete`。
- `index.ts`：barrel 导出。

## 双域说明
PC 后台 = ADMIN 认证域，AdminBearerAuth = 现有框架 sys_user JWT（同一 token，无需第二套登录）。vi 适配器只是**新增一个 axios 实例 + V6 错误契约映射**，与 sys 的"恒 200 + body.code"拦截器并存、互不干扰。小程序 MINI 域（不透明令牌）在 xinyi-miniapp（E04 已建 http.ts），两端不串用。

## 验证（真实执行）
| 项 | 命令 | 结果 |
|---|---|---|
| 依赖干净安装 | `npm --prefix xinyi-ui ci` | exit 0，541 包（lockfile 有效，未改 xinyi-ui 依赖） |
| 生产构建 | `npm --prefix xinyi-ui run build:prod` | exit 0，dist/index.html 产出（ci 后复跑仍 0） |
| 适配器类型检查 | `vue-tsc --noEmit` | **api/rongli 错误 0**（首轮 10 个 axios 具名类型解析错误已修：改用 any 配置，与现有 request.ts 同风格） |
| 错误映射单测 | `node --experimental-strip-types E05/viError.test.ts` | **11 组断言 PASS**（401/409 版本·幂等·选择冲突/403/422 fieldErrors/429 Retry-After/503 权威不可用/网络层/默认码/msg 回退） |
| negative check | `git diff utils/request.ts errorCode.ts` | **均未改动**；xinyi-ui/src 改动仅 `?? api/rongli/` |

## 组件测试说明（AGENTS 约束）
xinyi-ui 无前端测试框架（package.json 无 test 脚本、无 vitest）。按根 AGENTS「前端测试框架需先约定再引入」，**未擅自加 vitest**；改用 Node 22 原生 `--experimental-strip-types` 跑纯逻辑单测（viError 映射），零新框架依赖。完整组件/E2E 测试框架选型留待约定（记 DECISIONS）。

## 既存问题（非 E05 引入，不在本任务修）
`vue-tsc --noEmit` 全项目有 **26 个既存类型错误**（`Module 'vue' has no exported member 'App'/'createApp'` 等，分布在 main.ts/plugins/directive/vue-cropper）——是项目原有的 vue-tsc 严格性问题；`build:prod`（vite/esbuild）不做全量类型检查故不受影响、构建通过。E05 未触碰这些文件，也不在本任务范围内修复（避免无关改动）。建议单列技术债任务评估。

## 边界（符合 negative_checks）
- 未生成假业务页面/假 Ack（实际 vi 后台页面由 B 系列任务实现）。
- 未改 sys 响应拦截器与错误处理。
- /api 只出现一次（baseURL 代理前缀 + 契约路径 /api，rewrite 后单次）。

## 完成判据对照（E05 TASK.md）
- [x] 保留原 PC 框架行为（request.ts/errorCode.ts 未改，build:prod 通过）
- [x] 新请求适配的组件测试（viError 11 断言）及生产构建
- [x] 不生成假 Ack 跳过未实现业务页面
- [x] 保存实际运行证据：机器可读证据入库 `evidence/E05.json`（每次 run 的 command/exitCode/checksRun 已由 ledger sha256 链接）；原始 `npm-ci.log/build-prod.log/vue-tsc.log` 为本地产物（仓库 `*.log` 全局忽略，与 E03/E04 一致）。
- [ ] 独立复核（REVIEWED 需复核人≠实现人）
