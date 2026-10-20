# B01 · 先时段后地址的预约界面

阶段：M2；主责任域：booking；前置：E04, A01, A02, C03。

## 本任务交付

- 实现O32→O33→O31→O34，草稿/返回/修改失效，链接API而非内存订单

## 必测失败与并发

- 切换地址车型重报价
- 已选时段不提前扣名额

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_023_get

画板：O03, O03-empty, O31, O32, O32-unavailable, O33, O33-location-failed, O33-outside, O34, O34-full, O34-no-price, O34-price-changed

建模对象：复用前置任务表；不因此新增无用表

源验收：TC-019, TC-020, TC-021, TC-022, TC-023, TC-024, TC-025, TC-026, TC-027, TC-028, TC-029, TC-030, TC-031, TC-032, TC-033, TC-034, TC-035, TC-036

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/booking/`  
`xinyi-rongli/src/main/resources/mapper/rongli/booking/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/booking/`  
`xinyi-ui/src/views/rongli/booking/`  
`xinyi-ui/src/api/rongli/booking.ts`  
`xinyi-miniapp/src/features/booking/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B01BookingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
