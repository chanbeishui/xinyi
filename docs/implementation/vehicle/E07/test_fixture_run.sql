-- E07 测试专用 run 资源登记表（仅存在于隔离 IT 库 xinyi_vi_it_*，不进入 66 业务主模型）
-- 用途：夹具按 (provider_run_id, fixture_kind) 幂等复用 + 隔离资源追踪/审计
-- 禁止对任意业务表开放 generic insert；本表只在 vi-it 测试 classpath 的 FixtureProvisioner 使用
CREATE TABLE IF NOT EXISTS test_fixture_run (
  id BIGINT NOT NULL AUTO_INCREMENT,
  provider_run_id VARCHAR(128) NOT NULL COMMENT '模拟器 /control/runs 下发的 run 标识',
  fixture_kind VARCHAR(32) NOT NULL COMMENT 'IDENTITY 或 BOOKING',
  run_id VARCHAR(64) NOT NULL COMMENT '本次夹具运行内部 ID',
  response_json JSON NOT NULL COMMENT '返回给测试客户端的夹具契约响应',
  created_at DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)),
  PRIMARY KEY (id),
  UNIQUE KEY uk_run (provider_run_id, fixture_kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='E07 测试夹具 run 登记（测试专用，非业务主模型）';
