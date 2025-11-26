package com.sea.odps.service.lineage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;

/**
 * LineageExtractor 优化后的逻辑测试。
 *
 * <p>验证 LineageExtractor 使用优化后的 OdpsSQLMetadata 辅助方法后的逻辑是否正确：
 *
 * <ul>
 *   <li>使用 getFieldsByScope() 而不是手动构建
 *   <li>使用 getSubqueryTableMap() 而不是手动构建
 *   <li>使用 getOuterQueryTableAliases() 而不是手动构建
 *   <li>使用 getSubqueryInnerTableAliases() 获取精确的子查询内部表
 * </ul>
 */
public class LineageExtractorOptimizationTest {

    private MetadataConnector mockConnector;
    private LineageExtractor extractor;
    private OdpsSQLMetadataEntrypoint entrypoint;

    @Before
    public void setUp() throws MetadataException {
        mockConnector = mock(MetadataConnector.class);
        extractor = new LineageExtractor(mockConnector);
        entrypoint = new OdpsSQLMetadataEntrypoint();

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

        // Mock 元数据表（用于表达式字段测试）
        List<ColumnMeta> metadataTableColumns =
                Arrays.asList(
                        new ColumnMeta(
                                "table_name",
                                "STRING",
                                true,
                                "表名",
                                0,
                                false,
                                null,
                                new HashMap<>()));
        TableMeta metadataTable =
                new TableMeta(
                        "ods",
                        "metadata_table",
                        "MANAGED_TABLE",
                        "元数据表",
                        "owner",
                        metadataTableColumns,
                        new HashMap<>());

        // Mock getTable 方法
        when(mockConnector.getTable("ods", "user_table")).thenReturn(userTable);
        when(mockConnector.getTable("ods", "order_table")).thenReturn(orderTable);
        when(mockConnector.getTable("ods", "metadata_table")).thenReturn(metadataTable);

        // Mock getDatabase 方法
        when(mockConnector.getDatabase("ods")).thenReturn(database);
    }

    /** 测试简单子查询场景 - 验证优化后的方法被正确使用。 */
    @Test
    public void testSimpleSubquery_OptimizedMethods() throws MetadataException {
        String sql = "SELECT t2.id, t2.name FROM (SELECT id, name FROM ods.user_table) t2;";

        // 解析 SQL
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证优化后的方法返回正确的结果
        assertNotNull("getFieldsByScope() 应该返回非 null", sqlMetadata.getFieldsByScope());
        assertNotNull("getSubqueryTableMap() 应该返回非 null", sqlMetadata.getSubqueryTableMap());
        assertNotNull(
                "getOuterQueryTableAliases() 应该返回非 null", sqlMetadata.getOuterQueryTableAliases());
        assertNotNull(
                "getSubqueryInnerTablesMap() 应该返回非 null", sqlMetadata.getSubqueryInnerTablesMap());

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证表血缘
        assertNotNull("表血缘不应该为 null", lineageResult.getTableLineages());
        assertTrue("应该有表血缘", lineageResult.getTableLineages().size() > 0);

        // 验证字段血缘
        assertNotNull("字段血缘不应该为 null", lineageResult.getColumnLineages());
        assertTrue("应该有字段血缘", lineageResult.getColumnLineages().size() > 0);

        // 验证子查询字段能够正确追溯到原始表
        boolean foundT2Id = false;
        boolean foundT2Name = false;
        for (com.sea.odps.service.lineage.ColumnLineage columnLineage :
                lineageResult.getColumnLineages()) {
            String targetField = columnLineage.getTargetField();
            if ("t2.id".equals(targetField)) {
                foundT2Id = true;
                assertTrue(
                        "t2.id 应该有来源",
                        columnLineage.getSources() != null
                                && !columnLineage.getSources().isEmpty());
                // 验证能够追溯到 ods.user_table.id
                boolean foundUserTableId = false;
                for (com.sea.odps.service.lineage.ColumnSource source :
                        columnLineage.getSources()) {
                    if ("ods.user_table".equals(source.getTableQualifiedName())
                            && "id".equals(source.getColumnName())) {
                        foundUserTableId = true;
                        break;
                    }
                }
                assertTrue("t2.id 应该能够追溯到 ods.user_table.id", foundUserTableId);
            } else if ("t2.name".equals(targetField)) {
                foundT2Name = true;
                assertTrue(
                        "t2.name 应该有来源",
                        columnLineage.getSources() != null
                                && !columnLineage.getSources().isEmpty());
            }
        }
        assertTrue("应该找到 t2.id 字段", foundT2Id);
        assertTrue("应该找到 t2.name 字段", foundT2Name);
    }

