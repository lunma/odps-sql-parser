package com.sea.odps.service.lineage;

import static org.junit.Assert.assertFalse;
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
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;

/** 中间字段识别逻辑的测试用例。 */
public class IntermediateFieldTest {

    private MetadataConnector mockConnector;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);

        // Mock 数据库列表
        DatabaseMeta database =
                new DatabaseMeta("cicdata_meta_dev", "owner", "元数据数据库", new HashMap<>());
        when(mockConnector.listDatabases()).thenReturn(Collections.singletonList(database));

        // Mock 表元数据
        List<ColumnMeta> columnTableColumns =
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
                        columnTableColumns,
                        new HashMap<>());

        when(mockConnector.listTables("cicdata_meta_dev"))
                .thenReturn(Collections.singletonList(columnTable));
        when(mockConnector.getTable("cicdata_meta_dev", "cic_m_column")).thenReturn(columnTable);
        when(mockConnector.getDatabase("cicdata_meta_dev")).thenReturn(database);
    }

    @Test
    public void testExpressionField_IntermediateFieldMarking() throws MetadataException {
        // 测试表达式字段场景：nvl(table_name, '1') AS tname
        // 应该创建两层血缘：
        // 1. t2.nvl(table_name, '1') - 中间字段（isFinalOutput=false）
        // 2. t2.tname - 最终输出字段（isFinalOutput=true）

        String sql =
                "SELECT project_name, nvl(table_name, '1') AS tname "
                        + "FROM cicdata_meta_dev.cic_m_column;";

        // 使用 Lineage 提取（它会调用 LineageExtractor）
        Lineage service = new Lineage(mockConnector);
        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        List<ColumnLineageModel> columnLineages = result.getColumnLineages();

        // 验证：应该只包含最终输出字段
        boolean hasProjectName = false;
        boolean hasTname = false;
        boolean hasIntermediate = false;

        for (ColumnLineageModel column : columnLineages) {
            String targetField = column.getTargetField();

            if (targetField.contains("project_name") && !targetField.contains("nvl")) {
                hasProjectName = true;
            }
            if (targetField.contains("tname")) {
                hasTname = true;
            }
            // 检查是否有中间字段（表达式层）出现在最终输出中
            if (targetField.contains("nvl(table_name, '1')") && !targetField.contains("tname")) {
                hasIntermediate = true;
            }

            // 所有字段都应该是最终输出字段
            assertTrue("字段 " + targetField + " 应该是最终输出字段", column.isFinalOutput());
        }

        // 验证中间字段被正确过滤
        assertFalse("中间字段 t2.nvl(table_name, '1') 不应该出现在最终输出中", hasIntermediate);

        // 验证最终输出字段存在
        assertTrue("应该包含 project_name 字段", hasProjectName);
        assertTrue("应该包含 tname 字段", hasTname);
    }

    @Test
    public void testDirectField_NoIntermediateField() throws MetadataException {
        // 测试直接字段场景：没有中间字段
        String sql = "SELECT project_name FROM cicdata_meta_dev.cic_m_column;";

        Lineage service = new Lineage(mockConnector);
        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        List<ColumnLineageModel> columnLineages = result.getColumnLineages();

        // 验证：所有字段都应该是最终输出字段
        for (ColumnLineageModel column : columnLineages) {
            assertTrue(column.isFinalOutput());
        }
    }

    @Test
    public void testSubqueryField_IntermediateFieldMarking() throws MetadataException {
        // 测试子查询字段场景
        // Mock 另一个表用于子查询
        List<ColumnMeta> tableTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "id", "BIGINT", false, "ID", 0, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "name", "STRING", true, "名称", 1, false, null, new HashMap<>()));
        TableMeta tableTable =
                new TableMeta(
                        "ods",
                        "user_table",
                        "MANAGED_TABLE",
                        "用户表",
                        "owner",
                        tableTableColumns,
                        new HashMap<>());

        DatabaseMeta odsDatabase = new DatabaseMeta("ods", "owner", "ODS数据库", new HashMap<>());
        when(mockConnector.listDatabases())
                .thenReturn(
                        Arrays.asList(
                                new DatabaseMeta(
                                        "cicdata_meta_dev", "owner", "元数据数据库", new HashMap<>()),
                                odsDatabase));
        when(mockConnector.getTable("ods", "user_table")).thenReturn(tableTable);
        when(mockConnector.getDatabase("ods")).thenReturn(odsDatabase);
        when(mockConnector.listTables("ods")).thenReturn(Collections.singletonList(tableTable));

        String sql = "SELECT sub.id FROM (SELECT id FROM ods.user_table) sub;";

        Lineage service = new Lineage(mockConnector);
        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        List<ColumnLineageModel> columnLineages = result.getColumnLineages();

        // 验证：所有字段都应该是最终输出字段
        for (ColumnLineageModel column : columnLineages) {
            assertTrue(column.isFinalOutput());
        }
    }

    @Test
    public void testComplexQuery_WithExpressionAndDirectFields() throws MetadataException {
        // 测试复杂查询：包含表达式字段和直接字段
        String sql =
                "SELECT t1.*, t2.* "
                        + "FROM cicdata_meta_dev.cic_m_table t1 "
                        + "JOIN ("
                        + "  SELECT project_name, nvl(table_name, '1') AS tname "
                        + "  FROM cicdata_meta_dev.cic_m_column"
                        + ") t2 "
                        + "ON t1.project_name = t2.project_name;";

        // Mock cic_m_table
        List<ColumnMeta> tableTableColumns =
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
                                "name", "STRING", true, "表名", 1, false, null, new HashMap<>()));
        TableMeta tableTable =
                new TableMeta(
                        "cicdata_meta_dev",
                        "cic_m_table",
                        "MANAGED_TABLE",
                        "表元数据表",
                        "owner",
                        tableTableColumns,
                        new HashMap<>());

        // 获取已存在的 cic_m_column 表（从 setUp 中设置的）
        List<ColumnMeta> columnTableColumns =
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
                        columnTableColumns,
                        new HashMap<>());

        when(mockConnector.getTable("cicdata_meta_dev", "cic_m_table")).thenReturn(tableTable);
        when(mockConnector.getTable("cicdata_meta_dev", "cic_m_column")).thenReturn(columnTable);
        when(mockConnector.listTables("cicdata_meta_dev"))
                .thenReturn(Arrays.asList(tableTable, columnTable));

        Lineage service = new Lineage(mockConnector);
        LineageResultModel result = service.extractLineage(sql);

        assertNotNull(result);

        List<ColumnLineageModel> columnLineages = result.getColumnLineages();

        // 验证：所有字段都应该是最终输出字段
        // 中间字段 t2.nvl(table_name, '1') 不应该出现在最终输出中
        for (ColumnLineageModel column : columnLineages) {
            assertTrue(column.isFinalOutput());

            // 验证中间字段被过滤
            String targetField = column.getTargetField();
            assertFalse(
                    "中间字段不应该出现在最终输出中: " + targetField,
                    targetField.contains("nvl(table_name, '1')") && !targetField.contains("tname"));
        }
    }
}
