# CAS、公开快照与同事务副作用

## 司机账单确认（伪代码，须落在真实MyBatis事务）
```text
authenticate MINI; resolve actual driver; validate Idempotency-Key/body digest
SELECT statement FOR UPDATE
assert driver ownership/payor visibility; state SENT; no withdrawal
assert version == expectedVersion; published_revision == expectedRevision
SELECT immutable snapshot by (statement_id,revision)
assert public_snapshot_hash == decoded snapshotHash
assert snapshot public amount == statement amount; no pending manual pricing
UPDATE statement SET status=CONFIRMED,confirmed_account_id=:actor,
 confirmed_at=UTC_TIMESTAMP(3),confirmation_revision=:rev,
 version=version+1,updated_at=UTC_TIMESTAMP(3)
 WHERE id=:id AND status='SENT' AND version=:version
 AND published_revision=:rev AND published_hash=:publicHash
assert affectedRows==1
insert unique confirmation fact; generate only final difference/initial price once
allocate generated ledger to this statement (not future selectable set)
insert audit/outbox; commit
return DriverStatementPublic from confirmed immutable public snapshot
```

published_hash是公开摘要；snapshot_hash是内部工作摘要，review用后者；两个不能混为一个。列类型BINARY(32)按hex解码比较，不把hex文本当32字节存储。snapshot JSON按公开规范签名，付款额变化不使已确认服务版本过时。

## 归还释放
先锁订单和承接、验证当前保管/码/证据版本。释放占用必须driver_id、holder_type和assignmentId同时匹配；旧单事件不匹配新占用则不删除。物理交接、承接结束、司机占用释放、订单阶段、领域流水、outbox同事务。异步核价/消息在提交后，失败可恢复，不重新占司机。

## 选择组单
按source fee entry ID稳定顺序锁行；验证sourceVersion和有效allocation；同driver/payor/currency。先全量校验再写工作行，任何冲突整单409。相同幂等键同体返回原账单；不同键部分重叠一份失败；同期间不重叠两份均成功。只读预览不持有长期锁，不以日期再选一次。

## 失败处理
更新0行返回VERSION_CONFLICT或STATE_CONFLICT，事务不得继续插成功事件。数据库唯一异常映射具体错误，不把所有SQL异常当并发冲突。权限撤销与写入要使用共享权威版本条件或受控锁顺序，不能指望Redis删除使已经执行中的请求消失。
