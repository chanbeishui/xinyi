# E03 vi 数据库初装与框架数据保护 · 报告

状态：核心完成（VERIFIED 待置）。执行者：Qoder-Agent。目标库：**现有 `xinyi`**（用户决定，非资料包默认的隔离 xinyi_vi_dev）。

## 路径偏差说明（重要）
资料包 `database.py` 的 `connect()` 硬性只允许 `xinyi_vi_dev`/`xinyi_vi_it_*` 且 `bootstrap` 拒绝非空库——这是为"一次性隔离容器库"设计的安全闸。用户决定 vi 表入现有 `xinyi` 库（vi 模块作为 xinyi-admin 一部分、复用框架单数据源，vi_station.dept_id / vi_employee_binding.sys_user_id 与 sys_ 同库直接关联）。因此：
- **不使用 database.py bootstrap**（它会拒绝 xinyi）；改手动 mysql 客户端前向执行 + 自写 pymysql 核验脚本。
- **安全网**：执行前 mysqldump 备份 xinyi → `.vi-local/backup/xinyi_pre_vi_<ts>.sql`（gitignore，不入库）。
- 仅 CREATE vi_* 表，绝不 DROP/ALTER/CREATE 任何 sys_ 或既有业务表。

## 执行步骤与结果（真实 MySQL 9.6.0）
1. **备份**：mysqldump xinyi（35 CREATE TABLE，132K）→ .vi-local/backup/（gitignore）。
2. **生成迁移脚本** `sql/migration_20261021_vehicle_initial.sql`：AGENTS 合规头部（用途/适用前置/执行顺序/备份要求/不可重复执行/失败恢复/数据安全/目标版本/时间约定/来源）+ 001 的 66 张 vi CREATE TABLE（含 E01 R1/R2 补齐的枚举 CHECK）。无 IF NOT EXISTS（遵 check#29）。
3. **执行**：对 xinyi 前向执行，退出码 0。结果：vi_ 表 **66**、总表 **102**（原 36+66）、sys_ 表 **23 未变**。
4. **结构一致性核验**（自写 `verify_structure_and_probes.py`，pymysql，替代在 9.6 下解包报错的 verify_mysql.py）：66 表齐全、missing=[]、unexpected=[]、**列集合 0 不符、主键 0 不符、生成列 0 不符**、CHECK 约束 **158** 个。structurePass=true。
5. **6 项真实探针**（事务 ROLLBACK，无残留数据）全部 PASS：
   - CHECK 拒绝非法枚举（vi_account.status='INVALID' → errno 3819）
   - 生成列禁止手动写入（vi_aftersale_case.active_order → errno 3105）
   - 自然键唯一/主键（vi_dispatch_cursor.scope_key 重复 → 1062）
   - 唯一约束（vi_account uk_app_open 重复 → 1062）
   - UTC 默认值不依赖连接时区（created_at vs UTC_TIMESTAMP(3) 差<5s）
   - 乐观锁 version 默认 0
6. **同步完整初始化** `sql/ry_20260417.sql`：追加 vi_ 段（66 CREATE），与迁移脚本 vi CREATE 集合**完全一致**；ry 总行数 809→2308。
7. **不可重复执行守卫**：重建 vi_account → ERROR 1050（table exists），证明无 IF NOT EXISTS 守卫有效。
8. **sys_ 保护**：迁移前后 sys_ 表均 23 张，未改动。
9. **字典种子** `sql/migration_20261021_vehicle_dict.sql`：创建 vehicle_type(3 示例)/inspection_type(2 示例) 字典类型+数据，存在性守卫、**可重复执行**（二次运行无重复）、不硬造 dict_id/dict_code、样本值标注非生产默认。

## 菜单/权限种子（R10 剩余，延后到 E05）
126 个 biz:* 权限码对应的 sys_menu 树与后台路由/组件强耦合。在 E05 后台页面存在前插入菜单会产生指向不存在路由的坏菜单项。故**菜单/权限种子随 E05 生成**（用 ≥3000 安全 menu_id 段，现 max=9002 需复核避让），不默认授予全部员工权限。字典种子已在 E03 补齐（vi 表引用的 vehicle_type/inspection_type）。

## 版本偏差与门槛
- 开发实测 MySQL **9.6.0**；生产目标 **8.4**。9.6 满足 ≥8.0.22（CHECK/表达式默认/生成列均验证通过）。**生产 8.4 上线前须复跑同样 SQL/CAS/探针**（RELEASE 门槛，记 BLOCKERS）。
- xinyi 库排序规则 utf8mb4_0900_ai_ci 与 vi 表一致 → 无 collation 混用风险（R11 消除）。

## 产物
- `sql/migration_20261021_vehicle_initial.sql`（66 vi 表，AGENTS 合规）
- `sql/migration_20261021_vehicle_dict.sql`（字典种子，可重复执行）
- `sql/ry_20260417.sql`（同步 vi 段）
- `docs/implementation/vehicle/E03/verify_structure_and_probes.py`、`structure_and_probes.json`
- 备份：`.vi-local/backup/xinyi_pre_vi_<ts>.sql`（gitignore）

## 完成判据对照（E03 TASK.md）
- [x] 实际执行初装 + 6 项 MySQL 探针并留报告（对 xinyi 库，非隔离空库——用户决定，已备份+仅 CREATE vi_）
- [x] 受控新增 vi 脚本与完整初始化结构一致（66 CREATE 集合一致 + 结构核验 0 不符）
- [x] 生成列不手动写入（探针 3105 验证）
- [x] 非空库拒绝/不可重复执行（1050 守卫）、sys 保护（23 表未变）均已验证
- [ ] 菜单/权限种子（延后 E05，字典种子已补）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）
