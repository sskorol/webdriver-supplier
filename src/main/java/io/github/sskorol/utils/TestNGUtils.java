package io.github.sskorol.utils;

import io.github.sskorol.config.XmlConfig;
import one.util.streamex.StreamEx;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

import static io.github.sskorol.config.XmlConfig.TEST_NAME;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static org.joor.Reflect.on;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

@SuppressWarnings("JavadocType")
public final class TestNGUtils {

    private TestNGUtils() {
        throw new UnsupportedOperationException("Illegal access to private constructor");
    }

    public static StreamEx<Optional<XmlConfig>> getBrowserConfiguration(final XmlTest xmlTest, final String method) {
        return StreamEx.of(getMethodBrowserConfiguration(xmlTest, method),
                getClassBrowserConfiguration(xmlTest, method),
                getTestGroupBrowserConfiguration(xmlTest, method),
                getSuiteBrowserConfiguration(xmlTest.getSuite(), method));
    }

    public static Optional<XmlConfig> getMethodBrowserConfiguration(final XmlTest xmlTest, final String method) {
        return StreamEx.of(xmlTest.getClasses())
                       .flatMap(xmlClass -> StreamEx.of(xmlClass.getIncludedMethods()))
                       .filter(xmlInclude -> xmlInclude.getName().equals(method))
                       .map(XmlInclude::getAllParameters)
                       .map(parameters -> mapConfiguration(parameters, method))
                       .findFirst();
    }

    public static Optional<XmlConfig> getClassBrowserConfiguration(final XmlTest xmlTest, final String method) {
        return StreamEx.of(xmlTest.getClasses())
                       .filter(xmlClass -> isMethodPresent(xmlClass, method))
                       .map(XmlClass::getAllParameters)
                       .map(parameters -> mapConfiguration(parameters, method))
                       .findFirst();
    }

    public static Optional<XmlConfig> getTestGroupBrowserConfiguration(final XmlTest xmlTest, final String method) {
        final Map<String, String> parameters = xmlTest.getAllParameters();
        parameters.putIfAbsent(TEST_NAME, method);
        return Optional.of(new XmlConfig(parameters));
    }

    public static Optional<XmlConfig> getSuiteBrowserConfiguration(final XmlSuite xmlSuite, final String method) {
        final Map<String, String> parameters = new HashMap<>();
        ofNullable(xmlSuite.getParameter(BROWSER_NAME)).ifPresent(val -> parameters.put(BROWSER_NAME, val));
        ofNullable(xmlSuite.getParameter(VERSION)).ifPresent(val -> parameters.put(VERSION, val));
        ofNullable(xmlSuite.getParameter(PLATFORM)).ifPresent(val -> parameters.put(PLATFORM, val));
        parameters.putIfAbsent(TEST_NAME, method);
        return Optional.of(new XmlConfig(unmodifiableMap(parameters)));
    }

    public static boolean isMethodPresent(final XmlClass xmlClass, final String method) {
        return StreamEx.of(xmlClass.getIncludedMethods())
                       .anyMatch(xmlInclude -> xmlInclude.getName().equals(method));
    }

    public static XmlConfig mapConfiguration(final Map<String, String> parameters, final String method) {
        parameters.putIfAbsent(TEST_NAME, method);
        return on(XmlConfig.class).create(parameters).get();
    }
}
