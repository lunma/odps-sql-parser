package com.sea.odps.service.lineage;

import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;

/**
 * 列源信息。
 *
 * <p>表示字段血缘中一个来源列的信息，包括表限定名、表别名、列名和列元数据。
 */
public class ColumnSource {
    private final String tableQualifiedName;
    private final String tableAlias;
    private final String columnName;
    private final ColumnMeta columnMeta;

    public ColumnSource(
            String tableQualifiedName,
            String tableAlias,
            String columnName,
            ColumnMeta columnMeta) {
        this.tableQualifiedName = tableQualifiedName;
        this.tableAlias = tableAlias;
        this.columnName = columnName;
        this.columnMeta = columnMeta;
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

    public ColumnMeta getColumnMeta() {
        return columnMeta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnSource that = (ColumnSource) o;
        return tableQualifiedName.equals(that.tableQualifiedName)
                && columnName.equals(that.columnName);
    }

    @Override
    public int hashCode() {
        return tableQualifiedName.hashCode() * 31 + columnName.hashCode();
    }
}
