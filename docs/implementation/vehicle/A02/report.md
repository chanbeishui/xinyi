# A02 · 地址簿与位置输入 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.1-UNCHANGED。
接口：API_018_get（列表）、API_019_post（新增）、API_020_put（修改）、API_021_delete（软删除）、API_022_post（设常用），均 MINI。主责任表：vi_address。

## 交付（对照 TASK.md）
- **新增/更新/删除/常用地址**：`AddressService` CRUD+设常用；归属本人（越权 404 不泄露）；软删除 deleted_at；API 返回 version 供 CAS。
- **并发设常用单默认**：以账号行锁串行+清旧默认（排除目标 id，避免 CAS 版本被清默认操作 bump）保证并发不出现多个默认；创建即默认亦清旧。
- **地址/位置加密**：`AddressCrypto` AES-GCM(data-key) 加密 address_cipher/location_cipher（不做手机号规范化，保留原文/坐标 JSON）；明文不落日志/审计；密钥缺失 fail-closed。
- **地图失败可手填后解析**：位置为 GCJ02 坐标+详细地址文本，前端可手填；报价前须可解析到服务坐标（B 系列校验）。
- **临时地址不强制先保存**：地址簿仅管理已保存地址；list 空可用；预约可携带草稿地址（B 系列语义）。
- **前端**：小程序 features/address/api.ts + pages/address/list.vue（手填坐标兜底）；PC address.ts 共享类型（车主地址为 MINI 私有，后台不读明文）。两端构建通过。

## 真实集成验证（A02AddressIT 7/7，真实 HTTP+MINI链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A02AddressIT verify` → exit 0，7 用例 0 失败 0 错误 0 跳过。
覆盖：CRUD+版本返回、设常用并发唯一默认、创建即默认清旧、越权404、CAS冲突409、删除默认不残留、非法位置/未知字段拒绝+空列表可用。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10、A02 7 = 91，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0；`npm --prefix xinyi-ui run build:prod` exit 0。

## Findings / 遗留
- **无默认唯一索引**：vi_address 无部分唯一索引，单默认靠账号行锁+同事务清旧保证（并发测试验证）。
- **临时地址草稿语义属 B 系列**：A02 不强制保存；报价携带草稿地址的解析校验在 B01/B02。
- **PC 不读车主地址明文**：隐私边界，PC 仅共享 DTO 类型。

## 完成判据对照
- [x] 源接口/表有明确实现文件与关联测试（A02 无画板/验收用例，负向以 IT 覆盖）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（A02AddressIT 7/7）
- [x] 标注测试第三方替身（SIMULATOR），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）