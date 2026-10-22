# D04 · 司机接受与拒绝邀约

阶段：M3；责任域：dispatch；前置：D02, E04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`69fd9e6103260c9eeb92c17a1fe81ec2339478ced5cfb1daa3fd6c8a1653a4e9`。

## 本任务交付
- 本人邀约列表详情响应、当前承接，响应版本和占用转换

## 必测失败与并发
- 超时与接受只一成功
- 伪造driver拒绝
- 晚到响应不覆盖新单

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_051_get, API_052_get, API_053_post, API_054_get
画板：D07, D08
主责任表：vi_driver_assignment
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/dispatch/`
`xinyi-rongli/src/main/resources/mapper/rongli/dispatch/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/dispatch/`
`xinyi-ui/src/views/rongli/dispatch/`
`xinyi-ui/src/api/rongli/dispatch.ts`
`xinyi-miniapp/src/features/dispatch/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D04DispatchIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
