package com.sea.odps.service.lineage;

import java.util.HashMap;
import java.util.Map;

import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.model.field.FieldCategory;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;

/**
 * 字段解析辅助工具类。
 *
 * <p>提供字段类型判断、列源解析等辅助功能，简化 LineageExtractor 的逻辑。
 */
class LineageFieldResolver {

    /**
     * 判断是否是简单列引用。
     *
     * <p>简单列引用是指表达式等于字段名（或别名），且只有一个来源列。 例如：SELECT table_name FROM ... 或 SELECT t1.name AS name FROM
     * ...
     */
    static boolean isSimpleColumnReference(
            FieldMetadata field, String expression, String fieldName) {
        if (expression == null || fieldName == null) {
            return false;
        }
        // 表达式等于字段名，且只有一个来源列
        return expression.equals(fieldName)
                && field.getSourceColumns() != null
                && field.getSourceColumns().size() == 1;
    }

    /**
     * 判断是否是表达式字段。
     *
     * <p>表达式字段是指包含函数调用、字符串字面量或复杂表达式的字段。 利用 sql-parser 的 FieldCategory 来判断，同时检查字段特征。
     */
    static boolean isExpressionField(FieldMetadata field, String expression, String fieldName) {
        if (expression == null || fieldName == null) {
            return false;
        }

        // 利用 sql-parser 的 FieldCategory 判断
        if (field.getCategory() == FieldCategory.EXPRESSION) {
            // 有别名且表达式不等于别名，说明是表达式字段
            boolean hasAlias = field.getAlias() != null && !field.getAlias().isEmpty();
            if (hasAlias && !expression.equals(fieldName)) {
                return true;
            }
        }

        // 检查表达式特征：包含函数调用或字符串字面量
        boolean hasFunctionCall = expression.contains("(");
        boolean hasStringLiteral = expression.contains("'");
        boolean expressionDiffersFromName = !expression.equals(fieldName);

        // 如果表达式明显比字段名长，且包含函数调用或字符串字面量，则认为是表达式字段
        if (expressionDiffersFromName && (hasFunctionCall || hasStringLiteral)) {
            return true;
        }

        return false;
    }

    /**
     * 查找中间字段的 ColumnLineage。
     *
     * <p>复用 LineageVisualizer 中的逻辑，通过多种策略匹配中间字段。
     */
    static ColumnLineage findIntermediateLineage(
            ColumnSource source, Map<String, ColumnLineage> fieldMap, String currentTargetField) {
        String columnName = source.getColumnName();
        String tableAlias = source.getTableAlias();
        String tableQualifiedName = source.getTableQualifiedName();

        // 策略1: 尝试通过表别名.列名匹配
        if (tableAlias != null && !tableAlias.isEmpty()) {
            String qualifiedFieldName = tableAlias + "." + columnName;
            ColumnLineage qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略2: 尝试通过表限定名.列名匹配（用于表达式字段）
        if (tableQualifiedName != null && !tableQualifiedName.isEmpty() && columnName != null) {
            String qualifiedFieldName = tableQualifiedName + "." + columnName;
            ColumnLineage qualifiedMatch = fieldMap.get(qualifiedFieldName);
            if (qualifiedMatch != null
                    && !qualifiedMatch.getTargetField().equals(currentTargetField)) {
                if (!qualifiedMatch.getSources().isEmpty()) {
                    return qualifiedMatch;
                }
            }
        }

        // 策略3: 尝试通过列名匹配
        ColumnLineage directMatch = fieldMap.get(columnName);
        if (directMatch != null && !directMatch.getTargetField().equals(currentTargetField)) {
            if (!directMatch.getSources().isEmpty()) {
                return directMatch;
            }
        }

        return null;
    }

    /**
     * 匹配字段名。
     *
     * <p>检查字段的别名或表达式是否匹配指定的列名。
     */
    static boolean matchesColumnName(FieldMetadata field, String columnName) {
        if (field == null || columnName == null) {
            return false;
        }
        if (columnName.equals(field.getAlias())) {
            return true;
        }
        String expression = field.getExpression();
        if (expression == null) {
            return false;
        }
        String trimmedExpression = expression.trim();
        return columnName.equals(trimmedExpression) || trimmedExpression.endsWith("." + columnName);
    }

    /**
     * 从表元数据中解析列元数据。
     *
     * <p>使用缓存机制提高性能。
     */
    static ColumnMeta resolveColumnMeta(
            TableMeta tableMeta,
            String columnName,
            Map<String, Map<String, ColumnMeta>> columnCache) {
        if (tableMeta == null || columnName == null) {
            return null;
        }

        String cacheKey = tableMeta.getDatabase() + "." + tableMeta.getName();
        Map<String, ColumnMeta> columns = columnCache.get(cacheKey);
        if (columns == null) {
            columns = new HashMap<>();
            for (ColumnMeta col : tableMeta.getColumns()) {
                columns.put(col.getName(), col);
            }
            columnCache.put(cacheKey, columns);
        }

        return columns.get(columnName);
    }

    /**
     * 获取表的限定名。
     *
     * <p>优先使用表血缘中的限定名，如果不可用则从表元数据中构建。
     */
    static String getTableQualifiedName(TableLineage tableLineage, TableMeta tableMeta) {
        String qualifiedName = tableLineage.getTableRef().getQualifiedName();
        if (qualifiedName == null || qualifiedName.isEmpty() || "UNKNOWN".equals(qualifiedName)) {
            if (tableMeta != null) {
                String db = tableMeta.getDatabase();
                String name = tableMeta.getName();
                if (db != null && name != null) {
                    qualifiedName = db + "." + name;
                }
            }
        }
        return qualifiedName != null ? qualifiedName : "UNKNOWN";
    }
}
