# Q03 · 学习播放与考试尝试

阶段：M1；主责任域：training；前置：Q01, Q02。

## 本任务交付

- 播放会话/心跳/墙钟进度、考试尝试生成读取提交、结果复用

## 必测失败与并发

- 倍速伪造累计拒绝
- 超时/重复提交只判一次
- 次数由配置限制

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_046_get, API_047_post, v5_get_api_app_driver_qualification_training_trainingId, v5_post_api_app_driver_qualification_training_trainingId_playback, v5_post_api_app_driver_qualification_training_trainingId_progress, v5_post_api_app_driver_qualification_training_trainingId_attempts, v5_get_api_app_driver_qualification_training_attempts_attemptId

画板：D39, D40, D41, D42

建模对象：vi_training_progress, vi_training_playback, vi_training_attempt

源验收：TC-R50-020

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/training/`  
`xinyi-rongli/src/main/resources/mapper/rongli/training/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/training/`  
`xinyi-ui/src/views/rongli/training/`  
`xinyi-ui/src/api/rongli/training.ts`  
`xinyi-miniapp/src/features/training/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q03TrainingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
