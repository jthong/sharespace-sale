-- Preflight: resolve duplicate active rows before applying these indexes.
-- Both platform rows carry the same Yunlaoban shop ID for one local store.
ALTER TABLE member_store_shop_mapping
    ADD UNIQUE KEY uk_group_store_platform (store_id, platform, deleted),
    ADD UNIQUE KEY uk_group_platform_shop (platform, shop_id, deleted);
