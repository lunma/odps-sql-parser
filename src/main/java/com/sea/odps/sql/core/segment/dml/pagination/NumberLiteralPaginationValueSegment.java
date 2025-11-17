package com.sea.odps.sql.core.segment.dml.pagination;

import lombok.Getter;

/** 数字字面量分页值片段，用于表示 LIMIT 子句中的数字值。 */
@Getter
public final class NumberLiteralPaginationValueSegment implements PaginationValueSegment {

  private final int startIndex;

  private final int stopIndex;

  private final long value;

  /**
   * 构造函数。
   *
   * @param startIndex 起始索引
   * @param stopIndex 结束索引
   * @param value 数字值
   */
  public NumberLiteralPaginationValueSegment(
      final int startIndex, final int stopIndex, final long value) {
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
    this.value = value;
  }

  @Override
  public boolean isBoundOpened() {
    return false;
  }
}
