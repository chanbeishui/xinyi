# F01 · 私有文件全生命周期

阶段：M1；责任域：file；前置：I01, I06, E07。
范围版本：`V6.4-PRIVATE-FILE-20261105`；definitionHash：`ecf9fea54c027ce06c6878ee3ead1e6116618df0457a2865b258f2dc35b0ee60`。

## 本任务交付
- 签上传、实际字节存储、元数据核验、短期读取、敏感访问审计
- 双域上传者及明确父业务归属；私有字节传输、限流大小/像素和会话撤权复核；部署目录未配置拒绝使用

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
`xinyi-admin/src/test/java/com/xinyi/web/viit/F01FileIT.java`
`docs/implementation/vehicle/F01/`

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
