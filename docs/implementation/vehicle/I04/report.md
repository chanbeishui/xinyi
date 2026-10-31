# I04 · 角色与站点上下文切换 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-IDENTITY-CONSISTENCY-20261022。
接口：API_006_post（session/role）、API_007_post（session/station），均 MINI、X-Client-Type 仅审计。主责任表：无（切换原子更新 vi_session.context_version/active_role/station_id；只读 vi_account/vi_account_security/vi_employee_binding/vi_station_grant/vi_driver_profile 权威状态）。

## 交付（对照 TASK.md / T3.4 §18）
- **只切 availableRoles**：`SessionSwitchService` 以 `IdentityContextService.compute` 实时可用角色为权威；OWNER 恒可切回；未批准无 DRIVER（TC-R63-010/016，请求 role 不授予身份）；STATION 须有效员工绑定 + 启用站点授权交集。
- **原子上下文切换**：CAS `expectedContextVersion`（`switchContext` WHERE context_version=expected AND ACTIVE），递增 context_version、签发绑定新上下文的 ACCESS 令牌（`issueAccessForContext`，沿用最大代际、不轮换 REFRESH）；重发旧上下文 409 VI_CONTEXT_CHANGED（conflicts 携带 currentVersion）。
- **旧上下文拒绝、他设备不受影响**：`MiniTokenService.resolveAccess` 增加 token.contextVersion 与 session.contextVersion 校验（I01 扩展），本会话旧上下文 ACCESS 立即失效；其他设备会话 context 不变、令牌仍可用。
- **切换不释放任务**：切换只改会话视角，不写 vi_driver_assignment/不改车辆保管责任（TC-R62-024）；资格/在手承接保留。
- **撤权回安全视角**：站点授权被撤后 STATION 不可再切（403），但基础会话仍可读、可切回 OWNER（S22 允许 0 授权、不自动回无权所属站点）；切 STATION 保留仍获授权当前站点，失权置空待选。
- **同源一致（TC-R63-001）**：修复 `ProfileService.phoneMask`（I02 遗留占位 null）→ 由已核验 phone_cipher 解密脱敏，与会话 phoneBound/员工关联/可用角色同源；会话/档案/切换投影一致。
- **前端**：小程序 U01 切换身份页（含 unqualified/driver/staff/all 状态变体，同一页面按权威状态渲染）、S21 切换检测点（仅授权站点）、S22 无工作权限；http.ts 增 applyAccessSession（合并新令牌、保留 refreshToken）；PC identity.ts 补 AccessSession。两端构建通过。

## 真实集成验证（I04IdentityIT 10/10，真实 HTTP+Security(MINI链)+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I04IdentityIT verify` → exit 0，10 用例 0 失败 0 错误 0 跳过。
1. OWNER→DRIVER 切换成功、上下文递增、新令牌可用、旧上下文令牌 401、不返回 refreshToken
2. 待审切代驾 403、刷新不审批（TC-R63-010/016）
3. 已批准我的入口显示 APPROVED 资格、可切代驾（TC-R63-009）
4. 员工站点同源一致切 STATION（真实手机号绑定；会话/档案 phoneMask/employeeLink/availableRoles/stationIds 一致，TC-R63-001）
5. 越权站点 403、0 授权无 STATION/切站点/切STATION 拒绝、仍可 OWNER（S22）
6. 切换与退出均不释放 assignment（TC-R62-024）
7. 旧检测权限失效仍可切回 OWNER、失权后不可再切 STATION
8. 上下文版本冲突 409（错误版本 + 重发旧上下文）
9. 非法字段/角色/stationId/负版本/缺 X-Client-Type 400
10. 切换仅影响本设备、其他会话视角与版本不变

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10 = 54，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS，无回归（phoneMask 修复不破坏 I01/I02）。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0（产出 switch/station/no-access 页）；`npm --prefix xinyi-ui run build:prod` exit 0。

## 负向场景对照（TASK.md 必测）
- **旧检测权限失效仍可切回OWNER**：用例 7 真实撤权后切回 OWNER 成功。
- **切换不释放assignment**：用例 6 切 DRIVER/OWNER 后承接 ended_at 仍 NULL。

## Findings / 遗留
- **resolveAccess 上下文校验为 I01 必要扩展**：T3.4 要求"本会话旧上下文拒绝"，I01 初版未校验 contextVersion；I04 补齐，刷新/登录令牌不受影响（同上下文）。
- **ProfileService.phoneMask 为 I02 遗留回填**：I02 绑定落地后档案 phoneMask 仍为占位 null，I04 以 TC-R63-001 同源要求修复（解密脱敏）。
- **站点名称/地址富化属公开站点目录任务**：S21 以授权 stationIds 渲染（检测站 #id），不臆造名称、不暴露无权站点单量/司机/订单；名称富化待站点目录任务。
- **代驾申请/员工关联详情页**：U01 的"申请代驾"提示状态（资格申请属 D 系列）、"员工关联"跳转 O25 手机号页；不重复引导已批准账号。
- **refresh 端点限流**仍属平台横切/X 系列（同 I03）。

## 完成判据对照
- [x] 源接口/页面/用例有明确实现文件与关联测试（U01/S21/S22 + 状态变体；非以目录存在当完成）
- [x] 真实集成与负向场景实际通过并保存原始报告（I04IdentityIT 10/10 真实 HTTP+MySQL+模拟器）
- [x] 标注测试第三方替身（SIMULATOR）与延后项，不声称真实微信渠道通过（L01 另验）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）