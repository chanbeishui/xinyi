# I06 · 部门、站点、主体组合权限

阶段：M1；主责任域：scope；前置：I04, I05。

## 本任务交付

- 对接真实DeptScope
- 查询/详情/批量写/下载二次授权
- 空范围拒绝

## 必测失败与并发

- 站点不以OR绕部门边界
- ALL部门不自动全站
- 财务不泄跨付款主体

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_094_get, API_131_get, API_132_put

画板：S18, B25, B26, B26-revoke

建模对象：vi_station_grant

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/scope/`  
`xinyi-rongli/src/main/resources/mapper/rongli/scope/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/scope/`  
`xinyi-ui/src/views/rongli/scope/`  
`xinyi-ui/src/api/rongli/scope.ts`  
`xinyi-miniapp/src/features/scope/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I06ScopeIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
