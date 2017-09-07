package io.github.sskorol.core;

import io.github.sskorol.config.XmlConfig;
import io.vavr.control.Try;
import org.joor.Reflect;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

@SuppressWarnings("JavadocType")
public class WebDriverFactory implements WebDriverProvider {

    public static final String WDP_DEFAULT = "wdp_default";

    public String label() {
        return WDP_DEFAULT;
    }

    public WebDriver createDriver(final Browser browser, final XmlConfig config) {
        final Reflect driver = wrapDriver(browser);
        return Try.of(() -> Match(browser).of(
                Case($(Browser::isRemote), () -> createRemote(driver, browser, config)),
                Case($(), () -> createLocal(driver, browser, config)))
        ).map(d -> (WebDriver) d.get()).getOrElseThrow(ex -> {
            throw new SkipException("Unable to create " + browser.name().getDriverClassName()
                    + " with the following capabilities: " + browser.configuration(config), ex);
        });
    }
}
