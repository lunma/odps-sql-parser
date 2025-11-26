# SQL Parser (ODPS)

基于 ANTLR4的 ODPS SQL解析

## 项目简介

本项目是一个功能完整的 ODPS SQL 解析器，支持将 SQL 语句解析为结构化的抽象语法树（AST），并提供丰富的元数据抽取能力。项目采用 ANTLR4 进行词法和语法分析，使用语义模型构建 AST，支持语句解析、语法校验、元数据抽取和注释提取等功能。

## 核心功能

### 1. SQL 解析与 AST 构建

将 ODPS SQL 解析为统一的 AST 语义片段，支持完整的 SELECT 语句解析：

- **基础 SELECT 子句**：列、表达式、别名、`DISTINCT` 关键字
- **FROM 子句**：表引用、子查询、各种 JOIN 类型（INNER、LEFT、RIGHT、FULL、CROSS）
- **WHERE 子句**：条件表达式、逻辑运算符
- **GROUP BY 子句**：分组表达式、多列分组
- **HAVING 子句**：聚合后过滤条件
- **ORDER BY 子句**：排序表达式、排序方向（ASC/DESC）、NULL 值排序方式
- **LIMIT 子句**：限制行数、OFFSET 支持
- **集合操作**：`UNION`、`INTERSECT`、`EXCEPT`
- **WITH CTE**：公共表表达式（Common Table Expression）
- **HINT 子句**：查询提示
- **WINDOW 子句**：窗口函数定义（支持窗口名称、PARTITION BY、ORDER BY、窗口框架等）
- **窗口函数**：支持 `ROW_NUMBER()`、`RANK()`、`DENSE_RANK()`、`SUM() OVER` 等窗口函数解析
- **LATERAL VIEW**：侧视图支持

### 2. SQL 语句校验

- **语法错误检测**：通过 ANTLR 语法解析器检测语法错误
- **语句类型判断**：验证是否为支持的 SELECT 语句
- **错误报告**：提供详细的错误位置（行号、列号）和错误信息
- **友好的错误摘要**：通过 `formatSummary()` 方法生成用户友好的错误摘要

### 3. 元数据抽取

从解析后的 AST 中提取丰富的结构化元数据：

- **表信息**：使用的表、别名映射、子查询识别、表注释
- **JOIN 关系**：JOIN 类型、连接条件、USING 子句、JOIN 注释
- **字段血缘**：字段来源追踪、字段分类（普通字段、聚合字段、表达式字段等）、字段注释
- **子句元数据**：WHERE、GROUP BY、HAVING、ORDER BY、LIMIT 条件信息和注释
- **特性元数据**：CTE、HINT、LATERAL VIEW、集合操作信息
- **窗口函数元数据**：窗口函数类型、分区列、排序列、窗口框架、窗口定义等
- **函数调用提取**：提取 SQL 中所有函数调用信息，包括：
  - 函数名称（如 `SUM`、`COUNT`、`SUBSTRING`、`DATE_FORMAT` 等）
  - 函数类型（聚合函数、窗口函数、字符串函数、数学函数、日期时间函数、类型转换函数、条件函数等）
  - 函数参数（参数表达式、参数数量）
  - 调用位置（在 SELECT、WHERE、HAVING、ORDER BY 等子句中的位置）
  - 函数别名（如果有）
  - DISTINCT 标识（如 `COUNT(DISTINCT col)`）
  - 嵌套函数调用支持
- **注释提取**：支持表注释、字段注释、子句注释、语句级别注释的提取和关联

### 4. 注释提取功能

支持 SQL 注释的精确提取和关联，采用混合方案：

- **表注释**：表名和别名后的注释，精确关联到表引用
- **字段注释**：SELECT 列表中的字段注释，精确关联到字段元数据
- **子句注释**：WHERE、GROUP BY、HAVING、ORDER BY、LIMIT、WITH、HINT、LATERAL VIEW 等子句注释
- **语句级别注释**：不在任何特定 Segment 附近的注释，通过位置计算关联
- **注释去重**：通过位置去重，避免同一注释被重复提取和关联
- **注释关联**：注释精确关联到对应的 SQL 元素（表、字段、子句等）

## 技术栈

- **Java 8+**：项目基于 Java 8 开发
- **ANTLR 4.9.2**：词法和语法分析
- **Maven**：项目构建和依赖管理
- **Lombok**：简化 Java 代码
- **Guava**：工具类库
- **Logback**：日志框架
- **JUnit 4**：单元测试框架

