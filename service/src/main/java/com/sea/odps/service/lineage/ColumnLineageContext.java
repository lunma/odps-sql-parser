package com.sea.odps.service.lineage;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/**
 * 字段血缘提取的辅助数据结构。
 *
 * <p>用于在字段血缘提取过程中共享和传递上下文信息。
 */
class ColumnLineageContext {
    /** 表键到表血缘的映射（用于通过 buildTableKey 查找）。 */
    final Map<String, TableLineage> tableLineageMap;

    /** 表引用到表血缘的直接映射（用于通过 TableReference.equals 查找，O(1) 性能）。 */
    final Map<TableReference, TableLineage> tableRefToLineageMap;

    /** 表别名到表血缘的映射（用于通过别名快速查找）。 */
    final Map<String, TableLineage> aliasToLineageMap;

    /** 表名到表血缘的映射（用于通过表名快速查找）。 */
    final Map<String, TableLineage> nameToLineageMap;

    final Map<String, TableReference> subqueryTableMap;
    final Set<String> subqueryInnerTableAliases;
    final Set<String> outerQueryTableAliases;
    final Map<String, List<FieldMetadata>> fieldsByScope;

    ColumnLineageContext(
            Map<String, TableLineage> tableLineageMap,
            Map<TableReference, TableLineage> tableRefToLineageMap,
            Map<String, TableLineage> aliasToLineageMap,
            Map<String, TableLineage> nameToLineageMap,
            Map<String, TableReference> subqueryTableMap,
            Set<String> subqueryInnerTableAliases,
            Set<String> outerQueryTableAliases,
            Map<String, List<FieldMetadata>> fieldsByScope) {
        this.tableLineageMap = tableLineageMap;
        this.tableRefToLineageMap = tableRefToLineageMap;
        this.aliasToLineageMap = aliasToLineageMap;
        this.nameToLineageMap = nameToLineageMap;
        this.subqueryTableMap = subqueryTableMap;
        this.subqueryInnerTableAliases = subqueryInnerTableAliases;
        this.outerQueryTableAliases = outerQueryTableAliases;
        this.fieldsByScope = fieldsByScope;
    }
}
