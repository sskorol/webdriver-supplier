package io.github.sskorol.core;

import io.github.sskorol.cdt.services.ChromeDevToolsService;
import io.github.sskorol.cdt.services.exceptions.WebSocketServiceException;

import java.net.URISyntaxException;

/**
 * An interface for working Chrome DevTools Protocol.
 */
public interface CDP {

    default ChromeDevToolsService initCDP(final String sessionId) {
        try {
            return ChromeDevToolsService.from(cdpWebSocketUrl(sessionId));
        } catch (WebSocketServiceException | URISyntaxException ignored) {
            return null;
        }
    }

    String cdpWebSocketUrl(String sessionId);
}
