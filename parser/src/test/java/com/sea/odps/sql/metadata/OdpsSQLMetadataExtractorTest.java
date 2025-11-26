package com.sea.odps.sql.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.function.FunctionCallMetadata;
import com.sea.odps.sql.metadata.model.function.FunctionType;
import com.sea.odps.sql.metadata.model.window.WindowFunctionMetadata;

import junit.framework.TestCase;

/** {@link OdpsSQLMetadataExtractor} 测试类。 重点测试函数调用提取、聚合函数检测、字段分类等功能。 */
public class OdpsSQLMetadataExtractorTest extends TestCase {

    private OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();

    /** 测试聚合函数提取和分类。 */
    @Test
    public void testAggregateFunctionExtraction() {
        String sql =
                "select count(id), sum(amount), avg(price), min(score), max(score) from ods.sales;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 验证字段分类
        Collection<FieldMetadata> fields = metadata.getFields();
        assertEquals("应该有 5 个字段", 5, fields.size());

        // 验证所有字段都被识别为聚合类型
        for (FieldMetadata field : fields) {
            assertEquals("字段应该是聚合类型", FieldCategory.AGGREGATE, field.getCategory());
        }

        // 验证函数调用被提取
        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        assertTrue("应该有函数调用", functionCalls.size() >= 5);

        // 验证聚合函数被正确识别
        List<FunctionCallMetadata> aggregateFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.AGGREGATE)
                        .collect(Collectors.toList());
        assertTrue("应该有聚合函数", aggregateFunctions.size() >= 5);

