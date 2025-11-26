package com.sea.odps.sql.core.segment.generic.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** NOT 表达式片段，表示 SQL 中的 NOT 运算（如 `NOT condition`）。 */
@RequiredArgsConstructor
@Getter
public final class NotExpression implements ExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    private final ExpressionSegment expression;
}
