# D02 · 自动邀约轮转与扩圈

阶段：M3；责任域：dispatch；前置：D01, P03。
范围版本：`V6.1-UNCHANGED`；definitionHash：`d6fcd551cc7600326ebbe5fd6afff8bc7c434d697ed785e0d9fae0da6b2a15c1`。

## 本任务交付
- 游标、公平轮转、单次邀约、截止和人工兜底
- DB占用原子

## 必测失败与并发
- 司机/订单不能双占
- 候选为空不死循环
- 超时撤旧邀约

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：vi_dispatch_invite, vi_driver_occupancy, vi_dispatch_cursor
验收：TC-051, TC-052, TC-053, TC-054, TC-055, TC-056, TC-057, TC-058, TC-059, TC-060, TC-061, TC-062, TC-063, TC-064, TC-065, TC-066

## 代码落位
`docs/implementation/vehicle/D02/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D02DispatchIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
