package io.github.sskorol.listeners;

import io.github.sskorol.config.XmlConfig;
import org.testng.*;

import java.util.*;

import static io.github.sskorol.utils.TestNGUtils.getBrowserConfiguration;

/**
 * Key listener which should be included on client side. Creates / cleans WebDrivers before/after test invocation.
 */
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
            setupDriver(getBrowserConfiguration(testResult.getTestContext().getCurrentXmlTest(),
                    method.getTestMethod().getMethodName())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst(XmlConfig::hasBrowser)
                    .orElseThrow(() -> new SkipException("Unable to find a valid browser configuration."
                            + " Check if SPI implementation class is provided,"
                            + " and browserName parameter is specified in xml.")), testResult);
        }
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            cleanUp(testResult);
        }
    }
}
