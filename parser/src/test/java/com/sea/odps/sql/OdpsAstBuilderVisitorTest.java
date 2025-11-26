package com.sea.odps.sql;

import java.util.Collection;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.sea.odps.sql.autogen.OdpsLexer;
import com.sea.odps.sql.autogen.OdpsParser;
import com.sea.odps.sql.core.enums.CombineType;
import com.sea.odps.sql.core.enums.OrderDirection;
import com.sea.odps.sql.core.segment.dml.combine.CombineSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionsSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;
import com.sea.odps.sql.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowDefinitionSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.core.segment.generic.OwnerSegment;
import com.sea.odps.sql.core.segment.generic.table.JoinTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SimpleTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SubqueryTableSegment;
import com.sea.odps.sql.visitor.OdpsAstBuilderVisitor;
import com.sea.odps.sql.visitor.odps.OdpsSQLSelectStatement;

import junit.framework.TestCase;

/** {@link OdpsAstBuilderVisitor} 测试。 测试 AST 构建访问器的各种功能，包括 SELECT 子句、JOIN、子查询、集合操作、窗口函数等。 */
public class OdpsAstBuilderVisitorTest extends TestCase {

    /**
     * 创建访问器并解析 SQL。
     *
     * @param sql SQL 语句
     * @return OdpsSQLSelectStatement 对象
     */
    private OdpsSQLSelectStatement parseSQL(String sql) {
        OdpsLexer odpsLexer = new OdpsLexer(CharStreams.fromString(sql));
        CommonTokenStream tokens = new CommonTokenStream(odpsLexer);
        OdpsParser parser = new OdpsParser(tokens);
        OdpsAstBuilderVisitor visitor = new OdpsAstBuilderVisitor();
        visitor.setTokenStream(tokens);
        return (OdpsSQLSelectStatement) visitor.visit(parser.script());
    }

    @Test
    public void testSelectWithGroupByAndOrderBy() {
        String sql =
                "select a, count(*) cnt from ods.table_a group by a having count(*) > 0 order by a desc;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull(selectStatement.getGroupBy());
        assertNotNull(selectStatement.getHaving());
        assertNotNull(selectStatement.getOrderBy());
    }

    /** 测试 DISTINCT 关键字。 */
    @Test
    public void testDistinct() {
        String sql = "select distinct id, name from ods.users;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull(selectStatement.getProjections());
        ProjectionsSegment projections = selectStatement.getProjections();
        assertTrue("应该有 DISTINCT 标志", projections.isDistinct());
        assertEquals("应该有 2 个投影项", 2, projections.getProjections().size());
    }

