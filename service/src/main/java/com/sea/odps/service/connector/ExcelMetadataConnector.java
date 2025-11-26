package com.sea.odps.service.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Excel 导入连接器，支持从模板化 Excel 中读取 Catalog/Database/Table/Column 信息。
 *
 * <p>适用于没有可用元数据 API、但能提供表结构 Excel 清单的场景。
 */
public final class ExcelMetadataConnector implements MetadataConnector {

    private final String workbookName;
    private final ExcelReader reader;

    public ExcelMetadataConnector(String workbookName, ExcelReader reader) {
        this.workbookName = workbookName;
        this.reader = reader;
    }

    @Override
    public String id() {
        return "excel-" + workbookName;
    }

    @Override
    public CatalogType catalogType() {
        return CatalogType.EXCEL;
    }

    @Override
    public List<DatabaseMeta> listDatabases() throws MetadataException {
        try {
            List<ExcelRow> rows = reader.rows();
            Map<String, DatabaseBuilder> builderMap = new LinkedHashMap<>();
            for (ExcelRow row : rows) {
                DatabaseBuilder builder =
                        builderMap.computeIfAbsent(
                                row.database(),
                                name -> new DatabaseBuilder(name, row.dbOwner(), row.dbComment()));
                builder.add(row);
            }
            return builderMap.values().stream()
                    .map(DatabaseBuilder::build)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new MetadataException("Failed to read excel workbook: " + workbookName, ex);
        }
    }

    @Override
    public List<TableMeta> listTables(String databaseName) throws MetadataException {
        return listDatabases().stream()
                .filter(db -> Objects.equals(db.getName(), databaseName))
                .findFirst()
                .map(
                        db ->
                                db.getProperties().containsKey("_tables")
                                        ? (List<TableMeta>) db.getProperties().get("_tables")
                                        : Collections.<TableMeta>emptyList())
                .orElse(Collections.emptyList());
    }

    @Override
    public DatabaseMeta getDatabase(String databaseName) throws MetadataException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        return listDatabases().stream()
                .filter(db -> Objects.equals(db.getName(), databaseName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public TableMeta getTable(String databaseName, String tableName) throws MetadataException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        Objects.requireNonNull(tableName, "tableName must not be null");
        List<TableMeta> tables = listTables(databaseName);
        return tables.stream()
                .filter(table -> Objects.equals(table.getName(), tableName))
                .findFirst()
                .orElse(null);
    }

    private static final class DatabaseBuilder {
        private final String name;
        private final String owner;
        private final String description;
        private final Map<String, List<ExcelRow>> tables = new LinkedHashMap<>();

        private DatabaseBuilder(String name, String owner, String description) {
            this.name = name;
            this.owner = owner;
            this.description = description;
        }

        private void add(ExcelRow row) {
            tables.computeIfAbsent(row.table(), key -> new ArrayList<>()).add(row);
        }

        private DatabaseMeta build() {
            List<TableMeta> tableMetas =
                    tables.entrySet().stream()
                            .map(
                                    entry -> {
                                        List<ExcelRow> rows = entry.getValue();
                                        List<ColumnMeta> columns = new ArrayList<>();
                                        for (ExcelRow row : rows) {
                                            columns.add(
                                                    new ColumnMeta(
                                                            row.column(),
                                                            row.dataType(),
                                                            row.nullable(),
                                                            row.columnComment(),
                                                            row.ordinal(),
                                                            row.partition(),
                                                            row.defaultValue(),
                                                            Collections.singletonMap(
                                                                    "sourceSheet", row.sheet())));
                                        }
                                        ExcelRow first = rows.get(0);
                                        Map<String, Object> tableProps = new HashMap<>();
                                        tableProps.put("sourceSheet", first.sheet());
                                        return new TableMeta(
                                                name,
                                                entry.getKey(),
                                                first.tableType(),
                                                first.tableComment(),
                                                first.tableOwner(),
                                                columns,
                                                tableProps);
                                    })
                            .collect(Collectors.toList());
            Map<String, Object> dbProps = new HashMap<>();
            dbProps.put("_tables", tableMetas);
            return new DatabaseMeta(name, owner, description, dbProps);
        }
    }

    /** Excel Reader 抽象，方便使用 Apache POI / EasyExcel 等实现。 */
    public interface ExcelReader {
        /** @return Excel 中的每一行，需按模板约定解析。 */
        List<ExcelRow> rows() throws IOException;

        /** 当需从 InputStream 构造时使用此工厂方法。 */
        static ExcelReader of(InputStream in, ExcelRowMapper mapper) {
            return () -> mapper.map(in);
        }
    }

    /** Excel 行模型，假定模板列包含 catalog/database/table/column 的所有字段。 */
    public static final class ExcelRow {
        private final String sheet;
        private final String catalog;
        private final String database;
        private final String dbOwner;
        private final String dbComment;
        private final String table;
        private final String tableOwner;
        private final String tableType;
        private final String tableComment;
        private final String column;
        private final String dataType;
        private final boolean nullable;
        private final String columnComment;
        private final Integer ordinal;
        private final boolean partition;
        private final String defaultValue;

        public ExcelRow(
                String sheet,
                String catalog,
                String database,
                String dbOwner,
                String dbComment,
                String table,
                String tableOwner,
                String tableType,
                String tableComment,
                String column,
                String dataType,
                boolean nullable,
                String columnComment,
                Integer ordinal,
                boolean partition,
                String defaultValue) {
            this.sheet = sheet;
            this.catalog = catalog;
            this.database = database;
            this.dbOwner = dbOwner;
            this.dbComment = dbComment;
            this.table = table;
            this.tableOwner = tableOwner;
            this.tableType = tableType;
            this.tableComment = tableComment;
            this.column = column;
            this.dataType = dataType;
            this.nullable = nullable;
            this.columnComment = columnComment;
            this.ordinal = ordinal;
            this.partition = partition;
            this.defaultValue = defaultValue;
        }

        public String sheet() {
            return sheet;
        }

        public String catalog() {
            return catalog;
        }

        public String database() {
            return database;
        }

        public String dbOwner() {
            return dbOwner;
        }

        public String dbComment() {
            return dbComment;
        }

        public String table() {
            return table;
        }

        public String tableOwner() {
            return tableOwner;
        }

        public String tableType() {
            return tableType;
        }

        public String tableComment() {
            return tableComment;
        }

        public String column() {
            return column;
        }

        public String dataType() {
            return dataType;
        }

        public boolean nullable() {
            return nullable;
        }

        public String columnComment() {
            return columnComment;
        }

        public Integer ordinal() {
            return ordinal;
        }

        public boolean partition() {
            return partition;
        }

        public String defaultValue() {
            return defaultValue;
        }
    }

    @FunctionalInterface
    public interface ExcelRowMapper {
        List<ExcelRow> map(InputStream inputStream) throws IOException;
    }
}
