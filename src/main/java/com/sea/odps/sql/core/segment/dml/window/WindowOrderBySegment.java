package com.sea.odps.sql.core.segment.dml.window;

import java.util.List;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 窗口排序规范片段，表示窗口函数中的 ORDER BY 子句。 */
@RequiredArgsConstructor
@Getter
public final class WindowOrderBySegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** 排序项列表。 */
  private final List<OrderByItemSegment> items;
}