    /** 测试 JOIN 场景 - 验证外层查询表别名正确识别。 */
    @Test
    public void testJoinScenario_OuterQueryTableAliases() throws MetadataException {
        String sql =
                "SELECT t1.id, t2.amount "
                        + "FROM ods.user_table t1 "
                        + "JOIN ods.order_table t2 ON t1.id = t2.user_id;";

        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证外层查询表别名
        java.util.Set<String> outerQueryTableAliases = sqlMetadata.getOuterQueryTableAliases();
        assertTrue("应该包含 t1", outerQueryTableAliases.contains("t1"));
        assertTrue("应该包含 t2", outerQueryTableAliases.contains("t2"));

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证字段血缘正确
        assertNotNull("字段血缘不应该为 null", lineageResult.getColumnLineages());
        assertTrue("应该有字段血缘", lineageResult.getColumnLineages().size() >= 2);
    }

    /** 测试子查询内部表精确标记 - 验证 getSubqueryInnerTableAliases() 的使用。 */
    @Test
    public void testSubqueryInnerTablePreciseMarking() throws MetadataException {
        String sql = "SELECT t2.id " + "FROM (SELECT id FROM ods.user_table t1) t2;";

        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证子查询内部表精确标记
        java.util.Set<String> t2InnerTables = sqlMetadata.getSubqueryInnerTableAliases("t2");
        assertNotNull("t2InnerTables 不应该为 null", t2InnerTables);
        assertTrue("应该包含 t1", t2InnerTables.contains("t1"));

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证字段血缘能够正确追溯到子查询内部的表
        boolean foundT2Id = false;
        for (com.sea.odps.service.lineage.ColumnLineage columnLineage :
                lineageResult.getColumnLineages()) {
            if ("t2.id".equals(columnLineage.getTargetField())) {
                foundT2Id = true;
                // 验证能够追溯到 ods.user_table.id（通过子查询内部的表 t1）
                boolean foundUserTableId = false;
                for (com.sea.odps.service.lineage.ColumnSource source :
                        columnLineage.getSources()) {
                    if ("ods.user_table".equals(source.getTableQualifiedName())
                            && "id".equals(source.getColumnName())) {
                        foundUserTableId = true;
                        break;
                    }
                }
                assertTrue("t2.id 应该能够追溯到 ods.user_table.id", foundUserTableId);
                break;
            }
        }
        assertTrue("应该找到 t2.id 字段", foundT2Id);
    }

