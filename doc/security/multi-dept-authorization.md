# 多部门任职、安全授权与审计设计

本文档是用户多部门任职及后续业务数据权限开发的强制安全基线。实现以数据库中的任职关系和 `auth_version` 为权威来源，Redis Token 清理仅作为加速手段。

## 1. 权限模型

- `sys_user.dept_id` 是唯一主部门。
- `sys_user_dept` 保存完整任职集合，并且必须包含主部门。
- `management_scope=DEPT` 时只能有一条任职，且等于主部门。
- 增加第二个任职后账号自动变为粘性的 `PLATFORM`；减回单部门不自动降级。
- `PLATFORM → DEPT` 只能由 `SysUser.isAdmin(userId)` 所定义的最高平台管理员显式执行，并填写原因。
- 用户角色在全部任职部门统一生效，不建立角色—部门绑定。
- DataScope 3 是全部有效任职部门；DataScope 4 是全部有效任职部门及各自启用的下级部门；多角色按并集计算。
- 用户列表按目标用户任一任职部门与可见范围相交显示。用户详情和账户写操作要求操作者覆盖目标全部任职部门。
- `PLATFORM` 账号的后台完整资料、部门、岗位、角色、状态、密码重置和删除只允许最高平台管理员操作。

## 2. 数据库对象

一次性迁移脚本为 `sql/migration_multi_dept_authorization.sql`，全新环境由 `sql/ry_20260417.sql` 初始化。

新增对象：

- `sys_user_dept(user_id, dept_id)`：用户任职关系，另有 `(dept_id, user_id)` 索引。
- `sys_user.management_scope`：`DEPT` 或 `PLATFORM`。
- `sys_user.auth_version`：授权撤销版本。
- `sys_security_outbox`：提交后会话清理事件，支持幂等重试和递增退避。
- `sys_security_audit_log`：安全审计，只提供新增 Mapper，不提供业务删除接口。
- `sys_user_dept_migration_issue`：迁移异常报告。
- `sys_user_dept_integrity_issue`：完整性异常视图。

迁移只自动回填“有效用户的有效主部门”这一确定关系。主部门缺失、部门无效及孤儿数据会写入异常报告，不猜测修复。放量前以下查询必须为零：

```sql
select count(*) from sys_user_dept_migration_issue;
select count(*) from sys_user_dept_integrity_issue;
```

应用每天 02:30 再次执行完整性巡检，异常写日志和失败安全审计。

## 3. 统一读写边界

### 查询

`DeptScopeServiceImpl` 统一从数据库加载有效任职，解析角色 DataScope。用户列表使用 `EXISTS sys_user_dept`，避免 JOIN 造成重复行和分页错误。列表只附加操作者可见的任职 ID；主部门不可见时，主部门及其名称在 SQL 层置空。

`checkUserDataScope` 不再复用列表查询。详情使用“目标全部任职都被覆盖”的校验，并额外保护 `PLATFORM` 账号。

### 写入

`UserAuthorizationMutationServiceImpl` 是用户任职、角色、岗位、状态、密码和删除的唯一后台写入口。处理顺序为：

1. 锁定目标 `sys_user` 行。
2. 校验目标是否存在、最高管理员保护、PLATFORM 保护和禁止普通管理员修改本人任职/角色。
3. 校验旧、新部门并集以及旧、新角色并集。
4. 批量操作先验证全部对象。
5. 写关系和业务字段。
6. 原子递增 `auth_version`。
7. 同事务写成功审计和 outbox。
8. 提交后立即尝试按用户强退；失败由 outbox 重试。

旧的用户、角色直接写服务保留接口兼容，但敏感方法会拒绝执行。旧 `/system/user/authRole` 仍可调用，但内部委托统一授权服务并要求原因。角色侧授予、撤销和批量入口同样委托统一服务。

角色新增、权限/菜单修改、DataScope、状态和删除同样要求填写原因；配置审计与受影响用户的 `auth_version` 递增在同一事务完成。

## 4. 接口

- `PUT /system/user/{userId}/departments`
  - 权限：`system:user:dept:edit`
  - JSON：`primaryDeptId`、`deptIds`、`reason`
- `PUT /system/user/{userId}/roles`
  - 权限：`system:user:role:edit`
  - JSON：`roleIds`、`reason`
- `PUT /system/user/{userId}/management-scope`
  - 权限：`system:user:management:edit`
  - 服务端仍强制校验最高平台管理员
- `POST /system/user/import/preview`
  - 上传文件并返回影响摘要和 `previewToken`
- `POST /system/user/import/execute`
  - 必须提交相同文件、相同更新选项、预检令牌、确认标志和原因

后台基础资料 DTO 不接收部门、角色、密码、状态和内部权限字段。员工自助资料与本人密码修改继续可用；本人密码变化后所有旧会话失效并要求重新登录。

## 5. 会话撤权

登录缓存 `LoginUser` 保存全部有效任职和签发时的 `authVersion`。每次认证请求在刷新 Redis TTL 和建立 Spring Security 上下文之前执行：

1. 从 Redis 读取 Token 缓存。
2. 从数据库读取当前 `auth_version`、状态和删除标志。
3. 版本不一致、停用或删除时返回 HTTP 401，并尝试删除当前 Token。
4. 数据库权威状态不可读取时 fail-closed，返回 HTTP 503。
5. 校验通过后才刷新 TTL。

