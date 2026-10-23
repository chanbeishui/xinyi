"""把荣利业务表/菜单/字典同步进框架完整初始化脚本 sql/ry_20260417.sql。

背景：资料包 E03 要求「在仓库 sql/ 首次新增 vi 的受控增量并更新完整初始化」，
即新环境导入 ry_20260417.sql 后应当直接具备 66 张 vi_* 表与荣利菜单字典，
不能只依赖增量脚本。

做法：增量脚本保持为唯一事实来源，本工具把
  - sql/migration_..._01（全部 vi_* 建表）
  - sql/migration_..._02（菜单、字典、业务参数、演示检测点与计价）
  - sql/migration_..._03（仅 sys_menu 语句）
以标记块形式追加到 ry_20260417.sql 末尾；重复执行会先删除旧块再重建。

用法：python logs/build_init_sql.py
"""
import re
import sys
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
SQL = BASE / "sql"
INIT = SQL / "ry_20260417.sql"
MIG1 = SQL / "migration_20261021_01_rongli_vi_business_tables.sql"
MIG2 = SQL / "migration_20261021_02_rongli_menu_dict_seed.sql"
MIG3 = SQL / "migration_20261021_03_rongli_admin_pages.sql"

BEGIN = "-- @rongli-vi-begin（本块由 logs/build_init_sql.py 生成，勿手工编辑）"
END = "-- @rongli-vi-end"


def menu_statements(path):
    """从 03 里只取涉及 sys_menu 的语句，跳过演示课程与题库数据。"""
    text = path.read_text(encoding="utf-8")
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    out = []
    for stmt in text.split(";"):
        s = stmt.strip()
        if not s or s.startswith("--"):
            continue
        if "sys_menu" in s:
            out.append(s + ";")
    return "\n".join(out)


def main():
    for p in (INIT, MIG1, MIG2, MIG3):
        if not p.exists():
            print("缺少文件:", p)
            return 2

    text = INIT.read_text(encoding="utf-8")
    if BEGIN in text:
        head = text.split(BEGIN)[0].rstrip() + "\n"
    else:
        head = text.rstrip() + "\n"

    parts = [
        BEGIN,
        "-- 荣利机动车检测预约 V6.1：业务表 66 张 + 菜单 2100-2120 + 字典与业务参数",
        "-- 说明：增量脚本为唯一事实来源；此处为初始化内联副本，执行一次即得到最新结构。",
        "",
        "-- ---------------------------------------------------------------- 业务表",
        MIG1.read_text(encoding="utf-8").strip(),
        "",
        "-- ---------------------------------------------------------------- 菜单、字典与业务参数",
        MIG2.read_text(encoding="utf-8").strip(),
        "",
        "-- ---------------------------------------------------------------- 后台补齐页面菜单（2115-2120）",
        menu_statements(MIG3),
        END,
        "",
    ]
    INIT.write_text(head + "\n" + "\n".join(parts), encoding="utf-8")
    print("已同步:", INIT)
    print("总行数:", len(INIT.read_text(encoding="utf-8").splitlines()))
    return 0


if __name__ == "__main__":
    sys.exit(main())
