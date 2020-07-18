package io.github.sskorol.utils;

import one.util.streamex.StreamEx;
import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.Dimension;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * A helper class for strings processing.
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Illegal access to private constructor");
    }

    public static Optional<Dimension> toDimension(final String value) {
        return ofNullable(value)
                .map(val -> StreamEx.of(val.split("x"))
                                    .filter(NumberUtils::isDigits)
                                    .mapToInt(Integer::parseInt)
                                    .toArray())
                .filter(val -> val.length >= 2)
                .map(val -> new Dimension(val[0], val[1]));
    }
}