## 项目结构

```
sql-parser/
├─ README.md                          # 项目说明文档
├─ pom.xml                            # Maven 项目配置
├─ src/
│  ├─ main/
│  │  ├─ antlr4/                      # ODPS 语法定义（仅提交 .g4）
│  │  │  ├─ com/sea/odps/sql/autogen/
│  │  │  │  ├─ OdpsLexer.g4           # 词法规则定义
│  │  │  │  └─ OdpsParser.g4          # 语法规则定义
│  │  │  └─ imports/maxcompute/       # 导入的语法文件
│  │  │     └─ OdpsLexer.g4
│  │  ├─ java/
│  │  │  └─ com/sea/odps/sql/
│  │  │     ├─ core/                  # 通用 SQL 语义模型
│  │  │     │  ├─ enums/              # 枚举定义
│  │  │     │  ├─ segment/            # SQL 语义片段
│  │  │     │  │  ├─ dml/             # DML 片段（列、表达式、JOIN、窗口等）
│  │  │     │  │  └─ generic/         # 通用片段（表、别名、注释等）
│  │  │     │  ├─ statement/          # 语句抽象
│  │  │     │  ├─ util/               # 工具类
│  │  │     │  └─ value/              # 值节点
│  │  │     ├─ error/                 # 错误处理
│  │  │     │  └─ OdpsSyntaxErrorListener.java
│  │  │     ├─ metadata/              # 元数据抽取
│  │  │     │  ├─ model/              # 元数据模型
│  │  │     │  │  ├─ clause/          # 子句元数据
│  │  │     │  │  ├─ comment/         # 注释元数据
│  │  │     │  │  ├─ core/            # 核心元数据
│  │  │     │  │  ├─ feature/         # 特性元数据
│  │  │     │  │  ├─ field/           # 字段元数据
│  │  │     │  │  ├─ function/        # 函数调用元数据
│  │  │     │  │  ├─ join/            # JOIN 关系元数据
│  │  │     │  │  ├─ reference/       # 引用元数据
│  │  │     │  │  └─ window/          # 窗口函数元数据
│  │  │     │  ├─ OdpsSQLMetadata.java
│  │  │     │  ├─ OdpsSQLMetadataError.java
│  │  │     │  ├─ OdpsSQLMetadataExtractor.java
│  │  │     │  └─ OdpsSQLMetadataResult.java
│  │  │     └─ visitor/               # AST Visitor
│  │  │        ├─ core/               # Visitor 核心接口
│  │  │        ├─ odps/               # ODPS 特定实现
│  │  │        └─ OdpsAstBuilderVisitor.java
│  │  └─ resources/
│  │     └─ logback.xml               # Logback 日志配置
│  └─ test/
│     └─ java/
│        └─ com/sea/odps/sql/
│           ├─ metadata/              # 元数据抽取器测试
│           └─ OdpsAstBuilderVisitorTest.java
```

### 自动生成的解析代码

- 仓库仅包含 `.g4` 语法文件，ANTLR4 插件在 `mvn compile`/`mvn package` 时自动将解析器生成到 `target/generated-sources/antlr4`。
- 生成目录会自动加入编译 classpath，并在打包阶段被一同编译进最终的 JAR 中，因此无需也不应将 `com/sea/odps/sql/autogen/*.java` 提交到版本库。
- 若需向他人分发可运行包，执行 `mvn clean package`（或 `mvn clean install`）即可；需要源码包时，可另外执行 `mvn -DskipTests source:jar` 产出 `*-sources.jar`。
- 如果语法更新导致生成物发生变化，提交前运行 `mvn clean generate-sources` 或 `mvn clean package` 即可保证本地、CI 与用户环境生成的代码一致。

## 关键模块说明

