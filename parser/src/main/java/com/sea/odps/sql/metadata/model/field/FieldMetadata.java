package com.sea.odps.sql.metadata.model.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;

import lombok.Getter;

/** 字段元信息。 */
@Getter
public class FieldMetadata {

    private final String alias;

    private final String expression;

    private final FieldCategory category;

    private final List<ColumnReference> sourceColumns;

    /** 字段所属的查询作用域（如子查询别名）。顶层查询为 null。 */
    private final String scopeAlias;

    public FieldMetadata(
            final String alias,
            final String expression,
            final FieldCategory category,
            final List<ColumnReference> sourceColumns) {
        this(alias, expression, category, sourceColumns, null);
    }

    public FieldMetadata(
            final String alias,
            final String expression,
            final FieldCategory category,
            final List<ColumnReference> sourceColumns,
            final String scopeAlias) {
        this.alias = alias;
        this.expression = expression;
        this.category = null == category ? FieldCategory.EXPRESSION : category;
        List<ColumnReference> columns = new ArrayList<>();
        if (null != sourceColumns) {
            for (ColumnReference each : sourceColumns) {
                if (null != each) {
                    columns.add(each);
                }
            }
        }
        this.sourceColumns = Collections.unmodifiableList(columns);
        this.scopeAlias = scopeAlias;
    }

    public Optional<String> getAliasOptional() {
        return Optional.ofNullable(alias).filter(s -> !s.isEmpty());
    }

    public String getScopeAlias() {
        return scopeAlias;
    }

    @Override
    public String toString() {
        return "FieldMetadata{"
                + "alias='"
                + alias
                + '\''
                + ", expression='"
                + expression
                + '\''
                + ", category="
                + category
                + ", sourceColumns="
                + sourceColumns
                + ", scopeAlias='"
                + scopeAlias
                + '\''
                + '}';
    }
}
