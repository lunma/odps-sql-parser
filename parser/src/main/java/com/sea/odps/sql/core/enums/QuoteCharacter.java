package com.sea.odps.sql.core.enums;

import java.util.Arrays;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 引号字符枚举，表示 SQL 标识符使用的引号类型。 */
@RequiredArgsConstructor
@Getter
public enum QuoteCharacter {

    /** 反引号（`）。 */
    BACK_QUOTE("`", "`"),

    /** 单引号（'）。 */
    SINGLE_QUOTE("'", "'"),

    /** 双引号（"）。 */
    QUOTE("\"", "\""),

    /** 方括号（[ ]）。 */
    BRACKETS("[", "]"),

    /** 圆括号（( )）。 */
    PARENTHESES("(", ")"),

    /** 无引号。 */
    NONE("", "");

    private final String startDelimiter;

    private final String endDelimiter;

    /**
     * 根据字符串获取引号字符类型。
     *
     * @param value 待检测的字符串
     * @return 引号字符类型
     */
    public static QuoteCharacter getQuoteCharacter(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return NONE;
        }
        return Arrays.stream(values())
                .filter(each -> NONE != each && each.startDelimiter.charAt(0) == value.charAt(0))
                .findFirst()
                .orElse(NONE);
    }

    /**
     * 使用引号字符包装值。
     *
     * @param value 待包装的值
     * @return 包装后的值
     */
    public String wrap(final String value) {
        return startDelimiter + value + endDelimiter;
    }
}
