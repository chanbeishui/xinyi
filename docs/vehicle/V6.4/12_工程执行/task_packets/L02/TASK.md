# L02 · 微信真机和提交审核

阶段：L；责任域：release；前置：T08, L01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`0f1cb894637c9978cd059d8324b23fed4b73e0df2196f54072f67bdf5d065cc5`。

## 本任务交付
- iOS/Android登录相机定位导航切后台锁屏重连支付，隐私域名配置

## 必测失败与并发
- 不能只凭开发者工具/浏览器通过
- 不承诺连续后台定位

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/L02/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=L02ReleaseIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 指定真实环境/设备/主体的测试通过
- 负责人员批准，凭证保存在受控目录且对报告脱敏

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
