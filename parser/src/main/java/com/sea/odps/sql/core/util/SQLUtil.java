package com.sea.odps.sql.core.util;

import com.google.common.base.CharMatcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** SQL utility class. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtil {

    private static final String EXCLUDED_CHARACTERS = "[]`'\"";

    /**
     * Get exactly value for SQL expression.
     *
     * <p>remove special char for SQL expression
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value) {
        return null == value ? null : CharMatcher.anyOf(EXCLUDED_CHARACTERS).removeFrom(value);
    }

    /**
     * Get exactly value for SQL expression.
     *
     * <p>remove special char for SQL expression
     *
     * @param value SQL expression
     * @param reservedCharacters characters to be reserved
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value, final String reservedCharacters) {
        if (null == value) {
            return null;
        }
        String toBeExcludedCharacters =
                CharMatcher.anyOf(reservedCharacters).removeFrom(EXCLUDED_CHARACTERS);
        return CharMatcher.anyOf(toBeExcludedCharacters).removeFrom(value);
    }
}
