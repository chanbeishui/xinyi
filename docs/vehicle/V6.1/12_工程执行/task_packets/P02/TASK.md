# P02 · 支付可信通知、查单与关单

阶段：M2；主责任域：payment；前置：P01, E06。

## 本任务交付

- 原始body验签/解密/金额商户一致、查单补偿、30分钟可信关单

## 必测失败与并发

- 回调与关单竞争
- 未知不误报未扣款
- 回调防重放与永久去重

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_030_get, API_031_post, API_165_post, API_166_post

画板：B31, B31-query

建模对象：vi_channel_event

源验收：TC-037, TC-038, TC-039, TC-040, TC-041, TC-042, TC-043, TC-044, TC-045, TC-046, TC-047, TC-048, TC-049, TC-050

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/payment/`  
`xinyi-rongli/src/main/resources/mapper/rongli/payment/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/payment/`  
`xinyi-ui/src/views/rongli/payment/`  
`xinyi-ui/src/api/rongli/payment.ts`  
`xinyi-miniapp/src/features/payment/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=P02PaymentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
