package com.cloudsteward.module.member.service.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.common.util.date.DateUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.controller.admin.index.vo.AppConfigReqVO;
import com.cloudsteward.module.member.controller.app.index.vo.*;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreVipConfigListRespVO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.mysql.bannerinfo.BannerInfoMapper;
import com.cloudsteward.module.member.dal.mysql.discountrules.DiscountRulesMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.pkginfo.PkgInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storetemplate.StoreTemplateMapper;
import com.cloudsteward.module.member.dal.mysql.storevipconfig.StoreVipConfigMapper;
import com.cloudsteward.module.member.dal.mysql.user.MemberUserMapper;
import com.cloudsteward.module.member.service.iot.IotGroupPayService;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPaySelectByPhoneReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPaySelectByPhoneRespVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.member.service.index
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/7/28 9:41
 */
@Service
@Validated
@Slf4j
public class IndexServiceImpl implements IndexService {

    public static final int SEVEN_DAY = 7;

    @Resource
    private StoreInfoMapper storeInfoMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private BannerInfoMapper bannerInfoMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private DiscountRulesMapper discountRulesMapper;

    @Resource
    private StoreTemplateMapper storeTemplateMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private IotGroupPayService iotGroupPayService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private PkgInfoMapper pkgInfoMapper;
    @Resource
    private ConfigApi configApi;

    @Resource
    private StoreVipConfigMapper storeVipConfigMapper;

    @Override
    public List<String> getCityList() {
        return storeInfoMapper.getCityList();
    }

    @Override
    public List<AppBannerInfoRespVO> getBannerList() {
        return bannerInfoMapper.getBannerList(1);//1=首页
    }

    @Override
    public PageResult<AppStorePageRespVO> getStorePageList(AppStorePageReqVO reqVO) {
        Long userId = null;
        if (reqVO.isOften()) {
            userId = getLoginUserId();
        }
        if (!ObjectUtils.isEmpty(reqVO.getCityName())) {
            if (reqVO.getCityName().equals("选择城市") || reqVO.getCityName().equals("请选择")) {
                reqVO.setCityName("");
            }
        }
        IPage<AppStorePageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storeInfoMapper.getStorePageList(page, reqVO, userId);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            page.getRecords().forEach(x -> {
                if (!ObjectUtils.isEmpty(x.getDistance())) {
                    x.setDistance(x.getDistance().setScale(2, BigDecimal.ROUND_CEILING));
                }
            });
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }


    @Override
    public AppIndexStoreInfoRespVO getStoreInfo(Long storeId, String lat, String lon) {
        AppIndexStoreInfoRespVO storeInfo = storeInfoMapper.getStoreInfo(storeId, lat, lon);
        if (!ObjectUtils.isEmpty(storeInfo)) {
            storeInfo.setRoomClassList(roomInfoMapper.getClassList(storeId));
            if (!ObjectUtils.isEmpty(storeInfo.getDistance())) {
                storeInfo.setDistance(storeInfo.getDistance().setScale(2, BigDecimal.ROUND_CEILING));
            } else {
                storeInfo.setDistance(new BigDecimal(9999));
            }
            //如果有模板 设置模板图片
            if (!ObjectUtils.isEmpty(storeInfo.getTemplateKey())) {
                storeInfo.setImgList(JSONArray.parseArray(storeTemplateMapper.getImgByKey(storeInfo.getTemplateKey()), String.class));
            }
        }
        return storeInfo;
    }

    @Override
    public List<KeyValue<String, Long>> getStoreList(String name, String cityName) {
        return storeInfoMapper.getStoreListByMember(name, cityName);
    }

    @Override
    public List<KeyValue<String, Long>> getRoomList(Long storeId) {
        return roomInfoMapper.getRoomListByStoreId(storeId);
    }

    @Override
    public List<KeyValue<String, Long>> getRoomListByAdmin(Long storeId) {
        return roomInfoMapper.getRoomListByAdmin(storeId);
    }