| 模块 | 位置 | 作用 |
| --- | --- | --- |
| **语法定义** | `src/main/antlr4` | ODPS 语法定义，执行 `mvn generate-sources` 可重新生成解析代码 |
| **语义片段** | `core/segment` | SQL 语义片段模型（列、JOIN、Group/Order、Limit、Where/Having、窗口等），详见下方结构说明 |
| **AST 构建器** | `visitor/OdpsAstBuilderVisitor` | AST 构建访问器，将 ANTLR ParseTree 转换为 `OdpsSQLSelectStatement`，支持注释提取 |
| **元数据抽取器** | `metadata/OdpsSQLMetadataExtractor` | 遍历 AST，输出表、JOIN、字段血缘、注释、窗口函数等元数据信息 |
| **校验入口** | `metadata/OdpsSQLMetadataEntrypoint` | 封装解析、错误监听、AST 构造与元数据抽取，提供 `result(String sql)` 方法 |
| **校验结果** | `metadata/OdpsSQLMetadataResult` | 校验结果对象，包含 `formatSummary()` 生成用户友好摘要 |
| **错误处理** | `error/OdpsSyntaxErrorListener` | 语法错误监听器，收集解析过程中的语法错误 |
| **测试** | `src/test/java/com/sea/odps/sql` | 单元测试覆盖 AST 构建、校验、注释提取与复杂 SQL 场景 |

### 校验流程与结果

`OdpsSQLMetadataEntrypoint#result(String sql)` 按以下顺序执行：

1. **预检**：判空并裁剪 SQL，空语句直接返回 `unsupported`。
2. **词法/语法分析**：使用 `OdpsLexer` + `OdpsParser` 生成 ParseTree，同时注册 `OdpsSyntaxErrorListener` 收集语法错误。
3. **AST 构建**：`OdpsAstBuilderVisitor` 将 ParseTree 转换为 `OdpsSQLSelectStatement`，并提取隐藏通道注释。
4. **语句类型限制**：只接受 SELECT 语句，其他类型会返回 `unsupported`。
5. **元数据抽取**：`OdpsSQLMetadataExtractor` 将 AST 映射为结构化的 `OdpsSQLMetadata`，失败时返回 `withErrors`。

校验结果的语义：

| 方法 | 说明 |
| --- | --- |
| `isSupported()` | 语句是否为受支持的 SELECT |
| `isValid()` | 是否通过语法与语义校验 |
| `getErrors()` | 返回 `OdpsSQLMetadataError` 列表（包含行列号、消息） |
| `getMetadata()` | 可选的 `OdpsSQLMetadata`，包含表、字段、JOIN、窗口、函数、注释等元数据 |
| `formatSummary()` | 将关键表/字段/JOIN 信息串联为友好摘要 |

如需定制提取行为，可注入自定义 `OdpsSQLMetadataExtractor`：

```java
OdpsSQLMetadataExtractor extractor = new OdpsSQLMetadataExtractor(/* 自定义配置 */);
OdpsSQLMetadataEntrypoint entrypoint = new OdpsSQLMetadataEntrypoint(extractor);
OdpsSQLMetadataResult result = entrypoint.result(sql);
```

### `core` 子目录详细说明

#### `core/enums`
通用枚举定义，统一维护常量：
- `OrderDirection`：排序方向（ASC/DESC）
- `NullsOrderType`：NULL 值排序方式（NULLS FIRST/LAST）
- `QuoteCharacter`：引号类型（单引号、双引号、反引号等）
- `SubqueryType`：子查询类型
- `CombineType`：集合操作类型（UNION、INTERSECT、EXCEPT）

#### `core/segment`
SQL 语义片段抽象，包含通用片段与 DML 细分结构，是 AST 的主体模型：

- **`dml/combine/`**：集合操作片段（`CombineSegment`，用于 UNION/INTERSECT/EXCEPT）
- **`dml/expr/`**：表达式片段（`WindowFunctionSegment` 等）
- **`dml/helper/`**：辅助类（`SelectRestResult`、`PreSelectResult`、`PostSelectResult`，用于 AST 构建过程中的数据传递）
- **`dml/item/`**：投影项片段（`ProjectionSegment`、`ColumnProjectionSegment`、`ExpressionProjectionSegment`、`ProjectionsSegment`）
- **`dml/order/`**：排序相关片段（`OrderBySegment`、`GroupBySegment`、`OrderByItemSegment`、`ExpressionOrderByItemSegment`）
- **`dml/pagination/`**：分页片段（`LimitSegment`、`PaginationValueSegment`、`NumberLiteralPaginationValueSegment`、`ExpressionPaginationValueSegment`）
- **`dml/predicate/`**：谓词片段（`WhereSegment`、`HavingSegment`）
- **`dml/window/`**：窗口函数片段（`WindowSegment`、`WindowDefinitionSegment`、`WindowSpecificationSegment`、`PartitionBySegment`、`WindowOrderBySegment`、`WindowFrameSegment`、`WindowFrameBoundarySegment`）
- **`dml/WithSegment`**：WITH CTE 片段
- **`dml/LateralViewSegment`**：LATERAL VIEW 片段
- **`generic/column/`**：列片段（`ColumnSegment`）
- **`generic/expr/`**：表达式片段（`ExpressionSegment`、`BinaryOperationExpression`、`NotExpression`、`LiteralExpressionSegment`、`SimpleExpressionSegment`、`SubquerySegment`、`CommonTableExpressionSegment` 等）
- **`generic/hint/`**：HINT 片段（`HintSegment`、`HintItemSegment`）
- **`generic/table/`**：表片段（`TableSegment`、`SimpleTableSegment`、`JoinTableSegment`、`SubqueryTableSegment`、`TableNameSegment`）
- **`generic/`**：通用片段（`AliasSegment`、`OwnerSegment`、`CommentSegment` 等）

