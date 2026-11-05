# S02 · 一次审核及退款预留

阶段：M2；责任域：aftersale；前置：S01, I06。
范围版本：`V6.4-AFTERSALE-REVIEW-20261106`；definitionHash：`1cbea4eaf33807c569ce60206434b62bffae6d26c665768e94d457ca8336ccb2`。

## 本任务交付
- 当前保管事实+可退额预览、批准/驳回
- 已接车WAIT_RETURN
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路
- 订单部门与原收款主体资金权限分别校验；后台代办与公开补充、可信同人贡献者不可自审
- 公开publicOpinion与内部opinion隔离；预览不写款，审核同事务预留与稳定原方式退款单，渠道执行交P04

## 必测失败与并发
- 取消批准不释放持车司机
- 仅退款不改履约
- 审核并发只一次

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_post_api_admin_orders__orderNo__after_sales, V4_get_api_admin_orders__orderNo__after_sales, V4_get_api_admin_after_sales, V4_get_api_admin_after_sales__caseId, V4_post_api_admin_after_sales__caseId__notes, V4_post_api_admin_after_sales__caseId__review_preview, V4_post_api_admin_after_sales__caseId__review, V4_get_api_admin_owners__ownerId__after_sales
画板：B13, B13-return, B14, B14-before, B14-complete, B14-conflict, B14-processing, B14-refund, B14-rejected, B14-return, B14-unknown
主责任表：vi_aftersale_review, vi_aftersale_participant_user
验收：TC-091, TC-092, TC-096, TC-097, TC-098, TC-116, TC-117, TC-122, TC-126, TC-143, TC-144, TC-145, TC-147, TC-149, TC-150, TC-151, TC-152

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/aftersale/`
`xinyi-rongli/src/main/resources/mapper/rongli/aftersale/`
`xinyi-ui/src/views/rongli/aftersale/`
`xinyi-ui/src/api/rongli/aftersale.ts`
`xinyi-miniapp/src/features/aftersale/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/aftersale/controller/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/S02AftersaleIT.java`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S02AftersaleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
