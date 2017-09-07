package io.github.sskorol.testcases;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.sskorol.config.XmlConfig;
import io.github.sskorol.core.Browser;
import io.github.sskorol.core.WebDriverProvider;
import one.util.streamex.StreamEx;
import org.joor.Reflect;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static io.github.sskorol.utils.ServiceLoaderUtils.load;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.joor.Reflect.on;
import static org.mockito.Mockito.*;

@PrepareForTest(WebDriverManager.class)
public class CoreTests extends PowerMockTestCase {

    private List<Browser> browsers;
    private List<WebDriverProvider> factories;
    private WebDriverProvider defaultFactory;
    private Browser firefox;

    @BeforeClass
    public void setUp() {
        browsers = load(Browser.class, getClass().getClassLoader()).toList();
        factories = load(WebDriverProvider.class, getClass().getClassLoader()).toList();
        defaultFactory = StreamEx.of(factories)
                                 .findFirst(f -> f.label().equals(WDP_DEFAULT))
                                 .orElseThrow(() -> new AssertionError("Unable to get default factory"));
        firefox = StreamEx.of(browsers)
                          .findFirst(f -> f.name() == Browser.Name.Firefox)
                          .orElseThrow(() -> new AssertionError("Unable to get Firefox implementation"));
    }

    @Test
    public void shouldLoadImplementedBrowserServices() {
        assertThat(browsers).hasSize(4);
    }

    @Test
    public void browsersShouldHaveProvidedEnumConstants() {
        assertThat(browsers)
                .extracting(Browser::name)
                .containsExactlyInAnyOrder(Browser.Name.Chrome,
                        Browser.Name.Edge,
                        Browser.Name.Firefox,
                        Browser.Name.InternetExplorer);
    }

    @Test
    public void browsersShouldHaveDefaultNames() {
        assertThat(browsers)
                .extracting(Browser::name)
                .extracting(Browser.Name::getBrowserName)
                .containsExactlyInAnyOrder("chrome", "firefox", "edge", "ie");
    }

    @Test
    public void browsersShouldHaveDefaultDrivers() {
        assertThat(browsers)
                .extracting(Browser::name)
                .extracting(Browser.Name::getDriverClassName)
                .containsExactlyInAnyOrder("org.openqa.selenium.chrome.ChromeDriver",
                        "org.openqa.selenium.firefox.FirefoxDriver",
                        "org.openqa.selenium.edge.EdgeDriver",
                        "org.openqa.selenium.ie.InternetExplorerDriver");
    }

    @Test
    public void shouldRetrievePrecededBrowsers() {
        assertThat(Browser.Name.values()).containsExactly(Browser.Name.Chrome,
                Browser.Name.Firefox,
                Browser.Name.InternetExplorer,
                Browser.Name.Edge,
                Browser.Name.Remote);
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
        chromeParameters.put("browserName", "chrome");

        final XmlConfig config = new XmlConfig(chromeParameters);
        final Browser chrome = StreamEx.of(browsers)
                                       .findFirst(b -> b.name() == Browser.Name.Chrome)
                                       .orElseThrow(() -> new AssertionError("Unable to retrieve Chrome"));

        assertThat(chrome.isRemote()).isFalse();
        assertThat(chrome.url()).isEqualTo("http://localhost:4444/wd/hub");
        assertThat(chrome.defaultConfiguration(config))
                .extracting(Capabilities::getBrowserName)
                .containsExactly("chrome");
        assertThat(chrome.defaultConfiguration(config))
                .extracting(Capabilities::getVersion)
                .containsExactly("");
        assertThat(chrome.defaultConfiguration(config))
                .extracting(Capabilities::getPlatform)
                .containsExactly(Platform.getCurrent());
        assertThat(chrome.configuration(config)).isEqualTo(chrome.defaultConfiguration(config));
    }

    @Test
    public void shouldCreateDriverFactories() {
        assertThat(factories).hasSize(2);
        assertThat(StreamEx.of(factories).findFirst(f -> f.label().equals(WDP_DEFAULT)))
                .isPresent();
    }

    @Test
    public void shouldCreateRemoteDriverInstance() throws MalformedURLException {
        XmlConfig config = new XmlConfig(new HashMap<String, String>() {
            {
                put("browserName", "firefox");
                put("platform", "ANY");
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
    public void shouldCreateLocalDriverInstance() throws Exception {
        Browser chrome = StreamEx.of(browsers)
                                 .findFirst(f -> f.name() == Browser.Name.Chrome)
                                 .orElseThrow(() -> new AssertionError(
                                         "Unable to get Chrome implementation"));

        XmlConfig config = new XmlConfig(new HashMap<String, String>() {
            {
                put("browserName", "chrome");
            }
        });

        PowerMockito.mockStatic(WebDriverManager.class);
        BrowserManager browserManager = mock(BrowserManager.class, RETURNS_DEEP_STUBS);
        WebDriver driver = mock(ChromeDriver.class, RETURNS_DEEP_STUBS);
        WebDriverProvider spyFactory = spy(defaultFactory);
        Reflect spyReflectedDriver = spy(spyFactory.wrapDriver(chrome));

        PowerMockito.when(WebDriverManager.getInstance(spyReflectedDriver.type())).thenReturn(browserManager);
        doNothing().when(browserManager).setup();
        doReturn(spyReflectedDriver).when(spyFactory).wrapDriver(chrome);
        doReturn(on(driver)).when(spyReflectedDriver).create(chrome.configuration(config));

        assertThat(spyFactory.createDriver(chrome, config)).isInstanceOf(ChromeDriver.class);
    }

    @Test
    public void shouldThrowHiddenMalformedURLException() {
        XmlConfig config = new XmlConfig(new HashMap<String, String>() {
            {
                put("browserName", "firefox");
                put("platform", "ANY");
            }
        });

        Browser browser = spy(firefox);
        doReturn("localhost").when(browser).url();

        assertThat(catchThrowable(() -> defaultFactory.createDriver(browser, config)))
                .isInstanceOf(SkipException.class)
                .hasStackTraceContaining("java.net.MalformedURLException");
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
