# B02 · 报价快照与服务校验

阶段：M2；责任域：booking；前置：C03, C04, A01, A02。
范围版本：`V6.1-UNCHANGED`；definitionHash：`52828b00ec96ea7d114994dac4fa41d48107821cdd74ebe3262cd52c404b5106`。

## 本任务交付
- 四组合输入归属、地图路线、站点匹配、保存报价输入和规则快照

## 必测失败与并发
- 越权地址车辆拒绝
- 快照过期/配置变更显式冲突
- 无隐藏自动保存地址簿

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_024_post
画板：无
主责任表：vi_quote
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/booking/`
`xinyi-rongli/src/main/resources/mapper/rongli/booking/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/booking/`
`xinyi-ui/src/views/rongli/booking/`
`xinyi-ui/src/api/rongli/booking.ts`
`xinyi-miniapp/src/features/booking/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B02BookingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
