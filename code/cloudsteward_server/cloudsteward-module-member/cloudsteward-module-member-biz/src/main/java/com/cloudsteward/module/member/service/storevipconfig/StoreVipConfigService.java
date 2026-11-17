package com.cloudsteward.module.member.service.storevipconfig;


import com.cloudsteward.module.member.controller.app.store.vo.AppAddMemberVipReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppEditMemberVipReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreVipConfigListRespVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreVipConfigSaveReqVO;

import java.util.List;

/**
 * 门店会员配置 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreVipConfigService {

    List<AppStoreVipConfigListRespVO> getVipConfig(Long storeId);

    void saveVipConfig(AppStoreVipConfigSaveReqVO reqVO);

    void editMemberVip(AppEditMemberVipReqVO reqVO);


    void addMemberVip(AppAddMemberVipReqVO reqVO);

    void deleteVipConfig(Long vipId);

}
