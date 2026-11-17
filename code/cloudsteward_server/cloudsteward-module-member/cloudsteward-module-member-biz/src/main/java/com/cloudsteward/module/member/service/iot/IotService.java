package com.cloudsteward.module.member.service.iot;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.tenant.core.aop.TenantIgnore;
import com.cloudsteward.framework.tenant.core.util.TenantUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.controller.app.order.vo.OrderSaveReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppRoomListVO;
import com.cloudsteward.module.member.dal.dataobject.facerecord.FaceRecordDO;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.dal.dataobject.groupPay.GroupPayInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.pkginfo.PkgInfoDO;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.dataobject.user.MemberUserDO;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.facerecord.FaceRecordMapper;
import com.cloudsteward.module.member.dal.mysql.groupPay.GroupPayInfoMapper;
import com.cloudsteward.module.member.dal.mysql.merchantaccount.MerchantAccountMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.pkginfo.PkgInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.forest.IotDeviceClient;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.iot.device.*;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayOrderInfoVO;
import com.cloudsteward.module.member.service.iot.platform.IotRoomListRespVO;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.user.AppUserService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

@Slf4j
@Component
public class IotService {

    @Resource
    private ConfigApi configApi;

    public String clientId() {
        return configApi.getConfigValueByKey("iot.clientId");
    }

    public String secret() {
        return configApi.getConfigValueByKey("iot.secret");
    }

    public boolean groupPay() {
        return configApi.getConfigValueByKey("iot.groupPay").equals("1");
    }

    @Resource
    private IotDeviceClient iotDeviceClient;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private FaceRecordMapper faceRecordMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private StoreInfoMapper storeInfoMapper;
    @Resource
    private WorkWxService workWxService;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private GroupPayInfoMapper groupPayInfoMapper;

    @Resource
    private PkgInfoMapper pkgInfoMapper;
    @Resource
    @Lazy
    private AppOrderService appOrderService;

    @Resource
    @Lazy
    private AppUserService appUserService;

    @Resource
    private MerchantAccountMapper merchantAccountMapper;

    @Resource
    @Lazy
    private DeviceService deviceService;

    /**
     * 设备控制
     */
    public Boolean control(DeviceControlReqVO reqVO) {
        List<DeviceInfoDO> devices = deviceInfoMapper.getBySn(reqVO.getDeviceNo());
        String gatewaySn = null;
        if (devices != null) {
            for (DeviceInfoDO device : devices) {
                if (ObjectUtils.isEmpty(device.getGatewaySn())) continue;
                if (gatewaySn != null && !gatewaySn.equals(device.getGatewaySn())) {
                    throw exception(DEVICE_IOT_OP_ERROR, "设备关联网关冲突，请重新同步子设备");
                }
                gatewaySn = device.getGatewaySn();
            }
        }
        if (gatewaySn != null) {
            // The persisted discovery relationship is authoritative for every caller,
            // including customer orders, scheduled shutoff and manual controls.
            reqVO.setGatewayNo(gatewaySn);
        } else if (devices != null && devices.stream().anyMatch(d ->
                Integer.valueOf(2).equals(d.getType()))) {
            throw exception(DEVICE_IOT_OP_ERROR, "电控设备尚未关联网关，请先在设备列表同步网关子设备");
        }
        return sendControl(reqVO);
    }

    /** 开启网关子设备配网，与平台 control(type=201) 请求保持一致。 */
    public Boolean pairGateway(String gatewayNo) {
        DeviceControlReqVO reqVO = new DeviceControlReqVO();
        reqVO.setGatewayNo(gatewayNo);
        reqVO.setType(201);
        return sendControl(reqVO);
    }

    private Boolean sendControl(DeviceControlReqVO reqVO) {
        IotResult<String> resp;
        try {
            resp = iotDeviceClient.control(reqVO, clientId(), secret());
        } catch (com.dtflys.forest.exceptions.ForestNetworkException ex) {
            log.warn("设备控制平台请求失败，deviceNo={}", reqVO.getDeviceNo());
            throw exception(DEVICE_IOT_OP_ERROR, "设备平台请求失败，请检查网关连接后重试");
        }
        if (resp == null || !Integer.valueOf(0).equals(resp.getCode())) {
            throw exception(DEVICE_IOT_OP_ERROR, resp == null ? "设备平台未返回结果" : resp.getMsg());
        }
        return true;
    }

