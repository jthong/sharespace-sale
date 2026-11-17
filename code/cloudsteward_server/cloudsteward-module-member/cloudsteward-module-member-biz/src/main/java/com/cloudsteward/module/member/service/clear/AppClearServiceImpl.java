package com.cloudsteward.module.member.service.clear;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.common.util.date.DateUtils;
import com.cloudsteward.module.member.controller.app.clear.vo.*;
import com.cloudsteward.module.member.controller.app.store.vo.AppOpRoomReqVO;
import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import com.cloudsteward.module.member.dal.mysql.clearbill.ClearBillMapper;
import com.cloudsteward.module.member.dal.mysql.clearinfo.ClearInfoMapper;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

@Service
@Validated
public class AppClearServiceImpl implements AppClearService {

    @Resource
    private ClearInfoMapper clearInfoMapper;

    @Resource
    private ClearBillMapper clearBillMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private DeviceService deviceService;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private StoreInfoService storeInfoService;

    @Resource
    private StoreUserMapper storeUserMapper;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private AppOrderService appOrderService;

    @Resource
    private WorkWxService workWxService;

    @Override
    public PageResult<AppClearPageRespVO> getClearPage(AppClearPageReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        IPage<AppClearPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        orderInfoMapper.getClearPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(id);
        //接单1/开始2/取消3
        switch (status) {
            case 1:
                //在自己权限下的单子 才能接单
                storeInfoService.checkPermission(clearInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.CLEAR.getValue());
                //没有被别人接单，才能接单 并且状态是待接单
                if (ObjectUtils.isEmpty(clearInfoDO.getUserId()) && clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.DEFAULT.getValue()) == 0) {
                    clearInfoDO.setUserId(getLoginUserId());
                    clearInfoDO.setCreateTime(LocalDateTime.now());
                    clearInfoDO.setStatus(status);
                    clearInfoMapper.updateById(clearInfoDO);
                } else {
                    throw exception(CLEAR_ORDER_NOT_JIEDAN);
                }
                break;
            default:
                //只有自己的单子 才能改状态
                if (clearInfoDO.getUserId().compareTo(getLoginUserId()) == 0) {
                    switch (status) {
                        case 2:
                            //开始订单  只有已接单状态才能开始
                            if (clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.JIEDAN.getValue()) != 0) {
                                throw exception(CLEAR_ORDER_STATUS_ERROR);
                            }
                            clearInfoDO.setStatus(AppEnum.clear_info_status.START.getValue());
                            clearInfoDO.setStartTime(LocalDateTime.now());
                            clearInfoMapper.updateById(clearInfoDO);
                            //异步发送微信通知
                            workWxService.sendClearFinishMsg(clearInfoDO.getStoreId(), clearInfoDO.getRoomId(), getLoginUserId(), "开始清洁任务");
                            break;
                        case 3:
                            //取消订单 只有已接单状态才能取消
                            if (clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.JIEDAN.getValue()) != 0) {
                                throw exception(CLEAR_ORDER_STATUS_ERROR);
                            }
                            clearInfoMapper.cancelById(clearInfoDO.getClearId());
                            break;
                    }
                } else {
                    throw exception(OPRATION_ERROR);
                }
                break;
        }
    }

    @Override
    public void openStoreDoor(Long id) {
        //获取任务信息
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(id);
        //只有是自己的订单 才能开门
        if (clearInfoDO.getUserId().compareTo(getLoginUserId()) == 0) {
            deviceService.openStoreDoor(getLoginUserId(), clearInfoDO.getStoreId(), 3);
        } else {
            throw exception(ORDER_OPEN_DOOR_ERROR);
        }
    }

    @Override
    public void openRoomDoor(Long id) {
        //获取任务信息
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(id);
        //只有是自己的订单 并且状态是已开始 才能开门
        if (clearInfoDO.getUserId().compareTo(getLoginUserId()) == 0 && clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.START.getValue()) == 0) {
            deviceService.openRoomDoor(getLoginUserId(), clearInfoDO.getStoreId(), clearInfoDO.getRoomId(), 3);
        } else {
            throw exception(ORDER_OPEN_DOOR_ERROR);
        }
    }

    @Override
    public AppClearInfoRespVO getDetail(Long clearId) {
        return clearInfoMapper.getDetail(clearId);
    }

    @Override
    public AppClearChartRespVO getChartData() {
        AppClearChartRespVO respVO = new AppClearChartRespVO();
        //获取今日的时间
        String today = DateUtils.dateToStr(new Date(), DateUtils.FORMAT_YEAR_MONTH_DAY);
        Long loginUserId = getLoginUserId();
        //1=日 2=月 3=全部
        respVO.setTodayJiedan(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.JIEDAN.getValue(), 1, today));
        respVO.setTodayStart(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.START.getValue(), 1, today));
        respVO.setTodayFinish(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.FINISH.getValue(), 1, today));
        respVO.setTomonthJiesuan(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.JIESUAN.getValue(), 2, today));
        respVO.setTomonthFinish(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.FINISH.getValue(), 2, today));
        respVO.setTomonthBohui(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.TOUSU.getValue(), 2, today));
        respVO.setTotalFinish(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.FINISH.getValue(), 3, null));
        respVO.setTotalSettlementt(clearInfoMapper.countByUserIdAndStatus(loginUserId, AppEnum.clear_info_status.JIESUAN.getValue(), 3, null));
        //总收入
        respVO.setTotalMoney(clearBillMapper.sumMoneyByUserId(loginUserId));
        return respVO;
    }

    @Override
    public PageResult<AppClearBillRespVO> getClearBillPage(AppClearBillReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        IPage<AppClearBillRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        clearBillMapper.getClearBillPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void finish(AppStartClearReqVO reqVO) {
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(reqVO.getId());
        //只有自己的单子 并且状态是已开始 才能改状态
        if (clearInfoDO.getUserId().compareTo(getLoginUserId()) == 0
                && clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.START.getValue()) == 0) {
            //校验图片的数量
//            if (StringUtils.isEmpty(reqVO.getImgs())) {
//                throw exception(CLEAR_IMAGE_NOT_FOUNT_ERROR);
//            }
            clearInfoDO.setImgs(reqVO.getImgs());
            clearInfoDO.setStatus(AppEnum.clear_info_status.FINISH.getValue());
            clearInfoDO.setFinishTime(LocalDateTime.now());
            clearInfoMapper.updateById(clearInfoDO);
            //任务完成了  要关闭房间电源 但是如果房间已经开始后面的订单  就不关闭电源
            if (orderInfoMapper.countByRoomCurrent(clearInfoDO.getRoomId(), null) == 0) {
                //房间目前没有订单是进行中 关闭电源
                //orderCount = 0 ，因为这里关电，只要房间有订单  就不要去关共享设备
                deviceService.closeRoomDoor(getLoginUserId(), clearInfoDO.getStoreId(), clearInfoDO.getRoomId(), 3, 0);
            } else {
                // 如果当前有订单进行 就改成进行中
//                roomInfoMapper.updateStatusById(AppEnum.room_status.USED.getValue(), clearInfoDO.getRoomId());
//                 4.19修改 如果有订单进行中 就不允许完成
//                throw exception(CLEAR_FINISH_ORDER_START_ERROR);
            }
            appOrderService.flushRoomStatus(clearInfoDO.getRoomId(), null);
            workWxService.sendClearFinishMsg(clearInfoDO.getStoreId(), clearInfoDO.getRoomId(), getLoginUserId(), "完成房间清洁");

        } else {
            throw exception(OPRATION_ERROR);
        }
    }

    // getLockPwd 方法已下线（新IoT平台不支持）
    // @Override
    // public String getLockPwd(Long id) { ... }

    @Override
    public void opRoom(AppClearOpRoomReqVO reqVO) {
        //获取任务信息
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(reqVO.getId());
        //只有是自己的订单 并且状态是已开始 才能操作
        if (clearInfoDO.getUserId().compareTo(getLoginUserId()) == 0 && clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.START.getValue()) == 0) {
            deviceService.opRoom(getLoginUserId(),new AppOpRoomReqVO().setRoomId(clearInfoDO.getRoomId()).setType(reqVO.getType()));
        } else {
            throw exception(ORDER_OPEN_DOOR_ERROR);
        }
    }


}
