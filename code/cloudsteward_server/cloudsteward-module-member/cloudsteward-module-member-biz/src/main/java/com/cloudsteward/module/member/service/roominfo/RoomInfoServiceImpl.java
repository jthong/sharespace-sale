package com.cloudsteward.module.member.service.roominfo;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.cloudsteward.module.member.controller.admin.roominfo.vo.*;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;
import com.cloudsteward.framework.common.pojo.PageResult;

import com.cloudsteward.module.member.convert.roominfo.RoomInfoConvert;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 房间管理 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RoomInfoServiceImpl implements RoomInfoService {

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Override
    public Long createRoomInfo(RoomInfoCreateReqVO createReqVO) {
        // 插入
        RoomInfoDO roomInfo = RoomInfoConvert.INSTANCE.convert(createReqVO);
        roomInfoMapper.insert(roomInfo);
        // 返回
        return roomInfo.getRoomId();
    }

    @Override
    public void updateRoomInfo(RoomInfoUpdateReqVO updateReqVO) {
        // 校验存在
        validateRoomInfoExists(updateReqVO.getRoomId());
        // 更新
        RoomInfoDO updateObj = RoomInfoConvert.INSTANCE.convert(updateReqVO);
        roomInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteRoomInfo(Long id) {
        // 校验存在
        validateRoomInfoExists(id);
        // 删除
        roomInfoMapper.deleteById(id);
    }

    private void validateRoomInfoExists(Long id) {
        if (roomInfoMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public RoomInfoDO getRoomInfo(Long id) {
        return roomInfoMapper.selectById(id);
    }

    @Override
    public List<RoomInfoDO> getRoomInfoList(Collection<Long> ids) {
        return roomInfoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<RoomInfoDO> getRoomInfoPage(RoomInfoPageReqVO pageReqVO) {
        return roomInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<RoomInfoDO> getRoomInfoList(RoomInfoExportReqVO exportReqVO) {
        return roomInfoMapper.selectList(exportReqVO);
    }

}
