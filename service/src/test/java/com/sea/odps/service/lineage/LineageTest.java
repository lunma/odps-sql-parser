package com.sea.odps.service.lineage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sea.odps.service.api.model.ColumnLineageModel;
import com.sea.odps.service.api.model.LineageResultModel;
import com.sea.odps.service.api.model.TableLineageModel;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;

/** Lineage 单元测试用例。 */
public class LineageTest {

    private MetadataConnector mockConnector;
    private Lineage service;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);
        service = new Lineage(mockConnector);

        // Mock 数据库列表
        DatabaseMeta database = new DatabaseMeta("ods", "owner", "ODS数据库", new HashMap<>());
        when(mockConnector.listDatabases()).thenReturn(Collections.singletonList(database));

        // Mock 表元数据
        List<ColumnMeta> userTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "id", "BIGINT", false, "用户ID", 0, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "name", "STRING", true, "用户名称", 1, false, null, new HashMap<>()));
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
                                "user_id",
                                "BIGINT",
                                false,
                                "用户ID",
                                0,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "amount",
                                "DECIMAL",
                                true,
                                "订单金额",
                                1,
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

        // Mock getTable 方法
        when(mockConnector.getTable("ods", "user_table")).thenReturn(userTable);
        when(mockConnector.getTable("ods", "order_table")).thenReturn(orderTable);

        // Mock getDatabase 方法
        when(mockConnector.getDatabase("ods")).thenReturn(database);
    }

    @Test
    public void testExtractLineage_SimpleSelect() throws MetadataException {
        String sql = "SELECT t1.id, t1.name FROM ods.user_table t1;";

        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);
        assertEquals(sql, result.getSql());

        // 验证表血缘
        List<TableLineageModel> tableLineages = result.getTableLineages();
        assertEquals(1, tableLineages.size());
        assertEquals("ods.user_table", tableLineages.get(0).getQualifiedName());
        assertEquals("t1", tableLineages.get(0).getAlias());

        // 验证字段血缘（只包含最终输出字段）
        List<ColumnLineageModel> columnLineages = result.getColumnLineages();
        assertTrue(columnLineages.size() >= 2);

        // 验证所有字段都是最终输出字段
        for (ColumnLineageModel column : columnLineages) {
            assertTrue(column.isFinalOutput());
        }
    }

    @Test
    public void testExtractLineage_JoinQuery() throws MetadataException {
        String sql =
                "SELECT t1.id, t1.name, t2.amount "
                        + "FROM ods.user_table t1 "
                        + "LEFT JOIN ods.order_table t2 ON t1.id = t2.user_id;";

        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);
        assertEquals(sql, result.getSql());

        // 验证表血缘
        List<TableLineageModel> tableLineages = result.getTableLineages();
        assertEquals(2, tableLineages.size());

        // 验证字段血缘
        List<ColumnLineageModel> columnLineages = result.getColumnLineages();
        assertTrue(columnLineages.size() >= 3);
    }

    @Test
    public void testExtractLineage_ExpressionField() throws MetadataException {
        // 测试表达式字段，验证中间字段被正确过滤
        String sql =
                "SELECT project_name, nvl(table_name, '1') AS tname FROM cicdata_meta_dev.cic_m_column;";

        // Mock cic_m_column 表
        List<ColumnMeta> columnColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "project_name",
                                "STRING",
                                true,
                                "Project名称",
                                0,
                                false,
                                null,
                                new HashMap<>()),
                        new ColumnMeta(
                                "table_name",
                                "STRING",
                                true,
                                "表名",
                                1,
                                false,
                                null,
                                new HashMap<>()));
        TableMeta columnTable =
                new TableMeta(
                        "cicdata_meta_dev",
                        "cic_m_column",
                        "MANAGED_TABLE",
                        "列元数据表",
                        "owner",
                        columnColumns,
                        new HashMap<>());

        when(mockConnector.getTable("cicdata_meta_dev", "cic_m_column")).thenReturn(columnTable);
        when(mockConnector.listTables("cicdata_meta_dev"))
                .thenReturn(Collections.singletonList(columnTable));

        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        // 验证字段血缘：应该只包含最终输出字段
        List<ColumnLineageModel> columnLineages = result.getColumnLineages();

        // 应该包含 project_name 和 tname（最终输出字段）
        // 不应该包含 nvl(table_name, '1')（中间字段）
        boolean hasIntermediate = false;

        for (ColumnLineageModel column : columnLineages) {
            String targetField = column.getTargetField();
            // 检查是否有中间字段（表达式层）出现在最终输出中
            if (targetField.contains("nvl(table_name, '1')") && !targetField.contains("tname")) {
                hasIntermediate = true;
            }
            assertTrue(column.isFinalOutput());
        }

        // 验证中间字段被过滤掉
        assertTrue("中间字段不应该出现在最终输出中", !hasIntermediate);

        // 验证至少有一些字段
        assertTrue("应该有字段血缘", columnLineages.size() > 0);
    }

    @Test
    public void testExtractLineage_Wildcard() throws MetadataException {
        String sql = "SELECT t1.* FROM ods.user_table t1;";

        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        // 验证字段血缘
        List<ColumnLineageModel> columnLineages = result.getColumnLineages();
        assertTrue(columnLineages.size() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractLineage_NullSql() throws MetadataException {
        service.extractLineage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractLineage_EmptySql() throws MetadataException {
        service.extractLineage("");
    }

    @Test(expected = MetadataException.class)
    public void testExtractLineage_InvalidSql() throws MetadataException {
        String sql = "INVALID SQL STATEMENT";
        service.extractLineage(sql);
    }
}
