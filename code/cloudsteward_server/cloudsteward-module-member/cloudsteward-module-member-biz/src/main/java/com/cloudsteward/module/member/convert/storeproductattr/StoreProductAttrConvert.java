package com.cloudsteward.module.member.convert.storeproductattr;

import com.cloudsteward.module.member.controller.app.storeproduct.vo.AppStoreProductAttrQueryVo;
import com.cloudsteward.module.member.dal.dataobject.storeproductattr.StoreProductAttrDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 商品属性 Convert
 *
 * @author yshop
 */
@Mapper
public interface StoreProductAttrConvert {

    StoreProductAttrConvert INSTANCE = Mappers.getMapper(StoreProductAttrConvert.class);

    AppStoreProductAttrQueryVo convert(StoreProductAttrDO bean);
    

}
