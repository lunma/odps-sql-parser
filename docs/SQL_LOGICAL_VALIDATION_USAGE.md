# SQL 逻辑校验使用指南

## 快速开始

### 基本使用

```java
import com.sea.odps.service.validation.OdpsLogicalValidator;
import com.sea.odps.service.validation.ValidationResult;
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.service.connector.MetadataConnector;

// 1. 准备 SQL 和元数据连接器
String sql = "SELECT * FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;";
        MetadataConnector connector = ...; // 你的元数据连接器

        // 2. 语法校验（获取 SQL 元数据）
        OdpsSQLMetadataEntrypoint syntaxValidator = new OdpsSQLMetadataEntrypoint();
        OdpsSQLMetadataResult syntaxResult = syntaxValidator.validate(sql);

if(!syntaxResult.

        isValid()){
        System.out.

        println("语法错误: "+syntaxResult.getErrors());
        return;
        }

        // 3. 逻辑校验
        OdpsSQLMetadata metadata = syntaxResult.getMetadata().orElse(null);
if(metadata !=null){
        OdpsLogicalValidator logicalValidator = new OdpsLogicalValidator();
        ValidationResult logicalResult = logicalValidator.validate(metadata, connector);
    
    if(!logicalResult.

        isValid()){
        System.out.

        println("逻辑错误:");
        for(
        ValidationError error :logicalResult.

        getErrors()){
        System.out.

        println("  - "+error.getMessage());
        }
        }else{
        System.out.

        println("✓ SQL 逻辑校验通过");
    }
            }
```

## 校验规则说明

### 规则 1：SELECT * 列名冲突检测

**问题场景**：
```sql
SELECT * FROM cic_m_table t1 JOIN cic_m_column t2 ON t1.project_name = t2.project_name;
```

**检测逻辑**：
1. 检查是否有多个表参与 JOIN
2. 检查 SELECT 子句是否包含 `*`（没有表别名）
3. 收集所有表的列名
4. 检测是否有列名冲突（多个表有相同的列名）
5. 如果有冲突，生成错误信息

**错误示例**：
```
SELECT * 在多个表 JOIN 时存在列名冲突。冲突的列: 列 'project_name' 在多个表中存在: ods.cic_m_table, ods.cic_m_column。请使用表别名明确指定，如 SELECT t1.* 或 SELECT t1.col1, t2.col2
```

**正确的写法**：
```sql
-- 方式 1：使用表别名通配符
SELECT t1.* FROM cic_m_table t1 JOIN cic_m_column t2 ON t1.project_name = t2.project_name;

-- 方式 2：明确指定列
SELECT t1.project_name, t2.project_name FROM cic_m_table t1 JOIN cic_m_column t2 ON t1.project_name = t2.project_name;
```

## 实现原理

### 1. 校验流程

```
SQL 文本
  ↓
语法校验 (OdpsSQLMetadataEntrypoint)
  ↓
SQL 元数据 (OdpsSQLMetadata)
  ↓
逻辑校验 (OdpsLogicalValidator)
  ├── 提取表血缘 (LineageExtractor)
  ├── 列名冲突检测 (ColumnConflictChecker)
  ├── 表存在性检测 (可扩展)
  └── JOIN 条件检测 (可扩展)
  ↓
校验结果 (ValidationResult)
```

### 2. 列名冲突检测算法

```java
算法：列名冲突检测

输入：
  - sqlMetadata: SQL 元数据
  - tableLineages: 表血缘列表

输出：
  - errors: 错误列表

步骤：
1. 如果表数量 <= 1，返回空错误列表（单个表不需要检查）

2. 检查 SELECT 字段中是否有通配符
   - 遍历所有字段，查找 expression == "*"
   - 如果找到，检查是否有表别名（ColumnReference.owner）
   - 如果有表别名（如 t1.*），不需要检查冲突
   - 如果没有表别名（如 *），需要检查冲突

3. 收集所有表的列名
   - 遍历所有表血缘
   - 对每个表，收集所有列名
   - 建立列名到表的映射：columnName -> [table1, table2, ...]

4. 检测冲突
   - 遍历列名映射
   - 如果某个列名对应多个表，记录冲突

5. 生成错误
   - 如果有冲突，生成 ValidationError
   - 错误类型：COLUMN_CONFLICT
   - 错误信息：列出所有冲突的列和对应的表
```

### 3. 核心代码

```java
// 检测列名冲突的核心逻辑
Map<String, List<TableLineage>> columnToTables = new HashMap<>();

// 收集所有表的列名
for (TableLineage lineage : tableLineages) {
    TableMeta tableMeta = lineage.getTableMeta();
    if (tableMeta != null && tableMeta.getColumns() != null) {
        for (ColumnMeta column : tableMeta.getColumns()) {
            String columnName = column.getName();
            columnToTables.computeIfAbsent(columnName, k -> new ArrayList<>())
                          .add(lineage);
        }
    }
}

// 检查冲突
for (Map.Entry<String, List<TableLineage>> entry : columnToTables.entrySet()) {
    String columnName = entry.getKey();
    List<TableLineage> tables = entry.getValue();
    
    if (tables.size() > 1) {
        // 有冲突！
        String tableNames = tables.stream()
            .map(t -> t.getQualifiedName())
            .collect(Collectors.joining(", "));
        conflicts.add(String.format("列 '%s' 在多个表中存在: %s", columnName, tableNames));
    }
}
```

