package com.sea.odps.service.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.lineage.ColumnLineage;
import com.sea.odps.service.lineage.ColumnSource;
import com.sea.odps.service.lineage.LineageExtractor;
import com.sea.odps.service.lineage.LineageResult;
import com.sea.odps.service.lineage.TableLineage;
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;

/**
 * 血缘关系可视化器，用于将 SQL 血缘关系格式化为可读的文本输出。
 *
 * <p>使用示例：
 *
 * <pre>
 * MetadataConnector connector = new ExcelMetadataConnector("path/to/metadata.xlsx");
 * LineageVisualizer visualizer = new LineageVisualizer(connector);
 * visualizer.visualize("SELECT t1.id, t1.name FROM ods.user_table t1;");
 * </pre>
 */
public class LineageVisualizer {

    private static final int MAX_DEPTH = 10;

    private final LineageExtractor extractor;
    private final OdpsSQLMetadataEntrypoint validator;

    public LineageVisualizer(MetadataConnector metadataConnector) {
        this.extractor = new LineageExtractor(metadataConnector);
        this.validator = new OdpsSQLMetadataEntrypoint();
    }

    /**
     * 可视化 SQL 的血缘关系。
     *
     * @param sql SQL 语句
     * @throws MetadataException 当元数据服务不可用时抛出
     */
    public void visualize(String sql) throws MetadataException {
        System.out.println(repeat("=", 80));
        System.out.println("SQL 血缘关系分析");
        System.out.println(repeat("=", 80));
        System.out.println("\n【SQL 语句】");
        System.out.println(sql);
        System.out.println();

        // 1. 解析 SQL
        OdpsSQLMetadataResult validationResult = validator.result(sql);

        // 先检查是否支持该 SQL 类型
        if (!validationResult.isSupported()) {
            System.out.println("❌ 不支持的 SQL 类型！");
            System.out.println("错误信息：");
            if (!validationResult.getErrors().isEmpty()) {
                validationResult
                        .getErrors()
                        .forEach(
                                error ->
                                        System.out.println(
                                                String.format(
                                                        "  - [%d:%d] %s",
                                                        error.getLine(),
                                                        error.getCharPositionInLine(),
                                                        error.getMessage())));
            } else {
                System.out.println("  - 当前仅支持 SELECT 语句");
            }
            return;
        }

        // 再检查是否有语法或语义错误
        if (!validationResult.isValid()) {
            System.out.println("❌ SQL 解析失败！");
            System.out.println("错误信息：");
            validationResult
                    .getErrors()
                    .forEach(
                            error ->
                                    System.out.println(
                                            String.format(
                                                    "  - [%d:%d] %s",
                                                    error.getLine(),
                                                    error.getCharPositionInLine(),
                                                    error.getMessage())));
            return;
        }

        OdpsSQLMetadata sqlMetadata =
                validationResult
                        .getMetadata()
                        .orElseThrow(() -> new RuntimeException("SQL 解析失败：无法提取元数据"));

        // 2. 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 3. 可视化输出
        visualizeTableLineage(result.getTableLineages());
        visualizeColumnLineage(result.getColumnLineages());

        System.out.println(repeat("=", 80));
    }

    /** 重复字符串（Java 8 兼容）。 */
    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /** 可视化表血缘关系。 */
    private void visualizeTableLineage(List<TableLineage> tableLineages) {
        System.out.println("【表血缘关系】");
        if (tableLineages.isEmpty()) {
            System.out.println("  未发现表血缘关系");
            System.out.println();
            return;
        }

        System.out.println("  共发现 " + tableLineages.size() + " 个表：");
        System.out.println();

        for (int i = 0; i < tableLineages.size(); i++) {
            TableLineage lineage = tableLineages.get(i);
            System.out.println("  " + (i + 1) + ". " + lineage.getQualifiedName());

            if (lineage.getAlias() != null && !lineage.getAlias().isEmpty()) {
                System.out.println("     别名: " + lineage.getAlias());
            }

            if (lineage.getTableMeta() != null) {
                System.out.println("     数据库: " + lineage.getTableMeta().getDatabase());
                System.out.println("     表名: " + lineage.getTableMeta().getName());
                System.out.println("     类型: " + lineage.getTableMeta().getType());
                if (lineage.getTableMeta().getComment() != null
                        && !lineage.getTableMeta().getComment().isEmpty()) {
                    System.out.println("     注释: " + lineage.getTableMeta().getComment());
                }
            }
            System.out.println();
        }
    }

