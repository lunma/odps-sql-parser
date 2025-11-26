package com.sea.odps.sql.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.sea.odps.sql.core.segment.dml.LateralViewSegment;
import com.sea.odps.sql.core.segment.dml.WithSegment;
import com.sea.odps.sql.core.segment.dml.combine.CombineSegment;
import com.sea.odps.sql.core.segment.dml.expr.SimpleFunctionSegment;
import com.sea.odps.sql.core.segment.dml.expr.WindowFunctionSegment;
import com.sea.odps.sql.core.segment.dml.item.ColumnProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ExpressionProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionsSegment;
import com.sea.odps.sql.core.segment.dml.order.GroupBySegment;
import com.sea.odps.sql.core.segment.dml.order.OrderBySegment;
import com.sea.odps.sql.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;
import com.sea.odps.sql.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.core.segment.dml.predicate.HavingSegment;
import com.sea.odps.sql.core.segment.dml.predicate.WhereSegment;
import com.sea.odps.sql.core.segment.dml.window.PartitionBySegment;
import com.sea.odps.sql.core.segment.dml.window.WindowDefinitionSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowFrameSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowOrderBySegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSpecificationSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.OwnerSegment;
import com.sea.odps.sql.core.segment.generic.column.ColumnSegment;
import com.sea.odps.sql.core.segment.generic.expr.BinaryOperationExpression;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.NotExpression;
import com.sea.odps.sql.core.segment.generic.expr.complex.CommonTableExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.simple.LiteralExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.subquery.SubquerySegment;
import com.sea.odps.sql.core.segment.generic.hint.HintItemSegment;
import com.sea.odps.sql.core.segment.generic.hint.HintSegment;
import com.sea.odps.sql.core.segment.generic.table.JoinTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SimpleTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SubqueryTableSegment;
import com.sea.odps.sql.core.segment.generic.table.TableSegment;
import com.sea.odps.sql.core.statement.dml.SelectStatement;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;
import com.sea.odps.sql.metadata.model.clause.GroupByMetadata;
import com.sea.odps.sql.metadata.model.clause.HavingConditionMetadata;
import com.sea.odps.sql.metadata.model.clause.LimitMetadata;
import com.sea.odps.sql.metadata.model.clause.OrderByMetadata;
import com.sea.odps.sql.metadata.model.clause.WhereConditionMetadata;
import com.sea.odps.sql.metadata.model.comment.CommentMetadata;
import com.sea.odps.sql.metadata.model.comment.CommentTargetType;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.feature.CombineMetadata;
import com.sea.odps.sql.metadata.model.feature.CteMetadata;
import com.sea.odps.sql.metadata.model.feature.HintMetadata;
import com.sea.odps.sql.metadata.model.feature.LateralViewMetadata;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.function.FunctionCallMetadata;
import com.sea.odps.sql.metadata.model.function.FunctionType;
import com.sea.odps.sql.metadata.model.join.JoinConditionPair;
import com.sea.odps.sql.metadata.model.join.JoinRelation;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;
import com.sea.odps.sql.metadata.model.window.WindowFunctionMetadata;
import com.sea.odps.sql.metadata.model.window.WindowMetadata;
import com.sea.odps.sql.visitor.odps.OdpsSQLSelectStatement;

/** ODPS SQL 元数据抽取器，负责从解析后的 SQL AST 中提取结构化元数据。 遍历 AST 节点，提取表、列、JOIN、字段、子句、特性、注释、窗口等元数据信息。 */
public class OdpsSQLMetadataExtractor {

    // 函数调用匹配模式（预编译，避免重复编译）
    private static final Pattern FUNCTION_PATTERN =
            Pattern.compile("(?i)\\b([a-z_][a-z0-9_]*)\\s*\\(");

