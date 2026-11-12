# D03 · 人工邀约与计划变更

阶段：M3；责任域：dispatch；前置：D02, E05。
范围版本：`V6.1-UNCHANGED`；definitionHash：`81937cf8e47cb944a0a42340a0c644b581dcaa0f26fcdbb74501194194ebfedb`。

## 本任务交付
- 调度队列/候选/邀约记录/规则和影响预览
- 当前API098–101/B05/B06已由D02共享真实回归覆盖；剩余102–105尤其旧计划影响预览/应用仍待D03完成，当前测试为D02DispatchIT

## 必测失败与并发
- 人工派单仍需司机接受
- 不可绕资格
- 配置修改不追改在手任务

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_098_get, API_099_get, API_100_post, API_101_get, API_102_get, API_103_put, API_104_post, API_105_post
画板：B05, B05-invite, B06, B24, B24-impact
主责任表：无
验收：TC-054

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/dispatch/`
`xinyi-rongli/src/main/resources/mapper/rongli/dispatch/`
`xinyi-ui/src/views/rongli/dispatch/`
`xinyi-ui/src/api/rongli/dispatch.ts`
`xinyi-miniapp/src/features/dispatch/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/dispatch/controller/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D03DispatchIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
