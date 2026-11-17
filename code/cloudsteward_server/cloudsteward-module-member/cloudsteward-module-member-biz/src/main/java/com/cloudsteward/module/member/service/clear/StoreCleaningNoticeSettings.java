package com.cloudsteward.module.member.service.clear;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.enums.UserTypeEnum;
import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.member.StoreWxpayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import com.cloudsteward.module.system.enums.permission.RoleCodeEnum;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.service.clear.CleaningNoticeService.*;

@Service
public class StoreCleaningNoticeSettings {
    private static final ErrorCode INVALID = new ErrorCode(1004999013, "{}");

    @Resource private StoreInfoMapper storeInfoMapper;
    @Resource private StoreWxpayConfigMapper storeWxpayConfigMapper;
    @Resource private ConfigApi configApi;
    @Resource private CleaningNoticeService cleaningNotices;
    @Resource private SecurityFrameworkService security;

    public Settings get(Long storeId) {
        StoreInfoDO store = requireStore(storeId);
        MiniappConfigVO app = storeWxpayConfigMapper.getMiniappConfig(store.getTenantId());
        CleaningNoticeService.Template template = cleaningNotices.getTemplate(app);
        String webhook = store.getOrderWebhook();
        return new Settings(cleaningNotices.useMiniapp(storeId) ? MINIAPP : WECOM,
                StringUtils.hasText(webhook) && webhook.startsWith("https://qyapi.weixin.qq.com/"),
                app == null ? null : app.getMiniappId(), canEditTemplate(),
                template == null ? null : template.id, template == null ? Collections.emptyMap() : template.fields);
    }

    public void saveChannel(Long storeId, String channel) {
        StoreInfoDO store = requireStore(storeId);
        if (MINIAPP.equals(channel)) {
            MiniappConfigVO app = storeWxpayConfigMapper.getMiniappConfig(store.getTenantId());
            if (app == null || !StringUtils.hasText(app.getMiniappSecret()) || cleaningNotices.getTemplate(app) == null) {
                throw exception(INVALID, "请先配置门店所属小程序及保洁订阅消息模板");
            }
        } else if (!WECOM.equals(channel)) {
            throw exception(INVALID, "请选择企业微信或微信小程序订阅消息");
        }
        configApi.updateConfigValue(CHANNEL_PREFIX + storeId, channel);
    }

    public void saveTemplate(Long storeId, String templateId, Map<String, String> fields) {
        StoreInfoDO store = requireStore(storeId);
        if (!canEditTemplate()) {
            throw exception(INVALID, "仅超级管理员可以修改小程序共用模板");
        }
        MiniappConfigVO app = storeWxpayConfigMapper.getMiniappConfig(store.getTenantId());
        if (app == null || !StringUtils.hasText(app.getMiniappId())) {
            throw exception(INVALID, "门店所属租户尚未配置小程序 AppID");
        }
        if (!validTemplateId(templateId) || fields == null || fields.isEmpty()) {
            throw exception(INVALID, "请填写有效的微信订阅模板 ID 和模板字段");
        }
        fields.forEach((key, value) -> {
            if (key == null || !validField(key, value)) {
                throw exception(INVALID, "模板字段与业务内容不匹配，请检查字段 " + key);
            }
        });
        JSONObject config = new JSONObject();
        config.put("templateId", templateId);
        config.put("fields", fields);
        String value = config.toJSONString();
        if (value.length() > 500) {
            throw exception(INVALID, "模板配置超过长度限制，请检查字段编号");
        }
        configApi.updateConfigValue(CONFIG_PREFIX + app.getMiniappId(), value);
    }

    private boolean canEditTemplate() {
        return security.hasRole(RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    private StoreInfoDO requireStore(Long storeId) {
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        if (user == null || !UserTypeEnum.ADMIN.getValue().equals(user.getUserType())) {
            throw exception(INVALID, "请使用有门店管理权限的后台账号");
        }
        StoreInfoDO store = storeId == null ? null : storeInfoMapper.selectById(storeId);
        if (store == null) {
            throw exception(INVALID, "门店不存在或无权访问");
        }
        return store;
    }

    @Value
    public static class Settings {
        String channel;
        boolean wecomConfigured;
        String appId;
        boolean canEditTemplate;
        String templateId;
        Map<String, String> fields;
    }
}
