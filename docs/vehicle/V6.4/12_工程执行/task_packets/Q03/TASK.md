# Q03 · 学习播放与考试尝试

阶段：M1；责任域：training；前置：Q01, Q02。
范围版本：`V6.4-CONTROLLED-TRAINING-20261109`；definitionHash：`d9ac188d22efb9230e37e801c04b9182b1b65a83f4d207cc02ee80b1ace9c5bd`。

## 本任务交付
- 公司后台明确必修课程及确切已发布版本，正式次数/间隔配置与显式政策发布，无默认通过
- 首次播放冻结本申请/公司epoch课表；材料新run保留同公司冻结课表，后续政策/课程发布不覆盖
- 受保护真实媒体、播放会话/毫秒心跳/共享墙钟、考试尝试生成读取一次提交及结果复用
- 每题等分100，精确集合匹配，全卷最终HALF_UP整数后判通过；同申请稳定课程累计次数，材料不重置，失败超时计次
- 真实全部必修完成才产正式TRAINING证明；Q01实时核对申请/材料/公司epoch/课表与run，旧公司/伪造PASSED不旁路

## 必测失败与并发
- 未配置/未发布/空必修集不得真空通过；选入别公司或非可信发布课程整体拒绝
- 倍速/跳进度/重复乱序/离线补时/多会话不能超共享真实墙钟；毫秒余数不丢失
- 旧application/run/材料/epoch请求及未知原键不能漂移；A→B→A不复活旧证明
- 超时/并发/重复提交只判一次，旧V1题卷不被V2覆盖；HALF_UP只能最终全卷舍入
- 次数和间隔由冻结政策限制，同申请同courseKey累计；材料修改不能重置
- MINI仅当前本人冻结教材可读，无标准答案/阅卷密文/内容HMAC泄露，不授后台或DRIVER

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_046_get, API_047_post, v5_get_api_app_driver_qualification_training_trainingId, v5_post_api_app_driver_qualification_training_trainingId_playback, v5_post_api_app_driver_qualification_training_trainingId_progress, v5_post_api_app_driver_qualification_training_trainingId_attempts, v5_get_api_app_driver_qualification_training_attempts_attemptId, q03_get_api_admin_training_policies_ownerDeptId, q03_put_api_admin_training_policies_ownerDeptId, q03_post_api_admin_training_policies_ownerDeptId_publish
画板：D39, D40, D41, D42
主责任表：vi_training_progress, vi_training_playback, vi_training_attempt, vi_training_policy, vi_training_policy_revision, vi_training_plan_snapshot, vi_training_run
验收：TC-R50-017, TC-R50-018, TC-R50-019

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/training/`
`xinyi-rongli/src/main/resources/mapper/rongli/training/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/file/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/qualification/`
`xinyi-ui/src/views/rongli/training/`
`xinyi-ui/src/api/rongli/training.ts`
`xinyi-miniapp/src/features/training/`
`xinyi-miniapp/src/pages/training/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/Q03TrainingIT.java`
`xinyi-ui/src/views/rongli/training/policy.vue`
`xinyi-ui/src/api/rongli/trainingPolicy.ts`

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
