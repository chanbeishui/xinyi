# A02 · 地址簿与位置输入

阶段：M1；责任域：address；前置：I01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`4222768afc1371407a992bad6af72f73a79c8a398b0432c65aefeceec9fb4b56`。

## 本任务交付
- 新增/更新/删除/常用地址
- API返回版本
- 地图失败可手填后解析

## 必测失败与并发
- 首次临时地址不强制先保存
- 设置常用并发不出现多个默认

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_018_get, API_019_post, API_020_put, API_021_delete, API_022_post
画板：无
主责任表：vi_address
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/address/`
`xinyi-rongli/src/main/resources/mapper/rongli/address/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/address/`
`xinyi-ui/src/views/rongli/address/`
`xinyi-ui/src/api/rongli/address.ts`
`xinyi-miniapp/src/features/address/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A02AddressIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
