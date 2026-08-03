-- 授权记录只追加状态事实，不物理删除；链交易沿用既有 chain_transactions 表。
CREATE TABLE authorizations (
  id VARCHAR(36) PRIMARY KEY,
  authorization_no VARCHAR(64) NOT NULL UNIQUE,
  asset_id BIGINT NOT NULL,
  asset_version_id VARCHAR(36),
  licensor_user_id BIGINT NOT NULL,
  licensee_name VARCHAR(255) NOT NULL,
  licensee_organization VARCHAR(255),
  usage_type VARCHAR(32) NOT NULL,
  commercial BOOLEAN NOT NULL,
  revenue_share_ratio DECIMAL(5,2) NOT NULL,
  starts_at TIMESTAMP NOT NULL,
  ends_at TIMESTAMP NOT NULL,
  status VARCHAR(32) NOT NULL,
  chain_transaction_id BIGINT,
  chain_tx_id VARCHAR(128),
  chain_mode VARCHAR(16),
  revoked_at TIMESTAMP,
  revoke_reason VARCHAR(1000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT ck_authorization_period CHECK (ends_at > starts_at),
  CONSTRAINT ck_authorization_share CHECK (revenue_share_ratio >= 0 AND revenue_share_ratio <= 100),
  CONSTRAINT fk_authorizations_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_authorizations_version FOREIGN KEY (asset_version_id) REFERENCES asset_versions(id),
  CONSTRAINT fk_authorizations_licensor FOREIGN KEY (licensor_user_id) REFERENCES users(id),
  CONSTRAINT fk_authorizations_chain_transaction FOREIGN KEY (chain_transaction_id) REFERENCES chain_transactions(id)
);

CREATE INDEX idx_authorizations_asset_status_period ON authorizations(asset_id, status, starts_at, ends_at);
CREATE INDEX idx_authorizations_asset_version ON authorizations(asset_version_id);
