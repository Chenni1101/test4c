-- 修复 user_roles 由 JPA 多对多关联写入时未显式提供 created_at 的兼容性问题
-- 通过数据库默认值保证插入 user_id/role_id 也能满足非空约束。
ALTER TABLE user_roles
ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
