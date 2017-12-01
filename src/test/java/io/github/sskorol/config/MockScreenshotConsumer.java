package io.github.sskorol.config;

import io.github.sskorol.core.ScreenshotConsumer;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static org.testng.ITestResult.FAILURE;

@Slf4j
public class MockScreenshotConsumer implements ScreenshotConsumer {

    private static final Map<byte[], ITestResult> SCREENSHOTS = new ConcurrentHashMap<>();

    @Override
    public void handle(final byte[] screenshot, final ITestResult testResult) {
        if (nonNull(screenshot) && screenshot.length > 0 && testResult.getStatus() == FAILURE) {
            SCREENSHOTS.putIfAbsent(screenshot, testResult);
        }
    }

    public static Map<byte[], ITestResult> getScreenshots() {
        return unmodifiableMap(SCREENSHOTS);
    }

    public static void clearScreenshots() {
        SCREENSHOTS.clear();
    }
}
