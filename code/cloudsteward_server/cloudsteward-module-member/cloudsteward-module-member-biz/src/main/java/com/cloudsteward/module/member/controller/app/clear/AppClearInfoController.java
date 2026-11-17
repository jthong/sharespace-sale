package com.cloudsteward.module.member.controller.app.clear;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.app.clear.vo.*;
import com.cloudsteward.module.member.service.clear.AppClearService;
import com.cloudsteward.module.member.service.clear.CleaningNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;

@Tag(name = "miniapp - 保洁角色使用")
@RestController
@RequestMapping("/member/clear")
@Validated
@Slf4j
public class AppClearInfoController {

    @Resource
    private AppClearService appClearService;

    @Resource
    private CleaningNoticeService cleaningNoticeService;

    @GetMapping("/notice-template")
    @Operation(summary = "获取保洁任务订阅模板")
    @PreAuthenticated
    public CommonResult<String> getNoticeTemplate() {
        return success(cleaningNoticeService.getTemplateId(getLoginUserId(), getLoginUserType()));
    }

    @PostMapping("/notice-subscription")
    @Operation(summary = "记录保洁员已同意接收保洁提醒")
    @PreAuthenticated
    public CommonResult<Boolean> recordNoticeSubscription(@RequestParam("templateId") String templateId) {
        cleaningNoticeService.recordSubscription(getLoginUserId(), getLoginUserType(), templateId);
        return success(true);
    }

    @PostMapping("/getClearPage")
    @Operation(summary = "获取任务大厅分页列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppClearPageRespVO>> getClearPage(@RequestBody @Valid AppClearPageReqVO reqVO) {
        return success(appClearService.getClearPage(reqVO));
    }

    @PostMapping("/jiedan/{id}")
    @Operation(summary = "接单")
    @PreAuthenticated
    @Parameter(name = "id")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> jiedan(@PathVariable("id") Long id) {
        appClearService.changeStatus(id, 1);
        return success(true);
    }

    @PostMapping("/start/{id}")
    @Operation(summary = "开始")
    @PreAuthenticated
    @Parameter(name = "id")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> start(@PathVariable("id") Long id) {
        appClearService.changeStatus(id, 2);
        return success(true);
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消")
    @PreAuthenticated
    @Parameter(name = "id")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> cancel(@PathVariable("id") Long id) {
        appClearService.changeStatus(id, 3);
        return success(true);
    }

    @PostMapping("/finish/{id}")
    @Operation(summary = "完成")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> finish(@RequestBody @Valid AppStartClearReqVO reqVO) {
        appClearService.finish(reqVO);
        return success(true);
    }

    @PostMapping("/openStoreDoor/{id}")
    @Operation(summary = "任务大厅-(开关)门店的大门,传任务id", description = "保洁任务大厅使用")
    @PreAuthenticated
    @Parameter(name = "id")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openStoreDoor(@PathVariable("id") Long id) {
        appClearService.openStoreDoor(id);
        return success(true);
    }

    @PostMapping("/openRoomDoor/{id}")
    @Operation(summary = "任务大厅-(开关)房间的大门,传任务id", description = "保洁任务大厅使用")
    @PreAuthenticated
    @Parameter(name = "id")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openRoomDoor(@PathVariable("id") Long id) {
        appClearService.openRoomDoor(id);
        return success(true);
    }

    @PostMapping("/opRoom")
    @Operation(summary = "任务大厅-(开关)房间的大门,传任务id", description = "保洁任务大厅使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> opRoom(@Validated @RequestBody AppClearOpRoomReqVO reqVO) {
        appClearService.opRoom(reqVO);
        return success(true);
    }


    @GetMapping("/getDetail/{clearId}")
    @Operation(summary = "获取保洁任务详情")
    @PreAuthenticated
    @Parameter(name = "clearId")
    public CommonResult<AppClearInfoRespVO> getDetail(@PathVariable("clearId") Long clearId) {
        return success(appClearService.getDetail(clearId));
    }

    @GetMapping("/getChartData")
    @Operation(summary = "获取统计图表数据")
    @PreAuthenticated
    public CommonResult<AppClearChartRespVO> getChartData() {
        return success(appClearService.getChartData());
    }

    @PostMapping("/getClearBillPage")
    @Operation(summary = "获取结算账单记录分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppClearBillRespVO>> getClearBillPage(@RequestBody @Valid AppClearBillReqVO reqVO) {
        return success(appClearService.getClearBillPage(reqVO));
    }

    // getLockPwd 接口已下线（新IoT平台不支持）
    // @PostMapping("/getLockPwd")
    // @Operation(summary = "获取智能锁随机密码", description = "保洁订单使用")
    // @PreAuthenticated
    // @Idempotent(timeout = 5, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    // public CommonResult<String> getLockPwd(@PathVariable("id") Long id) {
    //     return success(appClearService.getLockPwd(id));
    // }
}
