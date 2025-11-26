package com.sea.odps.sql.core.segment.dml.order.item;

import com.sea.odps.sql.core.enums.NullsOrderType;
import com.sea.odps.sql.core.enums.OrderDirection;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;

/** 表达式排序项片段，扩展 OrderByItemSegment，包含排序表达式。 用于 ORDER BY 和 GROUP BY 子句中的表达式排序项。 */
@Getter
public final class ExpressionOrderByItemSegment extends OrderByItemSegment {

    private final ExpressionSegment expression;

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param expression 排序表达式
     * @param orderDirection 排序方向
     * @param nullsOrderType NULL 值排序方式
     */
    public ExpressionOrderByItemSegment(
            final int startIndex,
            final int stopIndex,
            final ExpressionSegment expression,
            final OrderDirection orderDirection,
            final NullsOrderType nullsOrderType) {
        super(startIndex, stopIndex, orderDirection, nullsOrderType);
        this.expression = expression;
    }
}
