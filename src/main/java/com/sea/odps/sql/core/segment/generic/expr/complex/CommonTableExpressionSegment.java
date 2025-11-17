package com.sea.odps.sql.core.segment.generic.expr.complex;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.segment.generic.column.ColumnSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.subquery.SubquerySegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 公共表表达式（CTE）片段，表示 WITH 子句中的一个 CTE 定义（如 `WITH cte AS (SELECT ...)`）。 包含 CTE 名称、子查询和列名列表（可选）。 */
@RequiredArgsConstructor
@Getter
public class CommonTableExpressionSegment implements ExpressionSegment {

  private final int startIndex;

  private final int stopIndex;

  private final IdentifierValue identifier;

  private final SubquerySegment subquery;

  private final Collection<ColumnSegment> columns = new LinkedList<>();
}
