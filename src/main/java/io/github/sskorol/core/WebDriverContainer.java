package io.github.sskorol.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.sskorol.cdt.services.ChromeDevToolsService;
import io.github.sskorol.config.XmlConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Encapsulates driver, wait and devtools services.
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@RequiredArgsConstructor
@Getter
public class WebDriverContainer {

    private final WebDriver webDriver;
    private final WebDriverWait webDriverWait;
    private final XmlConfig config;
    private ChromeDevToolsService devToolsService;

    public WebDriverContainer withDevToolsService(final ChromeDevToolsService devToolsService) {
        this.devToolsService = devToolsService;
        return this;
    }
}