    /**
     * 获取网关子设备列表
     */
    public IotResult<String> getSubDevices(String gatewayNo) {
        GetSubDevicesReqVO reqVO = new GetSubDevicesReqVO();
        reqVO.setGatewayNo(gatewayNo);
        return iotDeviceClient.getSubDevices(reqVO, clientId(), secret());
    }

    /**
     * 新版本回调接收 建议用此方式
     *
     * @param json
     * @param response
     */
    public JSONObject iotPlatform(JSONObject json, HttpServletResponse response) {
        if (json.containsKey("type") && json.containsKey("t") && json.containsKey("sign")) {
            String type = json.getString("type");
            String sign = json.getString("sign");
            long t = json.getLong("t");
            //签名校验
            String newSign = SecureUtil.md5(secret() + t);
            if (newSign.equals(sign)) {
                JSONObject data = json.getJSONObject("data");
                switch (type) {
                    case "online":
                        //设备上线/下线
                        //有版本号和信号值的  也取出进行更新
                        Integer rssi = null;
                        String cversion = null;
                        String deviceData = null;
                        if (data.containsKey("info")) {
                            JSONObject info = data.getJSONObject("info");
                            if (info.containsKey("rssi")) {
                                rssi = info.getInteger("rssi");
                            }
                            if (info.containsKey("cversion")) {
                                cversion = info.getString("cversion");
                            }
                            if (info.containsKey("deviceData")) {
                                deviceData = info.getString("deviceData");
                            }
                        }
                        deviceInfoMapper.updateInfoBySN(data.getString("deviceSn"), data.getInteger("status"), rssi, cversion, deviceData);
                        break;
                    case "face_record":
                        //人脸识别记录回调
                        callBackFace(data);
                        break;
                    case "call":
                        //客户呼叫
                        callTask(data);
                        break;
                    case "getRoomList":
                        //获取房间列表
                        return getRoomList(data);
                    case "sendBooking":
                        // 用户发起预定
                        return sendBooking(data);
                    case "syncBookingResult":
                        // 预订结果同步
                        return syncBookingResult(data);
                    case "syncBookingStart":
                        //通知三方核销 （开门）
                        return syncBookingStart(data);
                    case "updateBookingResult":
                        // 核销状态同步
                        return updateBookingResult(data);
                    case "getBookingStatus":
                        // 订单状态查询
                        return getBookingStatus(data);
                    case "refundBooking":
                        // 用户申请退款（需业务系统审核）
                        return refundBooking(data);
                    case "cancelBookingResult":
                        // 用户取消预订结果通知
                        return cancelBookingResult(data);
                    case "platform_order_submit":
                        // 平台下单  取消订单复用syncBookingResult
                        return platformOrderSubmit(data);
                    case "platform_order_open":
                        // 开门
                        return platformOrderOpen(data);
                    case "update_yeepay_status":
                        // 修改易宝申请状态
                        return updateYeepayStatus(data);
                }
            } else {
                log.error("签名不匹配,{}", sign);
//                throw exception(IOT_SIGN_ERROR);
            }
        }
        return null;
    }

    /**
     * 修改易宝申请状态
     *
     * @param data
     * @return
     */
    private JSONObject updateYeepayStatus(JSONObject data) {
        String requestNo = data.getString("requestNo");
        String remark = data.getString("remark");
        Integer status = data.getInteger("status");
        merchantAccountMapper.updateByRequestNo(requestNo, remark, status);
        return new JSONObject();
    }

