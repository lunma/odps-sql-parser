package com.sea.odps.sql.core.segment.dml.window;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 窗口定义片段，表示 WINDOW 子句中的一个窗口定义（如 `w1 AS (PARTITION BY dept ORDER BY salary)`）。 */
@RequiredArgsConstructor
@Getter
public final class WindowDefinitionSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    /** 窗口名称（如 `w1`）。 */
    private final IdentifierValue windowName;

    /** 窗口规范。 */
    private final WindowSpecificationSegment specification;
}
