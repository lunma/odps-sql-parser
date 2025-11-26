package com.sea.odps.sql.core.segment.generic.table;

import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.expr.subquery.SubquerySegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 子查询表片段，表示 FROM 子句中的子查询表（如 `FROM (SELECT ...) AS t`）。 包含子查询和别名（可选）。 */
@RequiredArgsConstructor
@Getter
public final class SubqueryTableSegment implements TableSegment {

    private final SubquerySegment subquery;

    @Setter private AliasSegment alias;

    @Override
    public Optional<String> getAlias() {
        return alias == null
                ? Optional.empty()
                : Optional.ofNullable(alias.getIdentifier().getValue());
    }

    @Override
    public int getStartIndex() {
        return subquery.getStartIndex();
    }

    @Override
    public int getStopIndex() {
        // 如果有别名，返回别名的结束位置；否则返回子查询的结束位置
        return alias != null ? alias.getStopIndex() : subquery.getStopIndex();
    }
}
