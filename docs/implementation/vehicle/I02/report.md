# I02 · 手机号验证回执与自动关联触发 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-CONFIG-20261022。
接口：API_002_post（phone/bind）、API_003_post（sms/send）、API_004_post（sms/verify）、API_012_patch（owner/profile）、v62_get_phone_verification（回执查询）。主责任表：vi_phone_verification_receipt（并写 vi_employee_binding/vi_account/vi_account_security/vi_owner_profile，只读 sys_user 目录、vi_employee_directory_guard 互斥）。

## 交付（对照 TASK.md / V6.4 §2/§3/§4）
- **分离 wx.login code 与 phoneCode**：phone/bind 严格只接受 {phoneCode,expectedAccountVersion,verificationRequestId}，混入裸 phone/sysUserId/openId/role 一律 400（TC-R62-008），绝不用于自动绑定；身份取自可信 MiniPrincipal。
- **专用回执去重 + 一次性凭据**：`PhoneBindService` 以 uk_phone_request(account_id,request_id) 去重、uk_phone_code(app_id,code_hash) 防跨账号重放；code_hash=SHA256(phoneCode) 只存摘要不存原文；处理中不重复消费；幂等重试（同 requestId）先于版本守卫返回既有回执（响应丢失安全恢复，TC-R62-014）；不进通用幂等缓存、不缓存令牌。
- **状态机 + 不假成功**：PROCESSING→VERIFIED/UNKNOWN/FAILED；外部换取在短事务/锁外，IO 不确定→UNKNOWN（查回执/重新授权，TC-R63-012）；无效凭据→FAILED 不假成功（TC-R62-021）。
- **手机号验证与员工结果分开**：核验成功原子存手机号事实（phone_lookup=HMAC、phone_cipher=AES-GCM、verified_at、account.phone_source=WECHAT）；员工匹配失败/重号/冲突不推翻手机号验证、不取消 OWNER（TC-R62-025）。
- **目录锁自动关联**：GLOBAL_DIRECTORY 保护行 SELECT FOR UPDATE 串行（锁序：目录guard→sys_user→account→binding→audit/outbox）；唯一有效员工（DISTINCT user_id）匹配则建 vi_employee_binding（source=WECHAT_PHONE_MATCH、bound_by=NULL）；重号 AMBIGUOUS_PHONE/已绑/禁用记内部 outcome，小程序仅收粗粒度 NEEDS_ADMIN，不泄其他员工/账号；关联成功递增 permission_version→requiresSessionRefresh=true。
- **SMS 不新增员工关联**：sms/send、sms/verify 无已发布 LOGIN_OTP/PHONE_VERIFY 路由→VI_CAPABILITY_NOT_CONFIGURED 拒绝（禁止固定成功），且 SMS 路径绝不调用自动关联（TC-R62-030 原则）。实际路由/供应商发送属 X04/X05。
- **档案 PATCH（API_012）**：CAS expectedVersion，仅本人 OWNER；nickname/avatarFileId→vi_account，realName→vi_owner_profile.service_name，defaultAddressId→vi_address 归属校验+默认切换；未提供字段保持原值。
- **前端**：小程序 O25 手机号授权页（getPhoneNumber→bind→PROCESSING 轮询/VERIFIED/UNKNOWN/FAILED/DENIED）；PC identity.ts 补 PhoneBindResult/PhoneVerificationStatus 类型。两端构建通过。

## 真实集成验证（I02IdentityIT 9/9，真实 HTTP+Security+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I02IdentityIT verify` → exit 0，9 用例 0 失败 0 错误 0 跳过。
1. 混入裸 phone/sysUserId/openId → 400、不登记回执、不关联（TC-R62-008）
2. 核验成功响应丢失 → 查回执 VERIFIED + 同 requestId 安全重试幂等不重复消费（TC-R62-014）
3. 无效 phoneCode → FAILED 不假成功、不开 STATION（TC-R62-021 / TC-R63-012）
4. 目录无匹配 → 手机号 VERIFIED 与员工 UNLINKED 分开、OWNER 不受影响（TC-R62-025）
5. 目录重号（2 sys_user 同号）→ VERIFIED + NEEDS_ADMIN、不自动绑定（TC-R62-025）
6. 唯一有效员工 + 站点授权 → LINKED + requiresSessionRefresh + 档案含 STATION
7. 同 requestId 并发 → 只一条回执（一次外部消费）、查询 VERIFIED（TC-R62-031）
8. SMS 未配置 → 422 VI_CAPABILITY_NOT_CONFIGURED、不关联（TC-R62-030 原则）
9. 档案 PATCH CAS 更新昵称、旧版本 409（API_012）

全量回归（同次）：E06 10/10、E07 5/5、E08 1/1、I01 9/9、I02 9/9、平台单元 16/16，BUILD SUCCESS，无回归。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0（产出 pages/identity/phone）；`npm --prefix xinyi-ui run build:prod` exit 0。

## 跨任务用例（诚实边界，本 IT 不伪造）
- TC-R62-029（普通订单联系人改员工号码不关联）属订单联系人任务（O 系列），本 IT 不实现订单写入；I02 保证只有本人微信手机号核验（WECHAT 证明）才触发关联，联系人/SMS 号码绝不成为身份依据。
- TC-R62-030（SMS 验证成功但不关联）的"成功路径"依赖 X01–X07 第三方消息路由/模板/供应商；当前未配置→拒绝（已验证），SMS 不关联的原则由代码路径保证（SmsService 绝不调用 associate）。完整 SMS 成功用例待 X04/X05 落地。

## Findings / 遗留
- **ViErrorCode 补齐 V6.4 错误码**：新增 VI_PHONE_PROOF_REQUIRED/REUSED、VI_EMPLOYEE_LINK_CONFLICT/BLOCKED、VI_CONFIG_*、VI_SECRET_UNAVAILABLE、VI_PAYMENT_METHOD_*、VI_SEND_RESULT_UNKNOWN、VI_SUBSCRIPTION_UNAVAILABLE、VI_OFFLINE_PROOF_CONFLICT（HTTP 状态按 V6.4 错误码表）。
- **框架 sys_user 写钩子纳管目录 guard**：V6.4 §4 要求框架新增/导入/改手机号/启停/删除走受控服务并遵循 GLOBAL_DIRECTORY 保护。I02 实现了自动关联侧的目录锁与唯一匹配；框架人员目录写入侧的钩子纳管（拦截 sys_user 手机号变更→暂停旧绑定）属后续身份/框架集成任务，当前 sys_user 写仍走框架原路径（I02 只读目录）。
- **真实微信手机号 API（getuserphonenumber）属 L01 门禁**：WeChatIdentityProvider.exchangePhone 缺凭据 fail-closed，不以模拟器替代真实认证。
- **手机号加密密钥**：vi.crypto.data-key / vi.lookup-key 来自部署环境/KMS，缺失 fail-closed；IT 用测试值（非生产凭据）。
- defaultAddressId 切换为 A02 地址簿的最小默认切换；完整地址 CRUD 属 A02。

## 完成判据对照
- [x] 源接口/页面/表/用例有明确实现文件与关联测试（非以目录存在当完成）
- [x] 真实集成与负向场景实际通过并保存原始报告（I02IdentityIT 9/9 真实 HTTP+MySQL+模拟器）
- [x] 标注测试第三方替身（SIMULATOR provider）与跨任务延后用例，不声称真实微信渠道通过（L01 另验）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）
