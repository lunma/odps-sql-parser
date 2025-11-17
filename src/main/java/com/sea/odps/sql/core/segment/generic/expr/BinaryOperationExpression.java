package com.sea.odps.sql.core.segment.generic.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 二元运算表达式片段，表示 SQL 中的二元运算（如 `a + b`、`x > y`、`a AND b` 等）。 */
@RequiredArgsConstructor
@Getter
@Setter
public final class BinaryOperationExpression implements ExpressionSegment {

  private final int startIndex;

  private final int stopIndex;

  private final ExpressionSegment left;

  private final ExpressionSegment right;

  private final String operator;

  private final String text;
}
