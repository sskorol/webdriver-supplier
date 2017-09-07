package io.github.sskorol.utils;

import io.github.sskorol.config.XmlConfig;
import one.util.streamex.StreamEx;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

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
                getTestGroupBrowserConfiguration(xmlTest),
                getSuiteBrowserConfiguration(xmlTest.getSuite()));
    }

    public static Optional<XmlConfig> getMethodBrowserConfiguration(final XmlTest xmlTest, final String method) {
        return StreamEx.of(xmlTest.getClasses())
                       .flatMap(xmlClass -> StreamEx.of(xmlClass.getIncludedMethods()))
                       .filter(xmlInclude -> xmlInclude.getName().equals(method))
                       .map(XmlInclude::getAllParameters)
                       .map(TestNGUtils::mapConfiguration)
                       .findFirst();
    }

    public static Optional<XmlConfig> getClassBrowserConfiguration(final XmlTest xmlTest, final String method) {
        return StreamEx.of(xmlTest.getClasses())
                       .filter(xmlClass -> isMethodPresent(xmlClass, method))
                       .map(XmlClass::getAllParameters)
                       .map(TestNGUtils::mapConfiguration)
                       .findFirst();
    }

    public static Optional<XmlConfig> getTestGroupBrowserConfiguration(final XmlTest xmlTest) {
        return Optional.of(new XmlConfig(xmlTest.getAllParameters()));
    }

    public static Optional<XmlConfig> getSuiteBrowserConfiguration(final XmlSuite xmlSuite) {
        final Map<String, String> parameters = new HashMap<>();
        ofNullable(xmlSuite.getParameter(BROWSER_NAME)).ifPresent(val -> parameters.put(BROWSER_NAME, val));
        ofNullable(xmlSuite.getParameter(VERSION)).ifPresent(val -> parameters.put(VERSION, val));
        ofNullable(xmlSuite.getParameter(PLATFORM)).ifPresent(val -> parameters.put(PLATFORM, val));
        return Optional.of(new XmlConfig(unmodifiableMap(parameters)));
    }

    public static boolean isMethodPresent(final XmlClass xmlClass, final String method) {
        return StreamEx.of(xmlClass.getIncludedMethods())
                       .anyMatch(xmlInclude -> xmlInclude.getName().equals(method));
    }

    public static XmlConfig mapConfiguration(final Map<String, String> parameters) {
        return on(XmlConfig.class).create(parameters).get();
    }
}
