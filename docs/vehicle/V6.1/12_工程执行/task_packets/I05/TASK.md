# I05 · 员工绑定完整生产消费链

阶段：M1；主责任域：identity；前置：I03。

## 本任务交付

- 本人请求→后台核实名单→一次性凭证签发/撤销→本人消费

## 必测失败与并发

- 跨账户/重复/过期凭证拒绝
- 员工失效后OWNER仍可使用

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_008_post, v5_post_api_common_employee_binding_requests, v5_get_api_common_employee_binding_requests, v5_get_api_admin_employee_binding_requests, v5_post_api_admin_employee_binding_credentials, v5_post_api_admin_employee_binding_credentials_credentialId_revoke

画板：U02, U02-expired, U02-pending, B36

建模对象：vi_employee_binding, vi_employee_binding_request, vi_employee_binding_credential

源验收：TC-R50-017, TC-R50-018, TC-R50-019

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/identity/`  
`xinyi-rongli/src/main/resources/mapper/rongli/identity/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/identity/`  
`xinyi-ui/src/views/rongli/identity/`  
`xinyi-ui/src/api/rongli/identity.ts`  
`xinyi-miniapp/src/features/identity/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I05IdentityIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
