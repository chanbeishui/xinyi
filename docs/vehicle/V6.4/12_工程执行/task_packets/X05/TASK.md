# X05 · 订阅模板与用户授权事件链

阶段：M4；责任域：notification；前置：X01, N01, I01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`a75905a80c9c7795dfedd0c2a99a9e6e99428c7f6651db2bf44b43aea3f094f2`。

## 本任务交付
- AppSecret公共凭据服务、AccessToken分应用版本单飞缓存
- 真实模板key类型长度/页面白名单/环境校验；发布选项供客户端
- 本人prompt nonce和结果，业务事件发送，拒绝不阻断业务

## 必测失败与并发
- 伪造accept无限发送、复用旧模板授权
- 未登录自动弹窗、任意page注入、长期无资格

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V64_post_api_common_subscriptions_prompts, V64_post_api_common_subscriptions_prompts_promptId_report
画板：无
主责任表：vi_subscription_prompt
验收：TC-CFG-046, TC-CFG-047, TC-CFG-048, TC-CFG-049, TC-CFG-050, TC-CFG-051, TC-CFG-052, TC-CFG-053, TC-CFG-054

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
