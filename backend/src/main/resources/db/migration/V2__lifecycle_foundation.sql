-- 非破坏性扩展：保留原型接口字段，新增生命周期表和未来切换所需列。
CREATE TABLE roles (
  id VARCHAR(36) PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(500),
  permissions_json TEXT NOT NULL,
  is_system BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN display_name VARCHAR(128);
ALTER TABLE users ADD COLUMN email VARCHAR(254);
ALTER TABLE users ADD COLUMN status VARCHAR(32);
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE assets ADD COLUMN asset_code VARCHAR(64);
ALTER TABLE assets ADD COLUMN owner_user_id BIGINT;
ALTER TABLE assets ADD COLUMN metadata_json TEXT;
ALTER TABLE assets ADD COLUMN current_version_id VARCHAR(36);
ALTER TABLE assets ADD COLUMN visibility VARCHAR(32);
ALTER TABLE assets ADD COLUMN copyright_expires_at TIMESTAMP;
ALTER TABLE assets ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE assets ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE assets ADD CONSTRAINT fk_assets_owner FOREIGN KEY (owner_user_id) REFERENCES users(id);

CREATE TABLE asset_versions (
  id VARCHAR(36) PRIMARY KEY,
  asset_id BIGINT NOT NULL,
  version_no INTEGER NOT NULL,
  content_sha256 VARCHAR(64) NOT NULL,
  original_filename VARCHAR(512) NOT NULL,
  mime_type VARCHAR(127) NOT NULL,
  file_size_bytes BIGINT NOT NULL,
  storage_status VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  submitted_by_user_id BIGINT,
  change_note VARCHAR(1000),
  metadata_snapshot TEXT,
  immutable_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT uq_asset_versions_no UNIQUE (asset_id, version_no),
  CONSTRAINT uq_asset_versions_hash UNIQUE (asset_id, content_sha256),
  CONSTRAINT fk_asset_versions_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_asset_versions_submitter FOREIGN KEY (submitted_by_user_id) REFERENCES users(id)
);

ALTER TABLE assets ADD CONSTRAINT fk_assets_current_version FOREIGN KEY (current_version_id) REFERENCES asset_versions(id);

CREATE TABLE ipfs_files (
  id VARCHAR(36) PRIMARY KEY,
  asset_version_id VARCHAR(36) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  mode VARCHAR(16) NOT NULL,
  cid VARCHAR(255),
  gateway_url VARCHAR(2048),
  content_sha256 VARCHAR(64) NOT NULL,
  size_bytes BIGINT NOT NULL,
  pin_status VARCHAR(32) NOT NULL,
  provider_request_id VARCHAR(255),
  error_code VARCHAR(64),
  error_message VARCHAR(1000),
  pinned_at TIMESTAMP,
  verified_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT uq_ipfs_provider_cid UNIQUE (provider, cid),
  CONSTRAINT fk_ipfs_files_version FOREIGN KEY (asset_version_id) REFERENCES asset_versions(id)
);

CREATE INDEX idx_assets_owner_status ON assets(owner_user_id, status);
CREATE INDEX idx_asset_versions_asset_status ON asset_versions(asset_id, status);
CREATE INDEX idx_ipfs_files_version_status ON ipfs_files(asset_version_id, pin_status);
