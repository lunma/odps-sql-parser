package com.sea.odps.sql.metadata.model.clause;

import java.util.Collections;
import java.util.List;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;

import lombok.Getter;

/** ORDER BY 元数据。 */
@Getter
public final class OrderByMetadata {

  /** 排序列引用列表。 */
  private final List<ColumnReference> columns;

  /**
   * 构造函数。
   *
   * @param columns 排序列引用列表
   */
  public OrderByMetadata(final List<ColumnReference> columns) {
    this.columns =
        columns != null ? Collections.unmodifiableList(columns) : Collections.emptyList();
  }
}
