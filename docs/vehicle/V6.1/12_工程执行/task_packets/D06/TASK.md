# D06 · 未接车解除与原订单重派

阶段：M3；主责任域：fulfillment；前置：D04, E06。

## 本任务交付

- 解除指定承接/释放精确占用/保留支付预约/重派
- 生成取消规则事实

## 必测失败与并发

- 已接车禁止自助丢车
- 原订单不取消
- 不重复记满额报酬

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_060_post

画板：D31, D32, D33, D34, D35

建模对象：复用前置任务表；不因此新增无用表

源验收：TC-090, TC-091, TC-092, TC-093, TC-094, TC-095, TC-096, TC-097, TC-098, TC-099, TC-100, TC-101, TC-102, TC-103, TC-104

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/fulfillment/`  
`xinyi-rongli/src/main/resources/mapper/rongli/fulfillment/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/fulfillment/`  
`xinyi-ui/src/views/rongli/fulfillment/`  
`xinyi-ui/src/api/rongli/fulfillment.ts`  
`xinyi-miniapp/src/features/fulfillment/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D06FulfillmentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
