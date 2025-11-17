package com.sea.odps.sql.core.segment.dml.order;

import java.util.Collection;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** GROUP BY 子句片段，表示 SELECT 语句的 GROUP BY 分组子句。 */
@RequiredArgsConstructor
@Getter
public class GroupBySegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** GROUP BY 分组项列表。 */
  private final Collection<OrderByItemSegment> groupByItems;

  /** GROUP BY 子句的注释。 */
  @lombok.Setter private CommentSegment comment;
}
