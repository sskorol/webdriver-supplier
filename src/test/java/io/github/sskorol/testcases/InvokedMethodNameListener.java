package io.github.sskorol.testcases;

import one.util.streamex.StreamEx;
import org.testng.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.vavr.API.*;
import static org.testng.ITestResult.*;

public class InvokedMethodNameListener implements IInvokedMethodListener, ITestListener {

    private final List<String> foundMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> invokedMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> failedMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> failedBeforeInvocationMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> skippedMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> skippedBeforeInvocationMethodNames = new CopyOnWriteArrayList<>();
    private final List<String> succeedMethodNames = new CopyOnWriteArrayList<>();
    private final Map<String, ITestResult> results = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> threads = new ConcurrentHashMap<>();

    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            final String rawMethodName = method.getTestMethod().getMethodName();
            final long currentThreadId = Thread.currentThread().getId();

            threads.putIfAbsent(rawMethodName, new CopyOnWriteArrayList<>());
            threads.computeIfPresent(rawMethodName,
                    (s, l) -> StreamEx.of(l).append(currentThreadId).distinct().toList());

            invokedMethodNames.add(getName(testResult));
        }
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            final String name = getName(testResult);
            Match(testResult.getStatus()).of(
                    Case($(FAILURE), () -> failedMethodNames.add(name)),
                    Case($(SKIP), () -> skippedMethodNames.add(name)),
                    Case($(SUCCESS), () -> succeedMethodNames.add(name)),
                    Case($(), () -> {
                        throw new AssertionError("Unexpected value: " + testResult.getStatus());
                    })
            );
        }
    }

    @Override
    public void onTestStart(final ITestResult result) {
        foundMethodNames.add(getName(result));
    }

    @Override
    public void onTestSuccess(final ITestResult result) {
        final String name = getName(result);
        results.put(name, result);
        if (!succeedMethodNames.contains(name)) {
            throw new IllegalStateException("A succeed test is supposed to be invoked");
        }
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        final String name = getName(result);
        results.put(name, result);
        if (!failedMethodNames.contains(name)) {
            failedBeforeInvocationMethodNames.add(name);
        }
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
        final String name = getName(result);
        results.put(name, result);
        if (!skippedMethodNames.contains(name)) {
            skippedBeforeInvocationMethodNames.add(name);
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
    }

    @Override
    public void onStart(final ITestContext context) {
    }

    @Override
    public void onFinish(final ITestContext context) {
    }

    private static String getName(final ITestResult result) {
        return result.getMethod().getConstructorOrMethod().getName()
                + "(" + getParameterNames(result.getParameters()).joining(",") + ")";
    }

    private static StreamEx<String> getParameterNames(final Object[] parameters) {
        return StreamEx.of(parameters)
                       .map(p -> p instanceof Object[]
                               ? "[" + StreamEx.of((Object[]) p).joining(",") + "]"
                               : Objects.toString(p));
    }

    public List<String> getFoundMethodNames() {
        return Collections.unmodifiableList(foundMethodNames);
    }

    public List<String> getInvokedMethodNames() {
        return Collections.unmodifiableList(invokedMethodNames);
    }

    public List<String> getFailedMethodNames() {
        return Collections.unmodifiableList(failedMethodNames);
    }

    public List<String> getSkippedMethodNames() {
        return Collections.unmodifiableList(skippedMethodNames);
    }

    public List<String> getSucceedMethodNames() {
        return Collections.unmodifiableList(succeedMethodNames);
    }

    public List<String> getFailedBeforeInvocationMethodNames() {
        return Collections.unmodifiableList(failedBeforeInvocationMethodNames);
    }

    public List<String> getSkippedBeforeInvocationMethodNames() {
        return Collections.unmodifiableList(skippedBeforeInvocationMethodNames);
    }

    public ITestResult getResult(final String name) {
        return results.get(name);
    }

    public Map<String, ITestResult> getResults() {
        return results;
    }

    public Map<String, List<Long>> getThreads() {
        return threads;
    }
}