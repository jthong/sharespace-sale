package com.cloudsteward.module.member.service.groupPay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.module.member.dal.dataobject.store.StoreShopMappingDO;
import com.cloudsteward.module.member.dal.mysql.store.StoreShopMappingMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

/** One Yunlaoban shop per local store; platform rows share that same shop ID. */
@Service
public class GroupBuyStoreBindingService {
    private static final ErrorCode INVALID = new ErrorCode(1004999002, "{}");
    @Resource private StoreInfoMapper stores;
    @Resource private StoreShopMappingMapper mappings;
    @Resource private JdbcTemplate jdbc;

    private void requireStore(Long storeId) {
        // This mapper is tenant-scoped for PC merchant accounts.
        if (storeId == null || stores.selectById(storeId) == null) {
            throw exception(INVALID, "门店不存在或无权访问");
        }
    }

    public String getShopId(Long storeId) {
        requireStore(storeId);
        List<StoreShopMappingDO> rows = mappings.selectList(new LambdaQueryWrapper<StoreShopMappingDO>()
                .eq(StoreShopMappingDO::getStoreId, storeId));
        String shopId = null;
        for (StoreShopMappingDO row : rows) {
            if (row.getShopId() == null || row.getShopId().trim().isEmpty()) continue;
            if (shopId != null && !Objects.equals(shopId, row.getShopId())) {
                throw exception(INVALID, "当前门店的团购绑定不一致，请重新绑定团购服务商门店 ID");
            }
            shopId = row.getShopId();
        }
        return shopId;
    }

    public String requireShopId(Long storeId) {
        String shopId = getShopId(storeId);
        if (shopId == null) throw exception(INVALID, "请先在门店管理中绑定团购服务商门店 ID");
        return shopId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void bind(Long storeId, String value) {
        requireStore(storeId);
        String shopId = value == null ? "" : value.trim();
        if (!shopId.matches("[1-9][0-9]{0,63}")) {
            throw exception(INVALID, "请输入 1 至 64 位数字的团购服务商门店 ID，不能以 0 开头");
        }
        // Serialize changes to the same store; DB unique indexes arbitrate concurrent stores.
        jdbc.queryForObject("SELECT store_id FROM member_store_info WHERE store_id=? AND deleted=0 FOR UPDATE", Long.class, storeId);
        Integer occupied = jdbc.queryForObject("SELECT COUNT(*) FROM member_store_shop_mapping WHERE shop_id=? AND store_id<>? AND deleted=0", Integer.class, shopId, storeId);
        if (occupied != null && occupied > 0) throw exception(INVALID, "该团购服务商门店 ID 已绑定其他系统门店，不能重复绑定");
        String previous = getShopId(storeId);
        if (previous != null && !previous.equals(shopId)
                && jdbc.queryForObject("SELECT COUNT(*) FROM member_group_pay_info WHERE store_id=? AND deleted=0", Integer.class, storeId) > 0) {
            throw exception(INVALID, "门店已有团购核销记录，暂不支持更换团购门店 ID");
        }
        try {
            List<StoreShopMappingDO> existing = mappings.selectList(new LambdaQueryWrapper<StoreShopMappingDO>()
                    .eq(StoreShopMappingDO::getStoreId, storeId));
            for (StoreShopMappingDO row : existing) {
                if (!shopId.equals(row.getShopId())) {
                    row.setShopId(shopId);
                    mappings.updateById(row);
                }
            }
            for (int platform : new int[]{1, 2}) {
                StoreShopMappingDO row = mappings.selectByStoreIdAndPlatform(storeId, platform);
                if (row == null) {
                    mappings.insert(StoreShopMappingDO.builder().storeId(storeId).platform(platform).shopId(shopId).build());
                } else if (!shopId.equals(row.getShopId())) {
                    row.setShopId(shopId);
                    mappings.updateById(row);
                }
            }
        } catch (DuplicateKeyException ex) {
            throw exception(INVALID, "门店或团购门店 ID 已被绑定，请刷新后重试");
        }
    }
}
