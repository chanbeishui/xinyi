# F05 · 最终检测结果与更正

阶段：M3；主责任域：inspection；前置：F04。

## 本任务交付

- 收车后检测中，仅登记最终结果
- 不合格凭证、两端CAS、更正版本

## 必测失败与并发

- 不增加开始暂停子进度
- 并发双端只一结果
- 失败保留草稿

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_040_get, API_076_post, API_077_get, API_078_post, API_079_post, API_087_post, API_088_get, API_089_post, API_090_post, API_139_get, API_140_post

画板：O17, O18, O18-corrected, O18-pass, D17, D18, D18-unsafe, S10, S11, S11-conflict, S11-pass, S12, S12-notify-failed, S23, B11, B11-conflict, B30, B30-review

建模对象：vi_inspection_result, vi_result_correction

源验收：TC-067, TC-068, TC-069, TC-070, TC-071, TC-072, TC-073, TC-074, TC-075, TC-076, TC-077, TC-078, TC-079, TC-080, TC-081, TC-082, TC-083, TC-084, TC-085, TC-086, TC-087, TC-088, TC-089

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/inspection/`  
`xinyi-rongli/src/main/resources/mapper/rongli/inspection/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/inspection/`  
`xinyi-ui/src/views/rongli/inspection/`  
`xinyi-ui/src/api/rongli/inspection.ts`  
`xinyi-miniapp/src/features/inspection/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F05InspectionIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
