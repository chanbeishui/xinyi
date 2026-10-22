# X04 · 腾讯云和阿里云短信及场景路由

阶段：M4；责任域：notification；前置：X01, N01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`11e4a427d4d8b65e4f4e9d916f5b4e3b39ad85b92e7d315bd629cddbd8f2232c`。

## 本任务交付
- 真实官方SDK/HTTP适配器和独立参数，模板顺序/命名映射
- 按业务事件唯一主通道；白名单测试、回执查询、限频和OTP保护
- UNKNOWN不自动重发；仅确定未发送才受控备用

## 必测失败与并发
- 缺签名/模板、跨供应商参数混用
- 接收超时双发、accepted假装delivered、日志含OTP

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V64_get_api_admin_integrations_templates, V64_post_api_admin_integrations_templates, V64_get_api_admin_integrations_templates_templateId, V64_put_api_admin_integrations_templates_templateId_draft, V64_post_api_admin_integrations_templates_templateId_checks, V64_post_api_admin_integrations_templates_templateId_preview, V64_post_api_admin_integrations_templates_templateId_publish, V64_get_api_admin_integrations_templates_templateId_publications, V64_post_api_admin_integrations_templates_templateId_test, V64_get_api_admin_integrations_routes, V64_put_api_admin_integrations_routes_routeId, V64_post_api_admin_integrations_routes, V64_get_api_admin_integrations_deliveries, V64_post_api_admin_integrations_deliveries_deliveryId_query, V64_post_api_admin_integrations_deliveries_deliveryId_retry
画板：无
主责任表：vi_message_template, vi_message_template_revision, vi_notification_route, vi_notification_delivery
验收：TC-CFG-036, TC-CFG-037, TC-CFG-038, TC-CFG-039, TC-CFG-040, TC-CFG-041, TC-CFG-042, TC-CFG-043, TC-CFG-044, TC-CFG-045

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
