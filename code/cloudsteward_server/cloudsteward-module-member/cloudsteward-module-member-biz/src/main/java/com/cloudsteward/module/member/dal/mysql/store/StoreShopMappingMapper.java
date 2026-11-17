package com.cloudsteward.module.member.dal.mysql.store;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.dal.dataobject.store.StoreShopMappingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店-店铺映射 Mapper
 *
 * @author MrGuan
 */
@Mapper
public interface StoreShopMappingMapper extends BaseMapperX<StoreShopMappingDO> {

    default StoreShopMappingDO selectByStoreIdAndPlatform(Long storeId, Integer platform) {
        return selectOne(StoreShopMappingDO::getStoreId, storeId,
                         StoreShopMappingDO::getPlatform, platform);
    }

}
