package io.github.sskorol.testcases;

import io.github.sskorol.cdt.services.ChromeDevToolsService;
import io.github.sskorol.cdt.services.impl.ChromeDevToolsServiceImpl;
import io.github.sskorol.core.CDP;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CDPTests {
    static class CDPImpl implements CDP {
        @Override
        public String cdpWebSocketUrl(final String sessionId) {
            return "ws://localhost:9222/devtools/page/" + sessionId;
        }
    }

    @Test
    public void shouldNotGetCDPInstance() {
        var cdpService = new CDPImpl().initCDP(UUID.randomUUID().toString());
        assertThat(cdpService).isNull();
    }

    @Test
    public void shouldGetCDPInstance() {
        var cdpSpy = spy(new CDPImpl());
        var cdpsi = mock(ChromeDevToolsServiceImpl.class);

        try (var cdps = Mockito.mockStatic(ChromeDevToolsService.class)) {
            cdps.when(() -> ChromeDevToolsService.from(anyString())).thenReturn(cdpsi);
            try (var pseudoRealCDPS = cdpSpy.initCDP(UUID.randomUUID().toString())) {
                assertThat(pseudoRealCDPS).isNotNull();
                cdps.verify(() -> ChromeDevToolsService.from(anyString()), times(1));
            }
        }
    }
}
