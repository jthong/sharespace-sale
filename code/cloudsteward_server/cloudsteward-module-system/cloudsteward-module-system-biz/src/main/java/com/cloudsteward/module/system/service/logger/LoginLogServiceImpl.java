package com.cloudsteward.module.system.service.logger;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.cloudsteward.module.system.controller.admin.logger.vo.loginlog.LoginLogExportReqVO;
import com.cloudsteward.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import com.cloudsteward.module.system.convert.logger.LoginLogConvert;
import com.cloudsteward.module.system.dal.dataobject.logger.LoginLogDO;
import com.cloudsteward.module.system.dal.mysql.logger.LoginLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 登录日志 Service 实现
 */
@Service
@Validated
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO reqVO) {
        return loginLogMapper.selectPage(reqVO);
    }

    @Override
    public List<LoginLogDO> getLoginLogList(LoginLogExportReqVO reqVO) {
        return loginLogMapper.selectList(reqVO);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogDO loginLog = LoginLogConvert.INSTANCE.convert(reqDTO);
        loginLogMapper.insert(loginLog);
    }

}
