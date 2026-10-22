# A03 · 车主档案管理

阶段：M1；责任域：owner；前置：I02, I06, E05。
范围版本：`V6.1-UNCHANGED`；definitionHash：`21b395d8f6e06eb43e55b5218aa52deaa16d4aec4375bd0b175bddba3715df20`。

## 本任务交付
- 车主列表、资料、关联车辆订单售后、备注与关注标记

## 必测失败与并发
- 手机号不能后台直接改验证结果
- 跨公司历史不因一条订单暴露

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_owners, V4_get_api_admin_owners__ownerId, V4_patch_api_admin_owners__ownerId, V4_get_api_admin_owners__ownerId__vehicles, V4_get_api_admin_owners__ownerId__orders
画板：B27, B27-detail, B27-edit, B27-empty, B27-error, B27-forbidden
主责任表：vi_owner_profile
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/owner/`
`xinyi-rongli/src/main/resources/mapper/rongli/owner/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/owner/`
`xinyi-ui/src/views/rongli/owner/`
`xinyi-ui/src/api/rongli/owner.ts`
`xinyi-miniapp/src/features/owner/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A03OwnerIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