    /** 可视化字段血缘关系。 */
    private void visualizeColumnLineage(List<ColumnLineage> columnLineages) {
        System.out.println("【字段血缘关系】");
        if (columnLineages.isEmpty()) {
            System.out.println("  未发现字段血缘关系");
            System.out.println();
            return;
        }

        // 构建字段名到 ColumnLineage 的映射，用于查找中间字段
        Map<String, ColumnLineage> fieldMap = buildFieldMap(columnLineages);

        // 只显示最终输出字段（使用提取时标记的 isFinalOutput 标志）
        List<ColumnLineage> finalOutputFields = new ArrayList<>();
        for (ColumnLineage lineage : columnLineages) {
            if (lineage.isFinalOutput()) {
                finalOutputFields.add(lineage);
            }
        }

        System.out.println("  共发现 " + finalOutputFields.size() + " 个输出字段：");
        System.out.println();

        for (int i = 0; i < finalOutputFields.size(); i++) {
            ColumnLineage lineage = finalOutputFields.get(i);
            System.out.println("  " + (i + 1) + ". 输出字段: " + lineage.getTargetField());
            System.out.println("     表达式: " + lineage.getExpression());
            System.out.println("     类型: " + lineage.getCategory());

            visualizeColumnSources(lineage, lineage.getSources(), fieldMap, "     ", 1);

            System.out.println();
        }
    }

    /**
     * 递归可视化字段来源，支持多层级关系。
     *
     * @param currentLineage 当前字段血缘
     * @param sources 字段来源列表
     * @param fieldMap 字段名到 ColumnLineage 的映射
     * @param prefix 输出前缀
     * @param depth 当前深度（用于控制递归深度，防止无限递归）
     */
    private void visualizeColumnSources(
            ColumnLineage currentLineage,
            List<ColumnSource> sources,
            Map<String, ColumnLineage> fieldMap,
            String prefix,
            int depth) {
        if (sources.isEmpty()) {
            System.out.println(prefix + "来源: 无（可能是常量或表达式）");
            return;
        }

        // 防止无限递归，限制最大深度
        if (depth >= MAX_DEPTH) {
            System.out.println(prefix + "来源: (递归深度超过限制，已截断)");
            return;
        }

        System.out.println(prefix + "来源 (" + sources.size() + " 个):");
        for (int j = 0; j < sources.size(); j++) {
            ColumnSource source = sources.get(j);
            String sourcePrefix = prefix + "  " + (j + 1) + ". ";

            // 检查该来源是否对应一个中间字段（即它本身也是一个 ColumnLineage 的输出）
            String sourceFieldKey = buildSourceFieldKey(source);
            ColumnLineage intermediateLineage =
                    findIntermediateLineage(source, fieldMap, currentLineage.getTargetField());

            if (intermediateLineage != null) {
                // 这是一个中间字段，需要递归显示其来源
                System.out.println(sourcePrefix + sourceFieldKey + " (中间字段)");
                System.out.println(prefix + "     表达式: " + intermediateLineage.getExpression());
                System.out.println(prefix + "     类型: " + intermediateLineage.getCategory());

                // 递归显示中间字段的来源
                String nextPrefix = prefix + "     ";
                visualizeColumnSources(
                        intermediateLineage,
                        intermediateLineage.getSources(),
                        fieldMap,
                        nextPrefix,
                        depth + 1);
            } else {
                // 这是最终来源（直接来自表列）
                System.out.println(sourcePrefix + sourceFieldKey);

                if (source.getTableAlias() != null && !source.getTableAlias().isEmpty()) {
                    System.out.println(prefix + "        表别名: " + source.getTableAlias());
                }

                if (source.getColumnMeta() != null) {
                    System.out.println(
                            prefix + "        列类型: " + source.getColumnMeta().getDataType());
                    if (source.getColumnMeta().getComment() != null
                            && !source.getColumnMeta().getComment().isEmpty()) {
                        System.out.println(
                                prefix + "        列注释: " + source.getColumnMeta().getComment());
                    }
                }
            }
        }
    }

