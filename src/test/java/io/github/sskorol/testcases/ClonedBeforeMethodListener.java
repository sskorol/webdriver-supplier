package io.github.sskorol.testcases;

import io.github.sskorol.listeners.BeforeMethodListener;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

public class ClonedBeforeMethodListener extends BeforeMethodListener {
    private static final ThreadLocal<List<MetaDataContainer>> results = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        var driverMetaData = getDriverMetaData();
        getResultsContainer().add(new MetaDataContainer(
            driverMetaData.getWebDriver() != null,
            driverMetaData.getWebDriverWait() != null,
            driverMetaData.getDevToolsService() != null,
            driverMetaData.getConfig()));
    }

    public List<MetaDataContainer> getResultsContainer() {
        return ofNullable(results.get())
            .orElseThrow(() -> new IllegalStateException("Results container is not initialized"));
    }
}
