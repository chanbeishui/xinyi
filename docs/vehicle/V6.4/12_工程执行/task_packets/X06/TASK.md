# X06 · 第三方配置与支付页全量落地

阶段：M4；责任域：frontend；前置：X02, X03, X04, X05, E05。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`da33bc22b1c3cd6e275a0d982414eef829afa0ef7ba45d3dd6fa4b15ea5df5a6`。

## 本任务交付
- 实现B46–B53、原B21/B24导航与O10实际支付选择
- 主PRD、API类型、字段清单、配置状态/失败恢复/权限不足同步
- 关键动作保存/校验/发布分开，未保存离开、敏感输入清理

## 必测失败与并发
- 仅静态画板/保存即生效/两开关联动关闭
- 密钥暴露、跨主体旧表单、未知发送盲目重试

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：B46, B47, B48, B49, B50, B51, B52, B53, B46-both, B46-offline, B46-paused, B46-invalid, B46-draft, B46-conflict, B47-empty, B47-certificate, B47-rotating, B48-aliyun, B48-disabled, B48-failed, B49-aliyun, B49-invalid, B49-test, B50-invalid, B50-no-consent, B50-disabled, B51-unknown, B51-failed, B52-review, B52-verified, B52-refund, B52-late, B53-empty, O10-both, O10-offline, O10-disabled, O10-review, O10-offline-paid
主责任表：无
验收：TC-CFG-055, TC-CFG-056, TC-CFG-057, TC-CFG-058

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
