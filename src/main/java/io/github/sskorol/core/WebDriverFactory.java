package io.github.sskorol.core;

import io.github.sskorol.config.XmlConfig;
import io.vavr.control.Try;
import org.joor.Reflect;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static io.github.sskorol.utils.StringUtils.toDimension;
import static io.vavr.API.*;

@SuppressWarnings("MissingJavadocType")
public class WebDriverFactory implements WebDriverProvider {

    @Override
    public String label() {
        return WDP_DEFAULT;
    }

    @Override
    public WebDriver createDriver(final Browser browser, final XmlConfig config) {
        final Reflect driver = wrapDriver(browser);
        return Try.of(() -> Match(browser).of(
                Case($(Browser::isRemote), () -> createRemote(driver, browser, config)),
                Case($(), () -> createLocal(driver, browser, config)))
        ).map(d -> withCustomScreenResolution(d.get())).getOrElseThrow(ex -> {
            throw new SkipException("Unable to create " + browser.name().getDriverClassName()
                    + " with the following capabilities: " + browser.configuration(config), ex);
        });
    }

    private WebDriver withCustomScreenResolution(final WebDriver driver) {
        Match(WD_CONFIG.screenResolution()).of(
                Case($("max"), () -> run(() -> driver.manage().window().maximize())),
                Case($(), value -> run(() -> toDimension(value).ifPresent(d -> driver.manage().window().setSize(d))))
        );
        return driver;
    }
}
