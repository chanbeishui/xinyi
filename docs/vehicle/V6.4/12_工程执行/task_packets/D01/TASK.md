# D01 · 听单、位置与资格判定

阶段：M3；责任域：dispatch；前置：Q05, I04。
范围版本：`V6.4-LISTENING-DAY-20261112`；definitionHash：`06dc4295165b1ef9698ea6b0cc01b1724e81a4667e355176af1ebe93bae8777e`。

## 本任务交付
- 公司独立配置每日下班时间及位置/可达阈值，真实权限/CAS/审计
- 本人手动开启固定当日截止；切后台保留听单意愿，下班关闭、次日不自动恢复
- 分开听单意愿/行政许可/资格/客户端可达/定位新鲜度/在手任务，拒绝原因明确

## 必测失败与并发
- 旧位置/旧会话/旧公司epoch不报实时
- 截止及次日不能因延迟任务或幂等重放继续听单
- 关闭听单不释放在手承接、占用或车辆责任
- 跨公司、功能或数据越权拒绝；坐标不写明文审计、不提供后台轨迹直播

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_048_get, API_049_patch, API_050_post, d01_post_driver_presence, d01_get_company_listening_config, d01_put_company_listening_config
画板：D06, D06-occupied, D06-offline, B24-listening-company
主责任表：vi_driver_location, vi_driver_presence, vi_driver_listening_config
验收：TC-D01-001, TC-D01-002, TC-D01-003, TC-D01-004, TC-D01-005

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/dispatch/`
`xinyi-rongli/src/main/resources/mapper/rongli/dispatch/`
`xinyi-ui/src/views/rongli/dispatch/`
`xinyi-ui/src/api/rongli/listeningPolicy.ts`
`xinyi-miniapp/src/features/dispatch/`
`xinyi-miniapp/src/pages/work/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D01DispatchIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
