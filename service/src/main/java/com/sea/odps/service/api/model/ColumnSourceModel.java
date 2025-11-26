package com.sea.odps.service.api.model;

import java.util.Objects;

/**
 * 字段来源模型。
 *
 * <p>用于表示字段血缘关系中的来源信息，可以是表列或中间字段。
 */
public class ColumnSourceModel {

    /** 表的限定名（如 database.table） */
    private final String tableQualifiedName;

    /** 表别名 */
    private final String tableAlias;

    /** 列名或表达式 */
    private final String columnName;

    /** 列数据类型 */
    private final String dataType;

    /** 列注释 */
    private final String comment;

    /** 是否为中间字段（即该来源本身也是一个输出字段） */
    private final boolean isIntermediateField;

    /** 如果是中间字段，对应的字段名 */
    private final String intermediateFieldName;

    public ColumnSourceModel(
            String tableQualifiedName,
            String tableAlias,
            String columnName,
            String dataType,
            String comment) {
        this(tableQualifiedName, tableAlias, columnName, dataType, comment, false, null);
    }

    public ColumnSourceModel(
            String tableQualifiedName,
            String tableAlias,
            String columnName,
            String dataType,
            String comment,
            boolean isIntermediateField,
            String intermediateFieldName) {
        this.tableQualifiedName = tableQualifiedName;
        this.tableAlias = tableAlias;
        this.columnName = columnName;
        this.dataType = dataType;
        this.comment = comment;
        this.isIntermediateField = isIntermediateField;
        this.intermediateFieldName = intermediateFieldName;
    }

    public String getTableQualifiedName() {
        return tableQualifiedName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public String getComment() {
        return comment;
    }

    public boolean isIntermediateField() {
        return isIntermediateField;
    }

    public String getIntermediateFieldName() {
        return intermediateFieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnSourceModel that = (ColumnSourceModel) o;
        return Objects.equals(tableQualifiedName, that.tableQualifiedName)
                && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableQualifiedName, columnName);
    }

    @Override
    public String toString() {
        return "ColumnSourceModel{"
                + "tableQualifiedName='"
                + tableQualifiedName
                + '\''
                + ", tableAlias='"
                + tableAlias
                + '\''
                + ", columnName='"
                + columnName
                + '\''
                + ", dataType='"
                + dataType
                + '\''
                + ", comment='"
                + comment
                + '\''
                + ", isIntermediateField="
                + isIntermediateField
                + ", intermediateFieldName='"
                + intermediateFieldName
                + '\''
                + '}';
    }
}
