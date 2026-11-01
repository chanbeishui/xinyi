# C01 · 字典、公开条款与参数配置

阶段：M1；责任域：config；前置：I06, E05。
范围版本：`V6.4-PRICE-CONTEXT-20261101`；definitionHash：`5173e4b3de42b1f99e8661147d887af213b271dffa99c0f85f99cc5ae6312112`。

## 本任务交付
- 车型字典、检测类型、协议版本、客服资料、权限注册、默认安全配置
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

- C04适配：仅车型计数入口关闭；正式价格须校验真实站点、公司、车型/检测类型、时间和部署环境

## 必测失败与并发
- 新车型无价格不能0元下单
- 生产不能采用测试主体与金额

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_010_get, API_155_get, API_156_put, V6_get_public_service_config
画板：B21, B21-edit, B33, B33-edit
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/config/`
`xinyi-rongli/src/main/resources/mapper/rongli/config/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/config/`
`xinyi-ui/src/views/rongli/config/`
`xinyi-ui/src/api/rongli/config.ts`
`xinyi-miniapp/src/features/config/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C01ConfigIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
