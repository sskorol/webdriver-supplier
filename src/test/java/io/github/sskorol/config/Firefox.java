package io.github.sskorol.config;

import io.github.sskorol.core.Browser;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Firefox implements Browser {

    public Name name() {
        return Name.Firefox;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public MutableCapabilities configuration(final XmlConfig config) {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("screenResolution", "1280x1024x24");
        return merge(config, capabilities);
    }
}
