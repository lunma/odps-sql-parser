package com.sea.odps.sql.core.segment.dml.helper;

import java.util.Optional;

import com.sea.odps.sql.core.segment.dml.LateralViewSegment;
import com.sea.odps.sql.core.segment.dml.order.GroupBySegment;
import com.sea.odps.sql.core.segment.dml.order.OrderBySegment;
import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.core.segment.dml.predicate.HavingSegment;
import com.sea.odps.sql.core.segment.dml.predicate.WhereSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.core.segment.generic.table.TableSegment;
import com.sea.odps.sql.visitor.core.ASTNode;

import lombok.RequiredArgsConstructor;

/**
 * SELECT 后续子句结果类，用于封装 FROM、WHERE、GROUP BY、HAVING、ORDER BY、LIMIT、WINDOW、LATERAL VIEW 等子句的片段。
 * 这是一个辅助类，用于在 AST 构建过程中传递 SELECT 语句的各个子句信息。
 */
@RequiredArgsConstructor
public final class SelectRestResult implements ASTNode {

    private final TableSegment tableSegment;

    private final WhereSegment whereSegment;

    private final GroupBySegment groupBySegment;

    private final HavingSegment havingSegment;

    private final OrderBySegment orderBySegment;

    private final LimitSegment limitSegment;

    private final WindowSegment windowSegment;

    private final LateralViewSegment lateralViewSegment;

    /**
     * 获取表片段。
     *
     * @return 表片段
     */
    public TableSegment getTableSegment() {
        return tableSegment;
    }

    /**
     * 获取 WHERE 片段。
     *
     * @return WHERE 片段，可能为空
     */
    public Optional<WhereSegment> getWhereSegment() {
        return Optional.ofNullable(whereSegment);
    }

    /**
     * 获取 GROUP BY 片段。
     *
     * @return GROUP BY 片段，可能为空
     */
    public Optional<GroupBySegment> getGroupBySegment() {
        return Optional.ofNullable(groupBySegment);
    }

    /**
     * 获取 HAVING 片段。
     *
     * @return HAVING 片段，可能为空
     */
    public Optional<HavingSegment> getHavingSegment() {
        return Optional.ofNullable(havingSegment);
    }

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

    /**
     * 获取 WINDOW 片段。
     *
     * @return WINDOW 片段，可能为空
     */
    public Optional<WindowSegment> getWindowSegment() {
        return Optional.ofNullable(windowSegment);
    }

    /**
     * 获取 LATERAL VIEW 片段。
     *
     * @return LATERAL VIEW 片段，可能为空
     */
    public Optional<LateralViewSegment> getLateralViewSegment() {
        return Optional.ofNullable(lateralViewSegment);
    }
}
