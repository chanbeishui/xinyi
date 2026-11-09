# Q04 对主代理人工域与微信适配器的交叉复核

复核者：/root/s01_backend。仅对自己未编写的初稿 ManualVerificationService/Mapper/DTO/Controller、WeChatSoterProvider、PublishedSoterSettings、SoterConfigurationMapper及ADMIN安全链进行独立交叉审查；已授权最小修复如下。业务全量测试由主代理串行执行，本记录不代替JUnit证据。

- 人工核验读取、创建、复核按真实公司归属和独立功能权限；query不是由写权限替代，空范围生成AND1=0。新增按当前公司、历史读按case原公司，变更材料/epoch后复核拒绝，未使用主部门猜归属。
- 人工两人不同、申请人历史绑定和可信phone HMAC映射排除自办/自审；已冻结申请人后台ID集合不因后续换号消失。用户已明确无凭证/无标准发布，不增设门槛或第三审批人。
- 备注及复核备注分别AAD绑定真实case/申请/公司epoch/材料/类型/操作者；审计/outbox/幂等结果不存明文。复核事实→canonical证明→proof_id链接同事务；失败整体回滚。已存在事实及终态不能覆盖。
- 真实ADMIN专用安全链覆盖manual-verifications和verification-sessions；MINI不能替代ADMIN，未登录真实401。Service再按数据库auth_version及scope校验，不依赖前端按钮或Redis强退成功。
- 发现并修复人工共同故障：Mapper原autoMapping依赖当前环境未开启的underscore转camel，find有行却owner/application等为空。已将26个继承/持久化/联结投影字段逐一显式映射，两个业务时刻及created/updated使用UTC handler。没有放宽权限或修改断言。
- 发现并加固共用人工publisher授权入口：requireReviewAuthority现在独立lockAuthority，未来内部调用也重新校验数据库auth_version，不只依赖HTTP外层事务守卫。
- 微信适配器生产固定HTTPS，集成运输仅strict loopback，禁自动连接重试和重定向，响应限64KiB。微信原文、签名、OpenID、token不进入审计或普通响应；只接受is_ok明确布尔结果，错误/未知均UNKNOWN。GET不触发verify。
- 发现并加固微信回复解析：独立STRICT_DUPLICATE_DETECTION拒重复is_ok/errcode及尾随JSON；stable_token access_token必须textual，数字或其他类型不能当Token继续请求。
- 发布配置绑定原公司/环境/WECHAT_APP/发布记录/AppID，秘密每次检查可用性再使用缓存token，缓冲区finally清零。冻结挑战使用原发布凭据，真实上游同AppID更换AppSecret后，原凭据不可用会返回UNKNOWN，需受控配置/重新本机挑战处理；本轮按主代理决定不新增跨修订凭据自动轮换逻辑，不声称已经验证此恢复能力。
- 新设备ADMIN列表/详情已隐藏request_no为空或非WECHAT_SOTER/WX_SOTER_DEVICE_AUTH历史行，不把旧face历史重标成设备认证；历史记录仍留数据库。

静态检查：Java AOSP formatter解析通过；ManualMapper XML解析通过，26个domain/base字段与resultMap集合完全一致；生产定向git diff--check通过。未由本复核者运行Maven/DB。最终是否通过以主代理复测报告为准。
