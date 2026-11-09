# Q02 · 培训课程和题库管理

阶段：M1；责任域：training；前置：I06, C01。
范围版本：`V6.4-COMPANY-TRAINING-20261109`；definitionHash：`20ca296a5d1fa0ed258e8b51163615338d0194e566e744965a7eb08637d8be31`。

## 本任务交付
- 各公司独立课程/题库草稿，旗下站点与公司代驾共用；公司显式归属，不从主部门推断
- 先建允许未完成草稿，再上传真实私有MP4与维护题目；完整门槛校验后发布不可变内容版本
- 同课程族派生新草稿，不覆盖原发布/题库/历史试卷；列表无答案，详情query+公司范围，含答案摘要HMAC
- F01扩展TRAINING_MEDIA父课程、有限内存流传输/完整性与容器/轨道校验，图片规则保持；不生成正式培训通过证明

## 必测失败与并发
- 公司越权、空范围、客户端伪造公司/状态/内容版本/答案权限均拒绝
- 不向学员下发答案；列表或无query写响应无题库；实际学员及阅卷留Q03
- 发布后不覆盖旧试卷版本，单族最多一个草稿，UUID/CAS并发及数据库/outbox失败整体回滚
- 真实视频必须当前草稿/同族继承白名单/READY；错误用途、证件/异公司文件及伪造MP4不得发布

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：v5_get_api_admin_training_courses, v5_post_api_admin_training_courses, v5_get_api_admin_training_courses_trainingId, v5_put_api_admin_training_courses_trainingId, v5_post_api_admin_training_courses_trainingId_publish
画板：B43, B44
主责任表：vi_training_course, vi_training_question
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/training/`
`xinyi-rongli/src/main/resources/mapper/rongli/training/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/file/`
`xinyi-ui/src/views/rongli/training/index.vue`
`xinyi-ui/src/api/rongli/training.ts`
`xinyi-ui/src/api/rongli/privateFile.ts`
`xinyi-admin/src/test/java/com/xinyi/web/viit/Q02TrainingIT.java`

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
