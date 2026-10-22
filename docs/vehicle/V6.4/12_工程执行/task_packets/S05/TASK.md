# S05 · 服务确认与评价

阶段：M4；责任域：review；前置：F06, S04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`6c2634b1ef955a8f1720bc519cc45a477a2c59e0dcbce6bbf921e220707c3dd8`。

## 本任务交付
- 实际归还后24h无争议自动完成，车主确认/评价和评分来源

## 必测失败与并发
- 司机已释放不受后续评价影响
- 一单一次评价和防重

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_034_post, API_039_post
画板：O22, O22-review
主责任表：vi_review
验收：TC-115, TC-116, TC-117, TC-118, TC-119, TC-120, TC-121, TC-122, TC-123, TC-124, TC-125, TC-126, TC-143, TC-144, TC-148, TC-149, TC-150, TC-151, TC-152, TC-153, TC-154, TC-196

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/review/`
`xinyi-rongli/src/main/resources/mapper/rongli/review/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/review/`
`xinyi-ui/src/views/rongli/review/`
`xinyi-ui/src/api/rongli/review.ts`
`xinyi-miniapp/src/features/review/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S05ReviewIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
