package com.cloudsteward.module.member.dal.mysql.deviceinfo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.framework.tenant.core.aop.TenantIgnore;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoExportReqVO;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoPageReqVO;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoRespVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreDeviceRoomInfoPO;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.service.iot.device.IotDeviceRoomInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DeviceInfoMapper extends BaseMapperX<DeviceInfoDO> {

    default PageResult<DeviceInfoDO> selectPage(DeviceInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeviceInfoDO>().likeIfPresent(DeviceInfoDO::getDeviceSn, reqVO.getDeviceSn()).eqIfPresent(DeviceInfoDO::getType, reqVO.getType()).eqIfPresent(DeviceInfoDO::getRoomId, reqVO.getRoomId()).eqIfPresent(DeviceInfoDO::getStoreId, reqVO.getStoreId()).eqIfPresent(DeviceInfoDO::getStatus, reqVO.getStatus()).betweenIfPresent(DeviceInfoDO::getCreateTime, reqVO.getCreateTime()).orderByDesc(DeviceInfoDO::getDeviceId));
    }

    default List<DeviceInfoDO> selectList(DeviceInfoExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DeviceInfoDO>().likeIfPresent(DeviceInfoDO::getDeviceSn, reqVO.getDeviceSn()).eqIfPresent(DeviceInfoDO::getType, reqVO.getType()).eqIfPresent(DeviceInfoDO::getRoomId, reqVO.getRoomId()).eqIfPresent(DeviceInfoDO::getStoreId, reqVO.getStoreId()).eqIfPresent(DeviceInfoDO::getStatus, reqVO.getStatus()).betweenIfPresent(DeviceInfoDO::getCreateTime, reqVO.getCreateTime()).orderByDesc(DeviceInfoDO::getDeviceId));
    }


    List<DeviceInfoDO> getByRoomIdAndType(@Param("roomId") Long roomId, @Param("type") Integer[] type);
    List<DeviceInfoDO> getByStoreIdAndType(@Param("storeId") Long storeId, @Param("type") Integer[] type);

    String getSnByStoreIdAndType(@Param("storeId") Long storeId, @Param("type") Integer type);
    String getSnByRoomIdAndType(@Param("roomId") Long roomId, @Param("type") Integer type);


    List<DeviceInfoDO> getByRoomId(@Param("roomId") Long roomId);

    IPage<DeviceInfoRespVO> getDeviceInfoPage(@Param("page") IPage<DeviceInfoRespVO> page, @Param("reqVO") DeviceInfoPageReqVO reqVO
            , @Param("userId") Long userId, @Param("isAdmin") boolean isAdmin);

    int updateStatusBySN(@Param("deviceSn") String deviceSn, @Param("state") Integer state);

    int updateInfoBySN(@Param("deviceSn") String deviceSn, @Param("status") Integer status, @Param("rssi") Integer rssi, @Param("cversion") String cversion, @Param("deviceData") String deviceData);

    int countGateway(Long storeId);

    @TenantIgnore
    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true")
    int countBySN(String deviceSn);

    int updateBindInfo(@Param("deviceId") Long deviceId, @Param("roomId") Long roomId);

    IotDeviceRoomInfoVO getDeviceRoomVO(@Param("deviceSn") String deviceSn);

    List<IotDeviceRoomInfoVO> getStoreVoice(Long storeId);

    Long getTenantBySN(String deviceSn);

    int countByTypeAndRoomId(@Param("deviceType") Integer deviceType, @Param("storeId") Long storeId,@Param("roomId") Long roomId);

    int countKongtiao(Long roomId);

    int deleteStoreId(Long id);

    List<DeviceInfoDO> getIRListByRoomId(Long roomId);

    int countShare(String deviceSn);

    List<AppStoreDeviceRoomInfoPO> getLockBySn(@Param("deviceSnList") List<String> deviceSnList);

    List<DeviceInfoDO> getBySn(String sn);

    int setDeviceData(@Param("sn") String sn, @Param("json") String json);

}
