package com.sea.odps.sql.core.segment.dml.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sea.odps.sql.core.segment.SQLSegment;

import lombok.Getter;

/** Window segment，表示 WINDOW 子句。 这是 DML（SELECT）语句特有的功能，仅在 SELECT 语句中使用。 */
@Getter
public final class WindowSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** 窗口定义列表。 */
  private final List<WindowDefinitionSegment> windowDefinitions;

  /**
   * 构造函数。
   *
   * @param startIndex 起始索引
   * @param stopIndex 结束索引
   * @param windowDefinitions 窗口定义列表
   */
  public WindowSegment(
      final int startIndex,
      final int stopIndex,
      final List<WindowDefinitionSegment> windowDefinitions) {
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
    this.windowDefinitions =
        null == windowDefinitions
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(windowDefinitions));
  }
}
