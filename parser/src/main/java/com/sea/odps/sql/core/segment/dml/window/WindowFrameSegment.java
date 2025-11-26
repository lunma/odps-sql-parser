package com.sea.odps.sql.core.segment.dml.window;

import com.sea.odps.sql.core.segment.SQLSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 窗口框架片段，表示窗口函数的窗口框架（如 `ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING`）。 */
@RequiredArgsConstructor
@Getter
public final class WindowFrameSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    /** 框架类型：ROWS、RANGE 或 GROUPS。 */
    private final String frameType;

    /** 起始边界。 */
    private final WindowFrameBoundarySegment startBoundary;

    /** 结束边界（如果使用 BETWEEN...AND 格式）。 */
    private final WindowFrameBoundarySegment endBoundary;

    /** 框架排除选项。 */
    private final String exclusion;
}
