package com.cloudsteward.module.member.service.inventorydetail;

import com.cloudsteward.module.member.controller.app.inventory.vo.*;
import com.cloudsteward.module.member.dal.dataobject.inventorydetail.InventoryDetailDO;
import com.cloudsteward.module.member.dal.dataobject.inventoryinfo.InventoryInfoDO;
import com.cloudsteward.module.member.dal.dataobject.inventoryrecord.InventoryRecordDO;
import com.cloudsteward.module.member.dal.dataobject.user.MemberUserDO;
import com.cloudsteward.module.member.dal.mysql.inventorygoods.InventoryGoodsMapper;
import com.cloudsteward.module.member.dal.mysql.inventoryinfo.InventoryInfoMapper;
import com.cloudsteward.module.member.dal.mysql.inventoryrecord.InventoryRecordMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.user.AppUserService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import com.cloudsteward.module.member.dal.mysql.inventorydetail.InventoryDetailMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;


/**
 * 商品寄存单详细 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class InventoryDetailServiceImpl implements InventoryDetailService {

    @Resource
    private InventoryDetailMapper inventoryDetailMapper;

    @Resource
    private InventoryRecordMapper inventoryRecordMapper;

    @Resource
    private InventoryGoodsMapper inventoryGoodsMapper;

    @Resource
    private WorkWxService workWxService;

    @Resource
    private InventoryInfoMapper inventoryInfoMapper;

    @Resource
    private AppUserService appUserService;

    @Resource
    private StoreInfoService storeInfoService;

    @Override
    public List<AppUserInventoryDetailRespVO> getUserInventoryPage(AppUserInventoryDetailReqVO reqVO) {
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        //先查用户
        MemberUserDO user = appUserService.getUserByMobile(reqVO.getMobile());
        if (ObjectUtils.isEmpty(user)) {
            throw exception(USER_NOT_EXISTS);
        }
        return inventoryDetailMapper.getUserInventoryPage(user.getId(), reqVO.getStoreId());
    }

    @Override
    @Transactional
    public void addInventory(AppAddInventoryReqVO reqVO) {
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        //先查用户
        MemberUserDO memberUserDO = appUserService.createUserIfAbsent(reqVO.getPhone(), getClientIP());
        //创建寄存单
        InventoryInfoDO inventoryInfoDO = new InventoryInfoDO();
        inventoryInfoDO.setUserId(memberUserDO.getId()).setStoreId(reqVO.getStoreId()).setExpireTime(reqVO.getExpireTime()).setOrderNo(getOrderNo());
        inventoryInfoMapper.insert(inventoryInfoDO);
        //保存商品明细
        List<InventoryDetailDO> inventoryDetailDOS = new ArrayList<>(reqVO.getGoodsList().size());
        //拼接通知内容
        StringBuffer content = new StringBuffer();
        //先添加表头
        List<InventoryGoodsSaveRespVO> goodsList = inventoryGoodsMapper.getGoodsList(reqVO.getStoreId());
        Map<String, InventoryGoodsSaveRespVO> collect = goodsList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getId()), Function.identity()));
        for (AppGoodsVO appGoodsVO : reqVO.getGoodsList()) {
            InventoryDetailDO detailDO = new InventoryDetailDO();
            detailDO.setInventoryId(inventoryInfoDO.getId()).setSaveNum(appGoodsVO.getNum()).setStockNum(appGoodsVO.getNum()).setGoodsId(appGoodsVO.getGoodsId());
            inventoryDetailDOS.add(detailDO);
            InventoryGoodsSaveRespVO item = collect.get(String.valueOf(appGoodsVO.getGoodsId()));
            content.append("`").append(item.getGoodsName()).append(" ").append(appGoodsVO.getNum()).append(item.getUnits()).append("`\n\n");
        }
        inventoryDetailMapper.insertBatch(inventoryDetailDOS);
        //发送企业微信通知
        workWxService.sendJCmsg(reqVO.getStoreId(), inventoryInfoDO.getOrderNo(), memberUserDO.getMobile(), getLoginUserId(), content.toString(), new Date(), reqVO.getExpireTime());
    }

    @Override
    @Transactional
    public void takeInventory(AppTakeInventoryReqVO reqVO) {
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        //先查用户
        MemberUserDO user = appUserService.getUserByMobile(reqVO.getPhone());
        if (ObjectUtils.isEmpty(user)) {
            throw exception(USER_NOT_EXISTS);
        }
        //再查商品是否充足 并且是没有过期的
        List<InventoryDetailDO> detailList = inventoryDetailMapper.selectBatchIds(reqVO.getGoodsList().stream().map(x -> x.getGoodsId()).collect(Collectors.toSet()));
        Map<String, InventoryDetailDO> detailDOMap = detailList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getId()), Function.identity()));
        //再反查出寄存单
        List<InventoryInfoDO> inventoryInfoDOS = inventoryInfoMapper.selectBatchIds(detailList.stream().map(x -> x.getInventoryId()).collect(Collectors.toSet()));
        Map<String, InventoryInfoDO> inventoryInfoDOMap = inventoryInfoDOS.stream().collect(Collectors.toMap(x -> String.valueOf(x.getId()), Function.identity()));
        List<InventoryGoodsSaveRespVO> goodsList = inventoryGoodsMapper.getGoodsList(reqVO.getStoreId());
        Map<String, InventoryGoodsSaveRespVO> goodsMap = goodsList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getId()), Function.identity()));

        List<InventoryRecordDO> recordDOS = new ArrayList<>(detailList.size());
        //检查的同时  添加寄存记录
        StringBuffer content = new StringBuffer();
        Date now = new Date();
        for (AppGoodsVO goodsVO : reqVO.getGoodsList()) {
            InventoryDetailDO detailDO = detailDOMap.get(String.valueOf(goodsVO.getGoodsId()));
            InventoryGoodsSaveRespVO goodsSaveRespVO = goodsMap.get(String.valueOf(detailDO.getGoodsId()));
            if (detailDO.getStockNum() < goodsVO.getNum()) {
                throw exception(INVENTORY_TAKE_ERROR, goodsSaveRespVO.getGoodsName());
            }
            InventoryInfoDO inventoryInfoDO = inventoryInfoDOMap.get(String.valueOf(detailDO.getInventoryId()));
            InventoryRecordDO recordDO = new InventoryRecordDO();
            recordDO.setNum(goodsVO.getNum() * -1)//取出是负
                    .setInventoryId(detailDO.getInventoryId())
                    .setDetailId(detailDO.getId())
                    .setStoreId(inventoryInfoDO.getStoreId());
            recordDOS.add(recordDO);
            //检查是否过期
            if (inventoryInfoDO.getExpireTime().before(now)) {
                throw exception(INVENTORY_EXPIRE_ERROR, goodsSaveRespVO.getGoodsName(), inventoryInfoDO.getExpireTime());
            }
            content.append("`").append(goodsSaveRespVO.getGoodsName()).append(" ").append(goodsVO.getNum()).append(goodsSaveRespVO.getUnits()).append("`\n\n");
            //扣除数量
            detailDO.setStockNum(detailDO.getStockNum() - goodsVO.getNum());
            inventoryDetailMapper.updateById(detailDO);
        }
        inventoryRecordMapper.insertBatch(recordDOS);
        //发送企业微信通知
        workWxService.sendTakemsg(reqVO.getStoreId(), reqVO.getPhone(), getLoginUserId(), content.toString());


    }

    @Override
    public List<AppInventoryPageRespVO> getInventoryList(AppInventoryPageReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        List<InventoryDetailRespVO> inventoryList = null;
        switch (reqVO.getStatus()) {
            case 0:
            case 2:
                //待取 过期
                inventoryList = inventoryDetailMapper.getInventoryList(reqVO);
                break;
            case 1:
                //已取
                inventoryList = inventoryRecordMapper.getInventoryList(reqVO);
                break;
        }
        if (!CollectionUtils.isEmpty(inventoryList)) {
            //按门店分组
            Map<String, List<InventoryDetailRespVO>> collect = inventoryList.stream().collect(Collectors.groupingBy(x -> x.getStoreName()));
            List<AppInventoryPageRespVO> result = new ArrayList<>(collect.size());
            for (Map.Entry<String, List<InventoryDetailRespVO>> entry : collect.entrySet()) {
                AppInventoryPageRespVO respVO = new AppInventoryPageRespVO();
                respVO.setStoreName(entry.getKey()).setGoodsList(entry.getValue());
                result.add(respVO);
            }
            return result;
        }
        return new ArrayList<>();
    }

    private String getOrderNo() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMdd");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDate = currentDateTime.format(dateFormatter);
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        String randomNumString = String.format("%06d", randomNum);
        return "JC" + currentDate + randomNumString;
    }
}
