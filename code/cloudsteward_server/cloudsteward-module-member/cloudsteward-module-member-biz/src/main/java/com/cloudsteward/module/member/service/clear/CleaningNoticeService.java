package com.cloudsteward.module.member.service.clear;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudsteward.framework.common.enums.UserTypeEnum;
import com.cloudsteward.framework.common.util.date.DateUtils;
import com.cloudsteward.framework.tenant.core.util.TenantUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.controller.app.store.vo.AppRoomListVO;
import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeuser.StoreUserDO;
import com.cloudsteward.module.member.dal.mysql.clearinfo.ClearInfoMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import com.cloudsteward.module.member.service.wx.MyWxService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import com.cloudsteward.module.system.api.social.SocialUserApi;
import com.cloudsteward.module.system.enums.social.SocialTypeEnum;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.OPRATION_ERROR;

@Service
@Slf4j
public class CleaningNoticeService {
    private static final String TASK_PAGE = "packageA/pages/task/task?status=0&fromCleaningNotice=1";
    static final String CONFIG_PREFIX = "wechat.cleaning-notice.";
    static final String CHANNEL_PREFIX = CONFIG_PREFIX + "channel.";
    static final String MINIAPP = "miniapp";
    static final String WECOM = "wecom";
    private static final String REDIS_PREFIX = "member:cleaning-notice:";
    private static final ZoneId STORE_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Resource private ConfigApi configApi;
    @Resource private MyWxService myWxService;
    @Resource private RoomInfoMapper roomInfoMapper;
    @Resource private ClearInfoMapper clearInfoMapper;
    @Resource private OrderInfoMapper orderInfoMapper;
    @Resource private StoreUserMapper storeUserMapper;
    @Resource private SocialUserApi socialUserApi;
    @Resource private StringRedisTemplate stringRedisTemplate;
    @Resource private WorkWxService workWxService;

    public String getTemplateId(Long userId, Integer userType) {
        Template template = getTemplate(getCleanerApp(userId, userType));
        return template == null ? null : template.id;
    }

    public void recordSubscription(Long userId, Integer userType, String templateId) {
        MiniappConfigVO app = getCleanerApp(userId, userType);
        Template template = getTemplate(app);
        if (template == null || !template.id.equals(templateId)) {
            throw exception(OPRATION_ERROR);
        }
        stringRedisTemplate.opsForValue().set(subscriptionKey(app.getMiniappId(), templateId, userId), "1");
    }

    private MiniappConfigVO getCleanerApp(Long userId, Integer userType) {
        if (userId == null || !Objects.equals(userType, UserTypeEnum.MEMBER.getValue())
                || storeUserMapper.getByUserIdAndType(userId, AppEnum.member_user_type.CLEAR.getValue())
                    .stream().noneMatch(user -> Integer.valueOf(0).equals(user.getStatus())
                            && useMiniapp(user.getStoreId()))) {
            return null;
        }
        return myWxService.getMiniAppConfig();
    }

    @Async
    @TransactionalEventListener
    public void onOrderFinished(CleaningOrderFinished event) {
        try {
            TenantUtils.executeIgnore(() -> {
                AppRoomListVO room = roomInfoMapper.getInfoById(event.getRoomId());
                if (room != null && room.getStoreId() != null && room.getTenantId() != null) {
                    TenantUtils.execute(room.getTenantId(), () -> notifyCleaners(event, room));
                }
            });
        } catch (RuntimeException e) {
            log.warn("保洁通知处理失败，房间编号={}，异常类型={}", event.getRoomId(), e.getClass().getSimpleName());
        }
    }

    boolean useMiniapp(Long storeId) {
        if (storeId == null) {
            return false;
        }
        String channel = configApi.getConfigValueByKey(CHANNEL_PREFIX + storeId);
        return channel != null && MINIAPP.equalsIgnoreCase(channel.trim());
    }

