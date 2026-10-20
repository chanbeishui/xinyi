# B04 · 四端订单投影与时间轴

阶段：M2；主责任域：order；前置：B03, I06, E05, E04。

## 本任务交付

- Owner/Driver/Station/Admin四DTO和列表
- 金额地址额外裁剪

## 必测失败与并发

- 不能从列表绕详情拿敏感字段
- 结束任务后driver地址联系人null

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_026_get, API_027_get, API_028_get, API_055_get, API_056_get, API_072_get, API_073_get, API_083_get, API_084_get

画板：O14, O14-cancel-return, O14-cancel-review, O14-cancelled, O14-dispatching, O14-manual, O15, O26, O26-empty, O26-error, O26-loading, D09, D25, D25-cancelled, D36, S02, S03, S04, S05, S06, B03, B03-empty, B03-error, B03-forbidden, B03-loading, B04, B04-returned, B10

建模对象：复用前置任务表；不因此新增无用表

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/order/`  
`xinyi-rongli/src/main/resources/mapper/rongli/order/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/order/`  
`xinyi-ui/src/views/rongli/order/`  
`xinyi-ui/src/api/rongli/order.ts`  
`xinyi-miniapp/src/features/order/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=B04OrderIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
