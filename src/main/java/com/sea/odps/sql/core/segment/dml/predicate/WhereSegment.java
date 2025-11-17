package com.sea.odps.sql.core.segment.dml.predicate;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/** WHERE 子句片段，表示 SELECT 语句的 WHERE 条件子句。 */
@RequiredArgsConstructor
@Data
public class WhereSegment implements SQLSegment {
  private final int startIndex;

  private final int stopIndex;

  /** WHERE 条件表达式。 */
  private final ExpressionSegment expr;

  /** WHERE 子句的注释。 */
  private CommentSegment comment;
}
