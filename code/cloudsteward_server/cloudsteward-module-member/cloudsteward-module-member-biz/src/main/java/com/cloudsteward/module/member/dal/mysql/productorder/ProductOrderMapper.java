package com.cloudsteward.module.member.dal.mysql.productorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.tenant.core.aop.TenantIgnore;
import com.cloudsteward.module.member.controller.app.productorder.vo.AppHaveOrderStoreRespVO;
import com.cloudsteward.module.member.controller.app.productorder.vo.AppUserOrderPageReqVO;
import com.cloudsteward.module.member.controller.app.productorder.vo.AppUserOrderPageRespVO;
import com.cloudsteward.module.member.dal.dataobject.productorder.ProductOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品订单 Mapper
 *
 * @author
 */
@Mapper
public interface ProductOrderMapper extends BaseMapperX<ProductOrderDO> {

    @TenantIgnore
    ProductOrderDO selectByOrderNo(String orderNo);

    void updateByOrderNo(String orderNo);

    List<AppHaveOrderStoreRespVO> selectHaveOrderStore(Long userId);

    String getPhone(Long orderId);

    IPage<AppUserOrderPageRespVO> userOrderByPage(@Param("page") IPage<AppUserOrderPageRespVO> page, @Param("reqVO") AppUserOrderPageReqVO reqVO
            , @Param("userId") Long userId, @Param("storeId") Long storeId);
}
