# F02 · 四节点照片与里程证据

阶段：M3；主责任域：evidence；前置：F01, D05。

## 本任务交付

- 创建证据组、六类照片、替换留版本、接车归还里程必填/异常说明

## 必测失败与并发

- 缺一照片或倒退里程不ready
- ready不等于交接成功

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_041_get, API_063_post, API_064_get, API_065_post, API_066_put, API_067_post

画板：O16, D12, D12-low-ocr, D12-missing, D12-upload-failed, D15, D19, D22, D22-missing

建模对象：vi_evidence_group, vi_photo

源验收：工程验收见本卡负向项与done_when

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/evidence/`  
`xinyi-rongli/src/main/resources/mapper/rongli/evidence/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/evidence/`  
`xinyi-ui/src/views/rongli/evidence/`  
`xinyi-ui/src/api/rongli/evidence.ts`  
`xinyi-miniapp/src/features/evidence/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F02EvidenceIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
