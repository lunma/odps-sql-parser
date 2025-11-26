package com.sea.odps.service.lineage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.sea.odps.service.api.model.ColumnLineageModel;
import com.sea.odps.service.api.model.ColumnSourceModel;
import com.sea.odps.service.api.model.LineageResultModel;
import com.sea.odps.service.api.model.TableLineageModel;
import com.sea.odps.service.connector.MetadataConnector.ColumnMeta;
import com.sea.odps.service.connector.MetadataConnector.TableMeta;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/** LineageModelConverter 单元测试用例。 */
public class LineageModelConverterTest {

    @Test
    public void testConvertTableLineages() {
        // 准备测试数据
        TableReference tableRef =
                new TableReference("ods", "user_table", "t1", false, "ods.user_table");
        TableMeta tableMeta =
                new TableMeta(
                        "ods",
                        "user_table",
                        "MANAGED_TABLE",
                        "用户表",
                        "owner",
                        Collections.emptyList(),
                        Collections.emptyMap());
        TableLineage tableLineage = new TableLineage(tableRef, tableMeta, "ods.user_table", "t1");

        LineageResult result =
                new LineageResult(Arrays.asList(tableLineage), Collections.emptyList());

        // 转换
        LineageResultModel model =
                LineageModelConverter.convert(result, "SELECT * FROM ods.user_table t1");

        // 验证
        List<TableLineageModel> tableLineages = model.getTableLineages();
        assertEquals(1, tableLineages.size());

        TableLineageModel tableModel = tableLineages.get(0);
        assertEquals("ods.user_table", tableModel.getQualifiedName());
        assertEquals("t1", tableModel.getAlias());
        assertEquals("ods", tableModel.getDatabase());
        assertEquals("user_table", tableModel.getTableName());
        assertEquals("MANAGED_TABLE", tableModel.getTableType());
        assertEquals("用户表", tableModel.getComment());
        assertEquals("SELECT * FROM ods.user_table t1", model.getSql());
    }

    @Test
    public void testConvertColumnLineages_FilterIntermediateFields() {
        // 准备测试数据：模拟表达式字段场景
        // t2.nvl(table_name, '1') 是中间字段
        // t2.tname 是最终输出字段，来源是 t2.nvl(table_name, '1')

        ColumnMeta tableNameColumn =
                new ColumnMeta(
                        "table_name", "STRING", true, "表名", 0, false, null, Collections.emptyMap());

        // 中间字段：t2.nvl(table_name, '1')
        ColumnSource intermediateSource =
                new ColumnSource(
                        "cicdata_meta_dev.cic_m_column", null, "table_name", tableNameColumn);
        ColumnLineage intermediateLineage =
                new ColumnLineage(
                        "t2.nvl(table_name, '1')",
                        "nvl(table_name, '1')",
                        "EXPRESSION",
                        Arrays.asList(intermediateSource),
                        false // 中间字段
                        );

        // 最终输出字段：t2.tname
        ColumnSource finalSource = new ColumnSource("t2", null, "nvl(table_name, '1')", null);
        ColumnLineage finalLineage =
                new ColumnLineage(
                        "t2.tname",
                        "nvl(table_name, '1')",
                        "EXPRESSION",
                        Arrays.asList(finalSource),
                        true // 最终输出字段
                        );

        LineageResult result =
                new LineageResult(
                        Collections.emptyList(), Arrays.asList(intermediateLineage, finalLineage));

        // 转换
        LineageResultModel model = LineageModelConverter.convert(result, null);

        // 验证：只包含最终输出字段
        List<ColumnLineageModel> columnLineages = model.getColumnLineages();
        assertEquals(1, columnLineages.size());

        ColumnLineageModel columnModel = columnLineages.get(0);
        assertEquals("t2.tname", columnModel.getTargetField());
        assertEquals("nvl(table_name, '1')", columnModel.getExpression());
        assertEquals("EXPRESSION", columnModel.getCategory());
        assertTrue(columnModel.isFinalOutput());

        // 验证来源：应该包含中间字段信息
        List<ColumnSourceModel> sources = columnModel.getSources();
        assertEquals(1, sources.size());

        ColumnSourceModel sourceModel = sources.get(0);
        assertTrue(sourceModel.isIntermediateField());
        assertEquals("t2.nvl(table_name, '1')", sourceModel.getIntermediateFieldName());
    }

