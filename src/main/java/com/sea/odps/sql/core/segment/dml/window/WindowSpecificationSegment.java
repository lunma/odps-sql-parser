package com.sea.odps.sql.core.segment.dml.window;

import java.util.Optional;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 窗口规范片段，表示窗口函数的窗口规范。 可以是命名窗口引用（如 `w1`）或内联窗口规范（如 `(PARTITION BY dept ORDER BY salary)`）。 */
@RequiredArgsConstructor
@Getter
public final class WindowSpecificationSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  /** 命名窗口引用（如果引用已定义的窗口）。 */
  private final IdentifierValue windowNameRef;

  /** 分区规范（PARTITION BY 子句）。 */
  private final PartitionBySegment partitionBy;

  /** 排序规范（ORDER BY 子句）。 */
  private final WindowOrderBySegment orderBy;

  /** 窗口框架。 */
  private final WindowFrameSegment windowFrame;

  /**
   * 获取命名窗口引用。
   *
   * @return 命名窗口引用，如果不存在则返回空
   */
  public Optional<IdentifierValue> getWindowNameRef() {
    return Optional.ofNullable(windowNameRef);
  }
}
