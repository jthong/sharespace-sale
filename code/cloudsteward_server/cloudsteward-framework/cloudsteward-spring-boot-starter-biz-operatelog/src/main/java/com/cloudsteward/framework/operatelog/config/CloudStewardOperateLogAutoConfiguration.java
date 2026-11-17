package com.cloudsteward.framework.operatelog.config;

import com.cloudsteward.framework.operatelog.core.aop.OperateLogAspect;
import com.cloudsteward.framework.operatelog.core.service.OperateLogFrameworkService;
import com.cloudsteward.framework.operatelog.core.service.OperateLogFrameworkServiceImpl;
import com.cloudsteward.module.system.api.logger.OperateLogApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CloudStewardOperateLogAutoConfiguration {

    @Bean
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

    @Bean
    public OperateLogFrameworkService operateLogFrameworkService(OperateLogApi operateLogApi) {
        return new OperateLogFrameworkServiceImpl(operateLogApi);
    }

}
