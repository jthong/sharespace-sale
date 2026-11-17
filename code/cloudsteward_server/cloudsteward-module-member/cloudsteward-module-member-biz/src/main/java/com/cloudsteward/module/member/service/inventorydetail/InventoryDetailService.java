package com.cloudsteward.module.member.service.inventorydetail;


import com.cloudsteward.module.member.controller.app.inventory.vo.*;

import java.util.List;

/**
 * 商品寄存单详细 Service 接口
 *
 * @author 超级管理员
 */
public interface InventoryDetailService {

    List<AppUserInventoryDetailRespVO> getUserInventoryPage(AppUserInventoryDetailReqVO reqVO);

    void addInventory(AppAddInventoryReqVO reqVO);

    void takeInventory(AppTakeInventoryReqVO reqVO);

    List<AppInventoryPageRespVO> getInventoryList(AppInventoryPageReqVO reqVO);



}