#### `core/statement`
语句层级抽象，定义 `SQLStatement` 及 DML 语句基类（`SelectStatement`），承载 Segment 组合结果，支持语句级别注释存储。

#### `core/util`
工具类集合，例如 SQL 文本处理（`SQLUtil`）、日志封装（`LogUtil`）等通用辅助能力。

#### `core/value`
值节点与标识符封装，负责 AST 中字面量、集合、标识符（`IdentifierValue`）的统一表示。

### 元数据模型说明

- **`metadata/model/reference`**：引用模型（`TableReference`、`ColumnReference`）
- **`metadata/model/field`**：字段元数据（`FieldMetadata`、`FieldCategory`）
- **`metadata/model/join`**：JOIN 关系（`JoinRelation`、`JoinConditionPair`）
- **`metadata/model/clause`**：子句元数据（`WhereConditionMetadata`、`GroupByMetadata`、`HavingConditionMetadata`、`OrderByMetadata`、`LimitMetadata`）
- **`metadata/model/feature`**：特性元数据（`CteMetadata`、`HintMetadata`、`LateralViewMetadata`、`CombineMetadata`）
- **`metadata/model/comment`**：注释元数据（`CommentMetadata`、`CommentTargetType`）
- **`metadata/model/window`**：窗口函数元数据（`WindowMetadata`、`WindowFunctionMetadata`）
- **`metadata/model/function`**：函数调用元数据（`FunctionCallMetadata`、`FunctionType`）

## 快速开始

### 环境要求

- **JDK 8+**（建议使用 JDK 11 或更高版本）
- **Maven 3.8+**

### 编译 & 测试

```bash
# 编译项目
mvn clean compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OdpsSQLMetadataTest
mvn test -Dtest=CommentExtractionTest
mvn test -Dtest=OdpsAstBuilderVisitorTest
mvn test -Dtest=OdpsSQLMetadataExtractorTest
```

> **首次运行**：若需重新生成 ANTLR 代码，可执行：
> ```bash
> mvn generate-sources
> ```

### 使用示例

#### 基础使用

```java
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;

String sql = "SELECT t1.id, t2.name " +
        "FROM ods.table_a t1 " +
        "LEFT JOIN ods.table_b t2 ON t1.id = t2.id";

OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
OdpsSQLMetadataResult result = validator.result(sql);

if (result.isValid()) {
    com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata metadata =
            result.getMetadata().orElseThrow(IllegalStateException::new);

    // 获取表信息
    metadata.getTables().forEach(table ->
            System.out.println("表: " + table.getQualifiedName() +
                    (table.getAlias() != null ? " AS " + table.getAlias() : "")));

    // 获取字段信息
    metadata.getFields().forEach(field -> {
        System.out.println("字段: " + field.getExpression());
        System.out.println("  别名: " + field.getAliasOptional().orElse("无"));
        System.out.println("  类型: " + field.getCategory());
        System.out.println("  来源: " + field.getSourceColumns());
    });

    // 获取 JOIN 关系
    metadata.getJoins().forEach(join ->
            System.out.println("JOIN: " + join.getJoinType() + " " +
                    join.getLeft().getQualifiedName() + " ⇔ " +
                    join.getRight().getQualifiedName()));

    // 打印摘要
    System.out.println(result.formatSummary());
} else {
    result.getErrors().forEach(error ->
            System.err.printf("语法错误: [%d:%d] %s%n",
                    error.getLine(), error.getCharPositionInLine(), error.getMessage()));
}
```

#### 注释提取示例

