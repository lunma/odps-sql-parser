# Service

整合血缘关系 HTTP 服务与模型 API，两者共同提供 SQL 血缘提取、对外接口与标准化数据模型，便于嵌入自定义工具或服务。

## 目录结构

```
metadata-service/
├─ pom.xml
├─ src/main/java/com/sea/odps/metadata/
│  ├─ api/        // HTTP API 与服务器入口
│  ├─ connector/  // 元数据连接器（ODPS、Excel 等）
│  ├─ lineage/    // 血缘提取核心逻辑
│  ├─ model/      // 向外暴露的血缘模型
│  └─ validation/ // 逻辑校验（列冲突、表存在等）
└─ src/test/java/… // 对应测试
```

## 核心组件

| 组件 | 说明 |
| --- | --- |
| `LineageHttpServer` | 基于 Vert.x 的 HTTP 服务，提供 REST API |
| `LineageService` | 对接元数据连接器，负责解析 SQL、提取表/字段血缘 |
| `LineageResultModel` | 标准化输出模型，包含表血缘与字段血缘 |
| `LineageModelConverter` | 将 `LineageExtractor` 结果转换为模型对象 |
| `MetadataConnector` | 抽象元数据来源，内置 ODPS 与 Excel 实现 |

## 快速开始

### 编译测试

```bash
mvn clean package
mvn test
```

### 启动 HTTP 服务

```bash
# 默认端口 8080
java -cp target/metadata-service-*.jar com.sea.odps.service.api.LineageServerApp

# 指定端口
java -cp target/metadata-service-*.jar com.sea.odps.service.api.LineageServerApp --port 9090
```

## REST API

### 1. 健康检查

- **GET** `/health`
- 响应：

```json
{
  "status": "UP",
  "service": "Lineage"
}
```

### 2. 提取完整血缘

- **POST** `/api/lineage/extract`
- 请求体：

```json
{
  "sql": "SELECT t1.id, t1.name FROM ods.user_table t1",
  "connectorType": "ODPS",
  "connectorConfig": {
    "endpoint": "http://service.cn-hangzhou.maxcompute.aliyun.com/api",
    "project": "your_project",
    "accessKeyId": "your_access_key_id",
    "accessKeySecret": "your_access_key_secret",
    "accountType": "ALIYUN"
  }
}
```

- 响应示例（节选）：

```json
{
  "sql": "SELECT t1.id, t1.name FROM ods.user_table t1",
  "tableLineages": [
    {
      "qualifiedName": "ods.user_table",
      "alias": "t1",
      "tableType": "MANAGED_TABLE",
      "comment": "用户表"
    }
  ],
  "columnLineages": [
    {
      "targetField": "t1.id",
      "expression": "t1.id",
      "category": "DIRECT",
      "isFinalOutput": true,
      "sources": [
        {
          "tableQualifiedName": "ods.user_table",
          "tableAlias": "t1",
          "columnName": "id",
          "dataType": "BIGINT",
          "comment": "用户ID"
        }
      ]
    }
  ]
}
```

### 3. 只提取表血缘

- **POST** `/api/lineage/tables`
- 请求体同 `/api/lineage/extract`

### 4. 只提取字段血缘

- **POST** `/api/lineage/columns`
- 请求体同 `/api/lineage/extract`

### 错误响应

```json
{
  "error": true,
  "message": "错误信息",
  "statusCode": 400
}
```

- 常见状态码：`400`（参数/配置错误），`500`（服务器内部错误、SQL 解析失败等）

## 连接器

| 类型 | connectorType | 配置示例 |
| --- | --- | --- |
| ODPS | `ODPS` | `endpoint`、`project`、`accessKeyId`、`accessKeySecret`、`accountType`、`securityToken`（STS 可选）、`projectWhitelist`（可选） |
| Excel | `EXCEL` | `filePath`: `/path/to/metadata.xlsx` |

## HTTP 客户端示例

### cURL

```bash
curl -X POST http://localhost:8080/api/lineage/extract \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT t1.id FROM ods.user_table t1",
    "connectorType": "ODPS",
    "connectorConfig": {
      "endpoint": "http://service.cn-hangzhou.maxcompute.aliyun.com/api",
      "project": "your_project",
      "accessKeyId": "your_access_key_id",
      "accessKeySecret": "your_access_key_secret",
      "accountType": "ALIYUN"
    }
  }'
```

