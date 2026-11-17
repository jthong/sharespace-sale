package com.cloudsteward.module.member.service.device;

import com.cloudsteward.module.member.controller.app.store.vo.AppDeviceRunSoundReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppOpRoomReqVO;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.member.service.device
 * @DESCRIPTION: 设备服务接口
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/7/28 12:12
 */
public interface DeviceService {

    void openStoreDoor(Long userId, Long storeId, int type);

    void openRoomDoor(Long userId, Long storeId, Long roomId, int type);

    void closeRoomDoor(Long userId, Long storeId, Long roomId, int type, int orderCount);

    /**
     * @param roomId 房间id
     * @param type   提示语类型 1欢迎语 2结束时间30分钟提醒  3结束时间15分钟提示  4 结束时间5分钟提醒
     */
    void runSound(Long roomId, Integer type);

    void testYunlaba(Long roomId);

    void closeLightByRoomId(Long userId, Long storeId, Long roomId, int type, int orderCount);

    void clearByRoomId(Long roomId);

    int countGateway(Long storeId);

    int countKongtiao(Long roomId);

    void runYunlaba(AppDeviceRunSoundReqVO reqVO);

    boolean runBall(String sn, Long roomId, String cmd);

    void opRoom(Long userId, AppOpRoomReqVO reqVO);

}
