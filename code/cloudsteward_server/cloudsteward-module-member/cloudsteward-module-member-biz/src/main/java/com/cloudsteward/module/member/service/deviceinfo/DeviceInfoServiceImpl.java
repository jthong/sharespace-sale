package com.cloudsteward.module.member.service.deviceinfo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.*;
import com.cloudsteward.module.member.convert.deviceinfo.DeviceInfoConvert;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;
import com.baomidou.lock.annotation.Lock4j;
import java.util.Objects;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.iot.device.IotResult;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 设备管理 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private IotService iotService;

    @Resource
    private StoreInfoService storeInfoService;

    @Resource
    private StoreInfoMapper storeInfoMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;


    @Override
    @Transactional
    public void createDeviceInfo(DeviceInfoCreateReqVO createReqVO) {
        StoreInfoDO store = storeInfoMapper.selectById(createReqVO.getStoreId());
        if (store == null) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (Objects.equals(createReqVO.getType(), AppEnum.device_type.CLOUDSTEWARD_GATEWAY.getValue())) {
            createReqVO.setRoomId(null);
            createReqVO.setShare(false);
        }
        validateRoomInStore(createReqVO.getStoreId(), createReqVO.getRoomId());
        //如果不是共用设备  那新增的设备不能存在
        if (!Boolean.TRUE.equals(createReqVO.getShare())) {
            int i = deviceInfoMapper.countBySN(createReqVO.getDeviceSn());
            if (i > 0) {
                throw exception(DEVICE_DATA_EXISTS_ERROR);
            }
        }
        checkMaxNum(createReqVO.getStoreId(),createReqVO.getRoomId(),createReqVO.getType());
        //设备已在新平台预绑定
        String data = createReqVO.getDeviceSn();
        // 插入
        DeviceInfoDO deviceInfo = DeviceInfoConvert.INSTANCE.convert(createReqVO);
        deviceInfo.setDeviceData(data);
        deviceInfo.setTenantId(store.getTenantId());
        deviceInfoMapper.insert(deviceInfo);

    }

    public void checkMaxNum(Long storeId, Long roomId, Integer type) {
        //门禁=1  空开=2  云喇叭=3  灯具=4 密码锁=5 网关=6  插座=7
        //锁球器控制器（12V)=8 人脸门禁机=9 智能语音喇叭=10 二维码识别器=11
        //红外控制器=12 三路控制器=13 AI锁球器=14 计时器=16
        if (ObjectUtils.isEmpty(roomId)) {
            //绑定到门店的
            switch (type) {
                case 5:
                    int c = deviceInfoMapper.countByTypeAndRoomId(type, storeId, roomId);
                    if (c > 0) {
                        throw exception(DEVICE_ADD_MAX_NUM_ERROR);
                    }
                    break;
            }
        } else {
            //绑定到房间的
            switch (type) {
                case 5:
                case 8:
                case 13:
                case 14:
                case 16:
                    int c = deviceInfoMapper.countByTypeAndRoomId(type, storeId, roomId);
                    if (c > 0) {
                        throw exception(DEVICE_ADD_MAX_NUM_ERROR);
                    }
                    break;
            }
        }

    }


    @Override
    @Transactional
    public void deleteDeviceInfo(Long id) {
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(id);
        //后台管理员是随便删
        if (!ObjectUtils.isEmpty(deviceInfoDO)) {
            //设备已在新平台预绑定，解绑由平台处理，本地直接删除
            // 删除
            deviceInfoMapper.deleteById(id);
        }
    }

    private void validateDeviceInfoExists(Long id) {
        if (deviceInfoMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public DeviceInfoDO getDeviceInfo(Long id) {
        return deviceInfoMapper.selectById(id);
    }

    @Override
    public List<DeviceInfoDO> getDeviceInfoList(Collection<Long> ids) {
        return deviceInfoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<DeviceInfoRespVO> getDeviceInfoPage(DeviceInfoPageReqVO reqVO, boolean isAdmin) {
        //检查权限
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        IPage<DeviceInfoRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        deviceInfoMapper.getDeviceInfoPage(page, reqVO, getLoginUserId(), isAdmin);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<DeviceInfoDO> getDeviceInfoList(DeviceInfoExportReqVO exportReqVO) {
        return deviceInfoMapper.selectList(exportReqVO);
    }

    @Override
    @Transactional
    public void bind(DeviceInfoBindReqVO reqVO) {
        //如果设备已经被绑定了， 就更新绑定关系
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(reqVO.getDeviceId());
        if (ObjectUtils.isEmpty(deviceInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (Objects.equals(deviceInfoDO.getType(), AppEnum.device_type.CLOUDSTEWARD_GATEWAY.getValue())) {
            throw exception(DEVICE_OPRATION_ERROR);
        }
        validateRoomInStore(deviceInfoDO.getStoreId(), reqVO.getRoomId());
        //有的设备每个房间只能存在一个
        checkMaxNum(deviceInfoDO.getStoreId(),reqVO.getRoomId(),deviceInfoDO.getType());
        deviceInfoMapper.updateBindInfo(deviceInfoDO.getDeviceId(), reqVO.getRoomId());
    }

    private void validateRoomInStore(Long storeId, Long roomId) {
        if (roomId == null) return;
        RoomInfoDO room = roomInfoMapper.selectById(roomId);
        if (room == null || !Objects.equals(room.getStoreId(), storeId)) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public void pairGateway(Long deviceId) {
        DeviceInfoDO gateway = requireGateway(deviceId);
        if (!Boolean.TRUE.equals(iotService.pairGateway(gateway.getDeviceSn()))) {
            throw exception(DEVICE_OPRATION_ERROR);
        }
    }

    private DeviceInfoDO requireGateway(Long deviceId) {
        DeviceInfoDO gateway = deviceInfoMapper.selectById(deviceId);
        if (gateway == null || gateway.getStoreId() == null
                || storeInfoMapper.selectById(gateway.getStoreId()) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (!Objects.equals(gateway.getType(), AppEnum.device_type.CLOUDSTEWARD_GATEWAY.getValue())) {
            throw exception(DEVICE_OPRATION_ERROR);
        }
        return gateway;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(expire = 180000, acquireTimeout = 1000)
    public DeviceInfoSyncRespVO syncSubDevices(Long deviceId) {
        DeviceInfoDO gateway = requireGateway(deviceId);
        // Tenant-scoped selection above plus controller permissions authorize the action.
        // Do not restrict it to the creator: other authorized merchant staff must also work.
        IotResult<String> result = iotService.getSubDevices(gateway.getDeviceSn());
        if (result == null || !Integer.valueOf(0).equals(result.getCode()) || result.getData() == null) {
            throw exception(DEVICE_IOT_OP_ERROR, result == null ? "网关未返回数据" : result.getMsg());
        }
        List<SubDeviceParser.Device> children;
        try {
            children = SubDeviceParser.parse(result.getData());
        } catch (IllegalArgumentException e) {
            throw exception(DEVICE_IOT_OP_ERROR, "子设备数据格式错误，请检查网关返回值");
        }
        DeviceInfoSyncRespVO response = new DeviceInfoSyncRespVO();
        response.setDiscovered(children.size());
        for (SubDeviceParser.Device child : children) {
            // 1800 is the coordinator's virtual device, not a second deletable gateway.
            if (Objects.equals(child.getSn(), gateway.getDeviceSn())
                    || Integer.valueOf(1800).equals(child.getUiid())) {
                response.setSkipped(response.getSkipped() + 1);
                continue;
            }
            if (child.getType() == null) {
                response.setSkipped(response.getSkipped() + 1);
                String uiid = String.valueOf(child.getUiid());
                if (!response.getUnsupportedUiids().contains(uiid)) response.getUnsupportedUiids().add(uiid);
                continue;
            }
            List<DeviceInfoDO> existing = deviceInfoMapper.getBySn(child.getSn());
            if (existing != null && !existing.isEmpty()) {
                for (DeviceInfoDO device : existing) {
                    if (!Objects.equals(device.getStoreId(), gateway.getStoreId())) {
                        response.setConflicts(response.getConflicts() + 1);
                    } else {
                        DeviceInfoDO update = new DeviceInfoDO();
                        update.setDeviceId(device.getDeviceId());
                        update.setGatewaySn(gateway.getDeviceSn());
                        update.setType(child.getType());
                        update.setStatus(child.getOnline());
                        deviceInfoMapper.updateById(update);
                        response.setExisting(response.getExisting() + 1);
                    }
                }
                continue;
            }
            // Global check prevents taking a device already assigned to another tenant.
            if (deviceInfoMapper.countBySN(child.getSn()) > 0) {
                response.setConflicts(response.getConflicts() + 1);
                continue;
            }
            DeviceInfoDO device = new DeviceInfoDO();
            device.setTenantId(gateway.getTenantId());
            device.setDeviceSn(child.getSn());
            device.setGatewaySn(gateway.getDeviceSn());
            device.setDeviceData(child.getSn());
            device.setStoreId(gateway.getStoreId());
            device.setRoomId(null);
            device.setType(child.getType());
            device.setStatus(child.getOnline());
            device.setShare(false);
            device.setCreator(String.valueOf(getLoginUserId()));
            deviceInfoMapper.insert(device);
            response.setAdded(response.getAdded() + 1);
        }
        return response;
    }

    @Override
    public void setLockAutoLock(DeviceInfoSetAutoLockReqVO reqVO) {
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(reqVO.getDeviceId());
        //只能操作自己的设备
        if (!ObjectUtils.isEmpty(deviceInfoDO) && deviceInfoDO.getCreator().equals(String.valueOf(getLoginUserId()))) {
            //自动锁设置已迁移到新平台，此处无需额外操作
        }

    }

    @Override
    public void control(DeviceControlReqVO reqVO) {
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(reqVO.getDeviceId());
        if (deviceInfoDO == null) {
            throw exception(DATA_NOT_EXISTS);
        }
        com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO controlReq = new com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO();
        controlReq.setDeviceNo(deviceInfoDO.getDeviceSn());
        controlReq.setOutlet(0);
        if (Objects.equals(deviceInfoDO.getType(), AppEnum.device_type.SOUND.getValue()) && "1".equals(reqVO.getCmd())) {
            controlReq.setType(101);
            controlReq.setVoiceMsg("设备语音测试");
        } else if ((Objects.equals(deviceInfoDO.getType(), AppEnum.device_type.ELECTRIC.getValue())
                || Objects.equals(deviceInfoDO.getType(), AppEnum.device_type.DOOR.getValue()))
                && ("on".equals(reqVO.getCmd()) || "off".equals(reqVO.getCmd()))) {
            controlReq.setType("off".equals(reqVO.getCmd()) ? 71 : 70);
        } else {
            throw exception(DEVICE_IOT_OP_ERROR, "设备类型或控制命令不支持");
        }
        if (!iotService.control(controlReq)) {
            throw exception(DEVICE_OPRATION_ERROR);
        }
    }
}