    @Override
    public List<AppRoomInfoListRespVO> getRoomInfoList(AppRoomListReqVO reqVO) {
        //如果roomClass是空  则取第一个roomClass
        if (ObjectUtils.isEmpty(reqVO.getRoomClass())) {
            List<Integer> classList = roomInfoMapper.getClassList(reqVO.getStoreId());
            if (!CollectionUtils.isEmpty(classList)) {
                reqVO.setRoomClass(classList.get(0));
            }
        } else {
            //如果传的-1 则获取全部
            if (reqVO.getRoomClass().intValue() == -1) {
                reqVO.setRoomClass(null);
            }
        }
        //获取所有房间信息
        List<AppRoomInfoListRespVO> roomInfoList = storeInfoMapper.getRoomInfoList(reqVO);
        if (!CollectionUtils.isEmpty(roomInfoList)) {
            //查询会员配置
            List<AppStoreVipConfigListRespVO> vipConfig = storeVipConfigMapper.getVipConfig(reqVO.getStoreId());
            //找出所有房间的订单
            List<OrderInfoDO> orderList = orderInfoMapper.getByRoomIds(roomInfoList.stream().map(x -> x.getRoomId()).collect(Collectors.toList()));
            //把订单按照房间id分组
            Map<String, List<OrderInfoDO>> orederMap;
            if (!CollectionUtils.isEmpty(orderList)) {
                orederMap = orderList.stream().collect(Collectors.groupingBy(x -> String.valueOf(x.getRoomId())));
            } else {
                orederMap = new HashMap<>();
            }
            DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            DateTimeFormatter formatterHour = DateTimeFormatter.ofPattern("HH:mm");
            // 获取当前日期
            int hour = LocalDateTime.now().getHour();
//            LocalDate now = LocalDate.now();
            LocalDate currentDate = LocalDate.now();
            Set<String> days = new HashSet<>(5);
            for (int i = 0; i < 5; i++) {
                days.add(currentDate.format(formatterDay));
                currentDate = currentDate.plusDays(1);
            }
            //重新处理不可用时间段的显示
            for (AppRoomInfoListRespVO respVO : roomInfoList) {
                //一天24个 5天就是120个
                List<AppTimeSlotRespVO> timeSlot = new ArrayList<>(120);
                //暂存所有禁用的时间段
                List<AppOrderTimeVO> bookings = new ArrayList<>();
                //找出该房间所有订单
                if (orederMap.containsKey(respVO.getRoomId().toString())) {
                    List<OrderInfoDO> sortOrder = orederMap.get(respVO.getRoomId().toString()).stream().sorted(Comparator.comparing(OrderInfoDO::getStartTime)).collect(Collectors.toList());
                    List<AppOrderTimeVO> orderTimeVOList = new ArrayList<>(sortOrder.size());
                    //把第一个订单的开始和结束时间 设置给房间
                    respVO.setStartTime(sortOrder.get(0).getStartTime());
                    respVO.setEndTime(sortOrder.get(0).getEndTime());
                    sortOrder.forEach(x -> {
                        bookings.add(new AppOrderTimeVO(x.getStartTime(), x.getEndTime()));
                        orderTimeVOList.add(new AppOrderTimeVO(x.getStartTime(), x.getEndTime()));
                    });
                    respVO.setOrderTimeList(orderTimeVOList);
                }
                //每日不可用时间
                LocalTime bstart = null;
                LocalTime bend = null;
                // 禁用时间段列表，包含禁用开始时间和结束时间 new TimeRange("02:00", "08:00")
                if (!ObjectUtils.isEmpty(respVO.getBanTimeStart()) && !ObjectUtils.isEmpty(respVO.getBanTimeStart())) {
                    bstart = LocalTime.parse(respVO.getBanTimeStart());
                    bend = LocalTime.parse(respVO.getBanTimeEnd());
                }
                // 获取从今天起未来5天的小时数
                int totalHours = 5 * 24;
                // 获取当前时间的当天0时
                LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
                // 获取每小时的可用状态
                for (int i = 0; i < totalHours; i++) {
                    LocalDateTime currentHour = currentDateTime.plusHours(i);
                    boolean isAvailable = false;
                    for (AppOrderTimeVO booking : bookings) {
                        if (booking.getStartTime().isBefore(currentHour.plusHours(1)) &&
                                booking.getEndTime().isAfter(currentHour)) {
                            isAvailable = true;
                            break;
                        }
                    }
                    if (null != bstart && null != bend) {
                        // 处理禁用时间跨越两天的情况
                        int currentHourValue = currentHour.getHour();
                        if (bstart.getHour() > bend.getHour()) {
                            if (currentHourValue >= bstart.getHour() || currentHourValue < bend.getHour()) {
                                isAvailable = true;
                            }
                        } else {
                            if (currentHourValue >= bstart.getHour() && currentHourValue < bend.getHour()) {
                                isAvailable = true;
                            }
                        }
                    }
                    // 将小时和可用状态放入结果中
                    String formattedHour = currentHour.format(DateTimeFormatter.ofPattern("HH"));
                    timeSlot.add(new AppTimeSlotRespVO(formattedHour, isAvailable));
                }
                //重新处理当日的，从当前时间的小时开始 设置到次日
                List<AppTimeSlotRespVO> appTimeSlotRespVOS = timeSlot.subList(hour, hour + 24);
                for (int i = 0; i < appTimeSlotRespVOS.size(); i++) {
                    AppTimeSlotRespVO slotRespVO = appTimeSlotRespVOS.get(i);
                    if (slotRespVO.getHour().equals("00")) {
                        slotRespVO = new AppTimeSlotRespVO("次", slotRespVO.getDisable());
                    }
                    timeSlot.set(i, slotRespVO);
                }
                respVO.setTimeSlot(timeSlot);

                List<AppRoomPkgInfoRespVO> pkgList = pkgInfoMapper.getByRoomId(respVO.getStoreId(), respVO.getType(), respVO.getRoomId());
                if (!CollectionUtils.isEmpty(pkgList)) {
                    respVO.setPkgList(pkgList);
                    String pkgName = pkgList.get(0).getPkgName() + " ￥" + pkgList.get(0).getPrice();
                    respVO.setPkgName(pkgName);
                }
                //处理会员价格
                if (!CollectionUtils.isEmpty(vipConfig)) {
                    respVO.setVipPriceList(vipConfig.stream().map(x -> new AppRoomVipPriceRespVO()
                                    .setVipName(x.getVipName())
                                    .setVipLevel(x.getVipLevel())
                                    .setPrice(
                                            respVO.getPrice().multiply(new BigDecimal(x.getVipDiscount())
                                                    .divide(new BigDecimal(100))

                                            )))
                            .collect(Collectors.toList()));
                }
            }
        }
        return roomInfoList;
    }

