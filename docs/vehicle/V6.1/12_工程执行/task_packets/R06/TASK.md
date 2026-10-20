# R06 · 付款凭证登记与独立核实

阶段：M5；主责任域：externalpayment；前置：R04。

## 本任务交付

- 账单确认后关联真实付款凭证、流水、部分付款和0元无需付款

## 必测失败与并发

- 待核实暂占余额但不计已付
- 同主体流水唯一
- 不能按行随意分摊

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_get_api_admin_driver_payment_records, V4_post_api_admin_driver_payment_records, V4_post_api_admin_driver_payment_records__recordId__verify, V42_get_api_admin_driver_payment_records__recordId, V42_get_api_app_driver_payment_records

画板：B40, B40-record, B40-verify, B40-detail

建模对象：vi_driver_external_payment

源验收：TC-183, TC-184, TC-185, TC-186, TC-187, TC-188, TC-189, TC-R42-021, TC-R42-022, TC-R42-025, TC-R42-026, TC-R42-027, TC-R42-028, TC-R42-029, TC-R42-030, TC-R42-031, TC-R42-032, TC-R42-033, TC-R42-034, TC-R42-036, TC-R42-037, TC-R42-038

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/externalpayment/`  
`xinyi-rongli/src/main/resources/mapper/rongli/externalpayment/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/externalpayment/`  
`xinyi-ui/src/views/rongli/externalpayment/`  
`xinyi-ui/src/api/rongli/externalpayment.ts`  
`xinyi-miniapp/src/features/externalpayment/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R06ExternalpaymentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
