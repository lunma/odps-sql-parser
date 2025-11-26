package com.sea.odps.service.lineage;

import java.util.List;

/**
 * 血缘关系结果。
 *
 * <p>包含表血缘和字段血缘的提取结果。
 */
public class LineageResult {
    private final List<TableLineage> tableLineages;
    private final List<ColumnLineage> columnLineages;

    public LineageResult(List<TableLineage> tableLineages, List<ColumnLineage> columnLineages) {
        this.tableLineages = tableLineages;
        this.columnLineages = columnLineages;
    }

    public List<TableLineage> getTableLineages() {
        return tableLineages;
    }

    public List<ColumnLineage> getColumnLineages() {
        return columnLineages;
    }
}
