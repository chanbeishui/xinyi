# C05 · 司机独立费用规则

阶段：M5；主责任域：driverfee；前置：C01, I06。

## 本任务交付

- 固定/约定路线/取消返还/解除场景，草稿试算独立审核发布

## 必测失败与并发

- 客户收费不等于司机报酬
- 未匹配NULL不当0
- 不改历史

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_get_api_admin_driver_fee_rules, V4_post_api_admin_driver_fee_rules, V4_get_api_admin_driver_fee_rules__ruleId, V4_put_api_admin_driver_fee_rules__ruleId, V4_post_api_admin_driver_fee_rules__ruleId__publish, V4_post_api_admin_driver_fee_rules_trial

画板：B39, B39-edit, B39-review

建模对象：vi_driver_fee_rule

源验收：TC-169, TC-170

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/driverfee/`  
`xinyi-rongli/src/main/resources/mapper/rongli/driverfee/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/driverfee/`  
`xinyi-ui/src/views/rongli/driverfee/`  
`xinyi-ui/src/api/rongli/driverfee.ts`  
`xinyi-miniapp/src/features/driverfee/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C05DriverfeeIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
