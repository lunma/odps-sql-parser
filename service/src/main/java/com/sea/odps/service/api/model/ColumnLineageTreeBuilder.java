package com.sea.odps.service.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段血缘关系树构建器。
 *
 * <p>将扁平的 ColumnLineageModel 列表转换为树形结构，便于展示层级关系。
 */
public class ColumnLineageTreeBuilder {

    private static final int MAX_DEPTH = 10;

    /**
     * 将字段血缘关系列表转换为树形结构。
     *
     * @param columnLineages 字段血缘关系列表
     * @return 树形结构的根节点列表（只包含最终输出字段）
     */
    public static List<ColumnLineageTreeNode> buildTree(List<ColumnLineageModel> columnLineages) {
        if (columnLineages == null || columnLineages.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建字段名到 ColumnLineageModel 的映射
        Map<String, ColumnLineageModel> fieldMap = buildFieldMap(columnLineages);

        // 只处理最终输出字段
        List<ColumnLineageTreeNode> rootNodes = new ArrayList<>();
        for (ColumnLineageModel lineage : columnLineages) {
            if (lineage.isFinalOutput()) {
                ColumnLineageTreeNode rootNode =
                        buildTreeNode(lineage, fieldMap, new HashMap<>(), 0);
                if (rootNode != null) {
                    rootNodes.add(rootNode);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 递归构建树节点。
     *
     * @param lineage 当前字段血缘
     * @param fieldMap 字段名到 ColumnLineageModel 的映射
     * @param visited 已访问的字段集合（用于防止循环引用）
     * @param depth 当前深度（用于控制递归深度）
     * @return 树节点
     */
    private static ColumnLineageTreeNode buildTreeNode(
            ColumnLineageModel lineage,
            Map<String, ColumnLineageModel> fieldMap,
            Map<String, Boolean> visited,
            int depth) {

        // 防止无限递归
        if (depth >= MAX_DEPTH) {
            return new ColumnLineageTreeNode(
                    lineage.getTargetField(),
                    lineage.getExpression(),
                    lineage.getCategory(),
                    null,
                    false);
        }

        // 防止循环引用
        String fieldKey = lineage.getTargetField();
        if (visited.containsKey(fieldKey)) {
            return new ColumnLineageTreeNode(
                    fieldKey, lineage.getExpression(), lineage.getCategory(), null, false);
        }
        visited.put(fieldKey, true);

        // 处理来源字段
        List<ColumnSourceModel> sources = lineage.getSources();
        boolean isLeaf = (sources == null || sources.isEmpty());

        // 创建当前节点
        ColumnLineageTreeNode node =
                new ColumnLineageTreeNode(
                        lineage.getTargetField(),
                        lineage.getExpression(),
                        lineage.getCategory(),
                        null,
                        isLeaf);

        // 处理来源字段
        if (!isLeaf && sources != null) {
            for (ColumnSourceModel source : sources) {
                ColumnLineageTreeNode childNode = null;

                // 尝试查找中间字段（无论 isIntermediateField 是否为 true）
                // 因为有些中间字段可能没有正确标记
                ColumnLineageModel intermediateLineage =
                        findIntermediateLineage(source, fieldMap, fieldKey);
                if (intermediateLineage != null) {
                    // 递归构建子节点
                    Map<String, Boolean> newVisited = new HashMap<>(visited);
                    childNode = buildTreeNode(intermediateLineage, fieldMap, newVisited, depth + 1);
                }

                // 如果不是中间字段，创建叶子节点（最终来源）
                if (childNode == null) {
                    String sourceFieldName =
                            source.getTableQualifiedName() + "." + source.getColumnName();
                    childNode =
                            new ColumnLineageTreeNode(
                                    sourceFieldName,
                                    source.getColumnName(),
                                    "DIRECT",
                                    source.getDataType(),
                                    true);
                }

                if (childNode != null) {
                    node.addChild(childNode);
                }
            }
        }

        return node;
    }

    /**
     * 构建字段名到 ColumnLineageModel 的映射。
     *
     * @param columnLineages 字段血缘关系列表
     * @return 字段名到 ColumnLineageModel 的映射
     */
    private static Map<String, ColumnLineageModel> buildFieldMap(
            List<ColumnLineageModel> columnLineages) {
        Map<String, ColumnLineageModel> map = new HashMap<>();
        for (ColumnLineageModel lineage : columnLineages) {
            map.put(lineage.getTargetField(), lineage);
        }
        return map;
    }

    /**
     * 查找中间字段对应的 ColumnLineageModel。
     *
     * @param source 来源字段
     * @param fieldMap 字段名到 ColumnLineageModel 的映射
     * @param currentTargetField 当前目标字段（用于避免自引用）
     * @return 中间字段对应的 ColumnLineageModel，如果不存在则返回 null
     */
    private static ColumnLineageModel findIntermediateLineage(
            ColumnSourceModel source,
            Map<String, ColumnLineageModel> fieldMap,
            String currentTargetField) {

        // 策略0: 优先使用 intermediateFieldName（如果存在）
        String intermediateFieldName = source.getIntermediateFieldName();
        if (intermediateFieldName != null && !intermediateFieldName.isEmpty()) {
            ColumnLineageModel lineage = fieldMap.get(intermediateFieldName);
            if (lineage != null && !lineage.getTargetField().equals(currentTargetField)) {
                // 检查该 ColumnLineageModel 是否有来源（如果有，说明是中间字段）
                if (!lineage.getSources().isEmpty()) {
                    return lineage;
                }
            }
        }

        String columnName = source.getColumnName();
        String tableAlias = source.getTableAlias();
        String tableQualifiedName = source.getTableQualifiedName();

        // 策略1: 尝试通过表限定名.列名匹配（用于表达式字段，如 t2.nvl(table_name, '1')）
        // 这个策略应该优先于表别名匹配，因为表达式字段通常使用表限定名
        if (tableQualifiedName != null && !tableQualifiedName.isEmpty() && columnName != null) {
            String qualifiedFieldName = tableQualifiedName + "." + columnName;
            ColumnLineageModel qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                // 如果有来源，说明是中间字段
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略2: 尝试通过完整限定名匹配（表别名.列名）
        if (tableAlias != null && !tableAlias.isEmpty()) {
            String qualifiedFieldName = tableAlias + "." + columnName;
            ColumnLineageModel qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略3: 尝试通过列名匹配（可能是子查询字段）
        if (columnName != null && !columnName.isEmpty()) {
            ColumnLineageModel directMatch = fieldMap.get(columnName);
            if (directMatch != null && !directMatch.getTargetField().equals(currentTargetField)) {
                if (!directMatch.getSources().isEmpty()) {
                    return directMatch;
                }
            }
        }

        return null;
    }
}
