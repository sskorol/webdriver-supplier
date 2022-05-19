package io.github.sskorol.listeners;

import io.github.sskorol.cdt.services.ChromeDevToolsService;
import io.github.sskorol.core.*;
import io.github.sskorol.config.XmlConfig;
import io.vavr.control.Try;
import lombok.val;
import one.util.streamex.StreamEx;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.sskorol.utils.ServiceLoaderUtils.load;
import static io.github.sskorol.config.WebDriverConfig.WD_CONFIG;
import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static org.openqa.selenium.OutputType.BYTES;

@SuppressWarnings({"MissingJavadocType", "FinalLocalVariable"})
public abstract class BaseListener {

    private static final ThreadLocal<WebDriverContainer> DRIVER_CONTAINER = new ThreadLocal<>();
    private static final List<Browser> BROWSERS = new CopyOnWriteArrayList<>();
    private static final List<WebDriverProvider> WEB_DRIVER_PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<ScreenshotConsumer> SCREENSHOT_CONSUMERS = new CopyOnWriteArrayList<>();

    public static WebDriverContainer getDriverMetaData() {
        return DRIVER_CONTAINER.get();
    }

    public List<WebDriverProvider> getWebDriverProviders() {
        return WEB_DRIVER_PROVIDERS;
    }

    public void setupDriver(final XmlConfig context, final ITestResult testResult) {
        val webDriverProviders = getWebDriverProviders();
        val browser = getCurrentBrowser(context);
        val driver = StreamEx.of(webDriverProviders)
            .findFirst(this::isWebDriverProviderMatching)
            .map(wdp -> wdp.createDriver(browser, context))
            .map(d -> new WebDriverContainer(d, new WebDriverWait(d, ofSeconds(WD_CONFIG.wdWaitTimeout())))
                .withDevToolsService(getDevToolsService(browser, d)))
            .orElse(null);

        if (driver == null) {
            throw new SkipException("Unable to find a suitable driver for " + context.toString());
        }

        DRIVER_CONTAINER.set(driver);
        injectSessionId(testResult);
    }

    public void cleanUp(final ITestResult testResult) {
        ofNullable(getDriverMetaData())
            .ifPresent(md -> {
                ofNullable(md.getDevToolsService()).ifPresent(cdp -> Try.run(cdp::close));
                val driver = md.getWebDriver();
                takeScreenshot(driver, testResult);
                Try.run(driver::quit);
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

    private ChromeDevToolsService getDevToolsService(final Browser browser, final WebDriver driver) {
        return CDP.class.isAssignableFrom(browser.getClass()) && driver instanceof RemoteWebDriver
               ? ((CDP) browser).initCDP(((RemoteWebDriver) driver).getSessionId().toString()) : null;
    }

    private boolean isWebDriverProviderMatching(final WebDriverProvider provider) {
        final List<WebDriverProvider> webDriverProviders = getWebDriverProviders();
        return (webDriverProviders.size() == 1 && WDP_DEFAULT.equals(provider.label()))
               || (webDriverProviders.size() > 1 && !WDP_DEFAULT.equals(provider.label()));
    }

    private void injectSessionId(final ITestResult testResult) {
        ofNullable(DRIVER_CONTAINER.get())
            .map(WebDriverContainer::getWebDriver)
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
