package com.sea.odps.service.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.lineage.TableLineage;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;

/**
 * 列名冲突检测器。
 *
 * <p>检测 SELECT * 在多个表 JOIN 时的列名冲突问题。
 *
 * <p>规则：
 *
 * <ul>
 *   <li>当多个表 JOIN 时，如果使用 SELECT * 且没有指定表别名，需要检查是否有列名冲突
 *   <li>如果多个表有相同的列名，SELECT * 会产生歧义，应该报错
 *   <li>如果使用 SELECT t1.* 明确指定了表别名，则允许
 * </ul>
 *
 * <p>示例：
 *
 * <pre>
 * -- ❌ 错误：列名冲突
 * SELECT * FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;
 * -- 如果 table1 和 table2 都有 name 列，会产生冲突
 *
 * -- ✅ 正确：明确指定表别名
 * SELECT t1.* FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;
 * </pre>
 */
public class ColumnConflictChecker {

    /**
     * 检测 SELECT * 在 JOIN 时的列名冲突。
     *
     * @param sqlMetadata SQL 元数据
     * @param tableLineages 表血缘列表
     * @return 校验错误列表
     */
    public List<ValidationError> checkWildcardConflict(
            OdpsSQLMetadata sqlMetadata, List<TableLineage> tableLineages) {

        List<ValidationError> errors = new ArrayList<>();

        // 1. 检查是否有多个表
        if (tableLineages == null || tableLineages.size() <= 1) {
            return errors; // 单个表，不需要检查冲突
        }

        // 2. 检查 SELECT 字段中是否有通配符
        boolean hasWildcard = false;
        String wildcardTableAlias = null;

        for (FieldMetadata field : sqlMetadata.getFields()) {
            String expression = field.getExpression();
            if ("*".equals(expression)) {
                hasWildcard = true;
                // 检查是否有表别名
                for (ColumnReference colRef : field.getSourceColumns()) {
                    if ("*".equals(colRef.getName())
                            && colRef.getOwner() != null
                            && !colRef.getOwner().isEmpty()) {
                        wildcardTableAlias = colRef.getOwner();
                        break;
                    }
                }
                break;
            }
        }

        // 3. 如果没有通配符，或者通配符有明确的表别名，不需要检查冲突
        if (!hasWildcard || wildcardTableAlias != null) {
            return errors;
        }

        // 4. 收集所有表的列名，检测冲突
        Map<String, List<TableLineage>> columnToTables = new HashMap<>();

        for (TableLineage lineage : tableLineages) {
            if (lineage.getTableMeta() != null && lineage.getTableMeta().getColumns() != null) {
                for (ColumnMeta column : lineage.getTableMeta().getColumns()) {
                    String columnName = column.getName();
                    columnToTables.computeIfAbsent(columnName, k -> new ArrayList<>()).add(lineage);
                }
            }
        }

        // 5. 检查冲突
        List<String> conflicts = new ArrayList<>();
        for (Map.Entry<String, List<TableLineage>> entry : columnToTables.entrySet()) {
            String columnName = entry.getKey();
            List<TableLineage> tables = entry.getValue();

            if (tables.size() > 1) {
                // 有多个表包含相同的列名
                String tableNames =
                        tables.stream()
                                .map(t -> t.getQualifiedName())
                                .collect(Collectors.joining(", "));
                conflicts.add(String.format("列 '%s' 在多个表中存在: %s", columnName, tableNames));
            }
        }

        // 6. 如果有冲突，生成错误
        if (!conflicts.isEmpty()) {
            String message =
                    String.format(
                            "SELECT * 在多个表 JOIN 时存在列名冲突。冲突的列: %s。请使用表别名明确指定，如 SELECT t1.* 或 SELECT t1.col1, t2.col2",
                            String.join("; ", conflicts));
            errors.add(
                    new ValidationError(
                            ValidationErrorType.COLUMN_CONFLICT,
                            message,
                            0,
                            0 // 行号和列号可以从 SQL 元数据中获取
                            ));
        }

        return errors;
    }
}
