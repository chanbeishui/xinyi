// E05 · vi 错误映射适配器单测（纯逻辑，无框架依赖）
// 运行：node --experimental-strip-types docs/implementation/vehicle/E05/viError.test.ts
// 不引入 vitest（AGENTS 要求前端测试框架先约定）；用 Node22 原生类型剥离跑断言。
import assert from 'node:assert'
import { mapViError, isAuthRequired, isVersionConflict, isForbidden, isNetwork } from '../../../../xinyi-ui/src/api/rongli/viError.ts'

let n = 0
const ok = (cond: boolean, label: string) => { assert.ok(cond, label); n++ }

// 401 → 需重新登录
let e = mapViError(401, { errorCode: 'VI_AUTH_REQUIRED', msg: 'x' })
ok(e.httpStatus === 401 && e.errorCode === 'VI_AUTH_REQUIRED' && isAuthRequired(e), '401 auth required')

// 409 版本/幂等/选择冲突均判为可刷新冲突
e = mapViError(409, { errorCode: 'VI_VERSION_CONFLICT' }); ok(isVersionConflict(e), '409 version conflict')
e = mapViError(409, { errorCode: 'VI_IDEMPOTENCY_CONFLICT' }); ok(isVersionConflict(e), '409 idempotency conflict')
e = mapViError(409, { errorCode: 'VI_SELECTION_CONFLICT', data: { conflicts: [{ code: 'ALREADY_ALLOCATED' }] } })
ok(isVersionConflict(e) && e.conflicts.length === 1, '409 selection conflict carries conflicts')

// 403 无权（warning，不触发重新登录）
e = mapViError(403, { errorCode: 'VI_FORBIDDEN' }); ok(isForbidden(e) && !isAuthRequired(e), '403 forbidden')

// 422 领域前置不满足，带 fieldErrors
e = mapViError(422, { errorCode: 'VI_DATA_INVALID', data: { fieldErrors: [{ field: 'amount' }] } })
ok(e.fieldErrors.length === 1 && e.errorCode === 'VI_DATA_INVALID', '422 field errors')

// 429 限流：retryable + Retry-After 头解析
e = mapViError(429, undefined, '30'); ok(e.retryable === true && e.retryAfterSeconds === 30 && e.errorCode === 'VI_RATE_LIMITED', '429 retry-after')

// 503 权限权威源不可用：默认 retryable，不放行
e = mapViError(503, undefined); ok(e.errorCode === 'VI_AUTHORITY_UNAVAILABLE' && e.retryable === true, '503 authority unavailable')

// 网络层（无 HTTP 状态）→ status 0 + VI_NETWORK
e = mapViError(undefined, undefined); ok(e.httpStatus === 0 && isNetwork(e) && e.errorCode === 'VI_NETWORK', 'network error')

// 无 errorCode 时按状态派生默认码
e = mapViError(404, {}); ok(e.errorCode === 'VI_NOT_FOUND', '404 default code')

// 无 msg 时回退到 errorCode 提示（客户端按 errorCode 分支，不依赖中文 msg）
e = mapViError(403, { errorCode: 'VI_FORBIDDEN' }); ok(e.msg === '无权执行此操作', 'msg fallback to hint')

console.log(`viError 单测 PASS：${n} 组断言（纯逻辑，非业务集成）`)