登录签发 Token 前后各校验一次数据库版本，避免认证期间并发授权变更。Token 写入后必须同步写入 `sys_user_tokens:{userId}` 索引；索引失败会删除 Token 并使登录失败。

首次启用版本校验时必须清空历史 `login_tokens:*` 和 `sys_user_tokens:*`，因为旧缓存没有完整的版本及任职字段。

## 6. 安全审计

审计记录操作者、目标、请求 ID、IP、原因、前后值、版本、结果和失败码。密码、Token 和其他凭据永不进入快照。被拒绝和业务事务失败使用独立事务记录；成功审计、版本和 outbox 与业务写入处于同一事务。

当前覆盖：

- 任职、主部门、管理范围、岗位和角色；
- 状态、管理员密码重置、本人密码变更、删除、注册和导入覆盖；
- 角色菜单、角色状态和 DataScope；
- 部门状态；
- 会话强退、outbox 失败重试和完整性巡检；
- 统一授权服务内的越权或参数拒绝。

部门停用、层级调整或删除会保守地递增全部有效账号的 `auth_version`，以覆盖 DataScope 4、角色自定义部门和移动子树带来的间接范围变化。组织结构变更属于低频高影响操作，上线前应按实际用户量压测该事务和 outbox 写入量。

业务系统不得增加安全审计删除、清空或覆盖接口。数据库账号应进一步限制为应用账号仅可 `INSERT/SELECT` 审计表，归档账号单独管理。

## 7. Excel 导入

导入使用文件 SHA-256、操作者、覆盖选项和行数绑定预检令牌，有效期 15 分钟。正式执行会原子消费一次性令牌，再重新解析和全量校验；并发重放会被拒绝。任一行无权或无效时整批回滚，执行失败后必须重新预检。

更新已有用户时，导入部门成为主部门和唯一任职部门，但原 `PLATFORM` 标记保持不变。涉及 `PLATFORM` 账号仅最高平台管理员可执行。角色和岗位不会被 Excel 暗中覆盖。

## 8. 发布与回滚

配置：

```yaml
security:
  multi-dept-query-enabled: true
  multi-dept-write-enabled: false
```

推荐发布顺序：

1. 备份并执行迁移脚本。
2. 人工处理迁移和完整性异常，运行关键 SQL `EXPLAIN`。
3. 部署后端兼容代码和前端。
4. 暂停权限写入，清理全部旧 Token，验证 401/503、并发和 Redis 故障场景。
5. 保持写开关关闭，先验证多部门查询。
6. 开启 `multi-dept-write-enabled`，观察版本不一致、拒绝数、审计失败和 outbox 积压。

回滚时先关闭多部门写开关；保留新增表和 `auth_version` 校验，不做破坏性数据库回滚。

## 9. 后续业务功能强制设计

每个新模块必须在编码前填写“数据归属契约”：

| 项目 | 必须回答 |
| --- | --- |
| 权威归属 | 哪张表、哪个 `Long dept_id`，或由哪个父记录确定 |
| 查询范围 | 哪些角色和 DataScope 可以读取 |
| 新增 | 谁可以把记录放入目标部门 |
| 修改 | 原部门与新部门并集如何校验 |
| 删除/批量 | 如何从数据库加载真实归属并先全量验证 |
| 跨部门共享 | 使用哪张明确授权关系表 |
| 异步入口 | 定时任务、导入、消费者调用哪个统一 Service |
| 会话影响 | 是否改变接口权限或数据范围，是否递增版本 |
| 安全审计 | 事件、原因和前后快照是什么 |

强制规则：

- Controller 只接收参数和校验功能权限，数据写权限在 Service 层执行。
- `@PreAuthorize` 与部门权限必须同时存在。
- `@DataScope` 只用于查询；写操作必须加载原记录并校验原、新归属并集。
- 带 `@DataScope` 的方法必须经 Spring 代理调用。
- 不允许客户端提交 `params.dataScope`、`visibleDeptIds` 等内部字段。
- 不允许用前端隐藏按钮、客户端 `deptId` 或列表可见性替代写授权。
- 无直接部门字段的数据必须明确父记录或授权表，不得默认使用主部门或创建人。
- 跨部门共享使用关联表和参数化 SQL，不得使用逗号字符串或模糊匹配表达权限。
- 权限批量操作必须先全部验证，再整体写入。
- 定时任务、消息消费者、导入和内部接口不得直接写权限 Mapper。

PR 必须说明数据归属、查询权限、写权限、会话影响、审计事件，并附正向、越权、伪造 ID、批量混入、并发、事务回滚和 Redis 故障测试结果。

## 10. 验收矩阵

- A+B 任职且角色 DataScope 3：可以读取 A、B 的数据。
- A+B 任职且角色 DataScope 4：可以读取 A、B 及各自下级数据。
- 主部门 B、兼任 A 的目标用户能出现在 A 列表，但 A 管理员不能读取完整详情或执行写操作。
- 多角色范围按并集；空范围生成 `1=0`，不得省略条件。
- 任职、角色、状态、密码和删除后，旧 Token 首次请求立即 401。
- Redis 故障不恢复旧权限，outbox 恢复后能幂等清理。
- 批量请求混入一个越权目标时全部回滚，且有失败审计。
- `DEPT` 单部门约束、主部门包含关系和孤儿关系巡检为零。

本实现不能替代上线前的专业渗透测试、数据库权限复核和独立安全审计。
