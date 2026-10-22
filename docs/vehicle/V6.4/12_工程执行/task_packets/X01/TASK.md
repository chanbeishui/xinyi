# X01 · 配置版本、密钥保管与发布事务

阶段：M2；责任域：integration；前置：E06, E07。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`d645e4a1406930527637e4456ef6987327d51d7005b8876d8fd1e65b38883546`。

## 本任务交付
- 实现五类配置专用DTO、秘密KEEP/REPLACE/CLEAR、只读脱敏
- 不可变版本与check哈希绑定；父锁CAS发布/回滚，主库生效指针权威
- 权限/主体/环境分离，真实测试白名单、日志/幂等记录清洗

## 必测失败与并发
- secret回显/日志/对象存储泄漏
- 草稿生效、旧check发布、跨主体更新、KMS故障明文降级

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V64_get_api_admin_integrations_profiles, V64_post_api_admin_integrations_profiles, V64_get_api_admin_integrations_profiles_profileId, V64_put_api_admin_integrations_profiles_profileId_draft, V64_post_api_admin_integrations_profiles_profileId_checks, V64_post_api_admin_integrations_profiles_profileId_publish, V64_get_api_admin_integrations_profiles_profileId_publications, V64_post_api_admin_integrations_profiles_profileId_rollback, V64_get_api_admin_integrations_profiles_profileId_revisions, V64_get_api_admin_integrations_test_recipients, V64_post_api_admin_integrations_test_recipients, V64_post_api_admin_integrations_test_recipients_recipientId_revoke
画板：无
主责任表：vi_integration_profile, vi_integration_revision, vi_integration_secret, vi_integration_check, vi_integration_publication, vi_integration_test_recipient
验收：TC-CFG-001, TC-CFG-002, TC-CFG-003, TC-CFG-004, TC-CFG-005, TC-CFG-006, TC-CFG-007, TC-CFG-008, TC-CFG-009, TC-CFG-010

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/integration/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/`
`xinyi-ui/src/views/rongli/integrations/`
`xinyi-miniapp/src/`
`sql/migration_YYYYMMDD_integration_config.sql`

## 验证命令
```bash
mvn -pl xinyi-admin -am test
python docs/vehicle/V6.4/11_工具/test_integration_policy.py
python -m pytest docs/vehicle/V6.4/13_真实集成测试/live/test_integration_config_live.py -q
```

## 完成标准
- 主契约与状态机所有本任务分支实现，无固定Ack/假成功
- 实际MySQL与真实HTTP证据齐全；仅外部供应商可用隔离测试替身
- 变更秘密不进入日志、源码和通用响应；副作用/权限/并发测试通过
- 独立复核证据和覆盖表更新；未执行的真实供应商/真机项保持NOT_RUN

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
