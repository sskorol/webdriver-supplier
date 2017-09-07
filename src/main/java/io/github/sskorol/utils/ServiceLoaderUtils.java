package io.github.sskorol.utils;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.ServiceLoader;

@Slf4j
@SuppressWarnings("JavadocType")
public final class ServiceLoaderUtils {

    private ServiceLoaderUtils() {
        throw new UnsupportedOperationException("Illegal access to private constructor");
    }

    public static <T> StreamEx<T> load(final Class<T> type, final ClassLoader classLoader) {
        return Try.of(() -> StreamEx.of(ServiceLoader.load(type, classLoader).iterator()))
                  .getOrElseGet(ex -> {
                      log.error("Could not load service of type " + type + ":", ex);
                      return StreamEx.empty();
                  });
    }
}
