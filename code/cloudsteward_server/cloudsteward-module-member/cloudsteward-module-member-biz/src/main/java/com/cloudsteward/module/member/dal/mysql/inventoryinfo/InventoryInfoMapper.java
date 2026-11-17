package com.cloudsteward.module.member.dal.mysql.inventoryinfo;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppInventoryPageReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.AppInventoryPageRespVO;
import com.cloudsteward.module.member.dal.dataobject.inventoryinfo.InventoryInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品寄存单 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface InventoryInfoMapper extends BaseMapperX<InventoryInfoDO> {


}
