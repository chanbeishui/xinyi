# X07 · 第三方配置真实链路与回归证据

阶段：M6；责任域：integration；前置：X06, T02, T05, T07。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`fa7efff4831007063571aa9bf0f9282cfce0f743c6516039f2a6062c403d31c3`。

## 本任务交付
- 通过真实xinyi HTTP/Service/MyBatis/MySQL完成配置发布至支付/发送
- 故障恢复、并发版本、秘密扫描、跨主体负向、双支付各状态
- 更新全量API/页面/表/用例覆盖、L01与L02真实门槛，不重置已有进度

## 必测失败与并发
- 只用内存原型冒充后端测试
- 测试控制器入生产Jar、日志/源码含真实凭据

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：TC-CFG-059, TC-CFG-060, TC-CFG-061, TC-CFG-062

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
python -m pytest docs/vehicle/V6.4/13_真实集成测试/live/test_integration_config.py -q
```

## 完成标准
- 主契约与状态机所有本任务分支实现，无固定Ack/假成功
- 实际MySQL与真实HTTP证据齐全；仅外部供应商可用隔离测试替身
- 变更秘密不进入日志、源码和通用响应；副作用/权限/并发测试通过
- 独立复核证据和覆盖表更新；未执行的真实供应商/真机项保持NOT_RUN

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
