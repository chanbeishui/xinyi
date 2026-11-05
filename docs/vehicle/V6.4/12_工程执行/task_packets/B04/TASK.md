# B04 · 四端订单投影与时间轴

阶段：M2；责任域：order；前置：B03, I06, E05, E04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`9e8617a0adea4ac76053ac1c9aa95c5ae754199c4f2e5cfc2bd7f6fd2c1a4681`。

## 本任务交付
- Owner/Driver/Station/Admin四DTO和列表
- 金额地址额外裁剪

## 必测失败与并发
- 不能从列表绕详情拿敏感字段
- 结束任务后driver地址联系人null

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_026_get, API_027_get, API_028_get, API_055_get, API_056_get, API_072_get, API_073_get, API_083_get, API_084_get
画板：O14, O14-cancel-return, O14-cancel-review, O14-cancelled, O14-dispatching, O14-manual, O15, O26, O26-empty, O26-error, O26-loading, D09, D25, D25-cancelled, D36, S02, S03, S04, S05, S06, B03, B03-empty, B03-error, B03-forbidden, B03-loading, B04, B04-returned, B10
主责任表：无
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
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B04OrderIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
