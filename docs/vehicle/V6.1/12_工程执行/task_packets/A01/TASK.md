# A01 · 车辆档案与PC车辆列表

阶段：M1；主责任域：vehicle；前置：I01, I06, E05。

## 本任务交付

- 本人车辆CRUD/常用/软删，PC详情/维护/关联记录
- 历史快照不追改

## 必测失败与并发

- 伪造owner、猜车牌、软删后冲突、生成列不入DTO

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_013_get, API_014_post, API_015_put, API_016_delete, API_017_post, V4_get_api_admin_vehicles, V4_get_api_admin_vehicles__vehicleId, V4_patch_api_admin_vehicles__vehicleId, V4_get_api_admin_vehicles__vehicleId__orders, V4_get_api_admin_vehicles__vehicleId__inspection_results

画板：O04, O05, O27, O27-address, B09, B09-detail, B09-edit, B09-empty, B09-error, B09-forbidden

建模对象：vi_vehicle

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/vehicle/`  
`xinyi-rongli/src/main/resources/mapper/rongli/vehicle/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/vehicle/`  
`xinyi-ui/src/views/rongli/vehicle/`  
`xinyi-ui/src/api/rongli/vehicle.ts`  
`xinyi-miniapp/src/features/vehicle/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A01VehicleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
