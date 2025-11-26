package com.sea.odps.sql.core.segment.dml.expr;

import com.sea.odps.sql.core.segment.dml.window.WindowSpecificationSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 窗口函数片段，表示带 OVER 子句的函数调用（如 `ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary)`）。 这是
 * DML（SELECT）语句特有的功能，仅在 SELECT 语句中使用。
 */
@RequiredArgsConstructor
@Getter
public final class WindowFunctionSegment implements ExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    /** 函数名称。 */
    private final String functionName;

    /** 函数参数表达式列表。 */
    private final java.util.List<ExpressionSegment> arguments;

    /** 窗口规范（OVER 子句）。 */
    private final WindowSpecificationSegment windowSpecification;

    /** 原始 SQL 文本。 */
    private final String originalText;
}
