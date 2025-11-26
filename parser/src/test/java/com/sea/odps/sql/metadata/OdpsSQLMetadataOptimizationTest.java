package com.sea.odps.sql.metadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.TableReference;

import junit.framework.TestCase;

/**
 * {@link OdpsSQLMetadata} 优化后的辅助方法测试。
 *
 * <p>验证以下优化后的方法：
 *
 * <ul>
 *   <li>{@link OdpsSQLMetadata#getFieldsByScope()} - 按作用域分组的字段映射
 *   <li>{@link OdpsSQLMetadata#getSubqueryTableMap()} - 子查询表映射
 *   <li>{@link OdpsSQLMetadata#getOuterQueryTableAliases()} - 外层查询表别名集合
 *   <li>{@link OdpsSQLMetadata#getSubqueryInnerTableAliases(String)} - 指定子查询的内部表别名集合
 *   <li>{@link OdpsSQLMetadata#getSubqueryInnerTablesMap()} - 所有子查询与其内部表的映射
 * </ul>
 */
public class OdpsSQLMetadataOptimizationTest extends TestCase {

    private OdpsSQLMetadataEntrypoint entrypoint = new OdpsSQLMetadataEntrypoint();

    /** 测试 getFieldsByScope() 方法 - 简单子查询场景。 */
    @Test
    public void testGetFieldsByScope_SimpleSubquery() {
        String sql = "SELECT t2.id, t2.name " + "FROM (SELECT id, name FROM ods.user_table) t2;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证 getFieldsByScope() 返回正确的结果
        Map<String, List<FieldMetadata>> fieldsByScope = metadata.getFieldsByScope();
        assertNotNull("fieldsByScope 不应该为 null", fieldsByScope);
        assertTrue("应该有子查询 t2 的字段", fieldsByScope.containsKey("t2"));
        assertEquals("子查询 t2 应该有 2 个字段", 2, fieldsByScope.get("t2").size());

        // 验证字段的 scopeAlias 正确设置
        List<FieldMetadata> t2Fields = fieldsByScope.get("t2");
        for (FieldMetadata field : t2Fields) {
            assertEquals("字段的 scopeAlias 应该是 t2", "t2", field.getScopeAlias());
        }

        // 验证顶层查询的字段不在 fieldsByScope 中（因为 scopeAlias 为 null）
        boolean hasTopLevelField = false;
        for (List<FieldMetadata> fields : fieldsByScope.values()) {
            for (FieldMetadata field : fields) {
                if (field.getScopeAlias() == null) {
                    hasTopLevelField = true;
                    break;
                }
            }
        }
        assertFalse("顶层查询的字段不应该在 fieldsByScope 中", hasTopLevelField);
    }

    /** 测试 getFieldsByScope() 方法 - 多个子查询场景。 */
    @Test
    public void testGetFieldsByScope_MultipleSubqueries() {
        String sql =
                "SELECT t1.id, t2.name "
                        + "FROM (SELECT id FROM ods.user_table) t1 "
                        + "JOIN (SELECT name FROM ods.order_table) t2 ON t1.id = t2.id;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Map<String, List<FieldMetadata>> fieldsByScope = metadata.getFieldsByScope();
        assertTrue("应该有子查询 t1 的字段", fieldsByScope.containsKey("t1"));
        assertTrue("应该有子查询 t2 的字段", fieldsByScope.containsKey("t2"));
        assertEquals("子查询 t1 应该有 1 个字段", 1, fieldsByScope.get("t1").size());
        assertEquals("子查询 t2 应该有 1 个字段", 1, fieldsByScope.get("t2").size());
    }

    /** 测试 getSubqueryTableMap() 方法。 */
    @Test
    public void testGetSubqueryTableMap() {
        String sql =
                "SELECT t1.id, t2.name "
                        + "FROM (SELECT id FROM ods.user_table) t1 "
                        + "JOIN (SELECT name FROM ods.order_table) t2 ON t1.id = t2.id;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证 getSubqueryTableMap() 返回正确的结果
        Map<String, TableReference> subqueryTableMap = metadata.getSubqueryTableMap();
        assertNotNull("subqueryTableMap 不应该为 null", subqueryTableMap);
        assertEquals("应该有 2 个子查询", 2, subqueryTableMap.size());
        assertTrue("应该包含子查询 t1", subqueryTableMap.containsKey("t1"));
        assertTrue("应该包含子查询 t2", subqueryTableMap.containsKey("t2"));

        // 验证子查询表引用正确
        TableReference t1Ref = subqueryTableMap.get("t1");
        assertNotNull("t1 表引用不应该为 null", t1Ref);
        assertTrue("t1 应该是子查询", t1Ref.isSubquery());
        assertEquals("t1 的别名应该是 t1", "t1", t1Ref.getAlias());

        TableReference t2Ref = subqueryTableMap.get("t2");
        assertNotNull("t2 表引用不应该为 null", t2Ref);
        assertTrue("t2 应该是子查询", t2Ref.isSubquery());
        assertEquals("t2 的别名应该是 t2", "t2", t2Ref.getAlias());
    }

