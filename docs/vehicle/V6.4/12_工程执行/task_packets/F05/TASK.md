# F05 · 最终检测结果与更正

阶段：M3；责任域：inspection；前置：F04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`b8e7751be17c51743caac6f5019b25acd18f4c7e9c9daafd67b9240f66c71012`。

## 本任务交付
- 收车后检测中，仅登记最终结果
- 不合格凭证、两端CAS、更正版本

## 必测失败与并发
- 不增加开始暂停子进度
- 并发双端只一结果
- 失败保留草稿

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_040_get, API_076_post, API_077_get, API_078_post, API_079_post, API_087_post, API_088_get, API_089_post, API_090_post, API_139_get, API_140_post
画板：O17, O18, O18-corrected, O18-pass, D17, D18, D18-unsafe, S10, S11, S11-conflict, S11-pass, S12, S12-notify-failed, S23, B11, B11-conflict, B30, B30-review
主责任表：vi_inspection_result, vi_result_correction
验收：TC-067, TC-068, TC-069, TC-070, TC-071, TC-072, TC-073, TC-074, TC-075, TC-076, TC-077, TC-078, TC-079, TC-080, TC-081, TC-082, TC-083, TC-084, TC-085, TC-086, TC-087, TC-088, TC-089

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/inspection/`
`xinyi-rongli/src/main/resources/mapper/rongli/inspection/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/inspection/`
`xinyi-ui/src/views/rongli/inspection/`
`xinyi-ui/src/api/rongli/inspection.ts`
`xinyi-miniapp/src/features/inspection/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F05InspectionIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
