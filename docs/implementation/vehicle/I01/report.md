# I01 · 统一微信登录与默认车主 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-IDENTITY-CONSISTENCY-20261022。
接口：API_001_post（POST /api/common/login/wechat）、API_005_get（GET /api/common/profile）。主责任表：vi_account、vi_account_security（并读写 vi_session/vi_session_token，只读 vi_driver_profile/vi_employee_binding/vi_station_grant 计算资格）。

## 交付（对照 TASK.md）
- **可信身份创建/恢复唯一账号**：`MiniLoginService` 经 `IdentityProvider` 服务端换取 AppID/OpenID（绝不接收客户端 role/openid/sysUserId）；`uk_app_open` + `INSERT ... ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)` 原子 get-or-create；新账号仅 OWNER。
- **新会话默认 OWNER + 恢复真实资格**：`IdentityContextService` 读权威态计算 availableRoles（OWNER 恒有；DRIVER 需 vi_driver_profile 已批准且证件准驾当前有效；STATION 需 vi_employee_binding 未撤销 + ≥1 启用 vi_station_grant），恢复老账号资格而不重置、不建重复 sys_user、不签发 ADMIN 令牌。
- **不透明令牌**：`MiniTokenService` 生成 32 字节安全随机令牌，仅存 SHA-256 摘要（vi_session_token.token_hash BINARY(32) 唯一键）；明文只在登录响应返回一次；解析校验令牌/会话/绝对截止/session_epoch 代际/账号锁定。
- **返回 accountVersion、资格、公开员工关联**：SessionResponse/ProfileResponse 含 accountVersion、driverQualification、EmployeeLinkPublic（粗粒度，无 sysUserId/员工名单/完整手机号/完整 OpenID）。
- **MINI 认证域**：`MiniSecurityConfig`（@Order+securityMatcher `/api/**`）新增不透明 Bearer 链，框架 ADMIN 链不动、令牌不交叉；`ViApiErrorAdvice`（限 rongli 控制器包，@Order(0)）渲染真实 HTTP 状态 + ApiError（vi 域区别于框架 sys「恒 200+body.code」）。
- **provider 适配**：`SimulatorIdentityProvider`（mode=SIMULATOR，调 /wechat/exchange，测试替身）+ `WeChatIdentityProvider`（mode=WECHAT 生产默认，缺凭据 fail-closed VI_CAPABILITY_NOT_CONFIGURED，真实渠道属 L01 门禁）；按 vi.providers.mode 条件装配，生产绝不缺省走模拟器。
- **前端**：小程序 `features/identity`（api + useIdentityLogin + O29 登录页，处理 PROCESSING/SUCCESS/DENIED/ERROR 与员工关联提示）；PC `api/rongli/identity.ts`（V6.4 身份域共享类型）。两端构建通过。

## 真实集成验证（I01IdentityIT 9/9，真实 HTTP+SpringSecurity+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I01IdentityIT verify` → exit 0，9 用例 0 失败 0 错误 0 跳过。
1. 新微信用户默认 OWNER 且不建 sys_user（TC-R62-001/TC-R63-014）
2. 同身份再次登录恢复同账号、仍默认 OWNER（TC-001）
3. 伪造 role/openid 字段 → 400 VI_REQUEST_INVALID、不创建账号、不提权（TC-002/TC-R62-002）
4. **同 openid 并发登录只产生一个账号**（negative_check；READ_COMMITTED + FOR UPDATE + uk_app_open）
5. 无效 code → 真实 401 + ApiError(VI_AUTH_REQUIRED)
6. 档案带令牌 200（no-store）/无令牌真实 401（API_005_get）
7. 旧 /api/common/logout 无业务映射、不撤销会话（TC-R50-007）
8. **MINI 令牌不能访问 ADMIN 端点、accountId 不触发框架超管**（TC-R50-032/negative_check）
9. 已审核 DRIVER + 有效 STATION 再登录保持资格、默认 OWNER（TC-R62-018/035；直接种子权威资格表验证恢复读取）

全量回归（同次）：E06PlatformIT 10/10、E07TestingIT 5/5、E08PlatformIT 1/1、平台单元 16/16，BUILD SUCCESS，新增 MINI 链无回归。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0（产出 pages/identity/login）；`npm --prefix xinyi-ui run build:prod` exit 0。

## 跨任务用例（诚实边界，本 IT 不伪造）
I01 case_ids 含整个「身份权限」模块 27 用例，但 TC-003..018（接单 403、切站、派单、退款审批、统计鉴权、准驾/资格到期派单）与 TC-R50-008（双域退出，属 session/logout 任务）/TC-R50-012（中文自由权限契约检查，属静态契约门禁）依赖尚未实现的派单/订单/站点/退款/契约检查任务，保持 NOT_RUN，由其归属任务覆盖。本 IT 只断言 I01 直接实现并可真实验证的登录/档案/会话/角色恢复/不提权/跨域隔离用例。

## Findings / 遗留
- **小程序 api/types.ts 为 V6.0 旧生成**：Session/Profile 缺 V6.4 的 employeeLink/accountVersion，且含已退役 EmployeeBinding.bindingCredential。本任务已就地把 Session/Profile 补齐 employeeLink/accountVersion 并新增 EmployeeLink* 类型；**完整从 V6.4 dto-types.ts 重生成 types.ts（含移除退役 EmployeeBinding）建议由契约同步任务统一处理**。
- **vi 域真实状态码 vs 框架恒 200**：vi 端点按契约返回真实 HTTP 状态 + ApiError（已由 ViApiErrorAdvice/MiniAuthenticationEntryPoint 落实）；未映射的 /api 路径当前仍被框架 GlobalExceptionHandler 以恒 200 兜底（NoResourceFoundException），TC-R50-007 以「非成功 + 会话未撤销」断言其退役语义；将 /api/** 未映射统一为真实 404 属后续 vi web 层细化。
- **JDK HttpURLConnection 对 401 响应体读取怪癖**：测试客户端改用 JdkClientHttpRequestFactory 读取 401 body；服务端已正确发送 ApiError（Content-Length 一致），真实客户端 wx.request/axios 无此问题。
- ProfileService 的 phoneMask/realName/defaultAddressId 在 I01 为 null（分属手机号绑定/实名/车主地址任务），不臆造。
- capabilities/permissions 为登录/档案返回的视角基线（UI 提示），逐端点精确门禁由各归属任务在服务端实时重算。

## 完成判据对照
- [x] 源接口/页面/表/用例有明确实现文件与关联测试（非以目录存在当完成）
- [x] 真实集成与负向场景实际通过并保存原始报告（I01IdentityIT 9/9 真实 HTTP+MySQL+模拟器）
- [x] 标注测试第三方替身（SIMULATOR provider）与跨任务延后用例，不声称真实微信渠道通过（L01 另验）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）
