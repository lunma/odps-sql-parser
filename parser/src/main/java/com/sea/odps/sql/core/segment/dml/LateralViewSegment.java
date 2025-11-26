package com.sea.odps.sql.core.segment.dml;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;

/** LATERAL VIEW 片段，用于展开数组或 Map 类型的列。 这是 DML（SELECT）语句特有的功能，仅在 SELECT 语句中使用。 */
@Getter
public final class LateralViewSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final ExpressionSegment function;

    private final AliasSegment tableAlias;

    private final Collection<IdentifierValue> columnAliases;

    private final boolean outer;

    @lombok.Setter private CommentSegment comment;

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param function 表值函数表达式
     * @param tableAlias 表别名
     * @param columnAliases 列别名集合
     * @param outer 是否使用 OUTER 关键字
     */
    public LateralViewSegment(
            final int startIndex,
            final int stopIndex,
            final ExpressionSegment function,
            final AliasSegment tableAlias,
            final Collection<IdentifierValue> columnAliases,
            final boolean outer) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.function = function;
        this.tableAlias = tableAlias;
        this.columnAliases =
                new LinkedList<>(columnAliases != null ? columnAliases : new LinkedList<>());
        this.outer = outer;
    }
}
