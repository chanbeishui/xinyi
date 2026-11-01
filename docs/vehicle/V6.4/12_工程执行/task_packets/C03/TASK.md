# C03 · 上门时段与容量计划

阶段：M2；责任域：capacity；前置：C02, E06。
范围版本：`V6.4-STATION-QUOTA-20261101`；definitionHash：`3f0fa553908852ece5326e383d7dea51dc5ff108acc91cebdd8c91073f69c301`。

## 本任务交付
- 每站独立接车和接收额度；后台显式创建未来时段，拒绝同站同类区间重叠
- 容量调整只读影响预览、停约及预占/正式/已消耗统计；调容不改订单占用
- 服务端实时权限、版本CAS与事务幂等，代码及前后端真实验证

## 必测失败与并发
- 上门时段非到站考核
- 无地址不承诺具体站名额
- 不得缩容低于有效占用

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_081_get, API_082_put, API_092_get, API_093_put, V64_post_api_admin_capacity
画板：S17, S17-impact, B19, B19-impact
主责任表：vi_quota_slot
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/capacity/`
`xinyi-rongli/src/main/resources/mapper/rongli/capacity/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/capacity/`
`xinyi-ui/src/views/rongli/capacity/`
`xinyi-ui/src/api/rongli/capacity.ts`
`xinyi-miniapp/src/features/capacity/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C03CapacityIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