    /** 测试 getOuterQueryTableAliases() 方法 - 有 JOIN 的场景。 */
    @Test
    public void testGetOuterQueryTableAliases_WithJoin() {
        String sql =
                "SELECT t1.id, t2.name "
                        + "FROM ods.user_table t1 "
                        + "JOIN ods.order_table t2 ON t1.id = t2.user_id;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证 getOuterQueryTableAliases() 返回正确的结果
        Set<String> outerQueryTableAliases = metadata.getOuterQueryTableAliases();
        assertNotNull("outerQueryTableAliases 不应该为 null", outerQueryTableAliases);
        assertTrue("应该包含 t1", outerQueryTableAliases.contains("t1"));
        assertTrue("应该包含 t2", outerQueryTableAliases.contains("t2"));
        assertEquals("应该有 2 个外层查询表", 2, outerQueryTableAliases.size());
    }

    /** 测试 getOuterQueryTableAliases() 方法 - 无 JOIN 的场景。 */
    @Test
    public void testGetOuterQueryTableAliases_WithoutJoin() {
        String sql = "SELECT id, name FROM ods.user_table t1;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Set<String> outerQueryTableAliases = metadata.getOuterQueryTableAliases();
        assertNotNull("outerQueryTableAliases 不应该为 null", outerQueryTableAliases);
        assertTrue("应该包含 t1", outerQueryTableAliases.contains("t1"));
        assertEquals("应该有 1 个外层查询表", 1, outerQueryTableAliases.size());
    }

    /** 测试 getSubqueryInnerTableAliases() 方法。 */
    @Test
    public void testGetSubqueryInnerTableAliases() {
        String sql = "SELECT t2.id, t2.name " + "FROM (SELECT id, name FROM ods.user_table t1) t2;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证 getSubqueryInnerTableAliases() 返回正确的结果
        Set<String> t2InnerTables = metadata.getSubqueryInnerTableAliases("t2");
        assertNotNull("t2InnerTables 不应该为 null", t2InnerTables);
        assertTrue("应该包含子查询 t2 内部的表 t1", t2InnerTables.contains("t1"));
        assertEquals("子查询 t2 应该有 1 个内部表", 1, t2InnerTables.size());

        // 验证不存在的子查询返回空集合
        Set<String> nonExistentSubquery = metadata.getSubqueryInnerTableAliases("non_existent");
        assertNotNull("不存在的子查询应该返回非 null", nonExistentSubquery);
        assertTrue("不存在的子查询应该返回空集合", nonExistentSubquery.isEmpty());
    }

    /** 测试 getSubqueryInnerTablesMap() 方法。 */
    @Test
    public void testGetSubqueryInnerTablesMap() {
        String sql =
                "SELECT t1.id, t2.name "
                        + "FROM (SELECT id FROM ods.user_table u1) t1 "
                        + "JOIN (SELECT name FROM ods.order_table o1) t2 ON t1.id = t2.id;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证 getSubqueryInnerTablesMap() 返回正确的结果
        Map<String, Set<String>> subqueryInnerTablesMap = metadata.getSubqueryInnerTablesMap();
        assertNotNull("subqueryInnerTablesMap 不应该为 null", subqueryInnerTablesMap);
        assertEquals("应该有 2 个子查询", 2, subqueryInnerTablesMap.size());
        assertTrue("应该包含子查询 t1", subqueryInnerTablesMap.containsKey("t1"));
        assertTrue("应该包含子查询 t2", subqueryInnerTablesMap.containsKey("t2"));

        // 验证子查询 t1 的内部表
        Set<String> t1InnerTables = subqueryInnerTablesMap.get("t1");
        assertNotNull("t1InnerTables 不应该为 null", t1InnerTables);
        assertTrue("应该包含 u1", t1InnerTables.contains("u1"));

        // 验证子查询 t2 的内部表
        Set<String> t2InnerTables = subqueryInnerTablesMap.get("t2");
        assertNotNull("t2InnerTables 不应该为 null", t2InnerTables);
        assertTrue("应该包含 o1", t2InnerTables.contains("o1"));
    }

