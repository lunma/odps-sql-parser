# SQL 格式化插件实现方案

## 概述

SQL 格式化插件用于将 SQL 语句格式化为统一、易读的格式。基于 ANTLR4 + AST + Token Stream，可以实现保持注释的格式化输出。

## 实现方案

### 方案一：基于 AST + Token Stream（推荐）

**优点**：
- 可以保留原始注释
- 可以保留原始格式细节（如引号类型）
- 格式化质量高

**缺点**：
- 实现复杂度较高
- 需要处理 Token 流和 AST 的映射关系

#### 实现思路

1. **保存原始 SQL 和 Token Stream**
   - 在解析时保存原始 SQL 文本
   - 保存 CommonTokenStream 对象
   - 每个 Segment 记录 startIndex 和 stopIndex

2. **格式化策略**
   - 遍历 AST 节点，根据节点类型决定格式化规则
   - 对于有原始文本的节点，使用 Token Stream 提取原始文本
   - 对于需要格式化的部分，应用格式化规则（缩进、换行等）

3. **格式化规则**
   - 关键字大写/小写（可配置）
   - 缩进层级（可配置，默认 2 或 4 空格）
   - 子句换行规则
   - 逗号位置（前置/后置）
   - 注释保留位置

#### 核心代码结构

```java
public class OdpsSQLFormatter {
    
    private final FormattingOptions options;
    private final String originalSQL;
    private final CommonTokenStream tokenStream;
    
    public OdpsSQLFormatter(String sql, CommonTokenStream tokenStream) {
        this.originalSQL = sql;
        this.tokenStream = tokenStream;
        this.options = new FormattingOptions();
    }
    
    /**
     * 格式化 SQL 语句
     */
    public String format(OdpsSQLSelectStatement statement) {
        StringBuilder sb = new StringBuilder();
        formatStatement(statement, sb, 0);
        return sb.toString();
    }
    
    private void formatStatement(OdpsSQLSelectStatement stmt, StringBuilder sb, int indent) {
        // 格式化 WITH CTE
        if (stmt.getWith().isPresent()) {
            formatWithClause(stmt.getWith().get(), sb, indent);
        }
        
        // 格式化 SELECT 子句
        formatSelectClause(stmt.getProjections(), sb, indent);
        
        // 格式化 FROM 子句
        if (stmt.getFrom() != null) {
            formatFromClause(stmt.getFrom(), sb, indent);
        }
        
        // 格式化 WHERE 子句
        if (stmt.getWhere().isPresent()) {
            formatWhereClause(stmt.getWhere().get(), sb, indent);
        }
        
        // 格式化 GROUP BY 子句
        if (stmt.getGroupBy().isPresent()) {
            formatGroupByClause(stmt.getGroupBy().get(), sb, indent);
        }
        
        // 格式化 HAVING 子句
        if (stmt.getHaving().isPresent()) {
            formatHavingClause(stmt.getHaving().get(), sb, indent);
        }
        
        // 格式化 ORDER BY 子句
        if (stmt.getOrderBy().isPresent()) {
            formatOrderByClause(stmt.getOrderBy().get(), sb, indent);
        }
        
        // 格式化 LIMIT 子句
        if (stmt.getLimit() != null) {
            formatLimitClause(stmt.getLimit(), sb, indent);
        }
    }
    
    /**
     * 从 Token Stream 提取原始文本（保留注释）
     */
    private String extractOriginalText(SQLSegment segment) {
        int start = segment.getStartIndex();
        int stop = segment.getStopIndex();
        
        // 获取该范围内的所有 Token（包括隐藏通道的注释）
        List<Token> tokens = getTokensInRange(start, stop);
        
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            if (token.getChannel() == Token.DEFAULT_CHANNEL || 
                token.getChannel() == OdpsLexer.HIDDEN) {
                sb.append(token.getText());
            }
        }
        return sb.toString();
    }
    
    /**
     * 添加缩进
     */
    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(options.getIndentString());
        }
    }
    
    /**
     * 添加换行
     */
    private void appendNewline(StringBuilder sb) {
        sb.append(options.getLineSeparator());
    }
}
```

### 方案二：基于 AST 重建（简化版）

**优点**：
- 实现相对简单
- 格式化规则统一

**缺点**：
- 可能丢失原始注释
- 可能改变原始格式细节

#### 实现思路

1. **遍历 AST 节点**
   - 使用 Visitor 模式遍历 AST
   - 根据节点类型生成对应的 SQL 文本

2. **格式化规则应用**
   - 在生成 SQL 文本时应用格式化规则
   - 控制缩进、换行、空格等

#### 核心代码结构

```java
public class OdpsSQLFormatterVisitor extends OdpsParserBaseVisitor<String> {
    
    private final FormattingOptions options;
    private int indentLevel = 0;
    
    @Override
    public String visitSelectQueryExpression(OdpsParser.SelectQueryExpressionContext ctx) {
        StringBuilder sb = new StringBuilder();
        
        // 格式化 SELECT 子句
        sb.append(formatSelectClause(ctx.selectClause()));
        
        // 格式化 FROM 子句
        if (ctx.fromClause() != null) {
            sb.append("\n");
            appendIndent(sb);
            sb.append(formatFromClause(ctx.fromClause()));
        }
        
        // 格式化 WHERE 子句
        if (ctx.whereClause() != null) {
            sb.append("\n");
            appendIndent(sb);
            sb.append(formatWhereClause(ctx.whereClause()));
        }
        
        // ... 其他子句
        
        return sb.toString();
    }
    
    private void appendIndent(StringBuilder sb) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append(options.getIndentString());
        }
    }
}
```

