package com.cloudsteward.module.member.dal.mysql.storesound;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreSoundInfoRespVO;
import com.cloudsteward.module.member.dal.dataobject.storesound.StoreSoundInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoreSoundInfoMapper extends BaseMapperX<StoreSoundInfoDO> {

    StoreSoundInfoDO getByStoreId(@Param("storeId") Long storeId, @Param("roomClass") Integer roomClass);
    StoreSoundInfoDO getByRoomId(@Param("storeId") Long storeId, @Param("roomId") Long roomId);

    AppStoreSoundInfoRespVO getStoreSoundInfo(@Param("storeId") Long storeId, @Param("roomClass") Integer roomClass);
}
