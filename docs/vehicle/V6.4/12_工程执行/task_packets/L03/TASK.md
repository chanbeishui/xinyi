# L03 · 生产部署、恢复与值班演练

阶段：L；责任域：release；前置：T05, T06, T07, T08。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`550cb7f42b74f73f6c121623dff22824ccec7e959618f116e470cda26ea2dec6`。

## 本任务交付
- Linux容器/Nginx/RDS或MySQL/Redis/私有OSS，灰度健康探针备份回滚

## 必测失败与并发
- 未知业务政策禁止开放收费
- 公网不暴露管理/测试/Swagger/Druid

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/L03/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=L03ReleaseIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 指定真实环境/设备/主体的测试通过
- 负责人员批准，凭证保存在受控目录且对报告脱敏

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
