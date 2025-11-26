package com.sea.odps.service.lineage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.OdpsSQLMetadataExtractor;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.join.JoinRelation;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/**
 * 数据血缘提取器，专门负责从 SQL 解析结果和元数据服务中提取血缘关系。
 *
 * <p>与 {@link OdpsSQLMetadataExtractor} 的区别：
 *
 * <ul>
 *   <li><b>OdpsSQLMetadataExtractor</b>：纯 SQL 文本解析，从 SQL AST 中提取结构化信息（表引用、列引用等）， 不涉及外部元数据，可用于 SQL
 *       校验、格式化等场景。
 *   <li><b>LineageExtractor</b>：血缘关系提取，将 SQL 解析结果与元数据服务关联， 建立表血缘和字段血缘，需要访问外部元数据服务。
 * </ul>
 *
 * <p>设计原则：
 *
 * <ul>
 *   <li>SQL 解析层：只负责从 SQL 文本中提取信息，不依赖外部系统
 *   <li>血缘提取层：负责将 SQL 引用关联到真实元数据，依赖元数据服务
 *   <li>职责分离：各层可以独立使用和测试
 * </ul>
 */
public class LineageExtractor {

    private final MetadataConnector metadataConnector;
    private final Map<String, TableMeta> tableCache = new HashMap<>();
    private final Map<String, Map<String, ColumnMeta>> columnCache = new HashMap<>();

    public LineageExtractor(MetadataConnector metadataConnector) {
        this.metadataConnector = metadataConnector;
    }

    /**
     * 从 SQL 解析结果中提取血缘关系。
     *
     * <p>该方法将 SQL 解析层提取的表引用、列引用等信息，与元数据服务中的真实元数据关联， 建立完整的表血缘和字段血缘关系。
     *
     * @param sqlMetadata SQL 解析结果（由 OdpsSQLMetadataExtractor 提取）
     * @return 血缘关系结果
     * @throws MetadataException 当元数据服务不可用时抛出
     */
    public LineageResult extract(OdpsSQLMetadata sqlMetadata) throws MetadataException {
        if (sqlMetadata == null) {
            return new LineageResult(Collections.emptyList(), Collections.emptyList());
        }

        // 1. 提取表血缘：将 SQL 中的表引用关联到元数据服务中的表元数据
        List<TableLineage> tableLineages = extractTableLineage(sqlMetadata);

        // 2. 提取字段血缘：将 SQL 中的字段引用关联到元数据服务中的列元数据
        List<ColumnLineage> columnLineages = extractColumnLineage(sqlMetadata, tableLineages);

        return new LineageResult(tableLineages, columnLineages);
    }