    // 关键字集合（用于过滤）
    private static final Set<String> KEYWORDS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList("CASE", "WHEN", "THEN", "ELSE", "END", "IF")));

    // 聚合函数集合（使用不可变集合，性能更好且线程安全）
    private static final Set<String> AGGREGATE_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "COUNT",
                                    "SUM",
                                    "AVG",
                                    "MIN",
                                    "MAX",
                                    "GROUP_CONCAT",
                                    "STDDEV",
                                    "VARIANCE",
                                    "COLLECT_LIST",
                                    "COLLECT_SET")));

    // 字符串函数集合
    private static final Set<String> STRING_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "SUBSTRING",
                                    "SUBSTR",
                                    "CONCAT",
                                    "UPPER",
                                    "LOWER",
                                    "TRIM",
                                    "LTRIM",
                                    "RTRIM",
                                    "REPLACE",
                                    "LENGTH",
                                    "CHAR_LENGTH",
                                    "POSITION",
                                    "LOCATE",
                                    "INSTR",
                                    "LPAD",
                                    "RPAD",
                                    "REVERSE",
                                    "SPLIT")));

    // 数学函数集合
    private static final Set<String> MATH_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "ABS",
                                    "ROUND",
                                    "FLOOR",
                                    "CEIL",
                                    "CEILING",
                                    "POWER",
                                    "SQRT",
                                    "EXP",
                                    "LOG",
                                    "LN",
                                    "MOD",
                                    "SIGN",
                                    "RAND",
                                    "RANDOM",
                                    "TRUNCATE",
                                    "TRUNC")));

    // 日期时间函数集合
    private static final Set<String> DATE_TIME_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "DATE_FORMAT",
                                    "YEAR",
                                    "MONTH",
                                    "DAY",
                                    "HOUR",
                                    "MINUTE",
                                    "SECOND",
                                    "WEEK",
                                    "QUARTER",
                                    "NOW",
                                    "CURRENT_DATE",
                                    "CURRENT_TIME",
                                    "CURRENT_TIMESTAMP",
                                    "DATE_ADD",
                                    "DATE_SUB",
                                    "DATEDIFF",
                                    "TO_DATE",
                                    "FROM_UNIXTIME",
                                    "UNIX_TIMESTAMP")));

    // 类型转换函数集合
    private static final Set<String> CAST_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "CAST",
                                    "CONVERT",
                                    "TO_NUMBER",
                                    "TO_STRING",
                                    "TO_DATE",
                                    "TO_CHAR")));

    // 条件函数集合
    private static final Set<String> CONDITIONAL_FUNCTIONS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            java.util.Arrays.asList(
                                    "IF",
                                    "CASE",
                                    "COALESCE",
                                    "NULLIF",
                                    "IFNULL",
                                    "NVL",
                                    "DECODE")));

    /**
     * 抽取 select 语句元数据。
     *
     * @param selectStatement 解析后的 select 语句
     * @return 元数据
     */
    public OdpsSQLMetadata extract(final OdpsSQLSelectStatement selectStatement) {
        OdpsSQLMetadata metadata = new OdpsSQLMetadata();
        if (selectStatement == null) {
            return metadata;
        }

        // 提取 WITH CTE
        selectStatement.getWith().ifPresent(with -> extractWithCte(with, metadata));

        // 提取 DISTINCT
        if (selectStatement.getProjections() != null) {
            metadata.setDistinct(selectStatement.getProjections().isDistinct());
        }

        // 提取 HINT
        selectStatement.getHint().ifPresent(hint -> extractHint(hint, metadata));

        // 提取表信息
        if (selectStatement.getFrom() != null) {
            collectTableSegment(selectStatement.getFrom(), metadata);
        }

        // 提取字段信息
        if (selectStatement.getProjections() != null) {
            extractFields(selectStatement.getProjections(), metadata, null);
        }

        // 提取 WHERE 条件
        selectStatement.getWhere().ifPresent(where -> extractWhereCondition(where, metadata));

        // 提取 GROUP BY
        selectStatement.getGroupBy().ifPresent(groupBy -> extractGroupBy(groupBy, metadata));

        // 提取 HAVING 条件
        selectStatement.getHaving().ifPresent(having -> extractHavingCondition(having, metadata));

        // 提取 ORDER BY
        selectStatement.getOrderBy().ifPresent(orderBy -> extractOrderBy(orderBy, metadata));

        // 提取 LIMIT
        if (selectStatement.getLimit() != null) {
            extractLimit(selectStatement.getLimit(), metadata);
        }

        // 提取 LATERAL VIEW
        selectStatement
                .getLateralView()
                .ifPresent(lateralView -> extractLateralView(lateralView, metadata));

        // 提取 WINDOW 子句
        if (selectStatement.getWindow() != null) {
            extractWindow(selectStatement.getWindow(), metadata);
        }

        // 提取集合操作
        selectStatement.getCombine().ifPresent(combine -> extractCombine(combine, metadata));

        // 提取注释信息
        extractComments(selectStatement, metadata);

        // 提取函数调用列表
        extractFunctionCalls(selectStatement, metadata);

        return metadata;
    }

    private TableReference collectTableSegment(
            final TableSegment segment, final OdpsSQLMetadata metadata) {
        return collectTableSegment(segment, metadata, null);
    }

    private TableReference collectTableSegment(
            final TableSegment segment,
            final OdpsSQLMetadata metadata,
            final String subqueryAlias) {
        if (segment instanceof SimpleTableSegment) {
            return collectSimpleTable((SimpleTableSegment) segment, metadata, subqueryAlias);
        }
        if (segment instanceof JoinTableSegment) {
            return collectJoinTable((JoinTableSegment) segment, metadata, subqueryAlias);
        }
        if (segment instanceof SubqueryTableSegment) {
            return collectSubqueryTable((SubqueryTableSegment) segment, metadata);
        }
        return null;
    }

    /** 收集简单表信息。 */
    private TableReference collectSimpleTable(
            final SimpleTableSegment segment,
            final OdpsSQLMetadata metadata,
            final String subqueryAlias) {
        TableReference tableRef = metadata.addTable(toTableReference(segment));
        // 如果当前在子查询内部，记录该表属于该子查询
        if (subqueryAlias != null && tableRef != null) {
            metadata.addSubqueryInnerTable(
                    subqueryAlias, tableRef.getAlias(), tableRef.getQualifiedName());
        }
        return tableRef;
    }

    /** 收集 JOIN 表信息。 */
    private TableReference collectJoinTable(
            final JoinTableSegment joinSegment,
            final OdpsSQLMetadata metadata,
            final String subqueryAlias) {
        TableReference left = collectTableSegment(joinSegment.getLeft(), metadata, subqueryAlias);
        TableReference right = collectTableSegment(joinSegment.getRight(), metadata, subqueryAlias);
        JoinRelation joinRelation = toJoinRelation(joinSegment, left, right, metadata);
        metadata.addJoin(joinRelation);
        return registerJoinAlias(joinSegment, metadata);
    }

    /** 收集子查询表信息。 */
    private TableReference collectSubqueryTable(
            final SubqueryTableSegment subqueryTableSegment, final OdpsSQLMetadata metadata) {
        // 添加子查询表本身（标记为子查询）
        TableReference subqueryRef = metadata.addTable(toTableReference(subqueryTableSegment));
        String currentSubqueryAlias = subqueryTableSegment.getAlias().orElse(null);

        // 递归提取子查询内部的表和字段信息
        SubquerySegment subquery = subqueryTableSegment.getSubquery();
        if (subquery != null && subquery.getSelect() != null) {
            extractSubqueryContent(subquery.getSelect(), metadata, currentSubqueryAlias);
        }

        return subqueryRef;
    }

    /** 提取子查询内容（表和字段）。 统一处理 OdpsSQLSelectStatement 和通用 SelectStatement 类型。 */
    private void extractSubqueryContent(
            final SelectStatement subSelect,
            final OdpsSQLMetadata metadata,
            final String subqueryAlias) {
        // 提取子查询内部的表
        if (subSelect.getFrom() != null) {
            collectTableSegment(subSelect.getFrom(), metadata, subqueryAlias);
        }

        // 提取子查询内部的字段信息
        if (subSelect.getProjections() != null) {
            extractFields(subSelect.getProjections(), metadata, subqueryAlias);
        }

        // 处理子查询可能包含的集合操作（UNION/INTERSECT/EXCEPT）
        if (subSelect.getCombine().isPresent()) {
            extractCombineFromSubquery(subSelect.getCombine().get(), metadata, subqueryAlias);
        }
    }

    private TableReference registerJoinAlias(
            final JoinTableSegment joinSegment, final OdpsSQLMetadata metadata) {
        return joinSegment
                .getAlias()
                .map(
                        alias ->
                                metadata.addTable(
                                        new TableReference(null, null, alias, false, alias)))
                .orElse(null);
    }

    private JoinRelation toJoinRelation(
            final JoinTableSegment joinSegment,
            final TableReference left,
            final TableReference right,
            final OdpsSQLMetadata metadata) {
        TableReference leftRef =
                left != null ? left : resolveReference(joinSegment.getLeft(), metadata);
        TableReference rightRef =
                right != null ? right : resolveReference(joinSegment.getRight(), metadata);
        List<JoinConditionPair> columnPairs =
                extractJoinPairs(joinSegment.getCondition(), metadata);
        List<ColumnReference> usingColumns = extractUsingColumns(joinSegment, metadata);
        String conditionText = expressionToText(joinSegment.getCondition());
        return new JoinRelation(
                leftRef,
                rightRef,
                joinSegment.getJoinType(),
                conditionText,
                columnPairs,
                usingColumns);
    }

    private List<JoinConditionPair> extractJoinPairs(
            final ExpressionSegment condition, final OdpsSQLMetadata metadata) {
        if (condition == null || !(condition instanceof BinaryOperationExpression)) {
            return Collections.emptyList();
        }
        BinaryOperationExpression binary = (BinaryOperationExpression) condition;
        ColumnReference left = toColumnReference(binary.getLeft(), metadata);
        ColumnReference right = toColumnReference(binary.getRight(), metadata);
        if (left != null && right != null) {
            return Collections.singletonList(
                    new JoinConditionPair(left, right, binary.getOperator()));
        }
        return Collections.emptyList();
    }

    private List<ColumnReference> extractUsingColumns(
            final JoinTableSegment joinSegment, final OdpsSQLMetadata metadata) {
        List<ColumnSegment> usingColumns = joinSegment.getUsing();
        if (usingColumns == null || usingColumns.isEmpty()) {
            return Collections.emptyList();
        }
        return usingColumns.stream()
                .map(column -> toColumnReference(column, metadata))
                .collect(java.util.stream.Collectors.toList());
    }

    private TableReference resolveReference(
            final TableSegment segment, final OdpsSQLMetadata metadata) {
        if (segment instanceof SimpleTableSegment) {
            SimpleTableSegment simple = (SimpleTableSegment) segment;
            Optional<String> alias = simple.getAlias();
            if (alias.isPresent()) {
                return metadata.findByAlias(alias.get()).orElse(null);
            }
            return metadata.findByQualifiedName(buildQualifiedName(simple)).orElse(null);
        }
        if (segment instanceof SubqueryTableSegment) {
            SubqueryTableSegment subqueryTableSegment = (SubqueryTableSegment) segment;
            return subqueryTableSegment.getAlias().flatMap(metadata::findByAlias).orElse(null);
        }
        if (segment instanceof JoinTableSegment) {
            JoinTableSegment join = (JoinTableSegment) segment;
            if (join.getAlias().isPresent()) {
                return metadata.findByAlias(join.getAlias().get()).orElse(null);
            }
            return resolveReference(join.getRight(), metadata);
        }
        return null;
    }

    private void extractFields(
            final ProjectionsSegment projectionsSegment,
            final OdpsSQLMetadata metadata,
            final String scopeAlias) {
        if (projectionsSegment == null || projectionsSegment.getProjections() == null) {
            return;
        }
        for (ProjectionSegment projectionSegment : projectionsSegment.getProjections()) {
            FieldMetadata fieldMetadata =
                    extractFieldFromProjection(projectionSegment, metadata, scopeAlias);
            if (fieldMetadata != null) {
                metadata.addField(fieldMetadata);
            }
        }
    }

    /**
     * 从投影段中提取字段元数据。
     *
     * @param projectionSegment 投影段
     * @param metadata 元数据对象
     * @param scopeAlias 作用域别名
     * @return 字段元数据，如果无法提取则返回 null
     */
    private FieldMetadata extractFieldFromProjection(
            final ProjectionSegment projectionSegment,
            final OdpsSQLMetadata metadata,
            final String scopeAlias) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return extractFieldFromColumnProjection(
                    (ColumnProjectionSegment) projectionSegment, metadata, scopeAlias);
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return extractFieldFromExpressionProjection(
                    (ExpressionProjectionSegment) projectionSegment, metadata, scopeAlias);
        }
        return null;
    }

    /** 从列投影段中提取字段元数据。 */
    private FieldMetadata extractFieldFromColumnProjection(
            final ColumnProjectionSegment columnProjection,
            final OdpsSQLMetadata metadata,
            final String scopeAlias) {
        ColumnSegment columnSegment = columnProjection.getColumn();
        ColumnReference columnReference = toColumnReference(columnSegment, metadata);
        String alias = columnProjection.getAlias().orElse(null);
        String expression = columnSegment.getExpression();
        return new FieldMetadata(
                alias,
                expression,
                FieldCategory.DIRECT,
                Collections.singletonList(columnReference),
                scopeAlias);
    }

    /** 从表达式投影段中提取字段元数据。 */
    private FieldMetadata extractFieldFromExpressionProjection(
            final ExpressionProjectionSegment expressionProjection,
            final OdpsSQLMetadata metadata,
            final String scopeAlias) {
        ExpressionSegment expressionSegment = expressionProjection.getExpression();
        String alias = expressionProjection.getAlias().orElse(null);
        String expression = expressionToText(expressionSegment);

        // 检查是否是窗口函数
        if (expressionSegment instanceof WindowFunctionSegment) {
            extractWindowFunction((WindowFunctionSegment) expressionSegment, metadata);
        }

        FieldCategory category = determineCategory(expressionSegment, expression);
        List<ColumnReference> columnReferences =
                collectColumnReferences(expressionSegment, metadata);
        return new FieldMetadata(alias, expression, category, columnReferences, scopeAlias);
    }

    private FieldCategory determineCategory(
            final ExpressionSegment expressionSegment, final String expressionText) {
        if (expressionSegment instanceof ColumnSegment) {
            return FieldCategory.DIRECT;
        }
        // 使用 FUNCTION_PATTERN 匹配函数调用，然后检查是否在聚合函数集合中
        if (expressionText != null && containsAggregateFunction(expressionText)) {
            return FieldCategory.AGGREGATE;
        }
        return FieldCategory.EXPRESSION;
    }

    /** 检查表达式文本中是否包含聚合函数调用。 使用预编译的正则表达式和 Set 查找，性能优于直接使用正则表达式匹配。 */
    private boolean containsAggregateFunction(final String expressionText) {
        if (expressionText == null || expressionText.isEmpty()) {
            return false;
        }
        java.util.regex.Matcher matcher = FUNCTION_PATTERN.matcher(expressionText);
        while (matcher.find()) {
            String functionName = matcher.group(1).toUpperCase();
            if (AGGREGATE_FUNCTIONS.contains(functionName)) {
                return true;
            }
        }
        return false;
    }

    private List<ColumnReference> collectColumnReferences(final ExpressionSegment segment) {
        return collectColumnReferences(segment, null);
    }

    private List<ColumnReference> collectColumnReferences(
            final ExpressionSegment segment, final OdpsSQLMetadata metadata) {
        if (segment == null) {
            return Collections.emptyList();
        }
        Set<ColumnReference> result = new LinkedHashSet<>();
        collectColumnReferences(segment, result, metadata);
        return new ArrayList<>(result);
    }

    private void collectColumnReferences(
            final ExpressionSegment segment,
            final Set<ColumnReference> collector,
            final OdpsSQLMetadata metadata) {
        if (segment instanceof ColumnSegment) {
            ColumnReference columnReference = toColumnReference((ColumnSegment) segment, metadata);
            if (columnReference != null) {
                collector.add(columnReference);
            }
            return;
        }
        if (segment instanceof BinaryOperationExpression) {
            BinaryOperationExpression binary = (BinaryOperationExpression) segment;
            collectColumnReferences(binary.getLeft(), collector, metadata);
            collectColumnReferences(binary.getRight(), collector, metadata);
            return;
        }
        if (segment instanceof LiteralExpressionSegment) {
            return;
        }
        if (segment instanceof SubquerySegment) {
            return;
        }
        if (segment instanceof SimpleFunctionSegment) {
            SimpleFunctionSegment function = (SimpleFunctionSegment) segment;
            // 收集函数参数中的列引用
            if (function.getArguments() != null) {
                for (ExpressionSegment arg : function.getArguments()) {
                    collectColumnReferences(arg, collector, metadata);
                }
            }
            return;
        }
        if (segment instanceof WindowFunctionSegment) {
            WindowFunctionSegment windowFunction = (WindowFunctionSegment) segment;
            // 收集窗口函数参数中的列引用
            if (windowFunction.getArguments() != null) {
                for (ExpressionSegment arg : windowFunction.getArguments()) {
                    collectColumnReferences(arg, collector, metadata);
                }
            }
            // 收集窗口规范中的列引用
            if (windowFunction.getWindowSpecification() != null) {
                WindowSpecificationSegment spec = windowFunction.getWindowSpecification();
                if (spec.getPartitionBy() != null) {
                    for (ExpressionSegment expr : spec.getPartitionBy().getExpressions()) {
                        collectColumnReferences(expr, collector, metadata);
                    }
                }
                if (spec.getOrderBy() != null) {
                    for (OrderByItemSegment item : spec.getOrderBy().getItems()) {
                        if (item instanceof ExpressionOrderByItemSegment) {
                            collectColumnReferences(
                                    ((ExpressionOrderByItemSegment) item).getExpression(),
                                    collector,
                                    metadata);
                        }
                    }
                }
            }
            return;
        }
    }

    private ColumnReference toColumnReference(final ExpressionSegment segment) {
        return toColumnReference(segment, null);
    }

    private ColumnReference toColumnReference(
            final ExpressionSegment segment, final OdpsSQLMetadata metadata) {
        if (segment instanceof ColumnSegment) {
            return toColumnReference((ColumnSegment) segment, metadata);
        }
        return null;
    }

    private ColumnReference toColumnReference(final ColumnSegment columnSegment) {
        return toColumnReference(columnSegment, null);
    }

    /**
     * 创建列引用，通过表映射来确定 owner 是表名还是表别名，并建立表引用关系。
     *
     * <p>当提供了 metadata 时，会通过表映射查找来确定 owner 的类型：
     *
     * <ul>
     *   <li>如果通过别名找到表，说明 owner 是表别名或子查询别名
     *   <li>如果通过限定名找到表，说明 owner 是完整的表限定名
     *   <li>如果通过表名找到表，说明 owner 是表名
     * </ul>
     *
     * <p>如果找到对应的表，会在 ColumnReference 中建立表引用关系，后续的 LineageExtractor 可以直接使用这个引用，而不需要再次查找。
     *
     * @param columnSegment 列片段
     * @param metadata 元数据对象（可选，如果提供则通过表映射查找来确定 owner 类型并建立表引用）
     * @return 列引用对象
     */
    private ColumnReference toColumnReference(
            final ColumnSegment columnSegment, final OdpsSQLMetadata metadata) {
        if (columnSegment == null) {
            return null;
        }
        String owner = columnSegment.getOwner().map(this::resolveOwner).orElse(null);
        String name = columnSegment.getIdentifier().getValue();
        String raw = columnSegment.getExpression();

        // 如果提供了 metadata 且 owner 不为空，尝试通过表映射来确定 owner 的类型并建立表引用
        TableReference tableRef = null;
        if (metadata != null && owner != null && !owner.isEmpty()) {
            // 先尝试通过别名查找（表别名或子查询别名）
            Optional<TableReference> tableByAlias = metadata.findByAlias(owner);
            if (tableByAlias.isPresent()) {
                tableRef = tableByAlias.get();
            } else {
                // 再尝试通过限定名查找（可能是完整的表名，如 db.schema.table）
                Optional<TableReference> tableByQualifiedName = metadata.findByQualifiedName(owner);
                if (tableByQualifiedName.isPresent()) {
                    tableRef = tableByQualifiedName.get();
                } else {
                    // 尝试通过表名查找（可能是简单的表名，如 table）
                    for (TableReference table : metadata.getTables()) {
                        if (owner.equals(table.getName())) {
                            tableRef = table;
                            break;
                        }
                    }
                }
            }
        }

        return new ColumnReference(owner, name, raw, tableRef);
    }

    private TableReference toTableReference(final SimpleTableSegment tableSegment) {
        String owner = tableSegment.getOwner().map(this::resolveOwner).orElse(null);
        String name = tableSegment.getTableName().getIdentifier().getValue();
        String alias = tableSegment.getAlias().orElse(null);
        String raw = buildQualifiedName(tableSegment);
        return new TableReference(owner, name, alias, false, raw);
    }

    private TableReference toTableReference(final SubqueryTableSegment subqueryTableSegment) {
        String alias = subqueryTableSegment.getAlias().orElse(null);
        return new TableReference(null, null, alias, true, "SUBQUERY");
    }

    private String buildQualifiedName(final SimpleTableSegment tableSegment) {
        String owner = tableSegment.getOwner().map(this::resolveOwner).orElse(null);
        String tableName = tableSegment.getTableName().getIdentifier().getValue();
        if (tableName == null) {
            return null;
        }
        return owner == null || owner.isEmpty() ? tableName : owner + "." + tableName;
    }

    private String resolveOwner(final OwnerSegment ownerSegment) {
        if (ownerSegment == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        OwnerSegment current = ownerSegment;
        while (current != null) {
            parts.add(0, current.getIdentifier().getValue());
            current = current.getOwner().orElse(null);
        }
        return String.join(".", parts);
    }

    private String expressionToText(final ExpressionSegment segment) {
        if (segment == null) {
            return null;
        }
        if (segment instanceof BinaryOperationExpression) {
            return ((BinaryOperationExpression) segment).getText();
        }
        if (segment instanceof LiteralExpressionSegment) {
            Object literal = ((LiteralExpressionSegment) segment).getLiterals();
            return literal == null ? null : literal.toString();
        }
        if (segment instanceof ColumnSegment) {
            return ((ColumnSegment) segment).getExpression();
        }
        if (segment instanceof SubquerySegment) {
            return "SUBQUERY";
        }
        if (segment instanceof SimpleFunctionSegment) {
            return ((SimpleFunctionSegment) segment).getOriginalText();
        }
        if (segment instanceof WindowFunctionSegment) {
            return ((WindowFunctionSegment) segment).getOriginalText();
        }
        return segment.toString();
    }

    private void extractWhereCondition(
            final WhereSegment whereSegment, final OdpsSQLMetadata metadata) {
        if (whereSegment == null || whereSegment.getExpr() == null) {
            return;
        }
        String expression = expressionToText(whereSegment.getExpr());
        List<ColumnReference> columns = collectColumnReferences(whereSegment.getExpr(), metadata);
        metadata.setWhereCondition(new WhereConditionMetadata(expression, columns));
    }

    private void extractGroupBy(
            final GroupBySegment groupBySegment, final OdpsSQLMetadata metadata) {
        Collection<OrderByItemSegment> items =
                groupBySegment != null ? groupBySegment.getGroupByItems() : null;
        if (items == null || items.isEmpty()) {
            return;
        }
        List<ColumnReference> columns =
                items.stream()
                        .filter(ExpressionOrderByItemSegment.class::isInstance)
                        .map(ExpressionOrderByItemSegment.class::cast)
                        .flatMap(
                                item ->
                                        collectColumnReferences(item.getExpression(), metadata)
                                                .stream())
                        .collect(java.util.stream.Collectors.toList());
        metadata.setGroupBy(new GroupByMetadata(columns));
    }

    private void extractHavingCondition(
            final HavingSegment havingSegment, final OdpsSQLMetadata metadata) {
        if (havingSegment == null || havingSegment.getExpr() == null) {
            return;
        }
        String expression = expressionToText(havingSegment.getExpr());
        List<ColumnReference> columns = collectColumnReferences(havingSegment.getExpr(), metadata);
        metadata.setHavingCondition(new HavingConditionMetadata(expression, columns));
    }

    private void extractOrderBy(
            final OrderBySegment orderBySegment, final OdpsSQLMetadata metadata) {
        Collection<OrderByItemSegment> items =
                orderBySegment != null ? orderBySegment.getOrderByItems() : null;
        if (items == null || items.isEmpty()) {
            return;
        }
        List<ColumnReference> columns =
                items.stream()
                        .filter(ExpressionOrderByItemSegment.class::isInstance)
                        .map(ExpressionOrderByItemSegment.class::cast)
                        .flatMap(
                                item ->
                                        collectColumnReferences(item.getExpression(), metadata)
                                                .stream())
                        .collect(java.util.stream.Collectors.toList());
        metadata.setOrderBy(new OrderByMetadata(columns));
    }

    private void extractLimit(final LimitSegment limitSegment, final OdpsSQLMetadata metadata) {
        if (limitSegment == null) {
            return;
        }
        Long offset =
                limitSegment
                        .getOffset()
                        .filter(NumberLiteralPaginationValueSegment.class::isInstance)
                        .map(NumberLiteralPaginationValueSegment.class::cast)
                        .map(NumberLiteralPaginationValueSegment::getValue)
                        .orElse(null);
        Long rowCount =
                limitSegment
                        .getRowCount()
                        .filter(NumberLiteralPaginationValueSegment.class::isInstance)
                        .map(NumberLiteralPaginationValueSegment.class::cast)
                        .map(NumberLiteralPaginationValueSegment::getValue)
                        .orElse(null);
        metadata.setLimit(new LimitMetadata(offset, rowCount));
    }

    private void extractWithCte(final WithSegment withSegment, final OdpsSQLMetadata metadata) {
        Collection<CommonTableExpressionSegment> ctes =
                withSegment != null ? withSegment.getCommonTableExpressions() : null;
        if (ctes == null || ctes.isEmpty()) {
            return;
        }
        for (CommonTableExpressionSegment cte : ctes) {
            String name = cte.getIdentifier().getValue();
            List<String> columns =
                    Optional.ofNullable(cte.getColumns())
                            .filter(c -> !c.isEmpty())
                            .map(
                                    cteColumns ->
                                            cteColumns.stream()
                                                    .map(
                                                            column ->
                                                                    column.getIdentifier()
                                                                            .getValue())
                                                    .collect(java.util.stream.Collectors.toList()))
                            .orElse(Collections.emptyList());
            metadata.addCte(new CteMetadata(name, columns));
        }
    }

    private void extractHint(final HintSegment hintSegment, final OdpsSQLMetadata metadata) {
        Collection<HintItemSegment> items = hintSegment != null ? hintSegment.getHintItems() : null;
        if (items == null || items.isEmpty()) {
            return;
        }
        List<String> hints =
                items.stream()
                        .map(HintItemSegment::getHintText)
                        .collect(java.util.stream.Collectors.toList());
        metadata.setHint(new HintMetadata(hints));
    }

    private void extractWindow(final WindowSegment windowSegment, final OdpsSQLMetadata metadata) {
        List<WindowDefinitionSegment> definitions =
                windowSegment != null ? windowSegment.getWindowDefinitions() : null;
        if (definitions == null || definitions.isEmpty()) {
            return;
        }
        for (WindowDefinitionSegment windowDef : definitions) {
            String windowName =
                    Optional.ofNullable(windowDef.getWindowName())
                            .map(name -> name.getValue())
                            .orElse(null);

            WindowSpecificationSegment spec = windowDef.getSpecification();
            WindowSpecInfo specInfo = extractWindowSpecInfo(spec);

            metadata.addWindow(
                    new WindowMetadata(
                            windowName,
                            specInfo.partitionColumns,
                            specInfo.orderColumns,
                            specInfo.frameType));
        }
    }

    /** 窗口规范信息提取结果。 */
    private static class WindowSpecInfo {
        final List<String> partitionColumns;
        final List<String> orderColumns;
        final String frameType;

        WindowSpecInfo(
                final List<String> partitionColumns,
                final List<String> orderColumns,
                final String frameType) {
            this.partitionColumns = partitionColumns;
            this.orderColumns = orderColumns;
            this.frameType = frameType;
        }
    }

    /** 提取窗口规范信息（分区列、排序列、窗口框架类型）。 供 extractWindow 和 extractWindowFunction 共用。 */
    private WindowSpecInfo extractWindowSpecInfo(final WindowSpecificationSegment spec) {
        List<String> partitionColumns = new ArrayList<>();
        List<String> orderColumns = new ArrayList<>();
        String frameType = null;

        if (spec != null) {
            // 提取分区列
            PartitionBySegment partitionBy = spec.getPartitionBy();
            if (partitionBy != null && partitionBy.getExpressions() != null) {
                partitionColumns =
                        partitionBy.getExpressions().stream()
                                .map(this::expressionToText)
                                .filter(col -> col != null && !col.isEmpty())
                                .collect(java.util.stream.Collectors.toList());
            }

            // 提取排序列
            WindowOrderBySegment orderBy = spec.getOrderBy();
            if (orderBy != null && orderBy.getItems() != null) {
                orderColumns =
                        orderBy.getItems().stream()
                                .filter(ExpressionOrderByItemSegment.class::isInstance)
                                .map(ExpressionOrderByItemSegment.class::cast)
                                .map(item -> expressionToText(item.getExpression()))
                                .filter(col -> col != null && !col.isEmpty())
                                .collect(java.util.stream.Collectors.toList());
            }

            // 提取窗口框架类型
            WindowFrameSegment windowFrame = spec.getWindowFrame();
            if (windowFrame != null) {
                frameType = windowFrame.getFrameType();
            }
        }

        return new WindowSpecInfo(partitionColumns, orderColumns, frameType);
    }

    private void extractWindowFunction(
            final WindowFunctionSegment windowFunction, final OdpsSQLMetadata metadata) {
        if (windowFunction == null) {
            return;
        }

        String functionName = windowFunction.getFunctionName();
        List<String> arguments =
                Optional.ofNullable(windowFunction.getArguments())
                        .filter(a -> !a.isEmpty())
                        .map(
                                functionArgs ->
                                        functionArgs.stream()
                                                .map(this::expressionToText)
                                                .filter(
                                                        argText ->
                                                                argText != null
                                                                        && !argText.isEmpty())
                                                .collect(java.util.stream.Collectors.toList()))
                        .orElse(Collections.emptyList());

        WindowSpecificationSegment spec = windowFunction.getWindowSpecification();
        String windowNameRef =
                Optional.ofNullable(spec)
                        .flatMap(WindowSpecificationSegment::getWindowNameRef)
                        .map(IdentifierValue::getValue)
                        .orElse(null);

        WindowSpecInfo specInfo = extractWindowSpecInfo(spec);

        metadata.addWindowFunction(
                new WindowFunctionMetadata(
                        functionName,
                        arguments,
                        windowNameRef,
                        specInfo.partitionColumns,
                        specInfo.orderColumns,
                        specInfo.frameType,
                        windowFunction.getOriginalText()));
    }

    private void extractLateralView(
            final LateralViewSegment lateralViewSegment, final OdpsSQLMetadata metadata) {
        if (lateralViewSegment == null) {
            return;
        }
        String function = expressionToText(lateralViewSegment.getFunction());
        String tableAlias =
                Optional.ofNullable(lateralViewSegment.getTableAlias())
                        .map(alias -> alias.getIdentifier().getValue())
                        .orElse(null);
        List<String> columnAliases =
                Optional.ofNullable(lateralViewSegment.getColumnAliases())
                        .filter(a -> !a.isEmpty())
                        .map(
                                aliases ->
                                        aliases.stream()
                                                .map(IdentifierValue::getValue)
                                                .collect(java.util.stream.Collectors.toList()))
                        .orElse(Collections.emptyList());
        metadata.setLateralView(
                new LateralViewMetadata(
                        function, tableAlias, columnAliases, lateralViewSegment.isOuter()));
    }

    private void extractCombine(
            final CombineSegment combineSegment, final OdpsSQLMetadata metadata) {
        if (combineSegment == null) {
            return;
        }
        String typeName = combineSegment.getCombineType().name();
        boolean all = typeName.endsWith("_ALL");
        if (all) {
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        metadata.addCombine(new CombineMetadata(typeName, all));

        // 递归提取右侧查询的表和字段（主查询级别的集合操作）
        // 注意：主查询的集合操作不需要传递 subqueryAlias（传递 null）
        SelectStatement rightStatement = combineSegment.getRight();
        if (rightStatement instanceof OdpsSQLSelectStatement) {
            OdpsSQLSelectStatement odpsRight = (OdpsSQLSelectStatement) rightStatement;

            // 提取右侧查询的表
            if (odpsRight.getFrom() != null) {
                collectTableSegment(odpsRight.getFrom(), metadata, null);
            }

            // 提取右侧查询的字段
            if (odpsRight.getProjections() != null) {
                extractFields(odpsRight.getProjections(), metadata, null);
            }

            // 递归处理右侧查询可能包含的集合操作
            odpsRight.getCombine().ifPresent(combine -> extractCombine(combine, metadata));
        } else {
            // 对于非 OdpsSQLSelectStatement 类型，使用通用接口
            if (rightStatement.getFrom() != null) {
                collectTableSegment(rightStatement.getFrom(), metadata, null);
            }
            if (rightStatement.getProjections() != null) {
                extractFields(rightStatement.getProjections(), metadata, null);
            }
            // 递归处理右侧查询可能包含的集合操作
            rightStatement.getCombine().ifPresent(combine -> extractCombine(combine, metadata));
        }
    }

    /**
     * 从子查询中提取集合操作，递归处理右侧查询的表和字段。
     *
     * @param combineSegment 集合操作片段
     * @param metadata 元数据对象
     * @param subqueryAlias 子查询别名（用于标记属于该子查询的表）
     */
    private void extractCombineFromSubquery(
            final CombineSegment combineSegment,
            final OdpsSQLMetadata metadata,
            final String subqueryAlias) {
        if (combineSegment == null) {
            return;
        }

        // 递归提取右侧查询的表和字段
        SelectStatement rightStatement = combineSegment.getRight();
        if (rightStatement instanceof OdpsSQLSelectStatement) {
            OdpsSQLSelectStatement odpsRight = (OdpsSQLSelectStatement) rightStatement;

            // 提取右侧查询的表
            if (odpsRight.getFrom() != null) {
                collectTableSegment(odpsRight.getFrom(), metadata, subqueryAlias);
            }

            // 提取右侧查询的字段
            if (odpsRight.getProjections() != null) {
                extractFields(odpsRight.getProjections(), metadata, subqueryAlias);
            }

            // 递归处理右侧查询可能包含的集合操作
            odpsRight
                    .getCombine()
                    .ifPresent(
                            combine ->
                                    extractCombineFromSubquery(combine, metadata, subqueryAlias));
        } else {
            // 对于非 OdpsSQLSelectStatement 类型，使用通用接口
            if (rightStatement.getFrom() != null) {
                collectTableSegment(rightStatement.getFrom(), metadata, subqueryAlias);
            }
            if (rightStatement.getProjections() != null) {
                extractFields(rightStatement.getProjections(), metadata, subqueryAlias);
            }
            // 递归处理右侧查询可能包含的集合操作
            rightStatement
                    .getCombine()
                    .ifPresent(
                            combine ->
                                    extractCombineFromSubquery(combine, metadata, subqueryAlias));
        }
    }

    private void extractComments(
            final OdpsSQLSelectStatement selectStatement, final OdpsSQLMetadata metadata) {
        if (selectStatement == null) {
            return;
        }

        // 混合方案：从 Segment 中收集注释（精确关联）
        // 1. 从表 Segment 中收集注释
        if (selectStatement.getFrom() != null) {
            extractCommentFromTable(selectStatement.getFrom(), metadata);
        }

        // 2. 从字段 Segment 中收集注释
        if (selectStatement.getProjections() != null) {
            extractCommentFromFields(selectStatement.getProjections(), metadata);
        }

        // 3. 从 WHERE 子句中收集注释
        if (selectStatement.getWhere().isPresent()) {
            extractCommentFromWhere(selectStatement.getWhere().get(), metadata);
        }

        // 4. 从 GROUP BY 子句中收集注释
        if (selectStatement.getGroupBy().isPresent()) {
            extractCommentFromGroupBy(selectStatement.getGroupBy().get(), metadata);
        }

        // 5. 从 HAVING 子句中收集注释
        if (selectStatement.getHaving().isPresent()) {
            extractCommentFromHaving(selectStatement.getHaving().get(), metadata);
        }

        // 6. 从 ORDER BY 子句中收集注释
        if (selectStatement.getOrderBy().isPresent()) {
            extractCommentFromOrderBy(selectStatement.getOrderBy().get(), metadata);
        }

        // 7. 从 LIMIT 子句中收集注释
        if (selectStatement.getLimit() != null) {
            extractCommentFromLimit(selectStatement.getLimit(), metadata);
        }

        // 8. 从 WITH CTE 子句中收集注释
        if (selectStatement.getWith().isPresent()) {
            extractCommentFromWith(selectStatement.getWith().get(), metadata);
        }

        // 9. 从 HINT 子句中收集注释
        if (selectStatement.getHint().isPresent()) {
            extractCommentFromHint(selectStatement.getHint().get(), metadata);
        }

        // 10. 从 LATERAL VIEW 子句中收集注释
        if (selectStatement.getLateralView().isPresent()) {
            extractCommentFromLateralView(selectStatement.getLateralView().get(), metadata);
        }

        // 11. 从 AbstractSQLStatement 中提取语句级别的注释（不在任何特定 Segment 附近的注释）
        // 先收集所有 Segment 级别的注释位置，用于去重
        Set<Integer> segmentCommentPositions = collectSegmentCommentPositions(selectStatement);

        Collection<CommentSegment> statementComments = selectStatement.getCommentSegments();
        if (statementComments != null) {
            for (CommentSegment commentSegment : statementComments) {
                if (commentSegment != null) {
                    // 检查是否已被 Segment 提取（通过位置判断）
                    // 如果注释的起始位置在 Segment 注释位置集合中，说明已被 Segment 提取，跳过
                    if (!segmentCommentPositions.contains(commentSegment.getStartIndex())) {
                        // 这是真正的语句级别注释，使用位置计算关联
                        CommentMetadata commentMetadata =
                                associateCommentWithElement(
                                        commentSegment, selectStatement, metadata);
                        metadata.addComment(commentMetadata);
                    }
                    // 如果已被 Segment 提取，则跳过（避免重复）
                }
            }
        }
    }

    /**
     * 将注释与具体的 SQL 元素关联。 根据注释的位置信息，找到最近的 SQL 元素并建立关联。
     *
     * @param commentSegment 注释片段
     * @param selectStatement SELECT 语句
     * @param metadata 元数据
     * @return 关联后的注释元数据
     */
    private CommentMetadata associateCommentWithElement(
            final CommentSegment commentSegment,
            final OdpsSQLSelectStatement selectStatement,
            final OdpsSQLMetadata metadata) {
        int commentStart = commentSegment.getStartIndex();
        int commentEnd = commentSegment.getStopIndex();
        String commentText = commentSegment.getText();

        // 策略1：检查注释是否在字段附近（SELECT 子句中的字段）
        FieldMetadata nearestField =
                findNearestField(commentStart, commentEnd, selectStatement, metadata);
        if (null != nearestField) {
            return CommentMetadata.forField(commentText, commentStart, commentEnd, nearestField);
        }

        // 策略2：检查注释是否在表附近
        TableReference nearestTable =
                findNearestTable(commentStart, commentEnd, selectStatement, metadata);
        if (null != nearestTable) {
            return CommentMetadata.forTable(commentText, commentStart, commentEnd, nearestTable);
        }

        // 策略3：检查注释是否在列引用附近（WHERE、GROUP BY、ORDER BY 等中的列）
        ColumnReference nearestColumn =
                findNearestColumn(commentStart, commentEnd, selectStatement, metadata);
        if (null != nearestColumn) {
            return CommentMetadata.forColumn(commentText, commentStart, commentEnd, nearestColumn);
        }

        // 策略4：检查注释是否在特定子句附近
        CommentTargetType clauseType = findNearestClause(commentStart, commentEnd, selectStatement);
        if (clauseType != CommentTargetType.UNKNOWN) {
            return CommentMetadata.forTarget(
                    commentText, commentStart, commentEnd, clauseType, null);
        }

        // 如果无法关联到具体元素，返回基础注释元数据
        return new CommentMetadata(commentText, commentStart, commentEnd);
    }

    /**
     * 查找最近的表引用。 通过访问原始 SelectStatement 中的 TableSegment 来获取位置信息，计算距离。
     *
     * @param commentStart 注释起始位置
     * @param commentEnd 注释结束位置
     * @param selectStatement SELECT 语句
     * @param metadata 元数据
     * @return 最近的表引用，如果未找到则返回 null
     */
    private TableReference findNearestTable(
            final int commentStart,
            final int commentEnd,
            final OdpsSQLSelectStatement selectStatement,
            final OdpsSQLMetadata metadata) {
        if (null == selectStatement.getFrom() || metadata.getTables().isEmpty()) {
            return null;
        }
        // 通过访问原始 TableSegment 获取位置信息
        TableSegment tableSegment = selectStatement.getFrom();
        int tableStart = tableSegment.getStartIndex();
        int tableEnd = tableSegment.getStopIndex();

        // 计算注释与表的距离（注释在表之后且距离较近）
        // 如果注释在表的范围内或紧跟在表之后，认为注释的是表
        if (commentStart >= tableStart && commentStart <= tableEnd + 50) {
            // 查找对应的 TableReference（通过表名或别名匹配）
            return findTableReferenceBySegment(tableSegment, metadata);
        }
        return null;
    }

    /**
     * 根据 TableSegment 查找对应的 TableReference。
     *
     * @param tableSegment 表片段
     * @param metadata 元数据
     * @return 表引用，如果未找到则返回 null
     */
    private TableReference findTableReferenceBySegment(
            final TableSegment tableSegment, final OdpsSQLMetadata metadata) {
        if (tableSegment instanceof SimpleTableSegment) {
            SimpleTableSegment simpleTable = (SimpleTableSegment) tableSegment;
            String qualifiedName = buildQualifiedName(simpleTable);
            Optional<String> alias = simpleTable.getAlias();

            // 先尝试通过限定名查找
            if (null != qualifiedName) {
                Optional<TableReference> found = metadata.findByQualifiedName(qualifiedName);
                if (found.isPresent()) {
                    return found.get();
                }
            }
            // 再尝试通过别名查找
            if (alias.isPresent()) {
                Optional<TableReference> found = metadata.findByAlias(alias.get());
                if (found.isPresent()) {
                    return found.get();
                }
            }
        }
        // 如果无法精确匹配，返回第一个表引用
        if (!metadata.getTables().isEmpty()) {
            return metadata.getTables().iterator().next();
        }
        return null;
    }

    /**
     * 查找最近的字段元数据。 通过访问原始 SelectStatement 中的 ProjectionSegment 来获取位置信息，计算距离。
     *
     * @param commentStart 注释起始位置
     * @param commentEnd 注释结束位置
     * @param selectStatement SELECT 语句
     * @param metadata 元数据
     * @return 最近的字段元数据，如果未找到则返回 null
     */
    private FieldMetadata findNearestField(
            final int commentStart,
            final int commentEnd,
            final OdpsSQLSelectStatement selectStatement,
            final OdpsSQLMetadata metadata) {
        if (null == selectStatement.getProjections() || metadata.getFields().isEmpty()) {
            return null;
        }
        // 通过访问原始 ProjectionsSegment 获取位置信息
        ProjectionsSegment projections = selectStatement.getProjections();
        int projectionsStart = projections.getStartIndex();
        int projectionsEnd = projections.getStopIndex();

        // 如果注释在 SELECT 子句的范围内，尝试匹配字段
        if (commentStart >= projectionsStart && commentStart <= projectionsEnd + 50) {
            // 遍历所有投影项，找到最近的字段
            int minDistance = Integer.MAX_VALUE;
            FieldMetadata nearestField = null;
            int fieldIndex = 0;

            for (ProjectionSegment projection : projections.getProjections()) {
                int fieldStart = projection.getStartIndex();
                int fieldEnd = projection.getStopIndex();

                // 计算注释与字段的距离
                int distance = calculateDistance(commentStart, commentEnd, fieldStart, fieldEnd);
                if (distance < minDistance) {
                    minDistance = distance;
                    // 通过索引查找对应的 FieldMetadata
                    List<FieldMetadata> fields = new ArrayList<>(metadata.getFields());
                    if (fieldIndex < fields.size()) {
                        nearestField = fields.get(fieldIndex);
                    }
                }
                fieldIndex++;
            }
            return nearestField;
        }
        return null;
    }

    /**
     * 计算两个位置范围之间的距离。 如果重叠或相邻，距离为 0；否则返回它们之间的最小距离。
     *
     * @param start1 第一个范围的起始位置
     * @param end1 第一个范围的结束位置
     * @param start2 第二个范围的起始位置
     * @param end2 第二个范围的结束位置
     * @return 距离
     */
    private int calculateDistance(
            final int start1, final int end1, final int start2, final int end2) {
        if (end1 < start2) {
            // 第一个范围在第二个范围之前
            return start2 - end1;
        }
        if (end2 < start1) {
            // 第二个范围在第一个范围之前
            return start1 - end2;
        }
        // 重叠或相邻，距离为 0
        return 0;
    }

    /**
     * 查找最近的列引用。
     *
     * @param commentStart 注释起始位置
     * @param commentEnd 注释结束位置
     * @param selectStatement SELECT 语句
     * @param metadata 元数据
     * @return 最近的列引用，如果未找到则返回 null
     */
    private ColumnReference findNearestColumn(
            final int commentStart,
            final int commentEnd,
            final OdpsSQLSelectStatement selectStatement,
            final OdpsSQLMetadata metadata) {
        // 从 WHERE、GROUP BY、ORDER BY 等子句中查找列引用
        // 简化实现：从 WHERE 条件中查找第一个列引用
        if (selectStatement.getWhere().isPresent()) {
            WhereConditionMetadata whereCondition = metadata.getWhereCondition().orElse(null);
            if (null != whereCondition && !whereCondition.getColumns().isEmpty()) {
                return whereCondition.getColumns().get(0);
            }
        }
        // 从 GROUP BY 中查找
        if (selectStatement.getGroupBy().isPresent()) {
            GroupByMetadata groupBy = metadata.getGroupBy().orElse(null);
            if (null != groupBy && !groupBy.getColumns().isEmpty()) {
                return groupBy.getColumns().get(0);
            }
        }
        // 从 ORDER BY 中查找
        if (selectStatement.getOrderBy().isPresent()) {
            OrderByMetadata orderBy = metadata.getOrderBy().orElse(null);
            if (null != orderBy && !orderBy.getColumns().isEmpty()) {
                return orderBy.getColumns().get(0);
            }
        }
        return null;
    }

    /**
     * 查找最近的子句类型。
     *
     * @param commentStart 注释起始位置
     * @param commentEnd 注释结束位置
     * @param selectStatement SELECT 语句
     * @return 最近的子句类型
     */
    private CommentTargetType findNearestClause(
            final int commentStart,
            final int commentEnd,
            final OdpsSQLSelectStatement selectStatement) {
        // 根据子句的位置信息判断注释是否在某个子句附近
        // 简化实现：根据子句是否存在来判断
        if (selectStatement.getWhere().isPresent()) {
            return CommentTargetType.WHERE;
        }
        if (selectStatement.getGroupBy().isPresent()) {
            return CommentTargetType.GROUP_BY;
        }
        if (selectStatement.getHaving().isPresent()) {
            return CommentTargetType.HAVING;
        }
        if (selectStatement.getOrderBy().isPresent()) {
            return CommentTargetType.ORDER_BY;
        }
        if (null != selectStatement.getLimit()) {
            return CommentTargetType.LIMIT;
        }
        if (selectStatement.getWith().isPresent()) {
            return CommentTargetType.CTE;
        }
        if (selectStatement.getHint().isPresent()) {
            return CommentTargetType.HINT;
        }
        if (selectStatement.getLateralView().isPresent()) {
            return CommentTargetType.LATERAL_VIEW;
        }
        return CommentTargetType.UNKNOWN;
    }

    // ==================== 混合方案：从 Segment 中收集注释 ====================

    /**
     * 从表 Segment 中收集注释。
     *
     * @param tableSegment 表片段
     * @param metadata 元数据
     */
    private void extractCommentFromTable(
            final TableSegment tableSegment, final OdpsSQLMetadata metadata) {
        if (tableSegment == null) {
            return;
        }

        CommentSegment comment = null;
        if (tableSegment instanceof SimpleTableSegment) {
            comment = ((SimpleTableSegment) tableSegment).getComment();
        }

        if (comment != null) {
            TableReference tableRef = findTableReferenceBySegment(tableSegment, metadata);
            if (tableRef != null) {
                CommentMetadata commentMetadata =
                        CommentMetadata.forTable(
                                comment.getText(),
                                comment.getStartIndex(),
                                comment.getStopIndex(),
                                tableRef);
                metadata.addComment(commentMetadata);
            }
        }

        // 递归处理 JOIN 中的表
        if (tableSegment instanceof JoinTableSegment) {
            JoinTableSegment joinSegment = (JoinTableSegment) tableSegment;
            extractCommentFromTable(joinSegment.getLeft(), metadata);
            extractCommentFromTable(joinSegment.getRight(), metadata);
        }
    }

    /**
     * 从字段 Segment 中收集注释。
     *
     * @param projectionsSegment 投影片段
     * @param metadata 元数据
     */
    private void extractCommentFromFields(
            final ProjectionsSegment projectionsSegment, final OdpsSQLMetadata metadata) {
        if (projectionsSegment == null) {
            return;
        }

        List<FieldMetadata> fields = new ArrayList<>(metadata.getFields());
        int fieldIndex = 0;

        for (ProjectionSegment projection : projectionsSegment.getProjections()) {
            CommentSegment comment = null;
            if (projection instanceof ColumnProjectionSegment) {
                comment = ((ColumnProjectionSegment) projection).getComment();
            } else if (projection instanceof ExpressionProjectionSegment) {
                comment = ((ExpressionProjectionSegment) projection).getComment();
            }

            if (comment != null && fieldIndex < fields.size()) {
                FieldMetadata field = fields.get(fieldIndex);
                if (field != null) {
                    CommentMetadata commentMetadata =
                            CommentMetadata.forField(
                                    comment.getText(),
                                    comment.getStartIndex(),
                                    comment.getStopIndex(),
                                    field);
                    metadata.addComment(commentMetadata);
                }
            }
            fieldIndex++;
        }
    }

    /**
     * 从 WHERE 子句中收集注释。
     *
     * @param whereSegment WHERE 片段
     * @param metadata 元数据
     */
    private void extractCommentFromWhere(
            final WhereSegment whereSegment, final OdpsSQLMetadata metadata) {
        if (whereSegment == null) {
            return;
        }

        CommentSegment comment = whereSegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.WHERE,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 GROUP BY 子句中收集注释。
     *
     * @param groupBySegment GROUP BY 片段
     * @param metadata 元数据
     */
    private void extractCommentFromGroupBy(
            final GroupBySegment groupBySegment, final OdpsSQLMetadata metadata) {
        if (groupBySegment == null) {
            return;
        }

        CommentSegment comment = groupBySegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.GROUP_BY,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 HAVING 子句中收集注释。
     *
     * @param havingSegment HAVING 片段
     * @param metadata 元数据
     */
    private void extractCommentFromHaving(
            final HavingSegment havingSegment, final OdpsSQLMetadata metadata) {
        if (havingSegment == null) {
            return;
        }

        CommentSegment comment = havingSegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.HAVING,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 ORDER BY 子句中收集注释。
     *
     * @param orderBySegment ORDER BY 片段
     * @param metadata 元数据
     */
    private void extractCommentFromOrderBy(
            final OrderBySegment orderBySegment, final OdpsSQLMetadata metadata) {
        if (orderBySegment == null) {
            return;
        }

        CommentSegment comment = orderBySegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.ORDER_BY,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 LIMIT 子句中收集注释。
     *
     * @param limitSegment LIMIT 片段
     * @param metadata 元数据
     */
    private void extractCommentFromLimit(
            final LimitSegment limitSegment, final OdpsSQLMetadata metadata) {
        if (limitSegment == null) {
            return;
        }

        CommentSegment comment = limitSegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.LIMIT,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 WITH CTE 子句中收集注释。
     *
     * @param withSegment WITH 片段
     * @param metadata 元数据
     */
    private void extractCommentFromWith(
            final WithSegment withSegment, final OdpsSQLMetadata metadata) {
        if (withSegment == null) {
            return;
        }

        CommentSegment comment = withSegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.CTE,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 HINT 子句中收集注释。
     *
     * @param hintSegment HINT 片段
     * @param metadata 元数据
     */
    private void extractCommentFromHint(
            final HintSegment hintSegment, final OdpsSQLMetadata metadata) {
        if (hintSegment == null) {
            return;
        }

        CommentSegment comment = hintSegment.getComment();
        if (comment != null) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.HINT,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 从 LATERAL VIEW 子句中收集注释。
     *
     * @param lateralViewSegment LATERAL VIEW 片段
     * @param metadata 元数据
     */
    private void extractCommentFromLateralView(
            final LateralViewSegment lateralViewSegment, final OdpsSQLMetadata metadata) {
        if (null == lateralViewSegment) {
            return;
        }

        CommentSegment comment = lateralViewSegment.getComment();
        if (null != comment) {
            CommentMetadata commentMetadata =
                    CommentMetadata.forTarget(
                            comment.getText(),
                            comment.getStartIndex(),
                            comment.getStopIndex(),
                            CommentTargetType.LATERAL_VIEW,
                            null);
            metadata.addComment(commentMetadata);
        }
    }

    /**
     * 收集所有 Segment 级别的注释位置。 用于在提取语句级别注释时进行去重，避免重复提取。
     *
     * @param selectStatement SELECT 语句
     * @return Segment 注释位置集合（使用 startIndex 作为唯一标识）
     */
    private Set<Integer> collectSegmentCommentPositions(
            final OdpsSQLSelectStatement selectStatement) {
        Set<Integer> positions = new HashSet<>();

        // 收集表注释位置
        if (null != selectStatement.getFrom()) {
            collectTableCommentPositions(selectStatement.getFrom(), positions);
        }

        // 收集字段注释位置
        if (null != selectStatement.getProjections()) {
            for (ProjectionSegment projection : selectStatement.getProjections().getProjections()) {
                CommentSegment comment = null;
                if (projection instanceof ColumnProjectionSegment) {
                    comment = ((ColumnProjectionSegment) projection).getComment();
                } else if (projection instanceof ExpressionProjectionSegment) {
                    comment = ((ExpressionProjectionSegment) projection).getComment();
                }
                if (null != comment) {
                    positions.add(comment.getStartIndex());
                }
            }
        }

        // 收集 WHERE 注释位置
        if (selectStatement.getWhere().isPresent()) {
            CommentSegment comment = selectStatement.getWhere().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 GROUP BY 注释位置
        if (selectStatement.getGroupBy().isPresent()) {
            CommentSegment comment = selectStatement.getGroupBy().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 HAVING 注释位置
        if (selectStatement.getHaving().isPresent()) {
            CommentSegment comment = selectStatement.getHaving().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 ORDER BY 注释位置
        if (selectStatement.getOrderBy().isPresent()) {
            CommentSegment comment = selectStatement.getOrderBy().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 LIMIT 注释位置
        if (null != selectStatement.getLimit()) {
            CommentSegment comment = selectStatement.getLimit().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 WITH CTE 注释位置
        if (selectStatement.getWith().isPresent()) {
            CommentSegment comment = selectStatement.getWith().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 HINT 注释位置
        if (selectStatement.getHint().isPresent()) {
            CommentSegment comment = selectStatement.getHint().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 收集 LATERAL VIEW 注释位置
        if (selectStatement.getLateralView().isPresent()) {
            CommentSegment comment = selectStatement.getLateralView().get().getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        return positions;
    }

    /**
     * 递归收集表注释位置（包括 JOIN 中的表）。
     *
     * @param tableSegment 表片段
     * @param positions 位置集合
     */
    private void collectTableCommentPositions(
            final TableSegment tableSegment, final Set<Integer> positions) {
        if (null == tableSegment) {
            return;
        }

        if (tableSegment instanceof SimpleTableSegment) {
            CommentSegment comment = ((SimpleTableSegment) tableSegment).getComment();
            if (null != comment) {
                positions.add(comment.getStartIndex());
            }
        }

        // 递归处理 JOIN 中的表
        if (tableSegment instanceof JoinTableSegment) {
            JoinTableSegment joinSegment = (JoinTableSegment) tableSegment;
            collectTableCommentPositions(joinSegment.getLeft(), positions);
            collectTableCommentPositions(joinSegment.getRight(), positions);
        }
    }

    /**
     * 提取函数调用列表。
     *
     * @param selectStatement SELECT 语句
     * @param metadata 元数据对象
     */
    private void extractFunctionCalls(
            final OdpsSQLSelectStatement selectStatement, final OdpsSQLMetadata metadata) {
        if (selectStatement == null) {
            return;
        }

        // 从 SELECT 子句提取函数调用
        if (selectStatement.getProjections() != null) {
            extractFunctionCallsFromProjections(
                    selectStatement.getProjections(), metadata, "SELECT");
        }

        // 从 WHERE 子句提取函数调用
        if (selectStatement.getWhere().isPresent()) {
            extractFunctionCallsFromExpression(
                    selectStatement.getWhere().get().getExpr(), metadata, "WHERE");
        }

        // 从 GROUP BY 子句提取函数调用
        if (selectStatement.getGroupBy().isPresent()) {
            extractFunctionCallsFromGroupBy(
                    selectStatement.getGroupBy().get(), metadata, "GROUP BY");
        }

        // 从 HAVING 子句提取函数调用
        if (selectStatement.getHaving().isPresent()) {
            extractFunctionCallsFromExpression(
                    selectStatement.getHaving().get().getExpr(), metadata, "HAVING");
        }

        // 从 ORDER BY 子句提取函数调用
        if (selectStatement.getOrderBy().isPresent()) {
            extractFunctionCallsFromOrderBy(
                    selectStatement.getOrderBy().get(), metadata, "ORDER BY");
        }
    }

    /** 从投影列表提取函数调用。 */
    private void extractFunctionCallsFromProjections(
            final ProjectionsSegment projections,
            final OdpsSQLMetadata metadata,
            final String clauseLocation) {
        if (projections == null || projections.getProjections() == null) {
            return;
        }
        for (ProjectionSegment projection : projections.getProjections()) {
            if (projection instanceof ExpressionProjectionSegment) {
                ExpressionProjectionSegment exprProjection =
                        (ExpressionProjectionSegment) projection;
                ExpressionSegment expression = exprProjection.getExpression();

                // 窗口函数已经在 extractWindowFunction 中处理，跳过
                if (expression instanceof WindowFunctionSegment) {
                    continue;
                }

                String alias = exprProjection.getAlias().orElse(null);
                extractFunctionCallsFromExpression(expression, metadata, clauseLocation, alias);
            }
        }
    }

    /** 从表达式提取函数调用。 */
    private void extractFunctionCallsFromExpression(
            final ExpressionSegment expression,
            final OdpsSQLMetadata metadata,
            final String clauseLocation) {
        extractFunctionCallsFromExpression(expression, metadata, clauseLocation, null);
    }

    /** 从表达式提取函数调用。 */
    private void extractFunctionCallsFromExpression(
            final ExpressionSegment expression,
            final OdpsSQLMetadata metadata,
            final String clauseLocation,
            final String alias) {
        if (expression == null) {
            return;
        }

        String expressionText = expressionToText(expression);
        if (expressionText == null || expressionText.isEmpty()) {
            return;
        }

        // 缓存大写表达式文本，避免重复转换
        String upperExpressionText = expressionText.toUpperCase();
        boolean containsOver = upperExpressionText.contains(" OVER ");
        boolean containsDistinct = upperExpressionText.contains("DISTINCT");

        // 使用预编译的正则表达式匹配函数调用
        java.util.regex.Matcher matcher = FUNCTION_PATTERN.matcher(expressionText);

        while (matcher.find()) {
            String functionName = matcher.group(1);
            String upperFunctionName = functionName.toUpperCase();

            // 跳过已知的关键字
            if (KEYWORDS.contains(upperFunctionName)) {
                continue;
            }

            // 提取函数参数
            List<String> arguments = extractFunctionArguments(expressionText, matcher.start());

            // 判断是否为聚合函数
            boolean isAggregate = AGGREGATE_FUNCTIONS.contains(upperFunctionName);

            // 识别函数类型
            FunctionType functionType =
                    identifyFunctionType(upperFunctionName, isAggregate, containsOver);

            // 创建函数调用元数据
            FunctionCallMetadata functionCall =
                    new FunctionCallMetadata(
                            functionName,
                            functionType,
                            arguments,
                            expressionText,
                            clauseLocation,
                            alias,
                            containsOver,
                            isAggregate,
                            containsDistinct && isAggregate);

            metadata.addFunctionCall(functionCall);
        }

        // 递归处理嵌套表达式（避免重复提取）
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binary = (BinaryOperationExpression) expression;
            extractFunctionCallsFromExpression(binary.getLeft(), metadata, clauseLocation, alias);
            extractFunctionCallsFromExpression(binary.getRight(), metadata, clauseLocation, alias);
        } else if (expression instanceof NotExpression) {
            NotExpression notExpr = (NotExpression) expression;
            extractFunctionCallsFromExpression(
                    notExpr.getExpression(), metadata, clauseLocation, alias);
        }
    }

    /** 从 GROUP BY 子句提取函数调用。 */
    private void extractFunctionCallsFromGroupBy(
            final GroupBySegment groupBy,
            final OdpsSQLMetadata metadata,
            final String clauseLocation) {
        extractFunctionCallsFromOrderByItems(
                groupBy != null ? groupBy.getGroupByItems() : null, metadata, clauseLocation);
    }

    /** 从 ORDER BY 子句提取函数调用。 */
    private void extractFunctionCallsFromOrderBy(
            final OrderBySegment orderBy,
            final OdpsSQLMetadata metadata,
            final String clauseLocation) {
        extractFunctionCallsFromOrderByItems(
                orderBy != null ? orderBy.getOrderByItems() : null, metadata, clauseLocation);
    }

    /** 从排序项列表提取函数调用（GROUP BY 和 ORDER BY 共用逻辑）。 */
    private void extractFunctionCallsFromOrderByItems(
            final Collection<OrderByItemSegment> items,
            final OdpsSQLMetadata metadata,
            final String clauseLocation) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderByItemSegment item : items) {
            if (item instanceof ExpressionOrderByItemSegment) {
                ExpressionOrderByItemSegment exprItem = (ExpressionOrderByItemSegment) item;
                extractFunctionCallsFromExpression(
                        exprItem.getExpression(), metadata, clauseLocation);
            }
        }
    }

    /** 提取函数参数。 处理嵌套括号、字符串字面量等情况。 */
    private List<String> extractFunctionArguments(
            final String expressionText, final int functionStart) {
        List<String> arguments = new ArrayList<>();

        // 找到函数调用的开始位置（函数名后的左括号）
        int parenStart = expressionText.indexOf('(', functionStart);
        if (parenStart == -1) {
            return arguments;
        }

        // 提取括号内的内容，处理嵌套括号和字符串
        int depth = 0;
        int start = parenStart + 1;
        StringBuilder currentArg = new StringBuilder();
        boolean inString = false;
        char stringDelimiter = 0; // 字符串分隔符（单引号或双引号）
        boolean escaped = false; // 转义字符标志

        for (int i = start; i < expressionText.length(); i++) {
            char c = expressionText.charAt(i);

            if (escaped) {
                currentArg.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                currentArg.append(c);
                continue;
            }

            if (inString) {
                currentArg.append(c);
                if (c == stringDelimiter) {
                    inString = false;
                    stringDelimiter = 0;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                inString = true;
                stringDelimiter = c;
                currentArg.append(c);
                continue;
            }

            if (c == '(') {
                depth++;
                currentArg.append(c);
            } else if (c == ')') {
                if (depth == 0) {
                    // 找到匹配的右括号
                    if (currentArg.length() > 0) {
                        arguments.add(currentArg.toString().trim());
                    }
                    break;
                }
                depth--;
                currentArg.append(c);
            } else if (c == ',' && depth == 0) {
                // 参数分隔符
                if (currentArg.length() > 0) {
                    arguments.add(currentArg.toString().trim());
                    currentArg = new StringBuilder();
                }
            } else {
                currentArg.append(c);
            }
        }

        return arguments;
    }

    /** 识别函数类型。 使用 Set 查找，性能优于正则表达式匹配。 */
    private FunctionType identifyFunctionType(
            final String upperFunctionName,
            final boolean isAggregate,
            final boolean isWindowFunction) {
        if (null == upperFunctionName || upperFunctionName.isEmpty()) {
            return FunctionType.OTHER;
        }

        if (isWindowFunction) {
            return FunctionType.WINDOW;
        }

        if (isAggregate) {
            return FunctionType.AGGREGATE;
        }

        // 使用 Set 查找，性能优于正则表达式
        if (STRING_FUNCTIONS.contains(upperFunctionName)) {
            return FunctionType.STRING;
        }

        if (MATH_FUNCTIONS.contains(upperFunctionName)) {
            return FunctionType.MATH;
        }

        if (DATE_TIME_FUNCTIONS.contains(upperFunctionName)) {
            return FunctionType.DATE_TIME;
        }

        if (CAST_FUNCTIONS.contains(upperFunctionName)) {
            return FunctionType.CAST;
        }

        if (CONDITIONAL_FUNCTIONS.contains(upperFunctionName)) {
            return FunctionType.CONDITIONAL;
        }

        return FunctionType.OTHER;
    }
}
