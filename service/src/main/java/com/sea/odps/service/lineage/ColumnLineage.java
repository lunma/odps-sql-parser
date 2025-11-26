package com.sea.odps.service.lineage;

import java.util.List;

/**
 * 字段血缘关系。
 *
 * <p>表示 SQL 中字段的来源信息，包括字段表达式、类别和来源列。
 */
public class ColumnLineage {
    private final String targetField;
    private final String expression;
    private final String category;
    private final List<ColumnSource> sources;
    private final boolean isFinalOutput;

    public ColumnLineage(
            String targetField, String expression, String category, List<ColumnSource> sources) {
        this(targetField, expression, category, sources, true);
    }

    public ColumnLineage(
            String targetField,
            String expression,
            String category,
            List<ColumnSource> sources,
            boolean isFinalOutput) {
        this.targetField = targetField;
        this.expression = expression;
        this.category = category;
        this.sources = sources;
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

    public List<ColumnSource> getSources() {
        return sources;
    }

    /**
     * 是否为最终输出字段。
     *
     * <p>最终输出字段是指 SQL 的最终输出字段，不是被其他字段引用作为来源的中间字段。
     *
     * @return true 表示是最终输出字段，false 表示是中间字段
     */
    public boolean isFinalOutput() {
        return isFinalOutput;
    }
}
