package com.sea.odps.service.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelReader;
import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelRow;

public class ExcelMetadataConnectorTest {

    private ExcelMetadataConnector newConnector(List<ExcelRow> rows) {
        ExcelReader reader = () -> rows;
        return new ExcelMetadataConnector("workbook.xlsx", reader);
    }

    @Test
    public void listDatabasesAggregatesRows() throws Exception {
        List<ExcelRow> rows =
                Arrays.asList(
                        new ExcelRow(
                                "sheet1",
                                "catalog",
                                "analytics",
                                "owner",
                                "db comment",
                                "orders",
                                "tblOwner",
                                "MANAGED",
                                "orders table",
                                "id",
                                "STRING",
                                true,
                                "identifier",
                                0,
                                false,
                                null),
                        new ExcelRow(
                                "sheet1",
                                "catalog",
                                "analytics",
                                "owner",
                                "db comment",
                                "orders",
                                "tblOwner",
                                "MANAGED",
                                "orders table",
                                "ds",
                                "STRING",
                                false,
                                "partition",
                                1,
                                true,
                                null));

        ExcelMetadataConnector connector = newConnector(rows);
        List<MetadataConnector.DatabaseMeta> databases = connector.listDatabases();

        assertEquals(1, databases.size());
        MetadataConnector.DatabaseMeta database = databases.get(0);
        assertEquals("analytics", database.getName());
        assertEquals("owner", database.getOwner());
        assertTrue(database.getProperties().containsKey("_tables"));
        @SuppressWarnings("unchecked")
        List<MetadataConnector.TableMeta> tables =
                (List<MetadataConnector.TableMeta>) database.getProperties().get("_tables");
        assertEquals(1, tables.size());
        MetadataConnector.TableMeta table = tables.get(0);
        assertEquals("orders", table.getName());
        assertEquals(2, table.getColumns().size());
        assertEquals("sheet1", table.getProperties().get("sourceSheet"));
    }

    @Test
    public void listTablesReturnsTablesForDatabase() throws Exception {
        List<ExcelRow> rows =
                Arrays.asList(
                        new ExcelRow(
                                "sheet1",
                                "catalog",
                                "analytics",
                                "owner",
                                "db comment",
                                "orders",
                                "tblOwner",
                                "MANAGED",
                                "orders table",
                                "id",
                                "STRING",
                                true,
                                "identifier",
                                0,
                                false,
                                null),
                        new ExcelRow(
                                "sheet2",
                                "catalog",
                                "marketing",
                                "owner2",
                                "db2",
                                "campaigns",
                                "owner2",
                                "MANAGED",
                                "campaign table",
                                "campaign_id",
                                "STRING",
                                true,
                                "cid",
                                0,
                                false,
                                null));

        ExcelMetadataConnector connector = newConnector(rows);
        List<MetadataConnector.TableMeta> tables = connector.listTables("analytics");

        assertEquals(1, tables.size());
        MetadataConnector.TableMeta table = tables.get(0);
        assertEquals("orders", table.getName());
        assertEquals("orders table", table.getComment());
        assertEquals(1, table.getColumns().size());
    }
}
