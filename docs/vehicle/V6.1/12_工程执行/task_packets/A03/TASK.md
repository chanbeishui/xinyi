# A03 · 车主档案管理

阶段：M1；主责任域：owner；前置：I02, I06, E05。

## 本任务交付

- 车主列表、资料、关联车辆订单售后、备注与关注标记

## 必测失败与并发

- 手机号不能后台直接改验证结果
- 跨公司历史不因一条订单暴露

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_get_api_admin_owners, V4_get_api_admin_owners__ownerId, V4_patch_api_admin_owners__ownerId, V4_get_api_admin_owners__ownerId__vehicles, V4_get_api_admin_owners__ownerId__orders

画板：B27, B27-detail, B27-edit, B27-empty, B27-error, B27-forbidden

建模对象：vi_owner_profile

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/owner/`  
`xinyi-rongli/src/main/resources/mapper/rongli/owner/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/owner/`  
`xinyi-ui/src/views/rongli/owner/`  
`xinyi-ui/src/api/rongli/owner.ts`  
`xinyi-miniapp/src/features/owner/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A03OwnerIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
