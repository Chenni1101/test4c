INSERT INTO roles (id, code, name, description, permissions_json, is_system, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'GUEST', '访客', '仅可访问已授权的公开能力', '[]', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (id, code, name, description, permissions_json, is_system, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000002', 'CREATOR', '创作者', '仅可管理本人资产', '[]', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (id, code, name, description, permissions_json, is_system, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000003', 'MUSEUM_ADMIN', '文博管理员', '可管理本机构资产', '[]', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (id, code, name, description, permissions_json, is_system, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000004', 'SUPER_ADMIN', '超级管理员', '可管理全平台数据', '[]', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
