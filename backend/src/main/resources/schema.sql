CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  organization VARCHAR(128),
  role_code VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS assets (
  id BIGINT PRIMARY KEY,
  asset_name VARCHAR(128) NOT NULL,
  asset_type VARCHAR(32) NOT NULL,
  creator VARCHAR(128),
  organization VARCHAR(128),
  description TEXT,
  file_hash VARCHAR(128) NOT NULL UNIQUE,
  cid VARCHAR(128),
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS evidence_records (
  id BIGINT PRIMARY KEY,
  asset_id BIGINT NOT NULL,
  file_hash VARCHAR(128) NOT NULL UNIQUE,
  owner_name VARCHAR(128) NOT NULL,
  timestamp_iso VARCHAR(64) NOT NULL,
  chain_network VARCHAR(128) NOT NULL,
  contract_name VARCHAR(64) NOT NULL,
  tx_id VARCHAR(128) NOT NULL UNIQUE,
  block_height BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS chain_transactions (
  id BIGINT PRIMARY KEY,
  tx_id VARCHAR(128) NOT NULL UNIQUE,
  file_hash VARCHAR(128) NOT NULL,
  contract_name VARCHAR(64) NOT NULL,
  method_name VARCHAR(64) NOT NULL,
  request_payload TEXT NOT NULL,
  response_payload TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY,
  action VARCHAR(64) NOT NULL,
  operator_name VARCHAR(128) NOT NULL,
  target_hash VARCHAR(128),
  detail TEXT,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_assets_hash ON assets(file_hash);
CREATE INDEX IF NOT EXISTS idx_evidence_hash ON evidence_records(file_hash);
CREATE INDEX IF NOT EXISTS idx_audit_hash ON audit_logs(target_hash);
