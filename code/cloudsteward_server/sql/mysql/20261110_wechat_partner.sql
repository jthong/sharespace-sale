-- Service-provider credentials belong in private application configuration, not these tables.
CREATE TABLE IF NOT EXISTS member_store_partner_merchant (
 store_id BIGINT NOT NULL PRIMARY KEY, sub_mch_id VARCHAR(32) NOT NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS member_wechat_partner_payment (
 order_no VARCHAR(64) NOT NULL PRIMARY KEY, out_trade_no VARCHAR(32) NOT NULL,
 store_id BIGINT NOT NULL, user_id BIGINT NOT NULL, tenant_id BIGINT NULL,
 sp_mchid VARCHAR(32) NOT NULL, sp_appid VARCHAR(32) NOT NULL, sub_mchid VARCHAR(32) NOT NULL,
 app_id VARCHAR(32) NOT NULL, payer_scope VARCHAR(8) NOT NULL, payer_open_id VARCHAR(128) NOT NULL,
 amount INT NOT NULL, prepay_id VARCHAR(128) NULL, transaction_id VARCHAR(64) NULL,
 state VARCHAR(16) NOT NULL DEFAULT 'PENDING', business_settled TINYINT NOT NULL DEFAULT 0,
 business_context LONGTEXT NULL, pre_order_context LONGTEXT NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_partner_trade_no (out_trade_no), UNIQUE KEY uk_partner_transaction (transaction_id)
);
CREATE TABLE IF NOT EXISTS member_wechat_partner_refund (
 out_refund_no VARCHAR(64) NOT NULL PRIMARY KEY, order_no VARCHAR(64) NOT NULL,
 refund_amount INT NOT NULL, refund_id VARCHAR(64) NULL, state VARCHAR(16) NOT NULL DEFAULT 'PENDING',
 applied TINYINT NOT NULL DEFAULT 0, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 KEY idx_partner_refund_order (order_no)
);
