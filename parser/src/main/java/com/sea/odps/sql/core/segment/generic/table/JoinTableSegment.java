package com.sea.odps.sql.core.segment.generic.table;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.column.ColumnSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;
import lombok.Setter;

/**
 * JOIN 表片段，表示 SQL 中的 JOIN 操作（如 INNER JOIN、LEFT JOIN 等）。 包含左表、右表、JOIN 类型、连接条件（ON 或 USING）和别名（可选）。
 */
@Getter
@Setter
public final class JoinTableSegment implements TableSegment {

    private int startIndex;

    private int stopIndex;

    private AliasSegment alias;

    private TableSegment left;

    private boolean natural;

    private String joinType;

    private TableSegment right;

    private ExpressionSegment condition;

    private List<ColumnSegment> using = Collections.emptyList();

    @Override
    public Optional<String> getAlias() {
        return null == alias
                ? Optional.empty()
                : Optional.ofNullable(alias.getIdentifier().getValue());
    }
}
