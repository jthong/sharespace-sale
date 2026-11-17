package com.cloudsteward.module.member.controller.admin.workspace;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.module.member.service.workspace.WorkspaceOperationsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import javax.annotation.Resource;
import java.util.Map;
import static com.cloudsteward.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/member/workspace")
public class WorkspaceOperationsController {
    @Resource private WorkspaceOperationsService operations;
    @GetMapping("/overview")
    @PreAuthorize("@ss.hasPermission('member:workspace:query')")
    public CommonResult<Map<String,Object>> overview(@RequestParam("storeId") Long storeId) { return success(operations.overview(storeId)); }
    @PostMapping("/page/{resource}")
    @PreAuthorize("@ss.hasPermission('member:workspace:query')")
    public CommonResult<Map<String,Object>> page(@PathVariable("resource") String resource,@RequestBody JSONObject request) { return success(operations.page(resource,request)); }
    @PostMapping("/save/{resource}")
    @PreAuthorize("@ss.hasPermission('member:workspace:manage')")
    @Idempotent(timeout=3)
    public CommonResult<Object> save(@PathVariable("resource") String resource,@RequestBody JSONObject request) { return success(operations.save(resource,request)); }
    @PostMapping("/action/{action}")
    @PreAuthorize("@ss.hasPermission('member:workspace:manage')")
    @Idempotent(timeout=3)
    public CommonResult<Object> action(@PathVariable("action") String action,@RequestBody JSONObject request) { return success(operations.action(action,request)); }
}
