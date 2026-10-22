# Q05 · 资格审批与到期复审

阶段：M1；责任域：qualification；前置：Q01, Q03, Q04, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`c2180563939e2d2361d93e1876f7240b7f548f84e55e65fb687fe1ada9cc88e0`。

## 本任务交付
- 后台资格列表详情审核，资格快照、到期任务、拒绝原因

## 必测失败与并发
- 个人不能自批
- 证件失效禁新单但历史/返还不丢
- 无保险条件

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_121_get, API_122_get, API_123_post
画板：B16, B16-review
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/qualification/`
`xinyi-rongli/src/main/resources/mapper/rongli/qualification/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/qualification/`
`xinyi-ui/src/views/rongli/qualification/`
`xinyi-ui/src/api/rongli/qualification.ts`
`xinyi-miniapp/src/features/qualification/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q05QualificationIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
