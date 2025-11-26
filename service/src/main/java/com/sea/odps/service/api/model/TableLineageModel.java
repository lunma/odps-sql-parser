package com.sea.odps.service.api.model;

import java.util.Objects;

/**
 * 表血缘关系模型。
 *
 * <p>用于表示 SQL 中涉及的表及其元数据信息，便于通过接口提供给其他应用使用。
 */
public class TableLineageModel {

    /** 表的限定名（如 database.table） */
    private final String qualifiedName;

    /** 表别名（SQL 中使用的别名） */
    private final String alias;

    /** 数据库名 */
    private final String database;

    /** 表名 */
    private final String tableName;

    /** 表类型（如 MANAGED_TABLE, VIEW 等） */
    private final String tableType;

    /** 表注释 */
    private final String comment;

    public TableLineageModel(
            String qualifiedName,
            String alias,
            String database,
            String tableName,
            String tableType,
            String comment) {
        this.qualifiedName = qualifiedName;
        this.alias = alias;
        this.database = database;
        this.tableName = tableName;
        this.tableType = tableType;
        this.comment = comment;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getAlias() {
        return alias;
    }

    public String getDatabase() {
        return database;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableLineageModel that = (TableLineageModel) o;
        return Objects.equals(qualifiedName, that.qualifiedName)
                && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName, alias);
    }

    @Override
    public String toString() {
        return "TableLineageModel{"
                + "qualifiedName='"
                + qualifiedName
                + '\''
                + ", alias='"
                + alias
                + '\''
                + ", database='"
                + database
                + '\''
                + ", tableName='"
                + tableName
                + '\''
                + ", tableType='"
                + tableType
                + '\''
                + ", comment='"
                + comment
                + '\''
                + '}';
    }
}
