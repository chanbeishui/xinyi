# C04 · 客户计价规则和试算

阶段：M2；主责任域：pricing；前置：C01, C02。

## 本任务交付

- 固定费/包含米/超里程单位进位/单或往返，独立审核发布版本

## 必测失败与并发

- 多匹配不猜最便宜
- 未配置拒绝报价
- test配置与prod隔离

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_133_get, API_134_post, API_135_put, API_136_post, API_137_post, API_138_post

画板：B20, B20-edit, B20-review

建模对象：vi_price_rule

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/pricing/`  
`xinyi-rongli/src/main/resources/mapper/rongli/pricing/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/pricing/`  
`xinyi-ui/src/views/rongli/pricing/`  
`xinyi-ui/src/api/rongli/pricing.ts`  
`xinyi-miniapp/src/features/pricing/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C04PricingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
