package com.cloudsteward.module.member.dal.mysql.inventorydetail;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppInventoryPageReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppUserInventoryDetailReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppUserInventoryDetailRespVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryDetailRespVO;
import com.cloudsteward.module.member.dal.dataobject.inventorydetail.InventoryDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品寄存单详细 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface InventoryDetailMapper extends BaseMapperX<InventoryDetailDO> {

    List<AppUserInventoryDetailRespVO> getUserInventoryPage(@Param("userId") Long userId, @Param("storeId") Long storeId);


    List<InventoryDetailRespVO> getInventoryList(AppInventoryPageReqVO reqVO);

}