    @Override
    public AppRoomInfoListRespVO getRoomInfo(Long roomId) {
        AppRoomInfoListRespVO respVO = storeInfoMapper.getRoomInfo(roomId);
        if (ObjectUtils.isEmpty(respVO)) {
            return null;
        }
        if (!ObjectUtils.isEmpty(respVO.getPrePrice())) {
            //单价/60 得到每分钟价格
            BigDecimal minutePrice = respVO.getPrice().divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
            //每分钟价格 * 计费区间 = 区间价格
            BigDecimal unitPrice = minutePrice.multiply(new BigDecimal(respVO.getPreUnit()).setScale(4, RoundingMode.HALF_UP));
            respVO.setPreUnitPrice(unitPrice);
        }
        //找出所有房间的订单
        List<OrderInfoDO> orderList = orderInfoMapper.getByRoomId(roomId, null);
        //把订单按照房间id分组
        Map<Long, List<OrderInfoDO>> orderMap;
        if (!CollectionUtils.isEmpty(orderList)) {
            //把第一个订单的信息设置给房间
            respVO.setStartTime(orderList.get(0).getStartTime());
            respVO.setEndTime(orderList.get(0).getEndTime());
            orderMap = orderList.stream().collect(Collectors.groupingBy(x -> x.getRoomId()));
        } else {
            orderMap = new HashMap<>();
        }
        //每日不可用时间
        LocalTime bstart = null;
        LocalTime bend = null;
        // 禁用时间段列表，包含禁用开始时间和结束时间 new TimeRange("02:00", "08:00")
        if (!ObjectUtils.isEmpty(respVO.getBanTimeStart()) && !ObjectUtils.isEmpty(respVO.getBanTimeStart())) {
            bstart = LocalTime.parse(respVO.getBanTimeStart());
            bend = LocalTime.parse(respVO.getBanTimeEnd());
        }
        //所有禁用的时间段
        List<AppOrderTimeVO> bookings = new ArrayList<>();
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        now = now.withSecond(0);//秒设置为0
        now = now.withNano(0);//纳秒设置为0
        //处理时间占用条
        processTimeSlot(now, respVO, orderMap, bookings, bstart, bend);
        //处理日期选择器
        Integer clearTime = respVO.getClearTime();
        if (respVO.getJumpClear()) {
            clearTime = 0;
        }
        try {
            respVO.setTimeSelectLists(getRoomSelectList(now, respVO.getStatus(), respVO.getClearOpen(), bookings, bstart, bend, clearTime));
        } catch (Exception e) {
            log.error("获取房间时间段列表异常", e);
        }
        //处理会员价格
        processVipPrice(respVO);
        return respVO;
    }


