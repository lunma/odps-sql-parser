package com.sea.odps.service.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.aliyun.odps.Column;
import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsType;
import com.aliyun.odps.Project;
import com.aliyun.odps.ProjectFilter;
import com.aliyun.odps.Projects;
import com.aliyun.odps.Table;
import com.aliyun.odps.TableSchema;
import com.aliyun.odps.Tables;

import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.DatabaseMeta;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;

public class OdpsMetastoreConnectorTest {

    @Test
    public void listDatabasesAndTablesFromOdpsSdk() throws Exception {
        Odps odps = mock(Odps.class);
        Projects projects = mock(Projects.class);
        Tables tables = mock(Tables.class);
        when(odps.projects()).thenReturn(projects);
        when(odps.tables()).thenReturn(tables);
        when(odps.getDefaultProject()).thenReturn("defaultProject");

        Project project = mock(Project.class);
        when(project.getName()).thenReturn("analytics");
        when(project.getOwner()).thenReturn("owner");
        when(project.getComment()).thenReturn("project comment");
        when(project.getCreatedTime()).thenReturn(new Date(1_000L));
        when(project.getLastModifiedTime()).thenReturn(new Date(2_000L));
        when(project.getRegionId()).thenReturn("cn");

        Iterator<Project> projectIterator = Arrays.asList(project).iterator();
        when(projects.iteratorByFilter(any(ProjectFilter.class))).thenReturn(projectIterator);

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("orders");
        when(table.getOwner()).thenReturn("tblOwner");
        when(table.getComment()).thenReturn("orders table");
        when(table.getCreatedTime()).thenReturn(new Date(3_000L));
        when(table.getLastMetaModifiedTime()).thenReturn(new Date(4_000L));
        when(table.getSize()).thenReturn(1024L);
        when(table.getRecordNum()).thenReturn(10L);
        when(table.isPartitioned()).thenReturn(true);
        when(table.isExternalTable()).thenReturn(false);
        when(table.getType()).thenReturn(Table.TableType.MANAGED_TABLE);

        TableSchema schema = new TableSchema();
        schema.addColumn(new Column("id", OdpsType.STRING));
        schema.addPartitionColumn(new Column("ds", OdpsType.STRING));
        when(table.getSchema()).thenReturn(schema);

        Iterator<Table> tableIterator = Arrays.asList(table).iterator();
        when(tables.iterator(eq("analytics"))).thenReturn(tableIterator);

        OdpsMetastoreConnector connector = new OdpsMetastoreConnector(odps);

        List<DatabaseMeta> databases = connector.listDatabases();
        assertEquals(1, databases.size());
        DatabaseMeta database = databases.get(0);
        assertEquals("analytics", database.getName());
        assertEquals("owner", database.getOwner());
        assertEquals("project comment", database.getDescription());
        assertEquals("cn", database.getProperties().get("region"));

        List<TableMeta> tableMetas = connector.listTables("analytics");
        assertEquals(1, tableMetas.size());
        TableMeta tableMeta = tableMetas.get(0);
        assertEquals("orders", tableMeta.getName());
        assertEquals("tblOwner", tableMeta.getOwner());
        assertEquals("orders table", tableMeta.getComment());
        assertEquals("MANAGED_TABLE", tableMeta.getType());

        List<ColumnMeta> columns = tableMeta.getColumns();
        assertEquals(2, columns.size());
        ColumnMeta column = columns.get(0);
        assertEquals("id", column.getName());
        assertFalse(column.isPartition());

        ColumnMeta partitionColumn = columns.get(1);
        assertEquals("ds", partitionColumn.getName());
        assertTrue(partitionColumn.isPartition());
    }
}