```java
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;

String sql = "SELECT id, -- 用户ID\n" +
        "       name -- 用户名\n" +
        "FROM ods.user_table t1 -- 用户表\n" +
        "WHERE id > 0 -- WHERE 条件\n" +
        "GROUP BY id -- 分组\n" +
        "ORDER BY id -- 排序\n" +
        "LIMIT 10; -- 限制";

OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
OdpsSQLMetadataResult result = validator.result(sql);

if (result.isValid()) {
    com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata metadata =
            result.getMetadata().orElseThrow(IllegalStateException::new);

    // 获取注释信息
    metadata.getComments().forEach(comment -> {
        System.out.println("注释: " + comment.getText());
        System.out.println("  目标类型: " + comment.getTargetType());
        System.out.println("  位置: [" + comment.getStartIndex() +
                "-" + comment.getStopIndex() + "]");
    });

    // 表注释
    metadata.getTables().forEach(table -> {
        if (table.getComment() != null) {
            System.out.println("表 " + table.getQualifiedName() +
                    " 的注释: " + table.getComment());
        }
    });

    // 字段注释
    metadata.getFields().forEach(field -> {
        if (field.getComment() != null) {
            System.out.println("字段 " + field.getExpression() +
                    " 的注释: " + field.getComment());
        }
    });
}
```

#### 窗口函数示例

```java
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;

String sql = "SELECT user_id, " +
        "       ROW_NUMBER() OVER (PARTITION BY country ORDER BY amount DESC) as rn, " +
        "       SUM(amount) OVER w1 as total_amount " +
        "FROM ods.sales " +
        "WINDOW w1 AS (PARTITION BY country ORDER BY dt ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)";

OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
OdpsSQLMetadataResult result = validator.result(sql);

if (result.isValid()) {
    com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata metadata =
            result.getMetadata().orElseThrow(IllegalStateException::new);

    // 获取窗口函数信息
    metadata.getWindowFunctions().forEach(windowFunc -> {
        System.out.println("窗口函数: " + windowFunc.getFunctionName());
        System.out.println("  分区列: " + windowFunc.getPartitionByColumns());
        System.out.println("  排序列: " + windowFunc.getOrderByColumns());
    });

    // 获取窗口定义
    metadata.getWindows().forEach(window -> {
        System.out.println("窗口定义: " + window.getName());
        System.out.println("  分区列: " + window.getPartitionByColumns());
    });
}
```

#### 函数调用提取示例

```java
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;
import com.sea.odps.sql.metadata.model.function.FunctionType;

String sql = "SELECT count(id) as user_count, " +
        "       sum(amount) total_amount, " +
        "       upper(name), " +
        "       round(price, 2), " +
        "       date_format(create_time, '%Y-%m-%d'), " +
        "       coalesce(value, 0) " +
        "FROM ods.sales " +
        "WHERE substring(name, 1, 5) = 'test' " +
        "GROUP BY region " +
        "HAVING sum(amount) > 1000 " +
        "ORDER BY sum(amount) desc;";

OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
OdpsSQLMetadataResult result = validator.result(sql);

if (result.isValid()) {
    com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata metadata =
            result.getMetadata().orElseThrow(IllegalStateException::new);

    // 获取所有函数调用
    metadata.getFunctionCalls().forEach(func -> {
        System.out.println("函数: " + func.getFunctionName());
        System.out.println("  类型: " + func.getFunctionType());
        System.out.println("  位置: " + func.getClauseLocation());
        System.out.println("  参数数量: " + func.getArgumentCount());
        System.out.println("  参数: " + func.getArguments());
        if (func.getAliasOptional().isPresent()) {
            System.out.println("  别名: " + func.getAlias());
        }
        if (func.isDistinct()) {
            System.out.println("  DISTINCT: true");
        }
    });

    // 按类型分组统计
    long aggregateCount = metadata.getFunctionCalls().stream()
            .filter(f -> f.getFunctionType() == FunctionType.AGGREGATE)
            .count();
    long stringCount = metadata.getFunctionCalls().stream()
            .filter(f -> f.getFunctionType() == FunctionType.STRING)
            .count();
    System.out.println("聚合函数数量: " + aggregateCount);
    System.out.println("字符串函数数量: " + stringCount);
}
```

#### 复杂 SQL 示例

