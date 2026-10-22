# Q03 · 学习播放与考试尝试

阶段：M1；责任域：training；前置：Q01, Q02。
范围版本：`V6.1-UNCHANGED`；definitionHash：`5bc191f7020657e66d9074fee545dd71cf7b05e7a4224fe76f81dac167ea95f4`。

## 本任务交付
- 播放会话/心跳/墙钟进度、考试尝试生成读取提交、结果复用

## 必测失败与并发
- 倍速伪造累计拒绝
- 超时/重复提交只判一次
- 次数由配置限制

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_046_get, API_047_post, v5_get_api_app_driver_qualification_training_trainingId, v5_post_api_app_driver_qualification_training_trainingId_playback, v5_post_api_app_driver_qualification_training_trainingId_progress, v5_post_api_app_driver_qualification_training_trainingId_attempts, v5_get_api_app_driver_qualification_training_attempts_attemptId
画板：D39, D40, D41, D42
主责任表：vi_training_progress, vi_training_playback, vi_training_attempt
验收：TC-R50-017, TC-R50-018, TC-R50-019, TC-R50-020

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/training/`
`xinyi-rongli/src/main/resources/mapper/rongli/training/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/training/`
`xinyi-ui/src/views/rongli/training/`
`xinyi-ui/src/api/rongli/training.ts`
`xinyi-miniapp/src/features/training/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q03TrainingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
