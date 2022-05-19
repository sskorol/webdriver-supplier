package io.github.sskorol.testcases;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.sskorol.config.PowerMockObjectFactory;
import io.github.sskorol.config.XmlConfig;
import io.github.sskorol.core.Browser;
import io.github.sskorol.core.WebDriverProvider;
import one.util.streamex.StreamEx;
import org.joor.Reflect;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestObjectFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.sskorol.config.XmlConfig.TEST_NAME;
import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static io.github.sskorol.utils.ServiceLoaderUtils.load;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.joor.Reflect.on;
import static org.mockito.Mockito.*;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM_NAME;

@PrepareForTest(WebDriverManager.class)
public class CoreTests extends PowerMockTestCase {

    private List<Browser> browsers;
    private List<WebDriverProvider> factories;
    private WebDriverProvider defaultFactory;
    private Browser firefox;

    @BeforeClass
    public void setUp() {
        browsers = load(Browser.class, getClass().getClassLoader());
        factories = load(WebDriverProvider.class, getClass().getClassLoader());
        defaultFactory = StreamEx.of(factories)
            .findFirst(f -> f.label().equals(WDP_DEFAULT))
            .orElseThrow(() -> new AssertionError("Unable to get default factory"));
        firefox = StreamEx.of(browsers)
            .findFirst(f -> f.name() == Browser.Name.Firefox)
            .orElseThrow(() -> new AssertionError("Unable to get Firefox implementation"));
    }

    @Test
    public void shouldLoadImplementedBrowserServices() {
        assertThat(browsers).hasSize(5);
    }

    @Test
    public void browsersShouldHaveProvidedEnumConstants() {
        assertThat(browsers)
            .extracting(Browser::name)
            .containsExactlyInAnyOrder(
                Browser.Name.Chrome,
                Browser.Name.Chrome,
                Browser.Name.Edge,
                Browser.Name.Firefox,
                Browser.Name.InternetExplorer
            );
    }

    @Test
    public void browsersShouldHaveDefaultNames() {
        assertThat(browsers)
            .extracting(Browser::name)
            .extracting(Browser.Name::getBrowserName)
            .containsExactlyInAnyOrder("chrome", "chrome", "firefox", "edge", "ie");
    }

    @Test
    public void browsersShouldHaveDefaultDrivers() {
        assertThat(browsers)
            .extracting(Browser::name)
            .extracting(Browser.Name::getDriverClassName)
            .containsExactlyInAnyOrder(
                "org.openqa.selenium.chrome.ChromeDriver",
                "org.openqa.selenium.chrome.ChromeDriver",
                "org.openqa.selenium.firefox.FirefoxDriver",
                "org.openqa.selenium.edge.EdgeDriver",
                "org.openqa.selenium.ie.InternetExplorerDriver"
            );
    }

    @Test
    public void shouldRetrievePrecededBrowsers() {
        assertThat(Browser.Name.values()).containsExactly(
            Browser.Name.Chrome,
            Browser.Name.Firefox,
            Browser.Name.InternetExplorer,
            Browser.Name.Edge,
            Browser.Name.Remote
        );
    }