    /** 构建字段名到 ColumnLineage 的映射。 */
    private Map<String, ColumnLineage> buildFieldMap(List<ColumnLineage> columnLineages) {
        Map<String, ColumnLineage> map = new HashMap<>();
        for (ColumnLineage lineage : columnLineages) {
            map.put(lineage.getTargetField(), lineage);
        }
        return map;
    }

    /** 构建来源字段的键值（用于匹配）。 */
    private String buildSourceFieldKey(ColumnSource source) {
        return source.getTableQualifiedName() + "." + source.getColumnName();
    }

    /**
     * 查找中间字段的 ColumnLineage。 如果来源字段本身也是一个 ColumnLineage 的输出，则返回该 ColumnLineage。
     *
     * <p>识别逻辑：
     *
     * <ul>
     *   <li>如果 source 有表别名，尝试通过 "表别名.列名" 匹配
     *   <li>如果 source 有表别名，尝试通过 "列名" 匹配（可能是子查询字段）
     *   <li>如果 source 没有表别名，尝试通过 "列名" 匹配
     *   <li>如果匹配到的 ColumnLineage 的来源不是直接来自表列（即 sources 不为空），则认为是中间字段
     * </ul>
     */
    private ColumnLineage findIntermediateLineage(
            ColumnSource source, Map<String, ColumnLineage> fieldMap, String currentTargetField) {
        String columnName = source.getColumnName();
        String tableAlias = source.getTableAlias();
        String tableQualifiedName = source.getTableQualifiedName();

        // 策略1: 尝试通过完整限定名匹配（表别名.列名 或 表限定名.列名）
        // 例如：source 是 "sub.id"，查找 targetField 为 "sub.id" 的 ColumnLineage
        if (tableAlias != null && !tableAlias.isEmpty()) {
            String qualifiedFieldName = tableAlias + "." + columnName;
            ColumnLineage qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                // 检查该 ColumnLineage 是否有来源（如果有，说明是中间字段）
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略1.5: 尝试通过表限定名.列名匹配（用于表达式字段）
        // 例如：source 的 tableQualifiedName 是 "t2"，columnName 是 "nvl(table_name,'1')"
        // 查找 targetField 为 "t2.nvl(table_name,'1')" 的 ColumnLineage
        if (tableQualifiedName != null && !tableQualifiedName.isEmpty() && columnName != null) {
            String qualifiedFieldName = tableQualifiedName + "." + columnName;
            ColumnLineage qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                // 检查该 ColumnLineage 是否有来源（如果有，说明是中间字段）
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略2: 尝试通过列名匹配（可能是子查询字段）
        // 例如：source 是 "sub.id"，查找 targetField 为 "id" 的 ColumnLineage
        // 这种情况通常出现在子查询场景：外层查询引用子查询的字段
        ColumnLineage directMatch = fieldMap.get(columnName);
        if (directMatch != null && !directMatch.getTargetField().equals(currentTargetField)) {
            // 检查该 ColumnLineage 是否有来源（如果有，说明是中间字段）
            // 同时，如果 source 有表别名，更可能是中间字段（子查询字段）
            if (!directMatch.getSources().isEmpty()) {
                // 进一步检查：如果 source 有表别名，且该 ColumnLineage 的来源中包含
                // 与 source 表别名相关的信息，则更可能是中间字段
                if (tableAlias != null && !tableAlias.isEmpty()) {
                    // 检查该 ColumnLineage 的来源中是否有与 source 相关的表别名
                    // 这里简化处理：如果字段名匹配，且 source 有表别名，且该 ColumnLineage 有来源，则认为是中间字段
                    return directMatch;
                } else {
                    // 如果 source 没有表别名，但该 ColumnLineage 有来源，也可能是中间字段
                    // 但需要更谨慎，避免误判
                    // 这里暂时返回，因为如果字段名匹配且有来源，很可能是中间字段
                    return directMatch;
                }
            }
        }

        // 策略3: 尝试通过表达式匹配（如果 source 的列名出现在某个 ColumnLineage 的表达式中）
        // 这种情况较少见，但可能出现在复杂的 SQL 中
        for (ColumnLineage lineage : fieldMap.values()) {
            if (lineage.getTargetField().equals(currentTargetField)) {
                continue;
            }
            if (lineage.getExpression() != null && lineage.getExpression().contains(columnName)) {
                // 检查该 ColumnLineage 是否有来源
                if (!lineage.getSources().isEmpty()) {
                    // 进一步检查：如果 source 有表别名，检查表达式是否包含表别名
                    if (tableAlias != null && !tableAlias.isEmpty()) {
                        if (lineage.getExpression().contains(tableAlias + "." + columnName)
                                || lineage.getTargetField().equals(columnName)) {
                            return lineage;
                        }
                    }
                }
            }
        }

        return null;
    }

    /** 以树形结构可视化字段血缘关系。 */
    public void visualizeAsTree(String sql) throws MetadataException {
        System.out.println(repeat("=", 80));
        System.out.println("SQL 血缘关系树形图");
        System.out.println(repeat("=", 80));
        System.out.println("\n【SQL 语句】");
        System.out.println(sql);
        System.out.println();

        // 1. 解析 SQL
        OdpsSQLMetadataResult validationResult = validator.result(sql);

        // 先检查是否支持该 SQL 类型
        if (!validationResult.isSupported()) {
            System.out.println("❌ 不支持的 SQL 类型！");
            if (!validationResult.getErrors().isEmpty()) {
                validationResult
                        .getErrors()
                        .forEach(
                                error ->
                                        System.out.println(
                                                String.format(
                                                        "  - [%d:%d] %s",
                                                        error.getLine(),
                                                        error.getCharPositionInLine(),
                                                        error.getMessage())));
            } else {
                System.out.println("  - 当前仅支持 SELECT 语句");
            }
            return;
        }

        // 再检查是否有语法或语义错误
        if (!validationResult.isValid()) {
            System.out.println("❌ SQL 解析失败！");
            validationResult
                    .getErrors()
                    .forEach(
                            error ->
                                    System.out.println(
                                            String.format(
                                                    "  - [%d:%d] %s",
                                                    error.getLine(),
                                                    error.getCharPositionInLine(),
                                                    error.getMessage())));
            return;
        }

        com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata sqlMetadata =
                validationResult
                        .getMetadata()
                        .orElseThrow(() -> new RuntimeException("SQL 解析失败：无法提取元数据"));

        // 2. 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 3. 树形可视化
        System.out.println("【表血缘关系树】");
        System.out.println();
        visualizeTableHierarchy(result.getTableLineages());

        System.out.println();
        System.out.println("【字段血缘关系树】");
        System.out.println();
        visualizeColumnHierarchy(result.getColumnLineages());

        System.out.println();
        System.out.println(repeat("=", 80));
    }

    /** 输出表血缘的树形视图。 */
    private void visualizeTableHierarchy(List<TableLineage> tableLineages) {
        if (tableLineages.isEmpty()) {
            System.out.println("  未发现表血缘关系");
            return;
        }

        for (int i = 0; i < tableLineages.size(); i++) {
            TableLineage lineage = tableLineages.get(i);
            boolean isLast = (i == tableLineages.size() - 1);
            String connector = isLast ? "└── " : "├── ";

            // 显示表名和别名
            String tableInfo = lineage.getQualifiedName();
            if (lineage.getAlias() != null && !lineage.getAlias().isEmpty()) {
                tableInfo += " (别名: " + lineage.getAlias() + ")";
            }
            System.out.println("  " + connector + tableInfo);

            // 显示表的详细信息
            if (lineage.getTableMeta() != null) {
                String prefix = isLast ? "      " : "│     ";
                System.out.println(prefix + "数据库: " + lineage.getTableMeta().getDatabase());
                System.out.println(prefix + "表名: " + lineage.getTableMeta().getName());
                System.out.println(prefix + "类型: " + lineage.getTableMeta().getType());
                if (lineage.getTableMeta().getComment() != null
                        && !lineage.getTableMeta().getComment().isEmpty()) {
                    System.out.println(prefix + "注释: " + lineage.getTableMeta().getComment());
                }
            }
        }
    }

    /** 输出字段血缘的父子层级视图。 */
    private void visualizeColumnHierarchy(List<ColumnLineage> columnLineages) {
        if (columnLineages.isEmpty()) {
            System.out.println("  未发现字段血缘关系");
            return;
        }

        Map<String, ColumnLineage> fieldMap = buildFieldMap(columnLineages);

        // 只显示最终输出字段（使用提取时标记的 isFinalOutput 标志）
        List<ColumnLineage> finalOutputFields = new ArrayList<>();
        for (ColumnLineage lineage : columnLineages) {
            if (lineage.isFinalOutput()) {
                finalOutputFields.add(lineage);
            }
        }

        for (int i = 0; i < finalOutputFields.size(); i++) {
            ColumnLineage lineage = finalOutputFields.get(i);
            visualizeColumnTree(lineage, "", i == finalOutputFields.size() - 1, fieldMap, 0);
        }
        System.out.println();
    }

    /**
     * 递归可视化字段血缘树，支持多层级、多深度的关系。
     *
     * @param lineage 当前字段血缘
     * @param prefix 输出前缀
     * @param isLast 是否是最后一个节点
     * @param fieldMap 字段名到 ColumnLineage 的映射（用于查找中间字段）
     * @param depth 当前深度（用于控制递归深度，防止无限递归）
     */
    private void visualizeColumnTree(
            ColumnLineage lineage,
            String prefix,
            boolean isLast,
            Map<String, ColumnLineage> fieldMap,
            int depth) {

        // 防止无限递归，限制最大深度
        if (depth >= MAX_DEPTH) {
            String connector = isLast ? "└── " : "├── ";
            System.out.println(
                    prefix
                            + connector
                            + lineage.getTargetField()
                            + " ["
                            + lineage.getCategory()
                            + "] (递归深度超过限制，已截断)");
            return;
        }

        String connector = isLast ? "└── " : "├── ";
        System.out.println(
                prefix + connector + lineage.getTargetField() + " [" + lineage.getCategory() + "]");

        String nextPrefix = prefix + (isLast ? "    " : "│   ");
        List<ColumnSource> sources = lineage.getSources();

        for (int i = 0; i < sources.size(); i++) {
            ColumnSource source = sources.get(i);
            boolean isLastSource = (i == sources.size() - 1);
            String sourceConnector = isLastSource ? "└── " : "├── ";

            // 检查该来源是否对应一个中间字段（即它本身也是一个 ColumnLineage 的输出）
            ColumnLineage intermediateLineage =
                    findIntermediateLineage(source, fieldMap, lineage.getTargetField());

            if (intermediateLineage != null) {
                // 这是一个中间字段，直接递归显示其血缘树
                // visualizeColumnTree 会自己打印字段信息，所以这里不需要先打印
                visualizeColumnTree(
                        intermediateLineage, nextPrefix, isLastSource, fieldMap, depth + 1);
            } else {
                // 这是最终来源（直接来自表列）
                String sourceInfo = source.getTableQualifiedName() + "." + source.getColumnName();
                if (source.getColumnMeta() != null) {
                    sourceInfo += " [" + source.getColumnMeta().getDataType() + "]";
                }

                System.out.println(nextPrefix + sourceConnector + sourceInfo);
            }
        }
    }
}
