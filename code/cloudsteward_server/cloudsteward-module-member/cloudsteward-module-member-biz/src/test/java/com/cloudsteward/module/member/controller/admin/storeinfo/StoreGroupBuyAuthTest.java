package com.cloudsteward.module.member.controller.admin.storeinfo;

import com.cloudsteward.module.member.controller.app.store.vo.GroupPayAuthUrlReqVO;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreGroupBuyAuthTest {
    @Test void separateButtonsKeepStoreAndPlatformInRequest() {
        StoreInfoController controller = new StoreInfoController();
        StoreInfoService service = mock(StoreInfoService.class);
        ReflectionTestUtils.setField(controller,"storeInfoService",service);
        for (int platform:new int[]{1,2}) {
            GroupPayAuthUrlReqVO request = new GroupPayAuthUrlReqVO();
            request.setStoreId(18L);request.setGroupPayType(platform);
            when(service.getGroupPayAuthUrl(request)).thenReturn("https://example.test/auth/"+platform);
            assertEquals("https://example.test/auth/"+platform,controller.getGroupBuyAuthUrl(request).getData());
            verify(service).getGroupPayAuthUrl(request);
            assertEquals(18L,request.getStoreId());assertEquals(platform,request.getGroupPayType());
        }
    }
    @Test void unsupportedPlatformNeverCallsProvider() {
        StoreInfoController controller = new StoreInfoController();
        StoreInfoService service = mock(StoreInfoService.class);
        ReflectionTestUtils.setField(controller,"storeInfoService",service);
        for(Integer platform:new Integer[]{null,0,3}) {
            GroupPayAuthUrlReqVO request=new GroupPayAuthUrlReqVO();request.setStoreId(18L);request.setGroupPayType(platform);
            assertThrows(RuntimeException.class,()->controller.getGroupBuyAuthUrl(request));
        }
        verifyNoInteractions(service);
    }
}
