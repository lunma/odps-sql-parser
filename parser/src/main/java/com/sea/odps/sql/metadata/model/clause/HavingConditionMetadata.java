package com.sea.odps.sql.metadata.model.clause;

import java.util.Collections;
import java.util.List;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;

import lombok.Getter;

/** HAVING 条件元数据。 */
@Getter
public final class HavingConditionMetadata {

    /** 条件表达式文本。 */
    private final String expression;

    /** 条件中涉及的列引用列表。 */
    private final List<ColumnReference> columns;

    /**
     * 构造函数。
     *
     * @param expression 条件表达式文本
     * @param columns 列引用列表
     */
    public HavingConditionMetadata(final String expression, final List<ColumnReference> columns) {
        this.expression = expression;
        this.columns =
                columns != null ? Collections.unmodifiableList(columns) : Collections.emptyList();
    }
}
