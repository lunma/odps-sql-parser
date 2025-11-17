package com.sea.odps.sql.core.segment.dml;

import java.util.Collection;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.expr.complex.CommonTableExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** With segment，表示 WITH CTE 子句。 这是 DML（SELECT）语句特有的功能，仅在 SELECT 语句中使用。 */
@RequiredArgsConstructor
@Getter
public final class WithSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  private final Collection<CommonTableExpressionSegment> commonTableExpressions;

  @lombok.Setter private CommentSegment comment;
}
