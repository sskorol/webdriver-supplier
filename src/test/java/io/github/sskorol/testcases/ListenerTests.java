package io.github.sskorol.testcases;

import io.github.sskorol.core.WebDriverProvider;
import io.github.sskorol.listeners.BeforeMethodListener;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.sskorol.core.WebDriverFactory.WDP_DEFAULT;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ListenerTests {

    @Test
    public void shouldCallDefaultListenerWithMockProvider() {
        var nameListener = run("src/test/resources/testng1.xml", this::getDefaultListener);
        assertThat(nameListener._1.getSkippedMethods()).hasSize(2);
        assertThat(nameListener._1.getSucceedMethodNames()).hasSize(1);
    }

    @Test
    public void shouldCallDefaultListenerForSuiteLevelParameters() {
        var nameListener = run("src/test/resources/testng3.xml", this::getDefaultListener);
        assertThat(nameListener._1.getSucceedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldCallCustomListenerForDefaultProvider() {
        var nameListener = run(
            "src/test/resources/testng3.xml",
            () -> getCustomListener(p -> WDP_DEFAULT.equals(p.label()), 1)
        );
        assertThat(nameListener._1.getSucceedMethodNames()).hasSize(8);
    }

    @Test
    public void shouldSupplyDefaultWebDriverMetadata() {
        var nameListener = run(
            "src/test/resources/testng3.xml",
            this::getClonedListener
        );
        var resultsContainer = ((ClonedBeforeMethodListener) nameListener._2).getResultsContainer();
        assertThat(resultsContainer)
            .hasSize(8)
            .allMatch(item -> item.isDriverCreated()
                              && item.isWebDriverWaitCreated()
                              && !item.isChromeDevToolsServiceCreated()
                              && item.getConfig() != null
            );
    }

    @Test
    public void shouldCallCustomListenerForCustomProvider() {
        var nameListener = run(
            "src/test/resources/testng3.xml",
            () -> getCustomListener(p -> !WDP_DEFAULT.equals(p.label()), 1)
        );
        assertThat(nameListener._1.getSkippedMethods()).hasSize(8);
    }

    @Test
    public void shouldCallCustomListenerForDuplicateDefaultProviders() {
        var nameListener = run(
            "src/test/resources/testng3.xml",
            () -> getCustomListener(p -> WDP_DEFAULT.equals(p.label()), 2)
        );
        assertThat(nameListener._1.getSkippedMethods()).hasSize(8);
    }

    @Test
    public void shouldHaveInjectedSessionId() {
        var nameListener = run("src/test/resources/testng1.xml", this::getDefaultListener);
        assertThat(nameListener._1.getSessionIds()).hasSize(1);
    }

    private BeforeMethodListener getDefaultListener() {
        return spy(new BeforeMethodListener());
    }

    private BeforeMethodListener getClonedListener() {
        return spy(new ClonedBeforeMethodListener());
    }

    private BeforeMethodListener getCustomListener(final Predicate<WebDriverProvider> condition,
                                                   final int duplicatesAmount) {
        var spyListener = spy(new BeforeMethodListener());
        spyListener.loadServiceProviders();
        var provider = StreamEx.of(spyListener.getWebDriverProviders())
            .findFirst(condition)
            .orElseThrow(() -> new IllegalStateException("Unable to find required provider"));
        mockProvider(provider, duplicatesAmount, spyListener);
        return spyListener;
    }

    private void mockProvider(
        final WebDriverProvider provider,
        final int duplicatesAmount,
        final BeforeMethodListener listener
    ) {
        var spyProvider = spy(provider);
        var mockDriver = mock(ChromeDriver.class);
        doReturn(mockDriver).when(spyProvider).createDriver(any(), any());
        var providers = duplicatesAmount > 1
                        ? IntStreamEx.range(0, duplicatesAmount).mapToObj(i -> provider).toList()
                        : singletonList(spyProvider);
        doReturn(providers).when(listener).getWebDriverProviders();
    }

    private Tuple2<InvokedMethodNameListener, BeforeMethodListener> run(
        final String suitePath,
        final Supplier<BeforeMethodListener> listenerSupplier
    ) {
        var tng = create();
        var nameListener = new InvokedMethodNameListener();
        var spyListener = listenerSupplier.get();

        tng.addListener(nameListener);
        tng.addListener(spyListener);
        tng.setTestSuites(singletonList(suitePath));
        tng.run();

        return Tuple.of(nameListener, spyListener);
    }

    private TestNG create() {
        var result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);
        return result;
    }
}
