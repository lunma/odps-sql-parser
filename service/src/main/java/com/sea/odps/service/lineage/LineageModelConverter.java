package com.sea.odps.service.lineage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sea.odps.service.api.model.ColumnLineageModel;
import com.sea.odps.service.api.model.ColumnSourceModel;
import com.sea.odps.service.api.model.LineageResultModel;
import com.sea.odps.service.api.model.TableLineageModel;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;

/**
 * 血缘关系模型转换器。
 *
 * <p>将 LineageExtractor 的内部模型转换为便于接口使用的模型。
 */
public class LineageModelConverter {

    /**
     * 将 LineageExtractor.LineageResult 转换为 LineageResultModel。
     *
     * @param result LineageExtractor 的结果
     * @param sql SQL 语句（可选）
     * @return 转换后的模型
     */
    public static LineageResultModel convert(LineageResult result, String sql) {
        if (result == null) {
            return new LineageResultModel(new ArrayList<>(), new ArrayList<>(), sql);
        }

        // 转换表血缘
        List<TableLineageModel> tableLineages = convertTableLineages(result.getTableLineages());

        // 转换字段血缘
        List<ColumnLineageModel> columnLineages = convertColumnLineages(result.getColumnLineages());

        return new LineageResultModel(tableLineages, columnLineages, sql);
    }

    /** 转换表血缘列表。 */
    private static List<TableLineageModel> convertTableLineages(List<TableLineage> tableLineages) {
        List<TableLineageModel> result = new ArrayList<>();

        for (TableLineage lineage : tableLineages) {
            TableMeta tableMeta = lineage.getTableMeta();
            String database = tableMeta != null ? tableMeta.getDatabase() : null;
            String tableName = tableMeta != null ? tableMeta.getName() : null;
            String tableType = tableMeta != null ? tableMeta.getType() : null;
            String comment = tableMeta != null ? tableMeta.getComment() : null;

            TableLineageModel model =
                    new TableLineageModel(
                            lineage.getQualifiedName(),
                            lineage.getAlias(),
                            database,
                            tableName,
                            tableType,
                            comment);
            result.add(model);
        }

        return result;
    }

    /** 转换字段血缘列表，只返回最终输出字段，过滤掉中间字段。 中间字段的信息会通过 ColumnSourceModel.isIntermediateField() 在来源中体现。 */
    private static List<ColumnLineageModel> convertColumnLineages(
            List<ColumnLineage> columnLineages) {
        if (columnLineages == null || columnLineages.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建字段名到 ColumnLineage 的映射（用于查找中间字段）
        Map<String, ColumnLineage> fieldMap = new HashMap<>();
        for (ColumnLineage lineage : columnLineages) {
            fieldMap.put(lineage.getTargetField(), lineage);
        }

        // 找出所有被引用的中间字段（用于构建树形结构）
        // 先转换所有字段，以便能够查找中间字段
        Map<String, ColumnLineageModel> allModels = new HashMap<>();
        for (ColumnLineage lineage : columnLineages) {
            List<ColumnSourceModel> sources =
                    convertColumnSources(lineage.getSources(), fieldMap, lineage.getTargetField());

            ColumnLineageModel model =
                    new ColumnLineageModel(
                            lineage.getTargetField(),
                            lineage.getExpression(),
                            lineage.getCategory(),
                            sources,
                            lineage.isFinalOutput());
            allModels.put(lineage.getTargetField(), model);
        }

        // 只返回最终输出字段，过滤掉中间字段
        // 中间字段的信息会通过 ColumnSourceModel.isIntermediateField() 在来源中体现
        List<ColumnLineageModel> result = new ArrayList<>();
        for (ColumnLineageModel model : allModels.values()) {
            // 只包含最终输出字段
            if (model.isFinalOutput()) {
                result.add(model);
            }
        }

        return result;
    }

    /** 转换字段来源列表。 */
    private static List<ColumnSourceModel> convertColumnSources(
            List<ColumnSource> sources,
            Map<String, ColumnLineage> fieldMap,
            String currentTargetField) {
        List<ColumnSourceModel> result = new ArrayList<>();

        for (ColumnSource source : sources) {
            ColumnMeta columnMeta = source.getColumnMeta();
            String dataType = columnMeta != null ? columnMeta.getDataType() : null;
            String comment = columnMeta != null ? columnMeta.getComment() : null;

            // 检查是否为中间字段
            ColumnLineage intermediateLineage =
                    findIntermediateLineage(source, fieldMap, currentTargetField);

            if (intermediateLineage != null) {
                // 这是中间字段
                ColumnSourceModel model =
                        new ColumnSourceModel(
                                source.getTableQualifiedName(),
                                source.getTableAlias(),
                                source.getColumnName(),
                                dataType,
                                comment,
                                true,
                                intermediateLineage.getTargetField());
                result.add(model);
            } else {
                // 这是最终来源（直接来自表列）
                ColumnSourceModel model =
                        new ColumnSourceModel(
                                source.getTableQualifiedName(),
                                source.getTableAlias(),
                                source.getColumnName(),
                                dataType,
                                comment);
                result.add(model);
            }
        }

        return result;
    }

    /** 查找中间字段的 ColumnLineage。 复用 LineageFieldResolver 中的逻辑。 */
    private static ColumnLineage findIntermediateLineage(
            ColumnSource source, Map<String, ColumnLineage> fieldMap, String currentTargetField) {
        return LineageFieldResolver.findIntermediateLineage(source, fieldMap, currentTargetField);
    }
}