    /**
     * 提取表血缘关系。
     *
     * <p>将 SQL 解析层提取的表引用（TableReference）关联到元数据服务中的表元数据（TableMeta）， 建立表血缘关系。
     */
    private List<TableLineage> extractTableLineage(OdpsSQLMetadata sqlMetadata)
            throws MetadataException {
        // 使用 Set 来去重，避免 JOIN 中的表被重复添加
        Set<String> processedTables = new HashSet<>();
        List<TableLineage> result = new ArrayList<>();

        // 处理所有表引用（来自 SQL 解析层）
        for (TableReference tableRef : sqlMetadata.getTables()) {
            if (tableRef.isSubquery()) {
                // 子查询表本身是临时的，没有真实元数据，跳过
                // 注意：子查询内部的表（如子查询中的 FROM 表）应该会被 SQL 解析层
                // 单独提取到 getTables() 中，所以这里跳过子查询表本身不会影响
                // 子查询内部表的血缘识别
                continue;
            }

            String key = buildTableKey(tableRef);
            if (processedTables.contains(key)) {
                continue; // 已经处理过，跳过
            }

            // 从元数据服务中查询表元数据
            TableMeta tableMeta = resolveTableMeta(tableRef);
            if (tableMeta != null) {
                TableLineage lineage =
                        new TableLineage(
                                tableRef,
                                tableMeta,
                                tableRef.getQualifiedName(),
                                tableRef.getAlias());
                result.add(lineage);
                processedTables.add(key);
            }
        }

        // 处理 JOIN 关系中的表（可能不在主表列表中）
        for (JoinRelation join : sqlMetadata.getJoins()) {
            TableReference leftRef = join.getLeft();
            TableReference rightRef = join.getRight();

            if (leftRef != null && !leftRef.isSubquery()) {
                String key = buildTableKey(leftRef);
                if (!processedTables.contains(key)) {
                    TableMeta leftMeta = resolveTableMeta(leftRef);
                    if (leftMeta != null) {
                        TableLineage lineage =
                                new TableLineage(
                                        leftRef,
                                        leftMeta,
                                        leftRef.getQualifiedName(),
                                        leftRef.getAlias());
                        result.add(lineage);
                        processedTables.add(key);
                    }
                }
            }

            if (rightRef != null && !rightRef.isSubquery()) {
                String key = buildTableKey(rightRef);
                if (!processedTables.contains(key)) {
                    TableMeta rightMeta = resolveTableMeta(rightRef);
                    if (rightMeta != null) {
                        TableLineage lineage =
                                new TableLineage(
                                        rightRef,
                                        rightMeta,
                                        rightRef.getQualifiedName(),
                                        rightRef.getAlias());
                        result.add(lineage);
                        processedTables.add(key);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 提取字段血缘关系。
     *
     * <p>将 SQL 解析层提取的字段元数据（FieldMetadata）中的列引用（ColumnReference），
     * 关联到元数据服务中的列元数据（ColumnMeta），建立字段血缘关系。
     *
     * <p>对于子查询场景（如 `SELECT sub.id FROM (SELECT id FROM ods.user_table) sub`）：
     *
     * <ul>
     *   <li>子查询表本身（`sub`）没有真实元数据，不会出现在表血缘中
     *   <li>但子查询内部的表（`ods.user_table`）会被 SQL 解析层提取到表列表中
     *   <li>子查询输出的字段（`sub.id`）的 sourceColumns 应该包含子查询内部的字段引用
     *   <li>子查询内部的字段引用（`id`）应该能够追溯到原始表（`ods.user_table.id`）
     *   <li>因此，`sub.id` 最终能够追溯到 `ods.user_table.id`
     * </ul>
     */
    private List<ColumnLineage> extractColumnLineage(
            OdpsSQLMetadata sqlMetadata, List<TableLineage> tableLineages)
            throws MetadataException {

        // 构建辅助数据结构
        ColumnLineageContext context = buildColumnLineageContext(sqlMetadata, tableLineages);
        List<ColumnLineage> result = new ArrayList<>();

        // 处理 SELECT 字段（来自 SQL 解析层）
        for (FieldMetadata field : sqlMetadata.getFields()) {
            if (field.getScopeAlias() != null && !field.getScopeAlias().isEmpty()) {
                // 子查询作用域字段仅用于辅助解析，不参与最终血缘输出
                continue;
            }

            List<ColumnLineage> fieldLineages =
                    processField(field, sqlMetadata, tableLineages, context);
            result.addAll(fieldLineages);
        }

        // 标记中间字段：找出被其他字段引用的字段
        markIntermediateFields(result);
        return result;
    }

    /** 构建字段血缘提取的辅助数据结构。 */
    private ColumnLineageContext buildColumnLineageContext(
            OdpsSQLMetadata sqlMetadata, List<TableLineage> tableLineages) {

        // 构建表血缘映射（通过 buildTableKey 查找）
        Map<String, TableLineage> tableLineageMap = new HashMap<>();
        // 构建表引用到表血缘的直接映射（用于 O(1) 查找）
        Map<TableReference, TableLineage> tableRefToLineageMap = new HashMap<>();
        // 构建表别名到表血缘的映射（用于通过别名快速查找）
        Map<String, TableLineage> aliasToLineageMap = new HashMap<>();
        // 构建表名到表血缘的映射（用于通过表名快速查找）
        Map<String, TableLineage> nameToLineageMap = new HashMap<>();

        for (TableLineage lineage : tableLineages) {
            TableReference tableRef = lineage.getTableRef();
            // 通过 buildTableKey 查找的映射
            tableLineageMap.put(buildTableKey(tableRef), lineage);
            // 通过 TableReference.equals 查找的映射（O(1) 性能）
            tableRefToLineageMap.put(tableRef, lineage);

            // 通过别名查找的映射
            String alias = tableRef.getAlias();
            if (alias != null && !alias.isEmpty()) {
                // 如果别名冲突，保留第一个（通常不会发生）
                aliasToLineageMap.putIfAbsent(alias, lineage);
            }

            // 通过表名查找的映射
            String name = tableRef.getName();
            if (name != null && !name.isEmpty()) {
                // 如果表名冲突，保留第一个（通常不会发生）
                nameToLineageMap.putIfAbsent(name, lineage);
            }
        }

        // 获取子查询表引用映射（从 parser 中获取，避免重复遍历）
        Map<String, TableReference> subqueryTableMap = sqlMetadata.getSubqueryTableMap();

        // 构建所有子查询内部表的别名集合（使用精确的作用域标记）
        // 从 parser 中获取子查询与其内部表的映射，然后合并所有子查询的内部表
        Set<String> subqueryInnerTableAliases = new HashSet<>();
        Map<String, Set<String>> subqueryInnerTablesMap = sqlMetadata.getSubqueryInnerTablesMap();
        for (Set<String> innerTables : subqueryInnerTablesMap.values()) {
            subqueryInnerTableAliases.addAll(innerTables);
        }

        // 获取外层查询表别名集合（从 parser 中获取，避免重复遍历）
        Set<String> outerQueryTableAliases = sqlMetadata.getOuterQueryTableAliases();

        // 获取按作用域分组的字段映射（从 parser 中获取，避免重复遍历）
        Map<String, List<FieldMetadata>> fieldsByScope = sqlMetadata.getFieldsByScope();

        return new ColumnLineageContext(
                tableLineageMap,
                tableRefToLineageMap,
                aliasToLineageMap,
                nameToLineageMap,
                subqueryTableMap,
                subqueryInnerTableAliases,
                outerQueryTableAliases,
                fieldsByScope);
    }

    /** 处理单个字段，返回该字段的所有 ColumnLineage。 */
    private List<ColumnLineage> processField(
            FieldMetadata field,
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages,
            ColumnLineageContext context)
            throws MetadataException {

        String targetField = field.getAlias() != null ? field.getAlias() : field.getExpression();
        String expression = field.getExpression();

        // 检查是否是通配符字段
        WildcardInfo wildcardInfo = detectWildcard(field, expression, targetField);

        if (wildcardInfo.isWildcard) {
            return processWildcardField(field, wildcardInfo, sqlMetadata, tableLineages, context);
        } else {
            return processNormalField(field, targetField, sqlMetadata, context);
        }
    }

    /** 检测字段是否是通配符字段。 */
    private WildcardInfo detectWildcard(
            FieldMetadata field, String expression, String targetField) {
        boolean isWildcard = "*".equals(expression) || "*".equals(targetField);
        String wildcardTableAlias = null;

        if (!isWildcard && !field.getSourceColumns().isEmpty()) {
            for (ColumnReference colRef : field.getSourceColumns()) {
                if ("*".equals(colRef.getName())) {
                    isWildcard = true;
                    if (colRef.getOwner() != null && !colRef.getOwner().isEmpty()) {
                        wildcardTableAlias = colRef.getOwner();
                    }
                    break;
                }
            }
        } else if (isWildcard && !field.getSourceColumns().isEmpty()) {
            for (ColumnReference colRef : field.getSourceColumns()) {
                if ("*".equals(colRef.getName())
                        && colRef.getOwner() != null
                        && !colRef.getOwner().isEmpty()) {
                    wildcardTableAlias = colRef.getOwner();
                    break;
                }
            }
        }

        return new WildcardInfo(isWildcard, wildcardTableAlias);
    }

    /** 处理通配符字段（SELECT * 或 SELECT t1.*）。 */
    private List<ColumnLineage> processWildcardField(
            FieldMetadata field,
            WildcardInfo wildcardInfo,
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages,
            ColumnLineageContext context)
            throws MetadataException {

        if (wildcardInfo.tableAlias != null) {
            // SELECT t1.* 或 SELECT t2.* 的情况
            if (context.subqueryTableMap.containsKey(wildcardInfo.tableAlias)) {
                // 子查询别名（如 t2.*）
                return processSubqueryWildcardField(
                        wildcardInfo.tableAlias, sqlMetadata, tableLineages, context);
            } else {
                // 普通表别名（如 t1.*）
                return processTableWildcardField(wildcardInfo.tableAlias, tableLineages);
            }
        } else {
            // SELECT * 的情况
            return processUnqualifiedWildcardField(field, tableLineages, context);
        }
    }

    /** 处理子查询通配符字段（如 SELECT t2.*）。 */
    private List<ColumnLineage> processSubqueryWildcardField(
            String subqueryAlias,
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages,
            ColumnLineageContext context)
            throws MetadataException {

        List<FieldMetadata> subqueryFields =
                context.fieldsByScope.getOrDefault(subqueryAlias, Collections.emptyList());

        // 收集所有子查询字段的来源列
        Set<ColumnSource> allSources = new HashSet<>();
        for (FieldMetadata innerField : subqueryFields) {
            Set<ColumnSource> innerFieldSources =
                    collectSubqueryFieldSources(innerField, sqlMetadata, tableLineages, context);
            allSources.addAll(innerFieldSources);
        }

        // 创建一个 t2.* 的 ColumnLineage，包含所有子查询字段的来源
        // 与 processTableWildcardField 的行为保持一致
        if (!allSources.isEmpty()) {
            List<ColumnLineage> result = new ArrayList<>();
            result.add(
                    new ColumnLineage(
                            subqueryAlias + ".*",
                            subqueryAlias + ".*",
                            "DIRECT",
                            new ArrayList<>(allSources)));
            return result;
        }

        return Collections.emptyList();
    }

    /** 处理子查询内部的单个字段。 */
    private List<ColumnLineage> processSubqueryInnerField(
            FieldMetadata innerField,
            String subqueryAlias,
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages,
            ColumnLineageContext context)
            throws MetadataException {

        String innerFieldName =
                innerField.getAlias() != null ? innerField.getAlias() : innerField.getExpression();
        String innerFieldExpression = innerField.getExpression();

        // 收集字段的来源列
        Set<ColumnSource> innerFieldSources =
                collectSubqueryFieldSources(innerField, sqlMetadata, tableLineages, context);

        // 判断字段类型
        boolean isSimpleColumnRef =
                LineageFieldResolver.isSimpleColumnReference(
                        innerField, innerFieldExpression, innerFieldName);
        boolean isExpressionField =
                LineageFieldResolver.isExpressionField(
                        innerField, innerFieldExpression, innerFieldName);

        List<ColumnLineage> result = new ArrayList<>();

        if (isSimpleColumnRef
                || (innerFieldExpression != null && innerFieldExpression.equals(innerFieldName))) {
            // 简单列引用
            if (!innerFieldSources.isEmpty()) {
                result.add(
                        createSimpleColumnLineage(
                                subqueryAlias,
                                innerFieldName,
                                innerFieldExpression,
                                innerField,
                                innerFieldSources));
            }
        } else if (isExpressionField) {
            // 表达式字段，需要创建两层 ColumnLineage
            if (innerFieldSources.isEmpty() && !innerField.getSourceColumns().isEmpty()) {
                // Fallback: 尝试从 sourceColumns 中直接解析
                innerFieldSources =
                        fallbackResolveSubqueryFieldSources(innerField, tableLineages, context);
            }
            result.addAll(
                    createExpressionFieldLineages(
                            subqueryAlias,
                            innerFieldName,
                            innerFieldExpression,
                            innerField,
                            innerFieldSources));
        } else if (!innerFieldSources.isEmpty()) {
            // 其他情况
            result.add(
                    createSimpleColumnLineage(
                            subqueryAlias,
                            innerFieldName,
                            innerFieldExpression,
                            innerField,
                            innerFieldSources));
        }

        return result;
    }

    /** 收集子查询字段的来源列。 */
    private Set<ColumnSource> collectSubqueryFieldSources(
            FieldMetadata innerField,
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages,
            ColumnLineageContext context)
            throws MetadataException {

        Set<ColumnSource> sources = new HashSet<>();
        for (ColumnReference colRef : innerField.getSourceColumns()) {
            ColumnSource source =
                    resolveColumnSource(
                            colRef, sqlMetadata, context, true, context.subqueryInnerTableAliases);
            if (source != null
                    && source.getTableQualifiedName() != null
                    && !source.getTableQualifiedName().equals("UNKNOWN")) {
                sources.add(source);
            }
        }
        return sources;
    }

    /** Fallback: 尝试从 sourceColumns 中直接解析子查询字段的来源列。 */
    private Set<ColumnSource> fallbackResolveSubqueryFieldSources(
            FieldMetadata innerField,
            List<TableLineage> tableLineages,
            ColumnLineageContext context) {

        Set<ColumnSource> sources = new HashSet<>();
        for (ColumnReference colRef : innerField.getSourceColumns()) {
            String owner = colRef.getOwner();
            String columnName = colRef.getName();
            if (columnName == null || columnName.isEmpty() || "*".equals(columnName)) {
                continue;
            }

            // 在子查询内部的表中查找该列
            for (TableLineage tableLineage : tableLineages) {
                TableReference tableRef = tableLineage.getTableRef();
                if (tableRef.isSubquery()) {
                    continue;
                }
                String tableAlias = tableRef.getAlias();
                if (tableAlias != null && context.outerQueryTableAliases.contains(tableAlias)) {
                    continue;
                }

                // 匹配表别名或表名
                if (owner == null
                        || owner.isEmpty()
                        || (tableAlias != null && owner.equals(tableAlias))
                        || owner.equals(tableRef.getName())) {
                    TableMeta tableMeta = tableLineage.getTableMeta();
                    if (tableMeta != null) {
                        ColumnMeta columnMeta = resolveColumnMeta(tableMeta, columnName);
                        if (columnMeta != null) {
                            String qualifiedName =
                                    LineageFieldResolver.getTableQualifiedName(
                                            tableLineage, tableMeta);
                            sources.add(
                                    new ColumnSource(
                                            qualifiedName,
                                            tableRef.getAlias(),
                                            columnName,
                                            columnMeta));
                            break;
                        }
                    }
                }
            }
        }
        return sources;
    }

    /** 创建简单列引用的 ColumnLineage。 */
    private ColumnLineage createSimpleColumnLineage(
            String prefix,
            String fieldName,
            String expression,
            FieldMetadata field,
            Set<ColumnSource> sources) {
        return new ColumnLineage(
                prefix + "." + fieldName,
                expression != null ? expression : fieldName,
                field.getCategory() != null ? field.getCategory().name() : "DIRECT",
                new ArrayList<>(sources));
    }

    /** 创建表达式字段的两层 ColumnLineage。 */
    private List<ColumnLineage> createExpressionFieldLineages(
            String prefix,
            String fieldName,
            String expression,
            FieldMetadata field,
            Set<ColumnSource> sources) {

        List<ColumnLineage> result = new ArrayList<>();

        // 1. 表达式层（中间字段）
        ColumnLineage expressionLineage =
                new ColumnLineage(
                        prefix + "." + expression,
                        expression,
                        "EXPRESSION",
                        sources.isEmpty() ? Collections.emptyList() : new ArrayList<>(sources),
                        false); // 中间字段
        result.add(expressionLineage);

        // 2. 字段别名层（最终输出字段）
        ColumnSource expressionSource = new ColumnSource(prefix, null, expression, null);
        ColumnLineage fieldLineage =
                new ColumnLineage(
                        prefix + "." + fieldName,
                        expression,
                        field.getCategory() != null ? field.getCategory().name() : "DIRECT",
                        Collections.singletonList(expressionSource),
                        true); // 最终输出字段
        result.add(fieldLineage);

        return result;
    }

    /** 处理普通表通配符字段（如 SELECT t1.*）。 */
    private List<ColumnLineage> processTableWildcardField(
            String tableAlias, List<TableLineage> tableLineages) {

        List<ColumnLineage> result = new ArrayList<>();
        for (TableLineage tableLineage : tableLineages) {
            String alias = tableLineage.getTableRef().getAlias();
            String name = tableLineage.getTableRef().getName();
            if (tableAlias.equals(alias) || tableAlias.equals(name)) {
                TableMeta tableMeta = tableLineage.getTableMeta();
                if (tableMeta != null && tableMeta.getColumns() != null) {
                    Set<ColumnSource> sources = new HashSet<>();
                    for (ColumnMeta columnMeta : tableMeta.getColumns()) {
                        sources.add(
                                new ColumnSource(
                                        tableLineage.getTableRef().getQualifiedName(),
                                        tableLineage.getTableRef().getAlias(),
                                        columnMeta.getName(),
                                        columnMeta));
                    }
                    if (!sources.isEmpty()) {
                        result.add(
                                new ColumnLineage(
                                        tableAlias + ".*",
                                        tableAlias + ".*",
                                        "DIRECT",
                                        new ArrayList<>(sources)));
                    }
                }
                break;
            }
        }
        return result;
    }

    /** 处理未限定的通配符字段（SELECT *）。 */
    private List<ColumnLineage> processUnqualifiedWildcardField(
            FieldMetadata field, List<TableLineage> tableLineages, ColumnLineageContext context) {

        // 判断字段是否属于子查询内部
        boolean isSubqueryInnerField = isSubqueryInnerField(field, context);

        List<ColumnLineage> result = new ArrayList<>();
        for (TableLineage tableLineage : tableLineages) {
            TableReference tableRef = tableLineage.getTableRef();
            if (tableRef.isSubquery()) {
                continue;
            }
            String tableAlias = tableRef.getAlias();
            boolean isOuterQueryTable =
                    tableAlias == null || context.outerQueryTableAliases.contains(tableAlias);

            if (isSubqueryInnerField == !isOuterQueryTable) {
                // 匹配：子查询内部字段对应子查询内部的表，外层查询字段对应外层查询的表
                Set<ColumnSource> sources = collectTableColumns(tableLineage);
                if (!sources.isEmpty()) {
                    result.add(new ColumnLineage("*", "*", "DIRECT", new ArrayList<>(sources)));
                }
            }
        }
        return result;
    }

    /**
     * 判断字段是否属于子查询内部。
     *
     * <p>该判断依赖 sourceColumns 中记录的 owner 与预先收集的别名集合进行启发式推断， 并结合 subqueryTableMap 来识别外层子查询别名。
     */
    private boolean isSubqueryInnerField(FieldMetadata field, ColumnLineageContext context) {
        if (!field.getSourceColumns().isEmpty()) {
            for (ColumnReference colRef : field.getSourceColumns()) {
                String owner = colRef.getOwner();
                if (owner != null) {
                    if (context.subqueryInnerTableAliases.contains(owner)) {
                        return true;
                    }
                    if (context.subqueryTableMap.containsKey(owner)) {
                        return false; // 外层查询字段
                    }
                }
            }
        }
        return false;
    }

    /** 收集表的所有列作为 ColumnSource。 */
    private Set<ColumnSource> collectTableColumns(TableLineage tableLineage) {
        Set<ColumnSource> sources = new HashSet<>();
        TableMeta tableMeta = tableLineage.getTableMeta();
        if (tableMeta != null && tableMeta.getColumns() != null) {
            for (ColumnMeta columnMeta : tableMeta.getColumns()) {
                sources.add(
                        new ColumnSource(
                                tableLineage.getTableRef().getQualifiedName(),
                                tableLineage.getTableRef().getAlias(),
                                columnMeta.getName(),
                                columnMeta));
            }
        }
        return sources;
    }

    /** 处理普通字段（非通配符）。 */
    private List<ColumnLineage> processNormalField(
            FieldMetadata field,
            String targetField,
            OdpsSQLMetadata sqlMetadata,
            ColumnLineageContext context)
            throws MetadataException {

        Set<ColumnSource> sources = new HashSet<>();
        for (ColumnReference colRef : field.getSourceColumns()) {
            ColumnSource source = resolveColumnSource(colRef, sqlMetadata, context);
            if (source != null) {
                sources.add(source);
            }
        }

        if (sources.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new ColumnLineage(
                        targetField,
                        field.getExpression(),
                        field.getCategory().name(),
                        new ArrayList<>(sources)));
    }

    /**
     * 标记中间字段。
     *
     * <p>如果一个字段被其他字段引用作为来源，则它是中间字段。 由于 ColumnLineage 是不可变的，需要重新构建列表。
     */
    private void markIntermediateFields(List<ColumnLineage> columnLineages) {
        if (columnLineages == null || columnLineages.isEmpty()) {
            return;
        }

        // 构建字段名到 ColumnLineage 的映射
        Map<String, ColumnLineage> fieldMap = new HashMap<>();
        for (ColumnLineage lineage : columnLineages) {
            fieldMap.put(lineage.getTargetField(), lineage);
        }

        // 找出被引用的中间字段
        Set<String> referencedFields = new HashSet<>();
        for (ColumnLineage lineage : columnLineages) {
            for (ColumnSource source : lineage.getSources()) {
                ColumnLineage intermediateLineage =
                        LineageFieldResolver.findIntermediateLineage(
                                source, fieldMap, lineage.getTargetField());
                if (intermediateLineage != null) {
                    referencedFields.add(intermediateLineage.getTargetField());
                }
            }
        }

        // 重新构建列表，将中间字段标记为 false
        // 注意：由于 ColumnLineage 是不可变的，我们需要替换列表中的元素
        // 但由于列表是 ArrayList，我们可以通过索引替换
        for (int i = 0; i < columnLineages.size(); i++) {
            ColumnLineage lineage = columnLineages.get(i);
            if (referencedFields.contains(lineage.getTargetField()) && lineage.isFinalOutput()) {
                // 这是中间字段，需要重新创建并替换
                ColumnLineage intermediateLineage =
                        new ColumnLineage(
                                lineage.getTargetField(),
                                lineage.getExpression(),
                                lineage.getCategory(),
                                lineage.getSources(),
                                false // 标记为中间字段
                                );
                columnLineages.set(i, intermediateLineage);
            }
        }
    }

    /**
     * 从元数据服务中解析表元数据。
     *
     * <p>将 SQL 解析层提取的表引用（TableReference）关联到元数据服务中的表元数据（TableMeta）。
     */
    private TableMeta resolveTableMeta(TableReference tableRef) throws MetadataException {
        String qualifiedName = tableRef.getQualifiedName();
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return null;
        }

        // 检查缓存
        if (tableCache.containsKey(qualifiedName)) {
            return tableCache.get(qualifiedName);
        }

        // 解析数据库名和表名
        String[] parts = qualifiedName.split("\\.", 2);
        String databaseName;
        String tableName;

        if (parts.length == 2) {
            databaseName = parts[0];
            tableName = parts[1];
        } else {
            // 如果没有数据库名，尝试从表引用的 owner 获取
            databaseName = tableRef.getOwner();
            tableName = parts[0];
        }

        if (databaseName == null || databaseName.isEmpty()) {
            // 尝试从连接器获取默认数据库
            List<MetadataConnector.DatabaseMeta> databases = metadataConnector.listDatabases();
            if (databases.isEmpty()) {
                return null;
            }
            databaseName = databases.get(0).getName();
        }

        // 直接获取指定表元数据，避免查询所有表
        TableMeta table = metadataConnector.getTable(databaseName, tableName);
        if (table != null) {
            // 缓存结果
            tableCache.put(qualifiedName, table);
            return table;
        }

        return null;
    }

    /**
     * 解析列源信息。
     *
     * <p>将 SQL 解析层提取的列引用（ColumnReference）关联到元数据服务中的列元数据（ColumnMeta）。
     *
     * <p>对于子查询别名的列引用（如 `t3.name`），会查找子查询内部对应的字段， 并使用该字段的 sourceColumns 递归解析列源。
     *
     * @param colRef 列引用
     * @param sqlMetadata SQL 元数据
     * @param context 字段血缘提取上下文
     * @param restrictToSubqueryInnerTables 如果为 true，会跳过推断为外层查询的表，只在可能属于子查询的表中搜索
     *     （用于处理子查询字段的列引用，属于启发式过滤而非严格的作用域判定）
     * @param subqueryInnerTableAliases 子查询内部表别名集合（可选，用于精确限制搜索范围）
     */
    private ColumnSource resolveColumnSource(
            ColumnReference colRef,
            OdpsSQLMetadata sqlMetadata,
            ColumnLineageContext context,
            boolean restrictToSubqueryInnerTables,
            Set<String> subqueryInnerTableAliases)
            throws MetadataException {

        String owner = colRef.getOwner();
        String columnName = colRef.getName();

        if (columnName == null || columnName.isEmpty()) {
            return null;
        }

        // 优先使用 ColumnReference 中的表引用（如果存在）
        // 这样可以避免重复查找，提升性能
        if (colRef.getTableReference().isPresent()) {
            TableReference tableRef = colRef.getTableReference().get();

            // 如果是子查询，需要特殊处理
            if (tableRef.isSubquery()) {
                String subqueryAlias = tableRef.getAlias();
                if (subqueryAlias != null) {
                    return resolveSubqueryColumnSource(
                            subqueryAlias, columnName, sqlMetadata, context);
                }
            }

            // 使用 O(1) 查找对应的 TableLineage
            TableLineage lineage = context.tableRefToLineageMap.get(tableRef);
            if (lineage != null) {
                ColumnMeta columnMeta = resolveColumnMeta(lineage.getTableMeta(), columnName);
                if (columnMeta != null) {
                    return new ColumnSource(
                            lineage.getTableRef().getQualifiedName(),
                            lineage.getTableRef().getAlias(),
                            columnName,
                            columnMeta);
                }
            }
        }

        // 如果没有表引用，使用优化的查找逻辑（向后兼容）
        // 如果列引用有 owner（表别名或表名），尝试匹配表血缘
        if (owner != null && !owner.isEmpty()) {
            // 先检查是否是子查询别名
            if (context.subqueryTableMap.containsKey(owner)) {
                // 这是子查询别名的列引用，需要查找子查询内部对应的字段
                return resolveSubqueryColumnSource(owner, columnName, sqlMetadata, context);
            }

            // 优先通过别名查找（O(1) 性能）
            TableLineage lineage = context.aliasToLineageMap.get(owner);
            if (lineage == null) {
                // 如果别名查找失败，尝试通过表名查找（O(1) 性能）
                lineage = context.nameToLineageMap.get(owner);
            }

            if (lineage != null) {
                ColumnMeta columnMeta = resolveColumnMeta(lineage.getTableMeta(), columnName);
                if (columnMeta != null) {
                    return new ColumnSource(
                            lineage.getTableRef().getQualifiedName(),
                            lineage.getTableRef().getAlias(),
                            columnName,
                            columnMeta);
                }
            }
        } else {
            // 没有 owner，尝试在表中查找该列
            // 如果 restrictToSubqueryInnerTables 为 true，只搜索子查询内部的表
            Set<String> outerQueryTableAliases = context.outerQueryTableAliases;

            // 如果提供了子查询内部表别名集合，优先在这些表中搜索
            if (restrictToSubqueryInnerTables
                    && subqueryInnerTableAliases != null
                    && !subqueryInnerTableAliases.isEmpty()) {
                // 遍历所有表血缘，检查是否属于子查询内部表
                for (TableLineage lineage : context.tableLineageMap.values()) {
                    TableReference tableRef = lineage.getTableRef();
                    if (tableRef.isSubquery()) {
                        continue; // 跳过子查询表本身
                    }
                    // 检查表的别名、限定名或表名是否在子查询内部表集合中
                    String tableAlias = tableRef.getAlias();
                    String qualifiedName = tableRef.getQualifiedName();
                    String tableName = tableRef.getName();
                    boolean isSubqueryInnerTable =
                            (tableAlias != null && subqueryInnerTableAliases.contains(tableAlias))
                                    || (qualifiedName != null
                                            && subqueryInnerTableAliases.contains(qualifiedName))
                                    || (tableName != null
                                            && subqueryInnerTableAliases.contains(tableName));
                    if (isSubqueryInnerTable) {
                        ColumnMeta columnMeta =
                                resolveColumnMeta(lineage.getTableMeta(), columnName);
                        if (columnMeta != null) {
                            return new ColumnSource(
                                    lineage.getTableRef().getQualifiedName(),
                                    lineage.getTableRef().getAlias(),
                                    columnName,
                                    columnMeta);
                        }
                    }
                }
            } else {
                // 遍历所有表（需要过滤的情况）
                for (TableLineage lineage : context.tableLineageMap.values()) {
                    TableReference tableRef = lineage.getTableRef();
                    // 如果限制在子查询内部表，跳过外层查询的表
                    if (restrictToSubqueryInnerTables) {
                        if (tableRef.isSubquery()) {
                            continue; // 跳过子查询表本身
                        }
                        String tableAlias = tableRef.getAlias();
                        // 如果没有提供子查询内部表别名集合，使用通用逻辑：跳过外层查询的表
                        if (tableAlias != null && outerQueryTableAliases.contains(tableAlias)) {
                            continue; // 这是外层查询的表，跳过
                        }
                    }

                    ColumnMeta columnMeta = resolveColumnMeta(lineage.getTableMeta(), columnName);
                    if (columnMeta != null) {
                        return new ColumnSource(
                                lineage.getTableRef().getQualifiedName(),
                                lineage.getTableRef().getAlias(),
                                columnName,
                                columnMeta);
                    }
                }
            }
        }

        // 如果找不到，返回基础信息（不包含列元数据）
        return new ColumnSource(owner != null ? owner : "UNKNOWN", null, columnName, null);
    }

    /** 解析列源信息（重载方法，默认不限制搜索范围）。 */
    private ColumnSource resolveColumnSource(
            ColumnReference colRef, OdpsSQLMetadata sqlMetadata, ColumnLineageContext context)
            throws MetadataException {
        return resolveColumnSource(colRef, sqlMetadata, context, false, null);
    }

    /**
     * 解析子查询别名的列源信息。
     *
     * <p>当遇到子查询别名的列引用（如 `t3.name`）时，查找子查询内部对应的字段， 并使用该字段的 sourceColumns 递归解析列源。
     *
     * @param subqueryAlias 子查询别名（如 `t3`）
     * @param columnName 列名（如 `name`）
     * @param sqlMetadata SQL 元数据
     * @param context 字段血缘提取上下文
     * @return 列源信息，如果找不到则返回 null
     */
    private ColumnSource resolveSubqueryColumnSource(
            String subqueryAlias,
            String columnName,
            OdpsSQLMetadata sqlMetadata,
            ColumnLineageContext context)
            throws MetadataException {

        // 获取指定子查询的内部表别名集合（使用精确的作用域标记）
        Set<String> subqueryInnerTableAliases =
                sqlMetadata.getSubqueryInnerTableAliases(subqueryAlias);

        // 优先使用预先聚合的作用域字段，避免重复扫描所有字段
        ColumnSource scopedFieldSource =
                resolveColumnFromScopedFields(
                        context.fieldsByScope.get(subqueryAlias),
                        columnName,
                        sqlMetadata,
                        context,
                        subqueryInnerTableAliases);
        if (scopedFieldSource != null) {
            return scopedFieldSource;
        }

        // 查找子查询内部对应的字段
        // 子查询输出的字段可能是：
        // 1. 直接列引用（如 `SELECT t1.name FROM ...`），此时 expression 是 `t1.name`，alias 可能是 `name`
        // 2. 带别名的列（如 `SELECT t1.name AS name FROM ...`），此时 expression 是 `t1.name`，alias 是 `name`
        // 3. 表达式（如 `SELECT t1.name + 1 AS name FROM ...`），此时 expression 是表达式，alias 是 `name`
        // 我们需要匹配 alias 或 expression 中的列名

        // Fallback: 如果通过作用域字段没有找到，尝试从所有字段中查找
        // 优先使用子查询作用域的字段（已在上面处理），这里作为兜底逻辑
        // 使用 getSubqueryInnerTableAliases 来精确判断字段是否属于子查询内部
        for (FieldMetadata field : sqlMetadata.getFields()) {
            // 检查字段是否属于子查询内部：字段的 sourceColumns 中的列引用的 owner 应该是子查询内部的表别名
            boolean isSubqueryInnerField = false;
            boolean hasOuterSubqueryReference = false;

            for (ColumnReference colRef : field.getSourceColumns()) {
                String owner = colRef.getOwner();
                if (owner != null) {
                    if (subqueryInnerTableAliases.contains(owner)) {
                        // 字段引用了子查询内部的表，属于子查询内部字段
                        isSubqueryInnerField = true;
                    } else if (context.subqueryTableMap.containsKey(owner)) {
                        // 字段引用了子查询别名（如 t3），这是外层查询的字段
                        hasOuterSubqueryReference = true;
                    }
                }
            }

            // 如果字段引用了外层子查询别名，跳过（这是外层查询的字段）
            if (hasOuterSubqueryReference) {
                continue;
            }

            // 如果字段有 sourceColumns 但不属于子查询内部，跳过
            // 如果字段没有 sourceColumns（如常量字段），我们也考虑它，因为子查询可能输出常量字段
            if (!field.getSourceColumns().isEmpty() && !isSubqueryInnerField) {
                continue;
            }

            if (!LineageFieldResolver.matchesColumnName(field, columnName)) {
                continue;
            }

            ColumnSource source =
                    resolveFirstFieldSource(
                            field, sqlMetadata, context, true, subqueryInnerTableAliases);
            if (source != null) {
                return source;
            }
        }

        // 如果找不到匹配的字段，返回 null
        return null;
    }

    private ColumnSource resolveColumnFromScopedFields(
            List<FieldMetadata> scopedFields,
            String columnName,
            OdpsSQLMetadata sqlMetadata,
            ColumnLineageContext context,
            Set<String> subqueryInnerTableAliases)
            throws MetadataException {

        if (scopedFields == null || scopedFields.isEmpty()) {
            return null;
        }

        for (FieldMetadata field : scopedFields) {
            if (!LineageFieldResolver.matchesColumnName(field, columnName)) {
                continue;
            }
            ColumnSource source =
                    resolveFirstFieldSource(
                            field, sqlMetadata, context, true, subqueryInnerTableAliases);
            if (source != null) {
                return source;
            }
        }
        return null;
    }

    /**
     * 获取字段解析出的首个列源；用于子查询字段匹配时复用。
     *
     * @param restrictToSubqueryInnerTables 如果为 true，只搜索子查询内部的表
     * @param subqueryInnerTableAliases 子查询内部表别名集合（可选）
     */
    private ColumnSource resolveFirstFieldSource(
            FieldMetadata field,
            OdpsSQLMetadata sqlMetadata,
            ColumnLineageContext context,
            boolean restrictToSubqueryInnerTables,
            Set<String> subqueryInnerTableAliases)
            throws MetadataException {

        if (field.getSourceColumns().isEmpty()) {
            return null;
        }

        for (ColumnReference colRef : field.getSourceColumns()) {
            ColumnSource source =
                    resolveColumnSource(
                            colRef,
                            sqlMetadata,
                            context,
                            restrictToSubqueryInnerTables,
                            subqueryInnerTableAliases);
            if (source != null) {
                return source;
            }
        }
        return null;
    }

    /** 从表元数据中解析列元数据。 */
    private ColumnMeta resolveColumnMeta(TableMeta tableMeta, String columnName) {
        return LineageFieldResolver.resolveColumnMeta(tableMeta, columnName, columnCache);
    }

    /** 构建表键（用于缓存和匹配）。 */
    private String buildTableKey(TableReference tableRef) {
        String qualifiedName = tableRef.getQualifiedName();
        if (qualifiedName != null && !qualifiedName.isEmpty()) {
            return qualifiedName;
        }
        String alias = tableRef.getAlias();
        if (alias != null && !alias.isEmpty()) {
            return alias;
        }
        return tableRef.getName();
    }
}
