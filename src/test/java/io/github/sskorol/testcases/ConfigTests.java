package io.github.sskorol.testcases;

import io.github.sskorol.config.XmlConfig;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static io.github.sskorol.config.XmlConfig.TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.remote.CapabilityType.*;

public class ConfigTests {

    @Test
    public void shouldAccessDefaultTimeoutValue() {
        assertThat(WD_CONFIG.wdWaitTimeout()).isEqualTo(10);
    }

    @Test
    public void shouldOverrideDefaultTimeoutValue() {
        System.setProperty("wd.wait.timeout", "15");
        WD_CONFIG.reload();
        assertThat(WD_CONFIG.wdWaitTimeout()).isEqualTo(15);
    }

    @Test
    public void defaultResolutionShouldBeAbsent() {
        assertThat(WD_CONFIG.screenResolution()).isEqualTo("max");
    }

    @Test
    public void defaultResolutionShouldBeReloadable() {
        System.setProperty("wd.screen.resolution", "1280x1024x24");
        WD_CONFIG.reload();
        assertThat(WD_CONFIG.screenResolution()).isEqualTo("1280x1024x24");
    }

    @Test
    public void shouldWrapMainXmlParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(BROWSER_NAME, "chrome");
        parameters.put(BROWSER_VERSION, "60.0");
        parameters.put(PLATFORM_NAME, "WINDOWS");

        final XmlConfig config = new XmlConfig(parameters);
        assertThat(config.hasBrowser()).isTrue();
        assertThat(config.hasVersion()).isTrue();
        assertThat(config.hasPlatform()).isTrue();
    }

    @Test
    public void shouldProvideBrowserConfiguration() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(BROWSER_NAME, "firefox");
        parameters.put(BROWSER_VERSION, "55.0");
        parameters.put(PLATFORM_NAME, "linux");

        final XmlConfig config = new XmlConfig(parameters);
        assertThat(config).extracting(XmlConfig::getBrowser).isEqualTo("firefox");
        assertThat(config).extracting(XmlConfig::getVersion).isEqualTo("55.0");
        assertThat(config).extracting(XmlConfig::getPlatform).isEqualTo(Platform.LINUX);
    }

    @Test(expectedExceptions = WebDriverException.class)
    public void shouldThrowAnExceptionOnIllegalPlatform() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(PLATFORM_NAME, "os");
        new XmlConfig(parameters).getPlatform();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowAnExceptionOnMissingBrowser() {
        new XmlConfig(new HashMap<>()).getBrowser();
    }

    @Test
    public void shouldReplaceMissingPlatformWithRealOne() {
        assertThat(new XmlConfig(new HashMap<>()))
                .extracting(XmlConfig::getPlatform)
                .isEqualTo(Platform.getCurrent());
    }

    @Test
    public void shouldProvideEmptyVersionOnMissingParameter() {
        assertThat(new XmlConfig(new HashMap<>()).getVersion()).isEmpty();
    }

    @Test
    public void shouldProvideCustomParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("key", "value");
        final XmlConfig config = new XmlConfig(parameters);

        assertThat(config.hasValue("key")).isTrue();
        assertThat(config)
                .extracting(c -> c.getValue("key"))
                .isEqualTo(Optional.of("value"));
    }

    @Test
    public void shouldProvideValidTestName() {
        final String testName = "shouldProvideValidTestName";
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(TEST_NAME, testName);
        final XmlConfig config = new XmlConfig(parameters);

        assertThat(config)
                .extracting(XmlConfig::getTestName)
                .isEqualTo(testName);
    }

    @Test
    public void shouldProvideRandomTestNameOnMissingParameter() {
        assertThat(new XmlConfig(new HashMap<>()).getTestName()).isNotBlank();
    }
}
