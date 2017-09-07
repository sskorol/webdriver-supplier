package io.github.sskorol.core;

import io.github.sskorol.config.XmlConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.capitalize;

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

    default Capabilities defaultConfiguration(final XmlConfig context) {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(context.getBrowser());
        capabilities.setVersion(context.getVersion());
        capabilities.setPlatform(context.getPlatform());
        return capabilities;
    }

    default Capabilities configuration(final XmlConfig context) {
        return defaultConfiguration(context);
    }

    default boolean isRemote() {
        return false;
    }

    default String url() {
        return "http://localhost:4444/wd/hub";
    }
}
