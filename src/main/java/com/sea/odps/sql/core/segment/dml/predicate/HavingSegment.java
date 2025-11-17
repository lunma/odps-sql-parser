package com.sea.odps.sql.core.segment.dml.predicate;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** HAVING 子句片段，表示 SELECT 语句的 HAVING 条件子句。 HAVING 子句用于在 GROUP BY 之后对分组结果进行过滤。 */
@RequiredArgsConstructor
@Getter
@Setter
public final class HavingSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** HAVING 条件表达式。 */
  private final ExpressionSegment expr;

  /** HAVING 子句的注释。 */
  @Setter private CommentSegment comment;
}