    private void notifyCleaners(CleaningOrderFinished event, AppRoomListVO room) {
        Long storeId = room.getStoreId();
        if (!useMiniapp(storeId)) {
            String time = DateUtils.dateToStr(Date.from(event.getFinishedAt()), DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
            workWxService.sendClearMsg(storeId, "订单已结束通知\n>门店名称:" + room.getStoreName()
                    + "\n>房间名称:" + room.getRoomName() + "\n>时间:" + time + "\n");
            return;
        }
        if (event.getTasks().isEmpty()) {
            return;
        }
        MiniappConfigVO app = myWxService.getMiniAppConfig();
        Template template = getTemplate(app);
        if (template == null) {
            log.warn("未配置保洁订阅消息模板，门店编号={}", storeId);
            return;
        }
        List<Long> recipients = storeUserMapper.selectList(new LambdaQueryWrapper<StoreUserDO>()
                .select(StoreUserDO::getUserId)
                .eq(StoreUserDO::getStoreId, storeId)
                .eq(StoreUserDO::getType, AppEnum.member_user_type.CLEAR.getValue())
                .eq(StoreUserDO::getStatus, 0))
                .stream().map(StoreUserDO::getUserId).filter(Objects::nonNull).distinct()
                .filter(id -> Boolean.TRUE.equals(stringRedisTemplate.hasKey(subscriptionKey(app.getMiniappId(), template.id, id))))
                .collect(Collectors.toList());
        if (recipients.isEmpty()) {
            return;
        }
        WxMaService wx = myWxService.initWxMa();
        wx.getWxMaConfig().useStableAccessToken(true);
        for (ClearInfoDO task : event.getTasks()) {
            if (!Objects.equals(task.getStoreId(), storeId) || clearInfoMapper.selectCount(new LambdaQueryWrapper<ClearInfoDO>()
                    .eq(ClearInfoDO::getOrderId, task.getOrderId())
                    .eq(ClearInfoDO::getStoreId, storeId)
                    .eq(ClearInfoDO::getStatus, AppEnum.clear_info_status.DEFAULT.getValue())) == 0) {
                continue;
            }
            List<WxMaSubscribeMessage.MsgData> data = messageData(template, task, event.getFinishedAt(), room);
            for (Long userId : recipients) {
                send(wx, app.getMiniappId(), template.id, task.getOrderId(), userId, data);
            }
        }
    }

    private void send(WxMaService wx, String appId, String templateId, Long orderId, Long userId,
                      List<WxMaSubscribeMessage.MsgData> data) {
        try {
            String openId = socialUserApi.getUserOpenIdByType(userId, SocialTypeEnum.WECHAT_MINI_APP.getType());
            if (!StringUtils.hasText(openId)) {
                return;
            }
            String deliveryKey = REDIS_PREFIX + "sent:" + appId + ":" + templateId + ":" + orderId + ":" + userId;
            if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(deliveryKey, "1", Duration.ofDays(30)))) {
                return;
            }
            WxMaSubscribeMessage message = new WxMaSubscribeMessage();
            message.setToUser(openId);
            message.setTemplateId(templateId);
            message.setPage(TASK_PAGE);
            message.setData(data);
            wx.getMsgService().sendSubscribeMsg(message);
            log.info("保洁提醒已发送，订单编号={}，保洁员编号={}", orderId, userId);
        } catch (WxErrorException e) {
            int code = e.getError().getErrorCode();
            if (code == 43101) {
                log.debug("保洁员暂无可用订阅授权，订单编号={}", orderId);
            } else {
                log.warn("微信拒绝保洁通知，订单编号={}，微信错误码={}", orderId, code);
            }
        } catch (RuntimeException e) {
            log.warn("保洁通知发送失败，订单编号={}，异常类型={}", orderId, e.getClass().getSimpleName());
        }
    }

    private static String subscriptionKey(String appId, String templateId, Long userId) {
        return REDIS_PREFIX + "accepted:" + appId + ":" + templateId + ":" + userId;
    }

    Template getTemplate(MiniappConfigVO app) {
        if (app == null || !StringUtils.hasText(app.getMiniappId())) {
            return null;
        }
        String raw = configApi.getConfigValueByKey(CONFIG_PREFIX + app.getMiniappId());
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            JSONObject config = JSON.parseObject(raw);
            if (Boolean.FALSE.equals(config.getBoolean("enabled"))) {
                return null;
            }
            String id = config.getString("templateId");
            JSONObject configuredFields = config.getJSONObject("fields");
            if (!validTemplateId(id) || configuredFields == null || configuredFields.isEmpty()) {
                throw new IllegalArgumentException("Invalid cleaning notice configuration");
            }
            Map<String, String> fields = new LinkedHashMap<>();
            configuredFields.forEach((key, value) -> {
                if (!(value instanceof String) || !validField(key, (String) value)) {
                    throw new IllegalArgumentException("Invalid cleaning notice field");
                }
                fields.put(key, (String) value);
            });
            return new Template(id, fields);
        } catch (RuntimeException e) {
            log.warn("保洁通知模板配置格式错误，AppID={}", app.getMiniappId());
            return null;
        }
    }

    static boolean validTemplateId(String id) {
        return id != null && id.matches("[A-Za-z0-9_-]{20,100}");
    }

    static boolean validField(String key, String value) {
        if ("finishedAt".equals(value) || "nextBookingAt".equals(value)) {
            return key.matches("(thing|time|date)\\d+");
        }
        if ("orderNo".equals(value)) {
            return key.matches("(thing|character_string)\\d+");
        }
        if ("status".equals(value)) {
            return key.matches("(thing|phrase)\\d+");
        }
        return key.matches("thing\\d+") && Arrays.asList("storeName", "roomName", "remark").contains(value);
    }

    private List<WxMaSubscribeMessage.MsgData> messageData(Template template, ClearInfoDO task,
                                                           Instant finishedAt, AppRoomListVO room) {
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        Instant nextBookingAt = template.fields.containsValue("nextBookingAt")
                ? orderInfoMapper.getByRoomId(room.getRoomId(), task.getOrderId()).stream()
                    .map(OrderInfoDO::getStartTime).filter(Objects::nonNull).map(Date::toInstant)
                    .filter(time -> !time.isBefore(finishedAt)).findFirst().orElse(null)
                : null;
        template.fields.forEach((key, field) -> {
            String value;
            switch (field) {
                case "storeName": value = room.getStoreName(); break;
                case "roomName": value = room.getRoomName(); break;
                case "finishedAt":
                    value = (key.startsWith("date") ? DATE : TIME).format(finishedAt.atZone(STORE_ZONE));
                    break;
                case "nextBookingAt":
                    if (nextBookingAt == null && !key.startsWith("thing")) {
                        return;
                    }
                    value = nextBookingAt == null ? "暂无预约"
                            : (key.startsWith("date") ? DATE : TIME).format(nextBookingAt.atZone(STORE_ZONE));
                    break;
                case "orderNo": value = task.getOrderNo(); break;
                case "status": value = "待保洁"; break;
                default: value = "订单已结束，请查看保洁任务";
            }
            if (!StringUtils.hasText(value)) {
                throw new IllegalArgumentException("Missing cleaning notice field");
            }
            if (key.startsWith("thing") && value.codePointCount(0, value.length()) > 20) {
                value = value.substring(0, value.offsetByCodePoints(0, 20));
            }
            data.add(new WxMaSubscribeMessage.MsgData(key, value));
        });
        return data;
    }

    static final class Template {
        final String id;
        final Map<String, String> fields;

        private Template(String id, Map<String, String> fields) {
            this.id = id;
            this.fields = fields;
        }
    }
}
