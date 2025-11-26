package com.sea.odps.sql.core.segment.generic.expr.simple;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 字面量表达式片段，表示 SQL 中的字面量值（如数字、字符串、布尔值等）。 */
@RequiredArgsConstructor
@Getter
public class LiteralExpressionSegment implements SimpleExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Object literals;
}
