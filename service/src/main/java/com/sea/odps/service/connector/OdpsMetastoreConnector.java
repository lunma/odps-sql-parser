package com.sea.odps.service.connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.aliyun.odps.Column;
import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.Project;
import com.aliyun.odps.ProjectFilter;
import com.aliyun.odps.Table;
import com.aliyun.odps.TableSchema;

/**
 * 基于官方 {@link Odps} SDK 的元数据连接器实现。
 *
 * <p>该实现可以遍历当前账号可见的 ODPS Project，并拉取指定 Project 下的表结构， 然后映射为统一的 {@link MetadataConnector} 模型。
 */
public final class OdpsMetastoreConnector implements MetadataConnector {

    private final Odps odps;
    private final List<String> projectWhitelist;

    public OdpsMetastoreConnector(Odps odps) {
        this(odps, Collections.emptyList());
    }

    public OdpsMetastoreConnector(Odps odps, List<String> projectWhitelist) {
        this.odps = Objects.requireNonNull(odps, "odps must not be null");
        if (projectWhitelist == null || projectWhitelist.isEmpty()) {
            this.projectWhitelist = Collections.emptyList();
        } else {
            this.projectWhitelist = Collections.unmodifiableList(new ArrayList<>(projectWhitelist));
        }
    }

    @Override
    public String id() {
        return "odps-" + odps.getDefaultProject();
    }

    @Override
    public CatalogType catalogType() {
        return CatalogType.ODPS;
    }

    @Override
    public List<DatabaseMeta> listDatabases() throws MetadataException {
        try {
            List<DatabaseMeta> result = new ArrayList<>();
            if (!projectWhitelist.isEmpty()) {
                for (String projectName : projectWhitelist) {
                    Project project = odps.projects().get(projectName);
                    result.add(toDatabaseMeta(project));
                }
                return result;
            }
            Iterator<Project> iterator = odps.projects().iteratorByFilter(new ProjectFilter());
            while (iterator.hasNext()) {
                result.add(toDatabaseMeta(iterator.next()));
            }
            return result;
        } catch (OdpsException ex) {
            throw new MetadataException("Failed to list ODPS projects", ex);
        }
    }

    @Override
    public List<TableMeta> listTables(String databaseName) throws MetadataException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        try {
            Iterator<Table> iterator = odps.tables().iterator(databaseName);
            List<TableMeta> result = new ArrayList<>();
            while (iterator.hasNext()) {
                result.add(toTableMeta(databaseName, iterator.next()));
            }
            return result;
        } catch (OdpsException ex) {
            throw new MetadataException("Failed to fetch tables for project: " + databaseName, ex);
        }
    }

    @Override
    public DatabaseMeta getDatabase(String databaseName) throws MetadataException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        try {
            // 直接获取指定项目，避免遍历所有项目
            Project project = odps.projects().get(databaseName);
            if (project == null) {
                return null;
            }
            return toDatabaseMeta(project);
        } catch (OdpsException ex) {
            throw new MetadataException("Failed to get ODPS project: " + databaseName, ex);
        }
    }

    @Override
    public TableMeta getTable(String databaseName, String tableName) throws MetadataException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        Objects.requireNonNull(tableName, "tableName must not be null");
        try {
            // 直接获取指定表，避免遍历数据库下所有表
            Table table = odps.tables().get(databaseName, tableName);
            if (table == null) {
                return null;
            }
            return toTableMeta(databaseName, table);
        } catch (OdpsException ex) {
            throw new MetadataException(
                    "Failed to get table " + databaseName + "." + tableName, ex);
        }
    }

    private DatabaseMeta toDatabaseMeta(Project project) {
        Map<String, Object> props = new HashMap<>();
        props.put("region", project.getRegionId());
        props.put("createdTime", project.getCreatedTime());
        props.put("lastModifiedTime", project.getLastModifiedTime());
        return new DatabaseMeta(project.getName(), project.getOwner(), project.getComment(), props);
    }

    private TableMeta toTableMeta(String projectName, Table table) throws OdpsException {
        TableSchema schema = table.getSchema();
        List<ColumnMeta> columns = new ArrayList<>();
        columns.addAll(toColumnMeta(schema.getColumns(), false));
        columns.addAll(toColumnMeta(schema.getPartitionColumns(), true));

        Map<String, Object> props = new HashMap<>();
        props.put("createdTime", table.getCreatedTime());
        props.put("lastMetaModifiedTime", table.getLastMetaModifiedTime());
        props.put("size", table.getSize());
        props.put("recordNum", table.getRecordNum());
        props.put("isPartitioned", table.isPartitioned());
        props.put("isExternal", table.isExternalTable());

        return new TableMeta(
                projectName,
                table.getName(),
                table.getType().name(),
                table.getComment(),
                table.getOwner(),
                columns,
                props);
    }

    private List<ColumnMeta> toColumnMeta(List<Column> columns, boolean partition) {
        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }
        List<ColumnMeta> result = new ArrayList<>(columns.size());
        int ordinal = 0;
        for (Column column : columns) {
            Map<String, Object> extra = new HashMap<>();
            if (column.getTypeInfo() != null) {
                extra.put("typeInfo", column.getTypeInfo().getTypeName());
            }
            if (column.getCategoryLabel() != null) {
                extra.put("categoryLabel", column.getCategoryLabel());
            }
            if (column.getExtendedlabels() != null && !column.getExtendedlabels().isEmpty()) {
                extra.put("extendedLabels", column.getExtendedlabels());
            }
            result.add(
                    new ColumnMeta(
                            column.getName(),
                            column.getType() != null ? column.getType().name() : null,
                            column.isNullable(),
                            column.getComment(),
                            ordinal++,
                            partition,
                            column.getDefaultValue(),
                            extra));
        }
        return result;
    }
}
