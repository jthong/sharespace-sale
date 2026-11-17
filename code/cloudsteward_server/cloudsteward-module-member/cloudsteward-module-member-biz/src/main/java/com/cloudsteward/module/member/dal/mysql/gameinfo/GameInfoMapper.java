package com.cloudsteward.module.member.dal.mysql.gameinfo;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.game.vo.AppGameInfoRespVO;
import com.cloudsteward.module.member.controller.app.game.vo.AppGamePageReqVO;
import com.cloudsteward.module.member.dal.dataobject.gameinfo.GameInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 在线组局管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface GameInfoMapper extends BaseMapperX<GameInfoDO> {

    int countDayByUserId(Long userId);

    IPage<AppGameInfoRespVO> getOrderPage(@Param("page") IPage<AppGameInfoRespVO> page, @Param("reqVO") AppGamePageReqVO reqVO);
}
