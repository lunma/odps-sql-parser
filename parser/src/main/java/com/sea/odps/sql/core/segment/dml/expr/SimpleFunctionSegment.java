package com.sea.odps.sql.core.segment.dml.expr;

import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 简单函数调用片段，表示不带 OVER 子句的函数调用（如 `nvl(table_name, '1')`）。 用于正确提取函数参数中的列引用。 */
@RequiredArgsConstructor
@Getter
public final class SimpleFunctionSegment implements ExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    /** 函数名称。 */
    private final String functionName;

    /** 函数参数表达式列表。 */
    private final java.util.List<ExpressionSegment> arguments;

    /** 原始 SQL 文本。 */
    private final String originalText;
}
