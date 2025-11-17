package com.sea.odps.sql.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** SQL utility class. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtil {

  private static final String SQL_END = ";";

  private static final String COMMENT_PREFIX = "/*";

  private static final String COMMENT_SUFFIX = "*/";

  private static final String EXCLUDED_CHARACTERS = "[]`'\"";

  private static final Pattern SINGLE_CHARACTER_PATTERN = Pattern.compile("^_|([^\\\\])_");

  private static final Pattern SINGLE_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\_");

  private static final Pattern ANY_CHARACTER_PATTERN = Pattern.compile("^%|([^\\\\])%");

  private static final Pattern ANY_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\%");

  /**
   * Get exactly number value and type.
   *
   * @param value string to be converted
   * @param radix radix
   * @return exactly number value and type
   */
  public static Number getExactlyNumber(final String value, final int radix) {
    try {
      return getBigInteger(value, radix);
    } catch (final NumberFormatException ex) {
      return new BigDecimal(value);
    }
  }

  private static Number getBigInteger(final String value, final int radix) {
    BigInteger result = new BigInteger(value, radix);
    if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0
        && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
      return result.intValue();
    }
    if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0
        && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
      return result.longValue();
    }
    return result;
  }

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

  /**
   * Get exactly SQL expression.
   *
   * <p>remove space for SQL expression
   *
   * @param value SQL expression
   * @return exactly SQL expression
   */
  public static String getExactlyExpression(final String value) {
    return Strings.isNullOrEmpty(value) ? value : CharMatcher.anyOf(" ").removeFrom(value);
  }

  /**
   * Trim the semicolon of SQL.
   *
   * @param sql SQL to be trim
   * @return SQL without semicolon
   */
  public static String trimSemicolon(final String sql) {
    return sql.endsWith(SQL_END) ? sql.substring(0, sql.length() - 1) : sql;
  }

  /**
   * Trim the comment of SQL.
   *
   * @param sql SQL to be trim
   * @return remove comment from SQL
   */
  public static String trimComment(final String sql) {
    String result = sql;
    if (sql.startsWith(COMMENT_PREFIX)) {
      result = result.substring(sql.indexOf(COMMENT_SUFFIX) + 2);
    }
    if (sql.endsWith(SQL_END)) {
      result = result.substring(0, result.length() - 1);
    }
    return result.trim();
  }

  /**
   * Convert like pattern to regex.
   *
   * @param pattern like pattern
   * @return regex
   */
  public static String convertLikePatternToRegex(final String pattern) {
    String result = pattern;
    if (pattern.contains("_")) {
      result = SINGLE_CHARACTER_PATTERN.matcher(result).replaceAll("$1.");
      result = SINGLE_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("_");
    }
    if (pattern.contains("%")) {
      result = ANY_CHARACTER_PATTERN.matcher(result).replaceAll("$1.*");
      result = ANY_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("%");
    }
    return result;
  }
}
