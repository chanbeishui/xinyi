# F01 · 私有文件全生命周期

阶段：M1；责任域：file；前置：I01, I06, E07。
范围版本：`V6.1-UNCHANGED`；definitionHash：`cfdc25b0e8e82e7ff12f02af6f435bef2c5ae04fe3dcab5aa1e1f2b2b78fa9b9`。

## 本任务交付
- 签上传、实际字节存储、元数据核验、短期读取、敏感访问审计

## 必测失败与并发
- 伪造文件ID/对象路径/未上传/过期签名拒绝
- 生产非公开profile目录

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_157_post, API_158_post, API_159_get, API_160_post
画板：无
主责任表：vi_file
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/file/`
`xinyi-rongli/src/main/resources/mapper/rongli/file/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/file/`
`xinyi-ui/src/views/rongli/file/`
`xinyi-ui/src/api/rongli/file.ts`
`xinyi-miniapp/src/features/file/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F01FileIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
