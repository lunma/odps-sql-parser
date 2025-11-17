package com.sea.odps.sql.core.segment.generic;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

/** 别名片段，表示 SQL 中的别名（如 `table AS t` 中的 `t`，或 `column AS c` 中的 `c`）。 */
public class AliasSegment implements SQLSegment {
  private final int startIndex;

  private final int stopIndex;

  /** 别名标识符。 */
  private final IdentifierValue identifier;

  /**
   * 构造函数。
   *
   * @param startIndex 起始索引
   * @param stopIndex 结束索引
   * @param identifier 别名标识符
   */
  public AliasSegment(int startIndex, int stopIndex, IdentifierValue identifier) {
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
    this.identifier = identifier;
  }

  @Override
  public int getStartIndex() {
    return startIndex;
  }

  @Override
  public int getStopIndex() {
    return stopIndex;
  }

  /**
   * 获取别名标识符。
   *
   * @return 别名标识符
   */
  public IdentifierValue getIdentifier() {
    return identifier;
  }
}
