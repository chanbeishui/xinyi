# S04 · 普通异常与争议处置

阶段：M4；主责任域：support；前置：F06, I06。

## 本任务交付

- 普通异常、服务争议、人工接管与结论、暂停自动确认

## 必测失败与并发

- 不恢复事故理赔系统
- 通知失败不撤销已保存事实
- 保管责任始终明确

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_037_post, API_038_get, API_062_post, API_080_post, API_091_post, API_116_get, API_117_get, API_118_post, API_119_get, API_120_post

画板：O23, O23-progress, D24, S14, B15, B15-detail

建模对象：vi_dispute, vi_service_exception

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/support/`  
`xinyi-rongli/src/main/resources/mapper/rongli/support/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/support/`  
`xinyi-ui/src/views/rongli/support/`  
`xinyi-ui/src/api/rongli/support.ts`  
`xinyi-miniapp/src/features/support/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S04SupportIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
