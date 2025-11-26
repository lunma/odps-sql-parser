# SQL 逻辑校验原理与实现思路

## 问题背景

当 SQL 语句包含多个表的 JOIN 操作时，如果使用 `SELECT *`，可能会产生列名冲突的问题。例如：

```sql
SELECT * FROM cic_m_table t1 JOIN cic_m_column t2 ON t1.project_name = t2.project_name;
```

如果 `cic_m_table` 和 `cic_m_column` 都有 `project_name` 列，那么 `SELECT *` 会产生歧义，因为无法确定应该返回哪个表的 `project_name` 列。

## 逻辑校验原理

### 1. 校验层次

SQL 校验通常分为三个层次：

1. **语法校验（Syntax Validation）**
   - 检查 SQL 语句是否符合语法规则
   - 由 ANTLR 语法解析器完成
   - 例如：括号不匹配、关键字拼写错误等

2. **语义校验（Semantic Validation）**
   - 检查 SQL 语句的语义是否正确
   - 需要元数据信息（表结构、列信息等）
   - 例如：表是否存在、列是否存在、类型是否匹配等

3. **逻辑校验（Logical Validation）**
   - 检查 SQL 语句的逻辑是否正确
   - 需要结合元数据和业务规则
   - 例如：列名冲突、JOIN 条件合理性、权限检查等

### 2. 列名冲突检测原理

#### 2.1 问题场景

当多个表 JOIN 时，如果使用 `SELECT *`，需要检查：

1. **是否有列名冲突**
   - 多个表是否有相同的列名
   - 如果有冲突，`SELECT *` 会产生歧义

2. **是否明确指定表别名**
   - `SELECT *` → 不明确，可能产生冲突
   - `SELECT t1.*` → 明确，只返回 t1 表的列
   - `SELECT t1.col1, t2.col2` → 明确，指定了具体的列

#### 2.2 检测算法

```
算法：列名冲突检测

输入：
  - tables: 参与 JOIN 的表列表
  - selectFields: SELECT 子句中的字段列表

输出：
  - errors: 错误列表

步骤：
1. 收集所有表的列名，建立列名到表的映射
   columnToTables = {}
   for each table in tables:
     for each column in table.columns:
       if column.name not in columnToTables:
         columnToTables[column.name] = []
       columnToTables[column.name].append(table)

2. 检查 SELECT 字段
   for each field in selectFields:
     if field is wildcard (*):
       if tables.size > 1:
         // 检查是否有列名冲突
         conflicts = []
         for columnName, tableList in columnToTables:
           if tableList.size > 1:
             conflicts.append((columnName, tableList))
         
         if conflicts.size > 0:
           error = "SELECT * 在多个表 JOIN 时存在列名冲突: " + conflicts
           errors.append(error)
     
     else if field is table wildcard (t1.*):
       // 明确指定了表别名，不需要检查冲突
       continue
     
     else if field is explicit column (t1.col1):
       // 明确指定了表和列，不需要检查冲突
       continue
```

### 3. 实现思路

#### 3.1 架构设计

```
┌─────────────────────────────────────────────────────────┐
│              SQL Logical Validator                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────┐    ┌──────────────────┐        │
│  │  Column Conflict │    │  Table Existence  │        │
│  │     Checker      │    │     Checker       │        │
│  └──────────────────┘    └──────────────────┘        │
│                                                         │
│  ┌──────────────────┐    ┌──────────────────┐        │
│  │  Join Condition  │    │  Type Validation │        │
│  │     Checker      │    │     Checker       │        │
│  └──────────────────┘    └──────────────────┘        │
│                                                         │
└─────────────────────────────────────────────────────────┘
           │                    │
           │                    │
           ▼                    ▼
┌──────────────────┐  ┌──────────────────┐
│  Metadata        │  │  Validation      │
│  Connector       │  │  Result          │
└──────────────────┘  └──────────────────┘
```

#### 3.2 核心组件

1. **LogicalValidator**
   - 主校验器，协调各个子校验器
   - 输入：SQL 元数据（OdpsSQLMetadata）+ 元数据连接器（MetadataConnector）
   - 输出：校验结果（ValidationResult）

2. **ColumnConflictChecker**
   - 列名冲突检测器
   - 检测 `SELECT *` 在 JOIN 时的列名冲突

