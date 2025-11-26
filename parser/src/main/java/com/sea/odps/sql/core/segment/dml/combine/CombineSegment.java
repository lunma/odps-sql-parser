package com.sea.odps.sql.core.segment.dml.combine;

import com.sea.odps.sql.core.enums.CombineType;
import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.statement.dml.SelectStatement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 集合操作片段，表示 SELECT 语句的集合操作（如 UNION、INTERSECT、EXCEPT）。 包含左查询、操作类型和右查询。 */
@RequiredArgsConstructor
@Getter
public final class CombineSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final SelectStatement left;

    private final CombineType combineType;

    private final SelectStatement right;
}
