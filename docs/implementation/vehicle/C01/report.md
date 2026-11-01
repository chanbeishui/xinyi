# C01 · 字典、公开条款与参数配置 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-CONFIG-20261022。
接口：MINI API_010_get（业务字典）；ADMIN API_155_get/API_156_put（业务配置读/保存）；PUBLIC V6_get_public_service_config（公开服务配置）。
画板：B21/B21-edit（消息与内容模板）、B33/B33-edit（车型字典入口·复用框架）。主责任表：无（复用 sys_config/sys_dict_data/vi_price_rule）。验收用例：无（负向以 IT 覆盖）。

## 交付（对照 TASK.md）
- **业务字典（API_010，MINI）**：`ConfigService.dict` 白名单 vi_vehicle_type/vi_inspection_type，未知类型 404 不泄露；`DictController` 按契约返回 **rows/total 分页信封**（pageNum/pageSize，pageSize≤100），no-store。
- **业务配置读/保存（API_155/156，ADMIN）**：`ConfigService.getConfig/putConfig`；存 sys_config（vi.biz.* 白名单）+ vi.config.version CAS；功能权限 biz:config:query/edit 服务层校验；固定安全项（vi.biz.payment.cap.minutes/permission.registry/watermark）与非白名单键 403 拒绝；版本冲突 409 不静默覆盖；审计 + outbox 同事务；PUT 强制 Idempotency-Key。
- **公开服务配置（V6_get_public_service_config，PUBLIC 匿名）**：`ConfigService.publicConfig` 仅公开白名单发布值；**缺已发布协议（terms+privacy 版本）不开放下单**（bookingEnabled=false）；**第三方未配置不宣称可支付**（WECHAT 仅 vi.payment.wechat.configured=true 才暴露）；OFFLINE 由 vi.biz.payment.offline 控制；不含商户密钥/内部财务主体/员工名单。MiniSecurityConfig permitAll 增 /api/public/**。
- **价格守卫（防 0 元）**：`ConfigService.assertPriced` 新车型无 PUBLISHED 价格 → VI_SERVICE_UNAVAILABLE，供报价/下单调用。
- **前端**：PC B21 config.vue（vi.biz.* 可编辑表格+新增项+原因+CAS+幂等键，固定安全项禁用展示）+ B33 dict.vue（复用框架 getDicts 只读+跳转 /system/dict 维护）+ config.ts + 常量路由；小程序 features/config/api.ts（getPublicServiceConfig PUBLIC、getDict rows/total）。两端构建通过。

## 真实集成验证（C01ConfigIT 6/6，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C01ConfigIT verify` → exit 0，6 用例 0 失败 0 错误 0 跳过。
覆盖：
1. 公开配置匿名白名单+缺协议不开放下单+不宣称可支付+不泄商户密钥；
2. ADMIN 配置 CAS（旧版本 409）+固定安全项 403+非白名单 403；
3. 发布协议后开放下单且公开配置反映（termsVersion/serviceCities）；
4. 字典车型/检测类型 rows/total+未知类型 404；
5. 新车型无价格不能 0 元（assertPriced NEW_TYPE 拒绝；SMALL_CAR 有 PUBLISHED 放行）；
6. 生产不采用测试主体与金额（integration 不暴露 WECHAT/SIM_/test）。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10、A02 7、A01 7、C01 6 = 104，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-ui run build:prod` exit 0（config/dict chunk 产出）；`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0。

## Findings / 遗留
- **契约修正**：初版 DictController 误用 data 信封；主源 04_接口/openapi_V6.4.yaml API_010_get 明确 {code,msg,rows,total}（additionalProperties:false）。已新增 `ViRows` 信封并改控制器分页返回 rows/total，IT 同步断言 rows/total。公开/ADMIN 配置仍为 data 信封（与其契约一致）。
- **第三方配置/支付 method/外部路由/消息模板**：以主 PRD23 与 X01–X07 为准；本入口仅站内模板与公开内容/客服配置，第三方凭据/支付开关/外部短信订阅模板走 integrations（B49/B50），旧静态配置不形成旁路。WECHAT 暴露仅凭真实配置标志，未声称真实渠道通过。
- **B33 复用框架字典**：PC 经框架 getDicts（ADMIN）只读展示并跳转 /system/dict 维护，不另造重复字典；变更保留订单标签快照。计价规则维护属后续任务。
- **菜单基数据**：非超管需 sys_menu 授予 biz:config:query/edit（服务端强制）；常量路由仅超管可见，菜单/权限基数据属部署/权限任务。
- **小程序 config 特性**：api.ts 已就绪，待报价/下单/条款页消费（当前无页面引用，构建随工程通过）。

## 完成判据对照
- [x] 源接口/页面有明确实现文件与关联测试（API_010/155/156/V6_public；B21/B21-edit/B33/B33-edit；C01 无验收用例，负向以 IT 覆盖）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（C01ConfigIT 6/6 + 全量 104/104）
- [x] 标注测试第三方替身（SIMULATOR），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）