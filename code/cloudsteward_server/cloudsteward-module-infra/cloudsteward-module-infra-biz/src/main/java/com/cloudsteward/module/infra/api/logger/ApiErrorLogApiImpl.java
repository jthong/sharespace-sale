package com.cloudsteward.module.infra.api.logger;

import com.cloudsteward.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import com.cloudsteward.module.infra.service.logger.ApiErrorLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * API 访问日志的 API 接口
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ApiErrorLogApiImpl implements ApiErrorLogApi {

    @Resource
    private ApiErrorLogService apiErrorLogService;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        if (!ObjectUtils.isEmpty(createDTO.getRequestParams()) && createDTO.getRequestParams().length() > 4000) {
            createDTO.setRequestParams("");
        }
        if (!ObjectUtils.isEmpty(createDTO.getUserAgent()) && createDTO.getUserAgent().length() > 255) {
            createDTO.setUserAgent(createDTO.getUserAgent().substring(0, 255));
        }
        apiErrorLogService.createApiErrorLog(createDTO);
    }

}
