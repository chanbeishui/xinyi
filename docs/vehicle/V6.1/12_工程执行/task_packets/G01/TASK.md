# G01 · 全功能代码交付审计

阶段：M6；主责任域：testing；前置：T01, T02, T03, T04, T05, T06, T07, T08。

## 本任务交付

- 核对所有活动API、页面、表、用例的实现、集成和独立复核证据
- 真实渠道另计
- 先以--preflight检查除G01自身外的全部交付，再独立审查G01证据并标REVIEWED，最后不带--preflight运行最终完成门禁；避免审计任务自依赖死锁。

## 必测失败与并发

- 接口数量/截图数/空类/固定Ack不计完成
- 禁止借MVP删除已确认功能

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：基础能力/后台任务/测试，不另造公开业务API

画板：无独立画板

建模对象：复用前置任务表；不因此新增无用表

源验收：工程验收见本卡负向项与done_when

## 代码落位

`docs/implementation/vehicle/G01/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
python docs/vehicle/V6.1/12_工程执行/completion_gate.py --repo . --preflight
python docs/vehicle/V6.1/12_工程执行/taskctl.py summary --repo .
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
