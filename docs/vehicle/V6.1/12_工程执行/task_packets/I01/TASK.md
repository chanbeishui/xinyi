# I01 · 微信登录与基础账号

阶段：M1；主责任域：identity；前置：E06, E07。

## 本任务交付

- 可信openid创建唯一vi_account及安全状态
- 不透明token持久化
- 本人profile

## 必测失败与并发

- 同openid并发只一账号
- 不按role授高权
- accountId=1不成为超管

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_001_post, API_005_get

画板：O29, D01, S01, B01

建模对象：vi_account, vi_account_security

源验收：TC-001, TC-002, TC-003, TC-004, TC-005, TC-006, TC-007, TC-008, TC-009, TC-010, TC-011, TC-012, TC-013, TC-014, TC-015, TC-016, TC-017, TC-018, TC-R50-007, TC-R50-008, TC-R50-012, TC-R50-032

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
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I01IdentityIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