### Java

```java
HttpClient client = HttpClient.newHttpClient();

String requestBody = """
{
  "sql": "SELECT t1.id FROM ods.user_table t1",
  "connectorType": "ODPS",
  "connectorConfig": {
    "endpoint": "http://service.cn-hangzhou.maxcompute.aliyun.com/api",
    "project": "your_project",
    "accessKeyId": "your_access_key_id",
    "accessKeySecret": "your_access_key_secret",
    "accountType": "ALIYUN"
  }
}
""";

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/lineage/extract"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();

HttpResponse<String> response =
    client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println(response.body());
```

## 模型 API

### 1. LineageResultModel

```java
public class LineageResultModel {
    List<TableLineageModel> getTableLineages();
    List<ColumnLineageModel> getColumnLineages(); // 仅包含最终输出字段
    String getSql();
}
```

### 2. TableLineageModel

```java
public class TableLineageModel {
    String getQualifiedName(); // database.table
    String getAlias();
    String getDatabase();
    String getTableName();
    String getTableType(); // MANAGED_TABLE, VIEW 等
    String getComment();
}
```

### 3. ColumnLineageModel

```java
public class ColumnLineageModel {
    String getTargetField();       // 如 t1.id
    String getExpression();        // 原始表达式
    String getCategory();          // DIRECT, EXPRESSION
    List<ColumnSourceModel> getSources();
    boolean isFinalOutput();
}
```

### 4. ColumnSourceModel

```java
public class ColumnSourceModel {
    String getTableQualifiedName();
    String getTableAlias();
    String getColumnName();
    String getDataType();
    String getComment();
    boolean isIntermediateField();
    String getIntermediateFieldName();
}
```

### 使用方式

#### 方式一：`LineageService`（推荐）

```java
MetadataConnector connector = new ExcelMetadataConnector("metadata.xlsx");
// MetadataConnector connector = new OdpsMetastoreConnector(odps, projectWhitelist);

LineageService service = new LineageService(connector);
String sql = "SELECT t1.id, t1.name, t2.amount FROM ...";

LineageResultModel result = service.extractLineage(sql);

result.getTableLineages().forEach(table -> {
    System.out.println("表: " + table.getQualifiedName());
    System.out.println("别名: " + table.getAlias());
});

result.getColumnLineages().forEach(column -> {
    System.out.println("输出字段: " + column.getTargetField());
    column.getSources().forEach(source -> {
        if (source.isIntermediateField()) {
            System.out.println("  中间字段: " + source.getIntermediateFieldName());
        } else {
            System.out.println("  来源: " +
                source.getTableQualifiedName() + "." + source.getColumnName());
        }
    });
});
```

#### 方式二：`LineageModelConverter`

```java
LineageExtractor extractor = new LineageExtractor(metadataConnector);
LineageExtractor.LineageResult rawResult = extractor.extract(sqlMetadata);

LineageResultModel model = LineageModelConverter.convert(rawResult, sql);
```

## 示例 SQL 与输出

```sql
SELECT t1.*, t2.*
FROM cic_m_table t1
JOIN (
    SELECT project_name, nvl(table_name, '1') AS tname
    FROM cic_m_column
) t2
ON t1.project_name = t2.project_name;
```

- 表血缘：
  - `cic_m_table`（别名 `t1`）
  - `cic_m_column`

- 字段血缘（最终输出）：
  - `t1.*` → `cic_m_table` 全列
  - `t2.project_name` → `cic_m_column.project_name`
  - `t2.tname` → 中间字段 `nvl(table_name, '1')`，源列 `cic_m_column.table_name`

## 注意事项

1. **安全**：生产环境建议启用 HTTPS、鉴权（API Key/JWT）并保护凭证。
2. **性能**：高并发时推荐使用连接池、缓存及异步处理。
3. **容错**：客户端需处理 HTTP 状态码、JSON 解析错误与重试策略。
4. **模型特点**：仅保留最终输出字段，列表为不可变集合，便于安全共享。``