    /** 测试缓存机制 - 多次调用应该返回相同的对象。 */
    @Test
    public void testCacheMechanism() {
        String sql = "SELECT t2.id " + "FROM (SELECT id FROM ods.user_table t1) t2;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 第一次调用
        Map<String, List<FieldMetadata>> fieldsByScope1 = metadata.getFieldsByScope();
        Map<String, TableReference> subqueryTableMap1 = metadata.getSubqueryTableMap();
        Set<String> outerQueryTableAliases1 = metadata.getOuterQueryTableAliases();
        Map<String, Set<String>> subqueryInnerTablesMap1 = metadata.getSubqueryInnerTablesMap();

        // 第二次调用
        Map<String, List<FieldMetadata>> fieldsByScope2 = metadata.getFieldsByScope();
        Map<String, TableReference> subqueryTableMap2 = metadata.getSubqueryTableMap();
        Set<String> outerQueryTableAliases2 = metadata.getOuterQueryTableAliases();
        Map<String, Set<String>> subqueryInnerTablesMap2 = metadata.getSubqueryInnerTablesMap();

        // 验证返回的是同一个对象（缓存）
        assertTrue("fieldsByScope 应该被缓存", fieldsByScope1 == fieldsByScope2);
        assertTrue("subqueryTableMap 应该被缓存", subqueryTableMap1 == subqueryTableMap2);
        assertTrue(
                "outerQueryTableAliases 应该被缓存", outerQueryTableAliases1 == outerQueryTableAliases2);
        assertTrue(
                "subqueryInnerTablesMap 应该被缓存", subqueryInnerTablesMap1 == subqueryInnerTablesMap2);
    }

    /** 测试复杂场景 - 嵌套子查询。 */
    @Test
    public void testComplexNestedSubquery() {
        String sql =
                "SELECT t3.id, t3.name "
                        + "FROM ("
                        + "  SELECT t2.id, t2.name "
                        + "  FROM (SELECT id, name FROM ods.user_table t1) t2"
                        + ") t3;";
        OdpsSQLMetadataResult result = entrypoint.result(sql);
        assertTrue("SQL 应该解析成功", result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证子查询表映射
        // t3 是外层查询的 FROM 子句中的子查询，应该被识别
        // t2 是 t3 内部的子查询，也应该被识别
        Map<String, TableReference> subqueryTableMap = metadata.getSubqueryTableMap();
        assertTrue("应该包含子查询 t2", subqueryTableMap.containsKey("t2"));
        assertTrue("应该包含子查询 t3", subqueryTableMap.containsKey("t3"));

        // 验证子查询内部表映射
        // t2 内部有普通表 t1，所以 t2 应该出现在 subqueryInnerTablesMap 中
        // t3 内部只有子查询 t2（没有普通表），所以 t3 不会出现在 subqueryInnerTablesMap 中
        // 只有当子查询内部有普通表时，才会调用 addSubqueryInnerTable 来记录
        Map<String, Set<String>> subqueryInnerTablesMap = metadata.getSubqueryInnerTablesMap();
        assertTrue("应该包含子查询 t2", subqueryInnerTablesMap.containsKey("t2"));
        // t3 内部只有子查询 t2，没有普通表，所以 t3 不应该出现在 subqueryInnerTablesMap 中
        assertFalse(
                "t3 内部只有子查询，没有普通表，所以不应该出现在 subqueryInnerTablesMap 中",
                subqueryInnerTablesMap.containsKey("t3"));

        // 验证 t2 的内部表
        Set<String> t2InnerTables = subqueryInnerTablesMap.get("t2");
        assertNotNull("t2 的内部表集合不应该为 null", t2InnerTables);
        assertTrue("t2 应该包含内部表 t1", t2InnerTables.contains("t1"));
        assertEquals("t2 应该只有 1 个内部表", 1, t2InnerTables.size());

        // 验证字段作用域
        // t2 和 t3 都是子查询，它们的字段应该有 scopeAlias
        Map<String, List<FieldMetadata>> fieldsByScope = metadata.getFieldsByScope();
        assertTrue("应该包含子查询 t2 的字段", fieldsByScope.containsKey("t2"));
        assertTrue("应该包含子查询 t3 的字段", fieldsByScope.containsKey("t3"));
    }
}