    @Test
    public void shouldRetrieveBrowserEnumConstantByProvidedString() {
        assertThat(Browser.Name.valueOf("Chrome")).isEqualTo(Browser.Name.Chrome);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowAnExceptionOnUndefinedBrowserName() {
        Browser.Name.valueOf("opera");
    }

    @Test
    public void shouldRetrieveBrowserDefaults() {
        final Map<String, String> chromeParameters = new HashMap<>();
        chromeParameters.put(BROWSER_NAME, "chrome");

        final XmlConfig config = new XmlConfig(chromeParameters);
        final Browser chrome = StreamEx.of(browsers)
            .findFirst(b -> b.name() == Browser.Name.Chrome)
            .orElseThrow(() -> new AssertionError("Unable to retrieve Chrome"));

        assertThat(chrome.isRemote()).isFalse();
        assertThat(chrome.url()).isEqualTo("http://localhost:4444/wd/hub");
        assertThat(chrome.defaultConfiguration(config))
            .extracting(Capabilities::getBrowserName)
            .isEqualTo("chrome");
        assertThat(chrome.defaultConfiguration(config))
            .extracting(Capabilities::getBrowserVersion)
            .isEqualTo("");
        assertThat(chrome.defaultConfiguration(config))
            .extracting(Capabilities::getPlatformName)
            .isEqualTo(Platform.getCurrent());
        assertThat(chrome.configuration(config)).isEqualTo(chrome.defaultConfiguration(config));
    }

    @Test
    public void shouldRetrieveBrowserWithOptions() {
        final Map<String, String> edgeParameters = new HashMap<>();
        edgeParameters.put(BROWSER_NAME, "edge");

        final XmlConfig config = new XmlConfig(edgeParameters);
        final Browser edge = StreamEx.of(browsers)
            .findFirst(b -> b.name() == Browser.Name.Edge)
            .orElseThrow(() -> new AssertionError("Unable to retrieve Edge"));

        assertThat(edge.configuration(config)).isEqualTo(edge.merge(config, new EdgeOptions()));
    }

    @Test
    public void shouldCreateDriverFactories() {
        assertThat(factories).hasSize(2);
        assertThat(StreamEx.of(factories).anyMatch(f -> f.label().equals(WDP_DEFAULT))).isTrue();
    }

    @Test
    public void shouldCreateRemoteDriverInstance() throws MalformedURLException {
        XmlConfig config = new XmlConfig(new HashMap<>() {
            {
                put(BROWSER_NAME, "firefox");
                put(PLATFORM_NAME, "ANY");
                put(TEST_NAME, "shouldCreateRemoteDriverInstance");
            }
        });

        WebDriver driver = mock(RemoteWebDriver.class, RETURNS_DEEP_STUBS);
        WebDriverProvider spyFactory = spy(defaultFactory);
        Reflect spyReflectedDriver = spy(spyFactory.wrapDriver(firefox));

        doReturn(spyReflectedDriver).when(spyFactory).wrapDriver(firefox);
        doReturn(on(driver)).when(spyReflectedDriver).create(new URL(firefox.url()), firefox.configuration(config));

        assertThat(spyFactory.createDriver(firefox, config)).isInstanceOf(RemoteWebDriver.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateLocalDriverInstance() {
        Browser edge = StreamEx.of(browsers)
            .findFirst(f -> f.name() == Browser.Name.Edge)
            .orElseThrow(() -> new AssertionError(
                "Unable to get Edge implementation"));

        XmlConfig config = new XmlConfig(new HashMap<>() {
            {
                put(BROWSER_NAME, "edge");
            }
        });

        PowerMockito.mockStatic(WebDriverManager.class);
        WebDriverManager browserManager = mock(WebDriverManager.class, RETURNS_DEEP_STUBS);
        WebDriver driver = mock(EdgeDriver.class, RETURNS_DEEP_STUBS);
        WebDriverProvider spyFactory = spy(defaultFactory);
        Reflect spyReflectedDriver = spy(spyFactory.wrapDriver(edge));

        PowerMockito.when(WebDriverManager.getInstance((Class<? extends WebDriver>) spyReflectedDriver.type()))
            .thenReturn(browserManager);
        doNothing().when(browserManager).setup();
        doReturn(spyReflectedDriver).when(spyFactory).wrapDriver(edge);
        doReturn(on(driver)).when(spyReflectedDriver).create(edge.configuration(config));

        assertThat(spyFactory.createDriver(edge, config)).isInstanceOf(EdgeDriver.class);
    }

    @Test
    public void shouldThrowHiddenMalformedURLException() {
        XmlConfig config = new XmlConfig(new HashMap<>() {
            {
                put(BROWSER_NAME, "firefox");
                put(PLATFORM_NAME, "ANY");
            }
        });

        Browser browser = spy(firefox);
        doReturn("localhost").when(browser).url();

        assertThat(catchThrowable(() -> defaultFactory.createDriver(browser, config)))
            .isInstanceOf(SkipException.class)
            .hasStackTraceContaining("java.net.MalformedURLException");
    }

    @ObjectFactory
    public ITestObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }
}
