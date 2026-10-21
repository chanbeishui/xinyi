-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  management_scope  varchar(16)     not null default 'DEPT'    comment '管理范围（DEPT部门账号 PLATFORM平台账号）',
  auth_version      bigint(20)      not null default 0         comment '授权版本',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  pwd_update_date   datetime                                   comment '密码最后更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  103, 'DEPT', 0, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'DEPT', 0, 'ry',    '若依', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '岗位ID',
  post_code     varchar(64)     not null                   comment '岗位编码',
  post_name     varchar(50)     not null                   comment '岗位名称',
  post_sort     int(4)          not null                   comment '显示顺序',
  status        char(1)         not null                   comment '状态（0正常 1停用）',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''			       comment '更新者',
  update_time   datetime                                   comment '更新时间',
  remark        varchar(500)    default null               comment '备注',
  primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'ceo',  '董事长',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '项目经理',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '人力资源',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '普通员工',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');


-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  query             varchar(255)    default null               comment '路由参数',
  route_name        varchar(50)     default ''                 comment '路由名称',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');
insert into sys_menu values('4', '若依官网', '0', '4', 'http://ruoyi.vip', null, '', '', 0, 0, 'M', '0', '0', '', 'guide',    'admin', sysdate(), '', null, '若依官网地址');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      '', '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '服务监控菜单');
insert into sys_menu values('113',  '缓存监控', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缓存监控菜单');
insert into sys_menu values('114',  '缓存列表', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缓存列表菜单');
insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');
insert into sys_menu values('117',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, '系统接口菜单');
-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, '登录日志菜单');
-- 用户管理按钮
insert into sys_menu values('1000', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '用户修改', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '用户删除', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '用户导出', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '用户导入', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '重置密码', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 角色管理按钮
insert into sys_menu values('1007', '角色查询', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', '角色新增', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '角色修改', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '角色删除', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '角色导出', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 菜单管理按钮
insert into sys_menu values('1012', '菜单查询', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', '菜单新增', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '菜单修改', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '菜单删除', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 部门管理按钮
insert into sys_menu values('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 岗位管理按钮
insert into sys_menu values('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮
insert into sys_menu values('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 参数设置按钮
insert into sys_menu values('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 通知公告按钮
insert into sys_menu values('1035', '公告查询', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', '公告新增', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '公告修改', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '公告删除', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 操作日志按钮
insert into sys_menu values('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 登录日志按钮
insert into sys_menu values('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 定时任务按钮
insert into sys_menu values('1049', '任务查询', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '任务新增', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '任务修改', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '任务删除', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '状态修改', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '任务导出', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '生成修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '生成删除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '导入代码', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '预览代码', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '生成代码', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1061', '任职部门修改', '100', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:dept:edit',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1062', '用户角色修改', '100', '9', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:role:edit',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1063', '平台账号降级', '100', '10', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:management:edit', '#', 'admin', sysdate(), '', null, '');
-- 数据大屏菜单
insert into sys_menu values('2024', '数据大屏', '0', '4', 'dataScreen', null, '', 'DataScreen', 1, 0, 'M', '0', '0', '', 'monitor',       'admin', sysdate(), '', null, '数据大屏目录');
insert into sys_menu values('2025', '管理层BI看板', '2024', '1', 'biDashboard', 'dataScreen/index', '', 'BiDashboard', 1, 0, 'C', '0', '0', '', 'chart', 'admin', sysdate(), '', null, '管理层BI看板（ECharts 数据可视化）');
insert into sys_menu values('2026', '档案室管理', '2024', '2', 'archiveRoom', 'archiveRoom/index', '', 'ArchiveRoomManage', 1, 0, 'C', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '档案室平面图管理与检索');
-- 数字孪生菜单
insert into sys_menu values('9000', '数字孪生', '0', '5', 'digital-twin', null, '', 'DigitalTwin', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '', null, '数字孪生目录');
insert into sys_menu values('9001', '数字档案室', '9000', '1', 'archive-room', 'digital-twin/archive-room/index', '', 'ArchiveRoomDigitalTwin', 1, 0, 'C', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '数字档案室三维看板');
insert into sys_menu values('9002', '智慧仪器室', '9000', '2', 'instrument-room', 'digital-twin/instrument-room/index', '', 'InstrumentRoomDigitalTwin', 1, 0, 'C', '0', '0', '', 'dashboard', 'admin', sysdate(), '', null, '智慧仪器室三维看板');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 6.1、用户任职部门关联表
-- ----------------------------
drop table if exists sys_user_dept;
create table sys_user_dept (
  user_id      bigint(20)      not null                   comment '用户ID',
  dept_id      bigint(20)      not null                   comment '任职部门ID',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  primary key (user_id, dept_id),
  key idx_sys_user_dept_dept_user (dept_id, user_id)
) engine=innodb comment = '用户任职部门关联表';

insert into sys_user_dept values (1, 103, 'admin', sysdate());
insert into sys_user_dept values (2, 105, 'admin', sysdate());


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');
insert into sys_role_menu values ('2', '2024');
insert into sys_role_menu values ('2', '2025');
insert into sys_role_menu values ('2', '2026');
insert into sys_role_menu values ('2', '9000');
insert into sys_role_menu values ('2', '9001');
insert into sys_role_menu values ('2', '9002');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');


-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
insert into sys_config values(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '0',             'Y', 'admin', sysdate(), '', null, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');
insert into sys_config values(10, '用户注册-默认主部门',          'sys.user.defaultDeptId',           '103',           'Y', 'admin', sysdate(), '', null, '开启用户注册前必须配置为有效部门ID，禁止创建无部门账号');


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  start_time          datetime                                  comment '执行开始时间',
  end_time            datetime                                  comment '执行结束时间',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '公告ID',
  notice_title      varchar(50)     not null                   comment '公告标题',
  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
  notice_content    longblob        default null               comment '公告内容',
  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(255)    default null               comment '备注',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------
insert into sys_notice values('1', '温馨提醒：2018-07-01 若依新版本发布啦', '2', '新版本内容', '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('2', '维护通知：2018-07-01 若依系统凌晨维护', '1', '维护内容',   '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('3', '若依开源框架介绍', '1', '<p><span style=\"color: rgb(230, 0, 0);\">项目介绍</span></p><p><font color=\"#333333\">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style=\"color: rgb(51, 51, 51);\">岗位管理</span><span style=\"color: rgb(51, 51, 51);\">、定时任务</span><span style=\"color: rgb(51, 51, 51);\">、</span><span style=\"color: rgb(51, 51, 51);\">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color=\"#333333\">分布式文件存储</font><span style=\"color: rgb(51, 51, 51);\">、分库分表处理等技术特点。</span></p><p><img src=\"https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png\" style=\"width: 64px;\"><br></p><p><span style=\"color: rgb(230, 0, 0);\">官网及演示</span></p><p><span style=\"color: rgb(51, 51, 51);\">若依官网地址：&nbsp;</span><a href=\"http://ruoyi.vip\" target=\"_blank\">http://ruoyi.vip</a><a href=\"http://ruoyi.vip\" target=\"_blank\"></a></p><p><span style=\"color: rgb(51, 51, 51);\">若依文档地址：&nbsp;</span><a href=\"http://doc.ruoyi.vip\" target=\"_blank\">http://doc.ruoyi.vip</a><br></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【不分离版】：&nbsp;</span><a href=\"http://demo.ruoyi.vip\" target=\"_blank\">http://demo.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【分离版本】：&nbsp;</span><a href=\"http://vue.ruoyi.vip\" target=\"_blank\">http://vue.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【微服务版】：&nbsp;</span><a href=\"http://cloud.ruoyi.vip\" target=\"_blank\">http://cloud.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【移动端版】：&nbsp;</span><a href=\"http://h5.ruoyi.vip\" target=\"_blank\">http://h5.ruoyi.vip</a></p><p><br style=\"color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;\"></p>', '0', 'admin', sysdate(), '', null, '管理员');


-- ----------------------------
-- 18、公告已读记录表
-- ----------------------------
drop table if exists sys_notice_read;
create table sys_notice_read (
  read_id          bigint(20)       not null auto_increment    comment '已读主键',
  notice_id        int(4)           not null                   comment '公告id',
  user_id          bigint(20)       not null                   comment '用户id',
  read_time        datetime         not null                   comment '阅读时间',
  primary key (read_id),
  unique key uk_user_notice (user_id, notice_id)   comment '同一用户同一公告只记录一次'
) engine=innodb auto_increment=1 comment='公告已读记录表';


-- ----------------------------
-- 19、安全会话清理outbox
-- ----------------------------
drop table if exists sys_security_outbox;
create table sys_security_outbox (
  event_id            bigint(20)      not null auto_increment comment '事件ID',
  user_id             bigint(20)      not null                comment '受影响用户ID',
  target_auth_version bigint(20)      not null                comment '目标授权版本',
  event_type          varchar(64)     not null                comment '事件类型',
  status              varchar(16)     not null default 'PENDING' comment 'PENDING RETRY DONE',
  retry_count         int             not null default 0      comment '重试次数',
  next_retry_time     datetime        not null                comment '下次执行时间',
  last_error          varchar(1000)   default null            comment '最后错误',
  create_time         datetime        not null                comment '创建时间',
  complete_time       datetime        default null            comment '完成时间',
  primary key (event_id),
  key idx_security_outbox_pending (status, next_retry_time, event_id),
  key idx_security_outbox_user (user_id, event_id)
) engine=innodb comment='安全会话清理outbox';


-- ----------------------------
-- 20、安全审计表
-- ----------------------------
drop table if exists sys_security_audit_log;
create table sys_security_audit_log (
  audit_id            bigint(20)      not null auto_increment comment '审计ID',
  event_type          varchar(64)     not null                comment '事件类型',
  actor_user_id       bigint(20)      default null            comment '操作者用户ID',
  actor_user_name     varchar(64)     default null            comment '操作者账号',
  target_user_id      bigint(20)      default null            comment '目标用户ID',
  request_id          varchar(64)     not null                comment '请求ID',
  client_ip           varchar(128)    default null            comment '客户端IP',
  reason              varchar(500)    default null            comment '操作原因',
  before_json         text                                    comment '变更前快照',
  after_json          text                                    comment '变更后快照',
  auth_version_before bigint(20)      default null            comment '变更前版本',
  auth_version_after  bigint(20)      default null            comment '变更后版本',
  result              varchar(16)     not null                comment 'SUCCESS FAILURE',
  failure_code        varchar(64)     default null            comment '失败码',
  failure_message     varchar(1000)   default null            comment '失败信息',
  create_time         datetime        not null                comment '创建时间',
  primary key (audit_id),
  key idx_security_audit_target (target_user_id, create_time),
  key idx_security_audit_actor (actor_user_id, create_time),
  key idx_security_audit_request (request_id)
) engine=innodb comment='不可由业务接口删除的安全审计';


-- ----------------------------
-- 21、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '编号',
  table_name        varchar(200)    default ''                 comment '表名称',
  table_comment     varchar(500)    default ''                 comment '表描述',
  sub_table_name    varchar(64)     default null               comment '关联子表的表名',
  sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
  class_name        varchar(100)    default ''                 comment '实体类名称',
  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
  tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
  package_name      varchar(100)                               comment '生成包路径',
  module_name       varchar(30)                                comment '生成模块名',
  business_name     varchar(30)                                comment '生成业务名',
  function_name     varchar(50)                                comment '生成功能名',
  function_author   varchar(50)                                comment '生成功能作者',
  form_col_num      int(1)          default 1                  comment '表单布局（单列 双列 三列）',
  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
  options           varchar(1000)                              comment '其它生成选项',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 20、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '编号',
  table_id          bigint(20)                                 comment '归属表编号',
  column_name       varchar(200)                               comment '列名称',
  column_comment    varchar(500)                               comment '列描述',
  column_type       varchar(100)                               comment '列类型',
  java_type         varchar(500)                               comment 'JAVA类型',
  java_field        varchar(200)                               comment 'JAVA字段名',
  is_pk             char(1)                                    comment '是否主键（1是）',
  is_increment      char(1)                                    comment '是否自增（1是）',
  is_required       char(1)                                    comment '是否必填（1是）',
  is_insert         char(1)                                    comment '是否为插入字段（1是）',
  is_edit           char(1)                                    comment '是否编辑字段（1是）',
  is_list           char(1)                                    comment '是否列表字段（1是）',
  is_query          char(1)                                    comment '是否查询字段（1是）',
  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  dict_type         varchar(200)    default ''                 comment '字典类型',
  sort              int                                        comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ----------------------------
-- 机动车检测预约 V6.0：vi_* 业务表（66 张）
-- 与 sql/migration_20261021_vehicle_initial.sql 同源；新环境一次初始化到最新结构。
-- 仅 CREATE vi_*；不含 sys_ 改动。目标 MySQL 8.0.22+（CHECK/表达式默认）。
-- ----------------------------
-- 机动车检测预约 V6.0：从零初始化 vi_* 业务表，不是V4升级脚本。
-- 不创建/修改/删除任何sys_*表，不插入员工、真实价格或商户。
-- 仅在明确选定的空业务库/已备份的测试框架库执行；已有vi_*表会直接报错，禁止IF NOT EXISTS掩盖结构差异。
-- 目标MySQL 8.0.22+（CHECK及表达式DEFAULT）；正式采用版本须在目标环境执行验收。
-- DATETIME使用UTC分量；DEFAULT(UTC_TIMESTAMP(3))不依赖连接时区；UPDATE由Mapper显式写UTC_TIMESTAMP(3)。
-- DDL在MySQL中会隐式提交，不宣称全脚本事务回滚。请先跑环境预检。
SET NAMES utf8mb4;

-- 微信统一基础账户 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '微信应用标识',
  `open_id` VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '同一小程序身份',
  `union_id` VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '仅供应商可信返回时记录',
  `phone_cipher` VARBINARY(512) COMMENT '手机号密文',
  `phone_lookup` BINARY(32) COMMENT '带密钥摘要，非裸哈希',
  `nickname` VARCHAR(100) COMMENT '选填昵称',
  `status` VARCHAR(24) NOT NULL COMMENT 'AccountStatus: ACTIVE,LOCKED',
  `privacy_version` VARCHAR(64) COMMENT '最近同意版本',
  `avatar_file_id` BIGINT NULL COMMENT '本人已验证头像引用，不直接存URL',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_app_open (app_id,open_id),
  KEY ix_phone_lookup (phone_lookup),
  CONSTRAINT ck_66_592d8ece9cae CHECK (`status` IN ('ACTIVE','LOCKED')),
  CONSTRAINT ck_ver_account CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信统一基础账户';

-- 微信账户与框架员工受控绑定 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '关联vi_account',
  `sys_user_id` BIGINT NOT NULL COMMENT '引用现有sys_user，不重建员工',
  `bound_by` BIGINT NOT NULL COMMENT '受控绑定审批人',
  `bound_at` DATETIME(3) NOT NULL COMMENT '绑定时间',
  `revoked_at` DATETIME(3) COMMENT '解除时间',
  `active_account` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, account_id, NULL)) STORED COMMENT '生效账户唯一',
  `active_user` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, sys_user_id, NULL)) STORED COMMENT '生效员工唯一',
  `permission_version` BIGINT NOT NULL DEFAULT 0 COMMENT '员工关联权限版本；撤权事务更新',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_account (active_account),
  UNIQUE KEY uk_active_user (active_user),
  CONSTRAINT ck_ver_employee_binding CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信账户与框架员工受控绑定';

-- 代驾资格与听单偏好 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '同一个基础账户',
  `qualification_status` VARCHAR(24) NOT NULL COMMENT 'QualificationStatus: DRAFT,SUBMITTED,APPROVED,REJECTED,EXPIRED,SUSPENDED',
  `new_assignment_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许新增承接',
  `listening_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '本人听单偏好',
  `review_due_at` DATETIME(3) COMMENT '年度复审到期',
  `license_class` VARCHAR(64) COMMENT '实际准驾类别，不用字母排序比较',
  `license_valid_to` DATETIME(3) COMMENT '到期日',
  `license_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT '长期有效标记',
  `qualification_revision` BIGINT COMMENT '生效认证版本',
  `internal_remark` VARCHAR(1000) COMMENT '授权运营备注，变更留痕',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_account (account_id),
  KEY ix_pool (qualification_status,new_assignment_enabled,listening_enabled),
  CONSTRAINT ck_66_c6bf03f0ceed CHECK (`qualification_status` IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','EXPIRED','SUSPENDED')),
  CONSTRAINT ck_ver_driver_profile CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾资格与听单偏好';

-- 代驾资格申请与复审版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '关联代驾档案',
  `status` VARCHAR(24) NOT NULL COMMENT 'QualificationStatus: DRAFT,SUBMITTED,APPROVED,REJECTED,EXPIRED,SUSPENDED',
  `sensitive_payload_cipher` LONGBLOB COMMENT '证件信息加密，不存明文JSON',
  `evidence_file_ids` JSON COMMENT '证件文件关联ID',
  `provider_result_ids` JSON COMMENT '可信核验和培训记录ID',
  `submitted_at` DATETIME(3) COMMENT '完整提交时间',
  `reviewer_user_id` BIGINT COMMENT '审核人',
  `reviewed_at` DATETIME(3) COMMENT '审核时间',
  `review_opinion` VARCHAR(1000) COMMENT '审批意见',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_driver_status (driver_id,status),
  KEY ix_review_queue (status,submitted_at),
  CONSTRAINT ck_66_ef6019b02577 CHECK (`status` IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','EXPIRED','SUSPENDED')),
  CONSTRAINT ck_ver_driver_application CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾资格申请与复审版本';

-- 第三方核验与培训任务记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_provider_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `application_id` BIGINT NOT NULL COMMENT '代驾申请',
  `provider` VARCHAR(64) COMMENT '供应商或培训服务',
  `task_type` VARCHAR(64) COMMENT 'VerificationType: REALNAME,FACE,BACKGROUND,LICENSE,TRAINING',
  `provider_task_id` VARCHAR(128) COMMENT '外部任务标识',
  `status` VARCHAR(64) NOT NULL COMMENT 'VerificationStatus: CREATED,PROCESSING,PASSED,FAILED,EXPIRED,UNAVAILABLE',
  `result_digest` VARCHAR(64) COMMENT '结果摘要，不泄露生物信息',
  `verified_at` DATETIME(3) COMMENT '可信回调或查单确认时刻',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_provider_task (provider,provider_task_id),
  KEY ix_app_type (application_id,task_type),
  CONSTRAINT ck_66_2612e28e561f CHECK (`task_type` IN ('REALNAME','FACE','BACKGROUND','LICENSE','TRAINING')),
  CONSTRAINT ck_66_fd8e93f22cc6 CHECK (`status` IN ('CREATED','PROCESSING','PASSED','FAILED','EXPIRED','UNAVAILABLE')),
  CONSTRAINT ck_ver_provider_verification CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方核验与培训任务记录';

-- 检测点与可服务设置 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_station` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `name` VARCHAR(128) NOT NULL COMMENT '名称',
  `dept_id` BIGINT NOT NULL COMMENT '框架部门归属',
  `address` VARCHAR(255) NOT NULL COMMENT '检测点公开地址',
  `lng` DECIMAL(10,6) NOT NULL COMMENT 'GCJ02经度',
  `lat` DECIMAL(10,6) NOT NULL COMMENT 'GCJ02纬度',
  `service_radius_meter` INT NOT NULL COMMENT '服务半径',
  `supported_vehicle_codes` JSON NOT NULL COMMENT '引用现有vehicle_type数据字典',
  `business_hours` JSON NOT NULL COMMENT '营业日和接收边界',
  `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '配置完成后启用',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_dept_enabled (dept_id,enabled),
  CONSTRAINT ck_ver_station CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检测点与可服务设置';

-- 员工跨检测点操作范围 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_station_grant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `sys_user_id` BIGINT NOT NULL COMMENT '复用框架人员',
  `station_id` BIGINT NOT NULL COMMENT '获授权站点',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '功能权限仍单独验证',
  `granted_by` BIGINT NOT NULL COMMENT '授权人',
  `reason` VARCHAR(500) NOT NULL COMMENT '授权变更说明',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_user_station (sys_user_id,station_id),
  CONSTRAINT ck_ver_station_grant CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工跨检测点操作范围';

-- 授权客户-车辆关联档案，供独立车辆列表 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_vehicle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '基础账户',
  `plate_no` VARCHAR(20) NOT NULL COMMENT '归一化车牌',
  `vehicle_type_code` VARCHAR(64) NOT NULL COMMENT '数据字典编码',
  `brand_model` VARCHAR(128) COMMENT '可选',
  `vin_last4` VARCHAR(4) COMMENT '只读核验摘要',
  `registered_on` DATE COMMENT '登记日期，按需核验',
  `use_type` VARCHAR(64) COMMENT '使用性质',
  `verification_status` VARCHAR(24) NOT NULL DEFAULT 'PENDING' COMMENT 'VehicleVerificationStatus: PENDING,VERIFIED,REJECTED',
  `internal_remark` VARCHAR(1000) COMMENT '运营资料备注，不回写历史订单',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '服务层保证本人至多一辆默认',
  `deleted_at` DATETIME(3) COMMENT '软删除',
  `active_plate` VARCHAR(20) GENERATED ALWAYS AS (IF(deleted_at IS NULL,plate_no,NULL)) STORED COMMENT '软删除后不占用本人车牌约束',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_owner_plate (owner_account_id,active_plate),
  CONSTRAINT ck_66_c3de1069a647 CHECK (`verification_status` IN ('PENDING','VERIFIED','REJECTED')),
  CONSTRAINT ck_ver_vehicle CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='授权客户-车辆关联档案，供独立车辆列表';

-- 客户常用地址 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '所属账户',
  `label` VARCHAR(40) COMMENT '家/公司等',
  `address_cipher` VARBINARY(2048) COMMENT '精确地址加密',
  `location_cipher` VARBINARY(1024) COMMENT '坐标敏感数据',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '常用',
  `deleted_at` DATETIME(3) COMMENT '软删除',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_owner (owner_account_id,deleted_at),
  CONSTRAINT ck_ver_address CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户常用地址';

-- 不可变生效计价版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_price_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `rule_key` VARCHAR(64) NOT NULL COMMENT '稳定方案标识',
  `revision` INT NOT NULL COMMENT '新版本号',
  `vehicle_type_code` VARCHAR(64) NOT NULL COMMENT '车型字典编码',
  `inspection_type_code` VARCHAR(64) NOT NULL COMMENT '检测业务类型',
  `status` VARCHAR(64) NOT NULL COMMENT 'PriceRuleStatus: DRAFT,IN_REVIEW,APPROVED,PUBLISHED,DISABLED',
  `config_json` JSON NOT NULL COMMENT '按PriceInput约束的计价参数，分与米',
  `effective_from` DATETIME(3) COMMENT '生效时间',
  `effective_to` DATETIME(3) COMMENT '失效时间',
  `submitted_by` BIGINT COMMENT '提交人',
  `approved_by` BIGINT COMMENT '审核人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_rule_revision (rule_key,revision),
  KEY ix_lookup (vehicle_type_code,inspection_type_code,status,effective_from),
  CONSTRAINT ck_66_9d65819638e0 CHECK (`status` IN ('DRAFT','IN_REVIEW','APPROVED','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_price_rule CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变生效计价版本';

-- 上门服务与站点接收额度 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quota_slot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `quota_type` VARCHAR(24) NOT NULL COMMENT 'QuotaType: PICKUP,STATION_RECEIVE',
  `scope_key` VARCHAR(64) NOT NULL COMMENT '区域标识或站点标识',
  `station_id` BIGINT COMMENT '接收额度所属站点',
  `starts_at` DATETIME(3) NOT NULL COMMENT '时间区间起',
  `ends_at` DATETIME(3) NOT NULL COMMENT '时间区间止',
  `total_quota` INT NOT NULL COMMENT '可提供数量',
  `held_quota` INT NOT NULL DEFAULT 0 COMMENT '未付预占数量',
  `confirmed_quota` INT NOT NULL DEFAULT 0 COMMENT '已付预约尚未消耗',
  `consumed_quota` INT NOT NULL DEFAULT 0 COMMENT '已经真实消耗',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '停约开关',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_scope_slot (quota_type,scope_key,starts_at,ends_at),
  CONSTRAINT ck_quota_nonnegative CHECK (total_quota>=0 AND held_quota>=0 AND confirmed_quota>=0 AND consumed_quota>=0),
  CONSTRAINT ck_quota_limit CHECK (held_quota+confirmed_quota+consumed_quota<=total_quota),
  CONSTRAINT ck_66_6d097fde07cc CHECK (`quota_type` IN ('PICKUP','STATION_RECEIVE')),
  CONSTRAINT ck_ver_quota_slot CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上门服务与站点接收额度';

-- 短期报价快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quote` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '报价人',
  `station_id` BIGINT NOT NULL COMMENT '匹配站点',
  `vehicle_snapshot` JSON NOT NULL COMMENT '车牌、车型编码与名称快照',
  `address_snapshot_cipher` LONGBLOB NOT NULL COMMENT '加密接车地址快照',
  `pickup_slot_id` BIGINT NOT NULL COMMENT '上门时段',
  `receive_slot_id` BIGINT COMMENT '内部接收排期',
  `rule_id` BIGINT NOT NULL COMMENT '计价版本ID',
  `route_snapshot` JSON NOT NULL COMMENT '供应商路线距离、方向、时效',
  `amount_fen` BIGINT NOT NULL COMMENT '总费用分',
  `items_json` JSON NOT NULL COMMENT '逐项明细及计算依据',
  `expires_at` DATETIME(3) NOT NULL COMMENT '报价失效时间',
  `used_order_id` BIGINT COMMENT '已用于建单的订单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_owner_expire (owner_account_id,expires_at),
  UNIQUE KEY uk_used_order (used_order_id),
  CONSTRAINT ck_quote_amount CHECK (amount_fen>=0),
  CONSTRAINT ck_ver_quote CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短期报价快照';

-- 订单主履约与实际保管 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_no` VARCHAR(64) NOT NULL COMMENT '对外业务单号',
  `owner_account_id` BIGINT NOT NULL COMMENT '下单账户',
  `vehicle_id` BIGINT COMMENT '常用车辆档案',
  `station_id` BIGINT NOT NULL COMMENT '业务权威检测点归属',
  `dept_id` BIGINT NOT NULL COMMENT '框架数据归属快照',
  `quote_id` BIGINT NOT NULL COMMENT '报价及价格快照',
  `vehicle_snapshot` JSON NOT NULL COMMENT '历史车辆快照',
  `fulfillment_status` VARCHAR(32) NOT NULL COMMENT 'FulfillmentStatus: PENDING_PAY,SCHEDULED,PENDING_DISPATCH,DISPATCHING,MANUAL_DISPATCH,ACCEPTED,GOING_PICKUP,ARRIVED_PICKUP,PICKED_UP,ARRIVED_STATION,INSPECTING,INSPECTED,CAR_TAKEN,WAIT_CONFIRM,COMPLETED,CANCELLED,CLOSED',
  `cancel_status` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'CancelStatus: NONE,APPLIED,REJECTED,APPROVED_WAIT_RETURN,COMPLETED',
  `custody_type` VARCHAR(16) NOT NULL COMMENT 'CustodyType: OWNER,DRIVER,STATION',
  `custody_ref_id` BIGINT NOT NULL COMMENT '实际保管方ID，须与类型联查',
  `active_assignment_id` BIGINT COMMENT '当前承接指针，已释放时为空',
  `pickup_starts_at` DATETIME(3) NOT NULL COMMENT '对外约定上门时间',
  `pickup_ends_at` DATETIME(3) NOT NULL COMMENT '对外时段结束',
  `pay_expires_at` DATETIME(3) NOT NULL COMMENT '创建后30分钟',
  `dispatch_at` DATETIME(3) COMMENT '预约前N分钟',
  `dispatch_config_version` BIGINT COMMENT '派单参数快照',
  `returned_at` DATETIME(3) COMMENT '实际归还成功时间',
  `auto_confirm_at` DATETIME(3) COMMENT '归还后24小时服务确认截止',
  `completed_at` DATETIME(3) COMMENT '业务服务确认完成',
  `cancelled_at` DATETIME(3) COMMENT '取消正式办结',
  `total_fen` BIGINT NOT NULL COMMENT '应付分',
  `terms_version` VARCHAR(64) NOT NULL COMMENT '服务协议版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_quote (quote_id),
  KEY ix_owner_list (owner_account_id,created_at,id),
  KEY ix_station_queue (station_id,fulfillment_status,created_at,id),
  KEY ix_dispatch_due (fulfillment_status,dispatch_at,id),
  KEY ix_auto_confirm (fulfillment_status,auto_confirm_at,id),
  CONSTRAINT ck_order_total CHECK (total_fen>=0),
  CONSTRAINT ck_66_0ff90a4c30f3 CHECK (`fulfillment_status` IN ('PENDING_PAY','SCHEDULED','PENDING_DISPATCH','DISPATCHING','MANUAL_DISPATCH','ACCEPTED','GOING_PICKUP','ARRIVED_PICKUP','PICKED_UP','ARRIVED_STATION','INSPECTING','INSPECTED','CAR_TAKEN','WAIT_CONFIRM','COMPLETED','CANCELLED','CLOSED')),
  CONSTRAINT ck_66_0237ff511e2d CHECK (`cancel_status` IN ('NONE','APPLIED','REJECTED','APPROVED_WAIT_RETURN','COMPLETED')),
  CONSTRAINT ck_66_daacf35a5049 CHECK (`custody_type` IN ('OWNER','DRIVER','STATION')),
  CONSTRAINT ck_ver_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主履约与实际保管';

-- 每单额度占用凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quota_reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '关联订单',
  `slot_id` BIGINT NOT NULL COMMENT '关联quota_slot',
  `status` VARCHAR(24) NOT NULL COMMENT 'ReservationStatus: HELD,CONFIRMED,CONSUMED,RELEASED',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `expires_at` DATETIME(3) COMMENT '未付锁额截止',
  `released_at` DATETIME(3) COMMENT '释放时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_slot (order_id,slot_id),
  KEY ix_due (status,expires_at),
  CONSTRAINT ck_66_44d120d4cf5a CHECK (`status` IN ('HELD','CONFIRMED','CONSUMED','RELEASED')),
  CONSTRAINT ck_ver_quota_reservation CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每单额度占用凭证';

-- 单人派单邀约 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_dispatch_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `driver_id` BIGINT NOT NULL COMMENT '候选代驾',
  `status` VARCHAR(24) NOT NULL COMMENT 'InviteStatus: WAIT_RESPONSE,ACCEPTED,REJECTED,EXPIRED,REVOKED',
  `source` VARCHAR(16) NOT NULL COMMENT 'AUTO/MANUAL',
  `round_key` VARCHAR(64) COMMENT '派单轮次',
  `issued_at` DATETIME(3) NOT NULL COMMENT '邀约时刻',
  `expires_at` DATETIME(3) NOT NULL COMMENT '绝对失效时间',
  `responded_at` DATETIME(3) COMMENT '响应',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(status='WAIT_RESPONSE',order_id,NULL)) STORED COMMENT '同单最多一个待响应邀约',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_order (active_order),
  KEY ix_driver_status (driver_id,status),
  KEY ix_expire (status,expires_at),
  CONSTRAINT ck_66_22cd3ab6b830 CHECK (`status` IN ('WAIT_RESPONSE','ACCEPTED','REJECTED','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_dispatch_invite CHECK (version >= 0),
  CONSTRAINT ck_dispatch_invite_source CHECK (`source` IN ('AUTO','MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单人派单邀约';

-- 司机承接历史，结束后仍保留 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `invite_id` BIGINT NOT NULL COMMENT '接受的邀约',
  `accepted_at` DATETIME(3) NOT NULL COMMENT '接受时间',
  `ended_at` DATETIME(3) COMMENT '解除或归还时结束',
  `end_reason` VARCHAR(64) COMMENT 'AssignmentEndReason: RETURNED,CANCELLED,DRIVER_CANCEL',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(ended_at IS NULL,order_id,NULL)) STORED COMMENT '生效承接唯一订单',
  `active_driver` BIGINT GENERATED ALWAYS AS (IF(ended_at IS NULL,driver_id,NULL)) STORED COMMENT '生效承接唯一司机',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_invite (invite_id),
  UNIQUE KEY uk_active_order (active_order),
  UNIQUE KEY uk_active_driver (active_driver),
  KEY ix_history (driver_id,accepted_at),
  CONSTRAINT ck_66_8c02041ed220 CHECK (`end_reason` IN ('RETURNED','CANCELLED','DRIVER_CANCEL')),
  CONSTRAINT ck_ver_driver_assignment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='司机承接历史，结束后仍保留';

-- 司机唯一资源占用，不受订单待确认影响 | BUSINESS_KEY
CREATE TABLE `vi_driver_occupancy` (
  `driver_id` BIGINT NOT NULL COMMENT '主键，唯一占用',
  `holder_type` VARCHAR(16) NOT NULL COMMENT 'OccupancyHolderType: INVITE,ASSIGNMENT',
  `holder_id` BIGINT NOT NULL COMMENT '被占用邀约或承接ID',
  `order_id` BIGINT NOT NULL COMMENT '对应订单',
  `expires_at` DATETIME(3) COMMENT '仅邀约有自动失效',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`driver_id`),
  UNIQUE KEY uk_holder (holder_type,holder_id),
  CONSTRAINT ck_66_0d4b483893ce CHECK (`holder_type` IN ('INVITE','ASSIGNMENT')),
  CONSTRAINT ck_ver_driver_occupancy CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='司机唯一资源占用，不受订单待确认影响';

-- 全局轮转游标与版本 | BUSINESS_KEY
CREATE TABLE `vi_dispatch_cursor` (
  `scope_key` VARCHAR(64) NOT NULL COMMENT '全局或已确认调度域主键',
  `last_driver_id` BIGINT COMMENT '上次派发指针',
  `round_no` BIGINT NOT NULL DEFAULT 0 COMMENT '轮次定义须P01评审',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`scope_key`),
  CONSTRAINT ck_ver_dispatch_cursor CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局轮转游标与版本';

-- 代驾最近前台定位 | BUSINESS_KEY
CREATE TABLE `vi_driver_location` (
  `driver_id` BIGINT NOT NULL COMMENT '主键',
  `location_cipher` VARBINARY(1024) NOT NULL COMMENT '加密位置',
  `captured_at` DATETIME(3) NOT NULL COMMENT '设备采集时间',
  `received_at` DATETIME(3) NOT NULL COMMENT '服务器接收时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`driver_id`),
  CONSTRAINT ck_ver_driver_location CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾最近前台定位';

-- 一次实物交接事务凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_handover` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `assignment_id` BIGINT NOT NULL COMMENT '当前承接',
  `handover_type` VARCHAR(32) NOT NULL COMMENT 'HandoverType: OWNER_TO_DRIVER,DRIVER_TO_STATION,STATION_TO_DRIVER,DRIVER_TO_OWNER',
  `holder_account_id` BIGINT NOT NULL COMMENT '持码基础账号',
  `verifier_account_id` BIGINT NOT NULL COMMENT '核验基础账号',
  `code_mac` BINARY(32) NOT NULL COMMENT '含订单/类型/代次随机盐的服务端带密钥MAC；4位码不能只裸哈希',
  `code_cipher` VARBINARY(512) NOT NULL COMMENT '仅持码方短期读取所需的加密值，使用后销毁可读密文',
  `generation_no` INT NOT NULL COMMENT '码代次',
  `status` VARCHAR(24) NOT NULL COMMENT 'HandoverStatus: PENDING,CONFIRMED,LOCKED,EXPIRED',
  `expires_at` DATETIME(3) NOT NULL COMMENT '时效',
  `confirmed_at` DATETIME(3) COMMENT '实物交接成功',
  `evidence_group_id` BIGINT COMMENT '已经封存证据组',
  `active_key` VARCHAR(160) GENERATED ALWAYS AS (IF(status='PENDING',CONCAT(order_id,':',assignment_id,':',handover_type),NULL)) STORED COMMENT '同时仅一个有效码',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_handover (active_key),
  KEY ix_order_type (order_id,handover_type,confirmed_at),
  CONSTRAINT ck_66_73b760054eb8 CHECK (`handover_type` IN ('OWNER_TO_DRIVER','DRIVER_TO_STATION','STATION_TO_DRIVER','DRIVER_TO_OWNER')),
  CONSTRAINT ck_66_87e8157e4010 CHECK (`status` IN ('PENDING','CONFIRMED','LOCKED','EXPIRED')),
  CONSTRAINT ck_ver_handover CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一次实物交接事务凭证';

-- 跨重发的交接错误次数 | BUSINESS_KEY
CREATE TABLE `vi_handover_attempt_guard` (
  `guard_key` VARCHAR(160) NOT NULL COMMENT '订单+承接+交接类型',
  `failed_attempts` INT NOT NULL DEFAULT 0 COMMENT '累计错误次数',
  `locked_at` DATETIME(3) COMMENT '错3次锁定',
  `unlocked_by` BIGINT COMMENT '人工复核授权人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`guard_key`),
  CONSTRAINT ck_ver_handover_attempt_guard CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨重发的交接错误次数';

-- 照片节点分组 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_evidence_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `assignment_id` BIGINT NOT NULL COMMENT '承接',
  `node_no` TINYINT NOT NULL COMMENT '1接车2到站3取车4归还',
  `status` VARCHAR(16) NOT NULL COMMENT 'EvidenceStatus: DRAFT,READY,SEALED',
  `mileage_km` DECIMAL(7,1) COMMENT '仪表公里数',
  `mileage_source` VARCHAR(16) COMMENT 'MANUAL/OCR_REVIEWED',
  `mileage_reason` VARCHAR(1000) COMMENT '异常说明',
  `sealed_at` DATETIME(3) COMMENT '交接时封存',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_assignment_node (assignment_id,node_no),
  CONSTRAINT ck_node CHECK (node_no BETWEEN 1 AND 4),
  CONSTRAINT ck_mileage CHECK (mileage_km IS NULL OR mileage_km>=0),
  CONSTRAINT ck_66_6c92d2f8851d CHECK (`status` IN ('DRAFT','READY','SEALED')),
  CONSTRAINT ck_ver_evidence_group CHECK (version >= 0),
  CONSTRAINT ck_evidence_group_mileage_source CHECK (`mileage_source` IS NULL OR `mileage_source` IN ('MANUAL','OCR_REVIEWED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='照片节点分组';

-- 照片版本与服务端元数据 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_photo` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `group_id` BIGINT NOT NULL COMMENT '证据组',
  `photo_type` VARCHAR(24) NOT NULL COMMENT 'PhotoType: FRONT,BACK,LEFT,RIGHT,DASHBOARD,INTERIOR,EXTRA',
  `file_id` BIGINT NOT NULL COMMENT '私有文件',
  `client_capture_id` VARCHAR(64) NOT NULL COMMENT '同次重传去重',
  `revision` INT NOT NULL COMMENT '版本',
  `superseded_at` DATETIME(3) COMMENT '草稿重拍历史',
  `captured_at` DATETIME(3) NOT NULL COMMENT '客户端采集，仅作声明',
  `received_at` DATETIME(3) NOT NULL COMMENT '服务端收件时间',
  `location_cipher` VARBINARY(1024) COMMENT '节点位置',
  `watermark_file_id` BIGINT COMMENT '服务端生成水印副本',
  `active_type` VARCHAR(24) GENERATED ALWAYS AS (IF(superseded_at IS NULL AND photo_type<>'EXTRA',photo_type,NULL)) STORED COMMENT '每类有效图唯一，EXTRA不覆盖',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_capture (group_id,client_capture_id),
  UNIQUE KEY uk_current_type (group_id,active_type),
  CONSTRAINT ck_66_37b55c2a7354 CHECK (`photo_type` IN ('FRONT','BACK','LEFT','RIGHT','DASHBOARD','INTERIOR','EXTRA')),
  CONSTRAINT ck_ver_photo CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='照片版本与服务端元数据';

-- 私有文件登记与校验 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `uploader_account_id` BIGINT NOT NULL COMMENT '上传者',
  `purpose` VARCHAR(32) NOT NULL COMMENT '限定用途',
  `order_id` BIGINT COMMENT '订单归属',
  `application_id` BIGINT COMMENT '认证等申请归属',
  `object_key` VARCHAR(255) NOT NULL COMMENT '服务端生成不可猜键',
  `status` VARCHAR(24) NOT NULL COMMENT 'FileStatus: UPLOADING,VERIFYING,READY,REJECTED',
  `mime_type` VARCHAR(64) COMMENT '内容识别类型',
  `server_size` BIGINT COMMENT '实际大小',
  `server_sha256` BINARY(32) COMMENT '服务器复核摘要',
  `retain_until` DATETIME(3) COMMENT '按已审批保留政策',
  `legal_hold` TINYINT NOT NULL DEFAULT 0 COMMENT '争议等必要留存',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_object_key (object_key),
  KEY ix_order_purpose (order_id,purpose),
  CONSTRAINT ck_66_c1389ba0baeb CHECK (`status` IN ('UPLOADING','VERIFYING','READY','REJECTED')),
  CONSTRAINT ck_ver_file CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私有文件登记与校验';

-- 行驶证登记与人工复核 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_vehicle_license` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `sensitive_payload_cipher` LONGBLOB NOT NULL COMMENT '车主/VIN等密文',
  `main_file_id` BIGINT COMMENT '主页',
  `sub_file_id` BIGINT COMMENT '副页',
  `ocr_task_id` BIGINT COMMENT '服务端OCR任务',
  `review_status` VARCHAR(24) NOT NULL COMMENT 'LicenseReviewStatus: MISSING,PENDING,VERIFIED',
  `reviewer_user_id` BIGINT COMMENT '框架核验人',
  `reviewed_at` DATETIME(3) COMMENT '复核时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_66_1355e77a455a CHECK (`review_status` IN ('MISSING','PENDING','VERIFIED')),
  CONSTRAINT ck_ver_vehicle_license CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行驶证登记与人工复核';

-- 追加式结构化结果版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_inspection_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `revision` INT NOT NULL COMMENT '结果版本',
  `conclusion` VARCHAR(8) NOT NULL COMMENT 'PASS/FAIL',
  `items_json` JSON NOT NULL COMMENT '最终项目与凭证；没有实时阶段',
  `inspector_user_id` BIGINT NOT NULL COMMENT '真实检测人，验证可操作站点',
  `submitted_by` BIGINT NOT NULL COMMENT '记录人',
  `source` VARCHAR(16) NOT NULL COMMENT 'STATION/ADMIN服务端写入',
  `inspected_at` DATETIME(3) NOT NULL COMMENT '实际检测完成时间',
  `submitted_at` DATETIME(3) NOT NULL COMMENT '登记时间',
  `superseded_at` DATETIME(3) COMMENT '被更正替换',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(superseded_at IS NULL,order_id,NULL)) STORED COMMENT '当前生效结果唯一',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_revision (order_id,revision),
  UNIQUE KEY uk_current_order (active_order),
  CONSTRAINT ck_ver_inspection_result CHECK (version >= 0),
  CONSTRAINT ck_inspection_result_conclusion CHECK (`conclusion` IN ('PASS','FAIL')),
  CONSTRAINT ck_inspection_result_source CHECK (`source` IN ('STATION','ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='追加式结构化结果版本';

-- 检测结果更正审批 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_result_correction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `base_result_id` BIGINT NOT NULL COMMENT '原版本',
  `replacement_json` JSON NOT NULL COMMENT '拟更正内容',
  `status` VARCHAR(24) NOT NULL COMMENT 'CorrectionStatus: APPLIED,APPROVED,REJECTED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '原因',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人',
  `reviewer_id` BIGINT COMMENT '审核人',
  `opinion` VARCHAR(1000) COMMENT '意见',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_queue (status,created_at),
  CONSTRAINT ck_66_875cb83b2599 CHECK (`status` IN ('APPLIED','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_result_correction CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检测结果更正审批';

-- 支付交易与可退资金 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `out_trade_no` VARCHAR(64) NOT NULL COMMENT '稳定微信商户单号',
  `transaction_id` VARCHAR(64) COMMENT '渠道交易号',
  `status` VARCHAR(24) NOT NULL COMMENT 'PaymentStatus: PENDING,SUCCESS,CLOSED,UNKNOWN',
  `total_fen` BIGINT NOT NULL COMMENT '应付',
  `paid_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '实付',
  `refund_reserved_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '处理中退款金额预留',
  `refunded_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '已成功退款',
  `paid_at` DATETIME(3) COMMENT '支付成功',
  `expires_at` DATETIME(3) NOT NULL COMMENT '对应30分钟时点',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_trade (out_trade_no),
  UNIQUE KEY uk_transaction (transaction_id),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_refund_balance CHECK (paid_fen>=0 AND refund_reserved_fen>=0 AND refunded_fen>=0 AND refund_reserved_fen+refunded_fen<=paid_fen),
  CONSTRAINT ck_66_a1f02dcffc3b CHECK (`status` IN ('PENDING','SUCCESS','CLOSED','UNKNOWN')),
  CONSTRAINT ck_ver_payment_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付交易与可退资金';

-- 稳定退款单及渠道执行 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_refund_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_id` BIGINT NOT NULL COMMENT '关联唯一统一售后申请',
  `review_revision` INT NOT NULL COMMENT '批准决定版本，金额变更必须新版本审核',
  `payment_id` BIGINT NOT NULL COMMENT '支付单',
  `out_refund_no` VARCHAR(64) NOT NULL COMMENT '幂等商户退款号',
  `channel_refund_id` VARCHAR(64) COMMENT '渠道退款ID',
  `amount_fen` BIGINT NOT NULL COMMENT '金额分',
  `status` VARCHAR(24) NOT NULL COMMENT 'ChannelRefundStatus: CREATED,PROCESSING,SUCCESS,CLOSED,ABNORMAL,UNKNOWN',
  `success_at` DATETIME(3) COMMENT '只有SUCCESS存在',
  `last_query_at` DATETIME(3) COMMENT '查单',
  `next_query_at` DATETIME(3) COMMENT '退避后查单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_review (case_id,review_revision),
  UNIQUE KEY uk_refund_no (out_refund_no),
  KEY ix_query (status,next_query_at),
  CONSTRAINT ck_refund_amount CHECK (amount_fen>0),
  CONSTRAINT ck_66_8132f268f5fd CHECK (`status` IN ('CREATED','PROCESSING','SUCCESS','CLOSED','ABNORMAL','UNKNOWN')),
  CONSTRAINT ck_ver_refund_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='稳定退款单及渠道执行';

-- 永久渠道回调去重 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_channel_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `provider` VARCHAR(64) NOT NULL COMMENT '微信支付等',
  `event_key` VARCHAR(128) NOT NULL COMMENT '通知ID/交易业务唯一标识',
  `trade_no` VARCHAR(64) COMMENT '关联支付或退款号',
  `event_type` VARCHAR(64) COMMENT '事件类型',
  `verified_digest` BINARY(32) COMMENT '已验签原报文摘要',
  `processed_at` DATETIME(3) COMMENT '已持久化时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_event (provider,event_key),
  KEY ix_trade (trade_no),
  CONSTRAINT ck_ver_channel_event CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='永久渠道回调去重';

-- 普通服务争议 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_dispute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `type` VARCHAR(64) NOT NULL COMMENT '普通服务类型，不是事故',
  `status` VARCHAR(24) NOT NULL COMMENT 'ServiceCaseStatus: OPEN,PROCESSING,RESOLVED',
  `description` VARCHAR(2000) NOT NULL COMMENT '用户说明',
  `file_ids` JSON COMMENT '凭证',
  `handler_id` BIGINT COMMENT '处理人',
  `conclusion` VARCHAR(2000) COMMENT '结论',
  `resolved_at` DATETIME(3) COMMENT '解决时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_order (order_id,status),
  CONSTRAINT ck_66_c11343410b93 CHECK (`status` IN ('OPEN','PROCESSING','RESOLVED')),
  CONSTRAINT ck_ver_dispute CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通服务争议';

-- 普通履约阻碍与客服待办 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_service_exception` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `type` VARCHAR(64) NOT NULL COMMENT '失联/拒收/资料/上传等',
  `status` VARCHAR(24) NOT NULL COMMENT 'ServiceCaseStatus: OPEN,PROCESSING,RESOLVED',
  `description` VARCHAR(2000) COMMENT '说明',
  `file_ids` JSON COMMENT '可后补证据',
  `handler_id` BIGINT COMMENT '处理人',
  `resolution` VARCHAR(2000) COMMENT '实际处置依据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_queue (status,created_at),
  CONSTRAINT ck_66_0faa9ee9aef5 CHECK (`status` IN ('OPEN','PROCESSING','RESOLVED')),
  CONSTRAINT ck_ver_service_exception CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通履约阻碍与客服待办';

-- 服务评价 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `owner_account_id` BIGINT NOT NULL COMMENT '下单人',
  `score` TINYINT NOT NULL COMMENT '1到5',
  `tags` JSON COMMENT '评价标签',
  `content` VARCHAR(500) COMMENT '评语',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_review_score CHECK (score BETWEEN 1 AND 5),
  CONSTRAINT ck_ver_review CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务评价';

-- 普通取消计次与轮转处罚 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_penalty` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '司机',
  `assignment_id` BIGINT NOT NULL COMMENT '取消承接',
  `business_date` DATE NOT NULL COMMENT '北京时间自然日',
  `counted` TINYINT NOT NULL DEFAULT 1 COMMENT '审核后是否计入',
  `round_key` VARCHAR(64) COMMENT '恢复轮次：P01评审',
  `penalty_type` VARCHAR(24) NOT NULL COMMENT 'PenaltyType: SKIP_ROUND,STOP_TODAY',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'PenaltyStatus: ACTIVE,EXPIRED,REVOKED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '处罚依据，对本人可读',
  `rule_version` VARCHAR(64) NOT NULL COMMENT '实际适用轮转及计次规则版本',
  `effective_until` DATETIME(3) NULL COMMENT '明确时刻；轮转型另用round_key',
  `revoked_at` DATETIME(3) NULL COMMENT '通过申诉撤销时刻',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_assignment (assignment_id),
  KEY ix_day (driver_id,business_date,counted),
  CONSTRAINT ck_66_04646c41d887 CHECK (`penalty_type` IN ('SKIP_ROUND','STOP_TODAY')),
  CONSTRAINT ck_66_cdebbbec3ff7 CHECK (`status` IN ('ACTIVE','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_penalty CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通取消计次与轮转处罚';

-- 不可变业务事实流水 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT COMMENT '可关联订单',
  `aggregate_type` VARCHAR(64) NOT NULL COMMENT '聚合类型',
  `aggregate_id` BIGINT NOT NULL COMMENT '聚合ID',
  `action` VARCHAR(64) NOT NULL COMMENT '事件或动作',
  `actor_account_id` BIGINT COMMENT '小程序账户',
  `actor_user_id` BIGINT COMMENT '框架人员',
  `acting_role` VARCHAR(64) COMMENT '视角',
  `station_id` BIGINT COMMENT '操作上下文',
  `before_summary` JSON COMMENT '脱敏前值',
  `after_summary` JSON COMMENT '脱敏后值',
  `reason` VARCHAR(1000) COMMENT '原因',
  `actor_realm` VARCHAR(24) COMMENT 'MINI/ADMIN/SYSTEM/CALLBACK；不可混淆数值ID',
  `session_id` BIGINT COMMENT '可信小程序会话，可空',
  `context_version` BIGINT COMMENT '业务动作的可信上下文版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_order_time (order_id,created_at,id),
  CONSTRAINT ck_ver_event_log CHECK (version >= 0),
  CONSTRAINT ck_event_log_actor_realm CHECK (`actor_realm` IS NULL OR `actor_realm` IN ('MINI','ADMIN','SYSTEM','CALLBACK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变业务事实流水';

-- 可靠异步事务消息 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_outbox` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `event_key` VARCHAR(128) NOT NULL COMMENT '事件唯一键',
  `aggregate_type` VARCHAR(64) NOT NULL COMMENT '所属对象',
  `aggregate_id` BIGINT NOT NULL COMMENT '对象ID',
  `event_type` VARCHAR(64) NOT NULL COMMENT '通知/支付查单/补偿等',
  `payload` JSON NOT NULL COMMENT '最小必要载荷，不塞敏感原文',
  `status` VARCHAR(24) NOT NULL COMMENT 'OutboxStatus: READY,PROCESSING,DONE,FAILED',
  `attempts` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_attempt_at` DATETIME(3) NOT NULL COMMENT '退避时间',
  `lease_until` DATETIME(3) COMMENT '工作者租约',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_event (event_key),
  KEY ix_due (status,next_attempt_at,id),
  CONSTRAINT ck_66_61a047126522 CHECK (`status` IN ('READY','PROCESSING','DONE','FAILED')),
  CONSTRAINT ck_ver_outbox CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可靠异步事务消息';

-- 站内消息与下发记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '接收人',
  `role` VARCHAR(64) NOT NULL COMMENT '业务视角',
  `order_id` BIGINT COMMENT '订单',
  `scene` VARCHAR(64) NOT NULL COMMENT '场景',
  `outbox_id` BIGINT COMMENT '触发事件',
  `send_status` VARCHAR(64) COMMENT 'NotificationSendStatus: PENDING,ACCEPTED,FAILED,DISCARDED',
  `content` VARCHAR(2000) COMMENT '脱敏消息',
  `read_at` DATETIME(3) COMMENT '站内显式已读',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_inbox (account_id,role,created_at,id),
  CONSTRAINT ck_66_236222a99f9d CHECK (`send_status` IN ('PENDING','ACCEPTED','FAILED','DISCARDED')),
  CONSTRAINT ck_ver_notification CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内消息与下发记录';

-- 请求级幂等 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_idempotency` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `subject_key` VARCHAR(96) NOT NULL COMMENT '统一主体命名空间',
  `action_key` VARCHAR(160) NOT NULL COMMENT 'HTTP动作+资源',
  `request_key` VARCHAR(64) NOT NULL COMMENT 'UUID幂等头',
  `request_mac` BINARY(32) NOT NULL COMMENT '请求摘要',
  `result_json` JSON COMMENT '成功结果或可重放结果',
  `status` VARCHAR(64) NOT NULL COMMENT 'IdempotencyStatus: PROCESSING,DONE',
  `expires_at` DATETIME(3) NOT NULL COMMENT '至少24h，请求级保存时限',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_request (subject_key,action_key,request_key),
  KEY ix_expire (expires_at),
  CONSTRAINT ck_66_0b655b918367 CHECK (`status` IN ('PROCESSING','DONE')),
  CONSTRAINT ck_ver_idempotency CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请求级幂等';

-- 隐私、导出、敏感访问与对账审批台账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_governance_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_type` VARCHAR(64) NOT NULL COMMENT 'GovernanceType: PRIVACY,EXPORT,SENSITIVE_ACCESS,RECONCILE',
  `order_id` BIGINT COMMENT '可关联订单',
  `requester_account_id` BIGINT COMMENT '申请客户',
  `requester_user_id` BIGINT COMMENT '申请后台人员',
  `status` VARCHAR(64) NOT NULL COMMENT 'GovernanceStatus: APPLIED,APPROVED,CHECKING,COMPLETED,EXPIRED,FAILED,OPEN,PROCESSING,READY,REJECTED,RESOLVED,REVOKED',
  `payload_cipher` LONGBLOB COMMENT '必要申请与结果加密',
  `reason` VARCHAR(1000) COMMENT '申请/处置依据',
  `reviewer_id` BIGINT COMMENT '审批人',
  `expires_at` DATETIME(3) COMMENT '授权或下载失效',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_type_status (case_type,status,created_at),
  CONSTRAINT ck_66_4f31235a8dc4 CHECK (`case_type` IN ('PRIVACY','EXPORT','SENSITIVE_ACCESS','RECONCILE')),
  CONSTRAINT ck_66_54fd86e51e4e CHECK (`status` IN ('APPLIED','APPROVED','CHECKING','COMPLETED','EXPIRED','FAILED','OPEN','PROCESSING','READY','REJECTED','RESOLVED','REVOKED')),
  CONSTRAINT ck_ver_governance_case CHECK (version >= 0),
  CONSTRAINT ck_governance_type_status CHECK ((case_type='PRIVACY' AND status IN ('APPLIED','PROCESSING','COMPLETED','REJECTED')) OR (case_type='EXPORT' AND status IN ('APPLIED','APPROVED','REJECTED','PROCESSING','READY','FAILED','EXPIRED')) OR (case_type='SENSITIVE_ACCESS' AND status IN ('APPLIED','APPROVED','REJECTED','EXPIRED','REVOKED')) OR (case_type='RECONCILE' AND status IN ('OPEN','CHECKING','RESOLVED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='隐私、导出、敏感访问与对账审批台账';

-- 车主运营档案扩展，非另一套登录账户 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_owner_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '关联vi_account',
  `service_name` VARCHAR(100) COMMENT '服务称呼',
  `service_flag` VARCHAR(24) NOT NULL DEFAULT 'NORMAL' COMMENT 'OwnerServiceFlag: NORMAL,VERIFY_AGAIN',
  `internal_remark` VARCHAR(1000) COMMENT '内部备注',
  `maintained_by` BIGINT COMMENT '框架维护人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_owner_profile (account_id),
  CONSTRAINT ck_66_49501f04f5eb CHECK (`service_flag` IN ('NORMAL','VERIFY_AGAIN')),
  CONSTRAINT ck_ver_owner_profile CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车主运营档案扩展，非另一套登录账户';

-- 取消与退款的唯一申请与处理记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_aftersale_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_no` VARCHAR(64) NOT NULL COMMENT '对外售后号',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `payment_id` BIGINT NOT NULL COMMENT '支付记录',
  `kind` VARCHAR(24) NOT NULL COMMENT 'CANCEL_REFUND/REFUND_ONLY',
  `case_status` VARCHAR(24) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT 'AfterSaleStatus: PENDING_REVIEW,EXECUTING,CLOSED,REJECTED',
  `review_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'AfterSaleReviewStatus: PENDING,APPROVED,REJECTED',
  `review_revision` INT NOT NULL DEFAULT 1 COMMENT '审核修订号',
  `requested_fen` BIGINT NOT NULL COMMENT '申请金额',
  `approved_fen` BIGINT COMMENT '批准金额，未审为NULL',
  `return_state` VARCHAR(24) NOT NULL COMMENT 'UNDETERMINED/NOT_REQUIRED/WAIT_RETURN/RETURNED',
  `refund_state` VARCHAR(24) NOT NULL COMMENT 'RefundStatus: NONE,NOT_REQUIRED,WAIT_RETURN,READY,PROCESSING,SUCCESS,UNKNOWN,ABNORMAL,CLOSED',
  `applicant_type` VARCHAR(16) NOT NULL COMMENT 'ApplicantType: ACCOUNT,SYS_USER',
  `applicant_id` BIGINT NOT NULL COMMENT '实际申请身份',
  `reason` VARCHAR(1000) NOT NULL COMMENT '原始申请说明',
  `evidence_ids` JSON COMMENT '关联证据文件ID',
  `resolved_at` DATETIME(3) COMMENT '业务及资金均已办结',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(case_status IN ('PENDING_REVIEW','EXECUTING'),order_id,NULL)) STORED COMMENT '一个未办结申请',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_no (case_no),
  UNIQUE KEY uk_active_order (active_order),
  KEY ix_review (review_status,created_at),
  KEY ix_return (return_state,updated_at),
  CONSTRAINT ck_after_amount CHECK (requested_fen>=0 AND (approved_fen IS NULL OR approved_fen>=0)),
  CONSTRAINT ck_66_55e0ea12e908 CHECK (`case_status` IN ('PENDING_REVIEW','EXECUTING','CLOSED','REJECTED')),
  CONSTRAINT ck_66_dc8bfeb94c93 CHECK (`review_status` IN ('PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_66_c205c5c1a697 CHECK (`applicant_type` IN ('ACCOUNT','SYS_USER')),
  CONSTRAINT ck_ver_aftersale_case CHECK (version >= 0),
  CONSTRAINT ck_aftersale_case_kind CHECK (`kind` IN ('CANCEL_REFUND','REFUND_ONLY')),
  CONSTRAINT ck_aftersale_case_return_state CHECK (`return_state` IN ('UNDETERMINED','NOT_REQUIRED','WAIT_RETURN','RETURNED')),
  CONSTRAINT ck_aftersale_case_refund_state CHECK (`refund_state` IN ('NONE','NOT_REQUIRED','WAIT_RETURN','READY','PROCESSING','SUCCESS','UNKNOWN','ABNORMAL','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取消与退款的唯一申请与处理记录';

-- 统一审核的追加式决定凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_aftersale_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_id` BIGINT NOT NULL COMMENT '申请',
  `revision` INT NOT NULL COMMENT '唯一决定版本',
  `decision` VARCHAR(16) NOT NULL COMMENT 'APPROVE/REJECT',
  `approved_fen` BIGINT NOT NULL COMMENT '本决定金额',
  `observed_order_version` BIGINT NOT NULL COMMENT '审批时重新读取订单版本',
  `custody_at_review` VARCHAR(16) NOT NULL COMMENT '实际保管方快照',
  `return_required` TINYINT NOT NULL COMMENT '由服务端产生',
  `reviewer_user_id` BIGINT NOT NULL COMMENT '不能与申请人为同一真实身份',
  `opinion` VARCHAR(1000) NOT NULL COMMENT '审核意见',
  `reviewed_at` DATETIME(3) NOT NULL COMMENT 'UTC',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_revision (case_id,revision),
  CONSTRAINT ck_review_fen CHECK (approved_fen>=0),
  CONSTRAINT ck_ver_aftersale_review CHECK (version >= 0),
  CONSTRAINT ck_aftersale_review_decision CHECK (`decision` IN ('APPROVE','REJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一审核的追加式决定凭证';

-- 代驾报酬规则独立版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `payor_entity_id` BIGINT NOT NULL COMMENT '承担应付的主体',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '稳定规则标识',
  `revision` INT NOT NULL COMMENT '版本',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `scenario` VARCHAR(32) NOT NULL COMMENT '正常归还/取消返还/接车前解除',
  `scope_json` JSON NOT NULL COMMENT '车型/代驾/服务范围条件',
  `pricing_json` JSON NOT NULL COMMENT '固定费、含路线米、计费单位、单价及进位',
  `status` VARCHAR(24) NOT NULL COMMENT 'DriverFeeRuleStatus: DRAFT,PENDING_REVIEW,PUBLISHED,DISABLED',
  `effective_from` DATETIME(3) NOT NULL COMMENT '生效起点UTC',
  `effective_to` DATETIME(3) COMMENT '不含截止',
  `created_by` BIGINT NOT NULL COMMENT '规则编辑人',
  `approved_by` BIGINT COMMENT '独立审核人',
  `approved_at` DATETIME(3) COMMENT '发布确认时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_fee_rule_revision (payor_entity_id,rule_code,revision),
  KEY ix_fee_effective (payor_entity_id,scenario,status,effective_from),
  CONSTRAINT ck_66_78d970a2e61f CHECK (`status` IN ('DRAFT','PENDING_REVIEW','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_driver_fee_rule CHECK (version >= 0),
  CONSTRAINT ck_driver_fee_rule_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾报酬规则独立版本';

-- 按真实承接形成的不可覆盖费用台账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_ledger` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '付款责任主体',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `assignment_id` BIGINT NOT NULL COMMENT '具体承接，不仅订单',
  `order_id` BIGINT NOT NULL COMMENT '客户订单',
  `source_event_key` VARCHAR(128) NOT NULL COMMENT '归还/解除/调整永久去重键',
  `entry_kind` VARCHAR(32) NOT NULL COMMENT 'NORMAL_RETURN/CANCEL_RETURN/CANCEL_BEFORE_PICKUP/ADJUSTMENT/INITIAL_VALUATION/PRICE_DELTA',
  `service_ended_at` DATETIME(3) NOT NULL COMMENT '实际任务结束UTC；非客户服务确认时间',
  `pricing_status` VARCHAR(24) NOT NULL COMMENT 'LedgerPricingStatus: PENDING_PRICING,READY',
  `fee_rule_id` BIGINT COMMENT '匹配规则版本ID',
  `rule_snapshot` JSON COMMENT '冻结规则',
  `amount_fen` BIGINT COMMENT '未知为NULL；调整可负',
  `fee_breakdown` JSON COMMENT '基础/路线/调整明细',
  `source_entry_id` BIGINT COMMENT '调整源条目',
  `verified_by` BIGINT COMMENT '人工核价复核人',
  `included_statement_id` BIGINT COMMENT '确认结转生成的核价/差额已直接纳入本单，不能再入未来账单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_fee_source (payor_entity_id,driver_id,source_event_key),
  KEY ix_fee_period (payor_entity_id,driver_id,service_ended_at),
  CONSTRAINT ck_fee_known CHECK ((pricing_status='PENDING_PRICING' AND amount_fen IS NULL) OR (pricing_status='READY' AND amount_fen IS NOT NULL)),
  CONSTRAINT ck_66_fadb078c6aab CHECK (`pricing_status` IN ('PENDING_PRICING','READY')),
  CONSTRAINT ck_ver_driver_fee_ledger CHECK (version >= 0),
  CONSTRAINT ck_driver_fee_ledger_entry_kind CHECK (`entry_kind` IN ('NORMAL_RETURN','CANCEL_RETURN','CANCEL_BEFORE_PICKUP','ADJUSTMENT','INITIAL_VALUATION','PRICE_DELTA')),
  CONSTRAINT ck_driver_fee_ledger_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='按真实承接形成的不可覆盖费用台账';

-- 单主体单司机单币种对账单 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_no` VARCHAR(64) NOT NULL COMMENT '对外账单编号',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '应付主体',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `service_range_start` DATETIME(3) NULL COMMENT '当前有效明细最早服务结束日UTC；仅说明跨度，不用于组单；空草稿NULL',
  `service_range_end_exclusive` DATETIME(3) NULL COMMENT '有效明细最晚结束日次日UTC；仅说明，不限定成员；空草稿NULL',
  `cutoff_at` DATETIME(3) NOT NULL COMMENT '本次数据截点',
  `status` VARCHAR(24) NOT NULL COMMENT 'StatementStatus: DRAFT,INTERNAL_REVIEW,SENT,DISPUTED,CONFIRMED,VOID',
  `revision` INT NOT NULL DEFAULT 1 COMMENT '未确认修订版本',
  `amount_fen` BIGINT NULL COMMENT '草稿有未核价行时NULL；不是0；金额齐全时为净拟应付',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '当前工作副本规范化摘要，可包含NULL；发送前金额必须完整',
  `issued_by` BIGINT NOT NULL COMMENT '平台制作人',
  `sent_at` DATETIME(3) COMMENT '发出时冻结当前版本',
  `confirmed_account_id` BIGINT COMMENT '本人微信账户确认',
  `confirmed_at` DATETIME(3) COMMENT '实际确认时间',
  `confirmation_revision` INT COMMENT '确认的版本',
  `voided_at` DATETIME(3) COMMENT '仅未确认账单可作废',
  `known_amount_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '已填写金额小计；不能冒充完整应付',
  `unpriced_count` INT NOT NULL DEFAULT 0 COMMENT '待填写行数，发送和确认要求为0',
  `manual_price_count` INT NOT NULL DEFAULT 0 COMMENT '本次有人工填价/改价的行数',
  `review_status` VARCHAR(24) NOT NULL DEFAULT 'NOT_REQUIRED' COMMENT 'StatementReviewStatus: NOT_REQUIRED,REQUIRED,PENDING,APPROVED,REJECTED',
  `review_revision` INT COMMENT '仅对匹配revision和hash的整单复核生效',
  `published_revision` INT COMMENT '当前发送给司机的版本；撤回后失效',
  `published_hash` BINARY(32) COMMENT '当前有效公开快照SHA256，与public_snapshot_hash一致；内部复核用snapshot_hash',
  `submitted_by` BIGINT COMMENT '复核提交人，需与复核人分离',
  `selection_mode` VARCHAR(24) NOT NULL DEFAULT 'EXPLICIT' COMMENT 'SelectionMode: EXPLICIT',
  `filter_snapshot` JSON NULL COMMENT '查询条件留痕，不是组单成员定义；不能据此重跑批量选择',
  `active_entry_count` INT NOT NULL DEFAULT 0 COMMENT '当前有效工作行数；0草稿可保存不可发出',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_no (statement_no),
  KEY ix_statement_owner (driver_id,payor_entity_id,created_at,status),
  CONSTRAINT ck_statement_range CHECK ((service_range_start IS NULL AND service_range_end_exclusive IS NULL) OR (service_range_start IS NOT NULL AND service_range_end_exclusive IS NOT NULL AND service_range_end_exclusive>service_range_start)),
  CONSTRAINT ck_66_09ea0dc1cb1b CHECK (`status` IN ('DRAFT','INTERNAL_REVIEW','SENT','DISPUTED','CONFIRMED','VOID')),
  CONSTRAINT ck_66_8c948c332a52 CHECK (`review_status` IN ('NOT_REQUIRED','REQUIRED','PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_66_83b18b98eb4a CHECK (`selection_mode` IN ('EXPLICIT')),
  CONSTRAINT ck_ver_driver_statement CHECK (version >= 0),
  CONSTRAINT ck_driver_statement_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单主体单司机单币种对账单';

-- 对账工作明细；历史发布版本另表保存 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_line` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '对账单',
  `statement_revision` INT NOT NULL COMMENT '所属账单修订',
  `ledger_id` BIGINT NOT NULL COMMENT '费用源条目',
  `assignment_id` BIGINT NOT NULL COMMENT '承接关联',
  `amount_fen` BIGINT NULL COMMENT '本工作版本拟金额，可空；已发送快照必须完整',
  `snapshot` JSON NOT NULL COMMENT '车牌/订单/服务时间/类型/规则/金额',
  `line_hash` BINARY(32) NOT NULL COMMENT '内容摘要',
  `reference_amount_fen` BIGINT NULL COMMENT '规则/源台账金额，只读，NULL不伪装成0',
  `pricing_source` VARCHAR(24) NOT NULL COMMENT 'PricingSource: RULE,UNPRICED,MANUAL_FILLED,MANUAL_CHANGED,APPROVED_ADJUSTMENT',
  `manual_changed` TINYINT NOT NULL DEFAULT 0 COMMENT '人工参与标记；改回原金额不擦除操作轨迹',
  `price_reason` VARCHAR(1000) COMMENT '当前拟价依据',
  `last_editor_user_id` BIGINT COMMENT '最近编辑人；复核排除需查本轮全部改价人',
  `source_version` BIGINT NOT NULL COMMENT '源台账快照版本',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '0为从草稿移出，保留价格与审计；只对有效行求合计和发布快照',
  `removed_at` DATETIME(3) NULL COMMENT '最近移出时间',
  `removed_by` BIGINT NULL COMMENT '移出操作者',
  `remove_reason` VARCHAR(1000) NULL COMMENT '移出原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_source (statement_id,ledger_id),
  KEY ix_statement_lines (statement_id,id),
  KEY ix_statement_active_lines (statement_id,is_active,id),
  CONSTRAINT ck_66_e02515a7b248 CHECK (`pricing_source` IN ('RULE','UNPRICED','MANUAL_FILLED','MANUAL_CHANGED','APPROVED_ADJUSTMENT')),
  CONSTRAINT ck_ver_driver_statement_line CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账工作明细；历史发布版本另表保存';

-- 每费用条目至多一个活跃账单的分配历史 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_ledger_allocation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `ledger_id` BIGINT NOT NULL COMMENT '费用条目',
  `statement_id` BIGINT NOT NULL COMMENT '有效账单',
  `released_at` DATETIME(3) COMMENT '草稿合法移出或作废释放；已确认未付不能释放',
  `active_ledger` BIGINT GENERATED ALWAYS AS (IF(released_at IS NULL,ledger_id,NULL)) STORED COMMENT '每费用条目至多一个有效账单',
  `release_reason` VARCHAR(1000) NULL COMMENT '精确释放原因',
  `released_by` BIGINT NULL COMMENT '释放者',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_ledger (active_ledger),
  KEY ix_allocation_statement (statement_id),
  CONSTRAINT ck_ver_driver_ledger_allocation CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每费用条目至多一个活跃账单的分配历史';

-- 本人对账差异与处理记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_difference` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `statement_revision` INT NOT NULL COMMENT '被质疑版本',
  `driver_account_id` BIGINT NOT NULL COMMENT '本人',
  `entry_ids` JSON COMMENT '相关条目',
  `description` VARCHAR(2000) NOT NULL COMMENT '差异说明',
  `evidence_ids` JSON COMMENT '凭证引用',
  `status` VARCHAR(24) NOT NULL COMMENT 'StatementDifferenceStatus: OPEN,RESOLVED',
  `resolution` VARCHAR(2000) COMMENT '处理说明',
  `approved_adjustment_ids` JSON COMMENT '金额变更引用已审核调整',
  `resolved_by` BIGINT COMMENT '处理人',
  `resolved_at` DATETIME(3) COMMENT 'UTC',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY ix_difference_queue (status,created_at),
  CONSTRAINT ck_66_c02249164615 CHECK (`status` IN ('OPEN','RESOLVED')),
  CONSTRAINT ck_ver_driver_statement_difference CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人对账差异与处理记录';

-- 独立审核的费用调整 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_adjustment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `source_entry_id` BIGINT NOT NULL COMMENT '源费用记录',
  `driver_id` BIGINT NOT NULL COMMENT '源代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '源主体',
  `amount_fen` BIGINT NOT NULL COMMENT '非0有符号调整',
  `reason` VARCHAR(2000) NOT NULL COMMENT '业务依据',
  `evidence_ids` JSON NOT NULL COMMENT '必填凭证引用',
  `status` VARCHAR(16) NOT NULL COMMENT 'AdjustmentStatus: PENDING,APPROVED,REJECTED',
  `created_by` BIGINT NOT NULL COMMENT '经办人',
  `reviewer_id` BIGINT COMMENT '独立审核人',
  `opinion` VARCHAR(1000) COMMENT '意见',
  `generated_ledger_id` BIGINT COMMENT '审批后唯一派生费用条目',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_generated_entry (generated_ledger_id),
  CONSTRAINT ck_adjust_nonzero CHECK (amount_fen<>0),
  CONSTRAINT ck_66_32e34f614311 CHECK (`status` IN ('PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_driver_fee_adjustment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='独立审核的费用调整';

-- 外部已付款证据登记与核实，不发起转账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_external_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '已确认对账单',
  `driver_id` BIGINT NOT NULL COMMENT '收款代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '付款主体',
  `amount_fen` BIGINT NOT NULL COMMENT '正数已发生付款',
  `external_reference_cipher` VARBINARY(1024) NOT NULL COMMENT '外部流水密文',
  `reference_lookup` BINARY(32) NOT NULL COMMENT '带密钥查询摘要',
  `paid_at` DATETIME(3) NOT NULL COMMENT '外部付款时间',
  `evidence_ids` JSON NOT NULL COMMENT '凭证',
  `status` VARCHAR(24) NOT NULL COMMENT 'ExternalPaymentStatus: PENDING,VERIFIED,REJECTED,REVERSED',
  `registered_by` BIGINT NOT NULL COMMENT '登记人',
  `verified_by` BIGINT COMMENT '不可同一真实身份',
  `verified_at` DATETIME(3) COMMENT '核对时间',
  `reason` VARCHAR(1000) COMMENT '核对说明/冲正原因',
  `reversal_of` BIGINT COMMENT '有错通过冲正记录处理',
  `statement_revision` INT NOT NULL COMMENT '关联的已确认内容版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_external_reference (payor_entity_id,reference_lookup),
  KEY ix_statement_payment (statement_id,status),
  CONSTRAINT ck_external_positive CHECK (amount_fen>0),
  CONSTRAINT ck_66_cd7059501d32 CHECK (`status` IN ('PENDING','VERIFIED','REJECTED','REVERSED')),
  CONSTRAINT ck_ver_driver_external_payment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部已付款证据登记与核实，不发起转账';

-- 小程序账号安全版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_account_security` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT 'vi_account',
  `session_epoch` BIGINT NOT NULL DEFAULT 0 COMMENT '全局退出/安全封禁代际',
  `permission_version` BIGINT NOT NULL DEFAULT 0 COMMENT '角色/能力变化版本，不等于资料version',
  `security_status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'AccountStatus: ACTIVE,LOCKED',
  `changed_at` DATETIME(3) NOT NULL COMMENT '最近权限/安全变化时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_account_security (account_id),
  CONSTRAINT ck_66_553d429ab631 CHECK (`security_status` IN ('ACTIVE','LOCKED')),
  CONSTRAINT ck_ver_account_security CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小程序账号安全版本';

-- 小程序独立设备会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '基础账号',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '微信应用',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'SessionStatus: ACTIVE,REVOKED,EXPIRED',
  `session_epoch` BIGINT NOT NULL COMMENT '签发时代际',
  `context_version` BIGINT NOT NULL DEFAULT 1 COMMENT '当前设备角色/站点切换版本',
  `active_role` VARCHAR(24) NOT NULL DEFAULT 'OWNER' COMMENT 'OWNER/DRIVER/STATION；不是授权来源',
  `station_id` BIGINT COMMENT '只存获授权当前站点上下文',
  `linked_sys_user_id` BIGINT COMMENT '可信受控绑定快照；每次工作操作再查实时状态',
  `absolute_expires_at` DATETIME(3) NOT NULL COMMENT '会话绝对截止，不无限滑动',
  `revoked_at` DATETIME(3) COMMENT '退出或安全撤销',
  `device_label` VARCHAR(120) COMMENT '最小必要设备标识摘要，不作为授权凭据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  KEY ix_session_account (account_id,status),
  KEY ix_session_expiry (status,absolute_expires_at),
  CONSTRAINT ck_66_b41d9706dfcd CHECK (`status` IN ('ACTIVE','REVOKED','EXPIRED')),
  CONSTRAINT ck_ver_session CHECK (version >= 0),
  CONSTRAINT ck_session_active_role CHECK (`active_role` IN ('OWNER','DRIVER','STATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小程序独立设备会话';

-- 访问与刷新令牌摘要 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_session_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `session_id` BIGINT NOT NULL COMMENT 'vi_session',
  `token_hash` BINARY(32) NOT NULL COMMENT '安全随机令牌的摘要；不存明文',
  `token_type` VARCHAR(16) NOT NULL COMMENT 'SessionTokenType: ACCESS,REFRESH',
  `generation` BIGINT NOT NULL COMMENT '刷新令牌代际',
  `context_version` BIGINT COMMENT 'ACCESS绑定上下文',
  `parent_token_id` BIGINT COMMENT '刷新祖先关系',
  `replaced_by_token_id` BIGINT COMMENT '轮换后的令牌引用',
  `expires_at` DATETIME(3) NOT NULL COMMENT '到期',
  `consumed_at` DATETIME(3) COMMENT '刷新原子消费时间',
  `revoked_at` DATETIME(3) COMMENT '撤销时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_token_hash (token_hash),
  KEY ix_token_session (session_id,token_type,generation),
  KEY ix_token_expiry (expires_at),
  CONSTRAINT ck_66_dafe3b97503e CHECK (`token_type` IN ('ACCESS','REFRESH')),
  CONSTRAINT ck_ver_session_token CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访问与刷新令牌摘要';

-- 对账内填写与改价不可变记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_price_change` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `statement_line_id` BIGINT NOT NULL COMMENT '工作明细',
  `ledger_id` BIGINT NOT NULL COMMENT '不可变费用源',
  `revision` INT NOT NULL COMMENT '修改后的内容修订',
  `old_amount_fen` BIGINT NULL COMMENT '原拟价，未填写为NULL',
  `new_amount_fen` BIGINT NOT NULL COMMENT '新拟价，总额输入，不是重复加差额',
  `reason` VARCHAR(1000) NOT NULL COMMENT '修改原因或初次定价依据',
  `evidence_file_ids` JSON COMMENT '经权限及归属验证的文件引用',
  `actor_user_id` BIGINT NOT NULL COMMENT '从ADMIN会话读取',
  `actor_account_id` BIGINT COMMENT '与受控绑定一致的真实人员关联，可空',
  `idempotency_key` VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '同主体动作资源请求键',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_price_change_retry (actor_user_id,statement_line_id,idempotency_key),
  KEY ix_price_history (statement_id,revision,id),
  CONSTRAINT ck_price_nonnegative CHECK (new_amount_fen>=0),
  CONSTRAINT ck_ver_driver_price_change CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账内填写与改价不可变记录';

-- 整单内部复核记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `revision` INT NOT NULL COMMENT '提交内容版本',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '待审全部明细摘要',
  `snapshot_json` JSON NOT NULL COMMENT '冻结的明细与变更，不能只查当前值',
  `contributor_ids` JSON NOT NULL COMMENT '服务端查出的本轮所有实际改价人',
  `submitted_by` BIGINT NOT NULL COMMENT '提交人',
  `reviewed_by` BIGINT COMMENT '独立复核人',
  `status` VARCHAR(24) NOT NULL COMMENT 'InternalReviewRecordStatus: PENDING,APPROVED,REJECTED,WITHDRAWN',
  `opinion` VARCHAR(1000) COMMENT '复核或撤回意见',
  `decided_at` DATETIME(3) COMMENT '决定时间',
  `active_statement` BIGINT GENERATED ALWAYS AS (IF(status='PENDING',statement_id,NULL)) STORED COMMENT '每账单至多一份待审',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_statement_review (active_statement),
  KEY ix_statement_review_history (statement_id,revision),
  CONSTRAINT ck_66_081decdd6dd7 CHECK (`status` IN ('PENDING','APPROVED','REJECTED','WITHDRAWN')),
  CONSTRAINT ck_ver_driver_statement_review CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整单内部复核记录';

-- 已发送与确认的不可变版本快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `revision` INT NOT NULL COMMENT '内容版本',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '规范化SHA-256，包含主体司机币种和明细',
  `snapshot_json` JSON NOT NULL COMMENT '完整明细、理由、总额与审核来源',
  `amount_fen` BIGINT NOT NULL COMMENT '此版明确净应付',
  `published_by` BIGINT NOT NULL COMMENT '发送或复核人',
  `public_snapshot_json` JSON NOT NULL COMMENT '仅对本人发布的规范化字段，不含内部理由/改价者/复核者',
  `public_snapshot_hash` BINARY(32) NOT NULL COMMENT 'SHA256公开快照；MINI snapshotHash与此匹配，不用内部snapshot_hash',
  `published_at` DATETIME(3) NOT NULL COMMENT '版本发出时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_snapshot (statement_id,revision),
  CONSTRAINT ck_ver_driver_statement_snapshot CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='已发送与确认的不可变版本快照';

-- 对账成员变更不可变历史 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_membership_change` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `ledger_id` BIGINT NULL COMMENT '源条目；整单作废时可为空',
  `line_id` BIGINT NULL COMMENT '可重复激活的工作行',
  `action` VARCHAR(24) NOT NULL COMMENT 'CREATE/ADD/REMOVE/REACTIVATE/VOID',
  `revision_before` INT NOT NULL COMMENT '变更前内容版本',
  `revision_after` INT NOT NULL COMMENT '变更后内容版本',
  `actor_user_id` BIGINT NOT NULL COMMENT '真实授权后台操作者',
  `reason` VARCHAR(1000) NOT NULL COMMENT '操作依据',
  `snapshot_json` JSON NOT NULL COMMENT '集合变化及涉及行完整快照；不存凭据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  PRIMARY KEY (`id`),
  KEY ix_membership_history (statement_id,revision_after,id),
  CONSTRAINT ck_driver_statement_membership_change_action CHECK (`action` IN ('CREATE','ADD','REMOVE','REACTIVATE','VOID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账成员变更不可变历史';

-- 普通取消处罚申诉记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_penalty_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `penalty_id` BIGINT NOT NULL COMMENT '处罚主键',
  `driver_id` BIGINT NOT NULL COMMENT '本人司机档案',
  `status` VARCHAR(24) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'AppealRecordStatus: SUBMITTED,APPROVED,REJECTED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '本人说明',
  `evidence_file_ids` JSON NULL COMMENT '通过文件权限检查的证据引用',
  `submitted_at` DATETIME(3) NOT NULL COMMENT 'UTC提交时刻',
  `reviewer_user_id` BIGINT NULL COMMENT '独立后台审核人',
  `reviewed_at` DATETIME(3) NULL COMMENT 'UTC审核时刻',
  `public_opinion` VARCHAR(1000) NULL COMMENT '对本人公开结论',
  `active_penalty` BIGINT GENERATED ALWAYS AS (IF(status='SUBMITTED',penalty_id,NULL)) STORED COMMENT '同一处罚只允许一项待审申诉',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_pending_penalty_appeal (active_penalty),
  KEY ix_driver_appeal (driver_id,created_at),
  CONSTRAINT ck_66_623fb1878076 CHECK (`status` IN ('SUBMITTED','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_penalty_appeal CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通取消处罚申诉记录';

-- 本人发起的受控员工绑定申请 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `account_id` BIGINT NOT NULL COMMENT '不可由客户端选择申请主体',
  `request_code` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '公开核对编号，不是授权秘密',
  `status` VARCHAR(32) NOT NULL DEFAULT 'REQUESTED' COMMENT 'EmployeeBindingStatus: REQUESTED,CREDENTIAL_ISSUED,BOUND,REJECTED,EXPIRED,REVOKED',
  `request_reason` VARCHAR(500) NOT NULL COMMENT '本人申请理由',
  `expires_at` DATETIME(3) NOT NULL COMMENT '到期不自动绑定',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_binding_request_code (request_code),
  KEY ix_binding_request_owner (account_id,status),
  CONSTRAINT ck_66_8efc7aae47ab CHECK (`status` IN ('REQUESTED','CREDENTIAL_ISSUED','BOUND','REJECTED','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_employee_binding_request CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人发起的受控员工绑定申请';

-- 管理员签发的一次性员工绑定凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding_credential` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `request_id` BIGINT NOT NULL COMMENT '绑定申请',
  `target_account_id` BIGINT NOT NULL COMMENT '凭证只能由此已认证account消费',
  `target_sys_user_id` BIGINT NOT NULL COMMENT '核对现有sys_user及其全部任职',
  `token_hash` BINARY(32) NOT NULL COMMENT '随机凭证摘要；不保存或日志输出明文',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ISSUED' COMMENT 'BindingCredentialStatus: ISSUED,CONSUMED,REVOKED,EXPIRED',
  `issued_by_user_id` BIGINT NOT NULL COMMENT '真正后台签发人',
  `expires_at` DATETIME(3) NOT NULL COMMENT 'UTC短期失效',
  `consumed_at` DATETIME(3) NULL COMMENT '一次性消费',
  `reason` VARCHAR(500) NOT NULL COMMENT '签发理由',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_binding_token_hash (token_hash),
  KEY ix_binding_credential_request (request_id,status),
  CONSTRAINT ck_66_ec6a315733a1 CHECK (`status` IN ('ISSUED','CONSUMED','REVOKED','EXPIRED')),
  CONSTRAINT ck_ver_employee_binding_credential CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员签发的一次性员工绑定凭证';

-- 代驾培训不可变课程版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `course_key` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '稳定课程编码',
  `course_version` INT NOT NULL COMMENT '课程内容版本',
  `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
  `description` TEXT NULL COMMENT '培训说明',
  `media_file_id` BIGINT NOT NULL COMMENT '私有培训媒体',
  `required_duration_seconds` INT NOT NULL COMMENT '规定学习时长；发布时校验>0',
  `pass_score` INT NOT NULL COMMENT '0至100的配置门槛',
  `attempt_time_limit_seconds` INT NOT NULL COMMENT '考试时间配置',
  `status` VARCHAR(24) NOT NULL DEFAULT 'DRAFT' COMMENT 'TrainingCourseStatus: DRAFT,PUBLISHED,DISABLED',
  `published_at` DATETIME(3) NULL COMMENT 'UTC发布时间',
  `published_by_user_id` BIGINT NULL COMMENT '后台发布人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_course_revision (course_key,course_version),
  CONSTRAINT ck_course_score CHECK (pass_score BETWEEN 0 AND 100),
  CONSTRAINT ck_course_duration CHECK (required_duration_seconds>0 AND attempt_time_limit_seconds>=60),
  CONSTRAINT ck_66_0aa07ede2e69 CHECK (`status` IN ('DRAFT','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_training_course CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾培训不可变课程版本';

-- 培训课程版本的后台题库 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `training_id` BIGINT NOT NULL COMMENT '确切课程版本',
  `question_code` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '课程内稳定编码',
  `question_type` VARCHAR(24) NOT NULL COMMENT 'QuestionType: SINGLE,MULTIPLE,TRUE_FALSE',
  `prompt` TEXT NOT NULL COMMENT '题干',
  `options_json` JSON NOT NULL COMMENT 'code和text',
  `correct_option_codes` JSON NOT NULL COMMENT '仅课程管理/服务端阅卷可见，禁止MINI输出',
  `sort_no` INT NOT NULL COMMENT '排序',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_course_question (training_id,question_code),
  CONSTRAINT ck_66_3f48408ab1be CHECK (`question_type` IN ('SINGLE','MULTIPLE','TRUE_FALSE')),
  CONSTRAINT ck_ver_training_question CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训课程版本的后台题库';

-- 本人每个课程版本的学习进度 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `driver_id` BIGINT NOT NULL COMMENT '真正司机档案',
  `training_id` BIGINT NOT NULL COMMENT '课程版本主键',
  `verified_duration_seconds` INT NOT NULL DEFAULT 0 COMMENT '服务端按会话时间限幅的有效学习时长',
  `status` VARCHAR(24) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'TrainingStatus: NOT_STARTED,IN_PROGRESS,READY_FOR_EXAM,PASSED,FAILED',
  `last_position_seconds` INT NOT NULL DEFAULT 0 COMMENT '仅恢复位置，不等于有效学习时长',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_progress (application_id,training_id),
  CONSTRAINT ck_66_57bd05d422b1 CHECK (`status` IN ('NOT_STARTED','IN_PROGRESS','READY_FOR_EXAM','PASSED','FAILED')),
  CONSTRAINT ck_ver_training_progress CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人每个课程版本的学习进度';

-- 短期受控学习会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_playback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `progress_id` BIGINT NOT NULL COMMENT '本人进度',
  `session_id` BIGINT NOT NULL COMMENT '小程序设备会话',
  `started_at` DATETIME(3) NOT NULL COMMENT '服务端开始时间',
  `expires_at` DATETIME(3) NOT NULL COMMENT '服务端结束边界',
  `last_reported_at` DATETIME(3) NULL COMMENT '最近服务器接收时刻',
  `last_sequence` INT NOT NULL DEFAULT 0 COMMENT '乱序和重复不增加时长',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  KEY ix_playback_progress (progress_id,expires_at),
  CONSTRAINT ck_ver_training_playback CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短期受控学习会话';

-- 本人考试尝试及不可变题卷快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_attempt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `driver_id` BIGINT NOT NULL COMMENT '本人',
  `training_id` BIGINT NOT NULL COMMENT '确切课程版本',
  `attempt_no` INT NOT NULL COMMENT '本课程尝试序号',
  `status` VARCHAR(24) NOT NULL DEFAULT 'OPEN' COMMENT 'AttemptStatus: OPEN,SUBMITTED,PASSED,FAILED,EXPIRED',
  `question_snapshot` JSON NOT NULL COMMENT '确切题干/选项；无答案的对外投影单独构造',
  `answer_snapshot` JSON NULL COMMENT '本人作答',
  `score` INT NULL COMMENT '服务器阅卷0至100',
  `started_at` DATETIME(3) NOT NULL COMMENT 'UTC开始',
  `expires_at` DATETIME(3) NOT NULL COMMENT 'UTC失效',
  `submitted_at` DATETIME(3) NULL COMMENT 'UTC提交',
  `active_attempt` VARCHAR(128) GENERATED ALWAYS AS (IF(status='OPEN',CONCAT(application_id,':',training_id),NULL)) STORED COMMENT '同申请同课程仅一个开放尝试',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_attempt_number (application_id,training_id,attempt_no),
  UNIQUE KEY uk_open_training_attempt (active_attempt),
  CONSTRAINT ck_66_5b693ca15a96 CHECK (`status` IN ('OPEN','SUBMITTED','PASSED','FAILED','EXPIRED')),
  CONSTRAINT ck_ver_training_attempt CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人考试尝试及不可变题卷快照';

-- 本人实名活体启动与可信结果会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_face_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `verification_id` BIGINT NULL COMMENT '供应商核验记录',
  `account_id` BIGINT NOT NULL COMMENT '本人基础账户',
  `status` VARCHAR(24) NOT NULL DEFAULT 'CREATED' COMMENT 'FaceSessionStatus: CREATED,PROCESSING,PASSED,FAILED,EXPIRED',
  `launch_mode` VARCHAR(24) NOT NULL DEFAULT 'WEBVIEW_URL' COMMENT 'FaceLaunchMode: WEBVIEW_URL',
  `launch_payload_cipher` BLOB NULL COMMENT '加密短期URL；失效或结束销毁',
  `consent_version` VARCHAR(64) NOT NULL COMMENT '此次明确同意版本',
  `expires_at` DATETIME(3) NOT NULL COMMENT '供应商与本地较短有效期',
  `result_available` TINYINT NOT NULL DEFAULT 0 COMMENT '可信查询后赋值',
  `failure_code` VARCHAR(64) NULL COMMENT '安全映射错误，无原始生物数据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  KEY ix_face_app_status (application_id,status),
  CONSTRAINT ck_66_b67f9761e849 CHECK (`status` IN ('CREATED','PROCESSING','PASSED','FAILED','EXPIRED')),
  CONSTRAINT ck_66_a153de145e1b CHECK (`launch_mode` IN ('WEBVIEW_URL')),
  CONSTRAINT ck_ver_face_session CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人实名活体启动与可信结果会话';
