package com.cloudsteward.module.member.dal.mysql.storetemplate;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.store.vo.AppTemplateInfoRespVO;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloudsteward.module.member.controller.admin.storetemplate.vo.*;

/**
 * 门店模板 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreTemplateMapper extends BaseMapperX<StoreTemplateDO> {

    default PageResult<StoreTemplateDO> selectPage(StoreTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreTemplateDO>()
                .likeIfPresent(StoreTemplateDO::getTemplateName, reqVO.getTemplateName())
                .likeIfPresent(StoreTemplateDO::getTemplateKey, reqVO.getTemplateKey())
                .betweenIfPresent(StoreTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StoreTemplateDO::getTemplateId));
    }

    default List<StoreTemplateDO> selectList(StoreTemplateExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<StoreTemplateDO>()
                .likeIfPresent(StoreTemplateDO::getTemplateName, reqVO.getTemplateName())
                .likeIfPresent(StoreTemplateDO::getTemplateKey, reqVO.getTemplateKey())
                .betweenIfPresent(StoreTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StoreTemplateDO::getTemplateId));
    }

    String getImgByKey(String templateKey);

    StoreTemplateDO selectByKey(String templateKey);

    List<AppTemplateInfoRespVO> getTemplateList();



}
