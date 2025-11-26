package com.sea.odps.service.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.service.lineage.LineageExtractor;
import com.sea.odps.service.lineage.LineageResult;
import com.sea.odps.service.lineage.TableLineage;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/** 列名冲突检测器测试用例。 */
public class ColumnConflictCheckerTest {

    private MetadataConnector mockConnector;
    private ColumnConflictChecker checker;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);
        checker = new ColumnConflictChecker();

        // Mock 数据库列表
        DatabaseMeta database = new DatabaseMeta("ods", "owner", "ODS数据库", new HashMap<>());
        when(mockConnector.listDatabases()).thenReturn(Collections.singletonList(database));
        when(mockConnector.getDatabase("ods")).thenReturn(database);

        // Mock 表1：user_table
        List<ColumnMeta> table1Columns =
                Arrays.asList(
                        new ColumnMeta(
                                "id", "BIGINT", false, "ID", 0, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "name", "STRING", true, "名称", 1, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "project_name",
                                "STRING",
                                true,
                                "项目名",
                                2,
                                false,
                                null,
                                new HashMap<>()));
        TableMeta table1 =
                new TableMeta(
                        "ods",
                        "user_table",
                        "MANAGED_TABLE",
                        "用户表",
                        "owner",
                        table1Columns,
                        new HashMap<>());

        // Mock 表2：order_table（有相同的列名 name 和 project_name）
        List<ColumnMeta> table2Columns =
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
                                "name", "STRING", true, "名称", 1, false, null, new HashMap<>()),
                        new ColumnMeta(
                                "project_name",
                                "STRING",
                                true,
                                "项目名",
                                2,
                                false,
                                null,
                                new HashMap<>()));
        TableMeta table2 =
                new TableMeta(
                        "ods",
                        "order_table",
                        "MANAGED_TABLE",
                        "订单表",
                        "owner",
                        table2Columns,
                        new HashMap<>());

        when(mockConnector.getTable("ods", "user_table")).thenReturn(table1);
        when(mockConnector.getTable("ods", "order_table")).thenReturn(table2);
    }

    @Test
    public void testNoConflictWithSingleTable() throws MetadataException {
        // 单个表，不应该有冲突
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        sqlMetadata.addTable(table1);

        ColumnReference starCol = new ColumnReference(null, "*", "*");
        FieldMetadata starField =
                new FieldMetadata(
                        "*", "*", FieldCategory.DIRECT, Collections.singletonList(starCol));
        sqlMetadata.addField(starField);

        // 提取表血缘
        LineageExtractor extractor = new LineageExtractor(mockConnector);
        LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();

        // 检测冲突
        List<ValidationError> errors = checker.checkWildcardConflict(sqlMetadata, tableLineages);

        // 应该没有错误
        assertTrue("单个表不应该有冲突", errors.isEmpty());
    }

    @Test
    public void testConflictWithMultipleTables() throws MetadataException {
        // 多个表 JOIN，有列名冲突
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        ColumnReference starCol = new ColumnReference(null, "*", "*");
        FieldMetadata starField =
                new FieldMetadata(
                        "*", "*", FieldCategory.DIRECT, Collections.singletonList(starCol));
        sqlMetadata.addField(starField);

        // 提取表血缘
        LineageExtractor extractor = new LineageExtractor(mockConnector);
        LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();

        // 检测冲突
        List<ValidationError> errors = checker.checkWildcardConflict(sqlMetadata, tableLineages);

        // 应该有错误
        assertFalse("多个表有列名冲突时应该有错误", errors.isEmpty());
        assertEquals("应该有 1 个错误", 1, errors.size());

        ValidationError error = errors.get(0);
        assertEquals(
                "错误类型应该是 COLUMN_CONFLICT", ValidationErrorType.COLUMN_CONFLICT, error.getType());
        assertTrue(
                "错误信息应该包含冲突的列名",
                error.getMessage().contains("name") || error.getMessage().contains("project_name"));
    }

    @Test
    public void testNoConflictWithTableAlias() throws MetadataException {
        // 使用表别名，不应该有冲突
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // SELECT t1.* 明确指定了表别名
        ColumnReference tableStarCol = new ColumnReference("t1", "*", "t1.*");
        FieldMetadata tableStarField =
                new FieldMetadata(
                        "t1.*",
                        "t1.*",
                        FieldCategory.DIRECT,
                        Collections.singletonList(tableStarCol));
        sqlMetadata.addField(tableStarField);

        // 提取表血缘
        LineageExtractor extractor = new LineageExtractor(mockConnector);
        LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();

        // 检测冲突
        List<ValidationError> errors = checker.checkWildcardConflict(sqlMetadata, tableLineages);

        // 应该没有错误（因为明确指定了表别名）
        assertTrue("明确指定表别名时不应该有冲突", errors.isEmpty());
    }

    @Test
    public void testNoConflictWithExplicitColumns() throws MetadataException {
        // 明确指定列，不应该有冲突
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // SELECT t1.name, t2.name 明确指定了列
        ColumnReference col1 = new ColumnReference("t1", "name", "t1.name");
        ColumnReference col2 = new ColumnReference("t2", "name", "t2.name");
        FieldMetadata field1 =
                new FieldMetadata(
                        "name1", "t1.name", FieldCategory.DIRECT, Collections.singletonList(col1));
        FieldMetadata field2 =
                new FieldMetadata(
                        "name2", "t2.name", FieldCategory.DIRECT, Collections.singletonList(col2));
        sqlMetadata.addField(field1);
        sqlMetadata.addField(field2);

        // 提取表血缘
        LineageExtractor extractor = new LineageExtractor(mockConnector);
        LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();

        // 检测冲突
        List<ValidationError> errors = checker.checkWildcardConflict(sqlMetadata, tableLineages);

        // 应该没有错误（因为没有使用通配符）
        assertTrue("明确指定列时不应该有冲突", errors.isEmpty());
    }
}
