# S02 · 一次审核及退款预留

阶段：M2；主责任域：aftersale；前置：S01, I06。

## 本任务交付

- 当前保管事实+可退额预览、批准/驳回
- 已接车WAIT_RETURN

## 必测失败与并发

- 取消批准不释放持车司机
- 仅退款不改履约
- 审核并发只一次

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_post_api_admin_orders__orderNo__after_sales, V4_get_api_admin_orders__orderNo__after_sales, V4_get_api_admin_after_sales, V4_get_api_admin_after_sales__caseId, V4_post_api_admin_after_sales__caseId__notes, V4_post_api_admin_after_sales__caseId__review_preview, V4_post_api_admin_after_sales__caseId__review, V4_get_api_admin_owners__ownerId__after_sales

画板：B13, B13-return, B14, B14-before, B14-complete, B14-conflict, B14-processing, B14-refund, B14-rejected, B14-return, B14-unknown

建模对象：vi_aftersale_review

源验收：TC-163, TC-164, TC-165, TC-166, TC-167, TC-168, TC-171, TC-172, TC-173, TC-174, TC-175, TC-176, TC-177

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/aftersale/`  
`xinyi-rongli/src/main/resources/mapper/rongli/aftersale/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/aftersale/`  
`xinyi-ui/src/views/rongli/aftersale/`  
`xinyi-ui/src/api/rongli/aftersale.ts`  
`xinyi-miniapp/src/features/aftersale/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S02AftersaleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