    /**
     * 添加房间时间选项的第一个选项  也就是当前时间
     *
     * @param now
     * @param status
     * @param clearOpen
     * @return
     */
    private AppRoomTimeSelectVO getFirstTimeSelect(LocalDateTime now, Integer status, Boolean clearOpen) {
        AppRoomTimeSelectVO firstTimeSelect = new AppRoomTimeSelectVO()
                .setTime(DateUtils.localDateToStr(now, DateUtils.FORMAT_HOUR_MINUTE))
                .setDate(now).setRemark("现在");
        //房间当前是否可以用？
        int roomStatus = status.intValue();
        //状态 0禁用 1空闲 2待清洁 3使用中 4已预约
        if (roomStatus == 0) {
            firstTimeSelect.setAvailable(false);
            firstTimeSelect.setRemark("禁用");
        } else if (roomStatus == 3 || roomStatus == 4) {
            firstTimeSelect.setAvailable(false);
            firstTimeSelect.setRemark("已售");
        } else if (roomStatus == 2) {
            //判断待清洁是否允许预订
            firstTimeSelect.setAvailable(clearOpen);
            if (!clearOpen) {
                firstTimeSelect.setRemark("禁用");
            }
        } else {
            firstTimeSelect.setAvailable(true);
        }
        return firstTimeSelect;
    }

    /**
     * 获取房间的时间选项列表 5天
     *
     * @param now
     * @param status
     * @param clearOpen
     * @param bookings
     * @param bstart
     * @param bend
     * @return
     */
    private List<AppRoomTimeSelectList> getRoomSelectList(LocalDateTime now, Integer status, Boolean clearOpen,
                                                          List<AppOrderTimeVO> bookings, LocalTime bstart, LocalTime bend,
                                                          Integer clearTime) {
        List<AppRoomTimeSelectList> timeSelectLists = new ArrayList<>(6);
        AppRoomTimeSelectList roomTimeSelectList = new AppRoomTimeSelectList();
        roomTimeSelectList.setName("今日");
        List<AppRoomTimeSelectVO> selectList = new ArrayList<>();
        selectList.add(getFirstTimeSelect(now, status, clearOpen));

        // 调整当前时间到最近的半小时整点
        int minute = now.getMinute();
        if (minute != 0 && minute != 30) {
            now = minute < 30 ? now.withMinute(30) : now.withMinute(0).plusHours(1);
        }

        // 处理6天的时间段（每半小时一个）
        int totalTimeSelect = 6 * 48;
        for (int i = 0; i < totalTimeSelect; i++) {
            LocalDateTime currentTime = now.plusMinutes(i * 30);
            AppRoomTimeSelectVO timeSelectVO = new AppRoomTimeSelectVO().setDate(currentTime);

            // 检查订单占用情况
            boolean disable = false;
            String remark = "";
            for (AppOrderTimeVO booking : bookings) {
                if (booking.getStartTime().isBefore(currentTime.plusMinutes(30)) &&
                        booking.getEndTime().plusMinutes(clearTime).isAfter(currentTime)) {
                    disable = true;
                    remark = "已售";
                    break;
                }
            }

            // 处理禁用时间段（精确到分钟）
            if (!disable && bstart != null && bend != null) {
                LocalTime currentTimeOfDay = currentTime.toLocalTime();

                if (bstart.isBefore(bend)) {
                    // 同一天的禁用时间段
                    if ((currentTimeOfDay.equals(bstart) || currentTimeOfDay.isAfter(bstart)) &&
                            currentTimeOfDay.isBefore(bend)) {
                        disable = true;
                        remark = "禁用";
                    }
                } else {
                    // 跨天的禁用时间段
                    if (currentTimeOfDay.equals(bstart) || currentTimeOfDay.isAfter(bstart) ||
                            currentTimeOfDay.isBefore(bend)) {
                        disable = true;
                        remark = "禁用";
                    }
                }
            }

            timeSelectVO.setAvailable(!disable)
                    .setTime(DateUtils.localDateToStr(currentTime, DateUtils.FORMAT_HOUR_MINUTE));

            // 处理日期切换
            if (!selectList.isEmpty() &&
                    currentTime.getDayOfYear() != selectList.get(selectList.size() - 1).getDate().getDayOfYear()) {
                roomTimeSelectList.setSelectList(selectList);
                timeSelectLists.add(roomTimeSelectList);

                roomTimeSelectList = new AppRoomTimeSelectList();
                roomTimeSelectList.setName(DateUtils.localDateToStr(currentTime, DateUtils.FORMAT_MONTH_DAY));
                selectList = new ArrayList<>();
                timeSelectVO.setRemark(DateUtils.localDateToStr(currentTime, DateUtils.FORMAT_MONTH_DAY));
                timeSelectVO.setHightLight(true);
            } else {
                timeSelectVO.setRemark(remark);
            }
            selectList.add(timeSelectVO);
        }

        // 处理首尾时间段的合并逻辑
        if (timeSelectLists.get(0).getSelectList().size() > 1 &&
                timeSelectLists.get(0).getSelectList().get(0).getTime()
                        .equals(timeSelectLists.get(0).getSelectList().get(1).getTime())) {
            timeSelectLists.get(0).getSelectList().remove(0);
            timeSelectLists.get(0).getSelectList().get(0).setRemark("现在");
        }

        if (timeSelectLists.get(0).getSelectList().size() < 12) {
            timeSelectLists.get(1).getSelectList().addAll(0, timeSelectLists.get(0).getSelectList());
            if (timeSelectLists.size() > 5) {
                timeSelectLists.remove(0);
            }
        } else if (timeSelectLists.size() > 5) {
            timeSelectLists.remove(5);
        }

        return timeSelectLists;
    }

