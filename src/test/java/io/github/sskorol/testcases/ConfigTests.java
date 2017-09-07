package io.github.sskorol.testcases;

import io.github.sskorol.config.XmlConfig;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

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
    public void shouldWrapMainXmlParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("browserName", "chrome");
        parameters.put("version", "60.0");
        parameters.put("platform", "WINDOWS");

        final XmlConfig config = new XmlConfig(parameters);
        assertThat(config.hasBrowser()).isTrue();
        assertThat(config.hasVersion()).isTrue();
        assertThat(config.hasPlatform()).isTrue();
    }

    @Test
    public void shouldProvideBrowserConfiguration() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("browserName", "firefox");
        parameters.put("version", "55.0");
        parameters.put("platform", "linux");

        final XmlConfig config = new XmlConfig(parameters);
        assertThat(config).extracting(XmlConfig::getBrowser).containsExactly("firefox");
        assertThat(config).extracting(XmlConfig::getVersion).containsExactly("55.0");
        assertThat(config).extracting(XmlConfig::getPlatform).containsExactly(Platform.LINUX);
    }

    @Test(expectedExceptions = WebDriverException.class)
    public void shouldThrowAnExceptionOnIllegalPlatform() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("platform", "os");
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
                .containsExactly(Platform.getCurrent());
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
        assertThat(config).extracting(c -> c.getValue("key")).containsExactly(Optional.of("value"));
    }
}
