package io.github.sskorol.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.sskorol.config.XmlConfig;
import lombok.SneakyThrows;
import org.joor.Reflect;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static org.joor.Reflect.on;

/**
 * Key interface which should be implemented in case of a custom WebDriver factory.
 */
public interface WebDriverProvider {

    String WDP_DEFAULT = "wdp_default";

    String label();

    WebDriver createDriver(Browser browser, XmlConfig config);

    default Reflect wrapDriver(final Browser browser) {
        return on((browser.isRemote() ? Browser.Name.Remote : browser.name()).getDriverClassName());
    }

    default Reflect createLocal(final Reflect driver, final Browser browser, final XmlConfig config) {
        setupDriver(driver);
        return driver.create(browser.configuration(config));
    }

    @SneakyThrows(MalformedURLException.class)
    default Reflect createRemote(final Reflect driver, final Browser browser, final XmlConfig config) {
        return driver.create(new URL(browser.url()), browser.configuration(config));
    }

    default void setupDriver(final Reflect driver) {
        WebDriverManager.getInstance(driver.type()).setup();
    }
}
