# X02 · 支付方式与微信商户运行时接入

阶段：M2；责任域：payment；前置：X01, P01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`b9ba71ab92f05ff295ee8fe1b40952c854b85f120df3f1022690e52e5ba123df`。

## 本任务交付
- 接入独立开关、mchid/appId/签名/验签配置及必填提示
- 新交易读取已发布配置；历史交易仍原商户查单/退款/回调
- 同商户凭据轮换、原付款配置快照、关闭微信后的在途处理

## 必测失败与并发
- 双关闭被当免费
- 关闭开关误断退款、改商户追改旧单、预支付与线下竞态

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V64_get_api_app_owner_orders_orderNo_payment_options, V64_post_api_app_owner_orders_orderNo_offline_payment
画板：无
主责任表：无
验收：TC-CFG-011, TC-CFG-012, TC-CFG-013, TC-CFG-014, TC-CFG-015, TC-CFG-016, TC-CFG-017, TC-CFG-018, TC-CFG-019, TC-CFG-020, TC-CFG-021, TC-CFG-022

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
