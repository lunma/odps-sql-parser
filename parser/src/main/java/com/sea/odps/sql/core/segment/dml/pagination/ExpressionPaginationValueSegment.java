package com.sea.odps.sql.core.segment.dml.pagination;

import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;

/** 表达式分页值片段，用于表示 LIMIT 子句中的表达式值（如 `LIMIT 10 + 5`）。 */
@Getter
public final class ExpressionPaginationValueSegment implements PaginationValueSegment {

    private final int startIndex;

    private final int stopIndex;

    private final ExpressionSegment expression;

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param expression 表达式片段
     */
    public ExpressionPaginationValueSegment(
            final int startIndex, final int stopIndex, final ExpressionSegment expression) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.expression = expression;
    }

    @Override
    public boolean isBoundOpened() {
        return false;
    }
}
