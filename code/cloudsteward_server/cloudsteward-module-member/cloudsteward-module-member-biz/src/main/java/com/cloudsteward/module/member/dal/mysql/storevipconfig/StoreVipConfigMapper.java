package com.cloudsteward.module.member.dal.mysql.storevipconfig;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreVipConfigListRespVO;
import com.cloudsteward.module.member.dal.dataobject.storevipconfig.StoreVipConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 门店会员配置 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreVipConfigMapper extends BaseMapperX<StoreVipConfigDO> {


    List<AppStoreVipConfigListRespVO> getVipConfig(Long storeId);

    int deleteByStoreId(Long storeId);

    default List<StoreVipConfigDO> selectbyStoreId(Long storeId) {
        return selectList(StoreVipConfigDO::getStoreId, storeId);
    }

    StoreVipConfigDO getByStoreIdAndLevel(@Param("storeId") Long storeId, @Param("vipLevel") Byte vipLevel);

    int countVipByLevel(@Param("storeId") Long storeId, @Param("vipLevel") Integer vipLevel);

}
