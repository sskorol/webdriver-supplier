package io.github.sskorol.core;

import io.github.sskorol.config.XmlConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

/**
 * Key interface, which should be implemented on client side. Simplifies browser configuration staff.
 */
public interface Browser {

    /**
     * Supported browsers' names. Note that Remote is for internal usage only.
     */
    @RequiredArgsConstructor
    enum Name {
        Chrome("chrome", "ChromeDriver"),
        Firefox("firefox", "FirefoxDriver"),
        InternetExplorer("ie", "InternetExplorerDriver"),
        Edge("edge", "EdgeDriver"),
        Remote("remote", "RemoteWebDriver");

        @Getter
        private final String browserName;
        private final String driverName;

        public String getDriverClassName() {
            return format("org.openqa.selenium.%s.%s", browserName, capitalize(driverName));
        }
    }

    Name name();

    default MutableCapabilities defaultConfiguration(final XmlConfig config) {
        final MutableCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(BROWSER_NAME, config.getBrowser());
        capabilities.setCapability(VERSION, config.getVersion());
        capabilities.setCapability(PLATFORM, config.getPlatform());
        return capabilities;
    }

    default MutableCapabilities configuration(final XmlConfig config) {
        return defaultConfiguration(config);
    }

    @SuppressWarnings("unchecked")
    default <T extends MutableCapabilities> MutableCapabilities merge(final XmlConfig config, final T options) {
        return options.merge(defaultConfiguration(config));
    }

    default boolean isRemote() {
        return false;
    }

    default String url() {
        return "http://localhost:4444/wd/hub";
    }
}
