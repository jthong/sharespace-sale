package com.cloudsteward.module.member.controller.app.store;

import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.admin.faceblacklist.vo.FaceBlacklistPageReqVO;
import com.cloudsteward.module.member.controller.admin.faceblacklist.vo.FaceBlacklistRespVO;
import com.cloudsteward.module.member.controller.admin.facerecord.vo.FaceRecordPageReqVO;
import com.cloudsteward.module.member.controller.admin.facerecord.vo.FaceRecordRespVO;
import com.cloudsteward.module.member.controller.admin.storeinfo.vo.StoreSetTemplateReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppVipBlacklistRespVO;
import com.cloudsteward.module.member.controller.app.store.vo.*;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.faceblacklist.FaceBlacklistService;
import com.cloudsteward.module.member.service.facerecord.FaceRecordService;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.storevipconfig.StoreVipConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.pojo.CommonResult.success;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.infra.enums.ErrorCodeConstants.FILE_IS_EMPTY;

@Tag(name = "miniapp - 门店管理 （管理员）")
@RestController
@RequestMapping("/member/store")
@Validated
@Slf4j
public class AppStoreController {

    @Resource
    private StoreInfoService storeInfoService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private FaceRecordService faceRecordService;

    @Resource
    private FaceBlacklistService faceBlacklistService;

    @Resource
    private StoreVipConfigService storeVipConfigService;

    @PostMapping("/getServiceInfo")
    @Operation(summary = "获取服务到期信息")
    @PreAuthenticated
    public CommonResult<List<AppServiceInfoRespVO>> getServiceInfo(HttpServletRequest request) {
        return success(storeInfoService.getServiceInfo(request));
    }


    @PostMapping("/getPageList")
    @Operation(summary = "获取门店列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppStoreAdminRespVO>> getPageList(@RequestBody @Valid AppStoreAdminReqVO reqVO) {
        return success(storeInfoService.getPageList(reqVO));
    }


    @GetMapping("/getDetail/{storeId}")
    @Operation(summary = "获取门店详情")
    @PreAuthenticated
    @Parameter(name = "storeId")
    public CommonResult<AppStoreInfoRespVO> getDetail(@PathVariable("storeId") Long storeId) {
        return success(storeInfoService.getDetail(storeId));
    }

    @PostMapping("/save")
    @Operation(summary = "保存门店详情")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> save(@RequestBody @Valid AppStoreInfoReqVO reqVO) {
        storeInfoService.save(reqVO);
        return success(true);
    }

    @PostMapping("/openStoreDoor/{storeId}")
    @Operation(summary = "开门店的大门", description = "门店管理使用")
    @PreAuthenticated
    @Parameter(name = "storeId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openStoreDoor(@PathVariable("storeId") Long storeId) {
        //1用户开门 2管理员开门 3保洁开门
        deviceService.openStoreDoor(getLoginUserId(), storeId, 2);
        return success(true);
    }

    @GetMapping("/getRoomInfoList/{storeId}")
    @Operation(summary = "获取门店的房间列表")
    @PreAuthenticated
    @Parameter(name = "storeId")
    public CommonResult<List<AppRoomListRespVO>> getRoomInfoList(@PathVariable("storeId") Long storeId) {
        return success(storeInfoService.getRoomInfoList(storeId));
    }

    @PostMapping("/getRoomInfoList")
    @Operation(summary = "获取门店的房间列表")
    @PreAuthenticated
    @Parameter(name = "storeId")
    public CommonResult<List<AppRoomInfoListRespVO>> getRoomInfoList2(@RequestParam(value = "storeId", required = false) Long storeId) {
        return success(storeInfoService.getRoomInfoList2(storeId));
    }

    @GetMapping("/getRoomDetail/{roomId}")
    @Operation(summary = "获取房间详情")
    @PreAuthenticated
    @Parameter(name = "roomId")
    public CommonResult<AppRoomDetailRespVO> getRoomDetail(@PathVariable("roomId") Long roomId) {
        return success(storeInfoService.getRoomDetail(roomId));
    }

    @PostMapping("/saveRoomDetail")
    @Operation(summary = "保存房间详情")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> saveRoomDetail(@RequestBody @Valid AppRoomDetailReqVO reqVO) {
        storeInfoService.saveRoomDetail(reqVO);
        return success(true);
    }

    @PostMapping("/getGroupPayAuthUrl")
    @Operation(summary = "获取团购平台授权链接")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<String> getGroupPayAuthUrl(@RequestBody @Valid GroupPayAuthUrlReqVO reqVO) {
        return success(storeInfoService.getGroupPayAuthUrl(reqVO));
    }

