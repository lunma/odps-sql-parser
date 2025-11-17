# 语句级别注释提取优化

## 问题描述

根据代码注释，`extractComments()` 的意图是提取"不在任何特定 Segment 附近的注释"（语句级别的注释），但之前的实现会提取所有注释，包括已经被 Segment 提取的注释，导致：

1. **重复提取**：同一个注释可能被提取两次
2. **重复关联**：同一个注释可能有两种关联方式（精确关联 + 位置计算关联）

## 优化方案

### 实现思路

在元数据提取阶段进行去重，只处理真正的语句级别注释：

1. **收集 Segment 级别的注释位置**：遍历所有 Segment，收集已被提取的注释位置
2. **过滤语句级别注释**：从 `AbstractSQLStatement.commentSegments` 中过滤掉已被 Segment 提取的注释
3. **只处理真正的语句级别注释**：使用位置计算关联剩余的注释

### 实现细节

#### 1. 新增方法：`collectSegmentCommentPositions()`

收集所有 Segment 级别的注释位置（使用 `startIndex` 作为唯一标识）：

```java
private Set<Integer> collectSegmentCommentPositions(final OdpsSQLSelectStatement selectStatement) {
    Set<Integer> positions = new HashSet<>();
    
    // 收集表注释位置
    // 收集字段注释位置
    // 收集 WHERE/GROUP BY/HAVING/ORDER BY/LIMIT 注释位置
    // 收集 WITH CTE/HINT/LATERAL VIEW 注释位置
    
    return positions;
}
```

#### 2. 优化 `extractComments()` 方法

在元数据提取时进行去重：

```java
// 先收集所有 Segment 级别的注释位置，用于去重
Set<Integer> segmentCommentPositions = collectSegmentCommentPositions(selectStatement);

Collection<CommentSegment> statementComments = selectStatement.getCommentSegments();
if (null != statementComments) {
    for (CommentSegment commentSegment : statementComments) {
        if (null != commentSegment) {
            // 检查是否已被 Segment 提取（通过位置判断）
            if (!segmentCommentPositions.contains(commentSegment.getStartIndex())) {
                // 这是真正的语句级别注释，使用位置计算关联
                CommentMetadata commentMetadata = associateCommentWithElement(
                        commentSegment, selectStatement, metadata);
                metadata.addComment(commentMetadata);
            }
            // 如果已被 Segment 提取，则跳过（避免重复）
        }
    }
}
```

#### 3. 更新方法注释

更新 `extractComments()` 方法的注释，说明：
- 当前实现会提取所有注释（包括 Segment 级别的注释）
- 去重逻辑在元数据提取阶段完成
- 设计意图是提取语句级别的注释

## 工作流程

### 优化前（存在重复）

```
SQL 解析
  ↓
visitSelectItem() → 提取字段注释 → 存储到 ProjectionSegment.comment
visitTableSource() → 提取表注释 → 存储到 SimpleTableSegment.comment
... (其他 Segment)
  ↓
extractComments() → 提取所有注释 → 存储到 AbstractSQLStatement.commentSegments
  ↓
元数据提取
  ↓
从 Segment 收集注释（精确关联）✅
从 AbstractSQLStatement 收集注释（位置计算关联）⚠️ 可能重复
```

### 优化后（去重）

```
SQL 解析
  ↓
visitSelectItem() → 提取字段注释 → 存储到 ProjectionSegment.comment
visitTableSource() → 提取表注释 → 存储到 SimpleTableSegment.comment
... (其他 Segment)
  ↓
extractComments() → 提取所有注释 → 存储到 AbstractSQLStatement.commentSegments
  ↓
元数据提取
  ↓
从 Segment 收集注释（精确关联）✅
  ↓
collectSegmentCommentPositions() → 收集 Segment 注释位置
  ↓
从 AbstractSQLStatement 收集注释
  ↓
过滤：只处理不在 Segment 注释位置集合中的注释 ✅
  ↓
使用位置计算关联真正的语句级别注释 ✅
```

## 优化效果

### ✅ 解决的问题

1. **消除重复提取**：同一个注释不会被提取两次
2. **消除重复关联**：同一个注释只有一种关联方式
3. **符合设计意图**：只处理真正的语句级别注释

### ✅ 保持的功能

1. **不遗漏注释**：所有注释都会被提取（在 AST 构建阶段）
2. **精确关联**：Segment 级别的注释仍然精确关联
3. **后备机制**：语句级别的注释使用位置计算关联

## 示例

### 示例 SQL

```sql
SELECT id, -- 字段注释（Segment 级别）
name -- 字段注释（Segment 级别）
FROM ods.user_table t1 -- 表注释（Segment 级别）
WHERE id > 0 -- WHERE 注释（Segment 级别）
-- 这是语句级别的注释（不在任何 Segment 附近）
ORDER BY id;
```

### 处理流程

1. **AST 构建阶段**：
   - `visitSelectItem()` 提取字段注释 → 存储到 `ProjectionSegment.comment`
   - `visitTableSource()` 提取表注释 → 存储到 `SimpleTableSegment.comment`
   - `visitWhereClause()` 提取 WHERE 注释 → 存储到 `WhereSegment.comment`
   - `extractComments()` 提取所有注释 → 存储到 `AbstractSQLStatement.commentSegments`

2. **元数据提取阶段**：
   - 从 Segment 收集注释（精确关联）：
     - 字段注释 → 关联到 FieldMetadata
     - 表注释 → 关联到 TableReference
     - WHERE 注释 → 关联到 WHERE 子句
   - 收集 Segment 注释位置：`{字段注释位置1, 字段注释位置2, 表注释位置, WHERE注释位置}`
   - 从 `AbstractSQLStatement.commentSegments` 过滤：
     - 字段注释1 → 在 Segment 位置集合中 → 跳过
     - 字段注释2 → 在 Segment 位置集合中 → 跳过
     - 表注释 → 在 Segment 位置集合中 → 跳过
     - WHERE 注释 → 在 Segment 位置集合中 → 跳过
     - **语句级别注释** → 不在 Segment 位置集合中 → 处理 ✅

## 测试验证

✅ **所有测试通过**：13 个测试用例，0 个失败，0 个错误

优化后的实现：
- 保持了所有现有功能
- 消除了重复提取问题
- 符合设计意图（只处理语句级别注释）

## 总结

通过添加去重逻辑，优化了语句级别注释的提取：

1. ✅ **提取阶段**：`extractComments()` 仍然提取所有注释（确保不遗漏）
2. ✅ **去重阶段**：在元数据提取时，通过位置比较进行去重
3. ✅ **关联阶段**：只对真正的语句级别注释使用位置计算关联

这样既保证了不遗漏任何注释，又避免了重复提取和重复关联的问题。