3. **TableExistenceChecker**
   - 表存在性检测器
   - 验证表是否存在

4. **JoinConditionChecker**
   - JOIN 条件检测器
   - 验证 JOIN 条件中的列是否存在、类型是否匹配

5. **TypeCompatibilityChecker**
   - 类型兼容性检测器
   - 验证表达式中的类型是否兼容

#### 3.3 实现步骤

**步骤 1：创建校验器接口**

```java
public interface LogicalValidator {
    /**
     * 校验 SQL 逻辑
     * 
     * @param sqlMetadata SQL 元数据
     * @param metadataConnector 元数据连接器
     * @return 校验结果
     */
    ValidationResult validate(OdpsSQLMetadata sqlMetadata, MetadataConnector metadataConnector);
}
```

**步骤 2：实现列名冲突检测器**

```java
public class ColumnConflictChecker {
    
    /**
     * 检测 SELECT * 在 JOIN 时的列名冲突
     */
    public List<ValidationError> checkWildcardConflict(
            OdpsSQLMetadata sqlMetadata,
            List<TableLineage> tableLineages) {
        
        List<ValidationError> errors = new ArrayList<>();
        
        // 1. 检查是否有多个表
        if (tableLineages.size() <= 1) {
            return errors; // 单个表，不需要检查冲突
        }
        
        // 2. 检查 SELECT 字段中是否有通配符
        boolean hasWildcard = false;
        String wildcardTableAlias = null;
        
        for (FieldMetadata field : sqlMetadata.getFields()) {
            String expression = field.getExpression();
            if ("*".equals(expression)) {
                hasWildcard = true;
                // 检查是否有表别名
                for (ColumnReference colRef : field.getSourceColumns()) {
                    if ("*".equals(colRef.getName()) && colRef.getOwner() != null) {
                        wildcardTableAlias = colRef.getOwner();
                        break;
                    }
                }
                break;
            }
        }
        
        // 3. 如果没有通配符，或者通配符有明确的表别名，不需要检查冲突
        if (!hasWildcard || wildcardTableAlias != null) {
            return errors;
        }
        
        // 4. 收集所有表的列名，检测冲突
        Map<String, List<TableLineage>> columnToTables = new HashMap<>();
        
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
        
        // 5. 检查冲突
        List<String> conflicts = new ArrayList<>();
        for (Map.Entry<String, List<TableLineage>> entry : columnToTables.entrySet()) {
            String columnName = entry.getKey();
            List<TableLineage> tables = entry.getValue();
            
            if (tables.size() > 1) {
                // 有多个表包含相同的列名
                String tableNames = tables.stream()
                    .map(t -> t.getQualifiedName())
                    .collect(Collectors.joining(", "));
                conflicts.add(String.format("列 '%s' 在多个表中存在: %s", columnName, tableNames));
            }
        }
        
        // 6. 如果有冲突，生成错误
        if (!conflicts.isEmpty()) {
            String message = String.format(
                "SELECT * 在多个表 JOIN 时存在列名冲突。冲突的列: %s。请使用表别名明确指定，如 SELECT t1.* 或 SELECT t1.col1, t2.col2",
                String.join("; ", conflicts)
            );
            errors.add(new ValidationError(
                ValidationErrorType.COLUMN_CONFLICT,
                message,
                0, 0 // 行号和列号可以从 SQL 元数据中获取
            ));
        }
        
        return errors;
    }
}
```

**步骤 3：实现主校验器**

```java
public class OdpsLogicalValidator implements LogicalValidator {
    
    private final ColumnConflictChecker columnConflictChecker;
    private final TableExistenceChecker tableExistenceChecker;
    private final JoinConditionChecker joinConditionChecker;
    
    public OdpsLogicalValidator() {
        this.columnConflictChecker = new ColumnConflictChecker();
        this.tableExistenceChecker = new TableExistenceChecker();
        this.joinConditionChecker = new JoinConditionChecker();
    }
    
    @Override
    public ValidationResult validate(
            OdpsSQLMetadata sqlMetadata,
            MetadataConnector metadataConnector) throws MetadataException {
        
        List<ValidationError> errors = new ArrayList<>();
        
        // 1. 提取表血缘（需要元数据连接器）
        LineageExtractor extractor = new LineageExtractor(metadataConnector);
        LineageExtractor.LineageResult lineageResult = extractor.extract(sqlMetadata);
        List<TableLineage> tableLineages = lineageResult.getTableLineages();
        
        // 2. 列名冲突检测
        errors.addAll(columnConflictChecker.checkWildcardConflict(sqlMetadata, tableLineages));
        
        // 3. 表存在性检测
        errors.addAll(tableExistenceChecker.checkTableExistence(tableLineages));
        
        // 4. JOIN 条件检测
        errors.addAll(joinConditionChecker.checkJoinConditions(sqlMetadata, tableLineages));
        
        // 5. 返回校验结果
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

**步骤 4：集成到现有系统**

```java
// 在 LineageApp 或类似的应用中集成
public class LineageApp {
    
