package io.github.sskorol.core;

import org.testng.ITestResult;

/**
 * Special SPI, which allows a flexible screenshots handling on client side.
 */
public interface ScreenshotConsumer {

    void handle(byte[] screenshot, ITestResult testResult);
}
