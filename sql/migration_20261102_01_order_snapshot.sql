-- 用途：B03原子建单补齐联系人、地址、备注与协议接受快照。
-- 前置：V6.4及20261101客户计价迁移；执行顺序：既有迁移完成后执行本文件。
-- 备份：执行前备份目标库vi_order、vi_quote及DDL；正式环境按发布审批执行。
-- 不允许重复执行：新表已存在时先核对结构及迁移记录，不删除重建。
-- 兼容性：只新增表，无历史字段改写。历史订单缺少可信联系/接受证据，禁止猜测回填。
-- 回滚：先回退依赖本表的应用；保留新增表及交易快照，不DROP、不丢弃证据。
-- 先报告存量，执行后再次报告未补齐订单，由受控迁移人工核实原证据。
SELECT id,order_no,quote_id FROM vi_order ORDER BY id;

CREATE TABLE vi_order_snapshot (
  order_id BIGINT NOT NULL COMMENT '父订单；权限按vi_order.owner_account_id与真实dept_id校验',
  address_cipher LONGBLOB NOT NULL COMMENT '订单时点接车及默认返还地址密文',
  contact_cipher LONGBLOB NOT NULL COMMENT '服务端已核验联系人及手机号密文',
  remark_cipher LONGBLOB NULL COMMENT '客户备注密文；不进入审计或幂等结果',
  station_snapshot JSON NOT NULL COMMENT '创建时公开站点资料',
  agreement_snapshot JSON NOT NULL COMMENT '显式接受的服务及隐私协议版本与完整内容',
  accepted_at DATETIME(3) NOT NULL COMMENT '数据库UTC接受时刻，与建单同事务',
  created_at DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；不可覆盖原快照',
  version BIGINT NOT NULL DEFAULT 0 COMMENT '不可变快照版本',
  PRIMARY KEY (order_id),
  CONSTRAINT ck_order_snapshot_ver CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='建单私有及协议快照';

-- 执行后验证：缺快照历史订单必须输出，不假造已接受记录。
SELECT o.id,o.order_no,o.quote_id FROM vi_order o LEFT JOIN vi_order_snapshot s ON s.order_id=o.id WHERE s.order_id IS NULL ORDER BY o.id;
SELECT column_name,column_type,is_nullable,column_default FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='vi_order_snapshot' ORDER BY ordinal_position;
SELECT index_name,seq_in_index,column_name FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='vi_order_snapshot' ORDER BY index_name,seq_in_index;
