package io.github.sskorol.testcases;

import io.github.sskorol.utils.ServiceLoaderUtils;
import io.github.sskorol.utils.TestNGUtils;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;
import static org.joor.Reflect.on;

public class ReflectionTests {

    @Test
    public void shouldThrowAnExceptionOnTestNGUtilsConstructorAccess() {
        assertThatThrownBy(() -> on(TestNGUtils.class).create())
                .hasStackTraceContaining("java.lang.UnsupportedOperationException: Illegal access to private constructor");
    }

    @Test
    public void shouldThrowAnExceptionOnServiceLoaderUtilsConstructorAccess() {
        assertThatThrownBy(() -> on(ServiceLoaderUtils.class).create())
                .hasStackTraceContaining("java.lang.UnsupportedOperationException: Illegal access to private constructor");
    }

    @Test
    public void shouldReturnEmptyCollectionInCaseOfException() {
        assertThat(ServiceLoaderUtils.load(null, null)).isEmpty();
    }
}
