package io.github.sskorol.utils;

import io.github.sskorol.config.XmlConfig;
import one.util.streamex.StreamEx;
import org.testng.IInvokedMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.*;

import static io.github.sskorol.config.XmlConfig.TEST_NAME;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static org.joor.Reflect.onClass;
import static org.openqa.selenium.remote.CapabilityType.*;

@SuppressWarnings({"MissingJavadocType", "FinalLocalVariable"})
public final class TestNGUtils {

    private TestNGUtils() {
        throw new UnsupportedOperationException("Illegal access to private constructor");
    }

    public static StreamEx<Optional<XmlConfig>> getBrowserConfiguration(
        final XmlTest xmlTest,
        final IInvokedMethod method
    ) {
        var testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        var methodConfigs = getMethodBrowserConfiguration(xmlTest, testMethod);
        var classConfigs = getClassBrowserConfiguration(xmlTest, testMethod);
        var testConfigs = getTestGroupBrowserConfiguration(xmlTest, testMethod);
        var suiteConfigs = getSuiteBrowserConfiguration(xmlTest.getSuite(), testMethod);
        return StreamEx.of(methodConfigs, classConfigs, testConfigs, suiteConfigs);
    }

    public static Optional<XmlConfig> getMethodBrowserConfiguration(final XmlTest xmlTest, final Method method) {
        return StreamEx.of(xmlTest.getClasses())
                       .filter(xmlClass -> xmlClass.getName().equalsIgnoreCase(method.getDeclaringClass().getName()))
                       .flatMap(xmlClass -> StreamEx.of(xmlClass.getIncludedMethods()))
                       .filter(xmlInclude -> xmlInclude.getName().equals(method.getName()))
                       .map(XmlInclude::getAllParameters)
                       .map(parameters -> mapConfiguration(parameters, method))
                       .findFirst();
    }

    public static Optional<XmlConfig> getClassBrowserConfiguration(final XmlTest xmlTest, final Method method) {
        return StreamEx.of(xmlTest.getClasses())
                       .filter(xmlClass -> isMethodPresent(xmlClass, method))
                       .map(XmlClass::getAllParameters)
                       .map(parameters -> mapConfiguration(parameters, method))
                       .findFirst();
    }

    public static Optional<XmlConfig> getTestGroupBrowserConfiguration(final XmlTest xmlTest, final Method method) {
        final Map<String, String> parameters = xmlTest.getAllParameters();
        parameters.putIfAbsent(TEST_NAME, method.getName());
        return Optional.of(new XmlConfig(parameters));
    }

    public static Optional<XmlConfig> getSuiteBrowserConfiguration(final XmlSuite xmlSuite, final Method method) {
        final Map<String, String> parameters = new HashMap<>();
        ofNullable(xmlSuite.getParameter(BROWSER_NAME)).ifPresent(val -> parameters.put(BROWSER_NAME, val));
        ofNullable(xmlSuite.getParameter(BROWSER_VERSION)).ifPresent(val -> parameters.put(BROWSER_VERSION, val));
        ofNullable(xmlSuite.getParameter(PLATFORM_NAME)).ifPresent(val -> parameters.put(PLATFORM_NAME, val));
        parameters.putIfAbsent(TEST_NAME, method.getName());
        return Optional.of(new XmlConfig(unmodifiableMap(parameters)));
    }

    public static boolean isMethodPresent(final XmlClass xmlClass, final Method method) {
        if (!xmlClass.getName().equalsIgnoreCase(method.getDeclaringClass().getName())) {
            return false;
        }

        var methods = xmlClass.getIncludedMethods();
        return methods.isEmpty() || StreamEx.of(methods).anyMatch(
            xmlInclude -> xmlInclude.getName().equals(method.getName())
        );
    }

    public static XmlConfig mapConfiguration(final Map<String, String> parameters, final Method method) {
        parameters.putIfAbsent(TEST_NAME, method.getName());
        return onClass(XmlConfig.class).create(parameters).get();
    }
}
