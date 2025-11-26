package com.sea.odps.service.connector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Mock 元数据连接器，用于演示和测试。
 *
 * <p>提供一些示例表结构，方便在没有真实元数据源的情况下演示血缘关系分析功能。
 */
public class MockMetadataConnector implements MetadataConnector {

    @Override
    public String id() {
        return "mock";
    }

    @Override
    public CatalogType catalogType() {
        return CatalogType.EXCEL;
    }

    @Override
    public List<DatabaseMeta> listDatabases() throws MetadataException {
        DatabaseMeta database = new DatabaseMeta("ods", "owner", "ODS数据库", new HashMap<>());
        return Collections.singletonList(database);
    }

    @Override
    public List<TableMeta> listTables(String databaseName) throws MetadataException {
        if (!"ods".equals(databaseName)) {
            return Collections.emptyList();
        }

        // 用户表
        List<ColumnMeta> userTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "id", "BIGINT", false, "用户ID", 0, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "name", "STRING", true, "用户名称", 1, false, null, new HashMap<>()),
                        new ColumnMeta("age", "INT", true, "年龄", 2, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "email", "STRING", true, "邮箱", 3, false, null, new HashMap<>()));
        TableMeta userTable =
                new TableMeta(
                        "ods",
                        "user_table",
                        "MANAGED_TABLE",
                        "用户表",
                        "owner",
                        userTableColumns,
                        new HashMap<>());

        // 订单表
        List<ColumnMeta> orderTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "order_id",
                                "BIGINT",
                                false,
                                "订单ID",
                                0,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "user_id",
                                "BIGINT",
                                false,
                                "用户ID",
                                1,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "amount", "DECIMAL", true, "订单金额", 2, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "order_date",
                                "DATE",
                                true,
                                "订单日期",
                                3,
                                false,
                                null,
                                new HashMap<>()));
        TableMeta orderTable =
                new TableMeta(
                        "ods",
                        "order_table",
                        "MANAGED_TABLE",
                        "订单表",
                        "owner",
                        orderTableColumns,
                        new HashMap<>());

        // 商品表
        List<ColumnMeta> productTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "product_id",
                                "BIGINT",
                                false,
                                "商品ID",
                                0,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "product_name",
                                "STRING",
                                true,
                                "商品名称",
                                1,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "price", "DECIMAL", true, "价格", 2, false, null, new HashMap<>()));
        TableMeta productTable =
                new TableMeta(
                        "ods",
                        "product_table",
                        "MANAGED_TABLE",
                        "商品表",
                        "owner",
                        productTableColumns,
                        new HashMap<>());

        return Arrays.asList(userTable, orderTable, productTable);
    }

    @Override
    public DatabaseMeta getDatabase(String databaseName) throws MetadataException {
        if (databaseName == null || databaseName.isEmpty()) {
            return null;
        }
        List<DatabaseMeta> databases = listDatabases();
        for (DatabaseMeta db : databases) {
            if (databaseName.equals(db.getName())) {
                return db;
            }
        }
        return null;
    }

    @Override
    public TableMeta getTable(String databaseName, String tableName) throws MetadataException {
        if (databaseName == null
                || databaseName.isEmpty()
                || tableName == null
                || tableName.isEmpty()) {
            return null;
        }
        List<TableMeta> tables = listTables(databaseName);
        for (TableMeta table : tables) {
            if (tableName.equals(table.getName())) {
                return table;
            }
        }
        return null;
    }
}
