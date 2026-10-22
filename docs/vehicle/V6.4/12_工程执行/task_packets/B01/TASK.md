# B01 · 先时段后地址的预约界面

阶段：M2；责任域：booking；前置：E04, A01, A02, C03。
范围版本：`V6.1-UNCHANGED`；definitionHash：`bdb3ee7f1054a9f0261d90beaa98e2dc58bbedf4101bf5db859e9c264f4991d1`。

## 本任务交付
- 实现O32→O33→O31→O34，草稿/返回/修改失效，链接API而非内存订单

## 必测失败与并发
- 切换地址车型重报价
- 已选时段不提前扣名额

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_023_get
画板：O03, O03-empty, O31, O32, O32-unavailable, O33, O33-location-failed, O33-outside, O34, O34-full, O34-no-price, O34-price-changed
主责任表：无
验收：TC-019, TC-020, TC-021, TC-022, TC-023, TC-024, TC-025, TC-026, TC-027, TC-028, TC-029, TC-030, TC-031, TC-032, TC-033, TC-034, TC-035, TC-036

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/booking/`
`xinyi-rongli/src/main/resources/mapper/rongli/booking/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/booking/`
`xinyi-ui/src/views/rongli/booking/`
`xinyi-ui/src/api/rongli/booking.ts`
`xinyi-miniapp/src/features/booking/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B01BookingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
