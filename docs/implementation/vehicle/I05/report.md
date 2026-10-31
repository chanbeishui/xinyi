# I05 · 手机号自动关联员工与后台异常管理 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-IDENTITY-CONSISTENCY-20261022。
接口：v62_get_employee_link / v62_post_employee_link_reconcile（MINI，U02）；v62_get_employee_links / v62_get_employee_link_detail / v62_suspend_employee_link / v62_revoke_employee_link / v62_allow_employee_rematch（ADMIN，B36）。主责任表：vi_employee_binding、vi_employee_directory_guard。

## 交付（对照 TASK.md / V6.4 §3/§4/§6/§7）
- **唯一有效 sys_user 自动关联，无申请/凭证**：提取 `EmployeeAssociationService.associate` 为单一安全事实源（I02 绑定、I05 reconcile/后台/钩子共用）；DISTINCT user_id 计数（多部门 join 去重）；唯一有效员工+无抢占+无禁止重绑才建绑定（source=WECHAT_PHONE_MATCH、bound_by=NULL）。
- **小程序 U02**：`EmployeeLinkService.readPublic` 粗粒度投影（REVOKED 单独 LINK_REVOKED；SUSPENDED/LINKED 按权威态）；`reconcile` 仅重试本账号最近 10 分钟 VERIFIED WECHAT 回执（不重复消费 phoneCode、不接收任意手机号/目标员工），窗口外 VI_PHONE_PROOF_REQUIRED。
- **后台 B36**：`EmployeeLinkAdminService` 功能权限（biz:employeebinding:*）+ 部门数据范围（列表任职相交 resolve、详情/写 checkUserFullAccess 覆盖全部任职）；目录 GLOBAL_DIRECTORY 锁序；SUSPEND 保留唯一占用、REVOKE 置 auto_rebind_blocked+账号 blocked 防复绑、ALLOW_REMATCH 仅清标记不直接恢复（requiresNewPhoneAuthorization=true）；与 MINI permission_version、审计、outbox 同事务；不签 ADMIN Token 给 MINI。
- **框架目录钩子**：`EmployeeDirectoryChangeListener` SPI（xinyi-system）+ `EmployeeDirectoryHookService`（rongli）；受控写入口（updateUserBase 换号/updateStatus/deleteUsers/importUsers）afterCommit 通知，锁内重查最新状态/手机号→停用/删除/换号暂停旧绑定+递增 permission_version；不自动恢复 SUSPENDED。
- **fail-closed 实时校验**：`IdentityContextService` STATION 须链接员工在职（countActiveUserById）+启用站点授权；停用/删除立即失权，不靠旧缓存；车主不删除。
- **安全链**：MINI 链排除 /api/admin/**（交框架 JWT 链）；未知/退役 vi 路由 404（无授权写入）。
- **前端**：小程序 U02 link.vue（状态变体同一页面渲染）；PC B36 employeeLink.vue + employeeLink.ts + 常量路由；两端构建通过。

## 真实集成验证（I05IdentityIT 20/20，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I05IdentityIT verify` → exit 0，20 用例 0 失败 0 错误 0 跳过。ADMIN 令牌经框架 TokenService 铸造（测试夹具，非真实登录；微信为 SIMULATOR 替身）。
覆盖：唯一匹配无凭证(016/007)、重号不泄名单(009)、多部门去重(010)、员工被他微信占用拒覆盖(011)、账号已绑拒转移(012)、并发唯一(013)、停用失权车主保留(015)、换号暂停(019)、revoke 防复绑(020/032)、allow-rematch 不直接恢复(033/R63-005)、无关联不能暂停(R63-011)、MINI 不获 ADMIN(022)、退役路由拒绝(026/R63-015)、暂停登录不绕过(R63-004)、reconcile 窗口内/外、后台列表/详情脱敏、停用旧+启用新仅匹配有效(027)、改号后不提交到已变化身份(028)、自改不成为可信目录(036)、改选对象暂停B不处理A(R63-003)。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20 = 74，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS；框架受控写/安全链改动无回归。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0；`npm --prefix xinyi-ui run build:prod` exit 0。

## Findings / 遗留
- **I02 associate 提取为共享服务**：避免多处复制唯一性检查导致竞态口径不一；I02 行为不变（回归绿）。
- **框架钩子为 afterCommit 独立事务**：目录写提交后重查最新状态失效旧能力；实时 fail-closed 校验兜底（钩子失败不延长工作权限）。锁序 guard→account→binding，无嵌套锁反转。
- **PC B36 用常量路由**（首版 rongli 视图）；非超管角色需经 sys_menu 授予 biz:employeebinding:* 方可调用 API（服务端强制），菜单基数据登记属后续部署/权限任务。
- **ADMIN 令牌测试夹具**：IT 以 TokenService 铸造超管 JWT（真实 auth_version 校验），非真实 /login；真实后台登录属框架既有能力。
- **真实微信手机号 API 属 L01 门禁**；SIMULATOR 替身不声称真实渠道通过。

## 完成判据对照
- [x] 源接口/页面/表/用例有明确实现文件与关联测试（U02/B36 + 状态变体；22 验收用例映射 20 测试）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（I05IdentityIT 20/20）
- [x] 标注测试第三方替身与延后项，不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）