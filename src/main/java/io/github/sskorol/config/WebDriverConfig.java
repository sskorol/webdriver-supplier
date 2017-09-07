package io.github.sskorol.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Reloadable;

/**
 * WebDriver configuration file. Wait timeout could be overridden by system property or custom wd.properties file.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources("classpath:wd.properties")
public interface WebDriverConfig extends Config, Reloadable {

    WebDriverConfig WD_CONFIG = ConfigFactory.create(WebDriverConfig.class, System.getenv(), System.getProperties());

    @Key("wd.wait.timeout")
    @DefaultValue("10")
    long wdWaitTimeout();
}
