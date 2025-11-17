# 注释提取方式对比

## 方式一：统一提取（当前实现）

### 实现位置
- **提取阶段**：`OdpsAstBuilderVisitor.extractComments()` - 在 `visitSelectQueryExpression()` 中统一提取
- **关联阶段**：`OdpsSQLMetadataExtractor.associateCommentWithElement()` - 在元数据提取时关联

### 工作流程
```
SQL 解析 → AST 构建 → 统一提取所有注释 → 元数据提取 → 根据位置关联注释与元素
```

### 代码示例
```java
// OdpsAstBuilderVisitor.java
@Override
public ASTNode visitSelectQueryExpression(...) {
    OdpsSQLSelectStatement selectStatement = new OdpsSQLSelectStatement();
    // ... 构建 AST ...
    
    // 统一提取所有注释
    extractComments(ctx, selectStatement);
    return selectStatement;
}

private void extractComments(ParserRuleContext ctx, AbstractSQLStatement statement) {
    // 遍历整个上下文范围内的所有 token，提取注释
    for (Token token : allTokens) {
        if (token.getChannel() == COMMENT_CHANNEL) {
            statement.addCommentSegment(new CommentSegment(...));
        }
    }
}
```

### 优点
1. ✅ **集中管理**：所有注释在一个地方提取，逻辑清晰
2. ✅ **性能较好**：只需遍历一次 token stream
3. ✅ **易于维护**：注释提取逻辑集中，修改方便
4. ✅ **解耦设计**：AST 构建和注释提取分离，职责清晰

### 缺点
1. ❌ **关联延迟**：注释与元素的关联在元数据提取阶段完成，需要位置计算
2. ❌ **关联不精确**：通过位置距离计算关联，可能不够准确
3. ❌ **上下文丢失**：提取时不知道注释的具体上下文（是注释表还是列）

---

## 方式二：分散提取（在 visit 时提取）

### 实现位置
- **提取阶段**：在各个 `visit*` 方法中，访问节点时立即提取附近的注释
- **关联阶段**：提取时直接关联到当前正在处理的元素

### 工作流程
```
SQL 解析 → visitTableName() → 提取表附近的注释 → 直接关联到表
        → visitSelectItem() → 提取字段附近的注释 → 直接关联到字段
        → visitWhereClause() → 提取 WHERE 附近的注释 → 直接关联到 WHERE
```

### 代码示例
```java
// OdpsAstBuilderVisitor.java
@Override
public ASTNode visitTableName(final OdpsParser.TableNameContext ctx) {
    SimpleTableSegment result = new SimpleTableSegment(...);
    
    // 提取表附近的注释
    CommentSegment tableComment = extractCommentNearNode(ctx);
    if (null != tableComment) {
        // 可以直接关联到表
        result.setComment(tableComment);
    }
    
    return result;
}

@Override
public ASTNode visitSelectItem(final OdpsParser.SelectItemContext ctx) {
    ProjectionSegment projection = ...;
    
    // 提取字段附近的注释
    CommentSegment fieldComment = extractCommentNearNode(ctx);
    if (null != fieldComment) {
        // 可以直接关联到字段
        projection.setComment(fieldComment);
    }
    
    return projection;
}

private CommentSegment extractCommentNearNode(ParserRuleContext ctx) {
    if (null == tokenStream || !(tokenStream instanceof CommonTokenStream)) {
        return null;
    }
    CommonTokenStream stream = (CommonTokenStream) tokenStream;
    
    // 获取节点右侧的隐藏 token（注释通常在元素之后）
    List<Token> hiddenTokens = stream.getHiddenTokensToRight(
        ctx.getStop().getTokenIndex(), COMMENT_CHANNEL);
    
    if (null != hiddenTokens && !hiddenTokens.isEmpty()) {
        Token commentToken = hiddenTokens.get(0);
        return new CommentSegment(
            commentToken.getText(),
            commentToken.getStartIndex(),
            commentToken.getStopIndex()
        );
    }
    return null;
}
```

### 优点
1. ✅ **关联精确**：注释在解析时直接关联到对应的元素，上下文明确
2. ✅ **实时关联**：不需要后续的位置计算，关联更准确
3. ✅ **语义清晰**：注释与元素在同一个上下文中处理，语义更明确
4. ✅ **易于扩展**：每个 Segment 可以有自己的注释字段

