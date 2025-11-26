package com.sea.odps.sql.core.statement.dml;

import java.util.Optional;

import com.sea.odps.sql.core.segment.dml.LateralViewSegment;
import com.sea.odps.sql.core.segment.dml.WithSegment;
import com.sea.odps.sql.core.segment.dml.combine.CombineSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionsSegment;
import com.sea.odps.sql.core.segment.dml.order.GroupBySegment;
import com.sea.odps.sql.core.segment.dml.order.OrderBySegment;
import com.sea.odps.sql.core.segment.dml.predicate.HavingSegment;
import com.sea.odps.sql.core.segment.dml.predicate.WhereSegment;
import com.sea.odps.sql.core.segment.generic.hint.HintSegment;
import com.sea.odps.sql.core.segment.generic.table.TableSegment;
import com.sea.odps.sql.core.statement.AbstractSQLStatement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SELECT 语句抽象类，表示 SELECT 查询语句。 包含 SELECT 子句的所有组成部分：投影、FROM、WHERE、GROUP BY、HAVING、ORDER BY、LIMIT 等。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SelectStatement extends AbstractSQLStatement implements DMLStatement {

    private ProjectionsSegment projections;

    private TableSegment from;

    private WhereSegment where;

    private GroupBySegment groupBy;

    private HavingSegment having;

    private OrderBySegment orderBy;

    private CombineSegment combine;

    private WithSegment with;

    private HintSegment hint;

    private LateralViewSegment lateralView;

    /**
     * 获取 WHERE 子句片段。
     *
     * @return WHERE 子句片段，如果不存在则返回空
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }

    /**
     * 获取 GROUP BY 子句片段。
     *
     * @return GROUP BY 子句片段，如果不存在则返回空
     */
    public Optional<GroupBySegment> getGroupBy() {
        return Optional.ofNullable(groupBy);
    }

    /**
     * 获取 HAVING 子句片段。
     *
     * @return HAVING 子句片段，如果不存在则返回空
     */
    public Optional<HavingSegment> getHaving() {
        return Optional.ofNullable(having);
    }

    /**
     * 获取 ORDER BY 子句片段。
     *
     * @return ORDER BY 子句片段，如果不存在则返回空
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.ofNullable(orderBy);
    }

    /**
     * 获取集合操作片段（UNION、INTERSECT、EXCEPT）。
     *
     * @return 集合操作片段，如果不存在则返回空
     */
    public Optional<CombineSegment> getCombine() {
        return Optional.ofNullable(combine);
    }

    /**
     * 获取 WITH CTE 子句片段。
     *
     * @return WITH CTE 子句片段，如果不存在则返回空
     */
    public Optional<WithSegment> getWith() {
        return Optional.ofNullable(with);
    }

    /**
     * 获取 HINT 子句片段。
     *
     * @return HINT 子句片段，如果不存在则返回空
     */
    public Optional<HintSegment> getHint() {
        return Optional.ofNullable(hint);
    }

    /**
     * 获取 LATERAL VIEW 子句片段。
     *
     * @return LATERAL VIEW 子句片段，如果不存在则返回空
     */
    public Optional<LateralViewSegment> getLateralView() {
        return Optional.ofNullable(lateralView);
    }
}