    @Test
    public void testConvertColumnLineages_SimpleDirectField() {
        // 准备测试数据：简单直接字段
        ColumnMeta projectNameColumn =
                new ColumnMeta(
                        "project_name",
                        "STRING",
                        true,
                        "Project名称",
                        0,
                        false,
                        null,
                        Collections.emptyMap());

        ColumnSource source =
                new ColumnSource(
                        "cicdata_meta_dev.cic_m_column", null, "project_name", projectNameColumn);
        ColumnLineage lineage =
                new ColumnLineage(
                        "t2.project_name",
                        "project_name",
                        "DIRECT",
                        Arrays.asList(source),
                        true // 最终输出字段
                        );

        LineageResult result = new LineageResult(Collections.emptyList(), Arrays.asList(lineage));

        // 转换
        LineageResultModel model = LineageModelConverter.convert(result, null);

        // 验证
        List<ColumnLineageModel> columnLineages = model.getColumnLineages();
        assertEquals(1, columnLineages.size());

        ColumnLineageModel columnModel = columnLineages.get(0);
        assertEquals("t2.project_name", columnModel.getTargetField());
        assertEquals("project_name", columnModel.getExpression());
        assertEquals("DIRECT", columnModel.getCategory());
        assertTrue(columnModel.isFinalOutput());

        List<ColumnSourceModel> sources = columnModel.getSources();
        assertEquals(1, sources.size());

        ColumnSourceModel sourceModel = sources.get(0);
        assertFalse(sourceModel.isIntermediateField());
        assertEquals("cicdata_meta_dev.cic_m_column", sourceModel.getTableQualifiedName());
        assertEquals("project_name", sourceModel.getColumnName());
        assertEquals("STRING", sourceModel.getDataType());
        assertEquals("Project名称", sourceModel.getComment());
    }

    @Test
    public void testConvertColumnLineages_WildcardField() {
        // 准备测试数据：通配符字段
        ColumnMeta idColumn =
                new ColumnMeta("id", "BIGINT", false, "ID", 0, false, null, Collections.emptyMap());
        ColumnMeta nameColumn =
                new ColumnMeta(
                        "name", "STRING", true, "名称", 1, false, null, Collections.emptyMap());

        ColumnSource source1 = new ColumnSource("ods.user_table", "t1", "id", idColumn);
        ColumnSource source2 = new ColumnSource("ods.user_table", "t1", "name", nameColumn);
        ColumnLineage lineage =
                new ColumnLineage(
                        "t1.*", "t1.*", "DIRECT", Arrays.asList(source1, source2), true // 最终输出字段
                        );

        LineageResult result = new LineageResult(Collections.emptyList(), Arrays.asList(lineage));

        // 转换
        LineageResultModel model = LineageModelConverter.convert(result, null);

        // 验证
        List<ColumnLineageModel> columnLineages = model.getColumnLineages();
        assertEquals(1, columnLineages.size());

        ColumnLineageModel columnModel = columnLineages.get(0);
        assertEquals("t1.*", columnModel.getTargetField());
        assertEquals(2, columnModel.getSources().size());
    }

    @Test
    public void testConvert_NullResult() {
        // 测试空结果
        LineageResultModel model = LineageModelConverter.convert(null, "SELECT * FROM table");

        assertNotNull(model);
        assertEquals(0, model.getTableLineages().size());
        assertEquals(0, model.getColumnLineages().size());
        assertEquals("SELECT * FROM table", model.getSql());
    }

    @Test
    public void testConvert_EmptyResult() {
        // 测试空列表
        LineageResult result = new LineageResult(Collections.emptyList(), Collections.emptyList());

        LineageResultModel model = LineageModelConverter.convert(result, null);

        assertNotNull(model);
        assertEquals(0, model.getTableLineages().size());
        assertEquals(0, model.getColumnLineages().size());
    }
}
