# E03 · 新vi数据库初装与框架数据保护

阶段：M0；当前用户未实施M0。前置：E00, E02。

## 本任务交付

- 新隔离空库先导入框架基础，再初建本版66vi表与guard；不默认执行旧M0迁移
- 在仓库sql/生成首次新增vi的受控增量并更新完整初始化；以实际结构对比验证一致性
- 已有sys库保留；意外同名vi表停止相关写入并只读记录

## 代码落位

`sql/migration_YYYYMMDD_vehicle_initial.sql`  
`sql/ry_20260417.sql`  
`docs/implementation/vehicle/E03/`  

## 失败与边界

- 存在未知业务表/真实交易中止写入，不以IF NOT EXISTS隐藏结构差异

## 验证命令

```text
python docs/vehicle/V6.1/12_工程执行/database.py inventory --repo .
python docs/vehicle/V6.1/12_工程执行/database.py bootstrap --repo . --confirm-db xinyi_vi_dev
python docs/vehicle/V6.1/12_工程执行/engineer.py run --repo . --kind db-test
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 在本包新建空隔离库实际执行初装及6项MySQL探针，并留报告
- 受控新增vi脚本与框架初装结构一致，生成列不手动写入
- 非空库拒绝、sys保护均已验证；未知库不盲转旧M0

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。
