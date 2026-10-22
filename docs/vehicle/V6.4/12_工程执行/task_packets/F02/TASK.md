# F02 · 四节点照片与里程证据

阶段：M3；责任域：evidence；前置：F01, D05。
范围版本：`V6.1-UNCHANGED`；definitionHash：`a00e5f51a25951c2fdc9436201857f7f164b78f0a60e731bcf0a9b3be9775bd6`。

## 本任务交付
- 创建证据组、六类照片、替换留版本、接车归还里程必填/异常说明

## 必测失败与并发
- 缺一照片或倒退里程不ready
- ready不等于交接成功

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_041_get, API_063_post, API_064_get, API_065_post, API_066_put, API_067_post
画板：O16, D12, D12-low-ocr, D12-missing, D12-upload-failed, D15, D19, D22, D22-missing
主责任表：vi_evidence_group, vi_photo
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/evidence/`
`xinyi-rongli/src/main/resources/mapper/rongli/evidence/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/evidence/`
`xinyi-ui/src/views/rongli/evidence/`
`xinyi-ui/src/api/rongli/evidence.ts`
`xinyi-miniapp/src/features/evidence/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F02EvidenceIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
