# X03 · 客户线下收退款与人工资金核实

阶段：M2；责任域：payment；前置：X02, P02, S02。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`7cc3266ac1aaf24883458e48ea65385a0a41b6050e2da57b31337bc7a1409189`。

## 本任务交付
- 实现OFFLINE意图、全额证明登记、不同人员核实；独立于司机付款
- 30分钟超时/证明待审/容量释放与重获、资金事实与预约冲突分开
- 同一售后审核产生线下退款任务，凭证核实更新资金不再审核取消

## 必测失败与并发
- 自己核实/双通道重复付款/超额/重复流水
- 晚到钱不认账或强行复活名额、线下退款误调用微信

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V64_get_api_admin_offline_payments, V64_post_api_admin_offline_payments, V64_get_api_admin_offline_payments_proofId, V64_post_api_admin_offline_payments_proofId_verify
画板：无
主责任表：vi_offline_payment_record
验收：TC-CFG-023, TC-CFG-024, TC-CFG-025, TC-CFG-026, TC-CFG-027, TC-CFG-028, TC-CFG-029, TC-CFG-030, TC-CFG-031, TC-CFG-032, TC-CFG-033, TC-CFG-034, TC-CFG-035

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