    /**
     * 处理房间会员价格显示
     *
     * @param respVO
     */
    private void processVipPrice(AppRoomInfoListRespVO respVO) {
        //查询会员配置
        List<AppStoreVipConfigListRespVO> vipConfig = storeVipConfigMapper.getVipConfig(respVO.getStoreId());
        //处理会员价格
        if (!CollectionUtils.isEmpty(vipConfig)) {
            respVO.setVipPriceList(vipConfig.stream().map(x -> new AppRoomVipPriceRespVO()
                            .setVipName(x.getVipName())
                            .setVipLevel(x.getVipLevel())
                            .setPrice(
                                    respVO.getPrice().multiply(new BigDecimal(x.getVipDiscount())
                                            .divide(new BigDecimal(100))

                                    )))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * 处理房间的时间条
     *
     * @param now
     * @param respVO
     * @param orderMap
     */

    private void processTimeSlot(LocalDateTime now, AppRoomInfoListRespVO respVO, Map<Long, List<OrderInfoDO>> orderMap, List<AppOrderTimeVO> bookings
            , LocalTime bstart, LocalTime bend) {
        int hour = now.getHour();
        //处理不可用时间段的显示
        //一天24个 5天就是120个
        List<AppTimeSlotRespVO> timeSlot = new ArrayList<>(120);
        //找出该房间所有订单
        if (orderMap.containsKey(respVO.getRoomId())) {
            List<OrderInfoDO> sortOrder = orderMap.get(respVO.getRoomId()).stream().sorted(Comparator.comparing(OrderInfoDO::getStartTime)).collect(Collectors.toList());
            List<AppOrderTimeVO> orderTimeVOList = new ArrayList<>(sortOrder.size());
            sortOrder.forEach(x -> {
                bookings.add(new AppOrderTimeVO(x.getStartTime(), x.getEndTime()));
                orderTimeVOList.add(new AppOrderTimeVO(x.getStartTime(), x.getEndTime()));
            });
            respVO.setOrderTimeList(orderTimeVOList);
        }

        // 获取从今天起未来5天的小时数
        int totalHours = 5 * 24;
        // 获取当前时间的当天0时
        LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        // 获取每小时的可用状态
        for (int i = 0; i < totalHours; i++) {
            LocalDateTime currentHour = currentDateTime.plusHours(i);
            boolean isAvailable = false;
            for (AppOrderTimeVO booking : bookings) {
                if (booking.getStartTime().isBefore(currentHour.plusHours(1)) &&
                        booking.getEndTime().isAfter(currentHour)) {
                    isAvailable = true;
                    break;
                }
            }
            if (null != bstart && null != bend) {
                // 处理禁用时间跨越两天的情况
                int currentHourValue = currentHour.getHour();
                if (bstart.getHour() > bend.getHour()) {
                    if (currentHourValue >= bstart.getHour() || currentHourValue < bend.getHour()) {
                        isAvailable = true;
                    }
                } else {
                    if (currentHourValue >= bstart.getHour() && currentHourValue < bend.getHour()) {
                        isAvailable = true;
                    }
                }
            }
            // 将小时和可用状态放入结果中
            String formattedHour = currentHour.format(DateTimeFormatter.ofPattern("HH"));
            timeSlot.add(new AppTimeSlotRespVO(formattedHour, isAvailable));
        }
        //重新处理当日的，从当前时间的小时开始 设置到次日
        List<AppTimeSlotRespVO> appTimeSlotRespVOS = timeSlot.subList(hour, hour + 24);
        for (int i = 0; i < appTimeSlotRespVOS.size(); i++) {
            AppTimeSlotRespVO slotRespVO = appTimeSlotRespVOS.get(i);
            if (slotRespVO.getHour().equals("00")) {
                slotRespVO = new AppTimeSlotRespVO("次", slotRespVO.getDisable());
            }
            timeSlot.set(i, slotRespVO);
        }
        respVO.setTimeSlot(timeSlot);
    }

    @Override
    public AppSysInfoRespVO getSysInfo() {
        AppSysInfoRespVO respVO = new AppSysInfoRespVO();
        respVO.setVersion(configApi.getConfigValueByKey("app.version"));
        return respVO;
    }

    @Override
    public List<IotGroupPaySelectByPhoneRespVO> selectGroupPayByPhone(IotGroupPaySelectByPhoneReqVO reqVo) {
        //接口即将下线 直接返回空
        return new ArrayList<>();
    }

    @Override
    public AppConfigReqVO getAppConfig() {
        AppConfigReqVO reqVO = new AppConfigReqVO();
        String hostname = configApi.getConfigValueByKey("app.hostname");
        if (ObjectUtils.isEmpty(hostname)) {
            hostname = "api.xxxxxx.com";
            configApi.createConfig("app.hostname", hostname);
        }
        reqVO.setHostname(hostname);

        String clientId = configApi.getConfigValueByKey("iot.clientId");
        if (ObjectUtils.isEmpty(clientId)) {
            clientId = "xxxxxx";
            configApi.createConfig("iot.clientId", clientId);
        }
        reqVO.setClientId(clientId);

        String secret = configApi.getConfigValueByKey("iot.secret");
        if (ObjectUtils.isEmpty(secret)) {
            secret = "xxxxxx";
            configApi.createConfig("iot.secret", secret);
        }
        reqVO.setSecret(secret);

        return reqVO;
    }

    @Override
    @Transactional
    public void saveAppConfig(AppConfigReqVO reqVO) {
        configApi.updateConfigValue("app.hostname", reqVO.getHostname().trim());
        configApi.updateConfigValue("iot.clientId", reqVO.getClientId().trim());
        configApi.updateConfigValue("iot.secret", reqVO.getSecret().trim());
        configApi.initFileLocalCache();
    }

    @Override
    @SneakyThrows
    public JSONArray getRegCode() {
        InputStream is = getClass().getResourceAsStream("/regionTree.json");
        if (is == null) {
            throw new RuntimeException("regionTree.json not found");
        }
        return JSON.parseObject(is, JSONArray.class);
    }

    @Override
    @SneakyThrows
    public JSONArray getBankCode() {
        InputStream is = getClass().getResourceAsStream("/bankList.json");
        if (is == null) {
            throw new RuntimeException("bankList.json not found");
        }
        return JSON.parseObject(is, JSONArray.class);
    }
}
