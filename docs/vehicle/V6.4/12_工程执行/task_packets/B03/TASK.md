# B03 · 原子建单与预占

阶段：M2；责任域：order；前置：B02, E06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`35e1c7094b5ce27e35bd40e9ce565a7c61782f76d1c8aee4ae328747a2134d1d`。

## 本任务交付
- 只按报价创建订单/支付信息/预约预占/30分钟截止
- 协议接受记录

## 必测失败与并发
- 同键同体复用
- 不同体409
- 并发最后名额只一单成功

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_025_post
画板：无
主责任表：vi_order, vi_quota_reservation
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/order/`
`xinyi-rongli/src/main/resources/mapper/rongli/order/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/order/`
`xinyi-ui/src/views/rongli/order/`
`xinyi-ui/src/api/rongli/order.ts`
`xinyi-miniapp/src/features/order/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B03OrderIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
