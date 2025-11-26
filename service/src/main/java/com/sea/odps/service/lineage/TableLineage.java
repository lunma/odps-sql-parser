package com.sea.odps.service.lineage;

import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/**
 * 表血缘关系。
 *
 * <p>表示 SQL 中表引用与元数据服务中表元数据的关联关系。
 */
public class TableLineage {
    private final TableReference tableRef;
    private final TableMeta tableMeta;
    private final String qualifiedName;
    private final String alias;

    public TableLineage(
            TableReference tableRef, TableMeta tableMeta, String qualifiedName, String alias) {
        this.tableRef = tableRef;
        this.tableMeta = tableMeta;
        this.qualifiedName = qualifiedName;
        this.alias = alias;
    }

    public TableReference getTableRef() {
        return tableRef;
    }

    public TableMeta getTableMeta() {
        return tableMeta;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getAlias() {
        return alias;
    }
}
