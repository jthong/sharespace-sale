package com.cloudsteward.module.member.service.game;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.game.vo.AppGameInfoReqVO;
import com.cloudsteward.module.member.controller.app.game.vo.AppGameInfoRespVO;
import com.cloudsteward.module.member.controller.app.game.vo.AppGamePageReqVO;

public interface AppGameService {
    void save(AppGameInfoReqVO reqVO);

    PageResult<AppGameInfoRespVO> getOrderPage(AppGamePageReqVO reqVO);

    void deleteUser(Long gameId, Long userId);

    void join(Long gameId);
}
