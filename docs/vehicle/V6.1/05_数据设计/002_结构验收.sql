-- 只读；预期主键缺失查询0行。其余结果与data_model JSON对照。
SELECT t.table_name FROM information_schema.tables t
LEFT JOIN information_schema.table_constraints c ON c.table_schema=t.table_schema
 AND c.table_name=t.table_name AND c.constraint_type='PRIMARY KEY'
WHERE t.table_schema=DATABASE() AND LEFT(t.table_name,3)='vi_' AND c.constraint_name IS NULL;
SELECT table_name,column_name,column_type,extra,column_default FROM information_schema.columns
WHERE table_schema=DATABASE() AND LEFT(table_name,3)='vi_' AND
(column_key='PRI' OR extra LIKE '%GENERATED%' OR extra LIKE '%auto_increment%') ORDER BY table_name,ordinal_position;
SELECT tc.table_name, cc.constraint_name,cc.check_clause FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc ON tc.constraint_schema=cc.constraint_schema AND tc.constraint_name=cc.constraint_name
WHERE tc.table_schema=DATABASE() AND LEFT(tc.table_name,3)='vi_' AND tc.constraint_type='CHECK';