## 格式化选项配置

```java
public class FormattingOptions {
    
    // 缩进配置
    private int indentSize = 2;  // 缩进空格数
    private String indentString = "  ";  // 缩进字符串（空格或 Tab）
    
    // 关键字大小写
    private KeywordCase keywordCase = KeywordCase.UPPER;  // UPPER, LOWER, PRESERVE
    
    // 换行配置
    private boolean newlineBeforeAnd = true;  // AND/OR 前是否换行
    private boolean newlineAfterComma = false;  // 逗号后是否换行
    private boolean newlineBeforeComma = false;  // 逗号前是否换行
    
    // 子句换行
    private boolean newlineBeforeFrom = true;
    private boolean newlineBeforeWhere = true;
    private boolean newlineBeforeGroupBy = true;
    private boolean newlineBeforeOrderBy = true;
    
    // 注释处理
    private boolean preserveComments = true;  // 是否保留注释
    private CommentStyle commentStyle = CommentStyle.PRESERVE;  // 注释样式
    
    // 行长度限制
    private int maxLineLength = 120;  // 最大行长度
    
    // 其他选项
    private boolean alignColumns = false;  // 是否对齐列
    private boolean alignOperators = false;  // 是否对齐操作符
    
    public enum KeywordCase {
        UPPER,      // 大写
        LOWER,      // 小写
        PRESERVE    // 保持原样
    }
    
    public enum CommentStyle {
        PRESERVE,   // 保持原样
        ALIGN,      // 对齐注释
        REMOVE      // 移除注释
    }
}
```

## 格式化示例

### 输入 SQL

```sql
select t1.id,t2.name from ods.table_a t1 left join ods.table_b t2 on t1.id=t2.id where t1.status='active' group by t1.id order by t1.id limit 10;
```

### 格式化输出（方案一，保留注释）

```sql
SELECT t1.id,
       t2.name
FROM ods.table_a t1
LEFT JOIN ods.table_b t2
  ON t1.id = t2.id
WHERE t1.status = 'active'
GROUP BY t1.id
ORDER BY t1.id
LIMIT 10;
```

### 格式化输出（方案二，简化版）

```sql
SELECT
  t1.id,
  t2.name
FROM
  ods.table_a t1
  LEFT JOIN ods.table_b t2 ON t1.id = t2.id
WHERE
  t1.status = 'active'
GROUP BY
  t1.id
ORDER BY
  t1.id
LIMIT 10;
```

## 实现步骤

### 阶段一：基础格式化（基于 AST 重建）

1. **创建格式化器接口**
   ```java
   public interface SQLFormatter {
       String format(String sql);
       String format(OdpsSQLSelectStatement statement);
   }
   ```

2. **实现基础格式化器**
   - 实现 SELECT 子句格式化
   - 实现 FROM/JOIN 子句格式化
   - 实现 WHERE/GROUP BY/HAVING/ORDER BY/LIMIT 子句格式化

3. **添加格式化选项配置**
   - 创建 FormattingOptions 类
   - 支持常用格式化选项

### 阶段二：注释保留（基于 Token Stream）

1. **集成 Token Stream**
   - 在解析时保存 Token Stream
   - 实现 Token 范围提取方法

2. **注释提取和保留**
   - 从隐藏通道提取注释 Token
   - 在格式化时保留注释位置

3. **注释对齐**
   - 实现注释对齐逻辑
   - 支持注释样式配置

### 阶段三：高级功能

1. **智能换行**
   - 根据行长度自动换行
   - 长表达式自动换行

2. **对齐功能**
   - 列对齐
   - 操作符对齐

3. **自定义规则**
   - 支持自定义格式化规则
   - 支持规则配置文件

## 使用示例

```java
// 创建格式化器
OdpsSQLValidator validator = new OdpsSQLValidator();
OdpsSQLValidationResult result = validator.validate(sql);

if (result.isValid()) {
    // 获取解析后的 AST
    OdpsSQLSelectStatement statement = result.getStatement();
    
    // 创建格式化器
    FormattingOptions options = new FormattingOptions();
    options.setKeywordCase(KeywordCase.UPPER);
    options.setIndentSize(2);
    options.setPreserveComments(true);
    
    OdpsSQLFormatter formatter = new OdpsSQLFormatter(sql, tokenStream, options);
    
    // 格式化 SQL
    String formattedSQL = formatter.format(statement);
    
    System.out.println(formattedSQL);
}
```

## 注意事项

1. **性能考虑**
   - 格式化过程可能较慢，特别是对于大型 SQL
   - 考虑缓存格式化结果

2. **注释处理**
   - 注释可能出现在任意位置
   - 需要仔细处理注释与代码的关联关系

3. **格式一致性**
   - 确保格式化后的 SQL 语义不变
   - 测试格式化后的 SQL 是否能正确解析

4. **边界情况**
   - 处理不完整的 SQL
   - 处理特殊字符和转义字符
   - 处理多行字符串

## 参考实现

- **Prettier SQL**：JavaScript 实现的 SQL 格式化工具
- **sql-formatter**：Java 实现的 SQL 格式化库
- **ANTLR4 Formatter**：基于 ANTLR4 的格式化框架

## 后续优化

1. **增量格式化**：只格式化修改的部分
2. **格式化预览**：提供格式化前后的对比
3. **格式化规则学习**：从现有代码学习格式化规则
4. **多数据库支持**：支持不同数据库的格式化规则

