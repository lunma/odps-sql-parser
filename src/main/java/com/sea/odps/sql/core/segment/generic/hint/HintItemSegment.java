package com.sea.odps.sql.core.segment.generic.hint;

import com.sea.odps.sql.core.segment.SQLSegment;

import lombok.Getter;

/** HINT 项片段，表示单个优化提示项。 */
@Getter
public final class HintItemSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  private final String hintText;

  /**
   * 构造函数。
   *
   * @param startIndex 起始索引
   * @param stopIndex 结束索引
   * @param hintText HINT 文本内容
   */
  public HintItemSegment(final int startIndex, final int stopIndex, final String hintText) {
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
    this.hintText = hintText;
  }
}
