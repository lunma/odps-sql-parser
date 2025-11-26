package com.sea.odps.service.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 字段血缘关系模型。
 *
 * <p>用于表示 SQL 输出字段及其来源信息，便于通过接口提供给其他应用使用。
 */
public class ColumnLineageModel {

    /** 输出字段名（如 t1.id, t2.name） */
    private final String targetField;

    /** 字段表达式（如 id, name, nvl(col, '1')） */
    private final String expression;

    /** 字段类型（DIRECT, EXPRESSION 等） */
    private final String category;

    /** 字段来源列表 */
    private final List<ColumnSourceModel> sources;

    /** 是否为最终输出字段（false 表示是中间字段） */
    private final boolean isFinalOutput;

    public ColumnLineageModel(
            String targetField,
            String expression,
            String category,
            List<ColumnSourceModel> sources) {
        this(targetField, expression, category, sources, true);
    }

    public ColumnLineageModel(
            String targetField,
            String expression,
            String category,
            List<ColumnSourceModel> sources,
            boolean isFinalOutput) {
        this.targetField = targetField;
        this.expression = expression;
        this.category = category;
        this.sources = sources != null ? new ArrayList<>(sources) : Collections.emptyList();
        this.isFinalOutput = isFinalOutput;
    }

    public String getTargetField() {
        return targetField;
    }

    public String getExpression() {
        return expression;
    }

    public String getCategory() {
        return category;
    }

    public List<ColumnSourceModel> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public boolean isFinalOutput() {
        return isFinalOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnLineageModel that = (ColumnLineageModel) o;
        return Objects.equals(targetField, that.targetField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetField);
    }

    @Override
    public String toString() {
        return "ColumnLineageModel{"
                + "targetField='"
                + targetField
                + '\''
                + ", expression='"
                + expression
                + '\''
                + ", category='"
                + category
                + '\''
                + ", sourcesCount="
                + sources.size()
                + ", isFinalOutput="
                + isFinalOutput
                + '}';
    }
}
