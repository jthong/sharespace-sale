package com.cloudsteward.module.member.service.inventoryrecord;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;


import com.cloudsteward.module.member.dal.mysql.inventoryrecord.InventoryRecordMapper;


/**
 * 寄存商品存取记录 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class InventoryRecordServiceImpl implements InventoryRecordService {

    @Resource
    private InventoryRecordMapper inventoryRecordMapper;


}
