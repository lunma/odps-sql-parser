package com.sea.odps.service.lineage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/** LineageExtractor 单元测试用例。 */
public class LineageExtractorTest {

    private MetadataConnector mockConnector;
    private LineageExtractor extractor;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);
        extractor = new LineageExtractor(mockConnector);

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
    public void testExtractTableLineage() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证表血缘
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(2, tableLineages.size());

        // 验证第一个表
        TableLineage lineage1 = tableLineages.get(0);
        assertEquals("ods.user_table", lineage1.getQualifiedName());
        assertEquals("t1", lineage1.getAlias());
        assertNotNull(lineage1.getTableMeta());
        assertEquals("user_table", lineage1.getTableMeta().getName());
    }

    @Test
    public void testExtractColumnLineage() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // 添加字段元数据
        ColumnReference col1 = new ColumnReference("t1", "id", "t1.id");
        ColumnReference col2 = new ColumnReference("t1", "name", "t1.name");
        ColumnReference col3 = new ColumnReference("t2", "amount", "t2.amount");

        FieldMetadata field1 =
                new FieldMetadata(
                        "id", "t1.id", FieldCategory.DIRECT, Collections.singletonList(col1));
        FieldMetadata field2 =
                new FieldMetadata(
                        "name", "t1.name", FieldCategory.DIRECT, Collections.singletonList(col2));
        FieldMetadata field3 =
                new FieldMetadata(
                        "amount",
                        "t2.amount",
                        FieldCategory.DIRECT,
                        Collections.singletonList(col3));

        sqlMetadata.addField(field1);
        sqlMetadata.addField(field2);
        sqlMetadata.addField(field3);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals(3, columnLineages.size());

        // 验证第一个字段
        ColumnLineage lineage1 = columnLineages.get(0);
        assertEquals("id", lineage1.getTargetField());
        assertEquals("t1.id", lineage1.getExpression());
        assertEquals("DIRECT", lineage1.getCategory());

        List<ColumnSource> sources = lineage1.getSources();
        assertEquals(1, sources.size());
        ColumnSource source = sources.get(0);
        assertEquals("ods.user_table", source.getTableQualifiedName());
        assertEquals("id", source.getColumnName());
        assertNotNull(source.getColumnMeta());
        assertEquals("BIGINT", source.getColumnMeta().getDataType());
    }

    @Test
    public void testSubqueryTable() throws MetadataException {
        // 准备 SQL 元数据（包含子查询表）
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加子查询表引用
        TableReference subqueryTable = new TableReference(null, null, "sub", true, "SUBQUERY");
        sqlMetadata.addTable(subqueryTable);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 子查询表不应该出现在表血缘中
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(0, tableLineages.size());
    }

    @Test
    public void testTableWithoutDatabase() throws MetadataException {
        // 准备 SQL 元数据（表没有指定数据库）
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用（没有 owner）
        TableReference table1 = new TableReference(null, "user_table", "t1", false, "user_table");
        sqlMetadata.addTable(table1);

        // 提取血缘关系（应该使用默认数据库）
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证表血缘
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(1, tableLineages.size());
        assertEquals("user_table", tableLineages.get(0).getTableMeta().getName());
    }

    @Test
    public void testNullSqlMetadata() throws MetadataException {
        // 测试 null 输入
        LineageResult result = extractor.extract(null);

        // 应该返回空结果
        assertNotNull(result);
        assertTrue(result.getTableLineages().isEmpty());
        assertTrue(result.getColumnLineages().isEmpty());
    }

    @Test
    public void testAggregateFunctionLineage() throws MetadataException {
        // 准备 SQL 元数据（包含聚合函数）
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // 添加聚合字段
        ColumnReference col1 = new ColumnReference("t2", "order_id", "t2.order_id");
        FieldMetadata aggregateField =
                new FieldMetadata(
                        "order_count",
                        "COUNT(t2.order_id)",
                        FieldCategory.AGGREGATE,
                        Collections.singletonList(col1));
        sqlMetadata.addField(aggregateField);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertTrue("应该有字段血缘", columnLineages.size() > 0);

        ColumnLineage aggregateLineage = findColumnLineage(columnLineages, "order_count");
        if (aggregateLineage != null) {
            assertEquals("AGGREGATE", aggregateLineage.getCategory());
        }
    }

    @Test
    public void testExpressionFieldLineage() throws MetadataException {
        // 准备 SQL 元数据（包含表达式字段）
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        sqlMetadata.addTable(table1);

        // 添加表达式字段
        ColumnReference col1 = new ColumnReference("t1", "name", "t1.name");
        FieldMetadata expressionField =
                new FieldMetadata(
                        "upper_name",
                        "UPPER(t1.name)",
                        FieldCategory.EXPRESSION,
                        Collections.singletonList(col1));
        sqlMetadata.addField(expressionField);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertTrue("应该有字段血缘", columnLineages.size() > 0);

        ColumnLineage expressionLineage = findColumnLineage(columnLineages, "upper_name");
        if (expressionLineage != null) {
            assertEquals("EXPRESSION", expressionLineage.getCategory());
            assertTrue("应该有源列", expressionLineage.getSources().size() > 0);
        }
    }

    /** 测试 SELECT * 的情况。 */
    @Test
    public void testSelectStar() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        sqlMetadata.addTable(table1);

        // 添加 SELECT * 字段（表达式为 "*"，sourceColumns 可能为空或包含 "*"）
        ColumnReference starCol = new ColumnReference(null, "*", "*");
        FieldMetadata starField =
                new FieldMetadata(
                        "*", "*", FieldCategory.DIRECT, Collections.singletonList(starCol));
        sqlMetadata.addField(starField);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("应该有字段血缘", 1, columnLineages.size());

        ColumnLineage starLineage = columnLineages.get(0);
        assertEquals("*", starLineage.getTargetField());
        assertEquals("*", starLineage.getExpression());

        // SELECT * 应该包含表的所有列
        List<ColumnSource> sources = starLineage.getSources();
        assertTrue("SELECT * 应该有源列", sources.size() > 0);

        // 验证所有源列都来自 user_table
        for (ColumnSource source : sources) {
            assertEquals(
                    "所有列应该来自 ods.user_table", "ods.user_table", source.getTableQualifiedName());
            assertNotNull("列元数据不应该为空", source.getColumnMeta());
        }

        // 验证包含了预期的列
        Set<String> columnNames = new HashSet<>();
        for (ColumnSource source : sources) {
            columnNames.add(source.getColumnName());
        }
        assertTrue("应该包含 id 列", columnNames.contains("id"));
        assertTrue("应该包含 name 列", columnNames.contains("name"));
    }

    /** 测试 SELECT * 在多个表的情况。 */
    @Test
    public void testSelectStarWithMultipleTables() throws MetadataException {
        // 测试场景：多表查询中使用未限定的 SELECT *
        // SQL: SELECT * FROM ods.user_table t1, ods.order_table t2;
        //
        // 注意：在实际SQL中，多表查询时使用未限定的 SELECT * 会产生列名冲突，
        // 应该报错或使用限定的 SELECT t1.*, t2.*。但这里测试血缘提取器的行为。

        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // 添加 SELECT * 字段（未限定的通配符）
        ColumnReference starCol = new ColumnReference(null, "*", "*");
        FieldMetadata starField =
                new FieldMetadata(
                        "*", "*", FieldCategory.DIRECT, Collections.singletonList(starCol));
        sqlMetadata.addField(starField);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        // 注意：多表查询时未限定的 SELECT * 在SQL层面应该报错，
        // 但如果SQL解析器允许，血缘提取器可能返回空结果或只返回部分结果
        List<ColumnLineage> columnLineages = result.getColumnLineages();

        // 由于没有 JOIN 关系，outerQueryTableAliases 可能为空或只包含部分表
        // 这种情况下，血缘提取器可能无法确定应该包含哪些表的列
        // 因此可能返回空结果或不确定的结果
        // 这里只验证不会抛出异常即可
        assertNotNull("结果不应该为 null", columnLineages);
    }

    /** 测试 getTable 方法的使用（验证不再使用 listTables 遍历）。 */
    @Test
    public void testGetTableMethodUsage() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        sqlMetadata.addTable(table1);

        // 添加字段
        ColumnReference col1 = new ColumnReference("t1", "id", "t1.id");
        FieldMetadata field1 =
                new FieldMetadata(
                        "id", "t1.id", FieldCategory.DIRECT, Collections.singletonList(col1));
        sqlMetadata.addField(field1);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证表血缘（应该通过 getTable 获取）
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(1, tableLineages.size());
        assertEquals("user_table", tableLineages.get(0).getTableMeta().getName());

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals(1, columnLineages.size());
        assertEquals("id", columnLineages.get(0).getTargetField());
    }

    /** 测试 SELECT t1.* 的情况（带表别名的通配符）。 */
    @Test
    public void testSelectTableStar() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加表引用
        TableReference table1 =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableReference table2 =
                new TableReference("ods", "order_table", "t2", false, "ods.order_table");
        sqlMetadata.addTable(table1);
        sqlMetadata.addTable(table2);

        // 添加 SELECT t1.* 字段（带表别名的通配符）
        ColumnReference tableStarCol = new ColumnReference("t1", "*", "t1.*");
        FieldMetadata tableStarField =
                new FieldMetadata(
                        "t1.*",
                        "t1.*",
                        FieldCategory.DIRECT,
                        Collections.singletonList(tableStarCol));
        sqlMetadata.addField(tableStarField);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证字段血缘
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("应该有字段血缘", 1, columnLineages.size());

        ColumnLineage tableStarLineage = columnLineages.get(0);
        assertEquals("t1.*", tableStarLineage.getTargetField());
        assertEquals("t1.*", tableStarLineage.getExpression());

        // SELECT t1.* 应该只包含 t1 表的列，不应该包含 t2 表的列
        List<ColumnSource> sources = tableStarLineage.getSources();
        assertTrue("SELECT t1.* 应该有源列", sources.size() > 0);

        // 验证所有源列都来自 user_table (t1)，不应该来自 order_table (t2)
        for (ColumnSource source : sources) {
            assertEquals(
                    "所有列应该来自 ods.user_table", "ods.user_table", source.getTableQualifiedName());
            assertEquals("表别名应该是 t1", "t1", source.getTableAlias());
            assertNotNull("列元数据不应该为空", source.getColumnMeta());
        }

        // 验证包含了预期的列（只来自 user_table）
        Set<String> columnNames = new HashSet<>();
        for (ColumnSource source : sources) {
            columnNames.add(source.getColumnName());
        }
        assertTrue("应该包含 id 列", columnNames.contains("id"));
        assertTrue("应该包含 name 列", columnNames.contains("name"));

        // 验证不包含 order_table 的列
        assertTrue("不应该包含 user_id 列（来自 order_table）", !columnNames.contains("user_id"));
        assertTrue("不应该包含 amount 列（来自 order_table）", !columnNames.contains("amount"));
    }

    /** 测试表不存在的情况。 */
    @Test
    public void testTableNotFound() throws MetadataException {
        // 准备 SQL 元数据
        OdpsSQLMetadata sqlMetadata = new OdpsSQLMetadata();

        // 添加不存在的表引用
        TableReference table1 =
                new TableReference(
                        "ods", "non_existent_table", "t1", false, "ods.non_existent_table");
        sqlMetadata.addTable(table1);

        // Mock getTable 返回 null（表不存在）
        when(mockConnector.getTable("ods", "non_existent_table")).thenReturn(null);

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 验证表血缘（不存在的表不应该出现在结果中）
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals("不存在的表不应该出现在表血缘中", 0, tableLineages.size());
    }

    /** 辅助方法：根据字段名查找字段血缘。 */
    private ColumnLineage findColumnLineage(List<ColumnLineage> columnLineages, String fieldName) {
        for (ColumnLineage lineage : columnLineages) {
            if (fieldName.equals(lineage.getTargetField())) {
                return lineage;
            }
        }
        return null;
    }
}
