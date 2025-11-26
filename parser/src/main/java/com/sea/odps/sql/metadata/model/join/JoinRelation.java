package com.sea.odps.sql.metadata.model.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

import lombok.Getter;

/** JOIN 关系信息。 */
@Getter
public class JoinRelation {

    private final TableReference left;

    private final TableReference right;

    private final String joinType;

    private final String condition;

    private final List<JoinConditionPair> columnPairs;

    private final List<ColumnReference> usingColumns;

    public JoinRelation(
            final TableReference left,
            final TableReference right,
            final String joinType,
            final String condition,
            final List<JoinConditionPair> columnPairs,
            final List<ColumnReference> usingColumns) {
        this.left = left;
        this.right = right;
        this.joinType = joinType;
        this.condition = condition;
        this.columnPairs = wrap(columnPairs);
        this.usingColumns = wrapColumns(usingColumns);
    }

    private List<JoinConditionPair> wrap(final List<JoinConditionPair> values) {
        if (null == values || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private List<ColumnReference> wrapColumns(final List<ColumnReference> values) {
        if (null == values || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
