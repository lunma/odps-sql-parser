package com.sea.odps.service.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 血缘关系结果模型。
 *
 * <p>包含表血缘和字段血缘的完整信息，便于通过接口提供给其他应用使用。
 */
public class LineageResultModel {

    /** 表血缘关系列表 */
    private final List<TableLineageModel> tableLineages;

    /** 字段血缘关系列表（只包含最终输出字段） */
    private final List<ColumnLineageModel> columnLineages;

    /** SQL 语句 */
    private final String sql;

    public LineageResultModel(
            List<TableLineageModel> tableLineages, List<ColumnLineageModel> columnLineages) {
        this(tableLineages, columnLineages, null);
    }

    public LineageResultModel(
            List<TableLineageModel> tableLineages,
            List<ColumnLineageModel> columnLineages,
            String sql) {
        this.tableLineages =
                tableLineages != null ? new ArrayList<>(tableLineages) : Collections.emptyList();
        this.columnLineages =
                columnLineages != null ? new ArrayList<>(columnLineages) : Collections.emptyList();
        this.sql = sql;
    }

    public List<TableLineageModel> getTableLineages() {
        return Collections.unmodifiableList(tableLineages);
    }

    public List<ColumnLineageModel> getColumnLineages() {
        return Collections.unmodifiableList(columnLineages);
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "LineageResultModel{"
                + "tableCount="
                + tableLineages.size()
                + ", columnCount="
                + columnLineages.size()
                + ", sql='"
                + sql
                + '\''
                + '}';
    }
}
