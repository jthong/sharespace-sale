package com.cloudsteward.framework.idempotent.config;

import com.cloudsteward.framework.idempotent.core.aop.IdempotentAspect;
import com.cloudsteward.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.cloudsteward.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.cloudsteward.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.cloudsteward.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import com.cloudsteward.framework.redis.config.CloudStewardRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = CloudStewardRedisAutoConfiguration.class)
public class CloudStewardIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
