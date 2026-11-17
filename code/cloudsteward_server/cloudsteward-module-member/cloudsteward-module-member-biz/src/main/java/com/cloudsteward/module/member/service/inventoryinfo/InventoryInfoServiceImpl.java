package com.cloudsteward.module.member.service.inventoryinfo;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;



import com.cloudsteward.module.member.dal.mysql.inventoryinfo.InventoryInfoMapper;


/**
 * 商品寄存单 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class InventoryInfoServiceImpl implements InventoryInfoService {

    @Resource
    private InventoryInfoMapper inventoryInfoMapper;



}
