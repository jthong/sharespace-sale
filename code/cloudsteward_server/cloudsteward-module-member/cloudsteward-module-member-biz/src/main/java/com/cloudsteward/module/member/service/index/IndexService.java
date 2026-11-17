package com.cloudsteward.module.member.service.index;

import com.alibaba.fastjson.JSONArray;
import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.index.vo.AppConfigReqVO;
import com.cloudsteward.module.member.controller.app.index.vo.*;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPaySelectByPhoneReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPaySelectByPhoneRespVO;

import java.util.List;

public interface IndexService {
    List<String> getCityList();

    List<AppBannerInfoRespVO> getBannerList();

    PageResult<AppStorePageRespVO> getStorePageList(AppStorePageReqVO reqVO);

    AppIndexStoreInfoRespVO getStoreInfo(Long storeId,String lat,String lon);

    List<KeyValue<String, Long>> getStoreList(String name,String cityName);

    List<KeyValue<String, Long>> getRoomList(Long storeId);

    List<KeyValue<String, Long>> getRoomListByAdmin(Long storeId);

    List<AppRoomInfoListRespVO> getRoomInfoList(AppRoomListReqVO reqVO);

    AppRoomInfoListRespVO getRoomInfo(Long roomId);

    AppSysInfoRespVO getSysInfo();

    List<IotGroupPaySelectByPhoneRespVO> selectGroupPayByPhone(IotGroupPaySelectByPhoneReqVO reqVo);

    AppConfigReqVO getAppConfig();


    void saveAppConfig(AppConfigReqVO reqVO);


    JSONArray getRegCode();


    JSONArray getBankCode();

}
