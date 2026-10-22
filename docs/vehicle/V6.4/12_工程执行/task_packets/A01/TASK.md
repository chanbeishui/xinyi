# A01 · 车辆档案与PC车辆列表

阶段：M1；责任域：vehicle；前置：I01, I06, E05。
范围版本：`V6.1-UNCHANGED`；definitionHash：`ecd093ceb77b406549659d945c8275d079bc8513e8cf15e27050c90b4bb576fe`。

## 本任务交付
- 本人车辆CRUD/常用/软删，PC详情/维护/关联记录
- 历史快照不追改

## 必测失败与并发
- 伪造owner、猜车牌、软删后冲突、生成列不入DTO

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_013_get, API_014_post, API_015_put, API_016_delete, API_017_post, V4_get_api_admin_vehicles, V4_get_api_admin_vehicles__vehicleId, V4_patch_api_admin_vehicles__vehicleId, V4_get_api_admin_vehicles__vehicleId__orders, V4_get_api_admin_vehicles__vehicleId__inspection_results
画板：O04, O05, O27, O27-address, B09, B09-detail, B09-edit, B09-empty, B09-error, B09-forbidden
主责任表：vi_vehicle
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/vehicle/`
`xinyi-rongli/src/main/resources/mapper/rongli/vehicle/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/vehicle/`
`xinyi-ui/src/views/rongli/vehicle/`
`xinyi-ui/src/api/rongli/vehicle.ts`
`xinyi-miniapp/src/features/vehicle/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A01VehicleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
