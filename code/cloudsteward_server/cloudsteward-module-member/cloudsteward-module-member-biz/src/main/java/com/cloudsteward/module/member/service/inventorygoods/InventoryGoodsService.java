package com.cloudsteward.module.member.service.inventorygoods;


import com.cloudsteward.module.member.controller.app.inventory.vo.AppAddInventoryReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryGoodsSaveReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryGoodsSaveRespVO;

import java.util.List;

/**
 * 寄存商品信息 Service 接口
 *
 * @author 超级管理员
 */
public interface InventoryGoodsService {

    List<InventoryGoodsSaveRespVO> getGoodsList(Long storeId);

    void saveGoods(InventoryGoodsSaveReqVO reqVO);


    void deleteGoods(Long id);



}
