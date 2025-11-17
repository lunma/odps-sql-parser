package com.sea.odps.sql.validation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.sea.odps.sql.metadata.model.clause.LimitMetadata;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.join.JoinRelation;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;
import com.sea.odps.sql.metadata.model.window.WindowFunctionMetadata;
import com.sea.odps.sql.metadata.model.window.WindowMetadata;

import junit.framework.TestCase;

/** {@link OdpsSQLValidator} 测试。 */
public class OdpsSQLValidatorTest extends TestCase {

  @Test
  public void testValidateSelect() {
    String sql =
        "select t1.id, t2.name from ods.table_a t1 left join ods.table_b t2 on t1.id = t2.id;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    Collection<TableReference> tables = metadata.getTables();
    assertEquals(2, tables.size());

    Optional<TableReference> tableA = metadata.findByAlias("t1");
    Optional<TableReference> tableB = metadata.findByAlias("t2");
    assertTrue(tableA.isPresent());
    assertTrue(tableB.isPresent());

    Collection<JoinRelation> joins = metadata.getJoins();
    assertEquals(1, joins.size());
    JoinRelation joinRelation = joins.iterator().next();
    assertEquals("LEFTJOIN", joinRelation.getJoinType().replace(" ", "").toUpperCase());
    assertEquals(1, joinRelation.getColumnPairs().size());

    Collection<FieldMetadata> fields = metadata.getFields();
    assertEquals(2, fields.size());

    FieldMetadata idField =
        fields.stream()
            .filter(each -> "t1.id".equals(each.getExpression()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing t1.id field metadata"));
    List<ColumnReference> idSources = idField.getSourceColumns();
    assertEquals(1, idSources.size());
    ColumnReference idColumn = idSources.get(0);
    assertEquals("t1", idColumn.getOwner());
    assertEquals("id", idColumn.getName());

    FieldMetadata nameField =
        fields.stream()
            .filter(each -> "t2.name".equals(each.getExpression()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing t2.name field metadata"));
    List<ColumnReference> nameSources = nameField.getSourceColumns();
    assertEquals(1, nameSources.size());
    ColumnReference nameColumn = nameSources.get(0);
    assertEquals("t2", nameColumn.getOwner());
    assertEquals("name", nameColumn.getName());
  }

  @Test
  public void testValidateSelectSummaryMessage() {
    String sql =
        "select t1.id, t2.name from ods.table_a t1 left join ods.table_b t2 on t1.id = t2.id;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    String summary = result.formatSummary();
    System.out.println(summary);
    assertTrue(summary.contains("表"));
    assertTrue(summary.contains("ods.table_a"));
    assertTrue(summary.contains("字段"));
    assertTrue(summary.contains("t1.id"));
    assertTrue(summary.contains("来源"));
  }

  @Test
  public void testValidateSelectWithSubqueryAndFunctions() {
    String sql =
        "select t.country, sum(t.amount) total_amount, max(coalesce(s.score, 0)) max_score "
            + "from (select user_id, country, sum(amount) amount from ods.fact_sales where dt >= '2024-01-01' group by user_id, country) t "
            + "left join (select user_id, max(score) score from ods.user_score group by user_id) s on t.user_id = s.user_id "
            + "where t.country <> 'CN' group by t.country having sum(t.amount) > 100 order by total_amount desc;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    assertEquals(2, metadata.getTables().size());
    assertTrue(metadata.findByAlias("t").isPresent());
    assertTrue(metadata.findByAlias("s").isPresent());

    assertEquals(1, metadata.getJoins().size());
    JoinRelation joinRelation = metadata.getJoins().iterator().next();
    assertEquals("LEFTJOIN", joinRelation.getJoinType().replace(" ", "").toUpperCase());

    Collection<FieldMetadata> fields = metadata.getFields();
    assertEquals(3, fields.size());

    FieldMetadata totalAmount =
        fields.stream()
            .filter(
                each ->
                    each.getAliasOptional()
                        .map(alias -> "total_amount".equalsIgnoreCase(alias))
                        .orElse(false))
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing total_amount field"));
    assertTrue(totalAmount.getExpression().toLowerCase().contains("sum"));
    assertEquals(FieldCategory.AGGREGATE, totalAmount.getCategory());

    FieldMetadata maxScore =
        fields.stream()
            .filter(
                each ->
                    each.getAliasOptional()
                        .map(alias -> "max_score".equalsIgnoreCase(alias))
                        .orElse(false))
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing max_score field"));
    assertEquals(FieldCategory.AGGREGATE, maxScore.getCategory());

    String summary = result.formatSummary();
    System.out.println(summary);
    assertTrue(summary.contains("子查询"));
    assertTrue(summary.contains("total_amount"));
    assertTrue(summary.contains("max_score"));
  }

  @Test
  public void testLimitWithExpression() {
    // 测试 LIMIT 表达式支持（如 LIMIT 10 + 5）
    String sql = "select id, name from ods.user_table limit 10 + 5;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    // 表达式类型的 LIMIT 值在元数据中可能为 null（因为无法直接计算表达式值）
    // 但解析应该成功
    Optional<LimitMetadata> limit = metadata.getLimit();
    // 表达式类型的 LIMIT 值在元数据中为 null，但解析应该成功
    System.out.println("LIMIT 元数据: " + limit);
  }

  @Test
  public void testLimitWithSimpleNumber() {
    // 测试简单数字的 LIMIT（应该能提取到具体值）
    String sql = "select id, name from ods.user_table limit 10;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    Optional<LimitMetadata> limit = metadata.getLimit();
    assertTrue("应该有 LIMIT 元数据", limit.isPresent());
    LimitMetadata limitMetadata = limit.get();
    assertNotNull("rowCount 应该不为 null", limitMetadata.getRowCount());
    assertEquals("rowCount 应该为 10", Long.valueOf(10), limitMetadata.getRowCount());
  }

  @Test
  public void testLimitWithOffset() {
    // 测试带 OFFSET 的 LIMIT
    String sql = "select id, name from ods.user_table limit 10 offset 5;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    Optional<LimitMetadata> limit = metadata.getLimit();
    assertTrue("应该有 LIMIT 元数据", limit.isPresent());
    LimitMetadata limitMetadata = limit.get();
    assertNotNull("offset 应该不为 null", limitMetadata.getOffset());
    assertNotNull("rowCount 应该不为 null", limitMetadata.getRowCount());
    assertEquals("offset 应该为 5", Long.valueOf(5), limitMetadata.getOffset());
    assertEquals("rowCount 应该为 10", Long.valueOf(10), limitMetadata.getRowCount());
  }

  @Test
  public void testLimitWithCommaFormat() {
    // 测试逗号格式的 LIMIT（LIMIT offset, rowCount）
    String sql = "select id, name from ods.user_table limit 5, 10;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    Optional<LimitMetadata> limit = metadata.getLimit();
    assertTrue("应该有 LIMIT 元数据", limit.isPresent());
    LimitMetadata limitMetadata = limit.get();
    assertNotNull("offset 应该不为 null", limitMetadata.getOffset());
    assertNotNull("rowCount 应该不为 null", limitMetadata.getRowCount());
    assertEquals("offset 应该为 5", Long.valueOf(5), limitMetadata.getOffset());
    assertEquals("rowCount 应该为 10", Long.valueOf(10), limitMetadata.getRowCount());
  }

  @Test
  public void testWindowClause() {
    // 测试 WINDOW 子句解析
    String sql =
        "select id, name, row_number() over w1 as rn "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    // 验证 WINDOW 子句被解析（通过 AST 访问）
    // 注意：当前元数据提取器可能还没有提取 WINDOW 信息，但解析应该成功
    assertNotNull("元数据应该不为 null", metadata);
    System.out.println("WINDOW 子句解析测试通过");
  }

  @Test
  public void testWindowClauseWithMultipleDefinitions() {
    // 测试多个窗口定义的 WINDOW 子句
    String sql =
        "select id, name, "
            + "row_number() over w1 as rn, "
            + "sum(salary) over w2 as total "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary), "
            + "w2 as (partition by dept rows between 1 preceding and 1 following);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    assertNotNull("元数据应该不为 null", metadata);
    System.out.println("多个窗口定义解析测试通过");
  }

  @Test
  public void testWindowClauseWithFrame() {
    // 测试带窗口框架的 WINDOW 子句
    String sql =
        "select id, name, sum(salary) over w1 as running_total "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary rows between unbounded preceding and current row);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);
    assertNotNull("元数据应该不为 null", metadata);
    System.out.println("窗口框架解析测试通过");
  }

  @Test
  public void testWindowFunctionWithNamedWindow() {
    // 测试使用命名窗口的窗口函数
    String sql =
        "select id, name, dept, salary, "
            + "row_number() over w1 as rn, "
            + "sum(salary) over w1 as dept_total "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    // 验证窗口定义被提取
    Collection<WindowMetadata> windows = metadata.getWindows();
    assertTrue("应该有窗口定义", windows.size() >= 1);
    WindowMetadata window =
        windows.stream()
            .filter(w -> "w1".equals(w.getWindowName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少窗口定义 w1"));
    assertTrue("应该有分区列", window.getPartitionColumns().size() >= 1);
    assertTrue("应该有排序列", window.getOrderColumns().size() >= 1);

    // 验证窗口函数被提取
    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 2);

    WindowFunctionMetadata rowNumberFunc =
        windowFunctions.stream()
            .filter(f -> "ROW_NUMBER".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 ROW_NUMBER 窗口函数"));
    assertEquals("应该引用窗口 w1", "w1", rowNumberFunc.getWindowNameRef());

    WindowFunctionMetadata sumFunc =
        windowFunctions.stream()
            .filter(f -> "SUM".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 SUM 窗口函数"));
    assertEquals("应该引用窗口 w1", "w1", sumFunc.getWindowNameRef());
  }

  @Test
  public void testWindowFunctionWithInlineSpecification() {
    // 测试使用内联窗口规范的窗口函数
    String sql =
        "select id, name, dept, salary, "
            + "row_number() over (partition by dept order by salary) as rn, "
            + "sum(salary) over (partition by dept) as dept_total "
            + "from ods.user_table;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    // 验证窗口函数被提取
    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 2);

    WindowFunctionMetadata rowNumberFunc =
        windowFunctions.stream()
            .filter(f -> "ROW_NUMBER".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 ROW_NUMBER 窗口函数"));
    assertNull("不应该有窗口名称引用", rowNumberFunc.getWindowNameRef());
    assertTrue("应该有分区列", rowNumberFunc.getPartitionColumns().size() >= 1);
    assertTrue("应该有排序列", rowNumberFunc.getOrderColumns().size() >= 1);

    WindowFunctionMetadata sumFunc =
        windowFunctions.stream()
            .filter(f -> "SUM".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 SUM 窗口函数"));
    assertNull("不应该有窗口名称引用", sumFunc.getWindowNameRef());
    assertTrue("应该有分区列", sumFunc.getPartitionColumns().size() >= 1);
  }

  @Test
  public void testWindowFunctionWithFrame() {
    // 测试带窗口框架的窗口函数
    String sql =
        "select id, name, dept, salary, "
            + "sum(salary) over (partition by dept order by salary rows between unbounded preceding and current row) as running_total, "
            + "avg(salary) over (partition by dept rows between 1 preceding and 1 following) as moving_avg "
            + "from ods.user_table;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    // 验证窗口函数被提取
    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 2);

    WindowFunctionMetadata sumFunc =
        windowFunctions.stream()
            .filter(f -> "SUM".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 SUM 窗口函数"));
    assertNotNull("应该有窗口框架类型", sumFunc.getFrameType());
    assertTrue("应该有分区列", sumFunc.getPartitionColumns().size() >= 1);
    assertTrue("应该有排序列", sumFunc.getOrderColumns().size() >= 1);

    WindowFunctionMetadata avgFunc =
        windowFunctions.stream()
            .filter(f -> "AVG".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 AVG 窗口函数"));
    assertNotNull("应该有窗口框架类型", avgFunc.getFrameType());
  }

  @Test
  public void testWindowFunctionComplex() {
    // 测试复杂的窗口函数场景：命名窗口 + 内联窗口规范
    String sql =
        "select id, name, dept, salary, "
            + "row_number() over w1 as rn, "
            + "rank() over (partition by dept order by salary desc) as rank, "
            + "dense_rank() over w2 as dense_rank, "
            + "sum(salary) over w1 as dept_total "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary), "
            + "w2 as (partition by dept order by salary rows between unbounded preceding and unbounded following);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    // 验证窗口定义
    Collection<WindowMetadata> windows = metadata.getWindows();
    assertEquals("应该有 2 个窗口定义", 2, windows.size());

    // 验证窗口函数
    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 4);

    // 验证命名窗口引用
    long namedWindowRefs =
        windowFunctions.stream().filter(f -> f.getWindowNameRef() != null).count();
    assertTrue("应该有命名窗口引用", namedWindowRefs >= 3);

    // 验证内联窗口规范
    long inlineSpecs =
        windowFunctions.stream()
            .filter(f -> f.getWindowNameRef() == null && !f.getPartitionColumns().isEmpty())
            .count();
    assertTrue("应该有内联窗口规范", inlineSpecs >= 1);
  }

  @Test
  public void testWindowFunctionWithMultiplePartitions() {
    // 测试多个分区列的窗口函数
    String sql =
        "select id, name, dept, region, salary, "
            + "sum(salary) over (partition by dept, region order by salary) as dept_region_total "
            + "from ods.user_table;";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 1);

    WindowFunctionMetadata sumFunc =
        windowFunctions.stream()
            .filter(f -> "SUM".equalsIgnoreCase(f.getFunctionName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("缺少 SUM 窗口函数"));
    assertTrue("应该有多个分区列", sumFunc.getPartitionColumns().size() >= 2);
  }

  @Test
  public void testWindowFunctionMetadataExtraction() {
    // 测试窗口函数元数据提取的完整性
    String sql =
        "select id, name, dept, salary, "
            + "row_number() over w1 as rn, "
            + "sum(salary) over w1 as total, "
            + "avg(salary) over (partition by dept) as avg_salary "
            + "from ods.user_table "
            + "window w1 as (partition by dept order by salary rows between unbounded preceding and current row);";
    OdpsSQLValidator validator = new OdpsSQLValidator();
    OdpsSQLValidationResult result = validator.validate(sql);
    assertTrue(result.isSupported());
    assertTrue(result.isValid());

    OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

    // 验证窗口定义元数据
    Collection<WindowMetadata> windows = metadata.getWindows();
    assertTrue("应该有窗口定义", windows.size() >= 1);

    WindowMetadata window = windows.iterator().next();
    assertNotNull("窗口名称应该不为 null", window.getWindowName());
    assertFalse("应该有分区列", window.getPartitionColumns().isEmpty());
    assertFalse("应该有排序列", window.getOrderColumns().isEmpty());
    assertNotNull("应该有窗口框架类型", window.getFrameType());

    // 验证窗口函数元数据
    Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
    assertTrue("应该有窗口函数", windowFunctions.size() >= 3);

    for (WindowFunctionMetadata func : windowFunctions) {
      assertNotNull("函数名称应该不为 null", func.getFunctionName());
      assertNotNull("表达式应该不为 null", func.getExpression());
      // 要么有窗口名称引用，要么有分区列（内联规范）
      assertTrue(
          "应该有窗口名称引用或分区列",
          func.getWindowNameRef() != null || !func.getPartitionColumns().isEmpty());
    }
  }
}
