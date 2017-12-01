package io.github.sskorol.testcases;

import io.github.sskorol.core.WebDriverProvider;
import io.github.sskorol.listeners.BeforeMethodListener;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ListenerTests {

    @Test
    public void shouldCallDefaultListenerWithMockProvider() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng1.xml",
                this::getDefaultListener);
        assertThat(nameListener.getFailedMethodNames()).hasSize(2);
        assertThat(nameListener.getSucceedMethodNames()).hasSize(1);
    }

    @Test
    public void shouldCallDefaultListenerForSuiteLevelParameters() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng3.xml",
                this::getDefaultListener);
        assertThat(nameListener.getSucceedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldCallCustomListenerForDefaultProvider() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng3.xml",
                () -> getCustomListener(p -> WDP_DEFAULT.equals(p.label()), 1));
        assertThat(nameListener.getSucceedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldCallCustomListenerForCustomProvider() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng3.xml",
                () -> getCustomListener(p -> !WDP_DEFAULT.equals(p.label()), 1));
        assertThat(nameListener.getFailedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldCallCustomListenerForDuplicateDefaultProviders() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng3.xml",
                () -> getCustomListener(p -> WDP_DEFAULT.equals(p.label()), 2));
        assertThat(nameListener.getFailedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldHaveInjectedSessionId() {
        final InvokedMethodNameListener nameListener = run("src/test/resources/testng1.xml",
                this::getDefaultListener);
        assertThat(nameListener.getSessionIds()).hasSize(1);
    }

    private BeforeMethodListener getDefaultListener() {
        return spy(new BeforeMethodListener());
    }

    private BeforeMethodListener getCustomListener(final Predicate<WebDriverProvider> condition,
                                                   final int duplicatesAmount) {
        final BeforeMethodListener spyListener = spy(new BeforeMethodListener());
        spyListener.loadServiceProviders();
        final WebDriverProvider provider = StreamEx.of(spyListener.getWebDriverProviders())
                                                   .findFirst(condition)
                                                   .orElseThrow(() -> new IllegalStateException(
                                                           "Unable to find required provider"));
        mockProvider(provider, duplicatesAmount, spyListener);
        return spyListener;
    }

    private void mockProvider(final WebDriverProvider provider, final int duplicatesAmount,
                              final BeforeMethodListener listener) {
        final WebDriverProvider spyProvider = spy(provider);
        final WebDriver mockDriver = mock(ChromeDriver.class);
        doReturn(mockDriver).when(spyProvider).createDriver(any(), any());
        final List<WebDriverProvider> providers = duplicatesAmount > 1
                ? IntStreamEx.range(0, duplicatesAmount).mapToObj(i -> provider).toList()
                : singletonList(spyProvider);
        doReturn(providers).when(listener).getWebDriverProviders();
    }

    private InvokedMethodNameListener run(final String suitePath, final Supplier<BeforeMethodListener> listenerSupplier) {
        final TestNG tng = create();

        final InvokedMethodNameListener nameListener = new InvokedMethodNameListener();
        final BeforeMethodListener spyListener = listenerSupplier.get();

        tng.addListener((ITestNGListener) nameListener);
        tng.addListener((ITestNGListener) spyListener);
        tng.setTestSuites(singletonList(suitePath));
        tng.run();

        return nameListener;
    }

    private TestNG create() {
        final TestNG result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);
        return result;
    }
}
