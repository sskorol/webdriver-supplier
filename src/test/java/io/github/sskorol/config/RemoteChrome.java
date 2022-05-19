package io.github.sskorol.config;

import io.github.sskorol.core.Browser;
import io.github.sskorol.core.CDP;

import static java.lang.String.format;

public class RemoteChrome implements Browser, CDP {

    @Override
    public Name name() {
        return Name.Chrome;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public String cdpWebSocketUrl(final String sessionId) {
        return format("ws://localhost:4444/devtools/%s/page", sessionId);
    }
}
