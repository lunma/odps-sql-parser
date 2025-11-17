package com.sea.odps.sql.core.segment.dml.window;

import java.util.List;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 分区规范片段，表示 PARTITION BY 子句。 */
@RequiredArgsConstructor
@Getter
public final class PartitionBySegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** 分区表达式列表。 */
  private final List<ExpressionSegment> expressions;
}
