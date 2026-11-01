# C02 · 检测点、营业与服务区域

阶段：M1；责任域：station；前置：I06, C01。
范围版本：`V6.4-STATION-AUTH-20261101`；definitionHash：`4ea6e333e8b99104e5fb55418b6d2af00e81f959ea62554643a464b40708b50f`。

## 本任务交付
- 站点CRUD/资质/地址/服务能力/营业范围
- 后台与员工共用Service
- 站点显式选择权威部门，ADMIN写DTO与员工写DTO分离
- 工作请求实时校验有效关联、角色功能权限及站点部门范围，撤权不依赖Redis
- 创建和更新使用事务幂等，重放前仍检查当前访问权限

## 必测失败与并发
- 迁部门存在在手订单需拒绝或受控方案
- 版本必填

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_011_get, API_095_get, API_096_put, API_128_get, API_129_post, API_130_put
画板：S16, B17, B17-disable, B17-edit
主责任表：vi_station
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/station/`
`xinyi-rongli/src/main/resources/mapper/rongli/station/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/station/`
`xinyi-ui/src/views/rongli/station/`
`xinyi-ui/src/api/rongli/station.ts`
`xinyi-miniapp/src/features/station/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C02StationIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