    @PostMapping("/testYunlaba/{roomId}")
    @Operation(summary = "测试云喇叭", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> testYunlaba(@PathVariable("roomId") Long roomId) {
        deviceService.testYunlaba(roomId);
        return success(true);
    }

    @PostMapping("/runYunlaba")
    @Operation(summary = "测试云喇叭", description = "房间管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> testYunlaba(@RequestBody AppDeviceRunSoundReqVO reqVO) {
        deviceService.runYunlaba(reqVO);
        return success(true);
    }

    @PostMapping("/addDevice")
    @Operation(summary = "添加设备", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> addDevice(@RequestBody @Validated AppAddDeviceReqVO reqVO) {
        storeInfoService.addDevice(reqVO);
        return success(true);
    }

    @PostMapping("/delDevice/{deviceId}")
    @Operation(summary = "删除设备", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> delDevice(@PathVariable("deviceId") Long deviceId) {
        storeInfoService.delDevice(deviceId);
        return success(true);
    }


    @PostMapping("/deleteRoomInfo/{roomId}")
    @Operation(summary = "删除房间", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> deleteRoomInfo(@PathVariable("roomId") Long roomId) {
        storeInfoService.deleteRoomInfo(roomId);
        return success(true);
    }


    @PostMapping("/openRoomDoor/{roomId}")
    @Operation(summary = "开房间电源和灯光", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openRoomDoor(@PathVariable("roomId") Long roomId) {
        //1用户开门 2管理员开门 3保洁开门
        deviceService.openRoomDoor(getLoginUserId(), null, roomId, 2);
        return success(true);
    }


    @PostMapping("/opRoom")
    @Operation(summary = "操作房间设备", description = "房间管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> opRoom(@Validated @RequestBody AppOpRoomReqVO reqVO) {
        deviceService.opRoom(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/closeRoomDoor/{roomId}")
    @Operation(summary = "关房间的门和电源", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> closeRoomDoor(@PathVariable("roomId") Long roomId) {
        //1用户关门 2管理员关门 3保洁关门
        //orderCount = 0 ，因为这里关电，只要房间有订单  就不要去关共享设备
        deviceService.closeRoomDoor(getLoginUserId(), null, roomId, 2, 0);
        return success(true);
    }

    @PostMapping("/disableRoom/{roomId}")
    @Operation(summary = "启用禁用房间", description = "房间管理使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> disableRoom(@PathVariable("roomId") Long roomId) {
        storeInfoService.disableRoom(roomId);
        return success(true);
    }


    @PostMapping("/getDiscountRulesPage")
    @Operation(summary = "获取门店充值优惠信息分页列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppDiscountRulesPageRespVO>> getDiscountRulesPage(@RequestBody @Valid AppDiscountRulesPageReqVO reqVO) {
        return success(storeInfoService.getDiscountRulesPage(reqVO));
    }

    @PostMapping("/changeDiscountRulesStatus/{discountId}")
    @Operation(summary = "修改门店充值优惠信息状态（启用/禁用）")
    @PreAuthenticated
    @Parameter(name = "discountId")
    public CommonResult<Boolean> changeDiscountRulesStatus(@PathVariable("discountId") Long discountId) {
        storeInfoService.changeDiscountRulesStatus(discountId);
        return success(true);
    }

    @GetMapping("/getDiscountRuleDetail/{discountId}")
    @Operation(summary = "获取门店充值优惠信息详情")
    @PreAuthenticated
    @Parameter(name = "discountId")
    public CommonResult<AppDiscountRulesDetailRespVO> getDiscountRuleDetail(@PathVariable("discountId") Long discountId) {
        return success(storeInfoService.getDiscountRuleDetail(discountId));
    }

    @PostMapping("/saveDiscountRuleDetail")
    @Operation(summary = "保存门店充值优惠信息")
    @PreAuthenticated
    public CommonResult<Boolean> saveDiscountRuleDetail(@RequestBody @Valid AppDiscountRulesDetailReqVO reqVO) {
        storeInfoService.saveDiscountRuleDetail(reqVO);
        return success(true);
    }


    @PostMapping("/uploadImg")
    @Operation(summary = "上传图片，并返回图片访问地址")
    @PreAuthenticated
    public CommonResult<String> uploadImg(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        String avatar = storeInfoService.uploadImg(file.getInputStream());
        return success(avatar);
    }

    @GetMapping("/getStoreList")
    @Operation(summary = "管理员获取门店下拉选择列表")
    @PreAuthenticated
    public CommonResult<List<KeyValue<String, Long>>> getStoreList(@RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "cityName", required = false) String cityName) {
        return success(storeInfoService.getStoreList(name, cityName));
    }

    @GetMapping("/getStoreListByAdmin")
    @Operation(summary = "管理员获取门店下拉选择列表")
    @PreAuthenticated
    public CommonResult<List<KeyValue<String, Long>>> getStoreListByAdmin(@RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "cityName", required = false) String cityName) {
        return success(storeInfoService.getStoreListByAdmin(name, cityName));
    }

    @GetMapping("/getRoomList/{storeId}")
    @Operation(summary = "管理员获取房间下拉选择列表")
    @PreAuthenticated
    @Parameter(name = "storeId")
    public CommonResult<List<KeyValue<String, Long>>> getRoomList(@PathVariable("storeId") Long storeId) {
        return success(storeInfoService.getRoomList(storeId));
    }

    @GetMapping("/clearAndFinish/{roomId}")
    @Operation(summary = "管理员一键清洁并结单")
    @PreAuthenticated
    @Parameter(name = "roomId")
    public CommonResult<Boolean> clearAndFinish(@PathVariable("roomId") Long roomId) {
        storeInfoService.clearAndFinish(roomId);
        return success(true);
    }

    @GetMapping("/finishRoomOrder/{roomId}")
    @Operation(summary = "管理员提前结束房间的订单")
    @PreAuthenticated
    @Parameter(name = "roomId")
    public CommonResult<Boolean> finishRoomOrder(@PathVariable("roomId") Long roomId) {
        storeInfoService.finishRoomOrder(roomId);
        return success(true);
    }

//    @GetMapping("/workPrice/{storeId}")
//    @Operation(summary = "管理员启用/关闭工作日价格")
//    @PreAuthenticated
//    @Parameter(name = "storeId")
//    public CommonResult<Boolean> workPrice(@PathVariable("storeId") Long storeId) {
//        storeInfoService.syncPrice(storeId);
//        return success(true);
//    }

//    @PostMapping("/updateLockData")
//    @Operation(summary = "更新锁数据")
//    @PreAuthenticated
//    public CommonResult<Boolean> updateLockData(@PathVariable("storeId") Long storeId) {
//        return success(true);
//    }
//


    @GetMapping("/getStoreSoundInfo/{storeId}")
    @Operation(summary = "获取门店语音播报设置")
    @PreAuthenticated
    @Parameter(name = "storeId")
    @Idempotent(timeout = 1, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<AppStoreSoundInfoRespVO> getStoreSoundInfo(@PathVariable("storeId") Long storeId,
                                                                   @RequestParam(value = "roomClass", required = false) Integer roomClass) {
        return success(storeInfoService.getStoreSoundInfo(storeId, roomClass));
    }

    @PostMapping("/saveStoreSoundInfo")
    @Operation(summary = "保存门店语音播报设置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> saveStoreSoundInfo(@RequestBody @Valid AppStoreSoundInfoReqVO reqVO) {
        storeInfoService.saveStoreSoundInfo(reqVO);
        return success(true);
    }

    @PostMapping("/getFaceRecordPage")
    @Operation(summary = "获得人脸识别记录分页")
    @PreAuthenticated
    public CommonResult<PageResult<FaceRecordRespVO>> getFaceRecordPage(@RequestBody @Valid FaceRecordPageReqVO pageVO) {
        return success(faceRecordService.getFaceRecordPage(pageVO, false));
    }

    @PostMapping("/getFaceBlacklistPage")
    @Operation(summary = "获得人脸黑名单分页")
    @PreAuthenticated
    public CommonResult<PageResult<FaceBlacklistRespVO>> getFaceBlacklistPage(@RequestBody @Valid FaceBlacklistPageReqVO pageVO) {
        return success(faceBlacklistService.getFaceBlacklistPage(pageVO, false));
    }

    @PostMapping("/moveFaceByRecord")
    @Operation(summary = "根据人脸识别记录修改黑名单")
    @PreAuthenticated
    @Idempotent(timeout = 3, message = "你的点击太快了~")
    public CommonResult<Boolean> moveFaceByRecord(@RequestBody @Valid AppMoveBlacklistReqVO reqVO) {
        faceRecordService.moveFaceByRecord(reqVO.getId(), reqVO.getRemark(), false);
        return success(true);
    }

    @PostMapping("/moveFaceById/{id}")
    @Operation(summary = "移出人脸识黑名单")
    @PreAuthenticated
    @Idempotent(timeout = 3, message = "你的点击太快了~")
    public CommonResult<Boolean> moveFaceById(@PathVariable("id") Long id) {
        faceBlacklistService.moveFaceById(id);
        return success(true);
    }

    @PostMapping("/updateRoomLock")
    @Operation(summary = "校准房间智能锁", description = "设备管理使用")
//    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> updateRoomLock(@RequestBody @Valid AppUpRoomLockReqVO reqVO) {
        storeInfoService.updateRoomLock(reqVO);
        return success(true);
    }


    @PostMapping("/vip/blacklist")
    @Operation(summary = "获取黑名单列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppVipBlacklistRespVO>> getVipBlacklist(@RequestBody AppGetVipBlackListVO pageVo) {
        return success(storeInfoService.getVipBlacklist(pageVo));
    }

    @PostMapping("/addBlackList")
    @Operation(summary = "添加黑名单")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> addBlackList(@RequestBody @Valid AppAddBlackListVO addBlackList) {
        storeInfoService.addBlackList(addBlackList);
        return success(true);
    }

    @PostMapping("/remove/{id}")
    @Operation(summary = "移出黑名单")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> removeBlackList(@PathVariable Long id) {
        storeInfoService.removeBlackList(id);
        return success(true);
    }

    @PostMapping("/getPrePayConfig/{roomId}")
    @Operation(summary = "获取预付费配置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<AppRoomPrePayConfigRespVO> getPrePayConfig(@PathVariable Long roomId) {
        return success(storeInfoService.getPrePayConfig(roomId));
    }

    @PostMapping("/setPrePayConfig")
    @Operation(summary = "保存预付费配置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> setPrePayConfig(@RequestBody @Valid AppRoomPrePayConfigRespVO reqVO) {
        storeInfoService.setPrePayConfig(reqVO);
        return success(true);
    }


    @PostMapping("/getVipConfig/{storeId}")
    @Operation(summary = "获取会员配置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<List<AppStoreVipConfigListRespVO>> getVipConfig(@PathVariable("storeId") Long storeId) {
        return success(storeVipConfigService.getVipConfig(storeId));
    }

    @PostMapping("/saveVipConfig")
    @Operation(summary = "保存会员配置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> saveVipConfig(@RequestBody @Valid AppStoreVipConfigSaveReqVO reqVO) {
        storeVipConfigService.saveVipConfig(reqVO);
        return success(true);
    }

    @PostMapping("/deleteVipConfig/{vipId}")
    @Operation(summary = "删除会员配置")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> deleteVipConfig(@PathVariable("vipId") Long vipId) {
        storeVipConfigService.deleteVipConfig(vipId);
        return success(true);
    }

    @PostMapping("/editMemberVip")
    @Operation(summary = "修改用户会员信息")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> editMemberVip(@RequestBody @Valid AppEditMemberVipReqVO reqVO) {
        storeVipConfigService.editMemberVip(reqVO);
        return success(true);
    }

    @PostMapping("/addMemberVip")
    @Operation(summary = "添加用户会员信息")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> addMemberVip(@RequestBody @Valid AppAddMemberVipReqVO reqVO) {
        storeVipConfigService.addMemberVip(reqVO);
        return success(true);
    }

    @PostMapping("/getTemplateList")
    @Operation(summary = "获取门店模板列表")
    @PreAuthenticated
    public CommonResult<List<AppTemplateInfoRespVO>> getTemplateList() {
        return success(storeInfoService.getTemplateList());
    }


    @PostMapping("/setTemplate")
    @Operation(summary = "设置门店模板")
    @PreAuthenticated
    public CommonResult<Boolean> setTemplate(@Valid @RequestBody StoreSetTemplateReqVO reqVO) {
        storeInfoService.setTemplate(reqVO, false);
        return success(true);
    }


    @PostMapping("/checkBall/{deviceId}")
    @Operation(summary = "检测台球是否归还", description = "设备管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> checkBall(@PathVariable("deviceId") Long deviceId) {
        return success(storeInfoService.checkBall(deviceId));
    }

    @PostMapping("/resetQrcode")
    @Operation(summary = "重新生成门店的全部二维码", description = "门店管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> resetQrcode(@RequestParam("storeId") Long storeId) {
        storeInfoService.resetQrcode(storeId);
        return success(true);
    }

    @PostMapping("/setDouyinId")
    @Operation(summary = "设置抖音门店ID", description = "门店管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> setDouyinId(@RequestParam("storeId") Long storeId,@RequestParam("dyId") String dyId) {
        storeInfoService.setDouyinId(storeId,dyId);
        return success(true);
    }

    @PostMapping("/getNFCScheme")
    @Operation(summary = "获取NFC数据", description = "门店管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<String> getNFCScheme(@Valid @RequestBody AppNFCDataReqVO reqVO) {
        return success(storeInfoService.getNFCScheme(reqVO));
    }


}
