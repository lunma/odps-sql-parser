package com.sea.odps.sql.core.segment.dml.window;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 窗口框架边界片段，表示窗口框架的边界（如 `UNBOUNDED PRECEDING`、`1 PRECEDING`、`CURRENT ROW` 等）。 */
@RequiredArgsConstructor
@Getter
public final class WindowFrameBoundarySegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** 边界类型：UNBOUNDED_PRECEDING、UNBOUNDED_FOLLOWING、CURRENT_ROW、PRECEDING、FOLLOWING。 */
  private final String boundaryType;

  /** 偏移量表达式（用于 PRECEDING 或 FOLLOWING，如 `1 PRECEDING` 中的 `1`）。 */
  private final ExpressionSegment offsetExpression;
}