    public void validateSQL(String sql, MetadataConnector connector) {
        // 1. 语法校验
        OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
        OdpsSQLMetadataResult syntaxResult = validator.validate(sql);
        
        if (!syntaxResult.isValid()) {
            System.out.println("语法错误: " + syntaxResult.getErrors());
            return;
        }
        
        // 2. 逻辑校验
        OdpsSQLMetadata metadata = syntaxResult.getMetadata().orElse(null);
        if (metadata != null) {
            LogicalValidator logicalValidator = new OdpsLogicalValidator();
            ValidationResult logicalResult = logicalValidator.validate(metadata, connector);
            
            if (!logicalResult.isValid()) {
                System.out.println("逻辑错误:");
                for (ValidationError error : logicalResult.getErrors()) {
                    System.out.println("  - " + error.getMessage());
                }
            } else {
                System.out.println("✓ SQL 逻辑校验通过");
            }
        }
    }
}
```

## 校验规则示例

### 规则 1：SELECT * 列名冲突

**场景**：多个表 JOIN 时使用 `SELECT *`

**规则**：
- 如果多个表有相同的列名，且 `SELECT *` 没有指定表别名，则报错
- 如果 `SELECT t1.*` 明确指定了表别名，则允许

**示例**：
```sql
-- ❌ 错误：列名冲突
SELECT * FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;
-- 如果 table1 和 table2 都有 name 列，会产生冲突

-- ✅ 正确：明确指定表别名
SELECT t1.* FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;

-- ✅ 正确：明确指定列
SELECT t1.name, t2.name FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id;
```

### 规则 2：表存在性检查

**场景**：验证 SQL 中引用的表是否存在

**规则**：
- 所有表必须存在于元数据服务中
- 如果表不存在，报错

### 规则 3：JOIN 条件检查

**场景**：验证 JOIN 条件中的列是否存在

**规则**：
- JOIN 条件中的列必须存在于对应的表中
- JOIN 条件中的列类型应该兼容（可以比较）

**示例**：
```sql
-- ❌ 错误：列不存在
SELECT * FROM table1 t1 JOIN table2 t2 ON t1.non_existent_col = t2.id;

-- ❌ 错误：类型不兼容（取决于具体实现）
SELECT * FROM table1 t1 JOIN table2 t2 ON t1.id = t2.name; -- id 是数字，name 是字符串
```

## 扩展性考虑

### 1. 可配置的校验规则

```java
public class ValidationConfig {
    private boolean checkColumnConflict = true;
    private boolean checkTableExistence = true;
    private boolean checkJoinConditions = true;
    private boolean strictMode = false; // 严格模式，更多检查
    
    // getters and setters
}
```

### 2. 自定义校验器

```java
public interface CustomValidator {
    List<ValidationError> validate(OdpsSQLMetadata metadata, MetadataConnector connector);
}

// 用户可以注册自定义校验器
validator.addCustomValidator(new MyCustomValidator());
```

### 3. 错误级别

```java
public enum ValidationErrorLevel {
    ERROR,    // 错误，必须修复
    WARNING,  // 警告，建议修复
    INFO     // 信息，仅供参考
}
```

## 总结

SQL 逻辑校验的核心是：

1. **利用元数据信息**：通过 MetadataConnector 获取表结构信息
2. **检测列名冲突**：当多个表 JOIN 时，检查 `SELECT *` 是否会产生歧义
3. **提供明确错误**：给出具体的错误信息和修复建议
4. **可扩展架构**：支持添加更多的校验规则

实现时，可以基于现有的 `LineageExtractor` 来获取表血缘信息，然后在此基础上进行逻辑校验。