```java
import com.sea.odps.sql.metadata.OdpsSQLMetadataEntrypoint;
import com.sea.odps.sql.metadata.OdpsSQLMetadataResult;

String sql = "WITH sales AS ( -- CTE 注释\n" +
        "  SELECT user_id, country, SUM(amount) amount\n" +
        "  FROM ods.fact_sales\n" +
        "  WHERE dt >= '2024-01-01'\n" +
        "  GROUP BY user_id, country\n" +
        ")\n" +
        "SELECT t.country, -- 国家\n" +
        "       SUM(t.amount) total_amount, -- 总金额\n" +
        "       MAX(COALESCE(s.score, 0)) max_score\n" +
        "FROM sales t -- 销售数据\n" +
        "LEFT JOIN (SELECT user_id, MAX(score) score FROM ods.user_score GROUP BY user_id) s\n" +
        "  ON t.user_id = s.user_id\n" +
        "WHERE t.country <> 'CN' -- 排除中国\n" +
        "GROUP BY t.country -- 按国家分组\n" +
        "HAVING SUM(t.amount) > 100 -- 金额大于 100\n" +
        "ORDER BY total_amount DESC -- 按总金额降序\n" +
        "LIMIT 10; -- 限制 10 条";

OdpsSQLMetadataEntrypoint validator = new OdpsSQLMetadataEntrypoint();
OdpsSQLMetadataResult result = validator.result(sql);

if (result.isValid()) {
    com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata metadata =
            result.getMetadata().orElseThrow(IllegalStateException::new);

    // CTE 信息
    metadata.getCtes().forEach(cte ->
            System.out.println("CTE: " + cte.getName()));

    // WHERE 条件
    metadata.getWhereCondition().ifPresent(where ->
            System.out.println("WHERE 条件: " + where.getExpression()));

    // GROUP BY
    metadata.getGroupBy().ifPresent(groupBy ->
            System.out.println("GROUP BY: " + groupBy.getExpressions()));

    // HAVING 条件
    metadata.getHavingCondition().ifPresent(having ->
            System.out.println("HAVING 条件: " + having.getExpression()));

    // ORDER BY
    metadata.getOrderBy().ifPresent(orderBy ->
            System.out.println("ORDER BY: " + orderBy.getItems()));

    // LIMIT
    metadata.getLimit().ifPresent(limit ->
            System.out.println("LIMIT: " + limit.getValue()));

    // 函数调用信息
    metadata.getFunctionCalls().forEach(func ->
            System.out.println("函数调用: " + func.getFunctionName() +
                    " (" + func.getFunctionType() + ") " +
                    "在 " + func.getClauseLocation()));
}
```

## 代码结构说明

### Segment 模型层次

项目采用分层 Segment 模型来表示 SQL 的语义结构：

1. **基础接口层**：`SQLSegment` 继承 `ASTNode`，提供 `startIndex` 和 `stopIndex` 用于定位原始 SQL 文本位置
2. **通用片段层**：`generic/` 目录下的通用片段（表、别名、所有者、注释等）
3. **DML 片段层**：`dml/` 目录下按功能分类的 DML 片段（表达式、投影、排序、谓词、窗口等）
4. **辅助类层**：`dml/helper/` 目录下的辅助类，用于 AST 构建过程中的中间数据传递

### 注释提取机制

注释提取采用混合方案：

1. **Segment 级别注释**：在 AST 构建时，在关键节点（表、字段、子句）提取注释并直接关联到对应的 Segment
2. **语句级别注释**：在元数据提取时，从 `AbstractSQLStatement.commentSegments` 中提取未被 Segment 关联的注释，通过位置计算关联到最近的 SQL 元素
3. **注释去重**：通过位置去重，避免同一注释被重复提取和关联

### 窗口函数解析

项目完整支持窗口函数的解析和元数据提取：

1. **WINDOW 子句解析**：支持窗口定义（窗口名称、PARTITION BY、ORDER BY、窗口框架等）
2. **窗口函数识别**：识别 SELECT 表达式中的窗口函数（如 `ROW_NUMBER() OVER (...)`、`SUM(...) OVER w1`）
3. **窗口函数 Segment**：创建详细的窗口函数 Segment 并建立与 WINDOW 子句定义的映射关系
4. **窗口函数元数据**：支持窗口函数元数据提取（窗口函数类型、分区列、排序列、窗口框架等）

### 关键类说明

