ALTER TABLE chain_transactions ADD COLUMN asset_version_id VARCHAR(36);
ALTER TABLE chain_transactions ADD COLUMN provider VARCHAR(64);
ALTER TABLE chain_transactions ADD COLUMN mode VARCHAR(16);
ALTER TABLE chain_transactions ADD COLUMN network VARCHAR(128);
ALTER TABLE chain_transactions ADD COLUMN idempotency_key VARCHAR(128);
ALTER TABLE chain_transactions ADD COLUMN request_digest VARCHAR(64);
ALTER TABLE chain_transactions ADD COLUMN error_code VARCHAR(64);
ALTER TABLE chain_transactions ADD COLUMN error_message VARCHAR(1000);
ALTER TABLE chain_transactions ADD COLUMN attempt_no INTEGER DEFAULT 1;
ALTER TABLE chain_transactions ADD COLUMN confirmed_at TIMESTAMP;
ALTER TABLE chain_transactions ADD COLUMN chain_block_height BIGINT;
ALTER TABLE chain_transactions ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE chain_transactions ADD CONSTRAINT fk_chain_transaction_version FOREIGN KEY (asset_version_id) REFERENCES asset_versions(id);
ALTER TABLE chain_transactions ADD CONSTRAINT uq_chain_idempotency UNIQUE (idempotency_key);
ALTER TABLE assets ADD CONSTRAINT uq_assets_asset_code UNIQUE (asset_code);

CREATE INDEX idx_chain_transactions_version_status ON chain_transactions(asset_version_id, status);
CREATE INDEX idx_ipfs_files_cid ON ipfs_files(cid);
