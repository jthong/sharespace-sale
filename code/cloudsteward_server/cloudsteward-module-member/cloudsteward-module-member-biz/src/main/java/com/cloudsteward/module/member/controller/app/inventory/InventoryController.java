package com.cloudsteward.module.member.controller.app.inventory;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageReqVO;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageRespVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.*;
import com.cloudsteward.module.member.service.inventorydetail.InventoryDetailService;
import com.cloudsteward.module.member.service.inventorygoods.InventoryGoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

@Tag(name = "miniapp - 商品寄存")
@RestController
@RequestMapping("/member/inventory")
@Validated
@Slf4j
public class InventoryController {


    @Resource
    private InventoryGoodsService inventoryGoodsService;

    @Resource
    private InventoryDetailService inventoryDetailService;

    @PostMapping("/getGoodsList")
    @Operation(summary = "获取商品列表", description = "寄存管理使用")
    @PreAuthenticated
    public CommonResult<List<InventoryGoodsSaveRespVO>> getGoodsList(@RequestParam Long storeId) {
        return CommonResult.success(inventoryGoodsService.getGoodsList(storeId));
    }

    @PostMapping("/saveGoods")
    @Operation(summary = "保存商品", description = "寄存管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> saveGoods(@RequestBody @Validated InventoryGoodsSaveReqVO reqVO) {
        inventoryGoodsService.saveGoods(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/deleteGoods")
    @Operation(summary = "删除商品", description = "寄存管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> deleteGoods(@RequestParam Long id) {
        inventoryGoodsService.deleteGoods(id);
        return CommonResult.success(true);
    }

    @PostMapping("/getUserInventoryPage")
    @Operation(summary = "管理员获取用户寄存单列表")
    @PreAuthenticated
    public CommonResult<List<AppUserInventoryDetailRespVO>> getUserInventoryPage(@RequestBody @Validated AppUserInventoryDetailReqVO reqVO) {
        return success(inventoryDetailService.getUserInventoryPage(reqVO));
    }

    @PostMapping("/addInventory")
    @Operation(summary = "新增寄存", description = "寄存管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> addInventory(@RequestBody @Validated AppAddInventoryReqVO reqVO) {
        inventoryDetailService.addInventory(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/takeInventory")
    @Operation(summary = "取出寄存商品", description = "寄存管理使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> takeInventory(@RequestBody @Validated AppTakeInventoryReqVO reqVO) {
        inventoryDetailService.takeInventory(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/getInventoryList")
    @Operation(summary = "用户获取寄存单列表", description = "寄存管理使用")
    @PreAuthenticated
//    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<List<AppInventoryPageRespVO>> getInventoryList(@RequestBody @Validated AppInventoryPageReqVO reqVO) {
        return CommonResult.success(inventoryDetailService.getInventoryList(reqVO));
    }

}
