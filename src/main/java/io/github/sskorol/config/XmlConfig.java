package io.github.sskorol.config;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Platform;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

/**
 * TestNG xml wrapper, which stores parsed browser configuration.
 */
@RequiredArgsConstructor
public class XmlConfig {

    private final Map<String, String> parameters;

    public String getBrowser() {
        return getValue(BROWSER_NAME)
                .orElseThrow(() -> new IllegalArgumentException("browserName parameter is required"));
    }

    public String getVersion() {
        return getValue(VERSION).orElse("");
    }

    public Platform getPlatform() {
        return getValue(PLATFORM)
                .map(String::toUpperCase)
                .map(Platform::fromString)
                .orElseGet(Platform::getCurrent);
    }

    public boolean hasBrowser() {
        return hasValue(BROWSER_NAME);
    }

    public boolean hasPlatform() {
        return hasValue(PLATFORM);
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
