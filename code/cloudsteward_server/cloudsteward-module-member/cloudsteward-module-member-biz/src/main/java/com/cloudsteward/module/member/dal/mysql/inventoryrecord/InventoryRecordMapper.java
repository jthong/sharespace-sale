package com.cloudsteward.module.member.dal.mysql.inventoryrecord;


import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppInventoryPageReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryDetailRespVO;
import com.cloudsteward.module.member.dal.dataobject.inventoryrecord.InventoryRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 寄存商品存取记录 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface InventoryRecordMapper extends BaseMapperX<InventoryRecordDO> {

    List<InventoryDetailRespVO> getInventoryList(AppInventoryPageReqVO reqVO);

}
