package io.github.sskorol.listeners;

import io.github.sskorol.config.XmlConfig;
import one.util.streamex.StreamEx;
import org.testng.*;

import java.util.*;

import static io.github.sskorol.utils.TestNGUtils.getBrowserConfiguration;

/**
 * Key listener which should be included on client side. Creates / cleans WebDrivers before/after test invocation.
 */
@SuppressWarnings("FinalLocalVariable")
public class BeforeMethodListener extends BaseListener implements IInvokedMethodListener, ISuiteListener {

    @Override
    public void onStart(final ISuite suite) {
        loadServiceProviders();
    }

    @Override
    public void onFinish(final ISuite suite) {
        unloadServiceProviders();
    }

    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            var configs = getBrowserConfiguration(
                testResult.getTestContext().getCurrentXmlTest(), method
            ).toList();

            var customParameters = StreamEx.of(configs)
                                           .filter(Optional::isPresent)
                                           .map(Optional::get)
                                           .map(XmlConfig::getCustomParameters)
                                           .reduce(new HashMap<>(), (map, parameters) -> {
                                               map.putAll(parameters);
                                               return map;
                                           });

            var config = StreamEx.of(configs)
                                 .filter(Optional::isPresent)
                                 .map(Optional::get)
                                 .findFirst(XmlConfig::hasBrowser)
                                 .map(xmlConfig -> xmlConfig.extendParameters(customParameters))
                                 .orElseThrow(() -> new SkipException(
                                     "Unable to find a valid browser configuration. "
                                     + "Check if SPI implementation class is provided, "
                                     + "and browserName parameter is specified in xml."
                                 ));
            setupDriver(config, testResult);
        }
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            cleanUp(testResult);
        }
    }
}
