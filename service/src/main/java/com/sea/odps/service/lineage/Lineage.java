package com.sea.odps.service.lineage;

import com.sea.odps.service.api.model.LineageResultModel;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;

/**
 * 血缘关系服务类。
 *
 * <p>提供简洁的接口供其他应用使用，封装了 SQL 解析、血缘提取和模型转换的完整流程。
 *
 * <p>使用示例：
 *
 * <pre>
 * MetadataConnector connector = new ExcelMetadataConnector("metadata.xlsx");
 * Lineage service = new Lineage(connector);
 *
 * LineageResultModel result = service.extractLineage("SELECT t1.id FROM ods.user_table t1");
 *
 * // 获取表血缘
 * List&lt;TableLineageModel&gt; tables = result.getTableLineages();
 *
 * // 获取字段血缘
 * List&lt;ColumnLineageModel&gt; columns = result.getColumnLineages();
 * </pre>
 */
public class Lineage {

    private final LineageExtractor extractor;
    private final OdpsSQLMetadataEntrypoint entrypoint;

    /**
     * 创建血缘关系服务。
     *
     * @param metadataConnector 元数据连接器
     */
    public Lineage(MetadataConnector metadataConnector) {
        this.extractor = new LineageExtractor(metadataConnector);
        this.entrypoint = new OdpsSQLMetadataEntrypoint();
    }

    /**
     * 从 SQL 语句中提取血缘关系。
     *
     * @param sql SQL 语句
     * @return 血缘关系结果模型
     * @throws MetadataException 当元数据服务不可用或 SQL 解析失败时抛出
     */
    public LineageResultModel extractLineage(String sql) throws MetadataException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 语句不能为空");
        }

        // 1. 解析 SQL
        OdpsSQLMetadataResult sqlMetadataResult = entrypoint.result(sql);

        // 先检查是否支持该 SQL 类型
        if (!sqlMetadataResult.isSupported()) {
            StringBuilder errorMsg = new StringBuilder("不支持的 SQL 类型: ");
            if (!sqlMetadataResult.getErrors().isEmpty()) {
                sqlMetadataResult
                        .getErrors()
                        .forEach(
                                error ->
                                        errorMsg.append(
                                                String.format(
                                                        "[%d:%d] %s; ",
                                                        error.getLine(),
                                                        error.getCharPositionInLine(),
                                                        error.getMessage())));
            } else {
                errorMsg.append("当前仅支持 SELECT 语句");
            }
            throw new MetadataException(errorMsg.toString());
        }

        // 再检查是否有语法或语义错误
        if (!sqlMetadataResult.isValid()) {
            StringBuilder errorMsg = new StringBuilder("SQL 解析失败: ");
            sqlMetadataResult
                    .getErrors()
                    .forEach(
                            error ->
                                    errorMsg.append(
                                            String.format(
                                                    "[%d:%d] %s; ",
                                                    error.getLine(),
                                                    error.getCharPositionInLine(),
                                                    error.getMessage())));
            throw new MetadataException(errorMsg.toString());
        }

        OdpsSQLMetadata sqlMetadata =
                sqlMetadataResult
                        .getMetadata()
                        .orElseThrow(() -> new MetadataException("SQL 解析失败：无法提取元数据"));

        // 2. 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 3. 转换为模型
        return LineageModelConverter.convert(result, sql);
    }

    /**
     * 从已解析的 SQL 元数据中提取血缘关系。
     *
     * @param sqlMetadata 已解析的 SQL 元数据
     * @param sql SQL 语句（可选，用于记录）
     * @return 血缘关系结果模型
     * @throws MetadataException 当元数据服务不可用时抛出
     */
    public LineageResultModel extractLineage(OdpsSQLMetadata sqlMetadata, String sql)
            throws MetadataException {
        if (sqlMetadata == null) {
            throw new IllegalArgumentException("SQL 元数据不能为空");
        }

        // 提取血缘关系
        LineageResult result = extractor.extract(sqlMetadata);

        // 转换为模型
        return LineageModelConverter.convert(result, sql);
    }
}
