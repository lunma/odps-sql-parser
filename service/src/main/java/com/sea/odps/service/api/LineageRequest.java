package com.sea.odps.service.api;

import java.util.Map;

/** 血缘提取请求。 */
public class LineageRequest {
    private String sql;
    private String connectorType;
    private Map<String, Object> connectorConfig;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public Map<String, Object> getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(Map<String, Object> connectorConfig) {
        this.connectorConfig = connectorConfig;
    }
}
