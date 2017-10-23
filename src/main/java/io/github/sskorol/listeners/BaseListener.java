package io.github.sskorol.listeners;

import io.github.sskorol.core.Browser;
import io.github.sskorol.core.WebDriverProvider;
import io.github.sskorol.config.XmlConfig;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.sskorol.utils.ServiceLoaderUtils.load;
import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static java.util.Optional.ofNullable;

@SuppressWarnings("JavadocType")
public abstract class BaseListener {

    private static final ThreadLocal<Tuple2<WebDriver, WebDriverWait>> DRIVER_CONTAINER = new ThreadLocal<>();
    private static final List<Browser> BROWSERS = new CopyOnWriteArrayList<>();
    private static final List<WebDriverProvider> WEB_DRIVER_PROVIDERS = new CopyOnWriteArrayList<>();

    public static Tuple2<WebDriver, WebDriverWait> getDriverMetaData() {
        return DRIVER_CONTAINER.get();
    }

    public List<WebDriverProvider> getWebDriverProviders() {
        return WEB_DRIVER_PROVIDERS;
    }

    public void setupDriver(final XmlConfig context) {
        final List<WebDriverProvider> webDriverProviders = getWebDriverProviders();
        DRIVER_CONTAINER.set(
                StreamEx.of(webDriverProviders)
                        .findFirst(this::isWebDriverProviderMatching)
                        .map(wdp -> wdp.createDriver(getCurrentBrowser(context), context))
                        .map(d -> Tuple.of(d, new WebDriverWait(d, WD_CONFIG.wdWaitTimeout())))
                        .orElse(null));
    }

    public void cleanUp() {
        ofNullable(getDriverMetaData()).ifPresent(md -> Try.run(md._1::quit));
        DRIVER_CONTAINER.remove();
    }

    public void loadServiceProviders() {
        BROWSERS.addAll(load(Browser.class, getClass().getClassLoader()));
        WEB_DRIVER_PROVIDERS.addAll(load(WebDriverProvider.class, getClass().getClassLoader()));
    }

    public void unloadServiceProviders() {
        BROWSERS.clear();
        WEB_DRIVER_PROVIDERS.clear();
    }

    private Browser getCurrentBrowser(final XmlConfig config) {
        return StreamEx.of(BROWSERS)
                       .filter(b -> b.name().getBrowserName().equals(config.getBrowser()))
                       .findFirst()
                       .orElseThrow(() -> new SkipException("Unable to find implementation class for "
                               + config.getBrowser() + " browser."));
    }

    private boolean isWebDriverProviderMatching(final WebDriverProvider provider) {
        final List<WebDriverProvider> webDriverProviders = getWebDriverProviders();
        return (webDriverProviders.size() == 1 && WDP_DEFAULT.equals(provider.label()))
                || (webDriverProviders.size() > 1 && !WDP_DEFAULT.equals(provider.label()));
    }
}
