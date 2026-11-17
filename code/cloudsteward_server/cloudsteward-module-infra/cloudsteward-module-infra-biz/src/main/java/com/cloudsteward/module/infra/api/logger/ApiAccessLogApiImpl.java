package com.cloudsteward.module.infra.api.logger;

import com.cloudsteward.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import com.cloudsteward.module.infra.service.logger.ApiAccessLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * API 访问日志的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ApiAccessLogApiImpl implements ApiAccessLogApi {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        if (!ObjectUtils.isEmpty(createDTO.getRequestParams()) && createDTO.getRequestParams().length() > 4000) {
            createDTO.setRequestParams("");
        }
        if (!ObjectUtils.isEmpty(createDTO.getUserAgent()) && createDTO.getUserAgent().length() > 255) {
            createDTO.setUserAgent(createDTO.getUserAgent().substring(0, 255));
        }
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}
