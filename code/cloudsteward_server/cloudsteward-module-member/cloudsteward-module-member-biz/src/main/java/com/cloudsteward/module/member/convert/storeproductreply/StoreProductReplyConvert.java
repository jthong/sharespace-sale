package com.cloudsteward.module.member.convert.storeproductreply;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.storeproductreply.vo.StoreProductReplyRespVO;
import com.cloudsteward.module.member.controller.app.storeproductreply.vo.StoreProductReplyUpdateReqVO;
import com.cloudsteward.module.member.dal.dataobject.storeproductreply.StoreProductReplyDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 评论 Convert
 *
 * @author yshop
 */
@Mapper
public interface StoreProductReplyConvert {

    StoreProductReplyConvert INSTANCE = Mappers.getMapper(StoreProductReplyConvert.class);


    StoreProductReplyDO convert(StoreProductReplyUpdateReqVO bean);

    StoreProductReplyRespVO convert(StoreProductReplyDO bean);

    List<StoreProductReplyRespVO> convertList(List<StoreProductReplyDO> list);

    PageResult<StoreProductReplyRespVO> convertPage(PageResult<StoreProductReplyDO> page);


}
