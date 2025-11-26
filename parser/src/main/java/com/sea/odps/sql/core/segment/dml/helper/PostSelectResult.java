package com.sea.odps.sql.core.segment.dml.helper;

import java.util.Optional;

import com.sea.odps.sql.core.segment.dml.order.OrderBySegment;
import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.visitor.core.ASTNode;

import lombok.RequiredArgsConstructor;

/**
 * SELECT 后置子句结果类，用于封装 ORDER BY、LIMIT 子句的片段。 这些子句在 SELECT 语句的最后部分。 这是一个辅助类，用于在 AST 构建过程中传递 SELECT
 * 后置子句信息。
 */
@RequiredArgsConstructor
public final class PostSelectResult implements ASTNode {

    private final OrderBySegment orderBySegment;

    private final LimitSegment limitSegment;

    /**
     * 获取 ORDER BY 片段。
     *
     * @return ORDER BY 片段，可能为空
     */
    public Optional<OrderBySegment> getOrderBySegment() {
        return Optional.ofNullable(orderBySegment);
    }

    /**
     * 获取 LIMIT 片段。
     *
     * @return LIMIT 片段，可能为空
     */
    public Optional<LimitSegment> getLimitSegment() {
        return Optional.ofNullable(limitSegment);
    }
}
