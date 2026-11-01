# C02 · 检测点、营业与服务区域 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.1-UNCHANGED。
接口：MINI API_011_get（公开营业站点列表）、API_095_get/API_096_put（员工本站点读/改）；ADMIN API_128_get（列表）、API_129_post（创建）、API_130_put（更新）。
画板：S16（站点资料与资质·员工端）、B17/B17-edit/B17-disable（检测点管理/编辑抽屉/停用确认）。主责任表：vi_station。验收用例：无（负向以 IT 覆盖）。

## 契约与数据模型冲突处理（经用户确认：加列补齐·前向迁移）
派生 openapi 的 StationCreate/StationUpdate 要求 contactPhone+qualificationFileIds、Station/PublicStationView 要求 phoneMask，但 05_数据设计 vi_station 原无任何对应列。经用户拍板「加列补齐（前向迁移）」：
- 新增 `sql/migration_20261101_station_contact_qualification.sql`：vi_station 增 `contact_phone_cipher VARBINARY(512)`（PhoneCrypto 密文，对外仅脱敏）+ `qualification_file_ids JSON`（引用 vi_file 不透明ID）。
- 同步全量初始化 `sql/ry_20260417.sql` 与数据模型主源 `05_数据设计/data_model_V6.4.json`（vi_station cols/columnRules），并重新生成派生 `task_packets/C02/tables.json`。
- 已在测试库 xinyi_vi_it_e07 与业务库 xinyi 执行增量脚本并校验两列存在、类型/可空正确（可移植 SQL 随代码提交）。
- 主源 openapi 无 deptId 字段但有「迁部门在手订单拒绝」负向：deptId 作为 **ADMIN 受控扩展字段**（仅 /api/admin/stations 创建/更新接受，员工端 API_096 拒绝），迁部门须目标部门在本人数据范围内且无在手订单，否则 VI_STATE_CONFLICT 受控拒绝。

## 交付（对照 TASK.md）
- **后台与员工共用 Service**：`StationService` 同时服务 ADMIN（/api/admin/stations）与员工（/api/app/station/info）；员工端仅本人切换到的站点（actor.stationId + actingRole=STATION），不接受迁部门。
- **站点 CRUD/资质/地址/服务能力/营业范围**：创建（dept 权威归属：显式 deptId 经 checkDeptAccess 校验，否则取创建人部门）；更新全字段受控 + CAS 版本（版本必填，冲突 409）；启用/停用；支持车型/营业时间/服务半径/资质文件ID/联系电话。
- **公开营业列表（API_011，MINI）**：仅 enabled 站点的 PublicStationView（无 version/enabled/资质等内部状态）；电话脱敏；坐标 GCJ02。
- **ADMIN 列表数据范围**：按 vi_station.dept_id 经 IDeptScopeService 解析过滤；全量放行、空集合 fail-closed。
- **脱敏**：联系电话 PhoneCrypto 密文存储，Station/PublicStationView 仅返回 phoneMask，不泄完整号码/密文。

## 真实集成验证（C02StationIT 7/7，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C02StationIT verify` → exit 0，7 用例 0 失败 0 错误 0 跳过。
覆盖：
1. 公开营业列表仅启用站点+脱敏+无 version/enabled 内部字段+GCJ02；
2. ADMIN 创建（version 0、电话脱敏不泄完整号、GCJ02、支持车型）+契约校验（缺 Idempotency-Key 400、缺 name 400、坐标系非法 400）；
3. ADMIN 列表按部门数据范围（超管全量、受限管理员仅本部门、不见他部门）；
4. ADMIN 更新 CAS（version+1、停用生效 B17-disable、旧版本 409）；
5. **迁部门在手订单受控拒绝**（SCHEDULED 订单→deptId 迁移 409 VI_STATE_CONFLICT；订单办结后迁移成功且 dept_id 改变）；
6. **员工本站点读取/更新**（真实手机号绑定→站点授权→切 STATION 工作身份→API_095 读本站点→API_096 CAS 更新；员工端 deptId 400 拒绝）；
7. 员工无站点上下文拒绝（OWNER 读 /api/app/station/info 403）+ MINI 不获 ADMIN。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10、A02 7、A01 7、C01 6、A03 7、C02 7 = 118，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-ui run build:prod` exit 0（station chunk）；`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0（pages/station/info 产出）。

## Findings / 遗留
- **JSON 列健壮性**：vi_station.business_hours/supported_vehicle_codes 为 JSON 列，历史/异构值（如占位 '[]'）不应使列表 500；parseStr/parseList 改为按节点类型容错（字符串标量取文本、否则取原始 JSON；非数组返回空）。
- **businessHours 契约**：openapi 为 string，DB 列为 JSON；以 JSON 字符串标量存储（ViJson.write(string)），读回按节点类型还原。
- **资质文件ID**：qualification_file_ids 存不透明ID列表，实际文件生命周期属 F01（私有文件）；本任务仅存储引用，不校验文件存在。
- **deptId 扩展**：为落实「迁部门在手订单拒绝」负向，deptId 作为 ADMIN 受控字段加入创建/更新（员工端拒绝）；与派生 StationCreate/Update（additionalProperties:false）差异已在此登记，属契约-需求调和。
- **菜单基数据**：非超管需 sys_menu 授予 biz:station:list/create/edit（服务端强制）；常量路由仅超管可见，菜单/权限基数据属部署/权限任务。

## 完成判据对照
- [x] 源接口/页面/表有明确实现文件与关联测试（6 接口 + S16/B17 三态 + vi_station；C02 无验收用例，负向以 IT 覆盖）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（C02StationIT 7/7 + 全量 118/118）
- [x] 数据库变更随代码提供可移植 SQL（migration + ry 同步 + 数据模型主源 + 派生 tables.json 重生成），并在测试库执行校验
- [x] 标注测试第三方替身（SIMULATOR），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）