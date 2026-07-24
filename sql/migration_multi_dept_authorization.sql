-- 多部门任职、安全授权与审计迁移（MySQL，一次性执行）
-- 执行前必须备份；生产发布应先执行结构与回填，再根据文末校验结果决定是否放量。

alter table sys_user
  add column management_scope varchar(16) not null default 'DEPT'
    comment '管理范围（DEPT部门账号 PLATFORM平台账号）' after dept_id,
  add column auth_version bigint not null default 0
    comment '授权版本' after management_scope;

create table if not exists sys_user_dept (
  user_id      bigint not null comment '用户ID',
  dept_id      bigint not null comment '任职部门ID',
  create_by    varchar(64) default '' comment '创建者',
  create_time  datetime comment '创建时间',
  primary key (user_id, dept_id),
  key idx_sys_user_dept_dept_user (dept_id, user_id)
) engine=innodb comment='用户任职部门关联表';

create table if not exists sys_security_outbox (
  event_id            bigint not null auto_increment comment '事件ID',
  user_id             bigint not null comment '受影响用户ID',
  target_auth_version bigint not null comment '目标授权版本',
  event_type          varchar(64) not null comment '事件类型',
  status              varchar(16) not null default 'PENDING' comment 'PENDING RETRY DONE',
  retry_count         int not null default 0 comment '重试次数',
  next_retry_time     datetime not null comment '下次执行时间',
  last_error          varchar(1000) default null comment '最后错误',
  create_time         datetime not null comment '创建时间',
  complete_time       datetime default null comment '完成时间',
  primary key (event_id),
  key idx_security_outbox_pending (status, next_retry_time, event_id),
  key idx_security_outbox_user (user_id, event_id)
) engine=innodb comment='安全会话清理outbox';

create table if not exists sys_security_audit_log (
  audit_id            bigint not null auto_increment comment '审计ID',
  event_type          varchar(64) not null comment '事件类型',
  actor_user_id       bigint default null comment '操作者用户ID',
  actor_user_name     varchar(64) default null comment '操作者账号',
  target_user_id      bigint default null comment '目标用户ID',
  request_id          varchar(64) not null comment '请求ID',
  client_ip           varchar(128) default null comment '客户端IP',
  reason              varchar(500) default null comment '操作原因',
  before_json         text comment '变更前快照',
  after_json          text comment '变更后快照',
  auth_version_before bigint default null comment '变更前版本',
  auth_version_after  bigint default null comment '变更后版本',
  result              varchar(16) not null comment 'SUCCESS FAILURE',
  failure_code        varchar(64) default null comment '失败码',
  failure_message     varchar(1000) default null comment '失败信息',
  create_time         datetime not null comment '创建时间',
  primary key (audit_id),
  key idx_security_audit_target (target_user_id, create_time),
  key idx_security_audit_actor (actor_user_id, create_time),
  key idx_security_audit_request (request_id)
) engine=innodb comment='不可由业务接口删除的安全审计';

-- 先记录异常，再回填合法主部门；不猜测或修复异常部门。
create table if not exists sys_user_dept_migration_issue (
  issue_id     bigint not null auto_increment,
  issue_type   varchar(64) not null,
  user_id      bigint default null,
  dept_id      bigint default null,
  detail       varchar(500) default null,
  create_time  datetime not null,
  primary key (issue_id),
  key idx_user_dept_issue_user (user_id, issue_type)
) engine=innodb comment='多部门迁移异常报告';

insert into sys_user_dept_migration_issue(issue_type, user_id, dept_id, detail, create_time)
select 'PRIMARY_DEPT_MISSING', u.user_id, u.dept_id, '有效用户没有主部门', sysdate()
from sys_user u
where u.del_flag = '0' and u.dept_id is null;

insert into sys_user_dept_migration_issue(issue_type, user_id, dept_id, detail, create_time)
select 'PRIMARY_DEPT_INVALID', u.user_id, u.dept_id, '主部门不存在、已删除或已停用', sysdate()
from sys_user u
left join sys_dept d on d.dept_id = u.dept_id
where u.del_flag = '0' and u.dept_id is not null
  and (d.dept_id is null or d.del_flag <> '0' or d.status <> '0');

insert ignore into sys_user_dept(user_id, dept_id, create_by, create_time)
select u.user_id, u.dept_id, 'migration', sysdate()
from sys_user u
inner join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0' and d.status = '0'
where u.del_flag = '0' and u.dept_id is not null;

insert into sys_menu
select 1061, '任职部门修改', 100, 8, '', '', '', '', 1, 0, 'F', '0', '0',
       'system:user:dept:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where menu_id = 1061);

insert into sys_menu
select 1062, '用户角色修改', 100, 9, '', '', '', '', 1, 0, 'F', '0', '0',
       'system:user:role:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where menu_id = 1062);

insert into sys_menu
select 1063, '平台账号降级', 100, 10, '', '', '', '', 1, 0, 'F', '0', '0',
       'system:user:management:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where menu_id = 1063);

create or replace view sys_user_dept_integrity_issue as
select 'DEPT_SCOPE_RELATION_COUNT' as issue_type, u.user_id, u.dept_id
from sys_user u
left join sys_user_dept ud on ud.user_id = u.user_id
where u.del_flag = '0' and u.management_scope = 'DEPT'
group by u.user_id, u.dept_id
having count(ud.dept_id) <> 1 or min(ud.dept_id) <> u.dept_id
union all
select 'PRIMARY_NOT_IN_MEMBERSHIP', u.user_id, u.dept_id
from sys_user u
where u.del_flag = '0'
  and not exists (
    select 1 from sys_user_dept ud
    where ud.user_id = u.user_id and ud.dept_id = u.dept_id
  )
union all
select 'ORPHAN_OR_DELETED_RELATION', ud.user_id, ud.dept_id
from sys_user_dept ud
left join sys_user u on u.user_id = ud.user_id
left join sys_dept d on d.dept_id = ud.dept_id
where u.user_id is null or u.del_flag <> '0'
   or d.dept_id is null or d.del_flag <> '0';

-- 发布门禁：以下两个结果都必须为0，异常数据只允许人工确认后修复。
select count(*) as migration_issue_count from sys_user_dept_migration_issue;
select count(*) as integrity_issue_count from sys_user_dept_integrity_issue;

-- 如果开启自助注册，必须由平台管理员显式配置一个有效默认主部门：
-- insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
-- values('用户注册-默认主部门', 'sys.user.defaultDeptId', '<有效部门ID>', 'Y', 'admin', sysdate(),
--        '开启注册前必须配置，禁止创建无部门账号');
