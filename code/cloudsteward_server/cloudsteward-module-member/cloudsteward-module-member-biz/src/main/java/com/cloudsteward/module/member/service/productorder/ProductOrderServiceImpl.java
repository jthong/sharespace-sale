package com.cloudsteward.module.member.service.productorder;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderInfo;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderRespVO;
import com.cloudsteward.module.member.controller.app.productorder.vo.*;
import com.cloudsteward.module.member.controller.app.store.vo.AppRoomListVO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import com.cloudsteward.module.member.dal.dataobject.productorder.ProductOrderDO;
import com.cloudsteward.module.member.dal.dataobject.storeproduct.StoreProductDO;
import com.cloudsteward.module.member.dal.dataobject.user.MemberUserDO;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.productorder.ProductOrderMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeproduct.StoreProductMapper;
import com.cloudsteward.module.member.dal.mysql.storeproductattrvalue.StoreProductAttrValueMapper;
import com.cloudsteward.module.member.dal.mysql.user.MemberUserMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.payorder.PayOrderService;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.storeproduct.StoreProductService;
import com.cloudsteward.module.member.service.storeproduct.dto.FromatDetailDto;
import com.cloudsteward.module.member.service.storeproduct.dto.ProductDto;
import com.cloudsteward.module.member.service.storeproduct.dto.ProductFormatDto;
import com.cloudsteward.module.member.service.storeproductattrresult.StoreProductAttrResultService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static com.cloudsteward.module.member.enums.AppEnum.WX_PRODUCT_PAY_ORDER;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {

    @Resource
    private ProductOrderMapper productOrderMapper;

    @Resource
    private StoreProductService productService;

    @Resource
    private StoreProductMapper productMapper;

    @Resource
    private StoreProductAttrResultService storeProductAttrResultService;

    @Resource
    private StoreProductAttrValueMapper productAttrValueMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private PayOrderService payOrderService;

    @Resource
    private StoreInfoService storeInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberUserMapper userMapper;

    @Resource
    private WorkWxService workWxService;

    @Resource
    private IotService iotService;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Override
    @Transactional
    public WxPayOrderRespVO createOrder(AppSaveOrderReqVO reqVo) {
        AppRoomListVO roomInfo = roomInfoMapper.getInfoById(reqVo.getRoomId());
        if (ObjectUtils.isEmpty(roomInfo)) {
            throw exception(DATA_NOT_EXISTS);
        }
        Long uid = getLoginUserId();
        MemberUserDO memberUserDO = userMapper.selectById(uid);
        String orderNo = getOrderNo();
        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setOrderNo(orderNo);
        productOrderDO.setRoomId(reqVo.getRoomId());
        productOrderDO.setStoreId(roomInfo.getStoreId());
        productOrderDO.setUserId(uid);
        productOrderDO.setUserName(memberUserDO.getNickname());
        productOrderDO.setUserPhone(memberUserDO.getMobile());
        productOrderDO.setMark(reqVo.getMark());
        productOrderDO.setProductInfo(JSON.toJSONString(reqVo.getProductInfo()));
        int totalPrice = 0;//待计算
        // 遍历传递过来的产品
        for (int i = 0; i < reqVo.getProductInfo().size(); i++) {
            // 计算金额
            Long number = reqVo.getProductInfo().get(i).getNumber();
            BigDecimal price = reqVo.getProductInfo().get(i).getPrice();
            totalPrice = totalPrice + new BigDecimal(number).multiply(price).multiply(BigDecimal.valueOf(100)).intValue();
            // 查到相关产品 以及详细属性
            Map<String, Object> productInfo = productService.getProductInfo(reqVo.getProductInfo().get(i).getId());
            ProductDto productDto = (ProductDto) productInfo.get("productInfo");
            JSONObject productResult = productDto.getProductResult();
            List<FromatDetailDto> fromatDetailDtoArrayList = new ArrayList<>();
            List<ProductFormatDto> productFormatDtoList = new ArrayList<>();

            Object attrObject = productResult.get("attr");
            if (attrObject instanceof List) {
                List<?> attrList = (List<?>) attrObject;
                for (Object item : attrList) {
                    FromatDetailDto fromatDetailDto = JSON.toJavaObject(JSON.parseObject(item.toString()), FromatDetailDto.class);
                    fromatDetailDtoArrayList.add(fromatDetailDto);
                }
            }
            Object valueObject = productResult.get("value");
            if (valueObject instanceof List) {
                List<?> valueList = (List<?>) valueObject;
                for (Object item : valueList) {
                    ProductFormatDto productFormatDto = JSON.toJavaObject(JSON.parseObject(item.toString()), ProductFormatDto.class);
                    productFormatDtoList.add(productFormatDto);
                }
            }

            // 判断是哪个属性被购买
            for (int j = 0; j < productFormatDtoList.size(); j++) {
                System.out.println(productFormatDtoList.get(j));
                StringBuilder sku = new StringBuilder(productFormatDtoList.get(j).getValue1());
                if (!ObjectUtils.isEmpty(productFormatDtoList.get(j).getValue2())) {
                    sku.append(",").append(productFormatDtoList.get(j).getValue2());
                }
                // 产品属性一致 减少该产品的库存
                if (sku.toString().equals(reqVo.getProductInfo().get(i).getValueStr())) {
                    int afterNum = (int) (productFormatDtoList.get(j).getStock() - reqVo.getProductInfo().get(i).getNumber());
                    if (afterNum < 0) {
                        throw exception(PRODUCT_OUT_OF_STOCK);
                    }
                    productFormatDtoList.get(j).setStock(afterNum);
                    productDto.setStock(productDto.getStock() - reqVo.getProductInfo().get(i).getNumber());
                    productDto.setSales(productDto.getSales() + reqVo.getProductInfo().get(i).getNumber());
                    StoreProductDO productDO = BeanUtil.toBean(productDto, StoreProductDO.class);
                    productMapper.updateById(productDO);

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("attr", fromatDetailDtoArrayList);
                    map.put("value", productFormatDtoList);
                    storeProductAttrResultService.insertYxStoreProductAttrResult(map, productDto.getId());

                    productAttrValueMapper.decStockIncSales(Math.toIntExact(reqVo.getProductInfo().get(i).getNumber()), productDto.getId(), sku.toString());
                }
            }
        }
        WxPayOrderRespVO respVO = new WxPayOrderRespVO();
        respVO.setOrderNo(orderNo);
        productOrderDO.setTotalPrice(totalPrice);
        // 生成商品订单
        productOrderMapper.insert(productOrderDO);
        Long tenantId = TenantContextHolder.getTenantId();
        // 如果获取不到租户编号，则尝试使用登陆用户的租户编号
        if (tenantId == null) {
            LoginUser user = SecurityFrameworkUtils.getLoginUser();
            tenantId = user.getTenantId();
        }
        WxPayOrderInfo wxPayOrderInfo = new WxPayOrderInfo();
        wxPayOrderInfo.setOrderNo(orderNo).setTenantId(tenantId);
        redisTemplate.opsForValue().set(String.format(WX_PRODUCT_PAY_ORDER, orderNo), wxPayOrderInfo, 1, TimeUnit.DAYS);
        return payOrderService.create(respVO, uid, orderNo, null, roomInfo.getStoreId(), AppEnum.order_pay_type.WEIXIN.getValue(), "商品购买订单", totalPrice, "商品购买订单", "/pages/productOrder/productOrder");
    }

    @Override
    @Transactional
    public void cancelPay(AppCancelPayReqVO reqVo, boolean isAdmin) {
        ProductOrderDO productOrderDO = productOrderMapper.selectById(reqVo.getOrderId());
        if (ObjectUtils.isEmpty(productOrderDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (isAdmin) {
            //管理员取消 检查权限
            storeInfoService.checkPermission(productOrderDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        } else {
            //只能取消自己的订单
            if (!productOrderDO.getCreator().equals(String.valueOf(getLoginUserId()))) {
                throw exception(OPRATION_ERROR);
            }
        }
        if (productOrderDO.getStatus().intValue() == 1) {
            //发送企业微信提醒
            workWxService.sendProductOrderCancelMsg(productOrderDO.getStoreId(), productOrderDO.getRoomId(), productOrderDO.getUserId(), productOrderDO.getOrderNo(), productOrderDO.getCreateTime());
        }
        if (productOrderDO.getPayPrice().intValue() > 0) {
            PayOrderDO payOrderDO = payOrderService.getByOrderNo(productOrderDO.getOrderNo());
            payOrderService.refund(payOrderDO, payOrderDO.getPrice(), "订单取消");
            //状态改成取消
            productOrderDO.setStatus(3L);
            productOrderMapper.updateById(productOrderDO);
        } else {
            //状态改成取消
            productOrderDO.setStatus(3L);
            productOrderMapper.updateById(productOrderDO);
        }

    }

    @Override
    public PageResult<AppUserOrderPageRespVO> userOrderByPage(AppUserOrderPageReqVO reqVO) {
        IPage<AppUserOrderPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        productOrderMapper.userOrderByPage(page, reqVO, getLoginUserId(), null);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            page.getRecords().forEach(x -> {
                if (!ObjectUtils.isEmpty(x.getProductInfoJson())) {
                    x.setProductInfoVoList(JSONArray.parseArray(x.getProductInfoJson(), ProductInfoVo.class));
                }
            });
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<AppHaveOrderStoreRespVO> selectHaveOrderStore() {
        return productOrderMapper.selectHaveOrderStore(getLoginUserId());
    }

    @Override
    @Transactional
    public WxPayOrderRespVO pay(Long orderId) {
        ProductOrderDO productOrderDO = productOrderMapper.selectById(orderId);
        WxPayOrderRespVO respVO = new WxPayOrderRespVO();

        payOrderService.create(respVO, getLoginUserId(), productOrderDO.getOrderNo(), null, productOrderDO.getStoreId()
                , AppEnum.order_pay_type.WEIXIN.getValue(), "商品购买订单", productOrderDO.getTotalPrice(), "商品购买订单", "/pages/productOrder/productOrder");

        Long tenantId = TenantContextHolder.getTenantId();
        // 如果获取不到租户编号，则尝试使用登陆用户的租户编号
        if (tenantId == null) {
            LoginUser user = SecurityFrameworkUtils.getLoginUser();
            tenantId = user.getTenantId();
        }
        WxPayOrderInfo wxPayOrderInfo = new WxPayOrderInfo();
        wxPayOrderInfo.setOrderNo(productOrderDO.getOrderNo()).setTenantId(tenantId);
        redisTemplate.opsForValue().set(String.format(WX_PRODUCT_PAY_ORDER, productOrderDO.getOrderNo()), wxPayOrderInfo, 1, TimeUnit.DAYS);
        payOrderService.retainPartnerContext(productOrderDO.getOrderNo());
        return respVO;
    }


    @Override
    public PageResult<AppUserOrderPageRespVO> managerProductOrder(AppUserOrderPageReqVO reqVO) {
        IPage<AppUserOrderPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        productOrderMapper.userOrderByPage(page, reqVO, null, reqVO.getStoreId());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            page.getRecords().forEach(x -> {
                if (!ObjectUtils.isEmpty(x.getProductInfoJson())) {
                    x.setProductInfoVoList(JSONArray.parseArray(x.getProductInfoJson(), ProductInfoVo.class));
                }
            });
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void finishOrder(Long id) {
        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setOrderId(id);
        productOrderDO.setStatus(2L);
        productOrderMapper.updateById(productOrderDO);
    }

    @Override
    public AppUserOrderPageRespVO orderInfo(Long orderId) {
        ProductOrderDO productOrderDO = productOrderMapper.selectById(orderId);
        AppUserOrderPageRespVO bean = BeanUtil.toBean(productOrderDO, AppUserOrderPageRespVO.class);
        bean.setUserPhone(bean.getUserPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        bean.setProductInfoVoList(JSONObject.parseArray(productOrderDO.getProductInfo(), ProductInfoVo.class));
        return bean;
    }

    @Override
    public String getPhone(Long orderId) {
        return productOrderMapper.getPhone(orderId);
    }

    /**
     * 商品订单处理
     */
    @Override
    public void paymentOrder(PayOrderDO payOrderDO) {
        ProductOrderDO productOrderDO = productOrderMapper.selectByOrderNo(payOrderDO.getOrderNo());
        if (ObjectUtils.isEmpty(productOrderDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        productOrderDO.setPayTime(new Date());
        productOrderDO.setStatus(1L); // 已支付
        productOrderDO.setPayPrice(payOrderDO.getPrice());//支付金额
        //下单成功后发送企业微信提醒 告知是哪个房间购买了多少个商品
        productOrderMapper.updateById(productOrderDO);
        StringBuffer content = new StringBuffer();
        JSONArray objects = JSON.parseArray(productOrderDO.getProductInfo());
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            content.append("`").append(jsonObject.getString("name")).append(" 数量 ").append(jsonObject.getInteger("number")).append("`\n\n");
        }
        //发送企业微信提醒
        workWxService.sendProductOrderMsg(productOrderDO.getStoreId(), productOrderDO.getRoomId(), productOrderDO.getUserId(), content.toString(), productOrderDO.getCreateTime());
        //发送语音提醒
        iotService.runSound(productOrderDO.getStoreId(), "顾客已购买商品,请及时处理");
    }


    private String getOrderNo() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDate = currentDateTime.format(dateFormatter);
        Random random = new Random();
        int randomNum = random.nextInt(100000000);
        String randomNumString = String.format("%08d", randomNum);
        return "SH" + currentDate + randomNumString;
    }

}
