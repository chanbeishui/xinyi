# T03 · 派单交接并发集成

阶段：M3；责任域：testing；前置：D01, D02, D03, D04, D05, D06, F01, F02, F03, F04, F05, F06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`5c88a9d828960a8af174f05dc6a74c3313ba1633fca642768ce1caf7f5d0b194`。

## 本任务交付
- 真实四次交接、物品/码/照片、先A后B任务、并发接受/归还

## 必测失败与并发
- 证据就绪不算交接
- 后台不足权限亦拒绝
- 未检测取消不造结果

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T03/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T03TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
