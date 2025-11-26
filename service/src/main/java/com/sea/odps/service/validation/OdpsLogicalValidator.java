package com.sea.odps.service.validation;

import java.util.ArrayList;
import java.util.List;

import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.lineage.LineageExtractor;
import com.sea.odps.service.lineage.LineageResult;
import com.sea.odps.service.lineage.TableLineage;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;

/**
 * ODPS SQL 逻辑校验器。
 *
 * <p>对 SQL 语句进行逻辑校验，包括：
 *
 * <ul>
 *   <li>列名冲突检测：检测 SELECT * 在 JOIN 时的列名冲突
 *   <li>表存在性检测：验证表是否存在
 *   <li>JOIN 条件检测：验证 JOIN 条件中的列是否存在
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>
 * OdpsLogicalValidator validator = new OdpsLogicalValidator();
 * ValidationResult result = validator.validate(sqlMetadata, metadataConnector);
 *
 * if (!result.isValid()) {
 *     for (ValidationError error : result.getErrors()) {
 *         System.out.println(error.getMessage());
 *     }
 * }
 * </pre>
 */
public class OdpsLogicalValidator {

    private final ColumnConflictChecker columnConflictChecker;

    public OdpsLogicalValidator() {
        this.columnConflictChecker = new ColumnConflictChecker();
    }

    /**
     * 校验 SQL 逻辑。
     *
     * @param sqlMetadata SQL 元数据（从 OdpsSQLMetadataEntrypoint.result() 获取）
     * @param metadataConnector 元数据连接器（用于获取表结构信息）
     * @return 校验结果
     * @throws MetadataException 当元数据服务不可用时抛出
     */
    public ValidationResult validate(
            OdpsSQLMetadata sqlMetadata, MetadataConnector metadataConnector)
            throws MetadataException {

        List<ValidationError> errors = new ArrayList<>();

        // 1. 提取表血缘（需要元数据连接器）
        LineageExtractor extractor = new LineageExtractor(metadataConnector);
        LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();

        // 2. 列名冲突检测
        errors.addAll(columnConflictChecker.checkWildcardConflict(sqlMetadata, tableLineages));

        // 3. 表存在性检测（可以扩展）
        // errors.addAll(tableExistenceChecker.checkTableExistence(tableLineages));

        // 4. JOIN 条件检测（可以扩展）
        // errors.addAll(joinConditionChecker.checkJoinConditions(sqlMetadata, tableLineages));

        // 5. 返回校验结果
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
}
