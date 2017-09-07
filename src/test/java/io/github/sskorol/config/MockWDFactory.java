package io.github.sskorol.config;

import io.github.sskorol.core.Browser;
import io.github.sskorol.core.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.mockito.Mockito.mock;

public class MockWDFactory implements WebDriverProvider {

    @Override
    public String label() {
        return "mock";
    }

    @Override
    public WebDriver createDriver(final Browser browser, final XmlConfig config) {
        return mock(FirefoxDriver.class);
    }
}
