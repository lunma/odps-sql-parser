package com.sea.odps.sql.core.segment.generic;

import com.sea.odps.sql.core.segment.SQLSegment;

/** 注释片段，表示 SQL 中的注释（单行注释 -- 或多行注释 \/* *\/）。 */
public class CommentSegment implements SQLSegment {
  /** 注释文本内容。 */
  private final String text;

  private final int startIndex;

  private final int stopIndex;

  /**
   * 构造函数。
   *
   * @param text 注释文本
   * @param startIndex 起始索引
   * @param stopIndex 结束索引
   */
  public CommentSegment(String text, int startIndex, int stopIndex) {
    this.text = text;
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
  }

  /**
   * 获取注释文本内容。
   *
   * @return 注释文本
   */
  public String getText() {
    return text;
  }

  @Override
  public int getStartIndex() {
    return startIndex;
  }

  @Override
  public int getStopIndex() {
    return stopIndex;
  }
}
