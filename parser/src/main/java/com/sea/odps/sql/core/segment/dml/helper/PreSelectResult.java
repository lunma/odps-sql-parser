package com.sea.odps.sql.core.segment.dml.helper;

import java.util.Optional;

import com.sea.odps.sql.core.segment.dml.order.GroupBySegment;
import com.sea.odps.sql.core.segment.dml.predicate.HavingSegment;
import com.sea.odps.sql.core.segment.dml.predicate.WhereSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.visitor.core.ASTNode;

import lombok.RequiredArgsConstructor;

/**
 * SELECT 前置子句结果类，用于封装 WHERE、GROUP BY、HAVING、WINDOW 子句的片段。 这些子句在 SELECT 之后但在 ORDER BY 之前。
 * 这是一个辅助类，用于在 AST 构建过程中传递 SELECT 前置子句信息。
 */
@RequiredArgsConstructor
public final class PreSelectResult implements ASTNode {

    private final WhereSegment whereSegment;

    private final GroupBySegment groupBySegment;

    private final HavingSegment havingSegment;

    private final WindowSegment windowSegment;

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
     * 获取 WINDOW 片段。
     *
     * @return WINDOW 片段，可能为空
     */
    public Optional<WindowSegment> getWindowSegment() {
        return Optional.ofNullable(windowSegment);
    }
}
