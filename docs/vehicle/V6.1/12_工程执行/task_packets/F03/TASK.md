# F03 · 四次交接的主体核验

阶段：M3；主责任域：handover；前置：F02, I06。

## 本任务交付

- 类型/保管方/持码方/核验方/验证码/错误计数/一次性交接

## 必测失败与并发

- 自己拿码自己验证拒绝
- 错码计数即使业务回滚也持久
- 同码不可跨单

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_068_post, API_069_get, API_070_get, API_071_post

画板：O35, O35-receipt, O35-return, D13, D13-code-error, D13-locked, D20, D20-cancel, D23, D23-cancel, S07, S07-code-error, S13, S13-cancel, S13-released

建模对象：vi_handover, vi_handover_attempt_guard

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/handover/`  
`xinyi-rongli/src/main/resources/mapper/rongli/handover/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/handover/`  
`xinyi-ui/src/views/rongli/handover/`  
`xinyi-ui/src/api/rongli/handover.ts`  
`xinyi-miniapp/src/features/handover/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F03HandoverIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
