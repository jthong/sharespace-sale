package com.cloudsteward.module.member.convert.deviceinfo;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import com.cloudsteward.module.member.controller.app.store.vo.AppAddDeviceReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.*;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;

/**
 * 设备管理 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface DeviceInfoConvert {

    DeviceInfoConvert INSTANCE = Mappers.getMapper(DeviceInfoConvert.class);

    DeviceInfoDO convert(DeviceInfoCreateReqVO bean);

    DeviceInfoDO convert(DeviceInfoUpdateReqVO bean);

    DeviceInfoRespVO convert(DeviceInfoDO bean);

    List<DeviceInfoRespVO> convertList(List<DeviceInfoDO> list);

    PageResult<DeviceInfoRespVO> convertPage(PageResult<DeviceInfoDO> page);

    List<DeviceInfoExcelVO> convertList02(List<DeviceInfoDO> list);

    DeviceInfoDO convert2(AppAddDeviceReqVO reqVO);

}