    /** 测试各种 JOIN 类型。 */
    @Test
    public void testJoinTypes() {
        // LEFT JOIN
        String sql1 = "select * from ods.table_a t1 left join ods.table_b t2 on t1.id = t2.id;";
        OdpsSQLSelectStatement stmt1 = parseSQL(sql1);
        assertNotNull(stmt1.getFrom());
        assertTrue("应该是 JOIN 表", stmt1.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join1 = (JoinTableSegment) stmt1.getFrom();
        assertTrue("应该是 LEFT JOIN", join1.getJoinType().toUpperCase().contains("LEFT"));

        // INNER JOIN
        String sql2 = "select * from ods.table_a t1 inner join ods.table_b t2 on t1.id = t2.id;";
        OdpsSQLSelectStatement stmt2 = parseSQL(sql2);
        assertTrue("应该是 JOIN 表", stmt2.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join2 = (JoinTableSegment) stmt2.getFrom();
        assertTrue("应该是 INNER JOIN", join2.getJoinType().toUpperCase().contains("INNER"));

        // RIGHT JOIN
        String sql3 = "select * from ods.table_a t1 right join ods.table_b t2 on t1.id = t2.id;";
        OdpsSQLSelectStatement stmt3 = parseSQL(sql3);
        assertTrue("应该是 JOIN 表", stmt3.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join3 = (JoinTableSegment) stmt3.getFrom();
        assertTrue("应该是 RIGHT JOIN", join3.getJoinType().toUpperCase().contains("RIGHT"));

        // FULL JOIN
        String sql4 = "select * from ods.table_a t1 full join ods.table_b t2 on t1.id = t2.id;";
        OdpsSQLSelectStatement stmt4 = parseSQL(sql4);
        assertTrue("应该是 JOIN 表", stmt4.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join4 = (JoinTableSegment) stmt4.getFrom();
        assertTrue("应该是 FULL JOIN", join4.getJoinType().toUpperCase().contains("FULL"));

        // CROSS JOIN
        String sql5 = "select * from ods.table_a t1 cross join ods.table_b t2;";
        OdpsSQLSelectStatement stmt5 = parseSQL(sql5);
        assertTrue("应该是 JOIN 表", stmt5.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join5 = (JoinTableSegment) stmt5.getFrom();
        assertTrue("应该是 CROSS JOIN", join5.getJoinType().toUpperCase().contains("CROSS"));
    }

    /** 测试 LIMIT 子句（两种格式）。 */
    @Test
    public void testLimitClause() {
        // 格式1: LIMIT offset, rowCount
        String sql1 = "select * from ods.table_a limit 10, 20;";
        OdpsSQLSelectStatement stmt1 = parseSQL(sql1);
        assertNotNull("应该有 LIMIT 子句", stmt1.getLimit());
        LimitSegment limit1 = stmt1.getLimit();
        assertTrue("应该有 offset", limit1.getOffset().isPresent());
        assertTrue("应该有 rowCount", limit1.getRowCount().isPresent());
        assertTrue(
                "offset 应该是数字字面量",
                limit1.getOffset().get() instanceof NumberLiteralPaginationValueSegment);
        assertTrue(
                "rowCount 应该是数字字面量",
                limit1.getRowCount().get() instanceof NumberLiteralPaginationValueSegment);
        assertEquals(
                "offset 值应该是 10",
                10L,
                ((NumberLiteralPaginationValueSegment) limit1.getOffset().get()).getValue());
        assertEquals(
                "rowCount 值应该是 20",
                20L,
                ((NumberLiteralPaginationValueSegment) limit1.getRowCount().get()).getValue());

        // 格式2: LIMIT rowCount [OFFSET offset]
        String sql2 = "select * from ods.table_a limit 20 offset 10;";
        OdpsSQLSelectStatement stmt2 = parseSQL(sql2);
        assertNotNull("应该有 LIMIT 子句", stmt2.getLimit());
        LimitSegment limit2 = stmt2.getLimit();
        assertTrue("应该有 offset", limit2.getOffset().isPresent());
        assertTrue("应该有 rowCount", limit2.getRowCount().isPresent());
        assertEquals(
                "offset 值应该是 10",
                10L,
                ((NumberLiteralPaginationValueSegment) limit2.getOffset().get()).getValue());
        assertEquals(
                "rowCount 值应该是 20",
                20L,
                ((NumberLiteralPaginationValueSegment) limit2.getRowCount().get()).getValue());
    }

    /** 测试 WITH CTE。 */
    @Test
    public void testWithCTE() {
        String sql =
                "with sales as (select user_id, sum(amount) total from ods.orders group by user_id) "
                        + "select s.user_id, s.total from sales s;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 WITH 子句", selectStatement.getWith().isPresent());
        assertNotNull("应该有 CTE 列表", selectStatement.getWith().get().getCommonTableExpressions());
        assertEquals(
                "应该有 1 个 CTE",
                1,
                selectStatement.getWith().get().getCommonTableExpressions().size());
    }

    /** 测试 HINT 子句。 */
    @Test
    public void testHintClause() {
        String sql =
                "select /*+ mapjoin(t1) */ * from ods.table_a t1 join ods.table_b t2 on t1.id = t2.id;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 HINT 子句", selectStatement.getHint().isPresent());
        assertNotNull("应该有 HINT 项列表", selectStatement.getHint().get().getHintItems());
        assertTrue("应该有 HINT 项", selectStatement.getHint().get().getHintItems().size() > 0);
    }

    /** 测试 WINDOW 子句和窗口函数。 */
    @Test
    public void testWindowClause() {
        String sql =
                "select user_id, "
                        + "row_number() over (partition by dept order by salary) as rn, "
                        + "sum(salary) over w1 as total "
                        + "from ods.employees "
                        + "window w1 as (partition by dept order by dt rows between unbounded preceding and current row);";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 WINDOW 子句", selectStatement.getWindow());
        WindowSegment windowSegment = selectStatement.getWindow();
        assertNotNull("应该有窗口定义列表", windowSegment.getWindowDefinitions());
        assertEquals("应该有 1 个窗口定义", 1, windowSegment.getWindowDefinitions().size());

        WindowDefinitionSegment windowDef = windowSegment.getWindowDefinitions().iterator().next();
        assertNotNull("窗口定义应该有名称", windowDef.getWindowName());
        assertEquals("窗口名称应该是 w1", "w1", windowDef.getWindowName().getValue());
        assertNotNull("窗口定义应该有规范", windowDef.getSpecification());
    }

    /** 测试 LATERAL VIEW。 */
    @Test
    public void testLateralView() {
        String sql =
                "select user_id, item from ods.orders " + "lateral view explode(items) t as item;";

        /*   String sql2 = "SELECT * \n" +
        "FROM table1 t1\n" +
        "LEFT JOIN table2 t2 ON t1.id = t2.id\n" +
        "LATERAL VIEW explode(t2.items) t AS item;";*/
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 LATERAL VIEW", selectStatement.getLateralView().isPresent());
        assertNotNull("应该有表别名", selectStatement.getLateralView().get().getTableAlias());
        assertEquals(
                "表别名应该是 t",
                "t",
                selectStatement.getLateralView().get().getTableAlias().getIdentifier().getValue());
        assertNotNull("应该有列别名列表", selectStatement.getLateralView().get().getColumnAliases());
        assertEquals(
                "应该有 1 个列别名", 1, selectStatement.getLateralView().get().getColumnAliases().size());
    }

    /** 测试集合操作（UNION、INTERSECT、EXCEPT）。 */
    @Test
    public void testCombineOperations() {
        // UNION
        String sql1 = "select id, name from ods.table_a union select id, name from ods.table_b;";
        OdpsSQLSelectStatement stmt1 = parseSQL(sql1);
        assertTrue("应该有集合操作", stmt1.getCombine().isPresent());
        CombineSegment combine1 = stmt1.getCombine().get();
        assertEquals("应该是 UNION", CombineType.UNION, combine1.getCombineType());

        // UNION ALL
        String sql2 =
                "select id, name from ods.table_a union all select id, name from ods.table_b;";
        OdpsSQLSelectStatement stmt2 = parseSQL(sql2);
        assertTrue("应该有集合操作", stmt2.getCombine().isPresent());
        CombineSegment combine2 = stmt2.getCombine().get();
        assertEquals("应该是 UNION ALL", CombineType.UNION_ALL, combine2.getCombineType());

        // INTERSECT
        String sql3 =
                "select id, name from ods.table_a intersect select id, name from ods.table_b;";
        OdpsSQLSelectStatement stmt3 = parseSQL(sql3);
        assertTrue("应该有集合操作", stmt3.getCombine().isPresent());
        CombineSegment combine3 = stmt3.getCombine().get();
        assertEquals("应该是 INTERSECT", CombineType.INTERSECT, combine3.getCombineType());

        // EXCEPT
        String sql4 = "select id, name from ods.table_a except select id, name from ods.table_b;";
        OdpsSQLSelectStatement stmt4 = parseSQL(sql4);
        assertTrue("应该有集合操作", stmt4.getCombine().isPresent());
        CombineSegment combine4 = stmt4.getCombine().get();
        assertEquals("应该是 EXCEPT", CombineType.EXCEPT, combine4.getCombineType());
    }

    /** 测试嵌套集合操作。 */
    @Test
    public void testNestedCombineOperations() {
        String sql =
                "(select id from ods.table_a union select id from ods.table_b) "
                        + "intersect select id from ods.table_c;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有集合操作", selectStatement.getCombine().isPresent());
        CombineSegment combine = selectStatement.getCombine().get();
        assertEquals("应该是 INTERSECT", CombineType.INTERSECT, combine.getCombineType());
    }

    /** 测试子查询。 */
    @Test
    public void testSubquery() {
        String sql = "select * from (select id, name from ods.users) t;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该是子查询表", selectStatement.getFrom() instanceof SubqueryTableSegment);
        SubqueryTableSegment subqueryTable = (SubqueryTableSegment) selectStatement.getFrom();
        assertNotNull("应该有子查询", subqueryTable.getSubquery());
        assertTrue("应该有别名", subqueryTable.getAlias().isPresent());
        assertEquals("别名应该是 t", "t", subqueryTable.getAlias().get());
    }

    /** 测试 ORDER BY 子句（包含排序方向和 NULLS 处理）。 */
    @Test
    public void testOrderBy() {
        String sql = "select * from ods.table_a order by id desc nulls first, name asc nulls last;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 ORDER BY 子句", selectStatement.getOrderBy().isPresent());
        Collection<OrderByItemSegment> items = selectStatement.getOrderBy().get().getOrderByItems();
        assertEquals("应该有 2 个排序项", 2, items.size());

        OrderByItemSegment firstItem = items.iterator().next();
        assertEquals("第一个排序项应该是 DESC", OrderDirection.DESC, firstItem.getOrderDirection());
    }

