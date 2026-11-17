package com.cloudsteward.module.member.framework;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

/** Keeps merchant store data scoped even in legacy single-tenant deployments. */
@Configuration
public class MemberStoreScopeConfiguration {
    @Bean
    public static BeanPostProcessor memberStoreScope(ObjectProvider<SecurityFrameworkService> security) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String name) {
                if (bean instanceof MybatisPlusInterceptor) {
                    MybatisPlusInterceptor mybatis = (MybatisPlusInterceptor) bean;
                    List<InnerInterceptor> interceptors = new ArrayList<>(mybatis.getInterceptors());
                    // Scope before pagination so both the count and page query are filtered.
                    interceptors.add(0, new TenantLineInnerInterceptor(new ScopeHandler(security)));
                    mybatis.setInterceptors(interceptors);
                }
                return bean;
            }
        };
    }

    static class ScopeHandler implements TenantLineHandler {
        private final Set<String> tables = new HashSet<>(Arrays.asList(
                "member_store_info", "member_room_info", "member_device_info"));
        private final ObjectProvider<SecurityFrameworkService> security;

        ScopeHandler(ObjectProvider<SecurityFrameworkService> security) { this.security = security; }

        @Override
        public Expression getTenantId() {
            LoginUser user = SecurityFrameworkUtils.getLoginUser();
            if (user == null || user.getTenantId() == null) {
                throw new IllegalStateException("商户租户信息缺失");
            }
            return new LongValue(user.getTenantId());
        }

        @Override
        public boolean ignoreTable(String tableName) {
            // Check the table first: permission lookups use system tables and must not recurse.
            if (!tables.contains(tableName.toLowerCase())) return true;
            LoginUser user = SecurityFrameworkUtils.getLoginUser();
            if (user == null || !Integer.valueOf(2).equals(user.getUserType())) return true;
            return security.getObject().hasRole("super_admin");
        }
    }
}
