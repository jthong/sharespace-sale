package com.cloudsteward.module.member.service.workspace;
import com.cloudsteward.module.member.controller.admin.workspace.WorkspaceOperationsController;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Collections;

class WorkspaceRoutesTest {
    @Test void explicitRouteParametersWorkWithoutCompilerParameterMetadata() throws Exception {
        WorkspaceOperationsService service=mock(WorkspaceOperationsService.class);
        when(service.page(eq("orders"),any())).thenReturn(Collections.singletonMap("total",0L));
        when(service.overview(18L)).thenReturn(Collections.singletonMap("activeOrders",0));
        WorkspaceOperationsController controller=new WorkspaceOperationsController();ReflectionTestUtils.setField(controller,"operations",service);
        org.springframework.test.web.servlet.MockMvc mvc=MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(post("/member/workspace/page/orders").contentType("application/json").content("{\"storeId\":18}")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(get("/member/workspace/overview?storeId=18")).andExpect(status().isOk()).andExpect(jsonPath("$.data.activeOrders").value(0));
    }
}
