-- A04 每名代驾归属一家公司，旗下站点共用；前置20261107_01，按迁移序号执行。
-- 执行前备份vi_driver_profile及sys_menu，暂停代驾档案/派单/资格写入。
-- DDL不可重复执行。旧档案owner_dept_id保持NULL，不猜测员工主部门、创建者或历史订单公司。
-- NULL待显式全数据权限管理员认领；迁移不生成资格、不恢复新增承接、不修改听单、承接或车辆责任。
-- 回滚应用保留新增列/索引及归属审计，不删除或重写已有任务。重启后端生效；授权按统一会话撤权执行，无需手工清缓存。
ALTER TABLE vi_driver_profile
    ADD COLUMN owner_dept_id BIGINT NULL COMMENT '后台明确指定的归属公司部门；历史NULL待全局管理员认领' AFTER account_id,
    ADD KEY ix_driver_company (owner_dept_id,id),
    ADD CONSTRAINT ck_driver_owner_dept CHECK(owner_dept_id IS NULL OR owner_dept_id>0);

-- 无可信历史归属可自动回填，以下查询作为待认领清单，不静默修复。
SELECT id,account_id,qualification_status
FROM vi_driver_profile
WHERE owner_dept_id IS NULL
ORDER BY id;

CREATE TEMPORARY TABLE a04_menu_guard (
    violations INT NOT NULL CHECK(violations=0)
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971000
        AND (parent_id<>0
            OR COALESCE(perms,'')<>''));

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971000
        AND (parent_id<>0
            OR COALESCE(perms,'')<>''));

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971000,'代驾管理',0,100,'driver-management',
    NULL,1,1,'M','0',
    '0','','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971000
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971001
        AND (parent_id<>2971000
            OR COALESCE(perms,'')<>'biz:driver:list'))
    OR (perms='biz:driver:list'
        AND menu_id<>2971001);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971001
        AND (parent_id<>2971000
            OR COALESCE(perms,'')<>'biz:driver:list'))
    OR (perms='biz:driver:list'
        AND menu_id<>2971001);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971001,'代驾员档案',2971000,100,'drivers',
    'rongli/driver/index',1,1,'C','0',
    '0','biz:driver:list','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971001
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971002
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:query'))
    OR (perms='biz:driver:query'
        AND menu_id<>2971002);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971002
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:query'))
    OR (perms='biz:driver:query'
        AND menu_id<>2971002);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971002,'查看代驾档案',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:driver:query','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971002
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971003
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:edit'))
    OR (perms='biz:driver:edit'
        AND menu_id<>2971003);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971003
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:edit'))
    OR (perms='biz:driver:edit'
        AND menu_id<>2971003);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971003,'维护代驾备注',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:driver:edit','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971003
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971004
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:disablenewassignments'))
    OR (perms='biz:driver:disablenewassignments'
        AND menu_id<>2971004);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971004
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:disablenewassignments'))
    OR (perms='biz:driver:disablenewassignments'
        AND menu_id<>2971004);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971004,'暂停新增承接',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:driver:disablenewassignments','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971004
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971005
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:enablenewassignments'))
    OR (perms='biz:driver:enablenewassignments'
        AND menu_id<>2971005);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971005
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:enablenewassignments'))
    OR (perms='biz:driver:enablenewassignments'
        AND menu_id<>2971005);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971005,'恢复新增承接',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:driver:enablenewassignments','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971005
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971006
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:assigncompany'))
    OR (perms='biz:driver:assigncompany'
        AND menu_id<>2971006);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971006
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:driver:assigncompany'))
    OR (perms='biz:driver:assigncompany'
        AND menu_id<>2971006);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971006,'配置代驾归属公司',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:driver:assigncompany','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971006
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971007
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:assignment:list'))
    OR (perms='biz:assignment:list'
        AND menu_id<>2971007);

INSERT INTO a04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971007
        AND (parent_id<>2971001
            OR COALESCE(perms,'')<>'biz:assignment:list'))
    OR (perms='biz:assignment:list'
        AND menu_id<>2971007);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2971007,'查看代驾承接历史',2971001,100,'#',
    NULL,1,1,'F','0',
    '0','biz:assignment:list','peoples','migration','A04单公司代驾归属；转移/启停单独授权，不自动授角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971007
);

DROP TEMPORARY TABLE a04_menu_guard;

-- 执行后异常校验与结构/权限核对；异常公司不猜测替换。
SELECT d.id,d.owner_dept_id
FROM vi_driver_profile d
LEFT JOIN sys_dept company ON company.dept_id=d.owner_dept_id
WHERE d.owner_dept_id IS NOT NULL
    AND (company.dept_id IS NULL OR company.status<>'0' OR company.del_flag<>'0');

SHOW CREATE TABLE vi_driver_profile;

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE menu_id BETWEEN 2971000 AND 2971007
ORDER BY menu_id;
