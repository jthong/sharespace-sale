-- Apply once before deploying code that reads gateway_sn.
-- The deployment checks information_schema before executing this statement.
ALTER TABLE member_device_info
    ADD COLUMN gateway_sn varchar(64) NULL COMMENT '网关同步发现的子设备所属网关编号';
-- Existing children are backfilled by the authenticated gateway sync endpoint.
