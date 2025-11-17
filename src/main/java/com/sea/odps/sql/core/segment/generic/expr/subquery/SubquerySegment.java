package com.sea.odps.sql.core.segment.generic.expr.subquery;

import com.sea.odps.sql.core.enums.SubqueryType;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.statement.dml.SelectStatement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 子查询表达式片段，表示 SQL 中的子查询（如 `(SELECT ...)`）。 可以用作表达式、表或条件。 */
@RequiredArgsConstructor
@Getter
public class SubquerySegment implements ExpressionSegment {
  private final int startIndex;

  private final int stopIndex;

  private final SelectStatement select;

  @Setter private SubqueryType subqueryType;
}