    /**
     * 平台开门
     *
     * @param data
     * @return
     */
    private JSONObject platformOrderOpen(JSONObject data) {
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单不能为空");
        }
        IotGroupPayOrderInfoVO orderInfoVO = orderInfoMapper.getInfoByOrderNo(data.getString("orderId"));
        if (!ObjectUtils.isEmpty(orderInfoVO)) {
            //订单结束时间在现在之前  说明已经结束了
            if (orderInfoVO.getEndTime().before(new Date())) {
                throw exception(ORDER_STATUS_FINISH_OPRATION_ERROR);
            }
            //因为要新增设备操作记录 所以要模拟租户
            TenantUtils.execute(orderInfoVO.getTenantId(), () -> {
                appOrderService.openStoreDoor(orderInfoVO.getOrderKey());
                appOrderService.openRoomDoor(orderInfoVO.getOrderKey());
            });
            return null;
        } else {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    /**
     * 平台下单
     *
     * @param data
     * @return
     */
    private JSONObject platformOrderSubmit(JSONObject data) {
        JSONObject result = new JSONObject();
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId")) || !data.getString("orderId").startsWith("PL")) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "订单号不能为空");
        }
        if (!data.containsKey("orderKey") || ObjectUtils.isEmpty(data.getString("orderKey"))) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "订单号不能为空");
        }
        if (!data.containsKey("deviceSn") || ObjectUtils.isEmpty(data.getString("deviceSn"))) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "设备编码不存在");
        }
        if (!data.containsKey("payType") || ObjectUtils.isEmpty(data.getString("payType"))) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "支付方式不能为空");
        }
        if (!data.containsKey("amount") || ObjectUtils.isEmpty(data.getInteger("amount"))) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "订单的价格不能为空");
        }

        String deviceSn = data.getString("deviceSn");
        //通过设备编码 把绑的房间查询出来
        IotDeviceRoomInfoVO deviceRoomVO = deviceInfoMapper.getDeviceRoomVO(deviceSn);
        if (ObjectUtils.isEmpty(deviceRoomVO)) {
            throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "设备未绑定消费场地");
        }
        Long roomId = deviceRoomVO.getRoomId();
        String payType = data.getString("payType");//下单方式 qrPay meituan douyin kuaishou
        Integer pay_type = AppEnum.order_pay_type.PLATFORM.getValue();//默认是平台支付订单
        String orderId = data.getString("orderId");//订单的编号
        Integer amount = data.getInteger("amount");//订单的价格
        Integer orderHour = data.getInteger("orderHour");//订单的小时
        Date beginTime = data.getDate("beginTime");//可选的订单开始时间
        Date endTime = null;
        if (!data.containsKey("beginTime") || ObjectUtils.isEmpty(beginTime)) {
            //取当前时间为开始时间
            beginTime = new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginTime);
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
        if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.DISABLE.getValue()) == 0) {
            throw exception(ORDER_TIME_CHECK_ERROR);
        }
        GroupPayInfoDO groupPayInfoDO = new GroupPayInfoDO();
        //根据不同的付款方式  进行不同的处理
        switch (payType) {
            case "qrPay":
                //付款码模式
                if (!data.containsKey("orderHour") || ObjectUtils.isEmpty(data.getInteger("orderHour"))) {
                    throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "订单的时长不能为空");
                }
                calendar.add(Calendar.HOUR, orderHour);
                endTime = calendar.getTime();
                break;
            case "meituan":
            case "douyin":
            case "kuaishou":
                //团购券模式
                pay_type = AppEnum.order_pay_type.TUANGOU.getValue();//团购支付模式
                //检查团购券
                String groupPayTitle = data.getString("groupPayTitle");//团购券的标题
                String groupPayNo = data.getString("groupPayNo");//团购券
                String groupPayShopId = data.getString("groupPayShopId");//团购商品ID
                //先看团购券是否绑定了套餐
                try {
                    PkgInfoDO pkgInfoDO = pkgInfoMapper.getPkgByShopId(groupPayShopId, roomInfoDO.getStoreId());
                    if (!ObjectUtils.isEmpty(pkgInfoDO)) {
                        //订单时长 取套餐的
                        orderHour = pkgInfoDO.getHours();
                        calendar.add(Calendar.HOUR, orderHour);
                        endTime = calendar.getTime();
                        long orderMinutes = Math.abs(ChronoUnit.MINUTES.between(beginTime.toInstant(), endTime.toInstant()));
                        log.info("团购券:{}，套餐校验模式:{}", groupPayNo, pkgInfoDO.getPkgId());
                        // 走套餐的校验
                        appOrderService.checkPkgUse(pkgInfoDO, false, roomInfoDO.getType(), roomInfoDO.getStoreId(), beginTime, endTime, orderMinutes, Math.toIntExact(roomId));
                    } else {
                        //校验券合法性
                        log.info("团购券:{}，标题校验模式", groupPayNo);
                        orderHour = getTuangouHourByTitle(groupPayTitle);
                        calendar.add(Calendar.HOUR, orderHour);
                        endTime = calendar.getTime();
                        appOrderService.checkGroupNo(groupPayTitle, beginTime, endTime, roomInfoDO.getType(), false, null, null);
                    }
                } catch (Exception e) {
                    //统一返回异常
                    log.error("团购验券异常", e);
                    throw exception(PLATFORM_ORDER_GROUP_CHECK_ERROR, e.getMessage());
//                    throw new RuntimeException(e);
                }
                //新增团购券记录
                //以下操作需要模拟租户进行 否则会丢失租户ID
                TenantUtils.execute(deviceRoomVO.getTenantId(), () -> {
                            Integer groupPayType = 1;//美团1 抖音2 快手3
                            switch (payType) {
                                case "meituan":
                                    groupPayType = 1;
                                    break;
                                case "douyin":
                                    groupPayType = 2;
                                    break;
                                case "kuaishou":
                                    groupPayType = 3;
                                    break;
                            }

                            groupPayInfoDO.setGroupNo(groupPayNo);
                            groupPayInfoDO.setGroupShopId(groupPayShopId);
                            groupPayInfoDO.setStoreId(roomInfoDO.getStoreId());
                            groupPayInfoDO.setGroupPayType(groupPayType);
                            groupPayInfoDO.setGroupName(groupPayTitle);
                            //把单位从分 转化成元
                            groupPayInfoDO.setGroupPayPrice(new BigDecimal(data.getInteger("amount") / 100.0));
                        }
                );
                break;
            default:
                throw exception(PLATFORM_ORDER_SUBMIT_ERROR, "错误的支付方式");
        }
        result.put("beginTime", beginTime);
        result.put("endTime", endTime);
        result.put("roomId", roomId);
        result.put("roomName", roomInfoDO.getRoomName());
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(roomInfoDO.getStoreId());
        result.put("storeName", storeInfoDO.getStoreName());
        //查询出该房间，存在时间交集的订单 并考虑计算清洁时间
        Integer clearTime = storeInfoDO.getClearTime();
        if (roomInfoDO.getJumpClear()) {
            clearTime = 0;
        }
        Integer c = orderInfoMapper.countByPreOrder(roomInfoDO.getRoomId(), clearTime, beginTime, endTime, null);
        if (c > 0) {
            throw exception(ORDER_TIME_CHECK_ERROR);
        }
        //校验通过后创建订单
        OrderSaveReqVO reqVO = new OrderSaveReqVO();
        reqVO.setUserId(0L)//这种下单方式没有用户信息， 所以固定为0
                .setStartTime(beginTime)
                .setEndTime(endTime)
                .setRoomId(roomId)
                .setOrderNo(orderId)
                .setPayType(pay_type)
                .setPrice(amount)
                .setOrderKey(data.getString("orderKey"))
                .setNightLong(false);
        //以下操作需要模拟租户进行 否则会丢失租户ID
        return TenantUtils.execute(deviceRoomVO.getTenantId(), () -> {
            OrderInfoDO orderInfoDO = appOrderService.save(reqVO);
            if (!ObjectUtils.isEmpty(groupPayInfoDO.getGroupNo())) {
                groupPayInfoMapper.insert(groupPayInfoDO);
            }
            result.put("orderId", orderId);
            result.put("msg", "操作成功");
            //然后自动开门，因为开门可能会因为有设备未绑定/不属于自己的设备忘记解绑/网关忙等情况，所以要捕获异常
            try {
                appOrderService.openStoreDoor(orderInfoDO.getOrderKey());
                appOrderService.openRoomDoor(orderInfoDO.getOrderKey());
            } catch (Exception e) {
                log.error("开门异常", e);
                result.put("msg", e.getMessage());
            }
            return result;
        });
    }


    private int getTuangouHourByTitle(String title) {
        String regex = "(\\d+)(个小时|小时|时)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw exception(CHECK_GROUP_NO_TIME_ERROR);
        }
    }

    /**
     * 用户取消预订结果通知
     *
     * @param data
     * @return
     */
    private JSONObject cancelBookingResult(JSONObject data) {
        //这里已经收到了取消的结果  可能是商家同意了取消   也可能是用户那边强制让平台取消
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        IotGroupPayOrderInfoVO orderInfo = orderInfoMapper.getInfoByOrderNo(data.getString("orderId"));
        if (!ObjectUtils.isEmpty(orderInfo)) {
            //因为要新增设备操作记录 所以要模拟租户
            TenantUtils.execute(orderInfo.getTenantId(), () -> {
                //设置订单状态为取消
                orderInfoMapper.updateById(new OrderInfoDO().setOrderId(orderInfo.getOrderId()).setStatus(AppEnum.order_status.CANCEL.getValue()));
                //被取消的订单已开始了  那就触发一下关门
                if (orderInfo.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0) {
                    //orderCount = 0 ，因为这里关电，只要房间有订单  就不要去关共享设备
                    deviceService.closeRoomDoor(null, orderInfo.getStoreId(), orderInfo.getRoomId(), 4, 0);
                }
                appOrderService.flushRoomStatus(orderInfo.getRoomId(), null);
                workWxService.sendYDOrderCancelMsg(orderInfo.getStoreId(), orderInfo.getRoomId(), orderInfo.getOrderNo());
            });
            return null;
        } else {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    /**
     * 用户申请退款（需业务系统审核）
     *
     * @param data
     * @return
     */
    @TenantIgnore//可以忽略租户
    private JSONObject refundBooking(JSONObject data) {
        //这里主要是做通知  发消息提醒
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        OrderInfoDO orderInfoDO = orderInfoMapper.getByOrderNo(data.getString("orderId"));
        if (!ObjectUtils.isEmpty(orderInfoDO)) {
            String reason = data.getString("reason");
            workWxService.sendYDOrderCancelAuthMsg(orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), orderInfoDO.getOrderNo(), reason);
            return null;
        } else {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    /**
     * 订单状态查询
     *
     * @param data
     * @return
     */
    @TenantIgnore//可以忽略租户
    private JSONObject getBookingStatus(JSONObject data) {
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        JSONObject result = new JSONObject();
        result.put("orderId", data.getString("orderId"));
        result.put("status", 1);//默认不允许退款
        OrderInfoDO orderInfoDO = orderInfoMapper.getByOrderNo(data.getString("orderId"));
        if (!ObjectUtils.isEmpty(orderInfoDO)) {
            //未开始或者被取消了(管理员取消或者用户自己小程序点错了取消）允许退款     否则禁止退款
            if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 0
                    || orderInfoDO.getStatus().compareTo(AppEnum.order_status.CANCEL.getValue()) == 0) {
                result.put("status", 1);//允许退款
            } else {
                result.put("status", 2);//不允许退款
            }
        }
        return result;
    }

    /**
     * 用户核销 （开门）
     *
     * @param data
     * @return
     */
    private JSONObject syncBookingStart(JSONObject data) {
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        IotGroupPayOrderInfoVO orderInfoVO = orderInfoMapper.getInfoByOrderNo(data.getString("orderId"));
        if (!ObjectUtils.isEmpty(orderInfoVO)) {
            //因为要新增设备操作记录 所以要模拟租户
            TenantUtils.execute(orderInfoVO.getTenantId(), () -> {
                appOrderService.openStoreDoor(orderInfoVO.getOrderKey());
                appOrderService.openRoomDoor(orderInfoVO.getOrderKey());
            });
            return null;
        } else {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    /**
     * 核销状态同步
     *
     * @param data
     * @return
     */
    @TenantIgnore
    private JSONObject updateBookingResult(JSONObject data) {
        //暂时不需要处理
        return null;
    }

    /**
     * 预订结果同步
     *
     * @param data
     * @return
     */
    @TenantIgnore//可以忽略租户
    private JSONObject syncBookingResult(JSONObject data) {
        //主要处理预定失败的情况  把订单给关闭
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        Integer status = data.getInteger("status");
        //只处理预定失败
        if (status.compareTo(3) == 0) {
            OrderInfoDO orderInfo = orderInfoMapper.getByOrderNo(data.getString("orderId"));
            if (!ObjectUtils.isEmpty(orderInfo) && orderInfo.getStatus().compareTo(AppEnum.order_status.CANCEL.getValue()) != 0) {
                //只能取消未开始 进行中
                if (orderInfo.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 0
                        || orderInfo.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0
                ) {
                    orderInfoMapper.updateById(new OrderInfoDO().setOrderId(orderInfo.getOrderId()).setStatus(AppEnum.order_status.CANCEL.getValue()));
                    //刷新房间状态
                    appOrderService.flushRoomStatus(orderInfo.getRoomId(), null);
                    //异步发送微信通知
                    workWxService.sendYDOrderCancelMsg(orderInfo.getStoreId(), orderInfo.getRoomId(), orderInfo.getOrderNo());
                }
            }
        }
        //此接口不需要返回数据
        return null;
    }

    /**
     * 开始预定
     *
     * @param data
     * @return
     */
    private JSONObject sendBooking(JSONObject data) {
        if (!data.containsKey("orderId") || ObjectUtils.isEmpty(data.getString("orderId"))) {
            throw exception(IOT_PARAMS_ERROR, "订单ID不能为空");
        }
        if (!data.containsKey("phone") || ObjectUtils.isEmpty(data.getString("phone"))) {
            throw exception(IOT_PARAMS_ERROR, "手机号不能为空");
        }
        Long roomId = data.getLong("roomId");
        AppRoomListVO roomInfo = roomInfoMapper.getInfoById(roomId);
        if (ObjectUtils.isEmpty(roomInfo)) {
            throw exception(IOT_PARAMS_ERROR, "系统不存在的房间" + roomId);
        }
        //以下操作需要模拟租户进行 否则会丢失租户ID
        return TenantUtils.execute(roomInfo.getTenantId(), () -> {
            String phone = data.getString("phone").trim();
            //根据手机号查询出用户
            MemberUserDO user = appUserService.getUserByMobile(phone);
            if (ObjectUtils.isEmpty(user)) {
                //用户不存在则自动创建
                user = appUserService.createUserIfAbsent(phone, getClientIP());
            }
            String orderId = data.getString("orderId");
            Date beginTime = data.getDate("beginTime");
            Date endTime = data.getDate("endTime");
            RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomInfo.getRoomId());
            StoreInfoDO storeInfoDO = storeInfoMapper.selectById(roomInfoDO.getStoreId());
            //查询出该房间，存在时间交集的订单 并考虑计算清洁时间
            Integer clearTime = storeInfoDO.getClearTime();
            if (roomInfoDO.getJumpClear()) {
                clearTime = 0;
            }
            Integer c = orderInfoMapper.countByPreOrder(roomInfoDO.getRoomId(), clearTime, beginTime, endTime, null);
            if (c > 0) {
                throw exception(ORDER_TIME_CHECK_ERROR);
            }
            //校验通过后创建订单
            OrderSaveReqVO reqVO = new OrderSaveReqVO();
            reqVO.setUserId(user.getId())
                    .setStartTime(beginTime)
                    .setEndTime(endTime)
                    .setRoomId(roomId)
                    .setOrderNo(orderId)
                    .setPayType(AppEnum.order_pay_type.YUDING.getValue())
                    .setPrice(data.getInteger("amount"))
                    .setNightLong(false);
            appOrderService.save(reqVO);
            JSONObject result = new JSONObject();
            result.put("orderId", orderId);
            result.put("phone", phone);
            result.put("roomId", roomInfo.getRoomId());
            result.put("roomName", roomInfo.getRoomName());
            return result;
        });
    }

    /**
     * 获取房间列表数据
     *
     * @param data
     * @return
     */
    private JSONObject getRoomList(JSONObject data) {
        if (!data.containsKey("storeId") || data.getLong("storeId") == null) {
            return null;
        }
        Long storeId = data.getLong("storeId");
        List<IotRoomListRespVO> iotRoomList = roomInfoMapper.getIotRoomList(storeId);
        JSONObject result = new JSONObject();
        result.put("list", iotRoomList);
        return result;
    }

    private void callBackFace(JSONObject data) {
        //查找出设备
        IotDeviceRoomInfoVO deviceRoomVO = deviceInfoMapper.getDeviceRoomVO(data.getString("deviceSn"));
        //把照片url转成base64编码
        String base64Image = convertImageToBase64(data.getString("photoUrl"));
        FaceRecordDO faceRecordDO = new FaceRecordDO().setStoreId(deviceRoomVO.getStoreId()).setFaceId(data.getString("faceId")).setDeviceSn(data.getString("deviceSn")).setAdmitGuid(data.getString("admitGuid")).setPhotoUrl(data.getString("photoUrl")).setPhotoData(base64Image).setShowTime(new Date(data.getLong("showTime"))).setType(data.getInteger("type"));
        //模拟租户
        TenantUtils.execute(deviceRoomVO.getTenantId(), () -> {
            faceRecordMapper.insert(faceRecordDO);
        });
    }


    /**
     * 顾客呼叫处理
     *
     * @param data
     */
    private void callTask(JSONObject data) {
        String deviceSn = data.getString("deviceSn");
        //通过设备编号找出该设备所在门店的喇叭编号
        IotDeviceRoomInfoVO deviceRoomVO = deviceInfoMapper.getDeviceRoomVO(deviceSn);
        //如果房间现在是没有订单的  不执行指令
        int count = orderInfoMapper.countByRoomCurrent(deviceRoomVO.getRoomId(), null);
        if (count == 1) {
            String roomName = ObjectUtils.isEmpty(deviceRoomVO.getRoomCallName()) ? deviceRoomVO.getRoomName() : deviceRoomVO.getRoomCallName();
            String callType = data.getString("callType");
            if (!ObjectUtils.isEmpty(deviceRoomVO)) {
                String tts = getTTSByCallType(callType, roomName);
                //模拟租户
                TenantUtils.execute(deviceRoomVO.getTenantId(), () -> {
                    List<IotDeviceRoomInfoVO> storeVoiceList = deviceInfoMapper.getStoreVoice(deviceRoomVO.getStoreId());
                    if (!CollectionUtils.isEmpty(storeVoiceList)) {
                        storeVoiceList.forEach(x -> {
                            runSound(x.getStoreId(), tts);
                        });
                    }
                    //再异步发送企业微信通知
                    workWxService.sendCallMsg(deviceRoomVO.getStoreId(), tts);
                });
            }
        }
    }

    public String convertImageToBase64(String imageUrl) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建 GET 请求
            HttpGet request = new HttpGet(imageUrl);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            // 获取图片的字节数据
            if (entity != null) {
                byte[] imageBytes = EntityUtils.toByteArray(entity);

                String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                return base64Image;
            }
            return "Error: Image not found!";
        } catch (Exception e) {
            log.error("获取图片异常", e);
            return "Error: " + e.getMessage();
        }
    }

    private String getTTSByCallType(String callType, String roomName) {
        String tts = "";
        switch (callType) {
            case "CALL":
            case "CALL1":
                //呼叫服务员
                tts = roomName + ",顾客,呼叫服务员";
                break;
            case "CALL2":
                //需要换零钱
                tts = roomName + ",顾客,需要换零钱";
                break;
            case "CALL3":
                //需要购买商品
                tts = roomName + ",顾客,需要购买商品";
                break;
            case "CALL4":
                //需要加水
                tts = roomName + ",顾客,需要加水";
                break;
            case "CALL5":
                //需要清洁
                tts = roomName + ",顾客,需要清洁";
                break;
            case "CALL6":
                //需要点餐
                tts = roomName + ",顾客,需要点餐";
                break;
            case "CALL7":
                //需要换现金
                tts = roomName + ",顾客,需要换现金";
                break;
            case "BTN_ON":
                //呼叫服务员
                tts = roomName + ",顾客,呼叫服务员";
                break;
        }
        return tts;
    }

    public void runSound(Long storeId, String cmd) {
        //发送语音提醒
        try {
            List<IotDeviceRoomInfoVO> storeVoiceList = deviceInfoMapper.getStoreVoice(storeId);
            if (!CollectionUtils.isEmpty(storeVoiceList)) {
                storeVoiceList.forEach(x -> {
                    DeviceControlReqVO reqVO = new DeviceControlReqVO();
                    reqVO.setDeviceNo(x.getDeviceSn());
                    reqVO.setType(101); // TTS
                    reqVO.setVoiceMsg(cmd);
                    control(reqVO);
                });
            }
        } catch (Exception e) {
            //忽略发送语音提醒失败的错误
        }
    }


    public static Integer convertToMinutes(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    public Integer getMinuteByDate(Date date) {
        // 使用Calendar来计算分钟数
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); // 使用GMT以确保不受系统时区影响
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        // 计算从午夜开始到指定时间的总分钟数
        return hours * 60 + minutes;
    }


}
