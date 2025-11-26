package com.sea.odps.service.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 字段血缘关系树节点模型。
 *
 * <p>用于表示字段血缘关系的树形结构，每个节点包含字段信息和子节点列表。
 */
public class ColumnLineageTreeNode {

    /** 字段名（如 t1.id, t2.name） */
    private final String fieldName;

    /** 字段表达式（如 id, name, nvl(col, '1')） */
    private final String expression;

    /** 字段类型（DIRECT, EXPRESSION 等） */
    private final String category;

    /** 数据类型（如果是最终来源字段） */
    private final String dataType;

    /** 是否为最终来源（直接来自表列，不是中间字段） */
    private final boolean isLeaf;

    /** 子节点列表（如果该字段依赖于其他字段） */
    private final List<ColumnLineageTreeNode> children;

    public ColumnLineageTreeNode(
            String fieldName, String expression, String category, String dataType, boolean isLeaf) {
        this.fieldName = fieldName;
        this.expression = expression;
        this.category = category;
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.children = new ArrayList<>();
    }

    public ColumnLineageTreeNode(
            String fieldName,
            String expression,
            String category,
            String dataType,
            boolean isLeaf,
            List<ColumnLineageTreeNode> children) {
        this.fieldName = fieldName;
        this.expression = expression;
        this.category = category;
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getExpression() {
        return expression;
    }

    public String getCategory() {
        return category;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public List<ColumnLineageTreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(ColumnLineageTreeNode child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnLineageTreeNode that = (ColumnLineageTreeNode) o;
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName);
    }

    @Override
    public String toString() {
        return "ColumnLineageTreeNode{"
                + "fieldName='"
                + fieldName
                + '\''
                + ", expression='"
                + expression
                + '\''
                + ", category='"
                + category
                + '\''
                + ", dataType='"
                + dataType
                + '\''
                + ", isLeaf="
                + isLeaf
                + ", childrenCount="
                + children.size()
                + '}';
    }
}
