# T05 · 全域授权和字段负向集成

阶段：M6；责任域：testing；前置：T01, T02, T03, T04, N03。
范围版本：`V6.4-IDENTITY-20261022`；definitionHash：`32025807f653a93e69e54713fb0d3b00e0c73c3d273f92b80893206a3f6f8476`。

## 本任务交付
- 全部Mini/Admin端点按权限矩阵，序列化嵌套白名单，敏感访问

## 必测失败与并发
- 缓存删除失败仍撤权
- 未授权站点/主体/文件/培训答案不泄漏

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：TC-162, TC-R50-001, TC-R50-002, TC-R50-003, TC-R50-004, TC-R50-005, TC-R50-006, TC-R50-009, TC-R50-010, TC-R50-011, TC-R50-013, TC-R50-014, TC-R50-015, TC-R50-021, TC-R50-022, TC-R50-023, TC-R50-024, TC-R50-025, TC-R50-026, TC-R50-027, TC-R50-028, TC-R50-031

## 代码落位
`docs/implementation/vehicle/T05/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T05TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
