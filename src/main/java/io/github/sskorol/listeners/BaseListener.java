package io.github.sskorol.listeners;

import io.github.sskorol.core.Browser;
import io.github.sskorol.core.ScreenshotConsumer;
import io.github.sskorol.core.WebDriverProvider;
import io.github.sskorol.config.XmlConfig;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.sskorol.utils.ServiceLoaderUtils.load;
import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static java.util.Optional.ofNullable;
import static org.openqa.selenium.OutputType.BYTES;

@SuppressWarnings("MissingJavadocType")
public abstract class BaseListener {

    private static final ThreadLocal<Tuple2<WebDriver, WebDriverWait>> DRIVER_CONTAINER = new ThreadLocal<>();
    private static final List<Browser> BROWSERS = new CopyOnWriteArrayList<>();
    private static final List<WebDriverProvider> WEB_DRIVER_PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<ScreenshotConsumer> SCREENSHOT_CONSUMERS = new CopyOnWriteArrayList<>();

    public static Tuple2<WebDriver, WebDriverWait> getDriverMetaData() {
        return DRIVER_CONTAINER.get();
    }

    public List<WebDriverProvider> getWebDriverProviders() {
        return WEB_DRIVER_PROVIDERS;
    }

    public void setupDriver(final XmlConfig context, final ITestResult testResult) {
        final List<WebDriverProvider> webDriverProviders = getWebDriverProviders();
        DRIVER_CONTAINER.set(
                StreamEx.of(webDriverProviders)
                        .findFirst(this::isWebDriverProviderMatching)
                        .map(wdp -> wdp.createDriver(getCurrentBrowser(context), context))
                        .map(d -> Tuple.of(d, new WebDriverWait(d, Duration.ofSeconds(WD_CONFIG.wdWaitTimeout()))))
                        .orElse(null));
        injectSessionId(testResult);
    }

    public void cleanUp(final ITestResult testResult) {
        ofNullable(getDriverMetaData())
                .ifPresent(md -> {
                    takeScreenshot(md._1, testResult);
                    Try.run(md._1::quit);
                });
        DRIVER_CONTAINER.remove();
    }

    public void loadServiceProviders() {
        final ClassLoader loader = getClass().getClassLoader();
        BROWSERS.addAll(load(Browser.class, loader));
        WEB_DRIVER_PROVIDERS.addAll(load(WebDriverProvider.class, loader));
        SCREENSHOT_CONSUMERS.addAll(load(ScreenshotConsumer.class, loader));
    }

    public void unloadServiceProviders() {
        BROWSERS.clear();
        WEB_DRIVER_PROVIDERS.clear();
        SCREENSHOT_CONSUMERS.clear();
    }

    private Browser getCurrentBrowser(final XmlConfig config) {
        return StreamEx.of(BROWSERS)
                .findFirst(b -> b.name().getBrowserName().equals(config.getBrowser()))
                .orElseThrow(() -> new SkipException("Unable to find implementation class for "
                        + config.getBrowser() + " browser."));
    }

    private boolean isWebDriverProviderMatching(final WebDriverProvider provider) {
        final List<WebDriverProvider> webDriverProviders = getWebDriverProviders();
        return (webDriverProviders.size() == 1 && WDP_DEFAULT.equals(provider.label()))
                || (webDriverProviders.size() > 1 && !WDP_DEFAULT.equals(provider.label()));
    }

    private void injectSessionId(final ITestResult testResult) {
        ofNullable(DRIVER_CONTAINER.get())
                .map(t -> t._1)
                .filter(d -> d instanceof RemoteWebDriver)
                .map(d -> ((RemoteWebDriver) d).getSessionId())
                .ifPresent(id -> testResult.setAttribute("sessionId", id));
    }

    private void takeScreenshot(final WebDriver driver, final ITestResult testResult) {
        if (WD_CONFIG.takeScreenshot()) {
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(BYTES);
            StreamEx.of(SCREENSHOT_CONSUMERS).forEach(sc -> sc.handle(screenshot, testResult));
        }
    }
}
