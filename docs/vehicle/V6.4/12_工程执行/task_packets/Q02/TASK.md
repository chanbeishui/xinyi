# Q02 · 培训课程和题库管理

阶段：M1；责任域：training；前置：I06, C01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`82de4984663880db1afe235894ac8ad65e9f18879e77d0777c4ba54fdd23f96e`。

## 本任务交付
- 课程/题目草稿，核定标准和发布不可变版本

## 必测失败与并发
- 不向学员下发答案
- 发布后不覆盖旧试卷版本

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：v5_get_api_admin_training_courses, v5_post_api_admin_training_courses, v5_get_api_admin_training_courses_trainingId, v5_put_api_admin_training_courses_trainingId, v5_post_api_admin_training_courses_trainingId_publish
画板：B43, B44
主责任表：vi_training_course, vi_training_question
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/training/`
`xinyi-rongli/src/main/resources/mapper/rongli/training/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/training/`
`xinyi-ui/src/views/rongli/training/`
`xinyi-ui/src/api/rongli/training.ts`
`xinyi-miniapp/src/features/training/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q02TrainingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
