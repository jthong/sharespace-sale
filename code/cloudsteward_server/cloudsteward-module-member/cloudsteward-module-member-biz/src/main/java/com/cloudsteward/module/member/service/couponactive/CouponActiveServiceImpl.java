package com.cloudsteward.module.member.service.couponactive;

import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveInfoRespVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveReqVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveRespVO;
import com.cloudsteward.module.member.dal.dataobject.couponactive.CouponActiveDO;
import com.cloudsteward.module.member.dal.dataobject.couponinfo.CouponInfoDO;
import com.cloudsteward.module.member.dal.mysql.couponactive.CouponActiveMapper;
import com.cloudsteward.module.member.dal.mysql.couponinfo.CouponInfoMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.util.Date;
import java.util.UUID;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;


/**
 * 优惠券活动 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class CouponActiveServiceImpl implements CouponActiveService {

    @Resource
    private CouponActiveMapper couponActiveMapper;

    @Resource
    private CouponInfoMapper couponInfoMapper;

    @Resource
    private StoreInfoService storeInfoService;

    @Override
    public AppCouponActiveRespVO getAdminByCouponId(Long couponId) {
        //先查询出券
        CouponInfoDO couponInfoDO = couponInfoMapper.selectById(couponId);
        if (ObjectUtils.isEmpty(couponInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        //检查权限
        storeInfoService.checkPermission(couponInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //返回结果
        return couponActiveMapper.getAdminByCouponId(couponId);
    }

    @Override
    @Transactional
    public void saveAdminByCouponId(AppCouponActiveReqVO reqVO) {
        //先查询出券
        CouponInfoDO couponInfoDO = couponInfoMapper.selectById(reqVO.getCouponId());
        if (ObjectUtils.isEmpty(couponInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (couponInfoDO.getExpriceTime().before(new Date())) {
            throw exception(COUPON_EXPIRE_ERROR);
        }
        //活动的时间不能小于优惠券的截止时间
        if (reqVO.getEndTime().after(couponInfoDO.getExpriceTime())) {
            throw exception(COUPON_ACTIVE_END_TIME_ERROR);
        }
        //检查权限
        storeInfoService.checkPermission(couponInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //再看是否已存在活动
        CouponActiveDO couponActiveDO = couponActiveMapper.getByCouponId(reqVO.getCouponId());
        if (ObjectUtils.isEmpty(couponActiveDO)) {
            //没有 则创建活动   先给优惠券设置一个key
            String key = UUID.randomUUID().toString().substring(0, 13);
            couponInfoMapper.updateById(new CouponInfoDO().setCouponId(couponInfoDO.getCouponId()).setCouponKey(key));
            //保存活动
            couponActiveDO = new CouponActiveDO()
                    .setStoreId(couponInfoDO.getStoreId())
                    .setActiveName(reqVO.getActiveName())
                    .setCouponId(reqVO.getCouponId())
                    .setNum(reqVO.getNum())
                    .setBalanceNum(reqVO.getNum())
                    .setEndTime(reqVO.getEndTime());
            couponActiveMapper.insert(couponActiveDO);
        } else {
            //已经存在了 则修改
            couponActiveDO
                    .setActiveName(reqVO.getActiveName())
                    .setNum(reqVO.getNum())
                    .setBalanceNum(reqVO.getNum())
                    .setEndTime(reqVO.getEndTime());
            couponActiveMapper.updateById(couponActiveDO);
        }
    }

    @Override
    @Transactional
    public void stopActive(Long couponId) {
        //先查询出券
        CouponInfoDO couponInfoDO = couponInfoMapper.selectById(couponId);
        if (ObjectUtils.isEmpty(couponInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        //检查权限
        storeInfoService.checkPermission(couponInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //再看是否已存在活动
        CouponActiveDO couponActiveDO = couponActiveMapper.getByCouponId(couponId);
        if (ObjectUtils.isEmpty(couponActiveDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        couponActiveDO.setBalanceNum(0).setEndTime(new Date());
        couponActiveMapper.updateById(couponActiveDO);
    }

    @Override
    public AppCouponActiveInfoRespVO getById(Long couponId) {
        AppCouponActiveInfoRespVO respVO = couponActiveMapper.getByInfoId(couponId);
        if (!ObjectUtils.isEmpty(respVO)) {
            //如果用户已经登陆，则判断是否拥有这张券
            try {
                Long userId = getLoginUserId();
                int i = couponInfoMapper.countByUserIdAndKey(userId, respVO.getCouponKey());
                respVO.setIsGet(i > 0);
            } catch (Exception e) {
//                throw new RuntimeException(e);
            } finally {
                return respVO;
            }
        } else {
            throw exception(COUPON_ACTIVE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void putCoupon(Long id) {
        CouponActiveDO couponActiveDO = couponActiveMapper.selectById(id);
        if (!ObjectUtils.isEmpty(couponActiveDO)) {
            //检查是否可以领取
            if (couponActiveDO.getEndTime().before(new Date())) {
                throw exception(COUPON_ACTIVE_IS_END);
            }
            //查询出优惠券
            CouponInfoDO couponInfoDO = couponInfoMapper.selectById(couponActiveDO.getCouponId());
            Long userId = getLoginUserId();
            int i = couponInfoMapper.countByUserIdAndKey(userId, couponInfoDO.getCouponKey());
            if (i > 0) {
                throw exception(COUPON_ACTIVE_IS_GET);
            }
            //新增
            CouponInfoDO newCouponInfoDO = new CouponInfoDO();
            BeanUtils.copyProperties(couponInfoDO, newCouponInfoDO);
            newCouponInfoDO.setCouponId(null);
            newCouponInfoDO.setUserId(userId);
            couponInfoMapper.insert(newCouponInfoDO);
            //减少数量
            couponActiveMapper.updateById(new CouponActiveDO().setId(id).setBalanceNum(couponActiveDO.getBalanceNum() - 1));
        } else {
            throw exception(COUPON_ACTIVE_NOT_FOUND);
        }
    }
}
