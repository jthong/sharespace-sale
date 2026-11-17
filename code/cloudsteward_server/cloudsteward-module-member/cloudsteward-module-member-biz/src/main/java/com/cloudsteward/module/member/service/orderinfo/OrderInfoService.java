package com.cloudsteward.module.member.service.orderinfo;

import java.util.*;
import javax.validation.*;
import com.cloudsteward.module.member.controller.admin.orderinfo.vo.*;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.framework.common.pojo.PageResult;

/**
 * 订单管理 Service 接口
 *
 * @author 超级管理员
 */
public interface OrderInfoService {

    /**
     * 删除订单管理
     *
     * @param id 编号
     */
    void deleteOrderInfo(Long id);


    /**
     * 获得订单管理分页
     *
     * @param pageReqVO 分页查询
     * @return 订单管理分页
     */
    PageResult<OrderInfoRespVO> getOrderInfoPage(OrderInfoPageReqVO pageReqVO);


}
