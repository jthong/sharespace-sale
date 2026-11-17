package com.cloudsteward.module.member.service.clear;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.clear.vo.*;

public interface AppClearService {
    PageResult<AppClearPageRespVO> getClearPage(AppClearPageReqVO reqVO);

    void changeStatus(Long id, Integer status);

    void openStoreDoor(Long id);

    void openRoomDoor(Long id);

    AppClearInfoRespVO getDetail(Long clearId);

    AppClearChartRespVO getChartData();

    PageResult<AppClearBillRespVO> getClearBillPage(AppClearBillReqVO reqVO);

    void finish(AppStartClearReqVO reqVO);

    // getLockPwd 已下线（新IoT平台不支持）
    // String getLockPwd(Long id);

    void opRoom(AppClearOpRoomReqVO reqVO);

}