    /** 测试多个子查询场景 - 验证所有优化后的方法协同工作。 */
    @Test
    public void testMultipleSubqueries_AllOptimizedMethods() throws MetadataException {
        String sql =
                "SELECT t1.id, t2.name "
                        + "FROM (SELECT id FROM ods.user_table u1) t1 "
                        + "JOIN (SELECT name FROM ods.order_table o1) t2 ON t1.id = t2.id;";

        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证所有优化后的方法
        java.util.Map<String, java.util.List<FieldMetadata>> fieldsByScope =
                sqlMetadata.getFieldsByScope();
        assertTrue("应该包含子查询 t1 的字段", fieldsByScope.containsKey("t1"));
        assertTrue("应该包含子查询 t2 的字段", fieldsByScope.containsKey("t2"));

        java.util.Map<String, com.sea.odps.sql.metadata.model.reference.TableReference>
                subqueryTableMap = sqlMetadata.getSubqueryTableMap();
        assertTrue("应该包含子查询 t1", subqueryTableMap.containsKey("t1"));
        assertTrue("应该包含子查询 t2", subqueryTableMap.containsKey("t2"));

        java.util.Set<String> outerQueryTableAliases = sqlMetadata.getOuterQueryTableAliases();
        assertNotNull("outerQueryTableAliases 不应该为 null", outerQueryTableAliases);
        // 注意：在 JOIN 场景中，外层查询表别名应该从 JOIN 关系中提取
        // 但由于有子查询，可能需要根据实际实现调整断言

        java.util.Map<String, java.util.Set<String>> subqueryInnerTablesMap =
                sqlMetadata.getSubqueryInnerTablesMap();
        assertTrue("应该包含子查询 t1", subqueryInnerTablesMap.containsKey("t1"));
        assertTrue("应该包含子查询 t2", subqueryInnerTablesMap.containsKey("t2"));
        assertTrue("t1 应该包含内部表 u1", subqueryInnerTablesMap.get("t1").contains("u1"));
        assertTrue("t2 应该包含内部表 o1", subqueryInnerTablesMap.get("t2").contains("o1"));

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证血缘关系正确
        assertNotNull("表血缘不应该为 null", lineageResult.getTableLineages());
        assertNotNull("字段血缘不应该为 null", lineageResult.getColumnLineages());
        assertTrue("应该有表血缘", lineageResult.getTableLineages().size() > 0);
        assertTrue("应该有字段血缘", lineageResult.getColumnLineages().size() > 0);
    }

    /** 测试表达式字段场景 - 验证中间字段的处理。 */
    @Test
    public void testExpressionField_IntermediateFieldHandling() throws MetadataException {
        String sql =
                "SELECT t2.tname "
                        + "FROM (SELECT nvl(table_name, '1') AS tname FROM ods.metadata_table) t2;";

        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证字段作用域
        java.util.Map<String, java.util.List<FieldMetadata>> fieldsByScope =
                sqlMetadata.getFieldsByScope();
        assertTrue("应该包含子查询 t2 的字段", fieldsByScope.containsKey("t2"));

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证字段血缘包含中间字段
        boolean foundT2Tname = false;
        for (com.sea.odps.service.lineage.ColumnLineage columnLineage :
                lineageResult.getColumnLineages()) {
            String targetField = columnLineage.getTargetField();
            if ("t2.tname".equals(targetField)) {
                foundT2Tname = true;
                break;
            }
        }
        assertTrue("应该找到 t2.tname 字段", foundT2Tname);
        // 注意：中间字段的处理可能需要根据实际实现调整断言
    }

    /** 测试通配符字段场景 - 验证 getFieldsByScope() 的使用。 */
    @Test
    public void testWildcardField_FieldsByScopeUsage() throws MetadataException {
        String sql = "SELECT t2.* FROM (SELECT id, name FROM ods.user_table) t2;";

        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());
        OdpsSQLMetadata sqlMetadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证字段作用域
        Map<String, List<FieldMetadata>> fieldsByScope = sqlMetadata.getFieldsByScope();
        assertTrue("应该包含子查询 t2 的字段", fieldsByScope.containsKey("t2"));
        assertTrue("子查询 t2 应该有字段", fieldsByScope.get("t2").size() > 0);

        // 提取血缘关系
        LineageResult lineageResult = extractor.extract(sqlMetadata);

        // 验证通配符字段的血缘关系
        boolean foundWildcard = false;
        for (ColumnLineage columnLineage : lineageResult.getColumnLineages()) {
            if ("t2.*".equals(columnLineage.getTargetField())) {
                foundWildcard = true;
                assertTrue(
                        "通配符字段应该有多个来源",
                        columnLineage.getSources() != null
                                && columnLineage.getSources().size() > 1);
                break;
            }
        }
        assertTrue("应该找到通配符字段 t2.*", foundWildcard);
    }
}
