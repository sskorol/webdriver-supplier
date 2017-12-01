package io.github.sskorol.config;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Platform;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.openqa.selenium.remote.CapabilityType.*;

/**
 * TestNG xml wrapper, which stores parsed browser configuration.
 */
@RequiredArgsConstructor
public class XmlConfig {

    public static final String TEST_NAME = "testName";
    private final Map<String, String> parameters;

    public String getBrowser() {
        return getValue(BROWSER_NAME)
                .orElseThrow(() -> new IllegalArgumentException("browserName parameter is required"));
    }

    public String getVersion() {
        return getValue(VERSION).orElse("");
    }

    public Platform getPlatform() {
        return getValue(PLATFORM_NAME)
                .map(String::toUpperCase)
                .map(Platform::fromString)
                .orElseGet(Platform::getCurrent);
    }

    public String getTestName() {
        return getValue(TEST_NAME).orElse(UUID.randomUUID().toString());
    }

    public boolean hasBrowser() {
        return hasValue(BROWSER_NAME);
    }

    public boolean hasPlatform() {
        return hasValue(PLATFORM_NAME);
    }

    public boolean hasVersion() {
        return hasValue(VERSION);
    }

    public boolean hasValue(final String key) {
        return getValue(key).isPresent();
    }

    public Optional<String> getValue(final String key) {
        return ofNullable(parameters.get(key));
    }
}
