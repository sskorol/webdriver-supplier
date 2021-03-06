package io.github.sskorol.config;

import io.github.sskorol.core.Browser;
import io.github.sskorol.core.WebDriverProvider;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockWDFactory implements WebDriverProvider {

    @Override
    public String label() {
        return "mock";
    }

    @Override
    public WebDriver createDriver(final Browser browser, final XmlConfig config) {
        final RemoteWebDriver driver = mock(RemoteWebDriver.class);
        when(driver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1, 2, 3});
        doReturn(new SessionId(randomAlphanumeric(14))).when(driver).getSessionId();
        return driver;
    }
}