### 缺点
1. ❌ **代码分散**：需要在多个 visit 方法中添加注释提取逻辑
2. ❌ **性能开销**：每个节点都要调用 `getHiddenTokensToRight`，可能有性能开销
3. ❌ **维护成本**：注释提取逻辑分散在多个方法中，修改时需要多处更新
4. ❌ **重复代码**：多个地方需要实现类似的注释提取逻辑

---

## 详细对比表

| 对比维度 | 方式一：统一提取 | 方式二：分散提取 |
|---------|----------------|----------------|
| **提取时机** | AST 构建完成后统一提取 | 每个节点访问时立即提取 |
| **关联时机** | 元数据提取阶段关联 | 解析时直接关联 |
| **关联精度** | 通过位置计算，可能不够精确 | 上下文明确，关联精确 |
| **代码集中度** | 集中在一个方法 | 分散在多个 visit 方法 |
| **维护性** | 易于维护 | 需要多处修改 |
| **性能** | 一次遍历，性能较好 | 多次调用，可能有开销 |
| **扩展性** | 需要扩展元数据模型 | 可以扩展 Segment 模型 |
| **实现复杂度** | 简单 | 中等 |

---

## 混合方案（推荐）

结合两种方式的优点：

1. **在 visit 时提取注释**：在关键节点（表、字段、子句）访问时提取附近的注释
2. **存储到 Segment**：将注释直接存储到对应的 Segment 中
3. **元数据提取时收集**：在元数据提取时，从 Segment 中收集注释并建立关联

### 实现示例

```java
// 1. 扩展 Segment，添加注释字段
public class SimpleTableSegment implements TableSegment {
    private CommentSegment comment;  // 表的注释
    // ...
}

public interface ProjectionSegment extends SQLSegment {
    CommentSegment getComment();  // 字段的注释
    // ...
}

// 2. 在 visit 时提取注释
@Override
public ASTNode visitTableName(final OdpsParser.TableNameContext ctx) {
    SimpleTableSegment result = new SimpleTableSegment(...);
    
    // 提取表附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    result.setComment(comment);
    
    return result;
}

// 3. 在元数据提取时收集注释
private void extractComments(final OdpsSQLSelectStatement selectStatement, 
                            final OdpsSQLMetadata metadata) {
    // 从表 Segment 中收集注释
    if (null != selectStatement.getFrom()) {
        TableSegment table = selectStatement.getFrom();
        if (table instanceof SimpleTableSegment) {
            SimpleTableSegment simpleTable = (SimpleTableSegment) table;
            if (null != simpleTable.getComment()) {
                // 直接关联到表，无需位置计算
                CommentMetadata commentMetadata = CommentMetadata.forTable(
                    simpleTable.getComment().getText(),
                    simpleTable.getComment().getStartIndex(),
                    simpleTable.getComment().getStopIndex(),
                    findTableReference(simpleTable, metadata)
                );
                metadata.addComment(commentMetadata);
            }
        }
    }
    
    // 从字段 Segment 中收集注释
    if (null != selectStatement.getProjections()) {
        for (ProjectionSegment projection : selectStatement.getProjections().getProjections()) {
            if (null != projection.getComment()) {
                // 直接关联到字段
                FieldMetadata field = findFieldByProjection(projection, metadata);
                if (null != field) {
                    CommentMetadata commentMetadata = CommentMetadata.forField(
                        projection.getComment().getText(),
                        projection.getComment().getStartIndex(),
                        projection.getComment().getStopIndex(),
                        field
                    );
                    metadata.addComment(commentMetadata);
                }
            }
        }
    }
}
```

### 混合方案优点
1. ✅ **关联精确**：注释在解析时直接关联到 Segment
2. ✅ **代码清晰**：注释提取逻辑集中在关键节点
3. ✅ **易于维护**：注释信息存储在 Segment 中，元数据提取时只需收集
4. ✅ **性能平衡**：只在关键节点提取，性能开销可控

---

## 建议

**推荐使用混合方案**：
- 在关键节点（表、字段、子句）的 visit 方法中提取附近的注释
- 将注释存储到对应的 Segment 中
- 在元数据提取时从 Segment 中收集注释并建立关联

这样既保证了关联的精确性，又保持了代码的可维护性。

