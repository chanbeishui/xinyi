# E03 · 新vi数据库初装与框架数据保护

阶段：M0；责任域：database；前置：E00, E02。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`814654eb2d4e2dc9cbf26966871a9fa9734017b7613b1e9c89689f9761fd1e22`。

## 本任务交付
- 隔离新库首次创建本版业务模型；已有库先inventory再前向适配
- 旧申请/凭证流程停写留历史，不自动DROP
- 字段/PK/CHECK/生成列/UTC和初始化两路径真实一致性验证

## 必测失败与并发
- 存在未知业务表/真实交易中止写入，不以IF NOT EXISTS隐藏结构差异

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`sql/migration_YYYYMMDD_vehicle_initial.sql`
`sql/ry_20260417.sql`
`docs/implementation/vehicle/E03/`

## 验证命令
```bash
python docs/vehicle/V6.4/12_工程执行/database.py inventory --repo .
python docs/vehicle/V6.4/12_工程执行/database.py bootstrap --repo . --confirm-db xinyi_vi_dev
python docs/vehicle/V6.4/12_工程执行/engineer.py run --repo . --kind db-test
```

## 完成标准
- 在本包新建空隔离库实际执行初装及6项MySQL探针，并留报告
- 受控新增vi脚本与框架初装结构一致，生成列不手动写入
- 非空库拒绝、sys保护均已验证；未知库不盲转旧M0

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