    /** 测试表注释提取。 */
    @Test
    public void testTableComment() {
        String sql = "select * from ods.users t1 -- 用户表;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该是简单表", selectStatement.getFrom() instanceof SimpleTableSegment);
        SimpleTableSegment table = (SimpleTableSegment) selectStatement.getFrom();
        assertNotNull("表应该有注释", table.getComment());
        assertTrue("注释应该包含'用户表'", table.getComment().getText().contains("用户表"));
    }

    /** 测试字段注释提取。 */
    @Test
    public void testFieldComment() {
        String sql = "select id, -- 用户ID\n" + "       name -- 用户名\n" + "from ods.users;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有投影", selectStatement.getProjections());
        Collection<ProjectionSegment> projections =
                selectStatement.getProjections().getProjections();
        assertEquals("应该有 2 个投影项", 2, projections.size());
    }

    /** 测试 WHERE 子句注释。 */
    @Test
    public void testWhereComment() {
        String sql = "select * from ods.users where id > 0  -- WHERE 条件\\n;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 WHERE 子句", selectStatement.getWhere().isPresent());
        assertNotNull("WHERE 子句应该有注释", selectStatement.getWhere().get().getComment());
        assertTrue(
                "注释应该包含'WHERE 条件'",
                selectStatement.getWhere().get().getComment().getText().contains("WHERE 条件"));
    }

    /** 测试 GROUP BY 子句注释。 */
    @Test
    public void testGroupByComment() {
        String sql = "select dept, count(*) from ods.employees group by dept -- 按部门分组;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 GROUP BY 子句", selectStatement.getGroupBy().isPresent());
        assertNotNull("GROUP BY 子句应该有注释", selectStatement.getGroupBy().get().getComment());
        assertTrue(
                "注释应该包含'按部门分组'",
                selectStatement.getGroupBy().get().getComment().getText().contains("按部门分组"));
    }

    /** 测试 HAVING 子句注释。 */
    @Test
    public void testHavingComment() {
        String sql =
                "select dept, count(*) from ods.employees group by dept having count(*) > 10 -- 过滤条件;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 HAVING 子句", selectStatement.getHaving().isPresent());
        assertNotNull("HAVING 子句应该有注释", selectStatement.getHaving().get().getComment());
        assertTrue(
                "注释应该包含'过滤条件'",
                selectStatement.getHaving().get().getComment().getText().contains("过滤条件"));
    }

    /** 测试 ORDER BY 子句注释。 */
    @Test
    public void testOrderByComment() {
        String sql = "select * from ods.users order by id desc -- 按ID降序;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 ORDER BY 子句", selectStatement.getOrderBy().isPresent());
        assertNotNull("ORDER BY 子句应该有注释", selectStatement.getOrderBy().get().getComment());
        assertTrue(
                "注释应该包含'按ID降序'",
                selectStatement.getOrderBy().get().getComment().getText().contains("按ID降序"));
    }

    /** 测试 LIMIT 子句注释。 */
    @Test
    public void testLimitComment() {
        String sql = "select * from ods.users limit 10 -- 限制10条;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 LIMIT 子句", selectStatement.getLimit());
        assertNotNull("LIMIT 子句应该有注释", selectStatement.getLimit().getComment());
        assertTrue(
                "注释应该包含'限制10条'",
                selectStatement.getLimit().getComment().getText().contains("限制10条"));
    }

    /** 测试复杂 SQL（包含多个子句）。 */
    @Test
    public void testComplexSQL() {
        String sql =
                "with sales as ( "
                        + "  select user_id, country, sum(amount) amount "
                        + "  from ods.fact_sales "
                        + "  where dt >= '2024-01-01' "
                        + "  group by user_id, country "
                        + ") "
                        + "select t.country, "
                        + "       sum(t.amount) total_amount "
                        + "from sales t "
                        + "where t.country <> 'CN' "
                        + "group by t.country "
                        + "having sum(t.amount) > 100 "
                        + "order by total_amount desc "
                        + "limit 10;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertTrue("应该有 WITH 子句", selectStatement.getWith().isPresent());
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该有 WHERE 子句", selectStatement.getWhere().isPresent());
        assertTrue("应该有 GROUP BY 子句", selectStatement.getGroupBy().isPresent());
        assertTrue("应该有 HAVING 子句", selectStatement.getHaving().isPresent());
        assertTrue("应该有 ORDER BY 子句", selectStatement.getOrderBy().isPresent());
        assertNotNull("应该有 LIMIT 子句", selectStatement.getLimit());
    }

    /** 测试多表 JOIN。 */
    @Test
    public void testMultipleJoins() {
        String sql =
                "select * from ods.table_a t1 "
                        + "left join ods.table_b t2 on t1.id = t2.id "
                        + "inner join ods.table_c t3 on t2.id = t3.id;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该是 JOIN 表", selectStatement.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join = (JoinTableSegment) selectStatement.getFrom();
        // 检查嵌套的 JOIN
        assertTrue("右侧应该是 JOIN 表", join.getRight() instanceof JoinTableSegment);
    }

    /** 测试 USING 子句。 */
    @Test
    public void testUsingClause() {
        String sql = "select * from ods.table_a t1 left join ods.table_b t2 using (id, name);";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该是 JOIN 表", selectStatement.getFrom() instanceof JoinTableSegment);
        JoinTableSegment join = (JoinTableSegment) selectStatement.getFrom();
        assertNotNull("应该有 USING 列", join.getUsing());
        assertEquals("应该有 2 个 USING 列", 2, join.getUsing().size());
    }

    /** 测试三级表名（数据库.模式.表名）。 */
    @Test
    public void testQualifiedTableName() {
        String sql = "select * from db.schema.table_name t;";
        OdpsSQLSelectStatement selectStatement = parseSQL(sql);
        assertNotNull("应该有 FROM 子句", selectStatement.getFrom());
        assertTrue("应该是简单表", selectStatement.getFrom() instanceof SimpleTableSegment);
        SimpleTableSegment table = (SimpleTableSegment) selectStatement.getFrom();
        assertTrue("应该有所有者", table.getOwner().isPresent());
        // 检查三级命名结构
        Optional<OwnerSegment> schemaOwner = table.getOwner();
        assertTrue("应该有模式所有者", schemaOwner.isPresent());
    }
}
