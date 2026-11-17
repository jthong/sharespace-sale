package com.cloudsteward.module.member.service.inventorygoods;

import com.cloudsteward.module.member.controller.app.inventory.vo.AppAddInventoryReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryGoodsSaveReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.InventoryGoodsSaveRespVO;
import com.cloudsteward.module.member.dal.dataobject.inventorygoods.InventoryGoodsDO;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;


import com.cloudsteward.module.member.dal.mysql.inventorygoods.InventoryGoodsMapper;

import java.util.List;

import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;


/**
 * 寄存商品信息 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class InventoryGoodsServiceImpl implements InventoryGoodsService {

    @Resource
    private InventoryGoodsMapper inventoryGoodsMapper;

    @Resource
    private StoreInfoService storeInfoService;

    @Override
    public List<InventoryGoodsSaveRespVO> getGoodsList(Long storeId) {
        storeInfoService.checkPermission(storeId, getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        return inventoryGoodsMapper.getGoodsList(storeId);

    }

    @Override
    @Transactional
    public void saveGoods(InventoryGoodsSaveReqVO reqVO) {
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        InventoryGoodsDO inventoryGoodsDO = new InventoryGoodsDO();
        inventoryGoodsDO.setGoodsName(reqVO.getGoodsName()).setUnits(reqVO.getUnits()).setStoreId(reqVO.getStoreId()).setEnable(reqVO.getEnable());
        if (ObjectUtils.isEmpty(reqVO.getId())) {
            //新增
            inventoryGoodsMapper.insert(inventoryGoodsDO);
        } else {
            //修改
            inventoryGoodsDO.setId(reqVO.getId());
            inventoryGoodsMapper.updateById(inventoryGoodsDO);
        }
    }

    @Override
    @Transactional
    public void deleteGoods(Long id) {
        InventoryGoodsDO inventoryGoodsDO = inventoryGoodsMapper.selectById(id);
        if (!ObjectUtils.isEmpty(inventoryGoodsDO)) {
            storeInfoService.checkPermission(inventoryGoodsDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
            inventoryGoodsMapper.deleteById(id);
        }
    }

}
