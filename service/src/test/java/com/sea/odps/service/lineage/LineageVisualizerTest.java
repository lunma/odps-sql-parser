package com.sea.odps.service.lineage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sea.odps.service.cli.LineageVisualizer;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;

/** LineageVisualizer 测试用例。 */
public class LineageVisualizerTest {

    private MetadataConnector mockConnector;
    private LineageVisualizer visualizer;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);
        visualizer = new LineageVisualizer(mockConnector);

        // Mock 数据库列表
        DatabaseMeta database = new DatabaseMeta("ods", "owner", "ODS数据库", new HashMap<>());
        when(mockConnector.listDatabases()).thenReturn(Collections.singletonList(database));

        // Mock 表元数据
        List<ColumnMeta> userTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "id", "BIGINT", false, "用户ID", 0, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "name", "STRING", true, "用户名称", 1, false, null, new HashMap<>()),
                        new ColumnMeta("age", "INT", true, "年龄", 2, false, null, new HashMap<>()));
        TableMeta userTable =
                new TableMeta(
                        "ods",
                        "user_table",
                        "MANAGED_TABLE",
                        "用户表",
                        "owner",
                        userTableColumns,
                        new HashMap<>());

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
                                "amount",
                                "DECIMAL",
                                true,
                                "订单金额",
                                2,
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

        when(mockConnector.listTables("ods")).thenReturn(Arrays.asList(userTable, orderTable));

        // Mock getTable 方法（用于精确查询表元数据）
        when(mockConnector.getTable("ods", "user_table")).thenReturn(userTable);
        when(mockConnector.getTable("ods", "order_table")).thenReturn(orderTable);

        // Mock getDatabase 方法
        when(mockConnector.getDatabase("ods")).thenReturn(database);
    }

    @Test
    public void testVisualizeSimpleSelect() throws MetadataException {
        String sql =
                "SELECT t1.id, t1.name, t2.amount "
                        + "FROM ods.user_table t1 "
                        + "LEFT JOIN ods.order_table t2 ON t1.id = t2.user_id;";

        System.out.println("\n=== 测试简单 SELECT 查询 ===");
        visualizer.visualize(sql);
    }

    @Test
    public void testVisualizeAsTree() throws MetadataException {
        String sql =
                "SELECT t1.id, t1.name, t2.amount "
                        + "FROM ods.user_table t1 "
                        + "LEFT JOIN ods.order_table t2 ON t1.id = t2.user_id;";

        System.out.println("\n=== 测试树形血缘关系 ===");
        visualizer.visualizeAsTree(sql);
    }

    @Test
    public void testVisualizeSubquery() throws MetadataException {
        String sql = "SELECT sub.id, sub.name " + "FROM (SELECT id, name FROM ods.user_table) sub;";

        System.out.println("\n=== 测试子查询 ===");
        visualizer.visualize(sql);
    }
}
