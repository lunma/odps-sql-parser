package com.sea.odps.service.api;

import java.util.List;

import com.sea.odps.service.api.model.ColumnLineageModel;
import com.sea.odps.service.api.model.ColumnLineageTreeNode;
import com.sea.odps.service.api.model.TableLineageModel;

/** 血缘提取响应。 */
public class LineageResponse {
    private String sql;
    private List<TableLineageModel> tableLineages;
    private List<ColumnLineageModel> columnLineages;
    private List<ColumnLineageTreeNode> columnLineageTree;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<TableLineageModel> getTableLineages() {
        return tableLineages;
    }

    public void setTableLineages(List<TableLineageModel> tableLineages) {
        this.tableLineages = tableLineages;
    }

    public List<ColumnLineageModel> getColumnLineages() {
        return columnLineages;
    }

    public void setColumnLineages(List<ColumnLineageModel> columnLineages) {
        this.columnLineages = columnLineages;
    }

    public List<ColumnLineageTreeNode> getColumnLineageTree() {
        return columnLineageTree;
    }

    public void setColumnLineageTree(List<ColumnLineageTreeNode> columnLineageTree) {
        this.columnLineageTree = columnLineageTree;
    }
}