- **`ExpressionOrderByItemSegment`**：表达式排序项片段，扩展 `OrderByItemSegment`，用于 ORDER BY 和 GROUP BY 子句中的表达式排序项
- **`SelectRestResult`**：封装 SELECT 语句的后续子句（FROM、WHERE、GROUP BY、HAVING、ORDER BY、LIMIT、WINDOW）
- **`PreSelectResult`**：封装 SELECT 前置子句（WHERE、GROUP BY、HAVING、WINDOW）
- **`PostSelectResult`**：封装 SELECT 后置子句（ORDER BY、LIMIT）
- **`CommentSegment`**：注释片段，存储注释内容和位置信息
- **`CommentMetadata`**：注释元数据，包含注释内容、目标类型、目标对象等信息
- **`WindowFunctionSegment`**：窗口函数片段，存储窗口函数表达式和窗口规范
- **`WindowMetadata`**：窗口元数据，包含窗口定义、分区列、排序列、窗口框架等信息
- **`FunctionCallMetadata`**：函数调用元数据，包含函数名称、类型、参数、调用位置、别名等信息
- **`FunctionType`**：函数类型枚举，包括聚合、窗口、字符串、数学、日期时间、类型转换、条件等类型

## 测试

项目包含完整的单元测试，共 **67 个测试方法**，覆盖以下场景：

- ✅ **基础 SELECT 语句解析**：列、表达式、别名、DISTINCT
- ✅ **JOIN 操作**：INNER、LEFT、RIGHT、FULL、CROSS JOIN，USING 子句，多表 JOIN
- ✅ **子查询**：嵌套子查询、子查询别名
- ✅ **聚合函数和 GROUP BY**：聚合函数、GROUP BY、HAVING
- ✅ **WITH CTE 支持**：公共表表达式定义和使用
- ✅ **集合操作**：UNION、UNION ALL、INTERSECT、EXCEPT、嵌套集合操作
- ✅ **窗口函数解析**：WINDOW 子句、窗口函数表达式、PARTITION BY、ORDER BY、窗口框架
- ✅ **LIMIT 子句**：LIMIT offset, rowCount 和 LIMIT rowCount OFFSET offset 两种格式
- ✅ **ORDER BY 子句**：排序方向、NULLS FIRST/LAST
- ✅ **函数调用提取**：聚合函数、字符串函数、数学函数、日期时间函数、类型转换函数、条件函数、嵌套函数调用、DISTINCT 聚合
- ✅ **注释提取**：表注释、字段注释、WHERE/GROUP BY/HAVING/ORDER BY/LIMIT 子句注释、WITH/HINT/LATERAL VIEW 注释、语句级别注释
- ✅ **错误处理和校验**：语法错误检测、错误位置报告

运行测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OdpsSQLMetadataTest
mvn test -Dtest=CommentExtractionTest
mvn test -Dtest=OdpsAstBuilderVisitorTest
mvn test -Dtest=OdpsSQLMetadataExtractorTest
```

## 日志

日志由 `src/main/resources/logback.xml` 配置，默认输出到 `logs/<date>/app.XXX.0.log`。可根据需要调整级别或输出目录。

日志级别：
- **DEBUG**：详细的调试信息，包括 AST 构建过程
- **INFO**：一般信息，包括解析结果摘要
- **ERROR**：错误信息，包括语法错误和异常

## 依赖说明

项目主要依赖：

- **antlr4-runtime (4.9.2)**：ANTLR 运行时库
- **guava (32.0.1-android)**：Google Guava 工具类库
- **lombok (1.18.28)**：简化 Java 代码
- **logback-classic (1.4.14)**：日志框架
- **slf4j-api (1.7.36)**：SLF4J 日志接口
- **junit (4.13.1)**：单元测试框架（test scope）
- **odps-sdk-core (0.43.4-public)**：ODPS SDK（test scope，用于测试）

## 测试统计

项目包含 **4 个测试类**，共 **67 个测试方法**：

| 测试类 | 测试方法数 | 主要测试内容 |
| --- | --- | --- |
| `OdpsSQLMetadataTest` | 16 | SQL 校验、元数据提取、复杂 SQL 场景 |
| `CommentExtractionTest` | 13 | 注释提取功能（表、字段、子句、语句级别） |
| `OdpsAstBuilderVisitorTest` | 23 | AST 构建（DISTINCT、JOIN、LIMIT、CTE、HINT、WINDOW、LATERAL VIEW、集合操作、子查询、ORDER BY、注释） |
| `OdpsSQLMetadataExtractorTest` | 15 | 函数调用提取（各种函数类型、嵌套函数、DISTINCT、调用位置） |
