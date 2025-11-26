# ODPS SQL Parser

多模块 ODPS SQL 解析与血缘分析套件，提供 AST 构建、语法校验、元数据/血缘抽取与可部署的 HTTP 服务，便于在数据质量、血缘治理或 SQL 分析平台中复用。

## 功能亮点

- **ANTLR4 解析内核**：完整支持 ODPS 方言，覆盖 SELECT 全量子句、CTE、窗口、集合操作、LATERAL VIEW 等高级特性。
- **元数据与血缘抽取**：内置 `OdpsSQLMetadataExtractor`，输出表/字段、JOIN、窗口、函数调用、注释等结构化结果，可追踪字段血缘。
- **语句校验与摘要**：`OdpsSQLMetadataEntrypoint` 统一封装解析、错误监听与摘要生成，便于在服务端直接调用。
- **可部署的 REST 服务**：`service` 模块基于 Vert.x 暴露血缘提取 API，支持 ODPS、Excel 等多种元数据连接器。
- **丰富文档与测试**：`parser`、`service` 模块各自提供深入 README 与单元测试，便于快速掌握实现细节。

## 仓库结构

```
odps-sql-parser/
├─ parser/       # 核心解析与元数据抽取库
├─ service/      # HTTP 服务 & 连接器
├─ docs/         # 逻辑校验、格式化等补充文档
├─ pom.xml       # 顶层 Maven 聚合项目
└─ LICENSE       # Apache-2.0
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+

### 构建与测试

```bash
mvn clean install           # 编译两大模块并运行测试
mvn -pl parser test         # 仅运行解析核心测试
mvn -pl service test        # 仅运行服务侧测试
```

### 使用解析核心

在任意 Maven 项目中引入 `parser` 模块产物后，可直接通过入口类完成解析与元数据抽取：

```
OdpsSQLMetadataExtractor extractor = new OdpsSQLMetadataExtractor(/* 可选自定义配置 */);
OdpsSQLMetadataEntrypoint entrypoint = new OdpsSQLMetadataEntrypoint(extractor);
OdpsSQLMetadataResult result = entrypoint.result(sql);
```

常用能力：

- `result.isSupported()`：判断是否为受支持的 SELECT 语句
- `result.isValid()`：是否通过语法/语义校验
- `result.getMetadata()`：获取表、字段、JOIN、窗口、函数、注释等详细元数据
- `result.formatSummary()`：生成面向用户的友好摘要

### 启动血缘服务

```
cd service
mvn clean package
java -cp target/service-*.jar com.sea.odps.service.api.LineageServerApp --port 8080
```

核心 REST 接口：

- `GET /health`：健康检查
- `POST /api/lineage/extract`：提取表/字段血缘
- `POST /api/lineage/tables`：仅表级血缘
- `POST /api/lineage/columns`：仅字段血缘

默认提供 `ODPS`、`EXCEL` 等连接器，实现快速对接线下元数据或线上 MaxCompute 项目。

## 模块概览

| 模块 | 说明 |
| --- | --- |
| `parser` | ANTLR4 grammar、语义模型、Visitor、元数据抽取与注释提取，详见 `parser/README.md`。 |
| `service` | 基于 Vert.x 的 HTTP API、血缘模型、连接器与校验逻辑，详见 `service/README.md`。 |

## 文档与资源

- `parser/README.md`：语法覆盖面、AST/元数据模型、注释提取策略、测试说明。
- `service/README.md`：HTTP API、连接器配置、模型对象示例。
- `docs/SQL_LOGICAL_VALIDATION*.md`：逻辑校验规则、使用方式与格式化说明。

## 贡献指南

1. Fork & 建立特性分支。
2. 保持 `mvn test` 通过，必要时补充/更新单元测试。
3. 遵循已有代码风格（Lombok、Guava 工具、Logback 日志）。
4. 提交 PR 前请确保语法文件、生成代码一致（`mvn clean generate-sources`）。

## 许可

项目在 [Apache License 2.0](./LICENSE) 下发布，可自由用于商业或开源场景，需保留版权与许可声明。
