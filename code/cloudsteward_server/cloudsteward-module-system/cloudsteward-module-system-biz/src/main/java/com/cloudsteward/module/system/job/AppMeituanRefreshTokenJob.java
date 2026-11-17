package com.cloudsteward.module.system.job;

import com.cloudsteward.framework.quartz.core.handler.JobHandler;
import com.cloudsteward.framework.tenant.core.job.TenantJob;
import com.cloudsteward.module.member.api.user.MemberUserApi;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * app美团刷新token定时任务，用于自动刷新token
 */
@Component
@TenantJob
public class AppMeituanRefreshTokenJob implements JobHandler {
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public String execute(String param) {
        memberUserApi.executeMeituanRefreshTokenJob();
        return "success";
    }

}
