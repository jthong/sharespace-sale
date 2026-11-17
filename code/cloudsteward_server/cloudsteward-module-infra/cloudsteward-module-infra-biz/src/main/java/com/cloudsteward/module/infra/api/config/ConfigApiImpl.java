package com.cloudsteward.module.infra.api.config;

import com.cloudsteward.module.infra.controller.admin.config.vo.ConfigBaseVO;
import com.cloudsteward.module.infra.controller.admin.config.vo.ConfigCreateReqVO;
import com.cloudsteward.module.infra.dal.dataobject.config.ConfigDO;
import com.cloudsteward.module.infra.dal.mysql.config.ConfigMapper;
import com.cloudsteward.module.infra.service.config.ConfigService;
import com.cloudsteward.module.infra.service.file.FileConfigService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.infra.api.config
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/11/24 13:33
 */
@Service
@Validated
public class ConfigApiImpl implements ConfigApi {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private ConfigService configService;

    @Resource
    @Lazy
    private FileConfigService fileConfigService;

    @Override
    public int updateConfigValue(String k, String v) {
        //更新redis缓存
        stringRedisTemplate.opsForValue().set(k, v);
        ConfigDO config = configService.getConfigByKey(k);
        if (ObjectUtils.isEmpty(config)) {
            createConfig(k, v);
            return 1;
        } else {
            return configMapper.updateConfigValue(k, v);
        }
    }

    @Override
    public String getConfigValueByKey(String key) {
        if (stringRedisTemplate.hasKey(key)) {
            return stringRedisTemplate.opsForValue().get(key);
        } else {
            ConfigDO config = configService.getConfigByKey(key);
            if (config != null) {
                stringRedisTemplate.opsForValue().set(key, config.getValue());
                return config.getValue();
            }
            return null;
        }
    }

    @Override
    public void createConfig(String k, String v) {
        ConfigCreateReqVO config = new ConfigCreateReqVO();
        config.setKey(k).setName(k).setValue(v).setCategory("app").setVisible(true);
        configService.createConfig(config);
    }

    @Override
    public void initFileLocalCache() {
        fileConfigService.initLocalCache();
    }


}
