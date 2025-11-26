package com.sea.odps.service.connector;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一元数据服务的连接器接口。
 *
 * <p>该接口以 {@code unified-metadata.schema.json} 为契约，要求所有实现将外部系统的 Catalog/Database/Table/Column
 * 信息转换为统一模型，以便上层服务（SQL 解析器、数据地图、 元数据搜索、血缘等）无需关心数据源差异。
 */
public interface MetadataConnector {

    /** @return 连接器唯一标识，例如 {@code odps-default}、{@code hive-biz}。 */
    String id();

    /** @return 连接器对应的 Catalog 类型（ODPS/EXCEL/CSV/...），用于填充 JSON Schema 中的 {@code catalog.type}。 */
    CatalogType catalogType();

    /**
     * 拉取 Catalog 下所有数据库的概要信息。
     *
     * @return 数据库概要列表。
     * @throws MetadataException 当外部系统不可用或凭证失效时抛出。
     */
    List<DatabaseMeta> listDatabases() throws MetadataException;

    /**
     * 拉取指定数据库下的表详信息。
     *
     * @param databaseName 目标数据库。
     * @return 表元数据列表。
     * @throws MetadataException 当外部系统不可用或权限不足时抛出。
     */
    List<TableMeta> listTables(String databaseName) throws MetadataException;

    /**
     * 直接获取指定数据库的概要信息。
     *
     * <p>相比 {@link #listDatabases()}，此方法可以避免查询所有数据库，提高效率。 实现类可以根据数据源特性提供高效的实现。
     *
     * @param databaseName 数据库名称。
     * @return 数据库元数据，如果不存在则返回 {@code null}。
     * @throws MetadataException 当外部系统不可用或凭证失效时抛出。
     */
    DatabaseMeta getDatabase(String databaseName) throws MetadataException;

    /**
     * 直接获取指定表的元数据信息。
     *
     * <p>相比 {@link #listTables(String)}，此方法可以避免查询数据库下所有表，提高效率。 实现类可以根据数据源特性提供高效的实现。
     *
     * @param databaseName 数据库名称。
     * @param tableName 表名称。
     * @return 表元数据，如果不存在则返回 {@code null}。
     * @throws MetadataException 当外部系统不可用或权限不足时抛出。
     */
    TableMeta getTable(String databaseName, String tableName) throws MetadataException;

    /** @return 连接器配置（脱敏后）或运行时状态，用于诊断。 */
    default Map<String, Object> diagnostics() {
        Map<String, Object> details = new HashMap<>();
        details.put("connectorId", id());
        details.put("catalogType", catalogType().name());
        details.put("lastFetchedAt", Instant.now().toString());
        return Collections.unmodifiableMap(details);
    }

    /** Catalog 类型枚举，需与 JSON Schema 中的枚举保持一致。 */
    enum CatalogType {
        ODPS,
        EXCEL,
        CSV
    }

    /** 数据库概要信息。 */
    final class DatabaseMeta {
        private final String name;
        private final String owner;
        private final String description;
        private final Map<String, Object> properties;

        public DatabaseMeta(
                String name, String owner, String description, Map<String, Object> properties) {
            this.name = name;
            this.owner = owner;
            this.description = description;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    /** 表元数据描述，简化版，后续会映射到 JSON Schema 中的 Table/Column 节点。 */
    final class TableMeta {
        private final String database;
        private final String name;
        private final String type;
        private final String comment;
        private final String owner;
        private final List<ColumnMeta> columns;
        private final Map<String, Object> properties;

        public TableMeta(
                String database,
                String name,
                String type,
                String comment,
                String owner,
                List<ColumnMeta> columns,
                Map<String, Object> properties) {
            this.database = database;
            this.name = name;
            this.type = type;
            this.comment = comment;
            this.owner = owner;
            this.columns = columns;
            this.properties = properties;
        }

        public String getDatabase() {
            return database;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getComment() {
            return comment;
        }

        public String getOwner() {
            return owner;
        }

        public List<ColumnMeta> getColumns() {
            return columns;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    /** 列信息。 */
    final class ColumnMeta {
        private final String name;
        private final String dataType;
        private final boolean nullable;
        private final String comment;
        private final Integer ordinalPosition;
        private final boolean partition;
        private final String defaultValue;
        private final Map<String, Object> extra;

        public ColumnMeta(
                String name,
                String dataType,
                boolean nullable,
                String comment,
                Integer ordinalPosition,
                boolean partition,
                String defaultValue,
                Map<String, Object> extra) {
            this.name = name;
            this.dataType = dataType;
            this.nullable = nullable;
            this.comment = comment;
            this.ordinalPosition = ordinalPosition;
            this.partition = partition;
            this.defaultValue = defaultValue;
            this.extra = extra;
        }

        public String getName() {
            return name;
        }

        public String getDataType() {
            return dataType;
        }

        public boolean isNullable() {
            return nullable;
        }

        public String getComment() {
            return comment;
        }

        public Integer getOrdinalPosition() {
            return ordinalPosition;
        }

        public boolean isPartition() {
            return partition;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    /** 统一的连接器异常类型。 */
    class MetadataException extends Exception {
        public MetadataException(String message) {
            super(message);
        }

        public MetadataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
