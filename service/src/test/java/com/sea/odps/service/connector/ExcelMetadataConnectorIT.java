package com.sea.odps.service.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;

import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelReader;
import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelRow;

/**
 * 使用真实 Excel 文件驱动 {@link ExcelMetadataConnector} 的集成测试。
 *
 * <p>Excel 文件位于 {@code src/test/resources/metadata_sample.xlsx}，可根据需要替换成 业务实际的结构清单。
 */
public class ExcelMetadataConnectorIT {

    @Test
    public void readSampleExcelAndParseMetadata() throws Exception {
        ExcelReader reader = () -> loadRowsFromExcel("metadata_sample.xlsx");
        ExcelMetadataConnector connector =
                new ExcelMetadataConnector("metadata_sample.xlsx", reader);

        List<MetadataConnector.DatabaseMeta> databases = connector.listDatabases();
        assertEquals(1, databases.size());

        MetadataConnector.DatabaseMeta database = databases.get(0);
        assertEquals("analytics", database.getName());

        List<MetadataConnector.TableMeta> tables = connector.listTables("analytics");
        assertEquals(1, tables.size());
        MetadataConnector.TableMeta table = tables.get(0);
        assertEquals("orders", table.getName());
        assertFalse(table.getColumns().isEmpty());
    }

    private List<ExcelRow> loadRowsFromExcel(String resource) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
        if (input == null) {
            throw new IllegalStateException("无法找到测试 Excel 文件: " + resource);
        }
        List<ExcelResourceRow> rows =
                EasyExcel.read(input).head(ExcelResourceRow.class).sheet(0).doReadSync();
        return rows.stream().map(ExcelResourceRow::toExcelRow).collect(Collectors.toList());
    }

    public static class ExcelResourceRow {
        @ExcelProperty("Sheet")
        private String sheet;

        @ExcelProperty("Catalog")
        private String catalog;

        @ExcelProperty("Database")
        private String database;

        @ExcelProperty("DbOwner")
        private String dbOwner;

        @ExcelProperty("DbComment")
        private String dbComment;

        @ExcelProperty("Table")
        private String table;

        @ExcelProperty("TableOwner")
        private String tableOwner;

        @ExcelProperty("TableType")
        private String tableType;

        @ExcelProperty("TableComment")
        private String tableComment;

        @ExcelProperty("Column")
        private String column;

        @ExcelProperty("DataType")
        private String dataType;

        @ExcelProperty("Nullable")
        private String nullable;

        @ExcelProperty("ColumnComment")
        private String columnComment;

        @ExcelProperty("Ordinal")
        private String ordinal;

        @ExcelProperty("Partition")
        private String partition;

        @ExcelProperty("DefaultValue")
        private String defaultValue;

        ExcelRow toExcelRow() {
            return new ExcelRow(
                    sheet,
                    catalog,
                    database,
                    dbOwner,
                    dbComment,
                    table,
                    tableOwner,
                    tableType,
                    tableComment,
                    column,
                    dataType,
                    Boolean.parseBoolean(nullable),
                    columnComment,
                    parseInteger(ordinal),
                    Boolean.parseBoolean(partition),
                    defaultValue);
        }

        private Integer parseInteger(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            return Integer.parseInt(value);
        }
    }
}