## 扩展校验规则

### 1. 表存在性检测

```java
public class TableExistenceChecker {
    public List<ValidationError> checkTableExistence(
            List<TableLineage> tableLineages) {
        List<ValidationError> errors = new ArrayList<>();
        
        for (TableLineage lineage : tableLineages) {
            if (lineage.getTableMeta() == null) {
                errors.add(new ValidationError(
                    ValidationErrorType.TABLE_NOT_FOUND,
                    String.format("表 '%s' 不存在", lineage.getQualifiedName()),
                    0, 0
                ));
            }
        }
        
        return errors;
    }
}
```

### 2. JOIN 条件检测

```java
public class JoinConditionChecker {
    public List<ValidationError> checkJoinConditions(
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages) {
        List<ValidationError> errors = new ArrayList<>();
        
        // 建立表别名到表血缘的映射
        Map<String, TableLineage> aliasToLineage = new HashMap<>();
        for (TableLineage lineage : tableLineages) {
            if (lineage.getTableRef().getAlias() != null) {
                aliasToLineage.put(lineage.getTableRef().getAlias(), lineage);
            }
        }
        
        // 检查 JOIN 条件中的列是否存在
        for (JoinRelation join : sqlMetadata.getJoins()) {
            for (JoinConditionPair pair : join.getColumnPairs()) {
                ColumnReference left = pair.getLeft();
                ColumnReference right = pair.getRight();
                
                // 检查左列是否存在
                if (left.getOwner() != null) {
                    TableLineage leftTable = aliasToLineage.get(left.getOwner());
                    if (leftTable != null && !columnExists(leftTable, left.getName())) {
                        errors.add(new ValidationError(
                            ValidationErrorType.JOIN_CONDITION_ERROR,
                            String.format("JOIN 条件中的列 '%s' 在表 '%s' 中不存在", 
                                left.getName(), leftTable.getQualifiedName()),
                            0, 0
                        ));
                    }
                }
                
                // 检查右列是否存在（类似逻辑）
                // ...
            }
        }
        
        return errors;
    }
    
    private boolean columnExists(TableLineage lineage, String columnName) {
        TableMeta tableMeta = lineage.getTableMeta();
        if (tableMeta == null || tableMeta.getColumns() == null) {
            return false;
        }
        return tableMeta.getColumns().stream()
            .anyMatch(col -> columnName.equals(col.getName()));
    }
}
```

## 集成到现有系统

### 在 LineageApp 中集成

```java
public class LineageApp {
    
    public void validateAndExtractLineage(String sql, MetadataConnector connector) {
        // 1. 语法校验
        OdpsSQLMetadataEntrypoint syntaxValidator = new OdpsSQLMetadataEntrypoint();
        OdpsSQLMetadataResult syntaxResult = syntaxValidator.validate(sql);
        
        if (!syntaxResult.isValid()) {
            System.err.println("❌ SQL 语法错误:");
            for (OdpsSQLMetadataError error : syntaxResult.getErrors()) {
                System.err.println("  - " + error.getMessage());
            }
            return;
        }
        
        // 2. 逻辑校验
        OdpsSQLMetadata metadata = syntaxResult.getMetadata().orElse(null);
        if (metadata != null) {
            OdpsLogicalValidator logicalValidator = new OdpsLogicalValidator();
            try {
                ValidationResult logicalResult = logicalValidator.validate(metadata, connector);
                
                if (!logicalResult.isValid()) {
                    System.err.println("❌ SQL 逻辑错误:");
                    for (ValidationError error : logicalResult.getErrors()) {
                        System.err.println("  - " + error.getMessage());
                    }
                    return; // 逻辑错误，不继续提取血缘
                }
            } catch (MetadataException e) {
                System.err.println("❌ 元数据服务错误: " + e.getMessage());
                return;
            }
        }
        
        // 3. 提取血缘关系
        LineageExtractor extractor = new LineageExtractor(connector);
        try {
            LineageExtractor.LineageResult result = extractor.extract(metadata);
            // 显示血缘关系...
        } catch (MetadataException e) {
            System.err.println("❌ 血缘提取错误: " + e.getMessage());
        }
    }
}
```

## 总结

SQL 逻辑校验的核心是：

1. **利用元数据信息**：通过 MetadataConnector 获取表结构信息
2. **检测列名冲突**：当多个表 JOIN 时，检查 `SELECT *` 是否会产生歧义
3. **提供明确错误**：给出具体的错误信息和修复建议
4. **可扩展架构**：支持添加更多的校验规则

通过逻辑校验，可以在 SQL 执行前发现潜在的问题，提高 SQL 的质量和可维护性。