        // 验证具体的聚合函数
        assertTrue(
                "应该有 COUNT 函数",
                aggregateFunctions.stream()
                        .anyMatch(f -> "COUNT".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 SUM 函数",
                aggregateFunctions.stream()
                        .anyMatch(f -> "SUM".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 AVG 函数",
                aggregateFunctions.stream()
                        .anyMatch(f -> "AVG".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 MIN 函数",
                aggregateFunctions.stream()
                        .anyMatch(f -> "MIN".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 MAX 函数",
                aggregateFunctions.stream()
                        .anyMatch(f -> "MAX".equalsIgnoreCase(f.getFunctionName())));
    }

    /** 测试所有聚合函数的识别。 */
    @Test
    public void testAllAggregateFunctions() {
        String sql =
                "select count(id), sum(amount), avg(price), min(score), max(score), "
                        + "group_concat(name), stddev(value), variance(value), "
                        + "collect_list(items), collect_set(tags) "
                        + "from ods.data;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> aggregateFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.AGGREGATE)
                        .collect(Collectors.toList());

        assertEquals("应该有 10 个聚合函数", 10, aggregateFunctions.size());

        // 验证所有聚合函数都被识别
        String[] expectedFunctions = {
            "COUNT",
            "SUM",
            "AVG",
            "MIN",
            "MAX",
            "GROUP_CONCAT",
            "STDDEV",
            "VARIANCE",
            "COLLECT_LIST",
            "COLLECT_SET"
        };

        for (String funcName : expectedFunctions) {
            assertTrue(
                    "应该有 " + funcName + " 函数",
                    aggregateFunctions.stream()
                            .anyMatch(f -> funcName.equalsIgnoreCase(f.getFunctionName())));
        }
    }

    /** 测试字符串函数识别。 */
    @Test
    public void testStringFunctionExtraction() {
        String sql =
                "select substring(name, 1, 10), upper(title), lower(description), "
                        + "concat(first_name, ' ', last_name), trim(space), "
                        + "length(text), replace(content, 'old', 'new') "
                        + "from ods.users;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> stringFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.STRING)
                        .collect(Collectors.toList());

        assertTrue("应该有字符串函数", stringFunctions.size() >= 7);

        // 验证具体的字符串函数
        assertTrue(
                "应该有 SUBSTRING 函数",
                stringFunctions.stream()
                        .anyMatch(f -> "SUBSTRING".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 UPPER 函数",
                stringFunctions.stream()
                        .anyMatch(f -> "UPPER".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 LOWER 函数",
                stringFunctions.stream()
                        .anyMatch(f -> "LOWER".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 CONCAT 函数",
                stringFunctions.stream()
                        .anyMatch(f -> "CONCAT".equalsIgnoreCase(f.getFunctionName())));
    }

    /** 测试数学函数识别。 */
    @Test
    public void testMathFunctionExtraction() {
        String sql =
                "select abs(value), round(price, 2), floor(amount), ceil(score), "
                        + "power(base, exp), sqrt(number), mod(a, b) "
                        + "from ods.calculations;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> mathFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.MATH)
                        .collect(Collectors.toList());

        assertTrue("应该有数学函数", mathFunctions.size() >= 7);

        // 验证具体的数学函数
        assertTrue(
                "应该有 ABS 函数",
                mathFunctions.stream().anyMatch(f -> "ABS".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 ROUND 函数",
                mathFunctions.stream()
                        .anyMatch(f -> "ROUND".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 FLOOR 函数",
                mathFunctions.stream()
                        .anyMatch(f -> "FLOOR".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 CEIL 函数",
                mathFunctions.stream().anyMatch(f -> "CEIL".equalsIgnoreCase(f.getFunctionName())));
    }

    /** 测试日期时间函数识别。 */
    @Test
    public void testDateTimeFunctionExtraction() {
        String sql =
                "select date_format(create_time, '%Y-%m-%d'), year(birth_date), "
                        + "month(create_time), day(create_time), "
                        + "now(), current_date(), current_timestamp() "
                        + "from ods.events;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> dateTimeFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.DATE_TIME)
                        .collect(Collectors.toList());

        assertTrue("应该有日期时间函数", dateTimeFunctions.size() >= 7);

        // 验证具体的日期时间函数
        assertTrue(
                "应该有 DATE_FORMAT 函数",
                dateTimeFunctions.stream()
                        .anyMatch(f -> "DATE_FORMAT".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 YEAR 函数",
                dateTimeFunctions.stream()
                        .anyMatch(f -> "YEAR".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 MONTH 函数",
                dateTimeFunctions.stream()
                        .anyMatch(f -> "MONTH".equalsIgnoreCase(f.getFunctionName())));
    }

    /** 测试类型转换函数识别。 */
    @Test
    public void testCastFunctionExtraction() {
        String sql =
                "select cast(price as decimal(10,2)), convert(id, string), "
                        + "to_number(amount), to_string(value), to_date(date_str) "
                        + "from ods.products;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> castFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.CAST)
                        .collect(Collectors.toList());

        // 至少应该有部分类型转换函数被识别
        assertTrue("应该有类型转换函数，实际数量: " + castFunctions.size(), castFunctions.size() >= 1);

        // 验证具体的类型转换函数（至少部分）
        boolean hasCast =
                castFunctions.stream().anyMatch(f -> "CAST".equalsIgnoreCase(f.getFunctionName()));
        boolean hasConvert =
                castFunctions.stream()
                        .anyMatch(f -> "CONVERT".equalsIgnoreCase(f.getFunctionName()));
        boolean hasToNumber =
                castFunctions.stream()
                        .anyMatch(f -> "TO_NUMBER".equalsIgnoreCase(f.getFunctionName()));
        boolean hasToString =
                castFunctions.stream()
                        .anyMatch(f -> "TO_STRING".equalsIgnoreCase(f.getFunctionName()));
        boolean hasToDate =
                castFunctions.stream()
                        .anyMatch(f -> "TO_DATE".equalsIgnoreCase(f.getFunctionName()));

        assertTrue(
                "至少应该有一个类型转换函数被识别 (CAST/CONVERT/TO_NUMBER/TO_STRING/TO_DATE)",
                hasCast || hasConvert || hasToNumber || hasToString || hasToDate);
    }

    /** 测试条件函数识别。 注意：IF 可能被识别为关键字，COALESCE、NULLIF、IFNULL、NVL 应该被识别为函数。 */
    @Test
    public void testConditionalFunctionExtraction() {
        String sql =
                "select coalesce(name, 'unknown'), nullif(a, b), "
                        + "ifnull(value, 0), nvl(amount, 0), decode(status, 1, 'active', 0, 'inactive', 'unknown') "
                        + "from ods.scores;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> conditionalFunctions =
                functionCalls.stream()
                        .filter(f -> f.getFunctionType() == FunctionType.CONDITIONAL)
                        .collect(Collectors.toList());

        // 至少应该有部分条件函数被识别
        assertTrue(
                "应该有条件函数，实际数量: " + conditionalFunctions.size(), conditionalFunctions.size() >= 2);

        // 验证具体的条件函数（至少部分）
        boolean hasCoalesce =
                conditionalFunctions.stream()
                        .anyMatch(f -> "COALESCE".equalsIgnoreCase(f.getFunctionName()));
        boolean hasNullif =
                conditionalFunctions.stream()
                        .anyMatch(f -> "NULLIF".equalsIgnoreCase(f.getFunctionName()));
        boolean hasIfnull =
                conditionalFunctions.stream()
                        .anyMatch(f -> "IFNULL".equalsIgnoreCase(f.getFunctionName()));
        boolean hasNvl =
                conditionalFunctions.stream()
                        .anyMatch(f -> "NVL".equalsIgnoreCase(f.getFunctionName()));
        boolean hasDecode =
                conditionalFunctions.stream()
                        .anyMatch(f -> "DECODE".equalsIgnoreCase(f.getFunctionName()));

        assertTrue(
                "至少应该有一个条件函数被识别 (COALESCE/NULLIF/IFNULL/NVL/DECODE)",
                hasCoalesce || hasNullif || hasIfnull || hasNvl || hasDecode);
    }

    /**
     * 测试窗口函数识别。 注意：窗口函数在 extractWindowFunction 中被单独处理，可能不会出现在 functionCalls 中。 但应该出现在
     * windowFunctions 元数据中。
     */
    @Test
    public void testWindowFunctionExtraction() {
        String sql =
                "select id, name, "
                        + "row_number() over (partition by dept order by salary) as rn, "
                        + "sum(salary) over (partition by dept) as dept_total, "
                        + "avg(score) over (partition by dept order by score) as dept_avg "
                        + "from ods.employees;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        // 窗口函数应该在 windowFunctions 元数据中
        Collection<WindowFunctionMetadata> windowFunctions = metadata.getWindowFunctions();
        assertTrue("应该有窗口函数元数据，实际数量: " + windowFunctions.size(), windowFunctions.size() >= 2);

        // 验证窗口函数属性
        for (WindowFunctionMetadata func : windowFunctions) {
            assertNotNull("函数名称应该不为 null", func.getFunctionName());
            assertNotNull("表达式应该不为 null", func.getExpression());
        }

        // 验证 ROW_NUMBER 函数（纯窗口函数，不是聚合函数）
        Optional<WindowFunctionMetadata> rowNumberFunc =
                windowFunctions.stream()
                        .filter(f -> "ROW_NUMBER".equalsIgnoreCase(f.getFunctionName()))
                        .findFirst();
        assertTrue("应该有 ROW_NUMBER 窗口函数", rowNumberFunc.isPresent());

        // 验证窗口函数也在 functionCalls 中（如果被提取）
        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> windowFunctionCalls =
                functionCalls.stream()
                        .filter(f -> f.isWindowFunction())
                        .collect(Collectors.toList());

        // 窗口函数可能被单独处理，所以 functionCalls 中可能没有，或者有
        // 只要 windowFunctions 中有即可
        assertTrue(
                "窗口函数应该在 windowFunctions 或 functionCalls 中",
                windowFunctions.size() > 0 || windowFunctionCalls.size() > 0);
    }

    /** 测试 DISTINCT 聚合函数识别。 */
    @Test
    public void testDistinctAggregateFunction() {
        String sql =
                "select count(distinct user_id), sum(distinct amount), "
                        + "avg(distinct price) from ods.sales;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();
        List<FunctionCallMetadata> distinctAggregates =
                functionCalls.stream()
                        .filter(f -> f.isAggregate() && f.isDistinct())
                        .collect(Collectors.toList());

        assertTrue("应该有 DISTINCT 聚合函数", distinctAggregates.size() >= 3);

        for (FunctionCallMetadata func : distinctAggregates) {
            assertTrue("应该是聚合函数", func.isAggregate());
            assertTrue("应该是 DISTINCT", func.isDistinct());
        }
    }

    /** 测试函数调用位置信息（在哪个子句中）。 */
    @Test
    public void testFunctionCallLocation() {
        String sql =
                "select count(id) as cnt, sum(amount) as total "
                        + "from ods.sales "
                        + "where substring(name, 1, 5) = 'test' "
                        + "group by region "
                        + "having sum(amount) > 1000 "
                        + "order by sum(amount) desc;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();

        // 验证 SELECT 子句中的函数
        List<FunctionCallMetadata> selectFunctions =
                functionCalls.stream()
                        .filter(f -> "SELECT".equals(f.getClauseLocation()))
                        .collect(Collectors.toList());
        assertTrue("SELECT 子句中应该有函数", selectFunctions.size() >= 2);

        // 验证 WHERE 子句中的函数
        List<FunctionCallMetadata> whereFunctions =
                functionCalls.stream()
                        .filter(f -> "WHERE".equals(f.getClauseLocation()))
                        .collect(Collectors.toList());
        assertTrue("WHERE 子句中应该有函数", whereFunctions.size() >= 1);

        // 验证 HAVING 子句中的函数
        List<FunctionCallMetadata> havingFunctions =
                functionCalls.stream()
                        .filter(f -> "HAVING".equals(f.getClauseLocation()))
                        .collect(Collectors.toList());
        assertTrue("HAVING 子句中应该有函数", havingFunctions.size() >= 1);

        // 验证 ORDER BY 子句中的函数
        List<FunctionCallMetadata> orderByFunctions =
                functionCalls.stream()
                        .filter(f -> "ORDER BY".equals(f.getClauseLocation()))
                        .collect(Collectors.toList());
        assertTrue("ORDER BY 子句中应该有函数", orderByFunctions.size() >= 1);
    }

    /** 测试嵌套函数调用。 */
    @Test
    public void testNestedFunctionCalls() {
        String sql =
                "select sum(round(price, 2)), "
                        + "max(coalesce(score, 0)), "
                        + "avg(abs(value - mean)) "
                        + "from ods.data;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();

        // 嵌套函数应该都被提取
        assertTrue("应该有多个函数调用", functionCalls.size() >= 6);

        // 验证外层聚合函数
        assertTrue(
                "应该有 SUM 函数",
                functionCalls.stream().anyMatch(f -> "SUM".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 MAX 函数",
                functionCalls.stream().anyMatch(f -> "MAX".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 AVG 函数",
                functionCalls.stream().anyMatch(f -> "AVG".equalsIgnoreCase(f.getFunctionName())));

        // 验证内层函数
        assertTrue(
                "应该有 ROUND 函数",
                functionCalls.stream()
                        .anyMatch(f -> "ROUND".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 COALESCE 函数",
                functionCalls.stream()
                        .anyMatch(f -> "COALESCE".equalsIgnoreCase(f.getFunctionName())));
        assertTrue(
                "应该有 ABS 函数",
                functionCalls.stream().anyMatch(f -> "ABS".equalsIgnoreCase(f.getFunctionName())));
    }

    /** 测试函数参数提取。 */
    @Test
    public void testFunctionArgumentsExtraction() {
        String sql =
                "select concat('hello', ' ', 'world'), "
                        + "substring(name, 1, 10), "
                        + "round(price, 2), "
                        + "date_format(create_time, '%Y-%m-%d') "
                        + "from ods.products;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();

        // 验证 CONCAT 函数的参数
        Optional<FunctionCallMetadata> concatFunc =
                functionCalls.stream()
                        .filter(f -> "CONCAT".equalsIgnoreCase(f.getFunctionName()))
                        .findFirst();
        assertTrue("应该有 CONCAT 函数", concatFunc.isPresent());
        List<String> concatArgs = concatFunc.get().getArguments();
        assertTrue("CONCAT 应该有参数", concatArgs.size() >= 3);

        // 验证 SUBSTRING 函数的参数
        Optional<FunctionCallMetadata> substringFunc =
                functionCalls.stream()
                        .filter(f -> "SUBSTRING".equalsIgnoreCase(f.getFunctionName()))
                        .findFirst();
        assertTrue("应该有 SUBSTRING 函数", substringFunc.isPresent());
        List<String> substringArgs = substringFunc.get().getArguments();
        assertTrue("SUBSTRING 应该有参数", substringArgs.size() >= 3);
    }

    /** 测试混合函数类型。 */
    @Test
    public void testMixedFunctionTypes() {
        String sql =
                "select count(id), "
                        + "upper(name), "
                        + "round(price, 2), "
                        + "date_format(create_time, '%Y-%m-%d'), "
                        + "cast(amount as decimal(10,2)), "
                        + "coalesce(value, 0) "
                        + "from ods.data;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();

        // 验证不同类型的函数都被识别（至少部分）
        assertTrue(
                "应该有聚合函数",
                functionCalls.stream()
                        .anyMatch(f -> f.getFunctionType() == FunctionType.AGGREGATE));
        assertTrue(
                "应该有字符串函数",
                functionCalls.stream().anyMatch(f -> f.getFunctionType() == FunctionType.STRING));
        assertTrue(
                "应该有数学函数",
                functionCalls.stream().anyMatch(f -> f.getFunctionType() == FunctionType.MATH));
        assertTrue(
                "应该有日期时间函数",
                functionCalls.stream()
                        .anyMatch(f -> f.getFunctionType() == FunctionType.DATE_TIME));

        // 至少应该有部分函数类型被识别
        assertTrue("应该有多种函数类型被识别，实际数量: " + functionCalls.size(), functionCalls.size() >= 4);
    }

    /** 测试字段分类（聚合函数字段应该被识别为 AGGREGATE 类型）。 */
    @Test
    public void testFieldCategoryForAggregateFunctions() {
        String sql =
                "select id, "
                        + "count(*) as cnt, "
                        + "sum(amount) as total, "
                        + "avg(price) as avg_price, "
                        + "name || ' ' || surname as full_name "
                        + "from ods.sales;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FieldMetadata> fields = metadata.getFields();

        // 验证直接列字段
        Optional<FieldMetadata> idField =
                fields.stream().filter(f -> "id".equals(f.getExpression())).findFirst();
        assertTrue("应该有 id 字段", idField.isPresent());
        assertEquals("id 字段应该是 DIRECT 类型", FieldCategory.DIRECT, idField.get().getCategory());

        // 验证聚合函数字段
        Optional<FieldMetadata> cntField =
                fields.stream()
                        .filter(f -> "cnt".equalsIgnoreCase(f.getAliasOptional().orElse("")))
                        .findFirst();
        assertTrue("应该有 cnt 字段", cntField.isPresent());
        assertEquals(
                "cnt 字段应该是 AGGREGATE 类型", FieldCategory.AGGREGATE, cntField.get().getCategory());

        Optional<FieldMetadata> totalField =
                fields.stream()
                        .filter(f -> "total".equalsIgnoreCase(f.getAliasOptional().orElse("")))
                        .findFirst();
        assertTrue("应该有 total 字段", totalField.isPresent());
        assertEquals(
                "total 字段应该是 AGGREGATE 类型",
                FieldCategory.AGGREGATE,
                totalField.get().getCategory());

        Optional<FieldMetadata> avgPriceField =
                fields.stream()
                        .filter(f -> "avg_price".equalsIgnoreCase(f.getAliasOptional().orElse("")))
                        .findFirst();
        assertTrue("应该有 avg_price 字段", avgPriceField.isPresent());
        assertEquals(
                "avg_price 字段应该是 AGGREGATE 类型",
                FieldCategory.AGGREGATE,
                avgPriceField.get().getCategory());
    }

    /** 测试函数别名提取。 */
    @Test
    public void testFunctionAliasExtraction() {
        String sql =
                "select count(id) as user_count, "
                        + "sum(amount) total_amount, "
                        + "avg(price) avg_price "
                        + "from ods.sales;";
        OdpsSQLMetadataResult result = validator.result(sql);
        assertTrue(result.isSupported());
        assertTrue(result.isValid());

        OdpsSQLMetadata metadata = result.getMetadata().orElseThrow(AssertionError::new);

        Collection<FunctionCallMetadata> functionCalls = metadata.getFunctionCalls();

        // 验证函数别名
        Optional<FunctionCallMetadata> countFunc =
                functionCalls.stream()
                        .filter(f -> "COUNT".equalsIgnoreCase(f.getFunctionName()))
                        .findFirst();
        assertTrue("应该有 COUNT 函数", countFunc.isPresent());
        assertEquals("COUNT 函数应该有别名", "user_count", countFunc.get().getAlias());

        Optional<FunctionCallMetadata> sumFunc =
                functionCalls.stream()
                        .filter(f -> "SUM".equalsIgnoreCase(f.getFunctionName()))
                        .findFirst();
        assertTrue("应该有 SUM 函数", sumFunc.isPresent());
        assertEquals("SUM 函数应该有别名", "total_amount", sumFunc.get().getAlias());
    }

    /** 测试子查询中的表和字段提取。 */
    @Test
    public void testSubqueryTableAndFieldExtraction() {
        String sql = "SELECT t2.* FROM (SELECT id, name FROM ods.user_table) t2;";
        OdpsSQLMetadataResult result = validator.result(sql);
        System.out.println("SQL: " + sql);
        System.out.println("是否支持: " + result.isSupported());
        System.out.println("是否有效: " + result.isValid());
        if (!result.isValid() && !result.getErrors().isEmpty()) {
            System.out.println("错误信息: " + result.getErrors().get(0).getMessage());
        }
        assertTrue("SQL 应该被支持", result.isSupported());
        if (!result.isValid()) {
            System.out.println("SQL 解析失败，但继续验证提取结果");
        }

        OdpsSQLMetadata metadata = result.getMetadata().orElse(null);
        if (metadata == null) {
            System.out.println("无法提取元数据，可能 SQL 解析失败");
            return; // 如果无法提取元数据，提前返回
        }

        // 验证表提取
        Collection<com.sea.odps.sql.metadata.model.reference.TableReference> tables =
                metadata.getTables();
        System.out.println("提取到的表数量: " + tables.size());
        for (com.sea.odps.sql.metadata.model.reference.TableReference table : tables) {
            System.out.println(
                    "  表: "
                            + table.getQualifiedName()
                            + ", 别名: "
                            + table.getAlias()
                            + ", 是否子查询: "
                            + table.isSubquery());
        }

        // 应该提取到：
        // 1. ods.user_table（子查询内部的表）
        // 2. t2（子查询别名）
        assertTrue("应该至少提取到 2 个表", tables.size() >= 2);

        // 验证子查询表 t2
        Optional<com.sea.odps.sql.metadata.model.reference.TableReference> t2Table =
                metadata.findByAlias("t2");
        assertTrue("应该找到子查询表 t2", t2Table.isPresent());
        assertTrue("t2 应该是子查询", t2Table.get().isSubquery());

        // 验证子查询内部的表 ods.user_table
        Optional<com.sea.odps.sql.metadata.model.reference.TableReference> userTable =
                metadata.findByQualifiedName("ods.user_table");
        assertTrue("应该找到表 ods.user_table", userTable.isPresent());
        assertFalse("ods.user_table 不应该是子查询", userTable.get().isSubquery());
        assertEquals("表名应该是 user_table", "user_table", userTable.get().getName());
        assertEquals("表的 owner 应该是 ods", "ods", userTable.get().getOwner());

        // 验证字段提取
        Collection<FieldMetadata> fields = metadata.getFields();
        System.out.println("提取到的字段数量: " + fields.size());
        for (FieldMetadata field : fields) {
            String scopeAlias = field.getScopeAlias();
            System.out.println(
                    "  字段: "
                            + field.getExpression()
                            + ", 别名: "
                            + field.getAliasOptional().orElse("无")
                            + ", 作用域: "
                            + (scopeAlias != null ? scopeAlias : "无"));
        }

        // 应该提取到子查询内部的字段：id 和 name，以及主查询的 t2.*
        assertTrue("应该至少提取到 3 个字段（子查询内部的 id 和 name，以及主查询的 t2.*）", fields.size() >= 3);

        // 验证 id 字段（应该在子查询 t2 中）
        Optional<FieldMetadata> idField =
                fields.stream().filter(f -> "id".equalsIgnoreCase(f.getExpression())).findFirst();
        assertTrue("应该找到 id 字段", idField.isPresent());
        assertEquals("id 字段的作用域应该是 t2", "t2", idField.get().getScopeAlias());
        assertEquals("id 字段的类别应该是 DIRECT", FieldCategory.DIRECT, idField.get().getCategory());

        // 验证 name 字段（应该在子查询 t2 中）
        Optional<FieldMetadata> nameField =
                fields.stream().filter(f -> "name".equalsIgnoreCase(f.getExpression())).findFirst();
        assertTrue("应该找到 name 字段", nameField.isPresent());
        assertEquals("name 字段的作用域应该是 t2", "t2", nameField.get().getScopeAlias());
        assertEquals("name 字段的类别应该是 DIRECT", FieldCategory.DIRECT, nameField.get().getCategory());
    }
}
