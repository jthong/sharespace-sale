package com.cloudsteward.module.member.dal.mysql.inventorygoods;


import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryGoodsSaveRespVO;
import com.cloudsteward.module.member.dal.dataobject.inventorygoods.InventoryGoodsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 寄存商品信息 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface InventoryGoodsMapper extends BaseMapperX<InventoryGoodsDO> {

    List<InventoryGoodsSaveRespVO> getGoodsList(Long storeId);

}
