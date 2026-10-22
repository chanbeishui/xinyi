#!/usr/bin/env python3
"""同步 sql/ry_20260417.sql 的 vi_ 段到 V6.4 口径（完整初始化脚本须一次到最新结构，根 AGENTS 要求）。

保留 ry 的 sys_/gen 段原样不动；把 E03 追加的 V6.1 vi_ 段（66 表）替换为 V6.4 001 的 79 表。
退役 vi_employee_binding_request/credential 不进入完整初始化（新安装不建；已有库保留见 migration_20261022）。
可复现：重跑即以 V6.4 001 为权威重生成 vi_ 段。
"""
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[4] / "docs/vehicle/V6.4"
SQL001 = BASE / "05_数据设计/001_vi业务表_初始化_V6.4.sql"
RY = Path("sql/ry_20260417.sql")

ry_lines = RY.read_text(encoding="utf8").split("\n")
# 定位 E03 追加的 vi_ 段起始（"机动车检测预约 V6.0：vi_*" 前的 '-- ---' 分隔行）
marker = next(i for i, l in enumerate(ry_lines) if "机动车检测预约 V6" in l and "vi_*" in l)
sep = marker - 1 if ry_lines[marker - 1].startswith("-- ---") else marker
sys_part = "\n".join(ry_lines[:sep]).rstrip()

o001 = SQL001.read_text(encoding="utf8")
vi_creates = o001[o001.index("CREATE TABLE"):]

header = """-- ----------------------------
-- 机动车检测预约 V6.4：vi_* 业务表（79 张，新安装完整初始化到最新结构）
-- 与 migration_20261021_vehicle_initial.sql（V6.1 66 表）+ migration_20261022_01_integration_config.sql
-- （V6.4 前向适配：+15 表、4 表加列、payment.status 扩 REVIEW_PENDING）增量结果同构。
-- 退役 vi_employee_binding_request/vi_employee_binding_credential 不在完整初始化创建；
--   已按 V6.1 建库的既有库保留其历史（不 DROP），见 migration_20261022 头部与回滚说明。
-- 仅 CREATE vi_*；不含 sys_ 改动，不插入员工/真实价格/商户。目标 MySQL 8.0.22+（CHECK/表达式默认）。
-- DATETIME 使用 UTC 分量；DEFAULT(UTC_TIMESTAMP(3)) 不依赖连接时区；UPDATE 由 Mapper 显式写 UTC_TIMESTAMP(3)。
-- 权威来源：资料包 V6.4 05_数据设计/001_vi业务表_初始化_V6.4.sql；由 v64_adapt/sync_ry.py 同步。
-- ----------------------------
SET NAMES utf8mb4;
SET time_zone = '+00:00';

"""

RY.write_text(sys_part + "\n\n" + header + vi_creates.rstrip() + "\n", encoding="utf8")
n_vi = vi_creates.count("CREATE TABLE")
print(f"ry_20260417.sql 已同步：sys_ 段保留（{sep} 行），vi_ 段替换为 V6.4 {n_vi} 张表")
print(f"新 ry 总行数：{len(RY.read_text(encoding='utf8').splitlines())}")
