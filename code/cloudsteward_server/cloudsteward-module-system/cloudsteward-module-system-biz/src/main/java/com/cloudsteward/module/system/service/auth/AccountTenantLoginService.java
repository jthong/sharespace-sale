package com.cloudsteward.module.system.service.auth;

import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.framework.tenant.core.util.TenantUtils;
import com.cloudsteward.module.system.controller.admin.auth.vo.AuthAutoLoginReqVO;
import com.cloudsteward.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import com.cloudsteward.module.system.dal.dataobject.user.AdminUserDO;
import com.cloudsteward.module.system.dal.dataobject.tenant.TenantDO;
import com.cloudsteward.module.system.dal.mysql.user.AdminUserMapper;
import com.cloudsteward.module.system.service.tenant.TenantService;
import com.cloudsteward.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;

/** Resolves an account only after password verification. Never publishes tenant directories. */
@Service
public class AccountTenantLoginService {
    public static final ErrorCode ORGANIZATION_REQUIRED = new ErrorCode(1002999001, "账号属于多个组织，请补充所属组织名称");
    @Resource private AdminUserMapper users;
    @Resource private AdminUserService userService;
    @Resource private AdminAuthService authService;
    @Resource private TenantService tenants;
    @Resource private com.cloudsteward.module.system.service.logger.LoginLogService loginLogs;

    public AuthLoginRespVO login(AuthAutoLoginReqVO request) {
        List<AdminUserDO> matches = new ArrayList<>();
        for (AdminUserDO user : users.selectLoginCandidates(request.getUsername())) {
            if (!userService.isPasswordMatch(request.getPassword(), user.getPassword())) continue;
            if (StringUtils.hasText(request.getOrganizationName())) {
                TenantDO tenant = tenants.getTenant(user.getTenantId());
                if (tenant == null || !request.getOrganizationName().trim().equals(tenant.getName())) continue;
            }
            matches.add(user);
        }
        if (matches.isEmpty()) {
            com.cloudsteward.module.system.api.logger.dto.LoginLogCreateReqDTO log = new com.cloudsteward.module.system.api.logger.dto.LoginLogCreateReqDTO();
            log.setLogType(com.cloudsteward.module.system.enums.logger.LoginLogTypeEnum.LOGIN_USERNAME.getType());
            log.setUserType(2);log.setUsername(request.getUsername());
            log.setUserIp(com.cloudsteward.framework.common.util.servlet.ServletUtils.getClientIP());
            log.setUserAgent(com.cloudsteward.framework.common.util.servlet.ServletUtils.getUserAgent());
            log.setResult(com.cloudsteward.module.system.enums.logger.LoginResultEnum.BAD_CREDENTIALS.getResult());
            loginLogs.createLoginLog(log);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (matches.size() != 1) throw exception(ORGANIZATION_REQUIRED);
        AdminUserDO user = matches.get(0);
        // This check remains active even in legacy installations with the tenant SQL switch off.
        tenants.validTenant(user.getTenantId());
        return TenantUtils.execute(user.getTenantId(), () -> {
            // Retain captcha, disabled-account checks, social binding, login audit and token creation.
            AuthLoginRespVO response = authService.login(request);
            response.setTenantId(user.getTenantId());
            response.setTenantName(tenants.getTenant(user.getTenantId()).getName());
            return response;
        });
    }
}
