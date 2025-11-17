package com.sea.odps.sql.metadata.model.clause;

import lombok.Getter;

/** LIMIT 元数据。 */
@Getter
public final class LimitMetadata {

  /** 偏移量（OFFSET 值）。 */
  private final Long offset;

  /** 行数（LIMIT 值）。 */
  private final Long rowCount;

  /**
   * 构造函数。
   *
   * @param offset 偏移量
   * @param rowCount 行数
   */
  public LimitMetadata(final Long offset, final Long rowCount) {
    this.offset = offset;
    this.rowCount = rowCount;
  }
}
